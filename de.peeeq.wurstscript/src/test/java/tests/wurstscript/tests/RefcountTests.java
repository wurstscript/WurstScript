package tests.wurstscript.tests;

import de.peeeq.wurstio.jassinterpreter.DebugPrintError;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class RefcountTests extends WurstScriptTest {


    @Test
    public void simpleRefcount() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "@managedRefCount class A",
                "    ondestroy",
                "        testSuccess()",
                "init",
                "    let a = new A", // gets automatically deallocated
                "endpackage"
        );
    }


}
