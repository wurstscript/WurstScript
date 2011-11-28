package de.peeeq.wurstscript.tests;

import static org.junit.Assert.*;

import org.junit.Test;

public class SimpleStatementTests extends PscriptTest {

	@Test
	public void testIf1() {
		assertOk("testIf1", true, 
				"if 2 == 2 { \n" +
				"	testSuccess()\n" +	
				"} \n");
	}
	
	@Test
	public void testIf2() {
		assertOk("testIf1", true, 
				"if 10 > 5 { \n" +
				"	testSuccess()\n" +	
				"} \n");
	}
	
	@Test
	public void testIf3() {
		assertOk("testIf1", true, 
				"if not 10 > 5 or not 5 > 10 { \n" +
				"	testSuccess()\n" +	
				"} \n");
	}
	
	@Test
	public void testIf4() {
		assertOk("testIf1", true, 
				"if 10 >= 10 { \n" +
				"	testSuccess()\n" +	
				"} \n");
	}
	
	@Test
	public void testIf5() {
		assertOk("testIf1", true, 
				"if -4 <= -4 { \n" +
				"	testSuccess()\n" +	
				"} \n");
	}
	
	@Test
	public void testIf6() {
		assertOk("testIf1", true, 
				"if 3 != 2 { \n" +
				"	testSuccess()\n" +	
				"} \n");
	}
	
	@Test
	public void testIf7() {
		assertOk("testIf1", true, 
				"if (10 == 10 and 5 == 5) { \n" +
				"	testSuccess()\n" +	
				"} \n");
	}
	

	public void assertOk(String name, boolean executeProg, String body) {
		String prog = "package test {\n" +
				"native testFail(string msg)\n" +
				"native testSuccess()\n" +
				"init {\n" +
				body +
				"}\n" +
				"}\n";
		testAssertOk(name, executeProg, prog);
	}

}
