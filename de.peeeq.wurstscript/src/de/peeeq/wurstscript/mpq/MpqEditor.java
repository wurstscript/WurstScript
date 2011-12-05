package de.peeeq.wurstscript.mpq;

import java.io.File;
import java.io.IOException;

public interface MpqEditor {

	void extractFile(File mpqArchive, String fileToExtract, File tempFile)
			throws IOException, InterruptedException;

	void insertFile(File mpqArchive, String filenameInMpq, File tempFile)
			throws IOException, InterruptedException;

	void deleteFile(File mpqArchive, String filenameInMpq)
			throws IOException, InterruptedException;

}
