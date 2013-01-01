package de.peeeq.wurstscript.translation.imoptimizer;

import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

public class ImOptimizer {
	
	ImTranslator trans;
	
	public ImOptimizer(ImTranslator trans) {
		this.trans = trans;
	}
	
	public void optimize() {
		ImCompressor compressor = new ImCompressor(trans);
		compressor.compressNames();
	}
	
	public void doInlining() {
		GlobalsInliner globalsInliner = new GlobalsInliner(trans);
		globalsInliner.inlineGlobals();
		ImInliner inliner = new ImInliner(trans);
		inliner.doInlining();
		trans.assertProperties();
	}

	public void doNullsetting() {
		NullSetter ns = new NullSetter(trans);
		ns.optimize();
		trans.assertProperties();
	}
	
}
