package de.peeeq.wurstscript.intermediateLang.interpreter;

import de.peeeq.wurstscript.intermediateLang.ILconst;

import java.io.PrintStream;

public interface NativesProvider {

    ILconst invoke(String funcname, ILconst[] args) throws NoSuchNativeException;

    void setOutStream(PrintStream outStream);

}
