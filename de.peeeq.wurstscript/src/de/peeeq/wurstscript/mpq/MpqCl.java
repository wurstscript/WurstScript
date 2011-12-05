package de.peeeq.wurstscript.mpq;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.peeeq.wurstscript.WLogger;

public class MpqCl implements MpqEditor {
	
	MpqCl() {}
	
	@Override
	public void extractFile(File mpqArchive, String fileToExtract, File tempFile) throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		String[] commands = {"./wurstscript/MpqCL.exe", "extract", mpqArchive.getAbsolutePath(), fileToExtract, tempFile.getAbsolutePath()};
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
	public void insertFile(File mpqArchive, String filenameInMpq, File tempFile) throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		String[] commands = {"./wurstscript/MpqCL.exe", "inject", mpqArchive.getAbsolutePath(), filenameInMpq, tempFile.getAbsolutePath()};
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
		String[] commands = {"./wurstscript/MpqCL.exe", "delete", mpqArchive.getAbsolutePath(), filenameInMpq};
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
