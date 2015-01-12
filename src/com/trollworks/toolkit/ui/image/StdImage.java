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

package com.trollworks.toolkit.ui.image;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.ui.GraphicsUtilities;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.Icon;

import org.w3c.dom.Node;

/** Provides a {@link BufferedImage} that implements Swing's {@link Icon} interface for convenience. */
public class StdImage extends BufferedImage implements Icon {
	@Localize("Unable to load image")
	@Localize(locale = "ru", value = "Невозможно загрузить изображение")
	@Localize(locale = "de", value = "Kann Bild nicht laden")
	@Localize(locale = "es", value = "")
	private static String							UNABLE_TO_LOAD_IMAGE;
	@Localize("Invalid angle: %d")
	@Localize(locale = "ru", value = "Недопустимый угол: %d")
	@Localize(locale = "de", value = "Ungültiger Winkel: %d")
	@Localize(locale = "es", value = "")
	private static String							INVALID_ANGLE;
	@Localize("Invalid transparency")
	@Localize(locale = "ru", value = "Недопустимая прозрачность")
	@Localize(locale = "de", value = "Ungültige Transparenz")
	@Localize(locale = "es", value = "La transparencia no es válida")
	private static String							INVALID_TRANSPARENCY;

	static {
		Localization.initialize();
	}

	private static final String						COLORIZED_POSTFIX	= new String(new char[] { ':', 'C', 22 });
	private static final String						FADED_POSTFIX		= new String(new char[] { ':', 'F', 22 });
	private static final HashSet<URL>				LOCATIONS			= new HashSet<>();
	private static final HashMap<String, StdImage>	MAP					= new HashMap<>();
	private static final HashMap<StdImage, String>	REVERSE_MAP			= new HashMap<>();
	private static final HashSet<String>			FAILED_LOOKUPS		= new HashSet<>();

	static {
		addLocation(StdImage.class.getResource("images/")); //$NON-NLS-1$
	}

	public static final StdImage					ADD					= get("add");								//$NON-NLS-1$
	public static final StdImage					COLLAPSE			= get("collapse");							//$NON-NLS-1$
	public static final StdImage					DOCK_CLOSE			= get("dock_close");						//$NON-NLS-1$
	public static final StdImage					DOCK_MAXIMIZE		= get("dock_maximize");					//$NON-NLS-1$
	public static final StdImage					DOCK_RESTORE		= get("dock_restore");						//$NON-NLS-1$
	public static final StdImage					DOWN_TRIANGLE		= get("down_triangle");					//$NON-NLS-1$
	public static final StdImage					DOWN_TRIANGLE_ROLL	= get("down_triangle_roll");				//$NON-NLS-1$
	public static final StdImage					EXPAND				= get("expand");							//$NON-NLS-1$
	public static final StdImageSet					FILE				= StdImageSet.getOrLoad("file");			//$NON-NLS-1$
	public static final StdImageSet					FOLDER				= StdImageSet.getOrLoad("folder");			//$NON-NLS-1$
	public static final StdImage					LOCKED				= get("locked");							//$NON-NLS-1$
	public static final StdImage					MINI_WARNING		= get("mini_warning");						//$NON-NLS-1$
	public static final StdImage					MODIFIED_MARKER		= get("modified_marker");					//$NON-NLS-1$
	public static final StdImage					MORE				= get("more");								//$NON-NLS-1$
	public static final StdImage					NOT_MODIFIED_MARKER	= get("not_modified_marker");				//$NON-NLS-1$
	public static final StdImageSet					PREFERENCES			= StdImageSet.getOrLoad("preferences");	//$NON-NLS-1$
	public static final StdImage					REMOVE				= get("remove");							//$NON-NLS-1$
	public static final StdImage					RIGHT_TRIANGLE		= get("right_triangle");					//$NON-NLS-1$
	public static final StdImage					RIGHT_TRIANGLE_ROLL	= get("right_triangle_roll");				//$NON-NLS-1$
	public static final StdImage					SIZE_TO_FIT			= get("size_to_fit");						//$NON-NLS-1$
	public static final StdImage					TOGGLE_OPEN			= get("toggle_open");						//$NON-NLS-1$
	public static final StdImage					UNLOCKED			= get("unlocked");							//$NON-NLS-1$

	/**
	 * Adds a location to search for images.
	 *
	 * @param file A directory to be be searched.
	 */
	public static final synchronized void addLocation(File file) {
		try {
			if (file.isDirectory()) {
				addLocation(file.toURI().toURL());
			}
		} catch (Exception exception) {
			Log.error(exception);
		}
	}

