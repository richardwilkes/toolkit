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

package com.trollworks.toolkit.utility.cmdline;

import com.trollworks.toolkit.utility.BundleInfo;
import com.trollworks.toolkit.utility.I18n;
import com.trollworks.toolkit.utility.Platform;
import com.trollworks.toolkit.utility.text.Text;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Provides standardized command-line argument parsing. */
public class CmdLine {
    private static final CmdLineOption              HELP_OPTION    = new CmdLineOption(I18n.Text("Displays a description of each option."), null, "h", "?", "help");
    private static final CmdLineOption              VERSION_OPTION = new CmdLineOption(I18n.Text("Displays the program version information."), null, "v", "version");
    private              Map<String, CmdLineOption> mOptions       = new HashMap<>();
    private              String                     mHelpHeader;
    private              String                     mHelpFooter;
    private              List<CmdLineData>          mData;
    private              Set<CmdLineOption>         mUsedOptions;

    public CmdLine() {
        addOptions(HELP_OPTION, VERSION_OPTION);
    }

    /** @param option A {@link CmdLineOption} to add. */
    public void addOption(CmdLineOption option) {
        for (String name : option.getNames()) {
            mOptions.put(name, option);
        }
    }

    /** @param options One or more {@link CmdLineOption}s to add. */
    public void addOptions(CmdLineOption... options) {
        for (CmdLineOption option : options) {
            addOption(option);
        }
    }

    /** @param header Text to display before the options are displayed in help. */
    public void setHelpHeader(String header) {
        mHelpHeader = header;
    }

    /** @param footer Text to display after the options are displayed in help. */
    public void setHelpFooter(String footer) {
        mHelpFooter = footer;
    }

    /**
     * Processes the specified command line arguments. If the command line arguments are invalid, or
     * the version number or help is requested, then this method will cause the program to exit.
     *
     * @param args The arguments passed to {@code main}.
     */
    public void processArguments(String[] args) {
        List<String> msgs = new ArrayList<>();
        mData = new ArrayList<>();
        mUsedOptions = new HashSet<>();
        int length = args.length;
        for (int i = 0; i < length; i++) {
            String one = args[i];

            if (i == 0 && Platform.isMacintosh() && args[i].startsWith("-psn_")) {
                continue;
            }

            if (hasOptionPrefix(one)) {
                String        part  = one.substring(one.startsWith("--") ? 2 : 1);
                String        name  = part.toLowerCase();
                int           index = name.indexOf('=');
                String        arg;
                CmdLineOption option;

                if (index == -1) {
                    arg = null;
                } else {
                    arg = part.substring(index + 1);
                    name = name.substring(0, index);
                }

                option = mOptions.get(name);
                if (option != null) {
                    if (option.takesArgument()) {
                        if (arg != null) {
                            mData.add(new CmdLineData(option, arg));
                            mUsedOptions.add(option);
                        } else {
                            String requiredArgMsgFmt = I18n.Text("The option \"{0}\" requires an argument.");
                            if (++i < args.length) {
                                arg = args[i];
                                if (hasOptionPrefix(arg)) {
                                    msgs.add(MessageFormat.format(requiredArgMsgFmt, one));
                                    i--;
                                } else {
                                    mData.add(new CmdLineData(option, arg));
                                    mUsedOptions.add(option);
                                }
                            } else {
                                msgs.add(MessageFormat.format(requiredArgMsgFmt, one));
                            }
                        }
                    } else if (arg != null) {
                        msgs.add(MessageFormat.format(I18n.Text("The option \"{0}\" does not take an argument."), one));
                    } else {
                        mData.add(new CmdLineData(option));
                        mUsedOptions.add(option);
                    }
                } else {
                    msgs.add(MessageFormat.format(I18n.Text("Unknown option \"{0}\"."), one));
                }
            } else {
                mData.add(new CmdLineData(one));
            }
        }

        if (mUsedOptions.contains(HELP_OPTION)) {
            showHelpAndExit();
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
        return arg.startsWith("-") || Platform.isWindows() && arg.startsWith("/");
    }

    /** Shows the help, then calls {@link System#exit(int)}. */
    public void showHelpAndExit() {
        List<String> names    = new ArrayList<>(mOptions.keySet());
        int          cmdWidth = 0;

        Collections.sort(names);

        System.out.println();
        System.out.println(BundleInfo.getDefault().getAppBanner());
        System.out.println();
        if (mHelpHeader != null) {
            System.out.println(mHelpHeader);
            System.out.println();
        }
        System.out.println(I18n.Text("Available options:"));
        System.out.println();

        for (String name : names) {
            CmdLineOption option = mOptions.get(name);
            int           width  = 5 + name.length();

            if (option.takesArgument()) {
                width += 1 + option.getArgumentLabel().length();
            }
            if (width > cmdWidth) {
                cmdWidth = width;
            }
        }

        for (String name : names) {
            CmdLineOption option      = mOptions.get(name);
            StringBuilder builder     = new StringBuilder();
            String[]      allNames    = option.getNames();
            String        prefix      = Platform.isWindows() ? "/" : "-";
            String        description = allNames[allNames.length - 1].equals(name) ? option.getDescription() : MessageFormat.format(I18n.Text("The same as the \"{0}{1}\" option."), prefix, allNames[allNames.length - 1]);
            builder.append("  ");
            builder.append(prefix);
            builder.append(name);
            if (option.takesArgument()) {
                builder.append('=');
                builder.append(option.getArgumentLabel());
            }
            builder.append(Text.makeFiller(cmdWidth - builder.length(), ' '));
            System.out.print(Text.makeNote(builder.toString(), Text.wrapToCharacterCount(description, 75 - cmdWidth)));
        }

        if (mHelpFooter != null) {
            System.out.println();
            System.out.println(mHelpFooter);
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
    public List<String> getOptionArguments(CmdLineOption option) {
        List<String> list = new ArrayList<>();

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
    public List<String> getArguments() {
        List<String> arguments = new ArrayList<>();

        for (CmdLineData one : mData) {
            if (!one.isOption()) {
                arguments.add(one.getArgument());
            }
        }
        return arguments;
    }

    /** @return The arguments that were not options. */
    public List<File> getArgumentsAsFiles() {
        List<File> arguments = new ArrayList<>();
        for (CmdLineData one : mData) {
            if (!one.isOption()) {
                arguments.add(new File(one.getArgument()).getAbsoluteFile());
            }
        }
        return arguments;
    }
}
