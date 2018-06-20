package de.peeeq.wurstscript;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

public class BackupController {

    private final File backupFolder;
    private final int backupLimit;

    public BackupController() {
        this(new File("./backups/"), 24);
    }

    public BackupController(File backupFolder, int backupLimit) {
        this.backupFolder = backupFolder;
        if (backupLimit > 998) {
            backupLimit = 998;
        }
        this.backupLimit = backupLimit;
    }

    /**
     * create a backup of the mapfile
     */
    public void makeBackup(String mapFileName) throws Error, IOException {
        File mapFile = new File(mapFileName);
        if (!mapFile.exists()) {
            throw new Error("Mapfile " + mapFile + " does not exist.");
        }
        backupFolder.mkdirs();
        WLogger.info(mapFileName);
        mapFileName = new File(mapFileName).getName();
        String mapName = mapFileName.substring(0, mapFileName.lastIndexOf("."));
        WLogger.info(mapName);
        int count = backupCount(mapName);
        WLogger.info("Count " + count);
        if (count > backupLimit) {
            deleteOldBackups(mapName);
            count--;
        }


        File backupFile = new File("./backups/" + mapName + "-" + toCorrectString(count + 1) + ".w3x");
        Files.copy(mapFile, backupFile);
    }

    private String toCorrectString(int i) {
        String val = String.valueOf(i);
        if (i < 10) {
            val = "00" + val;
        } else if (i < 100) {
            val = "0" + val;
        }
        return val;
    }

    private int backupCount(String mapName) throws Error {
        int count = 0;
        for (File f : backupFolder.listFiles()) {
            String name = f.getName();
            if (name.lastIndexOf("-") > 0) {
                if (name.length() > 4 && name.substring(0, name.lastIndexOf("-")).equals(mapName)) {
                    count++;
                }
            }
        }
        return count;

    }

    private void deleteOldBackups(String mapName) throws Error, IOException {
        File[] files = new File[backupLimit + 1];
        for (File f : backupFolder.listFiles()) {
            String name = f.getName();
            if (name.lastIndexOf("-") > 0) {
                if (name.length() > 4 && name.substring(0, name.lastIndexOf("-")).equals(mapName)) {
                    files[Integer.valueOf(name.substring(name.lastIndexOf("-") + 1, name.lastIndexOf("."))) - 1] = f;
                }
            }
        }
        files[0].delete();

        for (int i = 1; i <= backupLimit; i++) {
            File f = files[i];
            String name = f.getName();
            WLogger.info("Current in array: " + name);
            name = name.replaceFirst("-" + toCorrectString(i + 1) + "\\.", "-" + toCorrectString(i) + ".");
            WLogger.info("Current in array replaced: " + name);
            File backupFile = new File("./backups/" + name);
            Files.copy(f, backupFile);
            f.delete();
        }

    }

}