	/**
	 * Adds a location to search for images.
	 *
	 * @param url The location this URL points to will be searched.
	 */
	public static final synchronized void addLocation(URL url) {
		if (!LOCATIONS.contains(url)) {
			LOCATIONS.add(url);
			FAILED_LOOKUPS.clear();
		}
	}

	/**
	 * @param name The name to search for.
	 * @return The image for the specified name.
	 */
	public static final synchronized StdImage get(String name) {
		return get(name, true);
	}

	/**
	 * @param name The name to search for.
	 * @param cache Whether or not to cache the image. If <code>cache</code> is <code>false</code>,
	 *            then a new copy of the image will be generated, even if it was in the cache
	 *            already.
	 * @return The image for the specified name.
	 */
	public static final synchronized StdImage get(String name, boolean cache) {
		StdImage img = cache ? MAP.get(name) : null;
		if (img == null && !FAILED_LOOKUPS.contains(name)) {
			for (URL url : LOCATIONS) {
				String filename = name + ".png"; //$NON-NLS-1$
				try {
					img = loadImage(new URL(url, filename));
				} catch (Exception exception) {
					// Ignore...
				}
				if (img != null) {
					break;
				}
			}
			if (img != null) {
				if (cache) {
					MAP.put(name, img);
					REVERSE_MAP.put(img, name);
				}
			} else {
				FAILED_LOOKUPS.add(name);
			}
		}
		return img;
	}

	/**
	 * Manually adds the specified image to the cache. If an image was already mapped to the
	 * specified name, it will be replaced.
	 *
	 * @param name The name to map the image to.
	 * @param image The image itself.
	 */
	public static final synchronized void add(String name, StdImage image) {
		MAP.put(name, image);
		REVERSE_MAP.put(image, name);
		FAILED_LOOKUPS.remove(name);
	}

	/**
	 * Removes from the image cache any image cached for the specified name.
	 *
	 * @param name The key for the image to release.
	 */
	public static final synchronized void release(String name) {
		Object img = MAP.remove(name);
		if (img != null) {
			REVERSE_MAP.remove(img);
		}
	}

	/**
	 * @param image The image to scale.
	 * @param scaledSize The height and width to scale the image to.
	 * @return The new image scaled to the given size.
	 */
	public static final StdImage scale(Image image, int scaledSize) {
		return scale(image, scaledSize, scaledSize);
	}

	/**
	 * @param image The image to scale.
	 * @param scaledWidth The width to scale the image to.
	 * @param scaledHeight The height to scale the image to.
	 * @return The new image scaled to the given size.
	 */
	public static final StdImage scale(Image image, int scaledWidth, int scaledHeight) {
		StdImage img = getToolkitImage(image);
		int currentWidth = img.getWidth();
		int currentHeight = img.getHeight();
		if (currentWidth < scaledWidth || currentHeight < scaledHeight) {
			return internalScale(img, scaledWidth, scaledHeight);
		}
		boolean needFlush = false;
		do {
			int prevCurrentWidth = currentWidth;
			int prevCurrentHeight = currentHeight;
			currentWidth = reduce(currentWidth, scaledWidth);
			currentHeight = reduce(currentHeight, scaledHeight);
			if (prevCurrentWidth == currentWidth && prevCurrentHeight == currentHeight) {
				break;
			}
			StdImage intermediate = internalScale(img, currentWidth, currentHeight);
			if (needFlush) {
				img.flush();
			}
			img = intermediate;
			needFlush = true;
		} while (currentWidth != scaledWidth || currentHeight != scaledHeight);
		return img;
	}

	private static final int reduce(int current, int desired) {
		if (current > desired) {
			current -= current / 7;
			if (current < desired) {
				current = desired;
			}
		}
		return current;
	}

	private static final StdImage internalScale(StdImage image, int scaledWidth, int scaledHeight) {
		StdImage buffer = createTransparent(scaledWidth, scaledHeight);
		Graphics2D gc = buffer.getGraphics();
		GraphicsUtilities.setMaximumQualityForGraphics(gc);
		gc.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
		gc.dispose();
		return buffer;
	}

	/**
	 * If the image passed in is already a {@link StdImage}, it is returned. However, if it is not,
	 * then a new {@link StdImage} is created with the contents of the image and returned.
	 *
	 * @param image The image to work on.
	 * @return A buffered image.
	 */
	public static final StdImage getToolkitImage(Image image) {
		if (image instanceof StdImage) {
			return (StdImage) image;
		}
		return createOptimizedImage(image);
	}

