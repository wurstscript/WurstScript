package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class RealWorldExamples extends PscriptTest {
	
	private static final String TEST_DIR = "./testscripts/concept/";

	
	@Override 
	protected boolean testOptimizer() {
		return false;
	}
	
	
	@Test
	public void simple() throws IOException {
		super.testAssertOkFileWithStdLib(new File(TEST_DIR + "TimerUtils.pscript"), false);
	}
	
	@Test
	public void test_war3map() throws IOException {
		super.testAssertOkFileWithStdLib(new File(TEST_DIR + "test_war3map.j"), false);
	}
	
}