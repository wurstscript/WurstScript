package de.peeeq.wurstio.utils;

import de.peeeq.wurstio.languageserver.WurstLanguageServer;
import de.peeeq.wurstscript.WLogger;
import net.moonlightflower.wc3libs.bin.GameExe;
import net.moonlightflower.wc3libs.port.*;
import net.moonlightflower.wc3libs.port.win.WinGameExeFinder;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class W3InstallationData {
    private final WurstLanguageServer languageServer;

    private Optional<File> gameExe = Optional.empty();

    private Optional<GameVersion> version = Optional.empty();

    private File selectedFolder;

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {
        }
    }

    public W3InstallationData(Optional<File> gameExe, Optional<GameVersion> version) {
        this.languageServer = null;
        this.gameExe = gameExe;
        this.version = version;
    }

    /** Evaluates the game path and version by discovering the system environment. */
    public W3InstallationData(WurstLanguageServer languageServer) {
        this.languageServer = languageServer;
        discoverExePath();
        discoverVersion();
    }

    /**
     * Evaluates the game path and version, attempting to use the provided path if possible, before discovering the
     * system environment.
     */
    public W3InstallationData(WurstLanguageServer languageServer, File wc3Path) {
        this.languageServer = languageServer;
        if (!Orient.isWindowsSystem()) {
            WLogger.warning("Game path configuration only works on windows");
            discoverExePath();
            discoverVersion();
            return;
        }

        loadFromPath(wc3Path);

        if (!gameExe.isPresent()) {
            WLogger.warning("The provided wc3 path wasn't suitable. Falling back to discovery.");
            discoverExePath();
            discoverVersion();
        }
    }

    private void loadFromPath(File wc3Path) {
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
    }

    private void discoverExePath() {
        try {
            gameExe = Optional.ofNullable(new StdGameExeFinder().get());
            WLogger.info("Discovered game path: " + gameExe);
        } catch (NotFoundException | UnsupportedPlatformException e) {
            WLogger.warning("Can't find game installation directory: " + e.getMessage());
            showFileChooser();
        }
    }

    private void showFileChooser() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select Warcraft III installation directory");
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                JDialog dialog = new JDialog();
                dialog.setAlwaysOnTop(true);

                int result = fileChooser.showOpenDialog(dialog);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFolder = fileChooser.getSelectedFile();
                } else {
                    WLogger.warning("No directory selected");
                }
            });

            loadFromPath(selectedFolder);

            if (gameExe.isPresent()) {
                languageServer.getRemoteEndpoint().notify("wurst/updateGamePath", selectedFolder.getAbsolutePath());
            }
        } catch (InterruptedException | InvocationTargetException ex) {
            WLogger.warning("Choosing game path failed", ex);
        }
    }

    private void discoverVersion() {
        try {
            version = Optional.ofNullable(new StdGameVersionFinder().get());
            WLogger.info("Parsed game version: " + version);
        } catch (NotFoundException e) {
            WLogger.warning("Wurst compiler failed to determine game version", e);
        } catch (UnsupportedPlatformException e) {
            WLogger.warning("Wurst compiler cannot determine game version: " + e.getMessage());
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
