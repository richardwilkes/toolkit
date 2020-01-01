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

import com.trollworks.toolkit.ui.image.StdImage;
import com.trollworks.toolkit.utility.I18n;
import com.trollworks.toolkit.utility.text.Text;

import javax.swing.JLabel;
import javax.swing.JRootPane;

/** A toolbar marker that tracks the modified state. */
public class ModifiedMarker extends JLabel implements DataModifiedListener {
    /** Creates a new {@link ModifiedMarker}. */
    public ModifiedMarker() {
        super(StdImage.NOT_MODIFIED_MARKER);
        setToolTipText(Text.wrapPlainTextForToolTip(I18n.Text("No changes have been made")));
    }

    @Override
    public void dataModificationStateChanged(Object obj, boolean modified) {
        if (modified) {
            setIcon(StdImage.MODIFIED_MARKER);
            setToolTipText(Text.wrapPlainTextForToolTip(I18n.Text("Changes have been made")));
        } else {
            setIcon(StdImage.NOT_MODIFIED_MARKER);
            setToolTipText(Text.wrapPlainTextForToolTip(I18n.Text("No changes have been made")));
        }
        repaint();
        JRootPane rootPane = getRootPane();
        if (rootPane != null) {
            rootPane.putClientProperty("Window.documentModified", modified ? Boolean.TRUE : Boolean.FALSE);
        }
    }
}
