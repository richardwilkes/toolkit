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
 * <APP_NAME>.app/
 *     Contents/
 *         Info.plist
 *         MacOS/
 *             <this executable>
 *             support/
 *                 jre/
 *                     <JRE files>
 *                 jars/
 *                     <JAR_NAME>.jar
 *                     <any other necessary jar files>
 *         PkgInfo
 *         Resources/
 *             <necessary resources>
 */

#import <Cocoa/Cocoa.h>
#import <dlfcn.h>
#import <pthread.h>

// APP_NAME defines the application name as shown in the menu bar
#ifndef APP_NAME
#error APP_NAME must be defined!
#endif

// JAR_NAME defines the name of the double-clickable jar that will run the application
#ifndef JAR_NAME
#error JAR_NAME must be defined!
#endif

// MAX_RAM defines the maximum amount of RAM the VM will use for the app
#ifndef MAX_RAM
#define MAX_RAM 256M
#endif

#define xstr(s)	str(s)
#define str(s)	#s

// JNI declarations
typedef unsigned char   jboolean;
#if defined(__LP64__) && __LP64__
typedef int jint;
#else
typedef long jint;
#endif
typedef int (*JLI)(int argc, char **argv, int jargc, const char **jargv, int appclassc, const char **appclassv, const char *fullversion, const char *dotversion, const char *pname, const char *lname, jboolean javaargs, jboolean cpwildcard, jboolean javaw, jint ergo);

static int launch(int argc, char **oargv) {
	// Set the working directory to the user's home directory
	chdir([NSHomeDirectory() UTF8String]);

	// Setup some paths
	NSBundle *mainBundle = [NSBundle mainBundle];
	NSString *exePath = [[mainBundle bundlePath] stringByAppendingPathComponent:@"Contents/MacOS"];
	NSString *supportPath = [exePath stringByAppendingPathComponent:@"support"];
	NSString *jrePath = [supportPath stringByAppendingPathComponent:@"jre"];
	NSString *jarsPath = [supportPath stringByAppendingPathComponent:@"jars"];

	// Determine if this is the main thread. On the Mac, JLI will create a secondary thread
	// and call our main method again, so we don't manipulate the arguments until we aren't
	// on the main thread.
	bool isMainThread = pthread_main_np() == 1;

	// Create the real args
	char *argv[argc + (isMainThread ? 1 : 5)];
	int i = 0;
	argv[i++] = strdup(oargv[0]); // Executable
	if (!isMainThread) {
		bool hadMaxRAM = FALSE;
		bool hadDockName = FALSE;
		bool hadShowArgs = FALSE;

		// Copy the user arguments that start with -J (i.e. those meant for the JVM)
		for (int j = 1; j < argc; j++) {
			if (strncmp("-J", oargv[j], 2) == 0) {
				if (strncmp("-J-Xmx", oargv[j], 6) == 0) {
					hadMaxRAM = TRUE;
				} else if (strncmp("-J-Xdock:name=", oargv[j], 14) == 0) {
					hadDockName = TRUE;
				} else if (strcmp("-Jshow_args", oargv[j]) == 0) {
					hadShowArgs = TRUE;
					continue;
				}
				argv[i++] = strdup(oargv[j] + 2);
			}
		}

		// Add our JVM settings
		if (!hadMaxRAM) {
			argv[i++] = strdup("-Xmx" xstr(MAX_RAM));
		}
		if (!hadDockName) {
			argv[i++] = strdup([[NSString stringWithFormat:@"-Xdock:name=%s", xstr(APP_NAME)] UTF8String]);
		}

		// Add the jar specification
		argv[i++] = strdup("-jar");
		argv[i++] = strdup([[jarsPath stringByAppendingPathComponent:@xstr(JAR_NAME)] UTF8String]);

		// Copy remaining user arguments (note that the -psn_* argument has already been trimmed)
		for (int j = 1; j < argc; j++) {
			if (strncmp("-J", oargv[j], 2) != 0) {
				argv[i++] = strdup(oargv[j]);
			}
		}

		if (hadShowArgs) {
			for (int j = 0; j < i; j++) {
				printf("%d = %s\n", j, argv[j]);
			}
		}
	} else {
		// Copy all user arguments except the -psn_* argument added by the OS when launching a bundle
		for (int j = 1; j < argc; j++) {
			if (strncmp("-psn_", oargv[j], 5) != 0) {
				argv[i++] = strdup(oargv[j]);
			}
		}
	}
	argv[i] = NULL;

	// Load the jli library
	NSString *jliPath = [jrePath stringByAppendingPathComponent:@"lib/jli/libjli.dylib"];
	void *jliLib = dlopen([jliPath fileSystemRepresentation], RTLD_LAZY);
	if (jliLib == NULL) {
		NSLog(@"Unable to open %@", jliPath);
		return 1;
	}
	JLI jli = dlsym(jliLib, "JLI_Launch");
	if (jli == NULL) {
		NSLog(@"Unable to locate JLI_Launch");
		return 1;
	}

	// Launch
	return jli(i, argv, 0, NULL, 0, NULL, "", "", "java", "java", FALSE, FALSE, FALSE, 0);
}

int main(int argc, char **argv) {
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];

	int result;
	@try {
		result = launch(argc, argv);
	} @catch (NSException *exception) {
		NSLog(@"%@: %@", exception, [exception callStackSymbols]);
		result = 1;
	}

	[pool drain];

	return result;
}