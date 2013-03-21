package de.peeeq.wurstscript.intermediateLang.optimizer;

import de.peeeq.wurstscript.jassIm.ImBoolVal;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.jassIm.JassImElement;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

public class SimpleRewrites {

	private final ImProg prog;
	private final ImTranslator trans;
	
	public SimpleRewrites(ImTranslator trans) {
		this.prog = trans.getImProg();
		this.trans = trans;
	}
	
	public void optimize() {
		optimizeElement(prog);
		// we need to flatten the program, because we introduced new StatementExprs 
		prog.flatten(trans);
	}

	/** Recursively optimizes the element */
	private void optimizeElement(JassImElement elem) {
		// optimize children:
		for (int i=0; i<elem.size(); i++) {
			optimizeElement(elem.get(i));
		}
		if (elem instanceof ImIf) {
			ImIf imIf = (ImIf) elem;
			optimizeIf(imIf);
		}
		
	}

	private void optimizeIf(ImIf imIf) {
		if (imIf.getCondition() instanceof ImBoolVal) {
			ImBoolVal boolVal = (ImBoolVal) imIf.getCondition();
			if (boolVal.getValB()) {
				// we have something like 'if true ...'
				// replace the if statement with the then-block
				// we have to use ImStatementExpr to get multiple statements into one statement as needed
				// for the replaceWith function
				// we need to copy the thenBlock because otherwise it would have two parents (we have not removed it from the old if-block)
				imIf.replaceWith(JassIm.ImStatementExpr(imIf.getThenBlock().copy(), JassIm.ImNull()));
			}
			
		}
		
	}
	
	
}
