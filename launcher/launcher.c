/*
 * Copyright (c) 1998-2015 by Richard A. Wilkes. All rights reserved.
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
 *
 * Note that two different approaches are being taken here, depending on platform.
 * The approach taken for Linux and Windows is to just exec() the JRE's java/javaw
 * executable with the appropriate command line. Unfortunately, this approach breaks
 * the Mac's ability to respond to double-clicking on our documents, so on the Mac
 * I use JLI. I don't use JLI on the other platforms since it has problems finding
 * its dependent shared libraries on those platforms. (sigh)
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

#if TARGET_WINDOWS
#define _CRT_SECURE_NO_WARNINGS
#define PATH_SEP "\\"
#define JAVA_EXE "javaw.exe"
#else
#define PATH_SEP "/"
#define JAVA_EXE "java"
#endif

#if TARGET_MAC
#import <dlfcn.h>
#import <pthread.h>

// JLI signature
typedef int (*JLI)(int argc, char **argv, int jargc, const char **jargv, int appclassc,
				   const char **appclassv, const char *fullversion, const char *dotversion,
				   const char *pname, const char *lname, unsigned char javaargs,
				   unsigned char cpwildcard, unsigned char javaw,
#if defined(__LP64__) && __LP64__
				   int
#else
				   long
#endif
				   ergo);
#endif // TARGET_MAC

#if TARGET_WINDOWS
#include <windows.h>
#include <process.h>
#endif
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <stdio.h>
#include <limits.h>
#if TARGET_MAC || TARGET_LINUX
#include <libgen.h>
#include <unistd.h>
#include <dirent.h>
#endif
#if TARGET_LINUX
#include <sys/stat.h>
#endif
#if TARGET_MAC
#include <mach-o/dyld.h>
#endif

#if TARGET_WINDOWS
#define PATH_MAX MAX_PATH
#define strdup _strdup
#define execv _execv
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
#if TARGET_WINDOWS
	GetModuleFileName(NULL, buffer, PATH_MAX);
	return strdup(buffer);
#else
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
#endif // !TARGET_WINDOWS
}

#if TARGET_WINDOWS
static char *dirname(char *path) {
	int i = strlen(path) - 1;
	while (i > 0 && path[i] != '\\' && path[i] != ':') {
		i--;
	}
	if (path[i] == '\\' || path[i] == ':') {
		path[i] = 0;
	}
	return path;
}

static char *basename(char *path) {
	int i = strlen(path) - 1;
	while (i > 0 && path[i] != '\\' && path[i] != ':') {
		i--;
	}
	if (path[i] == '\\' || path[i] == ':') {
		i++;
	}
	return &path[i];
}
#endif

static char *getParentDir(char *path) {
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

#if TARGET_WINDOWS
static char *getMainJar(char *jarDir, char *exeName) {
	WIN32_FIND_DATA ffd;
	char *path = concat(3, jarDir, PATH_SEP, "*.jar");
	HANDLE dp = FindFirstFile(path, &ffd);
	if (dp != INVALID_HANDLE_VALUE) {
		if (endsWith(".exe", exeName)) {
			exeName = strdup(exeName);
			exeName[strlen(exeName) - 4] = 0;
		}
		char *buffer = concat(2, exeName, "-");
		do {
			if (startsWith(buffer, ffd.cFileName) && endsWith(".jar", ffd.cFileName)) {
				char *jar = concat(3, jarDir, PATH_SEP, ffd.cFileName);
				FindClose(dp);
				free(buffer);
				free(path);
				return jar;
			}
		} while (FindNextFile(dp, &ffd) != 0);
		FindClose(dp);
		free(buffer);
		free(path);
	}
	fail("Unable to locate main jar");
	return NULL;
}
#else
static char *getMainJar(char *jarDir, char *exeName) {
	DIR *dp = opendir(jarDir);
	if (dp) {
		char *buffer = concat(2, exeName, "-");
		struct dirent *dir;
		while ((dir = readdir(dp))) {
			if (startsWith(buffer, dir->d_name) && endsWith(".jar", dir->d_name)) {
				char *jar = concat(3, jarDir, PATH_SEP, dir->d_name);
				closedir(dp);
				free(buffer);
				return jar;
			}
		}
		closedir(dp);
		free(buffer);
	}
	fail("Unable to locate main jar");
	return NULL;
}
#endif

#if TARGET_LINUX
static void createDesktopFile(char *exePath, char *exeName, char *supportDir) {
	char *desktop = concat(2, exePath, ".desktop");
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
		if (symlink(desktop, localDesktop) == -1) {
			perror("Unable to create a symlink to the .desktop file");
		}
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
			if (printArgs) {
				printf("%d: %s\n", startAt + 1, head->arg);
			}
#if TARGET_WINDOWS
			// Windows seems to concatenate the command line args together with spaces
			// and then re-parse it in the new process... which, of course, completely
			// defeats the purpose of separating them into separate arguments in the
			// first place. We'll attempt to escape them here.
			char *arg = (char *)malloc(strlen(head->arg) * 2);
			int i = 0;
			int j = 0;
			arg[j++] = '"';
			while (head->arg[i]) {
				char ch = head->arg[i++];
				if (ch == '"') {
					arg[j++] = '"';
					arg[j++] = '"';
				}
				arg[j++] = ch;
			}
			arg[j++] = '"';
			arg[j] = 0;
			args[startAt] = arg;
			startAt++;
#else
			args[startAt++] = head->arg;
#endif
		}
		head = head->next;
	}
	return startAt;
}

#if TARGET_MAC
static int launchViaJLI(char *jreDir, int argc, char **argv) {
	// Load the jli library
	char *jliDir = concat(2, jreDir, "/lib/jli/libjli.dylib");
	void *jliLib = dlopen(jliDir, RTLD_LAZY);
	if (!jliLib) {
		fail("Unable to open %s", jliDir);
	}
	JLI jli = dlsym(jliLib, "JLI_Launch");
	if (!jli) {
		fail("Unable to locate JLI_Launch");
	}

	// Launch
	return jli(argc, argv, 0, NULL, 0, NULL, "", "", "java", "java", FALSE, FALSE, FALSE, 0);
}
#endif

#if TARGET_WINDOWS
int WINAPI WinMain(HINSTANCE inst,HINSTANCE prevInst,LPSTR cmdLine,int cmdShow) {
	int argc = __argc;
	char **argv = __argv;
#else
int main(int argc, char **argv) {
#endif
	// Setup our paths
	char *exePath = getExecutablePath();
	char *exeDir = getParentDir(exePath);
	char *exeName = getLeafName(exePath);
	char *supportDir = concat(3, exeDir, PATH_SEP, "support");
	char *jreDir = concat(3, supportDir, PATH_SEP, "jre");

#if TARGET_MAC
	// Determine if this is the main thread. On the Mac, JLI will create a secondary thread
	// and call our main method again, so we don't manipulate the arguments until we aren't
	// on the main thread.
	if (pthread_main_np() != 1) {
		// Not on main thread, just pass the args through again.
		return launchViaJLI(jreDir, argc, argv);
	}
#endif

	// Prepare the VM arguments
	int debugArgs = FALSE;
	LinkPtr jvmArgs = calloc(1, sizeof(Link));
	LinkPtr currentJvmArgs = jvmArgs;
	LinkPtr appArgs = calloc(1, sizeof(Link));
	LinkPtr currentAppArgs = appArgs;
	LinkPtr maxRAMLink = NULL;
	LinkPtr logLink = NULL;
#if !TARGET_WINDOWS
	currentJvmArgs = addLink(currentJvmArgs, exePath);
#endif
#if TARGET_MAC
	currentJvmArgs = addLink(currentJvmArgs, strdup("-Xdock:name=" xstr(APP_NAME)));
	currentJvmArgs = addLink(currentJvmArgs, concat(3, "-Xdock:icon=", getParentDir(exeDir), "/Resources/app.icns"));
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
		} else if (startsWith("-J-Xdock:icon=", argv[i])) {
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
	currentJvmArgs = addLink(currentJvmArgs, getMainJar(concat(3, supportDir, PATH_SEP, "jars"), exeName));

	// Build the VM arguments array
	argc = countLinks(jvmArgs) + countLinks(appArgs);
	argv = (char **)calloc(argc + 1, sizeof(char *));
	addToArgsArray(appArgs, argv, addToArgsArray(jvmArgs, argv, 0, debugArgs), debugArgs); 

#if TARGET_LINUX
	createDesktopFile(exePath, exeName, supportDir);
#endif

#if TARGET_MAC
	return launchViaJLI(jreDir, argc, argv);
#else
	if (execv(concat(5, jreDir, PATH_SEP, "bin", PATH_SEP, JAVA_EXE), argv) == -1) {
		perror("Unable to exec the Java VM");
		return 1;
	}
#endif
	return 0;
}
