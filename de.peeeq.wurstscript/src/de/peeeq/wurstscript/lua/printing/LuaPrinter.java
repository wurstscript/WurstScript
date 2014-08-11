package de.peeeq.wurstscript.lua.printing;

import de.peeeq.wurstscript.luaAst.LuaAssignment;
import de.peeeq.wurstscript.luaAst.LuaBreak;
import de.peeeq.wurstscript.luaAst.LuaCompilationUnit;
import de.peeeq.wurstscript.luaAst.LuaDefinition;
import de.peeeq.wurstscript.luaAst.LuaExpr;
import de.peeeq.wurstscript.luaAst.LuaExprArrayAccess;
import de.peeeq.wurstscript.luaAst.LuaExprBinary;
import de.peeeq.wurstscript.luaAst.LuaExprBoolVal;
import de.peeeq.wurstscript.luaAst.LuaExprFieldAccess;
import de.peeeq.wurstscript.luaAst.LuaExprFuncRef;
import de.peeeq.wurstscript.luaAst.LuaExprFunctionAbstraction;
import de.peeeq.wurstscript.luaAst.LuaExprFunctionCall;
import de.peeeq.wurstscript.luaAst.LuaExprFunctionCallByName;
import de.peeeq.wurstscript.luaAst.LuaExprFunctionCallE;
import de.peeeq.wurstscript.luaAst.LuaExprIntVal;
import de.peeeq.wurstscript.luaAst.LuaExprMethodCall;
import de.peeeq.wurstscript.luaAst.LuaExprNull;
import de.peeeq.wurstscript.luaAst.LuaExprRealVal;
import de.peeeq.wurstscript.luaAst.LuaExprStringVal;
import de.peeeq.wurstscript.luaAst.LuaExprUnary;
import de.peeeq.wurstscript.luaAst.LuaExprVarAccess;
import de.peeeq.wurstscript.luaAst.LuaExprlist;
import de.peeeq.wurstscript.luaAst.LuaFunction;
import de.peeeq.wurstscript.luaAst.LuaIf;
import de.peeeq.wurstscript.luaAst.LuaLocal;
import de.peeeq.wurstscript.luaAst.LuaModel;
import de.peeeq.wurstscript.luaAst.LuaNoExpr;
import de.peeeq.wurstscript.luaAst.LuaOpAnd;
import de.peeeq.wurstscript.luaAst.LuaOpConcatString;
import de.peeeq.wurstscript.luaAst.LuaOpDiv;
import de.peeeq.wurstscript.luaAst.LuaOpEquals;
import de.peeeq.wurstscript.luaAst.LuaOpGreater;
import de.peeeq.wurstscript.luaAst.LuaOpGreaterEq;
import de.peeeq.wurstscript.luaAst.LuaOpLess;
import de.peeeq.wurstscript.luaAst.LuaOpLessEq;
import de.peeeq.wurstscript.luaAst.LuaOpMinus;
import de.peeeq.wurstscript.luaAst.LuaOpMod;
import de.peeeq.wurstscript.luaAst.LuaOpMult;
import de.peeeq.wurstscript.luaAst.LuaOpNot;
import de.peeeq.wurstscript.luaAst.LuaOpOr;
import de.peeeq.wurstscript.luaAst.LuaOpPlus;
import de.peeeq.wurstscript.luaAst.LuaOpUnequals;
import de.peeeq.wurstscript.luaAst.LuaParams;
import de.peeeq.wurstscript.luaAst.LuaReturn;
import de.peeeq.wurstscript.luaAst.LuaStatement;
import de.peeeq.wurstscript.luaAst.LuaStatements;
import de.peeeq.wurstscript.luaAst.LuaTableConstructor;
import de.peeeq.wurstscript.luaAst.LuaTableExprField;
import de.peeeq.wurstscript.luaAst.LuaTableField;
import de.peeeq.wurstscript.luaAst.LuaTableFields;
import de.peeeq.wurstscript.luaAst.LuaTableNamedField;
import de.peeeq.wurstscript.luaAst.LuaTableSingleField;
import de.peeeq.wurstscript.luaAst.LuaVariable;
import de.peeeq.wurstscript.luaAst.LuaWhile;
import de.peeeq.wurstscript.utils.Utils;

public class LuaPrinter {

	public static void print(LuaAssignment s, StringBuilder sb, int indent) {
		s.getLeft().print(sb, indent);
		sb.append(" = ");
		s.getRight().print(sb, indent);
	}

