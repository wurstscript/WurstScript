package de.peeeq.wurstio.mpq;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface MpqEditor extends Closeable {

    boolean canWrite();

    byte[] extractFile(String fileToExtract) throws Exception;

    void insertFile(String filenameInMpq, byte[] contents) throws Exception;

    void insertFile(String filenameInMpq, File contents) throws Exception;

    void deleteFile(String filenameInMpq) throws Exception;

    boolean hasFile(String fileName);

    /** Names of the files currently in the archive, including staged inserts and excluding staged deletes. */
    Collection<String> listFiles();

    void setKeepHeaderOffset(boolean flag);

    void closeWithCompression() throws IOException;
}
