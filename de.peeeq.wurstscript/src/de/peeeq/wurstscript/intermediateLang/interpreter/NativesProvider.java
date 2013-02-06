package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.io.PrintStream;

import de.peeeq.wurstscript.intermediateLang.ILconst;

public interface NativesProvider {

	ILconst invoke(String funcname, ILconst[] args) throws NoSuchNativeException;

	void setOutStream(PrintStream outStream);

}
