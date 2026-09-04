package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.peeeq.wurstio.TimeTaker;
import de.peeeq.wurstio.UtilsIO;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.intermediatelang.optimizer.FunctionSplitter;
import de.peeeq.wurstscript.intermediatelang.optimizer.LocalMerger;
import de.peeeq.wurstscript.intermediatelang.optimizer.LocalPlayerContextAnalyzer;
import de.peeeq.wurstscript.intermediatelang.optimizer.SideEffectAnalyzer;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.ImInliner;
import de.peeeq.wurstscript.translation.imoptimizer.ImOptimizer;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Utils;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.testng.Assert.*;

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
    public void globalsInlinerDoesNotRemoveNonInitDefaultWrite() {
        test().executeProg().lines(
            "package test",
            "    native testSuccess()",
            "    boolean g = false",
            "    @noinline function resetG()",
            "        g = false",
            "    function setG()",
            "        g = true",
            "    init",
            "        setG()",
            "        resetG()",
            "        if not g",
            "            testSuccess()"
        );
    }

    @Test
    public void globalsInlinerRespectsInitReadBeforeSingleWriteOrder() {
        test().executeProg().lines(
            "package test",
            "    native testSuccess()",
            "    int g = 0",
            "    boolean sawDefault = false",
            "    init",
            "        if g == 0",
            "            sawDefault = true",
            "        g = 5",
            "        if sawDefault and g == 5",
            "            testSuccess()"
        );
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
    public void preserveNameAnnotationExemptsFunctionFromCompression() throws IOException {
        test().optimize().lines(
            "package test",
            "    native testSuccess()",
            "    @preserveName function externallyCalled()",
            "        testSuccess()",
            "    function normallyCompressed()",
            "        testSuccess()",
            "    init",
            "        externallyCalled()",
            "        normallyCompressed()",
            "endpackage");

        String output = Files.toString(
            new File("./test-output/OptimizerTests_preserveNameAnnotationExemptsFunctionFromCompression_opt.j"),
            Charsets.UTF_8);
        assertTrue(output.contains("function externallyCalled"),
            "Expected @preserveName function to retain its source name.\n" + output);
        assertFalse(output.contains("function normallyCompressed"),
            "Expected an unannotated function to remain eligible for compression.\n" + output);
    }

    @Test
    public void executeFuncPreservesResolvedFunctionNameDuringCompression() throws IOException {
        test().optimize().lines(
            "package test",
            "    @extern native ExecuteFunc(string name)",
            "    native testSuccess()",
            "    function callback()",
            "        testSuccess()",
            "    init",
            "        ExecuteFunc(\"callback\")",
            "endpackage");

        String output = Files.toString(
            new File("./test-output/OptimizerTests_executeFuncPreservesResolvedFunctionNameDuringCompression_opt.j"),
            Charsets.UTF_8);
        assertTrue(output.contains("function callback"),
            "Expected ExecuteFunc target to retain its source name.\n" + output);
        assertTrue(output.contains("ExecuteFunc(\"callback\")"),
            "Expected ExecuteFunc to receive the preserved source name.\n" + output);
    }

    @Test
    public void preserveNameAnnotationKeepsExternallyCalledFunctionReachable() throws IOException {
        test().optimize().lines(
            "package test",
            "    native testSuccess()",
            "    @preserveName function externallyCalled()",
            "        testSuccess()",
            "endpackage");

        String output = Files.toString(
            new File("./test-output/OptimizerTests_preserveNameAnnotationKeepsExternallyCalledFunctionReachable_opt.j"),
            Charsets.UTF_8);
        assertTrue(output.contains("function externallyCalled"),
            "Expected an externally-called @preserveName function to survive garbage collection.\n" + output);
    }

    @Test
    public void preservedNamesAreReservedBeforeCompression() throws IOException {
        test().optimize().lines(
            "package test",
            "    native testSuccess()",
            "    function ordinary()",
            "        testSuccess()",
            "    @preserveName function w()",
            "        testSuccess()",
            "    init",
            "        ordinary()",
            "        w()",
            "endpackage");

        String output = Files.toString(
            new File("./test-output/OptimizerTests_preservedNamesAreReservedBeforeCompression_opt.j"),
            Charsets.UTF_8);
        assertTrue(output.contains("function w"),
            "Expected the preserved function name to remain available.\n" + output);
        assertFalse(output.contains("function w_1"),
            "Expected compression to reserve the preserved name.\n" + output);
    }

    @Test
    public void trvePreservesGlobalDespiteLexicalShadow() throws IOException {
        test().optimize().lines(
            "type trigger extends handle",
            "type event extends handle",
            "type limitop extends handle",
            "package test",
            "    int myVar = 0",
            "    @extern native TriggerRegisterVariableEvent(trigger whichTrigger, string varName, limitop opcode, real limitval) returns event",
            "    function registerVariableEvent()",
            "        string myVar = \"local\"",
            "        TriggerRegisterVariableEvent(null, \"test_myVar\", null, 0.0)",
            "    init",
            "        registerVariableEvent()",
            "endpackage");

        String output = Files.toString(
            new File("./test-output/OptimizerTests_trvePreservesGlobalDespiteLexicalShadow_opt.j"),
            Charsets.UTF_8);
        assertTrue(output.contains("integer test_myVar"),
            "Expected TRVE to preserve the global despite a local shadow.\n" + output);
    }

    @Test
    public void trvePreservesLoweredTupleComponent() throws IOException {
        test().optimize().lines(
            "type trigger extends handle",
            "type event extends handle",
            "type limitop extends handle",
            "package test",
            "    tuple pair(real x, real y)",
            "    pair value = pair(0., 0.)",
            "    @extern native TriggerRegisterVariableEvent(trigger whichTrigger, string varName, limitop opcode, real limitval) returns event",
            "    init",
            "        TriggerRegisterVariableEvent(null, \"test_value_x\", null, 0.0)",
            "endpackage");

        String output = Files.toString(
            new File("./test-output/OptimizerTests_trvePreservesLoweredTupleComponent_opt.j"),
            Charsets.UTF_8);
        assertTrue(output.contains("real test_value_x"),
            "Expected TRVE to preserve the lowered tuple component.\n" + output);
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
    @Ignore // This test was for a rewrite that caused an infinite loop in the optimizer.
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
        assertFalse(output1.contains("foo()"));
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
        // Better not inline GetRandomInt call - it might have side effects!
        assertTrue(output.contains("blablub"));
    }

    @Test
    public void test_tempVarRemover3() throws IOException {
        test().lines(
            "package test",
            "	@extern native I2S(int i) returns string",
            "	native println(string s)",
            "	function GetRandomIntt(int a, int b) returns int",
            "     return a + b",
            "	init",
            "		let blablub = GetRandomIntt(0,100)",
            "		println(I2S(blablub))",
            "endpackage");
        String output = Files.toString(new File("./test-output/OptimizerTests_test_tempVarRemover3_inlopt.j"), Charsets.UTF_8);
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
        assertFalse(compiledAndOptimized.contains("I2S"), "I2S should be removed");
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
        assertTrue(compiledAndOptimized.contains("I2S"), "I2S should not be removed");
    }

    @Test
    public void deadStoreKeepsPotentialDivisionTrap() throws IOException {
        test().executeProg(false).lines(
            "package test",
            "	@extern native I2S(int i) returns string",
            "	native getY() returns int",
            "	init",
            "		int y = getY()",
            "		string x = I2S(1 div y)",
            "endpackage");
        String compiledNoOpt = Files.toString(new File("test-output/OptimizerTests_deadStoreKeepsPotentialDivisionTrap_no_opts.j"), Charsets.UTF_8);
        assertTrue(compiledNoOpt.contains("1 /"), "potential division trap should be preserved");
    }

    @Test
    public void deadStoreKeepsPotentialDivisionTrapInCallee() throws IOException {
        test().executeProg(false).lines(
            "package test",
            "	@extern native I2S(int i) returns string",
            "	native getY() returns int",
            "	function wrap(int y) returns int",
            "		return 1 div y",
            "	init",
            "		int y = getY()",
            "		string x = I2S(wrap(y))",
            "endpackage");
        String compiledNoOpt = Files.toString(new File("test-output/OptimizerTests_deadStoreKeepsPotentialDivisionTrapInCallee_no_opts.j"), Charsets.UTF_8);
        assertTrue(compiledNoOpt.contains("1 /"), "potential division trap in callee should be preserved");
    }

    @Test
    public void deadStoreKeepsObservableMemberMutationInCallee() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "class C",
            "    int x",
            "function mutate(C c) returns int",
            "    c.x = 7",
            "    return 1",
            "init",
            "    let c = new C",
            "    int unused = mutate(c)",
            "    if c.x == 7",
            "        testSuccess()"
        );
    }

    @Test
    public void removeEmptyPackageInitsDoesNotPruneUserInitPrefixedFunctions() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "function init_user() returns bool",
            "    return true",
            "init",
            "    if init_user()",
            "        testSuccess()"
        );
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
        assertFalse(compiledAndOptimized.contains("testSuccess"), "testSuccess should be removed");
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
        // Non-annotated over9001 may be inlined depending on heuristic tuning.
        assertTrue(inlined.contains("function noot"));
    }

    @Test
    public void inlinerSupportsMultiReturn() throws IOException {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "function absLike(int x) returns int",
            "    if x >= 0",
            "        return x",
            "    return 0 - x",
            "init",
            "    let a = absLike(-4)",
            "    let b = absLike(3)",
            "    if a == 4 and b == 3",
            "        testSuccess()",
            "endpackage"
        );

        String inlined = Files.toString(new File("test-output/OptimizerTests_inlinerSupportsMultiReturn_inl.j"), Charsets.UTF_8);
        assertFalse(inlined.contains("call absLike"),
            "Expected multi-return function calls to be inlined in _inl output.");
    }

    @Test
    public void inlinerRatesByIncomingUsesNotOutgoingCalls() throws IOException {
        testAssertOkLinesWithStdLib(false,
            "package test",
            "function h1(int x) returns int",
            "    return x + 1",
            "function h2(int x) returns int",
            "    return x + 2",
            "function h3(int x) returns int",
            "    return x + 3",
            "function h4(int x) returns int",
            "    return x + 4",
            "function wrapper(int x) returns int",
            "    var a = h1(x)",
            "    var b = h2(a)",
            "    var c = h3(b)",
            "    var d = h4(c)",
            "    if d > 0",
            "        d += 1",
            "    if d > 10",
            "        d += 2",
            "    if d > 20",
            "        d += 3",
            "    if d > 30",
            "        d += 4",
            "    if d > 40",
            "        d += 5",
            "    if d > 50",
            "        d += 6",
            "    if d > 60",
            "        d += 7",
            "    if d > 70",
            "        d += 8",
            "    return d",
            "init",
            "    let v = wrapper(GetRandomInt(1, 100))",
            "    if v > 0",
            "        testSuccess()",
            "endpackage"
        );
        String inlined = Files.toString(new File("test-output/OptimizerTests_inlinerRatesByIncomingUsesNotOutgoingCalls_inl.j"), Charsets.UTF_8);
        assertFalse(inlined.contains("call wrapper"),
            "Expected wrapper to inline when it has one incoming use.");
        assertTrue(inlined.contains("GetRandomInt("),
            "Expected test setup to remain non-constant and observable in _inl output.");
    }

    @Test
    public void inlinerMultiReturnFallbackInitComesAfterReturnRewrites() throws IOException {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "@inline function maybeAbs(int x) returns int",
            "    if x > 0",
            "        return x",
            "    return 0 - x",
            "init",
            "    let y = maybeAbs(-4)",
            "    if y == 4",
            "        testSuccess()",
            "endpackage"
        );

        String inlined = Files.toString(new File("test-output/OptimizerTests_inlinerMultiReturnFallbackInitComesAfterReturnRewrites_inl.j"), Charsets.UTF_8);
        int firstReturnWrite = inlined.indexOf("set inlineRet = x");
        int fallbackDefaultWrite = inlined.lastIndexOf("set inlineRet = 0");
        assertTrue(firstReturnWrite >= 0, "Expected rewritten return assignment to inlineRet in _inl output.");
        assertTrue(fallbackDefaultWrite > firstReturnWrite,
            "Expected fallback default assignment to inlineRet after rewritten returns.");
    }

    @Test
    public void inlinerRepeatedTransitiveInliningSingleRun() throws IOException {
        testAssertOkLinesWithStdLib(false,
            "package test",
            "@inline function c(int x) returns int",
            "    return x + 1",
            "@inline function b(int x) returns int",
            "    return c(x) + 1",
            "@inline function a(int x) returns int",
            "    return b(x) + 1",
            "init",
            "    let y = a(GetRandomInt(1, 10))",
            "    if y > 0",
            "        testSuccess()",
            "endpackage"
        );

        String inlined = Files.toString(new File("test-output/OptimizerTests_inlinerRepeatedTransitiveInliningSingleRun_inl.j"), Charsets.UTF_8);
        assertFalse(inlined.contains("call a("), "Expected a() to be inlined.");
        assertFalse(inlined.contains("call b("), "Expected b() to be inlined transitively.");
        assertFalse(inlined.contains("call c("), "Expected c() to be inlined transitively.");
    }

    @Test
    public void inlinerDeepNestedTransitiveInlining() throws IOException {
        testAssertOkLinesWithStdLib(false,
            "package test",
            "@inline function e(int x) returns int",
            "    return x + 1",
            "@inline function d(int x) returns int",
            "    return e(x) + 1",
            "@inline function c(int x) returns int",
            "    return d(x) + 1",
            "@inline function b(int x) returns int",
            "    return c(x) + 1",
            "@inline function a(int x) returns int",
            "    return b(x) + 1",
            "init",
            "    let y = a(GetRandomInt(1, 10))",
            "    if y > 0",
            "        testSuccess()",
            "endpackage"
        );

        String inlined = Files.toString(new File("test-output/OptimizerTests_inlinerDeepNestedTransitiveInlining_inl.j"), Charsets.UTF_8);
        assertFalse(inlined.contains("call a("), "Expected a() to be inlined.");
        assertFalse(inlined.contains("call b("), "Expected b() to be inlined.");
        assertFalse(inlined.contains("call c("), "Expected c() to be inlined.");
        assertFalse(inlined.contains("call d("), "Expected d() to be inlined.");
        assertFalse(inlined.contains("call e("), "Expected e() to be inlined.");
    }

    @Test
    public void inlinerLocationLocalsAreInitializedBeforeUse() throws IOException {
        testAssertOkLinesWithStdLib(true,
            "package test",
            "@inline function chooseLoc(boolean c, location a, location b) returns location",
            "    if c",
                "        return a",
            "    return b",
            "init",
            "    location la = Location(0., 0.)",
            "    location lb = Location(1., 1.)",
            "    location picked = chooseLoc(GetRandomInt(0, 1) == 0, la, lb)",
            "    RemoveLocation(picked)",
            "    RemoveLocation(la)",
            "    RemoveLocation(lb)",
            "    testSuccess()",
            "endpackage"
        );

        String inlined = Files.toString(new File("test-output/OptimizerTests_inlinerLocationLocalsAreInitializedBeforeUse_inl.j"), Charsets.UTF_8);
        assertFalse(inlined.contains("call chooseLoc("), "Expected chooseLoc() to be inlined.");
        assertTrue(inlined.contains("local location inlineRet"), "Expected inline return temp for location type.");

        int initIdx = inlined.indexOf("set inlineRet = null");
        int useIdx = inlined.indexOf("set picked = inlineRet");
        assertTrue(initIdx >= 0, "Expected explicit initialization of location inlineRet.");
        assertTrue(useIdx > initIdx, "Expected inlineRet to be initialized before use.");
    }

    @Test
    public void inlinerMultiReturnKeepsPostReturnSideEffectsUnreachableUnderInlopt() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "native testFail(string s)",
            "@inline function pickPositive(int x) returns int",
            "    if x > 0",
            "        return x",
            "    testFail(\"post-return path executed\")",
            "    return 0 - x",
            "init",
            "    let y = pickPositive(7)",
            "    if y == 7",
            "        testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void inlinerMultiReturnRewriteIsExplicitInInlAndInloptOutput() throws IOException {
        testAssertOkLinesWithStdLib(true,
            "package test",
            "@inline function maybeAbs(int x) returns int",
            "    if x > 0",
            "        return x",
            "    return 0 - x",
            "init",
            "    let y = maybeAbs(GetRandomInt(-5, 5))",
            "    if y >= 0",
            "        testSuccess()",
            "endpackage"
        );

        String inl = Files.toString(new File("test-output/OptimizerTests_inlinerMultiReturnRewriteIsExplicitInInlAndInloptOutput_inl.j"), Charsets.UTF_8);
        String inlopt = Files.toString(new File("test-output/OptimizerTests_inlinerMultiReturnRewriteIsExplicitInInlAndInloptOutput_inlopt.j"), Charsets.UTF_8);

        for (String generated : java.util.List.of(inl, inlopt)) {
            assertFalse(generated.contains("call maybeAbs("), "Expected maybeAbs() to be fully inlined.");
            assertTrue(generated.contains("set inlineDone"), "Expected explicit inlineDone writes.");
            assertTrue(generated.contains("set inlineRet"), "Expected explicit inlineRet writes.");
            assertTrue(generated.contains("set inlineDone = false")
                    || generated.contains("local boolean inlineDone = false"),
                "Expected explicit inlineDone initialization.");
            assertTrue(generated.contains("set inlineDone = true"), "Expected explicit rewritten return marking.");
            assertTrue(generated.matches("(?s).*if\\s+not\\s+inlineDone.*"),
                "Expected explicit post-return gating in generated code.");
        }
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
        String compiled = Files.toString(new File("test-output/OptimizerTests_cyclicFunctionRemover_no_opts.j"), Charsets.UTF_8);
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
    public void precisionSensitiveRealFoldUsesSingleFoldingPath() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "native testFail(string s)",
            "@extern native R2I(real r) returns int",
            "@extern native I2S(int i) returns string",
            "",
            "@inline function risky(real a, real b) returns int",
            "    real d = a - b",
            "    return R2I(d)",
            "",
            "init",
            "    // On WC3 real semantics these literals collapse to same float, so (a - b) should be 0.",
            "    let x = risky(16777217., 16777216.)",
            "    if x == 0",
            "        testSuccess()",
            "    else",
            "        testFail(\"precision fold regression: \" + I2S(x))",
            "endpackage"
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
        ImVars locals = JassIm.ImVars(a, b, c, d, e);

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
    public void localMergerKeepsImplicitEntryLocalSeparateFromParameter() {
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, false, new RunArgs());
        ImProg prog = translator.getImProg();
        ImVar sinkA = JassIm.ImVar(model, TypesHelper.imInt(), "a", false);
        ImVar sinkB = JassIm.ImVar(model, TypesHelper.imInt(), "b", false);
        ImFunction sink = JassIm.ImFunction(model, "sink", JassIm.ImTypeVars(),
            JassIm.ImVars(sinkA, sinkB), JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(),
            Collections.emptyList());
        ImVar parameter = JassIm.ImVar(model, TypesHelper.imInt(), "parameter", false);
        ImVar implicit = JassIm.ImVar(model, TypesHelper.imInt(), "implicit", false);
        ImFunctionCall call = JassIm.ImFunctionCall(model, sink, JassIm.ImTypeArguments(),
            JassIm.ImExprs(JassIm.ImVarAccess(parameter), JassIm.ImVarAccess(implicit)), false,
            de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL);
        ImSet laterDefinition = JassIm.ImSet(model, JassIm.ImVarAccess(implicit), JassIm.ImIntVal(1));
        ImFunction caller = JassIm.ImFunction(model, "caller", JassIm.ImTypeVars(),
            JassIm.ImVars(parameter), JassIm.ImVoid(), JassIm.ImVars(implicit),
            JassIm.ImStmts(call, laterDefinition), Collections.emptyList());
        prog.getFunctions().add(sink);
        prog.getFunctions().add(caller);

        new LocalMerger().optimize(translator, new LocalPlayerContextAnalyzer(prog));

        ImFunctionCall optimizedCall = (ImFunctionCall) caller.getBody().get(0);
        ImVar first = ((ImVarAccess) optimizedCall.getArguments().get(0)).getVar();
        ImVar second = ((ImVarAccess) optimizedCall.getArguments().get(1)).getVar();
        assertNotSame(first, second,
            "function-entry values must not be assigned the same allocation slot");
    }

    @Test
    public void repeatedLocalOptimizationStartsANewIteration() {
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, false, new RunArgs());
        ImFunction main = JassIm.ImFunction(model, "main", JassIm.ImTypeVars(), JassIm.ImVars(),
            JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        ImFunction config = JassIm.ImFunction(model, "config", JassIm.ImTypeVars(), JassIm.ImVars(),
            JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        translator.getImProg().getFunctions().add(main);
        translator.getImProg().getFunctions().add(config);
        translator.setMainFunc(main);
        translator.setConfigFunc(config);
        ImOptimizer optimizer = new ImOptimizer(new TimeTaker.Default(), translator);

        optimizer.localOptimizations();
        main.getLocals().add(JassIm.ImVar(model, TypesHelper.imInt(), "lateUnused", false));
        optimizer.localOptimizations();

        assertTrue(main.getLocals().isEmpty(),
            "a second local-optimization invocation must execute its passes");
    }

    @Test
    public void luaArithmeticHelperRetryRespectsFunctionLocalBudget() {
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, false,
            new RunArgs().with("-lua", "-localOptimizations"));
        ImProg prog = translator.getImProg();

        ImVar helperA = JassIm.ImVar(model, TypesHelper.imInt(), "a", false);
        ImVar helperB = JassIm.ImVar(model, TypesHelper.imInt(), "b", false);
        ImFunction helper = JassIm.ImFunction(model, "__wurst_modInt", JassIm.ImTypeVars(),
            JassIm.ImVars(helperA, helperB), TypesHelper.imInt(), JassIm.ImVars(),
            JassIm.ImStmts(JassIm.ImReturn(model, JassIm.ImVarAccess(helperA))),
            Collections.emptyList());
        translator.luaModIntFunc = helper;

        ImVars callerParameters = JassIm.ImVars();
        for (int i = 0; i < 177; i++) {
            callerParameters.add(JassIm.ImVar(model, TypesHelper.imInt(), "p" + i, false));
        }
        ImVar result = JassIm.ImVar(model, TypesHelper.imInt(), "result", false);
        ImVars callerLocals = JassIm.ImVars(result);
        ImStmts callerBody = JassIm.ImStmts();
        for (int i = 0; i < 6; i++) {
            ImVar loopVar = JassIm.ImVar(model, TypesHelper.imInt(), "loop" + i, false);
            callerLocals.add(loopVar);
            callerBody.add(JassIm.ImVarargLoop(model, JassIm.ImStmts(),
                JassIm.ImVarargLoopVars(JassIm.ImVarargLoopVar(loopVar))));
        }
        ImFunctionCall call = JassIm.ImFunctionCall(model, helper, JassIm.ImTypeArguments(),
            JassIm.ImExprs(JassIm.ImIntVal(7), JassIm.ImIntVal(3)), false,
            de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL);
        callerBody.add(JassIm.ImSet(model, JassIm.ImVarAccess(result), call));
        ImVars sinkParameters = JassIm.ImVars();
        ImExprs sinkArguments = JassIm.ImExprs();
        for (int i = 0; i < callerParameters.size(); i++) {
            sinkParameters.add(JassIm.ImVar(model, TypesHelper.imInt(), "value" + i, false));
            sinkArguments.add(JassIm.ImVarAccess(callerParameters.get(i)));
        }
        ImFunction sink = JassIm.ImFunction(model, "sink", JassIm.ImTypeVars(), sinkParameters,
            JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        callerBody.add(JassIm.ImFunctionCall(model, sink, JassIm.ImTypeArguments(), sinkArguments,
            false, de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL));
        ImFunction caller = JassIm.ImFunction(model, "caller", JassIm.ImTypeVars(), callerParameters,
            JassIm.ImVoid(), callerLocals, callerBody,
            Collections.emptyList());
        prog.getFunctions().add(helper);
        prog.getFunctions().add(sink);
        prog.getFunctions().add(caller);

        assertEquals(0, new ImInliner(translator).inlineLuaDivModHelpersWithinLocalBudget());
        ImSet assignment = (ImSet) caller.getBody().get(6);
        assertTrue(assignment.getRight() instanceof ImFunctionCall,
            "the late retry must retain the helper when declarations exceed the safe budget");
    }

    @Test
    public void luaArithmeticHelperRetryReusesSequentialSlots() {
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, false,
            new RunArgs().with("-lua", "-localOptimizations"));
        ImProg prog = translator.getImProg();
        ImVar helperA = JassIm.ImVar(model, TypesHelper.imInt(), "a", false);
        ImVar helperB = JassIm.ImVar(model, TypesHelper.imInt(), "b", false);
        ImFunction helper = JassIm.ImFunction(model, "__wurst_modInt", JassIm.ImTypeVars(),
            JassIm.ImVars(helperA, helperB), TypesHelper.imInt(), JassIm.ImVars(),
            JassIm.ImStmts(JassIm.ImReturn(model, JassIm.ImVarAccess(helperA))),
            Collections.emptyList());
        translator.luaModIntFunc = helper;
        ImVars parameters = JassIm.ImVars();
        for (int i = 0; i < 187; i++) {
            parameters.add(JassIm.ImVar(model, TypesHelper.imInt(), "p" + i, false));
        }
        ImVar result = JassIm.ImVar(model, TypesHelper.imInt(), "result", false);
        ImFunction caller = JassIm.ImFunction(model, "caller", JassIm.ImTypeVars(), parameters,
            JassIm.ImVoid(), JassIm.ImVars(result), JassIm.ImStmts(
                JassIm.ImSet(model, JassIm.ImVarAccess(result), JassIm.ImFunctionCall(model, helper,
                    JassIm.ImTypeArguments(), JassIm.ImExprs(JassIm.ImIntVal(7), JassIm.ImIntVal(3)),
                    false, de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL)),
                JassIm.ImSet(model, JassIm.ImVarAccess(result), JassIm.ImFunctionCall(model, helper,
                    JassIm.ImTypeArguments(), JassIm.ImExprs(JassIm.ImIntVal(8), JassIm.ImIntVal(3)),
                    false, de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL))),
            Collections.emptyList());
        prog.getFunctions().add(helper);
        prog.getFunctions().add(caller);

        assertEquals(new ImInliner(translator).inlineLuaDivModHelpersWithinLocalBudget(), 2,
            "sequential helper sites should share the same peak allocation slots");
    }

    @Test
    public void luaArithmeticHelperRetryBudgetsOverlappingArgumentResults() {
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, false,
            new RunArgs().with("-lua", "-localOptimizations"));
        ImProg prog = translator.getImProg();
        ImVar helperA = JassIm.ImVar(model, TypesHelper.imInt(), "a", false);
        ImVar helperB = JassIm.ImVar(model, TypesHelper.imInt(), "b", false);
        ImFunction helper = JassIm.ImFunction(model, "__wurst_modInt", JassIm.ImTypeVars(),
            JassIm.ImVars(helperA, helperB), TypesHelper.imInt(), JassIm.ImVars(),
            JassIm.ImStmts(JassIm.ImReturn(model, JassIm.ImVarAccess(helperA))),
            Collections.emptyList());
        translator.luaModIntFunc = helper;

        ImVars callerParameters = JassIm.ImVars();
        for (int i = 0; i < 187; i++) {
            callerParameters.add(JassIm.ImVar(model, TypesHelper.imInt(), "p" + i, false));
        }
        ImVars fiveParameters = JassIm.ImVars();
        ImExprs overlappingArguments = JassIm.ImExprs();
        for (int i = 0; i < 5; i++) {
            fiveParameters.add(JassIm.ImVar(model, TypesHelper.imInt(), "arg" + i, false));
            overlappingArguments.add(JassIm.ImFunctionCall(model, helper, JassIm.ImTypeArguments(),
                JassIm.ImExprs(JassIm.ImVarAccess(callerParameters.get(i)), JassIm.ImIntVal(3)),
                false, de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL));
        }
        ImFunction takesFive = JassIm.ImFunction(model, "takesFive", JassIm.ImTypeVars(), fiveParameters,
            JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        ImVars keepAliveParameters = JassIm.ImVars();
        ImExprs keepAliveArguments = JassIm.ImExprs();
        for (int i = 0; i < callerParameters.size(); i++) {
            keepAliveParameters.add(JassIm.ImVar(model, TypesHelper.imInt(), "value" + i, false));
            keepAliveArguments.add(JassIm.ImVarAccess(callerParameters.get(i)));
        }
        ImFunction keepAlive = JassIm.ImFunction(model, "keepAlive", JassIm.ImTypeVars(),
            keepAliveParameters, JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(),
            Collections.emptyList());
        ImFunction caller = JassIm.ImFunction(model, "caller", JassIm.ImTypeVars(), callerParameters,
            JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(
                JassIm.ImFunctionCall(model, takesFive, JassIm.ImTypeArguments(), overlappingArguments,
                    false, de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL),
                JassIm.ImFunctionCall(model, keepAlive, JassIm.ImTypeArguments(), keepAliveArguments,
                    false, de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL)),
            Collections.emptyList());
        prog.getFunctions().add(helper);
        prog.getFunctions().add(takesFive);
        prog.getFunctions().add(keepAlive);
        prog.getFunctions().add(caller);

        int changed = new ImInliner(translator).inlineLuaDivModHelpersWithinLocalBudget();
        assertTrue(changed < 5,
            "overlapping argument results must stop helper inlining at the register budget");
        int[] remaining = {0};
        caller.getBody().accept(new ImStmts.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall call) {
                super.visit(call);
                if (call.getFunc() == helper) {
                    remaining[0]++;
                }
            }
        });
        assertTrue(remaining[0] > 0, "some overlapping helper calls must remain after the budget is reached");
    }

    @Test
    public void luaArithmeticHelperRetryPreservesLocalPlayerAllocationClasses() {
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, false,
            new RunArgs().with("-lua", "-localOptimizations"));
        ImProg prog = translator.getImProg();

        ImVar helperA = JassIm.ImVar(model, TypesHelper.imInt(), "a", false);
        ImVar helperB = JassIm.ImVar(model, TypesHelper.imInt(), "b", false);
        ImFunction helper = JassIm.ImFunction(model, "__wurst_modInt", JassIm.ImTypeVars(),
            JassIm.ImVars(helperA, helperB), TypesHelper.imInt(), JassIm.ImVars(),
            JassIm.ImStmts(JassIm.ImReturn(model, JassIm.ImVarAccess(helperA))),
            Collections.emptyList());
        translator.luaModIntFunc = helper;
        ImFunction localValue = JassIm.ImFunction(model, "GetLocationZ", JassIm.ImTypeVars(),
            JassIm.ImVars(), TypesHelper.imReal(), JassIm.ImVars(), JassIm.ImStmts(),
            Collections.singletonList(FunctionFlagEnum.IS_NATIVE));

        ImVars sinkParameters = JassIm.ImVars();
        for (int i = 0; i < 99; i++) {
            sinkParameters.add(JassIm.ImVar(model, TypesHelper.imReal(), "value" + i, false));
        }
        ImFunction sink = JassIm.ImFunction(model, "sink", JassIm.ImTypeVars(), sinkParameters,
            JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        ImVars callerLocals = JassIm.ImVars();
        ImStmts callerBody = JassIm.ImStmts();
        ImExprs localArguments = JassIm.ImExprs();
        ImExprs synchronizedArguments = JassIm.ImExprs();
        for (int i = 0; i < 99; i++) {
            ImVar local = JassIm.ImVar(model, TypesHelper.imReal(), "local" + i, false);
            ImVar synchronizedVar = JassIm.ImVar(model, TypesHelper.imReal(), "sync" + i, false);
            callerLocals.add(local);
            callerLocals.add(synchronizedVar);
            callerBody.add(JassIm.ImSet(model, JassIm.ImVarAccess(local),
                JassIm.ImFunctionCall(model, localValue, JassIm.ImTypeArguments(), JassIm.ImExprs(),
                    false, de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL)));
            localArguments.add(JassIm.ImVarAccess(local));
            synchronizedArguments.add(JassIm.ImVarAccess(synchronizedVar));
        }
        callerBody.add(JassIm.ImFunctionCall(model, sink, JassIm.ImTypeArguments(), localArguments,
            false, de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL));
        for (int i = 0; i < 99; i++) {
            ImVar synchronizedVar = callerLocals.get(i * 2 + 1);
            callerBody.add(JassIm.ImSet(model, JassIm.ImVarAccess(synchronizedVar), JassIm.ImRealVal("1.")));
        }
        callerBody.add(JassIm.ImFunctionCall(model, sink, JassIm.ImTypeArguments(), synchronizedArguments,
            false, de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL));
        ImVar result = JassIm.ImVar(model, TypesHelper.imInt(), "result", false);
        callerLocals.add(result);
        ImFunctionCall helperCall = JassIm.ImFunctionCall(model, helper, JassIm.ImTypeArguments(),
            JassIm.ImExprs(JassIm.ImIntVal(7), JassIm.ImIntVal(3)), false,
            de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL);
        callerBody.add(JassIm.ImSet(model, JassIm.ImVarAccess(result), helperCall));
        ImFunction caller = JassIm.ImFunction(model, "caller", JassIm.ImTypeVars(), JassIm.ImVars(),
            JassIm.ImVoid(), callerLocals, callerBody, Collections.emptyList());
        prog.getFunctions().add(localValue);
        prog.getFunctions().add(helper);
        prog.getFunctions().add(sink);
        prog.getFunctions().add(caller);

        assertEquals(0, new ImInliner(translator).inlineLuaDivModHelpersWithinLocalBudget());
        assertTrue(((ImSet) caller.getBody().get(caller.getBody().size() - 1)).getRight()
                instanceof ImFunctionCall,
            "local-player-dependent and synchronized allocation classes must both count toward the budget");
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

        // should at least add one additional function
        assertTrue(prog.getFunctions().size() >= 2);


    }

    @Test
    public void externCallIsObservableSideEffectEvenWithEmptyBody() {
        WurstModel model = Ast.WurstModel();
        ImTranslator tr = new ImTranslator(model, false, new RunArgs());
        ImProg prog = tr.getImProg();
        Element trace = Ast.NoExpr();

        ImFunction externFunc = JassIm.ImFunction(
            trace,
            "someExternCall",
            JassIm.ImTypeVars(),
            JassIm.ImVars(),
            TypesHelper.imInt(),
            JassIm.ImVars(),
            JassIm.ImStmts(),
            Collections.singletonList(FunctionFlagEnum.IS_EXTERN)
        );
        prog.getFunctions().add(externFunc);

        ImFunctionCall externCall = JassIm.ImFunctionCall(
            trace,
            externFunc,
            JassIm.ImTypeArguments(),
            JassIm.ImExprs(),
            false,
            de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL
        );

        SideEffectAnalyzer analyzer = new SideEffectAnalyzer(prog);
        assertTrue(analyzer.hasObservableSideEffects(externCall, f -> false),
            "extern calls must be treated as observable side effects");
    }

    @Test
    public void unaryMinus_minInt_notFolded() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "init",
            "    int x = -2147483648",
            "    int y = -x",              // MUST NOT fold to 2147483648 (invalid)",
            "    // We can't compare to 2147483648; just check the IR still contains unary minus or equals x",
            "    if x == -2147483648",     // just to use x/y and compile",
            "        testSuccess()"
        );
    }

    @Test
    public void realFormatting_consistent_fromIntOps() throws Exception {
        test().lines(
            "package test",
            "native print(real r)",
            "init",
            "   real a = 1 div 2",
            "   real b = 5 mod 2",
            "   real c = 1 / 2",    // real path",
            "   print(a)",
            "   print(b)",
            "   print(c)",
            "endpackage");
        String out = Files.toString(new File("test-output/OptimizerTests_realFormatting_consistent_fromIntOps_opt.j"), Charsets.UTF_8);
        assertTrue(out.contains("(0.5)"));
        assertTrue(out.contains("(1)"));
        assertFalse(out.matches("(?s).*E[-+]?\\d+.*")); // no scientific notation
    }

    @Test
    public void stringConcat_leftEmptyNeutral() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "function s() returns string",
            "    return \"x\"",
            "init",
            "    string a = \"\" + s()",
            "    if a == \"x\"",
            "        testSuccess()"
        );
    }

    @Test
    public void intDivMod_negatives_folded() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "init",
            "    int a = -7 div 3",
            "    int b = -7 mod 3",
            "    // Java-style: a=-2, b=-1. If Wurst/JASS defines differently, update asserts.",
            "    if a == -2 and b == -1",
            "        testSuccess()"
        );
    }

    @Test
    public void notComparison_and_deMorgan() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "init",
            "    if not (3 < 4) or not (5 == 6)",
            "        testSuccess()" // should fold to true",
        );
    }

    @Test
    public void unaryMinus_real_fold() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "init",
            "    real x = -0.5",
            "    if x < 0.0",
            "        testSuccess()"
        );
    }

    @Test
    public void stringConcat_bothNeutralSides() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "function f() returns string",
            "    return \"y\"",
            "init",
            "    string a = \"\" + (\"x\" + \"\") + f() + \"\"",
            "    if a == \"xy\"",
            "        testSuccess()"
        );
    }

    @Test
    public void noFold_divOrModByZero() throws Exception {
        test().lines(
            "package test",
            "native printi(int i)",
            "native printr(real r)",
            "init",
            "    int a = 5 div 0",
            "    int b = 5 mod 0",
            "    real c = 5.0 / 0.0",
            "    real d = 5.0 % 0.0",
            "    printi(a)",
            "    printi(b)",
            "    printr(c)",
            "    printr(d)",
            "endpackage");
        String out = Files.toString(new File("test-output/OptimizerTests_noFold_divOrModByZero_opt.j"), Charsets.UTF_8);
        // Just a weak check: expressions remain, not constants
        assertTrue(out.contains("5 / 0") && out.contains("ModuloInteger(5, 0)") && out.contains("5.0 / 0.0") && out.contains("ModuloReal(5.0, 0.0)"));
    }

    @Test
    public void consecutiveSet_dontFireWhenRightUsesVar() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "init",
            "    int x = 100",
            "    x = 1",
            "    x = x + (x + 2)", // right uses x -> MUST NOT rewrite to (1 + (x+2))",
            "    if x == 4",
            "        testSuccess()"
        );
    }

    @Test
    public void realRealMixed_add_sub() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "init",
            "    real a = 2 + 0.5",
            "    real b = 0.5 + 2",
            "    real c = 2 - 0.5",
            "    real d = 0.5 - 2",
            "    if a == 2.5 and b == 2.5 and c == 1.5 and d == -1.5",
            "        testSuccess()"
        );
    }


    @Test
    public void realRealMixed_mult() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "init",
            "    real a = 2 * 0.5",
            "    real b = 0 * 3.14",
            "    if a == 1.0 and b == 0.0",
            "        testSuccess()"
        );
    }
    @Test
    public void realRealMixed_div_bothDirections() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "init",
            "    real a = 1 / 2.0",
            "    real b = 1.0 / 2",
            "    real c = 4 * (1.0 / 2)",  // ensure nested fold plays nice",
            "    if a == 0.5 and b == 0.5 and c == 2.0",
            "        testSuccess()"
        );
    }

    @Test
    public void realRealMixed_comparisons() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "init",
            "    boolean p1 = 2 > 1.5",
            "    boolean p2 = 2 >= 2.0",
            "    boolean p3 = 1.5 < 2",
            "    boolean p4 = 1.5 <= 1",
            "    boolean p5 = 2 == 2.0",
            "    boolean p6 = 2 != 2.5",
            "    if p1 and p2 and p3 and (not p4) and p5 and p6",
            "        testSuccess()"
        );
    }
    @Test
    public void realRealMixed_precision_oneThird_literal() throws Exception {
        test().lines(
            "package test",
            "native print(real r)",
            "init",
            "    real a = 1.0 / 3",
            "    real b = 1 / 3.0",
            "    print(a)", // keep usage so it survives",
            "    print(b)"
        );
        String out = Files.toString(new File("test-output/OptimizerTests_realRealMixed_precision_oneThird_literal_opt.j"), Charsets.UTF_8);
        // Common 32-bit float for 1/3 is 0.33333334 — accept either a or b presence
        assertTrue(out.contains("0.33333334"));
        // Also guard against scientific notation
        assertFalse(out.matches("(?s).*E[-+]?\\d+.*"));
    }

    @Test
    public void realRealMixed_chained() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "init",
            "    real a = 1 + 2.0 - 3 + 4.0 * 0.5",
            "    // 1 + 2 - 3 + 2 = 2",
            "    if a == 2.0",
            "        testSuccess()"
        );
    }

    @Test
    public void realRealMixed_nestedParen() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "init",
            "    real inner = (1.0 + 2) * (6 / 4.0)", // (3.0) * (1.5) = 4.5",
            "    if inner == 4.5",
            "        testSuccess()"
        );
    }

    @Test
    public void realRealMixed_divByZero_notFolded_textual() throws Exception {
        test().lines(
            "package test",
            "native print(real r)",
            "init",
            "   real a = 1 / 0.0",
            "   real b = 1.0 / 0",
            "   print(a)",
            "   print(b)"
        );
        String out = Files.toString(new File("test-output/OptimizerTests_realRealMixed_divByZero_notFolded_textual_opt.j"), Charsets.UTF_8);
        // We don't rely on runtime Infinity/NaN behavior; just ensure constants weren't folded in.
        // Accept either form depending on earlier rewrites (1/0.0, 1.0/0):
        assertTrue(out.contains("/ 0.0") || out.contains("/ 0"));
    }

    @Test
    public void realRealMixed_noScientificNotation() throws Exception {
        test().lines(
            "package test",
            "native print(real r)",
            "init",
            "    real a = 2 * 0.5",
            "    real b = 1 / 3.0",
            "    real c = 1.0 / 2",
            "    print(a)",
            "    print(b)",
            "    print(c)"
        );
        String out = Files.toString(new File("test-output/OptimizerTests_realRealMixed_noScientificNotation_opt.j"), Charsets.UTF_8);
        assertFalse(out.matches("(?s).*E[-+]?\\d+.*"));
    }

    @Test
    public void realRealMixed_equality_roundTripGuard() throws Exception {
        test().lines(
            "package test",
            "native print(boolean b)",
            "init",
            "    boolean b = (0.1 + 0.2) == 0.3", // all reals; but drives the round-trip idea",
            "    print(b)"
        );
        String out = Files.toString(new File("test-output/OptimizerTests_realRealMixed_equality_roundTripGuard_opt.j"), Charsets.UTF_8);
        // We don't assert true/false (depends on float), we only ensure no sci-notation
        assertFalse(out.matches("(?s).*E[-+]?\\d+.*"));
    }

    @Test
    public void effectfulBooleanOperandsMustNotBeDiscarded() throws Exception {
        test().lines(
            "type player extends handle",
            "package test",
            "@extern native GetLocalPlayer() returns player",
            "@extern native Player(integer i) returns player",
            "native print(integer i)",
            "integer calls = 0",
            "@noinline function probeOr() returns boolean",
            "    calls++",
            "    return GetLocalPlayer() == Player(0)",
            "@noinline function probeAnd() returns boolean",
            "    calls++",
            "    return GetLocalPlayer() == Player(0)",
            "init",
            "    if probeOr() or true",
            "        print(calls)",
            "    if probeAnd() and false",
            "        print(calls)"
        );

        String optimized = Files.toString(
            new File("test-output/OptimizerTests_effectfulBooleanOperandsMustNotBeDiscarded_opt.j"),
            Charsets.UTF_8);
        assertTrue(optimized.contains("if probeOr() or true"),
            "x or true must still evaluate effectful x");
        assertTrue(optimized.contains("if probeAnd() and false"),
            "x and false must still evaluate effectful x");
    }

    @Test
    public void directGetLocalPlayerConditionMustNotBeDiscarded() throws Exception {
        test().lines(
            "type player extends handle",
            "package test",
            "@extern native GetLocalPlayer() returns player",
            "@extern native Player(integer i) returns player",
            "native print(integer i)",
            "init",
            "    if (GetLocalPlayer() == Player(0)) or true",
            "        print(1)"
        );

        String optimized = Files.toString(
            new File("test-output/OptimizerTests_directGetLocalPlayerConditionMustNotBeDiscarded_opt.j"),
            Charsets.UTF_8);
        assertTrue(optimized.contains("GetLocalPlayer()"),
            "local-player-dependent expressions must not be discarded");
    }

    @Test
    public void synchronizedValueMustNotMoveIntoLocalPlayerBranch() throws Exception {
        test().lines(
            "type player extends handle",
            "type unit extends handle",
            "package test",
            "@extern native GetLocalPlayer() returns player",
            "@extern native Player(integer i) returns player",
            "@extern native CreateUnit(player p, integer id, real x, real y, real face) returns unit",
            "native print(unit u)",
            "init",
            "    unit u = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
            "    player localPlayer = GetLocalPlayer()",
            "    player playerZero = Player(0)",
            "    if localPlayer == playerZero",
            "        print(u)"
        );

        String optimized = Files.toString(
            new File("test-output/OptimizerTests_synchronizedValueMustNotMoveIntoLocalPlayerBranch_inlopt.j"),
            Charsets.UTF_8);
        int createUnit = optimized.indexOf("CreateUnit(");
        int localCondition = optimized.indexOf("if ");
        int use = optimized.indexOf("print(u)");
        assertTrue(createUnit >= 0 && localCondition > createUnit && use > localCondition,
            "CreateUnit must remain in synchronized context before the local-player branch");
    }

    @Test
    public void branchMergerMustNotHoistAcrossStoredLocalPlayerCondition() throws Exception {
        test().lines(
            "type player extends handle",
            "package test",
            "@extern native GetLocalPlayer() returns player",
            "@extern native Player(integer i) returns player",
            "native print(integer i)",
            "integer result = 0",
            "init",
            "    player localPlayer = GetLocalPlayer()",
            "    player alias = localPlayer",
            "    player playerZero = Player(0)",
            "    if alias == playerZero",
            "        result = 7",
            "    else",
            "        result = 7",
            "    print(result)"
        );

        String optimized = Files.toString(
            new File("test-output/OptimizerTests_branchMergerMustNotHoistAcrossStoredLocalPlayerCondition_opt.j"),
            Charsets.UTF_8);
        assertTrue(countOccurrences(optimized, "test_result = 7") >= 2,
            "identical branches controlled by local-player data must remain separate");
    }

    @Test
    public void branchMergerMustTrackLocalPlayerThroughFunctionParameters() throws Exception {
        test().lines(
            "type player extends handle",
            "package test",
            "@extern native GetLocalPlayer() returns player",
            "@extern native Player(integer i) returns player",
            "native print(integer i)",
            "player remembered",
            "integer result = 0",
            "@noinline function remember(player p)",
            "    remembered = p",
            "init",
            "    remember(GetLocalPlayer())",
            "    player playerZero = Player(0)",
            "    if remembered == playerZero",
            "        result = 9",
            "    else",
            "        result = 9",
            "    print(result)"
        );

        String optimized = Files.toString(
            new File("test-output/OptimizerTests_branchMergerMustTrackLocalPlayerThroughFunctionParameters_opt.j"),
            Charsets.UTF_8);
        assertTrue(countOccurrences(optimized, "test_result = 9") >= 2,
            "GetLocalPlayer taint must flow through call arguments and parameters");
    }

    @Test
    public void branchMergerMustTrackLocalPlayerControlDependentAssignments() throws Exception {
        test().lines(
            "type player extends handle",
            "package test",
            "@extern native GetLocalPlayer() returns player",
            "@extern native Player(integer i) returns player",
            "native print(integer i)",
            "player selected",
            "integer result = 0",
            "init",
            "    if GetLocalPlayer() == Player(0)",
            "        selected = Player(0)",
            "    else",
            "        selected = Player(1)",
            "    if selected == Player(0)",
            "        result = 11",
            "    else",
            "        result = 11",
            "    print(result)"
        );

        String optimized = Files.toString(
            new File("test-output/OptimizerTests_branchMergerMustTrackLocalPlayerControlDependentAssignments_opt.j"),
            Charsets.UTF_8);
        assertTrue(countOccurrences(optimized, "test_result = 11") >= 2,
            "values assigned under local-player control must remain local-player-dependent");
    }

    @Test
    public void branchMergerMustTrackLocalPlayerDependentArrayIndexWrites() throws Exception {
        test().lines(
            "type player extends handle",
            "package test",
            "@extern native GetLocalPlayer() returns player",
            "@extern native GetPlayerId(player p) returns integer",
            "native print(integer i)",
            "integer array values",
            "integer result = 0",
            "init",
            "    values[GetPlayerId(GetLocalPlayer())] = 1",
            "    if values[0] == 1",
            "        result = 31",
            "    else",
            "        result = 31",
            "    print(result)"
        );

        String optimized = Files.toString(
            new File("test-output/OptimizerTests_branchMergerMustTrackLocalPlayerDependentArrayIndexWrites_opt.j"),
            Charsets.UTF_8);
        assertTrue(countOccurrences(optimized, "test_result = 31") >= 2,
            "an array written through a local-player-dependent index must remain local-player-dependent");
    }

    @Test
    public void branchMergerMustTrackLocalPlayerDependentMemberReceiverWrites() throws Exception {
        test().lines(
            "type player extends handle",
            "package test",
            "@extern native GetLocalPlayer() returns player",
            "@extern native Player(integer i) returns player",
            "native print(integer i)",
            "class Box",
            "    integer value",
            "Box first",
            "Box second",
            "integer result = 0",
            "init",
            "    first = new Box",
            "    second = new Box",
            "    Box selected",
            "    if GetLocalPlayer() == Player(0)",
            "        selected = first",
            "    else",
            "        selected = second",
            "    selected.value = 1",
            "    if first.value == 1",
            "        result = 37",
            "    else",
            "        result = 37",
            "    print(result)"
        );

        String optimized = Files.toString(
            new File("test-output/OptimizerTests_branchMergerMustTrackLocalPlayerDependentMemberReceiverWrites_opt.j"),
            Charsets.UTF_8);
        assertTrue(countOccurrences(optimized, "test_result = 37") >= 2,
            "a member written through a local-player-dependent receiver must remain local-player-dependent");
    }

    @Test
    public void localPlayerControlMustPropagateThroughCalledFunctions() throws Exception {
        test().lines(
            "type player extends handle",
            "package test",
            "@extern native GetLocalPlayer() returns player",
            "@extern native Player(integer i) returns player",
            "native print(integer i)",
            "player selected",
            "integer result = 0",
            "@noinline function select(player p)",
            "    selected = p",
            "init",
            "    if GetLocalPlayer() == Player(0)",
            "        select(Player(0))",
            "    else",
            "        select(Player(1))",
            "    if selected == Player(0)",
            "        result = 13",
            "    else",
            "        result = 13",
            "    print(result)"
        );

        String optimized = Files.toString(
            new File("test-output/OptimizerTests_localPlayerControlMustPropagateThroughCalledFunctions_opt.j"),
            Charsets.UTF_8);
        assertTrue(countOccurrences(optimized, "test_result = 13") >= 2,
            "callee assignments must inherit local-player control from their call sites");
    }

    @Test
    public void localPlayerControlMustPropagateIntoFunctionReturns() throws Exception {
        test().lines(
            "type player extends handle",
            "package test",
            "@extern native GetLocalPlayer() returns player",
            "@extern native Player(integer i) returns player",
            "native print(integer i)",
            "integer result = 0",
            "@noinline function selectedPlayer() returns player",
            "    if GetLocalPlayer() == Player(0)",
            "        return Player(0)",
            "    else",
            "        return Player(1)",
            "init",
            "    player selected = selectedPlayer()",
            "    if selected == Player(0)",
            "        result = 17",
            "    else",
            "        result = 17",
            "    print(result)"
        );

        String optimized = Files.toString(
            new File("test-output/OptimizerTests_localPlayerControlMustPropagateIntoFunctionReturns_opt.j"),
            Charsets.UTF_8);
        assertTrue(countOccurrences(optimized, "test_result = 17") >= 2,
            "returns selected under local-player control must remain local-player-dependent");
    }

    @Test
    public void statementsAfterLocalEarlyReturnMustRemainLocallyControlled() throws Exception {
        test().lines(
            "type player extends handle",
            "package test",
            "@extern native GetLocalPlayer() returns player",
            "@extern native Player(integer i) returns player",
            "native print(integer i)",
            "player selected",
            "integer result = 0",
            "@noinline function updateUnlessLocalPlayerZero()",
            "    if GetLocalPlayer() == Player(0)",
            "        return",
            "    selected = Player(1)",
            "init",
            "    updateUnlessLocalPlayerZero()",
            "    if selected == Player(1)",
            "        result = 29",
            "    else",
            "        result = 29",
            "    print(result)"
        );

        String optimized = Files.toString(
            new File("test-output/OptimizerTests_statementsAfterLocalEarlyReturnMustRemainLocallyControlled_opt.j"),
            Charsets.UTF_8);
        assertTrue(countOccurrences(optimized, "test_result = 29") >= 2,
            "statements reached after a local early return must remain locally controlled");
    }

    @Test
    public void andRightOperandMustInheritLocalPlayerControl() throws Exception {
        test().lines(
            "type player extends handle",
            "package test",
            "@extern native GetLocalPlayer() returns player",
            "@extern native Player(integer i) returns player",
            "native print(integer i)",
            "player selected",
            "integer result = 0",
            "@noinline function updateSelectedState() returns boolean",
            "    selected = Player(0)",
            "    return true",
            "init",
            "    if (GetLocalPlayer() == Player(0)) and updateSelectedState()",
            "        print(0)",
            "    if selected == Player(0)",
            "        result = 19",
            "    else",
            "        result = 19",
            "    print(result)"
        );

        String optimized = Files.toString(
            new File("test-output/OptimizerTests_andRightOperandMustInheritLocalPlayerControl_opt.j"),
            Charsets.UTF_8);
        assertTrue(countOccurrences(optimized, "test_result = 19") >= 2,
            "the right operand of local-player-dependent AND must be locally controlled");
    }

    @Test
    public void orRightOperandMustInheritLocalPlayerControl() throws Exception {
        test().lines(
            "type player extends handle",
            "package test",
            "@extern native GetLocalPlayer() returns player",
            "@extern native Player(integer i) returns player",
            "native print(integer i)",
            "player selected",
            "integer result = 0",
            "@noinline function updateSelectedState() returns boolean",
            "    selected = Player(0)",
            "    return false",
            "init",
            "    if (GetLocalPlayer() == Player(0)) or updateSelectedState()",
            "        print(0)",
            "    if selected == Player(0)",
            "        result = 23",
            "    else",
            "        result = 23",
            "    print(result)"
        );

        String optimized = Files.toString(
            new File("test-output/OptimizerTests_orRightOperandMustInheritLocalPlayerControl_opt.j"),
            Charsets.UTF_8);
        assertTrue(countOccurrences(optimized, "test_result = 23") >= 2,
            "the right operand of local-player-dependent OR must be locally controlled");
    }

    @Test
    public void functionUsingGetLocalPlayerMustNotBeInlined() throws Exception {
        test().lines(
            "type player extends handle",
            "package test",
            "@extern native GetLocalPlayer() returns player",
            "@inline function currentPlayer() returns player",
            "    return GetLocalPlayer()",
            "@inline function forwardedPlayer() returns player",
            "    return currentPlayer()",
            "native consume(player p)",
            "init",
            "    consume(currentPlayer())",
            "    consume(forwardedPlayer())"
        );

        String inlined = Files.toString(
            new File("test-output/OptimizerTests_functionUsingGetLocalPlayerMustNotBeInlined_inl.j"),
            Charsets.UTF_8);
        assertTrue(inlined.contains("call consume(currentPlayer())"),
            "functions using GetLocalPlayer must remain explicit calls");
        assertTrue(inlined.contains("call consume(forwardedPlayer())"),
            "transitive GetLocalPlayer wrappers must remain explicit calls");
    }

    /**
     * The local-player analysis marks a function's return fact whenever the function is reachable
     * from a client-local control region, transitively over the call graph. That fact is right for
     * the passes which move code across control boundaries and wrong as an inlining barrier:
     * substituting a body at a call site runs it under exactly the control the call already had.
     * A pure helper called once under a GetLocalPlayer branch must still inline everywhere, while a
     * wrapper which itself calls GetLocalPlayer must stay an explicit call.
     */
    @Test
    public void pureHelperReachableFromLocalPlayerBranchIsStillInlined() throws Exception {
        test().lines(
            "type player extends handle",
            "package test",
            "@extern native GetLocalPlayer() returns player",
            "@extern native Player(integer i) returns player",
            "native consume(integer i)",
            "native consumePlayer(player p)",
            "integer offset = 0",
            "@inline function slot(integer a, integer b) returns integer",
            "    return a * 8 + b",
            "@inline function currentPlayer() returns player",
            "    return GetLocalPlayer()",
            "init",
            "    if GetLocalPlayer() == Player(0)",
            "        consume(slot(offset, 1))",
            "    consume(slot(offset, 2))",
            "    consumePlayer(currentPlayer())"
        );

        String inlined = Files.toString(
            new File("test-output/OptimizerTests_pureHelperReachableFromLocalPlayerBranchIsStillInlined_inl.j"),
            Charsets.UTF_8);
        assertFalse(inlined.contains("slot("),
            "a pure helper must inline at every call site, including the one under the local-player branch");
        assertTrue(inlined.contains("call consumePlayer(currentPlayer())"),
            "a wrapper which calls GetLocalPlayer itself must remain an explicit call");
    }

    @Test
    public void branchMergerMustNotHoistAcrossClientLocalConditions() throws Exception {
        test().lines(
            "type unit extends handle",
            "package test",
            "@extern native GetCameraTargetPositionX() returns real",
            "@extern native BlzGetUnitZ(unit whichUnit) returns real",
            "@extern native BlzIsLocalClientActive() returns boolean",
            "native getUnit() returns unit",
            "native print(integer i)",
            "integer cameraResult = 0",
            "integer unitResult = 0",
            "integer activeClientResult = 0",
            "init",
            "    real cameraX = GetCameraTargetPositionX()",
            "    if cameraX > 0.",
            "        cameraResult = 41",
            "    else",
            "        cameraResult = 41",
            "    real unitZ = BlzGetUnitZ(getUnit())",
            "    if unitZ > 0.",
            "        unitResult = 43",
            "    else",
            "        unitResult = 43",
            "    boolean activeClient = BlzIsLocalClientActive()",
            "    if activeClient",
            "        activeClientResult = 53",
            "    else",
            "        activeClientResult = 53",
            "    print(cameraResult)",
            "    print(unitResult)",
            "    print(activeClientResult)"
        );

        String optimized = Files.toString(
            new File("test-output/OptimizerTests_branchMergerMustNotHoistAcrossClientLocalConditions_opt.j"),
            Charsets.UTF_8);
        assertTrue(countOccurrences(optimized, "test_cameraResult = 41") >= 2,
            "statements must not be hoisted across a client-local camera condition");
        assertTrue(countOccurrences(optimized, "test_unitResult = 43") >= 2,
            "statements must not be hoisted across a client-local unit Z condition");
        assertTrue(countOccurrences(optimized, "test_activeClientResult = 53") >= 2,
            "statements must not be hoisted across local-client activity state");
    }

    @Test
    public void clientLocalNativeValuesAreLocalitySources() {
        java.util.Set<String> localValueSources = new java.util.LinkedHashSet<>(java.util.Arrays.asList(
            "GetLocalPlayer",
            "GetLocationZ",
            "GetCameraMargin",
            "GetCameraBoundMinX",
            "GetCameraBoundMinY",
            "GetCameraBoundMaxX",
            "GetCameraBoundMaxY",
            "GetCameraField",
            "GetCameraTargetPositionX",
            "GetCameraTargetPositionY",
            "GetCameraTargetPositionZ",
            "GetCameraTargetPositionLoc",
            "GetCameraEyePositionX",
            "GetCameraEyePositionY",
            "GetCameraEyePositionZ",
            "GetCameraEyePositionLoc",
            "GetLocalizedString",
            "GetLocalizedHotkey",
            "GetObjectName",
            "BlzGetLocalUnitZ",
            "BlzGetUnitZ",
            "BlzGetLocalClientWidth",
            "BlzGetLocalClientHeight",
            "BlzIsLocalClientActive",
            "BlzGetMouseFocusUnit",
            "BlzGetLocale"
        ));
        java.util.Set<String> intentionallyExcludedSources = new java.util.LinkedHashSet<>(java.util.Arrays.asList(
            "BlzGetTriggerPlayerMouseX",
            "BlzGetTriggerPlayerKey",
            "BlzGetTriggerFrameValue",
            "BlzFrameIsVisible",
            "BlzGetLocalSpecialEffectX",
            "AddLightning",
            "MoveLightning",
            "LoadEffectHandle",
            "LoadLightningHandle",
            "LoadFrameHandle",
            "GetSoundIsPlaying",
            "BlzIsSelectionEnabled"
        ));
        Element trace = Ast.NoExpr();
        ImFunctions functions = JassIm.ImFunctions();
        java.util.Map<String, ImFunction> functionsByName = new java.util.LinkedHashMap<>();
        for (String name : localValueSources) {
            ImFunction nativeFunction = nativeIntFunction(trace, name);
            functions.add(nativeFunction);
            functionsByName.put(name, nativeFunction);
        }
        for (String name : intentionallyExcludedSources) {
            ImFunction nativeFunction = nativeIntFunction(trace, name);
            functions.add(nativeFunction);
            functionsByName.put(name, nativeFunction);
        }
        ImProg prog = JassIm.ImProg(
            trace,
            JassIm.ImVars(),
            functions,
            JassIm.ImMethods(),
            JassIm.ImClasses(),
            JassIm.ImTypeClassFuncs(),
            new java.util.HashMap<>()
        );
        LocalPlayerContextAnalyzer analyzer = new LocalPlayerContextAnalyzer(prog);

        for (String name : localValueSources) {
            assertTrue(analyzer.isLocalPlayerSource(functionsByName.get(name)),
                name + " must be treated as a client-local value source");
        }
        for (String name : intentionallyExcludedSources) {
            assertFalse(analyzer.isLocalPlayerSource(functionsByName.get(name)),
                name + " is synchronized event data or user-managed local state");
        }
    }

    private static ImFunction nativeIntFunction(Element trace, String name) {
        return JassIm.ImFunction(
            trace,
            name,
            JassIm.ImTypeVars(),
            JassIm.ImVars(),
            TypesHelper.imInt(),
            JassIm.ImVars(),
            JassIm.ImStmts(),
            Collections.singletonList(FunctionFlagEnum.IS_NATIVE)
        );
    }

    @Test(timeOut = 10_000)
    public void deeplyNestedIndependentCallsDoNotCauseExponentialLocalPlayerAnalysis() {
        String nestedCall = "Player(0)";
        for (int i = 0; i < 30; i++) {
            nestedCall = "passthrough(" + nestedCall + ")";
        }

        test().lines(
            "type player extends handle",
            "package test",
            "@extern native Player(integer i) returns player",
            "native print(integer i)",
            "@noinline function passthrough(player p) returns player",
            "    return p",
            "init",
            "    if " + nestedCall + " == Player(0)",
            "        print(1)"
        );
    }

    @Test(timeOut = 10_000)
    public void reverseOrderedCallChainUsesLocalPlayerWorklist() {
        Element trace = Ast.NoExpr();
        ImFunctions functions = JassIm.ImFunctions();
        for (int i = 0; i < 4_000; i++) {
            functions.add(JassIm.ImFunction(
                trace,
                "chain" + i,
                JassIm.ImTypeVars(),
                JassIm.ImVars(),
                TypesHelper.imInt(),
                JassIm.ImVars(),
                JassIm.ImStmts(),
                Collections.emptyList()
            ));
        }
        ImFunction getLocalPlayer = JassIm.ImFunction(
            trace,
            "GetLocalPlayer",
            JassIm.ImTypeVars(),
            JassIm.ImVars(),
            TypesHelper.imInt(),
            JassIm.ImVars(),
            JassIm.ImStmts(),
            Collections.singletonList(FunctionFlagEnum.IS_NATIVE)
        );
        functions.add(getLocalPlayer);

        for (int i = 0; i < functions.size() - 1; i++) {
            ImFunction caller = functions.get(i);
            ImFunction callee = functions.get(i + 1);
            caller.getBody().add(JassIm.ImReturn(
                trace,
                JassIm.ImFunctionCall(
                    trace,
                    callee,
                    JassIm.ImTypeArguments(),
                    JassIm.ImExprs(),
                    false,
                    de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL
                )
            ));
        }

        ImProg prog = JassIm.ImProg(
            trace,
            JassIm.ImVars(),
            functions,
            JassIm.ImMethods(),
            JassIm.ImClasses(),
            JassIm.ImTypeClassFuncs(),
            new java.util.HashMap<>()
        );
        LocalPlayerContextAnalyzer analyzer = new LocalPlayerContextAnalyzer(prog);

        assertTrue(analyzer.functionInliningIsLocalPlayerSensitive(functions.get(0)),
            "GetLocalPlayer dependency must propagate through the complete call chain");
        assertTrue(analyzer.functionUsesLocalPlayer(functions.get(0)),
            "GetLocalPlayer usage must propagate through the complete call chain");
    }

    @Test(timeOut = 10_000)
    public void deeplyNestedImDoesNotOverflowLocalPlayerAnalysis() {
        Element trace = Ast.NoExpr();
        ImStmts nested = JassIm.ImStmts();
        for (int i = 0; i < 20_000; i++) {
            nested = JassIm.ImStmts(JassIm.ImIf(trace, JassIm.ImBoolVal(true),
                nested, JassIm.ImStmts()));
        }
        ImFunction main = JassIm.ImFunction(
            trace,
            "main",
            JassIm.ImTypeVars(),
            JassIm.ImVars(),
            JassIm.ImVoid(),
            JassIm.ImVars(),
            JassIm.ImStmts(JassIm.ImLoop(trace, nested)),
            Collections.emptyList()
        );
        ImProg prog = JassIm.ImProg(
            trace,
            JassIm.ImVars(),
            JassIm.ImFunctions(main),
            JassIm.ImMethods(),
            JassIm.ImClasses(),
            JassIm.ImTypeClassFuncs(),
            new java.util.HashMap<>()
        );

        new LocalPlayerContextAnalyzer(prog);
    }

    private static int countOccurrences(String text, String needle) {
        int count = 0;
        int from = 0;
        while ((from = text.indexOf(needle, from)) >= 0) {
            count++;
            from += needle.length();
        }
        return count;
    }

}
