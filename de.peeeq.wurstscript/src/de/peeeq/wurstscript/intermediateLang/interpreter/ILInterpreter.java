package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.io.File;

import de.peeeq.wurstscript.ast.Annotation;
import de.peeeq.wurstscript.ast.AstElementWithModifiers;
import de.peeeq.wurstscript.ast.Modifier;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstNull;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVoid;
import de.peeeq.wurstscript.jassinterpreter.NativeFunctions;
import de.peeeq.wurstscript.jassinterpreter.ReturnException;
import de.peeeq.wurstscript.utils.Utils;

public class ILInterpreter {
	private final ImProg prog;
	private ProgramState globalState;

	public ILInterpreter(ImProg prog, WurstGui gui, File mapFile) {
		this.prog = prog;
		this.globalState = new ProgramState(mapFile, gui);
	}

	public static ILconst runFunc(ProgramState globalState, ImFunction f, ILconst ... args) {
		String[] parameterTypes = new String[args.length];
		for (int i=0; i<args.length; i++) {
			parameterTypes[i] = "" + args[i];
		}
		System.out.println("calling function " + f.getName() + "("+ Utils.printSep(", ", parameterTypes) +  ")");
		
		if (isCompiletimeNative(f)) {
			return runBuiltinFunction(f, new CompiletimeNatives(globalState), args);
		}
		
		
		if (f.isNative()) {
			return runBuiltinFunction(f, new NativeFunctions(), args);
		}
		LocalState localState = new LocalState();
		int i = 0;
		for (ImVar p : f.getParameters()) {
			localState.setVal(p, args[i]);
			i++;
		}
		try {
			f.getBody().runStatements(globalState, localState);
		} catch (ReturnException e) {
			return e.getVal();
		}
		if (f.getReturnType() instanceof ImVoid) {
			return ILconstNull.instance();
		}
		throw new InterprationError("function " + f.getName() + " did not return any value...");
	}

	private static ILconst runBuiltinFunction(ImFunction f, NativesProvider natives, ILconst... args)	throws Error, InterprationError {
		return natives.invoke(f.getName(), args);
	}

	private static boolean isCompiletimeNative(ImFunction f) {
		if (f.getTrace() instanceof AstElementWithModifiers) {
			AstElementWithModifiers f2 = (AstElementWithModifiers) f.getTrace();
			for (Modifier m : f2.getModifiers()) {
				if (m instanceof Annotation) {
					Annotation annotation = (Annotation) m;
					if (annotation.getAnnotationType().equals("@compiletimenative")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void executeFunction(String funcName) {
		for (ImFunction f : prog.getFunctions()) {
			if (f.getName().equals(funcName)) {
				runFunc(globalState, f);
				return;
			}
		}
		throw new Error("no function with name "+ funcName + "was found.");
	}

	public void runVoidFunc(ImFunction f) {
		runFunc(globalState, f);
	}

	public ImStmt getLastStatement() {
		return globalState.getLastStatement();
	}

	public void writebackGlobalState() {
		globalState.writeBack();
		
	}


}
