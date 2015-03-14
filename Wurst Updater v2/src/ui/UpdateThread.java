package ui;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import file.FileChecksum;
import file.FileEx;

public class UpdateThread extends SwingWorker<Void, Void> {

	@Override
	protected Void doInBackground() throws Exception {
		if (!Download.isServerOnline()){
			JOptionPane.showMessageDialog(null, "The update server is currently ofline, please try again later!");
			return null;
		}
		if (CheckChanges.hasPack()){
			LinkedList<FileEx> toChange = null;
			LinkedList<FileEx> toAsk = null;
			LinkedList<FileEx> toDelete = null;
			try {
				File md5file = Download.downloadFile("wurstpack.md5", File.createTempFile("newChecksums", "md5"));
				toChange = CheckChanges.getFilesToChange(md5file);
				toDelete = CheckChanges.getFilesToDelete(md5file);
				toAsk = CheckChanges.getFilesToAsk(toChange, toDelete);
			} catch (IOException e1) {
				SwingUtilities.invokeAndWait(new Runnable() {
					
					@Override
					public void run() {
						JOptionPane.showMessageDialog(null, "Could not get change file from server: \n\n" + e1.getMessage());
					}
				});
				
				return null;
			}
			for(FileEx f: toAsk){
				int antwort = JOptionPane.showConfirmDialog(null, "It seems like you have changed the file Wustpack" + f.file + " do you want to overwrite it?", "Meldung", JOptionPane.YES_NO_OPTION, 
						JOptionPane.INFORMATION_MESSAGE, null); 
		        if (!(antwort == JOptionPane.OK_OPTION)) { 
		        	toChange.remove(f);
		        }
			}
			boolean success = downloadChangedFiles(toChange);
			if (!success) {
				Init.setProgress("There were problems updating.");
				Init.setMaxProgress(1);
				Init.setProgress(1);
				return null;
			}
			for(FileEx f: toDelete){
				File temp = new File("Wurstpack" + f.file);
				if (temp.exists()){
					temp.delete();
				}
			}
			Download.downloadFile("wurstpack.md5", new File("Wurstpack/wurstpack.md5"));
			Init.setProgress("Wurstpack is now uptodate!");
			Init.setMaxProgress(1);
			Init.setProgress(1);
		}else{
	        int antwort = JOptionPane.showConfirmDialog(null, "There is no Wurstpack, so a new folder containing the Wurstpack will be created next to the updater", "Meldung", JOptionPane.YES_NO_OPTION, 
	                JOptionPane.INFORMATION_MESSAGE, null); 
	        if (antwort == JOptionPane.OK_OPTION) { 
	        	new File("Wurstpack").mkdir();
				LinkedList<FileEx> toDl =  FileChecksum.readChecksums(Download.downloadFile("wurstpack.md5", File.createTempFile("newChecksums", "md5")));
				boolean success = downloadChangedFiles(toDl);
				if (!success) {
					Init.setProgress("There were problems updating.");
					Init.setMaxProgress(1);
					Init.setProgress(1);
					return null;
				}
				
				Init.setProgress("Wurstpack is now uptodate!");
				Init.setMaxProgress(1);
				Init.setProgress(1);
	        }
		}
		return null;
	}

	private boolean downloadChangedFiles(LinkedList<FileEx> toChange) throws InterruptedException,
			InvocationTargetException {
		Init.setProgress("Downloading...");
		Init.setMaxProgress(toChange.size());
		Init.setProgress(0);
		int c = 0;
		boolean result = true;
		for(FileEx f: toChange){
			try {
				System.out.println("Downloading: " + "Wurstpack" + f.file);
				File temp = new File("Wurstpack" + f.file);
				new File(temp.getParent()).mkdirs();
				Download.downloadFile("Wurstpack" + f.file, temp);
				c++;
				Init.setProgress(c);
			} catch (IOException e1) {
				result = false;
				e1.printStackTrace();
				int[] temp = {0};
				SwingUtilities.invokeAndWait(new Runnable() {
					
					@Override
					public void run() {
						temp[0] = JOptionPane.showConfirmDialog(null, "The file " + f.file + " Could not be downloaded.\n\n" + e1.getMessage(), "Error", JOptionPane.OK_CANCEL_OPTION);
					}
				});
				if (temp[0] == JOptionPane.CANCEL_OPTION) {
					return false;
				}
			}
		}
		return result;
	}

}
