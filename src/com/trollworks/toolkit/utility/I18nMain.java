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

package com.trollworks.toolkit.utility;

import com.trollworks.toolkit.utility.text.Text;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides an entry point for extracting the strings used by calls to I18n.Text(). Note that this
 * is fairly naive, in that the strings are expected to be string literals and complete at the call
 * site, i.e. I18n.Text("string") and not things like I18n.Text("one" + " two") or
 * I18n.Text(STRING_CONSTANT).
 */
public class I18nMain {
    public static void main(String[] args) {
        Set<String> keys = new HashSet<>();
        Arrays.stream(args).flatMap(arg -> {
            try {
                return Files.walk(Path.of(arg));
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
                System.exit(1);
                return null;
            }
        }).filter(path -> {
            String lower = path.getFileName().toString().toLowerCase();
            return lower.endsWith(".java") && !lower.endsWith("i18n.java") && Files.isRegularFile(path) && Files.isReadable(path);
        }).distinct().forEach(path -> {
            try {
                Files.lines(path).forEachOrdered(line -> {
                    while (!line.isEmpty()) {
                        int i = line.indexOf("I18n.Text(");
                        if (i < 0) {
                            break;
                        }
                        int max = line.length();
                        i += 10;
                        while (i < max) {
                            char ch = line.charAt(i);
                            if (ch != ' ' && ch != '\t') {
                                break;
                            }
                            i++;
                        }
                        if (i >= max || line.charAt(i) != '"') {
                            break;
                        }
                        i++;
                        line = processLine(keys, line.substring(i));
                    }
                });
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
                System.exit(1);
            }
        });
        String filename = "template" + I18n.EXTENSION;
        try (PrintStream out = new PrintStream(filename, StandardCharsets.UTF_8)) {
            out.println("# Generated on " + new Date());
            out.println("#");
            out.println("# This file consists of UTF-8 text. Do not save it as anything else.");
            out.println("#");
            out.println("# Key-value pairs are defined as one or more lines prefixed with 'k:' for the");
            out.println("# key, followed by one or more lines prefixed with 'v:' for the value. These");
            out.println("# prefixes are then followed by a quoted string, as generated by Text.quote().");
            out.println("# When two or more lines are present in a row, they will be concatenated");
            out.println("# together with an intervening \\n character.");
            out.println("#");
            out.println("# Do NOT modify the 'k' values. They are the values as seen in the code.");
            out.println("#");
            out.println("# Replace the 'v' values with the appropriate translation.");
            keys.stream().sorted().forEachOrdered(key -> {
                out.println();
                String quoted = Text.quote(key);
                if (quoted.length() < 77) {
                    out.println("k:" + quoted);
                    out.println("v:" + quoted);
                } else {
                    String[] parts = key.split("\n", -1);
                    for (String part : parts) {
                        out.println("k:" + Text.quote(part));
                    }
                    for (String part : parts) {
                        out.println("v:" + Text.quote(part));
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        }
        System.out.println("Created " + filename);
    }

    private static String processLine(Set<String> keys, String in) {
        StringBuilder buffer       = new StringBuilder();
        int           len          = in.length();
        int           state        = 0;
        int           unicodeValue = 0;
        for (int i = 0; i < len; i++) {
            char ch = in.charAt(i);
            switch (state) {
            case 0: // Looking for end quote
                if (ch == '"') {
                    keys.add(buffer.toString());
                    return in.substring(i + 1);
                }
                if (ch == '\\') {
                    state = 1;
                    continue;
                }
                buffer.append(ch);
                break;
            case 1: // Processing escape sequence
                switch (ch) {
                case 't':
                    buffer.append('\t');
                    state = 0;
                    break;
                case 'b':
                    buffer.append('\b');
                    state = 0;
                    break;
                case 'n':
                    buffer.append('\n');
                    state = 0;
                    break;
                case 'r':
                    buffer.append('\r');
                    state = 0;
                    break;
                case '"':
                    buffer.append('"');
                    state = 0;
                    break;
                case '\\':
                    buffer.append('\\');
                    state = 0;
                    break;
                case 'u':
                    state = 2;
                    unicodeValue = 0;
                    break;
                default:
                    new RuntimeException("invalid escape sequence").printStackTrace(System.err);
                    System.exit(1);
                }
                break;
            case 2: // Processing first digit of unicode escape sequence
            case 3: // Processing second digit of unicode escape sequence
            case 4: // Processing third digit of unicode escape sequence
            case 5: // Processing fourth digit of unicode escape sequence
                if (!Text.isHexDigit(ch)) {
                    new RuntimeException("invalid unicode escape sequence").printStackTrace(System.err);
                    System.exit(1);
                }
                unicodeValue *= 16;
                unicodeValue += Text.hexDigitValue(ch);
                if (state == 5) {
                    state = 0;
                    buffer.append((char) unicodeValue);
                } else {
                    state++;
                }
                break;
            default:
                new RuntimeException("invalid state").printStackTrace(System.err);
                System.exit(1);
                break;
            }
        }
        return "";
    }
}
