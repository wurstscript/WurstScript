package de.peeeq.wurstio;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.peeeq.wurstio.intermediateLang.interpreter.CompiletimeNatives;
import de.peeeq.wurstio.intermediateLang.interpreter.ProgramStateIO;
import de.peeeq.wurstio.jassinterpreter.NativeFunctionsIO;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediateLang.interpreter.ILInterpreter;
import de.peeeq.wurstscript.intermediateLang.interpreter.ILStackFrame;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlag;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;

public class CompiletimeFunctionRunner {

	private final ImProg imProg;
	private final File mapFile;
	private ILInterpreter interpreter;
	private WurstGui gui;
	private FunctionFlag functionFlag;
	private List<ImFunction> successTests = Lists.newArrayList();
	private Map<ImFunction, Pair<ImStmt, String>> failTests = Maps.newLinkedHashMap();

	


	public CompiletimeFunctionRunner(ImProg imProg, File mapFile, WurstGui gui, FunctionFlag flag) {
		this.imProg = imProg;
		this.mapFile = mapFile;
		ProgramStateIO globalState = new ProgramStateIO(mapFile, gui);
		globalState.setProg(imProg);
		this.interpreter = new ILInterpreter(imProg, gui, mapFile, globalState);
		
		interpreter.addNativeProvider(new NativeFunctionsIO());
		interpreter.addNativeProvider(new CompiletimeNatives(globalState));
		this.gui = gui;
		this.functionFlag = flag;
	}
	

	public void run() {
//		interpreter.executeFunction("main");
//		interpreter.executeFunction("initGlobals");
		try {
			for (ImFunction f : imProg.getFunctions()) {
				if (f.hasFlag(functionFlag)) {
					try {
						WLogger.info("running " +functionFlag+" function " + f.getName());
						interpreter.runVoidFunc(f);
						successTests.add(f);
					} catch (TestSuccessException e) {
						successTests.add(f);
					} catch (TestFailException e) {
						failTests.put(f, Pair.create(interpreter.getLastStatement(), e.toString()));
					}
				}
			}
			if (functionFlag == FunctionFlag.IS_COMPILETIME) {
				interpreter.writebackGlobalState();
			}
		} catch (Throwable e) {
			WLogger.severe(e);
			ImStmt s = interpreter.getLastStatement();
			AstElement origin = s.attrTrace();
			if (origin != null) { 
				gui.sendError(new CompileError(origin.attrSource(), e.getMessage()));
				
				// stackframe messages ...
				for (ILStackFrame sf : Utils.iterateReverse(interpreter.getStackFrames())) {
					gui.sendError(sf.makeCompileError());
				}
				
			} else {
				throw new Error("could not get origin");
			}
		}
		
	}

	public List<ImFunction> getSuccessTests() {
		return successTests;
	}


	public Map<ImFunction, Pair<ImStmt, String>> getFailTests() {
		return failTests;
	}

}
