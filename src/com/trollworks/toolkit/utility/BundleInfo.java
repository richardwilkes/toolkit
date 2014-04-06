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

package com.trollworks.toolkit.utility;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.io.UrlUtils;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

/** Provides information for a bundle of code. */
public class BundleInfo {
	@Localize("Development")
	private static String		DEVELOPMENT;
	@Localize("Unspecified")
	private static String		UNSPECIFIED;
	@Localize("%s %s\n%s\n%s")
	private static String		APP_BANNER_FORMAT;
	@Localize("Copyright \u00A9 %s by %s")
	private static String		COPYRIGHT_FORMAT;
	@Localize("All rights reserved")
	private static String		ALL_RIGHTS_RESERVED;
	@Localize("%s. %s.")
	private static String		COPYRIGHT_BANNER_FORMAT;

	static {
		Localization.initialize();
	}

	private static BundleInfo	DEFAULT;
	private String				mName;
	private long				mVersion;
	private String				mCopyrightOwner;
	private String				mCopyrightYears;
	private String				mLicense;

	/** @param bundleInfo The {@link BundleInfo} to use for the application. */
	public static final synchronized void setDefault(BundleInfo bundleInfo) {
		DEFAULT = bundleInfo;
	}

	/**
	 * @return The application's {@link BundleInfo}. If one has not been explicitly set, then the
	 *         {@link BundleInfo} for the {@link BundleInfo} class will be returned.
	 */
	public static final synchronized BundleInfo getDefault() {
		if (DEFAULT == null) {
			DEFAULT = new BundleInfo(BundleInfo.class);
		}
		return DEFAULT;
	}

	/**
	 * Loads the bundle information out of the manifest, if it is available. If not, the some
	 * default values are setup and the version is marked as 'Development'. These attributes should
	 * be set in the jar's manifest file:
	 * <table>
	 * <tr>
	 * <th>key</th>
	 * <th>description</th>
	 * </tr>
	 * <tr>
	 * <td>bundle-name</td>
	 * <td>The name of the code bundle.</td>
	 * </tr>
	 * <tr>
	 * <td>bundle-version</td>
	 * <td>The version of the code bundle. May be any format compliant with {@link Version}.</td>
	 * </tr>
	 * <tr>
	 * <td>bundle-copyright-owner</td>
	 * <td>The copyright owner of the code bundle.</td>
	 * </tr>
	 * <tr>
	 * <td>bundle-copyright-years</td>
	 * <td>The copyright years of the code bundle.</td>
	 * </tr>
	 * <tr>
	 * <td>bundle-license</td>
	 * <td>The license the code bundle uses.</td>
	 * </tr>
	 * </table>
	 *
	 * @param theClass A class in the code bundle.
	 */
	public BundleInfo(Class<?> theClass) {
		try {
			Manifest manifest = UrlUtils.loadManifest(theClass);
			Attributes attributes = manifest.getMainAttributes();
			mName = attributes.getValue("bundle-name"); //$NON-NLS-1$
			mVersion = Version.extract(attributes.getValue("bundle-version"), 0); //$NON-NLS-1$
			mCopyrightOwner = attributes.getValue("bundle-copyright-owner"); //$NON-NLS-1$
			mCopyrightYears = attributes.getValue("bundle-copyright-years"); //$NON-NLS-1$
			mLicense = attributes.getValue("bundle-license"); //$NON-NLS-1$
		} catch (Exception exception) {
			// Ignore... we'll fill in default values below
		}
		if (mName == null || mName.trim().isEmpty()) {
			Package pkg = theClass.getPackage();
			mName = pkg != null ? pkg.getName() : theClass.getName();
		}
		if (mCopyrightOwner == null || mCopyrightOwner.trim().isEmpty()) {
			mCopyrightOwner = UNSPECIFIED;
		}
		if (mCopyrightYears == null || mCopyrightYears.trim().isEmpty()) {
			mCopyrightYears = UNSPECIFIED;
		}
		if (mLicense == null || mLicense.trim().isEmpty()) {
			mLicense = UNSPECIFIED;
		}
	}

	/**
	 * Create a {@link BundleInfo} with explicitly set values.
	 *
	 * @param name The name of the code bundle.
	 * @param version The version of the code bundle. May be any format compliant with
	 *            {@link Version}.
	 * @param copyrightOwner The copyright owner of the code bundle.
	 * @param copyrightYears The copyright years of the code bundle.
	 * @param license The license the code bundle uses.
	 */
	public BundleInfo(String name, String version, String copyrightOwner, String copyrightYears, String license) {
		mName = name;
		mVersion = Version.extract(version, 0);
		mCopyrightOwner = copyrightOwner;
		mCopyrightYears = copyrightYears;
		mLicense = license;
	}

	/** @return The name of the code bundle. */
	public String getName() {
		return mName;
	}

	/** @return The version of the code bundle. */
	public long getVersion() {
		return mVersion;
	}

	/** @return The copyright owner of the code bundle. */
	public String getCopyrightOwner() {
		return mCopyrightOwner;
	}

	/** @return The copyright years of the code bundle. */
	public String getCopyrightYears() {
		return mCopyrightYears;
	}

	/** @return The formatted copyright notice for the code bundle. */
	public String getCopyright() {
		return String.format(COPYRIGHT_FORMAT, mCopyrightYears, mCopyrightOwner);
	}

	/** @return The rights declaration for the code bundle, usually "All rights reserved". */
	@SuppressWarnings("static-method")
	public String getReservedRights() {
		return ALL_RIGHTS_RESERVED;
	}

	/** @return A full copyright banner for this code bundle. */
	public String getCopyrightBanner() {
		return String.format(COPYRIGHT_BANNER_FORMAT, getCopyright(), getReservedRights());
	}

	/** @return The license the code bundle uses. */
	public String getLicense() {
		return mLicense;
	}

	/** @return A full application banner for this code bundle. */
	public String getAppBanner() {
		return String.format(APP_BANNER_FORMAT, getName(), Version.toString(mVersion, false), Version.toBuildTimestamp(mVersion), getCopyrightBanner());
	}
}
