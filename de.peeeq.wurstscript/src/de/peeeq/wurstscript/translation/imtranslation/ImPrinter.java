package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.ImArrayType;
import de.peeeq.wurstscript.jassIm.ImBoolVal;
import de.peeeq.wurstscript.jassIm.ImCall;
import de.peeeq.wurstscript.jassIm.ImExitwhen;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFuncRef;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImIntVal;
import de.peeeq.wurstscript.jassIm.ImLoop;
import de.peeeq.wurstscript.jassIm.ImNoExpr;
import de.peeeq.wurstscript.jassIm.ImNull;
import de.peeeq.wurstscript.jassIm.ImOperatorCall;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImRealVal;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImSetArrayTuple;
import de.peeeq.wurstscript.jassIm.ImSetTuple;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImStatementExpr;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImStringVal;
import de.peeeq.wurstscript.jassIm.ImTupleArrayType;
import de.peeeq.wurstscript.jassIm.ImTupleExpr;
import de.peeeq.wurstscript.jassIm.ImTupleSelection;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayAccess;
import de.peeeq.wurstscript.jassIm.ImVoid;

public class ImPrinter {

	
	public static void print(ImProg p, StringBuilder sb, int indent) {
		for (ImVar g : p.getGlobals()) {
			g.print(sb, indent);
			sb.append("\n");
		}
		sb.append("\n\n");
		for (ImFunction f : p.getFunctions()) {
			f.print(sb, indent);
			sb.append("\n");
		}
	}

	
	public static void print(ImSimpleType p, StringBuilder sb, int indent) {
		sb.append(p.getTypename());
	}
	
	public static void print(ImArrayType t, StringBuilder sb, int indent) {
		sb.append("array " + t.getTypename());
	}
	
	public static void print(ImTupleArrayType p, StringBuilder sb, int indent) {
		sb.append("array<");
		boolean first = true;
		for (String t : p.getTypes()) {
			if (!first) sb.append(", ");
			sb.append(t);
			first = false;
		}
		sb.append(">");
	}
	
	public static void print(ImTupleType p, StringBuilder sb, int indent) {
		sb.append("<");
		boolean first = true;
		for (String t : p.getTypes()) {
			if (!first) sb.append(", ");
			sb.append(t);
			first = false;
		}
		sb.append(">");
	}

	public static void print(ImVoid p, StringBuilder sb, int indent) {
		sb.append("void");
	}
	
	public static void print(ImFunction p, StringBuilder sb, int indent) {
		sb.append("function " + p.getName() + "(");
		boolean first = true;
		for (ImVar p1 : p.getParameters()) {
			if (!first) sb.append(", ");
			p1.print(sb, indent);
			first = false;
		}
		sb.append(") returns ");
		p.getReturnType().print(sb, indent);
		sb.append("{ \n");
		for (ImVar v : p.getLocals()) {
			indent(sb, indent+1);
			sb.append("local ");
			v.print(sb, indent+1);
			sb.append("\n");
		}
		p.getBody().print(sb, indent+1);
		sb.append("}\n\n");
	}
	
	public static void print(ImIf p, StringBuilder sb, int indent) {
		sb.append("if " );
		p.getCondition().print(sb, indent);
		sb.append("{\n");
		p.getThenBlock().print(sb, indent+1);
		indent(sb,indent);
		sb.append("} else {\n");
		p.getElseBlock().print(sb, indent+1);
		indent(sb,indent);
		sb.append("}");
		
	}
	
	private static void indent(StringBuilder sb, int indent) {
		for (int i=0; i<indent; i++) {
			sb.append("    ");
		}
	}


	public static void print(ImLoop p, StringBuilder sb, int indent) {
		sb.append("loop {\n");
		p.getBody().print(sb, indent+1);		
		indent(sb,indent);
		sb.append("}");
	}
	
	public static void print(ImExitwhen p, StringBuilder sb, int indent) {
		sb.append("exitwhen ");
		p.getCondition().print(sb, indent);
	}
	
	public static void print(ImReturn p, StringBuilder sb, int indent) {
		sb.append("return ");
		p.getReturnValue().print(sb, indent);
	}
	
	public static void print(ImSet p, StringBuilder sb, int indent) {
		sb.append(p.getLeft().getName() + smallHash(p.getLeft()) + " = " );
		p.getRight().print(sb, indent);
	}
	
