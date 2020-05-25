package de.peeeq.wurstio.utils;

import de.peeeq.wurstscript.WLogger;
import net.moonlightflower.wc3libs.bin.GameExe;
import net.moonlightflower.wc3libs.port.GameVersion;
import net.moonlightflower.wc3libs.port.NotFoundException;
import net.moonlightflower.wc3libs.port.Orient;
import net.moonlightflower.wc3libs.port.StdGameExeFinder;
import net.moonlightflower.wc3libs.port.StdGameVersionFinder;
import net.moonlightflower.wc3libs.port.win.WinGameExeFinder;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class W3InstallationData {
    private Optional<File> gameExe = Optional.empty();

    private Optional<GameVersion> version = Optional.empty();

    /** Evaluates the game path and version by discovering the system environment. */
    public W3InstallationData() {
        discoverExePath();
        discoverVersion();
    }

    /**
     * Evaluates the game path and version, attempting to use the provided path if possible, before discovering the
     * system environment.
     */
    public W3InstallationData(File wc3Path) {
        if (!Orient.isWindowsSystem()) {
            WLogger.warning("Game path configuration only works on windows");
            discoverExePath();
            discoverVersion();
            return;
        }

        try {
            gameExe = Optional.ofNullable(WinGameExeFinder.fromDirIgnoreVersion(wc3Path));
        } catch (NotFoundException e) {
            WLogger.severe(e);
        }
        WLogger.info("Game Executable from path: " + gameExe);

        version = gameExe.flatMap(exe -> {
            try {
                return Optional.ofNullable(GameExe.getVersion(exe));
            } catch (IOException e) {
                WLogger.severe(e);
            }

            return Optional.empty();
        });
        WLogger.info("Parsed custom game version from executable: " + version);

        if (!gameExe.isPresent()) {
            WLogger.warning("The provided wc3 path wasn't suitable. Falling back to discovery.");
            discoverExePath();
            discoverVersion();
        }
    }

    private void discoverExePath() {
        try {
            gameExe = Optional.ofNullable(new StdGameExeFinder().get());
            WLogger.info("Parsed game path: " + gameExe);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    private void discoverVersion() {
        try {
            version = Optional.ofNullable(new StdGameVersionFinder().get());
            WLogger.info("Parsed game version: " + version);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return The wc3 patch version or empty if none has been found
     */
    public Optional<GameVersion> getWc3PatchVersion() {
        return version;
    }

    /**
     * @return The wc3 path or empty if none has been found
     */
    public Optional<File> getGameExe() {
        return gameExe;
    }
}
