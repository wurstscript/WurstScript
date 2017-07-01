package tests.wurstscript.tests;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.requests.GetCompletions;
import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.stream.Collectors;

/**
 * tests the autocomplete functionality.
 * <p>
 * the position of the cursor is denoted by a bar "|" in the test cases
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
    public void simpleExample3() {
        CompletionTestData testData = input(
                "package test",
                "	function int.foo(int a, bool b)",
                "	init",
                "		int x = 5",
                "		x.f|",
                "endpackage"
        );

        testCompletions(testData, "foo");
    }

    @Test
    public void overload1() {
        CompletionTestData testData = input(
                "package test",
                "	function int.foo()",
                "	function int.foo(int x)",
                "	init",
                "		int x = 5",
                "		x.f|",
                "endpackage"
        );

        testCompletions(testData, "foo", "foo");
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


    private void testCompletions(CompletionTestData testData, String... expectedCompletions) {
        testCompletions(testData, Arrays.asList(expectedCompletions));
    }

    private void testCompletions(CompletionTestData testData, List<String> expectedCompletions) {

        BufferManager bufferManager = new BufferManager();
        ModelManager modelManager = new ModelManagerImpl(new File("."), bufferManager);
        String uri = "file:///tmp/test.wurst";
        bufferManager.updateFile(WFile.create(uri), testData.buffer);
        TextDocumentIdentifier textDocument = new TextDocumentIdentifier(uri);
        Position pos = new Position(testData.line, testData.column);
        TextDocumentPositionParams position = new TextDocumentPositionParams(textDocument, pos);
        GetCompletions getCompletions = new GetCompletions(position, bufferManager);

                //new GetCompletions(1, "test", testData.buffer, testData.line, testData.column);

        Handler h = new ConsoleHandler();
        WLogger.setHandler(h);

        CompletionList result = getCompletions.execute(modelManager);

        // debug output:
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(result));

        List<String> completionLabels = result.getItems().stream()
                .sorted(Comparator.comparing(i -> i.getSortText()))
                .map(completion -> completion.getLabel())
                .collect(Collectors.toList());



        Assert.assertEquals(expectedCompletions, completionLabels);
    }

    private CompletionTestData input(String... lines) {
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

        return new CompletionTestData(buffer.toString(), completionLine-1, completionColumn-1);
    }

    private WurstModel compile(String... lines) {
        String input = String.join("\n", lines);
        WurstGui gui = new WurstGuiLogger();
        RunArgs runArgs = new RunArgs();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui, null, runArgs);
        compiler.getErrorHandler().enableUnitTestMode();
        Map<String, String> inputMap = ImmutableMap.of("test", input);
        return parseFiles(Collections.<File>emptyList(), inputMap, false, compiler);
    }


}
