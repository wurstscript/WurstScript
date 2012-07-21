package de.peeeq.wurstscript.mpq;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.utils.Debug;

public class LadikMpq implements MpqEditor {

	LadikMpq() {
	}
	
	@Override
	public File extractFile(File mpqArchive, String fileToExtract)
			throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		File tempFile1 = new File("./temp/" + fileToExtract);
		File script = MoPaqScriptfiles.extractFile(mpqArchive, "war3map.j");
		
		String[] commands = {MpqEditorFactory.getFilepath(), "/console", script.getAbsolutePath()};
		Debug.println(Arrays.toString(commands));
		
		Process proc = rt.exec(commands);
		InputStream procOut = proc.getInputStream();
		BufferedReader procOutReader = new BufferedReader(new InputStreamReader(procOut));
		proc.waitFor();
		String line;
		while ((line = procOutReader.readLine()) != null) {
			Debug.println(line);
			WLogger.info(line);
		}
		tempFile1 = new File("./temp/" + fileToExtract); 
		if (!tempFile1.exists()) {
			throw new Error("could not extract file");
		}
		script.delete();
		return tempFile1;
		
	}

	@Override
	public void insertFile(File mpqArchive, String filenameInMpq, File tempFile)
			throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		File script = MoPaqScriptfiles.insertFile(mpqArchive, tempFile, filenameInMpq);
		
		String[] commands = {MpqEditorFactory.getFilepath(), "/console", script.getAbsolutePath()};
		
		Process proc = rt.exec(commands);
		InputStream procOut = proc.getInputStream();
		BufferedReader procOutReader = new BufferedReader(new InputStreamReader(procOut));
		proc.waitFor();
		String line;
		script.delete();
		while ((line = procOutReader.readLine()) != null) {
			WLogger.info(line);
		}
		
	}

	@Override
	public void deleteFile(File mpqArchive, String filenameInMpq)
			throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		File script = MoPaqScriptfiles.deleteMapfile(mpqArchive, filenameInMpq);
		
		String[] commands = {MpqEditorFactory.getFilepath(), "/console", script.getAbsolutePath()};		
		
		Process proc = rt.exec(commands);
		InputStream procOut = proc.getInputStream();
		BufferedReader procOutReader = new BufferedReader(new InputStreamReader(procOut));
		proc.waitFor();
		String line;
		script.delete();
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

	@Override
	public void compactArchive(File mpqArchive) throws IOException,
			InterruptedException {
		Runtime rt = Runtime.getRuntime();
		File script = MoPaqScriptfiles.compactMapfile(mpqArchive);
		
		String[] commands = {MpqEditorFactory.getFilepath(), "/console", script.getAbsolutePath()};		
		
		Process proc = rt.exec(commands);
		InputStream procOut = proc.getInputStream();
		BufferedReader procOutReader = new BufferedReader(new InputStreamReader(procOut));
		proc.waitFor();
		String line;
		script.delete();
		while ((line = procOutReader.readLine()) != null) {
			WLogger.info(line);
		}
		
	}

}

