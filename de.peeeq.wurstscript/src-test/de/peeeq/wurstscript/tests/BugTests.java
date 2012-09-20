package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.peeeq.wurstscript.utils.Utils;

public class BugTests extends PscriptTest {

	private static final String TEST_DIR = "./testscripts/concept/";

	@Test
	public void cyclic() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "CyclicError.wurst"), false);
	}
	
	
	@Test
	public void bug62_codearray() {
		testAssertErrorsLines(false, "Code arrays", 
				"package test",
				"	code array coar",
				"endpackage");
	}
	
	@Test
	public void bug61_break() {
		testAssertErrorsLines(false, "inside a loop", 
				"package test",
				"	init",
				"		break",
				"endpackage");
	}
	
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
	public void test_inline_jass_div() {
		testAssertOkLines(false,
				"function divide takes integer a, integer b returns integer",
				"	return a / b",
				"endfunction",
				"package test",
				"	native testSuccess()",
				"	init",
				"		if divide(17,3) == 5",
				"			testSuccess()",
				"endpackage");
	}
	
	
	@Test
	public void test_for_from() {
		testAssertOkLines(false,
				"type unit extends handle",
				"type group extends handle",
				"native FirstOfGroup takes group g returns unit",
				"native GroupRemoveUnit takes group g, unit u returns nothing",
				"package test",
				"	function group.hasNext() returns boolean",
				"		return FirstOfGroup(this) != null",
				"	function group.next() returns unit",
				"		let u = FirstOfGroup(this)",
				"		GroupRemoveUnit(this, u)",
				"		return u",
				"	function group.close()",
				"		skip",
				"	init",
				"		group g = null",
				"		for unit u from g",
				"			skip",
				"endpackage");
	}
	
	
	@Test
	public void test_for_in() {
		testAssertOkLines(false,
				"type unit extends handle",
				"type group extends handle",
				"native FirstOfGroup takes group g returns unit",
				"native GroupRemoveUnit takes group g, unit u returns nothing",
				"package test",
				"	function group.iterator() returns group",
                "		// a correct implementation would return a copy",
                "		return this",
				"	function group.hasNext() returns boolean",
				"		return FirstOfGroup(this) != null",
				"	function group.next() returns unit",
				"		let u = FirstOfGroup(this)",
				"		GroupRemoveUnit(this, u)",
				"		return u",
				"	function group.close()",
				"		skip",
				"	init",
				"		group g = null",
				"		for unit u in g",
				"			skip",
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
	
	@Test
	public void testCodeNull() {
		testAssertOkLines(false, 
				"package test",
				"	function foo(code c)",
				"		skip",
				"	init",
				"		foo(null)",
				"endpackage");
	}
	
}
