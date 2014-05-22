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

package com.trollworks.toolkit.ui.image;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.ui.GraphicsUtilities;
import com.trollworks.toolkit.utility.Debug;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import org.w3c.dom.Node;

/** Provides standardized image access. */
public class Images {
	@Localize("Unable to load image")
	private static String								UNABLE_TO_LOAD_IMAGE;
	@Localize("Invalid angle: %d")
	private static String								INVALID_ANGLE;
	@Localize("Invalid transparency")
	private static String								INVALID_TRANSPARENCY;

	static {
		Localization.initialize();
	}

	/** The extensions used for image files. */
	public static final String[]						EXTENSIONS			= { ".png", ".gif", ".jpg" };				//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String							COLORIZED_POSTFIX	= new String(new char[] { ':', 'C', 22 });
	private static final String							FADED_POSTFIX		= new String(new char[] { ':', 'F', 22 });
	private static final HashSet<URL>					LOCATIONS			= new HashSet<>();
	private static final HashMap<String, ToolkitIcon>	MAP					= new HashMap<>();
	private static final HashMap<ToolkitIcon, String>	REVERSE_MAP			= new HashMap<>();
	private static final HashSet<String>				FAILED_LOOKUPS		= new HashSet<>();

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
			assert false : Debug.toString(exception);
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
	public static final synchronized ToolkitIcon get(String name) {
		return get(name, true);
	}

