package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.StmtIf;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.ast.WStatements;



/**
 * this attribute calculates for each sequence of statements whether
 * they have a return statement on every path 
 * 
 *
 */
public class AttrDoesReturn {
	
	public static Boolean calculate(WStatements statements) {
		boolean returns = false; 
		for (WStatement s : statements) {
			if (returns) {
				attr.addError(s.getSource(), "Unreachable code, function already returned");
				return true;
			}
			if (s instanceof StmtReturn) {
				returns = true;
			}
			// TODO AttrDoesReturn - voidreturns, break & exitwhen?
			if (s instanceof StmtIf) {
				StmtIf stmtIf = (StmtIf) s;
				returns = stmtIf.getThenBlock().attrDoesReturn() && stmtIf.getElseBlock().attrDoesReturn(); 
			}
		}
		return returns;
	}


}