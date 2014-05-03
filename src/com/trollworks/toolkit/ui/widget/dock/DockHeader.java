package com.trollworks.toolkit.ui.widget.dock;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.border.SelectiveLineBorder;
import com.trollworks.toolkit.ui.image.Images;
import com.trollworks.toolkit.ui.image.ToolkitIcon;
import com.trollworks.toolkit.ui.image.ToolkitImage;
import com.trollworks.toolkit.ui.layout.PrecisionLayout;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/** The header for a {@link DockContainer}. */
public class DockHeader extends JPanel implements ContainerListener {
	@Localize("Maximize")
	private static String	MAXIMIZE_TOOLTIP;
	@Localize("Restore")
	private static String	RESTORE_TOOLTIP;

	static {
		Localization.initialize();
	}

	/**
	 * Creates a new {@link DockHeader} for the specified {@link Dockable}.
	 *
	 * @param dockable The {@link Dockable} to work with.
	 */
	public DockHeader(Dockable dockable) {
		super(new PrecisionLayout("margins:0 vAlign:middle")); //$NON-NLS-1$
		setOpaque(true);
		setBorder(new CompoundBorder(new SelectiveLineBorder(DockColors.SHADOW, 0, 0, 1, 0), new EmptyBorder(2, 4, 2, 4)));
		addContainerListener(this);
		add(new DockTab(dockable), "hGrab:yes"); //$NON-NLS-1$
		if (dockable instanceof DockMaximizable) {
			JButton maximizeButton = createButton(ToolkitImage.getDockMaximize(), Color.CYAN, MAXIMIZE_TOOLTIP);
			add(maximizeButton, "hAlign:end"); //$NON-NLS-1$
		}
	}

	/**
	 * Creates a button suitable for the {@link DockHeader}.
	 *
	 * @param icon The icon to use.
	 * @param pressed The color to use when colorizing the image for the rollover state.
	 * @param tooltip The tooltip to use.
	 * @return The new {@link JButton}.
	 */
	public static JButton createButton(ToolkitIcon icon, Color pressed, String tooltip) {
		JButton button = new JButton(icon);
		button.setToolTipText(tooltip);
		button.setPressedIcon(Images.createColorizedImage(icon, pressed));
		button.setRolloverIcon(Images.createColorizedImage(icon, Color.YELLOW));
		button.setDisabledIcon(Images.createDisabledImage(icon));
		button.setBorderPainted(false);
		UIUtilities.setOnlySize(button, new Dimension(16, 16));
		return button;
	}

	@Override
	public PrecisionLayout getLayout() {
		return (PrecisionLayout) super.getLayout();
	}

	@Override
	public void setLayout(LayoutManager mgr) {
		if (mgr instanceof PrecisionLayout) {
			super.setLayout(mgr);
		} else {
			throw new IllegalArgumentException("Must use a PrecisionLayout."); //$NON-NLS-1$
		}
	}

	@Override
	public void componentAdded(ContainerEvent event) {
		getLayout().mColumns = getComponentCount();
	}

	@Override
	public void componentRemoved(ContainerEvent event) {
		getLayout().mColumns = getComponentCount();
	}
}
