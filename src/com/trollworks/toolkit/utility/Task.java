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

package com.trollworks.toolkit.utility;

import com.trollworks.toolkit.io.Log;

import java.awt.EventQueue;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Task implements Runnable {
	private static final ScheduledExecutorService	EXECUTOR	= Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);
	private static final HashSet<Object>			PENDING		= new HashSet<>();
	private static final AtomicLong					NEXT_KEY	= new AtomicLong();
	private Runnable								mTask;
	private Object									mKey;
	private boolean									mOnUIThread;
	private boolean									mWasCancelled;
	private boolean									mWasExecuted;

	/**
	 * Synchronously execute a {@link Runnable} on the UI thread.
	 *
	 * @param runnable The {@link Runnable} to execute.
	 */
	public static void blockingCallOnUIThread(Runnable runnable) {
		try {
			EventQueue.invokeAndWait(runnable);
		} catch (Throwable throwable) {
			Log.error(throwable);
		}
	}

	/**
	 * Execute a {@link Runnable} on the UI thread.
	 *
	 * @param runnable The {@link Runnable} to execute.
	 */
	public static void callOnUIThread(Runnable runnable) {
		try {
			EventQueue.invokeLater(runnable);
		} catch (Throwable throwable) {
			Log.error(throwable);
		}
	}

	/**
	 * Execute a {@link Runnable} on a background thread.
	 *
	 * @param runnable The {@link Runnable} to execute.
	 * @return The {@link Task}.
	 */
	public static Task callOnBackgroundThread(Runnable runnable) {
		return schedule(runnable, false, 0, TimeUnit.MILLISECONDS, null);
	}

	/**
	 * Execute a {@link Runnable} on the UI thread.
	 *
	 * @param runnable The {@link Runnable} to execute.
	 * @param delay The number of units to delay before execution begins.
	 * @param delayUnits The units the delay parameter has been specified in.
	 * @param key If this is not <code>null</code>, then the task will only be scheduled to run if
	 *            there isn't one with the same key already scheduled.
	 * @return The {@link Task}.
	 */
	public static final Task scheduleOnUIThread(Runnable runnable, long delay, TimeUnit delayUnits, Object key) {
		return schedule(runnable, true, delay, delayUnits, key);
	}

	/**
	 * Execute a {@link Runnable} on a background thread.
	 *
	 * @param runnable The {@link Runnable} to execute.
	 * @param delay The number of units to delay before execution begins.
	 * @param delayUnits The units the delay parameter has been specified in.
	 * @param key If this is not <code>null</code>, then the task will only be scheduled to run if
	 *            there isn't one with the same key already scheduled.
	 * @return The {@link Task}.
	 */
	public static Task scheduleOnBackgroundThread(Runnable runnable, long delay, TimeUnit delayUnits, Object key) {
		return schedule(runnable, false, delay, delayUnits, key);
	}

	/**
	 * Schedule a {@link Runnable} for execution.
	 *
	 * @param runnable The {@link Runnable} to execute.
	 * @param onUIThread Pass in <code>true</code> to execute on the UI thread.
	 * @param delay The number of units to delay before execution begins.
	 * @param delayUnits The units the delay parameter has been specified in.
	 * @param key If this is not <code>null</code>, then the task will only be scheduled to run if
	 *            there isn't one with the same key already scheduled.
	 * @return The {@link Task}.
	 */
	public static Task schedule(Runnable runnable, boolean onUIThread, long delay, TimeUnit delayUnits, Object key) {
		Task task = new Task(runnable, onUIThread, key);
		if (key != null) {
			synchronized (PENDING) {
				if (!PENDING.add(key)) {
					task.mWasCancelled = true;
					return task;
				}
			}
		}
		EXECUTOR.schedule(task, delay, delayUnits);
		return task;
	}

	/**
	 * @return A unique key for use with task scheduling. The key is only guaranteed to be unique
	 *         with respect to other keys obtained through this method within the same instantiation
	 *         of the JVM.
	 */
	public static Object getNextUniqueKey() {
		return Long.valueOf(NEXT_KEY.getAndIncrement());
	}

	private Task(Runnable runnable, boolean onUIThread, Object key) {
		mTask = runnable;
		mOnUIThread = onUIThread;
		mKey = key;
	}

	@Override
	public void run() {
		if (mOnUIThread && !EventQueue.isDispatchThread()) {
			EventQueue.invokeLater(this);
		} else {
			synchronized (this) {
				if (mWasCancelled) {
					return;
				}
				mWasExecuted = true;
			}
			try {
				if (mKey != null) {
					synchronized (PENDING) {
						PENDING.remove(mKey);
					}
				}
				mTask.run();
			} catch (Throwable throwable) {
				Log.error(throwable);
			}
		}
	}

	/** @return <code>true</code> if the task was cancelled and will not be executed. */
	public synchronized boolean wasCancelled() {
		return mWasCancelled;
	}

	/** @return <code>true</code> if the task was successfully cancelled and will not be executed. */
	public synchronized boolean cancel() {
		if (mWasExecuted) {
			return false;
		}
		mWasCancelled = true;
		return true;
	}
}
