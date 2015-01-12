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
import com.trollworks.toolkit.io.EndianUtils;
import com.trollworks.toolkit.io.StreamUtils;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.Numbers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Provides a set of images at different resolutions. */
public class StdImageSet implements Comparator<StdImage> {
	@Localize("Invalid ICNS")
	@Localize(locale = "ru", value = "Недопустимый ICNS")
	@Localize(locale = "de", value = "Fehlerhafte ICNS-Datei")
	@Localize(locale = "es", value = "fichero ICNS no es válido")
	private static String					INVALID_ICNS;
	@Localize("Invalid ICO")
	@Localize(locale = "ru", value = "Недопустимый ICO")
	@Localize(locale = "de", value = "Fehlerhafte ICO-Datei")
	@Localize(locale = "es", value = "fichero ICO no es válido")
	private static String					INVALID_ICO;
	@Localize("Unable to create PNG")
	@Localize(locale = "ru", value = "Невозможно создать PNG")
	@Localize(locale = "de", value = "Kann PNG-Datei nicht erstellen")
	@Localize(locale = "es", value = "Imposible crear fichero PNG")
	private static String					UNABLE_TO_CREATE_PNG;

	static {
		Localization.initialize();
	}

	public static final int[]				STD_SIZES	= { 1024, 512, 256, 128, 64, 48, 32, 16 };
	private static final int				TYPE_icns	= 0x69636e73;
	private static final int				TYPE_icp4	= 0x69637034;
	private static final int				TYPE_icp5	= 0x69637035;
	private static final int				TYPE_icp6	= 0x69637036;
	private static final int				TYPE_ic07	= 0x69633037;
	private static final int				TYPE_ic08	= 0x69633038;
	private static final int				TYPE_ic09	= 0x69633039;
	private static final int				TYPE_ic10	= 0x69633130;
	private static final int				TYPE_ic11	= 0x69633131;
	private static final int				TYPE_ic12	= 0x69633132;
	private static final int				TYPE_ic13	= 0x69633133;
	private static final int				TYPE_ic14	= 0x69633134;
	private static final int				TYPE_il32	= 0x696c3332;
	private static final int				TYPE_l8mk	= 0x6c386d6b;
	private static final int				TYPE_is32	= 0x69733332;
	private static final int				TYPE_s8mk	= 0x73386d6b;
	private static final int				TYPE_TOC	= 0x544f4320;
	private static Map<String, StdImageSet>	SETS		= new HashMap<>();
	private static int						SEQUENCE	= 0;
	private String							mName;
	private StdImageSet[]					mLayers;
	private List<StdImage>					mImages;
	private int								mSequence;

	/**
	 * @param name The name of the {@link StdImageSet}.
	 * @return The {@link StdImageSet}.
	 */
	public static final StdImageSet get(String name) {
		return SETS.get(name);
	}

	/**
	 * If the {@link StdImageSet} has not already been loaded, this method will attempt to load it
	 * from individual images matching the name.
	 *
	 * @param name The name of the {@link StdImageSet}.
	 * @return The {@link StdImageSet}.
	 */
	public static final StdImageSet getOrLoad(String name) {
		StdImageSet set = SETS.get(name);
		if (set == null) {
			List<StdImage> images = new ArrayList<>();
			for (int size : STD_SIZES) {
				StdImage img = StdImage.get(name + "_" + size); //$NON-NLS-1$
				if (img != null) {
					images.add(img);
				}
			}
			if (!images.isEmpty()) {
				set = new StdImageSet(name, images);
			}
		}
		return set;
	}

	/**
	 * Loads a Mac OS X icon set file (.icns). The format is described here: <a
	 * href="http://en.wikipedia.org/wiki/Apple_Icon_Image"
	 * >http://en.wikipedia.org/wiki/Apple_Icon_Image</a>.
	 *
	 * @param name The name to give this {@link StdImageSet}. Note that this should be unique, as it
	 *            will replace any existing {@link StdImageSet} with the same name.
	 * @param url The {@link URL} to load the images from.
	 */
	public static final StdImageSet loadIcns(String name, URL url) throws IOException {
		try (InputStream in = url.openStream()) {
			return loadIcns(name, in);
		}
	}

