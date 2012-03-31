package de.peeeq.wurstscript.mpq;

import java.io.File;
import java.io.IOException;

public interface MpqEditor {

	File extractFile(File mpqArchive, String fileToExtract)
			throws IOException, InterruptedException;

	void insertFile(File mpqArchive, String filenameInMpq, File tempFile)
			throws IOException, InterruptedException;

	void deleteFile(File mpqArchive, String filenameInMpq)
			throws IOException, InterruptedException;
	
	void compactArchive(File mpqArchive)
			throws IOException, InterruptedException;
}
