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

package com.trollworks.toolkit.ui.image;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Provides a set of images at different resolutions. */
public class StdImageSet implements Comparator<StdImage> {
    public static final int[]                    STD_SIZES = {1024, 512, 256, 128, 64, 48, 32, 16};
    private static      Map<String, StdImageSet> SETS      = new HashMap<>();
    private static      int                      SEQUENCE;
    private             String                   mName;
    private             StdImageSet[]            mLayers;
    private             List<StdImage>           mImages;
    private             int                      mSequence;

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
                StdImage img = StdImage.get(name + "_" + size);
                if (img == null) {
                    if (size == 16) {
                        // Try without the _16
                        img = StdImage.get(name);
                    } else if (size == 32) {
                        // Try with @2x instead of _32
                        img = StdImage.get(name + "@2x");
                    }
                }
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

    private static void track(String name, StdImage image) {
        StdImage.add("is:" + name + "_" + image.getWidth() + "x" + image.getHeight(), image);
    }

    /**
     * Creates a new {@link StdImageSet}.
     *
     * @param name   The name of this {@link StdImageSet}. This can be used to retrieve the {@link
     *               StdImageSet} later, via a call to {@link #get(String)}.
     * @param images The images that belong in this {@link StdImageSet}.
     */
    public StdImageSet(String name, List<StdImage> images) {
        mName = name;
        updateSequence();
        mImages = new ArrayList<>(images);
        mImages.sort(this);
        SETS.put(name, this);
    }

    /**
     * Creates a new {@link StdImageSet} that composites multiple images together from other {@link
     * StdImageSet}s to form its images.
     *
     * @param name   The name of this {@link StdImageSet}. This can be used to retrieve the {@link
     *               StdImageSet} later, via a call to {@link #get(String)}.
     * @param layers Two or more other {@link StdImageSet}s to use. Each one will be layered on top
     *               of the previous one, creating a single image for a given size.
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
        int result = Integer.compare(o2.getWidth(), o1.getWidth());
        if (result == 0) {
            result = Integer.compare(o2.getHeight(), o1.getHeight());
            if (result == 0) {
                result = Integer.compare(o2.hashCode(), o1.hashCode());
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
     * @return {@code true} if the image exists.
     */
    public boolean hasImage(int size) {
        return hasImage(size, size);
    }

    /**
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return {@code true} if the image exists.
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
     * @return An image from the set, or {@code null} if the desired dimensions cannot be found.
     */
    public StdImage getImageNoCreate(int size) {
        return getImageNoCreate(size, size);
    }

    /**
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return An image from the set, or {@code null} if the desired dimensions cannot be found.
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
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return An image from the set. If an exact match cannot be found, one of the existing images
     *         will be scaled to the desired size.
     */
    public StdImage getImage(int width, int height) {
        StdImage match = getImageNoCreate(width, height);
        if (match == null) {
            if (mLayers != null) {
                match = mLayers[0].getImage(width, height);
                int length = mLayers.length;
                for (int i = 1; i < length; i++) {
                    StdImage previous = match;
                    match = StdImage.superimpose(match, mLayers[i].getImage(width, height));
                    if (i > 1) {
                        previous.flush();
                    }
                }
            } else {
                StdImage inverseMatch = null;
                int      best         = Integer.MAX_VALUE;
                int      inverseBest  = Integer.MIN_VALUE;
                for (StdImage image : mImages) {
                    int imageWidth  = image.getWidth();
                    int imageHeight = image.getHeight();
                    int heuristic   = (imageWidth - width) * (imageHeight - height);
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
            mImages.sort(this);
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

    private void updateSequence() {
        synchronized (StdImageSet.class) {
            mSequence = ++SEQUENCE;
        }
    }
}
