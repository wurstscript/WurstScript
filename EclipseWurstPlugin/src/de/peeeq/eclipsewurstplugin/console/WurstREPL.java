package de.peeeq.eclipsewurstplugin.console;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.console.IOConsoleOutputStream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.peeeq.eclipsewurstplugin.builder.ModelManager;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.intermediateLang.interpreter.CompiletimeNatives;
import de.peeeq.wurstio.intermediateLang.interpreter.ProgramStateIO;
import de.peeeq.wurstio.jassinterpreter.DebugPrintError;
import de.peeeq.wurstio.jassinterpreter.NativeFunctionsIO;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstConfig;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstReal;
import de.peeeq.wurstscript.intermediateLang.ILconstTuple;
import de.peeeq.wurstscript.intermediateLang.interpreter.ILInterpreter;
import de.peeeq.wurstscript.intermediateLang.interpreter.ILconstError;
import de.peeeq.wurstscript.intermediateLang.interpreter.LocalState;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlag;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeNamedScope;
import de.peeeq.wurstscript.types.WurstTypePrimitive;
import de.peeeq.wurstscript.types.WurstTypeTuple;
import de.peeeq.wurstscript.types.WurstTypeVoid;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;


/**
 * 
 * Read eval print loop for wurst
 */
public class WurstREPL {

	
	private static final String WURST_REPL_RESULT = "res";
	private IOConsoleOutputStream out;
	private ModelManager modelManager;
	private ReplGui gui;
	private CompilationUnit editorCu;
	private ILInterpreter interpreter;
	private Map<String, String> currentState;
	private Set<String> importedPackages;
	private Random rand = new Random();
	
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
		init();
	}

	private void init() {
		currentState = Maps.newHashMap();
		importedPackages = Sets.newHashSet();
		RobustProgramState globalState = new RobustProgramState(null, gui);
		interpreter = new ILInterpreter(null, gui, null, globalState);
		
		interpreter.addNativeProvider(new NativeFunctionsIO());
//		interpreter.addNativeProvider(new CompiletimeNatives(globalState));
		
		globalState.setOutStream(new PrintStream(out));
	}
	
	private void print(String msg) {
		try {
			out.write(msg);
		} catch (IOException e) {
			WLogger.severe(e);
		}
	}

	
	Pattern asignmentPattern = Pattern.compile("^\\s*([a-zA-Z0-9_]*\\s+)?([a-zA-Z][a-zA-Z0-9_]*)\\s*=.*");
	
	public void putLine(String line) {
		
		
		try {
			if (line.isEmpty()) {
				return;
			} else if (line.equals("reset")) {
				init();
				return;
			} else if (line.equals("main")) {
				if (translateProg() != null) {
					interpreter.executeFunction("main");
				}
				return;
			} else if (line.equals("help")) {
				printHelp();
				return;
			} else if (line.equals("tests")) {
				runTests();
				return;
			} else if (line.startsWith("compile")) {
				compileProject(line.substring("compile".length()));
				return;
			}
			
			gui.clearErrors();
			StringBuilder code = new StringBuilder();
			code.append("package WurstREPL\n");
			// import packages from current compilation unit
			for (WPackage p : editorCu.getPackages()) {
				importedPackages.add(p.getName());
			}
			for (String pName : importedPackages) {
				code.append("import " + pName + "\n");
			}
			// TODO
			// set current state
			
			code.append("public function repl_start()\n");
			
			
			for (String cmd : currentState.values()) {
				code.append("\t" + cmd + "\n");
			}
			String varName = addEnteredCommand(line, code);
			
			
			System.out.println("############ code:");
			System.out.println(code.toString());
			
			
			CompilationUnit cu = parse(code);
			WStatement assignment = getTranslatedCommand(cu);
			
			String tempName = null;
			if (assignment instanceof LocalVarDef) {
				LocalVarDef def = (LocalVarDef) assignment;
				// change name to some temporary name
				tempName = "tempName" + (rand.nextInt());
				def.setName(tempName);
			}
			
			
			WurstModel model = cu.getModel();
			modelManager.typeCheckModel(gui, false);
			if (gui.getErrorCount() > 0) {
				return;
			}
			
			ImProg imProg = translate(model);
			
			if (gui.getErrorCount() > 0) {
				return;
			}
			
			
			
			
			interpreter.setProgram(imProg);
			LocalState val = interpreter.executeFunction("repl_start");
			
			
			
			
			if (assignment instanceof LocalVarDef) {
				LocalVarDef lvd = (LocalVarDef) assignment;
				WurstType typ = lvd.attrTyp();
				ILconst value = val.getVarValue(tempName);
				
				
				
				String valueTranslated = getTranslatedValue(typ, value);
				if (valueTranslated == null) return;
				if (value instanceof ILconstError) return;
				
				if (value instanceof ILconstReal) {
					ILconstReal r = (ILconstReal) value;
					if (Float.isNaN(r.getVal()) || r.getVal() == Float.NEGATIVE_INFINITY || r.getVal() == Float.POSITIVE_INFINITY) {
						print("cannot store result of computation: " + r.getVal() + "\n");
						return;
					}
				}
				
				print(varName + " = " + value + "     // " + typ + "\n");
				
				
				
				currentState.put(varName, "let " + varName + " = " + valueTranslated);
				
			} else if (assignment instanceof StmtSet) {
				StmtSet stmtSet = (StmtSet) assignment;
				WurstType typ = stmtSet.getRight().attrTyp();
				ILconst value = interpreter.getGlobalState().getVarValue(varName);
				print(varName + " = " + value + "     // " + typ + "\n");
			}
			
			
		} catch (CompileError e) {
			print(e.toString() + "\n");
		} catch (DebugPrintError e) {
			if (interpreter != null) {
				print("Runtime error at: " + Utils.printElementWithSource(interpreter.getLastStatement().attrTrace()) 
						 +"\n");
			}
			print(e.getMessage() + "\n");
		} catch (Throwable e) {
			print("Error: " + e + "\n");
			e.printStackTrace();
			return;
		}
	}

	private void compileProject(String args) {
		try {
			args = args.trim();
			String[] runArgs = args.split("\\s+");
			modelManager.clean();
			IProject project = modelManager.getNature().getProject();
			print("compiling project "+project.getName()+", please wait ...\n");
			project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			WurstConfig config = new WurstConfig();
			WurstCompilerJassImpl comp = new WurstCompilerJassImpl(config, gui, new RunArgs(runArgs));
			WurstModel root = modelManager.getModel();
			comp.checkAndTranslate(root);
			JassProg jassProg = comp.getProg();
			if (jassProg == null) {
				print("Could not compile project\n");
				return;
			}


			JassPrinter printer = new JassPrinter(true);
			String mapScript = printer.printProg(jassProg);

			IFile f = project.getFile("compiled.j");
			if (f.exists()) {
				f.delete(true, null);
			}
			InputStream source = new ByteArrayInputStream(mapScript.getBytes());

			f.create(source, true, null);
			
			print("Output created in " + f.getFullPath() + "\n");
		} catch (CoreException e) {
			e.printStackTrace();
			print(e.getMessage()+ "\n");
		}
	}

	private void runTests() {
		ImProg imProg = translateProg();
		if (imProg == null) {
			return;
		}
		List<ImFunction> successTests = Lists.newArrayList();
		Map<ImFunction, Pair<ImStmt, String>> failTests = Maps.newHashMap();
		for (ImFunction f : imProg.getFunctions()) {
			if (f.hasFlag(FunctionFlag.IS_TEST)) {
				try {
					interpreter.runVoidFunc(f);
					successTests.add(f);
				} catch (TestSuccessException e) {
					successTests.add(f);
				} catch (TestFailException e) {
					failTests.put(f, Pair.create(interpreter.getLastStatement(), e.toString()));
				}
			}
		}
		print(successTests.size() + " tests OK, ");
		print(failTests.size() + " tests failed\n");
		for (Entry<ImFunction, Pair<ImStmt, String>> e : failTests.entrySet()) {
			print(Utils.printElementWithSource(e.getKey().attrTrace()) 
				+ "\n\t" + e.getValue().getB()
				+ "\n\tat " + Utils.printElementWithSource(e.getValue().getA().attrTrace())+ "\n");
		}
	}

	private ImProg translateProg() {
		WurstModel model = modelManager.getModel();
		modelManager.typeCheckModel(gui, false);
		if (gui.getErrorCount() > 0) {
			return null;
		}
		ImProg imProg = translate(model);
		interpreter.setProgram(imProg);
		return imProg;
	}

	private void printHelp() {
		PrintWriter pw = new PrintWriter(out);
		pw.println("Available commands: ");
		pw.println("  'reset'    resets the console to the inital state.");
		pw.println("  'main'     run the main function of the program. ");
		pw.println("  'tests'    run all the tests (functions annotated with @test). ");
		pw.println();
		pw.println("Cou can write down any wurst expression, variable assignment or local variable definition.");
		pw.println("All functions from the currently opened editor can be used.");
		pw.flush();
	}

	private String getTranslatedValue(WurstType typ, ILconst value) {
		String valueTranslated = null;
		if (typ instanceof WurstTypePrimitive) {
			valueTranslated = value + "";
		} else if (typ instanceof WurstTypeNamedScope) {
			WurstTypeNamedScope ct = (WurstTypeNamedScope) typ;
			valueTranslated = value + " castTo " + ct;
		} else if (typ instanceof WurstTypeTuple) {
			WurstTypeTuple tt = (WurstTypeTuple) typ;
			TupleDef def = tt.getTupleDef();
			valueTranslated = def.getName() + "(";
			if (value instanceof ILconstTuple) {
				ILconstTuple tuple = (ILconstTuple) value;
				for (int i=0; i<def.getParameters().size(); i++) {
					WParameter p = def.getParameters().get(i);
					ILconst argVal = tuple.getValue(i);
					if (i > 0) {
						valueTranslated += ", ";
					}
					valueTranslated += getTranslatedValue(p.attrTyp(), argVal);
				}
			}
			valueTranslated += ")";
		} else if (typ instanceof WurstTypeVoid) {
			// do nothing
		} else {
			print("typ = " + typ);
		}
		return valueTranslated;
	}

	private WStatement getTranslatedCommand(CompilationUnit cu) {
		FuncDef f = (FuncDef) cu.getPackages().get(0).getElements().get(0);
		return f.getBody().get(f.getBody().size()-2);
	}

	private ImProg translate(WurstModel model) {
		ImTranslator imTranslator = new ImTranslator(model, false);
		imTranslator.setEclipseMode(true);
		ImProg imProg = imTranslator.translateProg();
		return imProg;
	}

	private CompilationUnit parse(StringBuilder code) {
		Reader source = new StringReader(code.toString());
		CompilationUnit cu = modelManager.parse(gui, "<REPL>", source);
		return cu;
	}

	private String addEnteredCommand(String enteredLine, StringBuilder code) {
		code.append("\t");
		Matcher asignmentPatternMatcher = asignmentPattern.matcher(enteredLine);
		String varName;
		if (asignmentPatternMatcher.matches()) {
			varName = asignmentPatternMatcher.group(2);
			code.append(enteredLine);
		} else {
			varName = WURST_REPL_RESULT;
			code.append("let "+varName+" = " + enteredLine);
		}
		code.append("\n");
		return varName;
	}

	public void setModelManager(ModelManager modelManager2) {
		this.modelManager = modelManager2;
	}

	public void setEditorCompilationUnit(CompilationUnit editorCu) {
		this.editorCu = editorCu;
		
	}
	
}
