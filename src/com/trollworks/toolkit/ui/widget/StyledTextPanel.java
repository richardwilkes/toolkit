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

package com.trollworks.toolkit.ui.widget;

import com.trollworks.toolkit.io.Log;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;

public class StyledTextPanel extends JTextPane {
    public void append(String text, Style style) {
        Document doc = getDocument();
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException exception) {
            Log.error(exception);
        }
    }
}
