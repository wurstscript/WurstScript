package tests.wurstscript.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.peeeq.wurstio.jassinterpreter.DebugPrintError;

public class ParserTests extends WurstScriptTest {
	
	
	@Test
	public void parenthesis1() {
		testAssertErrorsLines(false, "static", 
				"package test",
				"	init",
				"		print(\"hello\" ",
				"endpackage"
			);
	}
	
	@Test
	public void parenthesis2() {
		testAssertErrorsLines(false, "static", 
				"package test",
				"	init",
				"		print(\"hello\")) ",
				"endpackage"
			);
	}
	
	@Test
	public void err_in_closure() {
		testAssertErrorsLines(false, "static", 
				"package test",
				"	init",
				"		doAfter(0.1, ()->begin",
				"			print(\"hello\" + if)",
//				"			\"world\")",
				"		end)",
				"endpackage"
			);
	}
	
	
	
}
