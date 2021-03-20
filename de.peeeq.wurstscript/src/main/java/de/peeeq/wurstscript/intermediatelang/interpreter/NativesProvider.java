package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.wurstscript.intermediatelang.ILconst;
import java.io.PrintStream;

public interface NativesProvider {

  ILconst invoke(String funcname, ILconst[] args) throws NoSuchNativeException;

  void setOutStream(PrintStream outStream);
}
