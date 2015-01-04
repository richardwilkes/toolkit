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
import com.trollworks.toolkit.io.server.Personality;
import com.trollworks.toolkit.io.server.Session;
import com.trollworks.toolkit.io.server.websocket.WebSocket;
import com.trollworks.toolkit.io.server.websocket.WebSocketFactory;
import com.trollworks.toolkit.utility.Text;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A {@link Personality} for handling HTTP connections. */
@SuppressWarnings("nls")
public class Http extends Personality {
	private static final int			MAXIMUM_HEADER_SIZE		= 8 * 1024;
	private static final int			MAXIMUM_CONTENT_SIZE	= 1024 * 1024;
	private static final Pattern		REQUEST_PATTERN			= Pattern.compile("^(\\S+)\\s+(\\S+)\\s+HTTP/(\\d+)\\.(\\d+)$");
	private HttpSessionFactory			mFactory;
	private int							mState;
	private int							mContentSize;
	private ByteArrayOutputStream		mBuffer					= new ByteArrayOutputStream(MAXIMUM_HEADER_SIZE);
	private byte[]						mBody;
	private String						mUri;
	private HttpMethod					mMethod;
	private int							mVersionMajor;
	private int							mVersionMinor;
	private Map<String, List<String>>	mParameters				= new HashMap<>();
	private Map<String, List<String>>	mHeaders				= new HashMap<>();

	/** @param factory The factory to use when creating new {@link Session}s. */
	public Http(HttpSessionFactory factory) {
		mFactory = factory;
	}

	@Override
	public String toString() {
		return "HTTP";
	}

	private void reset() {
		mState = 0;
		mContentSize = 0;
		mBuffer = new ByteArrayOutputStream(MAXIMUM_HEADER_SIZE);
		mBody = null;
		mUri = null;
		mMethod = null;
		mVersionMajor = 0;
		mVersionMinor = 0;
		mParameters = new HashMap<>();
		mHeaders = new HashMap<>();
	}

	@Override
	public void processInput(ByteBuffer buffer) throws IOException {
		while (buffer.hasRemaining()) {
			if (parse(buffer.get())) {
				processRequest();
				reset();
			}
		}
	}

	private void processRequest() throws IOException {
		try {
			Path rootPath = mFactory.getRootPath();
			Path path = rootPath.resolve("./" + mUri).toAbsolutePath().normalize();
			if (!path.startsWith(rootPath)) {
				throw new HttpResponseException(HttpStatusCode.FORBIDDEN, "FORBIDDEN: Outside of web scope");
			}

			if (hasHeader("upgrade")) {
				upgradeToWebSocket();
				return;
			}

			HttpRequestHandler handler = mFactory.getHttpHandler(mUri);
			if (handler != null) {
				handler.handleHttpRequest(this).send(this);
			} else {
				if (!Files.exists(path)) {
					throw new HttpResponseException(HttpStatusCode.NOT_FOUND, "File not found");
				}

				if (Files.isDirectory(path)) {
					if (!mUri.endsWith("/")) {
						HttpResponse response = new HttpResponse(HttpStatusCode.REDIRECT, MimeTypes.HTML, "<html><body>Redirected: <a href=\"" + mUri + "/\">" + mUri + "/</a></body></html>");
						response.addHeader("Location", mUri + "/");
						response.send(this);
						requestClose(false);
						return;
					}
					handler = mFactory.getHttpHandler(mUri + "index.html");
					if (handler != null) {
						handler.handleHttpRequest(this).send(this);
						closeIfNotKeepAlive();
						return;
					}
					Path newPath = path.resolve("index.html");
					if (Files.exists(newPath)) {
						path = newPath;
					} else {
						throw new HttpResponseException(HttpStatusCode.FORBIDDEN, "FORBIDDEN: No directory listings");
					}
				}

				try {
					String name = path.getFileName().toString();
					int dot = name.lastIndexOf('.');
					if (dot != -1 && dot + 1 < name.length()) {
						name = name.substring(dot + 1);
					}
					String mime = MimeTypes.lookup(name);
					long size = Files.size(path);
					HttpResponse response = new HttpResponse(HttpStatusCode.OK, mime, path);
					response.addHeader("Content-Length", Long.toString(size));
					response.setRequestMethod(mMethod);
					response.send(this);
				} catch (IOException ioe) {
					throw new HttpResponseException(HttpStatusCode.FORBIDDEN, "FORBIDDEN: Reading file failed");
				}
			}
			closeIfNotKeepAlive();
		} catch (SocketTimeoutException timeoutEx) {
			throw timeoutEx;
		} catch (HttpResponseException re) {
			HttpResponse response = new HttpResponse(re.getStatus(), MimeTypes.TEXT, re.getMessage());
			response.send(this);
			requestClose(false);
		} catch (IOException ioe) {
			HttpResponse response = new HttpResponse(HttpStatusCode.INTERNAL_ERROR, MimeTypes.TEXT, "INTERNAL ERROR: " + ioe.getMessage());
			response.send(this);
			requestClose(false);
		} catch (Exception exception) {
			Log.warn(getSession(), exception);
			HttpResponse response = new HttpResponse(HttpStatusCode.INTERNAL_ERROR, MimeTypes.TEXT, "INTERNAL ERROR: " + exception.getMessage());
			response.send(this);
			requestClose(false);
		}
	}