	private static StdImage createOptimizedImage(Image image) {
		Toolkit tk = Toolkit.getDefaultToolkit();
		if (!tk.prepareImage(image, -1, -1, null)) {
			while (true) {
				int result = tk.checkImage(image, -1, -1, null);
				if ((result & (ImageObserver.ERROR | ImageObserver.ABORT)) != 0) {
					image = null;
					break;
				}
				if ((result & (ImageObserver.ALLBITS | ImageObserver.SOMEBITS | ImageObserver.FRAMEBITS)) != 0) {
					break;
				}
				// Try to allow the image loading thread a chance to run.
				// At least on Windows, not sleeping seems to be a problem.
				try {
					Thread.sleep(1);
				} catch (InterruptedException ie) {
					image = null;
					break;
				}
			}
		}
		if (image == null) {
			Log.error(new Exception(UNABLE_TO_LOAD_IMAGE));
			return create(1, 1);
		}
		StdImage buffer = createTransparent(image.getWidth(null), image.getHeight(null));
		Graphics2D gc = buffer.getGraphics();
		gc.drawImage(image, 0, 0, null);
		gc.dispose();
		return buffer;
	}

	/**
	 * @param image The image to work on.
	 * @return A new image rotated 90 degrees.
	 */
	public static final StdImage rotate90(StdImage image) {
		return rotate(image, 90);
	}

	/**
	 * @param image The image to work on.
	 * @return A new image rotated 180 degrees.
	 */
	public static final StdImage rotate180(StdImage image) {
		return rotate(image, 180);
	}

	/**
	 * @param image The image to work on.
	 * @return A new image rotated 270 degrees.
	 */
	public static final StdImage rotate270(StdImage image) {
		return rotate(image, 270);
	}

	private static final StdImage rotate(StdImage image, int angle) {
		int width = image.getWidth();
		int height = image.getHeight();
		StdImage buffer = createTransparent(height, width);
		Graphics2D gc = buffer.getGraphics();
		gc.rotate(Math.toRadians(angle));
		int x;
		int y;
		if (angle == 90) {
			x = 0;
			y = -width;
		} else if (angle == 180) {
			x = -width;
			y = -height;
		} else if (angle == 270) {
			x = -height;
			y = 0;
		} else {
			x = 0;
			y = 0;
			Log.error(String.format(INVALID_ANGLE, Integer.valueOf(angle)));
		}
		gc.drawImage(image, x, y, null);
		gc.dispose();
		return buffer;
	}

	/**
	 * Creates a new image by superimposing the specified image centered on top of another.
	 *
	 * @param baseImage The base image.
	 * @param image The image to superimpose.
	 * @return The new image.
	 */
	public static final StdImage superimpose(StdImage baseImage, StdImage image) {
		return superimpose(baseImage, image, (baseImage.getWidth() - image.getWidth()) / 2, (baseImage.getHeight() - image.getHeight()) / 2);
	}

	/**
	 * Creates a new image by superimposing the specified image on top of another.
	 *
	 * @param baseImage The base image.
	 * @param image The image to superimpose.
	 * @param x The x-coordinate to draw the top image at.
	 * @param y The y-coordinate to draw the top image at.
	 * @return The new image.
	 */
	public static final StdImage superimpose(StdImage baseImage, StdImage image, int x, int y) {
		int width = baseImage.getWidth();
		int height = baseImage.getHeight();
		int tWidth = image.getWidth();
		int tHeight = image.getHeight();
		if (x + tWidth > width) {
			width = x + tWidth;
		}
		if (y + tHeight > height) {
			height = y + tHeight;
		}
		StdImage buffer = createTransparent(width, height);
		Graphics2D gc = buffer.getGraphics();
		gc.drawImage(baseImage, 0, 0, null);
		gc.drawImage(image, x, y, null);
		gc.dispose();
		return buffer;
	}

	/**
	 * Creates a new image by superimposing the specified image centered on top of another. In
	 * addition, the newly created image is added as a named image.
	 *
	 * @param baseImage The base image.
	 * @param image The image to superimpose.
	 * @param name The name to use.
	 * @return The new image.
	 */
	public static final StdImage superimposeAndName(StdImage baseImage, StdImage image, String name) {
		StdImage img = superimpose(baseImage, image);
		add(name, img);
		return img;
	}

	/**
	 * Creates a new image by superimposing the specified image on top of another. In addition, the
	 * newly created image is added as a named image.
	 *
	 * @param baseImage The base image.
	 * @param image The image to superimpose.
	 * @param x The x-coordinate to draw the top image at.
	 * @param y The y-coordinate to draw the top image at.
	 * @param name The name to use.
	 * @return The new image.
	 */
	public static final StdImage superimposeAndName(StdImage baseImage, StdImage image, int x, int y, String name) {
		StdImage img = superimpose(baseImage, image, x, y);
		add(name, img);
		return img;
	}

