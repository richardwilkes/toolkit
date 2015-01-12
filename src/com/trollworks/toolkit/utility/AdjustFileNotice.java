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

package com.trollworks.toolkit.utility;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.utility.cmdline.CmdLine;
import com.trollworks.toolkit.utility.cmdline.CmdLineOption;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.Calendar;
import java.util.jar.Attributes;

public class AdjustFileNotice {
	private static String				VERSION			= "1.0.0.20150108071600";											//$NON-NLS-1$
	private static String				COPYRIGHT_OWNER	= "Richard A. Wilkes";												//$NON-NLS-1$
	private static String				COPYRIGHT_YEARS	= "2014-2015";														//$NON-NLS-1$
	private static String				LICENSE			= "Mozilla Public License 2.0";									//$NON-NLS-1$

	@Localize("PATH")
	private static String				PATH;
	@Localize("The path containing the Java files to process. May be a directory or a file.")
	@Localize(locale = "ru", value = "Обрабатывается путь, содержащий Java файлы. Может быть папкой или файлом.")
	@Localize(locale = "de", value = "Der Pfad, in dem die zu Verändernden Java-Dateien sind. Kann ein Verzeichnis oder eine Datei sein.")
	@Localize(locale = "es", value = "Ruta hasta los archivos Java a ejecutar. Puede ser un directorio o archivo.")
	private static String				PATH_DESCRIPTION;
	@Localize("You must specify a path with the %s option.\n")
	@Localize(locale = "ru", value = "Вы должны указать путь при помощи параметра %s.\n")
	@Localize(locale = "de", value = "Ein Pfad muss mittels der Option %s angeben werden.\n")
	@Localize(locale = "es", value = "Debes especificar una ruta con la opción %s.\n")
	private static String				PATH_MUST_BE_SPECIFIED;
	@Localize("TEMPLATE")
	@Localize(locale = "ru", value = "ШАБЛОН")
	private static String				TEMPLATE;
	@Localize("The template to use for the new file header. All occurrences of $YEAR$ within the template will be replaced with the current year. If this option is not specified, the default template for the toolkit will be used.")
	@Localize(locale = "ru", value = "Шаблон, используемый для нового заголовка файла. Все вхождения $YEAR$ в шаблоне будет заменены на текущий год. Если этот параметр не указан, будет использоваться шаблон по умолчанию для инструментария.")
	@Localize(locale = "de", value = "Die Vorlage für den neuen Dateikopf. Jedes Auftreten von $YEAR$ in der Vorlage wird durch das aktuelle Jahr ersetzt. Wenn diese Option nicht gesetzt wird, wird die Standard-Vorlage des Toolkit benutzt.")
	@Localize(locale = "es", value = "Plantilla a usar como nueva cabecera del archivo. Todas las ocurrencias de $YEAR$ en la plantilla se reemplazarán por el año en curso. Si no se especifica esta opción, se usará la plantilla por defecto")
	private static String				TEMPLATE_DESCRIPTION;
	@Localize("Unexpected argument: ")
	@Localize(locale = "ru", value = "Неизвестный аргумент:")
	@Localize(locale = "de", value = "Unerwartetes Argument: ")
	@Localize(locale = "es", value = "Argumento inesperado: ")
	private static String				UNEXPECTED_ARGUMENT;
	@Localize("\nAdjusted %,d %s\n")
	@Localize(locale = "ru", value = "\nНастройка %,d %s\n")
	@Localize(locale = "de", value = "\nErgebnis: %,d %s verändert\n")
	@Localize(locale = "es", value = "\nResultado %,d %s\n")
	private static String				RESULT;
	@Localize("file")
	@Localize(locale = "ru", value = "файл")
	@Localize(locale = "de", value = "Datei")
	@Localize(locale = "es", value = "archivo")
	private static String				FILE;
	@Localize("files")
	@Localize(locale = "ru", value = "файлы")
	@Localize(locale = "de", value = "Dateien")
	@Localize(locale = "es", value = "archivos")
	private static String				FILES;
	@Localize("Adjusted %s\n")
	@Localize(locale = "ru", value = "Настройка %s\n")
	@Localize(locale = "de", value = "Verändert: %s\n")
	@Localize(locale = "es", value = "Modificado: %s\n")
	private static String				ADJUSTED;
	@Localize("Skipped %s\n")
	@Localize(locale = "ru", value = "Пропущено %s\n")
	@Localize(locale = "de", value = "Übersprungen: %s\n")
	@Localize(locale = "es", value = "Omitido: %s\n")
	private static String				SKIPPED;

	static {
		Localization.initialize();
	}

