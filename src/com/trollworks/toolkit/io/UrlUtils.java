/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is com.trollworks.toolkit.
 *
 * The Initial Developer of the Original Code is Richard A. Wilkes.
 * Portions created by the Initial Developer are Copyright (C) 1998-2014,
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */

package com.trollworks.toolkit.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.Manifest;

/** Utilities for working with URLs. */
public class UrlUtils {
	private static final String	MANIFEST_FILE	= "META-INF/MANIFEST.MF";	//$NON-NLS-1$

	/**
	 * @param uri The URI to setup a connection for.
	 * @return A {@link URLConnection} configured with a 10 second timeout for connecting and
	 *         reading data.
	 */
	public static final URLConnection setupConnection(String uri) throws IOException {
		return setupConnection(new URL(uri));
	}

	/**
	 * @param url The URL to setup a connection for.
	 * @return A {@link URLConnection} configured with a 10 second timeout for connecting and
	 *         reading data.
	 */
	public static final URLConnection setupConnection(URL url) throws IOException {
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		return connection;
	}

	/**
	 * @param uri The URI to retrieve.
	 * @return The contents of what the URI points to.
	 */
	public static final String get(String uri) throws IOException {
		return get(new URL(uri));
	}

	/**
	 * @param url The URL to retrieve.
	 * @return The contents of what the URL points to.
	 */
	public static final String get(URL url) throws IOException {
		StringBuilder buffer = new StringBuilder();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(setupConnection(url).getInputStream()))) {
			String line;
			while ((line = in.readLine()) != null) {
				if (buffer.length() > 0) {
					buffer.append('\n');
				}
				buffer.append(line);
			}
		}
		return buffer.toString();
	}

	/**
	 * @param theClass A {@link Class} in the bundle you wish to load the manifest from.
	 * @return The loaded {@link Manifest}.
	 */
	public static final Manifest loadManifest(Class<?> theClass) throws IOException, URISyntaxException {
		URI uri = theClass.getProtectionDomain().getCodeSource().getLocation().toURI();
		File file = new File(uri.getPath());
		URL url = file.isDirectory() ? uri.resolve(MANIFEST_FILE).toURL() : new URL("jar:" + uri.toASCIIString() + "!/" + MANIFEST_FILE); //$NON-NLS-1$ //$NON-NLS-2$
		try (InputStream in = setupConnection(url).getInputStream()) {
			return new Manifest(in);
		}
	}
}
