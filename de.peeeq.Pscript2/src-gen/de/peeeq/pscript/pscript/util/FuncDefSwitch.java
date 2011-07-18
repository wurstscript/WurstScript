package de.peeeq.pscript.pscript.util;
import de.peeeq.pscript.pscript.*;
public abstract class FuncDefSwitch <T> {
	abstract public T caseNativeFunc(NativeFunc nativeFunc);
	public T doSwitch(FuncDef funcDef) {
if ( funcDef == null) throw new IllegalArgumentException("Switch element must not be null.");
		if (funcDef instanceof NativeFunc) return caseNativeFunc((NativeFunc)funcDef);
		throw new Error("Switch did not match any case: " + funcDef);
	}
}