	private void closeIfNotKeepAlive() {
		if ("close".equals(getFirstHeader("connection"))) {
			requestClose(false);
		}
	}

	private void upgradeToWebSocket() throws IOException, NoSuchAlgorithmException {
		if (!"websocket".equalsIgnoreCase(getFirstHeader("upgrade"))) {
			throw new HttpResponseException(HttpStatusCode.BAD_REQUEST, "BAD REQUEST: Invalid upgrade request");
		}
		int version = getFirstHeaderAsInt("sec-websocket-version");
		if (version < 0) {
			version = getFirstHeaderAsInt("sec-websocket-draft");
		}
		if (version != 13) {
			HttpResponse response = new HttpResponse(HttpStatusCode.BAD_REQUEST, MimeTypes.TEXT, "Unsupported websocket version specification");
			response.addHeader("Sec-WebSocket-Version", "13");
			response.send(this);
			requestClose(false);
			return;
		}
		String key = getFirstHeader("sec-websocket-key");
		if (key == null) {
			throw new HttpResponseException(HttpStatusCode.BAD_REQUEST, "BAD REQUEST: Invalid key");
		}
		WebSocketFactory factory = mFactory.getWebSocketFactory(mUri);
		if (factory == null) {
			throw new HttpResponseException(HttpStatusCode.BAD_REQUEST, "BAD REQUEST: No handler");
		}
		WebSocket ws = factory.createWebSocket(this);
		getSession().setPersonality(ws);
		HttpResponse response = new HttpResponse(HttpStatusCode.SWITCHING_PROTOCOLS);
		response.addHeader("Upgrade", "WebSocket");
		response.addHeader("Connection", "Upgrade");
		MessageDigest md = MessageDigest.getInstance("SHA1");
		md.update((key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(Text.UTF8_ENCODING));
		response.addHeader("Sec-WebSocket-Accept", Base64.getEncoder().encodeToString(md.digest()));
		response.send(this);
		ws.startConnection();
	}

	private boolean parse(byte b) throws IOException {
		mBuffer.write(b & 0xFF);
		switch (mState) {
			case 0:
				if (b == '\r') {
					mState = 1;
				}
				break;
			case 1:
				if (b == '\n') {
					mState = 2;
				} else if (b != '\r') {
					mState = 0;
				}
				break;
			case 2:
				mState = b == '\r' ? 3 : 0;
				break;
			case 3:
				if (b == '\n') {
					mState = 4;
					parseHeaders();
					return mContentSize == 0;
				}
				mState = b == '\r' ? 1 : 0;
				break;
			case 4:
				if (mContentSize != -1 && mBuffer.size() == mContentSize) {
					parseBody();
					return true;
				}
				return false;
			default:
				throw new EOFException("Read past end of request");
		}
		if (mBuffer.size() > MAXIMUM_HEADER_SIZE) {
			throw new HttpResponseException(HttpStatusCode.BAD_REQUEST, "BAD REQUEST: Header too large");
		}
		return false;
	}

	private void parseHeaders() throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(mBuffer.toByteArray())));
		String line = in.readLine();
		if (line == null) {
			throw new SocketTimeoutException();
		}

		Matcher matcher = REQUEST_PATTERN.matcher(line);
		if (!matcher.find() || matcher.groupCount() != 4) {
			throw new HttpResponseException(HttpStatusCode.BAD_REQUEST, "BAD REQUEST");
		}

		mMethod = HttpMethod.lookup(matcher.group(1));
		if (mMethod == null) {
			throw new HttpResponseException(HttpStatusCode.BAD_REQUEST, "BAD REQUEST");
		}

		mUri = matcher.group(2);
		mVersionMajor = Integer.parseInt(matcher.group(3));
		mVersionMinor = Integer.parseInt(matcher.group(4));

		int index = mUri.indexOf('?');
		if (index != -1) {
			if (mUri.length() > index + 1) {
				decodeParameters(mUri.substring(index + 1));
			}
			mUri = mUri.substring(0, index);
		}
		mUri = decodePercent(mUri);

		line = in.readLine();
		while (line != null) {
			line = line.trim();
			if (line.isEmpty()) {
				break;
			}
			index = line.indexOf(':');
			if (index != -1) {
				String name = line.substring(0, index).trim().toLowerCase();
				List<String> list = mHeaders.get(name);
				if (list == null) {
					list = new ArrayList<>();
					mHeaders.put(name, list);
				}
				if (++index < line.length()) {
					list.add(line.substring(index).trim());
				}
			}
			line = in.readLine();
		}
		mBuffer.reset();
		try {
			mContentSize = Integer.parseInt(getFirstHeader("content-length"));
			if (mContentSize > 0) {
				if (mContentSize > MAXIMUM_CONTENT_SIZE) {
					throw new HttpResponseException(HttpStatusCode.BAD_REQUEST, "BAD REQUEST: Content too large");
				}
				if (mContentSize > MAXIMUM_HEADER_SIZE) {
					mBuffer = new ByteArrayOutputStream(mContentSize);
				}
			} else {
				mContentSize = 0;
			}
		} catch (Exception exception) {
			// No support for indeterminate content size for now, as I don't need it. Assume zero in
			// this case.
			mContentSize = 0;
		}
	}

	private void parseBody() throws IOException {
		mState = 5;
		mBody = mBuffer.toByteArray();
		mBuffer = null;
		if (HttpMethod.POST.equals(mMethod)) {
			if ("application/x-www-form-urlencoded".equals(getFirstHeader("content-type"))) {
				BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(mBody)));
				String line = in.readLine();
				while (line != null) {
					decodeParameters(line);
					line = in.readLine();
				}
			}
		}
	}

	private static String decodePercent(String str) {
		try {
			return URLDecoder.decode(str, Text.UTF8_ENCODING);
		} catch (UnsupportedEncodingException ignored) {
			// Ignore. Shouldn't be possible.
			return str;
		}
	}

	private void decodeParameters(String parameters) {
		StringTokenizer tokenizer = new StringTokenizer(parameters, "&");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			int index = token.indexOf('=');
			String name = decodePercent(index == -1 ? token : token.substring(0, index)).trim();
			List<String> list = mParameters.get(name);
			if (list == null) {
				list = new ArrayList<>();
				mParameters.put(name, list);
			}
			if (index != -1) {
				if (++index < token.length()) {
					list.add(decodePercent(token.substring(index)));
				}
			}
		}
	}

	/** @return The URI of the current request. */
	public final String getUri() {
		return mUri;
	}

	/** @return The {@link HttpMethod} that was used for the current request. */
	public final HttpMethod getMethod() {
		return mMethod;
	}

	/** @return The major version number of the HTTP protocol used for the current request. */
	public final int getVersionMajor() {
		return mVersionMajor;
	}

	/** @return The minor version number of the HTTP protocol used for the current request. */
	public final int getVersionMinor() {
		return mVersionMinor;
	}

	/** @return An {@link InputStream} containing the body of the current request. */
	public final InputStream getBody() {
		return new ByteArrayInputStream(mBody);
	}

	/** @return All HTTP parameters in the current request. */
	public final Map<String, List<String>> getParameters() {
		return mParameters;
	}

	/**
	 * @param name The name of a parameter to return.
	 * @return The value(s) for that parameter, or <code>null</code> if no parameter with that name
	 *         exists.
	 */
	public final List<String> getParameter(String name) {
		return mParameters.get(name);
	}

	/**
	 * @param name The name of a parameter to return.
	 * @return The first value for that parameter, or <code>null</code> if no parameter with that
	 *         name exists.
	 */
	public final String getFirstParameter(String name) {
		List<String> list = getParameter(name);
		if (list != null) {
			return list.isEmpty() ? "" : list.get(0);
		}
		return null;
	}

	/** @return All HTTP headers in the current request. */
	public final Map<String, List<String>> getHeaders() {
		return mHeaders;
	}

	/**
	 * @param name The name of a header to return.
	 * @return The value(s) for that header, or <code>null</code> if no header with that name
	 *         exists.
	 */
	public final List<String> getHeader(String name) {
		return mHeaders.get(name);
	}

	/**
	 * @param name The name of a header to check.
	 * @return <code>true</code> if the header exists in the current request.
	 */
	public final boolean hasHeader(String name) {
		return mHeaders.containsKey(name);
	}

	/**
	 * @param name The name of a header to return.
	 * @return The first value for that header, or <code>null</code> if no header with that name
	 *         exists.
	 */
	public final String getFirstHeader(String name) {
		List<String> list = getHeader(name);
		if (list != null) {
			return list.isEmpty() ? "" : list.get(0);
		}
		return null;
	}

	/**
	 * @param name The name of a header to return.
	 * @return The first value for that header, transformed into an integer, or <code>-1</code> if
	 *         no header with that name exists or it cannot be parsed as an integer.
	 */
	public final int getFirstHeaderAsInt(String name) {
		try {
			return Integer.parseInt(getFirstHeader(name));
		} catch (Exception exception) {
			return -1;
		}
	}

	@Override
	public void closing() {
		//
	}
}
