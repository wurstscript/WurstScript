package de.peeeq.wurstio;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import javax.swing.JOptionPane;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import de.peeeq.wurstio.Pjass.Result;
import de.peeeq.wurstio.compilationserver.WurstServer;
import de.peeeq.wurstio.gui.About;
import de.peeeq.wurstio.gui.WurstGuiImpl;
import de.peeeq.wurstio.hotdoc.HotdocGenerator;
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
		WLogger.info( ">>> " + sdf.format(myDate) + " - Started compiler ("+About.version+") with args " + Utils.printSep(", ", args));
		try {
			WLogger.info("compiler path1: " + Main.class.getProtectionDomain().getCodeSource().getLocation());
			WLogger.info("compiler path2: " + ClassLoader.getSystemClassLoader().getResource(".").getPath());
		} catch (Throwable t) {}


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
			
			if (runArgs.isStartServer()) {
				WurstServer.startServer();
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
							gui.sendProgress("Writing to map", 0.99);
							mpqEditor.deleteFile("war3map.j");
							byte[] war3map = mapScript.toString().getBytes(Charsets.UTF_8);
							mpqEditor.insertFile("war3map.j", war3map);
						}
					}
				} else {
					doCompilation(gui, null, runArgs);
				}


				gui.sendProgress("Finished!", 1);
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

	private static @Nullable CharSequence doCompilation(WurstGui gui, @Nullable MpqEditor mpqEditor, RunArgs runArgs) throws IOException {
		WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui, mpqEditor, runArgs);
		for (String file: runArgs.getFiles()) {
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
			gui.sendProgress("Running tests", 0.9);
			CompiletimeFunctionRunner ctr = new CompiletimeFunctionRunner(compiler.getImProg(), mapFile, mpqEditor, gui, FunctionFlagEnum.IS_TEST);
			ctr.run();
		
			// compiletime functions
			gui.sendProgress("Running compiletime functions", 0.91);
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

		gui.sendProgress("Printing Jass", 0.91);
		JassPrinter printer = new JassPrinter(withSpace, jassProg);
		CharSequence mapScript = printer.printProg();





		// output to file
		gui.sendProgress("Writing output file", 0.98);
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
			String logFile = "logs/test.log";
			new File(logFile).mkdirs();
			Handler handler = new FileHandler(logFile, Integer.MAX_VALUE, 20);
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
