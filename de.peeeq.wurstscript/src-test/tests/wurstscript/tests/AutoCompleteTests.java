package tests.wurstscript.tests;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.stream.Collectors;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.requests.GetCompletions;
import de.peeeq.wurstio.languageserver.requests.GetCompletions.WurstCompletion;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import junit.framework.Assert;

/**
 * tests the autocomplete functionality.
 * 
 *  the position of the cursor is denoted by a bar "|" in the test cases 
 *
 */
public class AutoCompleteTests extends WurstScriptTest {

	
	@Test
	public void simpleExample1() {
		CompletionTestData testData = input(
				"package test",
				"	function int.foo()",
				"	function int.bar()",
				"	init",
				"		int x = 5",
				"		x.|",
				"endpackage"
		);
		
		testCompletions(testData, "bar", "foo");
	}
	
	@Test
	public void simpleExample2() {
		CompletionTestData testData = input(
				"package test",
				"	function int.foo()",
				"	function int.bar()",
				"	init",
				"		int x = 5",
				"		x.f|",
				"endpackage"
		);
		
		testCompletions(testData, "foo");
	}
	
	
	@Test
	public void onlyFromClasses() {
		CompletionTestData testData = input(
				"package test",
				"	function fuu()",
				"	function int.foo()",
				"	init",
				"		int x = 5",
				"		x.f|",
				"endpackage"
		);
		
		testCompletions(testData, "foo");
	}
	

	@Test
	public void inForLoop() {
		CompletionTestData testData = input(
				"package test",
				"	function int.foo()",
				"	function faaa()",
				"	function int.bar()",
				"	init",
				"		int x = 5",
				"		for i in x.f|",
				"endpackage"
		);
		
		testCompletions(testData, "foo");
	}
	
	@Test
	public void ratings_returnType1() {
		CompletionTestData testData = input(
				"package test",
				"	function int.foo() returns int",
				"	function int.fuu() returns bool",
				"	init",
				"		int x = 5",
				"		int y = x.f|",
				"endpackage"
		);
		
		testCompletions(testData, "foo", "fuu");
	}
	
	@Test
	public void ratings_returnType2() {
		CompletionTestData testData = input(
				"package test",
				"	function int.foo() returns int",
				"	function int.fuu() returns bool",
				"	init",
				"		int x = 5",
				"		bool y = x.f|",
				"endpackage"
		);
		
		testCompletions(testData, "fuu", "foo");
	}
	
	
	
	static class CompletionTestData {
		String buffer;
		int line;
		int column;
		
		public CompletionTestData(String buffer, int line, int column) {
			this.buffer = buffer;
			this.line = line;
			this.column = column;
		}
	}
	
	
	private void testCompletions(CompletionTestData testData, String ...expectedCompletions) {
		testCompletions(testData, Arrays.asList(expectedCompletions));
	}

	private void testCompletions(CompletionTestData testData, List<String> expectedCompletions) {
		GetCompletions getCompletions = new GetCompletions(1, "test", testData.buffer, testData.line, testData.column);

		ModelManager modelManager = new ModelManagerImpl(new File("."));

		Handler h = new ConsoleHandler();
		WLogger.setHandler(h);

		List<WurstCompletion> result = getCompletions.execute(modelManager);
		
		Collections.sort(result, Comparator
				.comparingDouble(WurstCompletion::getRating).reversed()
				.thenComparing(WurstCompletion::getDisplayString));
		
		// debug output:
//		System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(result));

		List<String> completionLabels = result.stream()
			.map(completion -> completion.getDisplayString())
			.collect(Collectors.toList());
		
		Assert.assertEquals(expectedCompletions, completionLabels);
	}

	private CompletionTestData input(String...lines) {
		StringBuilder buffer = new StringBuilder();
		int completionLine = -1;
		int completionColumn = -1;
		int lineNr = 0;
		for (String line : lines) {
			lineNr++;
			int cursorIndex = line.indexOf('|');
			if (cursorIndex >= 0) {
				completionLine = lineNr;
				completionColumn = cursorIndex + 1;
				buffer.append(line.replace("'", ""));
			} else {
				buffer.append(line);
			}
			buffer.append("\n");
		}
		
		return new CompletionTestData(buffer.toString(), completionLine, completionColumn);
	}

	private WurstModel compile(String ... lines) {
		String input = String.join("\n", lines);
		WurstGui gui = new WurstGuiLogger();
		RunArgs runArgs = new RunArgs();
		WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui, null, runArgs);
		compiler.getErrorHandler().enableUnitTestMode();
		Map<String, String> inputMap = ImmutableMap.of("test" , input);
		return parseFiles(Collections.<File>emptyList(), inputMap, false, compiler);
	}


}
