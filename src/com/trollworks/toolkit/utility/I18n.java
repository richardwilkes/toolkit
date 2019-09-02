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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Provides localization support via a single directory of translation files. */
@SuppressWarnings("nls")
public class I18n {
    public static final String               EXTENSION = ".i18n";
    private static I18n                      GLOBAL;
    private Map<String, Map<String, String>> mLangMap  = new HashMap<>();
    private Map<String, List<String>>        mHierMap  = new HashMap<>();
    private Locale                           mLocale   = Locale.getDefault();
    private String                           mLang     = mLocale.toString().toLowerCase();

    /** @return the global {@link I18n} object. */
    public static synchronized I18n getGlobal() {
        if (GLOBAL == null) {
            GLOBAL = new I18n(null);
        }
        return GLOBAL;
    }

    /** @param i18n the {@link I18n} to use as the global one. */
    public static synchronized void setGlobal(I18n i18n) {
        GLOBAL = i18n;
    }

    /**
     * Use the global {@link I18n} to localize some text.
     *
     * @param str the text to localize.
     * @return the localized version if one exists, or the original text if not.
     */
    public static String Text(String str) {
        return getGlobal().text(str);
    }

    /**
     * Creates a new I18n from the files at 'dir'.
     *
     * @param dir the directory to scan for localization files. If null, then a directory named
     *            'i18n' will be used.
     */
    public I18n(Path dir) {
        try {
            if (dir == null) {
                // user.dir may not be set correctly if this is a bundled app, so try PWD first
                String pwd = System.getenv("PWD");
                if (pwd == null || pwd.isEmpty()) {
                    pwd = System.getProperty("user.dir");
                }
                dir = Paths.get(pwd, "i18n").normalize().toAbsolutePath();
            }
            if (Files.isDirectory(dir)) {
                Files.list(dir).forEach(path -> {
                    if (path.toString().toLowerCase().endsWith(EXTENSION)) {
                        try {
                            final kvInfo kv = new kvInfo();
                            kv.translations = new HashMap<>();
                            Files.lines(path).forEachOrdered(line -> {
                                kv.line++;
                                if (line.startsWith("k:")) {
                                    if (kv.last == 'v') {
                                        if (!kv.translations.containsKey(kv.key)) {
                                            kv.translations.put(kv.key, kv.value);
                                        } else {
                                            System.err.println("ignoring duplicate key on line " + kv.lastKeyLineStart);
                                        }
                                        kv.key   = null;
                                        kv.value = null;
                                    }
                                    if (kv.key == null) {
                                        kv.key              = line.substring(2);
                                        kv.lastKeyLineStart = kv.line;
                                    } else {
                                        kv.key += '\n';
                                        kv.key += line.substring(2);
                                    }
                                    kv.last = 'k';
                                } else if (line.startsWith("v:")) {
                                    if (kv.key != null) {
                                        if (kv.value == null) {
                                            kv.value = line.substring(2);
                                        } else {
                                            kv.value += '\n';
                                            kv.value += line.substring(2);
                                        }
                                        kv.last = 'v';
                                    } else {
                                        System.err.println("ignoring value with no previous key on line " + kv.line);
                                    }
                                }
                            });
                            if (kv.key != null) {
                                if (kv.value != null) {
                                    if (!kv.translations.containsKey(kv.key)) {
                                        kv.translations.put(kv.key, kv.value);
                                    } else {
                                        System.err.println("ignoring duplicate key on line " + kv.lastKeyLineStart);
                                    }
                                } else {
                                    System.err.println("ignoring key with missing value on line " + kv.lastKeyLineStart);
                                }
                            }
                            String key = path.getFileName().toString().toLowerCase();
                            mLangMap.put(key.substring(0, key.length() - EXTENSION.length()), kv.translations);
                        } catch (IOException ex) {
                            ex.printStackTrace(System.err);
                        }
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @param str the text to localize.
     * @return the localized version if one exists, or the original text if not.
     */
    public String text(String str) {
        for (String lang : getHierarchy()) {
            Map<String, String> kvMap = mLangMap.get(lang);
            if (kvMap != null) {
                String value = kvMap.get(str);
                if (value != null) {
                    return value;
                }
            }
        }
        return str;
    }

    /** @return the current locale for this {@link I18n}. */
    public Locale getLocale() {
        synchronized (mHierMap) {
            return mLocale;
        }
    }

    /** @param locale the locale to set for this {@link I18n}. */
    public void setLocale(Locale locale) {
        synchronized (mHierMap) {
            mLocale = locale;
            mLang   = locale.toString().toLowerCase();
        }
    }

    private List<String> getHierarchy() {
        synchronized (mHierMap) {
            if (mHierMap.containsKey(mLang)) {
                return mHierMap.get(mLang);
            }
            String       one  = mLang;
            List<String> list = new ArrayList<>();
            while (true) {
                list.add(one);
                int last = Math.max(one.lastIndexOf('.'), one.lastIndexOf('_'));
                if (last == -1) {
                    break;
                }
                one = one.substring(0, last);
            }
            mHierMap.put(mLang, list);
            return list;
        }
    }

    private static class kvInfo {
        int                 line;
        int                 lastKeyLineStart;
        String              key;
        String              value;
        Map<String, String> translations;
        char                last;
    }

    /**
     * Provides an entry point for extracting the strings used by calls to I18n.Text(). Note that
     * this is fairly naive, in that the strings are expected to be string literals and complete at
     * the call site, i.e. I18n.Text("string") and not things like I18n.Text("one" + " two") or
     * I18n.Text(STRING_CONSTANT).
     */
    public static void main(String[] args) {
        Set<String> keys = new HashSet<>();
        Arrays.stream(args).flatMap(arg -> {
            try {
                return Files.walk(Path.of(arg));
            } catch (IOException ioe) {
                ioe.printStackTrace();
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
                ioe.printStackTrace();
                System.exit(1);
            }
        });
        System.out.println("# Generated " + new Date());
        keys.stream().sorted().forEachOrdered(key -> {
            System.out.println();
            String[] parts = key.split("\n", -1);
            for (String part : parts) {
                System.out.println("k:" + part);
            }
            for (String part : parts) {
                System.out.println("v:" + part);
            }
        });
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
                    new RuntimeException("invalid escape sequence").printStackTrace();
                    System.exit(1);
                }
                break;
            case 2: // Processing first digit of unicode escape sequence
            case 3: // Processing second digit of unicode escape sequence
            case 4: // Processing third digit of unicode escape sequence
            case 5: // Processing fourth digit of unicode escape sequence
                if (!isHexDigit(ch)) {
                    new RuntimeException("invalid unicode escape sequence").printStackTrace();
                    System.exit(1);
                }
                unicodeValue *= 16;
                unicodeValue += hexValue(ch);
                if (state == 5) {
                    state = 0;
                    buffer.append((char) unicodeValue);
                } else {
                    state++;
                }
                break;
            default:
                new RuntimeException("invalid state").printStackTrace();
                System.exit(1);
                break;
            }
        }
        return "";
    }

    private static boolean isHexDigit(char ch) {
        return ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'f' || ch >= 'A' && ch <= 'F';
    }

    private static int hexValue(char ch) {
        if (ch >= '0' && ch <= '9') {
            return ch - '0';
        }
        if (ch >= 'a' && ch <= 'f') {
            return 10 + ch - 'a';
        }
        return 10 + ch - 'A';
    }
}
