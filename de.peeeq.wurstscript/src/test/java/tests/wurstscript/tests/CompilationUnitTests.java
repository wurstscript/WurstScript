package tests.wurstscript.tests;

import org.testng.annotations.Test;

/**
 * Tests using using more than one compilation unit
 */
public class CompilationUnitTests extends WurstScriptTest {


    @Test
    public void packages() {
        testAssertOk(false, false,
                compilationUnit("A.wurst",
                        "package A",
                        "endpackage"
                ),
                compilationUnit("B.wurst",
                        "package B",
                        "	import A",
                        "endpackage"
                ));
    }

    @Test
    public void jass() {
        testAssertOk(false, false,
                compilationUnit("A.wurst",
                        "function foo takes nothing returns nothing",
                        "endfunction",
                        ""
                ),
                compilationUnit("B.wurst",
                        "package B",
                        "	init",
                        "		foo()",
                        "endpackage"
                ));
    }

    @Test
    public void jassLocalHandleDefaultsToNull() {
        testAssertOkLinesWithStdLib(false,
            "function tiw takes nothing returns nothing",
            "local location uiw",
            "set uiw = null",
            "endfunction",
            "package B",
            "    init",
            "        tiw()",
            "endpackage"
        );
    }

    @Test
    public void jassLocalHandleReadWithoutExplicitInitReportsEarly() {
        testAssertErrorsLinesWithStdLib(false, "read before explicit initialization in input JASS",
            "function tiw takes nothing returns nothing",
            "local location uiw",
            "call RemoveLocation(uiw)",
            "endfunction",
            "package B",
            "    init",
            "        tiw()",
            "endpackage"
        );
    }

}
