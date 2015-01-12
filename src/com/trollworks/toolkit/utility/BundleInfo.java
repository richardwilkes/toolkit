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

package com.trollworks.toolkit.utility;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.io.UrlUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.jar.Attributes;

/** Provides information for a bundle of code. */
public class BundleInfo {
	@Localize("Development")
	@Localize(locale = "ru", value = "Разработка")
	@Localize(locale = "de", value = "Entwicklung")
	@Localize(locale = "es", value = "Desarrollo")
	private static String		DEVELOPMENT;
	@Localize("Unspecified")
	@Localize(locale = "ru", value = "Не указан")
	@Localize(locale = "de", value = "Nicht angegeben")
	@Localize(locale = "es", value = "No especificado")
	private static String		UNSPECIFIED;
	@Localize("%s %s\n%s\n%s")
	private static String		APP_BANNER_FORMAT;
	@Localize("Copyright \u00A9 %s by %s")
	@Localize(locale = "ru", value = "Авторское право \u00A9 %s от %s")
	@Localize(locale = "de", value = "Copyright \u00A9 %2$s, %1$s")
	@Localize(locale = "es", value = "Copyright \u00A9 %2$s, %1$s")
	private static String		COPYRIGHT_FORMAT;
	@Localize("All rights reserved")
	@Localize(locale = "ru", value = "Все права защищены")
	@Localize(locale = "de", value = "Alle Rechte vorbehalten")
	@Localize(locale = "es", value = "Reservados todos los derechos")
	private static String		ALL_RIGHTS_RESERVED;
	@Localize("%s. %s.")
	private static String		COPYRIGHT_BANNER_FORMAT;

	static {
		Localization.initialize();
	}

	public static final String	BUNDLE_NAME				= "bundle-name";			//$NON-NLS-1$
	public static final String	BUNDLE_VERSION			= "bundle-version";		//$NON-NLS-1$
	public static final String	BUNDLE_COPYRIGHT_OWNER	= "bundle-copyright-owner"; //$NON-NLS-1$
	public static final String	BUNDLE_COPYRIGHT_YEARS	= "bundle-copyright-years"; //$NON-NLS-1$
	public static final String	BUNDLE_LICENSE			= "bundle-license";		//$NON-NLS-1$
	public static final String	BUNDLE_EXECUTABLE		= "bundle-executable";		//$NON-NLS-1$
	public static final String	BUNDLE_ID				= "bundle-id";				//$NON-NLS-1$
	public static final String	BUNDLE_SIGNATURE		= "bundle-signature";		//$NON-NLS-1$
	public static final String	BUNDLE_CATEGORY			= "bundle-category";		//$NON-NLS-1$
	private static BundleInfo	DEFAULT;
	private String				mName;
	private long				mVersion;
	private String				mCopyrightOwner;
	private String				mCopyrightYears;
	private String				mLicense;
	private String				mExecutableName;
	private String				mId;
	private String				mSignature;
	private String				mCategory;
	private PrintWriter			mOut;
	private int					mDepth;

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
	 * Loads the bundle information out of the manifest, if it is available.
	 *
	 * @param theClass A class in the code bundle.
	 */
	public BundleInfo(Class<?> theClass) {
		try {
			load(UrlUtils.loadManifest(theClass).getMainAttributes());
		} catch (Exception exception) {
			// Ignore... we'll fill in default values below
		}
		Package pkg = theClass.getPackage();
		validate(pkg != null ? pkg.getName() : theClass.getName());
	}

	/**
	 * Loads the bundle information out of the provided {@link Attributes}. These attributes should
	 * be set:
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
	 * <tr>
	 * <td>bundle-exe</td>
	 * <td>The name of the executable the code bundle is launched by.</td>
	 * </tr>
	 * <tr>
	 * <td>bundle-id</td>
	 * <td>The id of the code bundle.</td>
	 * </tr>
	 * <tr>
	 * <td>bundle-signature</td>
	 * <td>The signature of the code bundle.</td>
	 * </tr>
	 * <tr>
	 * <td>bundle-category</td>
	 * <td>The category of the code bundle.</td>
	 * </tr>
	 * </table>
	 *
	 * @param attributes The {@link Attributes} to load the information from.
	 * @param defaultName The default name to use, if the 'bundle-name' attribute isn't found.
	 */
	public BundleInfo(Attributes attributes, String defaultName) {
		load(attributes);
		validate(defaultName);
	}

	private void load(Attributes attributes) {
		mName = attributes.getValue(BUNDLE_NAME);
		mVersion = Version.extract(attributes.getValue(BUNDLE_VERSION), 0);
		mCopyrightOwner = attributes.getValue(BUNDLE_COPYRIGHT_OWNER);
		mCopyrightYears = attributes.getValue(BUNDLE_COPYRIGHT_YEARS);
		mLicense = attributes.getValue(BUNDLE_LICENSE);
		mExecutableName = attributes.getValue(BUNDLE_EXECUTABLE);
		mId = attributes.getValue(BUNDLE_ID);
		mSignature = attributes.getValue(BUNDLE_SIGNATURE);
		mCategory = attributes.getValue(BUNDLE_CATEGORY);
	}

