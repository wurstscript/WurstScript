package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstString;

public class CompiletimeNatives implements NativesProvider {

	

	private ProgramState globalState;

	public CompiletimeNatives(ProgramState globalState) {
		this.globalState = globalState;
	}

	@Override
	public ILconst invoke(String funcname, ILconst[] args) {
		for (Method method : this.getClass().getMethods()) {
			if (method.getName().equals(funcname)) {
				Object r = null;
				try {
					r = method.invoke(this, (Object[]) args);
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
		throw new InterprationError("Compiletime function " + funcname + " is not implemented yet.");
	}
	
	public void compileError(ILconstString msg) {
		AstElement trace = globalState.getLastStatement().attrTrace();
		globalState.getGui().sendError(new CompileError(trace.attrSource(), msg.getVal()));
	}
	
}
