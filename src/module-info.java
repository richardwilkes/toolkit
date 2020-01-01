/*
 * Copyright (c) 1998-2020 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

module com.trollworks.toolkit {
    requires java.management;
    requires java.prefs;
    requires gnu.trove;

    requires transitive java.datatransfer;
    requires transitive java.desktop;
    requires transitive java.xml;

    exports com.trollworks.toolkit.annotation;
    exports com.trollworks.toolkit.collections;
    exports com.trollworks.toolkit.expression;
    exports com.trollworks.toolkit.expression.function;
    exports com.trollworks.toolkit.expression.operator;
    exports com.trollworks.toolkit.io;
    exports com.trollworks.toolkit.io.conduit;
    exports com.trollworks.toolkit.io.json;
    exports com.trollworks.toolkit.io.server;
    exports com.trollworks.toolkit.io.server.http;
    exports com.trollworks.toolkit.io.server.websocket;
    exports com.trollworks.toolkit.io.xml;
    exports com.trollworks.toolkit.io.xml.helper;
    exports com.trollworks.toolkit.ui;
    exports com.trollworks.toolkit.ui.border;
    exports com.trollworks.toolkit.ui.image;
    exports com.trollworks.toolkit.ui.layout;
    exports com.trollworks.toolkit.ui.menu;
    exports com.trollworks.toolkit.ui.menu.edit;
    exports com.trollworks.toolkit.ui.menu.file;
    exports com.trollworks.toolkit.ui.menu.help;
    exports com.trollworks.toolkit.ui.menu.window;
    exports com.trollworks.toolkit.ui.preferences;
    exports com.trollworks.toolkit.ui.print;
    exports com.trollworks.toolkit.ui.scale;
    exports com.trollworks.toolkit.ui.widget;
    exports com.trollworks.toolkit.ui.widget.dock;
    exports com.trollworks.toolkit.ui.widget.outline;
    exports com.trollworks.toolkit.ui.widget.search;
    exports com.trollworks.toolkit.ui.widget.tree;
    exports com.trollworks.toolkit.ui.widget.tree.test;
    exports com.trollworks.toolkit.utility;
    exports com.trollworks.toolkit.utility.cmdline;
    exports com.trollworks.toolkit.utility.introspection;
    exports com.trollworks.toolkit.utility.noise;
    exports com.trollworks.toolkit.utility.notification;
    exports com.trollworks.toolkit.utility.task;
    exports com.trollworks.toolkit.utility.text;
    exports com.trollworks.toolkit.utility.undo;
    exports com.trollworks.toolkit.utility.units;
    exports com.trollworks.toolkit.workarounds;
}
