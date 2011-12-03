package de.peeeq.wurstscript.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import de.peeeq.wurstscript.utils.Utils;

public class SimpleStatementTests extends PscriptTest {

	@Test
	public void testIf1() {
		assertOk(true, 
				"if 2 == 2 ",
				"	testSuccess()\n" +	
				"");
	}
	
	@Test
	public void testIf2() {
		assertOk(true, 
				"if 10 > 5 ",
				"	testSuccess()\n" +	
				"");
	}
	
	@Test
	public void testIf3() {
		assertOk(true, 
				"if not 10 > 5 or not 5 > 10 ",
				"	testSuccess()\n" +	
				"");
	}
	
	@Test
	public void testIf4() {
		assertOk(true, 
				"if 10 >= 10 ",
				"	testSuccess()\n" +	
				"");
	}
	
	@Test
	public void testIf5() {
		assertOk(true, 
				"if -4 <= -4 ",
				"	testSuccess()\n" +	
				"");
	}
	
	@Test
	public void testIf6() {
		assertOk(true, 
				"if 3 != 2 ",
				"	testSuccess()",	
				"");
	}
	
	@Test
	public void testIf7() {
		assertOk(true, 
				"if (10 == 10 and 5 == 5)",
				"	testSuccess()",
				"");
	}
	
	@Test
	public void testWhile1() {
		assertOk(true,
				"int x = 10",
				"while x > 2",
				"	x = x - 1",
				"if x == 2",
				"	testSuccess()",
				"");
	}
	
	@Test
	public void testWhileBreak() {
		assertOk(true,
				"int x = 10",
				"while true",
				"	x = x - 1",
				"	if x <= 2",
				"		break",
				"if x == 2",
				"	testSuccess()",
				"");
	}
	
	@Test
	public void testFor1() {
		assertOk(true,
				"int x = 0",
				"for int i in 1 ... 10",
				"	x = x + 1",
				"if x == 10",
				"	testSuccess()",
				"");
	}
	
	@Test
	public void testFor2() {
		assertOk(true,
				"int x = 0",
				"for int i in 1 ... 10",
				"	x = x + i",
				"if x == 55",
				"	testSuccess()",
				"");
	}
	
	
	public void assertOk( boolean executeProg, String ... body) {
		String prog = "package test\n" +
				"	native testFail(string msg)\n" +
				"	native testSuccess()\n" +
				"	init \n" +
				"		" + Utils.join(body, "\n		") +
				"\n" +
				"endpackage\n";
		testAssertOk(Utils.getMethodName(1), executeProg, prog);
	}

}
