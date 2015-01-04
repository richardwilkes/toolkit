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

package com.trollworks.toolkit.ui.layout;

import static com.trollworks.toolkit.ui.layout.PrecisionLayoutAlignment.*;
import static com.trollworks.toolkit.ui.layout.PrecisionLayoutData.*;

import com.trollworks.toolkit.io.Log;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.HashMap;

/** A layout manager that can handle complex layouts with precision. */
public final class PrecisionLayout implements LayoutManager2 {
	private HashMap<Component, PrecisionLayoutData>	mConstraints	= new HashMap<>();
	private int										mRowCount;
	private int										mColumns		= 1;
	private int										mMarginTop		= 4;
	private int										mMarginLeft		= 4;
	private int										mMarginBottom	= 4;
	private int										mMarginRight	= 4;
	private int										mHSpacing		= 4;
	private int										mVSpacing		= 2;
	private PrecisionLayoutAlignment				mHAlign			= BEGINNING;
	private PrecisionLayoutAlignment				mVAlign			= BEGINNING;
	private boolean									mEqualColumns	= false;

	/** @return The number of cell columns in the layout. */
	public int getColumns() {
		return mColumns;
	}

	/**
	 * @param columns The number of cell columns in the layout. If this has a value less than 1, the
	 *            layout will not set the size and position of any children. The default value is 1.
	 * @return This layout.
	 */
	public PrecisionLayout setColumns(int columns) {
		mColumns = columns;
		return this;
	}

	/** @return <code>true</code> if all columns in the layout will be forced to have the same width. */
	public boolean isEqualColumns() {
		return mEqualColumns;
	}

	/**
	 * @param equal <code>true</code> if all columns in the layout will be forced to have the same
	 *            width. The default value is <code>false</code>.
	 * @return This layout.
	 */
	public PrecisionLayout setEqualColumns(boolean equal) {
		mEqualColumns = equal;
		return this;
	}

	/**
	 * @return The number of pixels of vertical margin that will be placed along the top edge of the
	 *         layout.
	 */
	public int getTopMargin() {
		return mMarginTop;
	}

	/**
	 * @param top The number of pixels of vertical margin that will be placed along the top edge of
	 *            the layout. The default value is 4.
	 * @return This layout.
	 */
	public PrecisionLayout setTopMargin(int top) {
		mMarginTop = top;
		return this;
	}

	/**
	 * @return The number of pixels of horizontal margin that will be placed along the left edge of
	 *         the layout.
	 */
	public int getLeftMargin() {
		return mMarginLeft;
	}

	/**
	 * @param left The number of pixels of horizontal margin that will be placed along the left edge
	 *            of the layout. The default value is 4.
	 * @return This layout.
	 */
	public PrecisionLayout setLeftMargin(int left) {
		mMarginLeft = left;
		return this;
	}

	/**
	 * @return The number of pixels of vertical margin that will be placed along the bottom edge of
	 *         the layout.
	 */
	public int getBottomMargin() {
		return mMarginBottom;
	}

	/**
	 * @param bottom The number of pixels of vertical margin that will be placed along the bottom
	 *            edge of the layout. The default value is 4.
	 * @return This layout.
	 */
	public PrecisionLayout setBottomMargin(int bottom) {
		mMarginBottom = bottom;
		return this;
	}

	/**
	 * @return The number of pixels of horizontal margin that will be placed along the right edge of
	 *         the layout.
	 */
	public int getRightMargin() {
		return mMarginRight;
	}

	/**
	 * @param right The number of pixels of horizontal margin that will be placed along the right
	 *            edge of the layout. The default value is 4.
	 * @return This layout.
	 */
	public PrecisionLayout setRightMargin(int right) {
		mMarginRight = right;
		return this;
	}

	/**
	 * @param margins The number of pixels of margin that will be placed along each edge of the
	 *            layout. The default value is 4.
	 * @return This layout.
	 */
	public PrecisionLayout setMargins(int margins) {
		mMarginTop = margins;
		mMarginLeft = margins;
		mMarginBottom = margins;
		mMarginRight = margins;
		return this;
	}

