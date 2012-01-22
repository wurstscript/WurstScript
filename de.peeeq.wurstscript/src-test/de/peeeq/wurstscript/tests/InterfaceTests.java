package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class InterfaceTests extends PscriptTest {
	
	
	@Test
	public void simple() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	interface I",
				"		function foo() returns int",
				"	class B implements I",
				"		function foo() returns int",
				"			return 2",
				"	class C implements I",
				"		function foo() returns int",
				"			return 3",
				"	init",
				"		I i1 = new B()",
				"		I i2 = new C()",
				"		if i1.foo() == 2 and i2.foo() == 3",
				"			testSuccess()",
				"endpackage"
			);
	}


	@Test
	public void as_argument() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	interface I",
				"		function foo() returns int",
				"	class B implements I",
				"		function foo() returns int",
				"			return 2",
				"	class C implements I",
				"		function foo() returns int",
				"			return 3",
				"	function test(I i1, I i2)",
				"		if i1.foo() == 2 and i2.foo() == 3",
				"			testSuccess()",
				"	init",
				"		I i1 = new B()",
				"		I i2 = new C()",
				"		test(i1, i2)",
				"endpackage"
			);
	}
	
	
	@Test
	public void as_return_value() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	interface I",
				"		function foo() returns int",
				"	class B implements I",
				"		function foo() returns int",
				"			return 2",
				"	class C implements I",
				"		function foo() returns int",
				"			return 3",
				"	function test(boolean b) returns I",
				"		if b",
				"			return new B()",
				"		else",
				"			return new C()",
				"	init",
				"		I i1 = test(true)",
				"		if i1.foo() == 2 and test(false).foo() == 3",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	
	
	
	@Test
	public void missing_method() {
		testAssertErrorsLines(false, "must implement the function foo", 
				"package test",
				"	native testSuccess()",
				"	interface I",
				"		function foo() returns int",
				"	class B implements I",
				"		function bar() returns int",
				"			return 3",
				"endpackage"
			);
	}
	
	
	@Test
	public void wrong_method() {
		testAssertErrorsLines(false, "not implement interface", 
				"package test",
				"	native testSuccess()",
				"	interface I",
				"		function foo(int x) returns int",
				"	class B implements I",
				"		function foo(real x) returns int",
				"			return 3",
				"endpackage"
			);
	}
}
