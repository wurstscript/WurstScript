package de.peeeq.wurstscript.lua.translation;

import java.util.List;

import de.peeeq.wurstscript.jassIm.ImError;
import de.peeeq.wurstscript.jassIm.ImExitwhen;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImLoop;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImSetArrayMulti;
import de.peeeq.wurstscript.jassIm.ImSetArrayTuple;
import de.peeeq.wurstscript.jassIm.ImSetTuple;
import de.peeeq.wurstscript.luaAst.LuaAst;
import de.peeeq.wurstscript.luaAst.LuaExpr;
import de.peeeq.wurstscript.luaAst.LuaIf;
import de.peeeq.wurstscript.luaAst.LuaStatement;

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

	public static void translate(ImError s, List<LuaStatement> res, LuaTranslator tr) {
		res.add(LuaAst.LuaExprFunctionCallByName("print", LuaAst.LuaExprlist(s.getMessage().translateToLua(tr))));
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
		res.add(LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(tr.luaVar.getFor(s.getLeft())), s.getRight().translateToLua(tr)));
	}

	public static void translate(ImSetArray s, List<LuaStatement> res, LuaTranslator tr) {
		res.add(LuaAst.LuaAssignment(
				LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(tr.luaVar.getFor(s.getLeft())), LuaAst.LuaExprlist(s.getIndex().translateToLua(tr))), 
				s.getRight().translateToLua(tr)));
	}

	public static void translate(ImSetArrayMulti s, List<LuaStatement> res, LuaTranslator tr) {
		res.add(LuaAst.LuaAssignment(
				LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(tr.luaVar.getFor(s.getLeft())), tr.translateExprList(s.getIndices())), 
				s.getRight().translateToLua(tr)));
	}

	public static void translate(ImSetArrayTuple s, List<LuaStatement> res, LuaTranslator tr) {
		res.add(LuaAst.LuaAssignment(
				LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(tr.luaVar.getFor(s.getLeft())), LuaAst.LuaExprlist(s.getIndex().translateToLua(tr), LuaAst.LuaExprIntVal(""+s.getTupleIndex()))), 
				s.getRight().translateToLua(tr)));
	}

	public static void translate(ImSetTuple s, List<LuaStatement> res, LuaTranslator tr) {
		res.add(LuaAst.LuaAssignment(
				LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(tr.luaVar.getFor(s.getLeft())), LuaAst.LuaExprlist(LuaAst.LuaExprIntVal(""+s.getTupleIndex()))), 
				s.getRight().translateToLua(tr)));
	}

}
