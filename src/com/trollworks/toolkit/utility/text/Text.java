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

package com.trollworks.toolkit.utility.text;

import com.trollworks.toolkit.utility.I18n;

import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.swing.SwingConstants;

/** Provides text manipulation. */
public class Text {
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * @param text The text to check.
     * @return "a" or "an", as appropriate for the text that will be following it.
     */
    public static final String aAn(String text) {
        return startsWithVowel(text) ? I18n.Text("an") : I18n.Text("a");
    }

    /**
     * @param amount The number of items.
     * @return "was" or "were", as appropriate for the number of items.
     */
    public static final String wasWere(int amount) {
        return amount == 1 ? I18n.Text("was") : I18n.Text("were");
    }

    /**
     * @param ch The character to check.
     * @return {@code true} if the character is a vowel.
     */
    public static final boolean isVowel(char ch) {
        ch = Character.toLowerCase(ch);
        return ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u';
    }

    /**
     * @param text The text to check.
     * @return {@code true} if the text starts with a vowel.
     */
    public static final boolean startsWithVowel(String text) {
        if (text != null && !text.isEmpty()) {
            return isVowel(text.charAt(0));
        }
        return false;
    }

    /**
     * @param ch the digit to convert.
     * @return the hex value or zero if it is not a valid hex digit.
     */
    public static int hexDigitValue(char ch) {
        if (ch >= '0' && ch <= '9') {
            return ch - '0';
        }
        if (ch >= 'a' && ch <= 'f') {
            return 10 + ch - 'a';
        }
        if (ch >= 'A' && ch <= 'F') {
            return 10 + ch - 'A';
        }
        return 0;
    }

    /**
     * @param data The data to create a hex string for.
     * @return A string of two character hexadecimal values for each byte.
     */
    public static final String bytesToHex(byte[] data) {
        return bytesToHex(data, 0, data.length);
    }

    /**
     * @param data   The data to create a hex string for.
     * @param offset The starting index.
     * @param length The number of bytes to use.
     * @return A string of two character hexadecimal values for each byte.
     */
    public static final String bytesToHex(byte[] data, int offset, int length) {
        StringBuilder buffer = new StringBuilder(length * 2);
        for (int i = 0; i < length; i++) {
            byte b = data[i + offset];
            buffer.append(HEX_DIGITS[b >>> 4 & 15]);
            buffer.append(HEX_DIGITS[b & 15]);
        }
        return buffer.toString();
    }

    /**
     * @param text The text to reflow.
     * @return The revised text.
     */
    public static final String reflow(String text) {
        if (text == null) {
            return "";
        }
        int             count     = 0;
        StringBuilder   buffer    = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(text.replaceAll("[\\x00-\\x08]", "").replaceAll("[\\x0b\\x0c]", "").replaceAll("[\\x0e-\\x1f]", "").replaceAll("[\\x7f-\\x9f]", "").replaceAll("\r\n", "\n").replace('\r', '\n').replaceAll("[ \t\f]+", " ").trim(), "\n", true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if ("\n".equals(token)) {
                count++;
            } else {
                if (count == 1) {
                    buffer.append(" ");
                } else if (count > 1) {
                    buffer.append("\n\n");
                }
                count = 0;
                buffer.append(token);
            }
        }
        return buffer.toString();
    }

    /**
     * If the text doesn't fit in the specified character count, it will be shortened and an ellipse
     * ("...") will be added.
     *
     * @param text             The text to work on.
     * @param count            The maximum character count.
     * @param truncationPolicy One of {@link SwingConstants#LEFT}, {@link SwingConstants#CENTER}, or
     *                         {@link SwingConstants#RIGHT}.
     * @return The adjusted text.
     */
    public static final String truncateIfNecessary(String text, int count, int truncationPolicy) {
        int tCount = text.length();

        count = tCount - count;
        if (count > 0) {
            count++; // Count is now the amount to remove from the string
            if (truncationPolicy == SwingConstants.LEFT) {
                return "\u2026" + text.substring(count);
            }
            if (truncationPolicy == SwingConstants.CENTER) {
                int           remaining = tCount - count;
                int           left      = remaining / 2;
                int           right     = remaining - left;
                StringBuilder buffer    = new StringBuilder(remaining + 1);

                if (left > 0) {
                    buffer.append(text, 0, left);
                }
                buffer.append("\u2026");
                if (right > 0) {
                    buffer.append(text.substring(tCount - right));
                }
                return buffer.toString();
            }
            return text.substring(0, tCount - count) + "\u2026";
        }
        return text;
    }

    /**
     * Convert text from other line ending formats into our internal format.
     *
     * @param data The text to convert.
     * @return The converted text.
     */
    public static final String standardizeLineEndings(String data) {
        return standardizeLineEndings(data, "\n");
    }

