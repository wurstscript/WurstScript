package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.peeeq.wurstscript.luaAst.LuaAst;
import de.peeeq.wurstscript.luaAst.LuaStatement;
import de.peeeq.wurstscript.luaAst.LuaVariable;
import de.peeeq.wurstscript.translation.lua.translation.LuaTranslator;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

/**
 * The Jass hashtable natives on Lua: each operation is printed where it is called, as the table
 * accesses its helper consists of, and calls the helper only where an operand has an effect.
 */
public class LuaHashtableTests extends WurstScriptTest {

    private String compiled(String testName) throws IOException {
        return Files.toString(new File("test-output/lua/LuaHashtableTests_" + testName + ".lua"), Charsets.UTF_8);
    }

    private String functionBody(String output, String functionName) {
        // Up to the closing 'end' at column 0; nested blocks are indented.
        Matcher matcher = Pattern.compile("function\\s*" + functionName + "\\s*\\([^\\n]*\\n(.*?)\\nend",
            Pattern.DOTALL).matcher(output);
        if (!matcher.find()) {
            fail("Function " + functionName + " was not found.");
        }
        return matcher.group(1);
    }

    private static void assertMatches(String what, String regex, String text) {
        assertTrue(what + ":\n" + text, Pattern.compile(regex).matcher(text).find());
    }

    /** Every operation family, in a program which runs; the shape of each one is asserted on the output. */
    @Test
    public void hashtableOperationsArePrintedWhereTheyAreCalled() throws IOException {
        test().testLua(true).withStdLib().inline().localOptimizations().executeProg().lines(
            "package Test",
            "init",
            "    let h = InitHashtable()",
            "    SaveInteger(h, 1, 2, 3)",
            "    SaveTimerHandle(h, 1, 2, null)",
            "    if LoadInteger(h, 1, 2) == 3 and HaveSavedInteger(h, 1, 2) and LoadTimerHandle(h, 1, 2) == null"
                + " and \"x\" + LoadStr(h, 1, 2) == \"x\"",
            "        RemoveSavedInteger(h, 1, 2)",
            "        FlushChildHashtable(h, 1)",
            "        FlushParentHashtable(h)",
            "        testSuccess()");

        String compiled = compiled("hashtableOperationsArePrintedWhereTheyAreCalled");
        LuaTranslator.assertNoLeakedHashtableNativeCalls(compiled);
        assertTrue("the shared empty child is a main-chunk local:\n" + compiled,
            compiled.contains("local __wurst_htEmpty = ({})\n"));
        assertTrue("the child constructor is a main-chunk local:\n" + compiled,
            compiled.contains("local function __wurst_htNewChild(t, p) \n"));

        String init = functionBody(compiled, "init_Test");
        assertFalse("no hashtable helper is called:\n" + init,
            Pattern.compile("__wurst_(InitHashtable|Save|Load|HaveSaved|RemoveSaved|Flush)\\w*\\(").matcher(init).find());
        // The library calls some with an operand which has an effect (String hashes its characters), but not these.
        for (String helper : new String[]{"InitHashtable", "LoadInteger", "HaveSavedInteger", "LoadStr", "LoadTimerHandle",
            "SaveTimerHandle", "RemoveSavedInteger", "FlushChildHashtable", "FlushParentHashtable"}) {
            assertFalse("a helper which nothing calls is not defined: " + helper,
                compiled.contains("function __wurst_" + helper + "("));
        }
        assertMatches("InitHashtable is the table constructor",
            "= \\(\\{__wurst_ht_int=\\(\\{\\}\\), __wurst_ht_bool=\\(\\{\\}\\), __wurst_ht_real=\\(\\{\\}\\), "
                + "__wurst_ht_str=\\(\\{\\}\\), __wurst_ht_handle=\\(\\{\\}\\), \\}\\)", init);
        assertMatches("a save creates the child only when there is none",
            "\n\\s*;\\((\\w+)\\.__wurst_ht_int\\[1\\] or __wurst_htNewChild\\(\\1\\.__wurst_ht_int, 1\\)\\)\\[2\\] = 3\n",
            init);
        assertMatches("a handle save goes to the handle subtable",
            ";\\((\\w+)\\.__wurst_ht_handle\\[1\\] or __wurst_htNewChild\\(\\1\\.__wurst_ht_handle, 1\\)\\)\\[2\\] = nil",
            init);
        assertMatches("an int load reads through the empty child and answers 0",
            "\\(\\(\\w+\\.__wurst_ht_int\\[1\\] or __wurst_htEmpty\\)\\[2\\] or 0\\)", init);
        assertMatches("a test is the same read",
            "\\(\\(\\w+\\.__wurst_ht_int\\[1\\] or __wurst_htEmpty\\)\\[2\\] ~= nil\\)", init);
        assertMatches("a handle load answers nil when absent, as the helper did",
            "\\(\\(\\w+\\.__wurst_ht_handle\\[1\\] or __wurst_htEmpty\\)\\[2\\] == nil\\)", init);
        assertMatches("so does a string load, which the concatenation guards",
            "\\(\\w+\\.__wurst_ht_str\\[1\\] or __wurst_htEmpty\\)\\[2\\] or \"\"\\)", init);
        assertMatches("a remove never writes the shared empty child",
            "if (\\w+)\\.__wurst_ht_int\\[1\\] then\\s+\\1\\.__wurst_ht_int\\[1\\]\\[2\\] = nil\\s+end", init);
        for (String subtable : new String[]{"int", "bool", "real", "str", "handle"}) {
            assertMatches("FlushChildHashtable drops the child of every kind",
                "\\w+\\.__wurst_ht_" + subtable + "\\[1\\] = nil", init);
            assertMatches("FlushParentHashtable replaces every subtable",
                "\\w+\\.__wurst_ht_" + subtable + " = \\(\\{\\}\\)", init);
        }
        assertFalse("a flush tests no subtable, every one is always there:\n" + init,
            Pattern.compile("if \\w+\\.__wurst_ht_\\w+ then").matcher(init).find());
    }

