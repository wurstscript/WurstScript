package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.luaAst.LuaAst;
import de.peeeq.wurstscript.luaAst.LuaExpr;
import de.peeeq.wurstscript.luaAst.LuaIf;
import de.peeeq.wurstscript.luaAst.LuaStatement;

import java.util.List;

public class StmtTranslation {

    public static void translate(ImExpr e, List<LuaStatement> res, LuaTranslator tr) {
        LuaExpr expr = e.translateToLua(tr);
        res.add(expr);
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
        LuaExpr left = s.getLeft().translateToLua(tr);
        LuaExpr right = s.getRight().translateToLua(tr);
        if (s.getRight().attrTyp() instanceof ImTupleType) {
            ImTupleType tt = (ImTupleType) s.getRight().attrTyp();
            // tuples must be copied
            right = LuaAst.LuaExprFunctionCall(ExprTranslation.getTupleCopyFunc(tt, tr), LuaAst.LuaExprlist(right));
        }
        res.add(LuaAst.LuaAssignment(left, right));
    }


    public static void translate(ImVarargLoop imVarargLoop, List<LuaStatement> res, LuaTranslator tr) {
        throw new Error("not implemented");
    }

}
