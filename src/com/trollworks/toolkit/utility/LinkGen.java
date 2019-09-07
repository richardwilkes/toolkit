/*
 * Copyright (c) 1998-2019 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.utility;

import com.trollworks.toolkit.io.EndianUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/** Provides generation of Windows shortcut files. */
@SuppressWarnings("unused")
public class LinkGen {
    // Link flags
    private static final int HasLinkTargetIDList                = 1 << 0;
    private static final int HasLinkInfo                        = 1 << 1;
    private static final int HasName                            = 1 << 2;
    private static final int HasRelativePath                    = 1 << 3;
    private static final int HasWorkingDir                      = 1 << 4;
    private static final int HasArguments                       = 1 << 5;
    private static final int HasIconLocation                    = 1 << 6;
    private static final int IsUnicode                          = 1 << 7;
    private static final int ForceNoLinkInfo                    = 1 << 8;
    private static final int HasExpString                       = 1 << 9;
    private static final int RunInSeparateProcess               = 1 << 10;
    private static final int HasDarwinID                        = 1 << 12;
    private static final int RunAsUser                          = 1 << 13;
    private static final int HasExpIcon                         = 1 << 14;
    private static final int NoPidlAlias                        = 1 << 15;
    private static final int RunWithShimLayer                   = 1 << 17;
    private static final int ForceNoLinkTrack                   = 1 << 18;
    private static final int EnableTargetMetadata               = 1 << 19;
    private static final int DisableLinkPathTracking            = 1 << 20;
    private static final int DisableKnownFolderTracking         = 1 << 21;
    private static final int DisableKnownFolderAlias            = 1 << 22;
    private static final int AllowLinkToLink                    = 1 << 23;
    private static final int UnaliasOnSave                      = 1 << 24;
    private static final int PreferEnvironmentPath              = 1 << 25;
    private static final int KeepLocalIDListForUNCTarget        = 1 << 26;

    // FileAttributesFlags
    private static final int FILE_ATTRIBUTE_READONLY            = 1 << 0;
    private static final int FILE_ATTRIBUTE_HIDDEN              = 1 << 1;
    private static final int FILE_ATTRIBUTE_SYSTEM              = 1 << 2;
    private static final int FILE_ATTRIBUTE_DIRECTORY           = 1 << 4;
    private static final int FILE_ATTRIBUTE_ARCHIVE             = 1 << 5;
    private static final int FILE_ATTRIBUTE_NORMAL              = 1 << 7;
    private static final int FILE_ATTRIBUTE_TEMPORARY           = 1 << 8;
    private static final int FILE_ATTRIBUTE_SPARSE_FILE         = 1 << 9;
    private static final int FILE_ATTRIBUTE_REPARSE_POINT       = 1 << 10;
    private static final int FILE_ATTRIBUTE_COMPRESSED          = 1 << 11;
    private static final int FILE_ATTRIBUTE_OFFLINE             = 1 << 12;
    private static final int FILE_ATTRIBUTE_NOT_CONTENT_INDEXED = 1 << 13;
    private static final int FILE_ATTRIBUTE_ENCRYPTED           = 1 << 14;

    // ShowCommand
    private static final int SW_SHOWNORMAL                      = 1;
    private static final int SW_SHOWMAXIMIZED                   = 3;
    private static final int SW_SHOWMINNOACTIVE                 = 7;

    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println("Usage: linkgen <destination file path> <name> <relative path to target> <actual target path> <relative path to icon> [args...]");
            System.err.println("Note: relative path to icon may be an empty string to omit it");
            System.exit(1);
        }
        String        destinationFilePath  = args[0];
        String        name                 = args[1];
        String        relativePathToTarget = args[2];
        String        actualTargetPath     = args[3];
        String        relativePathToIcon   = args[4];
        StringBuilder buffer               = new StringBuilder();
        for (int i = 5; i < args.length; i++) {
            if (i != 5) {
                buffer.append(' ');
            }
            buffer.append(args[i]);
        }
        try {
            Create(destinationFilePath, name, relativePathToTarget, actualTargetPath, relativePathToIcon, buffer.toString());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }

    /**
     * Creates a Windows shortcut file, per the description at
     * https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-shllink
     *
     * @param destinationFilePath  the path to write the link file data to.
     * @param name                 the name of the shortcut.
     * @param relativePathToTarget a relative path to the target of the shortcut.
     * @param actualTargetPath     a path to the actual target -- this is used to determine the
     *                             target's file size.
     * @param relativePathToIcon   a relative path to an icon that should be used.
     * @param args                 the args to pass to the target when invoked.
     */
    public static void Create(String destinationFilePath, String name, String relativePathToTarget, String actualTargetPath, String relativePathToIcon, String args) throws IOException {
        File file = new File(actualTargetPath);
        long size = file.length();
        try (FileOutputStream out = new FileOutputStream(destinationFilePath)) {
            // Write the header
            writeInt(out, 0x4c); // Header size
            writeLong(out, 0x21401); // LinkCLSID, part 1
            writeLong(out, 0x46000000000000c0L); // LinkCLSID, part 2
            int linkFlags = HasName | HasRelativePath | IsUnicode | ForceNoLinkTrack | DisableKnownFolderTracking;
            if (args != null && !args.isBlank()) {
                linkFlags |= HasArguments;
            }
            if (relativePathToIcon != null && !relativePathToIcon.isBlank()) {
                linkFlags |= HasIconLocation;
            }
            writeInt(out, linkFlags); // LinkFlags
            writeInt(out, FILE_ATTRIBUTE_NORMAL); // FileAttributes
            writeLong(out, 0); // CreationTime
            writeLong(out, 0); // AccessTime
            writeLong(out, 0); // WriteTime
            writeInt(out, (int) (size & 0xFFFFFFFF)); // FileSize
            writeInt(out, 0); // IconIndex
            writeInt(out, SW_SHOWNORMAL); // ShowCommand
            writeShort(out, (short) 0); // HotKey
            writeShort(out, (short) 0); // Reserved1
            writeInt(out, 0); // Reserved2
            writeInt(out, 0); // Reserved3

            // Write string data
            writeString(out, name);
            writeString(out, relativePathToTarget);
            if (args != null && !args.isBlank()) {
                writeString(out, args);
            }
            if (relativePathToIcon != null && !relativePathToIcon.isBlank()) {
                writeString(out, relativePathToIcon);
            }
        }
    }

    private static void writeShort(FileOutputStream out, short value) throws IOException {
        byte[] buffer = new byte[2];
        EndianUtils.writeLEShort(value, buffer, 0);
        out.write(buffer);
    }

    private static void writeInt(FileOutputStream out, int value) throws IOException {
        byte[] buffer = new byte[4];
        EndianUtils.writeLEInt(value, buffer, 0);
        out.write(buffer);
    }

    private static void writeLong(FileOutputStream out, long value) throws IOException {
        byte[] buffer = new byte[8];
        EndianUtils.writeLELong(value, buffer, 0);
        out.write(buffer);
    }

    private static void writeString(FileOutputStream out, String str) throws IOException {
        int len = str.length();
        writeShort(out, (short) len);
        for (int i = 0; i < len; i++) {
            writeShort(out, (short) str.charAt(i));
        }
    }
}
