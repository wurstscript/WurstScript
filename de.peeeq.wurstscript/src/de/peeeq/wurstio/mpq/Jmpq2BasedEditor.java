package de.peeeq.wurstio.mpq;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import de.peeeq.jmpq.JMpqEditor;
import de.peeeq.jmpq.JMpqException;

class Jmpq2BasedEditor implements MpqEditor {

	private JMpqEditor editor;
	private boolean bestCompression = false;

	private JMpqEditor getEditor() {
		if (editor == null) {
			throw new RuntimeException("editor already closed");
		}
		return editor;
	}
	public Jmpq2BasedEditor(File mpqArchive) throws Exception {
		Preconditions.checkNotNull(mpqArchive);
		if (!mpqArchive.exists()) {
			throw new FileNotFoundException("not found: " + mpqArchive);
		}
		this.editor = new JMpqEditor(mpqArchive);
	}

	@Override
	public void insertFile(String filenameInMpq, byte[] contents) throws Exception {
		getEditor().insertFile(contents, filenameInMpq);
	}
	
	
	@Override
	public byte[] extractFile(String fileToExtract)	throws Exception {
		return getEditor().extractFile(fileToExtract);
	}


	@Override
	public void deleteFile(String filenameInMpq) throws Exception {
		getEditor().deleteFile(filenameInMpq);
	}

	@Override
	public void close() throws IOException {
		if (editor != null) {
			try {
				editor.close(bestCompression);
			} catch (JMpqException e) {
				throw new IOException(e);
			}
			editor = null;
		}
	}

}

