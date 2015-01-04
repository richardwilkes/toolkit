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

/** Some standard mime types and utilities. */
@SuppressWarnings("nls")
public class MimeTypes {
	public static final String	BINARY		= "application/octet-stream";
	public static final String	CSS			= "text/css";
	public static final String	MSWORD		= "application/msword";
	public static final String	FLV			= "video/x-flv";
	public static final String	GIF			= "image/gif";
	public static final String	HTML		= "text/html";
	public static final String	JAVA		= "text/x-java-source, text/java";
	public static final String	JPEG		= "image/jpeg";
	public static final String	JAVASCRIPT	= "application/javascript";
	public static final String	MOV			= "video/quicktime";
	public static final String	MP3			= "audio/mpeg";
	public static final String	MP3_URL		= "audio/mpeg-url";
	public static final String	MP4			= "video/mp4";
	public static final String	OGG			= "video/ogg";
	public static final String	PDF			= "application/pdf";
	public static final String	PNG			= "image/png";
	public static final String	SWF			= "application/x-shockwave-flash";
	public static final String	TEXT		= "text/plain";
	public static final String	XML			= "text/xml";

	/**
	 * @param extension The extension of the file.
	 * @return The suggested mime type. If nothing specific can be determined, then {@link #BINARY}
	 *         will be returned.
	 */
	public static final String lookup(String extension) {
		switch (extension.toLowerCase()) {
			case "css":
				return CSS;
			case "htm":
			case "html":
				return HTML;
			case "xml":
				return XML;
			case "java":
				return JAVA;
			case "txt":
			case "text":
			case "asc":
				return TEXT;
			case "gif":
				return GIF;
			case "jpg":
			case "jpeg":
				return JPEG;
			case "png":
				return PNG;
			case "mp3":
				return MP3;
			case "m3u":
				return MP3_URL;
			case "mp4":
				return MP4;
			case "ogv":
				return OGG;
			case "flv":
				return FLV;
			case "mov":
				return MOV;
			case "swf":
				return SWF;
			case "js":
				return JAVASCRIPT;
			case "pdf":
				return PDF;
			case "doc":
				return MSWORD;
			default:
				return BINARY;
		}
	}
}
