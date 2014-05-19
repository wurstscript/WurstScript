package de.peeeq.wurstio.mpq;

import java.io.File;

import com.google.common.io.Files;

import de.peeeq.jmpq.JmpqEditor;
import de.peeeq.jmpq.JmpqError;

class JmpqBasedEditor implements MpqEditor {

	private JmpqEditor editor;

	private JmpqEditor getEditor() {
		if (editor == null) {
			throw new RuntimeException("editor not initialized");
		}
		return editor;
	}
	
	
	@Override
	public void open(File mpqArchive) throws Exception {
		this.editor = new JmpqEditor(mpqArchive);
	}

	@Override
	public void insertFile(String filenameInMpq, byte[] contents) throws Exception {
		File tempFile = getNewTempFile(filenameInMpq, 0);
		Files.write(contents, tempFile);
		getEditor().injectFile(tempFile, filenameInMpq);
	}
	
	
	@Override
	public byte[] extractFile(String fileToExtract)	throws Exception {
		File tempFile1 = getNewTempFile(fileToExtract, 0);
		tempFile1.getParentFile().mkdirs();
		getEditor().extractFile(fileToExtract, tempFile1);
		return Files.toByteArray(tempFile1);
	}


	@Override
	public void deleteFile(String filenameInMpq) throws Exception {
		getEditor().delete(filenameInMpq);
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
	public void close() throws JmpqError {
		if (editor != null) {
			editor.compact();
			editor.close();
			editor = null;
		}
	}

	

	

}

