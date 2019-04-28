package tests.wurstscript.tests;

import de.peeeq.wurstio.UtilsIO;
import de.peeeq.wurstscript.utils.Utils;
import org.testng.annotations.Test;

public class SimpleStatementTests extends WurstScriptTest {

    @Test
    public void testMod1() {
        assertOk(true,
                "if 10. % 2. == 0.",
                "	testSuccess()\n" +
                        "");
    }


    @Test
    public void testMod2() {
        assertOk(true,
                "if 2 mod 10 == 2",
                "	testSuccess()\n" +
                        "");
    }

    @Test
    public void testIf1() {
        assertOk(true,
                "if 2 == 2 ",
                "	testSuccess()\n" +
                        "");
    }

    @Test
    public void testIf2() {
        assertOk(true,
                "if 10 > 5 ",
                "	testSuccess()\n" +
                        "");
    }

    @Test
    public void testIf3() {
        assertOk(true,
                "if not 10 > 5 or not 5 > 10 ",
                "	testSuccess()\n" +
                        "");
    }

    @Test
    public void testIf4() {
        assertOk(true,
                "if 10 >= 10 ",
                "	testSuccess()\n" +
                        "");
    }

    @Test
    public void testIf5() {
        assertOk(true,
                "if -4 <= -4 ",
                "	testSuccess()\n" +
                        "");
    }

    @Test
    public void testIf6() {
        assertOk(true,
                "if 3 != 2 ",
                "	testSuccess()",
                "");
    }

    @Test
    public void testIf7() {
        assertOk(true,
                "if (10 == 10 and 5 == 5)",
                "	testSuccess()",
                "");
    }

    @Test
    public void testWhile1() {
        assertOk(true,
                "int x = 10",
                "while x > 2",
                "	x = x - 1",
                "if x == 2",
                "	testSuccess()",
                "");
    }

    @Test
    public void testWhileBreak() {
        assertOk(true,
                "int x = 10",
                "while true",
                "	x = x - 1",
                "	if x <= 2",
                "		break",
                "if x == 2",
                "	testSuccess()",
                "");
    }

    @Test
    public void testFor1() {
        assertOk(true,
                "int x = 0",
                "for int i = 1 to 10",
                "	x = x + 1",
                "if x == 10",
                "	testSuccess()",
                "");
    }

    @Test
    public void testFor2() {
        assertOk(true,
                "int x = 0",
                "for int i = 1 to 10",
                "	x = x + i",
                "if x == 55",
                "	testSuccess()",
                "");
    }

    @Test
    public void testForStep() {
        assertOk(true,
                "int x = 0",
                "for int i = 0 to 10 step 3",
                "	x = x + i",
                "if x == 18",
                "	testSuccess()",
                "");
    }

    @Test
    public void testForDownStep() {
        assertOk(true,
                "int x = 0",
                "for int i = 10 downto 0 step 3",
                "	x = x + i",
                "if x == 22",
                "	testSuccess()",
                "");
    }

    @Test
    public void testForIn() {
        test().executeProg(true).testLua(false).lines(
            "package test",
            "	class IntList",
            "		static int array elements",
            "		int size = 0",

            "		private function getOffset() returns int",
            "			return 64*((this castTo int)-1)",

            "		function add(int x) returns IntList",
            "			elements[getOffset() + size] = x",
            "			size++",
            "			return this", // 10

            "		function get(int i) returns int",
            "			return elements[getOffset() + i]",

            "		function iterator() returns IntListIterator",
            "			return new IntListIterator(this)",


            "	class IntListIterator", // 15
            "		IntList list",
            "		int pos = 0",

            "		construct(IntList list)",
            "			this.list = list",

            "		function hasNext() returns boolean", // 20
            "			return pos < list.size",

            "		function next() returns int",
            "			pos++",
            "			return list.get(pos-1)",

            "		function close()", // 25
            "			destroy this",


            "	init",
            "		IntList list = new IntList().add(7).add(3).add(5)",
            "		int sum = 0",
            "		for int i in list", // 30
            "			sum += i",
            "		if sum == 15",
            "			testSuccess()",


            "	native testSuccess()",
            "endpackage"
        );
    }


