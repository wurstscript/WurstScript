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

    /**
     * A class field whose initializer refers to itself must be rejected.
     * Globals are caught earlier by "must be declared before it is used" and locals by
     * flow analysis, so class fields are the only shape that reaches the check in
     * AttrExprType.calculate(ExprVarAccess). That check compared two freshly allocated
     * Optionals with ==, so it was unreachable and this compiled silently.
     */
    @Test
    public void test_recursive_class_field_def() {
        testAssertErrorsLines(false, "Recursive variable definition is not allowed",
                "package test",
                "	class C",
                "		int x = x + 1",
                "endpackage");
    }

    /** Guard against the check over-triggering: a field initialized from another field is fine. */
    @Test
    public void test_non_recursive_class_field_def_ok() {
        testAssertOkLines(false,
                "package test",
                "	class C",
                "		int a = 1",
                "		int b = a + 1",
                "endpackage");
    }

}
