package de.peeeq.wurstscript.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WurstCompilerJassImpl;
import de.peeeq.wurstscript.WurstConfig;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;

public class RealWorldExamples extends PscriptTest {
	
	private static final String TEST_DIR = "./testscripts/concept/";
	private static final String BUG_DIR = "./testscripts/realbugs/";
	
	@Override 
	protected boolean testOptimizer() {
		return false;
	}
	
	
	@Test
	public void testCyclic() throws IOException {
		super.testAssertErrorFileWithStdLib(new File(BUG_DIR + "cyclic.wurst"), "cyclic dependency", true);
	}
	
	@Test
	public void testLists() throws IOException {
		super.testAssertOkFileWithStdLib(new File(TEST_DIR + "ListsTest.wurst"), true);
	}

	
	@Test
	public void testVecs() throws IOException {
		super.testAssertOkFileWithStdLib(new File(TEST_DIR + "VecTest.wurst"), true);
	}
	
	@Test
	public void timerUtils() throws IOException {
		super.testAssertOkFileWithStdLib(new File(TEST_DIR + "TimerUtils.pscript"), false);
	}
	
	@Test
	public void thisBug() throws IOException {
		super.testAssertOkFileWithStdLib(new File(TEST_DIR + "ThisBug.wurst"), false);
	}
	
	@Test
	public void ForFrom() throws IOException {
		super.testAssertOkFileWithStdLib(new File(TEST_DIR + "ForFrom.wurst"), false);
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
	public void frottyTupleBug() throws IOException {
		super.testAssertOkFileWithStdLib(new File(TEST_DIR + "tupleBug.wurst"), false);
	}
	
	@Test
	public void optimizer() throws IOException {
		super.testAssertOkFileWithStdLib(new File(TEST_DIR + "optimizer.wurst"), false);
	}
	
	@Test
	public void staticCallback() throws IOException {
		super.testAssertOkFileWithStdLib(new File(TEST_DIR + "staticCallback.wurst"), false);
	}
	
	@Test
	public void nonStaticCallback() throws IOException {
		super.testAssertErrorFileWithStdLib(new File(TEST_DIR + "nonStaticCallback.wurst"), "without parameters", false);
	}
	
	@Test
	public void test_stdlib() throws IOException {
		List<File> inputs = Lists.newLinkedList();
//		settings.put("lib", "./wurstscript/lib/");
		WurstConfig config = WurstConfig.get();
		config.setSetting("lib", "../Wurstpack/wurstscript/lib/");
		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(new WurstGuiCliImpl(), RunArgs.defaults());
		for (File f : comp.getLibs().values()) {
			System.out.println("Adding file: " + f);
			inputs.add(f);
		}
		testScript(inputs, null, "stdlib", false, true);
	}
	
}