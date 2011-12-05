package de.peeeq.wurstscript.mpq;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.peeeq.wurstscript.WLogger;

public class WinMpq implements MpqEditor {

	WinMpq() {
	}
	
	@Override
	public void extractFile(File mpqArchive, String fileToExtract, File tempFile)
			throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		File tempFile1 = new File("./temp/" + fileToExtract);
		File tempFolder = new File(tempFile1.getParent());
		String[] commands = {"./winmpq/WinMPQ.exe", "extract", mpqArchive.getAbsolutePath(), fileToExtract, tempFolder.getAbsolutePath()};
		
		
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
		String[] commands = {"./winmpq/WinMPQ.exe", "add", mpqArchive.getAbsolutePath(), tempFile.getAbsolutePath(), filenameInMpq};
		
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
		String[] commands = {"./winmpq/WinMPQ.exe", "delete", mpqArchive.getAbsolutePath(), filenameInMpq};
		
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
