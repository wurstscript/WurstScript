package de.peeeq.wurstscript;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

public class WurstConfig {

	private static WurstConfig instance;

	private Map<String, String> settings = Maps.newHashMap();
	
	private WurstConfig() {
		File config1 = new File("./wurstscript/wurst.config");
		File config2 = new File("./wurst.config");
		 
		readDefaults();
		
		if (config1.exists()) {
			readFromFile(config1);
		} else if (config2.exists()) {
			readFromFile(config2);
		} else {
			WLogger.severe("Could not find config file wurst.config!");
		}
	}
	
	private void readDefaults() {
		// default values
		settings.put("mpq.extract", "./winmpq/WinMPQ.exe extract %map %nameInMpq %folder");
		settings.put("mpq.add", "./winmpq/WinMPQ.exe add %map %file %nameInMpq");
		settings.put("mpq.delete", "./winmpq/WinMPQ.exe delete %map %nameInMpq");
		settings.put("lib", "./wurstscript/lib/");
	}

	public static WurstConfig get() {
		if (instance == null) {
			instance = new WurstConfig();
		}
		return instance;
	}
	
	
	private void readFromFile(File file) {
		if (!file.exists()) {
			throw new Error("Config file " + file.getAbsolutePath() + " was not found.");
		}
		
		try {
			List<String> lines = Files.readLines(file, Charsets.UTF_8);
			int lineNr = 1;
			for (String line : lines) {
				parseLine(lineNr++, line);
			}
		} catch (IOException e) {
			WLogger.severe(e);
			throw new Error("Could not open config file " + file.getAbsolutePath() + "\n" + e.getMessage());
		}
	}

	private void parseLine(int lineNr, String line) {
		line = line.trim();
		
		// remove comments
		line = line.replaceAll("//.*", "");
		
		if (line.length() == 0) {
			return; // empty line
		}
		
		String[] parts = line.split("=");
		if (parts.length < 2) {
			throw new Error("Parse error (missing equal sign) in config file, line " + lineNr);
		}
		String key = parts[0];
		String value = parts[1];
		for (int i=2; i<parts.length; i++) {
			value += "=" + parts[i];
		}
		if (!settings.containsKey(key)) {
			throw new Error("Unknown setting: " + key);
		}
		
		settings.put(key, value);
		
	}

	
	String getSetting(String key) {
		if (!settings.containsKey(key)) {
			throw new Error("Unknown setting: " + key);
		}
		return settings.get(key);
	}
	
}
