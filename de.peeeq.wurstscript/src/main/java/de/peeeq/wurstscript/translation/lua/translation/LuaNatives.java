package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.luaAst.LuaAst;
import de.peeeq.wurstscript.luaAst.LuaFunction;
import de.peeeq.wurstscript.luaAst.LuaVariable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LuaNatives {

    private static Map<String, Consumer<LuaFunction>> nativeCodes = new HashMap<>();

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

        addNative("Player", f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return { id = x }"));
        });

        addNative("GetPlayerId", f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return x.id"));
        });

        addNative("GetRandomReal", f -> {
            f.getBody().add(LuaAst.LuaLiteral("return math.random"));
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
        });

        addNative("R2I", f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return math.floor(x)"));
        });
    }

    private static void addNative(String name, Consumer<LuaFunction> f) {
        nativeCodes.put(name,f);
    }

    private static void addNative(Iterable<String> names, Consumer<LuaFunction> f) {
        for (String name : names) {
            nativeCodes.put(name,f);
        }
    }

    public static void get(LuaFunction f) {
        nativeCodes.getOrDefault(f.getName(), name -> {
            throw new RuntimeException("native not implemented: " + f.getName());
        }).accept(f);
    }

}
