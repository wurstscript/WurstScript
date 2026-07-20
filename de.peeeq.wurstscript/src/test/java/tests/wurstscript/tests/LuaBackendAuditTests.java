package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Regression tests for the Wurst -> Lua backend audit.
 *
 * Each test is a minimal repro for a concrete emitted-code bug. All tests use
 * {@code testLua(true)}, which syntax-checks the generated Lua with luac;
 * several bugs manifest directly as luac syntax errors. Behavioral properties
 * are additionally asserted structurally on the generated source.
 */
public class LuaBackendAuditTests extends WurstScriptTest {

    private String compiledLua(String testName) throws IOException {
        return Files.toString(new File("test-output/lua/LuaBackendAuditTests_" + testName + ".lua"), Charsets.UTF_8);
    }

    /**
     * A bare {@code return} inside a vararg loop used to truncate the literal
     * {@code end} that closed the loop, producing unparseable Lua
     * (caught here by the luac syntax check).
     */
    @Test
    public void varargLoopWithBareReturn() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "function firstOr(vararg int xs) returns int",
            "    for x in xs",
            "        return x",
            "    return -1",
            "init",
            "    if firstOr(7, 8) == 7",
            "        testSuccess()"
        );
        String compiled = compiledLua("varargLoopWithBareReturn");
        // the loop must be built from real AST nodes, not from literal for/end lines
        assertFalse("vararg loop must not be emitted via literal 'for' lines",
            compiled.contains("for i=1,"));
    }

    /**
     * All fields of a class hierarchy are flattened into one instance table.
     * A closure-captured local whose name collides with an (IM-prefixed)
     * superclass field name used to silently alias that field: both ended up
     * as the same table key, so writes to one clobbered the other.
     */
    @Test
    public void hierarchyFieldsDoNotAlias() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "abstract class Cb",
            "    int y = 100",
            "    abstract function run() returns int",
            "    function base() returns int",
            "        return y",
            "init",
            "    let Cb_y = 5",
            "    Cb c = () -> Cb_y",
            "    if c.run() == 5 and c.base() == 100",
            "        testSuccess()"
        );
        String compiled = compiledLua("hierarchyFieldsDoNotAlias");
        // no allocation table may contain the same key twice
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("\\(\\{([^}]*)\\}\\)").matcher(compiled);
        while (m.find()) {
            String[] keys = m.group(1).split(",");
            java.util.Set<String> seen = new java.util.HashSet<>();
            for (String entry : keys) {
                int eq = entry.indexOf('=');
                if (eq > 0) {
                    String key = entry.substring(0, eq).trim();
                    assertTrue("duplicate field key '" + key + "' in allocation table: " + m.group(0),
                        seen.add(key));
                }
            }
        }
    }

    /**
     * Constructor helper methods are named create, create1, create2, ... in
     * class-translation order, while method dispatch slots use (normalized)
     * user method names. Both live in the same class-table key namespace, so
     * a user method whose dispatch slot name equals a class's constructor
     * name used to overwrite the constructor at main() time: allocations then
     * called the user method instead of the constructor.
     */
    @Test
    public void dispatchSlotMustNotStompConstructor() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "interface I",                       // class 0 -> constructor "create"
            "    function create2() returns int", // bare slot name "create2"
            "        return 42",
            "class A",                           // class 1 -> constructor "create1"
            "class C implements I",              // class 2 -> constructor "create2"
            "    int v = 7",
            "init",
            "    let a = new A()",
            "    let c = new C()",
            "    if c.v == 7 and c.create2() == 42 and a != null",
            "        testSuccess()"
        );
        String compiled = compiledLua("dispatchSlotMustNotStompConstructor");
        // no class may have a dispatch slot assigned over its own constructor:
        // for every "function X:NAME(" definition there must be no "X.NAME =" assignment
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("function\\s+([A-Za-z0-9_]+):(create\\d*)\\s*\\(").matcher(compiled);
        while (m.find()) {
            String cls = m.group(1);
            String ctor = m.group(2);
            assertFalse("constructor " + cls + ":" + ctor + " is overwritten by a dispatch slot assignment",
                compiled.contains(cls + "." + ctor + " ="));
        }
    }

    /**
     * Dispatch alias generation derives a "semantic name" from everything
     * after the last underscore of a method name. A user method containing
     * an underscore (my_x) therefore produced phantom alias slots
     * (Base_x, Child_x) that collided with the real dispatch slot of an
     * unrelated method named x — and could win the slot, so calls to x()
     * dispatched into my_x(int) with a missing argument.
     */
    @Test
    public void underscoreMethodNamesDoNotStompUnrelatedDispatchSlots() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class Base",
            "    function x() returns int",
            "        return 1",
            "class Child extends Base",
            "    function my_x(int i) returns int",
            "        return i + 100",
            "class Child2 extends Base",
            "    override function x() returns int",
            "        return 2",
            "init",
            "    Base b = new Child()",
            "    Base b2 = new Child2()",
            "    let c = new Child()",
            "    if b.x() == 1 and b2.x() == 2 and c.my_x(1) == 101",
            "        testSuccess()"
        );
        String compiled = compiledLua("underscoreMethodNamesDoNotStompUnrelatedDispatchSlots");
        assertFalse("phantom semantic slot must not bind my_x over the real x dispatch slot",
            compiled.contains("Child.Base_x = Child_Child_my_x"));
    }

    private static final String[] DIV_MOD_PROG = {
        "package Test",
        "native testSuccess()",
        "function d(int a, int b) returns int",
        "    return a div b",
        "function m(int a, int b) returns int",
        "    return a mod b",
        "init",
        "    if d(-7, 2) == -3 and m(-7, 2) == 1 and d(7, -2) == -3 and m(7, -2) == 1",
        "        if d(7, 2) == 3 and m(7, 2) == 1 and d(-8, 2) == -4 and m(-8, 2) == 0",
        "            testSuccess()"
    };

    /**
     * Reference semantics: integer div truncates toward zero and mod follows
     * Blizzard.j's ModuloInteger (truncated remainder, plus divisor when
     * negative). This guards the interpreter/Jass behavior the Lua backend
     * must match.
     */
    @Test
    public void integerDivModReferenceSemanticsInInterpreter() {
        test().executeProg().lines(DIV_MOD_PROG);
    }

    /**
     * EliminateLocalTypes#transformProgram runs after LuaEnsureFunctions
     * builds __wurst_ensureStr's/__wurst_stringConcat's bodies, and
     * unconditionally rewrites every string-typed ImNull node in the whole
     * program - including inside those functions themselves - into
     * ImStringVal(""). If their own "x ~= nil" checks were tagged with the
     * string type, that rewrite would silently turn them into "x ~= \"\"",
     * so a genuinely nil Lua value (e.g. an unset bound-generic string
     * field) would read as "not nil", skip normalization, and come out as
     * the literal string "nil" via tostring() instead of "" - or, for
     * stringConcat, get passed straight into raw ".." concatenation.
     * LuaEnsureFunctions#notNull tags its ImNull sentinel with ImAnyType
     * specifically to stay exempt from that rewrite.
     */
    @Test
    public void ensureStrAndStringConcatNilChecksSurviveEliminateLocalTypes() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "string array names",
            "function join(string a, string b) returns string",
            "    return a + b",
            "init",
            "    if names[5] == \"\" and join(\"a\", \"b\") == \"ab\"",
            "        testSuccess()"
        );
        String compiled = compiledLua("ensureStrAndStringConcatNilChecksSurviveEliminateLocalTypes");
        assertNilCheckNotCorruptedToEmptyStringCheck(compiled, "__wurst_ensureStr(");
        assertNilCheckNotCorruptedToEmptyStringCheck(compiled, "__wurst_stringConcat(");
    }

    private void assertNilCheckNotCorruptedToEmptyStringCheck(String compiled, String functionNamePrefix) {
        int fnStart = compiled.indexOf("function " + functionNamePrefix);
        assertTrue("expected " + functionNamePrefix + " to be present", fnStart >= 0);
        int fnEnd = compiled.indexOf("\nend", fnStart);
        String fnBody = compiled.substring(fnStart, fnEnd);
        assertTrue(functionNamePrefix + " must check for real nil", fnBody.contains("== nil"));
        assertFalse(functionNamePrefix + "'s nil check must not be corrupted into an empty-string check",
            fnBody.contains("== \"\""));
    }

    /**
     * The Lua backend used to emit floored {@code //} for div and
     * {@code math.floor(a % b)} for mod, which disagree with the Jass
     * backend and the interpreter for negative operands
     * (e.g. -7 div 2 was -4 instead of -3, and 7 mod -2 was -1 instead of 1).
     *
     * Div/mod are now lowered to portable IM functions before the optimizer
     * runs (see LuaNativeLowering#lowerDivMod), so calls with constant
     * arguments - like the ones below - may get inlined away entirely rather
     * than showing up as a helper call in the output. The floor-div/fmod
     * *native* they delegate to (Wurst has no such IM operator) always
     * survives somewhere in the output, inlined or not, so checking for it
     * is robust regardless of the inliner's decision.
     */
    @Test
    public void integerDivModMatchJassSemanticsInLua() throws IOException {
        test().testLua(true).executeProg().lines(DIV_MOD_PROG);
        String compiled = compiledLua("integerDivModMatchJassSemanticsInLua");
        assertTrue("div must go through the truncating floor-div correction",
            compiled.contains("__wurst_rawFloorDivInt("));
        assertTrue("mod must go through the ModuloInteger-compatible fmod correction",
            compiled.contains("__wurst_rawFmodInt("));
        assertFalse("mod/div must not use math.floor directly",
            compiled.contains("math.floor"));
    }

    /**
     * ErrorHandling.error()'s deliberate {@code I2S(1 div 0)} crash trap
     * relies on {@code lua.translation.ExprTranslation#isIntentionalThreadAbortCall}
     * pattern-matching the exact {@code ImOperatorCall(DIV_INT, [1, 0])} shape
     * as I2S's sole argument, to turn it into the {@code __wurst_abort_thread}
     * sentinel every callback xpcall handler ignores. The div/mod lowering
     * (LuaNativeLowering#lowerDivMod) runs before that check and rewrites
     * every DIV_INT it sees into a __wurst_intDiv(...) call - which would
     * destroy that exact shape and replace the sentinel with a real Lua
     * {@code n//0} runtime error, breaking every callback error handler's
     * "was this an intentional abort" check. This guards that the lowering
     * carves out an exception for precisely this pattern.
     */
    @Test
    public void i2sDivByZeroAbortTrapSurvivesDivModLowering() throws IOException {
        test().testLua(true).lines(
            "package Test",
            "native testSuccess()",
            "native I2S(int i) returns string",
            "native print(string s)",
            "function crashTrap() returns string",
            "    return I2S(1 div 0)",
            "init",
            "    print(crashTrap())",
            "    testSuccess()"
        );
        String compiled = compiledLua("i2sDivByZeroAbortTrapSurvivesDivModLowering");
        assertTrue("the abort trap must still produce the sentinel error call",
            compiled.contains("error(\"__wurst_abort_thread\", 0)"));
        assertFalse("the abort trap's 1 div 0 must not be lowered to a helper call",
            compiled.contains("__wurst_intDiv(1, 0)"));
    }

    /**
     * Div/mod helpers are real IM functions now, created only when the
     * program actually uses div/mod, instead of unconditionally-emitted Lua
     * source (as before) - so a program that never divides/mods must not
     * carry any of this machinery in its output.
     */
    @Test
    public void divModHelpersAreOmittedWhenUnused() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "init",
            "    if 1 + 1 == 2",
            "        testSuccess()"
        );
        String compiled = compiledLua("divModHelpersAreOmittedWhenUnused");
        assertFalse("unused div helper must not be emitted",
            compiled.contains("__wurst_intDiv"));
        assertFalse("unused mod helpers must not be emitted",
            compiled.contains("__wurst_mod"));
        assertFalse("unused raw floor-div/fmod natives must not be emitted",
            compiled.contains("__wurst_rawF"));
    }

    /**
     * A non-constant div/mod call (parameters, not literals) has no
     * constant-folding upside, so the inliner leaves it as a call to the
     * shared helper - proving div/mod is optimizable at all now (it used to
     * be opaque, always-emitted Lua source the IM optimizer never saw).
     */
    @Test
    public void nonConstantDivModCallsUseSharedHelper() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "function d(int a, int b) returns int",
            "    return a div b",
            "function m(int a, int b) returns int",
            "    return a mod b",
            "init",
            "    int x = 7",
            "    int y = -2",
            "    if d(x, y) == -3 and m(x, y) == 1 and d(y, x) == 0 and m(y, x) == 5",
            "        testSuccess()"
        );
        String compiled = compiledLua("nonConstantDivModCallsUseSharedHelper");
        assertEquals("expected exactly one shared int-div helper definition",
            1, countOccurrences(compiled, "function __wurst_intDiv("));
        assertEquals("expected exactly one shared int-mod helper definition",
            1, countOccurrences(compiled, "function __wurst_modInt("));
    }

    /**
     * String concatenation is lowered to a synthetic stringConcat IM function.
     * The polyfill and its call sites used to be linked only by both happening
     * to print the same Lua name "stringConcat" - a user function also named
     * stringConcat could hijack it, or get renamed away by the collision. The
     * polyfill now has its own permanently distinct internal name
     * (__wurst_stringConcat, see LuaEnsureFunctions), so a user-defined
     * stringConcat no longer collides with it at all and needs no renaming.
     */
    @Test
    public void userFunctionNamedStringConcatDoesNotBreakConcatenation() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "function stringConcat(string a, string b) returns string",
            "    return \"wrong\"",
            "function join(string a, string b) returns string",
            "    return a + b",
            "init",
            "    if join(\"a\", \"b\") == \"ab\" and stringConcat(\"a\", \"b\") == \"wrong\"",
            "        testSuccess()"
        );
        String compiled = compiledLua("userFunctionNamedStringConcatDoesNotBreakConcatenation");
        assertTrue("user stringConcat no longer collides with the internal polyfill and keeps its own name",
            compiled.contains("function stringConcat("));
        assertFalse("the internal polyfill must not leak the bare 'stringConcat' name",
            compiled.contains("function __wurst_stringConcat1("));
    }

    /**
     * Deferred bootstrap (global defaults, class dispatch tables, typecasting
     * maps) used to be prepended only to main(). WC3 calls config() before
     * main(), so any translated global or class metadata reachable from
     * config saw nil. Both entry points must run the bootstrap first.
     */
    @Test
    public void deferredInitRunsForConfigToo() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class C",
            "    function f() returns int",
            "        return 1",
            "int array counts",
            "init",
            "    let c = new C()",
            "    counts[3] = c.f()",
            "    if counts[3] == 1",
            "        testSuccess()"
        );
        String compiled = compiledLua("deferredInitRunsForConfigToo");
        java.util.regex.Matcher main = java.util.regex.Pattern
            .compile("function main\\(\\)\\s*\\n\\s*([A-Za-z0-9_]+)\\(").matcher(compiled);
        assertTrue("main must start with the bootstrap call", main.find());
        String bootstrapCall = main.group(1);
        assertTrue("bootstrap helper must be a __wurst function but was " + bootstrapCall,
            bootstrapCall.startsWith("__wurst"));
        java.util.regex.Matcher config = java.util.regex.Pattern
            .compile("function config\\(\\)\\s*\\n\\s*" + java.util.regex.Pattern.quote(bootstrapCall) + "\\(").matcher(compiled);
        assertTrue("config must start with the same bootstrap call", config.find());
    }

    /**
     * Instances used to get a fresh {@code {__index = Class}} metatable per
     * allocation — pure garbage. All instances of a class share one metatable.
     */
    @Test
    public void classInstancesShareOneMetatablePerClass() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class Foo",
            "    int v = 1",
            "init",
            "    let a = new Foo()",
            "    let b = new Foo()",
            "    if a.v + b.v == 2 and a != b",
            "        testSuccess()"
        );
        String compiled = compiledLua("classInstancesShareOneMetatablePerClass");
        assertTrue("expected a shared per-class metatable variable",
            compiled.contains("Foo_mt = ({__index=Foo, })"));
        assertFalse("create must not allocate a metatable per instance",
            compiled.contains("setmetatable(new_inst, ({"));
    }

    /**
     * Reading a never-written array slot must not permanently store an entry
     * for it - merely probing a sparse array would otherwise grow it
     * unboundedly. Immutable (primitive) defaults are served by a dedicated
     * index function that never writes back into the array table; only
     * table-typed defaults (tuples, nested arrays) store on first read,
     * because each slot needs its own, independently mutable default value.
     */
    @Test
    public void primitiveArrayReadsDoNotMaterializeEntries() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int array counts",
            "tuple pair(int x, int y)",
            "pair array ps",
            "init",
            "    counts[5] = 2",
            "    ps[2] = pair(4, 5)",
            "    let unset = ps[7]",
            "    if counts[3] + counts[5] == 2 and ps[2].x == 4 and unset.y == 0",
            "        testSuccess()"
        );
        String compiled = compiledLua("primitiveArrayReadsDoNotMaterializeEntries");
        int fnStart = compiled.indexOf("function __wurst_arrIndex_integer(");
        assertTrue("expected a dedicated index function for int array defaults", fnStart >= 0);
        int fnEnd = compiled.indexOf("\nend", fnStart);
        assertTrue("could not find end of int array index function", fnEnd >= 0);
        assertFalse("primitive array default reads must not write back into the array table",
            compiled.substring(fnStart, fnEnd).contains("="));

        assertTrue("tuple array defaults must still be lazily materialized per-slot for identity",
            compiled.contains("function __wurst_arrIndex("));
    }

    /**
     * Array-default infrastructure (metatable + index function) is shared
     * across every array with the same entry type, not allocated fresh per
     * array instance - mirrors the shared per-class instance metatable
     * ({@link #classInstancesShareOneMetatablePerClass}).
     */
    @Test
    public void arraysOfSameEntryTypeShareDefaultMetatable() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int array a",
            "int array b",
            "int array c",
            "init",
            "    a[1] = 1",
            "    b[1] = 2",
            "    c[1] = 3",
            "    if a[1] + b[1] + c[1] == 6 and a[9] == 0 and b[9] == 0",
            "        testSuccess()"
        );
        String compiled = compiledLua("arraysOfSameEntryTypeShareDefaultMetatable");
        assertEquals("expected exactly one shared metatable declaration for all int arrays",
            1, countOccurrences(compiled, "__wurst_arrMt_integer = "));
        assertEquals("expected exactly one shared index function for all int arrays",
            1, countOccurrences(compiled, "function __wurst_arrIndex_integer("));
        assertEquals("expected one setmetatable call per array instance",
            3, countOccurrences(compiled, "setmetatable(({}), __wurst_arrMt_integer)"));
    }

    /**
     * Handle-typed arrays (WC3 native types, not user classes) default to
     * nil, same as an untouched Lua table key - they must not pay for any
     * metatable machinery at all.
     */
    @Test
    public void handleTypedArraysAllocateNoMetatable() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "nativetype customHandle",
            "customHandle array hs",
            "init",
            "    if hs[3] == null",
            "        testSuccess()"
        );
        String compiled = compiledLua("handleTypedArraysAllocateNoMetatable");
        assertFalse("a nil-default array must not allocate any array-default metatable",
            compiled.contains("__wurst_arrMt"));
        assertFalse("a nil-default array must not allocate any array-default index function",
            compiled.contains("__wurst_arrIndex"));
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        int idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) >= 0) {
            count++;
            idx += needle.length();
        }
        return count;
    }

    /**
     * The wc3shim's GetLocalPlayer used to return a generic enum-handle table
     * ({handleKind, value}) while the generated Player/GetPlayerId fallbacks
     * use {id = x} with their own cache — so GetPlayerId(GetLocalPlayer())
     * returned nil and Player(0) was not identical to GetLocalPlayer().
     */
    @Test
    public void localPlayerHandleIsCompatibleWithPlayerAndGetPlayerId() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "nativetype player",
            "native Player(int id) returns player",
            "native GetPlayerId(player p) returns int",
            "native GetLocalPlayer() returns player",
            "init",
            "    if GetPlayerId(GetLocalPlayer()) == 0 and Player(0) == GetLocalPlayer()",
            "        testSuccess()"
        );
    }

    /**
     * {@code goto} is a reserved word in Lua 5.3 but was missing from the
     * translator's reserved-name list, so a Wurst local named {@code goto}
     * produced {@code local goto = ...} (a Lua syntax error).
     */
    @Test
    public void gotoIsRenamed() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "init",
            "    let goto = 1",
            "    if goto == 1",
            "        testSuccess()"
        );
    }

    /**
     * The emitted runtime helpers depend on Lua standard library globals
     * (math, table, string, setmetatable, ...). User identifiers with those
     * names used to keep their name and clobber the library at map load.
     */
    @Test
    public void luaStdlibGlobalsAreNotClobbered() throws IOException {
        // package globals are package-prefixed by the IM translator, but
        // top-level functions and locals keep their plain names
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "function math(int x) returns int",
            "    let table = x",
            "    return table",
            "init",
            "    if math(4) mod 3 == 1",
            "        testSuccess()"
        );
        String compiled = compiledLua("luaStdlibGlobalsAreNotClobbered");
        assertFalse("user function must not shadow Lua's math library",
            compiled.contains("function math("));
        assertFalse("user local must not shadow Lua's table library",
            compiled.contains("local table ="));
    }

    /**
     * Field names are printed verbatim as table keys / field accesses.
     * Regular class fields are scope-prefixed by the IM translator, but
     * closure-captured locals become fields with their raw source name, so a
     * captured local named after a Lua keyword (until, local, ...) used to
     * produce {@code obj.until} — a syntax error.
     */
    @Test
    public void luaKeywordFieldNamesAreRenamed() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "interface IntProvider",
            "    function get() returns int",
            "init",
            "    let until = 3",
            "    let local = 4",
            "    IntProvider p = () -> until + local",
            "    if p.get() == 7",
            "        testSuccess()"
        );
    }
}
