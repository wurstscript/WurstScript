package de.peeeq.wurstscript.jassinterpreter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.intermediateLang.ILconstNull;
import de.peeeq.wurstscript.intermediateLang.ILconstReal;
import de.peeeq.wurstscript.intermediateLang.ILconstString;
import de.peeeq.wurstscript.intermediateLang.interpreter.InterprationError;
import de.peeeq.wurstscript.intermediateLang.interpreter.NativesProvider;


/**
 * provides native functions which are invoked via reflection
 * a function should only take and return ILconsts
 * 
 *  remember that all functions must be static
 */
public class NativeFunctions implements NativesProvider {

	@Native
	static public void testSuccess() {
		throw TestSuccessException.instance;
	}
	
	static public void testFail(ILconstString msg) {
		throw new TestFailException(msg.getVal());
	}

	
	static public void BJDebugMsg(ILconstString msg) {
		System.out.println(msg.getVal());
	}
	
	static public void println(ILconstString msg) {
		System.out.println(msg.getVal());
	}
	
	static public ILconstInt ModuloInteger(ILconstInt a, ILconstInt b) {
		return new ILconstInt(a.getVal() % b.getVal());
	}
	
	static public ILconstReal ModuloReal(ILconstReal a, ILconstReal b) {
		return new ILconstReal(a.getVal() % b.getVal());
	}
	
	@Native
	static public ILconstString I2S(ILconstInt i) {
		return new ILconstString("" + i.getVal());
	}
	
	static public ILconstNull Player(ILconstInt p) {
		return ILconstNull.instance();
	}
	
	static public ILconstNull InitHashtable() {
		return ILconstNull.instance();
	}

	@Override
	public ILconst invoke(String funcname, ILconst[] args) {
		for (Method method : this.getClass().getMethods()) {
			if (method.getName().equals(funcname)) {
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
		throw new InterprationError("native function " + funcname + " can not be executed at compile time.");
	}
}
