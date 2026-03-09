package tests.wurstscript.tests;

import de.peeeq.wurstscript.luaAst.LuaAst;
import de.peeeq.wurstscript.luaAst.LuaFunction;
import de.peeeq.wurstscript.translation.lua.translation.LuaNatives;
import org.testng.annotations.Test;

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
        assertTrue(rendered.contains("return math.modf(x)"));
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

    @Test
    public void hashtableSavesUseTypeSpecificBuckets() {
        String saveInt = renderNative("__wurst_SaveInteger");
        String saveReal = renderNative("__wurst_SaveReal");
        String saveHandle = renderNative("__wurst_SaveAbilityHandle");
        assertTrue(saveInt.contains("h.__wurst_ht_int"));
        assertTrue(saveReal.contains("h.__wurst_ht_real"));
        assertTrue(saveHandle.contains("h.__wurst_ht_handle"));
    }

    @Test
    public void hashtableLoadsUseTypeSpecificBuckets() {
        String loadInt = renderNative("__wurst_LoadInteger");
        String loadStr = renderNative("__wurst_LoadStr");
        String loadHandle = renderNative("__wurst_LoadAbilityHandle");
        assertTrue(loadInt.contains("h.__wurst_ht_int"));
        assertTrue(loadStr.contains("h.__wurst_ht_str"));
        assertTrue(loadHandle.contains("h.__wurst_ht_handle"));
    }
}
