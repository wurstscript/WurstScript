package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstNull;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVoid;
import de.peeeq.wurstscript.jassinterpreter.NativeFunctions;
import de.peeeq.wurstscript.jassinterpreter.ReturnException;

public class ILInterpreter {
	private final ImProg prog;
	private ProgramState globalState;

	public ILInterpreter(ImProg prog) {
		this.prog = prog;
		this.globalState = new ProgramState();
	}
	
	public static ILconst runFunc(ProgramState globalState, ImFunction f, ILconst ... args) {
		if (f.getIsNative()) {
			Class<NativeFunctions> natives = NativeFunctions.class;
			for (Method method : natives.getMethods()) {
				if (method.getName().equals(f.getName())) {
					Object r = null;
					try {
						r = method.invoke(null, (Object[]) args);
					} catch (IllegalAccessException | IllegalArgumentException e) {
						e.printStackTrace();
						throw new Error(e);
					} catch (InvocationTargetException e) {
						if (e.getCause() instanceof Error) {
							throw (Error) e.getCause();
						}
						throw new Error(e.getCause());
					}
					return (ILconst) r;
				}
			}
			throw new Error("native function " + f.getName() + " can not be executed at compile time.");
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
		throw new Error("function " + f.getName() + " did not return any value...");
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


}
