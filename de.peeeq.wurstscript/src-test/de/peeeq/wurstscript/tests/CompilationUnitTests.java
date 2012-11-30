package de.peeeq.wurstscript.tests;

import org.junit.Test;

import de.peeeq.wurstscript.utils.Utils;

/**
 * Tests using using more than one compilation unit 
 *
 */
public class CompilationUnitTests extends WurstScriptTest {

	
	@Test
	public void packages() {
		testAssertOk(false, false, 
				compilationUnit("A.wurst", 
						"package A",
						"endpackage"
						), 
				compilationUnit("B.wurst", 
						"package B",
						"	import A",
						"endpackage"
						));
	}
	
	@Test
	public void jass() {
		testAssertOk(false, false, 
				compilationUnit("A.wurst", 
						"function foo takes nothing returns nothing",
						"endfunction",
						""
						), 
				compilationUnit("B.wurst", 
						"package B",
						"	init",
						"		foo()",
						"endpackage"
						));
	}

}
