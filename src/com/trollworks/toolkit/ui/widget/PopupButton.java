/*
 * Copyright (c) 1998-2016 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.widget;

import com.trollworks.toolkit.ui.Colors;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataListener;

public class PopupButton<T> extends JComboBox<T> {
	ComboBoxModel<T>					mModel;
	private ListCellRenderer<? super T>	mRenderer;

	@SuppressWarnings("unchecked")
	public PopupButton(List<T> items) {
		this((T[]) items.toArray());
	}

	public PopupButton(T[] items) {
		super(items);
		mModel = getModel();
		mRenderer = getRenderer();
		setModel(new ComboBoxModel<T>() {
			@Override
			public int getSize() {
				return mModel.getSize();
			}

			@Override
			public T getElementAt(int index) {
				return mModel.getElementAt(index);
			}

			@Override
			public void addListDataListener(ListDataListener listener) {
				mModel.addListDataListener(listener);
			}

			@Override
			public void removeListDataListener(ListDataListener listener) {
				mModel.removeListDataListener(listener);
			}

			@Override
			public void setSelectedItem(Object item) {
				if (item instanceof Enabled) {
					if (!((Enabled) item).isEnabled()) {
						return;
					}
				}
				mModel.setSelectedItem(item);
			}

			@Override
			public Object getSelectedItem() {
				return mModel.getSelectedItem();
			}
		});
		setRenderer((list, value, index, isSelected, cellHasFocus) -> {
			Component comp = mRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (!(value instanceof Enabled && ((Enabled) value).isEnabled())) {
				Color background = list.getBackground();
				comp.setBackground(background);
				comp.setForeground(Colors.adjustBrightness(background, -0.2f));
			}
			return comp;
		});
	}
}
