package de.peeeq.wurstscript.jassinterpreter;

import java.io.PrintStream;
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
import de.peeeq.wurstscript.intermediateLang.ILconstReal;
import de.peeeq.wurstscript.intermediateLang.ILconstString;
import de.peeeq.wurstscript.intermediateLang.IlConstHandle;
import de.peeeq.wurstscript.intermediateLang.interpreter.ILconstError;
import de.peeeq.wurstscript.intermediateLang.interpreter.NativesProvider;
import de.peeeq.wurstscript.utils.Utils;

/**
 * provides native functions which are invoked via reflection a function should
 * only take and return ILconsts
 * 
 * remember that all functions must be
 */
public class NativeFunctions implements NativesProvider {

	@Native
	public void testSuccess() {
		throw TestSuccessException.instance;
	}

	public void testFail(ILconstString msg) {
		throw new TestFailException(msg.getVal());
	}

	public void BJDebugMsg(ILconstString msg) {
		outStream.println(msg.getVal());
	}

	public void println(ILconstString msg) {
		outStream.println(msg.getVal());
	}

	public void $debugPrint(ILconstString msg) {
		outStream.println(msg.getVal());
		throw new DebugPrintError(msg.getVal());
	}

	public ILconstInt ModuloInteger(ILconstInt a, ILconstInt b) {
		return new ILconstInt(a.getVal() % b.getVal());
	}

	public ILconstReal ModuloReal(ILconstReal a, ILconstReal b) {
		return new ILconstReal(a.getVal() % b.getVal());
	}

	@Native
	public ILconstString I2S(ILconstInt i) {
		return new ILconstString("" + i.getVal());
	}

	public ILconstNull Player(ILconstInt p) {
		return ILconstNull.instance();
	}

	public IlConstHandle InitHashtable() {
		return new IlConstHandle(getRandomName("ht"), new HashMap<Integer, Map<Integer, Object>>());
	}

	public void SaveInteger(IlConstHandle ht, ILconstInt key1, ILconstInt key2, ILconstInt value) {
		Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
		Map<Integer, Object> map2 = map.get(key1.getVal());
		if (map2 == null) {
			map2 = Maps.newHashMap();
			map.put(key1.getVal(), map2);
		}
		map2.put(key2.getVal(), value);
	}

	public ILconstInt LoadInteger(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
		Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
		Map<Integer, Object> map2 = map.get(key1.getVal());
		if (map2 == null) {
			return ILconstInt.create(0);
		}
		return (ILconstInt) map2.get(key2.getVal());
	}

	public ILconstReal SquareRoot(ILconstReal r) {
		return new ILconstReal(Math.sqrt(r.getVal()));
	}
	
	public ILconstReal Sin(ILconstReal r) {
		return new ILconstReal(Math.sin(r.getVal()));
	}
	
	public ILconstReal Cos(ILconstReal r) {
		return new ILconstReal(Math.cos(r.getVal()));
	}
	
	public ILconstReal Tan(ILconstReal r) {
		return new ILconstReal(Math.tan(r.getVal()));
	}
	
	public ILconstReal ATan(ILconstReal r) {
		return new ILconstReal(Math.atan(r.getVal()));
	}
	
	
	public ILconstReal Atan2(ILconstReal y, ILconstReal x) {
		return new ILconstReal(Math.atan2(y.getVal(), x.getVal()));
	}
	
	
	public IlConstHandle GetLocalPlayer() {
		return new IlConstHandle("Local Player", "local player");
	}

	private String getRandomName(String string) {
		return string + new Random().nextInt(100);
	}

	public void DisplayTimedTextToPlayer(Object player, ILconstReal x, ILconstReal y, ILconstReal duration,
			ILconstString msg) {
		outStream.println(msg.getVal());
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
		return new ILconstError(funcname);
		// throw new InterprationError("native function " + funcname +
		// " can not be executed at compile time.");
	}
	
	private PrintStream outStream = System.out;

	@Override
	public void setOutStream(PrintStream outStream) {
		this.outStream = outStream;
	}
}
