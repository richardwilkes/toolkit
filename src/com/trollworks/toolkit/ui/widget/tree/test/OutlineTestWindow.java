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

package com.trollworks.toolkit.ui.widget.tree.test;

import com.trollworks.toolkit.ui.menu.file.SignificantFrame;
import com.trollworks.toolkit.ui.widget.AppWindow;
import com.trollworks.toolkit.ui.widget.outline.Column;
import com.trollworks.toolkit.ui.widget.outline.Outline;
import com.trollworks.toolkit.ui.widget.outline.OutlineModel;
import com.trollworks.toolkit.ui.widget.outline.Row;
import com.trollworks.toolkit.ui.widget.outline.TextCell;
import com.trollworks.toolkit.utility.text.Numbers;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class OutlineTestWindow extends AppWindow implements SignificantFrame {
	public OutlineTestWindow(String name) {
		super(name);
		getContentPane().add(new JScrollPane(createTestPanel(false)), BorderLayout.CENTER);
	}

	private static Outline createTestPanel(boolean wrapped) {
		Outline outline = new Outline();
		OutlineModel model = outline.getModel();
		outline.setDynamicRowHeight(wrapped);
		Column column = new Column(0, "Testy Goop", new TextCell(SwingConstants.LEFT, wrapped)); //$NON-NLS-1$
		model.addColumn(column);
		for (int i = 2; i < 10; i++) {
			column = new Column(i - 1, Integer.toString(i), new TextCell(SwingConstants.RIGHT));
			model.addColumn(column);
		}
		int rowCount = 0;
		for (int i = 0; i < 10000; i++) {
			rowCount += buildRows(model);
		}
		System.out.println("Added " + Numbers.format(rowCount) + " nodes"); //$NON-NLS-1$ //$NON-NLS-2$
		outline.sizeColumnsToFit();
		outline.setSize(outline.getPreferredSize());
		return outline;
	}

	private static int buildRows(OutlineModel model) {
		model.addRow(new OutlineTestRow("First")); //$NON-NLS-1$
		model.addRow(new OutlineTestRow("Second, but with a really long piece of non-pre-wrapped text.")); //$NON-NLS-1$
		model.addRow(new OutlineTestRow("Third, but with a\nreally long piece\nof pre-wrapped text.")); //$NON-NLS-1$
		model.addRow(new OutlineTestRow("Bob")); //$NON-NLS-1$
		int rowCount = 4;
		Row row = new OutlineTestRow("Marley"); //$NON-NLS-1$
		row.setCanHaveChildren(true);
		for (int i = 0; i < 10; i++) {
			row.addChild(new OutlineTestRow(Integer.toString(i)));
			rowCount++;
			if (i == 5) {
				Row sub = new OutlineTestRow("Submarine"); //$NON-NLS-1$
				row.setCanHaveChildren(true);
				sub.addChild(new OutlineTestRow("Yellow")); //$NON-NLS-1$
				sub.addChild(new OutlineTestRow("Black")); //$NON-NLS-1$
				sub.addChild(new OutlineTestRow("Blue")); //$NON-NLS-1$
				row.addChild(sub);
				rowCount += 4;
			}
			if (i == 9) {
				Row sub = new OutlineTestRow("Last"); //$NON-NLS-1$
				row.setCanHaveChildren(true);
				sub.addChild(new OutlineTestRow("Me")); //$NON-NLS-1$
				sub.addChild(new OutlineTestRow("You")); //$NON-NLS-1$
				sub.addChild(new OutlineTestRow("Them")); //$NON-NLS-1$
				row.addChild(sub);
				rowCount += 4;
			}
		}
		model.addRow(row);
		rowCount++;
		return rowCount;
	}
}
