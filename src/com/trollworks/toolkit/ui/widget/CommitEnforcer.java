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

package com.trollworks.toolkit.ui.widget;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Sends a 'focus lost' followed by a 'focus gained' event to the current keyboard focus, with the
 * intent that it will cause it to commit any changes it had pending.
 */
public class CommitEnforcer implements PrivilegedAction<Object> {
	private Component	mTarget;

	/**
	 * Sends a 'focus lost' followed by a 'focus gained' event to the current keyboard focus, with
	 * the intent that it will cause it to commit any changes it had pending.
	 */
	public static void forceFocusToAccept() {
		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		Component focus = focusManager.getPermanentFocusOwner();
		if (focus == null) {
			focus = focusManager.getFocusOwner();
		}
		if (focus != null) {
			CommitEnforcer action = new CommitEnforcer(focus);
			if (System.getSecurityManager() == null) {
				action.run();
			} else {
				AccessController.doPrivileged(action);
			}
		}
	}

	private CommitEnforcer(Component comp) {
		mTarget = comp;
	}

	@Override
	public Object run() {
		try {
			Class<?> cls = mTarget.getClass();
			Method method = null;
			while (method == null && cls != null) {
				try {
					method = cls.getDeclaredMethod("processFocusEvent", FocusEvent.class); //$NON-NLS-1$
				} catch (NoSuchMethodException nsm) {
					cls = cls.getSuperclass();
				}
			}
			if (method != null && cls != null) {
				method.setAccessible(true);
				method.invoke(mTarget, new FocusEvent(mTarget, FocusEvent.FOCUS_LOST, false, null));
				method.invoke(mTarget, new FocusEvent(mTarget, FocusEvent.FOCUS_GAINED, false, null));
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}
}
