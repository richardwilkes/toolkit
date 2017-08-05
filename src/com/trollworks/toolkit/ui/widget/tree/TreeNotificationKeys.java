/*
 * Copyright (c) 1998-2017 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.widget.tree;

import com.trollworks.toolkit.utility.notification.Notifier;

/** Defines the various notification keys sent out by the {@link TreeRoot}. */
public class TreeNotificationKeys {
    /** The base prefix used on all of these notifications. */
    public static final String PREFIX         = "tree" + Notifier.SEPARATOR;           				//$NON-NLS-1$
    /** The prefix used on all notifications regarding {@link TreeRow}s. */
    public static final String ROW_PREFIX     = PREFIX + "row" + Notifier.SEPARATOR;   		//$NON-NLS-1$
    /** The prefix used on all notifications regarding {@link TreeColumn}s. */
    public static final String COLUMN_PREFIX  = PREFIX + "column" + Notifier.SEPARATOR;	//$NON-NLS-1$
    /**
     * The notification emitted when the header visibility is changed. Producer: The owning
     * {@link TreePanel}. Data: A {@link Boolean} indicating whether or not the header is visible.
     */
    public static final String HEADER         = PREFIX + "header";                     						//$NON-NLS-1$
    /**
     * The notification emitted when one or more child {@link TreeRow}s are added. Producer: The
     * {@link TreeContainerRow} that was modified. Data: The array of child {@link TreeRow}s that
     * were added.
     */
    public static final String ROW_ADDED      = ROW_PREFIX + "added";                  						//$NON-NLS-1$
    /**
     * The notification emitted when one or more child {@link TreeRow}s are removed. Producer: The
     * {@link TreeContainerRow} that was modified. Data: The array of child {@link TreeRow}s that
     * were removed.
     */
    public static final String ROW_REMOVED    = ROW_PREFIX + "removed";                					//$NON-NLS-1$
    /**
     * The notification emitted when one or more {@link TreeRow}'s height have been invalidated.
     * Producer: The owning {@link TreePanel}. Data: The array of {@link TreeRow}s whose height was
     * invalidated.
     */
    public static final String ROW_HEIGHT     = ROW_PREFIX + "height";                 					//$NON-NLS-1$
    /**
     * The notification emitted when the row divider visibility is changed. Producer: The owning
     * {@link TreePanel}. Data: A {@link Boolean} indicating whether or not the row divider is
     * visible.
     */
    public static final String ROW_DIVIDER    = ROW_PREFIX + "divider";                					//$NON-NLS-1$
    /**
     * The notification emitted when the selection changes. Producer: The {@link TreePanel} whose
     * selection is about to be modified. Data: The array of {@link TreeRow}s that were in the
     * previous selection.
     */
    public static final String ROW_SELECTION  = ROW_PREFIX + "selection";              					//$NON-NLS-1$
    /**
     * The notification emitted when one or more {@link TreeContainerRow}s have been opened.
     * Producer: The owning {@link TreePanel}. Data: The array of {@link TreeContainerRow}s that
     * were opened.
     */
    public static final String ROW_OPENED     = ROW_PREFIX + "opened";                 					//$NON-NLS-1$
    /**
     * The notification emitted when one or more {@link TreeContainerRow}s have been closed.
     * Producer: The owning {@link TreePanel}. Data: The array of {@link TreeContainerRow}s that
     * were closed.
     */
    public static final String ROW_CLOSED     = ROW_PREFIX + "closed";                 					//$NON-NLS-1$
    /**
     * The notification emitted when one or more {@link TreeColumn}s are added. Producer: The
     * {@link TreePanel} that was modified. Data: The array of {@link TreeColumn}s that were added.
     */
    public static final String COLUMN_ADDED   = COLUMN_PREFIX + "added";               					//$NON-NLS-1$
    /**
     * The notification emitted when one or more {@link TreeColumn}s are removed. Producer: The
     * {@link TreePanel} that was modified. Data: The array of {@link TreeColumn}s that were
     * removed.
     */
    public static final String COLUMN_REMOVED = COLUMN_PREFIX + "removed";             				//$NON-NLS-1$
    /**
     * The notification emitted when the column divider visibility is changed. Producer: The owning
     * {@link TreePanel}. Data: A {@link Boolean} indicating whether or not the column divider is
     * visible.
     */
    public static final String COLUMN_DIVIDER = COLUMN_PREFIX + "divider";             				//$NON-NLS-1$
}
