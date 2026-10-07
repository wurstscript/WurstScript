package tests.wurstscript.interpreter;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.interpreter.EvaluateExpr;
import de.peeeq.wurstscript.intermediatelang.interpreter.LocalState;
import de.peeeq.wurstscript.intermediatelang.interpreter.ProgramState;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.TypesHelper;
import org.testng.annotations.Test;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/** Reading a variable is the interpreter's most frequent operation; it must not allocate. */
public class InterpreterAllocationTest {

    /** A program with one function, which owns {@code local}: a variable must hang in the program to be read. */
    private static ProgramState stateWithLocal(Element trace, ImVar local) {
        ImFunction function = JassIm.ImFunction(trace, "f", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(local), JassIm.ImStmts(), Collections.emptyList());
        ImProg prog = JassIm.ImProg(trace, JassIm.ImVars(), JassIm.ImFunctions(function), JassIm.ImMethods(),
            JassIm.ImClasses(), JassIm.ImTypeClassFuncs(), new HashMap<>());
        return new ProgramState(new WurstGuiLogger(), prog, true);
    }

    @Test
    public void readingALocalVariableDoesNotAllocate() {
        Element trace = Ast.NoExpr();
        ImVar var = JassIm.ImVar(trace, TypesHelper.imInt(), "counter", false);
        ProgramState state = stateWithLocal(trace, var);
        ImVarAccess access = JassIm.ImVarAccess(var);
        LocalState local = new LocalState();
        local.setVal(var, ILconstInt.create(7));

        int reads = 20_000;
        for (int i = 0; i < reads; i++) {
            EvaluateExpr.eval(access, state, local);
        }
        com.sun.management.ThreadMXBean threads = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long thread = Thread.currentThread().threadId();
        long before = threads.getThreadAllocatedBytes(thread);
        for (int i = 0; i < reads; i++) {
            EvaluateExpr.eval(access, state, local);
        }
        long perRead = (threads.getThreadAllocatedBytes(thread) - before) / reads;

        assertTrue(perRead < 16, "a read allocated " + perRead + " bytes");
    }

    @Test
    public void aLocalVariableWithoutValueIsReportedByName() {
        Element trace = Ast.NoExpr();
        ImVar var = JassIm.ImVar(trace, TypesHelper.imInt(), "counter", false);
        ProgramState state = stateWithLocal(trace, var);

        InterpreterException e = expectThrows(InterpreterException.class,
            () -> EvaluateExpr.eval(JassIm.ImVarAccess(var), state, new LocalState()));

        assertTrue(e.getMessage().contains("Local variable") && e.getMessage().contains("counter"), e.getMessage());
    }

    @Test
    public void aLocalVariableWithAValueIsReturned() {
        Element trace = Ast.NoExpr();
        ImVar var = JassIm.ImVar(trace, TypesHelper.imInt(), "counter", false);
        ProgramState state = stateWithLocal(trace, var);
        LocalState local = new LocalState();
        local.setVal(var, ILconstInt.create(7));

        ILconst value = EvaluateExpr.eval(JassIm.ImVarAccess(var), state, local);

        assertEquals(value, ILconstInt.create(7));
    }
}
