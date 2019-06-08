package de.peeeq.wurstio.utils;

import de.peeeq.wurstscript.WLogger;
import net.moonlightflower.wc3libs.bin.GameExe;
import net.moonlightflower.wc3libs.port.GameVersion;
import net.moonlightflower.wc3libs.port.NotFoundException;
import net.moonlightflower.wc3libs.port.StdGameExeFinder;

import java.io.File;
import java.io.IOException;

public class W3Utils {
    private static File gameExe;

    private static GameVersion version = null;
    private static boolean isWindows() {
        return System.getProperty("os.name").contains("win");
    }

    /**
     * @return The wc3 patch version or -1 if none has been found
     */
    public static GameVersion getWc3PatchVersion() {
        if (gameExe == null && isWindows()) {
            try {
                gameExe = new StdGameExeFinder().get();
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
        if (version == null && gameExe != null) {
            try {
                version = GameExe.getVersion(gameExe);
                WLogger.info("Parsed game version: " + version);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return version;
    }

    /**
     * Pass a custom directory here to attempt parsing patch level from.
     *
     * @return The wc3 patch version or -1 if none has been found
     */
    public static GameVersion parsePatchVersion(File wc3Path) {
        try {
            gameExe = StdGameExeFinder.fromDirIgnoreVersion(wc3Path);
            WLogger.info("Game Executable: " + gameExe);
        } catch (NotFoundException e) {
            try {
                gameExe = new StdGameExeFinder().get();
            } catch (NotFoundException ex) {
                WLogger.severe(e);
            }
        }
        if (gameExe != null) {
            try {
                W3Utils.version = GameExe.getVersion(gameExe);
                WLogger.info("Parsed game version: " + version);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return version;
    }


    public static File getGameExe() {
        return gameExe;
    }
}
