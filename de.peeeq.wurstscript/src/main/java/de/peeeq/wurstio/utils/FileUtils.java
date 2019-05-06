package de.peeeq.wurstio.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.parser.WPos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 */
public class FileUtils {


    public static void write(CharSequence data, File outFile) throws IOException {
        Files.asCharSink(outFile, Charsets.UTF_8).write(data);
    }

    public static boolean sameFile(File f1, File f2) {
        try {
            return java.nio.file.Files.isSameFile(f1.toPath(), f2.toPath());
        } catch (IOException e) {
            return false;
        }
    }


    public static boolean isInDirectoryTrans(WFile file, WFile directory) {
        try {
            return file.getPath().startsWith(directory.getPath());
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    public static void deleteRecursively(File f) throws IOException {
        if (!f.exists()) {
            return;
        }
        File[] files = f.listFiles();
        if (files != null) {
            for (File child : files) {
                deleteRecursively(child);
            }
        }
        boolean ok = f.delete();
        if (!ok) {
            throw new IOException("Could not delete file " + f);
        }
    }

    public static String getWPosParent(WPos pos) {
        String parentName = "";
        try {
            parentName = WFile.create(pos.getFile()).getPath().getParent().getFileName().toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return parentName;
    }
}