	public static void print(LuaBreak s, StringBuilder sb, int indent) {
		sb.append("break");
	}

	public static void print(LuaCompilationUnit cu, StringBuilder sb, int indent) {
		for (LuaDefinition d : cu) {
			d.print(sb, indent);
			sb.append("\n\n");
		}
	}

	public static void print(LuaExprArrayAccess e, StringBuilder sb, int indent) {
		e.getLeft().print(sb, indent);
		for (LuaExpr i : e.getIndexes()) {
			sb.append("[");
			i.print(sb, indent);
			sb.append("]");
		}
	}

	public static void print(LuaExprBinary e, StringBuilder sb, int indent) {
		sb.append("(");
		e.getLeftExpr().print(sb, indent);
		sb.append(" ");
		e.getOp().print(sb, indent);
		sb.append(" ");
		e.getRight().print(sb, indent);
		sb.append(")");
	}

	public static void print(LuaExprBoolVal e, StringBuilder sb, int indent) {
		sb.append(e.getValB());
	}

	public static void print(LuaExprFieldAccess e, StringBuilder sb, int indent) {
		e.getReceiver().print(sb, indent);
		sb.append(e.getFieldName());
	}

	public static void print(LuaExprFuncRef e, StringBuilder sb, int indent) {
		sb.append(e.getFunc().getName());
	}

	public static void print(LuaExprFunctionAbstraction e, StringBuilder sb, int indent) {
		sb.append("function (");
		e.getParams().print(sb, indent);
		sb.append(") \n");
		e.getBody().print(sb, indent+2);
		printIndent(sb,indent+1);
		sb.append("end");
	}

	private static void printIndent(StringBuilder sb, int indent) {
		for (int i=0; i<indent; i++) {
			sb.append("	");
		}
	}

	public static void print(LuaExprFunctionCall e, StringBuilder sb, int indent) {
		sb.append(e.getFunc().getName());
		sb.append("(");
		e.getArguments().print(sb, indent);
		sb.append(")");
	}

	public static void print(LuaExprFunctionCallByName e, StringBuilder sb, int indent) {
		sb.append(e.getFuncName());
		sb.append("(");
		e.getArguments().print(sb, indent);
		sb.append(")");
	}

	public static void print(LuaExprFunctionCallE e, StringBuilder sb, int indent) {
		sb.append("(");
		e.getFuncExpr().print(sb, indent);
		sb.append(")(");
		e.getArguments().print(sb, indent);
		sb.append(")");
	}

	public static void print(LuaExprIntVal e, StringBuilder sb, int indent) {
		sb.append(e.getValI());
	}

	public static void print(LuaExprlist e, StringBuilder sb, int indent) {
		boolean first = true;
		for (LuaExpr ee : e) {
			if (!first) {
				sb.append(", ");
			}
			ee.print(sb, indent);
			first = false;
		}
	}

	public static void print(LuaExprMethodCall e, StringBuilder sb, int indent) {
		e.getReceiver().print(sb, indent);
		sb.append(".");
		sb.append(e.getFunc().getName());
		sb.append("(");
		e.getArguments().print(sb, indent);
		sb.append(")");
	}

	public static void print(LuaExprNull e, StringBuilder sb, int indent) {
		sb.append("nil");
	}

	public static void print(LuaExprRealVal e, StringBuilder sb, int indent) {
		sb.append(e.getValR());
	}

	public static void print(LuaExprStringVal e, StringBuilder sb, int indent) {
		sb.append(Utils.escapeString(e.getValS()));
	}

	public static void print(LuaExprUnary e, StringBuilder sb, int indent) {
		e.getOpU().print(sb, indent);
		sb.append("(");
		e.getRight().print(sb, indent);
		sb.append(")");
	}

	public static void print(LuaExprVarAccess e, StringBuilder sb, int indent) {
		sb.append(e.getVar().getName());
	}

	public static void print(LuaFunction f, StringBuilder sb, int indent) {
		printIndent(sb,indent);
		sb.append("function ");
		sb.append(f.getName());
		sb.append("(");
		f.getParams().print(sb, indent);
		sb.append(") \n");
		f.getBody().print(sb, indent);
		printIndent(sb,indent);
		sb.append("end");
	}

	public static void print(LuaIf s, StringBuilder sb, int indent) {
		sb.append("if ");
		s.getCond().print(sb, indent);
		sb.append(" then\n");
		s.getThenStmts().print(sb, indent+1);
		printIndent(sb, indent);
		sb.append("else\n"); // TODO special case for else if
		s.getElseStmts().print(sb, indent);
		printIndent(sb, indent);
		sb.append("end");
	}

