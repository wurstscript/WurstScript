package tests.wurstscript.tests;

import de.peeeq.wurstio.UtilsIO;
import de.peeeq.wurstscript.utils.Utils;
import org.testng.annotations.Test;

public class SimpleFunctionTests extends WurstScriptTest {


    @Test
    public void testFuncref() {
        assertOk(false,
                "function foo(code c)",
                "	int i = 0",
                "function bar()",
                "	int j = 0",
                "init",
                "	foo( function bar )",
                "");
    }

    @Test
    public void testHasReturnIfs() {
        assertOk(false,
                "function foo(int x) returns int",
                "	if x == 3",
                "		return 1",
                "	else if x > 5",
                "		return 2",
                "	else",
                "		return 3",
                "");
    }


    @Test
    public void testUnreachableCode() {
        assertError(false, "Unreachable code",
                "function foo(int x) returns int",
                "	int i = 2",
                "	if x == 3",
                "		return 1",
                "		i++",
                "	return i",
                "");
    }


//	@Test
//	public void test_cyclic() {
//		assertError(false, "cyclic",
//				"function foo(int x) returns int",
//				"	return bar(x)",
//				"function bar(int x) returns int",
//				"	return blub(x)",
//				"function unrelated(int x) returns int",
//				"	return foo(x)",
//				"function blub(int x) returns int",
//				"	return foo(x)",
//				"");
//	}


    public void assertOk(boolean executeProg, String... body) {
        String prog = makeCode(body);
        testAssertOk(UtilsIO.getTestName(), executeProg, prog);
    }

    private String makeCode(String... body) {
        return "package test\n" +
                "	native testFail(string msg)\n" +
                "	native testSuccess()\n" +
                "	" + Utils.join(body, "\n	") +
                "\n" +
                "endpackage\n";
    }

    public void assertError(boolean executeProg, String expected, String... body) {
        String prog = makeCode(body);
        testAssertErrors(UtilsIO.getTestName(), executeProg, prog, expected);
    }

}
