package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class ModuleTests extends PscriptTest {
	
	private static final String TEST_DIR = "./testscripts/valid/modules/";

	
	@Test
	public void simple() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "simple.pscript"), true);
	}

	@Test
	public void multi() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "multi.pscript"), true);
	}
	
	@Test
	public void override() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "override.pscript"), true);
	}
	
	@Test
	public void override2() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "override2.pscript"), true);
	}
	
	@Test
	public void diamond1() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "diamond.pscript"), true);
	}
	
	@Test
	public void diamond2() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "diamond2.pscript"), true);
	}
	
	@Test
	public void initdestroy() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "initdestroy.pscript"), true);
	}
	
	
	@Test
	public void modules_conflict() {
		testAssertErrorsLines(true, "defined multiple times", 
				"package test",
				"	module A",
				"		public function foo() returns int",
				"			return 3",
				"	module B",
				"		public function foo() returns int",
				"			return 4",
				"	class C",
				"		use A",
				"		use B",
				"endpackage"
			);
	}
	
	@Test
	public void modules_not_public_function() {
		// each function in a module should be either private or public
		testAssertErrorsLines(true, "must be declared public or private", 
				"package test",
				"	module A",
				"		function foo() returns int",
				"			return 3",
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
				"		public function foo() returns thistype",
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
	public void modules_abstract() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	module A",
				"		public abstract function foo() returns int",
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
		testAssertErrorsLines(false, "abstract method foo must be implemented", 
				"package test",
				"	module A",
				"		public abstract function foo() returns int",
				"	class C",
				"		use A",
				"endpackage"
			);
	}
}
