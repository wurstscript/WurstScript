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
