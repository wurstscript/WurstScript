package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.requests.GetCompletions;
import org.eclipse.lsp4j.*;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * tests the autocomplete functionality.
 * <p>
 * the position of the cursor is denoted by a bar "|" in the test cases
 */
public class AutoCompleteTests extends WurstLanguageServerTest {


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

    @Test
    public void testNestedClass() { // see https://github.com/wurstscript/WurstScript/issues/753
        CompletionTestData testData = input(true,
                "package test",
                "public class SchoolSpell",
                "    static let qinyun   = new QINYUN()",
                "    static class QINYUN",
                "        let leiyunjianqi  = '0000'",
                "        let xuanbinjinqi  = '0000'",
                "        let zhanguishen   = '0000'",
                "        let shenjianyulei = '0000'",
                "init",
                "    let a = SchoolSpell.qinyun.|",
                "    let x = 42"

        );

        testCompletions(testData, "leiyunjianqi", "shenjianyulei", "xuanbinjinqi", "zhanguishen");
    }

	@Test
	public void testDeprecated() {
		CompletionTestData testData = input(true,
			"package test",
			"@deprecated function getIndexedUnit() returns unit",
			"    return null",
			"function getIndexingUnit() returns unit",
			"    return null",
			"",
			"init",
			"    unit u = getInd|"

		);

		testCompletions(testData, "getIndexingUnit", "getIndexedUnit");
	}


    @Test
    public void testInnerClasses() {
        CompletionTestData testData = input(true,
            "package test",
            "class A",
            "    static class Blue",
            "    static class Boris",
            "    static int Banana = 42",
            "init",
            "    let u = A.|"

        );

        testCompletions(testData, "Banana", "Blue", "Boris");
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


        assertEquals(completionLabels, expectedCompletions, "completionLabels = " + completionLabels);
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


}
