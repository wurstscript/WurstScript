package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.AssertJUnit.*;


public class LuaTranslationTests extends WurstScriptTest {


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
        assertTrue(compiled.contains("return \"\"") && !compiled.contains("return nil"));
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
        assertTrue(compiled.contains("takesString(\"\")") && !compiled.contains("takesString(nil)"));
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
        assertTrue(compiled.contains("return nil") && !compiled.contains("return \"\""));
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
        assertTrue(compiled.contains("takesObject(nil)") && !compiled.contains("takesObject(\"\")"));
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
        assertTrue(compiled.contains("return nil") && !compiled.contains("return \"\""));
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
        assertTrue(compiled.contains("takesUnit(nil)") && !compiled.contains("takesUnit(\"\")"));
    }
}

