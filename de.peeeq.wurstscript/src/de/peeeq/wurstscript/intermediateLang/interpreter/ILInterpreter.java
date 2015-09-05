package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.io.File;
import java.util.Stack;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.ast.Annotation;
import de.peeeq.wurstscript.ast.AstElementWithModifiers;
import de.peeeq.wurstscript.ast.Modifier;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.intermediateLang.ILconstReal;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVoid;
import de.peeeq.wurstscript.jassinterpreter.ReturnException;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;

public class ILInterpreter {
	private ImProg prog;
	private final ProgramState globalState;
	

	
	public ILInterpreter(ImProg prog, WurstGui gui, @Nullable File mapFile, ProgramState globalState) {
		this.prog = prog;
		this.globalState = globalState;
		globalState.addNativeProvider(new BuiltinFuncs(globalState));
		globalState.addNativeProvider(new NativeFunctions());
	}

	public ILInterpreter(ImProg prog, WurstGui gui, @Nullable File mapFile, boolean isCompiletime) {
		this(prog, gui, mapFile, new ProgramState(gui, prog, isCompiletime));
	}

	public static LocalState runFunc(ProgramState globalState, ImFunction f, ILconst ... args) {
		String[] parameterTypes = new String[args.length];
		for (int i=0; i<args.length; i++) {
			parameterTypes[i] = "" + args[i];
		}
		
		if (f.getParameters().size() != args.length) {
			throw new Error("wrong number of parameters when calling func " + f.getName());
		}
		
		for (int i = 0; i<f.getParameters().size(); i++) {
			// TODO could do typecheck here
			args[i] = adjustTypeOfConstant(args[i], f.getParameters().get(i).getType());
		}
		
		if (isCompiletimeNative(f)) {
			return runBuiltinFunction(globalState, f, args);
		}
		
		
		if (f.isNative()) {
			return runBuiltinFunction(globalState, f, args);
		}
		
		
		
		LocalState localState = new LocalState();
		int i = 0;
		for (ImVar p : f.getParameters()) {
			localState.setVal(p, args[i]);
			i++;
		}
		
		globalState.pushStackframe(f, args, f.attrTrace().attrSource());
		
		
		try {
			f.getBody().runStatements(globalState, localState);
		} catch (ReturnException e) {
			ILconst retVal = e.getVal();
			retVal = adjustTypeOfConstant(retVal, f.getReturnType());
			return localState.setReturnVal(retVal);
		} finally {
			globalState.popStackframe();
		}
		if (f.getReturnType() instanceof ImVoid) {
			return localState;
		}
		throw new InterpreterException("function " + f.getName() + " did not return any value...");
	}

	@SuppressWarnings("null")
	private static ILconst adjustTypeOfConstant(@Nullable ILconst retVal, ImType expectedType) {
		if (retVal instanceof ILconstInt
				&& isTypeReal(expectedType)) {
			ILconstInt retValI = (ILconstInt) retVal;
			retVal = new ILconstReal(retValI.getVal());
		}
		return retVal;
	}

	private static boolean isTypeReal(ImType t) {
		if (t instanceof ImSimpleType) {
			ImSimpleType st = (ImSimpleType) t;
			return st.getTypename().equals("real");
		}
		return false;
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
			globalState.getGui().sendError(new CompileError(source, "function " + f.getName() + " not found"));
		} else {
			globalState.getGui().sendError(new CompileError(new WPos("", new LineOffsets(), 0, 0), "function " + f.getName() + " not found"));
		}
		for (ILStackFrame sf : globalState.getStackFrames()) {
			globalState.getGui().sendError(new CompileError(sf.trace, sf.trace.printShort()));
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
