package de.peeeq.wurstio;

import org.wurstscript.projectconfig.WurstProjectConfigData;
import org.wurstscript.projectconfig.WurstProjectConfigReader;
import de.peeeq.wurstio.compilationserver.WurstServer;
import de.peeeq.wurstio.gui.AboutDialog;
import de.peeeq.wurstio.gui.WurstGuiImpl;
import de.peeeq.wurstio.languageserver.LanguageServerStarter;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.requests.CliBuildMap;
import de.peeeq.wurstio.map.importer.ImportFile;
import de.peeeq.wurstio.objectreader.ObjectExportService;
import de.peeeq.wurstscript.CompileTimeInfo;
import de.peeeq.wurstscript.ErrorReporting;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.prettyPrint.PrettyUtils;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.*;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.peeeq.wurstio.languageserver.ProjectConfigBuilder.FILE_NAME;
import static de.peeeq.wurstio.languageserver.WurstCommands.getCompileArgs;
import static java.util.Arrays.asList;

public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            // If no args are passed, display help
            new RunArgs("-help");
            return;
        }

        WurstGui gui = null;
        RunArgs runArgs = new RunArgs(args);
        try {
            if (runArgs.isLanguageServer()) {
                WLogger.setLogger("languageServer");
            }
            logStartup(args);

            if (runArgs.showHelp()) {
                return;
            }

            if (runArgs.isShowVersion()) {
                System.out.println(CompileTimeInfo.version);
                return;
            }

            if (runArgs.showAbout()) {
                new AboutDialog(null, false).setVisible(true);
                return;
            }

            if (runArgs.isStartServer()) {
                WurstServer.startServer();
                return;
            }

            if (runArgs.isLanguageServerAppCdsTrain()) {
                LanguageServerStarter.trainForAppCds();
                return;
            }

            if (runArgs.isLanguageServer()) {
                LanguageServerStarter.start();
                return;
            }

            if (runArgs.isPrettyPrint()) {
                PrettyUtils.pretty(runArgs.getFiles());
                return;
            }

            WLogger.info("runArgs.isExtractImports() = " + runArgs.isExtractImports());
            if (runArgs.isExtractImports()) {
                File mapFile = new File(runArgs.getMapFile());
                ImportFile.extractImportsFromMap(mapFile, runArgs);
                return;
            }

            if (runArgs.isExportObjects()) {
                String exportObjectsFile = runArgs.getExportObjectsFile();
                if (exportObjectsFile == null || exportObjectsFile.isBlank()) {
                    throw new RuntimeException("-exportobjects requires a map file or map folder.");
                }
                List<Path> written = ObjectExportService.exportObjects(
                    new File(exportObjectsFile),
                    runArgs.getExportObjectsOut().map(File::new)
                );
                if (written.isEmpty()) {
                    System.out.println("No object editor data found.");
                } else {
                    System.out.println("Exported object editor data:");
                    for (Path file : written) {
                        System.out.println(file.toAbsolutePath());
                    }
                }
                return;
            }

            if (runArgs.isGui()) {
                gui = new WurstGuiImpl();
                // use the error reporting with GUI
                ErrorReporting.instance = new ErrorReportingIO();
            } else {
                gui = new WurstGuiCliImpl(runArgs.isCompactOutput());
            }

            if (runArgs.showLastErrors()) {
                JOptionPane.showMessageDialog(null, "not implemented");
                return;
            }

            try {
                String workspaceroot = runArgs.getWorkspaceroot();
                RunArgs compileArgs = runArgs;
                List<String> mergedArgs = new ArrayList<>(asList(args));
                if (workspaceroot != null) {
                    WLogger.info("workspaceroot: " + workspaceroot);
                    List<String> argsList = getCompileArgs(WFile.create(workspaceroot), runArgs.isBuild());
                    WLogger.info("workspaceroot: " + (argsList == null));
                    mergedArgs.addAll(argsList);
                    compileArgs = new RunArgs(mergedArgs);
                }

                if (workspaceroot != null) {
                    Path root = Paths.get(workspaceroot);
                    Path inputMap = runArgs.isBuild() && runArgs.getInputmap() != null
                        ? root.resolve(runArgs.getInputmap())
                        : runArgs.getMapFile() == null ? null : Paths.get(runArgs.getMapFile());
                    WurstProjectConfigData projectConfig = WurstProjectConfigReader.load(root.resolve(FILE_NAME));
                    if (inputMap != null) {
                        if (!java.nio.file.Files.exists(inputMap)) {
                            throw new RuntimeException("Input map does not exist: " + inputMap);
                        }
                        if (projectConfig == null) {
                            throw new RuntimeException(FILE_NAME + " file doesn't exist or is invalid.");
                        }
                        CliBuildMap cliBuildMap = new CliBuildMap(
                            WFile.create(root.toFile()),
                            Optional.of(inputMap.toFile()),
                            mergedArgs,
                            Optional.empty(),
                            gui
                        );
                        de.peeeq.wurstio.languageserver.ModelManager modelManager =
                            new de.peeeq.wurstio.languageserver.ModelManagerImpl(root.toFile(), new de.peeeq.wurstio.languageserver.BufferManager());
                        modelManager.buildProject();
                        Object result = cliBuildMap.execute(modelManager);
                        WLogger.info("map build success");
                        System.out.println("Build succeeded. Output file: <" + result + ">");
                        gui.sendProgress("Finished!");
                        return;
                    }
                }

                CompilationProcess compilationProcess = new CompilationProcess(gui, compileArgs);
                @Nullable CharSequence compiledScript;

                compiledScript = compilationProcess.doCompilation(null, true);

                if (compiledScript != null) {
                    File scriptFile = new File(compileArgs.isLua() ? "compiled.lua.txt" : "compiled.j.txt");
                    java.nio.file.Files.writeString(scriptFile.toPath(), compiledScript);
                }

                gui.sendProgress("Finished!");
            } catch (AbortCompilationException e) {
                gui.showInfoMessage(e.getMessage());
            }
        } catch (Throwable t) {
            String source = "";
            // TODO add additional information to source
            ErrorReporting.instance.handleSevere(t, source);
            if (!runArgs.isGui()) {
                System.exit(2);
            }
        } finally {
            if (gui != null) {
                gui.sendFinished();
                if (!runArgs.isGui()) {
                    if (runArgs.isCompactOutput()) {
                        printCompactMessages(gui);
                    } else {
                        if (gui.getErrorCount() > 0) {
                            // 	print error messages
                            for (CompileError err : gui.getErrorList()) {
                                System.out.println(err);
                            }
                            // signal that there was an error when compiling
                            System.exit(1);
                        }
                        // print warnings:
                        for (CompileError err : gui.getWarningList()) {
                            System.out.println(err);
                        }
                    }
                    if (gui.getErrorCount() > 0) {
                        System.exit(1);
                    }
                }
            }
        }
    }

    private static void printCompactMessages(WurstGui gui) {
        if (gui.getErrorCount() > 0) {
            System.out.println("Errors: " + gui.getErrorCount());
            for (CompileError err : gui.getErrorList()) {
                System.out.println(err.toCompactString());
            }
        }
        if (!gui.getWarningList().isEmpty()) {
            Map<String, Integer> warningsByFile = new LinkedHashMap<>();
            for (CompileError err : gui.getWarningList()) {
                String file = new File(err.getSource().getFile()).getName();
                warningsByFile.put(file, warningsByFile.getOrDefault(file, 0) + 1);
            }
            System.out.println("Warnings: " + gui.getWarningList().size());
            for (Map.Entry<String, Integer> entry : warningsByFile.entrySet()) {
                System.out.println("Warning " + entry.getKey() + ": " + entry.getValue());
            }
        }
    }

    private static void logStartup(String[] args) {
        // VM Arguments
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();

        WLogger.info("### Started wurst version: (" + AboutDialog.version + ")");
        WLogger.info("### With wurst-args " + Utils.printSep(", ", args));
        if (arguments != null && arguments.size() > 0) {
            WLogger.info("### With vm-args " + Utils.printSep(", ", arguments.toArray(new String[0])));
        }
        try {
            WLogger.info("### compiler path1: " + Main.class.getProtectionDomain().getCodeSource().getLocation());
            WLogger.info("### compiler path2: " + ClassLoader.getSystemClassLoader().getResource(".").getPath());
        } catch (Throwable ignored) {
        }
        WLogger.info("### ============================================");
    }

}
