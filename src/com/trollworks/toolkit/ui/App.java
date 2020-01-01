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

import com.trollworks.toolkit.ui.menu.edit.PreferencesCommand;
import com.trollworks.toolkit.ui.menu.file.OpenCommand;
import com.trollworks.toolkit.ui.menu.file.OpenDataFileCommand;
import com.trollworks.toolkit.ui.menu.file.PrintCommand;
import com.trollworks.toolkit.ui.menu.file.QuitCommand;
import com.trollworks.toolkit.ui.menu.help.AboutCommand;
import com.trollworks.toolkit.ui.widget.AppWindow;
import com.trollworks.toolkit.utility.BundleInfo;
import com.trollworks.toolkit.utility.LaunchProxy;
import com.trollworks.toolkit.utility.cmdline.CmdLine;
import com.trollworks.toolkit.workarounds.AppHome;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.awt.EventQueue;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.desktop.QuitStrategy;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.BitSet;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

/**
 * Stores application-specific data that the rest of the library requires. To operate correctly,
 * this information should be filled out as early as possible.
 */
public class App implements KeyEventDispatcher, Runnable {
    private static boolean                 NOTIFICATION_ALLOWED;
    private static Class<? extends JPanel> ABOUT_PANEL_CLASS;
    private static BitSet                  KEY_STATE = new BitSet();
    private        boolean                 mHasStarted;

    public static final void setup(Class<?> theClass) {
        AppHome.setup(theClass);
        BundleInfo.setDefault(new BundleInfo(theClass));
    }

    /** @return The application's 'home' directory. */
    public static final Path getHomePath() {
        return AppHome.get();
    }

    /** Creates a new {@link App}. */
    protected App() {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Action.APP_ABOUT)) {
                desktop.setAboutHandler(AboutCommand.INSTANCE);
            }
            if (desktop.isSupported(Action.APP_PREFERENCES)) {
                desktop.setPreferencesHandler(PreferencesCommand.INSTANCE);
            }
            if (desktop.isSupported(Action.APP_OPEN_FILE)) {
                desktop.setOpenFileHandler(OpenCommand.INSTANCE);
            }
            if (desktop.isSupported(Action.APP_PRINT_FILE)) {
                desktop.setPrintFileHandler(PrintCommand.INSTANCE);
            }
            if (desktop.isSupported(Action.APP_QUIT_HANDLER)) {
                desktop.setQuitHandler(QuitCommand.INSTANCE);
            }
            if (desktop.isSupported(Action.APP_QUIT_STRATEGY)) {
                desktop.setQuitStrategy(QuitStrategy.NORMAL_EXIT);
            }
            if (desktop.isSupported(Action.APP_SUDDEN_TERMINATION)) {
                desktop.disableSuddenTermination();
            }
        }
    }

    /** Set a global menu bar for when no windows are open / focused. */
    public static void setDefaultMenuBar(JMenuBar menuBar) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Action.APP_MENU_BAR)) {
                desktop.setDefaultMenuBar(menuBar);
            }
        }
    }

    /**
     * Must be called as early as possible and only once.
     *
     * @param cmdLine The command-line arguments. May be {@code null}.
     */
    public final void startup(CmdLine cmdLine) {
        if (mHasStarted) {
            System.err.println(getClass().getSimpleName() + ".startup(...) may only be called once.");
        } else {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
            configureApplication(cmdLine);
            LaunchProxy launchProxy = LaunchProxy.getInstance();
            if (launchProxy != null) {
                launchProxy.setReady(true);
            }
            if (cmdLine != null) {
                for (File file : cmdLine.getArgumentsAsFiles()) {
                    OpenDataFileCommand.open(file);
                }
            }
            EventQueue.invokeLater(this);
        }
    }

    /**
     * Called to configure the application and start any asynchronous startup tasks. This method
     * should return quickly. Does nothing by default.
     *
     * @param cmdLine The command-line arguments. May be {@code null}.
     */
    public void configureApplication(CmdLine cmdLine) {
        // Does nothing.
    }

    @Override
    public final synchronized void run() {
        OpenDataFileCommand.enablePassThrough();
        if (AppWindow.getAllAppWindows().isEmpty()) {
            noWindowsAreOpenAtStartup(false);
            if (AppWindow.getAllAppWindows().isEmpty()) {
                noWindowsAreOpenAtStartup(true);
            }
        }
        finalStartup();
        setNotificationAllowed(true);
    }

    /**
     * Called during startup when no windows have been opened yet. Does nothing by default.
     *
     * @param finalChance If this is the second time the notification was made.
     */
    public void noWindowsAreOpenAtStartup(boolean finalChance) {
        // Do nothing.
    }

    /**
     * Called once right before the final startup processing is completed. Does nothing by default.
     */
    public void finalStartup() {
        // Do nothing.
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getID() == KeyEvent.KEY_PRESSED) {
            KEY_STATE.set(event.getKeyCode());
        } else if (event.getID() == KeyEvent.KEY_RELEASED) {
            KEY_STATE.clear(event.getKeyCode());
        }
        return false;
    }

    /**
     * @param keyCode The key code to check for.
     * @return Whether the specified key code is currently pressed.
     */
    public static boolean isKeyPressed(int keyCode) {
        return KEY_STATE.get(keyCode);
    }

    /** @return A newly created about panel, or {@code null}. */
    public static JPanel createAboutPanel() {
        if (ABOUT_PANEL_CLASS != null) {
            try {
                return ABOUT_PANEL_CLASS.getDeclaredConstructor().newInstance();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    /** @param aboutPanelClass The class that will create the about panel. */
    public static void setAboutPanel(Class<? extends JPanel> aboutPanelClass) {
        ABOUT_PANEL_CLASS = aboutPanelClass;
    }

    /** @return Whether it is OK to put up a notification dialog yet. */
    public static boolean isNotificationAllowed() {
        return NOTIFICATION_ALLOWED;
    }

    /** @param allowed Whether it is OK to put up a notification dialog yet. */
    public static void setNotificationAllowed(boolean allowed) {
        NOTIFICATION_ALLOWED = allowed;
    }
}
