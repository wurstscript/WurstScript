package de.peeeq.wurstscript;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.peeeq.wurstscript.Pjass.Result;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.About;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.gui.WurstGuiImpl;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassoptimizer.JassOptimizer;
import de.peeeq.wurstscript.jassoptimizer.JassOptimizerImpl;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.mpq.MpqEditor;
import de.peeeq.wurstscript.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.utils.LineOffsets;
import de.peeeq.wurstscript.utils.Utils;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//		JOptionPane.showMessageDialog(null , "time to connect profiler ^^");
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
		Date myDate = new Date();
		WLogger.info("Started compiler at" + sdf.format(myDate) + "with args " + Utils.printSep(", ", args));
		
		WurstGui gui = null;
		try {
			RunArgs runArgs = new RunArgs(args);

			if (runArgs.showAbout()) {
				About about = new About(null, false);
				about.setVisible(true);
				return;
			}

			
			if (runArgs.isGui()) {
				gui = new WurstGuiImpl();
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
			
			if (runArgs.getMapFile() != null) {
				BackupController bc = new BackupController();
				bc.makeBackup(runArgs.getMapFile(), 19);
			}

			compilation : do {


				WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui);
				for (String file: runArgs.getFiles()) {
					compiler.loadFiles(file);
				}
				compiler.parseFiles();

				if (gui.getErrorCount() > 0) {
					break compilation;
				}

				JassProg jassProg = compiler.getProg();

				if (jassProg == null || gui.getErrorCount() > 0) {
					break compilation;
				}

				boolean withSpace;
				if (runArgs.isOptimize()) {
					gui.sendProgress("Optimizing Jass", 0.85);
					JassOptimizer optimizer = new JassOptimizerImpl();
					optimizer.optimize(jassProg);
					withSpace = false;
				} else {
					withSpace = true;
				}

				gui.sendProgress("Printing Jass", 0.90);
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
				Files.write(mapScript, outputMapscript, Charsets.US_ASCII); // use ascii here, wc3 no understand utf8, you know?

				Result pJassResult = Pjass.runPjass(outputMapscript);
				System.out.println(pJassResult.getMessage());
				if (!pJassResult.isOk()) {
					for (String error : pJassResult.getMessage().split("(\n|\r)+")) {
						WLogger.info("Error = " + error);
						int pos = error.indexOf(".j:") + 3;
						if (pos < 0) {
							continue;
						}
						String line = "";
						while (Character.isDigit(error.charAt(pos))) {
							line+=error.charAt(pos);
							pos++;
						}
						if (line == "") line = "0";
						gui.sendError(new CompileError(Ast.WPos(outputMapscript.getAbsolutePath(), LineOffsets.dummy, Integer.parseInt(line), 0), error.substring(pos)));
					}
					break compilation;
				}

				if (runArgs.getMapFile() != null) { // output to map
					gui.sendProgress("Writing to map", 0.99);
					File mapFile = new File(runArgs.getMapFile());

					MpqEditor mpqEditor = MpqEditorFactory.getEditor();
					mpqEditor.deleteFile(mapFile, "war3map.j");
					mpqEditor.insertFile(mapFile, "war3map.j", outputMapscript);
				}

			} while (false); // dummy loop to allow "break compilation"
			gui.sendProgress("Finished!", 1);
			
//			List<CompileError> errors = gui.getErrorList();
//			Utils.saveToFile(errors, "lastErrors.data");

			




		} catch (Throwable t) {


			WLogger.severe(t);
			
			String title  = "Sorry!";
			String message = "You have encountered a bug in the Wurst Compiler.";
			
			Object[] options = {
					"Nothing",
					"Send automatic error report",
					"Create manual bug report"
				            };
			JFrame parent = new JFrame();
			parent.pack();
			parent.setVisible(true);
			Utils.setWindowToCenterOfScreen(parent);
			int n = JOptionPane.showOptionDialog(parent,
				message,
				title,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,     //do not use a custom Icon
				options,  //the titles of buttons
				options[0]); //default button titles
			
			if (n == 1) {
				boolean r = ErrorReporting.sendErrorReport(t);
				if (r) {
					JOptionPane.showMessageDialog(parent, "Thank you!");
				}else {
					JOptionPane.showMessageDialog(parent, "Error report could not be sent.");
				}
			} else if (n == 2) {
				Desktop desk = Desktop.getDesktop();
				try {
					desk.browse(new URI("http://code.google.com/p/pscript-lang/issues/entry"));
				} catch (Exception e) {
					WLogger.severe(e);
					JOptionPane.showMessageDialog(parent, "Could not open browser.");
				}
			}
				
			System.exit(1);


		} finally {
			if (gui != null) {
				gui.sendFinished();
			}
		}
	}
	

}
