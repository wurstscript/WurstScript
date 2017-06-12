package de.peeeq.wurstscript.lua.translation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.ImAlloc;
import de.peeeq.wurstscript.jassIm.ImBoolVal;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImDealloc;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFuncRef;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImGetStackTrace;
import de.peeeq.wurstscript.jassIm.ImInstanceof;
import de.peeeq.wurstscript.jassIm.ImIntVal;
import de.peeeq.wurstscript.jassIm.ImMemberAccess;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImMethodCall;
import de.peeeq.wurstscript.jassIm.ImNull;
import de.peeeq.wurstscript.jassIm.ImOperatorCall;
import de.peeeq.wurstscript.jassIm.ImRealVal;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImStatementExpr;
import de.peeeq.wurstscript.jassIm.ImStringVal;
import de.peeeq.wurstscript.jassIm.ImTupleExpr;
import de.peeeq.wurstscript.jassIm.ImTupleSelection;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImTypeIdOfClass;
import de.peeeq.wurstscript.jassIm.ImTypeIdOfObj;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayMultiAccess;
import de.peeeq.wurstscript.luaAst.LuaAst;
import de.peeeq.wurstscript.luaAst.LuaExpr;
import de.peeeq.wurstscript.luaAst.LuaExprlist;
import de.peeeq.wurstscript.luaAst.LuaFunction;
import de.peeeq.wurstscript.luaAst.LuaOpBinary;
import de.peeeq.wurstscript.luaAst.LuaOpUnary;
import de.peeeq.wurstscript.luaAst.LuaStatements;
import de.peeeq.wurstscript.luaAst.LuaTableFields;

public class ExprTranslation {

	private static final String TYPE_ID = "__typeId__";

	public static LuaExpr translate(ImAlloc e, LuaTranslator tr) {
		LuaTableFields fields = LuaAst.LuaTableFields();
		ImClass clazz = e.getClazz();
		fields.add(LuaAst.LuaTableNamedField("wurst_typeId", LuaAst.LuaExprIntVal(""+clazz.attrTypeId())));
		for (ImMethod m : clazz.getMethods()) {
			LuaFunction luaMethod = tr.luaMethod.getFor(m);
			fields.add(LuaAst.LuaTableNamedField(luaMethod.getName(), LuaAst.LuaExprFuncRef(tr.luaFunc.getFor(m.getImplementation()))));
		}
		return LuaAst.LuaTableConstructor(fields); // TODO any fields required? typeid?
	}

	public static LuaExpr translate(ImBoolVal e, LuaTranslator tr) {
		return LuaAst.LuaExprBoolVal(e.getValB());
	}

	public static LuaExpr translate(ImDealloc e, LuaTranslator tr) {
		return LuaAst.LuaExprNull(); // TODO maybe call some finalizer?
	}

	public static LuaExpr translate(ImFuncRef e, LuaTranslator tr) {
		// functions are just referenced by name
		return LuaAst.LuaExprStringVal(tr.luaFunc.getFor(e.getFunc()).getName());
	}

	public static LuaExpr translate(ImFunctionCall e, LuaTranslator tr) {
		return LuaAst.LuaExprFunctionCall(tr.luaFunc.getFor(e.getFunc()), tr.translateExprList(e.getArguments()));
	}

	public static LuaExpr translate(ImInstanceof e, LuaTranslator tr) {
		// what is the object layout?
		throw new Error("not implemented");
	}

	public static LuaExpr translate(ImIntVal e, LuaTranslator tr) {
		return LuaAst.LuaExprIntVal(""+e.getValI());
	}

	public static LuaExpr translate(ImMemberAccess e, LuaTranslator tr) {
		return LuaAst.LuaExprFieldAccess(e.getReceiver().translateToLua(tr), e.getVar().getName()); 
		// TODO can name be used here directly?
	}

	public static LuaExpr translate(ImMethodCall e, LuaTranslator tr) {
		return LuaAst.LuaExprMethodCall(e.getReceiver().translateToLua(tr), tr.luaMethod.getFor(e.getMethod()), tr.translateExprList(e.getArguments()));
	}

	public static LuaExpr translate(ImNull e, LuaTranslator tr) {
		return LuaAst.LuaExprNull();
	}

