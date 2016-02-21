package com.trollworks.toolkit.ui.widget;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.ui.GraphicsUtilities;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.image.StdImage;
import com.trollworks.toolkit.utility.FileType;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class ImageWell extends JPanel implements DropTargetListener, MouseListener {
	@Localize("Select an image file")
	private static String	IMAGE_FILE_PROMPT;
	@Localize("Clear Image")
	private static String	CLEAR;

	static {
		Localization.initialize();
	}

	private Getter	mGetter;
	private Setter	mSetter;

	public ImageWell(String tooltip, Getter getter, Setter setter) {
		mGetter = getter;
		mSetter = setter;
		setToolTipText(tooltip);
		setBorder(new LineBorder(Color.BLACK));
		UIUtilities.setOnlySize(this, new Dimension(22, 22));
		setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, this));
		addMouseListener(this);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Rectangle bounds = GraphicsUtilities.getLocalInsetBounds(this);
		StdImage img = mGetter.getWellImage();
		if (img != null) {
			g.drawImage(img, bounds.x, bounds.y, bounds.width, bounds.height, this);
		} else {
			g.setColor(Color.WHITE);
			g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			g.setColor(Color.LIGHT_GRAY);
			int xs = bounds.width / 4;
			int ys = bounds.height / 4;
			int offset = 0;
			for (int y = bounds.y; y < bounds.y + bounds.height; y += ys) {
				for (int x = bounds.x + offset; x < bounds.x + bounds.width; x += xs * 2) {
					g.fillRect(x, y, xs, ys);
				}
				offset = offset == 0 ? xs : 0;
			}
		}
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		acceptOrRejectDrag(dtde);
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		acceptOrRejectDrag(dtde);
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		acceptOrRejectDrag(dtde);
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		// Unused
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		for (DataFlavor dataFlavor : dtde.getCurrentDataFlavors()) {
			if (dataFlavor.isFlavorJavaFileListType()) {
				try {
					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					@SuppressWarnings("unchecked")
					List<File> transferData = (List<File>) dtde.getTransferable().getTransferData(dataFlavor);
					for (File file : transferData) {
						if (loadImageFile(file)) {
							break;
						}
					}
					dtde.dropComplete(true);
					dtde.getDropTargetContext().getComponent().requestFocusInWindow();
				} catch (Exception exception) {
					Log.error(exception);
				}
				return;
			}
		}
		dtde.dropComplete(false);
	}

	private static void acceptOrRejectDrag(DropTargetDragEvent dtde) {
		if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
		} else {
			dtde.rejectDrag();
		}
	}

	private boolean loadImageFile(File file) {
		StdImage img = StdImage.loadImage(file, true);
		if (img != null) {
			mSetter.setWellImage(img);
			repaint();
			return true;
		}
		return false;
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		JPanel panel = new JPanel(new FlowLayout());
		JButton button = new JButton(CLEAR);
		button.addActionListener(action -> {
			mSetter.setWellImage(null);
			repaint();
			JFileChooser dialog = UIUtilities.getAncestorOfType(button, JFileChooser.class);
			if (dialog != null) {
				dialog.cancelSelection();
			}
		});
		panel.add(button);
		File file = StdFileDialog.showOpenDialog(null, IMAGE_FILE_PROMPT, panel, FileType.getImageFilter());
		if (file != null) {
			loadImageFile(file);
		}
	}

	@Override
	public void mousePressed(MouseEvent event) {
		// Unused
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		// Unused
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		// Unused
	}

	@Override
	public void mouseExited(MouseEvent event) {
		// Unused
	}

	public interface Getter {
		StdImage getWellImage();
	}

	public interface Setter {
		void setWellImage(StdImage img);
	}
}
