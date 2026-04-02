package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.luaAst.*;

import java.util.List;

import static de.peeeq.wurstscript.translation.lua.translation.ExprTranslation.WURST_ABORT_THREAD_SENTINEL;
import de.peeeq.wurstscript.jassIm.ImFunction;

public class StmtTranslation {

    public static void translate(ImExpr e, List<LuaStatement> res, LuaTranslator tr) {
        // In Lua mode, package init functions are called directly and wrapped with xpcall.
        if (e instanceof ImFunctionCall) {
            ImFunctionCall call = (ImFunctionCall) e;
            if (tr.imTr.luaInitFunctions.containsKey(call.getFunc())) {
                emitLuaInitXpcall(call.getFunc(), res, tr);
                return;
            }
        }
        LuaExpr expr = e.translateToLua(tr);
        res.add(expr);
    }

    private static void emitLuaInitXpcall(ImFunction initFunc, List<LuaStatement> res, LuaTranslator tr) {
        String funcName = tr.luaFunc.getFor(initFunc).getName();
        String packageName = tr.imTr.luaInitFunctions.getOrDefault(initFunc, "?");
        String errHandler = "function(err) if err == \"" + WURST_ABORT_THREAD_SENTINEL + "\" then return end"
                + " BJDebugMsg(\"lua init error: \" .. tostring(err))"
                + " xpcall(function() " + ExprTranslation.callErrorFunc(tr, "tostring(err)") + " end,"
                + " function(err2) if err2 == \"" + WURST_ABORT_THREAD_SENTINEL + "\" then return end"
                + " BJDebugMsg(\"error reporting error: \" .. tostring(err2)) end) end";
        res.add(LuaAst.LuaLiteral("do"));
        res.add(LuaAst.LuaLiteral("  local __wurst_init_ok = xpcall(" + funcName + ", " + errHandler + ")"));
        res.add(LuaAst.LuaLiteral("  if not __wurst_init_ok then"));
        res.add(LuaAst.LuaLiteral("    " + ExprTranslation.callErrorFunc(tr, "\"Could not initialize package " + packageName + ".\"") + ""));
        res.add(LuaAst.LuaLiteral("  end"));
        res.add(LuaAst.LuaLiteral("end"));
    }

    public static void translate(ImExitwhen s, List<LuaStatement> res, LuaTranslator tr) {
        LuaIf r = LuaAst.LuaIf(s.getCondition().translateToLua(tr),
            LuaAst.LuaStatements(LuaAst.LuaBreak()),
            LuaAst.LuaStatements());
        res.add(r);
    }

    public static void translate(ImLoop s, List<LuaStatement> res, LuaTranslator tr) {
        res.add(LuaAst.LuaWhile(LuaAst.LuaExprBoolVal(true), tr.translateStatements(s.getBody())));
    }

    public static void translate(ImIf s, List<LuaStatement> res, LuaTranslator tr) {
        res.add(LuaAst.LuaIf(s.getCondition().translateToLua(tr),
            tr.translateStatements(s.getThenBlock()),
            tr.translateStatements(s.getElseBlock())));
    }

    public static void translate(ImReturn s, List<LuaStatement> res, LuaTranslator tr) {
        res.add(LuaAst.LuaReturn(tr.translateOptional(s.getReturnValue())));
    }

    public static void translate(ImSet s, List<LuaStatement> res, LuaTranslator tr) {
        LuaExpr left;
        if (s.getLeft() instanceof ImVarArrayAccess) {
            // Assignment LHS must stay a writable table access, never an ensured r-value wrapper.
            left = ExprTranslation.translateArrayAccessRaw((ImVarArrayAccess) s.getLeft(), tr);
        } else {
            left = s.getLeft().translateToLua(tr);
        }
        LuaExpr right = s.getRight().translateToLua(tr);
        if (s.getRight().attrTyp() instanceof ImTupleType) {
            ImTupleType tt = (ImTupleType) s.getRight().attrTyp();
            // tuples must be copied, unless they are literals
            if(!(right instanceof LuaTableConstructor)) {
                right = LuaAst.LuaExprFunctionCall(ExprTranslation.getTupleCopyFunc(tt, tr), LuaAst.LuaExprlist(right));
            }
        }
        res.add(LuaAst.LuaAssignment(left, right));
    }


    public static void translate(ImVarargLoop loop, List<LuaStatement> res, LuaTranslator tr) {
        LuaVariable loopVar = tr.luaVar.getFor(loop.getLoopVar());
//        res.add(loopVar);
        String argsName = tr.uniqueName("__args");
        LuaVariable i = LuaAst.LuaVariable(tr.uniqueName("i"),  LuaAst.LuaExprIntVal("0"));
        res.add(LuaAst.LuaLiteral("local " + argsName + " = table.pack(...)"));
        res.add(LuaAst.LuaLiteral("for " + i.getName() + "=1," + argsName + ".n do"));
        res.add(LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(loopVar), LuaAst.LuaExprArrayAccess(LuaAst.LuaLiteral(argsName), LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(i)))));
        tr.translateStatements(res, loop.getBody());
        res.add(LuaAst.LuaLiteral("end"));
    }

}
