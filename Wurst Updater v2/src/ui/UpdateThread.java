package ui;


import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JOptionPane;
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
				toChange = CheckChanges.getFilesToChange();
				toDelete = CheckChanges.getFilesToDelete();
				toAsk = CheckChanges.getFilesToAsk(toChange, toDelete);
			} catch (IOException e1) {
				//TODO
				e1.printStackTrace();
				throw new RuntimeException(e1);
			}
			for(FileEx f: toAsk){
				int antwort = JOptionPane.showConfirmDialog(null, "It seems like you have changed the file Wustpack" + f.file + " do you want to overwrite it?", "Meldung", JOptionPane.YES_NO_OPTION, 
						JOptionPane.INFORMATION_MESSAGE, null); 
		        if (!(antwort == JOptionPane.OK_OPTION)) { 
		        	toChange.remove(f);
		        }
			}
			Init.setProgress("Downloading...");
			Init.setMaxProgress(toChange.size());
			Init.setProgress(0);
			int c = 0;
			for(FileEx f: toChange){
				try {
					System.out.println("Downloading: " + "Wurstpack" + f.file);
					File temp = new File("Wurstpack" + f.file);
					new File(temp.getParent()).mkdirs();
					Download.downloadFile("Wurstpack" + f.file, temp);
					c++;
					Init.setProgress(c);
				} catch (IOException e1) {
					e1.printStackTrace();
					throw new RuntimeException(e1);
				}
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
				Init.setProgress("Downloading...");
				Init.setMaxProgress(toDl.size());
				Init.setProgress(0);
				int c = 0;
				try {
					for(FileEx f : toDl){
						File temp = new File("Wurstpack" + f.file);
						new File(temp.getParent()).mkdirs();
						Download.downloadFile("Wurstpack" + f.file, temp);
						c++;
						Init.setProgress(c);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
					throw new RuntimeException(e1);
				}
				Init.setProgress("Wurstpack is now uptodate!");
				Init.setMaxProgress(1);
				Init.setProgress(1);
	        }
		}
		return null;
	}

}
