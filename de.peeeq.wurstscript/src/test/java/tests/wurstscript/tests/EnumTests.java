package tests.wurstscript.tests;

import org.junit.Test;

public class EnumTests extends WurstScriptTest {
	
	@Test
	public void enum_short1() {
		testAssertOkLines(false, 
				"package test",
				"enum Blub",
				"	A",
				"	B",
				"init",
				"	Blub a = A",
				"	a = B"
			);
	}
	
	@Test
	public void enum_short2() {
		testAssertOkLines(false, 
				"package test",
				"enum Blub",
				"	A",
				"	B",
				"init",
				"	Blub a = Blub.A",
				"	switch a",
				"		case A",
				"		case B"
			);
	}
	
}