    /**
     * Convert text from other line ending formats into a specific format.
     *
     * @param data       The text to convert.
     * @param lineEnding The desired line ending.
     * @return The converted text.
     */
    public static final String standardizeLineEndings(String data, String lineEnding) {
        int           length   = data.length();
        StringBuilder buffer   = new StringBuilder(length);
        char          ignoreCh = 0;

        for (int i = 0; i < length; i++) {
            char ch = data.charAt(i);

            if (ch == ignoreCh) {
                ignoreCh = 0;
            } else if (ch == '\r') {
                ignoreCh = '\n';
                buffer.append(lineEnding);
            } else if (ch == '\n') {
                ignoreCh = '\r';
                buffer.append(lineEnding);
            } else {
                ignoreCh = 0;
                buffer.append(ch);
            }
        }

        return buffer.toString();
    }

    /**
     * Extracts lines of text from the specified data.
     *
     * @param data     The text to extract lines from.
     * @param tabWidth The width (in spaces) of a tab character. Pass in {@code 0} or less to leave
     *                 tab characters in place.
     * @return The lines of text.
     */
    public static final ArrayList<String> extractLines(String data, int tabWidth) {
        int               length   = data.length();
        StringBuilder     buffer   = new StringBuilder(length);
        char              ignoreCh = 0;
        ArrayList<String> lines    = new ArrayList<>();
        int               column   = 0;

        for (int i = 0; i < length; i++) {
            char ch = data.charAt(i);

            if (ch == ignoreCh) {
                ignoreCh = 0;
            } else if (ch == '\r') {
                ignoreCh = '\n';
                column = 0;
                lines.add(buffer.toString());
                buffer.setLength(0);
            } else if (ch == '\n') {
                ignoreCh = '\r';
                column = 0;
                lines.add(buffer.toString());
                buffer.setLength(0);
            } else if (ch == '\t' && tabWidth > 0) {
                int spaces = tabWidth - column % tabWidth;

                ignoreCh = 0;
                while (--spaces >= 0) {
                    buffer.append(' ');
                    column++;
                }
            } else {
                ignoreCh = 0;
                column++;
                buffer.append(ch);
            }
        }
        if (buffer.length() > 0) {
            lines.add(buffer.toString());
        }

        return lines;
    }

    /**
     * @param amt    The size of the string to create.
     * @param filler The character to fill it with.
     * @return A string filled with a specific character.
     */
    public static String makeFiller(int amt, char filler) {
        return String.valueOf(filler).repeat(Math.max(0, amt));
    }

    /**
     * Creates a "note" whose second and subsequent lines are indented by the amount of the marker,
     * which is prepended to the first line.
     *
     * @param marker The prefix to use on the first line.
     * @param note   The text of the note.
     * @return The formatted note.
     */
    public static String makeNote(String marker, String note) {
        StringBuilder   buffer    = new StringBuilder(note.length() * 2);
        String          indent    = makeFiller(marker.length() + 1, ' ');
        StringTokenizer tokenizer = new StringTokenizer(note, "\n");

        if (tokenizer.hasMoreTokens()) {
            buffer.append(marker);
            buffer.append(" ");
            buffer.append(tokenizer.nextToken());
            buffer.append("\n");

            while (tokenizer.hasMoreTokens()) {
                buffer.append(indent);
                buffer.append(tokenizer.nextToken());
                buffer.append("\n");
            }
        }

        return buffer.toString();
    }

