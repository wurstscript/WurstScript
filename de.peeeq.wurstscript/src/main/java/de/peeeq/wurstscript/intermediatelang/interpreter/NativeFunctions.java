package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.wurstscript.intermediatelang.ILconst;

import java.io.PrintStream;

public class NativeFunctions implements NativesProvider {

    @Override
    public ILconst invoke(String funcname, ILconst[] args) throws NoSuchNativeException {
        throw new NoSuchNativeException("No native function " + funcname + " found.");
    }

    @Override
    public void setOutStream(PrintStream outStream) {
        // TODO Auto-generated method stub
    }

}
