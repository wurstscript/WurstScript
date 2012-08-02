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
	public void classes_static_var() {
		testAssertErrorsLines(false, "static", 
				"package test",
				"	class C",
				"		static int size = 0",
				"		function foo() returns int",
				"			return this.size",
				"endpackage"
			);
	}
	
	@Test
	public void classes_static_var2() {
		testAssertErrorsLines(false, "static", 
				"package test",
				"	class C",
				"		static int size = 0",
				"		function foo() returns int",
				"			this.size++",
				"			return this.size",
				"endpackage"
			);
	}
	
	
	@Test
	public void array_members() {
		testAssertErrorsLines(false, "must be static", 
				"package test",
				"	class C",
				"		int array blub",
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
				"		static int i = 3",
				"endpackage",
				"package test",
				"	import Blub",
				"	native testSuccess()",
				"	init",
				"		C.i++",
				"		if C.i == 4",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	
	@Test
	public void static_static_array_field() {
		testAssertOkLines(true,
				"package Blub",
				"	public class C",
				"		static int array xs",
				"		static function setX(int i, int x)",
				"			xs[i] = x",
				"		static function getX(int i) returns int",
				"			return xs[i]",
				"endpackage",
				"package test",
				"	import Blub",
				"	native testSuccess()",
				"	init",
				"		C.setX(7, 4)",
				"		if C.getX(7) == 4",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	
	@Test
	public void ondestroy() {
		testAssertOkLines(false, 
				"package test",
				"	native testSuccess()",
				"	class C",
				"		int i = 3",
				"		ondestroy",
				"			i = i + 1",
				"			testSuccess()",
				"	init",
				"		destroy new C()",
				"endpackage"
			);
	}
	

	@Test
	public void recyling() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	native println(string msg)",
				"	native I2S(int i) returns string",
				"	class C",
				"		int i",
				"",
				"	init",
				"		C array cs",
				"		for int i = 0 to 6000",
				"			cs[i] = new C()",
				"		for int j = 0 to 6000",
				"			destroy cs[j]",
				"		for int k = 0 to 6000",
				"			cs[k] = new C()",
				"			println(I2S(cs[k] castTo int))",
				"		if cs[6000] castTo int <= 6001",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void cast_class() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	class A",
				"	class B extends A",
				"	init",
				"		A a = new B()",
				"		if a instanceof B",
				"			B b = a castTo B",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void cast_class2() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	class A",
				"	class B extends A",
				"	class C extends B",
				"	init",
				"		A a = new C()",
				"		if a instanceof B",
				"			B b = a castTo B",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void cast_class_unrelated() {
		testAssertErrorsLines(true, "not directly related", 
				"package test",
				"	native testSuccess()",
				"	class A",
				"	class B",
				"	init",
				"		A a = new A()",
				"		if a instanceof B",
				"			B b = a castTo B",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	
	@Test
	public void override() {
		testAssertErrorsLines(false, "uses override", 
				"package test",
				"	native testSuccess()",
				"	class A",
				"		override function foo()",
				"			skip",
				"endpackage"
			);
	}
}
