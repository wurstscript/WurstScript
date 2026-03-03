package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.requests.HoverInfo;
import org.eclipse.lsp4j.*;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * tests the get definition functionality.
 * <p>
 * the position of the cursor is denoted by a bar "|" in the test cases
 */
public class HoverTests extends WurstLanguageServerTest {


    @Test
    public void nonexistantModule() {
        CompletionTestData testData = input(
                "package test",
                "class A",
                "	use B|lub"
        );

        List<String> text = testHoverText(testData);
        assertEquals(Collections.emptyList(), text);
    }

    @Test
    public void hoverUsesHotdocComment() {
        CompletionTestData testData = input(
                "package test",
                "/** this is hover doc */",
                "function fo|o()",
                "endpackage"
        );

        List<String> text = testHoverText(testData);
        assertTrue(text.stream().anyMatch(s -> s.contains("this is hover doc")), "hover text = " + text);
    }

    @Test
    public void hoverOnCommentShowsNothing() {
        CompletionTestData testData = input(
                "package test",
                "// regular comment |text",
                "function foo()",
                "endpackage"
        );

        List<String> text = testHoverText(testData);
        assertEquals(text, Collections.emptyList(), "hover text = " + text);
    }

    @Test
    public void hoverOnThisTypeShowsKeywordExplanation() {
        CompletionTestData testData = input(
                "package test",
                "/** class doc which should not appear for thistype */",
                "class C",
                "    function f() returns th|istype",
                "        return this",
                "endpackage"
        );

        List<String> text = testHoverText(testData);
        assertTrue(text.stream().anyMatch(s -> s.contains("dynamic type of 'this'")), "hover text = " + text);
        assertTrue(text.stream().noneMatch(s -> s.contains("class doc which should not appear")), "hover text = " + text);
    }

    @Test
    public void hoverForTypeParameterIsReadable() {
        CompletionTestData testData = input(
                "package test",
                "class Box<T>",
                "    function f(T| value)",
                "endpackage"
        );

        List<String> text = testHoverText(testData);
        assertTrue(text.stream().anyMatch(s -> s.contains("type parameter T")), "hover text = " + text);
    }

    @Test
    public void hoverForGlobalShowsInlineInitializer() {
        CompletionTestData testData = input(
                "package test",
                "constant int DEFAULT_SOUND_VOLUME = 127",
                "init",
                "    DEFAULT_SOUND_V|OLUME",
                "endpackage"
        );

        List<String> text = testHoverText(testData);
        assertTrue(text.stream().anyMatch(s -> s.contains("int DEFAULT_SOUND_VOLUME = 127")), "hover text = " + text);
        assertTrue(text.stream().noneMatch(s -> s.startsWith(" = ")), "hover text = " + text);
    }

    @Test
    public void hoverForLocalVarIsStyledSignature() {
        CompletionTestData testData = input(
                "package test",
                "function f()",
                "    int tem|pPos = 42",
                "endpackage"
        );

        List<String> text = testHoverText(testData);
        assertTrue(text.stream().anyMatch(s -> s.contains("int tempPos = 42")), "hover text = " + text);
        assertTrue(text.stream().noneMatch(s -> s.contains("Local Variable")), "hover text = " + text);
    }

    @Test
    public void hoverOnImportShowsImportSignature() {
        CompletionTestData testData = input(
                "package source",
                "endpackage",
                "package target",
                "import so|urce",
                "endpackage"
        );

        List<String> text = testHoverText(testData);
        assertTrue(text.stream().anyMatch(s -> s.contains("import source")), "hover text = " + text);
    }


    private List<String> testHoverText(CompletionTestData testData) {
        Hover result = getHoverInfo(testData);
        if (result.getContents().isLeft()) {
            return result.getContents().getLeft()
                    .stream()
                    .map(s -> {
                        if (s.isLeft()) {
                            return s.getLeft();
                        } else {
                            return s.getRight().getValue();
                        }
                    })
                    .collect(Collectors.toList());
        } else {
            MarkupContent right = result.getContents().getRight();
            return Collections.singletonList(right.getValue());
        }
    }

    private Hover getHoverInfo(CompletionTestData testData) {
        BufferManager bufferManager = new BufferManager();
        File projectPath = new File("./test-output").getAbsoluteFile();
        ModelManager modelManager = new ModelManagerImpl(projectPath, bufferManager);
        String uri = projectPath.toURI() + "/wurst/test.wurst";
        bufferManager.updateFile(WFile.create(uri), testData.buffer);
        TextDocumentIdentifier textDocument = new TextDocumentIdentifier(uri);
        Position pos = new Position(testData.line, testData.column);
        CompletionParams position = new CompletionParams(textDocument, pos);
        HoverInfo hoverInfo = new HoverInfo(position, bufferManager);

        return hoverInfo.execute(modelManager);
    }


}
