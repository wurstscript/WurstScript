package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.luaAst.LuaAst;
import de.peeeq.wurstscript.luaAst.LuaFunction;
import de.peeeq.wurstscript.luaAst.LuaVariable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LuaNatives {

    private static final Map<String, Consumer<LuaFunction>> nativeCodes = new HashMap<>();

    static {
        addNative("testSuccess", f -> {
            f.getBody().add(LuaAst.LuaLiteral("print(\"testSuccess\")"));
            f.getBody().add(LuaAst.LuaLiteral("os.exit()"));
        });
        addNative(Arrays.asList("print", "println"), f -> {
            LuaVariable p = LuaAst.LuaVariable("text", LuaAst.LuaNoExpr());
            f.getParams().add(p);
            f.getBody().add(LuaAst.LuaExprFunctionCallByName("print",
                LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(p))));
        });

        addNative("testFail", f -> {
            f.getParams().add(LuaAst.LuaVariable("msg", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("print(\"error: \" .. msg)"));
            f.getBody().add(LuaAst.LuaLiteral("error()"));
        });


        addNative("Sin", f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return math.sin(x)"));
        });

        addNative("Cos", f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return math.cos(x)"));
        });

        addNative(Arrays.asList("I2S", "R2S"), f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return tostring(x)"));
        });

        addNative(Collections.singletonList("S2I"), f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local m = string.match(tostring(x), \"^[%+%-]?%d+\")"));
            f.getBody().add(LuaAst.LuaLiteral("if m then return tonumber(m) else return 0 end"));
        });

        addNative("Player", f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return { id = x }"));
        });

        addNative("GetPlayerId", f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return x.id"));
        });

        addNative("GetRandomReal", f -> {
            f.getParams().add(LuaAst.LuaVariable("l", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return l + math.random() * (h - l)"));
        });

        addNative("GetRandomInt", f -> {
            f.getParams().add(LuaAst.LuaVariable("l", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return math.random(l,h)"));
        });

        addNative("SquareRoot", f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return math.sqrt(x)"));
        });

        addNative("CreateTrigger", f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return { actions = {}}"));
        });

        addNative("TriggerAddAction", f -> {
            f.getParams().add(LuaAst.LuaVariable("t", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("table.insert(t.actions, c)"));
        });

        addNative("TriggerEvaluate", f -> {
            f.getParams().add(LuaAst.LuaVariable("t", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("for i,a in ipairs(t.actions) do a() end"));
            f.getBody().add(LuaAst.LuaLiteral("return true"));
        });

        addNative("R2I", f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return math.modf(x)"));
        });

        addNative(Arrays.asList("InitHashtable", "__wurst_InitHashtable"), f -> f.getBody().add(LuaAst.LuaLiteral("return {}")));

        addNative(Arrays.asList(
            "SaveInteger", "SaveBoolean", "SaveReal", "SaveStr",
            "__wurst_SaveInteger", "__wurst_SaveBoolean", "__wurst_SaveReal", "__wurst_SaveStr"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("i", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("if not h[p] then h[p] = {} end h[p][c] = i"));
        });

        addNative(Arrays.asList(
            "LoadInteger", "LoadBoolean", "LoadReal", "LoadStr",
            "__wurst_LoadInteger", "__wurst_LoadBoolean", "__wurst_LoadReal", "__wurst_LoadStr"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("if not h[p] then return nil end return h[p][c]"));
        });

        addNative(Arrays.asList(
            "HaveSavedInteger", "HaveSavedBoolean", "HaveSavedReal", "HaveSavedString",
            "__wurst_HaveSavedInteger", "__wurst_HaveSavedBoolean", "__wurst_HaveSavedReal", "__wurst_HaveSavedString"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return h[p] ~= nil and h[p][c] ~= nil"));
        });

        addNative(Arrays.asList("FlushChildHashtable", "__wurst_FlushChildHashtable"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("h[p] = nil"));
        });

        addNative(Arrays.asList("FlushParentHashtable", "__wurst_FlushParentHashtable"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("for k in pairs(h) do h[k] = nil end"));
        });

        addNative(Arrays.asList(
            "RemoveSavedInteger", "RemoveSavedBoolean", "RemoveSavedReal", "RemoveSavedString",
            "__wurst_RemoveSavedInteger", "__wurst_RemoveSavedBoolean", "__wurst_RemoveSavedReal", "__wurst_RemoveSavedString"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("if h[p] then h[p][c] = nil end"));
        });

        addNative("typeIdToTypeName", f -> {
            f.getParams().add(LuaAst.LuaVariable("typeId", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return \"\""));
        });

        addNative("maxTypeId", f -> {
            f.getBody().add(LuaAst.LuaLiteral("return 0"));
        });

        addNative("instanceCount", f -> {
            f.getParams().add(LuaAst.LuaVariable("typeId", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return 0"));
        });

        addNative("maxInstanceCount", f -> {
            f.getParams().add(LuaAst.LuaVariable("typeId", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return 0"));
        });

    }

    private static void addNative(String name, Consumer<LuaFunction> g) {
        nativeCodes.put(name, f -> {
            f.getParams().removeAll();
            g.accept(f);
        });
    }

    private static void addNative(Iterable<String> names, Consumer<LuaFunction> g) {
        for (String name : names) {
            addNative(name, g);
        }
    }

    public static void get(LuaFunction f) {
        nativeCodes.getOrDefault(f.getName(), ff -> {
            // generate a runtime exception
            f.getBody().add(LuaAst.LuaLiteral("error(\"The native '" + ff.getName() + "' is not implemented.\")"));
        }).accept(f);
    }

}
