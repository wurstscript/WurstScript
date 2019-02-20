package tests.wurstscript.tests;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.WurstModel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class InterpreterTests extends WurstScriptTest {




    @Test
    public void testR2SW() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "native testFail(string msg)",
                "@extern native R2SW(real r, integer width, integer precision) returns string",
                "native println(string s)",
                "init",
                "    if R2SW(1116.0, 2, 2) != \"1116.00\"",
                "        testFail(\"failed A \" + R2SW(1116.0, 2, 2))",
                "    if R2SW(1116.123, 10, 1) != \"1116.1    \"",
                "        testFail(\"failed B \" + R2SW(1116.123, 10, 1))",
                "    testSuccess()"
        );
    }

}