    private static final String[] BEHAVIOUR = {
        "package Test",
        "let ct = compiletime(InitHashtable())",
        "@compiletime function fill()",
        "    SaveInteger(ct, 1, 2, 12)",
        "    SaveStr(ct, 1, 3, \"ct\")",
        "    SaveBoolean(ct, 4, 5, true)",
        "    SaveReal(ct, 6, 7, 1.5)",
        "function check(boolean ok, string what)",
        "    if not ok",
        "        testFail(what)",
        "init",
        "    let h = InitHashtable()",
        "    check(LoadInteger(h, 1, 1) == 0 and LoadReal(h, 1, 1) == 0. and not LoadBoolean(h, 1, 1)"
            + " and \"x\" + LoadStr(h, 1, 1) == \"x\" and LoadTimerHandle(h, 1, 1) == null, \"an absent parent key reads defaults\")",
        "    check(not HaveSavedInteger(h, 1, 1) and not HaveSavedReal(h, 1, 1) and not HaveSavedBoolean(h, 1, 1)"
            + " and not HaveSavedString(h, 1, 1) and not HaveSavedHandle(h, 1, 1), \"an absent parent key holds nothing\")",
        "    SaveInteger(h, 1, 1, 11)",
        "    check(LoadInteger(h, 1, 1) == 11 and HaveSavedInteger(h, 1, 1), \"int\")",
        "    check(LoadInteger(h, 1, 2) == 0 and not HaveSavedInteger(h, 1, 2), \"an absent child key reads 0\")",
        "    check(LoadReal(h, 1, 1) == 0. and not HaveSavedReal(h, 1, 1), \"each kind has its own entries\")",
        "    SaveBoolean(h, 1, 1, false)",
        "    check(HaveSavedBoolean(h, 1, 1) and not LoadBoolean(h, 1, 1), \"a stored false\")",
        "    SaveBoolean(h, 1, 2, true)",
        "    check(LoadBoolean(h, 1, 2), \"a stored true\")",
        "    SaveReal(h, 1, 1, 2.5)",
        "    check(LoadReal(h, 1, 1) == 2.5 and HaveSavedReal(h, 1, 1), \"real\")",
        "    SaveStr(h, 1, 1, \"s\")",
        "    check(LoadStr(h, 1, 1) == \"s\" and HaveSavedString(h, 1, 1), \"string\")",
        "    let t = CreateTimer()",
        "    SaveTimerHandle(h, 1, 1, t)",
        "    check(LoadTimerHandle(h, 1, 1) == t and HaveSavedHandle(h, 1, 1), \"handle\")",
        "    RemoveSavedInteger(h, 1, 1)",
        "    check(not HaveSavedInteger(h, 1, 1) and LoadInteger(h, 1, 1) == 0 and LoadReal(h, 1, 1) == 2.5,"
            + " \"a remove takes only its kind\")",
        "    RemoveSavedInteger(h, 9, 1)",
        "    check(LoadInteger(h, 9, 1) == 0 and not HaveSavedInteger(h, 9, 1), \"a remove under an absent parent key\")",
        "    RemoveSavedBoolean(h, 1, 1)",
        "    RemoveSavedReal(h, 1, 1)",
        "    RemoveSavedString(h, 1, 1)",
        "    RemoveSavedHandle(h, 1, 1)",
        "    check(not HaveSavedBoolean(h, 1, 1) and not HaveSavedReal(h, 1, 1) and not HaveSavedString(h, 1, 1)"
            + " and not HaveSavedHandle(h, 1, 1) and LoadBoolean(h, 1, 2), \"removes of every kind\")",
        "    SaveInteger(h, 2, 1, 21)",
        "    SaveStr(h, 2, 2, \"t\")",
        "    FlushChildHashtable(h, 2)",
        "    check(not HaveSavedInteger(h, 2, 1) and not HaveSavedString(h, 2, 2) and LoadBoolean(h, 1, 2),"
            + " \"a flushed child is gone, the others stay\")",
        "    SaveInteger(h, 2, 1, 22)",
        "    check(LoadInteger(h, 2, 1) == 22, \"a flushed child takes a save\")",
        "    FlushParentHashtable(h)",
        "    check(not HaveSavedBoolean(h, 1, 2) and not HaveSavedInteger(h, 2, 1), \"a flushed hashtable is empty\")",
        "    SaveInteger(h, 1, 1, 31)",
        "    check(LoadInteger(h, 1, 1) == 31, \"a flushed hashtable takes a save\")",
        "    check(LoadInteger(ct, 1, 2) == 12 and LoadStr(ct, 1, 3) == \"ct\" and LoadBoolean(ct, 4, 5)"
            + " and LoadReal(ct, 6, 7) == 1.5 and not HaveSavedInteger(ct, 1, 3), \"a compile-time hashtable\")",
        "    testSuccess()"
    };

