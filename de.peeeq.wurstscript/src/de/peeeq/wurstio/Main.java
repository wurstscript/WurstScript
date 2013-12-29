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

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.peeeq.wurstio.Pjass.Result;
import de.peeeq.wurstio.gui.About;
import de.peeeq.wurstio.gui.WurstGuiImpl;
import de.peeeq.wurstio.hotdoc.HotdocGenerator;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.BackupController;
import de.peeeq.wurstscript.ErrorReporting;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstConfig;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlag;
import de.peeeq.wurstscript.utils.Utils;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			RunArgs.printHelpAndExit();
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
		WurstCompilerJassImpl compiler = null;
		try {
			RunArgs runArgs = new RunArgs(args);
			WurstConfig config = new WurstConfig().initFromStandardFiles();

			if (runArgs.showAbout()) {
				About about = new About(null, false);
				about.setVisible(true);
				return;
			}

			if (runArgs.createHotDoc()) {
				HotdocGenerator hg = new HotdocGenerator(config, runArgs.getFiles());
				hg.generateDoc();
			}


			if (runArgs.isGui()) {
				gui = new WurstGuiImpl();
				// use the error reporting with GUI
				ErrorReporting.instance = new ErrorReportingIO();
			} else {
				gui = new WurstGuiCliImpl();
			}

			if (runArgs.showLastErrors()) {
				//				@SuppressWarnings("unchecked")
				//
				//				List<CompileError> errors = (List<CompileError>) Utils.loadFromFile("lastErrors.data");
				//				if (errors == null || errors.size() == 0) {
				//					JOptionPane.showMessageDialog(null, "No errors where found.");
				//				} else {
				//					for (CompileError e : errors) {
				//						gui.sendError(e);
				//					}
				//				}
				//				gui.sendFinished();
				JOptionPane.showMessageDialog(null, "not implemented");
				return;
			}

			try {
				if (runArgs.getMapFile() != null) {
					// tempfolder
					File tempFolder = new File("./temp/");
					tempFolder.mkdirs();
					BackupController bc = new BackupController();
					bc.makeBackup(runArgs.getMapFile(), 24);
				}

				compilation : do {


					compiler = new WurstCompilerJassImpl(config, gui, runArgs);
					for (String file: runArgs.getFiles()) {
						compiler.loadFiles(file);
					}
					WurstModel model = compiler.parseFiles();
					compiler.checkProg(model);
					
					if (gui.getErrorCount() > 0) {
						break compilation;
					}
					
					compiler.translateProgToIm(model);

					if (gui.getErrorCount() > 0) {
						break compilation;
					}


					if (runArgs.runCompiletimeFunctions()) {
						gui.sendProgress("Running tests", 0.9);
						CompiletimeFunctionRunner ctr = new CompiletimeFunctionRunner(compiler.getImProg(), compiler.getMapFile(), gui, FunctionFlag.IS_TEST);
						ctr.run();
					}
					if (runArgs.runCompiletimeFunctions()) {
						gui.sendProgress("Running compiletime functions", 0.91);
						CompiletimeFunctionRunner ctr = new CompiletimeFunctionRunner(compiler.getImProg(), compiler.getMapFile(), gui, FunctionFlag.IS_COMPILETIME);
						ctr.run();
					}

					JassProg jassProg = compiler.transformProgToJass();

					if (jassProg == null || gui.getErrorCount() > 0) {
						break compilation;
					}

					boolean withSpace;
					if (runArgs.isOptimize()) {
						withSpace = false;
					} else {
						withSpace = true;
					}

					gui.sendProgress("Printing Jass", 0.91);
					JassPrinter printer = new JassPrinter(withSpace);
					CharSequence mapScript = printer.printProg(jassProg);





					// output to file
					gui.sendProgress("Writing output file", 0.98);
					File outputMapscript; 
					if (runArgs.getOutFile() != null) {
						outputMapscript = new File(runArgs.getOutFile());
					} else {
						//outputMapscript = File.createTempFile("outputMapscript", ".j");
						outputMapscript = new File("./temp/output.j");
					}
					Files.write(mapScript, outputMapscript, Charsets.UTF_8); // use ascii here, wc3 no understand utf8, you know?

					Result pJassResult = Pjass.runPjass(outputMapscript);
					WLogger.info(pJassResult.getMessage());
					if (!pJassResult.isOk()) {
						for (CompileError err : pJassResult.getErrors()) {
							gui.sendError(err);
						}
						break compilation;
					}

					if (runArgs.getMapFile() != null) { // output to map
						gui.sendProgress("Writing to map", 0.99);
						File mapFile = new File(runArgs.getMapFile());

						MpqEditor mpqEditor = MpqEditorFactory.getEditor();
						mpqEditor.deleteFile(mapFile, "war3map.j");
						mpqEditor.insertFile(mapFile, "war3map.j", outputMapscript);
						mpqEditor.compactArchive(mapFile);
					}

				} while (false); // dummy loop to allow "break compilation"
				gui.sendProgress("Finished!", 1);

				//			List<CompileError> errors = gui.getErrorList();
				//			Utils.saveToFile(errors, "lastErrors.data");
			} catch (AbortCompilationException e) {
				gui.showInfoMessage(e.getMessage());
			}
		} catch (Throwable t) {
			String source = "";
			try {
				if (compiler != null) {
					source = compiler.getCompleteSourcecode();
				}
			} catch (Throwable t2) {
				WLogger.severe(t2);
			}

			ErrorReporting.instance.handleSevere(t, source);


		} finally {
			if (gui != null) {
				gui.sendFinished();
			}
		}
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
