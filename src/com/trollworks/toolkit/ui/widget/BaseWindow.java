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

package com.trollworks.toolkit.ui.widget;

import com.trollworks.toolkit.ui.GraphicsUtilities;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.WindowSizeEnforcer;
import com.trollworks.toolkit.ui.menu.file.QuitCommand;
import com.trollworks.toolkit.ui.menu.file.SaveCommand;
import com.trollworks.toolkit.ui.menu.file.Saveable;
import com.trollworks.toolkit.utility.Preferences;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/** The base window class for the application windows. */
public class BaseWindow extends JFrame implements WindowListener, WindowFocusListener {
	private static final String	WINDOW_PREFERENCES			= "WindowPrefs";	//$NON-NLS-1$
	private static final int	WINDOW_PREFERENCES_VERSION	= 3;
	private static final String	KEY_LOCATION				= "Location";		//$NON-NLS-1$
	private static final String	KEY_SIZE					= "Size";			//$NON-NLS-1$
	private static final String	KEY_MAXIMIZED				= "Maximized";		//$NON-NLS-1$
	private static final String	KEY_LAST_UPDATED			= "LastUpdated";	//$NON-NLS-1$
	boolean						mWasAlive;
	private boolean				mIsClosed;

	/**
	 * @param window The window to check.
	 * @return <code>true</code> if an owned window is showing.
	 */
	public static boolean hasOwnedWindowsShowing(Window window) {
		if (window != null) {
			for (Window one : window.getOwnedWindows()) {
				if (one.isShowing()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param <T> The window type.
	 * @param type The window type to return.
	 * @return A list of all windows of the specified type.
	 */
	public static <T extends BaseWindow> ArrayList<T> getWindows(Class<T> type) {
		ArrayList<T> windows = new ArrayList<>();
		Frame[] frames = Frame.getFrames();
		for (Frame element : frames) {
			if (type.isInstance(element)) {
				T window = type.cast(element);
				if (window.mWasAlive && !window.isClosed()) {
					windows.add(window);
				}
			}
		}
		return windows;
	}

	/**
	 * Creates a new, untitled window title.
	 *
	 * @param <T> The window type.
	 * @param windowClass The window class to use for name comparisons.
	 * @param baseTitle The base untitled name.
	 * @param exclude A window to exclude from naming decisions. May be <code>null</code>.
	 * @return The new window title.
	 */
	public static <T extends BaseWindow> String getNextUntitledWindowName(Class<T> windowClass, String baseTitle, BaseWindow exclude) {
		List<T> windows = getWindows(windowClass);
		int value = 0;
		String title;
		boolean again;

		do {
			again = false;
			title = baseTitle;
			if (++value > 1) {
				title += " " + value; //$NON-NLS-1$
			}
			for (T window : windows) {
				if (window != exclude && title.equals(window.getTitle())) {
					again = true;
					break;
				}
			}
		} while (again);
		return title;
	}

	/**
	 * Creates a new {@link BaseWindow}.
	 *
	 * @param title The title of the window.
	 * @param gc The {@link GraphicsConfiguration} to use.
	 */
	public BaseWindow(String title, GraphicsConfiguration gc) {
		super(title, gc);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLocationByPlatform(true);
		((JComponent) getContentPane()).setDoubleBuffered(true);
		getToolkit().setDynamicLayout(true);
		addWindowListener(this);
		addWindowFocusListener(this);
		WindowSizeEnforcer.monitor(this);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			mWasAlive = true;
		}
		super.setVisible(visible);
	}

	/**
	 * Invalidates the specified component and all of its children, recursively.
	 *
	 * @param comp The root component to start with.
	 */
	public void invalidate(Component comp) {
		comp.invalidate();
		comp.repaint();
		if (comp instanceof Container) {
			for (Component child : ((Container) comp).getComponents()) {
				invalidate(child);
			}
		}
	}

	/** @return <code>true</code> if the window has been closed. */
	public final boolean isClosed() {
		return mIsClosed;
	}

	@Override
	public void dispose() {
		if (!mIsClosed) {
			try {
				saveBounds();
				super.dispose();
			} catch (Exception ex) {
				// Necessary, since the AWT appears to sometimes spuriously try
				// to call Container.removeNotify() more than once on itself.
			}
			mIsClosed = true;
		}
	}

	@Override
	public void windowActivated(WindowEvent event) {
		// Unused
	}

	@Override
	public void windowClosed(WindowEvent event) {
		QuitCommand.INSTANCE.quitIfNoSignificantWindowsOpen();
	}

	@Override
	public void windowClosing(WindowEvent event) {
		if (!hasOwnedWindowsShowing(this)) {
			List<Saveable> saveables = new ArrayList<>();
			collectSaveables(this, saveables);
			if (SaveCommand.attemptSave(saveables)) {
				dispose();
			}
		}
	}

	private void collectSaveables(Component component, List<Saveable> saveables) {
		if (component instanceof Container) {
			Container container = (Container) component;
			int count = container.getComponentCount();
			for (int i = 0; i < count; i++) {
				collectSaveables(container.getComponent(i), saveables);
			}
		}
		if (component instanceof Saveable) {
			saveables.add((Saveable) component);
		}
	}

	@Override
	public void windowDeactivated(WindowEvent event) {
		UIUtilities.forceFocusToAccept();
	}

	@Override
	public void windowDeiconified(WindowEvent event) {
		// Unused
	}

	@Override
	public void windowIconified(WindowEvent event) {
		// Unused
	}

	@Override
	public void windowOpened(WindowEvent event) {
		// On windows, this is necessary to prevent the window from opening in the background.
		toFront();
	}

	@Override
	public void windowGainedFocus(WindowEvent event) {
		// Unused
	}

	@Override
	public void windowLostFocus(WindowEvent event) {
		UIUtilities.forceFocusToAccept();
	}

	/**
	 * @return The prefix for keys in the {@link #WINDOW_PREFERENCES}module for this window. If
	 *         <code>null</code> is returned from this method, then no standard window preferences
	 *         will be saved. Returns <code>null</code> by default.
	 */
	@SuppressWarnings("static-method")
	public String getWindowPrefsPrefix() {
		return null;
	}

	/**
	 * @return The preference object to use for saving and restoring standard window preferences.
	 *         Returns the result of calling {@link Preferences#getInstance()} by default.
	 */
	@SuppressWarnings("static-method")
	public Preferences getWindowPreferences() {
		return Preferences.getInstance();
	}

	/**
	 * Saves the window bounds to preferences. Preferences must be saved for this to have a lasting
	 * effect.
	 */
	public void saveBounds() {
		String keyPrefix = getWindowPrefsPrefix();
		if (keyPrefix != null) {
			Preferences prefs = getWindowPreferences();
			boolean wasMaximized = (getExtendedState() & MAXIMIZED_BOTH) != 0;
			if (wasMaximized || getExtendedState() == ICONIFIED) {
				setExtendedState(NORMAL);
			}
			prefs.startBatch();
			prefs.setValue(WINDOW_PREFERENCES, keyPrefix + KEY_LOCATION, getLocation());
			prefs.setValue(WINDOW_PREFERENCES, keyPrefix + KEY_SIZE, getSize());
			prefs.setValue(WINDOW_PREFERENCES, keyPrefix + KEY_MAXIMIZED, wasMaximized);
			prefs.setValue(WINDOW_PREFERENCES, keyPrefix + KEY_LAST_UPDATED, System.currentTimeMillis());
			prefs.endBatch();
		}
	}

	/** Restores the window to its saved location and size. */
	public void restoreBounds() {
		Preferences prefs = getWindowPreferences();
		prefs.resetIfVersionMisMatch(WINDOW_PREFERENCES, WINDOW_PREFERENCES_VERSION);
		pruneOldWindowPreferences(prefs);
		boolean needPack = true;
		String keyPrefix = getWindowPrefsPrefix();
		if (keyPrefix != null) {
			Point location = prefs.getPointValue(WINDOW_PREFERENCES, keyPrefix + KEY_LOCATION);
			if (location != null) {
				setLocation(location);
			}
			Dimension size = prefs.getDimensionValue(WINDOW_PREFERENCES, keyPrefix + KEY_SIZE);
			if (size != null) {
				setSize(size);
				needPack = false;
			}
		}
		if (needPack) {
			pack();
		}
		GraphicsUtilities.forceOnScreen(this);
		if (prefs.getBooleanValue(WINDOW_PREFERENCES, keyPrefix + KEY_MAXIMIZED, false)) {
			setExtendedState(MAXIMIZED_BOTH);
		}
	}

	private static void pruneOldWindowPreferences(Preferences prefs) {
		// 45 days ago, in milliseconds
		long cutoff = System.currentTimeMillis() - 1000L * 60L * 60L * 24L * 45L;
		List<String> keys = prefs.getModuleKeys(WINDOW_PREFERENCES);
		ArrayList<String> list = new ArrayList<>();
		for (String key : keys) {
			if (key.endsWith(KEY_LAST_UPDATED)) {
				if (prefs.getLongValue(WINDOW_PREFERENCES, key, 0) < cutoff) {
					list.add(key.substring(0, key.length() - KEY_LAST_UPDATED.length()));
				}
			}
		}
		if (!list.isEmpty()) {
			for (String key : keys) {
				for (String prefix : list) {
					if (key.startsWith(prefix)) {
						prefs.removePreference(WINDOW_PREFERENCES, key);
						break;
					}
				}
			}
		}
	}
}
