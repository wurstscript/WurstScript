package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class ModuleTests extends PscriptTest {
	
	private static final String TEST_DIR = "./testscripts/valid/modules/";

	@Override protected boolean testOptimizer() {
		return false;
	}
	
	@Test
	public void simple() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "simple.pscript"), true);
	}

	@Test
	public void multi() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "multi.pscript"), true);
	}
	
	@Test
	public void override() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "override.pscript"), true);
	}
	
	@Test
	public void override2() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "override2.pscript"), true);
	}
	
	@Test
	public void diamond1() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "diamond.pscript"), true);
	}
	
	@Test
	public void diamond2() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "diamond2.pscript"), true);
	}
	
	@Test
	public void initdestroy() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "initdestroy.pscript"), true);
	}
	
}