    @Test
    public void testForFrom() {
        test().executeProg(true).testLua(false).lines(
                "package test",
                "	class IntList",
                "		static int array elements",
                "		int size = 0",

                "		private function getOffset() returns int",
                "			return 64*((this castTo int)-1)",

                "		function add(int x) returns IntList",
                "			elements[getOffset() + size] = x",
                "			size++",
                "			return this", // 10

                "		function get(int i) returns int",
                "			return elements[getOffset() + i]",

                "		function iterator() returns IntListIterator",
                "			return new IntListIterator(this)",


                "	class IntListIterator", // 15
                "		IntList list",
                "		int pos = 0",

                "		construct(IntList list)",
                "			this.list = list", // 20

                "		function hasNext() returns boolean",
                "			return pos < list.size",

                "		function next() returns int",
                "			pos++",
                "			return list.get(pos-1)",

                "		function close()", // 25
                "			destroy this",


                "	init",
                "		IntList list = new IntList().add(7).add(3).add(5)",
                "		int sum = 0",
                "		let listIterator = list.iterator()",
                "		for int i from listIterator", // 30
                "			sum += i",
                "		listIterator.close()",
                "		if sum == 15",
                "			testSuccess()",


                "	native testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void testForFrom3() {
        testAssertOkLinesWithStdLib(false,
                "package test",
                "init",
                "	group g = CreateGroup()",
                "	for unit u from g",
                "		skip",
                "	DestroyGroup( g)"
        );

    }

    @Test
    public void testForFrom_once() {
        // for-from expression should be evaluated only once
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "class C",
                "	int x = 0",
                "	static int count = 0",
                "	construct()",
                "		count = count + 1",
                "	function next() returns int",
                "		x = x + 1",
                "		return x",
                "	function hasNext() returns boolean",
                "		return x < 10",
                "init",
                "	for i from new C()",
                "	if C.count == 1",
                "		testSuccess()"
        );

    }

    @Test
    public void test_inc() {
        assertOk(true,
                "int x = 5",
                "x++",
                "if x == 6",
                "	testSuccess()",
                "");
    }

    @Test
    public void test_dec() {
        assertOk(true,
                "int x = 5",
                "x--",
                "if x == 4",
                "	testSuccess()",
                "");
    }

    @Test
    public void test_pluseq() {
        assertOk(true,
                "int x = 5",
                "x += 2",
                "if x == 7",
                "	testSuccess()",
                "");
    }

    @Test
    public void test_multeq() {
        assertOk(true,
                "int x = 5",
                "x *= 2",
                "if x == 10",
                "	testSuccess()",
                "");
    }

    @Test
    public void test_diveq() {
        assertOk(true,
                "real x = 5",
                "x /= 2",
                "if x == 2.5",
                "	testSuccess()",
                "");
    }

    @Test
    public void test_diveqFail() {
        assertError(true, "Cannot assign real",
                "int x = 5",
                "x /= 2",
                "if x == 2",
                "	testSuccess()",
                "");
    }

    @Test
    public void test_minuseq() {
        assertOk(true,
                "int x = 5",
                "x -= 2",
                "if x == 3",
                "	testSuccess()",
                "");
    }

    @Test
    public void test_unitialized() {
        assertError(false, "may not have been initialized",
                "int x",
                "int y = 2", // 6
                "if y == 3", // 7
                "	x = 2", // 8
                "x++"); // 9
    }

    @Test
    public void test_unitialized2() {
        assertOk(false,
                "int x",
                "int y = 2",
                "if y == 3",
                "	x = 2",
                "else",
                "	x = 4",
                "x++");
    }

    @Test
    public void test_arrayUpdate() {
        assertOk(false,
                "int array x",
                "x[5] = 2",
                "x[5] += 3"
        );
    }


    @Test
    public void test_forloop() {
        assertOk(true,
                "int a = 10",
                "for int i = 1 to a",
                "	a++",
                "if a == 20",
                "	testSuccess()"
        );
    }

    @Test
    public void test_let() {
        assertOk(true,
                "let a = 11",
                "let b = a + 9",
                "if b == 20",
                "	testSuccess()"
        );
    }

    @Test
    public void test_let2() {
        assertError(true, "Cannot assign",
                "let a = 11",
                "a = a + 9",
                "if a == 20",
                "	testSuccess()"
        );
    }

