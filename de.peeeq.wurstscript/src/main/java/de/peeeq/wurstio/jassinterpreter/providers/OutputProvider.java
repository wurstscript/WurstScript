package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.DebugPrintError;
import de.peeeq.wurstio.jassinterpreter.Implements;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;

import java.io.PrintStream;

public class OutputProvider extends Provider {
    protected PrintStream outStream = System.err;

    public OutputProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public void DisplayTimedTextToPlayer(IlConstHandle player, ILconstReal x, ILconstReal y, ILconstReal duration, ILconstString msg) {
        outStream.println(msg.getVal());
    }

    @Implements(funcNames = {"BJDebugMsg", "println"})
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
