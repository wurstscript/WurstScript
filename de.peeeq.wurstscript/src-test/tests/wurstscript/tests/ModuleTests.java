package tests.wurstscript.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class ModuleTests extends WurstScriptTest {
	
	private static final String TEST_DIR = "./testscripts/valid/modules/";

	
	@Test
	public void simple() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "simple.wurst"), true);
	}

	@Test
	public void multi1() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "multi.wurst"), true);
	}
	
	@Test
	public void multi2() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "multi2.wurst"), true);
	}
	
	@Test
	public void override() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "override.wurst"), true);
	}
	
	@Test
	public void override2() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "override2.wurst"), true);
	}
	
	@Test
	public void diamond1() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "diamond.wurst"), true);
	}
	
	@Test
	public void diamond2() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "diamond2.wurst"), true);
	}
	
	@Test
	public void initdestroy() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "initdestroy.wurst"), true);
	}
	
	
	@Test
	public void modules_conflict() {
		testAssertErrorsLines(false, "ambiguous", 
				"package test",
				"	module A",
				"		function foo() returns int",
				"			return 3",
				"	module B",
				"		function foo() returns int",
				"			return 4",
				"	class C",
				"		use A",
				"		use B",
				"		function test()",
				"			foo()",
				"endpackage"
			);
	}
	
	
	@Test
	public void modules_thistype() {
		// each function in a module should be either private or public
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	module A",
				"		function foo() returns thistype",
				"			return this",
				"	class C",
				"		use A",
				"	init",
				"		C c1 = new C()",
				"		C c2 = c1.foo()",
				"		if c1 == c2",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	
	@Test
	public void modules_import() {
		// each function in a module should be either private or public
		testAssertOkLines(true,
				"package Blub",
				"	public module BlubModule",
				"		function foo() returns int",
				"			return 3",
				"endpackage",
				"package test",
				"	import Blub",
				"	native testSuccess()",
				"	class C",
				"		use BlubModule",
				"	init",
				"		C c = new C()",
				"		if c.foo() == 3",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void modules_call_global() {
		// each function in a module should be either private or public
		testAssertOkLines(true,
				"function random takes nothing returns int",
				"	return 3 // totally random, chosen by fair dice roll",
				"endfunction",
				"",
				"package test",
				"	native testSuccess()",
				"	module BlubModule",
				"		function foo() returns int",
				"			return random()",
				"	class C",
				"		use BlubModule",
				"	init",
				"		C c = new C()",
				"		if c.foo() == 3",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void modules_abstract() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	module A",
				"		abstract function foo() returns int",
				"	class C",
				"		use A",
				"		override function foo() returns int",
				"			return 3",
				"",
				"	init",
				"		C c = new C()",
				"		if c.foo() == 3",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void modules_abstract_err() {
		testAssertErrorsLines(false, "must implement", 
				"package test",
				"	module A",
				"		abstract function foo() returns int",
				"	class C",
				"		use A",
				"endpackage"
			);
	}
	
	@Test
	public void modules_missing_override() {
		testAssertErrorsLines(false, "override", 
				"package test",
				"	module A",
				"		abstract function foo() returns int",
				"	class C",
				"		use A",
				"		function foo() returns int",
				"			return 3",
				"endpackage"
			);
	}
	
	@Test
	public void modules_wrong_param_count1() {
		testAssertErrorsLines(false, "parameter", 
				"package test",
				"	module A",
				"		function foo()",
				"			bar(3)",
				"		function bar()",
				"",
				"	class C",
				"		use A",
				"		function xyz()",
				"			foo()",
				"endpackage"
			);
	}
	
	@Test
	public void modules_wrong_param_count2() {
		testAssertErrorsLines(false, "parameter", 
				"package test",
				"	module A",
				"		function foo()",
				"			bar(3)",
				"		function bar(int x, int y)",
				"",
				"	class C",
				"		use A",
				"		function xyz()",
				"			foo()",
				"endpackage"
			);
	}
	
	@Test
	public void modules_call_indirect() {
		testAssertOkLines(false, 
				"package test",
				"	native testSuccess()",
				"	module A",
				"		function foo()",
				"			bar()",
				"		function bar()",
				"			testSuccess()",
				"	class C",
				"		use A",
				"		construct()",
				"			foo()",
				"	init",
				"		new C()",
				"endpackage"
			);
	}
	
	@Test
	public void static_external() {
		testAssertErrorsLines(false, "from within",
				"package test",
				"	module A",
				"		static int b = 0",
				"	init",
				"		A.b = 0",
				"endpackage"
			);
	}
	
	@Test
	public void staticmeth_external() {
		testAssertErrorsLines(false, "from within",
				"package test",
				"	module A",
				"		static function b(int b)",
				"	init",
				"		A.b(0)",
				"endpackage"
			);
	}
	
}
