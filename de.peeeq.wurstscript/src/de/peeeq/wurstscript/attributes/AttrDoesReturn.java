package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.NoDefaultCase;
import de.peeeq.wurstscript.ast.StmtIf;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.SwitchCase;
import de.peeeq.wurstscript.ast.SwitchDefaultCase;
import de.peeeq.wurstscript.ast.SwitchDefaultCaseStatements;
import de.peeeq.wurstscript.ast.SwitchStmt;
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
		nextStatement: for (WStatement s : statements) {
			if (returns) {
				s.addError("Unreachable code, function already returned");
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
			if (s instanceof SwitchStmt) {
				SwitchStmt switchStmt = (SwitchStmt) s;
				
				for( SwitchCase c : switchStmt.getCases() ) {
					if(!c.getStmts().attrDoesReturn()) {
						returns = false;
						continue nextStatement;
					}
				}
				SwitchDefaultCase dc = switchStmt.getSwitchDefault();
				if(dc instanceof NoDefaultCase) {
					returns = false;
					continue nextStatement;
					//TODO all enum cases
				}else if(dc instanceof SwitchDefaultCaseStatements)  {
					if (((SwitchDefaultCaseStatements) dc).getStmts().attrDoesReturn() ) {
						returns = true;
					}
				} else {
					throw new Error("not implemented");
				}
				
			}
		}
		return returns;
	}


}