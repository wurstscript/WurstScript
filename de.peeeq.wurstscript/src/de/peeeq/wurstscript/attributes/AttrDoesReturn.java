package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.StmtIfPos;
import de.peeeq.wurstscript.ast.StmtReturnPos;
import de.peeeq.wurstscript.ast.WStatementPos;
import de.peeeq.wurstscript.ast.WStatementsPos;


/**
 * this attribute calculates for each sequence of statements whether
 * they have a return statement on every path 
 * 
 *
 */
public class AttrDoesReturn extends Attribute<WStatementsPos, Boolean> {

	 
	public AttrDoesReturn(Attributes attr) {
		super(attr);
	}

	@Override
	protected Boolean calculate(WStatementsPos statements) {
		boolean returns = false; 
		for (WStatementPos s : statements) {
			if (returns) {
				attr.addError(s.source(), "Unreachable code, function already returned");
				return true;
			}
			if (s instanceof StmtReturnPos) {
				returns = true;
			}
			// TODO AttrDoesReturn - voidreturns, break & exitwhen?
			if (s instanceof StmtIfPos) {
				StmtIfPos stmtIfPos = (StmtIfPos) s;
				returns = get(stmtIfPos.thenBlock()) && get(stmtIfPos.elseBlock()); 
			}
		}
		return returns;
	}


}