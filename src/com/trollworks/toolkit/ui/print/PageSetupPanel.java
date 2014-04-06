/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.trollworks.toolkit.ui.print;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.GraphicsUtilities;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.layout.Alignment;
import com.trollworks.toolkit.ui.layout.FlexColumn;
import com.trollworks.toolkit.ui.layout.FlexComponent;
import com.trollworks.toolkit.ui.layout.FlexGrid;
import com.trollworks.toolkit.ui.widget.CommitEnforcer;
import com.trollworks.toolkit.ui.widget.EditorField;
import com.trollworks.toolkit.ui.widget.LinkedLabel;
import com.trollworks.toolkit.ui.widget.WindowUtils;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.text.DoubleFormatter;
import com.trollworks.toolkit.utility.units.LengthUnits;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.ResolutionSyntax;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.NumberUp;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.attribute.standard.Sides;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultFormatterFactory;

/** Provides the basic page setup panel. */
public class PageSetupPanel extends JPanel implements ActionListener {
	@Localize("Printer")
	private static String						PRINTER;
	@Localize("Paper Type")
	private static String						PAPER_TYPE;
	@Localize("Orientation")
	private static String						ORIENTATION;
	@Localize("Sides")
	private static String						SIDES;
	@Localize("Number Up")
	private static String						NUMBER_UP;
	@Localize("Color")
	private static String						CHROMATICITY;
	@Localize("Quality")
	private static String						QUALITY;
	@Localize("Resolution")
	private static String						RESOLUTION;
	@Localize(" dpi")
	private static String						DPI;
	@Localize("Margins")
	private static String						MARGINS;
	@Localize("(inches)")
	private static String						INCHES;

	static {
		Localization.initialize();
	}

	private PrintService						mService;
	private JComboBox<WrappedPrintService>		mServices;
	private JComboBox<PageOrientation>			mOrientation;
	private JComboBox<WrappedMediaSizeName>		mPaperType;
	private EditorField							mTopMargin;
	private EditorField							mLeftMargin;
	private EditorField							mRightMargin;
	private EditorField							mBottomMargin;
	private JComboBox<InkChromaticity>			mChromaticity;
	private JComboBox<PageSides>				mSides;
	private JComboBox<WrappedNumberUp>			mNumberUp;
	private JComboBox<Quality>					mPrintQuality;
	private JComboBox<WrappedPrinterResolution>	mResolution;

	/**
	 * Creates a new page setup panel.
	 *
	 * @param service The {@link PrintService} to use.
	 * @param set The {@link PrintRequestAttributeSet} to use.
	 */
	public PageSetupPanel(PrintService service, PrintRequestAttributeSet set) {
		super();
		setBorder(new EmptyBorder(5, 5, 5, 5));
		rebuild(service, set);
	}

	/** @return The service. */
	public PrintService getService() {
		return mService;
	}

	private void rebuild(PrintService service, PrintRequestAttributeSet set) {
		removeAll();
		mService = service;
		FlexGrid grid = new FlexGrid();
		int row = createPrinterCombo(grid, 0);
		rebuildSelf(set, grid, row);
		grid.apply(this);
		revalidate();
		Window window = WindowUtils.getWindowForComponent(this);
		if (window != null) {
			window.setSize(window.getPreferredSize());
			GraphicsUtilities.forceOnScreen(window);
		}
	}

	/**
	 * @param set The current {@link PrintRequestAttributeSet}.
	 * @param grid The {@link FlexGrid} to place components within.
	 * @param row The row to begin placement.
	 */
	protected void rebuildSelf(PrintRequestAttributeSet set, FlexGrid grid, int row) {
		row = createPaperTypeCombo(set, grid, row);
		row = createOrientationCombo(set, grid, row);
		row = createSidesCombo(set, grid, row);
		row = createNumberUpCombo(set, grid, row);
		row = createChromaticityCombo(set, grid, row);
		row = createPrintQualityCombo(set, grid, row);
		row = createResolutionCombo(set, grid, row);
		row = createMarginFields(set, grid, row);
	}

