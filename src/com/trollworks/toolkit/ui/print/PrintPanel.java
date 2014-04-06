/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.trollworks.toolkit.ui.print;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.layout.Alignment;
import com.trollworks.toolkit.ui.layout.FlexComponent;
import com.trollworks.toolkit.ui.layout.FlexGrid;
import com.trollworks.toolkit.ui.layout.FlexRow;
import com.trollworks.toolkit.ui.widget.EditorField;
import com.trollworks.toolkit.ui.widget.LinkedLabel;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.text.IntegerFormatter;

import java.awt.event.ActionEvent;

import javax.print.PrintService;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.PageRanges;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultFormatterFactory;

/** Provides the basic print panel. */
public class PrintPanel extends PageSetupPanel {
	@Localize("Copies")
	private static String	COPIES;
	@Localize("Print Range")
	private static String	PAGE_RANGE;
	@Localize("All")
	private static String	ALL;
	@Localize("Pages")
	private static String	PAGES;
	@Localize("to")
	private static String	TO;

	static {
		Localization.initialize();
	}

	private EditorField		mCopies;
	private JRadioButton	mPageRangeAll;
	private JRadioButton	mPageRangeSome;
	private EditorField		mPageRangeStart;
	private EditorField		mPageRangeEnd;

	/**
	 * Creates a new print panel.
	 *
	 * @param service The {@link PrintService} to use.
	 * @param set The {@link PrintRequestAttributeSet} to use.
	 */
	public PrintPanel(PrintService service, PrintRequestAttributeSet set) {
		super(service, set);
	}

	@Override
	protected void rebuildSelf(PrintRequestAttributeSet set, FlexGrid grid, int row) {
		row = createCopiesField(set, grid, row);
		row = createPageRangeFields(set, grid, row);
		super.rebuildSelf(set, grid, row);
	}

	private int createCopiesField(PrintRequestAttributeSet set, FlexGrid grid, int row) {
		PrintService service = getService();
		if (service.isAttributeCategorySupported(Copies.class)) {
			mCopies = new EditorField(new DefaultFormatterFactory(new IntegerFormatter(1, 999, false)), null, SwingConstants.RIGHT, new Integer(PrintUtilities.getCopies(service, set)), new Integer(999), null);
			UIUtilities.setOnlySize(mCopies, mCopies.getPreferredSize());
			add(mCopies);
			LinkedLabel label = new LinkedLabel(COPIES, mCopies);
			add(label);
			grid.add(new FlexComponent(label, Alignment.RIGHT_BOTTOM, Alignment.CENTER), row, 0);
			grid.add(mCopies, row, 1);
			return row + 1;
		}
		mCopies = null;
		return row;
	}

	private int createPageRangeFields(PrintRequestAttributeSet set, FlexGrid grid, int row) {
		PrintService service = getService();
		if (service.isAttributeCategorySupported(PageRanges.class)) {
			ButtonGroup group = new ButtonGroup();
			int start = 1;
			int end = 9999;
			PageRanges pageRanges = (PageRanges) set.get(PageRanges.class);
			if (pageRanges != null) {
				int[][] ranges = pageRanges.getMembers();
				if (ranges.length > 0 && ranges[0].length > 1) {
					start = ranges[0][0];
					end = ranges[0][1];
				} else {
					pageRanges = null;
				}
			}
			JLabel label = new JLabel(PAGE_RANGE, SwingConstants.CENTER);
			add(label);
			grid.add(new FlexComponent(label, Alignment.RIGHT_BOTTOM, Alignment.CENTER), row, 0);
			mPageRangeAll = new JRadioButton(ALL, pageRanges == null);
			add(mPageRangeAll);
			mPageRangeSome = new JRadioButton(PAGES, pageRanges != null);
			add(mPageRangeSome);
			mPageRangeStart = createPageRangeField(start);
			mPageRangeEnd = createPageRangeField(end);
			group.add(mPageRangeAll);
			group.add(mPageRangeSome);
			adjustPageRanges();
			mPageRangeAll.addActionListener(this);
			mPageRangeSome.addActionListener(this);
			label = new JLabel(TO, SwingConstants.CENTER);
			add(label);
			FlexRow fRow = new FlexRow();
			fRow.add(mPageRangeAll);
			fRow.add(mPageRangeSome);
			fRow.add(mPageRangeStart);
			fRow.add(label);
			fRow.add(mPageRangeEnd);
			grid.add(fRow, row, 1);
			return row + 1;
		}
		mPageRangeAll = null;
		mPageRangeSome = null;
		mPageRangeStart = null;
		mPageRangeEnd = null;
		return row;
	}

	private EditorField createPageRangeField(int value) {
		EditorField field = new EditorField(new DefaultFormatterFactory(new IntegerFormatter(1, 9999, false)), null, SwingConstants.RIGHT, new Integer(value), new Integer(9999), null);
		UIUtilities.setOnlySize(field, field.getPreferredSize());
		add(field);
		return field;
	}

	private void adjustPageRanges() {
		boolean enabled = !mPageRangeAll.isSelected();
		mPageRangeStart.setEnabled(enabled);
		mPageRangeEnd.setEnabled(enabled);
	}

	@Override
	public PrintService accept(PrintRequestAttributeSet set) {
		PrintService service = super.accept(set);
		if (mCopies != null) {
			PrintUtilities.setCopies(set, ((Integer) mCopies.getValue()).intValue());
		}
		if (mPageRangeAll != null) {
			if (mPageRangeAll.isSelected()) {
				PrintUtilities.setPageRanges(set, null);
			} else {
				int start = ((Integer) mPageRangeStart.getValue()).intValue();
				int end = ((Integer) mPageRangeEnd.getValue()).intValue();
				if (start > end) {
					int tmp = start;
					start = end;
					end = tmp;
				}
				PrintUtilities.setPageRanges(set, new PageRanges(start, end));
			}
		}
		return service;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if (src == mPageRangeAll || src == mPageRangeSome) {
			adjustPageRanges();
		} else {
			super.actionPerformed(event);
		}
	}
}
