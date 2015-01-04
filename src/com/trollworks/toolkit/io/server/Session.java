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
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

/** Tracks a single connection to the server. */
@SuppressWarnings("nls")
public class Session implements Runnable, Log.Context {
	private static final AtomicInteger	NEXT_ID	= new AtomicInteger();
	private int							mId;
	private NioServer					mServer;
	private SocketChannel				mChannel;
	private InetAddress					mAddress;
	private Personality					mPersonality;
	private SSLSupport					mSSLSupport;
	private LinkedList<Request>			mRequests;
	private long						mLastActivity;
	private boolean						mInRequest;
	private boolean						mHasClosed;
	private boolean						mNoFurtherWrites;

	/**
	 * @param server The {@link NioServer} that will be providing the connection.
	 * @param channel The {@link SocketChannel} that was connected.
	 * @param sslContext The {@link SSLContext} to use. May be <code>null</code> if SSL is not
	 *            required.
	 * @param personality The {@link Personality} to use for processing the incoming data.
	 */
	public Session(NioServer server, SocketChannel channel, SSLContext sslContext, Personality personality) throws SSLException {
		mServer = server;
		mChannel = channel;
		mAddress = channel.socket().getInetAddress();
		mRequests = new LinkedList<>();
		mId = NEXT_ID.incrementAndGet();
		if (sslContext != null) {
			mSSLSupport = new SSLSupport(this, sslContext);
		}
		setPersonality(personality);
	}

	/**
	 * @return The last time there was activity on this {@link Session}. Compare with the result of
	 *         {@link System#currentTimeMillis()} to determine how much time has elapsed since the
	 *         last activity.
	 */
	public final long getLastActivity() {
		return mLastActivity;
	}

	/**
	 * Requests that the {@link Session} be closed.
	 *
	 * @param dueToError Pass in <code>true</code> if an error condition triggered this request.
	 */
	final void requestClose(boolean dueToError) {
		addRequest(new Request(dueToError));
	}

	/**
	 * Request that the specified input data be processed.
	 *
	 * @param buffer The data to process. The buffer contents are copied to an internal buffer, so
	 *            the passed in buffer may be modified after this call.
	 */
	final void requestHandleInput(ByteBuffer buffer) {
		addRequest(new Request(buffer));
	}

	private final void addRequest(Request request) {
		synchronized (mRequests) {
			mRequests.add(request);
		}
		mServer.scheduleSession(this);
	}

	/** Process a pending request. */
	final void processNextRequest() throws IOException {
		Request request = null;
		synchronized (mRequests) {
			if (mInRequest) {
				return;
			}
			mInRequest = true;
			if (!mRequests.isEmpty()) {
				request = mRequests.removeFirst();
			}
		}
		try {
			if (request != null && !mHasClosed) {
				mLastActivity = System.currentTimeMillis();
				if (request.isInput()) {
					ByteBuffer buffer = request.getBuffer();
					if (isSecure()) {
						buffer = mSSLSupport.processInput(buffer);
					}
					while (buffer.hasRemaining() && !Thread.currentThread().isInterrupted()) {
						getPersonality().processInput(buffer);
					}
				} else {
					mHasClosed = true;
					mNoFurtherWrites = request.isCloseRequestDueToError();
					try {
						getPersonality().closing();
					} catch (Throwable throwable) {
						Log.error(this, throwable);
					}
					Thread thread = new Thread(this, "Closing " + this);
					thread.setDaemon(true);
					thread.start();
				}
			}
		} finally {
			boolean reschedule;
			synchronized (mRequests) {
				mInRequest = false;
				reschedule = !mRequests.isEmpty();
			}
			if (reschedule) {
				mServer.scheduleSession(this);
			}
		}
	}

	/**
	 * @param buffer The data to send. A copy of the data is not made, so do not modify it once
	 *            passed to this method.
	 */
	final void send(ByteBuffer buffer) {
		mLastActivity = System.currentTimeMillis();
		if (isSecure()) {
			try {
				mSSLSupport.processOutput(buffer);
			} catch (Throwable throwable) {
				Log.error(this, throwable);
			}
		} else {
			mServer.send(mChannel, buffer);
		}
	}

	/** @return The associated {@link NioServer}. */
	public final NioServer getServer() {
		return mServer;
	}

	/** @return The associated {@link SocketChannel}. */
	public final SocketChannel getChannel() {
		return mChannel;
	}

	/** @return The associated {@link Personality}. */
	public final synchronized Personality getPersonality() {
		return mPersonality;
	}

	/**
	 * @param personality The {@link Personality} to set, replacing any existing {@link Personality}
	 *            attached to this {@link Session}.
	 */
	public final synchronized void setPersonality(Personality personality) {
		mPersonality = personality;
		mPersonality.setSession(this);
	}

	/** @return <code>true</code> if SSL support has been enabled for this {@link Session}. */
	public final boolean isSecure() {
		return mSSLSupport != null;
	}

	/** @return The address of the remote end of the connection. */
	public final InetAddress getAddress() {
		return mAddress;
	}

	/** @return A human-readable version of {@link #getAddress()}. */
	public final String getHumanReadableAddress() {
		return (isSecure() ? "S:" : "N:") + mAddress.getHostAddress();
	}

	@Override
	public final String toString() {
		return "Session " + mId + " (" + getHumanReadableAddress() + ")";
	}

	@Override
	public void run() {
		if (!mNoFurtherWrites) {
			try {
				// Wait for any pending writes to finish, but not forever
				long maxWait = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
				while (mServer.hasPendingWrite(mChannel) && System.currentTimeMillis() < maxWait) {
					Thread.sleep(50);
				}
			} catch (InterruptedException exception) {
				// Even if interrupted, we still want to try and close the channel
			}
		}
		try {
			mChannel.close();
		} catch (IOException ioe) {
			Log.error(this, ioe);
		}
		mServer.sessionClosed(this);
	}

	@Override
	public String getLogContext() {
		return getHumanReadableAddress();
	}

	private static class Request {
		private ByteBuffer	mBuffer;
		private boolean		mDueToError;

		Request(boolean dueToError) {
			// Close request
			mDueToError = dueToError;
		}

		Request(ByteBuffer buffer) {
			// Input request
			buffer.flip();
			mBuffer = ByteBuffer.allocate(buffer.limit());
			mBuffer.put(buffer);
			mBuffer.flip();
		}

		final boolean isInput() {
			return mBuffer != null;
		}

		final ByteBuffer getBuffer() {
			return mBuffer;
		}

		final boolean isCloseRequestDueToError() {
			return mDueToError;
		}
	}
}
