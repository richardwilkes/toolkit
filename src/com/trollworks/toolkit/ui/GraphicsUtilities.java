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

import com.apple.eawt.Application;
import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.image.StdImageSet;
import com.trollworks.toolkit.ui.widget.AppWindow;
import com.trollworks.toolkit.utility.Geometry;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.Platform;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.UIManager;

/** Provides general graphics settings and manipulation. */
public class GraphicsUtilities {
	@Localize("There is no valid graphics display.")
	@Localize(locale = "ru", value = "Нет доступного графического дисплея")
	@Localize(locale = "de", value = "Kein gültiges Grafikausgabegerät gefunden.")
	@Localize(locale = "es", value = "No hay monitor válido disponible")
	private static String			HEADLESS;

	static {
		Localization.initialize();
	}

	private static Frame			HIDDEN_FRAME					= null;
	private static int				HIDDEN_FRAME_ICONSET_SEQUENCE	= -1;
	private static boolean			HEADLESS_PRINT_MODE				= false;
	private static int				HEADLESS_CHECK_RESULT			= 0;
	private static boolean			OK_TO_USE_FULLSCREEN_TRICK		= true;
	private static BufferedImage	FALLBACK_GRAPHICS_BACKING_STORE	= null;

	/** @return Whether the headless print mode is enabled. */
	public static boolean inHeadlessPrintMode() {
		return HEADLESS_PRINT_MODE;
	}

	/** @param inHeadlessPrintMode Whether the headless print mode is enabled. */
	public static void setHeadlessPrintMode(boolean inHeadlessPrintMode) {
		HEADLESS_PRINT_MODE = inHeadlessPrintMode;
	}

	/**
	 * Looks for the screen device that contains the largest part of the specified window.
	 *
	 * @param window The window to determine the preferred screen device for.
	 * @return The preferred screen device.
	 */
	public static GraphicsDevice getPreferredScreenDevice(Window window) {
		return getPreferredScreenDevice(window.getBounds());
	}

	/**
	 * Looks for the screen device that contains the largest part of the specified global bounds.
	 *
	 * @param bounds The global bounds to determine the preferred screen device for.
	 * @return The preferred screen device.
	 */
	public static GraphicsDevice getPreferredScreenDevice(Rectangle bounds) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice best = ge.getDefaultScreenDevice();
		Rectangle overlapBounds = Geometry.intersection(bounds, best.getDefaultConfiguration().getBounds());
		int bestOverlap = overlapBounds.width * overlapBounds.height;

