package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class ClassesExtTests extends PscriptTest {
	
	
	@Test
	public void extends_override() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	class C",
				"		function foo() returns int",
				"			return 3",
				"	class D extends C",
				"		function foo() returns int",
				"			return 4",
				"	init",
				"		if new D().foo() == 4",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void extends_simple() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	class C",
				"		function foo() returns int",
				"			return 3",
				"	class D extends C",
				"	init",
				"		if new D().foo() == 3",
				"			testSuccess()",
				"endpackage"
			);
	}

}
