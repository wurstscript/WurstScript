package de.peeeq.wurstscript.galaxy.printer;

import static de.peeeq.wurstscript.galaxy.printer.GalaxyPrinter.assign;
import static de.peeeq.wurstscript.galaxy.printer.GalaxyPrinter.comma;
import static de.peeeq.wurstscript.galaxy.printer.GalaxyPrinter.printIndent;
import static de.peeeq.wurstscript.galaxy.printer.GalaxyPrinter.printStatements;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyExpr;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyStmtCall;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyStmtExitwhen;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyStmtIf;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyStmtLoop;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyStmtReturn;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyStmtReturnVoid;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyStmtSet;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyStmtSetArray;

public class StatementPrinter {

	public static void print(GalaxyStmtCall s, StringBuilder sb, int indent, boolean withSpace) {
		sb.append(s.getFuncName());
		sb.append("(");
		boolean first = true;
		for (GalaxyExpr e : s.getArguments()) {
			if (!first) {
				sb.append(comma(withSpace));
			}
			e.print(sb, withSpace);
			first = false;
		}
		sb.append(")");
	}
	

	public static void print(GalaxyStmtExitwhen s, StringBuilder sb, int indent, boolean withSpace) {
		sb.append("if (");
		s.getCond().print(sb, withSpace);
		sb.append(") { break; }");
	}
	

	public static void print(GalaxyStmtIf s, StringBuilder sb, int indent, boolean withSpace) {
		sb.append("if (");
		s.getCond().print(sb, withSpace);
		sb.append(") \n");
		printStatements(sb, indent+1, s.getThenBlock(), withSpace);
		if (s.getElseBlock().size() > 0) {
			printIndent(sb, indent, withSpace);
			if (s.getElseBlock().size() == 1 && s.getElseBlock().get(0) instanceof GalaxyStmtIf) {
				sb.append("} else ");
				s.getElseBlock().get(0).print(sb, indent, withSpace);
				return;
			} else {
				sb.append("} else {\n");
				printStatements(sb, indent+1, s.getElseBlock(), withSpace);
			}
		}
		printIndent(sb, indent, withSpace);
		sb.append("}");
	}
	

	public static void print(GalaxyStmtLoop s, StringBuilder sb, int indent, boolean withSpace) {
		sb.append("while (true) {\n");
		printStatements(sb, indent+1, s.getBody(), withSpace);
		printIndent(sb, indent, withSpace);
		sb.append("}");
	}
	

	public static void print(GalaxyStmtReturn s, StringBuilder sb, int indent, boolean withSpace) {
		sb.append("return ");
		s.getReturnValue().print(sb, withSpace);
	}
	

	public static void print(GalaxyStmtReturnVoid s, StringBuilder sb, int indent, boolean withSpace) {
		sb.append("return");
	}
	

	public static void print(GalaxyStmtSet s, StringBuilder sb, int indent, boolean withSpace) {
		sb.append(s.getLeft());
		sb.append(assign(withSpace));
		s.getRight().print(sb, withSpace);
	}
	

	public static void print(GalaxyStmtSetArray s, StringBuilder sb, int indent, boolean withSpace) {
		sb.append(s.getLeft());
		sb.append("[");
		s.getIndex().print(sb, withSpace);
		sb.append("]");
		sb.append(assign(withSpace));
		s.getRight().print(sb, withSpace);
	}
	
}