	/**
	 * @param top The number of pixels of vertical margin that will be placed along the top edge of
	 *            the layout. The default value is 4.
	 * @param left The number of pixels of horizontal margin that will be placed along the left edge
	 *            of the layout. The default value is 4.
	 * @param bottom The number of pixels of vertical margin that will be placed along the bottom
	 *            edge of the layout. The default value is 4.
	 * @param right The number of pixels of horizontal margin that will be placed along the right
	 *            edge of the layout. The default value is 4.
	 * @return This layout.
	 */
	public PrecisionLayout setMargins(int top, int left, int bottom, int right) {
		mMarginTop = top;
		mMarginLeft = left;
		mMarginBottom = bottom;
		mMarginRight = right;
		return this;
	}

	/**
	 * @return The number of pixels between the right edge of one cell and the left edge of its
	 *         neighboring cell to the right.
	 */
	public int getHorizontalSpacing() {
		return mHSpacing;
	}

	/**
	 * @param spacing The number of pixels between the right edge of one cell and the left edge of
	 *            its neighboring cell to the right. The default value is 4.
	 * @return This layout.
	 */
	public PrecisionLayout setHorizontalSpacing(int spacing) {
		mHSpacing = spacing;
		return this;
	}

	/**
	 * @return The number of pixels between the bottom edge of one cell and the top edge of its
	 *         neighboring cell underneath.
	 */
	public int getVerticalSpacing() {
		return mHSpacing;
	}

	/**
	 * @param spacing The number of pixels between the bottom edge of one cell and the top edge of
	 *            its neighboring cell underneath. The default value is 2.
	 * @return This layout.
	 */
	public PrecisionLayout setVerticalSpacing(int spacing) {
		mVSpacing = spacing;
		return this;
	}

	/**
	 * @param horizontal The number of pixels between the right edge of one cell and the left edge
	 *            of its neighboring cell to the right. The default value is 4.
	 * @param vertical The number of pixels between the bottom edge of one cell and the top edge of
	 *            its neighboring cell underneath. The default value is 2.
	 * @return This layout.
	 */
	public PrecisionLayout setSpacing(int horizontal, int vertical) {
		mHSpacing = horizontal;
		mVSpacing = vertical;
		return this;
	}

	/**
	 * Position the components at the left of the container. This is the default.
	 *
	 * @return This layout.
	 */
	public PrecisionLayout setBeginningHorizontalAlign() {
		mHAlign = BEGINNING;
		return this;
	}

	/**
	 * Position the components in the horizontal center of the container.
	 *
	 * @return This layout.
	 */
	public PrecisionLayout setMiddleHorizontalAlignment() {
		mHAlign = MIDDLE;
		return this;
	}

	/**
	 * Position the components at the right of the container.
	 *
	 * @return This layout.
	 */
	public PrecisionLayout setEndHorizontalAlignment() {
		mHAlign = END;
		return this;
	}

	/** @return The horizontal positioning of components within the container. */
	public PrecisionLayoutAlignment getHorizontalAlignment() {
		return mHAlign;
	}

	/**
	 * @param alignment Specifies how components will be positioned horizontally within the
	 *            container. The default value is {@link #BEGINNING}.
	 * @return This layout.
	 */
	public PrecisionLayout setHorizontalAlignment(PrecisionLayoutAlignment alignment) {
		mHAlign = alignment;
		return this;
	}

	/**
	 * Position the components at the top of the container. This is the default.
	 *
	 * @return This layout.
	 */
	public PrecisionLayout setBeginningVerticalAlignment() {
		mVAlign = BEGINNING;
		return this;
	}

	/**
	 * Position the components in the vertical center of the container.
	 *
	 * @return This layout.
	 */
	public PrecisionLayout setMiddleVerticalAlignment() {
		mVAlign = MIDDLE;
		return this;
	}

	/**
	 * Position the components at the bottom of the container.
	 *
	 * @return This layout.
	 */
	public PrecisionLayout setEndVerticalAlignment() {
		mVAlign = END;
		return this;
	}

