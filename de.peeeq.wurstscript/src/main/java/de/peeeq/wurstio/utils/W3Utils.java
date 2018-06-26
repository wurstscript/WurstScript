package de.peeeq.wurstio.utils;

import net.moonlightflower.wc3libs.bin.GameExe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class W3Utils {
    private static final Logger log = LoggerFactory.getLogger(W3Utils.class.getName());
    private static GameExe gameExe;
    private static double version = -1;

    private static boolean isWindows() {
        return System.getProperty("os.name").contains("win");
    }

    /**
     * @return The wc3 patch version or -1 if none has been found
     */
    public static double getWc3PatchVersion() {
        if (gameExe == null && isWindows()) {
            gameExe = GameExe.fromRegistry();
        }
        if (version == -1 && gameExe != null) {
            try {
                log.info("Parsed game version: " + gameExe.getVersion());
                version = Double.parseDouble(gameExe.getVersion().replaceAll("\\.", ""));
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
    public static double parsePatchVersion(File wc3Path) {
        try {
            gameExe = GameExe.fromDir(wc3Path);
            if (gameExe != null) {
                String version = gameExe.getVersion();
                log.info("Parsed game version: " + version);
                W3Utils.version = Double.parseDouble(version.replaceAll("\\.", ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
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
