package de.peeeq.wurstscript.mpq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.peeeq.wurstscript.WLogger;

public class LadikMpq implements MpqEditor {

	LadikMpq() {
	}
	
	@Override
	public void extractFile(File mpqArchive, String fileToExtract, File tempFile)
			throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		File tempFile1 = new File("./temp/" + fileToExtract);
		//scriptfile
		File script = new File("extract.txt");
		String scriptString = "extract " + mpqArchive.getAbsolutePath() + " " + fileToExtract;  
		Files.write(scriptString, script, Charsets.UTF_8);
		String[] commands = {MpqEditorFactory.getFilepath(), "/console", script.getAbsolutePath(), "./temp"};
		System.out.println(Arrays.toString( commands ));
		System.out.println("yes");
		
		Process proc = rt.exec(commands);
		InputStream procOut = proc.getInputStream();
		BufferedReader procOutReader = new BufferedReader(new InputStreamReader(procOut));
		proc.waitFor();
		String line;
		while ((line = procOutReader.readLine()) != null) {
			WLogger.info(line);
		}
		
		if (!tempFile1.exists()) {
			throw new Error("could not extract file");
		}
		
		if (tempFile.exists()) {
			tempFile.delete();
		}
		
		tempFile1.renameTo(tempFile);
		if (!tempFile.exists()) {
			throw new Error("could not rename file");
		}
		
		
	}

	@Override
	public void insertFile(File mpqArchive, String filenameInMpq, File tempFile)
			throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		String[] commands = {MpqEditorFactory.getFilepath(),"/console", "add", mpqArchive.getAbsolutePath(), tempFile.getAbsolutePath(), filenameInMpq};
		
		Process proc = rt.exec(commands);
		InputStream procOut = proc.getInputStream();
		BufferedReader procOutReader = new BufferedReader(new InputStreamReader(procOut));
		proc.waitFor();
		String line;
		while ((line = procOutReader.readLine()) != null) {
			WLogger.info(line);
		}
		
	}

	@Override
	public void deleteFile(File mpqArchive, String filenameInMpq)
			throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		String[] commands = {MpqEditorFactory.getFilepath(),"/console", "delete", mpqArchive.getAbsolutePath(), filenameInMpq};
		
		Process proc = rt.exec(commands);
		InputStream procOut = proc.getInputStream();
		BufferedReader procOutReader = new BufferedReader(new InputStreamReader(procOut));
		proc.waitFor();
		String line;
		while ((line = procOutReader.readLine()) != null) {
			WLogger.info(line);
		}
		
	}
	
	public void compact(File mpqArchive)
			throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		String[] commands = {MpqEditorFactory.getFilepath(),"/console", "compact", mpqArchive.getAbsolutePath()};
		
		Process proc = rt.exec(commands);
		InputStream procOut = proc.getInputStream();
		BufferedReader procOutReader = new BufferedReader(new InputStreamReader(procOut));
		proc.waitFor();
		String line;
		while ((line = procOutReader.readLine()) != null) {
			WLogger.info(line);
		}
		
	}

}

