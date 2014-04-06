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

package com.trollworks.toolkit.utility.cmdline;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.utility.BundleInfo;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.Platform;
import com.trollworks.toolkit.utility.text.TextUtility;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/** Provides standardized command-line argument parsing. */
public class CmdLine {
	@Localize("Available options:")
	private static String				AVAILABLE_OPTIONS;
	@Localize("Unknown option \"{0}\".")
	private static String				UNEXPECTED_OPTION;
	@Localize("The option \"{0}\" does not take an argument.")
	private static String				UNEXPECTED_OPTION_ARGUMENT;
	@Localize("The option \"{0}\" requires an argument.")
	private static String				MISSING_OPTION_ARGUMENT;
	@Localize("Displays a description of each option.")
	private static String				HELP_DESCRIPTION;
	@Localize("Displays the program version information.")
	private static String				VERSION_DESCRIPTION;
	@Localize("The same as the \"{0}{1}\" option.")
	private static String				REFERENCE_DESCRIPTION;

	static {
		Localization.initialize();
	}

	private static final CmdLineOption	HELP_OPTION		= new CmdLineOption(HELP_DESCRIPTION, null, "h", "?", "help");	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final CmdLineOption	VERSION_OPTION	= new CmdLineOption(VERSION_DESCRIPTION, null, "v", "version"); //$NON-NLS-1$ //$NON-NLS-2$
	private ArrayList<CmdLineData>		mData;
	private HashSet<CmdLineOption>		mUsedOptions;

	/**
	 * Creates a new {@link CmdLine}. If the command line arguments are invalid, or the version
	 * number or help is requested, then this constructor will cause the program to exit.
	 *
	 * @param args The arguments passed to <code>main</code>.
	 */
	public CmdLine(String[] args) {
		this(args, null, new ArrayList<CmdLineOption>());
	}

	/**
	 * Creates a new {@link CmdLine}. If the command line arguments are invalid, or the version
	 * number or help is requested, then this constructor will cause the program to exit.
	 *
	 * @param args The arguments passed to <code>main</code>.
	 * @param options Valid options.
	 */
	public CmdLine(String[] args, CmdLineOption... options) {
		this(args, null, Arrays.asList(options));
	}

	/**
	 * Creates a new {@link CmdLine}. If the command line arguments are invalid, or the version
	 * number or help is requested, then this constructor will cause the program to exit.
	 *
	 * @param args The arguments passed to <code>main</code>.
	 * @param options Valid options.
	 */
	public CmdLine(String[] args, Collection<CmdLineOption> options) {
		this(args, null, options);
	}

	/**
	 * Creates a new {@link CmdLine}. If the command line arguments are invalid, or the version
	 * number or help is requested, then this constructor will cause the program to exit.
	 *
	 * @param args The arguments passed to <code>main</code>.
	 * @param extraHelp Any text that you would like appended to the end of the help output.
	 * @param options Valid options.
	 */
	public CmdLine(String[] args, String extraHelp, Collection<CmdLineOption> options) {
		HashMap<String, CmdLineOption> map = new HashMap<>();
		ArrayList<CmdLineOption> all = new ArrayList<>(options);
		ArrayList<String> msgs = new ArrayList<>();

		all.add(HELP_OPTION);
		all.add(VERSION_OPTION);

		for (CmdLineOption option : all) {
			for (String name : option.getNames()) {
				map.put(name, option);
			}
		}

		mData = new ArrayList<>();
		mUsedOptions = new HashSet<>();

		for (int i = 0; i < args.length; i++) {
			String one = args[i];

			if (i == 0 && Platform.isMacintosh() && args[i].startsWith("-psn_")) { //$NON-NLS-1$
				continue;
			}

			if (hasOptionPrefix(one)) {
				String part = one.substring(one.startsWith("--") ? 2 : 1); //$NON-NLS-1$
				String name = part.toLowerCase();
				int index = name.indexOf('=');
				String arg;
				CmdLineOption option;

				if (index != -1) {
					arg = part.substring(index + 1);
					name = name.substring(0, index);
				} else {
					arg = null;
				}

				option = map.get(name);
				if (option != null) {
					if (option.takesArgument()) {
						if (arg != null) {
							mData.add(new CmdLineData(option, arg));
							mUsedOptions.add(option);
						} else {
							if (++i < args.length) {
								arg = args[i];
								if (hasOptionPrefix(arg)) {
									msgs.add(MessageFormat.format(MISSING_OPTION_ARGUMENT, one));
									i--;
								} else {
									mData.add(new CmdLineData(option, arg));
									mUsedOptions.add(option);
								}
							} else {
								msgs.add(MessageFormat.format(MISSING_OPTION_ARGUMENT, one));
							}
						}
					} else if (arg != null) {
						msgs.add(MessageFormat.format(UNEXPECTED_OPTION_ARGUMENT, one));
					} else {
						mData.add(new CmdLineData(option));
						mUsedOptions.add(option);
					}
				} else {
					msgs.add(MessageFormat.format(UNEXPECTED_OPTION, one));
				}
			} else {
				mData.add(new CmdLineData(one));
			}
		}

		if (mUsedOptions.contains(HELP_OPTION)) {
			showHelp(map, extraHelp);
		}

		if (mUsedOptions.contains(VERSION_OPTION)) {
			System.out.println();
			System.out.println(BundleInfo.getDefault().getAppBanner());
			System.out.println();
			System.exit(0);
		}

		if (!msgs.isEmpty()) {
			for (String msg : msgs) {
				System.out.println(msg);
			}
			System.exit(1);
		}
	}