	/**
	 * Loads a Mac OS X icon set file (.icns). The format is described here: <a
	 * href="http://en.wikipedia.org/wiki/Apple_Icon_Image"
	 * >http://en.wikipedia.org/wiki/Apple_Icon_Image</a>.
	 *
	 * @param name The name to give this {@link StdImageSet}. Note that this should be unique, as it
	 *            will replace any existing {@link StdImageSet} with the same name.
	 * @param in The {@link InputStream} to load the images from.
	 */
	public static final StdImageSet loadIcns(String name, InputStream in) throws IOException {
		List<StdImage> images = new ArrayList<>();
		byte[] header = new byte[8];
		StreamUtils.readFully(in, header);
		if (EndianUtils.readBEInt(header, 0) != TYPE_icns) {
			throw new IOException(INVALID_ICNS);
		}
		int end = EndianUtils.readBEInt(header, 4);
		int pos = header.length;
		while (pos < end) {
			header = new byte[8];
			StreamUtils.readFully(in, header);
			pos += header.length;
			int type = EndianUtils.readBEInt(header, 0);
			int length = EndianUtils.readBEInt(header, 4);
			pos += length;
			switch (type) {
				case TYPE_icp4:
				case TYPE_icp5:
				case TYPE_icp6:
				case TYPE_ic07:
				case TYPE_ic08:
				case TYPE_ic09:
				case TYPE_ic10:
				case TYPE_ic11:
				case TYPE_ic12:
				case TYPE_ic13:
				case TYPE_ic14:
					loadImage(in, length - 8, name, images);
					break;
				default:
					StreamUtils.skipFully(in, length - 8);
					break;
			}
		}
		return images.isEmpty() ? null : new StdImageSet(name, images);
	}

	private static final void loadImage(InputStream in, int length, String name, List<StdImage> images) throws IOException {
		byte[] data = new byte[length];
		StreamUtils.readFully(in, data);
		StdImage img = StdImage.loadImage(data);
		if (img != null) {
			track(name, img);
			images.add(img);
		}
	}

	/**
	 * Loads a Windows OS icon set file (.ico). The format is described here: <a
	 * href="http://en.wikipedia.org/wiki/ICO_(file_format)"
	 * >http://en.wikipedia.org/wiki/ICO_(file_format)</a>. Note that only those ICO files that
	 * embed PNG images can be loaded with this method.
	 *
	 * @param name The name to give this {@link StdImageSet}. Note that this should be unique, as it
	 *            will replace any existing {@link StdImageSet} with the same name.
	 * @param url The {@link URL} to load the images from.
	 */
	public static final StdImageSet loadIco(String name, URL url) throws IOException {
		try (InputStream in = url.openStream()) {
			return loadIco(name, in);
		}
	}

	/**
	 * Loads a Windows OS icon set file (.ico). The format is described here: <a
	 * href="http://en.wikipedia.org/wiki/ICO_(file_format)"
	 * >http://en.wikipedia.org/wiki/ICO_(file_format)</a>. Note that only those ICO files that
	 * embed PNG images can be loaded with this method.
	 *
	 * @param name The name to give this {@link StdImageSet}. Note that this should be unique, as it
	 *            will replace any existing {@link StdImageSet} with the same name.
	 * @param in The {@link InputStream} to load the images from.
	 */
	public static final StdImageSet loadIco(String name, InputStream in) throws IOException {
		List<StdImage> images = new ArrayList<>();
		byte[] header = new byte[6];
		StreamUtils.readFully(in, header);
		if (EndianUtils.readLEUnsignedShort(header, 0) != 0 || EndianUtils.readLEUnsignedShort(header, 2) != 1) {
			throw new IOException(INVALID_ICO);
		}
		int count = EndianUtils.readLEUnsignedShort(header, 4);
		byte[] data = new byte[count * 16];
		StreamUtils.readFully(in, data);
		ico[] toc = new ico[count];
		for (int i = 0; i < count; i++) {
			toc[i].length = EndianUtils.readLEInt(data, i * 16 + 8);
			toc[i].offset = EndianUtils.readLEInt(data, i * 16 + 12);
		}
		Arrays.sort(toc);
		int pos = header.length + data.length;
		for (int i = 0; i < count; i++) {
			if (pos != toc[i].offset) {
				StreamUtils.skipFully(in, toc[i].offset - pos);
				pos = toc[i].offset;
			}
			loadImage(in, toc[i].length, name, images);
			pos += toc[i].length;
		}
		return images.isEmpty() ? null : new StdImageSet(name, images);
	}

