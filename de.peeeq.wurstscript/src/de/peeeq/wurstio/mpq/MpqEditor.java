package de.peeeq.wurstio.mpq;

import java.io.File;
import java.io.IOException;

public interface MpqEditor {

	File extractFile(File mpqArchive, String fileToExtract) throws Exception;

	void insertFile(File mpqArchive, String filenameInMpq, File tempFile) throws Exception;

	void deleteFile(File mpqArchive, String filenameInMpq) throws Exception;

	void compactArchive(File mpqArchive) throws Exception;
}
