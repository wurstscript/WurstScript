package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

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
        while (matcher.find()) {
            String body = matcher.group(1);
            if(!body.contains(search) && mustContain) {
                fail("Function " + functionName + " must contain " + search + ".");
            }
            if(body.contains(search) && !mustContain) {
                fail("Function " + functionName + " must not contain " + search + ".");
            }
        }
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
        assertFunctionBodyContains(compiled, "testClass", "objectToIndex", true);
        assertFunctionBodyContains(compiled, "testClass", "objectFromIndex", true);
        assertFunctionBodyContains(compiled, "testClass", "cInt = cObj", false);
        assertFunctionBodyContains(compiled, "testClass", "cObj2 = cInt", false);
    }
}

