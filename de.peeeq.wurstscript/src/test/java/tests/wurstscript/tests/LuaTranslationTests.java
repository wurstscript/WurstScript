package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.peeeq.wurstscript.validation.GlobalCaches;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.AssertJUnit.*;


public class LuaTranslationTests extends WurstScriptTest {

    private void assertFunctionReturns(String output, String functionName, String returnValue) {
        /*
        The function functionName must only return returnValue and do nothing else.
         */
        Pattern pattern = Pattern.compile("function\\s+" + functionName + "\\(.*?\\)\\s+return\\s+" + returnValue + "\\s+end");
        Matcher matcher = pattern.matcher(output);
        assertTrue("Function " + functionName + " with return value " + returnValue + " was not found.", matcher.find());
    }

    private void assertFunctionCall(String output, String functionName, String arguments) {
        /*
        The function declaration is ignored by using negative lookbehind.
        All function calls must use the specified arguments.
         */
        Pattern pattern = Pattern.compile("(?<!\\sfunction\\s)" + functionName + "\\((.*)\\)");
        Matcher matcher = pattern.matcher(output);
        boolean findAtLeastOne = false;
        while (matcher.find()) {
            assertEquals(arguments, matcher.group(1));
            findAtLeastOne = true;
        }
        assertTrue("Function call to function " + functionName + " with arguments (" + arguments + ") was not found.", findAtLeastOne);
    }

    private void assertFunctionBodyContains(String output, String functionName, String search, boolean mustContain) {
        Pattern pattern = Pattern.compile("function\\s*" + functionName + "\\s*\\(.*\\).*\\n" + "((?:\\n|.)*?)end");
        Matcher matcher = pattern.matcher(output);
        boolean found = false;
        while (matcher.find()) {
            found = true;
            String body = matcher.group(1);
            if(!body.contains(search) && mustContain) {
                fail("Function " + functionName + " must contain " + search + ".");
            }
            if(body.contains(search) && !mustContain) {
                fail("Function " + functionName + " must not contain " + search + ".");
            }
        }
        assertTrue("Function " + functionName + " was not found.", found);
    }

    private void assertDoesNotContainRegex(String output, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(output);
        assertFalse("Pattern must not occur: " + regex, matcher.find());
    }

    private void assertContainsRegex(String output, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(output);
        assertTrue("Pattern must occur: " + regex, matcher.find());
    }