	public static LuaExpr translate(ImOperatorCall e, LuaTranslator tr) {
		if (e.getArguments().size() == 2) {
			ImExpr left = e.getArguments().get(0);
			ImExpr right = e.getArguments().get(1);
			LuaExpr leftExpr = left.translateToLua(tr);
			LuaExpr rightExpr = right.translateToLua(tr);
			LuaOpBinary op;
			if (e.getOp() == WurstOperator.PLUS
					&& isStringType(left.attrTyp())
					&& isStringType(right.attrTyp())) {
				// special case for string concatenation
				op = LuaAst.LuaOpConcatString();
			} else if (e.getOp() == WurstOperator.MOD_INT) {
				op = LuaAst.LuaOpMod();
				
				return LuaAst.LuaExprFunctionCallE(LuaAst.LuaLiteral("math.floor"),
						LuaAst.LuaExprlist(LuaAst.LuaExprBinary(leftExpr, op, rightExpr)));
			} else if (e.getOp() == WurstOperator.DIV_INT) {
				op = LuaAst.LuaOpDiv();
				return LuaAst.LuaExprFunctionCallE(LuaAst.LuaLiteral("math.floor"),
						LuaAst.LuaExprlist(LuaAst.LuaExprBinary(leftExpr, op, rightExpr)));
			} else {
				// TODO special cases for integer division and modulo
				op = e.getOp().luaTranslateBinary();
			}
			
			return LuaAst.LuaExprBinary(leftExpr, op, rightExpr);
		} else if (e.getArguments().size() == 1) {
			ImExpr arg = e.getArguments().get(0);
			LuaExpr argT = arg.translateToLua(tr);
			LuaOpUnary op;
			if (e.getOp() == WurstOperator.NOT) {
				op = LuaAst.LuaOpNot();
			} else if (e.getOp() == WurstOperator.UNARY_MINUS) {
				op = LuaAst.LuaOpMinus();
			} else {
				throw new Error("not implemented: unary operator " + e.getOp());
			}
			return LuaAst.LuaExprUnary(op, argT);
			
		}
		throw new Error("not implemented: " + e);
	}


	private static boolean isStringType(ImType t) {
		if (t instanceof ImSimpleType) {
			ImSimpleType st = (ImSimpleType) t;
			return st.getTypename().equals("string");
		}
		return false;
	}

	public static LuaExpr translate(ImRealVal e, LuaTranslator tr) {
		return LuaAst.LuaExprRealVal(e.getValR());
	}

	public static LuaExpr translate(ImStatementExpr e, LuaTranslator tr) {
		LuaStatements body = tr.translateStatements(e.getStatements());
		body.add(LuaAst.LuaReturn(e.getExpr().translateToLua(tr)));
		return LuaAst.LuaExprFunctionCallE(
				LuaAst.LuaExprFunctionAbstraction(LuaAst.LuaParams(), body), 
				LuaAst.LuaExprlist());
	}

	public static LuaExpr translate(ImStringVal e, LuaTranslator tr) {
		return LuaAst.LuaExprStringVal(e.getValS());
	}

	public static LuaExpr translate(ImTupleExpr e, LuaTranslator tr) {
		LuaTableFields tableFields = LuaAst.LuaTableFields();
		for (ImExpr te : e.getExprs()) {
			tableFields.add(LuaAst.LuaTableSingleField(te.translateToLua(tr)));
		}
		return LuaAst.LuaTableConstructor(tableFields);
	}

	public static LuaExpr translate(ImTupleSelection e, LuaTranslator tr) {
		return LuaAst.LuaExprArrayAccess(e.getTupleExpr().translateToLua(tr), LuaAst.LuaExprlist(LuaAst.LuaExprIntVal(""+e.getTupleIndex())));
	}

	public static LuaExpr translate(ImTypeIdOfClass e, LuaTranslator tr) {
		int i = tr.typeId.getFor(e.getClazz());
		return LuaAst.LuaExprIntVal(""+i);
	}

	public static LuaExpr translate(ImTypeIdOfObj e, LuaTranslator tr) {
		return LuaAst.LuaExprFieldAccess(e.getObj().translateToLua(tr), TYPE_ID);
	}

	public static LuaExpr translate(ImVarAccess e, LuaTranslator tr) {
		return LuaAst.LuaExprVarAccess(tr.luaVar.getFor(e.getVar()));
	}

	public static LuaExpr translate(ImVarArrayMultiAccess e, LuaTranslator tr) {
		LuaExprlist indexes = LuaAst.LuaExprlist();
		indexes.add(e.getIndex1().translateToLua(tr));
		indexes.add(e.getIndex2().translateToLua(tr));
		return LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(tr.luaVar.getFor(e.getVar())), indexes);
	}

	public static LuaExpr translate(ImVarArrayAccess e, LuaTranslator tr) {
		LuaExprlist indexes = LuaAst.LuaExprlist();
		indexes.add(e.getIndex().translateToLua(tr));
		return LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(tr.luaVar.getFor(e.getVar())), indexes);
	}

	public static LuaExpr translate(ImGetStackTrace e, LuaTranslator tr) {
		throw new Error("not implemented");
	}

}
