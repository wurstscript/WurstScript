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
	
	public static boolean hasPack(){
		return new File("Wurstpack").exists();
	}
	
	public static LinkedList<FileEx> getFilesToAsk(LinkedList<FileEx> toChange, LinkedList<FileEx> toDelete) throws IOException{
		File checksums = new File("Wurstpack\\wurstpack.md5");
		LinkedList<FileEx> toAsk = new LinkedList<FileEx>();
		if (!checksums.exists()){
			return toAsk;
		}
		LinkedList<FileEx> currentSums = FileList.getCompleteFileList(new File("Wurstpack"));
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
	
	public static LinkedList<FileEx> getFilesToDelete() throws IOException{
		File checksums = new File("Wurstpack\\wurstpack.md5");
		LinkedList<FileEx> toDelete = new LinkedList<FileEx>();
		LinkedList<FileEx> newSums = FileChecksum.readChecksums(Download.downloadFile("wurstpack.md5", File.createTempFile("newChecksums", "md5")));
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
	
	public static LinkedList<FileEx> getFilesToChange() throws IOException{
		File checksums = new File("Wurstpack\\wurstpack.md5");
		LinkedList<FileEx> toChange = new LinkedList<FileEx>();
		LinkedList<FileEx> newSums = FileChecksum.readChecksums(Download.downloadFile("wurstpack.md5", File.createTempFile("newChecksums", "md5")));
		if (checksums.exists()){
			LinkedList<FileEx> currentSums = FileChecksum.readChecksums(checksums);
			Map<String, String> currentSumMap = FileEx.getFileMap(currentSums);
			for(FileEx f: newSums){
				if((currentSumMap.get(f.file) == null) || !(currentSumMap.get(f.file).equals(f.checksum))){
					toChange.add(f);
				}
			}
		}else{
			LinkedList<FileEx> currentSums = FileList.getCompleteFileList(new File("Wurstpack"));
			Map<String, String> currentSumMap = FileEx.getFileMap(currentSums);
			for(FileEx f: newSums){
				if((currentSumMap.get(f.file) == null) || !(currentSumMap.get(f.file).equals(f.checksum))){
					toChange.add(f);
				}
			}
		}
		return toChange;
	}
}
