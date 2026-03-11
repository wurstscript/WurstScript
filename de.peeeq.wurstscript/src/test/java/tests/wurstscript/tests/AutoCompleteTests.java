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

import static org.testng.Assert.*;

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

    @Test
    public void constantArrayLengthCompletion() {
        CompletionTestData testData = input(
            "package test",
            "const ints = [1, 2, 3]",
            "init",
            "    ints.|",
            "endpackage"
        );

        CompletionList completions = calculateCompletions(testData);

        assertTrue(
            completions.getItems().stream().anyMatch(c -> "length".equals(c.getLabel())),
            "Expected to suggest the synthetic array length property"
        );
    }

    @Test
    public void nonConstantArrayDoesNotSuggestLength() {
        CompletionTestData testData = input(
            "package test",
            "int array ints",
            "init",
            "    ints.|",
            "endpackage"
        );

        CompletionList completions = calculateCompletions(testData);

        assertFalse(
            completions.getItems().stream().anyMatch(c -> "length".equals(c.getLabel())),
            "Did not expect to suggest length for non-constant arrays"
        );
    }

    @Test
    public void completionUsesHotdocComment() {
        CompletionTestData testData = input(
                "package test",
                "/** docs for foo */",
                "function fooBar()",
                "init",
                "    foo|",
                "endpackage"
        );

        CompletionList completions = calculateCompletions(testData);
        CompletionItem completion = completions.getItems().stream()
                .filter(c -> "fooBar".equals(c.getLabel()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("fooBar completion not found"));

        assertTrue(
                completion.getDocumentation() != null && completion.getDocumentation().getLeft().contains("docs for foo"),
                "completion documentation = " + completion.getDocumentation()
        );
    }

    @Test
    public void noCompletionOnRealNumberDot() {
        CompletionTestData testData = input(
                "package test",
                "init",
                "    real x = 2.|",
                "endpackage"
        );

        CompletionList completions = calculateCompletions(testData);
        assertTrue(completions.getItems().isEmpty(), "completionLabels = " + sortedLabels(completions));
    }

    @Test
    public void separatorInsensitiveMatchKeepsTypePreferredResultOnTop() {
        CompletionTestData testData = input(
                "package test",
                "function MYXX_TEXT_JUSTIFY_CENTER() returns int",
                "    return 1",
                "function MYXX_TEXTURES() returns bool",
                "    return true",
                "init",
                "    int x = MYXXTEXTU|",
                "endpackage"
        );

        List<String> labels = sortedLabels(calculateCompletions(testData));
        assertTrue(labels.contains("MYXX_TEXT_JUSTIFY_CENTER"), "labels = " + labels);
        assertTrue(labels.contains("MYXX_TEXTURES"), "labels = " + labels);
        assertLabelBefore(labels, "MYXX_TEXT_JUSTIFY_CENTER", "MYXX_TEXTURES");
    }

    @Test
    public void shortPrefixExpectedTypeComesBeforeWrongType() {
        CompletionTestData testData = input(
                "package test",
                "function QZGood() returns int",
                "    return 1",
                "function QZBad() returns bool",
                "    return true",
                "init",
                "    int x = QZ|",
                "endpackage"
        );

        List<String> labels = sortedLabels(calculateCompletions(testData));
        assertTrue(labels.contains("QZGood"), "labels = " + labels);
        assertTrue(labels.contains("QZBad"), "labels = " + labels);
        assertLabelBefore(labels, "QZGood", "QZBad");
    }

    @Test
    public void emptyComparisonRhsUsesExpectedTypeForRanking() {
        CompletionTestData testData = input(
                "package test",
                "function CMP_PlayerCandidate() returns player",
                "    return Player(0)",
                "function CMP_BoolCandidate() returns bool",
                "    return true",
                "init",
                "    player p = Player(0)",
                "    if p == |",
                "endpackage"
        );

        List<String> labels = sortedLabels(calculateCompletions(testData));
        assertTrue(labels.contains("CMP_PlayerCandidate"), "labels = " + labels);
        assertEquals(labels.get(0), "CMP_PlayerCandidate", "labels = " + labels);
    }

    @Test
    public void emptyCallArgUsesExpectedTypeForRanking() {
        CompletionTestData testData = input(
                "package test",
                "function player.getState(playerstate s) returns int",
                "    return 0",
                "function ARG_PlayerStateCandidate() returns playerstate",
                "    return PLAYER_STATE_RESOURCE_GOLD",
                "function ARG_BoolCandidate() returns bool",
                "    return true",
                "init",
                "    player p = Player(0)",
                "    p.getState(|)",
                "endpackage"
        );

        List<String> labels = sortedLabels(calculateCompletions(testData));
        assertTrue(labels.contains("ARG_PlayerStateCandidate"), "labels = " + labels);
        assertTrue(labels.contains("ARG_BoolCandidate"), "labels = " + labels);
        assertLabelBefore(labels, "ARG_PlayerStateCandidate", "ARG_BoolCandidate");
    }

    @Test
    public void stdlibGetPlayerStateEmptyArgPrefersPlayerStateConstants() {
        CompletionTestData testData = input(
                "package test",
                "init",
                "    GetPlayerState(Player(0), |)",
                "endpackage"
        );

        List<String> labels = sortedLabels(calculateCompletions(testData));
        assertTrue(labels.contains("PLAYER_STATE_RESOURCE_GOLD"), "labels = " + labels);
        assertTrue(labels.contains("AbilityId"), "labels = " + labels);
        assertLabelBefore(labels, "PLAYER_STATE_RESOURCE_GOLD", "AbilityId");
    }

    @Test
    public void extensionGetStateEmptyArgPrefersPlayerStateConstants() {
        CompletionTestData testData = input(
                "package test",
                "function player.getState(playerstate whichState) returns int",
                "    return GetPlayerState(this, whichState)",
                "init",
                "    player p = Player(0)",
                "    p.getState(|)",
                "endpackage"
        );

        List<String> labels = sortedLabels(calculateCompletions(testData));
        assertTrue(labels.contains("PLAYER_STATE_RESOURCE_GOLD"), "labels = " + labels);
        assertTrue(labels.contains("AbilityId"), "labels = " + labels);
        assertLabelBefore(labels, "PLAYER_STATE_RESOURCE_GOLD", "AbilityId");
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
        String uri = projectPath.toURI() + "/wurst/test.wurst";
        bufferManager.updateFile(WFile.create(uri), testData.buffer);
        TextDocumentIdentifier textDocument = new TextDocumentIdentifier(uri);
        Position pos = new Position(testData.line, testData.column);
        CompletionParams position = new CompletionParams(textDocument, pos);
        GetCompletions getCompletions = new GetCompletions(position, bufferManager);

        //new GetCompletions(1, "test", testData.buffer, testData.line, testData.column);

        return getCompletions.execute(modelManager);
    }

    private List<String> sortedLabels(CompletionList result) {
        return result.getItems().stream()
                .sorted(Comparator.comparing(CompletionItem::getSortText))
                .map(CompletionItem::getLabel)
                .collect(Collectors.toList());
    }

    private void assertLabelBefore(List<String> labels, String first, String second) {
        int iFirst = labels.indexOf(first);
        int iSecond = labels.indexOf(second);
        assertTrue(iFirst >= 0, "Missing label " + first + " in " + labels);
        assertTrue(iSecond >= 0, "Missing label " + second + " in " + labels);
        assertTrue(iFirst < iSecond, "Expected " + first + " before " + second + ", labels = " + labels);
    }


}
