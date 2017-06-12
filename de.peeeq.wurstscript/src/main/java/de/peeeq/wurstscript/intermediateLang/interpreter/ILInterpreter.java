package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.io.File;
import java.util.Stack;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.ast.Annotation;
import de.peeeq.wurstscript.ast.HasModifier;
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
import de.peeeq.wurstscript.jassIm.Element;
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

	public static LocalState runFunc(ProgramState globalState, ImFunction f, @Nullable Element caller,
			ILconst... args) {
		try {
			String[] parameterTypes = new String[args.length];
			for (int i = 0; i < args.length; i++) {
				parameterTypes[i] = "" + args[i];
			}

			if (f.getParameters().size() != args.length) {
				throw new Error("wrong number of parameters when calling func " + f.getName());
			}

			for (int i = 0; i < f.getParameters().size(); i++) {
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

			globalState.pushStackframe(f, args, (caller == null ? f : caller).attrTrace().attrErrorPos());

			try {
				f.getBody().runStatements(globalState, localState);
				globalState.popStackframe();
			} catch (ReturnException e) {
				globalState.popStackframe();
				ILconst retVal = e.getVal();
				retVal = adjustTypeOfConstant(retVal, f.getReturnType());
				return localState.setReturnVal(retVal);
			}
			if (f.getReturnType() instanceof ImVoid) {
				return localState;
			}
			throw new InterpreterException("function " + f.getName() + " did not return any value...");
		} catch (InterpreterException e) {
			String msg = buildStacktrace(globalState, e);
			throw e.withStacktrace(msg);
		} catch (Exception e) {
			String msg = buildStacktrace(globalState, e);
			throw new InterpreterException(globalState.getLastStatement().attrTrace(), "You encountered a bug in the interpreter: " + e, e).withStacktrace(msg);
		}
	}

	private static String buildStacktrace(ProgramState globalState, Exception e) {
		StringBuilder err = new StringBuilder();
		try {
			WPos src = globalState.getLastStatement().attrTrace().attrSource();
			err.append("at : " + new File(src.getFile()).getName() + ", line " + src.getLine() + "\n");
		} catch (Exception _e) {
			// ignore
		}
		for (int i=globalState.getStackFrames().size()-1; i>=0; i--) {
			ILStackFrame sf = globalState.getStackFrames().get(i);
			err.append(sf.getMessage());
			err.append("\n");
		}
		String msg = err.toString();
		return msg;
	}

	@SuppressWarnings("null")
	private static ILconst adjustTypeOfConstant(@Nullable ILconst retVal, ImType expectedType) {
		if (retVal instanceof ILconstInt && isTypeReal(expectedType)) {
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
		String errors = "";
		for (NativesProvider natives : globalState.getNativeProviders()) {
			try {
				return new LocalState(natives.invoke(f.getName(), args));
			} catch (NoSuchNativeException e) {
				errors += "\n" + e.getMessage();
				// ignore
			}
		}
		ImStmt lastStatement = globalState.getLastStatement();
		String errorMessage = "function " + f.getName() + " cannot be used from the Wurst interpreter.\n" + errors;
		if (lastStatement != null) {
			WPos source = lastStatement.attrTrace().attrSource();
			globalState.getGui().sendError(new CompileError(source, errorMessage));
		} else {
			globalState.getGui().sendError(new CompileError(new WPos("", new LineOffsets(), 0, 0), errorMessage));
		}
		for (ILStackFrame sf : globalState.getStackFrames()) {
			globalState.getGui().sendError(new CompileError(sf.trace, sf.trace.printShort()));
		}
		return new LocalState();
	}

	private static boolean isCompiletimeNative(ImFunction f) {
		if (f.getTrace() instanceof HasModifier) {
			HasModifier f2 = (HasModifier) f.getTrace();
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

	public LocalState executeFunction(String funcName, @Nullable Element trace) {
		globalState.resetStackframes();
		for (ImFunction f : prog.getFunctions()) {
			if (f.getName().equals(funcName)) {
				return runFunc(globalState, f, trace);
			}
		}

		throw new Error("no function with name " + funcName + "was found.");
	}

	public void runVoidFunc(ImFunction f, @Nullable Element trace) {
		globalState.resetStackframes();
		runFunc(globalState, f, trace);
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