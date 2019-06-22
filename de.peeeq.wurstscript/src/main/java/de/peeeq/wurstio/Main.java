package de.peeeq.wurstio;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import config.WurstProjectConfig;
import config.WurstProjectConfigData;
import de.peeeq.wurstio.compilationserver.WurstServer;
import de.peeeq.wurstio.gui.AboutDialog;
import de.peeeq.wurstio.gui.WurstGuiImpl;
import de.peeeq.wurstio.hotdoc.HotdocGenerator;
import de.peeeq.wurstio.languageserver.LanguageServerStarter;
import de.peeeq.wurstio.languageserver.ProjectConfigBuilder;
import de.peeeq.wurstio.map.importer.ImportFile;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.*;
import de.peeeq.wurstscript.attributes.CompileError;
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
import java.nio.file.StandardCopyOption;
import java.util.List;

import static de.peeeq.wurstio.languageserver.ProjectConfigBuilder.FILE_NAME;

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

            if (runArgs.isLanguageServer()) {
                LanguageServerStarter.start();
                return;
            }

            WLogger.info("runArgs.isExtractImports() = " + runArgs.isExtractImports());
            if (runArgs.isExtractImports()) {
                File mapFile = new File(runArgs.getMapFile());
                ImportFile.extractImportsFromMap(mapFile, runArgs);
                return;
            }

            if (runArgs.createHotDoc()) {
                HotdocGenerator hg = new HotdocGenerator(runArgs.getFiles());
                hg.generateDoc();
                return;
            }

            if (runArgs.isGui()) {
                gui = new WurstGuiImpl();
                // use the error reporting with GUI
                ErrorReporting.instance = new ErrorReportingIO();
            } else {
                gui = new WurstGuiCliImpl();
            }

            if (runArgs.showLastErrors()) {
                JOptionPane.showMessageDialog(null, "not implemented");
                return;
            }

            try {
                WurstProjectConfigData projectConfig = null;
                Path buildDir = null;
                Path target = null;
                if (runArgs.isBuild() && runArgs.getInputmap() != null && runArgs.getWorkspaceroot() != null) {
                    Path root = Paths.get(runArgs.getWorkspaceroot());
                    Path inputMap = root.resolve(runArgs.getInputmap());
                    projectConfig = WurstProjectConfig.INSTANCE.loadProject(root.resolve(FILE_NAME));

                    if (java.nio.file.Files.exists(inputMap) && projectConfig != null) {
                        buildDir = root.resolve("_build");
                        java.nio.file.Files.createDirectories(buildDir);
                        target = buildDir.resolve(projectConfig.getBuildMapData().getFileName() + ".w3x");
                        java.nio.file.Files.copy(inputMap, target, StandardCopyOption.REPLACE_EXISTING);
                        runArgs.setMapFile(target.toAbsolutePath().toString());
                    }
                }

                String mapFilePath = runArgs.getMapFile();
                if (mapFilePath != null) {
                    // tempfolder
                    File tempFolder = new File("./temp/");
                    tempFolder.mkdirs();
                    BackupController bc = new BackupController();
                    bc.makeBackup(mapFilePath);
                }

                CompilationProcess compilationProcess = new CompilationProcess(gui, runArgs);
                @Nullable CharSequence compiledScript;

                if (mapFilePath != null) {
                    try (MpqEditor mpqEditor = MpqEditorFactory.getEditor(new File(mapFilePath))) {
                        compiledScript = compilationProcess.doCompilation(mpqEditor);
                        if (compiledScript != null) {
                            gui.sendProgress("Writing to map");
                            mpqEditor.deleteFile("war3map.j");
                            byte[] war3map = compiledScript.toString().getBytes(Charsets.UTF_8);
                            mpqEditor.insertFile("war3map.j", war3map);
                        }
                    }
                } else {
                    compiledScript = compilationProcess.doCompilation(null);
                }

                if (compiledScript != null) {
                    File scriptFile = new File("compiled.j.txt");
                    Files.write(compiledScript.toString().getBytes(Charsets.UTF_8), scriptFile);

                    if (projectConfig != null && target != null) {
                        ProjectConfigBuilder.apply(projectConfig, target.toFile(), scriptFile, buildDir.toFile(), runArgs);

                        WLogger.info("map build success");
                        System.out.println("Build succeeded. Output file: <" + target.toAbsolutePath() + ">");
                    }
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
