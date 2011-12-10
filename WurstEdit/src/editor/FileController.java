package editor;

import java.io.File;

import javax.swing.JFileChooser;

public class FileController {

	private MainView mainView;

	public FileController(MainView mainView) {
		this.mainView = mainView;
	}
	
	public void showLoadFolder() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Choose working folder ...");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(mainView) == JFileChooser.APPROVE_OPTION) {
			File dir = chooser.getCurrentDirectory();
			openFolder(dir);
		} else {
			// no selection -> no action
		}
	}

	private void openFolder(File dir) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

}
