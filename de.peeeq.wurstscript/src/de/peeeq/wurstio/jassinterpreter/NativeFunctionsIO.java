package de.peeeq.wurstio.jassinterpreter;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.intermediateLang.ILconstBool;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.intermediateLang.ILconstNull;
import de.peeeq.wurstscript.intermediateLang.ILconstReal;
import de.peeeq.wurstscript.intermediateLang.ILconstString;
import de.peeeq.wurstscript.intermediateLang.IlConstHandle;
import de.peeeq.wurstscript.intermediateLang.interpreter.NativesProvider;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;

/**
 * provides native functions which are invoked via reflection a function should
 * only take and return ILconsts
 * 
 * remember that all functions must be
 */
@SuppressWarnings("ucd") // ignore unused code detector warnings, because this class uses reflection
public class NativeFunctionsIO extends ReflectionBasedNativeProvider implements NativesProvider {

	private Random r = new Random(0);
	
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

	@Native
	public ILconstString R2S(ILconstReal r) {
		return new ILconstString("" + r.getVal());
	}
	
	public ILconstInt R2I(ILconstReal i) {
		return new ILconstInt((int) i.getVal());
	}
	
	public ILconstReal I2R(ILconstInt i) {
		return new ILconstReal(i.getVal());
	}
	
	@Native
	public ILconstInt StringHash(ILconstString s) {
		// TODO can we use same string hash function as used in wc3?
		return new ILconstInt(s.getVal().hashCode());
	}
	
	@Native
	public ILconstNull Player(ILconstInt p) {
		return ILconstNull.instance();
	}

	@Native
	public ILconstInt GetPlayerId(ILconstNull p) {
		return ILconstInt.create(0);
	}
	
	public IlConstHandle InitHashtable() {
		return new IlConstHandle(getRandomName("ht"), Maps.newLinkedHashMap());
	}

	public void SaveInteger(IlConstHandle ht, ILconstInt key1, ILconstInt key2, ILconstInt value) {
		@SuppressWarnings("unchecked")
		Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
		Map<Integer, Object> map2 = map.get(key1.getVal());
		if (map2 == null) {
			map2 = Maps.newLinkedHashMap();
			map.put(key1.getVal(), map2);
		}
		map2.put(key2.getVal(), value);
	}

	public ILconstInt LoadInteger(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
		@SuppressWarnings("unchecked")
		Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
		Map<Integer, Object> map2 = map.get(key1.getVal());
		if (map2 != null) {
			Object value = map2.get(key2.getVal());
			if (value instanceof ILconstInt) {
				return (ILconstInt) value;
			}
		}
		return ILconstInt.create(0);
	}
	
	public ILconstInt FlushChildHashtable(IlConstHandle ht, ILconstInt parentKey) {
		Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
		map.remove(parentKey.getVal());
		return ILconstInt.create(0);
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
	
	public ILconstInt StringLength(ILconstString string) {
		return new ILconstInt(string.getVal().length());
	}
	
	public ILconstString SubString(ILconstString string, ILconstInt start, ILconstInt end ) {
		return new ILconstString(string.getVal().substring(start.getVal(), end.getVal()));
	}
	
	public ILconstString StringCase(ILconstString string, ILconstBool upperCase) {
		return new ILconstString(
				upperCase.getVal() ? 
				string.getVal().toUpperCase()
				: string.getVal().toLowerCase());
	}
	
	public void testPrint(ILconstString msg) {
		outStream.println(msg.getVal());
	}
	
	
	public ILconstReal GetRandomReal(ILconstReal a, ILconstReal b) {
		return new ILconstReal(a.getVal() + r.nextFloat()*(b.getVal()-a.getVal()));
	}
	public ILconstInt GetRandomInt(ILconstInt a, ILconstInt b) {
		return new ILconstInt(a.getVal() + r.nextInt(1+b.getVal()-a.getVal()));
	}
}
