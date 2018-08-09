package de.peeeq.wurstio.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class FileUtils {



    public static void write(CharSequence data, File outFile) throws IOException {
        Files.asCharSink(outFile, Charsets.UTF_8).write(data);
    }
}
