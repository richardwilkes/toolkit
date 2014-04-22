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
 *         <jars>
 *
 * Note that on Mac OS X, this hierarchy is expected to be embedded in the application
 * bundle, like this:
 *
 * <APP_NAME>.app/
 *     Contents/
 *         Info.plist
 *         MacOS/
 *             <this executable>
 *             support/
 *                 jre/
 *                     <JRE files>
 *                 jars/
 *                     <jars>
 *         PkgInfo
 *         Resources/
 *             <necessary resources>
 */

// Setup the target macros
#if defined(__linux__)
	#define TARGET_LINUX	1
	#define TARGET_MAC		0
	#define TARGET_WINDOWS	0
#elif defined(__APPLE__) && defined(__MACH__)
	#define TARGET_LINUX	0
	#define TARGET_MAC		1
	#define TARGET_WINDOWS	0
#elif defined(_WIN32) || defined(_WIN64)
	#define TARGET_LINUX	0
	#define TARGET_MAC		0
	#define TARGET_WINDOWS	1
#else
	#error Not a valid target platform
#endif

#include <stdlib.h>
#include <string.h>
#include <libgen.h>
#include <stdarg.h>
#include <stdio.h>
#include <limits.h>
#include <unistd.h>
#include <dirent.h>
#if TARGET_MAC
#include <mach-o/dyld.h>
#endif

#define TRUE 1
#define FALSE 0

// APP_NAME defines the application name as shown in the menu bar
#ifndef APP_NAME
#error APP_NAME must be defined!
#endif

#if TARGET_LINUX

// CATEGORIES defines the categories used in the Linux .desktop file
#ifndef CATEGORIES
#error CATEGORIES must be defined!
#endif

// KEYWORDS defines the keywords used in the Linux .desktop file
#ifndef KEYWORDS
#error KEYWORDS must be defined!
#endif

#endif // TARGET_LINUX

// MAX_RAM defines the maximum amount of RAM the VM will use for the app
#ifndef MAX_RAM
#define MAX_RAM 256M
#endif

#define xstr(s)	str(s)
#define str(s)	#s

static void fail(char *format, ...) {
	va_list args;
	va_start(args, format);
	vfprintf(stderr, format, args);
	va_end(args);
	fprintf(stderr, "\n");
	exit(1);
}

static char *concat(int count, ...) {
	int length = 1;
	va_list args;
	va_start(args, count);
	for (int i = 0; i < count; i++) {
		length += strlen(va_arg(args, char *));
	}
	va_end(args);
	char *buffer = (char *)malloc(length);
	*buffer = 0;
	va_start(args, count);
	for (int i = 0; i < count; i++) {
		strcat(buffer, va_arg(args, char *));
	}
	va_end(args);
	return buffer;
}

static char *getExecutablePath() {
	char buffer[PATH_MAX + 1];
#if TARGET_LINUX
	ssize_t size = readlink("/proc/self/exe", buffer, PATH_MAX);
	if (size == -1) {
		fail("Unable to obtain the executable's path: readlink failed");
	}
	buffer[size] = 0;
#elif TARGET_MAC
	uint32_t size = PATH_MAX;
	if (_NSGetExecutablePath(buffer, &size) != 0) {
		fail("Unable to obtain the executable's path: _NSGetExecutablePath failed");
	}
#endif
	char *result = realpath(buffer, NULL);
	if (!result) {
		fail("Unable to resole the executable's path: realpath failed: %s", buffer);
	}
	return result;
}

static char *getDir(char *path) {
	char *buffer = strdup(path);
	char *result = dirname(buffer);
	if (!result) {
		fail("Unable to obtain the directory name from: %s", path);
	}
	result = strdup(result);
	free(buffer);
	return result;
}

static char *getLeafName(char *path) {
	char *buffer = strdup(path);
	char *result = basename(buffer);
	if (!result) {
		fail("Unable to obtain the leaf name from: %s", path);
	}
	result = strdup(result);
	free(buffer);
	return result;
}

static int equals(char *left, char *right) {
	return !strcmp(left, right);
}

static int startsWith(char *prefix, char *buffer) {
	return !strncmp(prefix, buffer, strlen(prefix));
}

static int endsWith(char *suffix, char *buffer) {
	int suffixLen = strlen(suffix);
	int bufferLen = strlen(buffer);
	return bufferLen >= suffixLen && equals(suffix, buffer + bufferLen - suffixLen);
}

static char *getMainJar(char *jarDir, char *exeName) {
	DIR *dp = opendir(jarDir);
	if (dp) {
		char *buffer = concat(2, exeName, "-");
		int nameLen = strlen(exeName);
		struct dirent *dir;
		while ((dir = readdir(dp))) {
			int len = strlen(dir->d_name);
			if (len > nameLen + 4) {
				if (startsWith(exeName, dir->d_name) && endsWith(".jar", dir->d_name)) {
					closedir(dp);
					free(buffer);
					return concat(3, jarDir, "/", dir->d_name);
				}
			}
		}
		closedir(dp);
		free(buffer);
	}
	fail("Unable to locate main jar");
	return NULL;
}

