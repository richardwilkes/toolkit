/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 *
 * Expected disk layout:
 *
 * <this executable>
 * support/
 *     jre/
 *         <JRE files>
 *     jars/
 *         <JAR_NAME>.jar
 *         <any other necessary jar files>
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <libgen.h>
#include <limits.h>
#include <sys/stat.h>

// APP_NAME defines the application name as shown in the menu bar
#ifndef APP_NAME
#error APP_NAME must be defined!
#endif

// JAR_NAME defines the name of the double-clickable jar that will run the application
#ifndef JAR_NAME
#error JAR_NAME must be defined!
#endif

// CATEGORIES defines the categories used in the Linux .desktop file
#ifndef CATEGORIES
#error CATEGORIES must be defined!
#endif

// KEYWORDS defines the keywords used in the Linux .desktop file
#ifndef KEYWORDS
#error KEYWORDS must be defined!
#endif

// MAX_RAM defines the maximum amount of RAM the VM will use for the app
#ifndef MAX_RAM
#define MAX_RAM 256M
#endif

#define TRUE 1
#define FALSE 0

#define xstr(s)	str(s)
#define str(s)	#s

static char *concat(char *left, char *right) {
	char *buffer = (char *)malloc(strlen(left) + strlen(right) + 1);
	strcpy(buffer, left);
	strcat(buffer, right);
	return buffer;
}

int main(int argc, char **argv) {
	// Determine the executable path
	char buffer[PATH_MAX]; 
	if (readlink("/proc/self/exe", buffer, PATH_MAX) != -1) {

		// Setup our paths
		char *full = strdup(buffer);
		char *base = strdup(dirname(buffer));
		strcpy(buffer, full);
		char *exeName = strdup(basename(buffer));
		char *jrePath = concat(base, "/support/jre");
		char *jarsPath = concat(base, "/support/jars");

		// Prepare the VM arguments
		char *jvmArgs[argc + 5];
		int i = 0;
		jvmArgs[i++] = full;	// Executable
		int hadMaxRAM = FALSE;
		int hadShowArgs = FALSE;

		// Copy the user arguments that start with a -J (i.e. those meant for the JVM)
		for (int j = 1; j < argc; j++) {
			if (strncmp("-J", argv[j], 2) == 0) {
				if (strncmp("-J-Xmx", argv[j], 6) == 0) {
					hadMaxRAM = TRUE;
				} else if (strcmp("-Jshow_args", argv[j]) == 0) {
					hadShowArgs = TRUE;
					continue;
				}
				jvmArgs[i++] = strdup(argv[j] + 2);
			}
		}

		// Add our JVM settings
		if (!hadMaxRAM) {
			jvmArgs[i++] = strdup("-Xmx" xstr(MAX_RAM));
		}
		
		// Add the jar specification
		jvmArgs[i++] = strdup("-jar");
		jvmArgs[i++] = concat(jarsPath, "/" xstr(JAR_NAME));

		// Copy the remaining user arguments
		for (int j = 1; j < argc; j++) {
			if (strncmp("-J", argv[j], 2) != 0) {
				jvmArgs[i++] = strdup(argv[j]);
			}
		}
		jvmArgs[i] = NULL;

		if (hadShowArgs) {
			for (int j = 0; j < i; j++) {
				printf("%d = %s\n", j, jvmArgs[j]);
			}
		}

		// (Re)create our .desktop file
		char *desktopFile = concat(full, ".desktop");
		FILE *fp = fopen(desktopFile, "w");
		if (fp) {
			fprintf(fp, "[Desktop Entry]\n");
			fprintf(fp, "Version=1.0\n");
			fprintf(fp, "Type=Application\n");
			fprintf(fp, "Name=%s\n", xstr(APP_NAME));
			fprintf(fp, "Icon=%s/support/%s.png\n", base, exeName);
			fprintf(fp, "Exec=%s %%F\n", full);
			fprintf(fp, "Categories=%s\n", xstr(CATEGORIES));
			fprintf(fp, "Keywords=%s\n", xstr(KEYWORDS));
			fclose(fp);
			chmod(desktopFile, S_IRUSR | S_IWUSR | S_IXUSR | S_IRGRP | S_IWGRP | S_IXGRP | S_IROTH | S_IXOTH);
			sprintf(buffer, "%s/.local/share/applications/%s.desktop", getenv("HOME"), exeName);
			unlink(buffer);
			symlink(desktopFile, buffer);
		}

		// Launch
		return execv(concat(jrePath, "/bin/java"), jvmArgs);
	}
	return 1;
}