    /** The same answers on Jass, in the interpreter and on Lua, where the operations are printed in place. */
    @Test
    public void hashtableOperationsBehaveAsTheNatives() {
        test().testLua(true).luaOnly(false).withStdLib().executeProg().executeProgOnlyAfterTransforms().lines(BEHAVIOUR);
    }

    /** As shipped: inlined, so the operands are the caller's literals and locals, and optimised. */
    @Test
    public void hashtableOperationsBehaveAsTheNativesWhenOptimized() {
        test().testLua(true).withStdLib().inline().localOptimizations().executeProg().lines(BEHAVIOUR);
    }

    /**
     * An operand with an effect has to be evaluated once and in the order of the call, which the operation
     * printed in place does not do (a save reads its parent key again to create the child, a remove skips its
     * child key under an absent parent key). Such a call stays the call of its helper, which is then defined;
     * one with plain operands in the same program is still printed in place.
     */
    @Test
    public void anOperandWithAnEffectKeepsTheCallOfTheHelper() throws IOException {
        test().testLua(true).luaOnly(false).withStdLib().inline().localOptimizations().executeProg().lines(
            "package Test",
            "string trace = \"\"",
            "@noinline function key(string tag, int k) returns int",
            "    trace += tag",
            "    return k",
            "init",
            "    let h = InitHashtable()",
            "    SaveInteger(h, key(\"p\", 1), key(\"c\", 2), key(\"v\", 3))",
            "    SaveInteger(h, 1, 4, key(\"w\", 5))",
            "    SaveInteger(h, 1, 6, 7)",
            "    if LoadInteger(h, key(\"P\", 1), key(\"C\", 2)) != 3 or not HaveSavedInteger(h, 1, key(\"H\", 4))",
            "        testFail(\"stored through the helper\")",
            "    RemoveSavedInteger(h, key(\"r\", 9), key(\"s\", 2))",
            "    RemoveSavedInteger(h, key(\"R\", 1), key(\"S\", 2))",
            "    FlushChildHashtable(h, key(\"f\", 1))",
            "    if trace != \"pcvwPCHrsRSf\"",
            "        testFail(\"operands evaluated as \" + trace)",
            "    if HaveSavedInteger(h, 1, 2) or HaveSavedInteger(h, 1, 4) or LoadInteger(h, 9, 2) != 0",
            "        testFail(\"removed and flushed\")",
            "    testSuccess()");

        String compiled = compiled("anOperandWithAnEffectKeepsTheCallOfTheHelper");
        LuaTranslator.assertNoLeakedHashtableNativeCalls(compiled);
        String init = functionBody(compiled, "init_Test");
        for (String helper : new String[]{"SaveInteger", "LoadInteger", "HaveSavedInteger", "RemoveSavedInteger",
            "FlushChildHashtable"}) {
            assertTrue("an operation with an effect in an operand calls its helper: " + helper + "\n" + init,
                init.contains("__wurst_" + helper + "("));
            assertTrue("a helper which is called is defined: " + helper + "\n" + compiled,
                compiled.contains("function __wurst_" + helper + "("));
        }
        assertMatches("a save with plain operands is printed in place",
            ";\\((\\w+)\\.__wurst_ht_int\\[1\\] or __wurst_htNewChild\\(\\1\\.__wurst_ht_int, 1\\)\\)\\[6\\] = 7", init);
        assertMatches("a load with plain operands is printed in place",
            "\\(\\w+\\.__wurst_ht_int\\[9\\] or __wurst_htEmpty\\)\\[2\\] or 0\\)", init);
        assertFalse("a helper nothing calls is not defined:\n" + compiled,
            compiled.contains("function __wurst_LoadReal(") || compiled.contains("function __wurst_InitHashtable("));
        assertFalse("the helper no longer tests for a missing subtable:\n" + compiled,
            compiled.contains("if t == nil"));
    }

