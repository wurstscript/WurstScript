package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.peeeq.wurstio.UtilsIO;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.intermediatelang.optimizer.FunctionSplitter;
import de.peeeq.wurstscript.intermediatelang.optimizer.LocalMerger;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Utils;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.*;

public class OptimizerTests extends WurstScriptTest {


    @Test
    public void test_number_shortening() {
        test().lines(
                "package test",
                "	function foo() returns int",
                "		return 800000",
                "endpackage");
    }

    @Test
    public void test_number_shortening2() {
        test().lines(
                "package test",
                "	function foo() returns real",
                "		if 1.0 > 0.1",
                "			return 0.0",
                "		else",
                "			return 1.10",
                "endpackage");
    }


    @Test
    public void test_double_renaming_bug() {
        test().lines(
                "package test",
                "	int testVar = 0",
                "	function w() returns int",
                "		return 1",
                "	function s(int j) returns int",
                "		return testVar",
                "	init",
                "		w()",
                "		s(2)",
                "		let c = function w",
                "endpackage");
    }

    @Test
    public void test_remove_useless() {
        test().lines(
                "package test",
                "	int testVar1 = 1",
                "	real testVar2 = 1.1",
                "	string testVar3 = \"blub\"",
                "	boolean testVar4 = true",
                "	init",
                "		int i = testVar1",
                "endpackage");
    }

    @Test
    public void test_inline_globals() {
        test().lines(
                "package test",
                "	int testVar1 = 1",
                "	real testVar2 = 1.1",
                "	string testVar3 = \"blub\"",
                "	boolean testVar4 = true",
                "	init",
                "		int i = testVar1",
                "		real r = testVar2",
                "		string s = testVar3",
                "		boolean b = testVar4",
                "endpackage");
    }


    @Test
    public void test_nullsetter1() {
        test().executeProg().lines(
                "type player extends handle",
                "package test",
                "	@extern native Player(integer id) returns player",
                "	@extern native GetPlayerId(player whichPlayer) returns integer",
                "	native testSuccess()",
                "	function foo()",
                "		player p = Player(0)",
                "	init",
                "		foo()",
                "		testSuccess()",
                "endpackage");
    }

    @Test
    public void test_nullsetter2() {
        test().executeProg().lines(
                "type player extends handle",
                "package test",
                "	@extern native Player(integer id) returns player",
                "	@extern native GetPlayerId(player whichPlayer) returns integer",
                "	native testSuccess()",
                "	function foo() returns player",
                "		player p = Player(0)",
                "		return p",
                "	init",
                "		foo()",
                "		testSuccess()",
                "endpackage");
    }

    @Test
    public void test_nullsetter3() {
        test().executeProg().lines(
                "type player extends handle",
                "package test",
                "	@extern native Player(integer id) returns player",
                "	@extern native GetPlayerId(player whichPlayer) returns integer",
                "	native testSuccess()",
                "	function foo() returns int",
                "		player p = Player(0)",
                "		return GetPlayerId(p)",
                "	init",
                "		foo()",
                "		testSuccess()",
                "endpackage");
    }

    @Test
    public void test_nullsetter4() {
        test().executeProg().lines(
                "type player extends handle",
                "package test",
                "	@extern native Player(integer id) returns player",
                "	@extern native GetPlayerId(player whichPlayer) returns integer",
                "	native testSuccess()",
                "	function foo() returns int",
                "		player p = Player(0)",
                "		return 0",
                "	init",
                "		foo()",
                "		testSuccess()",
                "endpackage");
    }

    //	(04:49:22 PM) Frotty: öh
//	(04:49:24 PM) Frotty: einfach
//	(04:49:28 PM) Frotty: 1 var erstellen
//	(04:49:31 PM) Frotty: constant int = 5
//	(04:49:34 PM) Frotty: nicht benutzen
//	(04:49:36 PM) Frotty: wird nicht entfernt
    @Test
    public void test_varRemoval() {
        test().lines(
                "package test",
                "	constant i = 5",
                "endpackage");
    }


