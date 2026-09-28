package tests.wurstscript.tests;

import de.peeeq.wurstio.UtilsIO;
import de.peeeq.wurstscript.utils.Utils;
import org.testng.annotations.Test;

public class FlowAnalysisTests extends WurstScriptTest {

    @Test
    public void testReturns1() {
        assertOk(false,
                "function foo(int i) returns int",
                "	if i == 2",
                "		return 3",
                "	else",
                "		return 2"
        );
    }

    @Test
    public void testReturns2() {
        assertError(false, "missing a return",
                "function foo(int i) returns int",
                "	if i == 2",
                "		return 3",
                "	else",
                "		skip"
        );
    }

    @Test
    public void testReturns3() {
        assertError(false, "missing a return",
                "function foo(int i) returns int",
                "	var j = i",
                "	while j > 5",
                "		j--"
        );
    }


    @Test
    public void testReturns4() {
        assertError(false, "missing a return",
                "function foo(int i) returns int",
                "	skip"
        );
    }


    @Test
    public void testReturns5() {
        assertOk(false,
                "function foo(int i) returns int",
                "	var j = i",
                "	while true",
                "		j--",
                "		if j < 0",
                "			break",
                "	return 3"
        );
    }

    @Test
    public void testUnreachable1() {
        assertError(false, "Unreachable code",
                "function foo(int i) returns int",
                "	if i < 5",
                "		return 4",
                "	else",
                "		return 5",
                "	return 3"
        );
    }

    @Test
    public void testInitalized() {
        assertError(false, "may not have been initialized",
                "function foo(int i) returns int",
                "	int j",
                "	if i < 5",
                "		j = 4",
                "	else",
                "		skip",
                "	return j"
        );
    }

    public void assertOk(boolean executeProg, String... body) {
        String prog = makeProg(body);
        testAssertOk(UtilsIO.getMethodName(1), executeProg, prog);
    }


    public void assertError(boolean executeProg, String expected, String... body) {
        String prog = makeProg(body);
        testAssertErrors(UtilsIO.getMethodName(1), executeProg, prog, expected);
    }


    private String makeProg(String... body) {
        String prog = "package test\n" +
                "native testFail(string msg)\n" +
                "native testSuccess()\n" + Utils.join(body, "\n") + "\n";
        return prog;
    }



    @Test
    public void destroyDataflowTest() {
        testAssertErrorsLines(false, "Variable a may have been destroyed already",
                "package test",
                "class A",
                "    function foo()",
                "init ",
                "    let a = new A()",
                "    destroy a",
                "    a.foo()"
        );
    }

    @Test
    public void destroyParameterThenUseIsReported() {
        testAssertErrorsLines(false, "Variable a may have been destroyed already",
                "package test",
                "class A",
                "    function foo()",
                "function consume(A a)",
                "    destroy a",
                "    a.foo()",
                "init",
                "    consume(new A())"
        );
    }

    @Test
    public void destroyShortParameterThenUseIsReported() {
        testAssertErrorsLines(false, "Variable a may have been destroyed already",
                "package test",
                "class A",
                "    function foo()",
                "interface Consumer",
                "    function accept(A a)",
                "function apply(Consumer c)",
                "    c.accept(new A())",
                "init",
                "    apply((A a) -> begin",
                "        destroy a",
                "        a.foo()",
                "    end)"
        );
    }

    @Test
    public void destroyThisDataflowTest() {
        testAssertErrorsLines(false, "Cannot access 'this' because it might already have been destroyed.",
                "package test",
                "class A",
                "    function foo()",
                "    function bar()",
                "        destroy this",
                "        foo()"
        );
    }

    @Test
    public void dataflowConvergesForManyMutatedLoopLocals() {
        testAssertOkLines(false,
                "package test",
                "function probe()",
                "    int a01 = 0",
                "    int a02 = 0",
                "    int a03 = 0",
                "    int a04 = 0",
                "    int a05 = 0",
                "    int a06 = 0",
                "    int a07 = 0",
                "    int a08 = 0",
                "    int a09 = 0",
                "    int a10 = 0",
                "    int a11 = 0",
                "    int a12 = 0",
                "    int a13 = 0",
                "    int a14 = 0",
                "    int a15 = 0",
                "    int a16 = 0",
                "    while true",
                "        a01++",
                "        a02++",
                "        a03++",
                "        a04++",
                "        a05++",
                "        a06++",
                "        a07++",
                "        a08++",
                "        a09++",
                "        a10++",
                "        a11++",
                "        a12++",
                "        a13++",
                "        a14++",
                "        a15++",
                "        a16++"
        );
    }

}
