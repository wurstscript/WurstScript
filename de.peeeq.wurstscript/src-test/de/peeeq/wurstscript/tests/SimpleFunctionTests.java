package de.peeeq.wurstscript.tests;

import org.junit.Test;

import de.peeeq.wurstscript.utils.Utils;

public class SimpleFunctionTests extends PscriptTest {

	
	@Test
	public void test_has_return_ifs() {
		assertOk(false,
				"function foo(int x) returns int",
				"	if x == 3",
				"		return 1",
				"	else if x > 5",
				"		return 2",
				"	else",
				"		return 3",
				"");
	}
	
	

	@Test
	public void test_unreachable_code() {
		assertError(false, "Unreachable code",
				"function foo(int x) returns int",
				"	int i = 2",
				"	if x == 3",
				"		return 1",
				"		i++",
				"	return i",
				"");
	}
	
	
	public void assertOk( boolean executeProg, String ... body) {
		String prog = makeCode(body);
		testAssertOk(Utils.getMethodName(1), executeProg, prog);
	}

	private String makeCode(String... body) {
		return "package test\n" +
				"	native testFail(string msg)\n" +
				"	native testSuccess()\n" +
				"	" + Utils.join(body, "\n	") +
				"\n" +
				"endpackage\n";
	}
	
	public void assertError( boolean executeProg, String expected, String ... body) {
		String prog = makeCode(body);
		testAssertErrors(Utils.getMethodName(1), executeProg, prog, expected);
	}

}