    /**
     * The library's Table keys its hashtable by the instance, {@code this castTo int}, which is {@code (t or 0)} on
     * Lua: once inlined, that cast is an operand of the native, and it can neither raise nor change anything, so the
     * operation is still printed in place.
     */
    @Test
    public void aTableOperationIsPrintedInPlace() throws IOException {
        test().testLua(true).withStdLib().inline().localOptimizations().executeProg().lines(
            "package Test",
            "import Table",
            "init",
            "    let t = new Table()",
            "    t.saveInt(5, 7)",
            "    if t.loadInt(5) == 7 and t.hasInt(5) and not t.hasInt(6)",
            "        t.removeInt(5)",
            "        if not t.hasInt(5)",
            "            testSuccess()");

        String init = functionBody(compiled("aTableOperationIsPrintedInPlace"), "init_Test");
        assertFalse("no hashtable helper is called:\n" + init,
            Pattern.compile("__wurst_(Save|Load|HaveSaved|RemoveSaved)\\w*\\(").matcher(init).find());
        assertMatches("the save is printed in place",
            ";\\(\\w+\\.__wurst_ht_int\\[[^\\]]+\\] or __wurst_htNewChild\\(\\w+\\.__wurst_ht_int, [^\\n]+\\)\\)\\[5\\] = 7",
            init);
        assertMatches("the load is printed in place under the instance id",
            "\\(Table_ht\\.__wurst_ht_int\\[\\(\\w+ or 0\\)\\] or __wurst_htEmpty\\)\\[5\\] or 0\\)", init);
    }

    /**
     * HashList keys its hashtable by elem castTo int, an old-generics value as an int, which reads its variable and
     * nothing else. Counted as an operand with an effect, it kept every inlined count and add a helper call.
     */
    @Test
    public void anOldGenericsKeyIsPrintedInPlace() throws IOException {
        test().testLua(true).withStdLib().inline().localOptimizations().executeProg().lines(
            "package Test",
            "import HashList",
            "init",
            "    let l = new HashList<int>()",
            "    l.add(3)",
            "    l.add(3)",
            "    if l.has(3) and not l.has(4) and l.size() == 2",
            "        testSuccess()");

        String lua = compiled("anOldGenericsKeyIsPrintedInPlace");
        String init = functionBody(lua, "init_Test");
        assertFalse("no hashtable helper is called:\n" + init,
            Pattern.compile("__wurst_(Save|Load|HaveSaved|RemoveSaved)\\w*\\(").matcher(init).find());
    }

