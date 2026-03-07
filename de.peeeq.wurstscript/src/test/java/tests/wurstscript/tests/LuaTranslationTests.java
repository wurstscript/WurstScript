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
    public void numericOpsAreGuardedWithEnsure() throws IOException {
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
        String compiled = Files.toString(new File("test-output/lua/LuaTranslationTests_numericOpsAreGuardedWithEnsure.lua"), Charsets.UTF_8);
        assertFunctionBodyContains(compiled, "cmp", "intEnsure", true);
        assertFunctionBodyContains(compiled, "divi", "intEnsure", true);
        assertTrue(compiled.contains("function addi") && (compiled.contains("realEnsure(a2)") || compiled.contains("intEnsure(a2)")));
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
        assertFalse(compiled.contains("local v0"));
        assertFalse(compiled.contains("local v209"));
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
