package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.DebugPrintError;
import de.peeeq.wurstio.jassinterpreter.Implements;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;

import java.io.PrintStream;

public class OutputProvider extends Provider {
    protected PrintStream outStream = System.err;

    public OutputProvider(ILInterpreter interpreter) {
        super(interpreter);
    }

    @Implements(funcNames = {"BJDebugMsg", "DisplayTimedTextToPlayer", "println"})
    public void println(ILconstString msg) {
        outStream.println(msg.getVal());
    }

    public void $debugPrint(ILconstString msg) {
        outStream.println(msg.getVal());
        throw new DebugPrintError(msg.getVal());
    }

    public void testSuccess() {
        throw TestSuccessException.instance;
    }

    public void testFail(ILconstString msg) {
        throw new TestFailException(msg.getVal());
    }
}
