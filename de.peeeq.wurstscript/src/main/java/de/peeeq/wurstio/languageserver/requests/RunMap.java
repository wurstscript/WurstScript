package de.peeeq.wurstio.languageserver.requests;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.wurstscript.projectconfig.WurstProjectConfigData;
import org.wurstscript.projectconfig.WurstProjectConfigReader;
import de.peeeq.wurstio.gui.WurstGuiImpl;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.WurstBuildConfig;
import de.peeeq.wurstio.languageserver.WurstLanguageServer;
import de.peeeq.wurstio.utils.W3InstallationData;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.utils.Utils;
import net.moonlightflower.wc3libs.port.GameVersion;
import net.moonlightflower.wc3libs.port.Orient;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.lsp4j.MessageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static de.peeeq.wurstio.languageserver.ProjectConfigBuilder.FILE_NAME;

/**
 * Created by peter on 16.05.16.
 */
public class RunMap extends MapRequest {

    private @Nullable File customTarget = null;


    public RunMap(WurstLanguageServer langServer, WFile workspaceRoot, Optional<String> wc3Path, Optional<File> map,
                  List<String> compileArgs, Optional<String> gameExePath) {
        super(langServer, map, compileArgs, workspaceRoot, wc3Path, gameExePath);
        safeCompilation = SafetyLevel.QuickAndDirty;
    }

    @Override
    public Object execute(ModelManager modelManager) throws IOException {
        WLogger.info("Execute RunMap, \nwc3Path =" + wc3Path
            + ",\n map = " + map
            + ",\n compileArgs = " + compileArgs
            + ",\n workspaceRoot = " + workspaceRoot
            + ",\n runArgs = " + compileArgs
        );

        if (modelManager.hasErrors()) {
            throw new RequestFailedException(MessageType.Error, "Fix errors in your code before running.\n" + modelManager.getFirstErrorDescription());
        }

        WurstProjectConfigData projectConfig = WurstProjectConfigReader.load(workspaceRoot.getFile().toPath().resolve(FILE_NAME));
        if (projectConfig == null) {
            throw new RequestFailedException(MessageType.Error, FILE_NAME + " file doesn't exist or is invalid. " +
                "Please install your project using grill or the wurst setup tool.");
        }
        // TODO use normal compiler for this, avoid code duplication
        WurstGui gui = new WurstGuiImpl(getWorkspaceAbsolute());
        try {
            String ok = compileMap(modelManager, gui, projectConfig);
            if (ok != null) return ok;
        } catch (CompileError e) {
            WLogger.info(e);
            throw new RequestFailedException(MessageType.Error, "A compilation error occurred when running the map:\n" + e);
        } catch (RequestFailedException e) {
            // Already carries an actionable message and the intended MessageType; don't bury it in a generic wrapper.
            throw e;
        } catch (Exception e) {
            WLogger.warning("Exception occurred", e);
            throw new RequestFailedException(MessageType.Error, "An exception was thrown when running the map:\n" + e);
        } finally {
            if (gui.getErrorCount() == 0) {
                gui.sendFinished();
            }
        }
        return "ok"; // TODO
    }

    @Nullable
    private String compileMap(ModelManager modelManager, WurstGui gui, WurstProjectConfigData projectConfig) throws Exception {
        if (map.isPresent() && !map.get().exists()) {
            throw new RequestFailedException(MessageType.Error, map.get().getAbsolutePath() + " does not exist.");
        }

        gui.sendProgress("Copying map");

        // first we copy in same location to ensure validity
        File buildDir = getBuildDir();
        if (map.isPresent()) {
            mapLastModified = getSourceMapLastModified(map.get());
            mapPath = map.get().getAbsolutePath();
        }
        if (!runArgs.isHotReload()) {
            File cacheTarget = ensureWritableTargetFile(
                getCachedMapFile(),
                "Run Map",
                "The cached run map file is in use and cannot be updated.\nClose Warcraft III and click Retry, choose Rename to use a temporary file name, or Cancel.",
                "Run canceled because the cached map file is in use."
            );
            cachedMapFileName = cacheTarget.getName();
        }
        Optional<File> testMap = map.map($ -> new File(buildDir, "WurstRunMap.w3x"));
        CompilationResult result = compileScript(modelManager, gui, testMap, projectConfig, buildDir, false);

        if (runArgs.isHotReload()) {
            // call jhcr update
            gui.sendProgress("Calling JHCR update");
            callJhcrUpdate(result.script);

            // if we are just reloading the mapscript with JHCR, we are done here
            gui.sendProgress("update complete");
            return "ok";
        }


        if (testMap.isPresent()) {
            startGame(gui, result);
        }
        return null;
    }

