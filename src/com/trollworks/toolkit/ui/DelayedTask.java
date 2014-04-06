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

package com.trollworks.toolkit.ui;

import com.trollworks.toolkit.utility.Debug;

import java.awt.EventQueue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A delayed task that will queue a {@link Runnable} onto the event queue thread for later
 * invocation.
 */
public class DelayedTask extends TimerTask {
	private static Timer	TIMER	= null;
	private Runnable		mRunnable;
	private boolean			mOnEventQueue;

	/**
	 * Creates a new {@link DelayedTask}. The {@link Runnable} passed in will be invoked on the
	 * event queue thread.
	 *
	 * @param runnable The {@link Runnable} to invoke.
	 */
	public DelayedTask(Runnable runnable) {
		this(runnable, true);
	}

	/**
	 * Creates a new {@link DelayedTask}.
	 *
	 * @param runnable The {@link Runnable} to invoke.
	 * @param onEventQueue Pass in <code>true</code> to cause the runnable task to be queued on the
	 *            primary event queue when it fires rather than being called directly.
	 */
	public DelayedTask(Runnable runnable, boolean onEventQueue) {
		mRunnable = runnable;
		mOnEventQueue = onEventQueue;
	}

	/** @return A common {@link Timer} object. */
	public static synchronized Timer getCommonTimer() {
		if (TIMER == null) {
			TIMER = new Timer(true);
		}
		return TIMER;
	}

	/** Puts the {@link Runnable} assigned to this task onto the event queue. */
	@Override
	public void run() {
		if (mRunnable != null) {
			if (mOnEventQueue) {
				EventQueue.invokeLater(mRunnable);
			} else {
				try {
					mRunnable.run();
				} catch (Exception exception) {
					assert false : Debug.toString(exception);
				}
			}
		}
	}

	/**
	 * Schedules the specified {@link Runnable} for execution on the primary event queue thread
	 * after the specified delay.
	 *
	 * @param runnable The runnable to schedule.
	 * @param delay The number of milliseconds to wait.
	 */
	public static void schedule(Runnable runnable, long delay) {
		schedule(runnable, delay, true);
	}

	/**
	 * Schedules the specified {@link Runnable} for execution after the specified delay.
	 *
	 * @param runnable The runnable to schedule.
	 * @param delay The number of milliseconds to wait.
	 * @param onEventQueue Pass in <code>true</code> to cause the runnable task to be queued on the
	 *            primary event queue when it fires rather than being called directly.
	 */
	public static void schedule(Runnable runnable, long delay, boolean onEventQueue) {
		getCommonTimer().schedule(new DelayedTask(runnable, onEventQueue), delay);
	}
}
