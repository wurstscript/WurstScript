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
	public void equality() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	interface I",
				"		function foo()",
				"	class B implements I",
				"		function foo()",
				"	class C implements I",
				"		function foo()",
				"	init",
				"		I a = new B()",
				"		I b = new B()",
				"		I c = new C()",
				"		if not (a == b or a == c)",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void inequality() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	interface I",
				"		function foo()",
				"	class B implements I",
				"		function foo()",
				"	class C implements I",
				"		function foo()",
				"	init",
				"		I a = new B()",
				"		I b = new B()",
				"		I c = new C()",
				"		if a != b and a != c",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void hierarchy() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	interface A",
				"		function f1() returns int",
				"	interface B extends A",
				"		function f2() returns int",
				"	class C implements B",
				"		function f1() returns int",
				"			return 3",
				"		function f2() returns int",
				"			return 4",
				"	init",
				"		B b = new C()",
				"		A a = b",
				"		if a.f1() == 3",
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
	public void type_param1() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	interface Collection{T}",
				"		function contains(int t) returns boolean",
				"	class Set{T} implements Collection{T}",
				"		function contains(int t) returns boolean",
				"			return false",
				"	init",
				"		Collection{int} c = new Set{int}()",
				"		if not c.contains(4)",
				"			testSuccess()",
				"endpackage"
			);
	}
	

	@Test
	public void type_param_fail_generics() {
		testAssertErrorsLines(false, "Cannot assign", 
				"package test",
				"	native testSuccess()",
				"	interface Collection{T}",
				"		function contains(int t) returns boolean",
				"	class Set{T} implements Collection{T}",
				"		function contains(int t) returns boolean",
				"			return false",
				"	class A",
				"	init",
				"		Collection{int} c = new Set{A}()", // cannot assign set of As to collection of int
				"		if not c.contains(4)",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	
	@Test
	public void type_param_complicated1() {
		testAssertOkLines(false,
				"package test",
				"	native testSuccess()",
				"	interface I{S,T,V}",
				"		function test() returns boolean",
				"	class A{X,Y} implements I{B, Y, X}",
				"		function test() returns boolean",
				"			return true",
				"	class B",
				"	class C",
				"	class D",
				"	init",
				"		I{B, C, D} i = new A{D, C}()",
				"		if i.test()",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void module_prob() {
		testAssertErrorsLines(false, "Expected I", 
				"package test",
				"	interface I",
				"		function foo()",
				"	module M",
				"		construct()",
				"			bla(this)",
				"	class C implements I",
				"		use M",
				"		function foo()",
				"		",
				"	function bla(I i)",
				"endpackage"
			);
	}
	
	@Test
	public void type_param_complicated1_fail() {
		testAssertErrorsLines(false, "Cannot assign", 
				"package test",
				"	native testSuccess()",
				"	interface I{S,T,V}",
				"		function test() returns boolean",
				"	class A{X,Y} implements I{B, Y, X}",
				"		function test() returns boolean",
				"			return true",
				"	class B",
				"	class C",
				"	class D",
				"	init",
				"		I{B, C, D} i = new A{C, D}()",
				"		if i.test()",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void type_param_complicated2() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	interface I{S,T,V}",
				"		function test(S s, T t, V v) returns boolean",
				"	class A{X,Y} implements I{B, Y, X}",
				"		function test(B b, Y y, X x) returns boolean",
				"			return true",
				"	class B",
				"	class C",
				"	class D",
				"	init",
				"		I{B, C, D} i = new A{D, C}()",
				"		B b = new B()",
				"		C c = new C()",
				"		D d = new D()",
				"		if i.test(b, c, d)",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void type_param_class() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	interface I{A}",
				"		function test(A a) returns boolean",
				"	class C{X} implements I{X}",
				"		function test(X x) returns boolean",
				"			return true",
				"	class B",
				"	init",
				"		I{B} i = new C{B}()",
				"		B b = null",
				"		if i.test(b)",
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