		for (GraphicsDevice gd : ge.getScreenDevices()) {
			if (gd.getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
				overlapBounds = Geometry.intersection(bounds, gd.getDefaultConfiguration().getBounds());
				if (overlapBounds.width * overlapBounds.height > bestOverlap) {
					best = gd;
				}
			}
		}
		return best;
	}

	/**
	 * @param component The {@link JComponent} to work with.
	 * @return The local, inset, bounds of the specified {@link JComponent}.
	 */
	public static Rectangle getLocalInsetBounds(JComponent component) {
		Insets insets = component.getInsets();
		return new Rectangle(insets.left, insets.top, component.getWidth() - (insets.left + insets.right), component.getHeight() - (insets.top + insets.bottom));
	}

	/** @return The maximum bounds that fits on the main screen. */
	public static Rectangle getMaximumWindowBounds() {
		return getMaximumWindowBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds());
	}

	/**
	 * Determines the screen that most contains the specified window and returns the maximum size
	 * the window can be on that screen.
	 *
	 * @param window The window to determine a maximum bounds for.
	 * @return The maximum bounds that fits on a screen.
	 */
	public static Rectangle getMaximumWindowBounds(Window window) {
		return getMaximumWindowBounds(window.getBounds());
	}

	/**
	 * Determines the screen that most contains the specified panel area and returns the maximum
	 * size a window can be on that screen.
	 *
	 * @param panel The panel that contains the area.
	 * @param area The area within the panel to use when determining the maximum bounds for a
	 *            window.
	 * @return The maximum bounds that fits on a screen.
	 */
	public static Rectangle getMaximumWindowBounds(Component panel, Rectangle area) {
		area = new Rectangle(area);
		UIUtilities.convertRectangleToScreen(area, panel);
		return getMaximumWindowBounds(area);
	}

	/**
	 * Determines the screen that most contains the specified global bounds and returns the maximum
	 * size a window can be on that screen.
	 *
	 * @param bounds The global bounds to use when determining the maximum bounds for a window.
	 * @return The maximum bounds that fits on a screen.
	 */
	public static Rectangle getMaximumWindowBounds(Rectangle bounds) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = getPreferredScreenDevice(bounds);

		if (gd == ge.getDefaultScreenDevice()) {
			bounds = ge.getMaximumWindowBounds();
			// The Mac (and now Windows as of Java 5) already return the correct
			// value... try to fix it up for the other platforms. This doesn't
			// currently work, either, since the other platforms seem to always
			// return empty insets.
			if (!Platform.isMacintosh() && !Platform.isWindows()) {
				Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());

				// Since this is failing to do the right thing anyway, we're going
				// to try and come up with some reasonable limitations...
				if (insets.top == 0 && insets.bottom == 0) {
					insets.bottom = 48;
				}

				bounds.x += insets.left;
				bounds.y += insets.top;
				bounds.width -= insets.left + insets.right;
				bounds.height -= insets.top + insets.bottom;
			}
			return bounds;
		}
		return gd.getDefaultConfiguration().getBounds();
	}

	/**
	 * Forces the specified window onscreen.
	 *
	 * @param window The window to force onscreen.
	 */
	public static void forceOnScreen(Window window) {
		Rectangle maxBounds = getMaximumWindowBounds(window);
		Rectangle bounds = window.getBounds();
		Point location = new Point(bounds.x, bounds.y);
		Dimension size = window.getMinimumSize();

		if (bounds.width < size.width) {
			bounds.width = size.width;
		}
		if (bounds.height < size.height) {
			bounds.height = size.height;
		}

		if (bounds.x < maxBounds.x) {
			bounds.x = maxBounds.x;
		} else if (bounds.x >= maxBounds.x + maxBounds.width) {
			bounds.x = maxBounds.x + maxBounds.width - 1;
		}

		if (bounds.x + bounds.width >= maxBounds.x + maxBounds.width) {
			bounds.x = maxBounds.x + maxBounds.width - bounds.width;
			if (bounds.x < maxBounds.x) {
				bounds.x = maxBounds.x;
				bounds.width = maxBounds.width;
			}
		}

		if (bounds.y < maxBounds.y) {
			bounds.y = maxBounds.y;
		} else if (bounds.y >= maxBounds.y + maxBounds.height) {
			bounds.y = maxBounds.y + maxBounds.height - 1;
		}

		if (bounds.y + bounds.height >= maxBounds.y + maxBounds.height) {
			bounds.y = maxBounds.y + maxBounds.height - bounds.height;
			if (bounds.y < maxBounds.y) {
				bounds.y = maxBounds.y;
				bounds.height = maxBounds.height;
			}
		}

		if (location.x != bounds.x || location.y != bounds.y) {
			window.setBounds(bounds);
		} else {
			window.setSize(bounds.width, bounds.height);
		}
		window.validate();
	}

	/** Forces a full repaint of all windows, disposing of any window buffers. */
	public static void forceRepaint() {
		for (AppWindow window : AppWindow.getAllWindows()) {
			window.repaint();
		}
	}

	/** Forces a full repaint and invalidate on all windows, disposing of any window buffers. */
	public static void forceRepaintAndInvalidate() {
		for (AppWindow window : AppWindow.getAllWindows()) {
			window.invalidate(window.getRootPane());
		}
	}

	/**
	 * @param gc The {@link Graphics} to prepare for use.
	 * @return The passed-in {@link Graphics2D}.
	 */
	public static Graphics2D prepare(Graphics gc) {
		Graphics2D g2d = (Graphics2D) gc;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		return g2d;
	}

	/**
	 * @param gc The {@link Graphics2D} to configure.
	 * @return The {@link RenderingHints} as they were prior to this call.
	 */
	public static RenderingHints setMaximumQualityForGraphics(Graphics2D gc) {
		RenderingHints saved = (RenderingHints) gc.getRenderingHints().clone();
		gc.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gc.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		gc.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		gc.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		return saved;
	}

	/**
	 * @return A graphics context obtained by looking for an existing window and asking it for a
	 *         graphics context.
	 */
	public static Graphics2D getGraphics() {
		Frame frame = AppWindow.getTopWindow();
		Graphics2D g2d = frame == null ? null : (Graphics2D) frame.getGraphics();

		if (g2d == null) {
			Frame[] frames = Frame.getFrames();

			for (Frame element : frames) {
				if (element.isDisplayable()) {
					g2d = (Graphics2D) element.getGraphics();
					if (g2d != null) {
						return g2d;
					}
				}
			}
			if (FALLBACK_GRAPHICS_BACKING_STORE == null) {
				FALLBACK_GRAPHICS_BACKING_STORE = new BufferedImage(32, 1, BufferedImage.TYPE_INT_ARGB);
			}
			return FALLBACK_GRAPHICS_BACKING_STORE.createGraphics();
		}
		return prepare(g2d);
	}

	/** @return A {@link Frame} to use when a valid frame of any sort is all that is needed. */
	public static Frame getAnyFrame() {
		Frame frame = AppWindow.getTopWindow();

		if (frame == null) {
			Frame[] frames = Frame.getFrames();

			for (Frame element : frames) {
				if (element.isDisplayable()) {
					return element;
				}
			}
			return getHiddenFrame(true);
		}
		return frame;
	}

	/** @param ok Whether using the momentary fullscreen window trick is OK or not. */
	public static void setOKToUseFullScreenTrick(boolean ok) {
		OK_TO_USE_FULLSCREEN_TRICK = ok;
	}

	/** Attempts to force the app to the front. */
	public static void forceAppToFront() {
		if (Platform.isMacintosh()) {
			Application.getApplication().requestForeground(true);
		} else if (OK_TO_USE_FULLSCREEN_TRICK) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gd = ge.getDefaultScreenDevice();
			AppWindow window = new AppWindow();
			window.setUndecorated(true);
			window.getContentPane().setBackground(new Color(0, 0, 0, 0));
			gd.setFullScreenWindow(window);
			gd.setFullScreenWindow(null);
			window.dispose();
		}
	}

	/**
	 * @param create Whether it should be created if it doesn't already exist.
	 * @return The single instance of a special, hidden window that can be used for various
	 *         operations that require a window before you actually have one available.
	 */
	public static Frame getHiddenFrame(boolean create) {
		if (HIDDEN_FRAME == null && create) {
			HIDDEN_FRAME = new Frame();
			HIDDEN_FRAME.setUndecorated(true);
			HIDDEN_FRAME.setBounds(0, 0, 0, 0);
		}
		StdImageSet icons = AppWindow.getDefaultWindowIcons();
		int sequence = icons != null ? icons.getSequence() : -1;
		if (HIDDEN_FRAME_ICONSET_SEQUENCE != sequence) {
			HIDDEN_FRAME_ICONSET_SEQUENCE = sequence;
			if (icons != null) {
				HIDDEN_FRAME.setIconImages(icons.toList());
			}
		}
		return HIDDEN_FRAME;
	}

	/** @return <code>true</code> if the graphics system is safe to use. */
	public static boolean areGraphicsSafeToUse() {
		if (!GraphicsEnvironment.isHeadless()) {
			if (HEADLESS_CHECK_RESULT == 0) {
				// We do the following just in case we're in an X-Windows
				// environment without a valid DISPLAY device...
				try {
					GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
					HEADLESS_CHECK_RESULT = 1;
				} catch (Error error) {
					HEADLESS_CHECK_RESULT = 2;
				}
			}
			if (HEADLESS_CHECK_RESULT == 1) {
				return true;
			}
		}
		return false;
	}

	/** @return The reason <code>areGraphicsSafeToUse()</code> returned <code>false</code>. */
	public static String getReasonForUnsafeGraphics() {
		return HEADLESS;
	}

	/** Sets up the standard UI. Should be called very early in the application launch process. */
	public static void configureStandardUI() {
		System.setProperty("apple.laf.useScreenMenuBar", Boolean.TRUE.toString()); //$NON-NLS-1$
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
}
