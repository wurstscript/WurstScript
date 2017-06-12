package tests.wurstscript.tests;

import org.junit.Test;

public class ConfigPackageTests extends WurstScriptTest {
	
	
	
	@Test
	public void configVar() {
		testAssertOkLines(true, 
				"package Test",
				"native testSuccess()",
				"int x = 5",
				"init",
				"	if x == 6",
				"		testSuccess()",
				"endpackage",
				"package Test_config",
				"@config int x = 6",
				"endpackage"
				);
	}
	
	@Test
	public void configVarWrongType() {
		testAssertErrorsLines(false, "Configured variable must have type int", 
				"package Test",
				"int x = 5",
				"endpackage",
				"package Test_config",
				"@config string x = \"6\"",
				"endpackage"
				);
	}
	
	@Test
	public void configFunc() {
		testAssertOkLines(true, 
				"package Test",
				"native testSuccess()",
				"function foo(int x, int y) returns int",
				"	return x+y",
				"init",
				"	if foo(3,4) == 12",
				"		testSuccess()",
				"endpackage",
				"package Test_config",
				"@config function foo(int x, int y) returns int",
				"	return x*y",
				"endpackage"
				);
	}
	
	@Test
	public void configFuncWrongType() {
		testAssertErrorsLines(true, "Could not find a function foo", 
				"package Test",
				"native testSuccess()",
				"function foo(int x, int y) returns int",
				"	return x+y",
				"endpackage",
				"package Test_config",
				"@config function foo(int x, real y) returns int",
				"	return x*y",
				"endpackage"
				);
	}
	
	
}
