package ui;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;

import javax.swing.SwingWorker;


import file.FileChecksum;
import file.FileEx;

public class UpdateButtonListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		new UpdateThread().execute();
	}

}
