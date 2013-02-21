package de.peeeq.wurstscript.translation.imoptimizer;

import de.peeeq.wurstscript.intermediateLang.optimizer.TempMerger;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

public class ImOptimizer {
	
	ImTranslator trans;
	
	public ImOptimizer(ImTranslator trans) {
		this.trans = trans;
	}
	
	public void optimize() {
		removeGarbage();
		ImCompressor compressor = new ImCompressor(trans);
		compressor.compressNames();
	}
	
	public void doInlining() {
		// remove garbage to reduce work for the inliner
		removeGarbage();
		GlobalsInliner globalsInliner = new GlobalsInliner(trans);
		globalsInliner.inlineGlobals();
		ImInliner inliner = new ImInliner(trans);
		inliner.doInlining();
		trans.assertProperties();
		// remove garbage, because inlined functions can be removed
		removeGarbage();
	}
	
	public void localOptimizations() {
		new TempMerger(trans).optimize();
		removeGarbage();
	}

	public void doNullsetting() {
		NullSetter ns = new NullSetter(trans);
		ns.optimize();
		trans.assertProperties();
	}
	
	public void removeGarbage() {
		if (trans.isUnitTestMode()) {
//			return;
		}
		ImProg prog = trans.imProg();
		trans.calculateCallRelationsAndUsedVariables();
		// keep only used variables
		prog.getGlobals().retainAll(trans.getUsedVariables());
		// keep only functions reachable from main and config
		prog.getFunctions().retainAll(trans.getUsedFunctions());
		
		for (ImFunction f: prog.getFunctions()) {
			// keep only used local variables
			f.getLocals().retainAll(trans.getUsedVariables());
		}
	}
	
}
