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
import com.trollworks.toolkit.ui.widget.tree.StdTreeDeleter;
import com.trollworks.toolkit.ui.widget.tree.TextTreeColumn;
import com.trollworks.toolkit.ui.widget.tree.TreePanel;
import com.trollworks.toolkit.ui.widget.tree.TreeRoot;
import com.trollworks.toolkit.utility.notification.Notifier;
import com.trollworks.toolkit.utility.text.Numbers;

import java.awt.BorderLayout;
import java.awt.dnd.DnDConstants;

import javax.swing.SwingConstants;

public class TreeTestWindow extends AppWindow implements SignificantFrame {
	public TreeTestWindow(String name, boolean allowDropRowCopy) {
		super(name);
		//Fonts.register(Fonts.KEY_STD_TEXT_FIELD, "Mine", new Font(Fonts.getDefaultFontName(), Font.PLAIN, 10)); //$NON-NLS-1$
		getContentPane().add(createTestPanel(true, allowDropRowCopy), BorderLayout.CENTER);
	}

	private static TreePanel createTestPanel(boolean wrapped, boolean allowDropRowCopy) {
		TreeRoot root = new TreeRoot(new Notifier());
		TreePanel panel = new TreePanel(root);
		panel.setAllowedRowDropTypes(allowDropRowCopy ? DnDConstants.ACTION_COPY_OR_MOVE : DnDConstants.ACTION_MOVE);
		panel.setAllowRowDropFromExternal(true);
		if (wrapped) {
			panel.setRowHeight(0);
		}
		TextTreeColumn column = new TextTreeColumn("Testy goop", TreeTestRow::getName, TreeTestRow::getIcon, SwingConstants.LEFT, wrapped ? TextTreeColumn.WrappingMode.WRAPPED : TextTreeColumn.WrappingMode.NORMAL); //$NON-NLS-1$
		panel.addColumn(column);
		for (int i = 2; i < 10; i++) {
			column = new TextTreeColumn(Integer.toString(i), TreeTestRow::getSecond, SwingConstants.RIGHT, TextTreeColumn.WrappingMode.SINGLE_LINE);
			panel.addColumn(column);
		}
		for (int i = 0; i < 10000; i++) {
			buildRows(root);
		}
		System.out.println("Added " + Numbers.format(root.getRecursiveChildCount()) + " nodes"); //$NON-NLS-1$ //$NON-NLS-2$
		panel.sizeColumnsToFit();
		panel.pack();
		panel.setDeletableProxy(new StdTreeDeleter(panel));
		return panel;
	}

	private static void buildRows(TreeRoot model) {
		model.addRow(new TreeTestRow("First")); //$NON-NLS-1$
		model.addRow(new TreeTestRow("Second, but with a really long piece of non-pre-wrapped text.")); //$NON-NLS-1$
		model.addRow(new TreeTestRow("Third, but with a\nreally long piece\nof pre-wrapped text.")); //$NON-NLS-1$
		model.addRow(new TreeTestRow("Bob")); //$NON-NLS-1$
		TreeTestContainerRow row = new TreeTestContainerRow("Marley"); //$NON-NLS-1$
		for (int i = 0; i < 10; i++) {
			row.addRow(new TreeTestRow(Integer.toString(i)));
			if (i == 5) {
				TreeTestContainerRow sub = new TreeTestContainerRow("Submarine"); //$NON-NLS-1$
				sub.addRow(new TreeTestRow("Yellow")); //$NON-NLS-1$
				sub.addRow(new TreeTestRow("Black")); //$NON-NLS-1$
				sub.addRow(new TreeTestRow("Blue")); //$NON-NLS-1$
				row.addRow(sub);
			}
			if (i == 9) {
				TreeTestContainerRow sub = new TreeTestContainerRow("Last"); //$NON-NLS-1$
				sub.addRow(new TreeTestRow("Me")); //$NON-NLS-1$
				sub.addRow(new TreeTestRow("You")); //$NON-NLS-1$
				sub.addRow(new TreeTestRow("Them")); //$NON-NLS-1$
				row.addRow(sub);
			}
		}
		model.addRow(row);
	}
}