	public static void print(LuaLocal s, StringBuilder sb, int indent) {
		sb.append("local ");
		sb.append(s.getDefinedVar().getName());
		if (s.getInitialValue() instanceof LuaExpr) {
			LuaExpr in = (LuaExpr) s.getInitialValue();
			sb.append(" = ");
			in.print(sb, indent);
		}
	}

	public static void print(LuaModel m, StringBuilder sb, int indent) {
		for (LuaCompilationUnit cu : m) {
			cu.print(sb, indent);
		}
	}

	public static void print(LuaNoExpr n, StringBuilder sb, int indent) {
		// nothing
	}

	public static void print(LuaOpAnd luaOpAnd, StringBuilder sb, int indent) {
		sb.append("and");
	}

	public static void print(LuaOpConcatString luaOpConcatString, StringBuilder sb, int indent) {
		sb.append("..");
	}

	public static void print(LuaOpDiv luaOpDiv, StringBuilder sb, int indent) {
		sb.append("/");
	}

	public static void print(LuaOpEquals luaOpEquals, StringBuilder sb, int indent) {
		sb.append("==");
	}

	public static void print(LuaOpGreater luaOpGreater, StringBuilder sb, int indent) {
		sb.append(">");
	}

	public static void print(LuaOpGreaterEq luaOpGreaterEq, StringBuilder sb, int indent) {
		sb.append(">=");
	}

	public static void print(LuaOpLess luaOpLess, StringBuilder sb, int indent) {
		sb.append("<");
	}

	public static void print(LuaOpLessEq luaOpLessEq, StringBuilder sb, int indent) {
		sb.append("<=");
	}

	public static void print(LuaOpMinus luaOpMinus, StringBuilder sb, int indent) {
		sb.append("-");
	}

	public static void print(LuaOpMod luaOpMod, StringBuilder sb, int indent) {
		sb.append("%");
	}

	public static void print(LuaOpMult luaOpMult, StringBuilder sb, int indent) {
		sb.append("*");
	}

	public static void print(LuaOpNot luaOpNot, StringBuilder sb, int indent) {
		sb.append("not");
	}

	public static void print(LuaOpOr luaOpOr, StringBuilder sb, int indent) {
		sb.append("or");
	}

	public static void print(LuaOpPlus luaOpPlus, StringBuilder sb, int indent) {
		sb.append("+");
	}

	public static void print(LuaOpUnequals luaOpUnequals, StringBuilder sb, int indent) {
		sb.append("~=");
	}

	public static void print(LuaParams luaParams, StringBuilder sb, int indent) {
		boolean first = true;
		for (LuaVariable p : luaParams) {
			if (!first) {
				sb.append(", ");
			}
			sb.append(p.getName());
			first = false;
		}
	}

	public static void print(LuaReturn s, StringBuilder sb, int indent) {
		sb.append("return ");
		s.getRetVal().print(sb, indent);
	}

	public static void print(LuaStatements stmts, StringBuilder sb, int indent) {
		for (LuaStatement s : stmts) {
			printIndent(sb, indent);
			s.print(sb, indent);
			sb.append("\n");
		}
	}

	public static void print(LuaTableConstructor e, StringBuilder sb, int indent) {
		sb.append("{");
		e.getTableFields().print(sb, indent);
		sb.append("}");
	}

	public static void print(LuaTableExprField e, StringBuilder sb, int indent) {
		sb.append("[");
		e.getFieldKey().print(sb, indent);
		sb.append("])");
		e.getVal().print(sb, indent);
	}

	public static void print(LuaTableFields fields, StringBuilder sb, int indent) {
		for (LuaTableField f : fields) {
			f.print(sb, indent);
			sb.append(", ");
		}
	}

	public static void print(LuaTableNamedField e, StringBuilder sb, int indent) {
		sb.append(e.getFieldName());
		sb.append("=");
		e.getVal().print(sb, indent);
	}

	public static void print(LuaTableSingleField e, StringBuilder sb, int indent) {
		e.getVal().print(sb, indent);
	}

	public static void print(LuaVariable v, StringBuilder sb, int indent) {
		sb.append(v.getName());
	}

	public static void print(LuaWhile s, StringBuilder sb, int indent) {
		sb.append("while ");
		s.getCond().print(sb, indent);
		sb.append(" do\n");
		s.getBody().print(sb, indent+1);
		sb.append("end");
	}

}
