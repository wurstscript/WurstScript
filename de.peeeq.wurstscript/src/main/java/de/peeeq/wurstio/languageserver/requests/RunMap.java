package de.peeeq.wurstio.languageserver.requests;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import de.peeeq.wurstio.gui.WurstGuiImpl;
import de.peeeq.wurstio.languageserver.ConfigProvider;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstio.utils.W3Utils;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.utils.WinRegistry;
import net.moonlightflower.wc3libs.bin.GameExe;
import org.eclipse.lsp4j.MessageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static net.moonlightflower.wc3libs.bin.GameExe.VERSION_1_29;

/**
 * Created by peter on 16.05.16.
 */
public class RunMap extends MapRequest {
    private final @Nullable
    String wc3Path;
    private File customTarget = null;


    public RunMap(ConfigProvider configProvider, WFile workspaceRoot, @Nullable String wc3Path, @Nullable File map, List<String> compileArgs) {
        super(configProvider, map, compileArgs, workspaceRoot);
        this.wc3Path = wc3Path;
    }

    @Override
    public Object execute(ModelManager modelManager) {
        WLogger.info("Execute RunMap, \nwc3Path =" + wc3Path
               + ",\n map = " + map
                + ",\n compileArgs = " + compileArgs
                + ",\n workspaceRoot = " + workspaceRoot
                + ",\n runArgs = " + runArgs
        );

        if (modelManager.hasErrors()) {
            throw new RequestFailedException(MessageType.Error, "Fix errors in your code before running.");
        }

        // TODO use normal compiler for this, avoid code duplication
        WLogger.info("received runMap command: map=" + map + ", wc3dir=" + wc3Path + ", args=" + compileArgs);
        WurstGui gui = new WurstGuiImpl(getWorkspaceAbsolute());
        try {
            if (wc3Path != null) {
                W3Utils.parsePatchVersion(new File(wc3Path));
            }

            if (map != null && !map.exists()) {
                throw new RequestFailedException(MessageType.Error, map.getAbsolutePath() + " does not exist.");
            }

            gui.sendProgress("Copying map");

            // first we copy in same location to ensure validity
            File buildDir = getBuildDir();
            File testMap = map == null ? null : new File(buildDir, "WurstRunMap.w3x");
            File compiledScript = compileScript(modelManager, gui, testMap);

            if (runArgs.isHotReload()) {
                // call jhcr update
                gui.sendProgress("Calling JHCR update");
                callJhcrUpdate(compiledScript);
            }


            if (runArgs.isHotReload()) {
                // if we are just reloading the mapscript with JHCR, we are done here
                gui.sendProgress("update complete");
                return "ok";
            }


            if (testMap != null) {
                gui.sendProgress("preparing testmap ... ");

                // then inject the script into the map
                gui.sendProgress("Injecting mapscript");
                try (MpqEditor mpqEditor = MpqEditorFactory.getEditor(testMap)) {
                    mpqEditor.deleteFile("war3map.j");
                    mpqEditor.insertFile("war3map.j", compiledScript);
                }


                File mapCopy = copyToWarcraftMapDir(testMap);

                WLogger.info("Starting wc3 ... ");
                String path = "";
                if (customTarget != null) {
                    path = new File(customTarget, testMap.getName()).getAbsolutePath();
                } else if (mapCopy != null) {
                    path = mapCopy.getAbsolutePath();
                }


                if (path.length() > 0) {
                    // now start the map
                    File gameExe = findGameExecutable();
                    if (W3Utils.getWc3PatchVersion() == null) {
                        throw new RequestFailedException(MessageType.Error, wc3Path + " does not exist.");
                    }
                    List<String> cmd = Lists.newArrayList(gameExe.getAbsolutePath(), "-window", "-loadfile", path);

                    if (!System.getProperty("os.name").startsWith("Windows")) {
                        // run with wine
                        cmd.add(0, "wine");
                    }

                    gui.sendProgress("running " + cmd);
                    Process p = Runtime.getRuntime().exec(cmd.toArray(new String[0]));
                }
            }
        } catch (CompileError e) {
            throw new RequestFailedException(MessageType.Error, "There was an error when compiling the map: " + e.getMessage());
        } catch (RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (gui.getErrorCount() == 0) {
                gui.sendFinished();
            }
        }
        return "ok"; // TODO
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

        ProcessBuilder pb = new ProcessBuilder(configProvider.getJhcrExe(), "update", mapScript.getName(), "--asm",
                "--preload-path", customMapDataPath.toAbsolutePath().toString());
        pb.directory(mapScriptFolder);
        Utils.ExecResult result = Utils.exec(pb, Duration.ofSeconds(30), System.err::println);
    }

    /**
     * Tries to find the path where the wc3 CustomMapData is stored.
     * For example this could be in:
     * C:\\Users\\Peter\\Documents\\Warcraft III\\CustomMapData
     */
    private Path getCustomMapDataPath() {
        String customMapDataPath = configProvider.getConfig("customMapDataPath", "");
        if (!customMapDataPath.isEmpty()) {
            return Paths.get(customMapDataPath);
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
     * Returns the executable for Warcraft III for starting maps
     * since it changed with 1.28.3
     */
    private File findGameExecutable() {
        return (W3Utils.getWc3PatchVersion().compareTo(VERSION_1_29) < 0 ? Stream.of("war3.exe", "War3.exe", "WAR3.EXE", "Warcraft III.exe", "Frozen Throne.exe") :
                Stream.of("Warcraft III.exe", "Frozen Throne.exe"))
                .map(exe -> new File(wc3Path, exe))
                .filter(File::exists)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No warcraft executatble found in path '" + wc3Path + "'. \n" +
                        "Please check your configuration."));
    }

    /**
     * Copies the map to the wc3 map directory
     * <p>
     * This directory depends on warcraft version and whether we are on windows or wine is used.
     */
    private File copyToWarcraftMapDir(File testMap) throws IOException {
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
        String documentPath = myDocumentsFolder.getAbsolutePath() + File.separator + "Warcraft III";
        if (!new File(documentPath).exists()) {
            WLogger.info("Warcraft folder " + documentPath + " does not exist.");
            // Try wine default:
            documentPath = System.getProperty("user.home")
                    + "/.wine/drive_c/users/" + System.getProperty("user.name") + "/My Documents/Warcraft III";
            if (!new File(documentPath).exists()) {
                WLogger.severe("Severe: Wine Warcraft folder " + documentPath + " does not exist.");
            }
        }


        if (W3Utils.getWc3PatchVersion().compareTo(new GameExe.Version("1.27.9")) <= 0) {
            // 1.27 and lower compat
            WLogger.info("Version 1.27 or lower detected, changing file location");
            documentPath = wc3Path;
        } else {
            // For 1.28+ the wc3/maps/test folder must not contain a map of the same name
            File oldFile = new File(wc3Path, "Maps" + File.separator + "Test" + File.separator + testMapName);
            if (oldFile.exists()) {
                if (!oldFile.delete()) {
                    WLogger.severe("Cannot delete old Wurst Test Map");
                }
            }
        }

        // copy the map to the appropriate directory
        File testFolder = new File(documentPath, "Maps" + File.separator + "Test");
        if (testFolder.mkdirs() || testFolder.exists()) {
            File testMap2 = new File(testFolder, testMapName);
            Files.copy(testMap, testMap2);
            return testMap2;
        } else {
            WLogger.severe("Could not create Test folder");
        }
        return null;
    }


}
