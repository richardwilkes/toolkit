/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.io;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

public class FileScanner implements FileVisitor<Path> {
	private Path	mPath;
	private Handler	mHandler;
	private boolean	mSkipHidden;

	public static final void walk(Path path, Handler handler) {
		walk(path, handler, true);
	}

	public static final void walk(Path path, Handler handler, boolean skipHidden) {
		try {
			Files.walkFileTree(path, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new FileScanner(path, handler, skipHidden));
		} catch (Exception exception) {
			Log.error(exception);
		}
	}

	private FileScanner(Path path, Handler handler, boolean skipHidden) {
		mPath = path;
		mHandler = handler;
		mSkipHidden = skipHidden;
	}

	private boolean shouldSkip(Path path) {
		return mSkipHidden && !mPath.equals(path) && path.getFileName().toString().startsWith("."); //$NON-NLS-1$
	}

	@Override
	public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
		if (shouldSkip(path)) {
			return FileVisitResult.SKIP_SUBTREE;
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
		if (!shouldSkip(path)) {
			mHandler.processFile(path);
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path path, IOException exception) throws IOException {
		Log.error(exception);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path path, IOException exception) throws IOException {
		if (exception != null) {
			Log.error(exception);
		}
		return FileVisitResult.CONTINUE;
	}

	public interface Handler {
		void processFile(Path path) throws IOException;
	}
}
