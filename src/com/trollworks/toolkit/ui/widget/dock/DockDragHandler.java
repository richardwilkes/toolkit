package com.trollworks.toolkit.ui.widget.dock;

import java.awt.event.MouseEvent;

/** Common base class for all {@link Dock} dragging behavior. */
public class DockDragHandler {
	private long	mDelay;
	private long	mStartedAt;
	private int		mStartX;
	private int		mStartY;
	private int		mPixelMovement;
	private boolean	mIsValid;

	/** Create a new {@link DockDragHandler} with a default pixel movement of 5 and delay of 250. */
	public DockDragHandler() {
		this(5, 250);
	}

	/**
	 * Create a new {@link DockDragHandler}.
	 *
	 * @param pixelMovement Mouse motion greater than this many pixels will start the drag.
	 * @param delay Mouse motion received after this many milliseconds will start the drag.
	 */
	public DockDragHandler(int pixelMovement, long delay) {
		mPixelMovement = pixelMovement;
		mDelay = delay;
	}

	/**
	 * Called in response to a mouse down in a draggable area.
	 *
	 * @param event The {@link MouseEvent} that is starting the drag.
	 */
	public final void start(MouseEvent event) {
		mStartedAt = event.getWhen();
		mStartX = event.getX();
		mStartY = event.getY();
		prepare(event);
	}

	/**
	 * Called by {@link #start(MouseEvent)} to allow sub-classes an opportunity to prepare for a
	 * drag.
	 *
	 * @param event The {@link MouseEvent} that is starting the drag.
	 */
	protected void prepare(MouseEvent event) {
		// Unused
	}

	/**
	 * Called in response to a mouse drag event after {@link #start(MouseEvent)} has been called.
	 *
	 * @param event The {@link MouseEvent} for the current drag location.
	 */
	public final void drag(MouseEvent event) {
		if (!mIsValid) {
			mIsValid = Math.abs(mStartX - event.getX()) > mPixelMovement || Math.abs(mStartY - event.getY()) > mPixelMovement || event.getWhen() - mStartedAt > mDelay;
		}
		if (mIsValid) {
			performDrag(event);
		}
	}

	/**
	 * Called by {@link #drag(MouseEvent)} to allow sub-classes an opportunity to respond to the
	 * drag.
	 *
	 * @param event The {@link MouseEvent} for the current drag location.
	 */
	protected void performDrag(MouseEvent event) {
		// Unused
	}

	/**
	 * Called in response to a mouse release event after {@link #start(MouseEvent)} has been called.
	 *
	 * @param event The {@link MouseEvent} for the last drag location.
	 */
	public final void finish(MouseEvent event) {
		if (mIsValid) {
			finishDrag(event);
		}
		cleanup();
	}

	/**
	 * Called by {@link #finish(MouseEvent)} to allow sub-classes an opportunity to finish the drag.
	 *
	 * @param event The {@link MouseEvent} for the last drag location.
	 */
	protected void finishDrag(MouseEvent event) {
		// Unused
	}

	/**
	 * Called by {@link #finish(MouseEvent)} to allow sub-classes an opportunity to clean up any
	 * lingering state after the drag.
	 */
	protected void cleanup() {
		// Unused
	}
}
