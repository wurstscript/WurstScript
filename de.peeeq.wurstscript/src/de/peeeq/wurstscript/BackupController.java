package de.peeeq.wurstscript;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

public class BackupController {
	
	File backupFolder;
	int backupLimit;
	
	/**
	 * create a backup of the mapfile
	 */
	public void makeBackup(String mapFileName, int limit) throws Error, IOException {
		File mapFile = new File(mapFileName);
		if (!mapFile.exists()) {
			throw new Error("Mapfile " + mapFile + " does not exist.");
		}
		backupFolder = new File("./backups/");
		backupFolder.mkdirs();
		backupLimit = limit;
		if (backupLimit > 998) { backupLimit = 998; }
		
		String mapName = mapFileName.substring(mapFileName.lastIndexOf("\\")+1,mapFileName.lastIndexOf("."));
		int count = backupCount(mapName);
		if (count > backupLimit ) {
			deleteOldBackups(mapName);
			count--;
		}
	
		
		File backupFile = new File("./backups/" + mapName + "." + toCorrectString(count+1) + ".w3x");
		Files.copy(mapFile, backupFile);
	}
	
	public String toCorrectString(int i){
		String val = String.valueOf(i);
		if ( i < 10 ){
			val = "00" + val;
		}else if ( i < 100 ){
			val = "0" + val;
		}
		return val;
	}
	
	public int backupCount(String mapName) throws Error, IOException {
		int count = 0;
		for ( File f : backupFolder.listFiles()) {
			String name = f.getName();
			if (name.length() > 4 && name.substring(0,name.indexOf(".")).equals(mapName) ) {
				count++;
			}
		}
		return count;
		
	}
	
	public void deleteOldBackups(String mapName) throws Error, IOException {
		File[] files = new File[backupLimit+1];
		for ( File f : backupFolder.listFiles()) {
			String name = f.getName();
			if (name.length() > 6 && name.substring(0,name.indexOf(".")).equals(mapName) ) {
				files[Integer.valueOf(name.substring(name.indexOf(".")+1,name.lastIndexOf(".")))-1] = f;
			}
		}
		files[0].delete();
		
		for ( int i = 1; i <= backupLimit; i++ ) {
			File f = files[i];
			String name = f.getName();
//			WLogger.info("Current in array: " + name);
			name = name.replaceFirst("."+toCorrectString(i+1)+".", "."+toCorrectString(i)+".");
//			WLogger.info("Current in array replaced: " + name);
			File backupFile = new File("./backups/" + name );
			Files.copy(f, backupFile);
			f.delete();
			i++;
		}
		
	}
	
	
	
	
}
