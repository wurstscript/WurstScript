package de.peeeq.wurstscript.tests;

import org.junit.Test;

import de.peeeq.wurstscript.utils.Utils;

public class PackageTests extends PscriptTest {

	
	@Test
	public void test_import_function_fail() {
		testAssertErrorsLines(false, "Could not resolve",
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
		testAssertErrorsLines(false, "undefined",
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
		testAssertErrorsLines(false, "undefined",
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
		testAssertErrorsLines(false, "undefined",
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
		testAssertErrorsLines(false, "refers to element",
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
		testAssertErrorsLines(false, "defines the same name",
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
		testAssertErrorsLines(false, "Could not resolve",
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
		testAssertOkLines(false, 
				"package B",
				"	import A",
				"endpackage",
				"package A",
				"	import B",
				"endpackage");
	}
	
	@Test
	public void test_cyclic_import_with_init() {
		testAssertOkLines(false, 
				"package B",
				"	import A",
				"	public int x",
				"	init",
				"		x = 1",
				"endpackage",
				"package A",
				"	import B",
				"	init",
				"		x = 2",
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

}