    @Test
    public void testStdLib() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "import MagicFunctions",
            "init",
            "   print(compiletime)"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_testStdLib.lua"), Charsets.UTF_8);
        assertTrue(compiled.contains("MagicFunctions_compiletime"));
        assertTrue(compiled.contains("function __wurst_InitHashtable("));
        assertTrue(compiled.contains("function __wurst_SaveInteger("));
        assertTrue(compiled.contains("function __wurst_LoadInteger("));
        assertFunctionBodyContains(compiled, "__wurst_LoadInteger", "return 0", true);
    }

    @Test
    public void continueLoweringInLua() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "native testSuccess()",
            "init",
            "    int i = 0",
            "    int sum = 0",
            "    while i < 5",
            "        i++",
            "        if i mod 2 == 0",
            "            continue",
            "        sum += i",
            "    if sum == 9",
            "        testSuccess()"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_continueLoweringInLua.lua"), Charsets.UTF_8);
        assertTrue("expected continue lowering helper flag in lua output", compiled.contains("continueFlag_"));
    }

    @Test
    public void noContinueDoesNotEmitContinueFlagInLua() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "native testSuccess()",
            "init",
            "    int i = 0",
            "    while i < 3",
            "        i++",
            "        if i > 10",
            "            skip",
            "    testSuccess()"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_noContinueDoesNotEmitContinueFlagInLua.lua"), Charsets.UTF_8);
        assertFalse("continue flag helper should not be emitted when no continue is present", compiled.contains("continueFlag_"));
    }

    @Ignore
    @Test
    public void testExecution() {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "init",
            "   testSuccess()"
        );
    }

    @Test
    public void nullString1() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "function nullString() returns string",
            "	return null",
            "init",
            "   nullString()"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_nullString1.lua"), Charsets.UTF_8);
        assertFunctionReturns(compiled, "nullString", "\"\"");
    }

    @Test
    public void nullString2() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "function takesString(string s)",
            "init",
            "   takesString(null)"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_nullString2.lua"), Charsets.UTF_8);
        assertFunctionCall(compiled, "takesString", "\"\"");
    }

    @Ignore
    @Test
    public void nullString3() {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "function nullString() returns string",
            "   return null",
            "function returnsString(string s) returns string",
            "   return s + nullString() + s",
            "init",
            "   let s = \".\"",
            "   let r = returnsString(s)",
            "   if r == s + s and r == \"..\"",
            "       testSuccess()"
        );
    }

    @Test
    public void nullObject1() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "class A",
            "function nullObject() returns A",
            "	return null",
            "init",
            "   nullObject()"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_nullObject1.lua"), Charsets.UTF_8);
        assertFunctionReturns(compiled, "nullObject", "nil");
    }

    @Test
    public void nullObject2() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "class A",
            "function takesObject(A a)",
            "init",
            "   takesObject(null)"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_nullObject2.lua"), Charsets.UTF_8);
        assertFunctionCall(compiled, "takesObject", "nil");
    }

    @Test
    public void nullUnit1() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "function nullUnit() returns unit",
            "	return null",
            "init",
            "   nullUnit()"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_nullUnit1.lua"), Charsets.UTF_8);
        assertFunctionReturns(compiled, "nullUnit", "nil");
    }

    @Test
    public void nullUnit2() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "function takesUnit(unit u)",
            "init",
            "   takesUnit(null)"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_nullUnit2.lua"), Charsets.UTF_8);
        assertFunctionCall(compiled, "takesUnit", "nil");
    }

    @Test
    public void stringConcatenation() throws IOException {
        // Use local variables to test if it works even when local types are eliminated.
        test().testLua(true).lines(
            "package Test",
            "native takesString(string s)",
            "function test()",
            "    let s1 = \"1\"",
            "    let s2 = \"2\"",
            "    takesString(s1 + s2)",
            "init",
            "    test()"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_stringConcatenation.lua"), Charsets.UTF_8);
        // strings use the stringConcat function in lua instead of + operator
        assertFunctionBodyContains(compiled, "test", "+", false);
        assertFunctionBodyContains(compiled, "test", "stringConcat", true);
    }

    @Test
    public void numericOpsDoNotUseGlobalEnsureWrapping() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "function cmp(int a, int b) returns boolean",
            "    return a < b",
            "function divi(int a, int b) returns int",
            "    return a div b",
            "function addi(int a, int b) returns int",
            "    return a + b",
            "init",
            "    cmp(1, 2)",
            "    divi(2, 1)",
            "    addi(1, 2)"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_numericOpsDoNotUseGlobalEnsureWrapping.lua"), Charsets.UTF_8);
        assertFunctionBodyContains(compiled, "cmp", "intEnsure", false);
        assertFunctionBodyContains(compiled, "cmp", "realEnsure", false);
        assertFunctionBodyContains(compiled, "divi", "intEnsure", false);
        assertFunctionBodyContains(compiled, "divi", "realEnsure", false);
        assertFunctionBodyContains(compiled, "addi", "intEnsure", false);
        assertFunctionBodyContains(compiled, "addi", "realEnsure", false);
    }

    @Test
    public void lazyGenericClosureDispatchWorksInLua() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "native testSuccess()",
            "public function lazy<T:>(Lazy<T> l) returns Lazy<T>",
            "    return l",
            "public abstract class Lazy<T:>",
            "    T val = null",
            "    var wasRetrieved = false",
            "    abstract function retrieve() returns T",
            "    function get() returns T",
            "        if not wasRetrieved",
            "            val = retrieve()",
            "            wasRetrieved = true",
            "        return val",
            "init",
            "    let l = lazy<int>(() -> 5)",
            "    if l.get() == 5",
            "        testSuccess()"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_lazyGenericClosureDispatchWorksInLua.lua"), Charsets.UTF_8);
        assertTrue(compiled.contains("Lazy_lazy_Test.Lazy_retrieve ="));
        assertTrue(compiled.contains("l:Lazy_get()"));
    }

    @Test
    public void stringArrayReadIsEnsured() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "string array playerName",
            "init",
            "    let i = 0",
            "    SetPlayerName(Player(i), playerName[i])"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_stringArrayReadIsEnsured.lua"), Charsets.UTF_8);
        assertContainsRegex(compiled, "SetPlayerName\\(Player\\([^\\)]*\\),\\s*stringEnsure\\(");
    }

    @Test
    public void methodFieldNameCollision() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "class Foo",
            "    int size = 3",
            "    function size() returns int",
            "        return size",
            "init",
            "    let f = new Foo()",
            "    f.size()"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_methodFieldNameCollision.lua"), Charsets.UTF_8);
        assertFunctionBodyContains(compiled, "Foo_Foo_size", "Foo_size_field", true);
        assertFunctionBodyContains(compiled, "Foo_Foo_size", "return this.Foo_size\n", false);
    }

    @Test
    public void overloadedMethodsDoNotAliasInLuaDispatchTables() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "class Writer",
            "    function write(int i)",
            "        skip",
            "    function write(string s)",
            "        skip",
            "init",
            "    let w = new Writer()",
            "    w.write(1)",
            "    w.write(\"x\")"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_overloadedMethodsDoNotAliasInLuaDispatchTables.lua"), Charsets.UTF_8);
        assertTrue(compiled.contains("Writer.Writer_write = Writer_Writer_write"));
        assertTrue(compiled.contains("Writer.Writer_write1 = Writer_Writer_write1"));
        assertFalse(compiled.contains("Writer.Writer_write = Writer_Writer_write1"));
    }

    @Test
    public void mainAndConfigNamesFixed() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "native takesInt(int i)",
            "function helper()",
            "    let main = 1",
            "    let config = 2",
            "    takesInt(main)",
            "    takesInt(config)",
            "init",
            "    helper()"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_mainAndConfigNamesFixed.lua"), Charsets.UTF_8);
        assertFunctionBodyContains(compiled, "helper", "local main1", true);
        assertFunctionBodyContains(compiled, "helper", "local config1", true);
        assertTrue(compiled.contains("function main("));
        assertTrue(compiled.contains("function config("));
        assertFalse(compiled.contains("function main2("));
        assertFalse(compiled.contains("function config2("));
    }


    @Test
    public void intCasting() throws IOException {
        // Use local variables to test if it works even when local types are eliminated.
        test().testLua(true).lines(
            "package Test",
            "native takesInt(int i)",
            "enum MyEnum",
            "    ZERO",
            "    ONE",
            "    TWO",
            "function testEnum()",
            "    let zeroEnum = MyEnum.ZERO",
            "    let zeroInt = zeroEnum castTo int",
            "    let zeroEnum2 = zeroInt castTo MyEnum",
            "    takesInt(zeroEnum castTo int)",
            "    takesInt(zeroInt)",
            "    takesInt(zeroEnum2 castTo int)",
            "class C",
            "native takesC(C c)",
            "function testClass()",
            "    let cObj = new C()",
            "    let cInt = cObj castTo int",
            "    let cObj2 = cInt castTo C",
            "    takesC(cObj)",
            "    takesInt(cInt)",
            "    takesC(cObj2)",
            "init",
            "    testEnum()",
            "    testClass()"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_intCasting.lua"), Charsets.UTF_8);
        // enums are cast implicitly, because they are ints
        assertFunctionBodyContains(compiled, "testEnum", "objectToIndex", false);
        assertFunctionBodyContains(compiled, "testEnum", "zeroEnum = 0", true);
        assertFunctionBodyContains(compiled, "testEnum", "zeroInt = zeroEnum", true);
        assertFunctionBodyContains(compiled, "testEnum", "zeroEnum2 = zeroInt", true);
        // classes are cast with objectToIndex and objectFromIndex in lua
        assertFunctionBodyContains(compiled, "testClass", "__wurst_objectToIndex", true);
        assertFunctionBodyContains(compiled, "testClass", "__wurst_objectFromIndex", true);
        assertFunctionBodyContains(compiled, "testClass", "cInt = cObj", false);
        assertFunctionBodyContains(compiled, "testClass", "cObj2 = cInt", false);
    }

    @Test
    public void configEntrypointNotRenamedWhenUserHasConfigFunction() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "function config()",
            "    skip",
            "init",
            "    config()"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_configEntrypointNotRenamedWhenUserHasConfigFunction.lua"), Charsets.UTF_8);
        assertTrue(compiled.contains("function config("));
        assertFalse(compiled.contains("function config2("));
        assertTrue(compiled.contains("function config1("));
        assertTrue(compiled.contains("config1()"));
    }

    @Test
    public void objectIndexFunctionsDoNotCollideWithUserFunctions() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "function objectToIndex(int i) returns int",
            "    return i + 1",
            "function objectFromIndex(int i) returns int",
            "    return i - 1",
            "native takesInt(int i)",
            "class C",
            "function testClass()",
            "    let cObj = new C()",
            "    let cInt = cObj castTo int",
            "    let cObj2 = cInt castTo C",
            "    takesInt(objectToIndex(3))",
            "    takesInt(objectFromIndex(5))",
            "    if cObj2 == null",
            "        skip",
            "init",
            "    testClass()"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_objectIndexFunctionsDoNotCollideWithUserFunctions.lua"), Charsets.UTF_8);
        assertTrue(compiled.contains("function objectToIndex("));
        assertTrue(compiled.contains("function objectFromIndex("));
        assertFunctionBodyContains(compiled, "testClass", "__wurst_objectToIndex", true);
        assertFunctionBodyContains(compiled, "testClass", "__wurst_objectFromIndex", true);
    }

    @Test
    public void tupleReturningMethodNamedFromIndexIsNotTypecastWrapper() throws IOException {
        test().testLua(true).lines(
            "package TupleFromIndexRepro",
            "public tuple vec2(real x, real y)",
            "public tuple searchResult(boolean found, vec2 pos)",
            "public constant ZERO2 = vec2(0., 0.)",
            "public class A",
            "    function next() returns searchResult",
            "        return nextFromIndex(1)",
            "    function nextFromIndex(int startIndex) returns searchResult",
            "        return searchResult(false, ZERO2)",
            "public class B extends A",
            "init",
            "    let b = new B()",
            "    let r = b.next()",
            "    if r.found",
            "        skip"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_tupleReturningMethodNamedFromIndexIsNotTypecastWrapper.lua"), Charsets.UTF_8);
        assertContainsRegex(compiled, "function\\s+[^\\n]*nextFromIndex[^\\n]*\\(");
        assertDoesNotContainRegex(compiled, "return\\s+__wurst_objectFromIndex\\s*\\(\\s*this\\s*\\)");
    }

    @Test
    public void oldGenericsCastingDoesNotUseGetHandleId() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "class C",
            "native takesInt(int i)",
            "native takesC(C c)",
            "function testCast()",
            "    let cObj = new C()",
            "    let cInt = cObj castTo int",
            "    let cObj2 = cInt castTo C",
            "    takesInt(cInt)",
            "    takesC(cObj2)",
            "init",
            "    testCast()"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_oldGenericsCastingDoesNotUseGetHandleId.lua"), Charsets.UTF_8);
        assertDoesNotContainRegex(compiled, "\\bGetHandleId\\(");
        assertFunctionBodyContains(compiled, "testCast", "__wurst_objectToIndex", true);
        assertFunctionBodyContains(compiled, "testCast", "__wurst_objectFromIndex", true);
    }

    @Test
    public void newGenericsDoNotUseOldTypecastHelpersInLua() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "class C",
            "function id<T>(T x) returns T",
            "    return x",
            "function testGeneric()",
            "    let c = new C()",
            "    let c2 = id<C>(c)",
            "    if c2 == null",
            "        skip",
            "init",
            "    testGeneric()"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_newGenericsDoNotUseOldTypecastHelpersInLua.lua"), Charsets.UTF_8);
        assertFunctionBodyContains(compiled, "testGeneric", "__wurst_objectToIndex", false);
        assertFunctionBodyContains(compiled, "testGeneric", "__wurst_objectFromIndex", false);
    }

    @Test
    public void newGenericsStringFieldAssignmentRoundTripsInLua() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "class C<T:>",
            "    T x",
            "function stringToIndex(string s) returns int",
            "    return 42",
            "function stringFromIndex(int i) returns string",
            "    return \"42\"",
            "function testGenericStringField() returns boolean",
            "    C<string> c = new C<string>",
            "    c.x = \"42\"",
            "    return c.x == \"42\"",
            "init",
            "    if testGenericStringField()",
            "        skip"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_newGenericsStringFieldAssignmentRoundTripsInLua.lua"), Charsets.UTF_8);
        assertFunctionBodyContains(compiled, "testGenericStringField", "c.C_x = \"42\"", true);
        assertFunctionBodyContains(compiled, "testGenericStringField", "stringEnsure(c.C_x)", true);
        assertFunctionBodyContains(compiled, "testGenericStringField", "__wurst_stringToIndex", false);
        assertFunctionBodyContains(compiled, "testGenericStringField", "__wurst_stringFromIndex", false);
    }

    @Test
    public void genericOverrideChainBindsRootSlotToMostSpecificImplInLua() throws IOException {
        test().testLua(true).compilationUnits(genericOverrideReproUnits());
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_genericOverrideChainBindsRootSlotToMostSpecificImplInLua.lua"), Charsets.UTF_8);

        Matcher slotMatcher = Pattern.compile("FSM_currentState:([A-Za-z0-9_]*_update)\\(").matcher(compiled);
        assertTrue("Expected FSM to dispatch through a virtual *_update slot.", slotMatcher.find());
        String dispatchedSlot = slotMatcher.group(1);

        String[] states = {"FindBuilder", "PlanNextAction", "FindSpot", "BuildAtTarget", "QuickBuild", "RescueStrikeTarget"};
        for (String state : states) {
            assertContainsRegex(compiled, state + "\\." + dispatchedSlot + "\\s*=\\s*" + state + "_" + state + "_update");
            assertDoesNotContainRegex(compiled, state + "\\." + dispatchedSlot + "\\s*=\\s*NoOpState_NoOpState_update");
        }
    }

    @Test
    public void luaOutputIsDeterministicForGenericOverrideSlots() throws IOException {
        test().testLua(true).compilationUnits(genericOverrideReproUnits());
        String first = Files.toString(new File("test-output/lua/LuaTranslationTests_luaOutputIsDeterministicForGenericOverrideSlots.lua"), Charsets.UTF_8);

        GlobalCaches.clearAll();

        test().testLua(true).compilationUnits(genericOverrideReproUnits());
        String second = Files.toString(new File("test-output/lua/LuaTranslationTests_luaOutputIsDeterministicForGenericOverrideSlots.lua"), Charsets.UTF_8);

        assertEquals(first, second);
    }

    @Test
    public void genericOverrideChainBindsGlobalStateSlotToMostSpecificImplInLua() throws IOException {
        test().testLua(true).compilationUnits(genericOverrideGlobalStateReproUnits());
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_genericOverrideChainBindsGlobalStateSlotToMostSpecificImplInLua.lua"), Charsets.UTF_8);

        Matcher slotMatcher = Pattern.compile("FSM_globalState:([A-Za-z0-9_]*_update)\\(").matcher(compiled);
        assertTrue("Expected FSM global state to dispatch through a virtual *_update slot.", slotMatcher.find());
        String dispatchedSlot = slotMatcher.group(1);

        assertContainsRegex(compiled, "GlobalCheckState\\." + dispatchedSlot + "\\s*=\\s*GlobalCheckState_GlobalCheckState_update");
        assertDoesNotContainRegex(compiled, "GlobalCheckState\\." + dispatchedSlot + "\\s*=\\s*NoOpState_NoOpState_update");
    }

    private CU[] genericOverrideReproUnits() {
        return new CU[]{
            compilationUnit("fsmLib.wurst",
                "package FSMReproLib",
                "",
                "public abstract class State<T:>",
                "    function enter(T owner)",
                "    function update(T owner, real dt)",
                "    function exit(T owner)",
                "",
                "public class NoOpState<T:> extends State<T>",
                "    override function enter(T owner)",
                "    override function update(T owner, real dt)",
                "    override function exit(T owner)",
                "",
                "public class FSM<T:>",
                "    T owner",
                "    State<T> currentState = null",
                "",
                "    construct(T owner)",
                "        this.owner = owner",
                "",
                "    function setInitialState(State<T> st)",
                "        currentState = st",
                "        if currentState != null",
                "            currentState.enter(owner)",
                "",
                "    function update(real dt)",
                "        if currentState != null",
                "            currentState.update(owner, dt)"
            ),
            compilationUnit("repro.wurst",
                "package GenericOverrideSlotRepro",
                "import FSMReproLib",
                "",
                "public class Owner",
                "    FSM<Owner> fsm = new FSM<Owner>(this)",
                "    int findBuilderTicks = 0",
                "    int planTicks = 0",
                "    int findSpotTicks = 0",
                "    int buildTicks = 0",
                "",
                "public constant globalCheckState = new GlobalCheckState()",
                "public constant findBuilderState = new FindBuilder()",
                "public constant planNextActionState = new PlanNextAction()",
                "public constant findSpotState = new FindSpot()",
                "public constant buildAtTargetState = new BuildAtTarget()",
                "public constant quickBuildState = new QuickBuild()",
                "public constant rescueStrikeTargetState = new RescueStrikeTarget()",
                "",
                "class GlobalCheckState extends NoOpState<Owner>",
                "    override function update(Owner o, real dt)",
                "",
                "class FindBuilder extends NoOpState<Owner>",
                "    override function enter(Owner o)",
                "        o.findBuilderTicks = 0",
                "    override function update(Owner o, real dt)",
                "        o.findBuilderTicks++",
                "",
                "class PlanNextAction extends NoOpState<Owner>",
                "    override function update(Owner o, real dt)",
                "        o.planTicks++",
                "",
                "class FindSpot extends NoOpState<Owner>",
                "    override function update(Owner o, real dt)",
                "        o.findSpotTicks++",
                "",
                "class BuildAtTarget extends NoOpState<Owner>",
                "    override function update(Owner o, real dt)",
                "        o.buildTicks++",

                "class QuickBuild extends NoOpState<Owner>",
                "    override function update(Owner o, real dt)",
                "        o.buildTicks++",
                "",
                "class RescueStrikeTarget extends NoOpState<Owner>",
                "    override function update(Owner o, real dt)",
                "        o.buildTicks++",
                "",
                "function runOne(State<Owner> st) returns int",
                "    let o = new Owner()",
                "    o.fsm.setInitialState(st)",
                "    for i = 0 to 4",
                "        o.fsm.update(0.1)",
                "    return o.findBuilderTicks",
                "",
                "init",
                "    runOne(findBuilderState)",
                "    runOne(planNextActionState)",
                "    runOne(findSpotState)",
                "    runOne(buildAtTargetState)",
                "    runOne(quickBuildState)",
                "    runOne(rescueStrikeTargetState)"
            )
        };
    }

    private CU[] genericOverrideGlobalStateReproUnits() {
        return new CU[]{
            compilationUnit("fsmLib.wurst",
                "package FSMReproLibGlobal",
                "",
                "public abstract class State<T:>",
                "    function enter(T owner)",
                "    function update(T owner, real dt)",
                "    function exit(T owner)",
                "",
                "public class NoOpState<T:> extends State<T>",
                "    override function enter(T owner)",
                "    override function update(T owner, real dt)",
                "    override function exit(T owner)",
                "",
                "public class FSM<T:>",
                "    T owner",
                "    State<T> globalState = null",
                "    State<T> currentState = null",
                "",
                "    construct(T owner)",
                "        this.owner = owner",
                "",
                "    function setInitialState(State<T> st)",
                "        currentState = st",
                "        if currentState != null",
                "            currentState.enter(owner)",
                "",
                "    function setGlobalState(State<T> st)",
                "        globalState = st",
                "",
                "    function update(real dt)",
                "        if globalState != null",
                "            globalState.update(owner, dt)",
                "        if currentState != null",
                "            currentState.update(owner, dt)"
            ),
            compilationUnit("repro.wurst",
                "package GenericOverrideGlobalStateSlotRepro",
                "import FSMReproLibGlobal",
                "",
                "public class Owner",
                "    FSM<Owner> fsm = new FSM<Owner>(this)",
                "    int globalTicks = 0",
                "    int findBuilderTicks = 0",
                "    int planTicks = 0",
                "    int findSpotTicks = 0",
                "    int buildTicks = 0",
                "",
                "public constant globalCheckState = new GlobalCheckState()",
                "public constant findBuilderState = new FindBuilder()",
                "public constant planNextActionState = new PlanNextAction()",
                "public constant findSpotState = new FindSpot()",
                "public constant buildAtTargetState = new BuildAtTarget()",
                "public constant quickBuildState = new QuickBuild()",
                "",
                "class GlobalCheckState extends NoOpState<Owner>",
                "    override function update(Owner o, real dt)",
                "        o.globalTicks++",
                "",
                "class FindBuilder extends NoOpState<Owner>",
                "    override function update(Owner o, real dt)",
                "        o.findBuilderTicks++",
                "",
                "class PlanNextAction extends NoOpState<Owner>",
                "    override function update(Owner o, real dt)",
                "        o.planTicks++",
                "",
                "class FindSpot extends NoOpState<Owner>",
                "    override function update(Owner o, real dt)",
                "        o.findSpotTicks++",
                "",
                "class BuildAtTarget extends NoOpState<Owner>",
                "    override function update(Owner o, real dt)",
                "        o.buildTicks++",
                "",
                "class QuickBuild extends NoOpState<Owner>",
                "    override function update(Owner o, real dt)",
                "        o.buildTicks++",
                "",
                "init",
                "    let o = new Owner()",
                "    o.fsm.setGlobalState(globalCheckState)",
                "    o.fsm.setInitialState(buildAtTargetState)",
                "    o.fsm.update(0.1)"
            )
        };
    }

    @Test
    public void largeFunctionSpillsLocalsIntoTableInLua() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("package Test");
        lines.add("native takesInt(int i)");
        lines.add("function huge()");
        lines.add("    var sum = 0");
        for (int i = 0; i < 210; i++) {
            lines.add("    let v" + i + " = " + i);
            lines.add("    sum += v" + i);
        }
        lines.add("    takesInt(sum)");
        lines.add("init");
        lines.add("    huge()");

        test().testLua(true).lines(lines.toArray(new String[0]));
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_largeFunctionSpillsLocalsIntoTableInLua.lua"), Charsets.UTF_8);
        assertTrue(compiled.contains("function huge("));
        assertTrue(compiled.contains("__wurst_locals"));
        assertContainsRegex(compiled, "__wurst_locals\\[[0-9]+\\]");
        assertFalse(compiled.contains("local v0"));
        assertFalse(compiled.contains("local v209"));
        assertFalse(compiled.contains("\nsum = "));
        assertFalse(compiled.contains("\nv0 = "));
        assertFalse(compiled.contains("takesInt(sum)"));
    }

    @Test
    public void inlinerDoesNotForceSpillWhenCallerStaysBelowLimit() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("package Test");
        lines.add("native takesInt(int i)");
        lines.add("function small(int x) returns int");
        lines.add("    let a = x");
        lines.add("    let b = a + 1");
        lines.add("    let c = b + 1");
        lines.add("    let d = c + 1");
        lines.add("    return d");
        lines.add("function caller()");
        lines.add("    var sum = 0");
        for (int i = 0; i < 195; i++) {
            lines.add("    let v" + i + " = " + i);
            lines.add("    sum += v" + i);
        }
        lines.add("    sum += small(1)");
        lines.add("    takesInt(sum)");
        lines.add("init");
        lines.add("    caller()");

        test().testLua(true).lines(lines.toArray(new String[0]));
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_inlinerDoesNotForceSpillWhenCallerStaysBelowLimit.lua"), Charsets.UTF_8);
        int start = compiled.indexOf("function caller(");
        assertTrue("caller function not found in generated lua output", start >= 0);
        int end = compiled.indexOf("\nend", start);
        assertTrue("caller function end not found in generated lua output", end > start);
        String callerBody = compiled.substring(start, end);

        assertFalse("caller should not spill locals into table in this shape", callerBody.contains("__wurst_locals"));
        assertTrue("caller should keep direct call in this shape", callerBody.contains("small(1)"));
    }

    @Test
    public void spilledLocalsKeepNestedBlockInitializationsInLua() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("package Test");
        lines.add("native takesInt(int i)");
        lines.add("function hugeNested(boolean b)");
        lines.add("    var sum = 0");
        lines.add("    if b");
        lines.add("        let inside = 7");
        lines.add("        sum += inside");
        for (int i = 0; i < 210; i++) {
            lines.add("    let v" + i + " = " + i);
            lines.add("    sum += v" + i);
        }
        lines.add("    takesInt(sum)");
        lines.add("init");
        lines.add("    hugeNested(true)");

        test().testLua(true).lines(lines.toArray(new String[0]));
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_spilledLocalsKeepNestedBlockInitializationsInLua.lua"), Charsets.UTF_8);
        int start = compiled.indexOf("function hugeNested(");
        assertTrue("hugeNested function not found in generated lua output", start >= 0);
        int end = compiled.indexOf("\nend", start);
        assertTrue("hugeNested function end not found in generated lua output", end > start);
        String body = compiled.substring(start, end);

        assertTrue(body.contains("__wurst_locals"));
        assertContainsRegex(body, "__wurst_locals\\[[0-9]+\\]\\s*=\\s*7");
        assertFalse("nested block local declaration should be rewritten", body.contains("local inside"));
    }

    @Test
    public void spilledLocalsDeclareTableBeforeFirstUseInLua() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("package Test");
        lines.add("native takesInt(int i)");
        lines.add("function huge()");
        lines.add("    var sum = 0");
        for (int i = 0; i < 210; i++) {
            lines.add("    let v" + i + " = " + i);
            lines.add("    sum += v" + i);
        }
        lines.add("    takesInt(sum)");
        lines.add("init");
        lines.add("    huge()");

        test().testLua(true).lines(lines.toArray(new String[0]));
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_spilledLocalsDeclareTableBeforeFirstUseInLua.lua"), Charsets.UTF_8);
        int start = compiled.indexOf("function huge(");
        assertTrue("huge function not found in generated lua output", start >= 0);
        int end = compiled.indexOf("\nend", start);
        assertTrue("huge function end not found in generated lua output", end > start);
        String body = compiled.substring(start, end);

        int declarationPos = body.indexOf("local __wurst_locals");
        int firstUsePos = body.indexOf("__wurst_locals[");
        assertTrue("expected __wurst_locals declaration in spilled function body", declarationPos >= 0);
        assertTrue("expected __wurst_locals use in spilled function body", firstUsePos >= 0);
        assertTrue("__wurst_locals must be declared before first table access", declarationPos < firstUsePos);
    }

    @Test
    public void luaInlinerDoesNotInlineFunctionsWithMultipleReturns() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "native takesInt(int i)",
            "function choose(boolean b, int x) returns int",
            "    if b",
            "        return x + 1",
            "    return x + 2",
            "function caller()",
            "    let v = choose(true, 40)",
            "    takesInt(v)",
            "init",
            "    caller()"
        );

        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_luaInlinerDoesNotInlineFunctionsWithMultipleReturns.lua"), Charsets.UTF_8);
        int start = compiled.indexOf("function caller(");
        assertTrue("caller function not found in generated lua output", start >= 0);
        int end = compiled.indexOf("\nend", start);
        assertTrue("caller function end not found in generated lua output", end > start);
        String callerBody = compiled.substring(start, end);

        assertTrue("caller should keep a direct call to choose for Lua target", callerBody.contains("choose(true, 40)"));
        assertFalse("caller should not contain multi-return inline control vars", callerBody.contains("inlineDone"));
        assertFalse("caller should not contain multi-return inline return temp vars", callerBody.contains("inlineRet"));
    }

    @Test
    public void reflectionNativesStubbedForLua() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "native typeIdToTypeName(int typeId) returns string",
            "native maxTypeId() returns int",
            "native instanceCount(int typeId) returns int",
            "native maxInstanceCount(int typeId) returns int",
            "class A",
            "init",
            "    let name = typeIdToTypeName(A.typeId)",
            "    let m = maxTypeId()",
            "    let c = instanceCount(A.typeId)",
            "    let mc = maxInstanceCount(A.typeId)"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_reflectionNativesStubbedForLua.lua"), Charsets.UTF_8);
        assertFalse(compiled.contains("The native 'typeIdToTypeName' is not implemented."));
        assertFalse(compiled.contains("The native 'maxTypeId' is not implemented."));
        assertFalse(compiled.contains("The native 'instanceCount' is not implemented."));
        assertFalse(compiled.contains("The native 'maxInstanceCount' is not implemented."));
        assertTrue(compiled.contains("typeIdToTypeName = function"));
        assertTrue(compiled.contains("maxTypeId = function"));
        assertTrue(compiled.contains("instanceCount = function"));
        assertTrue(compiled.contains("maxInstanceCount = function"));
    }

    @Test
    public void reflectionNativeStubBodiesReturnDefaults() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "native typeIdToTypeName(int typeId) returns string",
            "native maxTypeId() returns int",
            "native instanceCount(int typeId) returns int",
            "native maxInstanceCount(int typeId) returns int",
            "class A",
            "init",
            "    let _n = typeIdToTypeName(A.typeId)",
            "    let _m = maxTypeId()",
            "    let _c = instanceCount(A.typeId)",
            "    let _mc = maxInstanceCount(A.typeId)"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_reflectionNativeStubBodiesReturnDefaults.lua"), Charsets.UTF_8);
        assertTrue(compiled.contains("typeIdToTypeName = function"));
        assertTrue(compiled.contains("return \"\""));
        assertTrue(compiled.contains("maxTypeId = function"));
        assertTrue(compiled.contains("instanceCount = function"));
        assertTrue(compiled.contains("maxInstanceCount = function"));
        assertTrue(compiled.contains("return 0"));
    }

    @Test
    public void reflectionNativeStubsAreGuardedByExistingDefinitions() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "native typeIdToTypeName(int typeId) returns string",
            "native maxTypeId() returns int",
            "native instanceCount(int typeId) returns int",
            "native maxInstanceCount(int typeId) returns int",
            "class A",
            "init",
            "    let _n = typeIdToTypeName(A.typeId)",
            "    let _m = maxTypeId()",
            "    let _c = instanceCount(A.typeId)",
            "    let _mc = maxInstanceCount(A.typeId)"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_reflectionNativeStubsAreGuardedByExistingDefinitions.lua"), Charsets.UTF_8);
        assertTrue(compiled.contains("if typeIdToTypeName then"));
        assertTrue(compiled.contains("if maxTypeId then"));
        assertTrue(compiled.contains("if instanceCount then"));
        assertTrue(compiled.contains("if maxInstanceCount then"));
    }

    @Test
    public void stdLibInitUsesTriggerEvaluateGuardInMain() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "init",
            "    skip"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_stdLibInitUsesTriggerEvaluateGuardInMain.lua"), Charsets.UTF_8);
        assertTrue(compiled.contains("if not(TriggerEvaluate("));
        assertTrue(compiled.contains("TriggerClearConditions"));
    }

    @Test
    public void stdLibDoesNotEmitTimerBjNatives() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "init",
            "    skip"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_stdLibDoesNotEmitTimerBjNatives.lua"), Charsets.UTF_8);
        assertDoesNotContainRegex(compiled, "\\bCreateTimerBJ\\(");
        assertDoesNotContainRegex(compiled, "\\bStartTimerBJ\\(");
        assertDoesNotContainRegex(compiled, "\\bGetLastCreatedTimerBJ\\(");
        assertFalse(compiled.contains("bj_lastStartedTimer"));
    }

    @Test
    public void stdLibDoesNotEmitWar3HashtableNatives() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "init",
            "    skip"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_stdLibDoesNotEmitWar3HashtableNatives.lua"), Charsets.UTF_8);
        assertDoesNotContainRegex(compiled, "\\bInitHashtable\\(");
        assertDoesNotContainRegex(compiled, "\\bSaveInteger\\(");
        assertDoesNotContainRegex(compiled, "\\bSaveBoolean\\(");
        assertDoesNotContainRegex(compiled, "\\bSaveReal\\(");
        assertDoesNotContainRegex(compiled, "\\bSaveStr\\(");
        assertDoesNotContainRegex(compiled, "\\bLoadInteger\\(");
        assertDoesNotContainRegex(compiled, "\\bLoadBoolean\\(");
        assertDoesNotContainRegex(compiled, "\\bLoadReal\\(");
        assertDoesNotContainRegex(compiled, "\\bLoadStr\\(");
        assertDoesNotContainRegex(compiled, "\\bFlushChildHashtable\\(");
        assertDoesNotContainRegex(compiled, "\\bHaveSavedHandle\\(");
        assertDoesNotContainRegex(compiled, "\\bRemoveSavedHandle\\(");
        assertDoesNotContainRegex(compiled, "\\bSaveAbilityHandle\\(");
        assertDoesNotContainRegex(compiled, "\\bLoadAbilityHandle\\(");
        assertTrue(compiled.contains("function __wurst_HaveSavedHandle("));
    }

    @Test
    public void hashtableHandleExtensionsUseWurstLuaHelpers() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "import Hashtable",
            "init",
            "    let h = InitHashtable()",
            "    if h.hasHandle(1, 2)",
            "        skip"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_hashtableHandleExtensionsUseWurstLuaHelpers.lua"), Charsets.UTF_8);
        assertDoesNotContainRegex(compiled, "\\bHaveSavedHandle\\(");
        assertContainsRegex(compiled, "\\b__wurst_HaveSavedHandle\\(");
        assertTrue(compiled.contains("Wurst experimental Lua assertion guards"));
    }

    @Test
    public void hashtableHandleLoadSaveUseWurstLuaHelpers() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "import Hashtable",
            "init",
            "    let h = InitHashtable()",
            "    h.saveAbilityHandle(1, 2, null)",
            "    let a = h.loadAbilityHandle(1, 2)"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_hashtableHandleLoadSaveUseWurstLuaHelpers.lua"), Charsets.UTF_8);
        assertDoesNotContainRegex(compiled, "\\bSaveAbilityHandle\\(");
        assertDoesNotContainRegex(compiled, "\\bLoadAbilityHandle\\(");
        assertContainsRegex(compiled, "\\b__wurst_SaveAbilityHandle\\(");
        assertContainsRegex(compiled, "\\b__wurst_LoadAbilityHandle\\(");
    }

    @Test
    public void hashtableHelpersEmitPerTypeBucketsInLua() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "import Hashtable",
            "init",
            "    let h = InitHashtable()",
            "    h.saveInt(1, 2, 7)",
            "    h.saveReal(1, 2, 3.5)",
            "    h.saveString(1, 2, \"x\")",
            "    h.saveAbilityHandle(1, 2, null)",
            "    let i = h.loadInt(1, 2)",
            "    let r = h.loadReal(1, 2)",
            "    let s = h.loadString(1, 2)",
            "    let a = h.loadAbilityHandle(1, 2)"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_hashtableHelpersEmitPerTypeBucketsInLua.lua"), Charsets.UTF_8);
        assertFunctionBodyContains(compiled, "__wurst_InitHashtable", "__wurst_ht_int", true);
        assertFunctionBodyContains(compiled, "__wurst_InitHashtable", "__wurst_ht_real", true);
        assertFunctionBodyContains(compiled, "__wurst_InitHashtable", "__wurst_ht_str", true);
        assertFunctionBodyContains(compiled, "__wurst_InitHashtable", "__wurst_ht_handle", true);
        assertFunctionBodyContains(compiled, "__wurst_SaveInteger", "h.__wurst_ht_int", true);
        assertFunctionBodyContains(compiled, "__wurst_SaveReal", "h.__wurst_ht_real", true);
        assertFunctionBodyContains(compiled, "__wurst_SaveStr", "h.__wurst_ht_str", true);
        assertFunctionBodyContains(compiled, "__wurst_SaveAbilityHandle", "h.__wurst_ht_handle", true);
        assertFunctionBodyContains(compiled, "__wurst_LoadInteger", "h.__wurst_ht_int", true);
        assertFunctionBodyContains(compiled, "__wurst_LoadReal", "h.__wurst_ht_real", true);
        assertFunctionBodyContains(compiled, "__wurst_LoadStr", "h.__wurst_ht_str", true);
        assertFunctionBodyContains(compiled, "__wurst_LoadAbilityHandle", "h.__wurst_ht_handle", true);
    }

    @Test
    public void luaFunctionRefWrapperForwardsVarargs() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "init",
            "    let f = CreateForce()",
            "    ForForce(f, () -> skip)"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_luaFunctionRefWrapperForwardsVarargs.lua"), Charsets.UTF_8);
        assertTrue(compiled.contains("xpcall(function (...)"));
        assertTrue(compiled.contains(", ...)"));
        assertFalse(compiled.contains("local temp = ..."));
        assertFalse(compiled.contains("ForForce(f, function (...) \n\t\t\tlocal tempRes"));
    }

    @Test
    public void forForceIsRemappedToWurstHelperInLua() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "init",
            "    let f = CreateForce()",
            "    ForForce(f, () -> begin",
            "        if GetEnumPlayer() != null",
            "            skip",
            "    end)"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_forForceIsRemappedToWurstHelperInLua.lua"), Charsets.UTF_8);
        assertContainsRegex(compiled, "\\b__wurst_ForForce\\(");
        assertContainsRegex(compiled, "\\b__wurst_GetEnumPlayer\\(");
        assertContainsRegex(compiled, "\\bfunction\\s+__wurst_ForForce\\s*\\(");
        assertContainsRegex(compiled, "\\bfunction\\s+__wurst_GetEnumPlayer\\s*\\(");
    }

    @Test
    public void nestedForForceUsesRemappedHelpersInLua() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "force g",
            "function inner()",
            "    ForForce(g, () -> begin",
            "        if GetEnumPlayer() != null",
            "            skip",
            "    end)",
            "init",
            "    g = CreateForce()",
            "    ForForce(g, () -> inner())"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_nestedForForceUsesRemappedHelpersInLua.lua"), Charsets.UTF_8);
        assertContainsRegex(compiled, "\\bfunction\\s+__wurst_ForForce\\s*\\(");
        assertContainsRegex(compiled, "\\bfunction\\s+__wurst_GetEnumPlayer\\s*\\(");
        Matcher forForceCalls = Pattern.compile("\\b__wurst_ForForce\\s*\\(").matcher(compiled);
        int count = 0;
        while (forForceCalls.find()) {
            count++;
        }
        assertTrue("expected at least two remapped __wurst_ForForce call sites for nested loops", count >= 2);
    }

    @Test
    public void luaInlinerKeepsCallbackFuncRefFunctionsAsCallBoundary() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "player picked",
            "function pick(force f) returns player",
            "    picked = null",
            "    ForForce(f, () -> begin",
            "        picked = GetEnumPlayer()",
            "    end)",
            "    return picked",
            "function caller(force f) returns player",
            "    return pick(f)",
            "init",
            "    let f = CreateForce()",
            "    let p = caller(f)"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_luaInlinerKeepsCallbackFuncRefFunctionsAsCallBoundary.lua"), Charsets.UTF_8);
        assertContainsRegex(compiled, "function\\s+caller\\s*\\([^\\)]*\\)\\s+return\\s+pick\\([^\\)]*\\)\\s+end");
    }

    @Test
    public void wurstGetEnumPlayerPrefersNativeBeforeOverride() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "init",
            "    let f = CreateForce()",
            "    ForForce(f, () -> begin",
            "        if GetEnumPlayer() != null",
            "            skip",
            "    end)"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_wurstGetEnumPlayerPrefersNativeBeforeOverride.lua"), Charsets.UTF_8);
        assertContainsRegex(compiled, "function\\s+__wurst_GetEnumPlayer\\s*\\(\\)");
        assertContainsRegex(compiled, "if GetEnumPlayer ~= nil then\\s+local p = GetEnumPlayer\\(\\)\\s+if p ~= nil then return p end\\s+end\\s+if __wurst_enumPlayer_override ~= nil then return __wurst_enumPlayer_override end");
    }

    @Test
    public void groupItemDestructableCallbacksUseWurstContextHelpersInLua() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "init",
            "    let g = CreateGroup()",
            "    ForGroup(g, () -> begin",
            "        if GetEnumUnit() != null",
            "            skip",
            "    end)",
            "    EnumItemsInRect(null, null, () -> begin",
            "        if GetEnumItem() != null",
            "            skip",
            "    end)",
            "    EnumDestructablesInRect(null, null, () -> begin",
            "        if GetEnumDestructable() != null",
            "            skip",
            "    end)"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_groupItemDestructableCallbacksUseWurstContextHelpersInLua.lua"), Charsets.UTF_8);
        assertContainsRegex(compiled, "\\b__wurst_ForGroup\\(");
        assertContainsRegex(compiled, "\\b__wurst_GetEnumUnit\\(");
        assertContainsRegex(compiled, "\\b__wurst_EnumItemsInRect\\(");
        assertContainsRegex(compiled, "\\b__wurst_GetEnumItem\\(");
        assertContainsRegex(compiled, "\\b__wurst_EnumDestructablesInRect\\(");
        assertContainsRegex(compiled, "\\b__wurst_GetEnumDestructable\\(");
        assertContainsRegex(compiled, "\\bfunction\\s+__wurst_ForGroup\\s*\\(");
        assertContainsRegex(compiled, "\\bfunction\\s+__wurst_EnumItemsInRect\\s*\\(");
        assertContainsRegex(compiled, "\\bfunction\\s+__wurst_EnumDestructablesInRect\\s*\\(");
    }

    @Test
    public void i2sDivisionByZeroCrashTrapUsesAbortSentinelInLua() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "init",
            "    skip"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_i2sDivisionByZeroCrashTrapUsesAbortSentinelInLua.lua"), Charsets.UTF_8);
        assertDoesNotContainRegex(compiled, "tostring\\s*\\(\\s*1\\s*//\\s*0\\s*\\)");
        assertDoesNotContainRegex(compiled, "tostring\\s*\\(\\s*1\\s*/\\s*0\\s*\\)");
        assertDoesNotContainRegex(compiled, "I2S\\s*\\(\\s*1\\s*/\\s*0\\s*\\)");
        assertDoesNotContainRegex(compiled, "I2S\\s*\\(\\s*1\\s*//\\s*0\\s*\\)");
        assertTrue(compiled.contains("__wurst_abort_thread"));
        assertDoesNotContainRegex(compiled, "error\\s*\\(\\s*\"[^\"]*divide by zero[^\"]*\"");
    }

    @Test
    public void luaErrorWrapperIgnoresAbortSentinel() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "function f()",
            "    skip",
            "init",
            "    f()"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_luaErrorWrapperIgnoresAbortSentinel.lua"), Charsets.UTF_8);
        assertTrue(compiled.contains("if err == \"__wurst_abort_thread\" then return end"));
        assertTrue(compiled.contains("if err2 == \"__wurst_abort_thread\" then return end"));
    }

    @Test
    public void removesUnusedClassesFromLuaOutput() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "class Keep",
            "    function ping()",
            "        skip",
            "class Drop",
            "    function pong()",
            "        skip",
            "init",
            "    let k = new Keep()",
            "    k.ping()"
        );
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_removesUnusedClassesFromLuaOutput.lua"), Charsets.UTF_8);
        assertTrue(compiled.contains("Keep"));
        assertFalse(compiled.contains("Drop"));
    }

}
