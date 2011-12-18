package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class ClassesTests extends PscriptTest {
	
	private static final String TEST_DIR = "./testscripts/valid/classes/";

	
	@Test
	public void classes1() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "Classes_1.pscript"), true);
	}

	@Test
	public void classes_construct() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "Classes_construct.pscript"), true);
	}

	@Test
	public void classes_lifecycle() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "Classes_lifecycle.pscript"), true);
	}

	@Test
	public void classes_method_implicit() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "Classes_method_implicit.pscript"), true);
	}

	@Test
	public void classes_method() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "Classes_method.pscript"), true);
	}
	
	@Test
	public void classes_static_func() {
		testAssertErrorsLines(false, "static", 
				"package test",
				"	class C",
				"		function foo() returns int",
				"			return 3",
				"		static function bar()",
				"			foo()",
				"endpackage"
			);
	}


	@Test
	public void classes_static_var_get() {
		testAssertErrorsLines(false, "static", 
				"package test",
				"	class C",
				"		int i = 3",
				"		static function foo() returns int",
				"			return i",
				"endpackage"
			);
	}
	
		@Test
	public void constantVars() {
		testAssertErrorsLines(false, "constant variable", 
				"package test",
				"	class C",
				"		constant int i",
				"		function foo() returns int",
				"			i++",
				"			return i",
				"endpackage"
			);
	}
	
		
		
	
	@Test
	public void classes_static_var_set() {
		testAssertErrorsLines(false, "static", 
				"package test",
				"	class C",
				"		int i = 3",
				"		static function foo(int j)",
				"			i = j",
				"endpackage"
			);
	}
	
	@Test
	public void classes_static_var_set2() {
		testAssertErrorsLines(false, "static", 
				"package test",
				"	class C",
				"		int i = 3",
				"		static function foo(int j)",
				"			this.i = j",
				"endpackage"
			);
	}
	

	@Test
	public void classes_double_defined() {
		testAssertErrorsLines(false, "already defined", 
				"package test",
				"	class C",
				"		function foo() returns int",
				"			return 3",
				"		static function foo() returns int",
				"			return 4",
				"endpackage"
			);
	}
	
	@Test
	public void static_field() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	class C",
				"		static int i = 3",
				"	init",
				"			C.i++",
				"			if C.i == 4",
				"				testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void static_field_other_package() {
		testAssertOkLines(true,
				"package Blub",
				"	public class C",
				"		public static int i = 3",
				"endpackage",
				"package test",
				"	import Blub",
				"	native testSuccess()",
				"	init",
				"			C.i++",
				"			if C.i == 4",
				"				testSuccess()",
				"endpackage"
			);
	}
}