	/**
	 * @param name The name to search for.
	 * @param cache Whether or not to cache the image. If <code>cache</code> is <code>false</code>,
	 *            then a new copy of the image will be generated, even if it was in the cache
	 *            already.
	 * @return The image for the specified name.
	 */
	public static final synchronized ToolkitIcon get(String name, boolean cache) {
		ToolkitIcon img = cache ? MAP.get(name) : null;
		if (img == null && !FAILED_LOOKUPS.contains(name)) {
			for (URL url : LOCATIONS) {
				for (int i = 0; i < EXTENSIONS.length && img == null; i++) {
					String filename = name + EXTENSIONS[i];
					try {
						img = loadImage(new URL(url, filename));
					} catch (Exception exception) {
						// Ignore...
					}
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
	public static final synchronized void add(String name, ToolkitIcon image) {
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
	public static final ToolkitIcon scale(Image image, int scaledSize) {
		return scale(image, scaledSize, scaledSize);
	}

	/**
	 * @param image The image to scale.
	 * @param scaledWidth The width to scale the image to.
	 * @param scaledHeight The height to scale the image to.
	 * @return The new image scaled to the given size.
	 */
	public static final ToolkitIcon scale(Image image, int scaledWidth, int scaledHeight) {
		ToolkitIcon img = getToolkitIcon(image);
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
			ToolkitIcon intermediate = internalScale(img, currentWidth, currentHeight);
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

	private static final ToolkitIcon internalScale(ToolkitIcon image, int scaledWidth, int scaledHeight) {
		ToolkitIcon buffer = createTransparent(scaledWidth, scaledHeight);
		Graphics2D gc = buffer.getGraphics();
		GraphicsUtilities.setMaximumQualityForGraphics(gc);
		gc.setClip(0, 0, scaledWidth, scaledHeight);
		gc.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
		gc.dispose();
		return buffer;
	}

	/**
	 * If the image passed in is already a {@link ToolkitIcon}, it is returned. However, if it is
	 * not, then a new {@link ToolkitIcon} is created with the contents of the image and returned.
	 *
	 * @param image The image to work on.
	 * @return A buffered image.
	 */
	public static final ToolkitIcon getToolkitIcon(Image image) {
		if (image instanceof ToolkitIcon) {
			return (ToolkitIcon) image;
		}
		return createOptimizedImage(image);
	}

	private static ToolkitIcon createOptimizedImage(Image image) {
		image = loadToolkitImage(image);
		if (image == null) {
			Log.error(new Exception(UNABLE_TO_LOAD_IMAGE));
			return create(1, 1);
		}
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		ToolkitIcon buffer = createTransparent(width, height);
		Graphics2D gc = buffer.getGraphics();
		gc.setClip(0, 0, width, height);
		gc.drawImage(image, 0, 0, null);
		gc.dispose();
		return buffer;
	}

	/**
	 * @param image The image to work on.
	 * @return A new image rotated 90 degrees.
	 */
	public static final ToolkitIcon rotate90(Image image) {
		return rotate(image, 90);
	}

	/**
	 * @param image The image to work on.
	 * @return A new image rotated 180 degrees.
	 */
	public static final ToolkitIcon rotate180(Image image) {
		return rotate(image, 180);
	}

	/**
	 * @param image The image to work on.
	 * @return A new image rotated 270 degrees.
	 */
	public static final ToolkitIcon rotate270(Image image) {
		return rotate(image, 270);
	}

	private static final ToolkitIcon rotate(Image image, int angle) {
		ToolkitIcon input = getToolkitIcon(image);
		int width = input.getWidth();
		int height = input.getHeight();
		ToolkitIcon buffer = createTransparent(height, width);
		Graphics2D gc = buffer.getGraphics();
		gc.setClip(0, 0, height, width);
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
	public static final ToolkitIcon superimpose(Image baseImage, Image image) {
		ToolkitIcon img1 = getToolkitIcon(baseImage);
		ToolkitIcon img2 = getToolkitIcon(image);
		return superimpose(img1, img2, (img1.getWidth() - img2.getWidth()) / 2, (img1.getHeight() - img2.getHeight()) / 2);
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
	public static final ToolkitIcon superimpose(Image baseImage, Image image, int x, int y) {
		ToolkitIcon img1 = getToolkitIcon(baseImage);
		ToolkitIcon img2 = getToolkitIcon(image);
		int width = img1.getWidth();
		int height = img1.getHeight();
		int tWidth = img2.getWidth();
		int tHeight = img2.getHeight();
		if (x + tWidth > width) {
			width = x + tWidth;
		}
		if (y + tHeight > height) {
			height = y + tHeight;
		}
		ToolkitIcon buffer = createTransparent(width, height);
		Graphics2D gc = buffer.getGraphics();
		gc.setClip(0, 0, width, height);
		gc.drawImage(img1, 0, 0, null);
		gc.drawImage(img2, x, y, null);
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
	public static final ToolkitIcon superimposeAndName(Image baseImage, Image image, String name) {
		ToolkitIcon img = superimpose(baseImage, image);
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
	public static final ToolkitIcon superimposeAndName(Image baseImage, Image image, int x, int y, String name) {
		ToolkitIcon img = superimpose(baseImage, image, x, y);
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
	public static final synchronized ToolkitIcon createColorizedImage(ToolkitIcon image, Color color) {
		String name = REVERSE_MAP.get(image);
		ToolkitIcon img = null;
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
	public static final synchronized ToolkitIcon createFadedImage(ToolkitIcon image, int percentage, boolean useWhite) {
		String name = REVERSE_MAP.get(image);
		ToolkitIcon img = null;
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
	public static final ToolkitIcon createDisabledImage(ToolkitIcon image) {
		return createFadedImage(createColorizedImage(image, Color.white), 50, true);
	}

	/**
	 * Forces the image to be loaded.
	 *
	 * @param image The image to load.
	 * @return The fully loaded image, or <code>null</code> if it can't be loaded.
	 */
	public static final Image loadToolkitImage(Image image) {
		Toolkit tk = Toolkit.getDefaultToolkit();
		if (!tk.prepareImage(image, -1, -1, null)) {
			while (true) {
				int result = tk.checkImage(image, -1, -1, null);
				if ((result & (ImageObserver.ERROR | ImageObserver.ABORT)) != 0) {
					return null;
				}
				if ((result & (ImageObserver.ALLBITS | ImageObserver.SOMEBITS | ImageObserver.FRAMEBITS)) != 0) {
					break;
				}
				// Try to allow the image loading thread a chance to run.
				// At least on Windows, not sleeping seems to be a problem.
				try {
					Thread.sleep(1);
				} catch (InterruptedException ie) {
					return null;
				}
			}
		}
		return image;
	}

	/**
	 * Loads an optimized, buffered image from the specified file.
	 *
	 * @param file The file to load from.
	 * @return The image, or <code>null</code> if it cannot be loaded.
	 */
	public static final ToolkitIcon loadImage(File file) {
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
	public static final ToolkitIcon loadImage(URL url) {
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
	public static final ToolkitIcon loadImage(byte[] data) {
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
			ToolkitIcon img = getToolkitIcon(image);
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
				assert false : Debug.toString(exception);
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
	 * @return A new {@link ToolkitIcon} of the given size.
	 */
	public static ToolkitIcon create(int width, int height) {
		Graphics2D g2d = GraphicsUtilities.getGraphics();
		GraphicsConfiguration gc = g2d.getDeviceConfiguration();
		ToolkitIcon img = create(gc, width, height);
		g2d.dispose();
		return img;
	}

	/**
	 * @param gc The {@link GraphicsConfiguration} to make the image compatible with.
	 * @param width The width to create.
	 * @param height The height to create.
	 * @return A new {@link ToolkitIcon} of the given size.
	 */
	public static ToolkitIcon create(GraphicsConfiguration gc, int width, int height) {
		ColorModel model = gc.getColorModel();
		return new ToolkitIcon(model, model.createCompatibleWritableRaster(width, height), model.isAlphaPremultiplied(), null);
	}

	/**
	 * @param width The width to create.
	 * @param height The height to create.
	 * @param transparency A constant from {@link Transparency}.
	 * @return A new {@link ToolkitIcon} of the given size.
	 */
	public static ToolkitIcon create(int width, int height, int transparency) {
		Graphics2D g2d = GraphicsUtilities.getGraphics();
		GraphicsConfiguration gc = g2d.getDeviceConfiguration();
		ToolkitIcon img = create(gc, width, height, transparency);
		g2d.dispose();
		return img;
	}

	/**
	 * @param gc The {@link GraphicsConfiguration} to make the image compatible with.
	 * @param width The width to create.
	 * @param height The height to create.
	 * @param transparency A constant from {@link Transparency}.
	 * @return A new {@link ToolkitIcon} of the given size.
	 */
	public static ToolkitIcon create(GraphicsConfiguration gc, int width, int height, int transparency) {
		if (gc.getColorModel().getTransparency() == transparency) {
			return create(gc, width, height);
		}
		ColorModel cm = gc.getColorModel(transparency);
		if (cm == null) {
			throw new IllegalArgumentException(INVALID_TRANSPARENCY);
		}
		return new ToolkitIcon(cm, cm.createCompatibleWritableRaster(width, height), cm.isAlphaPremultiplied(), null);
	}

	/**
	 * @param width The width to create.
	 * @param height The height to create.
	 * @return A new {@link ToolkitIcon} of the given size.
	 */
	public static final ToolkitIcon createTransparent(int width, int height) {
		return create(width, height, Transparency.TRANSLUCENT);
	}

	/**
	 * @param gc The {@link GraphicsConfiguration} to make the image compatible with.
	 * @param width The width to create.
	 * @param height The height to create.
	 * @return A new {@link ToolkitIcon} of the given size.
	 */
	public static final ToolkitIcon createTransparent(GraphicsConfiguration gc, int width, int height) {
		return create(gc, width, height, Transparency.TRANSLUCENT);
	}
}