	// This is only here because the compiler wouldn't let me do this:
	// new ObjectWrapper<PrintService>[0]
	class WrappedPrintService extends ObjectWrapper<PrintService> {
		WrappedPrintService(String title, PrintService object) {
			super(title, object);
		}
	}

	private int createPrinterCombo(FlexGrid grid, int row) {
		PrintService[] services = PrinterJob.lookupPrintServices();
		if (services.length == 0) {
			services = new PrintService[] { new DummyPrintService() };
		}
		WrappedPrintService[] serviceWrappers = new WrappedPrintService[services.length];
		int selection = 0;
		for (int i = 0; i < services.length; i++) {
			serviceWrappers[i] = new WrappedPrintService(services[i].getName(), services[i]);
			if (services[i] == mService) {
				selection = i;
			}
		}
		mServices = new JComboBox<>(serviceWrappers);
		mServices.setSelectedIndex(selection);
		UIUtilities.setOnlySize(mServices, mServices.getPreferredSize());
		mServices.addActionListener(this);
		mService = services[selection];
		LinkedLabel label = new LinkedLabel(PRINTER, mServices);
		add(label);
		add(mServices);
		grid.add(new FlexComponent(label, Alignment.RIGHT_BOTTOM, Alignment.CENTER), row, 0);
		grid.add(mServices, row, 1);
		return row + 1;
	}

	// This is only here because the compiler wouldn't let me do this:
	// new ObjectWrapper<MediaSizeName>[0]
	class WrappedMediaSizeName extends ObjectWrapper<MediaSizeName> {
		WrappedMediaSizeName(String title, MediaSizeName object) {
			super(title, object);
		}
	}

	private int createPaperTypeCombo(PrintRequestAttributeSet set, FlexGrid grid, int row) {
		Media[] possible = (Media[]) mService.getSupportedAttributeValues(Media.class, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
		if (possible != null && possible.length > 0) {
			Media current = (Media) PrintUtilities.getSetting(mService, set, Media.class, true);
			if (current == null) {
				current = MediaSizeName.NA_LETTER;
			}
			ArrayList<WrappedMediaSizeName> types = new ArrayList<>();
			int selection = 0;
			int index = 0;
			for (Media one : possible) {
				if (one instanceof MediaSizeName) {
					MediaSizeName name = (MediaSizeName) one;
					types.add(new WrappedMediaSizeName(cleanUpMediaSizeName(name), name));
					if (name == current) {
						selection = index;
					}
					index++;
				}
			}
			mPaperType = new JComboBox<>(types.toArray(new WrappedMediaSizeName[0]));
			mPaperType.setSelectedIndex(selection);
			UIUtilities.setOnlySize(mPaperType, mPaperType.getPreferredSize());
			LinkedLabel label = new LinkedLabel(PAPER_TYPE, mPaperType);
			add(label);
			add(mPaperType);
			grid.add(new FlexComponent(label, Alignment.RIGHT_BOTTOM, Alignment.CENTER), row, 0);
			grid.add(mPaperType, row, 1);
			return row + 1;
		}
		mPaperType = null;
		return row;
	}

	private static String cleanUpMediaSizeName(MediaSizeName msn) {
		StringBuilder builder = new StringBuilder();
		StringTokenizer tokenizer = new StringTokenizer(msn.toString(), "- ", true); //$NON-NLS-1$

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();

			if (token.equalsIgnoreCase("na")) { //$NON-NLS-1$
				builder.append("US"); //$NON-NLS-1$
			} else if (token.equalsIgnoreCase("iso")) { //$NON-NLS-1$
				builder.append("ISO"); //$NON-NLS-1$
			} else if (token.equalsIgnoreCase("jis")) { //$NON-NLS-1$
				builder.append("JIS"); //$NON-NLS-1$
			} else if (token.equals("-")) { //$NON-NLS-1$
				builder.append(" "); //$NON-NLS-1$
			} else if (token.length() > 1) {
				builder.append(Character.toUpperCase(token.charAt(0)));
				builder.append(token.substring(1));
			} else {
				builder.append(token.toUpperCase());
			}
		}

		return builder.toString();
	}

