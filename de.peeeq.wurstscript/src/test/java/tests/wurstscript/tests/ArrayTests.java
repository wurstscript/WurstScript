package tests.wurstscript.tests;

import de.peeeq.wurstio.UtilsIO;
import de.peeeq.wurstscript.utils.Utils;
import org.testng.annotations.Test;

public class ArrayTests extends WurstScriptTest {


    @Test
    public void testArray1() {
        assertOk(true,
                "int array blub",
                "init",
                "	blub[5] = 3",
                "	if blub[5] == 3",
                "		testSuccess()"
        );
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


    @Test
    public void array_init_global1() {
        assertOk(true,
                "int array blub = [1,2,3,4]",
                "init",
                "	if blub[2] == 3",
                "		testSuccess()"
        );
    }

    @Test
    public void array_init_global_type_inference() {
        assertOk(true,
                "var blub = [1,2,3,4]",
                "init",
                "	if blub[2] == 3",
                "		testSuccess()"
        );
    }

    @Test
    public void array_init_local_type_inference() {
        assertOk(true,
                "init",
                "	var blub = [1,2,3,4]",
                "	if blub[2] == 3",
                "		testSuccess()"
        );
    }

    @Test
    public void array_init_global_fail1() {
        testAssertErrorsLines(false, "Expected expression of type int in array initialization, but found string",
                "package test",
                "int array blub = [1,\"2\",3,4]"
        );
    }

    @Test
    public void array_init_local_fail1() {
        testAssertErrorsLines(false, "Expected expression of type int in array initialization, but found string",
                "package test",
                "function foo()",
                "   int array blub = [1,\"2\",3,4]"
        );
    }

    @Test
    public void array_init_global_fail2() {
        testAssertErrorsLines(false, "Expected expression of type B in array initialization, but found C",
                "package test",
                "class A",
                "class B extends A",
                "class C extends A",
                "var blub = [new B, new C]"
        );
    }

    @Test
    public void array_init_global_fail3() {
        testAssertErrorsLines(false, "Cannot assign string to string array",
                "package test",
                "string array blub = \"Hello\""
        );
    }

    @Test
    public void array_init_length1() {
        assertOk(true,
                "init",
                "	var blub = [1,2,3,4]",
                "	if blub.length == 4",
                "		testSuccess()"
        );
    }

    @Test
    public void array_init_length2() {
        assertOk(true,
                "init",
                "	int array[7] blub",
                "	if blub.length == 7",
                "		testSuccess()"
        );
    }


    @Test
    public void multiArrayDefaultValue() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "class C",
                "    int array[5] v",
                "init",
                "    let c = new C",
                "    c.v[2] = c.v[3] + 1",
                "    if c.v[2] == 1",
                "        testSuccess()"
        );
    }

    @Test
    public void multiArrayInit() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "class C",
                "    int array[3] v = [7, 8, 9]",
                "init",
                "    let c = new C",
                "    if c.v[0] == 7 and c.v[1] == 8 and c.v[2] == 9",
                "        testSuccess()"
        );
    }

    @Test
    public void multiArrayWrongSize() {
        testAssertErrorsLines(true, "Array variable v is an array of size 3, but is initialized with 4 values here.",
                "package test",
                "class C",
                "    int array[3] v = [7, 8, 9, 10]"
        );
    }

    @Test
    public void conditionalWithArray() { // see #631
        testAssertOkLines(false,
                "package test",
                "bool cond = true",
                "int array[3] zzzz",
                "function ffff() returns int",
                "    return cond ? zzzz[1] : 0");
    }


    public void assertOk(boolean executeProg, String... input) {
        String prog = "package test\n" +
                "native testFail(string msg)\n" +
                "native testSuccess()\n" +
                Utils.join(input, "\n") + "\n" +
                "endpackage\n";
        testAssertOk(UtilsIO.getMethodName(1), executeProg, prog);
    }

    @Test
    public void classArrayInit() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "Test array ar",
            "class Test",
            "    int x",
            "init",
            "    if ar[5] == null",
            "        testSuccess()");
    }

    @Test
    public void intArrayInit() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "int array ar",
            "init",
            "    if ar[5] == 0",
            "        testSuccess()");
    }


}
