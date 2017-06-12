package de.peeeq.wurstscript.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.peeeq.wurstscript.WLogger;

public enum TempDir {
    ;
    private static File tempDir;

    public static File get() {
        if (tempDir == null) {
            try {
                Path path = Paths.get(System.getProperty("java.io.tmpdir") + "wurst");
                tempDir = path.toFile();
                if (!tempDir.exists())
                    java.nio.file.Files.createDirectory(path);

                File[] files = tempDir.listFiles();
                for (File f : files) {
                    f.delete();
                }
            } catch (IOException e) {
                try {
                    tempDir = java.nio.file.Files.createTempDirectory("wurst").toFile();
                } catch (IOException e1) {
                    WLogger.severe("Cant setup temp directory");
                }
            }
        }
        return tempDir;
    }

}
