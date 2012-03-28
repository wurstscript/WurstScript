package de.peeeq.wurstscript.tests;

import org.junit.Test;

import de.peeeq.wurstscript.utils.Utils;

public class OptimizerTests extends PscriptTest {

	
	@Test
	public void test_number_shortening() {
		assertOk(false,
				"package test",
				"	function foo() returns int",
				"		return 800000",
				"endpackage");
	}
	
	@Test
	public void test_number_shortening2() {
		assertOk(false,
				"package test",
				"	function foo() returns real",
				"		if 1.0 > 0.1",
				"			return 0.0",
				"		else",
				"			return 1.10",
				"endpackage");
	}
	
	@Test
	public void test_EF() {
		assertOk(false,
				"package abc",
				"	function ExecuteFunction( string s )",
				"		int a = 0",
				"	function foo1()",
				"		int i = 0",
				"	function foo2()",
				"		int i2 = 0",
				"	init",
				"		int k = 1",
				"		ExecuteFunction( \"foo\" )",
				"endpackage");
	}
	


	
	private String makeCode(String... body) {
		return Utils.join(body, "\n");
	}
	
	public void assertOk( boolean executeProg, String ... body) {
		String prog = makeCode(body);
		testAssertOk(Utils.getMethodName(1), executeProg, prog);
	}
	
	public void assertError( boolean executeProg, String expected, String ... body) {
		String prog = makeCode(body);
		testAssertErrors(Utils.getMethodName(1), executeProg, prog, expected);
	}

}
