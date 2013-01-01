package control;


import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;

public class UpdaterInfos {
	public static UpdaterInfos INFO;
	
	public String packVersion 			= "0";
	public String compilerVersion 		= "0";
	
	public String updateSite			= "http://www.sunayama.de/WurstUpdt/";
	public String infoFileName 			= "latestInfo.json";
	public String updatePackName 		= "packUpdate.zip";
	public String updateCompilerName 	= "compilerUpdate.zip";
	public String historyName			= "history.html";
	
	public UpdaterInfos() {
		
	}
	
	public static void getCurrentInfo() throws IOException {
		File f = new File("config.json");
		if (f.exists()) {
			String json = Files.toString(f, Charsets.UTF_8);
			INFO = new Gson().fromJson(json, UpdaterInfos.class);
		}else
			INFO = new UpdaterInfos();
		saveInfo();
	}
	
	public static void saveInfo() throws IOException {
		File f = new File("config.json");
		String json = new Gson().toJson(INFO);
		Files.write(json, f, Charsets.UTF_8);
	}
	
	public static void updateCurrentInfo(UpdaterInfos latest, boolean pack) throws IOException {
		if (INFO.packVersion.compareTo(latest.packVersion) < 0 && pack){
			INFO.packVersion = latest.packVersion;
			System.out.println(INFO.packVersion + "  " + latest.packVersion);
		}
		if (INFO.compilerVersion.compareTo(latest.compilerVersion) < 0)
			INFO.compilerVersion = latest.compilerVersion;
		
		if (!INFO.updateSite.equals(latest.updateSite))
			INFO.updateSite = latest.updateSite;
		if (!INFO.infoFileName.equals(latest.infoFileName))
			INFO.infoFileName = latest.infoFileName;
		if (!INFO.updatePackName.equals(latest.updatePackName))
			INFO.updatePackName = latest.updatePackName;
		if (!INFO.updateCompilerName.equals(latest.updateCompilerName))
			INFO.updateCompilerName = latest.updateCompilerName;

	}
	
	
}