	public static void print(ImSetTuple p, StringBuilder sb, int indent) {
		sb.append(p.getLeft().getName() + smallHash(p.getLeft()) + " #" + p.getTupleIndex());
		sb.append(" = " );
		p.getRight().print(sb, indent);
	}
	
	public static void print(ImSetArray p, StringBuilder sb, int indent) {
		sb.append(p.getLeft().getName() + smallHash(p.getLeft()) + "[");
		p.getIndex().print(sb, indent);
		sb.append("]");
		sb.append(" = " );
		p.getRight().print(sb, indent);
	}
	
	public static void print(ImSetArrayTuple p, StringBuilder sb, int indent) {
		sb.append(p.getLeft().getName() + smallHash(p.getLeft()) + "[");
		p.getIndex().print(sb, indent);
		sb.append("]");
		sb.append(" #" + p.getTupleIndex());
		sb.append(" = " );
		p.getRight().print(sb, indent);
	}
	
	public static void print(ImNoExpr p, StringBuilder sb, int indent) {
		sb.append("%nothing%");
	}	
	
	public static void print(ImStatementExpr p, StringBuilder sb, int indent) {
		sb.append("{\n");
		p.getStatements().print(sb, indent+1);
		indent(sb, indent+1);
		p.getExpr().print(sb, indent);
		sb.append("}");
	}
	
	
	public static void print(ImFunctionCall p, StringBuilder sb, int indent) {
		sb.append(p.getFunc().getName() + "(");
		boolean first = true;
		for (ImExpr a : p.getArguments()) {
			if (!first) {
				sb.append(", ");
			}
			a.print(sb, indent);
			first = false;
		}
		sb.append(")");
	}
	
	
	public static void print(ImVarAccess p, StringBuilder sb, int indent) {
		sb.append(p.getVar().getName() + smallHash(p.getVar()));
	
	}
	
	private static String smallHash(Object g) {
//		return "";
		String c = "" +g.hashCode();
		return c.substring(0, Math.min(3, c.length()-1));
	}
	
	
	public static void print(ImVarArrayAccess p, StringBuilder sb, int indent) {
		sb.append(p.getVar().getName() + smallHash(p.getVar()) + "[");
		p.getIndex().print(sb, indent);
		sb.append("]");
	}
	
	
	public static void print(ImTupleExpr p, StringBuilder sb, int indent) {
		sb.append("<");
		boolean first = true;
		for (ImExpr e : p.getExprs()) {
			if (!first) sb.append(", ");
			e.print(sb, indent);
			first = false;
		}
		sb.append(">");
	}
	
	
	public static void print(ImTupleSelection p, StringBuilder sb, int indent) {
		p.getTupleExpr().print(sb, indent);
		sb.append("#" + p.getTupleIndex());
	}
	
	
	public static void print(ImIntVal p, StringBuilder sb, int indent) {
		sb.append(p.getValI());
	}
	
	
	public static void print(ImRealVal p, StringBuilder sb, int indent) {
		sb.append(p.getValR());
	}
	
	
	public static void print(ImStringVal p, StringBuilder sb, int indent) {
		sb.append('"');
		sb.append(p.getValS());
		sb.append('"');
	}
	
	
	public static void print(ImBoolVal p, StringBuilder sb, int indent) {
		sb.append(p.getValB() ? "true" : "false");
	}
	
	
	public static void print(ImFuncRef p, StringBuilder sb, int indent) {
		sb.append("function " + p.getFunc().getName());
	}
	
	
	public static void print(ImNull p, StringBuilder sb, int indent) {
		sb.append("null");
	}


	public static void print(ImStmts ss, StringBuilder sb, int indent) {
		for (ImStmt s : ss) {
			indent(sb, indent);
			s.print(sb, indent);
			sb.append("\n");
		}
	}


	public static void print(ImVar v, StringBuilder sb, int indent) {
		v.getType().print(sb, indent);
		sb.append(" ");
		sb.append(v.getName() + smallHash(v));
	}


	public static void print(ImOperatorCall e, StringBuilder sb, int indent) {
		sb.append("(");
		if (e.getArguments().size() == 2) {
			// binary operator
			e.getArguments().get(0).print(sb, indent);
			sb.append(" " + e.getOp().toSymbol() + " ");
			e.getArguments().get(1).print(sb, indent);
		} else {
			sb.append(e.getOp().toSymbol());
			for (ImExpr a : e.getArguments()) {
				sb.append(" ");
				a.print(sb, indent);
			}
		}
		sb.append(")");
	}


	
	
	

	
}
