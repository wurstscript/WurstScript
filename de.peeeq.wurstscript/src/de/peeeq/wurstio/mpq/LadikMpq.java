package de.peeeq.wurstio.mpq;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.utils.Debug;
import de.peeeq.wurstscript.utils.Utils;

public class LadikMpq implements MpqEditor {

	LadikMpq() {
	}
	
	@Override
	public File extractFile(File mpqArchive, String fileToExtract)
			throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		File tempFile1 = getNewTempFile(fileToExtract, 0);
		WLogger.info("Extracting " + fileToExtract + " from " + mpqArchive.getAbsolutePath() + " into " + tempFile1.getAbsolutePath());
		File script = MoPaqScriptfiles.extractFile(mpqArchive, fileToExtract);
		
		String[] commands = {MpqEditorFactory.getFilepath(), "extract", mpqArchive.getAbsolutePath(), fileToExtract, tempFile1.getParentFile().getAbsolutePath()};
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
		if (!tempFile1.exists()) {
			throw new IOException("Could not extract file " + fileToExtract + " from " + mpqArchive.getAbsolutePath());
		}
//		script.delete();
		return tempFile1;
		
	}

	private File getNewTempFile(String fileName, int i) {
		String fileName2 = fileName;
		if (i > 0) {
			fileName2 = fileName2.replace(".", i + ".");
		}
		File f = new File("./temp/" + fileName2);
		if (f.exists()) {
			boolean deleted = f.delete();
			if (!deleted) {
				return getNewTempFile(fileName, i+1);
			}
		}
		return f;
	}
	
	private void runCommand(String ... args) throws IOException, InterruptedException {
		List<String> argsList = Lists.newArrayList(args);
		
		argsList.add(0, MpqEditorFactory.getFilepath());
		
		if (!System.getProperty("os.name").startsWith("Windows")) {
			// run with wine
			argsList.add(0, "wine");
		}
		Runtime rt = Runtime.getRuntime();
		
		Process proc = rt.exec(argsList.toArray(new String[0]));
		int exit = proc.waitFor();
		String out = Utils.readWholeStream(proc.getInputStream());
		String err = Utils.readWholeStream(proc.getErrorStream());
		WLogger.info("exit code =" + exit);
		WLogger.info("out =" + out);
		WLogger.info("err =" + err);
		if (!err.isEmpty()) {
			throw new IOException("Could not run MPQ editor: " + err);
		}
	}

	@Override
	public void insertFile(File mpqArchive, String filenameInMpq, File tempFile)
			throws IOException, InterruptedException {
		runCommand("add", mpqArchive.getAbsolutePath(), tempFile.getAbsolutePath(), filenameInMpq);
	}

	@Override
	public void deleteFile(File mpqArchive, String filenameInMpq)
			throws IOException, InterruptedException {
		runCommand("delete", mpqArchive.getAbsolutePath(), filenameInMpq);		
	}
	
	@Override
	public void compactArchive(File mpqArchive) throws IOException,
			InterruptedException {
		runCommand("compact", mpqArchive.getAbsolutePath());
//		Runtime rt = Runtime.getRuntime();
//		File script = MoPaqScriptfiles.compactMapfile(mpqArchive);
//		
//		String[] commands = {MpqEditorFactory.getFilepath(), "/console", script.getAbsolutePath()};		
//		
//		Process proc = rt.exec(commands);
//		InputStream procOut = proc.getInputStream();
//		BufferedReader procOutReader = new BufferedReader(new InputStreamReader(procOut));
//		proc.waitFor();
//		String line;
////		script.delete();
//		while ((line = procOutReader.readLine()) != null) {
//			WLogger.info(line);
//		}
	}

}

