package de.peeeq.wurstscript.jassinterpreter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.intermediateLang.ILconstNull;
import de.peeeq.wurstscript.intermediateLang.ILconstNum;
import de.peeeq.wurstscript.intermediateLang.ILconstReal;
import de.peeeq.wurstscript.intermediateLang.ILconstString;
import de.peeeq.wurstscript.intermediateLang.IlConstHandle;
import de.peeeq.wurstscript.intermediateLang.interpreter.InterprationError;
import de.peeeq.wurstscript.intermediateLang.interpreter.NativesProvider;
import de.peeeq.wurstscript.utils.Utils;


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
	
	static public void $debugPrint(ILconstString msg) {
		System.err.println(msg.getVal());
		throw new DebugPrintError(msg.getVal());
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
	
	static public IlConstHandle InitHashtable() {
		return new IlConstHandle(getRandomName("ht"), new HashMap<Integer, Map<Integer, Object>>());
	}
	
	static public void SaveInteger(IlConstHandle ht, ILconstInt key1, ILconstInt key2, ILconstInt value) {
		Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
		Map<Integer, Object> map2 = map.get(key1.getVal());
		if (map2 == null) {
			map2 = Maps.newHashMap();
			map.put(key1.getVal(), map2);
		}
		map2.put(key2.getVal(), value);
	}
	
	static public ILconstInt LoadInteger(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
		Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
		Map<Integer, Object> map2 = map.get(key1.getVal());
		if (map2 == null) {
			return ILconstInt.create(0);
		}
		return (ILconstInt) map2.get(key2.getVal());
	}
	
	static public IlConstHandle GetLocalPlayer() {
		return new IlConstHandle("Local Player", "local player");
	}
	
	private static String getRandomName(String string) {
		return string + new Random().nextInt(100);
	}

	static public void DisplayTimedTextToPlayer(Object player, ILconstReal x, ILconstReal y, ILconstReal duration, ILconstString msg) {
		System.out.println(msg.getVal());
	}

	@Override
	public ILconst invoke(String funcname, ILconst[] args) {
		nextMethod: for (Method method : this.getClass().getMethods()) {
			if (method.getName().equals(funcname)) {
				Object r = null;
				try {
					int i = 0;
					for (Class<?> paramType : method.getParameterTypes()) {
						if (!paramType.isAssignableFrom(args[i].getClass())) {
							continue nextMethod;
						}
						i++;
					}
					r = method.invoke(null, (Object[]) args);
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
		for (int i=0; i<args.length; i++) {
			parameterTypes[i] = "" + args[i];
		}
		System.err.println("native function " + funcname + "("+ Utils.printSep(", ", parameterTypes) +  ") can not be executed at compile time.");
		return ILconstNull.instance();
//		throw new InterprationError("native function " + funcname + " can not be executed at compile time.");
	}
}
