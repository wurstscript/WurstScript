package tests.prettyprint;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.AstElementWithSource;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.Identifier;
import de.peeeq.wurstscript.attributes.prettyPrint.MaxOneSpacer;
import de.peeeq.wurstscript.attributes.prettyPrint.PrettyPrinter;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.parser.WPosWithComments;
import de.peeeq.wurstscript.parser.WPosWithComments.Comment;
import org.testng.annotations.Test;
import tests.wurstscript.tests.WurstScriptTest;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

public class PrettyPrintTest extends WurstScriptTest {

    private String setUp(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        File inFile = new File(filename);
        String content = Files.toString(inFile, Charsets.UTF_8);

        WurstGui gui = new WurstGuiCliImpl();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, new RunArgs("-prettyPrint"));

        CompilationUnit cu = compiler.parse("test", new StringReader(content));

        debugPrint(cu);

        PrettyPrinter.prettyPrint(cu, new MaxOneSpacer(), sb, 0);

        return sb.toString();
    }

    private void debugPrint(CompilationUnit cu) {
        StringBuilder dsb = new StringBuilder();
        debugPrint(cu, dsb, 0);
        System.out.println(dsb);
    }

    private void debugPrint(Element e, StringBuilder sb, int i) {
        List<Comment> commentsBefore = Collections.emptyList();
        List<Comment> commentsAfter = Collections.emptyList();
        if (e instanceof AstElementWithSource) {
            AstElementWithSource s = (AstElementWithSource) e;
            WPos source = s.getSource();
            if (source instanceof WPosWithComments) {
                WPosWithComments source2 = (WPosWithComments) source;
                commentsBefore = source2.getCommentsBefore();
                commentsAfter = source2.getCommentsAfter();
            }
        }
        sb.append("(");
        for (Comment comment : commentsBefore) {
            sb.append(comment.getContent());
            newLine(sb, i + 1);
        }
        sb.append(e.getClass().getSimpleName().replaceAll("Impl", ""));
        if (e instanceof Identifier) {
            sb.append(" ").append(((Identifier) e).getName());
        }
        for (int j = 0; j < e.size(); j++) {
            newLine(sb, i + 1);
            debugPrint(e.get(j), sb, i + 2);
        }
        for (Comment comment : commentsAfter) {
            newLine(sb, i + 1);
            sb.append(comment.getContent());
        }
        sb.append(")");
    }

    private void newLine(StringBuilder sb, int i) {
        sb.append("\n");
        for (int j = 0; j < i; j++) {
            sb.append(" ");
        }
    }

    private String expectedFile(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        File inFile = new File(filename);
        return Files.toString(inFile, Charsets.UTF_8);
    }

    // Note: only works with single extension files, i.e it doesn't work with:
    // asdf.tar.gz
    private static String extension(String filename) {
        String extension = "";

        int i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i + 1);
        }
        return extension;
    }

    private void test(String filename) throws IOException {
        String testFilenameFormat = "testscripts/pretty/%s";
        String pretty = setUp(String.format(testFilenameFormat, filename));
        String expected = expectedFile(String.format(testFilenameFormat, filename));

        assertEquals(expected, pretty);
    }

    @Test
    public void testPrettyIf() throws IOException {
        test("If.wurst");
    }

    @Test
    public void testPrettyLoops() throws IOException {
        test("Loops.wurst");
    }

    @Test
    public void testPrettyAssignments() throws IOException {
        test("Assignment_shorthand.wurst");
    }

    @Test
    public void testCascade() throws IOException {
        test("Cascade.wurst");
    }

    @Test
    public void testSwitch() throws IOException {
        test("Switch.wurst");
    }

    @Test
    public void testAnnotations() throws IOException {
        test("Annotations.wurst");
    }

    @Test
    public void testClosures() throws IOException {
        test("Closures.wurst");
    }

    @Test
    public void testEnum() throws IOException {
        test("Enum.wurst");
    }

    @Test
    public void testFunctionDefinitions() throws IOException {
        test("FunctionDefinitions.wurst");
    }

    @Test
    public void testTernary() throws IOException {
        test("Ternary.wurst");
    }

    @Test
    public void testBinaryExprAndMethod() throws IOException {
        test("BinaryExprAndMethod.wurst");
    }

    @Test
    public void testReal1() throws IOException {
        test("Real1.wurst");
    }

    @Test
    public void testReal2() throws IOException {
        test("Real2.wurst");
    }

    @Test
    public void testReal3() throws IOException {
        test("Real3.wurst");
    }

    @Test
    public void testComments() throws IOException {
        test("Comments.wurst");
    }
}
