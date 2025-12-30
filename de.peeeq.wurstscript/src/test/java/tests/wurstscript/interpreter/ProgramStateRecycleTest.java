package tests.wurstscript.interpreter;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.intermediatelang.ILconstObject;
import de.peeeq.wurstscript.intermediatelang.interpreter.ProgramState;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImClassType;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.JassIm;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class ProgramStateRecycleTest {

    @Test
    public void reusesObjectIdsAfterDeallocateWithinIdSpace() {
        Element trace = Ast.NoExpr();
        ImClass clazz = JassIm.ImClass(trace, "A", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImMethods(), JassIm.ImFunctions(), Collections.emptyList());
        ImProg prog = JassIm.ImProg(trace, JassIm.ImVars(), JassIm.ImFunctions(), JassIm.ImMethods(), JassIm.ImClasses(clazz), JassIm.ImTypeClassFuncs(), new HashMap<>());
        ProgramState state = new ProgramState(new WurstGuiLogger(), prog, true);

        ImClassType classType = JassIm.ImClassType(clazz, JassIm.ImTypeArguments());
        ILconstObject first = state.allocate(classType, trace);
        state.deallocate(first, clazz, trace);
        ILconstObject second = state.allocate(classType, trace);

        assertEquals(first.getObjectId(), second.getObjectId());
        assertFalse(second.isDestroyed());
    }

    @Test
    public void maintainsSeparateAndSharedIdSpaces() {
        Element trace = Ast.NoExpr();
        ImClass classA = JassIm.ImClass(trace, "A", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImMethods(), JassIm.ImFunctions(), Collections.emptyList());
        ImClass classB = JassIm.ImClass(trace, "B", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImMethods(), JassIm.ImFunctions(), Collections.emptyList());
        ImClassType classASuperType = JassIm.ImClassType(classA, JassIm.ImTypeArguments());
        ImClass classC = JassIm.ImClass(trace, "C", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImMethods(), JassIm.ImFunctions(), Collections.singletonList(classASuperType));

        ImProg prog = JassIm.ImProg(trace, JassIm.ImVars(), JassIm.ImFunctions(), JassIm.ImMethods(), JassIm.ImClasses(classA, classB, classC), JassIm.ImTypeClassFuncs(), new HashMap<>());
        ProgramState state = new ProgramState(new WurstGuiLogger(), prog, true);

        ImClassType typeA = JassIm.ImClassType(classA, JassIm.ImTypeArguments());
        ImClassType typeB = JassIm.ImClassType(classB, JassIm.ImTypeArguments());
        ImClassType typeC = JassIm.ImClassType(classC, JassIm.ImTypeArguments());

        ILconstObject a1 = state.allocate(typeA, trace);
        ILconstObject b1 = state.allocate(typeB, trace);

        assertEquals(a1.getObjectId(), 1);
        assertEquals(b1.getObjectId(), 1, "Independent classes should start their own id counters.");

        state.deallocate(a1, classA, trace);
        state.deallocate(b1, classB, trace);

        ILconstObject c1 = state.allocate(typeC, trace);
        assertEquals(c1.getObjectId(), a1.getObjectId(), "Subclass should share the id space with its superclass.");

        ILconstObject b2 = state.allocate(typeB, trace);
        assertEquals(b2.getObjectId(), b1.getObjectId(), "Independent id space should recycle its own ids.");
    }

    @Test
    public void stressRecyclesAcrossMultipleIdSpaces() {
        Element trace = Ast.NoExpr();
        ImClass classA = JassIm.ImClass(trace, "A", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImMethods(), JassIm.ImFunctions(), Collections.emptyList());
        ImClass classB = JassIm.ImClass(trace, "B", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImMethods(), JassIm.ImFunctions(), Collections.emptyList());
        ImClassType classASuperType = JassIm.ImClassType(classA, JassIm.ImTypeArguments());
        ImClass classC = JassIm.ImClass(trace, "C", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImMethods(), JassIm.ImFunctions(), Collections.singletonList(classASuperType));

        ImProg prog = JassIm.ImProg(trace, JassIm.ImVars(), JassIm.ImFunctions(), JassIm.ImMethods(), JassIm.ImClasses(classA, classB, classC), JassIm.ImTypeClassFuncs(), new HashMap<>());
        ProgramState state = new ProgramState(new WurstGuiLogger(), prog, true);

        ImClassType typeA = JassIm.ImClassType(classA, JassIm.ImTypeArguments());
        ImClassType typeB = JassIm.ImClassType(classB, JassIm.ImTypeArguments());
        ImClassType typeC = JassIm.ImClassType(classC, JassIm.ImTypeArguments());

        for (int i = 0; i < 250; i++) {
            ILconstObject a = state.allocate(typeA, trace);
            ILconstObject b = state.allocate(typeB, trace);
            if (i % 5 == 0) {
                ILconstObject c = state.allocate(typeC, trace);
                state.deallocate(c, classC, trace);
            }
            state.deallocate(a, classA, trace);
            state.deallocate(b, classB, trace);
        }

        ILconstObject reusedA = state.allocate(typeA, trace);
        state.deallocate(reusedA, classA, trace);
        ILconstObject reusedC = state.allocate(typeC, trace);
        ILconstObject reusedB = state.allocate(typeB, trace);

        assertEquals(reusedA.getObjectId(), 1, "Repeated recycling should keep the smallest free id available.");
        assertEquals(reusedC.getObjectId(), reusedA.getObjectId(), "Classes sharing an id space must recycle together.");
        assertEquals(reusedB.getObjectId(), 1, "Independent id space should recycle separately.");
    }
}
