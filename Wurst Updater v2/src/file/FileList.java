package file;


import java.io.File;
import java.util.LinkedList;

import ui.Init;


public class FileList {

	public static LinkedList<FileEx> getCompleteFileList(File dir){
		LinkedList<FileEx> temp = new LinkedList<FileEx>();
		getCompleteFileList(dir, temp);
		Init.setProgress("Generating checksums...");
		Init.setMaxProgress(temp.size());
		int c = 0;
		for(FileEx f : temp){
			f.checksum = FileChecksum.get(new File("Wurstpack" + f.file));
			c++;
			Init.setProgress(c);
		}
		return temp;
	}
	
	private static void getCompleteFileList(File dir, LinkedList<FileEx> res){
		File[] temp = dir.listFiles();
		for(File f: temp){
			if (f.isDirectory()){
				getCompleteFileList(f, res);
			}else{
				res.add(new FileEx(f.getPath().replaceFirst("Wurstpack", "").replace("\\", "/"), ""));
			}
		}
	}
}
