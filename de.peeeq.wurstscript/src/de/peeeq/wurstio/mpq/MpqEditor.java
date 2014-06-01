package de.peeeq.wurstio.mpq;

import java.io.Closeable;

public interface MpqEditor extends Closeable {

	byte[] extractFile(String fileToExtract) throws Exception;

	void insertFile(String filenameInMpq, byte[] contents) throws Exception;

	void deleteFile(String filenameInMpq) throws Exception;
	
}