	/**
	 * Creates a colorized version of an image.
	 *
	 * @param image The image to work on.
	 * @param color The color to apply.
	 * @return The colorized image.
	 */
	public static final synchronized StdImage createColorizedImage(StdImage image, Color color) {
		String name = REVERSE_MAP.get(image);
		StdImage img = null;
		if (name != null) {
			name = name + color + COLORIZED_POSTFIX;
			img = get(name);
		}
		if (img == null) {
			img = ColorFilter.createColorizedImage(image, color);
			if (img != null && name != null) {
				MAP.put(name, img);
				REVERSE_MAP.put(img, name);
			}
		}
		return img;
	}

	/**
	 * Creates a faded version of an image.
	 *
	 * @param image The image to work on.
	 * @param percentage The percentage of black or white to use.
	 * @param useWhite Whether to use black or white.
	 * @return The faded image.
	 */
	public static final synchronized StdImage createFadedImage(StdImage image, int percentage, boolean useWhite) {
		String name = REVERSE_MAP.get(image);
		StdImage img = null;
		if (name != null) {
			name = name + percentage + useWhite + FADED_POSTFIX;
			img = get(name);
		}
		if (img == null) {
			img = FadeFilter.createFadedImage(image, percentage, useWhite);
			if (img != null && name != null) {
				MAP.put(name, img);
				REVERSE_MAP.put(img, name);
			}
		}
		return img;
	}

	/**
	 * Creates a disabled version of an image.
	 *
	 * @param image The image to work on.
	 * @return The disabled image.
	 */
	public static final StdImage createDisabledImage(StdImage image) {
		return createFadedImage(createColorizedImage(image, Color.white), 50, true);
	}

	/**
	 * Loads an optimized, buffered image from the specified file.
	 *
	 * @param file The file to load from.
	 * @return The image, or <code>null</code> if it cannot be loaded.
	 */
	public static final StdImage loadImage(File file) {
		try {
			return loadImage(file.toURI().toURL());
		} catch (Exception exception) {
			return null;
		}
	}

	/**
	 * Loads an optimized, buffered image from the specified URL.
	 *
	 * @param url The URL to load from.
	 * @return The image, or <code>null</code> if it cannot be loaded.
	 */
	public static final StdImage loadImage(URL url) {
		try {
			return createOptimizedImage(ImageIO.read(url));
		} catch (Exception exception) {
			return null;
		}
	}

	/**
	 * Loads an optimized, buffered image from the specified byte array.
	 *
	 * @param data The byte array to load from.
	 * @return The image, or <code>null</code> if it cannot be loaded.
	 */
	public static final StdImage loadImage(byte[] data) {
		try {
			return createOptimizedImage(ImageIO.read(new ByteArrayInputStream(data)));
		} catch (Exception exception) {
			return null;
		}
	}

	/**
	 * Writes a PNG to a file, using the specified DPI.
	 *
	 * @param file The file to write to.
	 * @param image The image to use.
	 * @param dpi The DPI to use.
	 * @return <code>true</code> on success.
	 */
	public static final boolean writePNG(File file, Image image, int dpi) {
		boolean result;
		try (FileOutputStream os = new FileOutputStream(file)) {
			result = writePNG(os, image, dpi);
		} catch (Exception exception) {
			result = false;
		}
		return result;
	}

	/**
	 * Writes a PNG to a stream, using the specified DPI.
	 *
	 * @param os The stream to write to.
	 * @param image The image to use.
	 * @param dpi The DPI to use.
	 * @return <code>true</code> on success.
	 */
	public static final boolean writePNG(OutputStream os, Image image, int dpi) {
		ImageWriter writer = null;
		try (ImageOutputStream stream = ImageIO.createImageOutputStream(os)) {
			StdImage img = getToolkitImage(image);
			ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(img);
			writer = ImageIO.getImageWriters(type, "png").next(); //$NON-NLS-1$
			IIOMetadata metaData = writer.getDefaultImageMetadata(type, null);
			try {
				Node root = metaData.getAsTree("javax_imageio_png_1.0"); //$NON-NLS-1$
				IIOMetadataNode pHYs_node = new IIOMetadataNode("pHYs"); //$NON-NLS-1$
				String ppu = Integer.toString((int) (dpi / 0.0254));
				pHYs_node.setAttribute("pixelsPerUnitXAxis", ppu); //$NON-NLS-1$
				pHYs_node.setAttribute("pixelsPerUnitYAxis", ppu); //$NON-NLS-1$
				pHYs_node.setAttribute("unitSpecifier", "meter"); //$NON-NLS-1$ //$NON-NLS-2$
				root.appendChild(pHYs_node);
				metaData.setFromTree("javax_imageio_png_1.0", root); //$NON-NLS-1$
			} catch (Exception exception) {
				Log.error(exception);
			}
			writer.setOutput(stream);
			writer.write(new IIOImage(img, null, metaData));
			stream.flush();
			writer.dispose();
			return true;
		} catch (Exception exception) {
			if (writer != null) {
				writer.dispose();
			}
			return false;
		}
	}

