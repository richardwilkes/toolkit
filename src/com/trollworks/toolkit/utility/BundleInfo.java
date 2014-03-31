/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is com.trollworks.toolkit.
 *
 * The Initial Developer of the Original Code is Richard A. Wilkes.
 * Portions created by the Initial Developer are Copyright (C) 1998-2014,
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */

package com.trollworks.toolkit.utility;

import static com.trollworks.toolkit.utility.BundleInfo_LS.*;

import com.trollworks.annotation.LS;
import com.trollworks.annotation.Localized;
import com.trollworks.toolkit.io.UrlUtils;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

/** Provides information for a bundle of code. */
@Localized({
				@LS(key = "DEVELOPMENT", msg = "Development"),
				@LS(key = "UNSPECIFIED", msg = "Unspecified"),
})
public class BundleInfo {
	private String	mName;
	private String	mVersion;
	private String	mLicense;
	private String	mCreator;
	private String	mCopyright;

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
	 * <td>The version of the code bundle, specified as <b>YYYYMMDD-HHMMSS</b>.</td>
	 * </tr>
	 * <tr>
	 * <td>bundle-license</td>
	 * <td>The license the code bundle uses.</td>
	 * </tr>
	 * <tr>
	 * <td>bundle-creator</td>
	 * <td>The creator of the code bundle.</td>
	 * </tr>
	 * <tr>
	 * <td>bundle-copyright</td>
	 * <td>The copyright of the code bundle.</td>
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
			mVersion = attributes.getValue("bundle-version"); //$NON-NLS-1$
			mLicense = attributes.getValue("bundle-license"); //$NON-NLS-1$
			mCreator = attributes.getValue("bundle-creator"); //$NON-NLS-1$
			mCopyright = attributes.getValue("bundle-copyright"); //$NON-NLS-1$
		} catch (Exception exception) {
			// Ignore... we'll fill in default values below
		}
		if (mName == null || mName.trim().isEmpty()) {
			Package pkg = theClass.getPackage();
			mName = pkg != null ? pkg.getName() : theClass.getName();
		}
		if (mVersion == null || mVersion.trim().isEmpty()) {
			mVersion = DEVELOPMENT;
		}
		if (mLicense == null || mLicense.trim().isEmpty()) {
			mLicense = UNSPECIFIED;
		}
		if (mCreator == null || mCreator.trim().isEmpty()) {
			mCreator = UNSPECIFIED;
		}
		if (mCopyright == null || mCopyright.trim().isEmpty()) {
			mCopyright = UNSPECIFIED;
		}
	}

	/** @return The name of the code bundle. */
	public String getName() {
		return mName;
	}

	/** @return The version of the code bundle, specified as <b>YYYYMMDD-HHMMSS</b>. */
	public String getVersion() {
		return mVersion;
	}

	/** @return The version of the code bundle. */
	public long getVersionAsNumber() {
		String[] parts = mVersion.split("-"); //$NON-NLS-1$
		if (parts.length == 2) {
			try {
				int date = Integer.parseInt(parts[0]);
				if (date > -1 && date < 100000000L) {
					int time = Integer.parseInt(parts[1]);
					if (time > -1 && time < 1000000L) {
						return date * 1000000L + time;
					}
				}
			} catch (NumberFormatException nfe) {
				// Ignore
			}
		}
		return 0;
	}

	/** @return The license the code bundle uses. */
	public String getLicense() {
		return mLicense;
	}

	/** @return The creator of the code bundle. */
	public String getCreator() {
		return mCreator;
	}

	/** @return The copyright of the code bundle. */
	public String getCopyright() {
		return mCopyright;
	}
}
