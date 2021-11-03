package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.wurstscript.intermediatelang.ILconst;

import java.io.PrintStream;
import java.util.Optional;

public interface NativesProvider {

   Optional<NativeHandle> find(String funcname);

    void setOutStream(PrintStream outStream);

    interface NativeHandle {
        ILconst invoke(ILconst[] args);
    }

}
