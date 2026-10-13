package tests.wurstscript.tests;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.FunctionCall;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.Utils;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class PositionTests extends WurstScriptTest {

    @Test
    public void testFuncCallPos() {
        WurstModel model = testScript("blub", false, String.join(System.lineSeparator(),
                "package Test",
                "function foo() returns int",
                "    return foo()",
                ""
        ));

        FunctionCall c = (FunctionCall) Utils.getAstElementAtPos(model.get(0), 3, 12, false).get();

        WPos pos = c.attrErrorPos();

    }

    /**
     * The program as a whole is in no one file, so its position is a stand-in. It used to be worked out
     * from every compilation unit each time it was asked for, and code the compiler generates is traced
     * to the program, so translating it walked the whole program once per statement.
     */
    @Test
    public void theProgramHasAStandInPosition() {
        WurstModel model = testScript("blub", false, String.join(System.lineSeparator(),
                "package Test",
                "function foo() returns int",
                "    return 42",
                ""
        ));

        WPos pos = model.attrSource();
        assertTrue(pos.isArtificial(), pos.toString());
        assertEquals(pos.getFile(), "<source of WurstModel not found>");
    }

    /** An element without a position names its kind in the stand-in; it used to print itself there. */
    @Test
    public void aStandInPositionNamesTheKindOfElement() {
        WurstModel model = testScript("blub", false, String.join(System.lineSeparator(),
                "package Test",
                "function foo() returns int",
                "    return 42",
                ""
        ));

        for (CompilationUnit cu : model) {
            if (cu.getJassDecls().isEmpty()) {
                assertEquals(cu.getJassDecls().attrSource().getFile(), "<source of JassToplevelDeclarations not found>");
                return;
            }
        }
        throw new AssertionError("expected a compilation unit without Jass declarations");
    }
}
