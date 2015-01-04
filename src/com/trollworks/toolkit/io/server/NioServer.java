/*
 * Copyright (c) 1998-2015 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.io.server;

import com.trollworks.toolkit.io.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;

/** The core non-blocking i/o server. */
public class NioServer extends Thread {
	private Selector									mSelector;
	private List<ChangeRequest>							mPendingChanges		= new LinkedList<>();
	private Map<SocketChannel, LinkedList<ByteBuffer>>	mPendingWriteData	= new HashMap<>();
	private ByteBuffer									mReadBuffer			= ByteBuffer.allocate(8192);
	private LinkedBlockingQueue<Session>				mQueue				= new LinkedBlockingQueue<>();
	private List<NioWorker>								mWorkers			= new ArrayList<>();
	private Set<Session>								mSessions			= new HashSet<>();
	private SSLContext									mSSLContext;
	private TimeoutMonitor								mTimeoutMonitor;

	/**
	 * @param sslContext The {@link SSLContext} to use. Typically created by calling
	 *            {@link SSLSupport#createContext(URL, String)}.
	 */
	public NioServer(SSLContext sslContext) throws IOException {
		setName(getClass().getSimpleName());
		setDaemon(true);
		mSSLContext = sslContext;
		mSelector = SelectorProvider.provider().openSelector();
		int count = Runtime.getRuntime().availableProcessors() + 1;
		for (int i = 0; i < count; i++) {
			NioWorker worker = new NioWorker(mQueue);
			worker.start();
			mWorkers.add(worker);
		}
		mTimeoutMonitor = new TimeoutMonitor(TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES));
		mTimeoutMonitor.start();
	}

	/** @return The {@link SSLContext} to use with this server. */
	public final SSLContext getSSLContext() {
		return mSSLContext;
	}

	/** @return The current {@link Session}s. */
	public final List<Session> getSessions() {
		synchronized (mSessions) {
			return new ArrayList<>(mSessions);
		}
	}

	/** Called when a session is closed. */
	final void sessionClosed(Session session) {
		synchronized (mSessions) {
			mSessions.remove(session);
		}
	}

	/** Call to shutdown the server. */
	public final void shutdown() {
		try {
			mSelector.close();
		} catch (IOException exception) {
			Log.error(exception);
		}
		try {
			join();
		} catch (InterruptedException exception) {
			// Ignore
		}
	}

	/**
	 * @param hostAddress The address to listen on. Pass in <code>null</code> to indicate all
	 *            addresses.
	 * @param port The port to listen on.
	 * @param sessionFactory The {@link SessionFactory} to use for new connections.
	 */
	public final void listen(InetAddress hostAddress, int port, SessionFactory sessionFactory) throws IOException {
		@SuppressWarnings("resource")
		ServerSocketChannel channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.socket().bind(new InetSocketAddress(hostAddress, port));
		channel.register(mSelector, SelectionKey.OP_ACCEPT, sessionFactory);
	}

	@Override
	public final void run() {
		while (true) {
			try {
				// Adjust what we're waiting on
				synchronized (mPendingChanges) {
					for (ChangeRequest request : mPendingChanges) {
						try {
							request.mSocket.keyFor(mSelector).interestOps(request.mOperation);
						} catch (Exception exception) {
							// Ignore
						}
					}
					mPendingChanges.clear();
				}

				// Wait for some work
				mSelector.select();

				// Handle the work
				Iterator<SelectionKey> keys = mSelector.selectedKeys().iterator();
				while (keys.hasNext()) {
					SelectionKey key = keys.next();
					keys.remove();
					if (key.isValid()) {
						if (key.isAcceptable()) {
							accept(key);
						} else if (key.isReadable()) {
							read(key);
						} else if (key.isWritable()) {
							write(key);
						}
					}
				}
			} catch (CancelledKeyException cke) {
				// Ignore
			} catch (ClosedSelectorException cse) {
				// Allow the work queue to drain
				while (!mQueue.isEmpty()) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException exception) {
						break;
					}
				}
				// Shutdown the workers
				for (NioWorker worker : mWorkers) {
					worker.interrupt();
					try {
						worker.join();
					} catch (InterruptedException ie) {
						break;
					}
				}
				break;
			} catch (Throwable throwable) {
				Log.error(throwable);
			}
		}
	}

	/**
	 * Puts the data into the send queue.
	 *
	 * @param socket The socket to send data through.
	 * @param data The data to send. A copy of the data is not made, so do not modify it once passed
	 *            to this method.
	 */
	final void send(SocketChannel socket, ByteBuffer data) {
		synchronized (mPendingWriteData) {
			LinkedList<ByteBuffer> list = mPendingWriteData.get(socket);
			if (list == null) {
				list = new LinkedList<>();
				mPendingWriteData.put(socket, list);
			}
			list.add(data);
		}
		synchronized (mPendingChanges) {
			mPendingChanges.add(new ChangeRequest(socket, SelectionKey.OP_WRITE));
		}
		mSelector.wakeup();
	}

	/**
	 * @param socket The socket to check.
	 * @return <code>true</code> if there is data waiting to be sent on the specified socket.
	 */
	public final boolean hasPendingWrite(SocketChannel socket) {
		synchronized (mPendingWriteData) {
			LinkedList<ByteBuffer> list = mPendingWriteData.get(socket);
			return list != null && !list.isEmpty();
		}
	}

	private final void accept(SelectionKey key) throws IOException {
		@SuppressWarnings("resource")
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		@SuppressWarnings("resource")
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);
		Session session = ((SessionFactory) key.attachment()).createSession(this, socketChannel);
		synchronized (mSessions) {
			mSessions.add(session);
		}
		socketChannel.register(mSelector, SelectionKey.OP_READ, session);
	}

	private final void read(SelectionKey key) {
		@SuppressWarnings("resource")
		SocketChannel socketChannel = (SocketChannel) key.channel();
		Session session = (Session) key.attachment();
		mReadBuffer.clear();
		int amount;
		try {
			amount = socketChannel.read(mReadBuffer);
			if (amount > 0) {
				session.requestHandleInput(mReadBuffer);
			} else if (amount == -1) {
				session.requestClose(false);
			}
		} catch (Throwable throwable) {
			session.requestClose(true);
		}
	}

	/**
	 * Adds the specified {@link Session} to the queue to be worked on.
	 *
	 * @param session The {@link Session} to schedule.
	 */
	final void scheduleSession(Session session) {
		mQueue.add(session);
	}

	@SuppressWarnings("resource")
	private final void write(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		synchronized (mPendingWriteData) {
			LinkedList<ByteBuffer> list = mPendingWriteData.get(socketChannel);
			if (list == null || list.isEmpty()) {
				key.interestOps(SelectionKey.OP_READ);
			} else {
				ByteBuffer buffer = list.getFirst();
				try {
					socketChannel.write(buffer);
				} catch (IOException ioe) {
					mPendingWriteData.remove(socketChannel);
					key.interestOps(SelectionKey.OP_READ);
					return;
				}
				if (buffer.remaining() == 0) {
					list.removeFirst();
					if (list.isEmpty()) {
						key.interestOps(SelectionKey.OP_READ);
					}
				}
			}
		}
	}

	private static class ChangeRequest {
		final SocketChannel	mSocket;
		final int			mOperation;

		ChangeRequest(SocketChannel socket, int operation) {
			mSocket = socket;
			mOperation = operation;
		}
	}

	private static class NioWorker extends Thread {
		private static final AtomicInteger		NEXT_ID	= new AtomicInteger();
		private LinkedBlockingQueue<Session>	mQueue;

		NioWorker(LinkedBlockingQueue<Session> queue) {
			mQueue = queue;
			setName("NioWorker " + NEXT_ID.incrementAndGet()); //$NON-NLS-1$
			setDaemon(true);
		}

		@Override
		public void run() {
			while (!isInterrupted()) {
				Session session = null;
				try {
					session = mQueue.take();
					session.processNextRequest();
				} catch (InterruptedException iex) {
					break;
				} catch (Throwable throwable) {
					Log.error(session, throwable);
					if (session != null) {
						session.requestClose(true);
					}
				}
			}
		}
	}

	private class TimeoutMonitor extends Thread {
		private long	mTimeout;

		TimeoutMonitor(long timeoutMilliseconds) {
			setName(getClass().getSimpleName());
			setDaemon(true);
			mTimeout = timeoutMilliseconds;
		}

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(Math.max(mTimeout / 4, 1000));
					long timeout = System.currentTimeMillis() - mTimeout;
					for (Session session : getSessions()) {
						if (session.getLastActivity() <= timeout) {
							session.requestClose(false);
						}
					}
				} catch (InterruptedException exception) {
					break;
				}
			}
		}
	}
}