	private static boolean hasOptionPrefix(String arg) {
		return arg.startsWith("-") || Platform.isWindows() && arg.startsWith("/"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static void showHelp(HashMap<String, CmdLineOption> options, String extraHelp) {
		ArrayList<String> names = new ArrayList<>(options.keySet());
		int cmdWidth = 0;

		Collections.sort(names);

		System.out.println();
		System.out.println(BundleInfo.getDefault().getAppBanner());
		System.out.println();
		System.out.println(AVAILABLE_OPTIONS);
		System.out.println();

		for (String name : names) {
			CmdLineOption option = options.get(name);
			int width = 5 + name.length();

			if (option.takesArgument()) {
				width += 1 + option.getArgumentLabel().length();
			}
			if (width > cmdWidth) {
				cmdWidth = width;
			}
		}

		for (String name : names) {
			CmdLineOption option = options.get(name);

			StringBuilder builder = new StringBuilder();
			String[] allNames = option.getNames();
			String prefix = Platform.isWindows() ? "/" : "-"; //$NON-NLS-1$ //$NON-NLS-2$
			String description;

			if (allNames[allNames.length - 1].equals(name)) {
				description = option.getDescription();
			} else {
				description = MessageFormat.format(REFERENCE_DESCRIPTION, prefix, allNames[allNames.length - 1]);
			}
			builder.append("  "); //$NON-NLS-1$
			builder.append(prefix);
			builder.append(name);
			if (option.takesArgument()) {
				builder.append('=');
				builder.append(option.getArgumentLabel());
			}
			builder.append(TextUtility.makeFiller(cmdWidth - builder.length(), ' '));
			System.out.print(TextUtility.makeNote(builder.toString(), TextUtility.wrapToCharacterCount(description, 75 - cmdWidth)));
		}

		if (extraHelp != null) {
			System.out.println();
			System.out.println(extraHelp);
		}
		System.out.println();
		System.exit(0);
	}

	/**
	 * @param option The option to check for.
	 * @return Whether the option was present on the command line or not.
	 */
	public boolean isOptionUsed(CmdLineOption option) {
		return mUsedOptions.contains(option);
	}

	/**
	 * @param option The option to return the argument for.
	 * @return The option's argument.
	 */
	public String getOptionArgument(CmdLineOption option) {
		if (isOptionUsed(option)) {
			for (CmdLineData one : mData) {
				if (one.isOption() && one.getOption() == option) {
					return one.getArgument();
				}
			}
		}
		return null;
	}

	/**
	 * @param option The option to return the arguments for.
	 * @return The option's arguments.
	 */
	public ArrayList<String> getOptionArguments(CmdLineOption option) {
		ArrayList<String> list = new ArrayList<>();

		if (isOptionUsed(option)) {
			for (CmdLineData one : mData) {
				if (one.isOption() && one.getOption() == option) {
					list.add(one.getArgument());
				}
			}
		}
		return list;
	}

	/** @return The arguments that were not options. */
	public ArrayList<String> getArguments() {
		ArrayList<String> arguments = new ArrayList<>();

		for (CmdLineData one : mData) {
			if (!one.isOption()) {
				arguments.add(one.getArgument());
			}
		}
		return arguments;
	}

	/** @return The arguments that were not options. */
	public ArrayList<File> getArgumentsAsFiles() {
		ArrayList<File> arguments = new ArrayList<>();

		for (CmdLineData one : mData) {
			if (!one.isOption()) {
				arguments.add(new File(one.getArgument()));
			}
		}
		return arguments;
	}
}
