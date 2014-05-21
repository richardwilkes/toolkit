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
import com.trollworks.toolkit.utility.BundleInfo;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.cmdline.CmdLine;
import com.trollworks.toolkit.utility.cmdline.CmdLineOption;

import java.io.File;

public class IconSetCreator {
	@Localize("1.0.0.20140520000000")
	private static String				VERSION;
	@Localize("Richard A. Wilkes")
	private static String				COPYRIGHT_OWNER;
	@Localize("2014")
	private static String				COPYRIGHT_YEARS;
	@Localize("Mozilla Public License 2.0")
	private static String				LICENSE;
	@Localize("The base prefix to use. An image size and png extension will be appended to this to find the base images. At a minimum, a 1024 pixel image must be present.")
	private static String				BASE_DESCRIPTION;
	@Localize("The overlay prefix to use. An image size and png extension will be appended to this to find the overlay images. At a minimum, a 1024 pixel image must be present.")
	private static String				OVERLAY_DESCRIPTION;
	@Localize("The directory to store the resulting files into.")
	private static String				OUTPUT_DESCRIPTION;
	@Localize("DIRECTORY")
	private static String				DIRECTORY;
	@Localize("The %s option must be specified.\n")
	private static String				MUST_BE_SPECIFIED;
	@Localize("The %s option must be an existing file.\n")
	private static String				MUST_BE_FILE;
	@Localize("The %s option may not point to an existing directory.\n")
	private static String				ALREADY_EXISTS;
	@Localize("Unable to load: %s\n")
	private static String				UNABLE_TO_LOAD;
	@Localize("Unable to write: %s\n")
	private static String				UNABLE_TO_WRITE;
	@Localize("Unexpected argument: %s\n")
	private static String				UNEXPECTED_ARGUMENT;

	static {
		Localization.initialize();
	}

	private static final CmdLineOption	BASE_DIR_OPTION		= new CmdLineOption(BASE_DESCRIPTION, DIRECTORY, "base_prefix");	//$NON-NLS-1$
	private static final CmdLineOption	OVERLAY_DIR_OPTION	= new CmdLineOption(OVERLAY_DESCRIPTION, DIRECTORY, "overlay");	//$NON-NLS-1$
	private static final CmdLineOption	OUTPUT_DIR_OPTION	= new CmdLineOption(OUTPUT_DESCRIPTION, DIRECTORY, "o", "output");	//$NON-NLS-1$ //$NON-NLS-2$