	private void validate(String defaultName) {
		if (mName == null || mName.trim().isEmpty()) {
			mName = defaultName;
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
		if (mExecutableName == null) {
			mExecutableName = ""; //$NON-NLS-1$
		}
		if (mId == null) {
			mId = ""; //$NON-NLS-1$
		}
		if (mSignature == null) {
			mSignature = ""; //$NON-NLS-1$
		}
		if (mCategory == null) {
			mCategory = ""; //$NON-NLS-1$
		}
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

	/** @return The name of the executable the code bundle is launched by. */
	public String getExecutableName() {
		return mExecutableName;
	}

	/** @return The id of the code bundle. */
	public String getId() {
		return mId;
	}

	/** @return The signature of the code bundle. */
	public String getSignature() {
		return mSignature;
	}

	/** @return The category of the code bundle. */
	public String getCategory() {
		return mCategory;
	}

	/**
	 * Creates an Info.plist file based on this {@link BundleInfo}. Before calling this method, make
	 * sure your {@link FileType}s are registered.
	 *
	 * @param file The Info.plist file to create.
	 */
	@SuppressWarnings("nls")
	public void write(File file) {
		try (PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file)))) {
			mOut = out;
			mDepth = 0;
			mOut.println("<?xml version=\"1.0\" ?>");
			mOut.println("<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">");
			mOut.println("<plist version=\"1.0\">");
			mDepth++;
			startDictionary();
			emitKeyValue("CFBundleDevelopmentRegion", "English");
			emitKeyValue("CFBundleExecutable", mExecutableName);
			emitKeyValue("CFBundleIconFile", "app.icns");
			emitKeyValue("CFBundleIdentifier", mId);
			emitKeyValue("CFBundleName", mName);
			emitKeyValue("CFBundleInfoDictionaryVersion", "6.0");
			emitKeyValue("CFBundlePackageType", "APPL");
			emitKeyValue("CFBundleShortVersionString", Version.toString(mVersion, false));
			emitKeyValue("CFBundleVersion", Version.toString(mVersion, true));
			emitKeyValue("CFBundleSignature", mSignature);
			emitKeyValue("LSApplicationCategoryType", mCategory);
			emitKeyValues("LSArchitecturePriority", "x86_64", "i386");
			emitKey("LSEnvironment");
			startDictionary();
			emitKeyValue("LC_CTYPE", "UTF-8");
			endDictionary();
			emitKeyValue("LSMinimumSystemVersion", "10.7.0");
			emitKeyValue("NSHumanReadableCopyright", getCopyrightBanner());
			emitKeyValue("NSHighResolutionCapable", true);
			emitKeyValue("NSSupportsAutomaticGraphicsSwitching", true);
			FileType[] openable = FileType.getOpenable();
			if (openable.length > 0) {
				emitKey("CFBundleDocumentTypes");
				startArray();
				for (FileType type : openable) {
					startDictionary();
					emitKeyValue("CFBundleTypeName", type.getDescription());
					String extension = type.getExtension();
					emitKeyValue("CFBundleTypeIconFile", extension + ".icns");
					emitKeyValue("CFBundleTypeRole", "Editor");
					emitKeyValue("LSHandlerRank", "Owner");
					emitKeyValues("LSItemContentTypes", mId + "." + extension);
					endDictionary();
				}
				endArray();
				emitKey("UTExportedTypeDeclarations");
				startArray();
				for (FileType type : openable) {
					startDictionary();
					String extension = type.getExtension();
					emitKeyValue("UTTypeIdentifier", mId + "." + extension);
					emitKeyValue("UTTypeReferenceURL", type.getReferenceURL());
					emitKeyValue("UTTypeDescription", type.getDescription());
					emitKeyValue("UTTypeIconFile", extension + ".icns");
					emitKeyValues("UTTypeConformsTo", "public.xml", "public.data");
					emitKey("UTTypeTagSpecification");
					startDictionary();
					emitKeyValue("com.apple.ostype", "." + extension);
					emitKeyValues("public.filename-extension", extension);
					emitKeyValue("public.mime-type", "application/" + mExecutableName + "." + extension);
					endDictionary();
					endDictionary();
				}
				endArray();
			}
			endDictionary();
			mDepth--;
			mOut.println("</plist>");
			mOut.flush();
		} catch (Exception exception) {
			Log.error(exception);
		}
	}

	private void startDictionary() {
		emitTabs();
		mOut.println("<dict>"); //$NON-NLS-1$
		mDepth++;
	}

	private void endDictionary() {
		mDepth--;
		emitTabs();
		mOut.println("</dict>"); //$NON-NLS-1$
	}

	private void startArray() {
		emitTabs();
		mOut.println("<array>"); //$NON-NLS-1$
		mDepth++;
	}

	private void endArray() {
		mDepth--;
		emitTabs();
		mOut.println("</array>"); //$NON-NLS-1$
	}

	public void emitKey(String key) {
		emitTabs();
		mOut.print("<key>"); //$NON-NLS-1$
		mOut.print(key);
		mOut.println("</key>"); //$NON-NLS-1$
	}

	private void emitKeyValue(String key, boolean value) {
		emitKey(key);
		emitTabs();
		mOut.print('<');
		mOut.print(value);
		mOut.println("/>"); //$NON-NLS-1$
	}

	private void emitKeyValue(String key, String value) {
		emitKey(key);
		emitTabs();
		mOut.print("<string>"); //$NON-NLS-1$
		mOut.print(value);
		mOut.println("</string>"); //$NON-NLS-1$
	}

	private void emitKeyValues(String key, String... values) {
		emitKey(key);
		startArray();
		for (String value : values) {
			emitTabs();
			mOut.print("<string>"); //$NON-NLS-1$
			mOut.print(value);
			mOut.println("</string>"); //$NON-NLS-1$
		}
		endArray();
	}

	private void emitTabs() {
		for (int i = 0; i < mDepth; i++) {
			mOut.print('\t');
		}
	}
}
