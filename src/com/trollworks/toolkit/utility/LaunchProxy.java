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

package com.trollworks.toolkit.utility;

import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.io.conduit.Conduit;
import com.trollworks.toolkit.io.conduit.ConduitMessage;
import com.trollworks.toolkit.io.conduit.ConduitReceiver;
import com.trollworks.toolkit.ui.GraphicsUtilities;
import com.trollworks.toolkit.ui.menu.file.OpenCommand;
import com.trollworks.toolkit.ui.menu.file.OpenDataFileCommand;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Provides the ability for an application to be launched from different terminals and still use
 * only a single instance.
 */
public class LaunchProxy implements ConduitReceiver {
	private static final String	AT					= "@";				//$NON-NLS-1$
	private static final String	SPACE				= " ";				//$NON-NLS-1$
	private static final String	COMMA				= ",";				//$NON-NLS-1$
	private static final String	AT_MARKER			= "@!";			//$NON-NLS-1$
	private static final String	SPACE_MARKER		= "@%";			//$NON-NLS-1$
	private static final String	COMMA_MARKER		= "@#";			//$NON-NLS-1$
	private static final String	LAUNCH_ID			= "Launched";		//$NON-NLS-1$
	private static final String	TOOK_OVER_FOR_ID	= "TookOverFor";	//$NON-NLS-1$
	private static LaunchProxy	INSTANCE			= null;
	private Conduit				mConduit;
	private long				mTimeStamp;
	private boolean				mReady;
	private ArrayList<File>		mFiles;

	/** @return The single instance of the app launch proxy. */
	public synchronized static LaunchProxy getInstance() {
		return INSTANCE;
	}

	/**
	 * Configures the one and only instance that may exist. It is an error to call this method more
	 * than once.
	 *
	 * @param files The files, if any, that should be passed on to another instance of the app that
	 *            may already be running.
	 */
	public synchronized static void configure(File... files) {
		if (INSTANCE == null) {
			INSTANCE = new LaunchProxy(files);
			try {
				// Give it a chance to terminate this run...
				Thread.sleep(1500);
			} catch (Exception exception) {
				// Ignore
			}
		} else {
			Log.error("Can only call configure once."); //$NON-NLS-1$
		}
	}

	private LaunchProxy(File... files) {
		StringBuilder buffer = new StringBuilder();
		mFiles = new ArrayList<>();
		mTimeStamp = System.currentTimeMillis();
		buffer.append(LAUNCH_ID);
		buffer.append(' ');
		buffer.append(mTimeStamp);
		buffer.append(' ');
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (i != 0) {
					buffer.append(',');
				}
				buffer.append(files[i].getAbsolutePath().replaceAll(AT, AT_MARKER).replaceAll(SPACE, SPACE_MARKER).replaceAll(COMMA, COMMA_MARKER));
			}
		}
		mConduit = new Conduit(this, false);
		mConduit.send(new ConduitMessage(BundleInfo.getDefault().getName(), buffer.toString()));
	}

	/**
	 * Sets whether this application is ready to take over responsibility for other copies being
	 * launched.
	 *
	 * @param ready Whether the application is ready or not.
	 */
	public void setReady(boolean ready) {
		mReady = ready;
	}

	@Override
	public void conduitMessageReceived(ConduitMessage msg) {
		StringTokenizer tokenizer = new StringTokenizer(msg.getMessage(), SPACE);
		if (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (mReady && LAUNCH_ID.equals(token)) {
				if (tokenizer.hasMoreTokens()) {
					long timeStamp = getLong(tokenizer);
					if (timeStamp != mTimeStamp) {
						mConduit.send(new ConduitMessage(BundleInfo.getDefault().getName(), TOOK_OVER_FOR_ID + ' ' + timeStamp));
						GraphicsUtilities.forceAppToFront();
						if (tokenizer.hasMoreTokens()) {
							tokenizer = new StringTokenizer(tokenizer.nextToken(), COMMA);
							while (tokenizer.hasMoreTokens()) {
								synchronized (mFiles) {
									mFiles.add(new File(tokenizer.nextToken().replaceAll(COMMA_MARKER, COMMA).replaceAll(SPACE_MARKER, SPACE).replaceAll(AT_MARKER, AT)));
								}
							}
							synchronized (mFiles) {
								if (!mFiles.isEmpty()) {
									for (File file : mFiles) {
										OpenDataFileCommand.open(file);
									}
									mFiles.clear();
								}
							}
						} else {
							EventQueue.invokeLater(() -> OpenCommand.open());
						}
					}
				}
			} else if (TOOK_OVER_FOR_ID.equals(token)) {
				if (tokenizer.hasMoreTokens()) {
					if (getLong(tokenizer) == mTimeStamp) {
						System.exit(0);
					}
				}
			}
		}
	}

	private static long getLong(StringTokenizer tokenizer) {
		try {
			return Long.parseLong(tokenizer.nextToken().trim());
		} catch (Exception exception) {
			return -1;
		}
	}

	@Override
	public String getConduitMessageIDFilter() {
		return BundleInfo.getDefault().getName();
	}

	@Override
	public String getConduitMessageUserFilter() {
		return System.getProperty("user.name"); //$NON-NLS-1$
	}
}
