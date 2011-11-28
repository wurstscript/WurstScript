package de.peeeq.wurstscript.tests;

import static org.junit.Assert.*;

import org.junit.Test;

public class SimpleStatementTests extends PscriptTest {

	@Test
	public void testIf1() {
		assertOk("testIf1",
				"if 2 == 2 { \n" +
				"} \n");
	}

	@Override
	public void assertOk(String name, String body) {
		String prog = "package test {\n" +
				"init {\n" +
				body +
				"}\n";
		super.assertOk(name, prog);
	}

}