	/** @return The vertical positioning of components within the container. */
	public PrecisionLayoutAlignment getVerticalAlignment() {
		return mVAlign;
	}

	/**
	 * @param alignment Specifies how components will be positioned vertically within the container.
	 *            The default value is {@link #BEGINNING}.
	 * @return This layout.
	 */
	public PrecisionLayout setVerticalAlignment(PrecisionLayoutAlignment alignment) {
		mVAlign = alignment;
		return this;
	}

	/**
	 * @param horizontal Specifies how components will be positioned horizontally within the
	 *            container. The default value is {@link #BEGINNING}.
	 * @param vertical Specifies how components will be positioned vertically within the container.
	 *            The default value is {@link #BEGINNING}.
	 * @return This layout.
	 */
	public PrecisionLayout setAlignment(PrecisionLayoutAlignment horizontal, PrecisionLayoutAlignment vertical) {
		mHAlign = horizontal;
		mVAlign = vertical;
		return this;
	}

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		if (constraints == null) {
			constraints = new PrecisionLayoutData();
		}
		if (!(constraints instanceof PrecisionLayoutData)) {
			Log.error("Contraints must be a PrecisionLayoutData object"); //$NON-NLS-1$
			constraints = new PrecisionLayoutData();
		}
		mConstraints.put(comp, (PrecisionLayoutData) constraints);
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		addLayoutComponent(comp, name);
	}

	public PrecisionLayoutData getGridData(Component comp) {
		return mConstraints.get(comp);
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		mConstraints.remove(comp);
	}

	@Override
	public float getLayoutAlignmentX(Container target) {
		return Component.CENTER_ALIGNMENT;
	}

	@Override
	public float getLayoutAlignmentY(Container target) {
		return Component.CENTER_ALIGNMENT;
	}

	@Override
	public void invalidateLayout(Container target) {
		for (PrecisionLayoutData one : mConstraints.values()) {
			one.clearCache();
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container target) {
		Insets insets = target.getInsets();
		Dimension size = layout(target, false, 0, 0, DEFAULT, DEFAULT, true);
		size.width += insets.left + insets.right;
		size.height += insets.top + insets.bottom;
		return size;
	}

	@Override
	public Dimension preferredLayoutSize(Container target) {
		Insets insets = target.getInsets();
		Dimension size = layout(target, false, 0, 0, DEFAULT, DEFAULT, false);
		size.width += insets.left + insets.right;
		size.height += insets.top + insets.bottom;
		return size;
	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		// Arbitrary. Will this be a problem?
		int max = Integer.MAX_VALUE / 8;
		return new Dimension(max, max);
	}

	@Override
	public void layoutContainer(Container target) {
		Insets insets = target.getInsets();
		layout(target, true, insets.left, insets.top, target.getWidth() - (insets.left + insets.right), target.getHeight() - (insets.top + insets.bottom), false);
	}

	private Dimension layout(Container target, boolean move, int x, int y, int width, int height, boolean useMinimumSize) {
		int totalWidth = mMarginLeft + mMarginRight;
		int totalHeight = mMarginTop + mMarginBottom;
		if (mColumns > 0) {
			Component[] children = getChildren(target, useMinimumSize);
			if (children.length > 0) {
				Component[][] grid = buildGrid(children);
				int[] widths = adjustColumnWidths(width, grid);
				wrap(width, grid, widths, useMinimumSize);
				int[] heights = adjustRowHeights(height, grid);
				totalWidth += mHSpacing * (mColumns - 1);
				totalHeight += mVSpacing * (mRowCount - 1);
				for (int i = 0; i < mColumns; i++) {
					totalWidth += widths[i];
				}
				for (int i = 0; i < mRowCount; i++) {
					totalHeight += heights[i];
				}
				if (move) {
					if (totalWidth < width) {
						if (mHAlign == MIDDLE) {
							x += (width - totalWidth) / 2;
						} else if (mHAlign == END) {
							x += width - totalWidth;
						}
					}
					if (totalHeight < height) {
						if (mVAlign == MIDDLE) {
							y += (height - totalHeight) / 2;
						} else if (mVAlign == END) {
							y += height - totalHeight;
						}
					}
					positionChildren(x, y, grid, widths, heights);
				}
			}
		}
		return new Dimension(totalWidth, totalHeight);
	}

	private void positionChildren(int x, int y, Component[][] grid, int[] widths, int[] heights) {
		int gridY = y + mMarginTop;
		for (int i = 0; i < mRowCount; i++) {
			int gridX = x + mMarginLeft;
			for (int j = 0; j < mColumns; j++) {
				PrecisionLayoutData data = getData(grid, i, j, mRowCount, true);
				if (data != null) {
					int hSpan = Math.max(1, Math.min(data.getHorizontalSpan(), mColumns));
					int vSpan = Math.max(1, data.getVerticalSpan());
					int cellWidth = 0, cellHeight = 0;
					for (int k = 0; k < hSpan; k++) {
						cellWidth += widths[j + k];
					}
					for (int k = 0; k < vSpan; k++) {
						cellHeight += heights[i + k];
					}
					cellWidth += mHSpacing * (hSpan - 1);
					int childX = gridX + data.getLeftMargin();
					int childWidth = Math.min(data.getCachedWidth(), cellWidth);
					switch (data.getHorizontalAlignment()) {
						case MIDDLE:
							childX += Math.max(0, (cellWidth - (data.getLeftMargin() + data.getRightMargin()) - childWidth) / 2);
							break;
						case END:
							childX += Math.max(0, cellWidth - (data.getLeftMargin() + data.getRightMargin()) - childWidth);
							break;
						case FILL:
							childWidth = cellWidth - (data.getLeftMargin() + data.getRightMargin());
							break;
						default:
							break;
					}
					cellHeight += mVSpacing * (vSpan - 1);
					int childY = gridY + data.getTopMargin();
					int childHeight = Math.min(data.getCachedHeight(), cellHeight);
					switch (data.getVerticalAlignment()) {
						case MIDDLE:
							childY += Math.max(0, (cellHeight - (data.getTopMargin() + data.getBottomMargin()) - childHeight) / 2);
							break;
						case END:
							childY += Math.max(0, cellHeight - (data.getTopMargin() + data.getBottomMargin()) - childHeight);
							break;
						case FILL:
							childHeight = cellHeight - (data.getTopMargin() + data.getBottomMargin());
							break;
						default:
							break;
					}
					Component child = grid[i][j];
					if (child != null) {
						child.setBounds(childX, childY, childWidth, childHeight);
					}
				}
				gridX += widths[j] + mHSpacing;
			}
			gridY += heights[i] + mVSpacing;
		}
	}

	private Component[] getChildren(Container target, boolean useMinimumSize) {
		ArrayList<Component> children = new ArrayList<>();
		for (Component child : target.getComponents()) {
			PrecisionLayoutData data = mConstraints.get(child);
			if (!data.shouldExclude()) {
				children.add(child);
				data.computeSize(child, DEFAULT, DEFAULT, useMinimumSize);
			}
		}
		return children.toArray(new Component[children.size()]);
	}

	private Component[][] buildGrid(Component[] children) {
		Component[][] grid = new Component[4][mColumns];
		int row = 0;
		int column = 0;
		mRowCount = 0;
		for (Component child : children) {
			PrecisionLayoutData data = mConstraints.get(child);
			int hSpan = Math.max(1, Math.min(data.getHorizontalSpan(), mColumns));
			int vSpan = Math.max(1, data.getVerticalSpan());
			while (true) {
				int lastRow = row + vSpan;
				if (lastRow >= grid.length) {
					Component[][] newGrid = new Component[lastRow + 4][mColumns];
					System.arraycopy(grid, 0, newGrid, 0, grid.length);
					grid = newGrid;
				}
				if (grid[row] == null) {
					grid[row] = new Component[mColumns];
				}
				while (column < mColumns && grid[row][column] != null) {
					column++;
				}
				int endCount = column + hSpan;
				if (endCount <= mColumns) {
					int index = column;
					while (index < endCount && grid[row][index] == null) {
						index++;
					}
					if (index == endCount) {
						break;
					}
					column = index;
				}
				if (column + hSpan >= mColumns) {
					column = 0;
					row++;
				}
			}
			for (int j = 0; j < vSpan; j++) {
				int pos = row + j;
				if (grid[pos] == null) {
					grid[pos] = new Component[mColumns];
				}
				for (int k = 0; k < hSpan; k++) {
					grid[pos][column + k] = child;
				}
			}
			mRowCount = Math.max(mRowCount, row + vSpan);
			column += hSpan;
		}
		return grid;
	}

	private PrecisionLayoutData getData(Component[][] grid, int row, int column, int rowCount, boolean first) {
		Component component = grid[row][column];
		if (component != null) {
			PrecisionLayoutData data = mConstraints.get(component);
			int hSpan = Math.max(1, Math.min(data.getHorizontalSpan(), mColumns));
			int vSpan = Math.max(1, data.getVerticalSpan());
			int i = first ? row + vSpan - 1 : row - vSpan + 1;
			int j = first ? column + hSpan - 1 : column - hSpan + 1;
			if (0 <= i && i < rowCount) {
				if (0 <= j && j < mColumns) {
					if (component == grid[i][j]) {
						return data;
					}
				}
			}
		}
		return null;
	}

	private int[] adjustColumnWidths(int width, Component[][] grid) {
		int availableWidth = width - mHSpacing * (mColumns - 1) - (mMarginLeft + mMarginRight);
		int expandCount = 0;
		int[] widths = new int[mColumns];
		int[] minWidths = new int[mColumns];
		boolean[] expandColumn = new boolean[mColumns];
		for (int j = 0; j < mColumns; j++) {
			for (int i = 0; i < mRowCount; i++) {
				PrecisionLayoutData data = getData(grid, i, j, mRowCount, true);
				if (data != null) {
					int hSpan = Math.max(1, Math.min(data.getHorizontalSpan(), mColumns));
					if (hSpan == 1) {
						int w = data.getCachedWidth() + data.getLeftMargin() + data.getRightMargin();
						if (widths[j] < w) {
							widths[j] = w;
						}
						if (data.shouldGrabHorizontalSpace()) {
							if (!expandColumn[j]) {
								expandCount++;
							}
							expandColumn[j] = true;
						}
						int minimumWidth = data.getCachedMinimumWidth();
						if (!data.shouldGrabHorizontalSpace() || minimumWidth != 0) {
							w = !data.shouldGrabHorizontalSpace() || minimumWidth == DEFAULT ? data.getCachedWidth() : minimumWidth;
							w += data.getLeftMargin() + data.getRightMargin();
							minWidths[j] = Math.max(minWidths[j], w);
						}
					}
				}
			}
			for (int i = 0; i < mRowCount; i++) {
				PrecisionLayoutData data = getData(grid, i, j, mRowCount, false);
				if (data != null) {
					int hSpan = Math.max(1, Math.min(data.getHorizontalSpan(), mColumns));
					if (hSpan > 1) {
						int spanWidth = 0;
						int spanMinWidth = 0;
						int spanExpandCount = 0;
						for (int k = 0; k < hSpan; k++) {
							spanWidth += widths[j - k];
							spanMinWidth += minWidths[j - k];
							if (expandColumn[j - k]) {
								spanExpandCount++;
							}
						}
						if (data.shouldGrabHorizontalSpace() && spanExpandCount == 0) {
							expandCount++;
							expandColumn[j] = true;
						}
						int w = data.getCachedWidth() + data.getLeftMargin() + data.getRightMargin() - spanWidth - (hSpan - 1) * mHSpacing;
						if (w > 0) {
							if (mEqualColumns) {
								int equalWidth = (w + spanWidth) / hSpan;
								int remainder = (w + spanWidth) % hSpan;
								int last = -1;
								for (int k = 0; k < hSpan; k++) {
									last = j - k;
									widths[last] = Math.max(equalWidth, widths[last]);
								}
								if (last > -1) {
									widths[last] += remainder;
								}
							} else {
								if (spanExpandCount == 0) {
									widths[j] += w;
								} else {
									int delta = w / spanExpandCount;
									int remainder = w % spanExpandCount;
									int last = -1;
									for (int k = 0; k < hSpan; k++) {
										if (expandColumn[j - k]) {
											last = j - k;
											widths[last] += delta;
										}
									}
									if (last > -1) {
										widths[last] += remainder;
									}
								}
							}
						}
						int minimumWidth = data.getCachedMinimumWidth();
						if (!data.shouldGrabHorizontalSpace() || minimumWidth != 0) {
							w = !data.shouldGrabHorizontalSpace() || minimumWidth == DEFAULT ? data.getCachedWidth() : minimumWidth;
							w += data.getLeftMargin() + data.getRightMargin() - spanMinWidth - (hSpan - 1) * mHSpacing;
							if (w > 0) {
								if (spanExpandCount == 0) {
									minWidths[j] += w;
								} else {
									int delta = w / spanExpandCount;
									int remainder = w % spanExpandCount;
									int last = -1;
									for (int k = 0; k < hSpan; k++) {
										if (expandColumn[j - k]) {
											last = j - k;
											minWidths[last] += delta;
										}
									}
									if (last > -1) {
										minWidths[last] += remainder;
									}
								}
							}
						}
					}
				}
			}
		}
		if (mEqualColumns) {
			int minColumnWidth = 0;
			int columnWidth = 0;
			for (int i = 0; i < mColumns; i++) {
				minColumnWidth = Math.max(minColumnWidth, minWidths[i]);
				columnWidth = Math.max(columnWidth, widths[i]);
			}
			columnWidth = width == DEFAULT || expandCount == 0 ? columnWidth : Math.max(minColumnWidth, availableWidth / mColumns);
			for (int i = 0; i < mColumns; i++) {
				expandColumn[i] = expandCount > 0;
				widths[i] = columnWidth;
			}
		} else {
			if (width != DEFAULT && expandCount > 0) {
				int totalWidth = 0;
				for (int i = 0; i < mColumns; i++) {
					totalWidth += widths[i];
				}
				int c = expandCount;
				int remainder = availableWidth - totalWidth;
				int delta = remainder / c;
				remainder %= c;
				int last = -1;
				while (totalWidth != availableWidth) {
					for (int j = 0; j < mColumns; j++) {
						if (expandColumn[j]) {
							if (widths[j] + delta > minWidths[j]) {
								last = j;
								widths[last] = widths[j] + delta;
							} else {
								widths[j] = minWidths[j];
								expandColumn[j] = false;
								c--;
							}
						}
					}
					if (last > -1) {
						widths[last] += remainder;
					}

					for (int j = 0; j < mColumns; j++) {
						for (int i = 0; i < mRowCount; i++) {
							PrecisionLayoutData data = getData(grid, i, j, mRowCount, false);
							if (data != null) {
								int hSpan = Math.max(1, Math.min(data.getHorizontalSpan(), mColumns));
								if (hSpan > 1) {
									int minimumWidth = data.getCachedMinimumWidth();
									if (!data.shouldGrabHorizontalSpace() || minimumWidth != 0) {
										int spanWidth = 0, spanExpandCount = 0;
										for (int k = 0; k < hSpan; k++) {
											spanWidth += widths[j - k];
											if (expandColumn[j - k]) {
												spanExpandCount++;
											}
										}
										int w = !data.shouldGrabHorizontalSpace() || minimumWidth == DEFAULT ? data.getCachedWidth() : minimumWidth;
										w += data.getLeftMargin() + data.getRightMargin() - spanWidth - (hSpan - 1) * mHSpacing;
										if (w > 0) {
											if (spanExpandCount == 0) {
												widths[j] += w;
											} else {
												int delta2 = w / spanExpandCount;
												int remainder2 = w % spanExpandCount, last2 = -1;
												for (int k = 0; k < hSpan; k++) {
													if (expandColumn[j - k]) {
														widths[last2 = j - k] += delta2;
													}
												}
												if (last2 > -1) {
													widths[last2] += remainder2;
												}
											}
										}
									}
								}
							}
						}
					}
					if (c == 0) {
						break;
					}
					totalWidth = 0;
					for (int i = 0; i < mColumns; i++) {
						totalWidth += widths[i];
					}
					delta = (availableWidth - totalWidth) / c;
					remainder = (availableWidth - totalWidth) % c;
					last = -1;
				}
			}
		}
		return widths;
	}

	private void wrap(int width, Component[][] grid, int[] widths, boolean useMinimumSize) {
		if (width != DEFAULT) {
			for (int j = 0; j < mColumns; j++) {
				for (int i = 0; i < mRowCount; i++) {
					PrecisionLayoutData data = getData(grid, i, j, mRowCount, false);
					if (data != null) {
						if (data.getHeightHint() == DEFAULT) {
							int hSpan = Math.max(1, Math.min(data.getHorizontalSpan(), mColumns));
							int currentWidth = 0;
							for (int k = 0; k < hSpan; k++) {
								currentWidth += widths[j - k];
							}
							currentWidth += (hSpan - 1) * mHSpacing - (data.getLeftMargin() + data.getRightMargin());
							if (currentWidth != data.getCachedWidth() && data.getHorizontalAlignment() == FILL || data.getCachedWidth() > currentWidth) {
								data.computeSize(grid[i][j], Math.max(data.getCachedMinimumWidth(), currentWidth), DEFAULT, useMinimumSize);
								int minimumHeight = data.getMinimumHeight();
								if (data.shouldGrabVerticalSpace() && minimumHeight > 0) {
									data.setCachedHeight(Math.max(data.getCachedHeight(), minimumHeight));
								}
							}
						}
					}
				}
			}
		}
	}

	private int[] adjustRowHeights(int height, Component[][] grid) {
		int availableHeight = height - mVSpacing * (mRowCount - 1) - (mMarginTop + mMarginBottom);
		int expandCount = 0;
		int[] heights = new int[mRowCount];
		int[] minHeights = new int[mRowCount];
		boolean[] expandRow = new boolean[mRowCount];
		for (int i = 0; i < mRowCount; i++) {
			for (int j = 0; j < mColumns; j++) {
				PrecisionLayoutData data = getData(grid, i, j, mRowCount, true);
				if (data != null) {
					int vSpan = Math.max(1, Math.min(data.getVerticalSpan(), mRowCount));
					if (vSpan == 1) {
						int h = data.getCachedHeight() + data.getTopMargin() + data.getBottomMargin();
						heights[i] = Math.max(heights[i], h);
						if (data.shouldGrabVerticalSpace()) {
							if (!expandRow[i]) {
								expandCount++;
							}
							expandRow[i] = true;
						}
						int minimumHeight = data.getMinimumHeight();
						if (!data.shouldGrabVerticalSpace() || minimumHeight != 0) {
							h = !data.shouldGrabVerticalSpace() || minimumHeight == DEFAULT ? data.getCachedHeight() : minimumHeight;
							h += data.getTopMargin() + data.getBottomMargin();
							minHeights[i] = Math.max(minHeights[i], h);
						}
					}
				}
			}
			for (int j = 0; j < mColumns; j++) {
				PrecisionLayoutData data = getData(grid, i, j, mRowCount, false);
				if (data != null) {
					int vSpan = Math.max(1, Math.min(data.getVerticalSpan(), mRowCount));
					if (vSpan > 1) {
						int spanHeight = 0, spanMinHeight = 0, spanExpandCount = 0;
						for (int k = 0; k < vSpan; k++) {
							spanHeight += heights[i - k];
							spanMinHeight += minHeights[i - k];
							if (expandRow[i - k]) {
								spanExpandCount++;
							}
						}
						if (data.shouldGrabVerticalSpace() && spanExpandCount == 0) {
							expandCount++;
							expandRow[i] = true;
						}
						int h = data.getCachedHeight() + data.getTopMargin() + data.getBottomMargin() - spanHeight - (vSpan - 1) * mVSpacing;
						if (h > 0) {
							if (spanExpandCount == 0) {
								heights[i] += h;
							} else {
								int delta = h / spanExpandCount;
								int remainder = h % spanExpandCount, last = -1;
								for (int k = 0; k < vSpan; k++) {
									if (expandRow[i - k]) {
										heights[last = i - k] += delta;
									}
								}
								if (last > -1) {
									heights[last] += remainder;
								}
							}
						}
						int minimumHeight = data.getMinimumHeight();
						if (!data.shouldGrabVerticalSpace() || minimumHeight != 0) {
							h = !data.shouldGrabVerticalSpace() || minimumHeight == DEFAULT ? data.getCachedHeight() : minimumHeight;
							h += data.getTopMargin() + data.getBottomMargin() - spanMinHeight - (vSpan - 1) * mVSpacing;
							if (h > 0) {
								if (spanExpandCount == 0) {
									minHeights[i] += h;
								} else {
									int delta = h / spanExpandCount;
									int remainder = h % spanExpandCount, last = -1;
									for (int k = 0; k < vSpan; k++) {
										if (expandRow[i - k]) {
											minHeights[last = i - k] += delta;
										}
									}
									if (last > -1) {
										minHeights[last] += remainder;
									}
								}
							}
						}
					}
				}
			}
		}
		if (height != DEFAULT && expandCount > 0) {
			int totalHeight = 0;
			for (int i = 0; i < mRowCount; i++) {
				totalHeight += heights[i];
			}
			int c = expandCount;
			int delta = (availableHeight - totalHeight) / c;
			int remainder = (availableHeight - totalHeight) % c;
			int last = -1;
			while (totalHeight != availableHeight) {
				for (int i = 0; i < mRowCount; i++) {
					if (expandRow[i]) {
						if (heights[i] + delta > minHeights[i]) {
							heights[last = i] = heights[i] + delta;
						} else {
							heights[i] = minHeights[i];
							expandRow[i] = false;
							c--;
						}
					}
				}
				if (last > -1) {
					heights[last] += remainder;
				}

				for (int i = 0; i < mRowCount; i++) {
					for (int j = 0; j < mColumns; j++) {
						PrecisionLayoutData data = getData(grid, i, j, mRowCount, false);
						if (data != null) {
							int vSpan = Math.max(1, Math.min(data.getVerticalSpan(), mRowCount));
							if (vSpan > 1) {
								int minimumHeight = data.getMinimumHeight();
								if (!data.shouldGrabVerticalSpace() || minimumHeight != 0) {
									int spanHeight = 0, spanExpandCount = 0;
									for (int k = 0; k < vSpan; k++) {
										spanHeight += heights[i - k];
										if (expandRow[i - k]) {
											spanExpandCount++;
										}
									}
									int h = !data.shouldGrabVerticalSpace() || minimumHeight == DEFAULT ? data.getCachedHeight() : minimumHeight;
									h += data.getTopMargin() + data.getBottomMargin() - spanHeight - (vSpan - 1) * mVSpacing;
									if (h > 0) {
										if (spanExpandCount == 0) {
											heights[i] += h;
										} else {
											int delta2 = h / spanExpandCount;
											int remainder2 = h % spanExpandCount, last2 = -1;
											for (int k = 0; k < vSpan; k++) {
												if (expandRow[i - k]) {
													heights[last2 = i - k] += delta2;
												}
											}
											if (last2 > -1) {
												heights[last2] += remainder2;
											}
										}
									}
								}
							}
						}
					}
				}
				if (c == 0) {
					break;
				}
				totalHeight = 0;
				for (int i = 0; i < mRowCount; i++) {
					totalHeight += heights[i];
				}
				delta = (availableHeight - totalHeight) / c;
				remainder = (availableHeight - totalHeight) % c;
				last = -1;
			}
		}
		return heights;
	}
}
