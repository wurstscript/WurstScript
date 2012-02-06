package de.peeeq.wurstscript.jassprinter;

import de.peeeq.wurstscript.jassAst.*;
import static de.peeeq.wurstscript.jassprinter.JassPrinter.*;

public class StatementPrinter {

	public static void print(JassStmtCall s, StringBuilder sb, int indent, boolean withSpace) {
		sb.append("call ");
		sb.append(s.getFuncName());
		sb.append("(");
		boolean first = true;
		for (JassExpr e : s.getArguments()) {
			if (!first) {
				sb.append(comma(withSpace));
			}
			e.print(sb, withSpace);
			first = false;
		}
		sb.append(")");
	}
	

	public static void print(JassStmtExitwhen s, StringBuilder sb, int indent, boolean withSpace) {
		sb.append("exitwhen ");
		s.getCond().print(sb, withSpace);
	}
	

	public static void print(JassStmtIf s, StringBuilder sb, int indent, boolean withSpace) {
		sb.append("if ");
		s.getCond().print(sb, withSpace);
		sb.append(" then\n");
		printStatements(sb, indent+1, s.getThenBlock(), withSpace);
		if (s.getElseBlock().size() > 0) {
			printIndent(sb, indent, withSpace);
			sb.append("else\n");
			printStatements(sb, indent+1, s.getElseBlock(), withSpace);
		}
		printIndent(sb, indent, withSpace);
		sb.append("endif");
	}
	

	public static void print(JassStmtLoop s, StringBuilder sb, int indent, boolean withSpace) {
		sb.append("loop\n");
		printStatements(sb, indent+1, s.getBody(), withSpace);
		printIndent(sb, indent, withSpace);
		sb.append("endloop");
	}
	

	public static void print(JassStmtReturn s, StringBuilder sb, int indent, boolean withSpace) {
		sb.append("return ");
		s.getReturnValue().print(sb, withSpace);
	}
	

	public static void print(JassStmtReturnVoid s, StringBuilder sb, int indent, boolean withSpace) {
		sb.append("return");
	}
	

	public static void print(JassStmtSet s, StringBuilder sb, int indent, boolean withSpace) {
		sb.append("set ");
		sb.append(s.getLeft());
		sb.append(assign(withSpace));
		s.getRight().print(sb, withSpace);
	}
	

	public static void print(JassStmtSetArray s, StringBuilder sb, int indent, boolean withSpace) {
		sb.append("set ");
		sb.append(s.getLeft());
		sb.append("[");
		s.getIndex().print(sb, withSpace);
		sb.append("]");
		sb.append(assign(withSpace));
		s.getRight().print(sb, withSpace);
	}
	
	
	

}
