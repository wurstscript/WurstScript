package tests.wurstscript.tests;

import org.testng.annotations.Test;

public class PackageTests extends WurstScriptTest {


    @Test
    public void test_static_init() { // test case for #68
        testAssertOkLines(true,
                "package A",
                "	public class Blub",
                "		static int b = 2",
                "endpackage",
                "package B",
                "	import A",
                "	native testSuccess()",
                "	init",
                "		if Blub.b == 2",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void duplicatePackageName() {
        testAssertErrorsLines(false, "already",
                "package A",
                "endpackage",
                "package A",
                "endpackage");
    }

    @Test
    public void test_import_function_fail() {
        testAssertErrorsLines(false, "not visible",
                "package A",
                "	function foo(int x) returns int",
                "		return x*2",
                "endpackage",
                "package B",
                "	import A",
                "	init",
                "		foo(2)",
                "endpackage");
    }

    @Test
    public void test_import_function() {
        testAssertOkLines(false,
                "package A",
                "	public function foo(int x) returns int",
                "		return x*2",
                "endpackage",
                "package B",
                "	import A",
                "	init",
                "		foo(2)",
                "endpackage");
    }


    @Test
    public void import_public() {
        testAssertOkLines(false,
                "package A",
                "	public function foo(int x) returns int",
                "		return x*2",
                "	public int x = 0",
                "endpackage",
                "package B",
                "	import public A",
                "endpackage",
                "package C",
                "	import B",
                "	init",
                "		x = foo(2)",
                "endpackage");
    }


    @Test
    public void test_import_ext_function() {
        testAssertOkLines(false,
                "package A",
                "	public function int.plusOne() returns int",
                "		return this + 2",
                "endpackage",
                "package B",
                "	import A",
                "	init",
                "		2 .plusOne()",
                "endpackage");
    }

    @Test
    public void test_import_ext_function_fail() {
        testAssertErrorsLines(false, "not visible",
                "package A",
                "	function int.plusOne() returns int",
                "		return this + 2",
                "endpackage",
                "package B",
                "	import A",
                "	init",
                "		2 .plusOne()",
                "endpackage");
    }

    @Test
    public void test_import_class() {
        testAssertOkLines(false,
                "package A",
                "	public class Blub",
                "		function foo()",
                "endpackage",
                "package B",
                "	import A",
                "	init",
                "		Blub blub = new Blub()",
                "		blub.foo()",
                "endpackage");
    }

    @Test
    public void test_import_class_protected() {
        testAssertErrorsLines(false, "not visible",
                "package A",
                "	public class Blub",
                "		protected function foo()",
                "endpackage",
                "package B",
                "	import A",
                "	init",
                "		Blub blub = new Blub()",
                "		blub.foo()",
                "endpackage");
    }

    @Test
    public void test_import_class_private() {
        testAssertErrorsLines(false, "not visible",
                "package A",
                "	public class Blub",
                "		private function foo()",
                "endpackage",
                "package B",
                "	import A",
                "	init",
                "		Blub blub = new Blub()",
                "		blub.foo()",
                "endpackage");
    }

    @Test
    public void test_import_class_fail() {
        testAssertErrorsLines(false, "Blub",
                "package A",
                "	class Blub",
                "		function foo()",
                "endpackage",
                "package B",
                "	import A",
                "	init",
                "		Blub blub = new Blub()",
                "		blub.foo()",
                "endpackage");
    }

    @Test
    public void test_typename_as_var() {
        testAssertErrorsLines(false, "unit",
                "type unit extends handle",
                "package A",
                "	unit unit",
                "	init",
                "		unit a = null",
                "		unit = a",
                "endpackage");
    }

    @Test
    public void test_typename_as_var2() {
        testAssertErrorsLines(false, "The name 'unit' is already used",
                "type unit extends handle",
                "package A",
                "	int unit",
                "	init",
                "		unit = 14",
                "endpackage");
    }


    @Test
    public void test_typename_as_var3() {
        testAssertErrorsLines(false, "Invalid assignment",
                "type player extends handle",
                "package A",
                "	function foo(player p)",
                "		player = p",
                "endpackage");
    }

    @Test
    public void test_import_var() {
        testAssertOkLines(false,
                "package A",
                "	public int x = 4",
                "endpackage",
                "package B",
                "	import A",
                "	init",
                "		int y = x + 1",
                "endpackage");
    }

    @Test
    public void test_import_var_fail() {
        testAssertErrorsLines(false, "not visible",
                "package A",
                "	int x = 4",
                "endpackage",
                "package B",
                "	import A",
                "	init",
                "		int y = x + 1",
                "endpackage");
    }

    @Test
    public void test_global_var() {
        testAssertOkLines(false,
                "globals",
                "	integer x = 4",
                "endglobals",
                "package B",
                "	init",
                "		int y = x + 1",
                "endpackage");
    }

    @Test
    public void test_global_type() {
        testAssertOkLines(false,
                "type unit extends handle",
                "package B",
                "	init",
                "		unit u = null",
                "endpackage");
    }


    @Test
    public void test_cyclic_import() {
        testAssertErrorsLines(false, "init dependency",
                "package B",
                "	import A",
                "endpackage",
                "package A",
                "	import B",
                "endpackage");
    }

    @Test
    public void test_cyclic_import_with_init() {
        testAssertErrorsLines(false, "cyclic init",
                "package B",
                "	import A",
                "	public int b",
                "	init",
                "		b = a + 1",
                "endpackage",
                "package A",
                "	import B",
                "	public int a",
                "	init",
                "		a = b + 1",
                "endpackage");
    }

    @Test
    public void test_cyclic_import_with_init2() {
        testAssertErrorsLines(false, "cyclic init",
                "package B",
                "	import A",
                "	public int b = a",
                "endpackage",
                "package A",
                "	import B",
                "	public int a",
                "	init",
                "		a = b",
                "endpackage");
    }


    @Test
    public void test_globals_init() {
        testAssertOkLines(true,
                "package B",
                "	native testSuccess()",
                "	public int x = 1",
                "	init",
                "		if x == 1",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void test_globals_init2() {
        testAssertOkLines(true,
                "globals",
                "	integer x = 1",
                "endglobals",
                "package B",
                "	native testSuccess()",
                "	init",
                "		if x == 1",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void test_same_name_as_class() {
        testAssertOkLines(false,
                "package A",
                "	class A",
                "endpackage");
    }

    @Test
    public void import_self1() {
        testAssertErrorsLines(false, "Packages cannot import themselves",
                "package A",
                "	import A",
                "endpackage");
    }

    @Test
    public void import_self2() {
        testAssertErrorsLines(false, "Packages cannot import themselves",
                "package A",
                "	import initlater A",
                "endpackage");
    }


    @Test
    public void testMembersNoImport() {
        testAssertOkLines(true,
                "package A",
                "	public class Blub",
                "		int x = 2",
                "		function f() returns int",
                "			return 3",
                "endpackage",
                "package B",
                "	import A",
                "	public let blub = new Blub",
                "endpackage",
                "package C",
                "	import B",
                "	native testSuccess()",
                "	init",
                "		if blub.x == 2 and blub.f() == 3",
                "			testSuccess()",
                "endpackage");
    }

}
