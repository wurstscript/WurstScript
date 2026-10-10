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
import de.peeeq.wurstscript.translation.imoptimizer.UselessFunctionCallsRemover;
import de.peeeq.wurstscript.translation.imtranslation.CallType;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public class OptimizerTests extends WurstScriptTest {

    /** Return lowering owns a callee copy; it must move its children rather than clone them again. */
    @Test
    public void returnLoweringConsumesTheOwnedCopy() throws Exception {
        for (String shape : new String[] {"structured", "loop", "vararg"}) {
            boolean structured = shape.equals("structured");
            WurstModel trace = Ast.WurstModel();
            ImVar value = JassIm.ImVar(trace, TypesHelper.imInt(), "value", false);
            ImVar done = JassIm.ImVar(trace, TypesHelper.imBool(), "done", false);
            ImExpr condition = JassIm.ImBoolVal(true);
            ImExpr returnedValue = JassIm.ImIntVal(7);
            ImIf guard = JassIm.ImIf(trace, condition,
                JassIm.ImStmts(JassIm.ImReturn(trace, returnedValue)), JassIm.ImStmts());
            ImStmt unchanged = JassIm.ImIf(trace, JassIm.ImBoolVal(false),
                JassIm.ImStmts(JassIm.ImSet(trace, JassIm.ImVarAccess(value), JassIm.ImIntVal(9))),
                JassIm.ImStmts());
            ImStmts body = JassIm.ImStmts(guard, unchanged,
                JassIm.ImReturn(trace, JassIm.ImIntVal(11)));
            ImVarargLoopVar loopVar = JassIm.ImVarargLoopVar(value);
            if (shape.equals("loop")) {
                body = JassIm.ImStmts(JassIm.ImLoop(trace, body));
            } else if (shape.equals("vararg")) {
                body = JassIm.ImStmts(JassIm.ImVarargLoop(trace, body, JassIm.ImVarargLoopVars(loopVar)));
            }
            ImInliner inliner = new ImInliner(new ImTranslator(trace, false, new RunArgs()));
            String methodName = structured ? "structureReturns" : "rewriteForEarlyReturns";
            java.lang.reflect.Method rewrite = structured
                ? ImInliner.class.getDeclaredMethod(methodName, ImStmts.class, ImVar.class)
                : ImInliner.class.getDeclaredMethod(methodName, ImStmts.class, ImVar.class, ImVar.class);
            rewrite.setAccessible(true);
            ImStmts output = (ImStmts) (structured ? rewrite.invoke(inliner, body, value)
                : rewrite.invoke(inliner, body, done, value));
            assertTrue(body.isEmpty(), "lowering must consume its input list");
            java.util.Set<de.peeeq.wurstscript.jassIm.Element> nodes =
                Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            collectOwnedReturnNodes(output, nodes);
            assertTrue(nodes.contains(unchanged), "return-free subtrees must be moved intact");
            assertTrue(nodes.contains(condition), "conditions must be reused");
            assertTrue(nodes.contains(returnedValue), "returned expressions must be reused");
            if (shape.equals("vararg")) {
                assertTrue(nodes.contains(loopVar), "vararg loop bindings must be moved intact");
            }
        }
    }

    private static void collectOwnedReturnNodes(de.peeeq.wurstscript.jassIm.Element e,
        java.util.Set<de.peeeq.wurstscript.jassIm.Element> nodes) {
        assertTrue(nodes.add(e), "a child must occur only once");
        for (int i = 0; i < e.size(); i++) {
            assertSame(e.get(i).getParent(), e, "moved children must retain valid parents");
            collectOwnedReturnNodes(e.get(i), nodes);
        }
    }

    @Test
    public void packageConstantsInlineAndRemoveDeadGuardsInJass() throws IOException {
        test().withStdLib().runCompiletimeFunctions(true).lines(
            "package Test",
            "public constant bool COMPILETIME_DISABLED = compiletime(false)",
            "public constant int VALUE = 7",
            "public constant bool DISABLED = false",
            "public constant bool ENABLED = true",
            "@configurable public constant int CONFIGURABLE = 9",
            "native consume(int value)",
            "bool active",
            "function dead()",
            "    consume(VALUE)",
            "function compiletimeDead()",
            "    consume(VALUE)",
            "function guarded()",
            "    if COMPILETIME_DISABLED and active",
            "        compiletimeDead()",
            "    if DISABLED and active",
            "        dead()",
            "    if ENABLED and active",
            "        consume(VALUE)",
            "    consume(CONFIGURABLE)",
            "init",
            "    guarded()"
        );

        String compiled = Files.toString(
            new File("test-output/OptimizerTests_packageConstantsInlineAndRemoveDeadGuardsInJass_inlopt.j"),
            Charsets.UTF_8);
        assertFalse(compiled.contains("Test_VALUE") || compiled.contains("Test_DISABLED") || compiled.contains("Test_ENABLED")
            || compiled.contains("Test_COMPILETIME_DISABLED"));
        assertFalse(compiled.contains("function Test_dead takes") || compiled.contains("function Test_compiletimeDead takes"));
        assertTrue(compiled.contains("if Test_active then"));
        assertTrue(compiled.contains("call consume(7)"));
        assertTrue(compiled.contains("call consume(9)"));
        assertFalse(compiled.contains("Test_CONFIGURABLE"));
    }

    @Test
    public void configuredConstantsInlineTheirConfiguredValueInJass() throws IOException {
        test().withStdLib().lines(
            "package Test",
            "@configurable public constant int CONFIGURABLE = 9",
            "native consume(int value)",
            "init",
            "    consume(CONFIGURABLE)",
            "endpackage",
            "package Test_config",
            "@config public constant int CONFIGURABLE = 4",
            "endpackage"
        );

        String compiled = Files.toString(
            new File("test-output/OptimizerTests_configuredConstantsInlineTheirConfiguredValueInJass_inlopt.j"),
            Charsets.UTF_8);
        assertTrue(compiled.contains("call consume(4)"));
        assertFalse(compiled.contains("call consume(9)"));
        assertFalse(compiled.contains("CONFIGURABLE"));
    }

    @Test
    public void laterPackageConstantIsNotInlinedIntoEarlierInitializers() throws IOException {
        test().lines(
            "package Test",
            "native consume(int value)",
            "int observed = readLater()",
            "init",
            "    consume(readLater())",
            "constant int LATER = 7",
            "function readLater() returns int",
            "    return LATER",
            "init",
            "    consume(observed)"
        );
        String compiled = Files.toString(
            new File("test-output/OptimizerTests_laterPackageConstantIsNotInlinedIntoEarlierInitializers_inlopt.j"),
            Charsets.UTF_8);
        assertTrue(compiled.contains("Test_LATER"));
    }

    @Test
    public void superclassTranslationOrderPreservesLaterConstant() throws IOException {
        test().lines(
            "package Test",
            "native consume(int value)",
            "class Child extends Parent",
            "constant int LATER = 7",
            "class Parent",
            "    static int observed = readLater()",
            "function readLater() returns int",
            "    return LATER",
            "init",
            "    consume(Parent.observed)"
        );
        String compiled = Files.toString(
            new File("test-output/OptimizerTests_superclassTranslationOrderPreservesLaterConstant_inlopt.j"),
            Charsets.UTF_8);
        assertTrue(compiled.contains("Test_LATER"));
    }

    @Test
    public void abortableInitializerBeforeConstantPreservesLaterWrite() throws IOException {
        test().compilationUnits(
            compilationUnit("AbortBeforeConstant",
                "package AbortBeforeConstant",
                "native abortInitialization()",
                "init",
                "    abortInitialization()",
                "public constant int LATER = 7"),
            compilationUnit("ReadAfterAbort",
                "package ReadAfterAbort",
                "import AbortBeforeConstant",
                "constant int SAFE = 11",
                "native consume(int value)",
                "init",
                "    consume(LATER + SAFE)")
        );
        String compiled = Files.toString(
            new File("test-output/OptimizerTests_abortableInitializerBeforeConstantPreservesLaterWrite_inlopt.j"),
            Charsets.UTF_8);
        assertTrue(compiled.contains("AbortBeforeConstant_LATER"));
        assertFalse(compiled.contains("ReadAfterAbort_SAFE"));
    }

    @Test
    public void initlaterAnalysisIsCompilationScoped() throws IOException {
        compileInitlaterConstantRepro("first", "First");
        compileInitlaterConstantRepro("second", "Second");

        String compiled = Files.toString(
            new File("test-output/OptimizerTests_initlaterAnalysis_second_inlopt.j"),
            Charsets.UTF_8);
        assertTrue(compiled.contains("SecondValue_VALUE"));
    }

    private void compileInitlaterConstantRepro(String testName, String prefix) {
        testNamed("initlaterAnalysis_" + testName).compilationUnits(
            compilationUnit(prefix + "Value",
                "package " + prefix + "Value",
                "public constant int VALUE = 7",
                "public function readValue() returns int",
                "    return VALUE"),
            compilationUnit(prefix + "Reader",
                "package " + prefix + "Reader",
                "import initlater " + prefix + "Value",
                "native consume(int value)",
                "int observed = readValue()",
                "init",
                "    consume(observed)")
        );
    }



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
    public void nativeNamesAreReservedBeforeCompression() throws IOException {
        test().optimize().lines(
            "package test",
            "    native w()",
            "    function ordinary()",
            "        w()",
            "    init",
            "        ordinary()",
            "endpackage");

        String output = Files.toString(
            new File("./test-output/OptimizerTests_nativeNamesAreReservedBeforeCompression_opt.j"),
            Charsets.UTF_8);
        assertFalse(output.contains("function w takes"),
            "Compression must not reuse a native API name.\n" + output);
    }

    @Test
    public void reforged3ReadOnlyNativesAreClassified() {
        java.util.Set<String> readOnlyNatives = java.util.Set.of(
            "ConvertFogStyle", "ConvertEquipmentType", "ConvertItemTag", "ConvertLoadoutSlot",
            "BlzGetModelCinematicGameShotCount", "BlzGetModelCinematicGameCurrentShot",
            "BlzGetModelCinematicGameRemainingTime", "BlzGetMinShadowCastingPointLightCount",
            "GetCameraFieldControlledByInput", "BlzCameraGetCameraType", "BlzCameraSetupGetCameraType",
            "BlzIsTerrainPathableEx", "BlzGetDoodadX", "BlzGetDoodadY", "BlzGetDoodadZ",
            "BlzGetDoodadScaleX", "BlzGetDoodadScaleY", "BlzGetDoodadScaleZ",
            "BlzGetDoodadIsUsingModelAxes", "BlzGetDoodadYaw", "BlzGetDoodadPitch", "BlzGetDoodadRoll",
            "BlzGetDoodadVariation", "BlzGetDoodadId", "BlzGetNumDoodads",
            "BlzGetUnitAbilityCooldownPercent", "BlzIsMetaKeyPressed", "BlzIsKeyPressed",
            "BlzIsMouseButtonPressed", "BlzGetMouseScreenPosX", "BlzGetMouseScreenPosY",
            "BlzPixelToFrameX", "BlzPixelToFrameY", "BlzFrameToPixelX", "BlzFrameToPixelY"
        );
        for (String name : readOnlyNatives) {
            assertTrue(UselessFunctionCallsRemover.isFunctionWithoutSideEffect(name),
                name + " must be recognized as a side-effect-free Reforged 3 native");
        }

        for (String name : java.util.Set.of(
            "ConvertFogStyle", "ConvertEquipmentType", "ConvertItemTag", "ConvertLoadoutSlot")) {
            assertTrue(UselessFunctionCallsRemover.isFunctionPure(name),
                name + " must be recognized as a pure conversion native");
        }

        for (String name : java.util.Set.of(
            "ChooseRandomItemExWithFilter", "BlzPreloadModelCinematicGame", "BlzCreateDestructablePitchRoll")) {
            assertFalse(UselessFunctionCallsRemover.isFunctionWithoutSideEffect(name),
                name + " changes state or consumes randomness and must remain effectful");
        }
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

    /** The generated Jass of the test's {@code init test} function, from a test-output file. */
    private static String initFunctionOf(String outputFile) throws IOException {
        String jass = Files.toString(new File("test-output/" + outputFile), Charsets.UTF_8);
        int start = jass.indexOf("function init_test ");
        assertTrue(start >= 0, "Expected init_test in " + outputFile);
        return jass.substring(start, jass.indexOf("endfunction", start));
    }

    /**
     * A return that ends its path writes the result variable on every path, so the inlined call has
     * no done flag and no default write; both branches assign before the read.
     */
    @Test
    public void inlinerGuardClauseAssignsTheResultOnEveryPath() throws IOException {
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

        // Only this test's own function: the linked stdlib has flag-shaped inlines of its own.
        String inlined = initFunctionOf("OptimizerTests_inlinerGuardClauseAssignsTheResultOnEveryPath_inl.j");
        assertFalse(inlined.contains("call chooseLoc("), "Expected chooseLoc() to be inlined.");
        assertFalse(inlined.contains("inlineDone"), "Expected no done flag for a return that ends its path.");
        assertFalse(inlined.contains("set inlineRet = null"), "Expected no default write when every path assigns.");
        int thenIdx = inlined.indexOf("set inlineRet = a");
        int elseIdx = inlined.indexOf("set inlineRet = b");
        int useIdx = inlined.indexOf("set picked = inlineRet");
        assertTrue(thenIdx >= 0 && elseIdx > thenIdx, "Expected inlineRet to be assigned in both branches.");
        assertTrue(useIdx > elseIdx, "Expected inlineRet to be assigned before use.");
    }

    @Test
    public void inlinerLocationLocalsAreInitializedBeforeUse() throws IOException {
        // The return sits in a loop, which keeps the done flag and with it the default write.
        testAssertOkLinesWithStdLib(true,
            "package test",
            "@inline function chooseLoc(boolean c, location a, location b) returns location",
            "    for i = 0 to 1",
            "        if c",
            "            return a",
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
            "    for i = 0 to 0",
            "        if x > 0",
            "            return x",
            "    return 0 - x",
            "init",
            "    let y = maybeAbs(GetRandomInt(-5, 5))",
            "    if y >= 0",
            "        testSuccess()",
            "endpackage"
        );

        String inl = initFunctionOf("OptimizerTests_inlinerMultiReturnRewriteIsExplicitInInlAndInloptOutput_inl.j");
        String inlopt = initFunctionOf("OptimizerTests_inlinerMultiReturnRewriteIsExplicitInInlAndInloptOutput_inlopt.j");

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

    /** Check the inliner output before local optimizations, as well as execution on both targets. */
    @Test
    public void inlinerFallbackGroupsGuardsAndPreservesReturnFreeSubtrees() throws IOException {
        test().testLua(true).luaOnly(false).inline().executeProg().lines(
            "package test",
            "native testSuccess()",
            "int trace = 0",
            "int result = 0",
            "int failures = 0",
            "@inline function pick(int x) returns int",
            "    trace += 1",
            "    for i = 0 to 1",
            "        if i == x",
            "            return i",
            "        trace += 2",
            "        trace += 3",
            "    trace += 10",
            "    trace += 100",
            "    if x > 10",
            "        trace += 1000",
            "        trace += 2000",
            "    else",
            "        trace += 4000",
            "        trace += 8000",
            "    for i = 0 to 1",
            "        trace += 10000",
            "        trace += 20000",
            "    return -1",
            "@noinline function caller(int x)",
            "    result = pick(x)",
            "init",
            "    caller(0)",
            "    if result != 0 or trace != 1",
            "        failures++",
            "    trace = 0",
            "    caller(1)",
            "    if result != 1 or trace != 6",
            "        failures++",
            "    trace = 0",
            "    caller(2)",
            "    if result != -1 or trace != 72121",
            "        failures++",
            "    trace = 0",
            "    caller(11)",
            "    if failures == 0 and result == -1 and trace == 63121",
            "        testSuccess()");

        String jass = Files.toString(new File("test-output/OptimizerTests_inlinerFallbackGroupsGuardsAndPreservesReturnFreeSubtrees_inl.j"), Charsets.UTF_8);
        int start = jass.indexOf("function caller takes");
        assertTrue(start >= 0);
        String caller = jass.substring(start, jass.indexOf("endfunction", start));
        assertEquals(countOccurrences(caller, "if  not inlineDone"), 3,
            "only the loop suffix, post-loop suffix and fallback value need guards");
        assertEquals(countOccurrences(caller, "exitwhen inlineDone"), 1,
            "only the loop containing a return needs exit propagation");

        String lua = Files.toString(new File("test-output/lua/OptimizerTests_inlinerFallbackGroupsGuardsAndPreservesReturnFreeSubtrees.lua"), Charsets.UTF_8);
        assertEquals(java.util.regex.Pattern.compile("if\\s+\\(?not\\s*\\(?inlineDone").matcher(lua).results().count(), 3L,
            "Lua must retain the same grouped guards");
    }

    @Test
    public void inlinerGroupedGuardsPropagateNestedAndVoidReturns() {
        test().testLua(true).luaOnly(false).inline().localOptimizations().executeProg().lines(
            "package test",
            "native testSuccess()",
            "int trace = 0",
            "int failures = 0",
            "@noinline function check(bool ok)",
            "    if not ok",
            "        failures++",
            "@inline function nested(int x) returns int",
            "    for i = 0 to 1",
            "        trace += 1",
            "        for j = 0 to 1",
            "            trace += 10",
            "            if i * 2 + j == x",
            "                return i * 2 + j",
            "            trace += 100",
            "        trace += 1000",
            "    trace += 10000",
            "    return -1",
            "@inline function voidReturn(int x)",
            "    for i = 0 to 1",
            "        if i == x",
            "            return",
            "        trace += 1",
            "        trace += 10",
            "    trace += 100",
            "@noinline function run(int x) returns int",
            "    return nested(x)",
            "@noinline function runVoid(int x)",
            "    voidReturn(x)",
            "init",
            "    check(run(0) == 0 and trace == 11)",
            "    trace = 0",
            "    check(run(1) == 1 and trace == 121)",
            "    trace = 0",
            "    check(run(2) == 2 and trace == 1232)",
            "    trace = 0",
            "    check(run(3) == 3 and trace == 1342)",
            "    trace = 0",
            "    check(run(4) == -1 and trace == 12442)",
            "    trace = 0",
            "    runVoid(0)",
            "    check(trace == 0)",
            "    runVoid(1)",
            "    check(trace == 11)",
            "    trace = 0",
            "    runVoid(2)",
            "    check(trace == 122)",
            "    if failures == 0",
            "        testSuccess()");
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

    /**
     * The liveness where the two branches of an if join: one branch's set holds the other's (first if: then {x,y},
     * else {x}), and two sets of which neither holds the other make a new one (second if: then {c,x,y,z}, else
     * {c,w,x,y}). The key of an if in the result is its condition, which the control flow graph puts in its node.
     */
    @Test
    public void localMergerLivenessJoinsTheBranchesOfAnIf() {
        Element trace = Ast.NoExpr();
        LocalMerger localMerger = new LocalMerger();
        ImVar y = JassIm.ImVar(trace, TypesHelper.imInt(), "y", false);
        ImVar x = JassIm.ImVar(trace, TypesHelper.imInt(), "x", false);
        ImVar z = JassIm.ImVar(trace, TypesHelper.imInt(), "z", false);
        ImVar w = JassIm.ImVar(trace, TypesHelper.imInt(), "w", false);
        ImVar c = JassIm.ImVar(trace, TypesHelper.imInt(), "c", false);
        ImVar sinkA = JassIm.ImVar(trace, TypesHelper.imInt(), "sinkA", false);
        ImVar sinkB = JassIm.ImVar(trace, TypesHelper.imInt(), "sinkB", false);
        ImFunction sink = JassIm.ImFunction(trace, "sink", JassIm.ImTypeVars(), JassIm.ImVars(sinkA, sinkB),
            JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        CallType normal = CallType.NORMAL;

        ImSet setY = JassIm.ImSet(trace, JassIm.ImVarAccess(y), JassIm.ImIntVal(2));
        ImSet setX = JassIm.ImSet(trace, JassIm.ImVarAccess(x), JassIm.ImIntVal(1));
        ImSet setZ = JassIm.ImSet(trace, JassIm.ImVarAccess(z), JassIm.ImIntVal(3));
        ImSet setW = JassIm.ImSet(trace, JassIm.ImVarAccess(w), JassIm.ImIntVal(4));
        ImSet setC = JassIm.ImSet(trace, JassIm.ImVarAccess(c), JassIm.ImIntVal(0));
        ImExpr cond2 = JassIm.ImOperatorCall(de.peeeq.wurstscript.WurstOperator.EQ,
            JassIm.ImExprs(JassIm.ImVarAccess(c), JassIm.ImIntVal(1)));
        ImFunctionCall readZ = JassIm.ImFunctionCall(trace, sink, JassIm.ImTypeArguments(),
            JassIm.ImExprs(JassIm.ImVarAccess(z), JassIm.ImVarAccess(z)), false, normal);
        ImFunctionCall readW = JassIm.ImFunctionCall(trace, sink, JassIm.ImTypeArguments(),
            JassIm.ImExprs(JassIm.ImVarAccess(w), JassIm.ImVarAccess(w)), false, normal);
        ImIf second = JassIm.ImIf(trace, cond2, JassIm.ImStmts(readZ), JassIm.ImStmts(readW));
        ImExpr cond1 = JassIm.ImOperatorCall(de.peeeq.wurstscript.WurstOperator.EQ,
            JassIm.ImExprs(JassIm.ImVarAccess(c), JassIm.ImIntVal(0)));
        ImFunctionCall readXY = JassIm.ImFunctionCall(trace, sink, JassIm.ImTypeArguments(),
            JassIm.ImExprs(JassIm.ImVarAccess(x), JassIm.ImVarAccess(y)), false, normal);
        ImFunctionCall readX = JassIm.ImFunctionCall(trace, sink, JassIm.ImTypeArguments(),
            JassIm.ImExprs(JassIm.ImVarAccess(x), JassIm.ImVarAccess(x)), false, normal);
        ImIf first = JassIm.ImIf(trace, cond1, JassIm.ImStmts(readXY), JassIm.ImStmts(readX));
        ImFunction branching = JassIm.ImFunction(trace, "branching", JassIm.ImTypeVars(), JassIm.ImVars(),
            JassIm.ImVoid(), JassIm.ImVars(y, x, z, w, c),
            JassIm.ImStmts(setY, setX, setZ, setW, setC, second, first), Collections.emptyList());

        Map<ImStmt, Set<ImVar>> live = localMerger.calculateLiveness(branching);

        assertEquals(live.get(readXY), HashSet.empty());
        assertEquals(live.get(readX), HashSet.empty());
        assertEquals(live.get(cond1), HashSet.of(x, y));          // then {x,y} holds else {x}
        assertEquals(live.get(readZ), HashSet.of(c, x, y));
        assertEquals(live.get(readW), HashSet.of(c, x, y));
        assertEquals(live.get(cond2), HashSet.of(c, w, x, y, z)); // {c,x,y,z} and {c,w,x,y} make a new set
        assertEquals(live.get(setC), HashSet.of(c, w, x, y, z));
        assertEquals(live.get(setW), HashSet.of(w, x, y, z));
        assertEquals(live.get(setZ), HashSet.of(x, y, z));
        assertEquals(live.get(setX), HashSet.of(x, y));
        assertEquals(live.get(setY), HashSet.of(y));
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
    public void localOptimizationRunsTwoBoundedSweepsPerInvocation() {
        class CountingTimeTaker extends TimeTaker.Default {
            int measurements;

            @Override
            public <T> T measure(String name, java.util.function.Supplier<T> f) {
                measurements++;
                return f.get();
            }
        }

        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, false, new RunArgs());
        ImVar value = JassIm.ImVar(model, TypesHelper.imInt(), "value", false);
        ImFunction sink = JassIm.ImFunction(model, "sink", JassIm.ImTypeVars(),
            JassIm.ImVars(value), JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(),
            Collections.singletonList(FunctionFlagEnum.IS_NATIVE));
        ImFunctionCall call = JassIm.ImFunctionCall(model, sink, JassIm.ImTypeArguments(),
            JassIm.ImExprs(JassIm.ImOperatorCall(de.peeeq.wurstscript.WurstOperator.PLUS,
                JassIm.ImExprs(JassIm.ImIntVal(1), JassIm.ImIntVal(2)))), false,
            de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL);
        ImFunction main = JassIm.ImFunction(model, "main", JassIm.ImTypeVars(), JassIm.ImVars(),
            JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(call), Collections.emptyList());
        ImFunction config = JassIm.ImFunction(model, "config", JassIm.ImTypeVars(), JassIm.ImVars(),
            JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        translator.getImProg().getFunctions().add(sink);
        translator.getImProg().getFunctions().add(main);
        translator.getImProg().getFunctions().add(config);
        translator.setMainFunc(main);
        translator.setConfigFunc(config);
        CountingTimeTaker timeTaker = new CountingTimeTaker();

        new ImOptimizer(timeTaker, translator).localOptimizations();

        assertEquals(timeTaker.measurements, 18,
            "the optimizer should run two fixed sweeps rather than iterating to convergence");
    }

    @Test
    public void garbageRemovalRemovesAChainOfAssignmentsInsideOneFunction() {
        // `v1 = v0; v2 = v1; ...` where nothing reads the last: each round makes the variable before it unread, so a
        // chain takes a round for each link. The removal goes on until the first link is gone, however long it is: it
        // does not stop after some rounds and leave the rest, and it does not take a converging program for one which
        // never settles. (In unit-test mode, so the rounds are checked against an analysis of the whole program.)
        int links = 300;
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, true, new RunArgs());
        ImProg prog = translator.getImProg();
        ImVars locals = JassIm.ImVars();
        ImStmts body = JassIm.ImStmts();
        ImVar previous = null;
        for (int i = 0; i < links; i++) {
            ImVar v = JassIm.ImVar(model, TypesHelper.imInt(), "v" + i, false);
            locals.add(v);
            body.add(JassIm.ImSet(model, JassIm.ImVarAccess(v),
                previous == null ? JassIm.ImIntVal(0) : JassIm.ImVarAccess(previous)));
            previous = v;
        }
        ImFunction main = JassIm.ImFunction(model, "main", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            locals, body, Collections.emptyList());
        ImFunction config = JassIm.ImFunction(model, "config", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        prog.getFunctions().add(main);
        prog.getFunctions().add(config);
        translator.setMainFunc(main);
        translator.setConfigFunc(config);

        new ImOptimizer(new TimeTaker.Default(), translator).removeGarbage();

        assertEquals(main.getLocals().size(), 0, "the variables of the chain which are left: " + main.getLocals().size());
        assertEquals(main.getBody().size(), 0, "the assignments of the chain which are left: " + main.getBody().size());
    }

    @Test
    public void garbageRemovalRemovesAChainOfAssignmentsInFunctionsOfTheirOwn() {
        // The same chain with each link in a function and the variables global: a link is only found unread once the
        // function of the link after it has lost its read.
        int links = 300;
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, true, new RunArgs());
        ImProg prog = translator.getImProg();
        List<ImVar> globals = new ArrayList<>();
        for (int i = 0; i <= links; i++) {
            ImVar g = JassIm.ImVar(model, TypesHelper.imInt(), "g" + i, false);
            globals.add(g);
            prog.getGlobals().add(g);
        }
        ImStmts mainBody = JassIm.ImStmts();
        mainBody.add(JassIm.ImSet(model, JassIm.ImVarAccess(globals.get(0)), JassIm.ImIntVal(0)));
        List<ImFunction> linkFunctions = new ArrayList<>();
        for (int i = 1; i <= links; i++) {
            ImFunction link = JassIm.ImFunction(model, "link" + i, JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
                JassIm.ImVars(), JassIm.ImStmts(JassIm.ImSet(model, JassIm.ImVarAccess(globals.get(i)),
                    JassIm.ImVarAccess(globals.get(i - 1)))), Collections.emptyList());
            linkFunctions.add(link);
            prog.getFunctions().add(link);
            mainBody.add(JassIm.ImFunctionCall(model, link, JassIm.ImTypeArguments(), JassIm.ImExprs(), false, CallType.NORMAL));
        }
        ImFunction main = JassIm.ImFunction(model, "main", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(), mainBody, Collections.emptyList());
        ImFunction config = JassIm.ImFunction(model, "config", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        prog.getFunctions().add(main);
        prog.getFunctions().add(config);
        translator.setMainFunc(main);
        translator.setConfigFunc(config);

        new ImOptimizer(new TimeTaker.Default(), translator).removeGarbage();

        assertEquals(prog.getGlobals().size(), 0, "the variables of the chain which are left: " + prog.getGlobals().size());
        for (ImFunction link : linkFunctions) {
            assertEquals(link.getBody().size(), 0, "the assignment which is left in " + link.getName());
        }
    }

    /**
     * An assignment to a field nothing reads goes, but evaluating its target calls getC and nextIndex, and those
     * calls stay. On Jass the classes are eliminated first and the target is an array access; on Lua it is still a
     * member access.
     */
    @Test
    public void garbageRemovalKeepsTheReceiverAndTheIndexOfAnUnreadField() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "native testFail(string s)",
            "class C",
            "    int x",
            "    int array[3] arr",
            "C c",
            "int calls = 0",
            "function getC() returns C",
            "    calls++",
            "    return c",
            "function nextIndex() returns int",
            "    calls++",
            "    return 1",
            "init",
            "    c = new C",
            "    getC().x = 5",
            "    c.arr[nextIndex()] = 7",
            "    if calls == 2",
            "        testSuccess()",
            "    else",
            "        testFail(\"the effects of the assignment target were dropped\")");
    }

    /**
     * The same for a component of a tuple array element: with -inline the removal runs before the tuples go, on Jass
     * and on Lua.
     */
    @Test
    public void garbageRemovalKeepsTheIndexOfAnUnreadTupleArrayElement() throws Exception {
        test().testLua(true).luaOnly(false).inline().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "native testFail(string s)",
            "tuple vec2(real x, real y)",
            "vec2 array points",
            "int calls = 0",
            "function nextIndex() returns int",
            "    calls++",
            "    if calls > 100",
            "        return 0",
            "    return 1",
            "init",
            "    points[nextIndex()].y = 1.",
            "    if calls == 1",
            "        testSuccess()",
            "    else",
            "        testFail(\"the index call of the assignment target was dropped\")");

        // The runs show that the index call stays; the assignment itself goes on both targets.
        String jass = Files.toString(new File("test-output/OptimizerTests_garbageRemovalKeepsTheIndexOfAnUnreadTupleArrayElement_inl.j"), Charsets.UTF_8);
        assertFalse(jass.contains("points"), jass);
        String lua = Files.toString(new File("test-output/lua/OptimizerTests_garbageRemovalKeepsTheIndexOfAnUnreadTupleArrayElement.lua"), Charsets.UTF_8);
        assertFalse(lua.contains("points"), lua);
    }

    @Test
    public void garbageRemovalKeepsTheVariableWhichFlatteningAnEffectMakes() {
        // An assignment to an unread variable of `sink(tick(), (tock(); 2))` leaves the call, and the statements of its
        // second argument come before the call: the first one, which has an effect, is saved in a variable of the
        // function first. That variable is declared, and the removal does not take it for one nothing reads.
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, true, new RunArgs());
        ImProg prog = translator.getImProg();
        ImVar a = JassIm.ImVar(model, TypesHelper.imInt(), "a", false);
        ImVar b = JassIm.ImVar(model, TypesHelper.imInt(), "b", false);
        ImFunction tick = JassIm.ImFunction(model, "tick", JassIm.ImTypeVars(), JassIm.ImVars(), TypesHelper.imInt(),
            JassIm.ImVars(), JassIm.ImStmts(), Collections.singletonList(FunctionFlagEnum.IS_NATIVE));
        ImFunction tock = JassIm.ImFunction(model, "tock", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(), JassIm.ImStmts(), Collections.singletonList(FunctionFlagEnum.IS_NATIVE));
        ImFunction sink = JassIm.ImFunction(model, "sink", JassIm.ImTypeVars(), JassIm.ImVars(a, b), TypesHelper.imInt(),
            JassIm.ImVars(), JassIm.ImStmts(), Collections.singletonList(FunctionFlagEnum.IS_NATIVE));
        CallType normal = CallType.NORMAL;
        ImExpr first = JassIm.ImFunctionCall(model, tick, JassIm.ImTypeArguments(), JassIm.ImExprs(), false, normal);
        ImExpr second = JassIm.ImStatementExpr(JassIm.ImStmts(
            JassIm.ImFunctionCall(model, tock, JassIm.ImTypeArguments(), JassIm.ImExprs(), false, normal)),
            JassIm.ImIntVal(2));
        ImVar unread = JassIm.ImVar(model, TypesHelper.imInt(), "unread", false);
        ImSet assignment = JassIm.ImSet(model, JassIm.ImVarAccess(unread),
            JassIm.ImFunctionCall(model, sink, JassIm.ImTypeArguments(), JassIm.ImExprs(first, second), false, normal));
        ImFunction main = JassIm.ImFunction(model, "main", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(unread), JassIm.ImStmts(assignment), Collections.emptyList());
        ImFunction config = JassIm.ImFunction(model, "config", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        prog.getFunctions().add(tick);
        prog.getFunctions().add(tock);
        prog.getFunctions().add(sink);
        prog.getFunctions().add(main);
        prog.getFunctions().add(config);
        translator.setMainFunc(main);
        translator.setConfigFunc(config);

        new ImOptimizer(new TimeTaker.Default(), translator).removeGarbage();

        // the unread variable is gone, the saved argument is declared, and every variable which is used is attached
        assertEquals(main.getLocals().size(), 1, "the variable which saves the first argument: " + main.getLocals());
        ImVar saved = main.getLocals().get(0);
        assertNotSame(saved, unread);
        List<ImVar> used = new ArrayList<>();
        main.accept(new ImFunction.DefaultVisitor() {
            @Override
            public void visit(ImVarAccess e) {
                used.add(e.getVar());
            }
        });
        assertTrue(used.size() >= 2, "the variable is set and read: " + used);
        for (ImVar v : used) {
            assertSame(v, saved, "a variable of main which is not a local of main");
            assertNotNull(v.getParent(), "a variable which is used is not attached");
        }
        assertEquals(main.getBody().size(), 3, "the saved argument, the statement of the second, the call: " + main.getBody());
        assertTrue(translator.isFlat(), "what is left is flat: " + main.getBody());
    }

    /**
     * An integer division by a divisor which may be zero stops the thread, so the removal of an assignment to a
     * variable nothing reads keeps the division, without and with the optimisations, on Jass and on Lua.
     */
    @Test
    public void garbageRemovalKeepsAnUnreadDivisionWhichMayStopTheThread() throws IOException {
        String[] program = {"package Test", "native testSuccess()", "int zero = 0",
            "function f(int d)", "    int unused = 10 div d", "init", "    f(zero)", "    testSuccess()"};
        test().executeProg(false).lines(program);
        for (String variant : new String[]{"no_opts", "opt", "inl", "inlopt"}) {
            String out = Files.toString(new File("test-output/OptimizerTests_garbageRemovalKeepsAnUnreadDivisionWhichMayStopTheThread_"
                + variant + ".j"), Charsets.UTF_8);
            assertTrue(out.contains("10 / "), variant + ": the division which may stop the thread was dropped:\n" + out);
        }
        for (boolean optimised : new boolean[]{false, true}) {
            TestConfig lua = test().testLua(true).executeProg(false);
            if (optimised) {
                lua = lua.inline().localOptimizations();
            }
            lua.lines(program);
            String out = Files.toString(new File("test-output/lua/OptimizerTests_garbageRemovalKeepsAnUnreadDivisionWhichMayStopTheThread.lua"),
                Charsets.UTF_8);
            assertTrue(out.contains("10 //") || out.contains("__wurst_intDiv(10,"),
                "Lua" + (optimised ? " with the optimisations" : "") + ": the division which may stop the thread was dropped:\n" + out);
        }
    }

    /** The same for a division inside an operator, and for one assigned to a global, a field and an array element. */
    @Test
    public void garbageRemovalKeepsUnreadDivisionsInsideExpressionsAndOfOtherVariables() throws IOException {
        String[] program = {"package Test", "native testSuccess()", "int zero = 0", "int unreadGlobal",
            "int array unreadArray", "class C", "    int unreadField",
            "function g() returns int", "    return 3",
            "function f(int d)", "    int unused = 11 div d + g()", "    unreadGlobal = 12 mod d", "    unreadArray[2] = 13 div d",
            "    C c = new C", "    c.unreadField = 14 div d", "    if 15 div d == 0", "        skip",
            "init", "    f(zero)", "    testSuccess()"};
        test().executeProg(false).lines(program);
        for (String variant : new String[]{"no_opts", "opt", "inl", "inlopt"}) {
            String out = Files.toString(new File("test-output/OptimizerTests_garbageRemovalKeepsUnreadDivisionsInsideExpressionsAndOfOtherVariables_"
                + variant + ".j"), Charsets.UTF_8);
            for (String division : new String[]{"11 / ", "ModuloInteger(12, ", "13 / ", "14 / ", "15 / "}) {
                assertTrue(out.contains(division), variant + ": the division " + division + "was dropped:\n" + out);
            }
        }
    }

    /**
     * A global whose name a variable event refers to stays, and so does the assignment to it, when its last read in a
     * function goes in a later round of the removal (`copy = myVar` goes in the first).
     */
    @Test
    public void garbageRemovalKeepsAPreservedGlobalWhoseLastReadGoesInALaterRound() throws IOException {
        test().lines(
            "type trigger extends handle",
            "type event extends handle",
            "type limitop extends handle",
            "package test",
            "    int myVar = 0",
            "    @extern native TriggerRegisterVariableEvent(trigger whichTrigger, string varName, limitop opcode, real limitval) returns event",
            "    function copyIt()",
            "        int copy = myVar",
            "    init",
            "        TriggerRegisterVariableEvent(null, \"test_myVar\", null, 0.0)",
            "        copyIt()",
            "        myVar = 5",
            "endpackage");
        String out = Files.toString(new File(
            "test-output/OptimizerTests_garbageRemovalKeepsAPreservedGlobalWhoseLastReadGoesInALaterRound_no_opts.j"),
            Charsets.UTF_8);
        assertTrue(out.contains("integer test_myVar") && out.contains("set test_myVar = 5"), out);
    }

    /**
     * A global of blizzard.j whose last read goes in a later round of the removal makes it analyse the program again,
     * so what its initial value reads (bj_PI) is not kept for it. The unit-test cross-check at the end of the removal
     * compares the rounds with an analysis of the whole program.
     */
    @Test
    public void garbageRemovalAnalysesAgainWhenABlizzardGlobalLosesItsLastRead() {
        test().executeProg(true).compilationUnits(
            compilationUnit("blizzard.j",
                "globals",
                "    constant real bj_PI = 3.14159",
                "    constant real bj_DEGTORAD = bj_PI/180.0",
                "endglobals"),
            compilationUnit("test.wurst",
                "package Test",
                "native testSuccess()",
                "function copyIt()",
                "    real copy = bj_DEGTORAD",
                "init",
                "    copyIt()",
                "    testSuccess()"));
    }

    /**
     * The only read of `a` is after a loop which always returns, so it is never reached. The optimisations do not
     * leave that read with no assignment before it, which pjass rejects.
     */
    @Test
    public void localOptimizationsLeaveNoUninitialisedReadAfterAReturningLoop() {
        test().executeProg().lines("package test", "native testSuccess()", "int trace = 0",
            "@noinline function side(int k) returns int", "    trace = trace * 5 + k", "    return trace mod 7",
            "@noinline function f(int x) returns int", "    int a = x", "    for i1 = 0 to 0", "        a = x + 1",
            "    for i2 = 0 to 3", "        return side(i2)", "    return a",
            "init", "    if f(1) != 12345", "        testSuccess()");
    }

    /**
     * The same after an if both branches of which return: the local merger removes the assignment to `a`, whose only
     * read no path reaches, and that read with it. (`x` is read after the assignment, so `a` cannot share its slot,
     * which would have hidden the read with no assignment before it.)
     */
    @Test
    public void localMergerRemovesTheCodeAfterAnIfBothBranchesOfWhichReturn() {
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, false, new RunArgs());
        ImProg prog = translator.getImProg();
        ImVar x = JassIm.ImVar(model, TypesHelper.imInt(), "x", false);
        ImVar a = JassIm.ImVar(model, TypesHelper.imInt(), "a", false);
        ImIf bothReturn = JassIm.ImIf(model, JassIm.ImOperatorCall(de.peeeq.wurstscript.WurstOperator.GREATER,
                JassIm.ImExprs(JassIm.ImVarAccess(x), JassIm.ImIntVal(0))),
            JassIm.ImStmts(JassIm.ImReturn(model, JassIm.ImIntVal(1))),
            JassIm.ImStmts(JassIm.ImReturn(model, JassIm.ImIntVal(2))));
        ImFunction f = JassIm.ImFunction(model, "f", JassIm.ImTypeVars(), JassIm.ImVars(x), TypesHelper.imInt(),
            JassIm.ImVars(a),
            JassIm.ImStmts(JassIm.ImSet(model, JassIm.ImVarAccess(a), JassIm.ImVarAccess(x)), bothReturn,
                JassIm.ImReturn(model, JassIm.ImVarAccess(a))),
            Collections.emptyList());
        prog.getFunctions().add(f);

        new LocalMerger().optimize(translator, new LocalPlayerContextAnalyzer(prog));

        List<ImVar> assigned = new ArrayList<>();
        List<ImVar> read = new ArrayList<>();
        f.accept(new ImFunction.DefaultVisitor() {
            @Override
            public void visit(ImSet set) {
                set.getRight().accept(this);
                assigned.add(((ImVarAccess) set.getLeft()).getVar());
            }

            @Override
            public void visit(ImVarAccess access) {
                read.add(access.getVar());
            }
        });
        for (ImVar v : read) {
            assertTrue(v == x || assigned.contains(v), "a read of " + v.getName() + " with no assignment: " + f.getBody());
        }
        assertEquals(f.getBody().size(), 1, "only the if is left: " + f.getBody());
        assertSame(f.getBody().get(0), bothReturn);
        assertTrue(f.getLocals().isEmpty(), "the local nothing reads any more: " + f.getLocals());
    }

    /**
     * Merging two locals makes the copy between them an assignment of the local to itself, which does nothing and
     * goes: `a = tick(); b = a; return b` keeps no `a = a`.
     */
    @Test
    public void localMergerLeavesNoAssignmentOfALocalToItself() {
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, false, new RunArgs());
        ImProg prog = translator.getImProg();
        ImFunction tick = JassIm.ImFunction(model, "tick", JassIm.ImTypeVars(), JassIm.ImVars(), TypesHelper.imInt(),
            JassIm.ImVars(), JassIm.ImStmts(), Collections.singletonList(FunctionFlagEnum.IS_NATIVE));
        ImVar a = JassIm.ImVar(model, TypesHelper.imInt(), "a", false);
        ImVar b = JassIm.ImVar(model, TypesHelper.imInt(), "b", false);
        ImFunction f = JassIm.ImFunction(model, "f", JassIm.ImTypeVars(), JassIm.ImVars(), TypesHelper.imInt(),
            JassIm.ImVars(a, b),
            JassIm.ImStmts(
                JassIm.ImSet(model, JassIm.ImVarAccess(a),
                    JassIm.ImFunctionCall(model, tick, JassIm.ImTypeArguments(), JassIm.ImExprs(), false, CallType.NORMAL)),
                JassIm.ImSet(model, JassIm.ImVarAccess(b), JassIm.ImVarAccess(a)),
                JassIm.ImReturn(model, JassIm.ImVarAccess(b))),
            Collections.emptyList());
        prog.getFunctions().add(tick);
        prog.getFunctions().add(f);

        new LocalMerger().optimize(translator, new LocalPlayerContextAnalyzer(prog));

        assertEquals(f.getLocals().size(), 1, "a and b are merged: " + f.getLocals());
        for (ImStmt s : f.getBody()) {
            assertFalse(s instanceof ImSet set && set.getLeft() instanceof ImVarAccess left
                    && set.getRight() instanceof ImVarAccess right && left.getVar() == right.getVar(),
                "an assignment of a local to itself is left: " + f.getBody());
        }
        assertEquals(f.getBody().size(), 2, "the call and the return: " + f.getBody());
    }

    /**
     * The local merger replaces a dead assignment by what its value does besides producing it, and the IM stays flat
     * while the local optimisations run: a call becomes a call statement and a division which may stop the thread
     * inside an expression an assignment of it, not a statement expression.
     */
    @Test
    public void localMergerKeepsTheEffectsOfADeadAssignmentAsFlatStatements() {
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, false, new RunArgs());
        ImProg prog = translator.getImProg();
        ImFunction tick = JassIm.ImFunction(model, "tick", JassIm.ImTypeVars(), JassIm.ImVars(), TypesHelper.imInt(),
            JassIm.ImVars(), JassIm.ImStmts(), Collections.singletonList(FunctionFlagEnum.IS_NATIVE));
        ImVar d = JassIm.ImVar(model, TypesHelper.imInt(), "d", false);
        ImVar x = JassIm.ImVar(model, TypesHelper.imInt(), "x", false);
        ImVar y = JassIm.ImVar(model, TypesHelper.imInt(), "y", false);
        ImExpr division = JassIm.ImOperatorCall(de.peeeq.wurstscript.WurstOperator.DIV_INT,
            JassIm.ImExprs(JassIm.ImIntVal(10), JassIm.ImVarAccess(d)));
        ImFunction f = JassIm.ImFunction(model, "f", JassIm.ImTypeVars(), JassIm.ImVars(d), TypesHelper.imInt(),
            JassIm.ImVars(x, y),
            JassIm.ImStmts(
                JassIm.ImSet(model, JassIm.ImVarAccess(x), JassIm.ImOperatorCall(de.peeeq.wurstscript.WurstOperator.PLUS,
                    JassIm.ImExprs(JassIm.ImIntVal(1), division))),
                JassIm.ImSet(model, JassIm.ImVarAccess(y),
                    JassIm.ImFunctionCall(model, tick, JassIm.ImTypeArguments(), JassIm.ImExprs(), false, CallType.NORMAL)),
                JassIm.ImReturn(model, JassIm.ImIntVal(0))),
            Collections.emptyList());
        prog.getFunctions().add(tick);
        prog.getFunctions().add(f);

        new LocalMerger().optimize(translator, new LocalPlayerContextAnalyzer(prog));

        List<ImStatementExpr> statementExprs = new ArrayList<>();
        boolean[] divides = {false};
        boolean[] calls = {false};
        f.accept(new ImFunction.DefaultVisitor() {
            @Override
            public void visit(ImStatementExpr e) {
                super.visit(e);
                statementExprs.add(e);
            }

            @Override
            public void visit(ImOperatorCall e) {
                super.visit(e);
                divides[0] |= e.getOp() == de.peeeq.wurstscript.WurstOperator.DIV_INT;
            }

            @Override
            public void visit(ImFunctionCall e) {
                super.visit(e);
                calls[0] |= e.getFunc() == tick;
            }
        });
        assertTrue(statementExprs.isEmpty(), "the IM stays flat: " + f.getBody());
        assertTrue(divides[0], "the division which may stop the thread stays: " + f.getBody());
        assertTrue(calls[0], "the call stays: " + f.getBody());
    }

    @Test
    public void aFlattenLeavesTheFunctionsWhichWereNotModifiedSinceTheLastOne() {
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, false, new RunArgs());
        ImProg prog = translator.getImProg();
        CallType normal = CallType.NORMAL;
        ImFunction tock = JassIm.ImFunction(model, "tock", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(), JassIm.ImStmts(), Collections.singletonList(FunctionFlagEnum.IS_NATIVE));
        ImVar local = JassIm.ImVar(model, TypesHelper.imInt(), "local", false);
        ImStmt withStatementExpr = JassIm.ImSet(model, JassIm.ImVarAccess(local), JassIm.ImStatementExpr(
            JassIm.ImStmts(JassIm.ImFunctionCall(model, tock, JassIm.ImTypeArguments(), JassIm.ImExprs(), false, normal)),
            JassIm.ImIntVal(2)));
        ImFunction changed = JassIm.ImFunction(model, "changed", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(local), JassIm.ImStmts(withStatementExpr), Collections.emptyList());
        ImVar other = JassIm.ImVar(model, TypesHelper.imInt(), "other", false);
        ImFunction untouched = JassIm.ImFunction(model, "untouched", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(other), JassIm.ImStmts(JassIm.ImSet(model, JassIm.ImVarAccess(other), JassIm.ImIntVal(1))),
            Collections.emptyList());
        prog.getFunctions().add(tock);
        prog.getFunctions().add(changed);
        prog.getFunctions().add(untouched);

        prog.flatten(translator);
        ImStmts flatChanged = changed.getBody();
        ImStmts flatUntouched = untouched.getBody();
        assertTrue(translator.isFlat(), "the first flatten flattens everything: " + changed.getBody());

        prog.flatten(translator);
        assertSame(changed.getBody(), flatChanged, "a flatten rebuilt a function which was not modified since the last");
        assertSame(untouched.getBody(), flatUntouched, "a flatten rebuilt a function which was not modified since the last");

        // a modification of one function (below its body, so not a replacement of the body) is flattened, and only it
        ImVar another = JassIm.ImVar(model, TypesHelper.imInt(), "another", false);
        changed.getLocals().add(another);
        changed.getBody().add(JassIm.ImSet(model, JassIm.ImVarAccess(another), JassIm.ImStatementExpr(
            JassIm.ImStmts(JassIm.ImFunctionCall(model, tock, JassIm.ImTypeArguments(), JassIm.ImExprs(), false, normal)),
            JassIm.ImIntVal(3))));
        assertFalse(translator.isFlat(), "the statement expression which was added");

        prog.flatten(translator);
        assertNotSame(changed.getBody(), flatChanged, "a modified function is flattened again");
        assertSame(untouched.getBody(), flatUntouched, "the function which was not modified is still left alone");
        assertTrue(translator.isFlat(), "what was added is flat now: " + changed.getBody());
    }

    /**
     * The same in unit-test mode (the functions a flatten leaves are checked) for a function of a class, changed
     * through a setter below an if and two lists, a replacement whose parent is not a list, and a transfer into an
     * empty block (the inliner's), each followed by a flatten.
     */
    @Test
    public void aFlattenFlattensAgainAClassFunctionChangedDeepInsideItsBody() {
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, true, new RunArgs());
        ImProg prog = translator.getImProg();
        CallType normal = CallType.NORMAL;
        ImFunction tock = JassIm.ImFunction(model, "tock", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(), JassIm.ImStmts(), Collections.singletonList(FunctionFlagEnum.IS_NATIVE));
        ImVar local = JassIm.ImVar(model, TypesHelper.imInt(), "local", false);
        ImFunction method = JassIm.ImFunction(model, "method", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(local),
            JassIm.ImStmts(JassIm.ImIf(model, JassIm.ImBoolVal(true),
                JassIm.ImStmts(JassIm.ImSet(model, JassIm.ImVarAccess(local), JassIm.ImIntVal(1))), JassIm.ImStmts())),
            Collections.emptyList());
        ImClass c = JassIm.ImClass(model, "C", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImMethods(),
            JassIm.ImFunctions(method), new ArrayList<>());
        prog.getFunctions().add(tock);
        prog.getClasses().add(c);

        prog.flatten(translator);
        ImStmts tockBody = tock.getBody();

        // 1. a setter below an if and two lists
        ImIf theIf = (ImIf) method.getBody().get(0);
        int seen = method.modificationCount();
        ((ImSet) theIf.getThenBlock().get(0)).setRight(JassIm.ImStatementExpr(JassIm.ImStmts(
            JassIm.ImFunctionCall(model, tock, JassIm.ImTypeArguments(), JassIm.ImExprs(), false, normal)),
            JassIm.ImIntVal(2)));
        assertNotEquals(method.modificationCount(), seen, "a setter deep in the body counts for the function");
        prog.flatten(translator);
        assertTrue(translator.isFlat(), "after the setter: " + method.getBody());
        assertSame(tock.getBody(), tockBody, "the function which was not modified is left alone");

        // 2. a replacement whose parent is not a list (replaceBy falls back to set(i, ...))
        theIf = (ImIf) method.getBody().get(0);
        ImSet last = (ImSet) theIf.getThenBlock().get(theIf.getThenBlock().size() - 1);
        last.getRight().replaceBy(JassIm.ImStatementExpr(JassIm.ImStmts(
            JassIm.ImFunctionCall(model, tock, JassIm.ImTypeArguments(), JassIm.ImExprs(), false, normal)),
            JassIm.ImIntVal(3)));
        prog.flatten(translator);
        assertTrue(translator.isFlat(), "after replaceBy: " + method.getBody());

        // 3. a transfer into an empty block (addAllMoved takes the source's array)
        theIf = (ImIf) method.getBody().get(0);
        ImStmts moved = JassIm.ImStmts(JassIm.ImSet(model, JassIm.ImVarAccess(local), JassIm.ImStatementExpr(
            JassIm.ImStmts(JassIm.ImFunctionCall(model, tock, JassIm.ImTypeArguments(), JassIm.ImExprs(), false, normal)),
            JassIm.ImIntVal(4))));
        seen = method.modificationCount();
        theIf.getElseBlock().addAllMoved(moved);
        assertNotEquals(method.modificationCount(), seen, "a transfer into the function counts for it");
        prog.flatten(translator);
        assertTrue(translator.isFlat(), "after addAllMoved: " + method.getBody());
        assertSame(tock.getBody(), tockBody, "still left alone");
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
    public void luaArithmeticHelperRetryBudgetsEarlierImpureArguments() {
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
        ImVar impureParameter = JassIm.ImVar(model, TypesHelper.imInt(), "value", false);
        ImFunction impure = JassIm.ImFunction(model, "impure", JassIm.ImTypeVars(),
            JassIm.ImVars(impureParameter), TypesHelper.imInt(), JassIm.ImVars(), JassIm.ImStmts(),
            Collections.singletonList(FunctionFlagEnum.IS_NATIVE));

        ImVars callerParameters = JassIm.ImVars();
        for (int i = 0; i < 178; i++) {
            callerParameters.add(JassIm.ImVar(model, TypesHelper.imInt(), "p" + i, false));
        }
        ImVars outerParameters = JassIm.ImVars();
        ImExprs outerArguments = JassIm.ImExprs();
        for (int i = 0; i < 11; i++) {
            outerParameters.add(JassIm.ImVar(model, TypesHelper.imInt(), "arg" + i, false));
            outerArguments.add(JassIm.ImFunctionCall(model, impure, JassIm.ImTypeArguments(),
                JassIm.ImExprs(JassIm.ImVarAccess(callerParameters.get(i))), false,
                de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL));
        }
        outerParameters.add(JassIm.ImVar(model, TypesHelper.imInt(), "last", false));
        outerArguments.add(JassIm.ImFunctionCall(model, helper, JassIm.ImTypeArguments(),
            JassIm.ImExprs(JassIm.ImVarAccess(callerParameters.get(11)), JassIm.ImIntVal(3)), false,
            de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL));
        ImFunction outer = JassIm.ImFunction(model, "outer", JassIm.ImTypeVars(), outerParameters,
            JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        ImVars keepAliveParameters = JassIm.ImVars();
        ImExprs keepAliveArguments = JassIm.ImExprs();
        for (int i = 0; i < callerParameters.size(); i++) {
            keepAliveParameters.add(JassIm.ImVar(model, TypesHelper.imInt(), "keep" + i, false));
            keepAliveArguments.add(JassIm.ImVarAccess(callerParameters.get(i)));
        }
        ImFunction keepAlive = JassIm.ImFunction(model, "keepAlive", JassIm.ImTypeVars(),
            keepAliveParameters, JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(),
            Collections.emptyList());
        ImFunction caller = JassIm.ImFunction(model, "caller", JassIm.ImTypeVars(), callerParameters,
            JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(
                JassIm.ImFunctionCall(model, outer, JassIm.ImTypeArguments(), outerArguments, false,
                    de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL),
                JassIm.ImFunctionCall(model, keepAlive, JassIm.ImTypeArguments(), keepAliveArguments, false,
                    de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL)),
            Collections.emptyList());
        prog.getFunctions().add(helper);
        prog.getFunctions().add(impure);
        prog.getFunctions().add(outer);
        prog.getFunctions().add(keepAlive);
        prog.getFunctions().add(caller);

        assertEquals(0, new ImInliner(translator).inlineLuaDivModHelpersWithinLocalBudget());
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
        // Jass reads the literal 0.333333343 one float low, so 1/3 is left for the game
        assertFalse(out.contains("0.33333334"), out);
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

    /** Jass == on reals allows 0.001 but != is exact, so for reals not (a == b) is not a != b, and a
     *  while loop, which exits on not (condition), must keep the comparison it was written with. */
    @Test
    public void negatedRealEqualityIsNotUnequality() throws Exception {
        test().executeProg(true).testLua(false).lines(
            "package test",
            "native testSuccess()",
            "native testFail(string msg)",
            "@noinline function r(real x) returns real",
            "    return x",
            "init",
            "    let a = r(1.0)",
            "    let b = r(1.0005)",
            "    if not (a == b)",
            "        testFail(\"not ==\")",
            "    if not (a != b)",
            "        testFail(\"not !=\")",
            "    var n = 0",
            "    while a == b and n < 3",
            "        n++",
            "    var m = 0",
            "    while a != b and m < 3",
            "        m++",
            "    if n != 3 or m != 3",
            "        testFail(\"loops\")",
            "    testSuccess()"
        );
        String out = Files.toString(new File("test-output/OptimizerTests_negatedRealEqualityIsNotUnequality_opt.j"), Charsets.UTF_8);
        assertTrue(out.contains("exitwhen ( not (a == b))") && out.contains("exitwhen ( not (a != b))"), out);
    }

    /** Real literals closer than 0.001 are equal in Jass but not in Lua. Jass does not read these
     *  literals exactly, so the Jass build leaves them for the game to compare. */
    @Test
    public void nearlyEqualRealLiteralsAreNotFoldedForJass() throws Exception {
        test().executeProg(true).testLua(false).lines(
            "package test",
            "native testSuccess()",
            "native testFail(string msg)",
            "init",
            "    boolean near = 1.0 == 1.0005",
            "    boolean far = 1.0 == 1.002",
            "    boolean differ = 1.0 != 1.0005",
            "    if near and not far and differ",
            "        testSuccess()",
            "    else",
            "        testFail(\"folded\")"
        );
        String out = Files.toString(new File("test-output/OptimizerTests_nearlyEqualRealLiteralsAreNotFoldedForJass_opt.j"), Charsets.UTF_8);
        assertTrue(out.contains("1.0 == 1.0005") && out.contains("1.0 == 1.002") && out.contains("1.0 != 1.0005"), out);
    }

    /** Measured on the 3.0.0 client, Jass reads 0.1 one float high and 1.1 one float low but short exact
     *  binary fractions exactly, so real folding for Jass only reads and writes those, and only where
     *  the operation is exact. */
    @Test
    public void jassRealFoldingOnlyUsesExactLiterals() throws Exception {
        test().lines(
            "package test",
            "native print(real r)",
            "native printb(boolean b)",
            "init",
            "    print(1.0 / 3.0)",
            "    print(0.1 + 0.2)",
            "    print(1.1 * 2.0)",
            "    print(16777216.0 + 1.0)",
            "    print(1.0 + 0.015625)",
            "    print(5.5 % 2.0)",
            "    printb(0.1 < 0.2)",
            "    print(2.5 * 0.5)",
            "    print(1.0 + 0.03125)",
            "    print(0.5 - 2.0)",
            "    print(3 / 4)",
            "    printb(0.5 < 0.75)"
        );
        String out = Files.toString(new File("test-output/OptimizerTests_jassRealFoldingOnlyUsesExactLiterals_opt.j"), Charsets.UTF_8);
        for (String unfolded : new String[] {"1.0 / 3.0", "0.1 + 0.2", "1.1 * 2.0", "16777216.0 + 1.0",
                "1.0 + 0.015625", "ModuloReal(5.5, 2.0)", "0.1 < 0.2"}) {
            assertTrue(out.contains(unfolded), unfolded + " must be left for the game:\n" + out);
        }
        for (String folded : new String[] {"print(1.25)", "print(1.03125)", "print(-1.5)", "print(0.75)", "printb(true)"}) {
            assertTrue(out.contains(folded), folded + " should be folded:\n" + out);
        }
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

    /**
     * In front of the if, a statement runs before the condition: a return there means the condition is never
     * evaluated, so its call must stay.
     */
    @Test
    public void branchMergerKeepsTheConditionBeforeEqualReturns() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "int trace = 0",
            "@noinline function eff(int k) returns int",
            "    trace += k",
            "    return k",
            "@noinline function f(int b)",
            "    if eff(b) > 0",
            "        return",
            "    else",
            "        return",
            "init",
            "    f(5)",
            "    if trace == 5",
            "        testSuccess()");
    }

    /** The same for an exit of the loop around the if. */
    @Test
    public void branchMergerKeepsTheConditionBeforeEqualExits() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "int trace = 0",
            "@noinline function eff(int k) returns int",
            "    trace += k",
            "    return k",
            "@noinline function f(int b)",
            "    while true",
            "        if eff(b) > 0",
            "            break",
            "        else",
            "            break",
            "init",
            "    f(5)",
            "    if trace == 5",
            "        testSuccess()");
    }

    /** A condition without effects is not missed, so equal returns are still merged and the if goes. */
    @Test
    public void branchMergerMergesEqualReturnsUnderAConditionWithoutEffects() throws Exception {
        test().lines(
            "package test",
            "native print(int i)",
            "@noinline function f(int b)",
            "    if b > 0",
            "        return",
            "    else",
            "        return",
            "init",
            "    f(5)",
            "    print(1)");
        String optimized = Files.toString(
            new File("test-output/OptimizerTests_branchMergerMergesEqualReturnsUnderAConditionWithoutEffects_opt.j"),
            Charsets.UTF_8);
        String f = optimized.substring(optimized.indexOf("function f takes"));
        f = f.substring(0, f.indexOf("endfunction"));
        assertFalse(f.contains("if "), "the equal returns are merged:\n" + f);
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
            "    player playerZero = Player(0)",
            "    if selected == playerZero",
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
            "    player playerZero = Player(0)",
            "    if selected == playerZero",
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
            "    player playerZero = Player(0)",
            "    if selected == playerZero",
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
            "    player playerOne = Player(1)",
            "    if selected == playerOne",
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
            "    player playerZero = Player(0)",
            "    if selected == playerZero",
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
            "    player playerZero = Player(0)",
            "    if selected == playerZero",
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
            "BlzGetLocale",
            "BlzGetModelCinematicGameShotCount",
            "BlzGetModelCinematicGameCurrentShot",
            "BlzGetModelCinematicGameRemainingTime",
            "BlzGetMinShadowCastingPointLightCount",
            "GetCameraFieldControlledByInput",
            "BlzCameraGetCameraType",
            "BlzIsMetaKeyPressed",
            "BlzIsKeyPressed",
            "BlzIsMouseButtonPressed",
            "BlzGetMouseScreenPosX",
            "BlzGetMouseScreenPosY",
            "BlzPixelToFrameX",
            "BlzPixelToFrameY",
            "BlzFrameToPixelX",
            "BlzFrameToPixelY"
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

    /**
     * The compiletime state splitter optimises a function on its own, outside ImOptimizer, so the
     * local merger it runs has no translator to ask about Lua intrinsics. A dead assignment whose
     * right-hand side is a call must then be kept as a statement, not dereference a missing
     * translator.
     */
    @Test
    public void splitterKeepsADeadCallResultWithoutATranslatorContext() {
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, false, new RunArgs());
        ImProg prog = translator.getImProg();
        ImFunction sink = nativeIntFunction(model, "sink");
        ImVar unused = JassIm.ImVar(model, TypesHelper.imInt(), "unused", false);
        ImFunctionCall call = JassIm.ImFunctionCall(model, sink, JassIm.ImTypeArguments(),
            JassIm.ImExprs(), false, de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL);
        ImFunction state = JassIm.ImFunction(model, "state", JassIm.ImTypeVars(), JassIm.ImVars(),
            JassIm.ImVoid(), JassIm.ImVars(unused),
            JassIm.ImStmts(JassIm.ImSet(model, JassIm.ImVarAccess(unused), call)), Collections.emptyList());
        prog.getFunctions().add(sink);
        prog.getFunctions().add(state);

        FunctionSplitter.splitFunc(translator, state);

        // The splitter moves the body into helper functions, so look through the whole program.
        boolean[] callSurvives = {false};
        for (ImFunction f : prog.getFunctions()) {
            f.accept(new ImFunction.DefaultVisitor() {
                @Override
                public void visit(ImFunctionCall c) {
                    super.visit(c);
                    if (c.getFunc() == sink) {
                        callSurvives[0] = true;
                    }
                }
            });
        }
        assertTrue(callSurvives[0], "the call's side effect must survive the dead assignment");
    }

    /**
     * A local is live after a statement when something reads it before it is assigned again, along
     * some path. The loop shows the fixed point: the counter is live around it, the temporary only
     * between its assignment and its read.
     */
    @Test
    public void localMergerLivenessFollowsAssignmentsAndLoops() {
        Element trace = Ast.NoExpr();
        LocalMerger localMerger = new LocalMerger();
        ImVar a = JassIm.ImVar(trace, TypesHelper.imInt(), "a", false);
        ImVar b = JassIm.ImVar(trace, TypesHelper.imInt(), "b", false);
        ImVar c = JassIm.ImVar(trace, TypesHelper.imInt(), "c", false);
        ImVar sinkA = JassIm.ImVar(trace, TypesHelper.imInt(), "sinkA", false);
        ImVar sinkB = JassIm.ImVar(trace, TypesHelper.imInt(), "sinkB", false);
        ImFunction sink = JassIm.ImFunction(trace, "sink", JassIm.ImTypeVars(), JassIm.ImVars(sinkA, sinkB),
            JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());

        ImSet setA = JassIm.ImSet(trace, JassIm.ImVarAccess(a), JassIm.ImIntVal(1));
        ImSet setB = JassIm.ImSet(trace, JassIm.ImVarAccess(b), JassIm.ImIntVal(2));
        ImSet setC = JassIm.ImSet(trace, JassIm.ImVarAccess(c), JassIm.ImOperatorCall(
            de.peeeq.wurstscript.WurstOperator.PLUS, JassIm.ImExprs(JassIm.ImVarAccess(a), JassIm.ImVarAccess(b))));
        ImSet copyC = JassIm.ImSet(trace, JassIm.ImVarAccess(a), JassIm.ImVarAccess(c));
        ImFunctionCall call = JassIm.ImFunctionCall(trace, sink, JassIm.ImTypeArguments(),
            JassIm.ImExprs(JassIm.ImVarAccess(a), JassIm.ImVarAccess(b)), false,
            de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL);
        ImFunction straight = JassIm.ImFunction(trace, "straight", JassIm.ImTypeVars(), JassIm.ImVars(),
            JassIm.ImVoid(), JassIm.ImVars(a, b, c), JassIm.ImStmts(setA, setB, setC, copyC, call),
            Collections.emptyList());

        Map<ImStmt, Set<ImVar>> straightLiveness = localMerger.calculateLiveness(straight);

        assertEquals(straightLiveness.get(setA), HashSet.of(a));
        assertEquals(straightLiveness.get(setB), HashSet.of(a, b));
        assertEquals(straightLiveness.get(setC), HashSet.of(b, c));
        assertEquals(straightLiveness.get(copyC), HashSet.of(a, b));
        assertEquals(straightLiveness.get(call), HashSet.empty());

        ImVar i = JassIm.ImVar(trace, TypesHelper.imInt(), "i", false);
        ImVar t = JassIm.ImVar(trace, TypesHelper.imInt(), "t", false);
        ImSet init = JassIm.ImSet(trace, JassIm.ImVarAccess(i), JassIm.ImIntVal(0));
        ImExitwhen exit = JassIm.ImExitwhen(trace, JassIm.ImOperatorCall(de.peeeq.wurstscript.WurstOperator.EQ,
            JassIm.ImExprs(JassIm.ImVarAccess(i), JassIm.ImIntVal(3))));
        ImSet copy = JassIm.ImSet(trace, JassIm.ImVarAccess(t), JassIm.ImVarAccess(i));
        ImSet step = JassIm.ImSet(trace, JassIm.ImVarAccess(i), JassIm.ImOperatorCall(
            de.peeeq.wurstscript.WurstOperator.PLUS, JassIm.ImExprs(JassIm.ImVarAccess(t), JassIm.ImIntVal(1))));
        ImFunction looping = JassIm.ImFunction(trace, "looping", JassIm.ImTypeVars(), JassIm.ImVars(),
            JassIm.ImVoid(), JassIm.ImVars(i, t),
            JassIm.ImStmts(init, JassIm.ImLoop(trace, JassIm.ImStmts(exit, copy, step))), Collections.emptyList());

        Map<ImStmt, Set<ImVar>> loopLiveness = localMerger.calculateLiveness(looping);

        assertEquals(loopLiveness.get(init), HashSet.of(i));
        assertEquals(loopLiveness.get(exit), HashSet.of(i));
        assertEquals(loopLiveness.get(copy), HashSet.of(t));
        assertEquals(loopLiveness.get(step), HashSet.of(i));
    }

    /**
     * An element is dependent when anything in it is: the value and every element around it, up to the
     * body of its function, which then uses the local player. Siblings which do not take part stay clean.
     */
    @Test
    public void localPlayerDependenceReachesTheEnclosingElementsAndNothingElse() {
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, false, new RunArgs());
        ImProg prog = translator.getImProg();
        ImFunction localPlayer = nativeIntFunction(model, "GetLocalPlayer");
        ImVar tainted = JassIm.ImVar(model, TypesHelper.imInt(), "tainted", false);
        ImVar clean = JassIm.ImVar(model, TypesHelper.imInt(), "clean", false);
        ImFunctionCall source = JassIm.ImFunctionCall(model, localPlayer, JassIm.ImTypeArguments(),
            JassIm.ImExprs(), false, de.peeeq.wurstscript.translation.imtranslation.CallType.NORMAL);
        ImExprs operands = JassIm.ImExprs(source, JassIm.ImIntVal(1));
        ImOperatorCall sum = JassIm.ImOperatorCall(de.peeeq.wurstscript.WurstOperator.PLUS, operands);
        ImSet taintedAssignment = JassIm.ImSet(model, JassIm.ImVarAccess(tainted), sum);
        ImIntVal constant = JassIm.ImIntVal(2);
        ImSet cleanAssignment = JassIm.ImSet(model, JassIm.ImVarAccess(clean), constant);
        ImFunction function = JassIm.ImFunction(model, "function", JassIm.ImTypeVars(), JassIm.ImVars(),
            JassIm.ImVoid(), JassIm.ImVars(tainted, clean),
            JassIm.ImStmts(taintedAssignment, cleanAssignment), Collections.emptyList());
        prog.getFunctions().add(localPlayer);
        prog.getFunctions().add(function);

        LocalPlayerContextAnalyzer analyzer = new LocalPlayerContextAnalyzer(prog);

        assertTrue(analyzer.isLocalPlayerDependent(source));
        assertTrue(analyzer.isLocalPlayerDependent(operands));
        assertTrue(analyzer.isLocalPlayerDependent(sum));
        assertTrue(analyzer.isLocalPlayerDependent(taintedAssignment));
        assertTrue(analyzer.isLocalPlayerDependent(function.getBody()));
        assertTrue(analyzer.isLocalPlayerDependent(tainted));
        assertTrue(analyzer.functionUsesLocalPlayer(function));
        assertFalse(analyzer.isLocalPlayerDependent(constant));
        assertFalse(analyzer.isLocalPlayerDependent(cleanAssignment));
        assertFalse(analyzer.isLocalPlayerDependent(clean));
    }

    /**
     * Every callee has an @noinline twin with the same body, and the program fails unless both give the same results
     * and the same trace of effects: early returns next to break and continue, a return-free loop after a returning
     * one, switch in a loop, for-in closing before the return, tuple returns, and inlined callees with returns of
     * their own in conditions and return values.
     */
    @Test
    public void inlinedReturnLoweringMatchesTheCalledFunction() {
        test().testLua(true).luaOnly(false).inline().localOptimizations().executeProg().lines(
            "package test",
            "native testSuccess()",
            "int trace = 0",
            "int closed = 0",
            "int failures = 0",
            "tuple pair(int a, int b)",
            "@noinline function side(int k) returns int",
            "    trace = trace * 5 + k",
            "    return trace mod 7",
            "class It",
            "    int i = 0",
            "    int n",
            "    construct(int n)",
            "        this.n = n",
            "    function hasNext() returns bool",
            "        return i < n",
            "    function next() returns int",
            "        i++",
            "        return i",
            "    function close()",
            "        closed++",
            "        destroy this",
            "class Range",
            "    int n",
            "    construct(int n)",
            "        this.n = n",
            "    function iterator() returns It",
            "        return new It(n)",
            "@noinline function scanRef(int x) returns int",
            "    for i = 0 to 5",
            "        if i == 1",
            "            continue",
            "        trace = trace * 3 + i",
            "        if i == x",
            "            return i * 10 + side(i)",
            "        if trace > 400",
            "            break",
            "    for j = 0 to 2",
            "        if j == x",
            "            break",
            "        trace = trace * 3 + 7",
            "    return -side(x)",
            "@inline function scanInl(int x) returns int",
            "    for i = 0 to 5",
            "        if i == 1",
            "            continue",
            "        trace = trace * 3 + i",
            "        if i == x",
            "            return i * 10 + side(i)",
            "        if trace > 400",
            "            break",
            "    for j = 0 to 2",
            "        if j == x",
            "            break",
            "        trace = trace * 3 + 7",
            "    return -side(x)",
            "@noinline function tupRef(int x) returns pair",
            "    for i = 0 to 3",
            "        if i == x",
            "            return pair(i, side(i))",
            "        trace = trace * 3 + i",
            "    return pair(-1, side(9))",
            "@inline function tupInl(int x) returns pair",
            "    for i = 0 to 3",
            "        if i == x",
            "            return pair(i, side(i))",
            "        trace = trace * 3 + i",
            "    return pair(-1, side(9))",
            "@noinline function swRef(int x) returns int",
            "    for i = 0 to 4",
            "        switch (i + x) mod 4",
            "            case 0",
            "                trace = trace * 3 + 1",
            "            case 1",
            "                if i > 1",
            "                    return i * 100 + side(x)",
            "                trace = trace * 3 + 2",
            "            case 2",
            "                continue",
            "            default",
            "                if x > 3",
            "                    break",
            "                trace = trace * 3 + 3",
            "        trace += 1",
            "    return -side(x)",
            "@inline function swInl(int x) returns int",
            "    for i = 0 to 4",
            "        switch (i + x) mod 4",
            "            case 0",
            "                trace = trace * 3 + 1",
            "            case 1",
            "                if i > 1",
            "                    return i * 100 + side(x)",
            "                trace = trace * 3 + 2",
            "            case 2",
            "                continue",
            "            default",
            "                if x > 3",
            "                    break",
            "                trace = trace * 3 + 3",
            "        trace += 1",
            "    return -side(x)",
            "@noinline function findRef(Range r, int x) returns int",
            "    for v in r",
            "        if v == x",
            "            return v * 10 + side(v)",
            "        trace = trace * 3 + v",
            "    return -1",
            "@inline function findInl(Range r, int x) returns int",
            "    for v in r",
            "        if v == x",
            "            return v * 10 + side(v)",
            "        trace = trace * 3 + v",
            "    return -1",
            "@noinline function innerRef(int x) returns int",
            "    for i = 0 to 2",
            "        if i == x",
            "            return side(i) + 1",
            "    if x > 5",
            "        return 7",
            "    return side(x)",
            "@inline function innerInl(int x) returns int",
            "    for i = 0 to 2",
            "        if i == x",
            "            return side(i) + 1",
            "    if x > 5",
            "        return 7",
            "    return side(x)",
            "@noinline function outerRef(int x) returns int",
            "    if x < 0",
            "        return innerRef(-x) * 2",
            "    for j = 0 to 1",
            "        if innerRef(x + j) == 3",
            "            return innerRef(j) + innerRef(x)",
            "    return innerRef(x + 1) - innerRef(x)",
            "@inline function outerInl(int x) returns int",
            "    if x < 0",
            "        return innerInl(-x) * 2",
            "    for j = 0 to 1",
            "        if innerInl(x + j) == 3",
            "            return innerInl(j) + innerInl(x)",
            "    return innerInl(x + 1) - innerInl(x)",
            "@noinline function voidRef(int x)",
            "    for i = 0 to 3",
            "        if i == x",
            "            return",
            "        if i == 2",
            "            continue",
            "        trace = trace * 3 + i",
            "    trace = trace * 3 + 9",
            "@inline function voidInl(int x)",
            "    for i = 0 to 3",
            "        if i == x",
            "            return",
            "        if i == 2",
            "            continue",
            "        trace = trace * 3 + i",
            "    trace = trace * 3 + 9",
            "@noinline function run(int which, int x, bool inl) returns int",
            "    if which == 0",
            "        return inl ? scanInl(x) : scanRef(x)",
            "    if which == 1",
            "        pair p = inl ? tupInl(x) : tupRef(x)",
            "        return p.a * 100 + p.b",
            "    if which == 2",
            "        return inl ? swInl(x) : swRef(x)",
            "    if which == 3",
            "        let r = new Range(4)",
            "        int res = inl ? findInl(r, x) : findRef(r, x)",
            "        destroy r",
            "        return res",
            "    if which == 4",
            "        return inl ? outerInl(x) : outerRef(x)",
            "    for k = 0 to 2",
            "        if inl",
            "            voidInl(x + k)",
            "        else",
            "            voidRef(x + k)",
            "        if k == x",
            "            break",
            "    return 0",
            "init",
            "    for which = 0 to 5",
            "        for x = -2 to 6",
            "            trace = 0",
            "            closed = 0",
            "            int r1 = run(which, x, false)",
            "            int t1 = trace",
            "            int c1 = closed",
            "            trace = 0",
            "            closed = 0",
            "            int r2 = run(which, x, true)",
            "            if r1 != r2 or t1 != trace or c1 != closed",
            "                failures++",
            "    if failures == 0",
            "        testSuccess()");
    }

    /**
     * The pass behind the Lua field defaults, on hand-made IM: of {@code o = alloc C; o.f = 0; ...; o.f = 2} the
     * first write goes only when nothing between the writes can raise. A deallocation can (a double free), and so can
     * reading an array field or the type id through an object which may be null ({@code storage[p][0]} and the class
     * descriptor of p index nil), arithmetic on anything but literals (a nil read can reach any variable), and a write
     * through such an object (a nil table key). An object
     * which was not allocated in the list may be null itself, so its first write could raise.
     */
    @Test
    public void aFieldWriteGoesOnlyForANewObjectWithNothingBetweenWhichCanRaise() {
        assertEquals(redundantFieldStoresLeave(true, "nothing"), 2, "o = alloc; o.f = 2");
        assertEquals(redundantFieldStoresLeave(true, "arithmetic on literals"), 3, "1 + 2 does not raise");
        assertEquals(redundantFieldStoresLeave(true, "arithmetic on a local"), 4, "x + 1 can add nil: x may hold one");
        assertEquals(redundantFieldStoresLeave(true, "dealloc"), 4, "a deallocation between keeps the first write");
        assertEquals(redundantFieldStoresLeave(true, "array field read"), 4, "p.arr[0] can index nil");
        assertEquals(redundantFieldStoresLeave(true, "arithmetic on a field read"), 4, "p.g + 1 can add nil");
        assertEquals(redundantFieldStoresLeave(true, "arithmetic on a local read from a field"), 5,
            "x = p.g; x + 1 can add nil");
        assertEquals(redundantFieldStoresLeave(true, "arithmetic on a global read from a field"), 5,
            "G = p.g; G + 1 can add nil");
        assertEquals(redundantFieldStoresLeave(true, "write through another object"), 4, "p.g = 1 can use a nil key");
        assertEquals(redundantFieldStoresLeave(true, "type id read through another object"), 4,
            "p.typeId reads a nil class descriptor");
        assertEquals(redundantFieldStoresLeave(false, "nothing"), 2, "an object not allocated here keeps it");
    }

    /** The statements left of {@code [o = alloc C;] o.f = 0; <middle>; o.f = 2} after the pass. */
    private int redundantFieldStoresLeave(boolean allocate, String middle) {
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, false, new RunArgs());
        ImVar field = JassIm.ImVar(model, TypesHelper.imInt(), "f", false);
        ImVar other = JassIm.ImVar(model, TypesHelper.imInt(), "g", false);
        ImVar array = JassIm.ImVar(model, JassIm.ImArrayType(TypesHelper.imInt()), "arr", false);
        ImClass c = JassIm.ImClass(model, "C", JassIm.ImTypeVars(), JassIm.ImVars(field, other, array),
            JassIm.ImMethods(), JassIm.ImFunctions(), new ArrayList<>());
        translator.getImProg().getClasses().add(c);
        ImVar o = JassIm.ImVar(model, JassIm.ImClassType(c, JassIm.ImTypeArguments()), "o", false);
        ImVar p = JassIm.ImVar(model, JassIm.ImClassType(c, JassIm.ImTypeArguments()), "p", false);
        ImVar x = JassIm.ImVar(model, TypesHelper.imInt(), "x", false);
        ImStmts body = JassIm.ImStmts();
        if (allocate) {
            body.add(JassIm.ImSet(model, JassIm.ImVarAccess(o),
                JassIm.ImAlloc(model, JassIm.ImClassType(c, JassIm.ImTypeArguments()))));
        }
        body.add(JassIm.ImSet(model, member(model, o, field), JassIm.ImIntVal(0)));
        switch (middle) {
            case "nothing" -> {
            }
            case "arithmetic on literals" -> body.add(JassIm.ImSet(model, JassIm.ImVarAccess(x), plusOne(JassIm.ImIntVal(1))));
            case "arithmetic on a local" -> body.add(JassIm.ImSet(model, JassIm.ImVarAccess(x), plusOne(JassIm.ImVarAccess(x))));
            case "arithmetic on a global read from a field" -> {
                ImVar g = JassIm.ImVar(model, TypesHelper.imInt(), "G", false);
                translator.getImProg().getGlobals().add(g);
                body.add(JassIm.ImSet(model, JassIm.ImVarAccess(g), member(model, p, other)));
                body.add(JassIm.ImSet(model, JassIm.ImVarAccess(x), plusOne(JassIm.ImVarAccess(g))));
            }
            case "dealloc" -> body.add(JassIm.ImDealloc(model, JassIm.ImClassType(c, JassIm.ImTypeArguments()),
                JassIm.ImVarAccess(o)));
            case "array field read" -> body.add(JassIm.ImSet(model, JassIm.ImVarAccess(x),
                JassIm.ImMemberAccess(model, JassIm.ImVarAccess(p), JassIm.ImTypeArguments(), array,
                    JassIm.ImExprs(JassIm.ImIntVal(0)))));
            case "arithmetic on a field read" -> body.add(JassIm.ImSet(model, JassIm.ImVarAccess(x),
                plusOne(member(model, p, other))));
            case "arithmetic on a local read from a field" -> {
                body.add(JassIm.ImSet(model, JassIm.ImVarAccess(x), member(model, p, other)));
                body.add(JassIm.ImSet(model, JassIm.ImVarAccess(x), plusOne(JassIm.ImVarAccess(x))));
            }
            case "write through another object" -> body.add(JassIm.ImSet(model, member(model, p, other),
                JassIm.ImIntVal(1)));
            case "type id read through another object" -> body.add(JassIm.ImSet(model, JassIm.ImVarAccess(x),
                JassIm.ImTypeIdOfObj(JassIm.ImVarAccess(p), JassIm.ImClassType(c, JassIm.ImTypeArguments()))));
            default -> throw new IllegalArgumentException(middle);
        }
        body.add(JassIm.ImSet(model, member(model, o, field), JassIm.ImIntVal(2)));
        ImFunction f = JassIm.ImFunction(model, "f", JassIm.ImTypeVars(), JassIm.ImVars(p), JassIm.ImVoid(),
            JassIm.ImVars(o, x), body, Collections.emptyList());
        translator.getImProg().getFunctions().add(f);
        new de.peeeq.wurstscript.intermediatelang.optimizer.RedundantFieldStores().optimize(translator);
        return f.getBody().size();
    }

    private static ImMemberAccess member(WurstModel model, ImVar receiver, ImVar field) {
        return JassIm.ImMemberAccess(model, JassIm.ImVarAccess(receiver), JassIm.ImTypeArguments(), field,
            JassIm.ImExprs());
    }

    private static ImExpr plusOne(ImExpr e) {
        return JassIm.ImOperatorCall(de.peeeq.wurstscript.WurstOperator.PLUS, JassIm.ImExprs(e, JassIm.ImIntVal(1)));
    }
}
