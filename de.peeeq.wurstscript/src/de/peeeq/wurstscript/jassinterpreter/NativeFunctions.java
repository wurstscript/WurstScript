package de.peeeq.wurstscript.jassinterpreter;

import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.intermediateLang.ILconstReal;
import de.peeeq.wurstscript.intermediateLang.ILconstString;


/**
 * provides native functions which are invoked via reflection
 * a function should only take and return ILconsts
 * 
 *  remember that all functions must be static
 */
public class NativeFunctions {

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
	
	static public ILconstInt ModuloInteger(ILconstInt a, ILconstInt b) {
		return new ILconstInt(a.getVal() % b.getVal());
	}
	
	static public ILconstReal ModuloReal(ILconstReal a, ILconstReal b) {
		return new ILconstReal(a.getVal() % b.getVal());
	}
}
