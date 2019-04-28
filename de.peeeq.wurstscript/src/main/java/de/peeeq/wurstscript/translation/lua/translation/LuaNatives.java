package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.luaAst.LuaAst;
import de.peeeq.wurstscript.luaAst.LuaFunction;
import de.peeeq.wurstscript.luaAst.LuaVariable;

public class LuaNatives {

    private static void native_testSuccess(LuaFunction f) {
        f.getBody().add(LuaAst.LuaLiteral("print(\"testSuccess\")"));
    }

    private static void native_print(LuaFunction f) {
        LuaVariable p = LuaAst.LuaVariable("text", LuaAst.LuaNoExpr());
        f.getParams().add(p);
        f.getBody().add(LuaAst.LuaExprFunctionCallByName("print",
            LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(p))));
    }

    private static void native_testFail(LuaFunction f) {
        f.getParams().add(LuaAst.LuaVariable("msg", LuaAst.LuaNoExpr()));
        f.getBody().add(LuaAst.LuaLiteral("print(\"error: \" .. msg)"));
        f.getBody().add(LuaAst.LuaLiteral("error()"));
    }

    public static void get(LuaFunction f) {
        switch (f.getName()) {
            case "testSuccess":
                native_testSuccess(f);
                break;
            case "testFail":
                native_testFail(f);
                break;
            case "print":
            case "println":
                native_print(f);
                break;
            default:
                throw new RuntimeException("native not implemented: " + f.getName());
        }
    }

}
