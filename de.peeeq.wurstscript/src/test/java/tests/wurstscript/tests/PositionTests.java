package tests.wurstscript.tests;

import de.peeeq.wurstscript.ast.FunctionCall;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.Utils;
import org.testng.annotations.Test;

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
        System.out.println("pos = " + pos.getLine());
        System.out.println("pos = " + pos.getStartColumn());
        System.out.println("pos = " + pos.getEndColumn());

    }
}
