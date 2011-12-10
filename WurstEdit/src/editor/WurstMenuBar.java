package editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class WurstMenuBar extends JMenuBar {
	
	private WurstController controller;

	public WurstMenuBar(final WurstController wurstController) {
		this.controller = wurstController;
		JMenu fileMenu = new JMenu("File");
		JMenuItem file_load = new JMenuItem("Open folder");
		file_load.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.getFileController().showLoadFolder();
			}
		});
		fileMenu.add(file_load);
		add(fileMenu);
	}
}