	private static class ico implements Comparable<ico> {
		int	length;
		int	offset;

		@Override
		public int compareTo(ico other) {
			return Numbers.compare(offset, other.offset);
		}
	}

	private static final void track(String name, StdImage image) {
		StdImage.add("is:" + name + "_" + image.getWidth() + "x" + image.getHeight(), image); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Creates a new {@link StdImageSet}.
	 *
	 * @param name The name of this {@link StdImageSet}. This can be used to retrieve the
	 *            {@link StdImageSet} later, via a call to {@link #get(String)}.
	 * @param images The images that belong in this {@link StdImageSet}.
	 */
	public StdImageSet(String name, List<StdImage> images) {
		mName = name;
		updateSequence();
		mImages = new ArrayList<>(images);
		Collections.sort(mImages, this);
		SETS.put(name, this);
	}

	/**
	 * Creates a new {@link StdImageSet} that composites multiple images together from other
	 * {@link StdImageSet}s to form its images.
	 *
	 * @param name The name of this {@link StdImageSet}. This can be used to retrieve the
	 *            {@link StdImageSet} later, via a call to {@link #get(String)}.
	 * @param layers Two or more other {@link StdImageSet}s to use. Each one will be layered on top
	 *            of the previous one, creating a single image for a given size.
	 */
	public StdImageSet(String name, StdImageSet... layers) {
		if (layers == null || layers.length < 2) {
			throw new IllegalArgumentException();
		}
		mName = name;
		updateSequence();
		mLayers = new StdImageSet[layers.length];
		System.arraycopy(layers, 0, mLayers, 0, layers.length);
		mImages = new ArrayList<>();
		SETS.put(name, this);
	}

	@Override
	public int compare(StdImage o1, StdImage o2) {
		int result = Numbers.compare(o2.getWidth(), o1.getWidth());
		if (result == 0) {
			result = Numbers.compare(o2.getHeight(), o1.getHeight());
			if (result == 0) {
				result = Numbers.compare(o2.hashCode(), o1.hashCode());
			}
		}
		return result;
	}

	/** @return The name of this {@link StdImageSet}. */
	public String getName() {
		return mName;
	}

	/**
	 * @param size The width and height of the image.
	 * @return <code>true</code> if the image exists.
	 */
	public boolean hasImage(int size) {
		return hasImage(size, size);
	}

	/**
	 * @param width The width of the image.
	 * @param height The height of the image.
	 * @return <code>true</code> if the image exists.
	 */
	public boolean hasImage(int width, int height) {
		for (StdImage image : mImages) {
			if (width == image.getWidth() && height == image.getHeight()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param size The width and height of the image.
	 * @return An image from the set, or <code>null</code> if the desired dimensions cannot be
	 *         found.
	 */
	public StdImage getImageNoCreate(int size) {
		return getImageNoCreate(size, size);
	}

	/**
	 * @param width The width of the image.
	 * @param height The height of the image.
	 * @return An image from the set, or <code>null</code> if the desired dimensions cannot be
	 *         found.
	 */
	public StdImage getImageNoCreate(int width, int height) {
		for (StdImage image : mImages) {
			if (width == image.getWidth() && height == image.getHeight()) {
				return image;
			}
		}
		return null;
	}

	/**
	 * @param size The width and height of the image.
	 * @return An image from the set. If an exact match cannot be found, one of the existing images
	 *         will be scaled to the desired size.
	 */
	public StdImage getImage(int size) {
		return getImage(size, size);
	}

	/**
	 * @param width The width of the image.
	 * @param height The height of the image.
	 * @return An image from the set. If an exact match cannot be found, one of the existing images
	 *         will be scaled to the desired size.
	 */
	public StdImage getImage(int width, int height) {
		StdImage match = getImageNoCreate(width, height);
		if (match == null) {
			if (mLayers != null) {
				match = mLayers[0].getImage(width, height);
				for (int i = 1; i < mLayers.length; i++) {
					StdImage previous = match;
					match = StdImage.superimpose(match, mLayers[i].getImage(width, height));
					if (i > 1) {
						previous.flush();
					}
				}
			} else {
				StdImage inverseMatch = null;
				int best = Integer.MAX_VALUE;
				int inverseBest = Integer.MIN_VALUE;
				for (StdImage image : mImages) {
					int imageWidth = image.getWidth();
					int imageHeight = image.getHeight();
					int heuristic = (imageWidth - width) * (imageHeight - height);
					if (imageWidth > width || imageHeight > height) {
						if (heuristic < best) {
							best = heuristic;
							match = image;
						}
					} else if (match == null && heuristic > inverseBest) {
						inverseBest = heuristic;
						inverseMatch = image;
					}
				}
				if (match == null) {
					match = inverseMatch;
				}
				match = StdImage.scale(match, width, height);
			}
			track(mName, match);
			mImages.add(match);
			Collections.sort(mImages, this);
		}
		return match;
	}

	/** @return A list containing all of the images within this {@link StdImageSet}. */
	public List<StdImage> toList() {
		return new ArrayList<>(mImages);
	}

	/**
	 * @return The current sequence number of this {@link StdImageSet}. This can be used to
	 *         determine if the {@link StdImageSet} is the same as the last time you used it. These
	 *         are unique across all {@link StdImageSet}s.
	 */
	public synchronized int getSequence() {
		return mSequence;
	}

	private synchronized void updateSequence() {
		mSequence = ++SEQUENCE;
	}

	/**
	 * @param out The stream to write an ICNS file with this {@link StdImageSet}s contents to. Only
	 *            square images which have been loaded and match the sizes appropriate for an ICNS
	 *            file will be output. You may need to call {@link #getImage(int)} on the
	 *            appropriate sizes first.
	 */
	public void saveAsIcns(OutputStream out) throws IOException {
		List<byte[]> imageData = new ArrayList<>();
		List<Integer> imageType = new ArrayList<>();
		int size = 8;
		for (StdImage image : mImages) {
			int width = image.getWidth();
			// We currently only write out square images
			if (width == image.getHeight()) {
				int type = 0;
				int hiResType = 0;
				int oldStyleType = 0;
				int oldStyleMaskType = 0;
				// We currently only write out certain sizes
				switch (width) {
					case 1024:
						hiResType = TYPE_ic10;
						break;
					case 512:
						type = TYPE_ic09;
						hiResType = TYPE_ic14;
						break;
					case 256:
						type = TYPE_ic08;
						hiResType = TYPE_ic13;
						break;
					case 128:
						type = TYPE_ic07;
						break;
					case 64:
						type = TYPE_icp6;
						hiResType = TYPE_ic12;
						break;
					case 32:
						// The next line is commented out because, at least in Mac OS X 10.9, the
						// Finder is unable to load the type correctly

						// type = TYPE_icp5;
						hiResType = TYPE_ic11;
						oldStyleType = TYPE_il32;
						oldStyleMaskType = TYPE_l8mk;
						break;
					case 16:
						// The next line is commented out because, at least in Mac OS X 10.9, the
						// Finder is unable to load the type correctly

						// type = TYPE_icp4;
						oldStyleType = TYPE_is32;
						oldStyleMaskType = TYPE_s8mk;
						break;
					default:
						break;
				}
				if (hiResType != 0) {
					size += createPNG(image, imageData, imageType, hiResType, 144);
				}
				if (type != 0) {
					size += createPNG(image, imageData, imageType, type, 72);
				}
				if (oldStyleType != 0) {
					size += createOldStyleIcon(image, imageData, imageType, oldStyleType);
				}
				if (oldStyleMaskType != 0) {
					size += createOldStyleMask(image, imageData, imageType, oldStyleMaskType);
				}
			}
		}
		int count = imageData.size();
		byte[] toc = new byte[(count + 1) * 8];
		EndianUtils.writeBEInt(TYPE_TOC, toc, 0);
		EndianUtils.writeBEInt(toc.length, toc, 4);
		for (int i = 0; i < count; i++) {
			EndianUtils.writeBEInt(imageType.get(i).intValue(), toc, (i + 1) * 8);
			EndianUtils.writeBEInt(8 + imageData.get(i).length, toc, (i + 1) * 8 + 4);
		}
		size += toc.length;
		byte[] buffer = new byte[8];
		EndianUtils.writeBEInt(TYPE_icns, buffer, 0);
		EndianUtils.writeBEInt(size, buffer, 4);
		out.write(buffer);
		out.write(toc);
		for (int i = 0; i < count; i++) {
			byte[] data = imageData.get(i);
			EndianUtils.writeBEInt(imageType.get(i).intValue(), buffer, 0);
			EndianUtils.writeBEInt(buffer.length + data.length, buffer, 4);
			out.write(buffer);
			out.write(data);
		}
	}

	private static int createPNG(StdImage image, List<byte[]> imageData, List<Integer> imageType, int type, int dpi) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (!StdImage.writePNG(baos, image, dpi)) {
			throw new IOException(UNABLE_TO_CREATE_PNG);
		}
		byte[] bytes = baos.toByteArray();
		imageData.add(bytes);
		imageType.add(Integer.valueOf(type));
		return 8 + bytes.length;
	}

	private static int createOldStyleIcon(StdImage image, List<byte[]> imageData, List<Integer> imageType, int type) {
		int size = image.getWidth();
		byte[] bytes = new byte[size * size * 4];
		int i = 0;
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				EndianUtils.writeBEInt(image.getRGB(x, y), bytes, 4 * i++);
			}
		}
		imageData.add(bytes);
		imageType.add(Integer.valueOf(type));
		return 8 + bytes.length;
	}

	private static int createOldStyleMask(StdImage image, List<byte[]> imageData, List<Integer> imageType, int type) {
		int size = image.getWidth();
		byte[] bytes = new byte[size * size];
		int i = 0;
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				bytes[i++] = (byte) (image.getRGB(x, y) >>> 24);
			}
		}
		imageData.add(bytes);
		imageType.add(Integer.valueOf(type));
		return 8 + bytes.length;
	}

