package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Maps;

public class WEStrings {

	private Map<String, String> data = Maps.newHashMap(); 
	
	public WEStrings parseFile(File file) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				parseLine(line);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	
	
	private void parseLine(String line) {
		if (line.contains("=")) {
			int pos = line.indexOf("=");
			String key = line.substring(0, pos);
			String val = line.substring(pos+1);
			if (val.startsWith("\"")) {
				val = val.substring(1, val.length()-1);
			}
			data.put(key, val);
		}
	}
	
	public String get(String key) {
		String result = key; 
		while (result.startsWith("WESTRING_")) {
			String newResult = data.get(result);
			if (newResult == null || newResult.equals(result)) {
				return result;
			}
			result = newResult;
		}
		return result;
	}
	
}
