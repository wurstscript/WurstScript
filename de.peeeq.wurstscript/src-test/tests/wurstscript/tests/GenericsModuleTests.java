package tests.wurstscript.tests;

import org.junit.Test;

public class GenericsModuleTests extends WurstScriptTest {
	
	
	@Test
	public void simpleGenericModule() {
		testAssertOkLines(false,  
				"type unit extends handle",
				"package test",
				"	native testSuccess()",
				"	module M<T>",
				"		T t",
				"	class C",
				"		use M<unit>",
				"	init",
				"		C c = new C",
				"		unit u = null",
				"		c.t = u",
				"endpackage"
			);
	}
	
	@Test
	public void biggerModule() {
		testAssertOkLines(false,  
				"type unit extends handle",
				"package test",
				"	native testSuccess()",
				"	module M<S,T>",
				"		private S a",
				"		private T b",
				"		function set(S a, T b) returns T",
				"			T temp = b",
				"			this.a = a",
				"			this.b = b",
				"			return temp",
				"	class C",
				"		use M<unit,real>",
				"	init",
				"		C c = new C",
				"		unit u = null",
				"		real r = c.set(u,1.23)",
				"endpackage"
			);
	}
		
}
