package tests.wurstscript.tests;

import org.testng.annotations.Test;

public class ScopingTests extends WurstScriptTest {

	@Test
	public void test_duplicates_cu() {
		testAssertErrorsLines(false, "An element with name A already exists",
				"package A",
				"endpackage",
				"package A",
				"endpackage",
				"package B",
				"	import A",
				"endpackage");
	}


    @Test
    public void test_duplicates_jass_func() {
        testAssertErrorsLines(false, "already defined",
                "function foo takes nothing returns integer",
                "	return 3",
                "endfunction",
                "function foo takes nothing returns integer",
                "	return foo()",
                "endfunction",
                "package A",
                "	init",
                "		foo()",
                "endpackage");
    }


    @Test
    public void test_import_same() {
         testAssertErrorsLines(false, "ambiguous",
                "package A",
                "	public int x = 2",
                "endpackage",
                "package B",
                "	public int x = 3",
                "endpackage",
                "package test",
                "	import B",
                "	import A",
                "	native testSuccess()",
                "	init",
                "		if x == 3",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void test_import_same_package() {
        testAssertOkLines(false,
                "package A",
                "	public int x = 2",
                "endpackage",
                "package B",
                "	public int x = 3",
                "endpackage",
                "package test",
                "	import B",
                "	import A",
                "	native testSuccess()",
                "	int x = 4",
                "	init",
                "		if x == 4", // prefer var from current package
                "			testSuccess()",
                "endpackage");
    }


    @Test
    public void privateClassMember() {
        testAssertErrorsLines(false, "private",
                "package A",
                "class C",
                "	private static int b = 0",
                "init",
                "	C.b++",
                "endpackage");
    }

    @Test
    public void privateCode() {
        testAssertErrorsLines(false, "not visible",
                "package A",
                "class C",
                "	private static function foo()",
                "init",
                "	code c = function C.foo",
                "endpackage");
    }


}
