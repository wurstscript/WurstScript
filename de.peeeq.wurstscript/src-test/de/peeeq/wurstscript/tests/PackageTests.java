package de.peeeq.wurstscript.tests;

import org.junit.Test;

import de.peeeq.wurstscript.utils.Utils;

public class PackageTests extends PscriptTest {

	
	@Test
	public void test_import_function_fail() {
		assertError(false, "Could not resolve",
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
		assertOk(false,
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
		assertOk(false,
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
	public void packageCalls() {
		assertError(false,
				"package A",
				"	int i",
				"endpackage",
				"package B",
				"	import A",
				"	init",
				"		A.i()",
				"endpackage");
	}
	
	
	@Test
	public void test_import_ext_function() {
		assertOk(false,
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
		assertError(false, "undefined",
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
		assertOk(false,
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
		assertError(false, "undefined",
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
		assertError(false, "undefined",
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
		assertError(false, "Blub",
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
		assertError(false, "type name 'unit'",
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
		assertOk(false,
				"type unit extends handle",
				"package A",
				"	int unit",
				"	init",
				"		unit = 14",
				"endpackage");
	}
	

	@Test
	public void test_typename_as_var3() {
		assertError(false, "Invalid assignment",
				"type player extends handle",
				"package A",
				"	function foo(player p)",
				"		player = p",
				"endpackage");
	}
	
	@Test
	public void test_import_var() {
		assertOk(false,
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
		assertError(false, "Could not resolve",
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
		assertOk(false, 
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
		assertOk(false, 
				"type unit extends handle",
				"package B",
				"	init",
				"		unit u = null",
				"endpackage");
	}
	
	

	@Test
	public void test_cyclic_import() {
		assertOk(false, 
				"package B",
				"	import A",
				"endpackage",
				"package A",
				"	import B",
				"endpackage");
	}
	
	@Test
	public void test_cyclic_import_with_init() {
		assertOk(false, 
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
	
	
	private String makeCode(String... body) {
		return Utils.join(body, "\n");
	}
	
	public void assertOk( boolean executeProg, String ... body) {
		String prog = makeCode(body);
		testAssertOk(Utils.getMethodName(1), executeProg, prog);
	}
	
	public void assertError( boolean executeProg, String expected, String ... body) {
		String prog = makeCode(body);
		testAssertErrors(Utils.getMethodName(1), executeProg, prog, expected);
	}

}
