package de.peeeq.wurstio.mpq;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.utils.Utils;

public class LadikMpq implements MpqEditor {

	LadikMpq() {
	}
	
	@Override
	public File extractFile(File mpqArchive, String fileToExtract)
			throws IOException, InterruptedException {
		File tempFile1 = getNewTempFile(fileToExtract, 0);
		
		runCommand("extract", mpqArchive.getAbsolutePath(), fileToExtract, tempFile1.getParentFile().getAbsolutePath());
		if (!tempFile1.exists()) {
			throw new IOException("Could not extract file " + fileToExtract + " from " + mpqArchive.getAbsolutePath());
		}
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
	}
	
	private void runCommand(String ... args) throws IOException, InterruptedException {
		List<String> argsList = Lists.newArrayList(args);
		
		argsList.add(0, MpqEditorFactory.getFilepath());
		
		if (!System.getProperty("os.name").startsWith("Windows")) {
			// run with wine
			argsList.add(0, "wine");
		}
		Runtime rt = Runtime.getRuntime();
		
		
		WLogger.info("running: " + Utils.join(argsList, " "));
		Process proc = rt.exec(argsList.toArray(new String[0]));
		int exit = proc.waitFor();
		String out = Utils.readWholeStream(proc.getInputStream());
		String err = Utils.readWholeStream(proc.getErrorStream());
		WLogger.info("exit code =" + exit);
		WLogger.info("out =" + out);
		WLogger.info("err =" + err);
		if (exit != 0) {
			throw new IOException("Could not run MPQ editor (" + exit + "):" + err);
		}
	}

}