    @Test
    public void test_var() {
        assertOk(true,
                "var a = 11",
                "var b = a + 9",
                "if b == 20",
                "	testSuccess()"
        );
    }

    @Test
    public void test_var_err() {
        assertError(false, "Could not infer the type of variable",
                "var a",
                "if true",
                "	a = 10"
        );
    }

    @Test
    public void test_var2() {
        assertOk(true,
                "var a = 11",
                "a = a + 9",
                "if a == 20",
                "	testSuccess()"
        );
    }

    public void assertOk(boolean executeProg, String... body) {
        String prog = "package test\n" +
                "	native testFail(string msg)\n" +
                "	native testSuccess()\n" +
                "	init \n" +
                "		" + Utils.join(body, "\n		") +
                "\n" +
                "endpackage\n";
        testAssertOk(UtilsIO.getMethodName(1), executeProg, prog);
    }

    @Test
    public void test_destroy() {
        testAssertErrorsLines(false, "Cannot destroy class",
                "package test",
                "class C",
                "init",
                "	destroy C"
        );
    }

    @Test
    public void test_array_assign() {
        testAssertErrorsLines(false, "Missing array index",
                "package test",
                "int array a",
                "int array b",
                "init",
                "	a = b"
        );
    }


    public void assertError(boolean executeProg, String expected, String... body) {
        String prog = "package test\n" +
                "	native testFail(string msg)\n" +
                "	native testSuccess()\n" +
                "	init \n" +
                "		" + Utils.join(body, "\n		") +
                "\n" +
                "endpackage\n";
        testAssertErrors(UtilsIO.getMethodName(1), executeProg, prog, expected);
    }

    @Test
    public void test_varAsStmt() {
        testAssertErrorsLines(false, "Use of variable x is an incomplete statement",
                "package test",
                "init",
                "	var x = 5",
                "	x"
        );
    }

    @Test
    public void intLiteralVar() {
        testAssertOkLines(false,
                "package test",
                "native testSuccess()",
                "function equals2(int x) returns boolean",
                "    return x == 2",
                "function equals2(real x) returns boolean",
                "    return x == 2",
                "init",
                "    var a = 2",
                "    if equals2(a)",
                "        testSuccess()"
        );
    }

    @Test
    public void intLiteralArray() {
        testAssertOkLines(false,
                "package test",
                "native testSuccess()",
                "function equals2(int x) returns boolean",
                "    return x == 2",
                "function equals2(real x) returns boolean",
                "    return x == 2",
                "init",
                "    var a = [1,2,3]",
                "    if equals2(a[1])",
                "        testSuccess()"
        );
    }


    @Test
    public void testArrayInit() {
        testAssertErrorsLines(false, "Array parameters",
                "package test",
                "native testSuccess()",
                "let a = [1,2]",
                "",
                "let b = [a]",
                ""
        );
    }

    @Test
    public void test_stupid_for_in() {
        testAssertErrorsLines(false, "doesn't provide a iterator() function",
                "package test",
                "init",
                "	for i in 42"
        );
    }

    @Test
    public void test_stupid_for_in2() {
        testAssertErrorsLines(false, "doesn't provide a proper next()",
                "package test",
                "class C",
                "	function iterator() returns int",
                "		return 42",
                "init",
                "	for i in new C"
        );
    }

    @Test
    public void test_stupid_for_from() {
        testAssertErrorsLines(false, "doesn't provide a proper next()",
                "package test",
                "init",
                "	for i from 42"
        );
    }

    @Test
    public void test_no_hasNext() {
        testAssertErrorsLines(false, "doesn't provide a hasNext() function that returns boolean",
                "package test",
                "class C",
                "	function iterator() returns C",
                "		return new C",
                "	function next() returns int",
                "		return 1",
                "init",
                "	for i in new C"
        );
    }

    @Test
    public void test_no_Next() {
        testAssertErrorsLines(false, "doesn't provide a proper next()",
                "package test",
                "class C",
                "	function iterator() returns C",
                "		return new C",
                "	function hasNext() returns boolean",
                "		return true",
                "init",
                "	for i in new C"
        );
    }

}
