package tests.wurstscript.tests;

import de.peeeq.wurstio.UtilsIO;
import de.peeeq.wurstscript.utils.Utils;
import org.junit.Test;

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
        testAssertErrorsLines(false, "Expected expression of type integer in array initialization, but found string",
                "package test",
                "int array blub = [1,\"2\",3,4]"
        );
    }

    @Test
    public void array_init_local_fail1() {
        testAssertErrorsLines(false, "Expected expression of type integer in array initialization, but found string",
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





    public void assertOk(boolean executeProg, String... input) {
        String prog = "package test\n" +
                "native testFail(string msg)\n" +
                "native testSuccess()\n" +
                Utils.join(input, "\n") + "\n" +
                "endpackage\n";
        testAssertOk(UtilsIO.getMethodName(1), executeProg, prog);
    }

}
