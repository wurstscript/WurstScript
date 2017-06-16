package tests.wurstscript.tests;

import de.peeeq.wurstio.UtilsIO;
import de.peeeq.wurstscript.utils.Utils;
import org.junit.Test;

public class ArrayTests extends WurstScriptTest {


    @Test
    public void testArray1() {
        assertOk(true,
                "	int array blub",
                "	init",
                "		blub[5] = 3",
                "		if blub[5] == 3",
                "			testSuccess()",
                "",
                "");
    }

    @Test
    public void testArrayParam() {
        testAssertErrorsLines(false, "arrays as parameter",
                "package Test",
                "function foo(int array a)",
                "endpackage");
    }


    @Test
    public void testArray_jass() {
        String[] lines = {
                "native testSuccess takes nothing returns nothing",
                "native testFail takes string s returns nothing",

                "globals",
                "	integer array blub",
                "endglobals",

                "function foo takes nothing returns nothing ",
                "	set blub[5] = 3",
                "	if blub[5] == 3 then",
                "		call testSuccess()",
                "	endif",
                "endfunction",

                "package test",
                "	init",
                "		foo()",
                "endpackage"
        };
        testAssertOk("", true, Utils.join(lines, "\n"));
    }


    public void assertOk(boolean executeProg, String... input) {
        String prog = "package test\n" +
                "	native testFail(string msg)\n" +
                "	native testSuccess()\n" +
                Utils.join(input, "\n") + "\n" +
                "endpackage\n";
        testAssertOk(UtilsIO.getMethodName(1), executeProg, prog);
    }

}
