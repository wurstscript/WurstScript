package tests.wurstscript.tests;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.StringReader;
import java.util.Collections;

import static org.testng.Assert.assertEquals;

public class ParserTests extends WurstScriptTest {


    @Test
    public void parenthesis1() {
        testAssertErrorsLines(false, "no",
                "package test",
                "	init",
                "		print(\"hello\" ",
                "endpackage"
        );
    }

    @Test
    public void parenthesis2() {
        testAssertErrorsLines(false, "no",
                "package test",
                "	init",
                "		print(\"hello\")) ",
                "endpackage"
        );
    }

    @Test
    public void err_in_closure() {
        testAssertErrorsLines(false, "no",
                "package test",
                "	init",
                "		doAfter(0.1, ()->begin",
                "			print(\"hello\" + if)",
//				"			\"world\")",
                "		end)",
                "endpackage"
        );
    }

    @Test
    public void halfAssign() {
        testAssertErrorsLines(false, "Missing expression",
                "package Test",
                "init",
                "	int x =",
                "	int y =",
                "	x = 1",
                "	y = 2",
                "	foo(x, y)",
                "function foo(int x, int y)");
    }


    @Test
    public void dotTo() {
        testAssertErrorsLines(false, "extraneous input 'to'",
                "package Test",
                "init",
                "	let x = 1",
                "	string s = 1.to");
    }

    @Test
    public void indentWithSpaces() {
        testAssertOkLines(false,
                "package Test",
                "init",
                "    var x = 1",
                "    if x > 10",
                "		x -= 1",
                "	 x += 1");
    }

    @Test
    public void indentWithSpaces2() {
        testAssertOkLines(false,
                "package Test",
                "init",
                "    var x = 1",
                "    int y = 2",
                "    if x > 10",
                "        x -= 1",
                "        y += 1",
                "	 x += 1");
    }

    @Test
    public void charAndFourChar() {
        testAssertOkLines(false,
                "package Test",
                "function bar(int a0,int a1)",
                "",
                "function foo()",
                "    bar((']'),'hfoo')",
                "");
    }

    @Test
    public void twoSpaces() {
        testAssertOkLines(false,
                "package test",
                "init",
                "  int x = 1",
                "  if x > 5",
                "    x = x + 1",
                "    if x > 10",
                "      x = x * 2"
        );
    }

    @Test
    public void twoSpacesMixed() {
        CompilationResult res = test().executeProg(false).lines(
                "package test",
                "function foo()",
                "  int x = 1",
                "  if x > 5",
                "    x = x + 1",
                "    if x > 10",
                "      x = x * 2",
                "function bar()",
                "    int x = 1",
                "    if x > 5",
                "        x = x + 1",
                "        if x > 10",
                "            x = x * 2");
        Assert.assertTrue(
                res.getGui()
                        .getWarningList()
                        .stream()
                        .anyMatch(w -> w.getMessage().contains("Inconsistent indentation: Earlier in this file 2 spaces were used for indentation and here it is 4 spaces."))
        );
    }

    @Test
    public void inconsistentIndentationWithin() {
        CompilationResult res = test().executeProg(false).lines(
            "package test",
            "interface F",
            "    function f()",
            "int x = 0",
            "function blub(F f)",
            "    f.f()",
            "function bar()",
            "    blub(() -> begin",
            "            x = x * 2",
            "        end)");
        Assert.assertEquals(
            Collections.emptyList(),
            res.getGui().getWarningList()
        );
    }


    @Test
    public void alignWithSpacesAllowed() {
        CompilationResult res = test().executeProg(false).lines(
                "package test",
                "function int.foo() returns int",
                "    return this + 1",
                "function bar(int xyz) returns int",
                "    return xyz..foo()",
                "              ..foo()",
                "               .foo()");
        Assert.assertEquals(res.getGui().getWarningList().toString(), "[]");
    }

    @Test
    public void alignWithTabsAllowed() {
        CompilationResult res = test().executeProg(false).lines(
                "package test",
                "function int.foo() returns int",
                "	return this + 1",
                "function bar(int xyz) returns int",
                "	return xyz..foo()",
                "			  ..foo()",
                "			   .foo()");
        Assert.assertEquals(res.getGui().getWarningList().toString(), "[]");
    }


    @Test
    public void positionsNormalLineBreaks() {
        CompilationUnit cu = parse(
                "package Test\n" +
                        "init\n" +
                        "    var x = 1\n" +
                        "    int y = 2\n" +
                        "    if x > 10\n" +
                        "        x -= 1\n" +
                        "        y += 1\n" +
                        "	 x += 1\n");

        WPackage p = cu.getPackages().get(0);

        InitBlock initBlock = (InitBlock) p.getElements().get(0);
        assertEquals(2, initBlock.getSource().getLine());
        assertEquals(1, initBlock.getSource().getStartColumn());

        WStatements stmts = initBlock.getBody();
        StmtIf stmtIf = (StmtIf) stmts.get(3);
        assertEquals(5, stmtIf.getSource().getLine());
        assertEquals(5, stmtIf.getSource().getStartColumn());

        Expr cond = stmtIf.getCond();
        assertEquals(5, cond.getSource().getLine());
        assertEquals(8, cond.getSource().getStartColumn());
        assertEquals(5, cond.getSource().getEndLine());
        assertEquals(14, cond.getSource().getEndColumn());


    }

    @Test
    public void positionsWindowsTypewriterLinebreaks() {
        CompilationUnit cu = parse(
                "package Test\r\n" +
                        "init\r\n" +
                        "    var x = 1\r\n" +
                        "    int y = 2\r\n" +
                        "    if x > 10\r\n" +
                        "        x -= 1\r\n" +
                        "        y += 1\r\n" +
                        "	 x += 1\r\n");

        WPackage p = cu.getPackages().get(0);

        InitBlock initBlock = (InitBlock) p.getElements().get(0);
        assertEquals(2, initBlock.getSource().getLine());
        assertEquals(1, initBlock.getSource().getStartColumn());

        WStatements stmts = initBlock.getBody();
        StmtIf stmtIf = (StmtIf) stmts.get(3);
        assertEquals(5, stmtIf.getSource().getLine());
        assertEquals(5, stmtIf.getSource().getStartColumn());

        Expr cond = stmtIf.getCond();
        assertEquals(5, cond.getSource().getLine());
        assertEquals(8, cond.getSource().getStartColumn());
        assertEquals(5, cond.getSource().getEndLine());
        assertEquals(14, cond.getSource().getEndColumn());


    }

    private CompilationUnit parse(String input) {
        WurstGui gui = new WurstGuiCliImpl();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, new RunArgs());
        compiler.getErrorHandler().enableUnitTestMode();
        return compiler.parse("test", new StringReader(input));
    }

}