	private static final CmdLineOption	PATH_OPTION		= new CmdLineOption(PATH_DESCRIPTION, PATH, "path");				//$NON-NLS-1$
	private static final CmdLineOption	TEMPLATE_OPTION	= new CmdLineOption(TEMPLATE_DESCRIPTION, TEMPLATE, "template");	//$NON-NLS-1$

	public static void main(String[] args) {
		String name = AdjustFileNotice.class.getSimpleName();
		Attributes attributes = new Attributes();
		attributes.putValue(BundleInfo.BUNDLE_NAME, name);
		attributes.putValue(BundleInfo.BUNDLE_VERSION, VERSION);
		attributes.putValue(BundleInfo.BUNDLE_COPYRIGHT_OWNER, COPYRIGHT_OWNER);
		attributes.putValue(BundleInfo.BUNDLE_COPYRIGHT_YEARS, COPYRIGHT_YEARS);
		attributes.putValue(BundleInfo.BUNDLE_LICENSE, LICENSE);
		BundleInfo.setDefault(new BundleInfo(attributes, name));
		CmdLine cmdline = new CmdLine();
		cmdline.addOptions(PATH_OPTION, TEMPLATE_OPTION);
		cmdline.processArguments(args);
		if (!cmdline.isOptionUsed(PATH_OPTION)) {
			System.err.printf(PATH_MUST_BE_SPECIFIED, PATH_OPTION);
			System.exit(1);
		}
		if (!cmdline.getArguments().isEmpty()) {
			for (String one : cmdline.getArguments()) {
				System.err.print(UNEXPECTED_ARGUMENT);
				System.err.println(one);
			}
			System.exit(1);
		}
		int count = process(new File(cmdline.getOptionArgument(PATH_OPTION)), cmdline.isOptionUsed(TEMPLATE_OPTION) ? loadTemplate(cmdline.getOptionArgument(TEMPLATE_OPTION)) : loadDefaultTemplate());
		System.out.printf(RESULT, Integer.valueOf(count), count == 1 ? FILE : FILES);
	}

	public static final String loadTemplate(String path) {
		try (FileReader in = new FileReader(path)) {
			return loadTemplate(in);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	public static final String loadDefaultTemplate() { // TODO L10n String Candidate
		return loadTemplate(new StringReader("Copyright (c) 1998-$YEAR$ by Richard A. Wilkes. All rights reserved.\n\nThis Source Code Form is subject to the terms of the Mozilla Public License,\nversion 2.0. If a copy of the MPL was not distributed with this file, You\ncan obtain one at http://mozilla.org/MPL/2.0/.\n\nThis Source Code Form is \"Incompatible With Secondary Licenses\", as defined\nby the Mozilla Public License, version 2.0.")); //$NON-NLS-1$
	}

	public static final String loadTemplate(Reader reader) {
		String year = String.format("%tY", Calendar.getInstance()); //$NON-NLS-1$
		StringBuilder buffer = new StringBuilder();
		buffer.append("/*\n"); //$NON-NLS-1$
		try (BufferedReader in = new BufferedReader(reader)) {
			String line;
			while ((line = in.readLine()) != null) {
				line = line.replace("$YEAR$", year); //$NON-NLS-1$
				buffer.append(" *"); //$NON-NLS-1$
				if (!line.isEmpty()) {
					buffer.append(' ');
					buffer.append(line);
				}
				buffer.append('\n');
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}
		buffer.append(" */\n\n"); //$NON-NLS-1$
		return buffer.toString();
	}

	public static final int process(File file, String template) {
		if (file.isDirectory()) {
			int total = 0;
			for (File one : file.listFiles()) {
				total += process(one, template);
			}
			return total;
		} else if (file.getName().endsWith(".java")) { //$NON-NLS-1$
			StringBuilder buffer = new StringBuilder(1024 * 1024);
			boolean found = false;
			try (BufferedReader in = new BufferedReader(new FileReader(file))) {
				String line;
				int state = 0;
				while ((line = in.readLine()) != null) {
					switch (state) {
						case 0:	// Looking for package
							if (line.startsWith("package ")) { //$NON-NLS-1$
								state = 1;
								found = true;
								buffer.append(template);
								buffer.append(line);
								buffer.append('\n');
							}
							break;
						case 1: // Copy remainder of file
						default:
							buffer.append(line);
							buffer.append('\n');
							break;
					}
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
				System.exit(1);
			}
			if (found) {
				System.out.printf(ADJUSTED, file);
				try (PrintWriter out = new PrintWriter(file)) {
					out.print(buffer);
				} catch (IOException ioe) {
					ioe.printStackTrace();
					System.exit(1);
				}
				return 1;
			}
			System.out.printf(SKIPPED, file);
		}
		return 0;
	}
}
