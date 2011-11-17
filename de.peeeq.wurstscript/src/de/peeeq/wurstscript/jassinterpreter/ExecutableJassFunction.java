package de.peeeq.wurstscript.jassinterpreter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.jassAst.JassFunction;

public interface ExecutableJassFunction {

	ILconst execute(JassInterpreter jassInterpreter, ILconst[] arguments);

	
	
}

class UserDefinedJassFunction implements ExecutableJassFunction {

	private JassFunction jassFunction;

	public UserDefinedJassFunction(JassFunction f) {
		this.jassFunction = f;
	}

	@Override
	public ILconst execute(JassInterpreter jassInterpreter, ILconst[] arguments) {
		return jassInterpreter.executeJassFunction(jassFunction, arguments);
	}
	
}

class NativeJassFunction implements ExecutableJassFunction {

	private Method method;

	public NativeJassFunction(Method method) {
		this.method = method;
	}

	@Override
	public ILconst execute(JassInterpreter jassInterpreter, ILconst[] arguments) {
		try {
			Object result = method.invoke(null, (Object[])arguments);
			return (ILconst) result;
		} catch (IllegalArgumentException e) {
			throw new Error(e);
		} catch (IllegalAccessException e) {
			throw new Error(e);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof Error) {
				throw (Error) e.getCause();
			} else {
				throw new Error(e.getCause());
			}
		}
	}
	
}


class UnknownJassFunction implements ExecutableJassFunction {

	private String name;

	public UnknownJassFunction(String name) {
		this.name = name;
	}

	@Override
	public ILconst execute(JassInterpreter jassInterpreter, ILconst[] arguments) {
		throw new Error("Function " + name + " could not be found.");
	}
	
}

