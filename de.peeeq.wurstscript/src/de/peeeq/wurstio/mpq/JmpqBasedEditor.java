package de.peeeq.wurstio.mpq;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.jmpq.JmpqEditor;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.utils.Utils;

public class JmpqBasedEditor implements MpqEditor {

	@Override
	public File extractFile(File mpqArchive, String fileToExtract)
			throws Exception {
		File tempFile1 = getNewTempFile(fileToExtract, 0);
		tempFile1.getParentFile().mkdirs();
		try (JmpqEditor editor = new JmpqEditor(mpqArchive)) {
			editor.extractFile(fileToExtract, tempFile1);
		}
		
		return tempFile1;
	}

	@Override
	public void insertFile(File mpqArchive, String filenameInMpq, File tempFile)
			throws Exception {
		try (JmpqEditor editor = new JmpqEditor(mpqArchive)) {
			editor.injectFile(tempFile, filenameInMpq);
		}
		
	}

	@Override
	public void deleteFile(File mpqArchive, String filenameInMpq)
			throws Exception {
		try (JmpqEditor editor = new JmpqEditor(mpqArchive)) {
			editor.delete(filenameInMpq);
		}
	}

	@Override
	public void compactArchive(File mpqArchive) throws Exception {
		// TODO
//		try (JmpqEditor editor = new JmpqEditor(mpqArchive)) {
//			editor.compact();
//		}
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

}

