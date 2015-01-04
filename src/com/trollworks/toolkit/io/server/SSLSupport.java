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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/** Provides simple SSL processing. */
@SuppressWarnings("nls")
public class SSLSupport {
	private static final ByteBuffer	EMPTY_BUFFER	= ByteBuffer.allocate(0);
	private Session					mSession;
	private SSLEngine				mEngine;
	private SSLSession				mSSLSession;
	private ByteBuffer				mUnderflowData;
	private ByteBuffer				mAppData;
	private ByteBuffer				mInboundData;
	private ByteBuffer				mOutboundData;

	/**
	 * @param keyStore The location to load a valid SSL keystore from.
	 * @param password The password required to unlock the keystore.
	 * @return A {@link SSLContext} configured for use with the specified keystore.
	 */
	public static final SSLContext createContext(URL keyStore, String password) throws GeneralSecurityException, IOException {
		try (InputStream keyStoreIn = keyStore.openStream()) {
			KeyStore keystore = KeyStore.getInstance("JKS");
			char[] passwordArray = password.toCharArray();
			keystore.load(keyStoreIn, passwordArray);
			KeyManagerFactory keyMgrFactory = KeyManagerFactory.getInstance("SunX509");
			keyMgrFactory.init(keystore, passwordArray);
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(keyMgrFactory.getKeyManagers(), null, null);
			return context;
		}
	}

	/**
	 * @param session The {@link Session} to use when sending data.
	 * @param sslContext The {@link SSLContext} to use.
	 */
	public SSLSupport(Session session, SSLContext sslContext) throws SSLException {
		mSession = session;
		mEngine = sslContext.createSSLEngine();
		mEngine.setUseClientMode(false);
		mEngine.setNeedClientAuth(false);
		mSSLSession = mEngine.getSession();
		int applicationBufferSize = mSSLSession.getApplicationBufferSize();
		int packetBufferSize = mSSLSession.getPacketBufferSize();
		mAppData = ByteBuffer.allocate(applicationBufferSize);
		mOutboundData = ByteBuffer.allocate(packetBufferSize);
		mEngine.beginHandshake();
	}

	private void runSSLTasks() {
		Runnable task = null;
		while ((task = mEngine.getDelegatedTask()) != null) {
			task.run();
		}
	}

	private boolean canProceed() throws SSLException {
		while (true) {
			switch (mEngine.getHandshakeStatus()) {
				case NEED_TASK:
					runSSLTasks();
					break;
				case NEED_UNWRAP:
					switch (mEngine.unwrap(mInboundData, mAppData).getStatus()) {
						case BUFFER_OVERFLOW:
							resizeAppDataBuffer();
							break;
						case BUFFER_UNDERFLOW:
							return false;
						case CLOSED:
							throw new SSLException("Connection closed (unwrap)");
						default:
							break;
					}
					break;
				case NEED_WRAP:
					switch (mEngine.wrap(EMPTY_BUFFER, mOutboundData).getStatus()) {
						case BUFFER_UNDERFLOW:
							// Should not be possible
							throw new SSLException("Buffer underflow during handshake wrap");
						case CLOSED:
							throw new SSLException("Connection closed (wrap)");
						default:
							break;
					}
					sendOutboundData();
					break;
				default:
					return true;
			}
		}
	}

	private void resizeAppDataBuffer() {
		ByteBuffer resized = ByteBuffer.allocate(mAppData.capacity() + mSSLSession.getApplicationBufferSize());
		mAppData.flip();
		resized.put(mAppData);
		mAppData = resized;
	}

	private void preserveRemainingInboundData() {
		if (mInboundData.hasRemaining()) {
			mUnderflowData = ByteBuffer.allocate(mInboundData.remaining());
			mUnderflowData.put(mInboundData);
			mUnderflowData.flip();
		}
	}

	/**
	 * @param buffer The data to read.
	 * @return The unencrypted data. The returned buffer will be reused on subsequent calls to this
	 *         method, so callers will need to copy the data elsewhere if they wish to preserve it.
	 */
	public ByteBuffer processInput(ByteBuffer buffer) throws SSLException {
		mAppData.clear();
		mInboundData = buffer;
		insertUnderflowData();
		loop:
			while (mInboundData.hasRemaining() && canProceed()) {
				SSLEngineResult result = mEngine.unwrap(mInboundData, mAppData);
				switch (result.getStatus()) {
					case BUFFER_OVERFLOW:
						resizeAppDataBuffer();
						break;
					case BUFFER_UNDERFLOW:
					case CLOSED:
						break loop;
					default:
						break;
				}
			}
		preserveRemainingInboundData();
		mAppData.flip();
		return mAppData;
	}

	private void insertUnderflowData() {
		if (mUnderflowData != null) {
			ByteBuffer newBuffer = ByteBuffer.allocate(mUnderflowData.remaining() + mInboundData.remaining());
			newBuffer.put(mUnderflowData);
			newBuffer.put(mInboundData);
			newBuffer.flip();
			mInboundData = newBuffer;
			mUnderflowData = null;
		}
	}

	/**
	 * Sends the specified data to the underlying {@link Session} output after first encrypting it.
	 *
	 * @param buffer The data to send.
	 */
	public synchronized void processOutput(ByteBuffer buffer) throws SSLException {
		if (buffer.hasRemaining()) {
			do {
				SSLEngineResult result = mEngine.wrap(buffer, mOutboundData);
				switch (result.getHandshakeStatus()) {
					case NEED_TASK:
						runSSLTasks();
						break;
					case NEED_UNWRAP:
						// Should not be possible
						throw new SSLException("Need unwrap during output");
					case NEED_WRAP:
						// Should not be possible
						throw new SSLException("Need wrap during output");
					default:
						break;
				}
				switch (result.getStatus()) {
					case BUFFER_OVERFLOW:
						sendOutboundData();
						break;
					case BUFFER_UNDERFLOW:
						// Should not be possible
						throw new SSLException("Buffer underflow during output");
					case CLOSED:
						mOutboundData.clear();
						return;
					default:
						break;
				}
			} while (buffer.hasRemaining());
			sendOutboundData();
		}
	}

	private synchronized void sendOutboundData() {
		mOutboundData.flip();
		int limit = mOutboundData.limit();
		if (limit > 0) {
			ByteBuffer buffer = ByteBuffer.allocate(limit);
			buffer.put(mOutboundData);
			buffer.flip();
			mSession.getServer().send(mSession.getChannel(), buffer);
		}
		mOutboundData.clear();
	}
}