#if TARGET_LINUX
static void createDesktopFile(char *exePath, char *exeName, char *supportDir) {
	char *desktop = concat(exePath, ".desktop");
	FILE *fp = fopen(desktop, "w");
	if (fp) {
		fprintf(fp, "[Desktop Entry]\n");
		fprintf(fp, "Version=1.0\n");
		fprintf(fp, "Type=Application\n");
		fprintf(fp, "Name=%s\n", xstr(APP_NAME));
		fprintf(fp, "Icon=%s/%s.png\n", supportDir, exeName);
		fprintf(fp, "Exec=%s %%F\n", exePath);
		fprintf(fp, "Categories=%s\n", xstr(CATEGORIES));
		fprintf(fp, "Keywords=%s\n", xstr(KEYWORDS));
		fclose(fp);
		chmod(desktop, S_IRUSR | S_IWUSR | S_IXUSR | S_IRGRP | S_IWGRP | S_IXGRP | S_IROTH | S_IXOTH);
		char *localDesktop = concat(4, getenv("HOME"), "/.local/share/applications/", exeName, ".desktop");
		unlink(localDesktop);
		symlink(desktop, localDesktop);
		free(localDesktop);
	}
	free(desktop);
}
#endif

typedef struct Link {
	struct Link *next;
	char *arg;
} Link, *LinkPtr;

static LinkPtr addLink(LinkPtr current, char *str) {
	current->arg = str;
	current->next = calloc(1, sizeof(Link));
	return current->next;
}

static int countLinks(LinkPtr head) {
	int count = 0;
	while (head) {
		if (head->arg) {
			count++;
		}
		head = head->next;
	}
	return count;
}

static int addToArgsArray(LinkPtr head, char **args, int startAt, int printArgs) {
	while (head) {
		if (head->arg) {
			args[startAt++] = head->arg;
			if (printArgs) {
				printf("%d: %s\n", startAt, head->arg);
			}
		}
		head = head->next;
	}
	return startAt;
}

int main(int argc, char **argv, char **envp) {
	// Setup our paths
	char *exePath = getExecutablePath();
	char *exeDir = getDir(exePath);
	char *exeName = getLeafName(exePath);
	char *supportDir = concat(2, exeDir, "/support");

	// Prepare the VM arguments
	int debugArgs = FALSE;
	LinkPtr jvmArgs = calloc(1, sizeof(Link));
	LinkPtr currentJvmArgs = jvmArgs;
	LinkPtr appArgs = calloc(1, sizeof(Link));
	LinkPtr currentAppArgs = appArgs;
	LinkPtr maxRAMLink = NULL;
	LinkPtr logLink = NULL;
	currentJvmArgs = addLink(currentJvmArgs, exePath);
#if TARGET_MAC
	currentJvmArgs = addLink(currentJvmArgs, strdup("-Xdock:name=" xstr(APP_NAME)));
#endif
	for (int i = 1; i < argc; i++) {
		if (startsWith("-J-Xmx", argv[i])) {
			char *maxRAM = strdup(argv[i] + 2);
			if (maxRAMLink) {
				free(maxRAMLink->arg);
				maxRAMLink->arg = maxRAM;
			} else {
				maxRAMLink = currentJvmArgs;
				currentJvmArgs = addLink(currentJvmArgs, maxRAM);
			}
#if TARGET_MAC
		} else if (startsWith("-J-Xdock:name=", argv[i])) {
			// Ignore... we've already set it and we don't want it overridden
		} else if (startsWith("-psn_", argv[i])) {
			// Ignore... this used to be emitted when a bundle was launched, although it
			// no longer appears to be in Mavericks (10.9)
#endif
		} else if (startsWith("-J", argv[i])) {
			currentJvmArgs = addLink(currentJvmArgs, strdup(argv[i] + 2));
		} else if (equals("-debug_args", argv[i])) {
			debugArgs = TRUE;
		} else if (startsWith("-debug_log=", argv[i])) {
			char *log = concat(2, "-Dcom.trollworks.log=", argv[i] + 11);
			if (logLink) {
				free(logLink->arg);
				logLink->arg = log;
			} else {
				logLink = currentJvmArgs;
				currentJvmArgs = addLink(currentJvmArgs, log);
			}
		} else {
			currentAppArgs = addLink(currentAppArgs, strdup(argv[i]));
		}
	}
	if (!maxRAMLink) {
		currentJvmArgs = addLink(currentJvmArgs, strdup("-Xmx" xstr(MAX_RAM)));
	}
	currentJvmArgs = addLink(currentJvmArgs, strdup("-jar"));
	currentJvmArgs = addLink(currentJvmArgs, getMainJar(concat(2, supportDir, "/jars"), exeName));

	// Build the VM arguments array
	char **args = (char **)calloc(countLinks(jvmArgs) + countLinks(appArgs) + 1, sizeof(char *));
	addToArgsArray(appArgs, args, addToArgsArray(jvmArgs, args, 0, debugArgs), debugArgs); 

#if TARGET_LINUX
	createDesktopFile(exePath, exeName, supportDir);
#endif

	if (execv(concat(2, supportDir, "/jre/bin/java"), args) == -1) {
		perror("Unable to exec the Java VM");
		return 1;
	}
	return 0;
}
