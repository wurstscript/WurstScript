package de.peeeq.wurstscript.tests;

import org.junit.Test;

import de.peeeq.wurstscript.utils.Utils;

public class BugTests extends PscriptTest {

	
	@Test
	public void test_empty_escapesequence() {
		testAssertErrorsLines(false, "Lexical error", 
				"package test",
				"	function foo() returns string",
				"		return \"\\ \" ",
				"endpackage");
	}

	@Test
	public void test_unit_array() {
		testAssertOkLines(false,
				"type unit extends handle",
				"package test",
				"	native testSuccess()",
				"	init",
				"		unit array blub",
				"		blub[4] = null",
				"		if blub[4] == null",
				"			testSuccess()",
				"endpackage");
	}
	
	@Test
	public void test_correct_escapesequence() {
		testAssertOkLines(false,
				"package test",
				"	function foo() returns string",
				"		return \"\\\\ \" ",
				"endpackage");
	}
	

	@Test
	public void varname_with_interface() {
		testAssertOkLines(false,
				"package test",
				"	interface I",
				"		function foo()",
				"	class C implements I",
				"		function foo()",
				"	init",
				"		I a = new C()",
				"		int a2 = 1337",
				"endpackage");
	}
	
}
