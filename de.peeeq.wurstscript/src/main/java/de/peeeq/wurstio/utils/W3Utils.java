package de.peeeq.wurstio.utils;

import de.peeeq.wurstscript.WLogger;
import net.moonlightflower.wc3libs.bin.GameExe;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class W3Utils {
    private static GameExe gameExe;
    private static GameExe.Version version = null;

    private static boolean isWindows() {
        return System.getProperty("os.name").contains("win");
    }

    /**
     * @return The wc3 patch version or -1 if none has been found
     */
    public static GameExe.Version getWc3PatchVersion() {
        if (gameExe == null && isWindows()) {
            gameExe = GameExe.fromRegistry();
        }
        if (version == null && gameExe != null) {
            try {
                WLogger.info("Parsed game version: " + gameExe.getVersion());
                version = gameExe.getVersion();
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
    public static GameExe.Version parsePatchVersion(File wc3Path) {
        try {
            gameExe = GameExe.fromDir(wc3Path);
            WLogger.info("Game Executable: " + gameExe);
            if (gameExe != null) {
                W3Utils.version = gameExe.getVersion();
                WLogger.info("Parsed game version: " + version);
            }
        } catch (IOException e) {
            WLogger.severe(e);
        }
        return version;
    }

    /**
     * @return The wc3 installation path
     */
    public static String getGamePath() {
        return isWindows() ? Objects.requireNonNull(GameExe.fromRegistry()).getFile().getParent() : null;
    }
}
