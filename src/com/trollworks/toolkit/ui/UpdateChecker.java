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

package com.trollworks.toolkit.ui;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.widget.WindowUtils;
import com.trollworks.toolkit.utility.BundleInfo;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.Preferences;
import com.trollworks.toolkit.utility.Version;
import com.trollworks.toolkit.utility.task.Tasks;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

/** Provides a background check for updates. */
public class UpdateChecker implements Runnable {
	@Localize("Checking for updates\u2026")
	@Localize(locale = "ru", value = "Проверка обновлений\u2026")
	@Localize(locale = "de", value = "Prüfe auf neue Version\u2026")
	@Localize(locale = "es", value = "Comprobando actulizaciones\u2026")
	private static String		CHECKING;
	@Localize("You have the most recent version")
	@Localize(locale = "ru", value = "У вас самая последняя версия")
	@Localize(locale = "de", value = "Programm ist aktuell")
	@Localize(locale = "es", value = "Ya tienes la versión más reciente")
	private static String		UP_TO_DATE;
	@Localize("A new version is available")
	@Localize(locale = "ru", value = "Доступна новая версия")
	@Localize(locale = "de", value = "Eine neue Version ist verfügbar")
	@Localize(locale = "es", value = "Hay una nueva versión disponible")
	private static String		OUT_OF_DATE;
	@Localize("Update")
	@Localize(locale = "ru", value = "Обновить")
	@Localize(locale = "de", value = "Aktualisieren")
	@Localize(locale = "es", value = "Actualizar")
	private static String		UPDATE_TITLE;
	@Localize("Ignore")
	@Localize(locale = "ru", value = "Игнорировать")
	@Localize(locale = "de", value = "Ignorieren")
	@Localize(locale = "es", value = "Ignorar")
	private static String		IGNORE_TITLE;

	static {
		Localization.initialize();
	}

	private static final String	MODULE					= "Updates";		//$NON-NLS-1$
	private static final String	LAST_VERSION_KEY		= "LastVersion";	//$NON-NLS-1$
	private static boolean		NEW_VERSION_AVAILABLE	= false;
	private static String		RESULT;
	private static String		UPDATE_URL;
	private String				mProductKey;
	private String				mCheckURL;
	private int					mMode;

	/**
	 * Initiates a check for updates.
	 *
	 * @param productKey The product key to check for.
	 * @param checkURL The URL to use for checking whether a new version is available.
	 * @param updateURL The URL to use when going to the update site.
	 */
	public static void check(String productKey, String checkURL, String updateURL) {
		Thread thread = new Thread(new UpdateChecker(productKey, checkURL), UpdateChecker.class.getSimpleName());
		UPDATE_URL = updateURL;
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.setDaemon(true);
		thread.start();
	}

	/** @return Whether a new version is available. */
	public static boolean isNewVersionAvailable() {
		return NEW_VERSION_AVAILABLE;
	}

	/** @return The result. */
	public static String getResult() {
		return RESULT;
	}

	/** Go to the update location on the web, if a new version is available. */
	public static void goToUpdate() {
		if (NEW_VERSION_AVAILABLE && Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI(UPDATE_URL));
			} catch (Exception exception) {
				WindowUtils.showError(null, exception.getMessage());
			}
		}
	}

	private UpdateChecker(String productKey, String checkURL) {
		mProductKey = productKey;
		mCheckURL = checkURL;
	}

	@Override
	public void run() {
		if (mMode == 0) {
			long currentVersion = BundleInfo.getDefault().getVersion();
			if (currentVersion == 0) {
				// Development version. Bail.
				mMode = 2;
				RESULT = UP_TO_DATE;
				return;
			}
			RESULT = CHECKING;
			long versionAvailable = currentVersion;
			mMode = 2;
			try {
				StringBuilder buffer = new StringBuilder(mCheckURL);
				try {
					NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
					if (!ni.isLoopback()) {
						if (ni.isUp()) {
							if (!ni.isVirtual()) {
								buffer.append('?');
								byte[] macAddress = ni.getHardwareAddress();
								for (byte one : macAddress) {
									buffer.append(Integer.toHexString(one >>> 4 & 0xF));
									buffer.append(Integer.toHexString(one & 0xF));
								}
							}
						}
					}
				} catch (Exception exception) {
					// Ignore. Means the code below is likely to fail, too.
				}
				URL url = new URL(buffer.toString());
				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
				String line = in.readLine();
				while (line != null) {
					StringTokenizer tokenizer = new StringTokenizer(line, "\t"); //$NON-NLS-1$
					if (tokenizer.hasMoreTokens()) {
						try {
							if (tokenizer.nextToken().equalsIgnoreCase(mProductKey)) {
								String token = tokenizer.nextToken();
								long version = Version.extract(token, 0);
								if (version > versionAvailable) {
									versionAvailable = version;
								}
							}
						} catch (Exception exception) {
							// Don't care
						}
					}
					line = in.readLine();
				}
			} catch (Exception exception) {
				// Don't care
			}
			if (versionAvailable > currentVersion) {
				Preferences prefs = Preferences.getInstance();
				NEW_VERSION_AVAILABLE = true;
				RESULT = OUT_OF_DATE;
				if (versionAvailable > prefs.getLongValue(MODULE, LAST_VERSION_KEY, BundleInfo.getDefault().getVersion())) {
					prefs.setValue(MODULE, LAST_VERSION_KEY, versionAvailable);
					prefs.save();
					mMode = 1;
					EventQueue.invokeLater(this);
					return;
				}
			} else {
				RESULT = UP_TO_DATE;
			}
		} else if (mMode == 1) {
			if (App.isNotificationAllowed()) {
				String result = getResult();
				mMode = 2;
				if (WindowUtils.showConfirmDialog(null, result, UPDATE_TITLE, JOptionPane.OK_CANCEL_OPTION, new String[] { UPDATE_TITLE, IGNORE_TITLE }, UPDATE_TITLE) == JOptionPane.OK_OPTION) {
					goToUpdate();
				}
			} else {
				Tasks.scheduleOnUIThread(this, 250, TimeUnit.MILLISECONDS, this);
			}
		}
	}
}
