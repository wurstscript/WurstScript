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
