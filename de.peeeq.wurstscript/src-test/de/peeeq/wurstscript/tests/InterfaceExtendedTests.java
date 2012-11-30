package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class InterfaceExtendedTests extends WurstScriptTest {
	
	
	@Test
	public void defaultImpl() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	native testFail(string s)",
				"	interface I",
				"		function foo() returns int",
				"			return 5",
				"	class B implements I",
				"		function foo() returns int",
				"			return 2",
				"	class C implements I",
				"		function foo() returns int",
				"			return 3",
				"	class D implements I",
				"	init",
				"		I b = new B()",
				"		I c = new C()",
				"		I d = new D()",
				"		if b.foo() != 2",
				"			testFail(\"b\")",
				"		if c.foo() != 3",
				"			testFail(\"c\")",
				"		if d.foo() != 5",
				"			testFail(\"d\")",
				"		testSuccess()",
				"endpackage"
			);
	}
	
	
	@Test
	public void defaultImplInClass() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	interface I",
				"		function foo() returns int",
				"			return 5",
				"	class B implements I",
				"		function foo() returns int",
				"			return 2",
				"	class C implements I",
				"	class D implements I",
				"		function foo() returns int",
				"			return 3",
				"	init",
				"		B b = new B()",
				"		C c = new C()",
				"		D d = new D()",
				"		if b.foo() == 2 and c.foo() == 5 and d.foo() == 3",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	
	@Test
	public void defaultImplInClassDelegation() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	interface I",
				"		function foo() returns int",
				"			return bar()",
				"		function bar() returns int",
				"			return 5",
				"	class B implements I",
				"		function foo() returns int",
				"			return 2",
				"	class C implements I",
				"	class D implements I",
				"		function bar() returns int",
				"			return 3",
				"	init",
				"		B b = new B()",
				"		C c = new C()",
				"		D d = new D()",
				"		if b.foo() == 2 and c.foo() == 5 and d.foo() == 3",
				"			testSuccess()",
				"endpackage"
			);
	}
	
}