    private void startGame(WurstGui gui, CompilationResult result) throws Exception {
        Optional<File> cachedMapFile = Optional.ofNullable(getCachedMapFile());
        injectMapData(gui, cachedMapFile, result);

        timeTaker.beginPhase("Starting Warcraft 3");
        gui.sendProgress("Starting Warcraft 3...");

        W3InstallationData launchData = resolveLaunchData();
        if (launchData == null) {
            throw new RequestFailedException(MessageType.Info, "Run canceled.");
        }

        // The auto-detected installation may be stale/incompatible (e.g. an old root launcher stub that the OS
        // refuses to start). When launching fails, surface an actionable message and let the user pick a different
        // Warcraft III folder, then retry with that selection (which also re-decides map placement).
        while (true) {
            try {
                launchGame(gui, launchData, cachedMapFile.get());
                timeTaker.endPhase();
                timeTaker.printReport();
                return;
            } catch (IOException e) {
                WLogger.warning("Could not launch Warcraft III", e);
                launchData = resolveLaunchData(recoverFromLaunchFailure(launchData, e));
                if (launchData == null) {
                    throw new RequestFailedException(MessageType.Info, "Run canceled.");
                }
            }
        }
    }

    private void launchGame(WurstGui gui, W3InstallationData launchData, File cachedMapFile) throws IOException {
        Optional<GameVersion> detectedGameVersion = launchData.getWc3PatchVersion();
        File mapCopy = cachedMapFile;
        if (buildConfig.shouldCopyRunMapToWarcraftMapDir(detectedGameVersion)) {
            mapCopy = copyToWarcraftMapDir(cachedMapFile, launchData);
        }

        WLogger.info("Starting wc3 ... ");
        String path = "";
        if (customTarget != null) {
            path = new File(customTarget, cachedMapFile.getName()).getAbsolutePath();
        } else if (mapCopy != null) {
            path = mapCopy.getAbsolutePath();
        }

        if (path.isEmpty()) {
            return;
        }

        // now start the map
        File gameExe = launchData.getGameExe()
            .orElseThrow(() -> new RequestFailedException(MessageType.Error, wc3Path + " does not exist."));
        List<String> cmd = buildLaunchCommand(gameExe, path, detectedGameVersion);

        gui.sendProgress("running " + cmd);
        Runtime.getRuntime().exec(cmd.toArray(new String[0]));
    }

    private List<String> buildLaunchCommand(File gameExe, String mapPath, Optional<GameVersion> detectedGameVersion) {
        List<String> cmd = Lists.newArrayList(gameExe.getAbsolutePath());
        Optional<String> wc3RunArgs = langServer.getConfigProvider().getWc3RunArgs();
        if (!wc3RunArgs.isPresent() || StringUtils.isBlank(wc3RunArgs.get())) {
            if (buildConfig.shouldUseReforgedLaunchArgs(detectedGameVersion)) {
                cmd.add("-launch");
            }
            if (buildConfig.shouldUseClassicWindowArg(detectedGameVersion)) {
                cmd.add("-window");
            } else {
                cmd.add("-windowmode");
                cmd.add("windowed");
            }
        } else {
            cmd.addAll(Arrays.asList(wc3RunArgs.get().split("\\s+")));
        }
        cmd.add("-loadfile");
        cmd.add(mapPath);

        if (Orient.isLinuxSystem()) {
            // run with wine
            cmd.add(0, "wine");
        }
        return cmd;
    }

    /**
     * Turns a launch failure into something the user can act on. In interactive mode the user may pick a different
     * Warcraft III folder to retry; otherwise (headless/CLI) a clear error is raised. Cancelling stops the run quietly.
     */
    private W3InstallationData recoverFromLaunchFailure(W3InstallationData launchData, IOException failure) {
        String message = launchFailureMessage(launchData.getGameExe().orElse(null), failure);
        WLogger.warning(message);
        if (GraphicsEnvironment.isHeadless()) {
            throw new RequestFailedException(MessageType.Error, message);
        }
        Object[] options = {"Choose Warcraft III folder", "Cancel"};
        int result = JOptionPane.showOptionDialog(
            null,
            message,
            "Could not launch Warcraft III",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            options,
            options[0]
        );
        if (result != 0) {
            throw new RequestFailedException(MessageType.Info, "Run canceled.");
        }
        return chooseAlternateGamePath()
            .orElseThrow(() -> new RequestFailedException(MessageType.Info, "Run canceled."));
    }

