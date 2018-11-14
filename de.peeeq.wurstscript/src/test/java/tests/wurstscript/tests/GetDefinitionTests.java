package tests.wurstscript.tests;

import com.google.common.collect.ImmutableMap;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.requests.GetDefinition;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import org.eclipse.lsp4j.*;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

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


    private void testGetDef(CompletionTestData testData, String... expectedPositions) {
        testGetDef(testData, Arrays.asList(expectedPositions));
    }

    private void testGetDef(CompletionTestData testData, List<String> expectedPositions) {

        List<? extends Location> result = calculateDefinition(testData);

        List<String> definitions = result.stream()
                .map((Location l) -> l.getRange().getStart().getLine() + ":" + l.getRange().getStart().getCharacter() + "-" + l.getRange().getEnd().getLine() + ":" + l.getRange().getEnd().getCharacter())
                .sorted()
                .collect(Collectors.toList());


        assertEquals(definitions, expectedPositions);
    }

    private List<? extends Location> calculateDefinition(CompletionTestData testData) {
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
