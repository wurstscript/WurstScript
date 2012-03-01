package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.ImArrayType;
import de.peeeq.wurstscript.jassIm.ImBoolVal;
import de.peeeq.wurstscript.jassIm.ImCall;
import de.peeeq.wurstscript.jassIm.ImExitwhen;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFuncRef;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctions;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImIntVal;
import de.peeeq.wurstscript.jassIm.ImLoop;
import de.peeeq.wurstscript.jassIm.ImNoExpr;
import de.peeeq.wurstscript.jassIm.ImNull;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImRealVal;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImSetArrayTuple;
import de.peeeq.wurstscript.jassIm.ImSetTuple;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImStatementExpr;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImStringVal;
import de.peeeq.wurstscript.jassIm.ImTupleExpr;
import de.peeeq.wurstscript.jassIm.ImTupleSelection;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImTypes;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayAccess;
import de.peeeq.wurstscript.jassIm.ImVars;
import de.peeeq.wurstscript.jassIm.ImVoid;

public class ImPrinter {

	
	public static void print(ImProg p, StringBuilder sb, int indent) {
		for (ImVar g : p.getGlobals()) {
			g.print(sb, indent);
		}
		for (ImFunction f : p.getFunctions()) {
			f.print(sb, indent);
		}
	}

	
	public static void print(ImSimpleType p, StringBuilder sb, int indent) {
		sb.append(p.getTypename());
	}
	
	public static void print(ImArrayType t, StringBuilder sb, int indent) {
		sb.append("array " + t.getTypename());
	}
	
	public static void print(ImTupleType p, StringBuilder sb, int indent) {
		sb.append("<");
		boolean first = true;
		for (ImType t : p.getTypes()) {
			if (!first) sb.append(", ");
			t.print(sb, indent);
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
		p.getBody().print(sb, indent);
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
		p.getLeft().print(sb, indent);
		sb.append(" = " );
		p.getRight().print(sb, indent);
	}
	
	public static void print(ImSetTuple p, StringBuilder sb, int indent) {
		p.getLeft().print(sb, indent);
		sb.append(" #" + p.getTupleIndex());
		sb.append(" = " );
		p.getRight().print(sb, indent);
	}
	
	public static void print(ImSetArray p, StringBuilder sb, int indent) {
		p.getLeft().print(sb, indent);
		sb.append("[");
		p.getIndex().print(sb, indent);
		sb.append("]");
		sb.append(" = " );
		p.getRight().print(sb, indent);
	}
	
	public static void print(ImSetArrayTuple p, StringBuilder sb, int indent) {
		p.getLeft().print(sb, indent);
		sb.append("[");
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
	
	
	public static void print(ImCall p, StringBuilder sb, int indent) {
		sb.append(p.getFunc().getName() + "(");
		
		sb.append(")");
	}
	
	
	public static void print(ImVarAccess p, StringBuilder sb, int indent) {
		throw new Error("not implemented");
		// TODO
	}
	
	
	public static void print(ImVarArrayAccess p, StringBuilder sb, int indent) {
		throw new Error("not implemented");
		// TODO
	}
	
	
	public static void print(ImTupleExpr p, StringBuilder sb, int indent) {
		throw new Error("not implemented");
		// TODO
	}
	
	
	public static void print(ImTupleSelection p, StringBuilder sb, int indent) {
		throw new Error("not implemented");
		// TODO
	}
	
	
	public static void print(ImIntVal p, StringBuilder sb, int indent) {
		throw new Error("not implemented");
		// TODO
	}
	
	
	public static void print(ImRealVal p, StringBuilder sb, int indent) {
		throw new Error("not implemented");
		// TODO
	}
	
	
	public static void print(ImStringVal p, StringBuilder sb, int indent) {
		throw new Error("not implemented");
		// TODO
	}
	
	
	public static void print(ImBoolVal p, StringBuilder sb, int indent) {
		throw new Error("not implemented");
		// TODO
	}
	
	
	public static void print(ImFuncRef p, StringBuilder sb, int indent) {
		throw new Error("not implemented");
		// TODO
	}
	
	
	public static void print(ImNull p, StringBuilder sb, int indent) {
		throw new Error("not implemented");
		// TODO
	}
	
	

	public static void print(ImExprs p, StringBuilder sb, int indent) {
		throw new Error("not implemented");
		// TODO
	}
	

	public static void print(ImFunctions p, StringBuilder sb, int indent) {
		throw new Error("not implemented");
		// TODO
	}
	

	public static void print(ImStmts p, StringBuilder sb, int indent) {
		throw new Error("not implemented");
		// TODO
	}
	

	public static void print(ImTypes p, StringBuilder sb, int indent) {
		throw new Error("not implemented");
		// TODO
	}
	
	

	public static void print(ImVar p, StringBuilder sb, int indent) {
		throw new Error("not implemented");
		// TODO
	}
	

	public static void print(ImVars p, StringBuilder sb, int indent) {
		throw new Error("not implemented");
		// TODO
	}
	
	
	
	
	
}