    static String launchFailureMessage(@Nullable File gameExe, IOException failure) {
        String exeText = gameExe != null ? gameExe.getAbsolutePath() : "the auto-detected Warcraft III executable";
        StringBuilder message = new StringBuilder();
        message.append("Could not launch Warcraft III.\n\n");
        message.append("Auto-detected executable: ").append(exeText).append("\n");
        String reason = failure.getMessage();
        if (reason != null && !reason.isBlank()) {
            message.append("The operating system refused to start it: ").append(reason.trim()).append("\n");
        }
        message.append("\nThis usually means an outdated or incompatible Warcraft III installation was auto-detected. ");
        message.append("Select a different Warcraft III folder, set \"wc3path\" in your Wurst settings, ");
        message.append("or pin \"wc3Patch\" in wurst.build.");
        return message.toString();
    }

    private W3InstallationData resolveLaunchData() {
        return resolveLaunchData(w3data);
    }

    @Nullable
    protected W3InstallationData resolveLaunchData(W3InstallationData launchData) {
        // When the project pins a wc3Patch, only auto-launch an install we can confirm is patch-compliant.
        // A mismatching OR unverifiable client is delegated to the user instead of "randomly" launching and failing.
        while (!isLaunchDataPatchCompliant(launchData)) {
            String message = patchComplianceMessage(launchData);
            WLogger.warning(message);

            MismatchChoice choice = chooseMismatchAction(message);
            if (choice == MismatchChoice.CANCEL) {
                return null;
            }
            if (choice == MismatchChoice.CONTINUE) {
                return launchData;
            }
            Optional<W3InstallationData> selected = chooseAlternateGamePath();
            if (selected.isEmpty()) {
                return null;
            }
            launchData = selected.get();
        }
        return launchData;
    }

    private boolean isLaunchDataPatchCompliant(W3InstallationData launchData) {
        return isPatchCompliant(buildConfig.wc3Patch(), launchData.getWc3PatchVersion());
    }

    /**
     * A launch install is patch-compliant when the project pins no patch (nothing to validate against), or the
     * detected client version is known and belongs to the same patch family as the pinned {@code wc3Patch}. An
     * unknown client version against a pinned patch is treated as non-compliant so the user is asked to choose,
     * rather than launching a client that may silently fail to start.
     */
    static boolean isPatchCompliant(Optional<WurstBuildConfig.Wc3Patch> projectKind, Optional<GameVersion> clientVersion) {
        if (projectKind.isEmpty()) {
            return true;
        }
        if (clientVersion.isEmpty()) {
            return false;
        }
        return projectKind.get() == kindForVersion(clientVersion.get());
    }

    private String patchComplianceMessage(W3InstallationData launchData) {
        String projectTarget = buildConfig.wc3PatchName().orElse("configured patch");
        Optional<GameVersion> clientVersion = launchData.getWc3PatchVersion();
        if (clientVersion.isPresent()) {
            return "This project targets " + projectTarget + ", but the selected Warcraft III client looks like "
                + describeClientVersion(clientVersion.get()) + ".\n\n"
                + "Choose a matching Warcraft III folder, or set \"wc3path\".";
        }
        String exe = launchData.getGameExe().map(File::getAbsolutePath).orElse("the auto-detected installation");
        return "This project targets " + projectTarget + ", but the version of the auto-detected Warcraft III could not "
            + "be determined:\n" + exe + "\n\n"
            + "Choose your Warcraft III folder (or set \"wc3path\") so a matching client is used, "
            + "instead of launching one that may not start.";
    }

    private static WurstBuildConfig.Wc3Patch kindForVersion(GameVersion version) {
        if (version.compareTo(new GameVersion("1.29")) < 0) {
            return WurstBuildConfig.Wc3Patch.PRE_129;
        }
        if (version.compareTo(GameVersion.VERSION_1_32) < 0) {
            return WurstBuildConfig.Wc3Patch.CLASSIC;
        }
        return WurstBuildConfig.Wc3Patch.REFORGED;
    }

    private static String describeClientVersion(GameVersion version) {
        return kindForVersion(version) + " (" + version + ")";
    }

