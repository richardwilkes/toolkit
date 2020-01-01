/*
 * Copyright (c) 1998-2020 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui;

import com.trollworks.toolkit.ui.widget.WindowUtils;
import com.trollworks.toolkit.utility.BundleInfo;
import com.trollworks.toolkit.utility.I18n;
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
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;

/** Provides a background check for updates. */
public class UpdateChecker implements Runnable {
    private static final String  MODULE           = "Updates";
    private static final String  LAST_VERSION_KEY = "LastVersion";
    private static       boolean NEW_VERSION_AVAILABLE;
    private static       String  RESULT;
    private static       String  UPDATE_URL;
    private              String  mProductKey;
    private              String  mCheckURL;
    private              int     mMode;

    /**
     * Initiates a check for updates.
     *
     * @param productKey The product key to check for.
     * @param checkURL   The URL to use for checking whether a new version is available.
     * @param updateURL  The URL to use when going to the update site.
     */
    public static void check(String productKey, String checkURL, String updateURL) {
        Thread thread = new Thread(new UpdateChecker(productKey, checkURL), UpdateChecker.class.getSimpleName());
        synchronized (UpdateChecker.class) {
            UPDATE_URL = updateURL;
        }
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }

    /** @return Whether a new version is available. */
    public static synchronized boolean isNewVersionAvailable() {
        return NEW_VERSION_AVAILABLE;
    }

    /** @return The result. */
    public static synchronized String getResult() {
        return RESULT;
    }

    private static synchronized void setResult(String result) {
        RESULT = result;
    }

    /** Go to the update location on the web, if a new version is available. */
    public static void goToUpdate() {
        if (isNewVersionAvailable() && Desktop.isDesktopSupported()) {
            String updateURL;
            synchronized (UpdateChecker.class) {
                updateURL = UPDATE_URL;
            }
            if (updateURL != null) {
                try {
                    Desktop.getDesktop().browse(new URI(updateURL));
                } catch (Exception exception) {
                    WindowUtils.showError(null, exception.getMessage());
                }
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
                setResult(I18n.Text("You have the most recent version"));
                return;
            }
            setResult(I18n.Text("Checking for updates\u2026"));
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
                try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                    String line = in.readLine();
                    while (line != null) {
                        StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                        if (tokenizer.hasMoreTokens()) {
                            try {
                                if (tokenizer.nextToken().equalsIgnoreCase(mProductKey)) {
                                    String token   = tokenizer.nextToken();
                                    long   version = Version.extract(token, 0);
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
                }
            } catch (Exception exception) {
                // Don't care
            }
            if (versionAvailable > currentVersion) {
                Preferences prefs = Preferences.getInstance();
                synchronized (UpdateChecker.class) {
                    NEW_VERSION_AVAILABLE = true;
                }
                setResult(I18n.Text("A new version is available"));
                if (versionAvailable > prefs.getLongValue(MODULE, LAST_VERSION_KEY, BundleInfo.getDefault().getVersion())) {
                    prefs.setValue(MODULE, LAST_VERSION_KEY, versionAvailable);
                    prefs.save();
                    mMode = 1;
                    EventQueue.invokeLater(this);
                }
            } else {
                setResult(I18n.Text("You have the most recent version"));
            }
        } else if (mMode == 1) {
            if (App.isNotificationAllowed()) {
                mMode = 2;
                String update = I18n.Text("Update");
                if (WindowUtils.showConfirmDialog(null, getResult(), update, JOptionPane.OK_CANCEL_OPTION, new String[]{update, I18n.Text("Ignore")}, update) == JOptionPane.OK_OPTION) {
                    goToUpdate();
                }
            } else {
                Tasks.scheduleOnUIThread(this, 250, TimeUnit.MILLISECONDS, this);
            }
        }
    }
}
