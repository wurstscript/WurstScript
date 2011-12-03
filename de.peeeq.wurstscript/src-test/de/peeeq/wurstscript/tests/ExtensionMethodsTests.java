package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class ExtensionMethodsTests extends PscriptTest {
	
	private static final String TEST_DIR = "./testscripts/valid/extensionmethods/";

	

	
	
	@Test
	public void extensionFunction_int() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	function int.add(int x) returns int",
				"		return this + x",
				"",
				"	init",
				"		int a = 3",
				"		if a.add(4) == 7",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void extensionFunction_class() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	class C",
				"		function foo() returns int",
				"			return 3",
				"	function C.foo() returns int",
				"		return 4",
				"	function C.bar() returns int",
				"		return 5",
				"",
				"	init",
				"		C c = new C()",
				"		if c.foo() == 3 and c.bar() == 5",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	
}
