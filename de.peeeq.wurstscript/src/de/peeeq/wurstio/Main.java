package de.peeeq.wurstio;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import javax.swing.JOptionPane;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import de.peeeq.wurstio.Pjass.Result;
import de.peeeq.wurstio.compilationserver.WurstServer;
import de.peeeq.wurstio.gui.About;
import de.peeeq.wurstio.gui.WurstGuiImpl;
import de.peeeq.wurstio.hotdoc.HotdocGenerator;
import de.peeeq.wurstio.languageserver.LanguageServer;
import de.peeeq.wurstio.map.importer.ImportFile;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.BackupController;
import de.peeeq.wurstscript.ErrorReporting;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.utils.WinRegistry;

public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            @SuppressWarnings("unused")
            RunArgs r = new RunArgs("-help");
            return;
        }
        setUpFileLogging();
        WLogger.keepLogs(true);

        //		JOptionPane.showMessageDialog(null , "time to connect profiler ^^");
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        Date myDate = new Date();
        WLogger.info(">>> " + sdf.format(myDate) + " - Started compiler (" + About.version + ") with args " + Utils.printSep(", ", args));
        try {
            WLogger.info("compiler path1: " + Main.class.getProtectionDomain().getCodeSource().getLocation());
            WLogger.info("compiler path2: " + ClassLoader.getSystemClassLoader().getResource(".").getPath());
        } catch (Throwable t) {
        }

        WurstGui gui = null;
        RunArgs runArgs = new RunArgs(args);
        try {
            if (runArgs.showHelp()) {
                return;
            }

            if (runArgs.showAbout()) {
                About about = new About(null, false);
                about.setVisible(true);
                return;
            }

            if (runArgs.insertKeys()) {
                insertKeys(runArgs);
                return;
            }

            if (runArgs.isStartServer()) {
                WurstServer.startServer();
                return;
            }

            if (runArgs.isLanguageServer()) {
                new LanguageServer().start();
                return;
            }

            WLogger.info("runArgs.isExtractImports() = " + runArgs.isExtractImports());
            String mapFilePath = runArgs.getMapFile();
            if (runArgs.isExtractImports()) {
                File mapFile = new File(mapFilePath);
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
                if (mapFilePath != null) {
                    // tempfolder
                    File tempFolder = new File("./temp/");
                    tempFolder.mkdirs();
                    BackupController bc = new BackupController();
                    bc.makeBackup(mapFilePath);
                }

                if (mapFilePath != null) {
                    try (MpqEditor mpqEditor = MpqEditorFactory.getEditor(new File(mapFilePath), runArgs)) {
                        CharSequence mapScript = doCompilation(gui, mpqEditor, runArgs);
                        if (mapScript != null) {
                            gui.sendProgress("Writing to map");
                            mpqEditor.deleteFile("war3map.j");
                            byte[] war3map = mapScript.toString().getBytes(Charsets.UTF_8);
                            mpqEditor.insertFile("war3map.j", war3map);
                        }
                    }
                } else {
                    doCompilation(gui, null, runArgs);
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

    private static void insertKeys(RunArgs runArgs) throws Exception {
        String wc3Path = WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, "SOFTWARE\\Blizzard Entertainment\\Warcraft III", "InstallPath");
        if(!wc3Path.endsWith("\\")) wc3Path = wc3Path + "\\";
        Path fontGID = Paths.get(Main.class.getClassLoader().getResource("font/font.gid").getPath().substring(1));
        Path fontCLH = Paths.get(Main.class.getClassLoader().getResource("font/font.clh").getPath().substring(1));
        Path fontROC = Paths.get(Main.class.getClassLoader().getResource("font/font_roc.ccd").getPath().substring(1));
        Path fontEXP = Paths.get(Main.class.getClassLoader().getResource("font/font.exp").getPath().substring(1));
        Path fontTFT = Paths.get(Main.class.getClassLoader().getResource("font/font_tft.ccd").getPath().substring(1));

        File rocMpq = new File(wc3Path + "War3.mpq");
        File tftMpq = new File(wc3Path + "War3x.mpq");
        MpqEditor roceditor = MpqEditorFactory.getEditor(rocMpq, runArgs);
        boolean rocHasKeys = roceditor.hasFile("font\\font.ccd") && roceditor.hasFile("font\\font.gid") && roceditor.hasFile("font\\font.clh");
        if (!rocHasKeys) {
            JOptionPane.showMessageDialog(null, "Wurstpack has detected a 1.28+ install of RoC and needs to fix your installation.\n"
                    + "This might take a few minutes. Please be patient.");
            roceditor.insertFile("font\\font.gid", java.nio.file.Files.readAllBytes(fontGID));
            roceditor.insertFile("font\\font.clh", java.nio.file.Files.readAllBytes(fontCLH));
            roceditor.insertFile("font\\font.ccd", java.nio.file.Files.readAllBytes(fontROC));
            roceditor.close();
            WLogger.info("inserted roc keys");
        } else {
            WLogger.info("Already has roc keys");
        }
        MpqEditor tfteditor = MpqEditorFactory.getEditor(tftMpq, runArgs);
        boolean tftHasKeys = tfteditor.hasFile("font\\font.ccd") && tfteditor.hasFile("font\\font.exp");
        if (!tftHasKeys) {
            JOptionPane.showMessageDialog(null, "Wurstpack has detected a 1.28+ install of TFT and needs to fix your installation.\n"
                    + "This might take a few minutes. Please be patient.");
            tfteditor.insertFile("font\\font.exp", java.nio.file.Files.readAllBytes(fontEXP));
            tfteditor.insertFile("font\\font.ccd", java.nio.file.Files.readAllBytes(fontTFT));
            tfteditor.close();
            WLogger.info("inserted tft keys");
        } else {
            WLogger.info("Already has tft keys");
        }
    }

    private static @Nullable CharSequence doCompilation(WurstGui gui, @Nullable MpqEditor mpqEditor, RunArgs runArgs) throws IOException {
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui, mpqEditor, runArgs);
        for (String file : runArgs.getFiles()) {
            compiler.loadFiles(file);
        }
        WurstModel model = compiler.parseFiles();

        if (gui.getErrorCount() > 0) {
            return null;
        }
        if (model == null) {
            return null;
        }

        compiler.checkProg(model);

        if (gui.getErrorCount() > 0) {
            return null;
        }

        compiler.translateProgToIm(model);

        if (gui.getErrorCount() > 0) {
            return null;
        }

        File mapFile = compiler.getMapFile();

        if (runArgs.runCompiletimeFunctions()) {
            if (mapFile == null) {
                throw new RuntimeException("mapFile must not be null when running compiletime functions");
            }
            if (mpqEditor == null) {
                throw new RuntimeException("mpqEditor must not be null when running compiletime functions");
            }
            // tests
            gui.sendProgress("Running tests");
            CompiletimeFunctionRunner ctr = new CompiletimeFunctionRunner(compiler.getImProg(), mapFile, mpqEditor, gui, FunctionFlagEnum.IS_TEST);
            ctr.run();

            // compiletime functions
            gui.sendProgress("Running compiletime functions");
            ctr = new CompiletimeFunctionRunner(compiler.getImProg(), mapFile, mpqEditor, gui, FunctionFlagEnum.IS_COMPILETIME);
            ctr.setInjectObjects(runArgs.isInjectObjects());
            ctr.run();
        }

        if (runArgs.isInjectObjects()) {
            Preconditions.checkNotNull(mpqEditor);
            // add the imports
            ImportFile.importFilesFromImportDirectory(new File(runArgs.getMapFile()), mpqEditor);
        }

        JassProg jassProg = compiler.transformProgToJass();

        if (jassProg == null || gui.getErrorCount() > 0) {
            return null;
        }

        boolean withSpace;
        if (runArgs.isOptimize()) {
            withSpace = false;
        } else {
            withSpace = true;
        }

        gui.sendProgress("Printing Jass");
        JassPrinter printer = new JassPrinter(withSpace, jassProg);
        CharSequence mapScript = printer.printProg();

        // output to file
        gui.sendProgress("Writing output file");
        File outputMapscript;
        if (runArgs.getOutFile() != null) {
            outputMapscript = new File(runArgs.getOutFile());
        } else {
            //outputMapscript = File.createTempFile("outputMapscript", ".j");
            outputMapscript = new File("./temp/output.j");
        }
        outputMapscript.getParentFile().mkdirs();
        Files.write(mapScript, outputMapscript, Charsets.UTF_8); // use ascii here, wc3 no understand utf8, you know?

        Result pJassResult = Pjass.runPjass(outputMapscript);
        WLogger.info(pJassResult.getMessage());
        if (!pJassResult.isOk()) {
            for (CompileError err : pJassResult.getErrors()) {
                gui.sendError(err);
            }
            return null;
        }
        return mapScript;
    }

    public static void setUpFileLogging() {
        try {
            // set up file logging:
            Handler handler = new FileHandler("%t/wurst/wurst%g.log", Integer.MAX_VALUE, 20);
            handler.setFormatter(new SimpleFormatter());
            WLogger.setHandler(handler);
            WLogger.setLevel(Level.INFO);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
