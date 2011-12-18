package de.peeeq.wurstscript.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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