	/**
	 * @param width The width to create.
	 * @param height The height to create.
	 * @return A new {@link StdImage} of the given size.
	 */
	public static StdImage create(int width, int height) {
		Graphics2D g2d = GraphicsUtilities.getGraphics();
		GraphicsConfiguration gc = g2d.getDeviceConfiguration();
		StdImage img = create(gc, width, height);
		g2d.dispose();
		return img;
	}

	/**
	 * @param gc The {@link GraphicsConfiguration} to make the image compatible with.
	 * @param width The width to create.
	 * @param height The height to create.
	 * @return A new {@link StdImage} of the given size.
	 */
	public static StdImage create(GraphicsConfiguration gc, int width, int height) {
		ColorModel model = gc.getColorModel();
		return new StdImage(model, model.createCompatibleWritableRaster(width, height), model.isAlphaPremultiplied(), null);
	}

	/**
	 * @param width The width to create.
	 * @param height The height to create.
	 * @param transparency A constant from {@link Transparency}.
	 * @return A new {@link StdImage} of the given size.
	 */
	public static StdImage create(int width, int height, int transparency) {
		Graphics2D g2d = GraphicsUtilities.getGraphics();
		GraphicsConfiguration gc = g2d.getDeviceConfiguration();
		StdImage img = create(gc, width, height, transparency);
		g2d.dispose();
		return img;
	}

	/**
	 * @param gc The {@link GraphicsConfiguration} to make the image compatible with.
	 * @param width The width to create.
	 * @param height The height to create.
	 * @param transparency A constant from {@link Transparency}.
	 * @return A new {@link StdImage} of the given size.
	 */
	public static StdImage create(GraphicsConfiguration gc, int width, int height, int transparency) {
		if (gc.getColorModel().getTransparency() == transparency) {
			return create(gc, width, height);
		}
		ColorModel cm = gc.getColorModel(transparency);
		if (cm == null) {
			throw new IllegalArgumentException(INVALID_TRANSPARENCY);
		}
		return new StdImage(cm, cm.createCompatibleWritableRaster(width, height), cm.isAlphaPremultiplied(), null);
	}

	/**
	 * @param width The width to create.
	 * @param height The height to create.
	 * @return A new {@link StdImage} of the given size.
	 */
	public static final StdImage createTransparent(int width, int height) {
		return create(width, height, Transparency.TRANSLUCENT);
	}

	/**
	 * @param gc The {@link GraphicsConfiguration} to make the image compatible with.
	 * @param width The width to create.
	 * @param height The height to create.
	 * @return A new {@link StdImage} of the given size.
	 */
	public static final StdImage createTransparent(GraphicsConfiguration gc, int width, int height) {
		return create(gc, width, height, Transparency.TRANSLUCENT);
	}

	private StdImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?, ?> properties) {
		super(cm, raster, isRasterPremultiplied, properties);
		clear();
	}

	@Override
	public Graphics2D getGraphics() {
		Graphics2D gc = (Graphics2D) super.getGraphics();
		gc.setClip(0, 0, getWidth(), getHeight());
		return gc;
	}

	/**
	 * Clears the image, filling it with black or, if the image isn't opaque, complete transparency.
	 */
	public void clear() {
		fill(new Color(0, getTransparency() != OPAQUE));
	}

	/** @param color The {@link Color} to fill the image with. */
	public void fill(Color color) {
		Graphics2D gc = getGraphics();
		gc.setBackground(color);
		gc.clearRect(0, 0, getWidth(), getHeight());
		gc.dispose();
	}

	@Override
	public void paintIcon(Component component, Graphics gc, int x, int y) {
		gc.drawImage(this, x, y, component);
	}

	@Override
	public int getIconWidth() {
		return getWidth();
	}

	@Override
	public int getIconHeight() {
		return getHeight();
	}
}
