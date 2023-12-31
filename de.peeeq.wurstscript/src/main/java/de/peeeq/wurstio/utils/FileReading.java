package de.peeeq.wurstio.utils;

import com.google.common.base.Charsets;
import de.peeeq.wurstscript.WLogger;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class FileReading {

    private static final UniversalDetector detector = new UniversalDetector();

    /**
     * get a reader for a file
     */
    private static Reader getFileReader(File file, Charset cs) throws IOException {
        return Files.newBufferedReader(file.toPath(), cs);
    }

    /**
     * get a reader for a file and guess the charset using jchardet
     */
    public static Reader getFileReader(File file) throws IOException {
        try (InputStream fis = Files.newInputStream(file.toPath())) {

            byte[] buf = new byte[4096];

            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();

            String encoding = detector.getDetectedCharset();

            detector.reset();

            if (encoding == null) {
                WLogger.severe("Could not get encoding for " + file.getAbsolutePath());
                return getFileReader(file, Charsets.UTF_8);
            } else {
                return getFileReader(file, Charset.forName(encoding));
            }
        }
    }

}
