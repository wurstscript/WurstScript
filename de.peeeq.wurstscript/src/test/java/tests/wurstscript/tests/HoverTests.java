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
