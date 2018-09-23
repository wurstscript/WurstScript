package tests.wurstscript.tests;

import com.google.common.collect.ImmutableMap;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.requests.GetCompletions;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import org.eclipse.lsp4j.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

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
    public void testWithParentheses() {
        CompletionTestData testData = input(
                "package test",
                "init",
                "    CreateG|()",
                ""
        );

        CompletionList completions = calculateCompletions(testData);
        assertEquals(1, completions.getItems().size());
        CompletionItem c = completions.getItems().get(0);
        assertEquals("CreateGroup", c.getInsertText());
    }


    @Test
    public void testWithParentheses2() {
        CompletionTestData testData = input(
                "package test",
                "init",
                "    CreateU|(x,y,z)",
                ""
        );

        CompletionList completions = calculateCompletions(testData);
        assertFalse(completions.getItems().isEmpty());
        CompletionItem comp = completions.getItems().stream()
                .filter(c -> c.getLabel().equals("CreateUnit"))
                .findFirst()
                .get();
        assertEquals(comp.getInsertText(), "CreateUnit");
    }

    @Test
    public void testWithoutParentheses() {
        CompletionTestData testData = input(
                "package test",
                "	init",
                "		CreateG|",
                "endpackage"
        );

        CompletionList completions = calculateCompletions(testData);
        assertEquals(1, completions.getItems().size());
        CompletionItem c = completions.getItems().get(0);
        assertEquals("CreateGroup()", c.getInsertText());
    }


    @Test
    public void testWithoutParentheses2() {
        CompletionTestData testData = input(
                "package test",
                "	init",
                "		CreateU|",
                "endpackage"
        );

        CompletionList completions = calculateCompletions(testData);
        assertFalse(completions.getItems().isEmpty());
        CompletionItem comp = completions.getItems().stream()
                .filter(c -> c.getLabel().equals("CreateUnit"))
                .findFirst()
                .get();
        assertEquals(comp.getInsertText(), "CreateUnit(${1:id}, ${2:unitid}, ${3:x}, ${4:y}, ${5:face})");
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

    @Test
    public void completionAtEndOfFileWithNewline() { // see #584
        CompletionTestData testData = input(false,
                "package test",
                "function int.foo() returns int",
                "function int.fuu() returns bool",
                "init",
                "	int x = 5",
                "",
                "	x.f|"
        );

        testCompletions(testData, "foo", "fuu");
    }

    @Test
    public void completionAtEndOfFileWithNewline2() {
        CompletionTestData testData = input(true,
                "package test",
                "function int.foo() returns int",
                "function int.fuu() returns bool",
                "init",
                "	int x = 5",
                "",
                "	x.f|"
        );

        testCompletions(testData, "foo", "fuu");
    }

    @Test
    public void testAfterDot() {
        CompletionTestData testData = input(true,
                "package test",
                "class A",
                "function A.foo() returns int",
                "function A.fuu() returns bool",
                "function test()",
                "	new A()",
                "		.|",
                "",
                "function a()"
        );

        testCompletions(testData, "foo", "fuu");
    }


    @Test
    public void closuresWithOperatorOverloading() {
        CompletionTestData testData = input(
                "package test",
                "	function int.foo()",
                "	function int.bar()",
                "	class C",
                "		construct(G g, int y)",
                "		construct(F f, int y)",
                "	interface F",
                "		function apply(int i) returns int",
                "	interface G",
                "		function apply() returns int",
                "	init",
                "		int x = 5",
                "		new C(i -> i + 1, x.|)",
                "endpackage"
        );

        testCompletions(testData, "bar", "foo");
    }

    @Test
    public void testPrivateMethod() {
        CompletionTestData testData = input(true,
                "package test",
                "class A",
                "    function foo() returns int",
                "        return 1",
                "    private function fuu() returns bool",
                "        return true",
                "function test()",
                "    let a = new A()",
                "    a.f|"
        );

        testCompletions(testData, "foo");
    }

    @Test
    public void testPrivateMethod2() {
        CompletionTestData testData = input(true,
                "package test",
                "class A",
                "    function foo() returns int",
                "        return 1",
                "    private function fuu() returns bool",
                "        return true",
                "    static function test()",
                "        let a = new A()",
                "        a.f|"
        );

        testCompletions(testData, "foo", "fuu");
    }

    @Test
    public void testProtectedMethod() {
        CompletionTestData testData = input(true,
                "package test",
                "class A",
                "    function foo() returns int",
                "        return 1",
                "    protected function fuu() returns bool",
                "        return true",
                "package test2",
                "import test",
                "class B extends A",
                "    static function test()",
                "        let a = new A()",
                "        a.f|"
        );


        testCompletions(testData, "foo", "fuu");
    }

    @Test
    public void testProtectedMethod2() {
        CompletionTestData testData = input(true,
                "package test",
                "class A",
                "    function foo() returns int",
                "        return 1",
                "    protected function fuu() returns bool",
                "        return true",
                "package test2",
                "import test",
                "class B",
                "    static function test()",
                "        let a = new A()",
                "        a.f|"
        );

        testCompletions(testData, "foo");
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

        CompletionList result = calculateCompletions(testData);

        List<String> completionLabels = result.getItems().stream()
                .sorted(Comparator.comparing(i -> i.getSortText()))
                .map(completion -> completion.getLabel())
                .collect(Collectors.toList());


        assertEquals(expectedCompletions, completionLabels);
    }

    private CompletionList calculateCompletions(CompletionTestData testData) {
        BufferManager bufferManager = new BufferManager();
        File projectPath = new File("./test-output").getAbsoluteFile();
        ModelManager modelManager = new ModelManagerImpl(projectPath, bufferManager);
        String uri = projectPath.toURI().toString() + "/wurst/test.wurst";
        bufferManager.updateFile(WFile.create(uri), testData.buffer);
        TextDocumentIdentifier textDocument = new TextDocumentIdentifier(uri);
        Position pos = new Position(testData.line, testData.column);
        CompletionParams position = new CompletionParams(textDocument, pos);
        GetCompletions getCompletions = new GetCompletions(position, bufferManager);

        //new GetCompletions(1, "test", testData.buffer, testData.line, testData.column);

        return getCompletions.execute(modelManager);
    }

    private CompletionTestData input(String... lines) {
        return input(true, lines);
    }

    private CompletionTestData input(boolean newLineAtEnd, String... lines) {
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
                buffer.append(line.replaceFirst("\\|", ""));
            } else {
                buffer.append(line);
            }
            if (newLineAtEnd || lineNr < lines.length) {
                buffer.append("\n");
            }
        }

        return new CompletionTestData(buffer.toString(), completionLine - 1, completionColumn - 1);
    }

    private WurstModel compile(String... lines) {
        String input = String.join("\n", lines);
        WurstGui gui = new WurstGuiLogger();
        RunArgs runArgs = new RunArgs();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, runArgs);
        compiler.getErrorHandler().enableUnitTestMode();
        Map<String, String> inputMap = ImmutableMap.of("test", input);
        return parseFiles(Collections.<File>emptyList(), inputMap, false, compiler);
    }


}
