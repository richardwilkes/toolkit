/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.menu.file;

import com.apple.eawt.AppEvent.PrintFilesEvent;
import com.apple.eawt.PrintFilesHandler;
import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.print.PrintManager;
import com.trollworks.toolkit.ui.widget.AppWindow;
import com.trollworks.toolkit.ui.widget.WindowUtils;
import com.trollworks.toolkit.utility.Localization;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.Printable;
import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.swing.JMenuItem;

/** Provides the "Print..." command. */
public class PrintCommand extends Command implements PrintFilesHandler {
	@Localize("Print\u2026")
	private static String				PRINT;
	@Localize("There is no system printer available.")
	private static String				NO_PRINTER_SELECTED;

	static {
		Localization.initialize();
	}

	/** The action command this command will issue. */
	public static final String			CMD_PRINT	= "Print";				//$NON-NLS-1$

	/** The singleton {@link PrintCommand}. */
	public static final PrintCommand	INSTANCE	= new PrintCommand();

	private PrintCommand() {
		super(PRINT, CMD_PRINT, KeyEvent.VK_P);
	}

	@Override
	public void adjustForMenu(JMenuItem item) {
		Window window = getActiveWindow();
		setEnabled(window instanceof AppWindow && window instanceof Printable);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		print((AppWindow) getActiveWindow());
	}

	/** @param window The {@link AppWindow} to print. */
	public static void print(AppWindow window) {
		if (window instanceof Printable) {
			PrintManager mgr = window.getPrintManager();
			if (mgr != null) {
				mgr.print(window, window.getTitle(), (Printable) window);
			} else {
				WindowUtils.showError(window, NO_PRINTER_SELECTED);
			}
		}
	}

	@Override
	public void printFiles(PrintFilesEvent event) {
		for (File file : event.getFiles()) {
			EventQueue.invokeLater(new DeferredPrint(file));
		}
	}

	class DeferredPrint implements Runnable {
		private long	mStart;
		private File	mFile;

		DeferredPrint(File file) {
			mFile = file;
			OpenDataFileCommand.open(file);
			mStart = System.currentTimeMillis();
		}

		@Override
		public void run() {
			AppWindow window = AppWindow.findWindow(mFile);
			if (window != null) {
				PrintCommand.print(window);
			} else if (System.currentTimeMillis() - mStart < TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES)) {
				EventQueue.invokeLater(this);
			}
		}
	}
}