    protected MismatchChoice chooseMismatchAction(String message) {
        if (GraphicsEnvironment.isHeadless()) {
            return MismatchChoice.CONTINUE;
        }
        Object[] options = {"Continue", "Choose Warcraft III folder", "Cancel"};
        int result = JOptionPane.showOptionDialog(
            null,
            message,
            "Warcraft III version mismatch",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            options[1]
        );
        if (result == 1) {
            return MismatchChoice.CHOOSE_OTHER;
        }
        if (result == 2 || result == JOptionPane.CLOSED_OPTION) {
            return MismatchChoice.CANCEL;
        }
        return MismatchChoice.CONTINUE;
    }

    protected Optional<W3InstallationData> chooseAlternateGamePath() {
        if (GraphicsEnvironment.isHeadless()) {
            return Optional.empty();
        }
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select Warcraft III installation folder");
        int result = fileChooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }
        File selectedFolder = fileChooser.getSelectedFile();
        return Optional.of(new W3InstallationData(langServer, selectedFolder, true, Optional.empty()));
    }

    protected enum MismatchChoice {
        CONTINUE,
        CHOOSE_OTHER,
        CANCEL
    }


    private void callJhcrUpdate(File mapScript) throws IOException, InterruptedException {
        File mapScriptFolder = mapScript.getParentFile();
        File commonJ = new File(mapScriptFolder, "common.j");
        File blizzardJ = new File(mapScriptFolder, "blizzard.j");
        if (!commonJ.exists()) {
            throw new IOException("Could not find file " + commonJ.getAbsolutePath());
        }

        if (!blizzardJ.exists()) {
            throw new IOException("Could not find file " + blizzardJ.getAbsolutePath());
        }

        Path customMapDataPath = getCustomMapDataPath();

        ProcessBuilder pb = new ProcessBuilder(langServer.getConfigProvider().getJhcrExe(), "update", mapScript.getName(), "--asm",
            "--preload-path", customMapDataPath.toAbsolutePath().toString());
        pb.directory(mapScriptFolder);
        Utils.ExecResult $ = Utils.exec(pb, Duration.ofSeconds(30), System.err::println);
    }

    /**
     * Tries to find the path where the wc3 CustomMapData is stored.
     * For example this could be in:
     * C:\\Users\\Peter\\Documents\\Warcraft III\\CustomMapData
     */
    private Path getCustomMapDataPath() {
        String customMapDataPath = langServer.getConfigProvider().getConfig("customMapDataPath", "");
        if (!customMapDataPath.isEmpty()) {
            return Paths.get(customMapDataPath);
        }

        if (Orient.isMacSystem()) {
            return Paths.get(System.getProperty("user.home"),
                "Library", "Application Support", "Blizzard", "Warcraft III", "CustomMapData");
        }

        Path documents;
        try {
            documents = FileSystemView.getFileSystemView().getDefaultDirectory().toPath();
        } catch (Throwable t) {
            WLogger.info(t);
            Path homeFolder = Paths.get(System.getProperty("user.home"));
            documents = homeFolder.resolve("Documents");
        }
        return documents.resolve(Paths.get("Warcraft III", "CustomMapData"));
    }

    @NotNull
    private String getWorkspaceAbsolute() {
        try {
            return workspaceRoot.getFile().getAbsolutePath();
        } catch (FileNotFoundException e) {
            throw new RequestFailedException(MessageType.Error, "Could not open workspace root: ", e);
        }
    }

    /**
     * Copies the map to the wc3 map directory
     * <p>
     * This directory depends on warcraft version and whether we are on windows or wine is used.
     */
    private File copyToWarcraftMapDir(File testMap, W3InstallationData launchData) throws IOException {
        String testMapName = "WurstTestMap.w3x";
        for (String arg : compileArgs) {
            if (arg.startsWith("-runmapTarget")) {
                String path = arg.substring(arg.indexOf(" ") + 1);
                // copy the map to the specified directory
                customTarget = new File(path);
                if (customTarget.exists() && customTarget.isDirectory()) {
                    File testMap2 = new File(customTarget, testMapName);
                    Files.copy(testMap, testMap2);
                    return testMap2;
                } else {
                    WLogger.severe("Directory specified via -runmapTarget does not exists or is not a directory");
                }
            }
        }

        File myDocumentsFolder = FileSystemView.getFileSystemView().getDefaultDirectory();
        Optional<String> documentPath = findMapDocumentPath(testMapName, myDocumentsFolder, launchData);

        // copy the map to the appropriate directory
        Optional<File> testFolder = documentPath.map(path -> new File(path, "Maps" + File.separator + "Test"));
        if (testFolder.isPresent() && (testFolder.get().mkdirs() || testFolder.get().exists())) {
            File testMap2 = new File(testFolder.get(), testMapName);
            while (true) {
                try {
                    Files.copy(testMap, testMap2);
                    break;
                } catch (IOException ex) {
                    JFrame jf = new JFrame();
                    jf.setAlwaysOnTop(true);
                    Object[] options = { "Retry", "Rename", "Cancel" };
                    int result = JOptionPane.showOptionDialog(jf, "Can't write to target map file, it's probably in use.",
                        "Run Map", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                        null, options, null);
                    if (result == JOptionPane.CANCEL_OPTION) {
                        return null;
                    } else if(result == JOptionPane.NO_OPTION) {
                        testMap2 = new File(testFolder.get(), testMapName + RandomStringUtils.randomNumeric(3));
                    }
                }

            }

            return testMap2;
        } else {
            WLogger.severe("Could not create Test folder");
        }
        return null;
    }

    private Optional<String> findMapDocumentPath(String testMapName, File myDocumentsFolder, W3InstallationData launchData) {
        Optional<String> documentPath = Optional.of(
            langServer.getConfigProvider().getMapDocumentPath().orElseGet(
                () -> myDocumentsFolder.getAbsolutePath() + File.separator + "Warcraft III"));

        if (!new File(documentPath.get()).exists()) {
            WLogger.info("Warcraft folder " + documentPath + " does not exist.");
            if (Orient.isMacSystem()) {
                // macOS 1.29+: ~/Library/Application Support/Blizzard/Warcraft III
                documentPath = Optional.of(System.getProperty("user.home")
                    + "/Library/Application Support/Blizzard/Warcraft III");
            } else {
                // Linux: try Wine default path
                documentPath = Optional.of(System.getProperty("user.home")
                    + "/.wine/drive_c/users/" + System.getProperty("user.name") + "/My Documents/Warcraft III");
            }
            if (!new File(documentPath.get()).exists()) {
                WLogger.severe("Severe: Warcraft folder " + documentPath + " does not exist.");
            }
        }

        if (buildConfig.shouldUseInstallDirForMaps(launchData.getWc3PatchVersion())) {
            // 1.27 and lower compat
            WLogger.info("Version 1.27 or lower detected, changing file location");
            documentPath = mapInstallDirectoryForLegacyLaunch(launchData, wc3Path);
        } else {
            // For 1.28+ the wc3/maps/test folder must not contain a map of the same name
            Optional<File> oldFile = installRootForLaunchData(launchData).map(File::getAbsolutePath).or(() -> wc3Path).map(
                w3p -> new File(w3p, "Maps" + File.separator + "Test" + File.separator + testMapName));
            if (oldFile.isPresent() && oldFile.get().exists()) {
                if (!oldFile.get().delete()) {
                    WLogger.severe("Cannot delete old Wurst Test Map");
                }
            }
        }
        return documentPath;
    }

    private static Optional<String> mapInstallDirectoryForLegacyLaunch(W3InstallationData launchData, Optional<String> fallbackWc3Path) {
        Optional<File> launchRoot = installRootForLaunchData(launchData);
        if (launchRoot.isPresent()) {
            return launchRoot.map(File::getAbsolutePath);
        }
        return fallbackWc3Path;
    }

    private static Optional<File> installRootForLaunchData(W3InstallationData launchData) {
        return launchData.getGameExe().map(RunMap::installRootForExecutable);
    }

    private static File installRootForExecutable(File executable) {
        File parent = executable.getAbsoluteFile().getParentFile();
        if (parent == null) {
            return executable.getAbsoluteFile();
        }
        if (parent.getName().equalsIgnoreCase("x86") || parent.getName().equalsIgnoreCase("x86_64")) {
            File maybeRetail = parent.getParentFile();
            if (maybeRetail != null && (maybeRetail.getName().equalsIgnoreCase("_retail_") || maybeRetail.getName().equalsIgnoreCase("_ptr_"))) {
                File installRoot = maybeRetail.getParentFile();
                return installRoot != null ? installRoot : maybeRetail;
            }
            return maybeRetail != null ? maybeRetail : parent;
        }
        return parent;
    }
}
