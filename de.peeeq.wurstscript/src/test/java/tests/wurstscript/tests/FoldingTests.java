package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.requests.Colors;
import de.peeeq.wurstio.languageserver.requests.FoldingRangeRequest;
import de.peeeq.wurstscript.ast.CompilationUnit;
import org.eclipse.lsp4j.ColorInformation;
import org.eclipse.lsp4j.FoldingRange;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 *
 */
public class FoldingTests extends WurstScriptTest {

    @Test
    public void testRanges() {
        CompilationResult compilationResult = test().lines("package B\n" +
            "endpackage\n" +
            "\n" +
            "package C\n" +
            "endpackage\n" +
            "\n" +
            "package D\n" +
            "endpackage\n" +
            "\n" +
            "package A\n" +
            "import B\n" +
            "import C\n" +
            "import D\n" +
            "\n" +
            "interface I\n" +
            "\tfunction foo()\n" +
            "\n" +
            "enum E\n" +
            "\tEEE\n" +
            "\n" +
            "class F\n" +
            "\tint i\n" +
            "\n" +
            "\tconstruct()\n" +
            "\t\tlet i = 0\n" +
            "\t\tif i > 0\n" +
            "\t\t\tskip\n" +
            "\n" +
            "\tfunction foo()\n" +
            "\t\tfor i = 0 to 10\n" +
            "\t\t\tskip\n" +
            "\n" +
            "\tondestroy\n" +
            "\t\tskip\n" +
            "\n" +
            "init\n" +
            "\tskip\n");
        CompilationUnit compilationUnit = compilationResult.getModel().get(0);
        List<FoldingRange> foldingRanges = FoldingRangeRequest.calculateFoldingRanges(compilationUnit);
        Assert.assertEquals(foldingRanges.get(0).getStartLine(), 10);
        Assert.assertEquals(foldingRanges.get(0).getEndLine(), 12);

        Assert.assertEquals(foldingRanges.get(1).getStartLine(), 14);
        Assert.assertEquals(foldingRanges.get(1).getEndLine(), 15);

        Assert.assertEquals(foldingRanges.get(2).getStartLine(), 17);
        Assert.assertEquals(foldingRanges.get(2).getEndLine(), 18);

        Assert.assertEquals(foldingRanges.get(3).getStartLine(), 20);
        Assert.assertEquals(foldingRanges.get(3).getEndLine(), 33);

        Assert.assertEquals(foldingRanges.get(4).getStartLine(), 28);
        Assert.assertEquals(foldingRanges.get(4).getEndLine(), 30);

        Assert.assertEquals(foldingRanges.get(5).getStartLine(), 29);
        Assert.assertEquals(foldingRanges.get(5).getEndLine(), 30);

        Assert.assertEquals(foldingRanges.get(6).getStartLine(), 23);
        Assert.assertEquals(foldingRanges.get(6).getEndLine(), 26);

        Assert.assertEquals(foldingRanges.get(7).getStartLine(), 25);
        Assert.assertEquals(foldingRanges.get(7).getEndLine(), 26);

        Assert.assertEquals(foldingRanges.get(8).getStartLine(), 32);
        Assert.assertEquals(foldingRanges.get(8).getEndLine(), 33);

        Assert.assertEquals(foldingRanges.get(9).getStartLine(), 35);
        Assert.assertEquals(foldingRanges.get(9).getEndLine(), 38);

    }

}