    /**
     * @param text      The text to wrap.
     * @param charCount The maximum character width to allow.
     * @return A new, wrapped version of the text.
     */
    public static String wrapToCharacterCount(String text, int charCount) {
        StringBuilder   buffer     = new StringBuilder(text.length() * 2);
        StringBuilder   lineBuffer = new StringBuilder(charCount + 1);
        StringTokenizer tokenizer  = new StringTokenizer(text + "\n", "\n", true);

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            if ("\n".equals(token)) {
                buffer.append(token);
            } else {
                StringTokenizer tokenizer2 = new StringTokenizer(token, " \t", true);
                int             length     = 0;

                lineBuffer.setLength(0);
                while (tokenizer2.hasMoreTokens()) {
                    String token2      = tokenizer2.nextToken();
                    int    tokenLength = token2.length();

                    if (length == 0 && " ".equals(token2)) {
                        continue;
                    }
                    if (length == 0 || length + tokenLength <= charCount) {
                        lineBuffer.append(token2);
                        length += tokenLength;
                    } else {
                        buffer.append(lineBuffer);
                        buffer.append("\n");
                        lineBuffer.setLength(0);
                        if (" ".equals(token2)) {
                            length = 0;
                        } else {
                            lineBuffer.append(token2);
                            length = tokenLength;
                        }
                    }
                }
                if (length > 0) {
                    buffer.append(lineBuffer);
                }
            }
        }
        buffer.setLength(buffer.length() - 1);
        return buffer.toString();
    }

    public static String wrapPlainTextForToolTip(String text) {
        if (text != null && !text.isEmpty() && !text.startsWith("<html>")) {
            return "<html><body>" + htmlEscape(wrapToCharacterCount(text, 40)).replaceAll("\n", "<br>") + "</body></html>";
        }
        return text;
    }

    public static String htmlEscape(String str) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        if (length == 0) {
            return str;
        }
        StringBuilder buffer = new StringBuilder(length + 16);
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            switch (ch) {
            case '&':
                buffer.append("&amp;");
                break;
            case '<':
                buffer.append("&lt;");
                break;
            case '>':
                buffer.append("&gt;");
                break;
            case '"':
                buffer.append("&quot;");
                break;
            case '\'':
                buffer.append("&#39;");
                break;
            case '/':
                buffer.append("&#47;");
                break;
            default:
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }

    /**
     * @param ch the character to check.
     * @return {@code true} if the character is a valid hex digit.
     */
    public static boolean isHexDigit(char ch) {
        return ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'f' || ch >= 'A' && ch <= 'F';
    }

    /**
     * @param ch the character to check.
     * @return {@code true} if the character is printable.
     */
    public static boolean isPrintableChar(char ch) {
        if (!Character.isISOControl(ch) && Character.isDefined(ch)) {
            try {
                Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
                return block != null && block != Character.UnicodeBlock.SPECIALS;
            } catch (Exception ex) {
                return false;
            }
        }
        return false;
    }

    /**
     * @param in the string to quote.
     * @return the quoted version of the string.
     */
    public static String quote(String in) {
        StringBuilder buffer = new StringBuilder();
        int           length = in.length();
        buffer.append('"');
        for (int i = 0; i < length; i++) {
            char ch = in.charAt(i);
            if (ch == '"' || ch == '\\') {
                buffer.append('\\');
                buffer.append(ch);
            } else if (isPrintableChar(ch)) {
                buffer.append(ch);
            } else {
                switch (ch) {
                case '\b':
                    buffer.append("\\b");
                    break;
                case '\f':
                    buffer.append("\\f");
                    break;
                case '\n':
                    buffer.append("\\n");
                    break;
                case '\r':
                    buffer.append("\\r");
                    break;
                case '\t':
                    buffer.append("\\t");
                    break;
                default:
                    buffer.append("\\u");
                    buffer.append(HEX_DIGITS[ch >> 12 & 0xF]);
                    buffer.append(HEX_DIGITS[ch >> 8 & 0xF]);
                    buffer.append(HEX_DIGITS[ch >> 4 & 0xF]);
                    buffer.append(HEX_DIGITS[ch & 0xF]);
                    break;
                }
            }
        }
        buffer.append('"');
        return buffer.toString();
    }

    /**
     * @param in a string previously passed to {@link #quote(String)} to unquote.
     * @return the unquoted version of the string.
     */
    public static String unquote(String in) {
        StringBuilder buffer = new StringBuilder();
        int           length = in.length();
        if (length < 2 || in.charAt(0) != '"' || in.charAt(length - 1) != '"') {
            return "";
        }
        length--;
        int state = 0;
        int value = 0;
        for (int i = 1; i < length; i++) {
            char ch = in.charAt(i);
            switch (state) {
            case 0: // Normal
                if (ch == '\\') {
                    state = 1;
                } else {
                    buffer.append(ch);
                }
                break;
            case 1: // Process escape
                switch (ch) {
                case '\\':
                case '"':
                    buffer.append(ch);
                    state = 0;
                    break;
                case 'b':
                    buffer.append('\b');
                    state = 0;
                    break;
                case 'f':
                    buffer.append('\f');
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
                case 't':
                    buffer.append('\t');
                    state = 0;
                    break;
                case 'u':
                    value = 0;
                    state = 2;
                    break;
                default:
                    state = 0; // In case bogus input was provided
                    break;
                }
                break;
            case 2: // Process 4-byte escape, part 1
                value = hexDigitValue(ch) << 12;
                state++;
                break;
            case 3: // Process 4-byte escape, part 2
                value |= hexDigitValue(ch) << 8;
                state++;
                break;
            case 4: // Process 4-byte escape, part 3
                value |= hexDigitValue(ch) << 4;
                state++;
                break;
            case 5: // Process 4-byte escape, part 4
                buffer.append((char) (value | hexDigitValue(ch)));
                state = 0;
                break;
            default:
                state = 0; // In case bogus input was provided
                break;
            }
        }
        return buffer.toString();
    }
}
