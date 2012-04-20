package de.peeeq.wurstscript.mpq;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class MoPaqScriptfiles {

	static File extractFile(File mpqArchive, String fileInMap) {
		File script = new File("./temp/extract.txt");
		String scriptString = "extract " + "\"" + mpqArchive.getAbsolutePath() + "\"" + " " + fileInMap;  
		try {
			Files.write(scriptString, script, Charsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return script;
	}
	
	static File insertFile(File mpqArchive, File file, String fileInMap) {
		File script = new File("./temp/insert.txt");
		String scriptString = "add " + "\"" + mpqArchive.getAbsolutePath() + "\"" + " " + "\"" + file.getAbsolutePath() + "\"" + " " + fileInMap;  
		try {
			Files.write(scriptString, script, Charsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return script;
	}
	
	static File deleteMapfile(File mpqArchive, String fileInMap) {
		File script = new File("./temp/delete.txt");
		String scriptString = "delete " + "\"" + mpqArchive.getAbsolutePath() + "\"" + " " + fileInMap;  
		try {
			Files.write(scriptString, script, Charsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return script;
	}
	
	static File compactMapfile(File mpqArchive) {
		File script = new File("./temp/compact.txt");
		String scriptString = "compact " + "\"" + mpqArchive.getAbsolutePath() + "\"";  
		try {
			Files.write(scriptString, script, Charsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return script;
	}
}
