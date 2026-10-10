package tests.wurstscript.tests;

import de.peeeq.wurstscript.luaAst.LuaAst;
import de.peeeq.wurstscript.luaAst.LuaFunction;
import de.peeeq.wurstscript.translation.lua.translation.LuaNatives;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class LuaNativesTests {

    private static String renderNative(String name) {
        LuaFunction f = LuaAst.LuaFunction(name, LuaAst.LuaParams(), LuaAst.LuaStatements());
        LuaNatives.get(f);
        StringBuilder sb = new StringBuilder();
        f.print(sb, 0);
        return sb.toString();
    }

    @Test
    public void s2iUsesPrefixIntegerParsing() {
        String rendered = renderNative("S2I");
        assertTrue(rendered.contains("string.match(tostring(x), \"^[%+%-]?%d+\")"));
        assertTrue(rendered.contains("return tonumber(m)"));
    }

    @Test
    public void r2iUsesTruncationTowardZero() {
        String rendered = renderNative("R2I");
        // math.modf must not be used: it returns two values (which expand
        // into enclosing argument lists) and a float integral part
        assertFalse(rendered.contains("math.modf"));
        assertTrue(rendered.contains("return math.floor(x)"));
        assertTrue(rendered.contains("return math.ceil(x)"));
    }

    @Test
    public void playerHandlesAreCachedForIdentityComparisons() {
        String rendered = renderNative("Player");
        assertTrue(rendered.contains("__wurst_test_players"));
        assertTrue(rendered.contains("return p"));
    }

    @Test
    public void getRandomRealUsesRangeFormula() {
        String rendered = renderNative("GetRandomReal");
        assertTrue(rendered.contains("return l + math.random() * (h - l)"));
    }

    @Test
    public void triggerEvaluateReturnsBoolInFallback() {
        String rendered = renderNative("TriggerEvaluate");
        assertTrue(rendered.contains("for i,a in ipairs(t.actions) do a() end"));
        assertTrue(rendered.contains("return true"));
    }

    @Test
    public void initHashtableCreatesPerTypeBuckets() {
        String rendered = renderNative("__wurst_InitHashtable");
        assertTrue(rendered.contains("__wurst_ht_int"));
        assertTrue(rendered.contains("__wurst_ht_bool"));
        assertTrue(rendered.contains("__wurst_ht_real"));
        assertTrue(rendered.contains("__wurst_ht_str"));
        assertTrue(rendered.contains("__wurst_ht_handle"));
    }

    /** The helpers a call with an effect in an operand still calls; InitHashtable creates every subtable. */
    @Test
    public void hashtableSavesUseTypeSpecificBuckets() {
        String saveInt = renderNative("__wurst_SaveInteger");
        String saveReal = renderNative("__wurst_SaveReal");
        String saveHandle = renderNative("__wurst_SaveAbilityHandle");
        assertTrue(saveInt.contains("h.__wurst_ht_int"));
        assertTrue(saveReal.contains("h.__wurst_ht_real"));
        assertTrue(saveHandle.contains("h.__wurst_ht_handle"));
        assertTrue(saveInt, saveInt.contains("if x == nil then x = {} t[p] = x end"));
        assertFalse("no subtable is ever missing: " + saveInt, saveInt.contains("t == nil"));
    }

    @Test
    public void hashtableLoadsUseTypeSpecificBuckets() {
        String loadInt = renderNative("__wurst_LoadInteger");
        String loadStr = renderNative("__wurst_LoadStr");
        String loadHandle = renderNative("__wurst_LoadAbilityHandle");
        String loadBool = renderNative("__wurst_LoadBoolean");
        assertTrue(loadInt.contains("h.__wurst_ht_int"));
        assertTrue(loadStr.contains("h.__wurst_ht_str"));
        assertTrue(loadHandle.contains("h.__wurst_ht_handle"));
        assertTrue(loadInt, loadInt.contains("return x[c] or 0"));
        assertTrue("a missing string is nil: " + loadStr, loadStr.contains("return nil") && loadStr.contains("return x[c]\n"));
        assertTrue(loadBool, loadBool.contains("return x[c] == true"));
        assertFalse("no subtable is ever missing: " + loadInt, loadInt.contains("t == nil"));
    }

    @Test
    public void hashtableFlushesTestNoSubtable() {
        String flushChild = renderNative("__wurst_FlushChildHashtable");
        assertTrue(flushChild, flushChild.contains("h.__wurst_ht_handle[p] = nil"));
        assertFalse(flushChild, flushChild.contains("if "));
    }
}
