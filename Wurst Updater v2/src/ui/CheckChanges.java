package ui;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

import file.FileChecksum;
import file.FileEx;
import file.FileList;

public class CheckChanges {
	
	private static final File wurstPackFolder = new File("Wurstpack");

	public static boolean hasPack(){
		return wurstPackFolder.exists();
	}
	
	public static LinkedList<FileEx> getFilesToAsk(LinkedList<FileEx> toChange, LinkedList<FileEx> toDelete) throws IOException{
		File checksums = new File(wurstPackFolder, "wurstpack.md5");
		LinkedList<FileEx> toAsk = new LinkedList<FileEx>();
		if (!checksums.exists()){
			return toAsk;
		}
		LinkedList<FileEx> currentSums = FileList.getCompleteFileList(wurstPackFolder);
		Map<String, String> currentSumMap = FileEx.getFileMap(currentSums);
		LinkedList<FileEx> currentFileSums = FileChecksum.readChecksums(checksums);
		Map<String, String> currentFileSumMap = FileEx.getFileMap(currentFileSums);
		for(FileEx f: toChange){
			if((currentSumMap.get(f.file)) != null && !(currentSumMap.get(f.file).equals(currentFileSumMap.get(f.file)))){
				toAsk.add(f);
			}
		}
		for(FileEx f: toDelete){
			if((currentSumMap.get(f.file)) != null && !(currentSumMap.get(f.file).equals(currentFileSumMap.get(f.file)))){
				toAsk.add(f);
			}
		}
		return toAsk;
	}
	
	public static LinkedList<FileEx> getFilesToDelete(File md5File) throws IOException{
		File checksums = new File(wurstPackFolder, "wurstpack.md5");
		LinkedList<FileEx> toDelete = new LinkedList<FileEx>();
		LinkedList<FileEx> newSums = FileChecksum.readChecksums(md5File);
		if (checksums.exists()){
			LinkedList<FileEx> currentSums = FileChecksum.readChecksums(checksums);
			Map<String, String> newSumMap = FileEx.getFileMap(newSums);
			for(FileEx f: currentSums){
				if(newSumMap.get(f.file) == null){
					toDelete.add(f);
				}
			}
		}
		return toDelete;
	}
	
	public static LinkedList<FileEx> getFilesToChange(File md5file) throws IOException{
		File checksums = new File(wurstPackFolder, "wurstpack.md5");
		LinkedList<FileEx> toChange = new LinkedList<FileEx>();
		LinkedList<FileEx> newSums = FileChecksum.readChecksums(md5file);
		if (checksums.exists()){
			LinkedList<FileEx> currentSums = FileChecksum.readChecksums(checksums);
			Map<String, String> currentSumMap = FileEx.getFileMap(currentSums);
			for(FileEx f: newSums){
				if((currentSumMap.get(f.file) == null) || !(currentSumMap.get(f.file).equals(f.checksum))){
					toChange.add(f);
				}
			}
		}else{
			File f = wurstPackFolder;
			if (! f.isDirectory()) {
				f.mkdir();
			}
			LinkedList<FileEx> currentSums = FileList.getCompleteFileList(f);
			Map<String, String> currentSumMap = FileEx.getFileMap(currentSums);
			for(FileEx fe: newSums){
				if((currentSumMap.get(fe.file) == null) || !(currentSumMap.get(fe.file).equals(fe.checksum))){
					toChange.add(fe);
				}
			}
		}
		return toChange;
	}
}
