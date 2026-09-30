package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

/**
 * The compiler-owned {@code CodeList}: on Jass a trigger whose conditions are the list, on Lua a
 * table of functions run by direct calls.
 */
public class LuaCodeListTests extends WurstScriptTest {

    /** The library's declarations: correct in source on Jass, replaced by the Lua lowering. */
    private static final String[] CODE_LIST = {
        "package CodeList",
        "import Trigger",
        "trigger array listTriggers",
        "int listCount = 0",
        "@compilerintrinsic public function codeListCreate() returns int",
        "    listTriggers[listCount] = CreateTrigger()",
        "    listCount++",
        "    return listCount - 1",
        "@compilerintrinsic public function codeListAdd(int list, code c)",
        "    listTriggers[list].addCondition(Condition(c))",
        "@compilerintrinsic public function codeListRun(int list)",
        "    listTriggers[list].evaluate()",
        "endpackage"};

    private static String[] withCodeList(String... usage) {
        java.util.List<String> lines = new java.util.ArrayList<>(java.util.Arrays.asList(CODE_LIST));
        lines.addAll(java.util.Arrays.asList(usage));
        return lines.toArray(new String[0]);
    }

    private String getFunctionBody(String output, String functionName) {
        Pattern pattern = Pattern.compile("function\\s*" + functionName + "\\s*\\([^\\n]*\\n(.*?)\\nend", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(output);
        if (!matcher.find()) {
            fail("Function " + functionName + " was not found.");
        }
        return matcher.group(1);
    }

    private String compiled(String testName) throws IOException {
        return Files.toString(new File("test-output/lua/LuaCodeListTests_" + testName + ".lua"), Charsets.UTF_8);
    }

    @Test
    public void codeListLowersToATableOfFunctionsOnLua() throws IOException {
        test().testLua(true).withStdLib().lines(withCodeList(
            "package Test",
            "import CodeList",
            "int count = 0",
            "function bump()",
            "    count++",
            "init",
            "    let list = codeListCreate()",
            "    codeListAdd(list, function bump)",
            "    codeListRun(list)",
            "endpackage"));

        String compiled = compiled("codeListLowersToATableOfFunctionsOnLua");
        assertTrue("create is a bare table", getFunctionBody(compiled, "__wurst_codeListCreate").contains("return {}"));
        assertTrue("add appends", getFunctionBody(compiled, "__wurst_codeListAdd").contains("t[#t + 1] = c"));
        assertTrue("run calls each in order",
            getFunctionBody(compiled, "__wurst_codeListRun").contains("while c do c() i = i + 1 c = t[i] end"));
        String init = getFunctionBody(compiled, "init_Test");
        assertTrue("the caller calls the stubs: " + init,
            init.contains("__wurst_codeListAdd(") && init.contains("__wurst_codeListRun("));
        assertFalse("no trigger is created to run them: " + init,
            init.contains("CreateTrigger") || init.contains("TriggerEvaluate") || init.contains("TriggerAddCondition"));
    }

    @Test
    public void codeListRunsItsCodeInOrderOnBothBackends() {
        test().testLua(true).luaOnly(false).executeProg(true).withStdLib().lines(withCodeList(
            "package Test",
            "import CodeList",
            "int count = 0",
            "int order = 0",
            "int innerList = 0",
            "function first()",
            "    count++",
            "    order = order * 10 + 1",
            "function second()",
            "    count++",
            "    order = order * 10 + 2",
            "function nested()",
            "    order = order * 10 + 3",
            "    codeListRun(innerList)",
            "init",
            "    let list = codeListCreate()",
            "    let other = codeListCreate()",
            "    innerList = other",
            "    codeListAdd(list, function first)",
            "    codeListAdd(list, function second)",
            "    codeListAdd(other, function second)",
            "    codeListRun(list)",
            "    codeListRun(list)",
            "    if count != 4 or order != 1212",
            "        testFail(\"two runs gave count=\" + count.toString() + \" order=\" + order.toString())",
            "    else",
            "        codeListRun(codeListCreate())",
            "        let outer = codeListCreate()",
            "        codeListAdd(outer, function nested)",
            "        codeListAdd(outer, function first)",
            "        order = 0",
            "        codeListRun(outer)",
            "        if order == 321",
            "            testSuccess()",
            "        else",
            "            testFail(\"nested run gave order=\" + order.toString())",
            "endpackage"));
    }

    /**
     * A trigger's conditions added while it evaluates run in that same evaluation (measured in the
     * game), so a value added while the list runs is reached in the same run and in this order.
     */
    @Test
    public void valueAddedWhileTheListRunsIsReachedInTheSameRun() {
        test().testLua(true).executeProg(true).withStdLib().lines(withCodeList(
            "package Test",
            "import CodeList",
            "int order = 0",
            "int list = 0",
            "bool added = false",
            "function late()",
            "    order = order * 10 + 9",
            "function first()",
            "    order = order * 10 + 1",
            "function adder()",
            "    order = order * 10 + 5",
            "    if not added",
            "        added = true",
            "        codeListAdd(list, function late)",
            "init",
            "    list = codeListCreate()",
            "    codeListAdd(list, function adder)",
            "    codeListAdd(list, function first)",
            "    codeListRun(list)",
            "    if order == 519",
            "        testSuccess()",
            "    else",
            "        testFail(\"first run gave order=\" + order.toString())",
            "endpackage"));
    }

    @Test
    public void codeListStaysNativeWithStackTraces() throws IOException {
        test().testLua(true).stacktraces().inline().withStdLib().lines(withCodeList(
            "package Test",
            "import CodeList",
            "int count = 0",
            "function bump()",
            "    count++",
            "init",
            "    let list = codeListCreate()",
            "    codeListAdd(list, function bump)",
            "    codeListRun(list)",
            "    print(count.toString())",
            "endpackage"));

        String compiled = compiled("codeListStaysNativeWithStackTraces");
        String init = getFunctionBody(compiled, "init_Test");
        assertTrue("the caller must call the stubs directly: " + init,
            init.contains("__wurst_codeListAdd(") && init.contains("__wurst_codeListRun("));
        assertFalse("no trigger is created to run them: " + init,
            init.contains("CreateTrigger") || init.contains("TriggerEvaluate"));
    }

    /** Functions of these names without the annotation, or of another shape, are left alone. */
    @Test
    public void otherFunctionsNamedLikeListOperationsAreNotLowered() throws IOException {
        test().testLua(true).luaOnly(false).executeProg(true).withStdLib().lines(
            "package Test",
            "int total = 0",
            "function codeListRun(int n)",
            "    total += n",
            "@compilerintrinsic function codeListAdd(int list, int n)",
            "    total += n * 10",
            "@compilerintrinsic function codeListCreate(int n) returns int",
            "    return n",
            "init",
            "    codeListRun(1)",
            "    codeListAdd(0, 2)",
            "    if codeListCreate(3) == 3 and total == 21",
            "        testSuccess()",
            "endpackage");

        String compiled = compiled("otherFunctionsNamedLikeListOperationsAreNotLowered");
        assertFalse("none of them is replaced by a stub", compiled.contains("__wurst_codeList"));
    }
}
