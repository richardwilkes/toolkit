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

package com.trollworks.toolkit.io.server.websocket;

import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.io.server.Personality;
import com.trollworks.toolkit.utility.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * A {@link Personality} for handling Web Socket connections.<br>
 * <br>
 * A Web Socket Frame as described in <a href="https://tools.ietf.org/html/rfc6455#section-5.2">RFC
 * 6455. Sec 5.2</a>
 *
 * <pre>
 *    0                   1                   2                   3
 *    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *   +-+-+-+-+-------+-+-------------+-------------------------------+
 *   |F|R|R|R| opcode|M| Payload len |    Extended payload length    |
 *   |I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
 *   |N|V|V|V|       |S|             |   (if payload len==126/127)   |
 *   | |1|2|3|       |K|             |                               |
 *   +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
 *   |     Extended payload length continued, if payload len == 127  |
 *   + - - - - - - - - - - - - - - - +-------------------------------+
 *   |                               |Masking-key, if MASK set to 1  |
 *   +-------------------------------+-------------------------------+
 *   | Masking-key (continued)       |          Payload Data         |
 *   +-------------------------------- - - - - - - - - - - - - - - - +
 *   :                     Payload Data continued ...                :
 *   + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
 *   |                     Payload Data continued ...                |
 *   +---------------------------------------------------------------+
 * </pre>
 */
@SuppressWarnings("nls")
public class WebSocket extends Personality {
	public static final int			MAX_PAYLOAD_LENGTH	= 128 * 1024;
	private WebSocketHandler		mHandler;
	private boolean					mFinalFragment;
	private Opcode					mOpcode;
	private Opcode					mLastOpcode;
	private byte[]					mData;
	private int						mState;
	private long					mLength;
	private int						mCount;
	private byte[]					mMask;
	private ByteArrayOutputStream	mBuffer;
	private boolean					mClosed;

	/** @param handler The {@link WebSocketHandler} to delegate to. */
	public WebSocket(WebSocketHandler handler) {
		mHandler = handler;
		mBuffer = new ByteArrayOutputStream();
		mLastOpcode = Opcode.UNDEFINED;
	}

	@Override
	public String toString() {
		return "WebSocket";
	}

	/** Call when a Web Socket connection has started. */
	public void startConnection() {
		mHandler.webSocketConnected(this);
	}

	private void reset() {
		mFinalFragment = false;
		mOpcode = Opcode.UNDEFINED;
		mData = null;
		mState = 0;
		mLength = 0;
		mCount = 0;
		mMask = null;
	}

	@Override
	public void processInput(ByteBuffer buffer) throws IOException {
		while (buffer.hasRemaining()) {
			if (parse(buffer.get())) {
				switch (mOpcode) {
					case CONTINUATION:
						mBuffer.write(mData);
						if (mFinalFragment) {
							if (mLastOpcode == Opcode.TEXT) {
								mHandler.webSocketTextData(this, mBuffer.toString(Text.UTF8_ENCODING));
							} else {
								mHandler.webSocketBinaryData(this, mBuffer.toByteArray());
							}
							mBuffer.reset();
						}
						break;
					case TEXT:
						if (mFinalFragment) {
							mHandler.webSocketTextData(this, new String(mData, Text.UTF8_ENCODING));
							mBuffer.reset();
						} else {
							mLastOpcode = mOpcode;
							mBuffer.write(mData);
						}
						break;
					case BINARY:
						if (mFinalFragment) {
							mHandler.webSocketBinaryData(this, mData);
							mBuffer.reset();
						} else {
							mLastOpcode = mOpcode;
							mBuffer.write(mData);
						}
						break;
					case PING:
						int length = mData != null ? mData.length : 0;
						byte[] response = new byte[length];
						if (length > 0) {
							System.arraycopy(mData, 0, response, 0, length);
						}
						send(Opcode.PONG, response);
						break;
					case PONG:
						// Ignore
						break;
					case CLOSE:
						requestClose(false);
						return;
					default:
						Log.warn(getSession(), "Ignoring unknown WebSocket opcode: " + mOpcode.getOpcode());
						break;
				}
				reset();
			}
		}
	}

