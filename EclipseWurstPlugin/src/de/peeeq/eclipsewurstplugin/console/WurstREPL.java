package de.peeeq.eclipsewurstplugin.console;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ui.console.IOConsoleOutputStream;

import de.peeeq.eclipsewurstplugin.builder.ModelManager;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.interpreter.ILInterpreter;
import de.peeeq.wurstscript.intermediateLang.interpreter.LocalState;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassinterpreter.DebugPrintError;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Utils;


/**
 * 
 * Read eval print loop for wurst
 */
public class WurstREPL {

	
	private IOConsoleOutputStream out;
	private ModelManager modelManager;
	private ReplGui gui;
	private CompilationUnit editorCu;
	
	private class ReplGui extends WurstGuiLogger {

		@Override
		public void sendError(CompileError err) {
			super.sendError(err);
			String msg = err.toString() + "\n";
			print(msg);			
		}

	}
	
	
	public WurstREPL(ModelManager mm, IOConsoleOutputStream s) {
		this.modelManager = mm;
		this.out = s;
		gui = this.new ReplGui();
	}
	
	private void print(String msg) {
		try {
			out.write(msg);
		} catch (IOException e) {
			WLogger.severe(e);
		}
	}

	
	Pattern asignmentPattern = Pattern.compile("^\\s*([a-zA-Z_])\\s*=.*");
	
	public void putLine(String line) {
		ILInterpreter interpreter = null;
		try {
			gui.clearErrors();
			StringBuilder code = new StringBuilder();
			code.append("package WurstREPL\n");
			// import packages from current compilation unit
			for (WPackage p : editorCu.getPackages()) {
				code.append("import " + p.getName() + "\n");
			}
			// TODO
			// set current state
			
			code.append("public function repl_start()\n");
			code.append("\t");
			Matcher matcher = asignmentPattern.matcher(line);
			String varName;
			if (matcher.matches()) {
				varName = matcher.group(1);
				code.append("let " + line);
				
			} else {
				code.append("let x = " + line);
				varName = "x";
			}
			code.append("\n");
			
			
			System.out.println("############ code:");
			System.out.println(code.toString());
			Reader source = new StringReader(code.toString());
			
			CompilationUnit cu = modelManager.parse(gui, "<REPL>", source);
			WurstModel model = cu.getModel();
			
			modelManager.typeCheckModel(gui, false);
			if (gui.getErrorCount() > 0) {
				return;
			}
			
			ImTranslator imTranslator = new ImTranslator(model);
			ImProg imProg = imTranslator.translateProg();
			
			
			if (gui.getErrorCount() > 0) {
				return;
			}
			
			interpreter = new ILInterpreter(imProg, gui, null);
			LocalState val = interpreter.executeFunction("repl_start");
			
			print(varName + " = " + val.getVarValue(varName) + "\n");
		} catch (CompileError e) {
			print(e.toString() + "\n");
		} catch (DebugPrintError e) {
			if (interpreter != null) {
				print("Runtime error at: " + Utils.printElementWithSource(interpreter.getLastStatement().attrTrace()) 
						 +"\n");
			}
			print(e.getMessage() + "\n");
		}
	}

	public void setModelManager(ModelManager modelManager2) {
		this.modelManager = modelManager2;
	}

	public void setEditorCompilationUnit(CompilationUnit editorCu) {
		this.editorCu = editorCu;
		
	}
	
}
