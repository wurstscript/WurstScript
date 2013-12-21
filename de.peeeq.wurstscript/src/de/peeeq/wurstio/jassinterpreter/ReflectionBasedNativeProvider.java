package de.peeeq.wurstio.jassinterpreter;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.interpreter.NativesProvider;
import de.peeeq.wurstscript.intermediateLang.interpreter.NoSuchNativeException;
import de.peeeq.wurstscript.utils.Utils;

public abstract class ReflectionBasedNativeProvider  implements NativesProvider{

	protected PrintStream outStream = System.out;

	@Override
	public ILconst invoke(String funcname, ILconst[] args) throws NoSuchNativeException {
		nextMethod: for (Method method : this.getClass().getMethods()) {
			if (method.getName().equals(funcname)) {
				Object r = null;
				try {
					if (args.length != method.getParameterTypes().length) {
						throw new Error("Wrong number of parameters when calling " +funcname+
								" . Expected " + 
								method.getParameterTypes().length + " but found " + 
								args.length);
					}
					int i = 0;
					for (Class<?> paramType : method.getParameterTypes()) {
						if (!paramType.isAssignableFrom(args[i].getClass())) {
							continue nextMethod;
						}
						i++;
					}
					r = method.invoke(this, (Object[]) args);
				} catch (IllegalAccessException | IllegalArgumentException e) {
					WLogger.severe(e);
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
		String[] parameterTypes = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			parameterTypes[i] = "" + args[i];
		}
		outStream.println("native function " + funcname + "(" + Utils.printSep(", ", parameterTypes)
				+ ") can not be executed at compile time.");
		throw new NoSuchNativeException();
	}

	@Override
	public void setOutStream(PrintStream outStream) {
		this.outStream = outStream;
	}

}