	private boolean parse(byte b) throws IOException {
		switch (mState) {
			case 0:
				if ((b & 0x70) != 0) {
					throw new IOException("Invalid reserved bits");
				}
				mFinalFragment = (b & 0x80) != 0;
				mOpcode = Opcode.lookup((byte) (b & 0x0F));
				if (mOpcode == Opcode.UNDEFINED) {
					throw new IOException("Unknown opcode: " + (b & 0x0F));
				}
				if (mOpcode.isControl() && !mFinalFragment) {
					throw new IOException("Fragmented control frame");
				}
				mState = 1;
				return false;
			case 1:
				if ((b & 0x80) != 0) {
					mMask = new byte[4];
				}
				mLength = b & 0x7F;
				if (mLength == 127) {
					mLength = 0;
					mState = 2;
				} else if (mLength == 126) {
					mLength = 0;
					mState = 3;
				} else {
					mData = new byte[(int) mLength];
					mState = mMask != null ? 4 : 5;
				}
				return false;
			case 2:
				mLength |= (b & 0xFF) << (7 - mCount) * 8;
				if (mLength > MAX_PAYLOAD_LENGTH) {
					throw new IOException("Payload length too large");
				}
				if (++mCount == 8) {
					mCount = 0;
					mData = new byte[(int) mLength];
					mState = mMask != null ? 4 : 5;
				}
				return false;
			case 3:
				mLength |= (b & 0xFF) << (1 - mCount) * 8;
				if (++mCount == 2) {
					mCount = 0;
					mData = new byte[(int) mLength];
					mState = mMask != null ? 4 : 5;
				}
				return false;
			case 4:
				mMask[mCount] = b;
				if (++mCount == mMask.length) {
					mCount = 0;
					mState = 5;
				}
				return false;
			case 5:
				if (mCount < mData.length) {
					if (mMask != null) {
						mData[mCount] = (byte) ((b ^ mMask[mCount % mMask.length]) & 0xFF);
					} else {
						mData[mCount] = b;
					}
					++mCount;
				}
				if (mCount == mData.length) {
					mState = 6;
					return true;
				}
				return false;
			default:
				throw new IOException("Read past end of frame");
		}
	}

	@Override
	public void closing() throws IOException {
		if (!mClosed) {
			mClosed = true;
			try {
				mHandler.webSocketClosed(this);
			} catch (Throwable throwable) {
				Log.error(getSession(), throwable);
			}
			send(Opcode.CLOSE, new byte[0]);
		}
	}

	/**
	 * Sends a text message to the remote end.
	 *
	 * @param msg The message to send.
	 */
	public final void send(String msg) {
		try {
			send(Opcode.TEXT, msg.getBytes(Text.UTF8_ENCODING));
		} catch (UnsupportedEncodingException exception) {
			Log.error(getSession(), exception);
		}
	}

	/**
	 * Sends a binary message to the remote end.
	 *
	 * @param data The data to send.
	 */
	public final void send(byte[] data) {
		send(Opcode.BINARY, data);
	}

	private final void send(Opcode opcode, byte[] data) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(10 + Math.min(data.length, MAX_PAYLOAD_LENGTH));
		int position = 0;
		int remaining = data.length;
		while (true) {
			boolean finalFragment = remaining <= MAX_PAYLOAD_LENGTH;
			int length = finalFragment ? remaining : MAX_PAYLOAD_LENGTH;
			baos.write((byte) (((finalFragment ? 0x80 : 0) | opcode.getOpcode()) & 0xFF));
			if (length < 126) {
				baos.write(length & 0xFF);
			} else if (length < 65536) {
				baos.write(126);
				baos.write(length >>> 8 & 0xFF);
				baos.write(length & 0xFF);
			} else {
				baos.write(127);
				for (int i = 56; i >= 0; i -= 8) {
					baos.write((int) ((long) length >>> i & 0xFF));
				}
			}
			if (length > 0) {
				baos.write(data, position, length);
				opcode = Opcode.CONTINUATION;
				position += length;
				remaining -= length;
			}
			if (remaining <= 0) {
				break;
			}
		}
		send(ByteBuffer.wrap(baos.toByteArray()));
	}

	static enum Opcode {
		UNDEFINED((byte) 0xFF, true),
		CONTINUATION((byte) 0, false),
		TEXT((byte) 1, false),
		BINARY((byte) 2, false),
		CLOSE((byte) 8, true),
		PING((byte) 9, true),
		PONG((byte) 10, true);

		private byte	mOpcode;
		private boolean	mIsControl;

		private Opcode(byte opcode, boolean isControl) {
			mOpcode = opcode;
			mIsControl = isControl;
		}

		final byte getOpcode() {
			return mOpcode;
		}

		final boolean isControl() {
			return mIsControl;
		}

		static final Opcode lookup(byte opcode) {
			for (Opcode one : values()) {
				if (opcode == one.mOpcode) {
					return one;
				}
			}
			return UNDEFINED;
		}
	}
}
