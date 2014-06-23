package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.io.File;
import java.util.Stack;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.ast.Annotation;
import de.peeeq.wurstscript.ast.AstElementWithModifiers;
import de.peeeq.wurstscript.ast.Modifier;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVoid;
import de.peeeq.wurstscript.jassinterpreter.ReturnException;
import de.peeeq.wurstscript.parser.WPos;

public class ILInterpreter {
	private ImProg prog;
	private final ProgramState globalState;
	

	
	public ILInterpreter(ImProg prog, WurstGui gui, File mapFile, ProgramState globalState) {
		this.prog = prog;
		this.globalState = globalState;
		globalState.addNativeProvider(new BuiltinFuncs(globalState));
		globalState.addNativeProvider(new NativeFunctions());
	}

	public ILInterpreter(ImProg prog, WurstGui gui, File mapFile) {
		this(prog, gui, mapFile, new ProgramState(gui, prog));
	}

	public static LocalState runFunc(ProgramState globalState, ImFunction f, ILconst ... args) {
		String[] parameterTypes = new String[args.length];
		for (int i=0; i<args.length; i++) {
			parameterTypes[i] = "" + args[i];
		}
		
		if (isCompiletimeNative(f)) {
			return runBuiltinFunction(globalState, f, args);
		}
		
		
		if (f.isNative()) {
			return runBuiltinFunction(globalState, f, args);
		}
		
		if (f.getParameters().size() != args.length) {
			throw new Error("wrong number of parameters when calling func " + f.getName());
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
			return localState.setReturnVal(e.getVal());
		}
		if (f.getReturnType() instanceof ImVoid) {
			return localState;
		}
		throw new InterpreterException("function " + f.getName() + " did not return any value...");
	}

	private static LocalState runBuiltinFunction(ProgramState globalState, ImFunction f, ILconst... args) {
		
		for (NativesProvider natives : globalState.getNativeProviders()) {
			try {
				return new LocalState(natives.invoke(f.getName(), args));
			} catch (NoSuchNativeException e) {
				// ignore
			}
		}
		ImStmt lastStatement = globalState.getLastStatement();
		if (lastStatement != null) {
			WPos source = lastStatement.attrTrace().attrSource();
			globalState.getOutStream().println("function " + f.getName() + " not found (line " + source.getLine() + " in " + source.getFile() + ")");
		} else {
			globalState.getOutStream().println("function " + f.getName() + " not found (no source)");
		}
		return new LocalState();
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

	public LocalState executeFunction(String funcName) {
		globalState.resetStackframes();
		for (ImFunction f : prog.getFunctions()) {
			if (f.getName().equals(funcName)) {
				return runFunc(globalState, f);
			}
		}
		throw new Error("no function with name "+ funcName + "was found.");
	}

	public void runVoidFunc(ImFunction f) {
		globalState.resetStackframes();
		runFunc(globalState, f);
	}

	public @Nullable ImStmt getLastStatement() {
		return globalState.getLastStatement();
	}

	public void writebackGlobalState(boolean injectObjects) {
		globalState.writeBack(injectObjects);
		
	}

	public ProgramState getGlobalState() {
		return globalState;
	}

	public void addNativeProvider(NativesProvider np) {
		globalState.addNativeProvider(np);		
	}

	public void setProgram(ImProg imProg) {
		this.prog = imProg;
		this.getGlobalState().setProg(imProg);
		globalState.resetStackframes();
	}

	public Stack<ILStackFrame> getStackFrames() {
		return globalState.getStackFrames();
		
	}


}