	private int createOrientationCombo(PrintRequestAttributeSet set, FlexGrid grid, int row) {
		OrientationRequested[] orientations = (OrientationRequested[]) mService.getSupportedAttributeValues(OrientationRequested.class, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
		if (orientations != null && orientations.length > 0) {
			HashSet<OrientationRequested> possible = new HashSet<>();
			for (OrientationRequested one : orientations) {
				possible.add(one);
			}
			ArrayList<PageOrientation> choices = new ArrayList<>();
			for (PageOrientation orientation : PageOrientation.values()) {
				if (possible.contains(orientation.getOrientationRequested())) {
					choices.add(orientation);
				}
			}
			mOrientation = new JComboBox<>(choices.toArray(new PageOrientation[0]));
			mOrientation.setSelectedItem(PrintUtilities.getPageOrientation(mService, set));
			UIUtilities.setOnlySize(mOrientation, mOrientation.getPreferredSize());
			LinkedLabel label = new LinkedLabel(ORIENTATION, mOrientation);
			add(label);
			add(mOrientation);
			grid.add(new FlexComponent(label, Alignment.RIGHT_BOTTOM, Alignment.CENTER), row, 0);
			grid.add(mOrientation, row, 1);
			return row + 1;
		}
		mOrientation = null;
		return row;
	}

	private int createSidesCombo(PrintRequestAttributeSet set, FlexGrid grid, int row) {
		Sides[] sides = (Sides[]) mService.getSupportedAttributeValues(Sides.class, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
		if (sides != null && sides.length > 0) {
			HashSet<Sides> possible = new HashSet<>();
			for (Sides one : sides) {
				possible.add(one);
			}
			ArrayList<PageSides> choices = new ArrayList<>();
			for (PageSides side : PageSides.values()) {
				if (possible.contains(side.getSides())) {
					choices.add(side);
				}
			}
			mSides = new JComboBox<>(choices.toArray(new PageSides[0]));
			mSides.setSelectedItem(PrintUtilities.getSides(mService, set));
			UIUtilities.setOnlySize(mSides, mSides.getPreferredSize());
			LinkedLabel label = new LinkedLabel(SIDES, mSides);
			add(label);
			add(mSides);
			grid.add(new FlexComponent(label, Alignment.RIGHT_BOTTOM, Alignment.CENTER), row, 0);
			grid.add(mSides, row, 1);
			return row + 1;
		}
		mSides = null;
		return row;
	}

	// This is only here because the compiler wouldn't let me do this:
	// new ObjectWrapper<NumberUp>[0]
	class WrappedNumberUp extends ObjectWrapper<NumberUp> {
		WrappedNumberUp(String title, NumberUp object) {
			super(title, object);
		}
	}

	private int createNumberUpCombo(PrintRequestAttributeSet set, FlexGrid grid, int row) {
		NumberUp[] numUp = (NumberUp[]) mService.getSupportedAttributeValues(NumberUp.class, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
		if (numUp != null && numUp.length > 0) {
			NumberUp current = PrintUtilities.getNumberUp(mService, set);
			WrappedNumberUp[] wrappers = new WrappedNumberUp[numUp.length];
			int selection = 0;
			for (int i = 0; i < numUp.length; i++) {
				wrappers[i] = new WrappedNumberUp(numUp[i].toString(), numUp[i]);
				if (numUp[i] == current) {
					selection = i;
				}
			}
			mNumberUp = new JComboBox<>(wrappers);
			mNumberUp.setSelectedIndex(selection);
			UIUtilities.setOnlySize(mNumberUp, mNumberUp.getPreferredSize());
			LinkedLabel label = new LinkedLabel(NUMBER_UP, mNumberUp);
			add(label);
			add(mNumberUp);
			grid.add(new FlexComponent(label, Alignment.RIGHT_BOTTOM, Alignment.CENTER), row, 0);
			grid.add(mNumberUp, row, 1);
			return row + 1;
		}
		mNumberUp = null;
		return row;
	}

	private int createChromaticityCombo(PrintRequestAttributeSet set, FlexGrid grid, int row) {
		Chromaticity[] chromacities = (Chromaticity[]) mService.getSupportedAttributeValues(Chromaticity.class, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
		if (chromacities != null && chromacities.length > 0) {
			HashSet<Chromaticity> possible = new HashSet<>();
			for (Chromaticity one : chromacities) {
				possible.add(one);
			}
			ArrayList<InkChromaticity> choices = new ArrayList<>();
			for (InkChromaticity chromaticity : InkChromaticity.values()) {
				if (possible.contains(chromaticity.getChromaticity())) {
					choices.add(chromaticity);
				}
			}
			mChromaticity = new JComboBox<>(choices.toArray(new InkChromaticity[0]));
			mChromaticity.setSelectedItem(PrintUtilities.getChromaticity(mService, set, true));
			UIUtilities.setOnlySize(mChromaticity, mChromaticity.getPreferredSize());
			LinkedLabel label = new LinkedLabel(CHROMATICITY, mChromaticity);
			add(label);
			add(mChromaticity);
			grid.add(new FlexComponent(label, Alignment.RIGHT_BOTTOM, Alignment.CENTER), row, 0);
			grid.add(mChromaticity, row, 1);
			return row + 1;
		}
		mChromaticity = null;
		return row;
	}

	private int createPrintQualityCombo(PrintRequestAttributeSet set, FlexGrid grid, int row) {
		PrintQuality[] qualities = (PrintQuality[]) mService.getSupportedAttributeValues(PrintQuality.class, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
		if (qualities != null && qualities.length > 0) {
			HashSet<PrintQuality> possible = new HashSet<>();
			for (PrintQuality one : qualities) {
				possible.add(one);
			}
			ArrayList<Quality> choices = new ArrayList<>();
			for (Quality quality : Quality.values()) {
				if (possible.contains(quality.getPrintQuality())) {
					choices.add(quality);
				}
			}
			mPrintQuality = new JComboBox<>(choices.toArray(new Quality[0]));
			mPrintQuality.setSelectedItem(PrintUtilities.getPrintQuality(mService, set, true));
			UIUtilities.setOnlySize(mPrintQuality, mPrintQuality.getPreferredSize());
			LinkedLabel label = new LinkedLabel(QUALITY, mPrintQuality);
			add(label);
			add(mPrintQuality);
			grid.add(new FlexComponent(label, Alignment.RIGHT_BOTTOM, Alignment.CENTER), row, 0);
			grid.add(mPrintQuality, row, 1);
			return row + 1;
		}
		mPrintQuality = null;
		return row;
	}

	// This is only here because the compiler wouldn't let me do this:
	// new ObjectWrapper<PrinterResolution>[0]
	class WrappedPrinterResolution extends ObjectWrapper<PrinterResolution> {
		WrappedPrinterResolution(String title, PrinterResolution object) {
			super(title, object);
		}
	}

	private int createResolutionCombo(PrintRequestAttributeSet set, FlexGrid grid, int row) {
		PrinterResolution[] resolutions = (PrinterResolution[]) mService.getSupportedAttributeValues(PrinterResolution.class, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
		if (resolutions != null && resolutions.length > 0) {
			PrinterResolution current = PrintUtilities.getResolution(mService, set, true);
			WrappedPrinterResolution[] wrappers = new WrappedPrinterResolution[resolutions.length];
			int selection = 0;
			for (int i = 0; i < resolutions.length; i++) {
				wrappers[i] = new WrappedPrinterResolution(generateResolutionTitle(resolutions[i]), resolutions[i]);
				if (resolutions[i] == current) {
					selection = i;
				}
			}
			mResolution = new JComboBox<>(wrappers);
			mResolution.setSelectedIndex(selection);
			UIUtilities.setOnlySize(mResolution, mResolution.getPreferredSize());
			LinkedLabel label = new LinkedLabel(RESOLUTION, mResolution);
			add(label);
			add(mResolution);
			grid.add(new FlexComponent(label, Alignment.RIGHT_BOTTOM, Alignment.CENTER), row, 0);
			grid.add(mResolution, row, 1);
			return row + 1;
		}
		mResolution = null;
		return row;
	}

	private static String generateResolutionTitle(PrinterResolution res) {
		StringBuilder buffer = new StringBuilder();
		int x = res.getCrossFeedResolution(ResolutionSyntax.DPI);
		int y = res.getFeedResolution(ResolutionSyntax.DPI);

		buffer.append(Integer.toString(x));
		if (x != y) {
			buffer.append(" x "); //$NON-NLS-1$
			buffer.append(Integer.toString(y));
		}
		buffer.append(DPI);
		return buffer.toString();
	}

	private int createMarginFields(PrintRequestAttributeSet set, FlexGrid grid, int row) {
		FlexColumn column = new FlexColumn();
		column.setFill(false);
		column.setVerticalAlignment(Alignment.CENTER);
		JLabel label = new JLabel(MARGINS, SwingConstants.RIGHT);
		add(label);
		column.add(new FlexComponent(label, Alignment.RIGHT_BOTTOM, Alignment.CENTER));
		label = new JLabel(INCHES, SwingConstants.RIGHT);
		add(label);
		column.add(new FlexComponent(label, Alignment.RIGHT_BOTTOM, Alignment.CENTER));
		grid.add(column, row, 0);
		FlexGrid innerGrid = new FlexGrid();
		grid.add(innerGrid, row++, 1);
		double[] margins = PrintUtilities.getPageMargins(mService, set, LengthUnits.INCHES);
		mTopMargin = createMarginField(margins[0]);
		innerGrid.add(mTopMargin, 0, 1);
		mLeftMargin = createMarginField(margins[1]);
		innerGrid.add(mLeftMargin, 1, 0);
		mRightMargin = createMarginField(margins[3]);
		innerGrid.add(mRightMargin, 1, 2);
		mBottomMargin = createMarginField(margins[2]);
		innerGrid.add(mBottomMargin, 2, 1);
		return row;
	}

	private EditorField createMarginField(double margin) {
		EditorField field = new EditorField(new DefaultFormatterFactory(new DoubleFormatter(0, 999.999, false)), null, SwingConstants.RIGHT, new Double(margin), new Double(999.999), null);
		UIUtilities.setOnlySize(field, field.getPreferredSize());
		add(field);
		return field;
	}

	/**
	 * Accepts the changes made by the user and incorporates them into the specified attribute set.
	 *
	 * @param set The set to modify.
	 * @return The {@link PrintService} selected by the user.
	 */
	public PrintService accept(PrintRequestAttributeSet set) {
		CommitEnforcer.forceFocusToAccept();
		mService = UIUtilities.getTypedSelectedItemFromCombo(mServices).getObject();
		if (mOrientation != null) {
			PrintUtilities.setPageOrientation(set, (PageOrientation) mOrientation.getSelectedItem());
		}
		if (mPaperType != null) {
			PrintUtilities.setPaperSize(mService, set, PrintUtilities.getMediaDimensions(((WrappedMediaSizeName) mPaperType.getSelectedItem()).getObject(), LengthUnits.INCHES), LengthUnits.INCHES);
		}
		PrintUtilities.setPageMargins(mService, set, new double[] { ((Double) mTopMargin.getValue()).doubleValue(), ((Double) mLeftMargin.getValue()).doubleValue(), ((Double) mBottomMargin.getValue()).doubleValue(), ((Double) mRightMargin.getValue()).doubleValue() }, LengthUnits.INCHES);
		if (mChromaticity != null) {
			PrintUtilities.setChromaticity(set, (InkChromaticity) mChromaticity.getSelectedItem());
		}
		if (mSides != null) {
			PrintUtilities.setSides(set, (PageSides) mSides.getSelectedItem());
		}
		if (mNumberUp != null) {
			PrintUtilities.setNumberUp(set, UIUtilities.getTypedSelectedItemFromCombo(mNumberUp).getObject());
		}
		if (mPrintQuality != null) {
			PrintUtilities.setPrintQuality(set, (Quality) mPrintQuality.getSelectedItem());
		}
		if (mResolution != null) {
			PrintUtilities.setResolution(set, UIUtilities.getTypedSelectedItemFromCombo(mResolution).getObject());
		}
		return mService instanceof DummyPrintService ? null : mService;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if (src == mServices) {
			HashPrintRequestAttributeSet set = new HashPrintRequestAttributeSet();
			rebuild(accept(set), set);
		}
	}
}
