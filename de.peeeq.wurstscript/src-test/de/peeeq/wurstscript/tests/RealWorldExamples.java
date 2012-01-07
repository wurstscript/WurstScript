package de.peeeq.wurstscript.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.WurstCompilerJassImpl;
import de.peeeq.wurstscript.WurstConfig;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;

public class RealWorldExamples extends PscriptTest {
	
	private static final String TEST_DIR = "./testscripts/concept/";

	
	@Override 
	protected boolean testOptimizer() {
		return false;
	}
	
	
	@Test
	public void timerUtils() throws IOException {
		super.testAssertOkFileWithStdLib(new File(TEST_DIR + "TimerUtils.pscript"), false);
	}
	
	@Test
	public void setNullTests() throws IOException {
		super.testAssertOkFileWithStdLib(new File(TEST_DIR + "SetNullTests.pscript"), false);
	}
	
	@Test
	public void setFrottyBugKnockbackNull() throws IOException {
		super.testAssertOkFileWithStdLib(new File(TEST_DIR + "knockback.wurst"), false);
	}
	
	@Test
	public void setFrottyBugEscaperData() throws IOException {
		super.testAssertOkFileWithStdLib(new File(TEST_DIR + "escaperdata.wurst"), false);
	}
	

	@Test
	public void setFrottyBugVector() throws IOException {
		super.testAssertOkFileWithStdLib(new File(TEST_DIR + "vector.j"), false);
	}
	
	@Test
	public void test_war3map() throws IOException {
		super.testAssertOkFileWithStdLib(new File(TEST_DIR + "test_war3map.j"), false);
	}
	
	@Test
	public void test_stdlib() throws IOException {
		List<File> inputs = Lists.newLinkedList();
//		settings.put("lib", "./wurstscript/lib/");
		WurstConfig config = WurstConfig.get();
		config.setSetting("lib", "../Wurstpack/wurstscript/lib/");
		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(new WurstGuiCliImpl());
		for (File f : comp.getLibs().values()) {
			System.out.println("Adding file: " + f);
			inputs.add(f);
		}
		String result = testScript(inputs, null, "stdlib", false, true);
		assertEquals("", result);
	}
	
}