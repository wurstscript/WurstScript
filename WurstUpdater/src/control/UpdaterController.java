package control;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;

import ui.UpdaterWindow;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class UpdaterController {
	UpdaterWindow window;
	UpdaterInfos latestInfo;
	
	File tempdir;

	
	public UpdaterController() {
		try {
			// Get/Generate the INFO
			UpdaterInfos.getCurrentInfo();
			
			// Create the UI
			window = new UpdaterWindow();
			window.open(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void showInfo() {

				
		// Tempdir
		tempdir = new File("temp");
		System.out.println(tempdir.mkdir() + " " + tempdir.getAbsolutePath());
		File jsonF = new File(tempdir + File.separator + "latest.json");
		String json = null;
		try {
			NetControl.downloadFile(UpdaterInfos.INFO.updateSite + UpdaterInfos.INFO.infoFileName, jsonF);
			json = Files.toString(jsonF, Charsets.UTF_8);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		latestInfo = new Gson().fromJson(json, UpdaterInfos.class);

		
		
		// Assign Values to Labels
		window.currentCompilerVersion.setText(UpdaterInfos.INFO.compilerVersion);
		window.currentPackVersion.setText(UpdaterInfos.INFO.packVersion);
		
		window.latestCompilerVersion.setText(latestInfo.compilerVersion);
		window.latestPackVersion.setText(latestInfo.packVersion);
		
		if (UpdaterInfos.INFO.packVersion.compareTo(latestInfo.packVersion) < 0)
			window.btnUpdatePack.setEnabled(true);
		else
			window.btnUpdatePack.setEnabled(false);
		if (UpdaterInfos.INFO.compilerVersion.compareTo(latestInfo.compilerVersion) < 0)
			if( ! UpdaterInfos.INFO.compilerVersion.equals("0")){
				window.btnUpdateCompiler.setEnabled(true);
			}
		else
			window.btnUpdateCompiler.setEnabled(false);
	}
	
	public void assignInfo() {
		
	}
		
	public static void main(String args[]) {
		new UpdaterController();
	}

	public void updatePack() throws MalformedURLException, IOException {
		window.lblStatus.setText("Status: " + "Downloading...");
		File f = new File(tempdir + File.separator + latestInfo.updatePackName);
		System.out.println(f.getAbsolutePath());
		NetControl.downloadFile(latestInfo.updateSite + latestInfo.updatePackName,f);
		ZipFile zipfile;
		try {
			zipfile = new ZipFile(f);
			File wurstpackDir = new File( "Wurstpack");
			wurstpackDir.mkdir();
			
			zipfile.extractAll(wurstpackDir.getAbsolutePath());
			window.lblStatus.setText("Status: " + "Extracting...");
			UpdaterInfos.updateCurrentInfo(latestInfo, true);
			UpdaterInfos.saveInfo();
			window.lblStatus.setText("Status: " + "Updating...");
			showInfo();
			window.lblStatus.setText("Status: " + "Done.");
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public void updateCompiler() throws MalformedURLException, IOException {
		window.lblStatus.setText("Status: " + "Downloading...");
		File f = new File(tempdir + File.separator + latestInfo.updateCompilerName);
		System.out.println(f.getAbsolutePath());
		NetControl.downloadFile(latestInfo.updateSite + latestInfo.updateCompilerName,f);
		ZipFile zipfile;
		try {
			zipfile = new ZipFile(f);
			File wurstpackDir = new File( "Wurstpack" + File.separator + "wurstscript");
			wurstpackDir.mkdir();
			window.lblStatus.setText("Status: " + "Extracting...");
			zipfile.extractAll(wurstpackDir.getAbsolutePath());
			UpdaterInfos.updateCurrentInfo(latestInfo, false);
			UpdaterInfos.saveInfo();
			window.lblStatus.setText("Status: " + "Updating...");
			showInfo();
			window.lblStatus.setText("Status: " + "Done.");
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
