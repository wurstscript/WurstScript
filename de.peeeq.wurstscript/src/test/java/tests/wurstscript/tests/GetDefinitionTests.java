package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.requests.GetDefinition;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;

/**
 * tests the get definition functionality.
 * <p>
 * the position of the cursor is denoted by a bar "|" in the test cases
 */
public class GetDefinitionTests extends WurstLanguageServerTest {


    @Test
    public void simpleExample1() {
        CompletionTestData testData = input(
                "package test",
                "	function int.foo()",
                "	function int.bar()",
                "	init",
                "		int x = 5",
                "		int y = x| + 5",
                "endpackage"
        );

        testGetDef(testData, "4:6-4:7");
    }

    @Test
    public void superConstructorCall() {
        CompletionTestData testData = input(
                "package test",
                "class C",
                "    construct(string s)",
                "    construct(int x)",
                "class D extends C",
                "    construct()",
                "        sup|er(8)",
                "endpackage"
        );

        testGetDef(testData, "3:4-3:13");
    }

    @Test
    public void nonexistantModule() {
        CompletionTestData testData = input(
                "package test",
                "class A",
                "	use B|lub"
        );

        testGetDef(testData, Collections.emptyList());
    }


    private void testGetDef(CompletionTestData testData, String... expectedPositions) {
        testGetDef(testData, Arrays.asList(expectedPositions));
    }

    private void testGetDef(CompletionTestData testData, List<String> expectedPositions) {

        Either<List<? extends Location>, List<? extends LocationLink>> result = calculateDefinition(testData);
        if (result.isLeft()) {
			List<String> definitions = result.getLeft().stream()
				.map((Location l) -> l.getRange().getStart().getLine() + ":" + l.getRange().getStart().getCharacter() + "-" + l.getRange().getEnd().getLine() + ":" + l.getRange().getEnd().getCharacter())
				.sorted()
				.collect(Collectors.toList());

			assertEquals(definitions, expectedPositions);
		} else {
			List<String> definitions = result.getRight().stream()
				.map((LocationLink l) -> l.getTargetRange().getStart().getLine() + ":" + l.getTargetRange().getStart().getCharacter() + "-" + l.getTargetRange().getEnd().getLine() + ":" + l.getTargetRange().getEnd().getCharacter())
				.sorted()
				.collect(Collectors.toList());

			assertEquals(definitions, expectedPositions);
		}
    }

    private Either<List<? extends Location>, List<? extends LocationLink>> calculateDefinition(CompletionTestData testData) {
        BufferManager bufferManager = new BufferManager();
        File projectPath = new File("./test-output").getAbsoluteFile();
        ModelManager modelManager = new ModelManagerImpl(projectPath, bufferManager);
        String uri = projectPath.toURI().toString() + "/wurst/test.wurst";
        bufferManager.updateFile(WFile.create(uri), testData.buffer);
        TextDocumentIdentifier textDocument = new TextDocumentIdentifier(uri);
        Position pos = new Position(testData.line, testData.column);
        CompletionParams position = new CompletionParams(textDocument, pos);
        GetDefinition getDefinition = new GetDefinition(position, bufferManager);

        //new GetCompletions(1, "test", testData.buffer, testData.line, testData.column);

        return getDefinition.execute(modelManager);
    }


}
