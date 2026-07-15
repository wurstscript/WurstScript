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
        // no local/do-block: locals hidden in literals are invisible to the
        // 200-locals accounting in enforceLuaLocalLimits
        res.add(LuaAst.LuaLiteral("if not xpcall(" + funcName + ", " + errHandler + ") then"));
        res.add(LuaAst.LuaLiteral("    " + ExprTranslation.callErrorFunc(tr, "\"Could not initialize package " + packageName + ".\"")));
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
        // The loop is built from real AST nodes (a while loop) instead of literal
        // 'for ... do' / 'end' lines: the printer stops printing a statement list
        // after a return/break (Lua forbids trailing statements), which would
        // truncate a literal closing 'end' and produce unparseable output.
        LuaVariable args = LuaAst.LuaVariable(tr.uniqueName("__args"), LuaAst.LuaLiteral("table.pack(...)"));
        LuaVariable i = LuaAst.LuaVariable(tr.uniqueName("__i"), LuaAst.LuaExprIntVal("0"));
        res.add(args);
        res.add(i);
        LuaStatements body = LuaAst.LuaStatements();
        body.add(LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(i),
            LuaAst.LuaExprBinary(LuaAst.LuaExprVarAccess(i), LuaAst.LuaOpPlus(), LuaAst.LuaExprIntVal("1"))));
        body.add(LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(loopVar),
            LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(args), LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(i)))));
        tr.translateStatements(body, loop.getBody());
        res.add(LuaAst.LuaWhile(
            LuaAst.LuaExprBinary(LuaAst.LuaExprVarAccess(i), LuaAst.LuaOpLess(),
                LuaAst.LuaExprFieldAccess(LuaAst.LuaExprVarAccess(args), "n")),
            body));
    }

}