	/**
	 * @param out The stream to write an ICO file with this {@link StdImageSet}s contents to. Only
	 *            square images which have been loaded and match the sizes appropriate for an ICO
	 *            file will be output. You may need to call {@link #getImage(int)} on the
	 *            appropriate sizes first.
	 */
	public void saveAsIco(OutputStream out) throws IOException {
		byte[] buffer = new byte[16];
		int count = 0;
		int[] sizes = new int[] { 256, 128, 64, 48, 32, 16 };
		for (int size : sizes) {
			if (hasImage(size)) {
				count++;
			}
		}
		EndianUtils.writeLEShort(0, buffer, 0);
		EndianUtils.writeLEShort(1, buffer, 2);
		EndianUtils.writeLEShort(count, buffer, 4);
		out.write(buffer, 0, 6);
		int totalImageBytes = 0;
		List<byte[]> images = new ArrayList<>();
		for (int size : sizes) {
			if (hasImage(size)) {
				buffer[0] = (byte) (size == 256 ? 0 : size);
				buffer[1] = buffer[0];
				buffer[2] = 0;
				buffer[3] = 0;
				EndianUtils.writeLEShort(1, buffer, 4);
				EndianUtils.writeLEShort(32, buffer, 6);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				if (!StdImage.writePNG(baos, getImage(size), 72)) {
					throw new IOException(UNABLE_TO_CREATE_PNG);
				}
				byte[] bytes = baos.toByteArray();
				images.add(bytes);
				EndianUtils.writeLEInt(bytes.length, buffer, 8);
				EndianUtils.writeLEInt(6 + buffer.length * count + totalImageBytes, buffer, 12);
				out.write(buffer);
				totalImageBytes = bytes.length;
			}
		}
		for (byte[] bytes : images) {
			out.write(bytes);
		}
	}
}
