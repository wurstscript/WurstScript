package tests.wurstscript.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.peeeq.wurstio.jassinterpreter.DebugPrintError;

public class ClassesTests extends WurstScriptTest {
	
	private static final String TEST_DIR = "./testscripts/valid/classes/";
	private static final String TEST_DIR2 = "./testscripts/concept/";

	@Test
	public void classes1() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "Classes_1.wurst"), true);
	}

	@Test
	public void classes_construct() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "Classes_construct.wurst"), true);
	}
	
	@Test
	public void OverrideClass() throws IOException {
		testAssertOkFile(new File(TEST_DIR2 + "OverrideTest.wurst"), false);
	}

	@Test
	public void classes_lifecycle() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "Classes_lifecycle.wurst"), true);
	}

	@Test
	public void classes_method_implicit() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "Classes_method_implicit.wurst"), true);
	}

	@Test
	public void classes_method() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "Classes_method.wurst"), true);
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
	
	
//	@Test
//	public void array_members() {
//		testAssertErrorsLines(false, "must be static", 
//				"package test",
//				"	class C",
//				"		int array blub",
//				"endpackage"
//			);
//	}
	
	@Test
	public void code_members() {
		testAssertErrorsLines(false, "code members not allowed", 
				"package test",
				"	class C",
				"		code c",
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
		testAssertErrorsLines(false, "assign a new value", 
				"package test",
				"	class C",
				"		constant int i = 1",
				"		function foo() returns int",
				"			i++",
				"			return i",
				"endpackage"
			);
	}
	
		@Test
		public void constantVars2() {
			testAssertErrorsLines(false, "", // TODO "initial value", 
					"package test",
					"	class C",
					"		constant int i",
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
				"	@extern native I2S(int i) returns string",
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
//				"			println(I2S(k) + \" --> \"  +I2S(cs[k] castTo int))",
				"		if cs[6000] castTo int <= 6001",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void recyling2() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	native println(string msg)",
				"	native I2S(int i) returns string",
				"	class C",
				"		int i",
				"",
				"	init",
				"		C a = new C",
				"		destroy a",
				"		a = new C",
				"		destroy a",
				"		a = new C",
				"		C b = new C",
//				"		println(\"###### \" + I2S(a castTo int) + \" -- \" + I2S(b castTo int))",
				"		if a != b",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void recyling_random() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	native println(string msg)",
				"	native testFail(string msg)",
				"	@extern native I2S(int i) returns string",
				"	@extern native GetRandomReal(real a, real b) returns real",
				"	@extern native GetRandomInt(int a, int b) returns int",
				"	class C",
				"		int alive",
				"		construct()",
				"			if alive == 1",
				"				testFail(\"already alive\")",
				"			alive = 1",
				"		ondestroy",
				"			alive = 2",
				"	C array cs",
				"	int count = 0",
				"	init",
				"		for i = 0 to 1000",
				"			if count < 100 and GetRandomReal(0,1) <= 0.5",
				"				cs[count] = new C",
				"				count++",
				"			if count > 0 and GetRandomReal(0,1) <= 0.1",
				"				let j = GetRandomInt(0,count-1)",
				"				destroy cs[j]",
				"				count--",
				"				cs[j] = cs[count]",
				"		testSuccess()",
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
	public void big_instanceof() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	class A",
				"	class B extends A",
				"	class B1 extends B",
				"	class B2 extends B",
				"	class B2a extends B2",
				"	class B2b extends B2",
				"	class B2c extends B2",
				"	class B3 extends B",
				"	class B4 extends B",
				"	class B5 extends B",
				"	init",
				"		A a = new B2a()",
				"		if a instanceof B",
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
	
	@Test
	public void override_valid() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	class A",
				"		function foo() returns int",
				"			return 7",
				"	class B extends A",
				"		override function foo() returns int",
				"			return 8",
				"	init",
				"		A b = new B()",
				"		if b.foo() == 8",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void override_valid2() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	class A",
				"		function foo() returns int",
				"			return 7",
				"	class B extends A",
				"		override function foo() returns int",
				"			return 8",
				"	init",
				"		A b = new A()",
				"		if b.foo() == 7",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void override_valid_trans() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	class A",
				"		function foo() returns int",
				"			return 7",
				"	class B extends A",
				"		override function foo() returns int",
				"			return 8",
				"	class C extends B",
				"	init",
				"		A c = new C()",
				"		if c.foo() == 8",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void override_valid_void() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	int i",
				"	class A",
				"		function foo()",
				"			i = 7",
				"	class B extends A",
				"		override function foo()",
				"			i = 8",
				"	init",
				"		A b = new B()",
				"		b.foo()",
				"		if i == 8",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void override_valid_trans_big() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	native println(string msg)",
				"	native testFail(string msg)",
				"	class A",
				"		function foo() returns int",
				"			return 7",
				"	class B extends A",
				"		override function foo() returns int",
				"			return 8",
				"	class B1 extends B",
				"	class B2 extends B",
				"	class B11 extends B1",
				"	class B111 extends B11",
				"	class C extends A",
				"		override function foo() returns int",
				"			return 9",
				"	class C1 extends C",
				"	class C2 extends C",
				"	class C11 extends C1",
				"	class C111 extends C11",
				"	init",
				"		A a = new C11()",
				"		if a.foo() != 9",
				"			testFail(\"c11\")",
				"		a = new B11()",
				"		if a.foo() != 8",
				"			testFail(\"b11\")",
				"		a = new C()",
				"		if a.foo() != 9",
				"			testFail(\"C\")",
				"		a = new A()",
				"		if a.foo() != 7",
				"			testFail(\"A\")",
				
				
				"		testSuccess()",
				"endpackage"
			);
	}
	
	@Test(expected=DebugPrintError.class)
	public void NPE() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	class A",
				"		function foo() returns int",
				"			return 7",
				"	init",
				"		A a = null",
				"		if a.foo() == 7",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test(expected=DebugPrintError.class)
	public void destroyed() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	class A",
				"		function foo() returns int",
				"			return 7",
				"	init",
				"		A a = new A()",
				"		destroy a",
				"		if a.foo() == 7",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void abstract_class() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	int i",
				"	abstract class A",
				"		abstract function foo()",
				"	class B extends A",
				"		override function foo()",
				"			i = 8",
				"	init",
				"		A b = new B()",
				"		b.foo()",
				"		if i == 8",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void abstract_class2() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	int i",
				"	abstract class A",
				"		abstract function foo() returns int",
				"	class B extends A",
				"		override function foo() returns int",
				"			i = 8",
				"			return i",
				"	init",
				"		A b = new B()",
				"		if b.foo() == 8",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void abstract_fail() {
		testAssertErrorsLines(true, "Cannot create an instance of the abstract class A", 
				"package test",
				"	native testSuccess()",
				"	int i",
				"	abstract class A",
				"		abstract function foo() returns int",
				"	init",
				"		A b = new A()",
				"		if b.foo() == 8",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	
	@Test
	public void abstract_must_not_be_private() {
		testAssertErrorsLines(true, "Abstract functions must not be private", 
				"package test",
				"	abstract class A",
				"		abstract private function foo()",
				"endpackage"
			);
	}

	@Test
	public void abstract_fail2() {
		testAssertErrorsLines(true, "must implement the abstract function foo", 
				"package test",
				"	native testSuccess()",
				"	int i",
				"	abstract class A",
				"		abstract function foo() returns int",
				"	abstract class B extends A",
				"	class C extends B",
				"endpackage"
			);
	}
	
	@Test
	public void construct_super() {
		testAssertErrorsLines(false, "Super call in a class which extends nothing", 
				"package test",
				"	class C",
				"		construct(int x)",
				"			super()",
				"endpackage"
			);
	}
	
	@Test
	public void constructor_overloading() {
		testAssertOkLines(false, 
				"type player extends handle",
				"package test",
				"	class A",
				"		construct(player p)",
				"			skip",
				"		construct(int y)",
				"			skip",
				"	class B extends A",
				"		construct (player p)",
				"			super(p)",
				"endpackage"
			);
	}
	
	@Test
	public void constant_fields() {
		testAssertErrorsLines(false, "constant",
				"package test",
				"	class A",
				"		constant int i = 0",
				"	init",
				"		A a = new A()",
				"		a.i = 1",
				"endpackage"
			);
	}
	
	@Test
	public void constant_fields2() {
		testAssertErrorsLines(false, "Cannot assign",
				"package test",
				"	class A",
				"		constant int i = 1",
				"	init",
				"		A a = new A()",
				"		a.i = 1",
				"endpackage"
			);
	}
	
	
	@Test
	public void duplicateConstructor() {
		testAssertErrorsLines(false, "Duplicate constructor",
				"package test",
				"	class A",
				"		int i",
				"		construct(int j)",
				"			i = j",
				"		construct(int j)",
				"			i = j",
				"endpackage"
			);
	}
	
	@Test
	public void thisHandling() { // see bug #145
		testAssertOkLines(true,
				"package test",
				"	native testSuccess()",
				"	class A",
				"		int i = this castTo int",
				"	init",
				"		let a = new A",
				"		if a.i == 1",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void dotdotOperator() { 
		testAssertOkLines(true,
				"package test",
				"	native testSuccess()",
				"	class A",
				"		int i",
				"		function plus(int x)",
				"			i += x",
				"		function minus(int x)",
				"			i -= x",
				"	init",
				"		A a = new A",
				"			..plus(3)",
				"			..minus(4)",
				"			..plus(5)",
				"		if a.i == 4",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void dotdotOperatorFail() { 
		testAssertErrorsLines(false, "cannot be used with the cascade operator",
				"package test",
				"	native testSuccess()",
				"	class A",
				"		static function foo(int x)",
				"	init",
				"		A a = A",
				"			..foo(1)",
				"			..foo(2)",
				"endpackage"
			);
	}
	
// TODO	@Test disabled until it is working
	public void arrayAttributeTest1() { 
		testAssertOkLines(true,
				"package test",
				"	native testSuccess()",
				"	native println(string msg)",
				"	native testFail(string msg)",
				"	class A",
				"		int array[4] ints",
				"	init",
				"		A a = new A()",
				"		a.ints[0] = 1234",
				"		if a.ints[0] == 1234",
				"			testSuccess()",
				"		else",
				"			testFail(\"wrong value\")",
				"		",
				"endpackage"
			);
	}
	
	@Test
	public void arrayAttributeTest2() { 
		testAssertOkLines(true,
				"package test",
				"	native testSuccess()",
				"	native println(string msg)",
				"	native testFail(string msg)",
				"	class A",
				"		string array[4] s",
				"	init",
				"		A a = new A()",
				"		a.s[0] = \"a\"",
				"		a.s[1] = \"b\"",
				"		a.s[2] = \"c\"",
				"		a.s[3] = \"d\"",
				"		if a.s[0] == \"a\" and a.s[1] == \"b\" and a.s[2] == \"c\" and a.s[3] == \"d\"",
				"			testSuccess()",
				"		else",
				"			testFail(\"wrong value\")",
				"		",
				"endpackage"
			);
	}
	
}
