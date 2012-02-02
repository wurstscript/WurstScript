package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class TupleTests extends PscriptTest {
	
	
	@Test
	public void simple() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	tuple vec(real x, real y, real z)",
				"	init",
				"		vec v = vec(1,2,3)",
				"		v.x = 4",
				"		vec u = v",
				"		u.y = 5",
				"		if v.x == 4 and v.y == 2 and u.y == 5",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void swap() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	tuple pair(real x, real y)",
				"	init",
				"		pair p = pair(1,2)",
				"		p = pair(p.y, p.x)",
				"		if p.x == 2 and p.y == 1",
				"			testSuccess()",
				"endpackage"
			);
	}
	
}
