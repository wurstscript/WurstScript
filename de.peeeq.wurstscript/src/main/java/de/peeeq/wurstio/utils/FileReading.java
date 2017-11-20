package de.peeeq.wurstio.utils;

import com.google.common.base.Charsets;
import de.peeeq.wurstscript.WLogger;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsPSMDetector;

import java.io.*;
import java.nio.charset.Charset;

public class FileReading {

    /**
     * get a reader for a file
     */
    public static Reader getFileReader(File file, Charset cs)
            throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader ir = new InputStreamReader(fis, cs);
        return ir;
    }

    /**
     * get a reader for a file and guess the charset using jchardet
     * http://jchardet.sourceforge.net/
     */
    public static Reader getFileReader(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream imp = new BufferedInputStream(fis)) {

            nsDetector det = new nsDetector(nsPSMDetector.ALL);

            final boolean[] found = new boolean[1];
            final String[] charset = new String[1];

            det.Init(cs -> {
                found[0] = true;
                charset[0] = cs;
            });

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
                found[0] = true;
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

    // /**
    // * get a reader for a file and guess the charset
    // * using juniversalchardet http://code.google.com/p/juniversalchardet/
    // * (seems not to work so well...)
    // */
    // public static Reader getFileReader(File file) throws IOException {
    // FileInputStream fis = new FileInputStream(file);
    // byte[] buf = new byte[4096];
    //
    // UniversalDetector detector = new UniversalDetector(null);
    // int nread;
    // while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
    // detector.handleData(buf, 0, nread);
    // }
    // detector.dataEnd();
    // String encoding = detector.getDetectedCharset();
    // if (encoding == null) {
    // throw new IOException("Could not get encoding for " +
    // file.getAbsolutePath());
    // } else {
    // WLogger.info("encoding = " + encoding);
    // return getFileReader(file, Charset.forName(encoding));
    // }
    // }

}
