package file;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class FileEx {
	public String file;
	public String checksum;
	
	public FileEx(String file, String checksum){
		this.file = file;
		this.checksum = checksum;
	}
	
	public static Map<String, String> getFileMap(LinkedList<FileEx> list){
		Map<String, String> temp = new HashMap<String, String>();
		for(FileEx f: list){
			temp.put(f.file, f.checksum);
		}
		return temp;
	}
}
