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

package com.trollworks.toolkit.io.server.http;

import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.utility.Text;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/** Stores a HTTP response for a request. */
@SuppressWarnings("nls")
public class HttpResponse {
	private static final SimpleDateFormat	GMT_DATE_FORMAT	= new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
	private static final String				LINE_ENDING		= "\r\n";
	private HttpStatusCode					mStatus;
	private String							mMimeType;
	private Object							mData;
	private Map<String, String>				mHeader			= new HashMap<>();
	private HttpMethod						mRequestMethod;

	static {
		GMT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Creates a new, empty {@link HttpResponse} with a type of {@link MimeTypes#TEXT} and a status
	 * of {@link HttpStatusCode#OK}.
	 */
	public HttpResponse() {
		this(HttpStatusCode.OK);
	}

	/**
	 * Creates a new, empty {@link HttpResponse} with a type of {@link MimeTypes#TEXT} and the
	 * specified status.
	 *
	 * @param status The {@link HttpStatusCode} to use.
	 */
	public HttpResponse(HttpStatusCode status) {
		this(status, MimeTypes.TEXT, null);
	}

	/**
	 * Creates a new {@link HttpResponse} with a type of {@link MimeTypes#HTML}, a status of
	 * {@link HttpStatusCode#OK}, and the specified message.
	 *
	 * @param msg The message to use.
	 */
	public HttpResponse(String msg) {
		this(HttpStatusCode.OK, MimeTypes.HTML, msg);
	}

	/**
	 * Creates a new {@link HttpResponse}.
	 *
	 * @param status The {@link HttpStatusCode} to use.
	 * @param mimeType The mime type to use.
	 * @param data The data for the body content. May be <code>null</code>.
	 */
	public HttpResponse(HttpStatusCode status, String mimeType, Object data) {
		mStatus = status;
		mMimeType = mimeType;
		mData = data;
	}

	/**
	 * Adds a header with the specified name and value, replacing any existing header with that
	 * name.
	 *
	 * @param name The name of the header.
	 * @param value The value of the header.
	 */
	public final void addHeader(String name, String value) {
		mHeader.put(name, value);
	}

	/** @return The {@link HttpStatusCode}. */
	public final HttpStatusCode getStatus() {
		return mStatus;
	}

	/** @param status The new {@link HttpStatusCode} to use. */
	public final void setStatus(HttpStatusCode status) {
		mStatus = status;
	}

	/** @return The mime type. */
	public final String getMimeType() {
		return mMimeType;
	}

	/** @param mimeType The new mime type to use. */
	public final void setMimeType(String mimeType) {
		mMimeType = mimeType;
	}

	/** @return The {@link HttpMethod} used. */
	public final HttpMethod getRequestMethod() {
		return mRequestMethod;
	}

	/** @param requestMethod The new {@link HttpMethod} to use. */
	public final void setRequestMethod(HttpMethod requestMethod) {
		mRequestMethod = requestMethod;
	}

	/**
	 * Formats this response appropriately for the HTTP protocol and sends it to the remote end.
	 *
	 * @param http The {@link Http} connection to send this response through.
	 */
	public final void send(Http http) {
		if (mStatus == null) {
			Log.error(http.getSession(), "sendResponse(): Status may not be null.");
		}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(16 * 1024);
			PrintWriter pw = new PrintWriter(baos);
			pw.print("HTTP/1.1 ");
			pw.print(mStatus.getDescription());
			pw.print(LINE_ENDING);

			if (mMimeType != null) {
				writeHeader(pw, "Content-Type", mMimeType);
			}

			if (mHeader == null || mHeader.get("Date") == null) {
				writeHeader(pw, "Date", GMT_DATE_FORMAT.format(new Date()));
			}

			if (mHeader != null) {
				for (String key : mHeader.keySet()) {
					writeHeader(pw, key, mHeader.get(key));
				}
			}

			if (mData != null) {
				writeHeader(pw, "Connection", "keep-alive");
				if (mData instanceof Path) {
					writeHeader(pw, "Content-Length", Long.toString(Files.size((Path) mData)));
				} else {
					if (mData instanceof ByteArrayOutputStream) {
						mData = ((ByteArrayOutputStream) mData).toByteArray();
					} else if (!(mData instanceof byte[])) {
						mData = mData.toString().getBytes(Text.UTF8_ENCODING);
					}
					writeHeader(pw, "Content-Length", Integer.toString(((byte[]) mData).length));
				}
			}

			pw.print(LINE_ENDING);
			pw.flush();

			if (mRequestMethod != HttpMethod.HEAD && mData != null) {
				if (mData instanceof Path) {
					try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream((Path) mData, StandardOpenOption.READ))) {
						byte[] buffer = new byte[16 * 1024];
						long pending = Files.size((Path) mData);
						while (pending > 0) {
							int read = in.read(buffer, 0, pending > buffer.length ? buffer.length : (int) pending);
							if (read <= 0) {
								break;
							}
							baos.write(buffer, 0, read);
							pending -= read;
						}
					}
				} else {
					baos.write((byte[]) mData);
				}
			}
			http.send(ByteBuffer.wrap(baos.toByteArray()));
		} catch (IOException exception) {
			// Ignore
		}
	}

	private static final void writeHeader(PrintWriter out, String name, String value) {
		out.print(name);
		out.print(": ");
		out.print(value);
		out.print(LINE_ENDING);
	}
}
