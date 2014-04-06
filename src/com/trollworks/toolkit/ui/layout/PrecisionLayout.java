/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.trollworks.toolkit.ui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public final class PrecisionLayout implements LayoutManager2 {
	private static final String						KEY_COLUMNS			= "columns";						//$NON-NLS-1$
	private static final String						KEY_EQUAL_COLUMNS	= "equalColumns";					//$NON-NLS-1$
	private static final String						KEY_HSPACING		= "hSpacing";						//$NON-NLS-1$
	private static final String						KEY_VSPACING		= "vSpacing";						//$NON-NLS-1$
	/**
	 * Specifies the number of cell columns in the layout. If this has a value less than 1, the
	 * layout will not set the size and position of any children. The default value is 1.
	 */
	public int										mColumns			= 1;
	/**
	 * Specifies whether all columns in the layout will be forced to have the same width. The
	 * default value is <code>false</code>.
	 */
	public boolean									mEqualColumns		= false;
	/**
	 * Specifies the number of pixels of horizontal margin that will be placed along the left edge
	 * of the layout. The default value is 5.
	 */
	public int										mMarginLeft			= 4;
	/**
	 * Specifies the number of pixels of vertical margin that will be placed along the top edge of
	 * the layout. The default value is 5.
	 */
	public int										mMarginTop			= 4;
	/**
	 * Specifies the number of pixels of horizontal margin that will be placed along the right edge
	 * of the layout. The default value is 5.
	 */
	public int										mMarginRight		= 4;
	/**
	 * Specifies the number of pixels of vertical margin that will be placed along the bottom edge
	 * of the layout. The default value is 5.
	 */
	public int										mMarginBottom		= 4;
	/**
	 * Specifies the number of pixels between the right edge of one cell and the left edge of its
	 * neighboring cell to the right. The default value is 4.
	 */
	public int										mHSpacing			= 4;
	/**
	 * Specifies the number of pixels between the bottom edge of one cell and the top edge of its
	 * neighboring cell underneath. The default value is 2.
	 */
	public int										mVSpacing			= 2;
	/**
	 * Specifies how components will be positioned vertically within the container. The default
	 * value is {@link PrecisionLayoutData#BEGINNING}. Possible values are:
	 * <ul>
	 * <li>{@link PrecisionLayoutData#BEGINNING}: Position the components at the top of the
	 * container</li>
	 * <li>{@link PrecisionLayoutData#MIDDLE}: Position the components in the vertical center of the
	 * container</li>
	 * <li>{@link PrecisionLayoutData#END}: Position the components at the bottom of the container</li>
	 * </ul>
	 */
	public int										mVAlign				= PrecisionLayoutData.BEGINNING;
	/**
	 * Specifies how components will be positioned horizontally within the container. The default
	 * value is {@link PrecisionLayoutData#BEGINNING}. Possible values are:
	 * <ul>
	 * <li>{@link PrecisionLayoutData#BEGINNING}: Position the components at the left of the
	 * container</li>
	 * <li>{@link PrecisionLayoutData#MIDDLE}: Position the components in the horizontal center of
	 * the container</li>
	 * <li>{@link PrecisionLayoutData#END}: Position the components at the right of the container</li>
	 * </ul>
	 */
	public int										mHAlign				= PrecisionLayoutData.BEGINNING;
	private HashMap<Component, PrecisionLayoutData>	mConstraints		= new HashMap<>();
	private int										mRowCount;

	public PrecisionLayout() {
		super();
	}

	/**
	 * Creates a new {@link PrecisionLayout} from a text description. Valid keywords are:
	 * <ul>
	 * <li>columns - sets {@link #mColumns}</li>
	 * <li>equalColumns - sets {@link #mEqualColumns}</li>
	 * <li>margins - sets {@link #mMarginTop}, {@link #mMarginLeft}, {@link #mMarginBottom} and
	 * {@link #mMarginRight} to the same value</li>
	 * <li>top - sets {@link #mMarginTop}</li>
	 * <li>left - sets {@link #mMarginLeft}</li>
	 * <li>bottom - sets {@link #mMarginBottom}</li>
	 * <li>right - sets {@link #mMarginRight}</li>
	 * <li>hSpacing - sets {@link #mHSpacing}</li>
	 * <li>vSpacing - sets {@link #mVSpacing}</li>
	 * <li>hAlign - sets {@link #mHAlign}</li>
	 * <li>vAlign - sets {@link #mVAlign}</li>
	 * </ul>
	 *
	 * @param settings The settings string to parse.
	 */
	public PrecisionLayout(String settings) {
		StringTokenizer tokenizer = new StringTokenizer(settings);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			int sep = token.indexOf(':');
			String key = token.substring(0, sep);
			String value = token.substring(sep + 1);
			if (KEY_COLUMNS.equalsIgnoreCase(key)) {
				mColumns = Integer.parseInt(value);
			} else if (KEY_EQUAL_COLUMNS.equalsIgnoreCase(key)) {
				mEqualColumns = PrecisionLayoutData.decodeBoolean(value);
			} else if (PrecisionLayoutData.KEY_MARGINS.equalsIgnoreCase(key)) {
				mMarginTop = Integer.parseInt(value);
				mMarginLeft = mMarginTop;
				mMarginBottom = mMarginTop;
				mMarginRight = mMarginTop;
			} else if (PrecisionLayoutData.KEY_TOP.equalsIgnoreCase(key)) {
				mMarginTop = Integer.parseInt(value);
			} else if (PrecisionLayoutData.KEY_LEFT.equalsIgnoreCase(key)) {
				mMarginLeft = Integer.parseInt(value);
			} else if (PrecisionLayoutData.KEY_BOTTOM.equalsIgnoreCase(key)) {
				mMarginBottom = Integer.parseInt(value);
			} else if (PrecisionLayoutData.KEY_RIGHT.equalsIgnoreCase(key)) {
				mMarginRight = Integer.parseInt(value);
			} else if (KEY_HSPACING.equalsIgnoreCase(key)) {
				mHSpacing = Integer.parseInt(value);
			} else if (KEY_VSPACING.equalsIgnoreCase(key)) {
				mVSpacing = Integer.parseInt(value);
			} else if (PrecisionLayoutData.KEY_HALIGN.equalsIgnoreCase(key)) {
				mHAlign = PrecisionLayoutData.decodeAlignment(value, false);
			} else if (PrecisionLayoutData.KEY_VALIGN.equalsIgnoreCase(key)) {
				mVAlign = PrecisionLayoutData.decodeAlignment(value, false);
			} else {
				throw new IllegalArgumentException(token);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		PrecisionLayoutData.emit(KEY_COLUMNS, mColumns, buffer);
		PrecisionLayoutData.emit(KEY_EQUAL_COLUMNS, mEqualColumns, buffer);
		if (mMarginTop == mMarginLeft && mMarginTop == mMarginBottom && mMarginTop == mMarginRight) {
			PrecisionLayoutData.emit(PrecisionLayoutData.KEY_MARGINS, mMarginTop, buffer);
		} else {
			PrecisionLayoutData.emit(PrecisionLayoutData.KEY_TOP, mMarginTop, buffer);
			PrecisionLayoutData.emit(PrecisionLayoutData.KEY_LEFT, mMarginLeft, buffer);
			PrecisionLayoutData.emit(PrecisionLayoutData.KEY_BOTTOM, mMarginBottom, buffer);
			PrecisionLayoutData.emit(PrecisionLayoutData.KEY_RIGHT, mMarginBottom, buffer);
		}
		PrecisionLayoutData.emit(KEY_HSPACING, mHSpacing, buffer);
		PrecisionLayoutData.emit(KEY_VSPACING, mVSpacing, buffer);
		PrecisionLayoutData.emitAlign(PrecisionLayoutData.KEY_HALIGN, mHAlign, buffer);
		PrecisionLayoutData.emitAlign(PrecisionLayoutData.KEY_VALIGN, mVAlign, buffer);
		return buffer.toString();
	}

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		if (constraints == null) {
			constraints = new PrecisionLayoutData();
		} else if (!(constraints instanceof PrecisionLayoutData)) {
			constraints = new PrecisionLayoutData(constraints.toString());
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
			one.mCacheHeight = 0;
			one.mCacheWidth = 0;
			one.mCacheMinWidth = 0;
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container target) {
		Insets insets = target.getInsets();
		Dimension size = layout(target, false, 0, 0, PrecisionLayoutData.DEFAULT, PrecisionLayoutData.DEFAULT, true);
		size.width += insets.left + insets.right;
		size.height += insets.top + insets.bottom;
		return size;
	}

	@Override
	public Dimension preferredLayoutSize(Container target) {
		Insets insets = target.getInsets();
		Dimension size = layout(target, false, 0, 0, PrecisionLayoutData.DEFAULT, PrecisionLayoutData.DEFAULT, false);
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
						if (mHAlign == PrecisionLayoutData.MIDDLE) {
							x += (width - totalWidth) / 2;
						} else if (mHAlign == PrecisionLayoutData.END) {
							x += width - totalWidth;
						}
					}
					if (totalHeight < height) {
						if (mVAlign == PrecisionLayoutData.MIDDLE) {
							y += (height - totalHeight) / 2;
						} else if (mVAlign == PrecisionLayoutData.END) {
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
					int hSpan = Math.max(1, Math.min(data.mHSpan, mColumns));
					int vSpan = Math.max(1, data.mVSpan);
					int cellWidth = 0, cellHeight = 0;
					for (int k = 0; k < hSpan; k++) {
						cellWidth += widths[j + k];
					}
					for (int k = 0; k < vSpan; k++) {
						cellHeight += heights[i + k];
					}
					cellWidth += mHSpacing * (hSpan - 1);
					int childX = gridX + data.mMarginLeft;
					int childWidth = Math.min(data.mCacheWidth, cellWidth);
					switch (data.mHAlign) {
						case PrecisionLayoutData.MIDDLE:
							childX += Math.max(0, (cellWidth - (data.mMarginLeft + data.mMarginRight) - childWidth) / 2);
							break;
						case PrecisionLayoutData.END:
							childX += Math.max(0, cellWidth - (data.mMarginLeft + data.mMarginRight) - childWidth);
							break;
						case PrecisionLayoutData.FILL:
							childWidth = cellWidth - (data.mMarginLeft + data.mMarginRight);
							break;
						default:
							break;
					}
					cellHeight += mVSpacing * (vSpan - 1);
					int childY = gridY + data.mMarginTop;
					int childHeight = Math.min(data.mCacheHeight, cellHeight);
					switch (data.mVAlign) {
						case PrecisionLayoutData.MIDDLE:
							childY += Math.max(0, (cellHeight - (data.mMarginTop + data.mMarginBottom) - childHeight) / 2);
							break;
						case PrecisionLayoutData.END:
							childY += Math.max(0, cellHeight - (data.mMarginTop + data.mMarginBottom) - childHeight);
							break;
						case PrecisionLayoutData.FILL:
							childHeight = cellHeight - (data.mMarginTop + data.mMarginBottom);
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
			if (!data.mExclude) {
				children.add(child);
				data.computeSize(child, PrecisionLayoutData.DEFAULT, PrecisionLayoutData.DEFAULT, useMinimumSize);
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
			int hSpan = Math.max(1, Math.min(data.mHSpan, mColumns));
			int vSpan = Math.max(1, data.mVSpan);
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
			int hSpan = Math.max(1, Math.min(data.mHSpan, mColumns));
			int vSpan = Math.max(1, data.mVSpan);
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
					int hSpan = Math.max(1, Math.min(data.mHSpan, mColumns));
					if (hSpan == 1) {
						int w = data.mCacheWidth + data.mMarginLeft + data.mMarginRight;
						if (widths[j] < w) {
							widths[j] = w;
						}
						if (data.mHGrab) {
							if (!expandColumn[j]) {
								expandCount++;
							}
							expandColumn[j] = true;
						}
						int minimumWidth = data.mCacheMinWidth;
						if (!data.mHGrab || minimumWidth != 0) {
							w = !data.mHGrab || minimumWidth == PrecisionLayoutData.DEFAULT ? data.mCacheWidth : minimumWidth;
							w += data.mMarginLeft + data.mMarginRight;
							minWidths[j] = Math.max(minWidths[j], w);
						}
					}
				}
			}
			for (int i = 0; i < mRowCount; i++) {
				PrecisionLayoutData data = getData(grid, i, j, mRowCount, false);
				if (data != null) {
					int hSpan = Math.max(1, Math.min(data.mHSpan, mColumns));
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
						if (data.mHGrab && spanExpandCount == 0) {
							expandCount++;
							expandColumn[j] = true;
						}
						int w = data.mCacheWidth + data.mMarginLeft + data.mMarginRight - spanWidth - (hSpan - 1) * mHSpacing;
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
						int minimumWidth = data.mCacheMinWidth;
						if (!data.mHGrab || minimumWidth != 0) {
							w = !data.mHGrab || minimumWidth == PrecisionLayoutData.DEFAULT ? data.mCacheWidth : minimumWidth;
							w += data.mMarginLeft + data.mMarginRight - spanMinWidth - (hSpan - 1) * mHSpacing;
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
			columnWidth = width == PrecisionLayoutData.DEFAULT || expandCount == 0 ? columnWidth : Math.max(minColumnWidth, availableWidth / mColumns);
			for (int i = 0; i < mColumns; i++) {
				expandColumn[i] = expandCount > 0;
				widths[i] = columnWidth;
			}
		} else {
			if (width != PrecisionLayoutData.DEFAULT && expandCount > 0) {
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
								int hSpan = Math.max(1, Math.min(data.mHSpan, mColumns));
								if (hSpan > 1) {
									int minimumWidth = data.mCacheMinWidth;
									if (!data.mHGrab || minimumWidth != 0) {
										int spanWidth = 0, spanExpandCount = 0;
										for (int k = 0; k < hSpan; k++) {
											spanWidth += widths[j - k];
											if (expandColumn[j - k]) {
												spanExpandCount++;
											}
										}
										int w = !data.mHGrab || minimumWidth == PrecisionLayoutData.DEFAULT ? data.mCacheWidth : minimumWidth;
										w += data.mMarginLeft + data.mMarginRight - spanWidth - (hSpan - 1) * mHSpacing;
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
		if (width != PrecisionLayoutData.DEFAULT) {
			for (int j = 0; j < mColumns; j++) {
				for (int i = 0; i < mRowCount; i++) {
					PrecisionLayoutData data = getData(grid, i, j, mRowCount, false);
					if (data != null) {
						if (data.mHeightHint == PrecisionLayoutData.DEFAULT) {
							int hSpan = Math.max(1, Math.min(data.mHSpan, mColumns));
							int currentWidth = 0;
							for (int k = 0; k < hSpan; k++) {
								currentWidth += widths[j - k];
							}
							currentWidth += (hSpan - 1) * mHSpacing - (data.mMarginLeft + data.mMarginRight);
							if (currentWidth != data.mCacheWidth && data.mHAlign == PrecisionLayoutData.FILL || data.mCacheWidth > currentWidth) {
								data.computeSize(grid[i][j], Math.max(data.mCacheMinWidth, currentWidth), PrecisionLayoutData.DEFAULT, useMinimumSize);
								int minimumHeight = data.mMinHeight;
								if (data.mVGrab && minimumHeight > 0) {
									data.mCacheHeight = Math.max(data.mCacheHeight, minimumHeight);
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
					int vSpan = Math.max(1, Math.min(data.mVSpan, mRowCount));
					if (vSpan == 1) {
						int h = data.mCacheHeight + data.mMarginTop + data.mMarginBottom;
						heights[i] = Math.max(heights[i], h);
						if (data.mVGrab) {
							if (!expandRow[i]) {
								expandCount++;
							}
							expandRow[i] = true;
						}
						int minimumHeight = data.mMinHeight;
						if (!data.mVGrab || minimumHeight != 0) {
							h = !data.mVGrab || minimumHeight == PrecisionLayoutData.DEFAULT ? data.mCacheHeight : minimumHeight;
							h += data.mMarginTop + data.mMarginBottom;
							minHeights[i] = Math.max(minHeights[i], h);
						}
					}
				}
			}
			for (int j = 0; j < mColumns; j++) {
				PrecisionLayoutData data = getData(grid, i, j, mRowCount, false);
				if (data != null) {
					int vSpan = Math.max(1, Math.min(data.mVSpan, mRowCount));
					if (vSpan > 1) {
						int spanHeight = 0, spanMinHeight = 0, spanExpandCount = 0;
						for (int k = 0; k < vSpan; k++) {
							spanHeight += heights[i - k];
							spanMinHeight += minHeights[i - k];
							if (expandRow[i - k]) {
								spanExpandCount++;
							}
						}
						if (data.mVGrab && spanExpandCount == 0) {
							expandCount++;
							expandRow[i] = true;
						}
						int h = data.mCacheHeight + data.mMarginTop + data.mMarginBottom - spanHeight - (vSpan - 1) * mVSpacing;
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
						int minimumHeight = data.mMinHeight;
						if (!data.mVGrab || minimumHeight != 0) {
							h = !data.mVGrab || minimumHeight == PrecisionLayoutData.DEFAULT ? data.mCacheHeight : minimumHeight;
							h += data.mMarginTop + data.mMarginBottom - spanMinHeight - (vSpan - 1) * mVSpacing;
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
		if (height != PrecisionLayoutData.DEFAULT && expandCount > 0) {
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
							int vSpan = Math.max(1, Math.min(data.mVSpan, mRowCount));
							if (vSpan > 1) {
								int minimumHeight = data.mMinHeight;
								if (!data.mVGrab || minimumHeight != 0) {
									int spanHeight = 0, spanExpandCount = 0;
									for (int k = 0; k < vSpan; k++) {
										spanHeight += heights[i - k];
										if (expandRow[i - k]) {
											spanExpandCount++;
										}
									}
									int h = !data.mVGrab || minimumHeight == PrecisionLayoutData.DEFAULT ? data.mCacheHeight : minimumHeight;
									h += data.mMarginTop + data.mMarginBottom - spanHeight - (vSpan - 1) * mVSpacing;
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
