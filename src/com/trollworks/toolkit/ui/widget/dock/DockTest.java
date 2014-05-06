package com.trollworks.toolkit.ui.widget.dock;

import com.trollworks.toolkit.ui.Colors;
import com.trollworks.toolkit.ui.border.SelectiveLineBorder;
import com.trollworks.toolkit.ui.image.ToolkitImage;
import com.trollworks.toolkit.ui.layout.PrecisionLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;

@SuppressWarnings("nls")
public class DockTest implements Dockable, DockCloseable, DockMaximizable {
	public static void main(String[] args) {
		JFrame frame = new JFrame("Dock Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Dock dock = new Dock();
		DockTest first = new DockTest("First", Color.WHITE);
		DockTest second = new DockTest("Second", Color.RED);
		DockTest third = new DockTest("Third", UIManager.getColor("List.selectionBackground"));
		DockTest fourth = new DockTest("Fourth", Colors.adjustSaturation(UIManager.getColor("List.selectionBackground"), -0.5f));
		DockTest fifth = new DockTest("Fifth", Color.BLUE);

		dock.dock(first, DockLocation.WEST);
		dock.dock(second, DockLocation.EAST);
		dock.dock(third, second, DockLocation.SOUTH);
		dock.dock(fourth, second, DockLocation.WEST);
		dock.dock(fifth, first, DockLocation.EAST);

		JToolBar toolbar = new JToolBar();
		toolbar.setRollover(true);
		toolbar.add(new AbstractAction("Dump", ToolkitImage.getMoreIcon()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				dump(dock);
			}
		});
		toolbar.setBorder(new CompoundBorder(new SelectiveLineBorder(DockColors.SHADOW, 0, 0, 1, 0), toolbar.getBorder()));

		Container content = frame.getContentPane();
		content.setLayout(new BorderLayout());
		content.add(toolbar, BorderLayout.NORTH);
		content.add(dock, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		first.getContent().transferFocus();
	}

	private String	mTitle;
	private JPanel	mContent;

	DockTest(String title, Color color) {
		mTitle = title;
		mContent = new JPanel(new PrecisionLayout());
		JLabel label = new JLabel(title + " Content", SwingConstants.CENTER);
		label.setForeground(Colors.isBright(color) ? Color.BLACK : Color.WHITE);
		mContent.add(label);
		JTextField field = new JTextField("Some text");
		field.setName(title);
		mContent.add(field);
		mContent.setBackground(color);
		mContent.setOpaque(true);
	}

	@Override
	public String getDescriptor() {
		return null;
	}

	@Override
	public Icon getTitleIcon() {
		return new ImageIcon(ToolkitImage.getFileIcon());
	}

	@Override
	public String getTitle() {
		return mTitle;
	}

	@Override
	public Component getContent() {
		return mContent;
	}

	@Override
	public boolean attemptClose() {
		return true;
	}

	public static void dump(Dock dock) {
		System.out.println("=====");
		System.out.println(dock);
		System.out.println("=====");
	}
}
