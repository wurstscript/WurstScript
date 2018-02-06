package tests.prettyprint;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.attributes.prettyPrint.MaxOneSpacer;
import de.peeeq.wurstscript.attributes.prettyPrint.PrettyPrinter;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import org.testng.annotations.Test;
import tests.wurstscript.tests.WurstScriptTest;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import static org.testng.AssertJUnit.assertEquals;

public class PrettyPrintTest extends WurstScriptTest {

    private String setUp(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        File inFile = new File(filename);
        String content = Files.toString(inFile, Charsets.UTF_8);

        WurstGui gui = new WurstGuiCliImpl();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui, null, new RunArgs());

        CompilationUnit cu = compiler.parse("test", new StringReader(content));
        PrettyPrinter.prettyPrint(cu, new MaxOneSpacer(), sb, 0);

        return sb.toString();
    }

    private String expectedFile(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        File inFile = new File(filename);
        return Files.toString(inFile, Charsets.UTF_8);
    }

    private void visitEvery(String rootPath) throws IOException {
        StringBuilder sb = new StringBuilder();
        File root = new File(rootPath);
        File[] list = root.listFiles();
        if (list == null) {
            return;
        }

        for (File f : list) {
            if (f.isDirectory()) {
                visitEvery(f.getAbsolutePath());
            }
            else {
                String path = f.getAbsolutePath();

                if (path.contains("later")) {
                    continue;
                }
                // Prettify our wurst files.
                if (extension(path).equals("wurst")) {
                    String pretty = setUp(path);
                    System.out.println(pretty);

                    System.out.println("Valid: " + path);
                    testAssertOk("prettyTest", false, pretty);
                }
            }
        }
    }

    // Note: only works with single extension files, i.e it doesn't work with:
    // asdf.tar.gz
    private static String extension(String filename) {
        String extension = "";

        int i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i+1);
        }
        return extension;
    }

    @Test
    public void testPrettyIf() throws IOException {
        String testFilename = "testscripts/pretty/%s/If.wurst";
        String pretty = setUp(String.format(testFilename, "in"));
        String expected = expectedFile(String.format(testFilename, "out"));

        assertEquals(expected, pretty);
    }

    @Test
    public void testPrettyLoops() throws IOException {
        String testFilename = "testscripts/pretty/%s/Loops.wurst";
        String pretty = setUp(String.format(testFilename, "in"));
        String expected = expectedFile(String.format(testFilename, "out"));

        assertEquals(expected, pretty);
    }

    @Test
    public void testPrettyAssignments() throws IOException {
        String testFilename = "testscripts/pretty/%s/Assignment_shorthand.wurst";
        String pretty = setUp(String.format(testFilename, "in"));
        String expected = expectedFile(String.format(testFilename, "out"));

        assertEquals(expected, pretty);
    }

    @Test
    public void testPrettyMany() throws IOException {
        visitEvery("testscripts/pretty");
    }
}
