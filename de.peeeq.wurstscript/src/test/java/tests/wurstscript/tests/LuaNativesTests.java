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
}