	public static void main(String[] args) {
		BundleInfo.setDefault(new BundleInfo(IconSetCreator.class.getSimpleName(), VERSION, COPYRIGHT_OWNER, COPYRIGHT_YEARS, LICENSE));
		CmdLine cmdline = new CmdLine();
		cmdline.addOptions(BASE_DIR_OPTION, OVERLAY_DIR_OPTION, OUTPUT_DIR_OPTION);
		cmdline.processArguments(args);
		File baseFile = getPrefixedFile(cmdline, BASE_DIR_OPTION);
		File overlayFile = getPrefixedFile(cmdline, OVERLAY_DIR_OPTION);
		File outputDir = getDir(cmdline, OUTPUT_DIR_OPTION);
		if (cmdline.isOptionUsed(BASE_DIR_OPTION) && overlayFile != null && outputDir != null) {
			if (!cmdline.getArguments().isEmpty()) {
				for (String one : cmdline.getArguments()) {
					System.err.printf(UNEXPECTED_ARGUMENT, one);
				}
				System.exit(1);
			}
			ToolkitIcon base = Images.loadImage(baseFile);
			if (base == null) {
				System.err.printf(UNABLE_TO_LOAD, baseFile);
				System.exit(1);
			}
			ToolkitIcon overlay = Images.loadImage(overlayFile);
			if (overlay == null) {
				System.err.printf(UNABLE_TO_LOAD, overlayFile);
				System.exit(1);
			}

			outputDir.mkdirs();
			Resolution[] resolutions = { new Resolution(1024, false, true),
							new Resolution(512, true, true),
							new Resolution(256, true, true),
							new Resolution(128, true, false),
							new Resolution(64, false, true),
							new Resolution(48, true, false),
							new Resolution(32, true, true),
							new Resolution(16, true, false) };
			for (Resolution resolution : resolutions) {
				int size = resolution.getSize();
				ToolkitIcon scaledBase;
				if (size != 1024) {
					File scaledBaseFile = new File(cmdline.getOptionArgument(BASE_DIR_OPTION) + size + ".png"); //$NON-NLS-1$
					if (scaledBaseFile.isFile()) {
						scaledBase = Images.loadImage(scaledBaseFile);
						if (scaledBase == null) {
							System.err.printf(UNABLE_TO_LOAD, scaledBaseFile);
							System.exit(1);
						}
					} else {
						scaledBase = Images.scale(base, size, size);
					}
				} else {
					scaledBase = base;
				}
				ToolkitIcon img = Images.superimpose(createScaled(cmdline, BASE_DIR_OPTION, resolution.getSize(), base), createScaled(cmdline, OVERLAY_DIR_OPTION, resolution.getSize(), overlay));
				for (String title : resolution.getTitles()) {
					File file = new File(outputDir, title);
					if (!Images.writePNG(file, img, 72)) {
						System.err.printf(UNABLE_TO_WRITE, file);
						System.exit(1);
					}
				}
			}
			System.exit(0);
		}
		System.exit(1);
	}

	private static ToolkitIcon createScaled(CmdLine cmdline, CmdLineOption option, int size, ToolkitIcon base) {
		File file = getPrefixedFile(cmdline, option, size);
		if (file.isFile()) {
			ToolkitIcon img = Images.loadImage(file);
			if (img == null) {
				System.err.printf(UNABLE_TO_LOAD, file);
				System.exit(1);
			}
			return img;
		}
		return Images.scale(base, size, size);
	}

	private static File getPrefixedFile(CmdLine cmdline, CmdLineOption option, int size) {
		return new File(cmdline.getOptionArgument(option) + size + ".png"); //$NON-NLS-1$
	}

	private static File getPrefixedFile(CmdLine cmdline, CmdLineOption option) {
		if (!cmdline.isOptionUsed(option)) {
			String[] names = option.getNames();
			System.err.printf(MUST_BE_SPECIFIED, names[names.length - 1]);
		} else {
			File file = getPrefixedFile(cmdline, option, 1024);
			if (file.isFile()) {
				return file;
			}
			System.err.printf(MUST_BE_FILE, cmdline.getOptionArgument(option));
		}
		return null;
	}

	private static File getDir(CmdLine cmdline, CmdLineOption option) {
		if (!cmdline.isOptionUsed(option)) {
			String[] names = option.getNames();
			System.err.printf(MUST_BE_SPECIFIED, names[names.length - 1]);
		} else {
			File file = new File(cmdline.getOptionArgument(option));
			if (!file.exists()) {
				return file;
			}
			System.err.printf(ALREADY_EXISTS, cmdline.getOptionArgument(option));
		}
		return null;
	}

	static class Resolution {
		private int			mSize;
		private String[]	mTitles;

		Resolution(int size, boolean includeNormal, boolean include2xAtHalfSize) {
			mSize = size;
			mTitles = new String[(includeNormal ? 1 : 0) + (include2xAtHalfSize ? 1 : 0)];
			int index = 0;
			if (includeNormal) {
				mTitles[index++] = "icon_" + mSize + "x" + mSize + ".png"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			if (include2xAtHalfSize) {
				size = mSize / 2;
				mTitles[index++] = "icon_" + size + "x" + size + "@2x.png"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}

		int getSize() {
			return mSize;
		}

		String[] getTitles() {
			return mTitles;
		}
	}
}
