package de.peeeq.wurstio.utils;

import com.google.common.base.Charsets;
import de.peeeq.wurstscript.WLogger;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsPSMDetector;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class FileReading {

    /**
     * get a reader for a file
     */
    private static Reader getFileReader(File file, Charset cs) throws IOException {
        return Files.newBufferedReader(file.toPath(), cs);
    }

    /**
     * get a reader for a file and guess the charset using jchardet
     * http://jchardet.sourceforge.net/
     */
    public static Reader getFileReader(File file) throws IOException {
        try (InputStream fis = Files.newInputStream(file.toPath());
             BufferedInputStream imp = new BufferedInputStream(fis)) {

            nsDetector det = new nsDetector(nsPSMDetector.ALL);

            final String[] charset = new String[1];

            det.Init(cs -> charset[0] = cs);

            byte[] buf = new byte[1024];
            int len;
            boolean done = false;
            boolean isAscii = true;

            while ((len = imp.read(buf, 0, buf.length)) != -1) {

                // Check if the stream is only ascii.
                if (isAscii)
                    isAscii = det.isAscii(buf, len);

                // DoIt if non-ascii and not done yet.
                if (!isAscii && !done)
                    done = det.DoIt(buf, len, false);
            }

            det.DataEnd();

            if (isAscii) {
                charset[0] = "ASCII";
            }

            String encoding = charset[0];

            if (encoding == null) {
                // throw new IOException("Could not get encoding for " +
                // file.getAbsolutePath());
                WLogger.severe("Could not get encoding for "
                        + file.getAbsolutePath());
                return getFileReader(file, Charsets.UTF_8);
            } else {
                return getFileReader(file, Charset.forName(encoding));
            }
        }
    }

}
