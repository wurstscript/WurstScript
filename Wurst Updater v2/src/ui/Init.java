package ui;


import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import file.FileEx;

public class Init {
	
	static MainWindow mainFrame;
	
	public static void initNormal(boolean isChecked) {
		mainFrame = new MainWindow();
		mainFrame.setVisible(true);
		if(isChecked || checkFiles()) {
			mainFrame.btnUpdate.setEnabled(true);
			mainFrame.lblChecking.setText("Updates available!");
		} else {
			mainFrame.lblChecking.setText("Your Wurstpack is uptodate!");
		}
	}
	
	private static boolean checkFiles() {
		LinkedList<FileEx> toChange = null;
		LinkedList<FileEx> toDelete = null;
		LinkedList<FileEx> toAsk = null;
		try {
			File md5file = Download.downloadFile("wurstpack.md5", File.createTempFile("newChecksums", "md5"));
			toChange = CheckChanges.getFilesToChange(md5file);
			toDelete = CheckChanges.getFilesToDelete(md5file);
			toAsk = CheckChanges.getFilesToAsk(toChange, toDelete);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if( toChange.size()+toDelete.size()+toAsk.size() > 0 ) {
			return true;
		}
		return false;
	}
	
	public static void initSilent() {
		if( checkFiles() ) {
			initNormal(true);
		}else{
			return;
		}
	}
	
	public static void setMaxProgress(int max){
		mainFrame.progressBar.setMaximum(max);
	}
	
	public static void setProgress(int current){
		mainFrame.progressBar.setValue(current);
	}
	
	public static void setProgress(String state){
		mainFrame.lblChecking.setText(state);
	}


}