    /**
     * Lua reads a statement which starts with '(' as the arguments of a call ending the previous statement, so an
     * assignment whose target starts with '(' is printed after a ';'. Here the save follows an assignment which ends
     * in a parenthesised expression; the harness compiles the script with luac.
     */
    @Test
    public void aStoreStartingWithAParenthesisIsSeparatedFromTheStatementBefore() throws IOException {
        test().testLua(true).withStdLib().executeProg().lines(
            "package Test",
            "init",
            "    let h = InitHashtable()",
            "    var x = 5",
            "    x = x + 1",
            "    SaveInteger(h, 1, 2, x)",
            "    if LoadInteger(h, 1, 2) == 6",
            "        testSuccess()");

        String init = functionBody(compiled("aStoreStartingWithAParenthesisIsSeparatedFromTheStatementBefore"),
            "init_Test");
        assertMatches("the save follows a statement ending in a parenthesis, after a ';'",
            "= \\(\\w+ \\+ 1\\)\n\\s*;\\(", init);
    }

    @Test
    public void anAssignmentIsPrintedAfterASemicolonOnlyWhenItsTargetStartsWithAParenthesis() {
        LuaVariable a = LuaAst.LuaVariable("a", LuaAst.LuaNoExpr());
        LuaVariable b = LuaAst.LuaVariable("b", LuaAst.LuaNoExpr());
        assertEquals(";(a or b)[1] = 2", print(LuaAst.LuaAssignment(LuaAst.LuaExprArrayAccess(
            LuaAst.LuaExprBinary(LuaAst.LuaExprVarAccess(a), LuaAst.LuaOpOr(), LuaAst.LuaExprVarAccess(b)),
            LuaAst.LuaExprlist(LuaAst.LuaExprIntVal("1"))), LuaAst.LuaExprIntVal("2"))));
        assertEquals(";(nil).f = 2", print(LuaAst.LuaAssignment(
            LuaAst.LuaExprFieldAccess(LuaAst.LuaExprNull(), "f"), LuaAst.LuaExprIntVal("2"))));
        assertEquals("a[1].f = 2", print(LuaAst.LuaAssignment(LuaAst.LuaExprFieldAccess(LuaAst.LuaExprArrayAccess(
            LuaAst.LuaExprVarAccess(a), LuaAst.LuaExprlist(LuaAst.LuaExprIntVal("1"))), "f"), LuaAst.LuaExprIntVal("2"))));
        assertEquals("InitHashtable = 2", print(LuaAst.LuaAssignment(
            LuaAst.LuaLiteral("InitHashtable"), LuaAst.LuaExprIntVal("2"))));
    }

    private static String print(LuaStatement statement) {
        StringBuilder sb = new StringBuilder();
        statement.print(sb, 0);
        return sb.toString();
    }

    /**
     * A load and a HaveSaved test only read, as the Jass natives they stand for do, so the optimizer drops one whose
     * result nothing uses. Before, it could not tell: the stub has another name than the native.
     */
    @Test
    public void anUnusedLoadIsDropped() throws IOException {
        test().testLua(true).withStdLib().inline().localOptimizations().executeProg().lines(
            "package Test",
            "@noinline function discard(hashtable h, int p)",
            "    let ignoredInt = LoadInteger(h, p, 1)",
            "    let ignoredString = LoadStr(h, p, 2)",
            "    let ignoredPresence = HaveSavedInteger(h, p, 1)",
            "init",
            "    let h = InitHashtable()",
            "    SaveInteger(h, 1, 1, 5)",
            "    discard(h, 1)",
            "    if LoadInteger(h, 1, 1) == 5",
            "        testSuccess()");

        String compiled = compiled("anUnusedLoadIsDropped");
        int start = compiled.indexOf("function discard(");
        assertTrue("expected function discard:\n" + compiled, start >= 0);
        String discard = compiled.substring(start, compiled.indexOf("\nend", start));
        assertFalse("the unused reads are dropped: " + discard,
            discard.contains("__wurst_ht") || discard.contains("wurstExpr") || discard.contains("__wurst_Load")
                || discard.contains("__wurst_HaveSaved"));
    }
}
