package control;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import ui.UpdateNotificationWindow;
import ui.UpdaterWindow;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;

public class UpdaterController {
	UpdaterWindow window;
	UpdaterInfos latestInfo;
	
	File tempdir;

	public boolean compiler;
	
	public UpdaterController() {
		try {
			// Get/Generate the INFO
			UpdaterInfos.getCurrentInfo();
			
			System.out.println("started");
			
			// Create the UI
			window = new UpdaterWindow();
			window.open(this);
			showInfo();
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
		if (UpdaterInfos.INFO.packVersion.compareTo(latestInfo.packVersion) < 0) {
			window.lblNoUpdatesAvailable.setText("Updates Available!");
			window.btnUpdate.setEnabled(true);
		} else if (UpdaterInfos.INFO.compilerVersion.compareTo(latestInfo.compilerVersion) < 0) {
			window.lblNoUpdatesAvailable.setText("Updates Available!");
			window.btnUpdate.setEnabled(true);
			compiler = true;
		}


		
	}
	
	public void assignInfo() {
		
	}
		
	public static void main(String args[]) {
		if ( args[0].equals("-check") ){
			System.out.println("check");
			try {
				UpdaterInfos.getCurrentInfo();
				File tempdir = new File("temp");
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
				UpdaterInfos latestInfo = new Gson().fromJson(json, UpdaterInfos.class);
				if (UpdaterInfos.INFO.packVersion.compareTo(latestInfo.packVersion) < 0) {
					UpdateNotificationWindow win = new UpdateNotificationWindow();
					win.open();
				} else if (UpdaterInfos.INFO.compilerVersion.compareTo(latestInfo.compilerVersion) < 0) {
					UpdateNotificationWindow win = new UpdateNotificationWindow();
					win.open();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			new UpdaterController();
		}
	}

	public void updatePack() throws MalformedURLException, IOException {
		File f = new File(tempdir + File.separator + latestInfo.updatePackName);
		System.out.println(f.getAbsolutePath());
		NetControl.downloadFile(latestInfo.updateSite + latestInfo.updatePackName,f);
		window.lblStatus.setText("Status: " + "Extracting...");
		window.progressBar.setSelection(60);
		ZipFile zipfile;
		try {
			zipfile = new ZipFile(f);
			String path =  f.getAbsolutePath();
			System.out.println(path);
			path = path.substring(0, path.lastIndexOf(File.separator));
			System.out.println(path);
			path = path.substring(0, path.lastIndexOf(File.separator));
			System.out.println(path);
			File wurstpackDir = new File( path);
			wurstpackDir.mkdir();
			
			zipfile.extractAll(wurstpackDir.getAbsolutePath());
			
			UpdaterInfos.updateCurrentInfo(latestInfo, true);
			UpdaterInfos.saveInfo();
			window.lblStatus.setText("Status: " + "Updating...");
			window.progressBar.setSelection(80);
			showInfo();
			window.lblStatus.setText("Status: " + "Done.");
			window.progressBar.setSelection(100);
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		f.delete();
		
	}

	public void updateCompiler() throws MalformedURLException, IOException {
		File f = new File(tempdir + File.separator + latestInfo.updateCompilerName);
		System.out.println(f.getAbsolutePath());
		NetControl.downloadFile(latestInfo.updateSite + latestInfo.updateCompilerName,f);
		window.lblStatus.setText("Status: " + "Extracting...");
		window.progressBar.setSelection(60);
		ZipFile zipfile;
		try {
			zipfile = new ZipFile(f);
			File wurstpackDir = new File( "Wurstpack" + File.separator + "wurstscript");
			wurstpackDir.mkdir();
			
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
		f.delete();
	}

}