    private String makeCode(String... body) {
        return Utils.join(body, "\n");
    }

    public void assertOk(boolean executeProg, String... body) {
        test().executeProg().lines(body);
    }

    public void assertError(boolean executeProg, String expected, String... body) {
        String prog = makeCode(body);
        testAssertErrors(UtilsIO.getMethodName(1), executeProg, prog, expected);
    }

    @Test
    public void test_ifTrue() {
        test().executeProg().lines(
                "package test",
                "	native testSuccess()",
                "	native testFail(string s)",
                "	constant b = true",
                "	init",
                "		if b",
                "			testSuccess()",
                "		else",
                "			testFail(\"\")",
                "endpackage");
    }

    @Test
    public void test_ifFalse() {
        test().executeProg().lines(
                "package test",
                "	native testSuccess()",
                "	native testFail(string s)",
                "	constant b = false",
                "	init",
                "		if b",
                "			testFail(\"\")",
                "		else",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void test_ifDoubleOr1() {
        test().executeProg().lines(
                "package test",
                "	native testSuccess()",
                "	native testFail(string s)",
                "	constant b = false",
                "	init",
                "		if b or true",
                "			testSuccess()",
                "		else",
                "			testFail(\"\")",
                "endpackage");
    }

    @Test
    public void test_ifDoubleOr2() {
        test().executeProg().lines(
                "package test",
                "	native testSuccess()",
                "	native testFail(string s)",
                "	constant b = false",
                "	init",
                "		if b or false",
                "			testFail(\"\")",
                "		else",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void test_ifDoubleAnd1() {
        test().executeProg().lines(
                "package test",
                "	native testSuccess()",
                "	native testFail(string s)",
                "	constant b = true",
                "	init",
                "		if b and true",
                "			testSuccess()",
                "		else",
                "			testFail(\"\")",
                "endpackage");
    }

    @Test
    public void test_ifDoubleAnd2() {
        test().executeProg().lines(
                "package test",
                "	native testSuccess()",
                "	native testFail(string s)",
                "	constant b = true",
                "	init",
                "		if b and false",
                "			testFail(\"\")",
                "		else",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void test_ifMulti() {
        test().executeProg().lines(
                "package test",
                "	native testSuccess()",
                "	native testFail(string s)",
                "	constant b = true",
                "	constant c = true",
                "	init",
                "		if b and true and c and true and false",
                "			testFail(\"\")",
                "		else",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void test_ifInt1() {
        test().executeProg().lines(
                "package test",
                "	native testSuccess()",
                "	native testFail(string s)",
                "	init",
                "		if 3 > 4",
                "			testFail(\"\")",
                "		else",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void test_ifInt2() {
        test().executeProg().lines(
                "package test",
                "	native testSuccess()",
                "	native testFail(string s)",
                "	init",
                "		if 3 < 4 - 2",
                "			testFail(\"\")",
                "		else",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void test_ifInt3() {
        test().executeProg().lines(
                "package test",
                "	native testSuccess()",
                "	native testFail(string s)",
                "	init",
                "		if 8 >= 8 and 50 != 40",
                "			testSuccess()",
                "		else",
                "			testFail(\"\")",
                "endpackage");
    }


    @Test
    public void test_ifInt4() {
        test().executeProg().lines(
                "package test",
                "	native testSuccess()",
                "	native testFail(string s)",
                "	init",
                "		if 8 >= 8 and 50 != 50",
                "		else",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void test_ifEmpty() {
        test().executeProg().lines(
                "package test",
                "	native testSuccess()",
                "	native testFail(string s)",
                "	int x = 0",
                "	function foo() returns boolean",
                "		if x == 0",
                "			x = 1",
                "			return true",
                "		return false",
                "	init",
                "		if foo()",
                "		if x == 1",
                "			testSuccess()",
                "endpackage");
    }


    @Test
    public void test_exitwhen() {
        test().lines(
                "package test",
                "	native testSuccess()",
                "	native testFail(string s)",
                "	init",
                "		while true",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void test_ConstFolding() {
        test().lines(
                "package test",
                "	init",
                "		int i = 3 + 7 * 2 * 33",
                "endpackage");
    }

    @Test
    public void test_ConstFoldingCombined() {
        test().executeProg().lines(
                "package test",
                "	native testSuccess()",
                "	native testFail(string s)",
                "	init",
                "		int i = 3 + 7 * 2 * 33",
                "		if i == 465",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void test_tempVarRemover() throws IOException {
        test().lines(
                "package test",
                "	@extern native I2S(int i) returns string",
                "	native println(string s)",
                "	@extern native GetRandomInt(int a, int b) returns int",
                "	init",
                "		let blub_a = GetRandomInt(0,100)",
                "		let blub_b = blub_a",
                "		let blub_c = blub_b + blub_b + blub_b",
                "		println(I2S(blub_c))",
                "endpackage");
        String output = Files.toString(new File("./test-output/OptimizerTests_test_tempVarRemover_inlopt.j"), Charsets.UTF_8);

        assertTrue(!output.contains("blub_a") ? (output.contains("blub_b") || output.contains("blub_c")) : (!output.contains("blub_b") && !output.contains
                ("blub_c")));
    }

    @Test
    public void test_mult2rewrite() throws IOException {
        test().lines(
                "package test",
                "	@extern native I2S(int i) returns string",
                "	native println(string s)",
                "	@extern native GetRandomInt(int a, int b) returns int",
                "	init",
                "		let blub_a = GetRandomInt(0,100)",
                "		let blub_b = blub_a",
                "		let blub_c = blub_b + blub_b",
                "		println(I2S(blub_c))",
                "endpackage");
        String output = Files.toString(new File("./test-output/OptimizerTests_test_mult2rewrite_inlopt.j"), Charsets.UTF_8);

        assertTrue(!output.contains("blub_a") && !(output.contains("blub_b") && !output.contains("blub_c")));
    }

    @Test
    public void test_mult3rewrite() throws IOException {
        test().lines(
                "package test",
                "	@extern native I2S(int i) returns string",
                "	native println(string s)",
                "	int ghs = 0",
                "	function foo() returns int",
                "		ghs += 2",
                "		return 4 + ghs",
                "	init",
                "		let blub_c = foo() + foo()",
                "		println(I2S(blub_c))",
                "endpackage");
        String output1 = Files.toString(new File("./test-output/OptimizerTests_test_mult3rewrite_inlopt.j"), Charsets.UTF_8);
        String output2 = Files.toString(new File("./test-output/OptimizerTests_test_mult3rewrite_opt.j"), Charsets.UTF_8);
        assertTrue(!output1.contains("foo()"));
        assertTrue(output2.contains("foo() + foo()"));
    }

    @Test
    public void test_tempVarRemover2() throws IOException {
        test().lines(
                "package test",
                "	@extern native I2S(int i) returns string",
                "	native println(string s)",
                "	@extern native GetRandomInt(int a, int b) returns int",
                "	init",
                "		let blablub = GetRandomInt(0,100)",
                "		println(I2S(blablub))",
                "endpackage");
        String output = Files.toString(new File("./test-output/OptimizerTests_test_tempVarRemover2_inlopt.j"), Charsets.UTF_8);
        assertFalse(output.contains("blablub"));
    }

    @Test
    public void test_localVarMerger() {
        test().executeProg().lines(
                "package test",
                "	native testSuccess()",
                "	native testFail(string s)",
                "	init",
                "		int a = 0",
                "		int b = 0",
                "		int c = 0",
                "		int d = 0",
                "		int e = 0",
                "		while c<1000",
                "			d = a+2",
                "			b = d-1",
                "			if b < a",
                "				c = c+b",
                "			else",
                "				c = c-b",
                "			e = b*4",
                "			d = e + 1",
                "			e = d - 1",
                "			a = e div 2",
                "			if a >= 20",
                "				break",
                "		if c == -26",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void test_localVarMerger2() {
        test().executeProg().lines(
                "package test",
                "	native testSuccess()",
                "	native testFail(string s)",
                "	@extern native Sin(real r) returns real",
                "	init",
                "		var i = 5",
                "		var x = Sin(5)",
                "		if x < 20",
                "			x = x + 1",
                "		if i == 5",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    @Ignore // test for #747
    public void test_localVarMerger3() throws IOException {
        test().lines(
                "package test",
                "native testSuccess()",
                "native testFail(string s)",
                "native sideEffects()",
                "@extern native Sin(real r) returns real",
                "int g = 0",
                "int h = 0",
                "function f(int x)",
                "	sideEffects()",
                "function foo(int x)",
                "	int a = g",
                "	if h == 10",
                "		f(a)",
                "function initVars()",
                "	g = 7",
                "	h = 10",
                "init",
                "	initVars()",
                "	foo(3)",
                "	testSuccess()"
        );
        String compiledAndOptimized = Files.toString(new File("test-output/OptimizerTests_test_localVarMerger3_opt.j"), Charsets.UTF_8);
        assertTrue(compiledAndOptimized.contains("call f(test_g)"));
    }

    @Test
    public void test_unused_func_remover() throws IOException {
        test().executeProg().lines(
                "package test",
                "	@extern native I2S(int i) returns string",
                "	native testSuccess()",
                "	init",
                "		I2S(5)",
                "		testSuccess()",
                "endpackage");
        String compiledAndOptimized = Files.toString(new File("test-output/OptimizerTests_test_unused_func_remover_opt.j"), Charsets.UTF_8);
        assertFalse("I2S should be removed", compiledAndOptimized.contains("I2S"));
    }

    @Test
    public void test_unused_func_remover2() throws IOException {
        test().lines(
                "package test",
                "	@extern native I2S(int i) returns string",
                "	init",
                "		I2S(1 div 0)",
                "endpackage");
        String compiledAndOptimized = Files.toString(new File("test-output/OptimizerTests_test_unused_func_remover2_opt.j"), Charsets.UTF_8);
        assertTrue("I2S should not be removed", compiledAndOptimized.contains("I2S"));
    }

    @Test
    public void test_unreachableCodeRemover() throws IOException {
        test().withStdLib().lines(
                "package test",
                "	import MagicFunctions",
                "	function foo()",
                "		if not false",
                "			return",
                "		testSuccess()",
                "	init",
                "		foo()",
                "endpackage");
        String compiledAndOptimized = Files.toString(new File("test-output/OptimizerTests_test_unreachableCodeRemover_opt.j"), Charsets.UTF_8);
        assertFalse("testSuccess should be removed", compiledAndOptimized.contains("testSuccess"));
    }

    @Test
    public void controlFlowMergeNoSideEffect() throws IOException {
        test().lines(
                "package Test",
                "native testSuccess()",
                "native testFail(string msg)",
                "var ghs = 12",
                "function nonInlinable(int x) returns bool",
                "	if x > 6",
                "		return true",
                "	else",
                "		return false",
                "init",
                "	var x = 6",
                "	if nonInlinable(x)",
                "		ghs = 0",
                "		testFail(\"bad\")",
                "	else",
                "		ghs = 0",
                "		if ghs == 0",
                "			testSuccess()"
        );
        String compiledAndOptimized = Files.toString(new File("test-output/OptimizerTests_controlFlowMergeNoSideEffect_opt.j"), Charsets.UTF_8);
        assertEquals(compiledAndOptimized.indexOf("Test_ghs = 0"), compiledAndOptimized.lastIndexOf("Test_ghs = 0"));
    }

    @Test
    public void test_controlFlowMergeSideEffect() throws IOException {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "native testFail(string msg)",
                "var ghs = 12",
                "function nonInlinable(int x) returns bool",
                "	ghs += 6",
                "	if x > 6",
                "		return true",
                "	else",
                "		return false",
                "init",
                "	var x = 6",
                "	if nonInlinable(x)",
                "		ghs = 0",
                "		testFail(\"bad\")",
                "	else",
                "		ghs = 0",
                "		if ghs == 0",
                "			testSuccess()"
        );
    }

    @Test
    public void controlFlowMergeSideEffect() throws IOException {
        test().lines(
                "package Test",
                "native testSuccess()",
                "native testFail(string msg)",
                "var ghs = 12",
                "function nonInlinable(int x) returns bool",
                "	ghs += 6",
                "	if x > 6",
                "		return true",
                "	else",
                "		return false",
                "init",
                "	var x = 6",
                "	if nonInlinable(x)",
                "		ghs = 0",
                "		testFail(\"bad\")",
                "	else",
                "		ghs = 0",
                "		if ghs == 0",
                "			testSuccess()"
        );
        String compiledAndOptimized = Files.toString(new File("test-output/OptimizerTests_controlFlowMergeSideEffect_opt.j"), Charsets.UTF_8);
        assertNotSame(compiledAndOptimized.indexOf("Test_ghs = 0"), compiledAndOptimized.lastIndexOf("Test_ghs = 0"));
    }

    @Test
    public void controlFlowMergeSideEffect2() throws IOException {
        test().withStdLib().lines(
                "package Test",
                "var ghs = 12",
                "function someSideEffectFunc(int x) returns bool",
                "	if x < 3",
                "		BJDebugMsg(\"test\")",
                "	if x > 6",
                "		return true",
                "	else",
                "		return false",
                "init",
                "	var x = 6",
                "	if someSideEffectFunc(x)",
                "		ghs = 0",
                "		testFail(\"bad\")",
                "	else",
                "		ghs = 0",
                "		if ghs == 0",
                "			testSuccess()"
        );
        String compiledAndOptimized = Files.toString(new File("test-output/OptimizerTests_controlFlowMergeSideEffect2_opt.j"), Charsets.UTF_8);
        assertNotSame(compiledAndOptimized.indexOf("Test_ghs = 0"), compiledAndOptimized.lastIndexOf("Test_ghs = 0"));
    }


    @Test
    public void optimizeSet() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "var ghs = 12",
                "init",
                "	var x = 6 + 3",
                "	ghs += 2",
                "	ghs -= 2",
                "	if ghs == 12 and x == 9",
                "		testSuccess()"
        );
    }

    @Test
    public void optimizeSet2() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "var x = 100",
                "init",
                "	var Test_x = x - 100",
                "	Test_x += 1",
                "	x += 1",
                "	if x == 101 and Test_x == 1",
                "		testSuccess()"
        );
    }

    @Test
    public void optimizeExitwhen() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "var x = 100",
                "init",
                "	while x > 0",
                "		if x == 50",
                "			break",
                "		if x == 101",
                "			break",
                "		x--",
                "	testSuccess()"
        );
    }

    @Test
    public void number() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "function foo(int x) returns bool",
                "	return (((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((x == 1) or (x == 852056)) or (x == 852064)) or (x == 852065)) or (x == 852067)) or (x == 852068)) or (x == 852076)) or (x == 852077)) or (x == 852090)) or (x == 852091)) or (x == 852100)) or (x == 852102)) or (x == 852103)) or (x == 852107)) or (x == 852108)) or (x == 852129)) or (x == 852130)) or (x == 852133)) or (x == 852134)) or (x == 852136)) or (x == 852137)) or (x == 852150)) or (x == 852151)) or (x == 852174)) or (x == 852158)) or (x == 852159)) or (x == 852162)) or (x == 852163)) or (x == 852174)) or (x == 852175)) or (x == 852177)) or (x == 852178)) or (x == 852191)) or (x == 852192)) or (x == 852198)) or (x == 852199)) or (x == 852203)) or (x == 852204)) or (x == 852212)) or (x == 852213)) or (x == 852244)) or (x == 852245)) or (x == 852249)) or (x == 852250)) or (x == 852255)) or (x == 852256)) or (x == 852458)) or (x == 852459)) or (x == 852478)) or (x == 852479)) or (x == 852484)) or (x == 852485)) or (x == 852515)) or (x == 852516)) or (x == 852522)) or (x == 852523)) or (x == 852540)) or (x == 852541)) or (x == 852543)) or (x == 852544)) or (x == 852546)) or (x == 852547)) or (x == 852549)) or (x == 852550)) or (x == 852552)) or (x == 852553)) or (x == 852562)) or (x == 852563)) or (x == 852571)) or (x == 852578)) or (x == 852579)) or (x == 852589)) or (x == 852590)) or (x == 852602)) or (x == 852603)) or (x == 852671)) or (x == 852672))",
                "init",
                "	if foo(852478)",
                "		testSuccess()"
        );
    }

    @Test
    public void optimizeDuplicateNullSets() throws IOException {
        testAssertOkLinesWithStdLib(true,
                "package Test",
                "var x = 100",
                "init",
                "	unit u = createUnit(Player(0), 'hfoo', vec2(0,0), angle(0))",
                "	print(u.getTypeId())",
                "	print(u.getTypeId() + 1)",
                "	print(u.getTypeId() + 2)",
                "	testSuccess()",
                "	u = null",
                "	u = null"
        );
        String compiledAndOptimized = Files.toString(new File("test-output/OptimizerTests_optimizeDuplicateNullSets_opt.j"), Charsets.UTF_8);
        assertEquals(compiledAndOptimized.indexOf("u = null"), compiledAndOptimized.lastIndexOf("u = null"));
    }

    @Test
    public void testInlineAnnotation() throws IOException {
        testAssertOkLinesWithStdLib(false,
                "package Test",
                "@inline function over9000(int i, boolean b, real r)",
                "	var s = \"\"",
                "	s += r.toString()",
                "	s += i.toString()",
                "	s += b.toString()",
                "	if s.length() > 5",
                "		print(s)",
                "	print(\"end\")",
                "function over9001(int i, boolean b, real r)",
                "	var s = \"\"",
                "	s += r.toString()",
                "	s += i.toString()",
                "	s += b.toString()",
                "	if s.length() > 5",
                "		print(s)",
                "	print(\"end\")",
                "function foo()",
                "	over9000(141, true and true, 12315.233)",
                "	over9001(141, true and true, 12315.233)",
                "function bar()",
                "	print(\"end\")",
                "@noinline function noot()",
                "	print(\"end\")",
                "init",
                "	over9000(12412411, true and true, 12315.233)",
                "	over9001(12412411, true and true, 12315.233)",
                "	foo()",
                "	bar()",
                "	noot()"

        );
        String inlined = Files.toString(new File("test-output/OptimizerTests_testInlineAnnotation_inl.j"), Charsets.UTF_8);
        assertFalse(inlined.contains("function bar"));
        assertFalse(inlined.contains("function over9000"));
        assertTrue(inlined.contains("function over9001"));
        assertTrue(inlined.contains("function noot"));
    }


    @Test
    public void moveTowardsBug() { // see #737
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "@extern native SquareRoot(real x) returns real",
                "@extern native R2S(real x) returns string",
                "native println(string s)",
                "tuple vec3(real x, real y, real z)",
                "public function vec3.length() returns real",
                "    return SquareRoot(this.x * this.x + this.y * this.y + this.z * this.z)",
                "public function vec3.op_plus(vec3 v)	returns vec3",
                "    return vec3(this.x + v.x, this.y + v.y, this.z + v.z)",
                "public function vec3.op_minus(vec3 v)	returns vec3",
                "    return vec3(this.x - v.x, this.y - v.y, this.z - v.z)",
                "public function vec3.op_mult(real factor) returns vec3",
                "    return vec3(this.x * factor, this.y * factor, this.z * factor)",
                "public function real.op_mult(vec3 v) returns vec3",
                "    return vec3(v.x * this, v.y * this, v.z * this)",
                "public function vec3.normalizedPointerTo(vec3 target) returns vec3",
                "    vec3 diff = target - this",
                "    real len = diff.length()",
                "    if len > 0",
                "        diff = diff * (1. / len)",
                "    else",
                "        diff = vec3(1, 0, 0)",
                "    return diff",
                "function vec3.moveTowards(vec3 target, real dist) returns vec3",
                "    return this + dist*this.normalizedPointerTo(target)",
                "function vec3.approxEq(vec3 o) returns bool",
                "    return this.x - 0.01 < o.x and o.x < this.x + 0.01",
                "       and this.y - 0.01 < o.y and o.y < this.y + 0.01",
                "       and this.z - 0.01 < o.z and o.z < this.z + 0.01",
                "init",
                "    let a = vec3(0,0,0).moveTowards(vec3(1,2,3), 10)",
                "    let b = vec3(0,0,0).moveTowards(vec3(6,5,4), 10)",
                "    if a.approxEq(vec3(2.673, 5.345, 8.018)) and b.approxEq(vec3(6.838, 5.698, 4.558))",
                "        testSuccess()",
                "endpackage");
    }

    @Test
    public void cyclicFunctionRemover() throws IOException {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "function foo(int x) returns int",
                "	if x > 1000",
                "		return g(x)",
                "	if x > 100",
                "		return h(x)",
                "	if x > 10",
                "		return i(x)",
                "	return x",
                "function g(int x) returns int",
                "	return foo(x div 1000)",
                "function h(int x) returns int",
                "	return foo(x div 100)",
                "function i(int x) returns int",
                "	return foo(x div 10)",
                "init",
                "	if foo(7531) == 7",
                "		testSuccess()"
        );
        String compiled = Files.toString(new File("test-output/OptimizerTests_cyclicFunctionRemover.j"), Charsets.UTF_8);
        System.out.println(compiled);
        assertFalse(compiled.contains("cyc_cyc"));
    }

    @Test
    public void constantFolding() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "function getDamage(int level) returns real",
            "    switch level ",
            "        case 1",
            "            return 6. * 20 ",
            "        case 2",
            "            return 6. * 40",
            "        case 3 ",
            "            return 6. * 60",
            "    return 0",
            "init",
            "    if getDamage(2) > 239 and getDamage(2) < 241",
            "        testSuccess()"
        );
    }

    @Test
    public void inlinerIntRealsConstantFolding() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "function getDamage(int level) returns real",
            "    switch level ",
            "        case 1",
            "            return getDamageDuration(level) * 20 ",
            "        case 2",
            "            return getDamageDuration(level) * 40",
            "        case 3 ",
            "            return getDamageDuration(level) * 60",
            "    return 0",
            "",
            "function getDamageDuration(int _level) returns real",
            "    return 6.",
            "init",
            "    if getDamage(2) > 239 and getDamage(2) < 241",
            "        testSuccess()"
            );
    }

    @Test
    public void multiArrayNoInline() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "class AssistTimestamps",
            "    int array[12] vals",
            "let at = new AssistTimestamps",
            "function foo()",
            "    at.vals[3] = 72",
            "init",
            "    at.vals[4] = 42",
            "    foo()",
            "    if at.vals[4] == 42",
            "        testSuccess()"
            );
    }


    @Test
    public void multiArrayNoInline2() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "class AssistTimestamps",
            "    int array[12] vals",
            "let at = new AssistTimestamps",
            "init",
            "    at.vals[3] = 42",
            "    if at.vals[4] == 0",
            "        testSuccess()"
        );
    }


    @Test
    public void copyPropagation() throws IOException {
        testAssertOkLines(true,
            "package Test",
            "native testSuccess()",
            "@extern native S2I(string s) returns int",
            "init",
            "    let a = S2I(\"7\")",
            "    let b = a",
            "    let c = b",
            "    if c == 7",
            "        testSuccess()"
        );
        String compiled = Files.toString(new File("test-output/OptimizerTests_copyPropagation_opt.j"), Charsets.UTF_8);
        System.out.println(compiled);
        assertTrue(compiled.contains("if a == 7 then"));
    }

    @Test
    public void copyPropagation2() throws IOException {
        testAssertOkLines(true,
            "package Test",
            "native testSuccess()",
            "@extern native S2I(string s) returns int",
            "integer test_x=0",
            "integer array B_nextFree",
            "integer B_firstFree=0",
            "integer B_maxIndex=0",
            "integer array B_typeId",
            "integer array B_y",
            "function destroyA(int this0)",
            "    let this_1 = this0",
            "    integer this_2",
            "    integer obj",
            "    test_x = test_x + B_y[this_1]",
            "    this_2 = this_1",
            "    test_x = test_x * B_y[this_2]",
            "    obj = this0",
            "    if B_typeId[obj] == 0",
            "    else",
            "        B_nextFree[B_firstFree] = obj",
            "        B_firstFree = B_firstFree + 1",
            "        B_typeId[obj] = 0",
            "        if B_nextFree[B_firstFree - 1] == 42",
            "            testSuccess()",
            "init",
            "    B_typeId[42] = 1",
            "    destroyA(42)"
        );
        String compiled = Files.toString(new File("test-output/OptimizerTests_copyPropagation2_opt.j"), Charsets.UTF_8);
        System.out.println(compiled);
        // copy propagation obj -> this0
        assertTrue(compiled.contains("set Test_B_nextFree[Test_B_firstFree] = this0"));
    }


    @Test
    public void localMergerLiveness() throws IOException {
        LocalMerger localMerger = new LocalMerger();

        Element trace = Ast.NoExpr();
        ImVar a = JassIm.ImVar(trace, TypesHelper.imInt(), "a", false);
        ImVar b = JassIm.ImVar(trace, TypesHelper.imInt(), "b", false);
        ImVar c = JassIm.ImVar(trace, TypesHelper.imInt(), "c", false);
        ImVar d = JassIm.ImVar(trace, TypesHelper.imInt(), "d", false);
        ImVar e = JassIm.ImVar(trace, TypesHelper.imInt(), "e", false);
        ImVars locals = JassIm.ImVars(a,b,c,d,e);

        ImStmts body = JassIm.ImStmts(
            JassIm.ImSet(trace, JassIm.ImVarAccess(a), JassIm.ImIntVal(0)),
            JassIm.ImSet(trace, JassIm.ImVarAccess(b), JassIm.ImIntVal(0)),
            JassIm.ImSet(trace, JassIm.ImVarAccess(c), JassIm.ImIntVal(0)),
            JassIm.ImSet(trace, JassIm.ImVarAccess(d), JassIm.ImIntVal(0)),
            JassIm.ImSet(trace, JassIm.ImVarAccess(e), JassIm.ImIntVal(0))
        );
        ImFunction func = JassIm.ImFunction(trace, "blub", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(), locals, body, Collections.emptyList());
        Map<ImStmt, Set<ImVar>> liveness = localMerger.calculateLiveness(func);

        for (ImStmt node : body) {
            assertEquals(HashSet.empty(), liveness.get(node));
        }
    }

    @Test
    public void testFunctionSplitter() {
        WurstModel model = Ast.WurstModel();

        ImTranslator tr = new ImTranslator(model, false, new RunArgs());
        ImProg prog = tr.getImProg();

        ImFunction func = JassIm.ImFunction(model, "blub", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        prog.getFunctions().add(func);

        for (int i = 0; i < 10000; i++) {
            ImVar l = JassIm.ImVar(model, TypesHelper.imInt(), "l" + i, false);
            func.getLocals().add(l);
            ImVar g = JassIm.ImVar(model, TypesHelper.imInt(), "g" + i, false);
            prog.getGlobals().add(g);
            func.getBody().add(JassIm.ImSet(model, JassIm.ImVarAccess(l), JassIm.ImIntVal(i)));
            func.getBody().add(JassIm.ImSet(model, JassIm.ImVarAccess(g), JassIm.ImVarAccess(l)));
        }

        FunctionSplitter.splitFunc(tr, func);

        System.out.println(prog);
        // should at least add one additional function
        assertTrue(prog.getFunctions().size() >= 2);



    }
}
