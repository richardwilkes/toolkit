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

import com.trollworks.toolkit.ui.image.StdImageSet;
import com.trollworks.toolkit.ui.widget.AppWindow;
import com.trollworks.toolkit.utility.Geometry;
import com.trollworks.toolkit.utility.I18n;
import com.trollworks.toolkit.utility.Platform;
import com.trollworks.toolkit.workarounds.WiderToolTipUI;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterGraphics;
import javax.swing.JComponent;
import javax.swing.UIManager;

/** Provides general graphics settings and manipulation. */
public class GraphicsUtilities {
    private static Frame         HIDDEN_FRAME;
    private static int           HIDDEN_FRAME_ICONSET_SEQUENCE = -1;
    private static boolean       HEADLESS_PRINT_MODE;
    private static int           HEADLESS_CHECK_RESULT;
    private static BufferedImage FALLBACK_GRAPHICS_BACKING_STORE;

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
        GraphicsEnvironment ge            = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice      best          = ge.getDefaultScreenDevice();
        Rectangle           overlapBounds = Geometry.intersection(bounds, best.getDefaultConfiguration().getBounds());
        int                 bestOverlap   = overlapBounds.width * overlapBounds.height;

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
     * @param area  The area within the panel to use when determining the maximum bounds for a
     *              window.
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
        GraphicsDevice      gd = getPreferredScreenDevice(bounds);

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

    public static void packAndCenterWindowOn(Window window, Component centeredOn) {
        window.pack();
        Dimension prefSize = window.getPreferredSize();
        Dimension minSize  = window.getMinimumSize();
        int       width    = Math.max(prefSize.width, minSize.width);
        int       height   = Math.max(prefSize.height, minSize.height);
        int       x;
        int       y;
        if (centeredOn != null) {
            Point     where = centeredOn.getLocationOnScreen();
            Dimension size  = centeredOn.getSize();
            x = where.x + (size.width - width) / 2;
            y = where.y + (size.height - height) / 2;
        } else {
            Rectangle bounds = getMaximumWindowBounds(window);
            x = bounds.x + (bounds.width - width) / 2;
            y = bounds.y + (bounds.height - height) / 2;
        }
        window.setLocation(x, y);
        forceOnScreen(window);
    }

    /**
     * Forces the specified window onscreen.
     *
     * @param window The window to force onscreen.
     */
    public static void forceOnScreen(Window window) {
        Rectangle maxBounds = getMaximumWindowBounds(window);
        Rectangle bounds    = window.getBounds();
        Point     location  = new Point(bounds.x, bounds.y);
        Dimension size      = window.getMinimumSize();

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
        for (AppWindow window : AppWindow.getAllAppWindows()) {
            window.repaint();
        }
    }

    /** Forces a full repaint and invalidate on all windows, disposing of any window buffers. */
    public static void forceRepaintAndInvalidate() {
        for (AppWindow window : AppWindow.getAllAppWindows()) {
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
        Frame      frame = AppWindow.getTopWindow();
        Graphics2D g2d   = frame == null ? null : (Graphics2D) frame.getGraphics();

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
            BufferedImage fallback;
            synchronized (GraphicsUtilities.class) {
                if (FALLBACK_GRAPHICS_BACKING_STORE == null) {
                    FALLBACK_GRAPHICS_BACKING_STORE = new BufferedImage(32, 1, BufferedImage.TYPE_INT_ARGB);
                }
                fallback = FALLBACK_GRAPHICS_BACKING_STORE;
            }
            return fallback.createGraphics();
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

    /** Attempts to force the app to the front. */
    public static void forceAppToFront() {
        // Calling Desktop.isDesktopSupported() generally doesn't have the desired effect on Windows
        boolean force = Platform.isWindows();
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().requestForeground(true);
            } catch (UnsupportedOperationException uoex) {
                force = true;
            }
        }
        if (force) {
            AppWindow topWindow = AppWindow.getTopWindow();
            if (topWindow != null) {
                if (!topWindow.isVisible()) {
                    topWindow.setVisible(true);
                }
                boolean alwaysOnTop = topWindow.isAlwaysOnTop();
                topWindow.setExtendedState(Frame.NORMAL);
                topWindow.toFront();
                topWindow.setAlwaysOnTop(true);
                try {
                    Point savedMouse = MouseInfo.getPointerInfo().getLocation();
                    Robot robot      = new Robot();
                    robot.mouseMove(topWindow.getX() + 100, topWindow.getY() + 10);
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    robot.mouseMove(savedMouse.x, savedMouse.y);
                } catch (Exception ex) {
                    // Ignore
                } finally {
                    topWindow.setAlwaysOnTop(alwaysOnTop);
                }
            }
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
        StdImageSet icons    = AppWindow.getDefaultWindowIcons();
        int         sequence = icons != null ? icons.getSequence() : -1;
        if (HIDDEN_FRAME_ICONSET_SEQUENCE != sequence) {
            HIDDEN_FRAME_ICONSET_SEQUENCE = sequence;
            if (icons != null) {
                HIDDEN_FRAME.setIconImages(icons.toList());
            }
        }
        return HIDDEN_FRAME;
    }

    /** @return {@code true} if the graphics system is safe to use. */
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
            return HEADLESS_CHECK_RESULT == 1;
        }
        return false;
    }

    /** @return The reason {@code areGraphicsSafeToUse()} returned {@code false}. */
    public static String getReasonForUnsafeGraphics() {
        return I18n.Text("There is no valid graphics display.");
    }

    /** Sets up the standard UI. Should be called very early in the application launch process. */
    public static void configureStandardUI() {
        System.setProperty("apple.laf.useScreenMenuBar", Boolean.TRUE.toString());
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Font current = UIManager.getFont(Fonts.KEY_STD_TEXT_FIELD);
            UIManager.getDefaults().put(Fonts.KEY_STD_TEXT_FIELD, new Font("SansSerif", current.getStyle(), current.getSize()));
            WiderToolTipUI.installIfNeeded();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * On the Mac (and perhaps Windows now, too), the graphics context will have a scale transform
     * of 2x if being drawn onto a retina display.
     *
     * @param gc The {@link Graphics} to check.
     * @return {@code true} if the specified graphics context is set to a 2x scale transform or is a
     *         printer context.
     */
    public static boolean isRetinaDisplay(Graphics gc) {
        if (gc instanceof PrinterGraphics) {
            return true;
        }
        AffineTransform transform = ((Graphics2D) gc).getFontRenderContext().getTransform();
        return transform.getScaleX() == 2 && transform.getScaleY() == 2;
    }
}
