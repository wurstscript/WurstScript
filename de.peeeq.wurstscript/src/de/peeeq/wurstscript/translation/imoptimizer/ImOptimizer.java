package de.peeeq.wurstscript.translation.imoptimizer;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.intermediateLang.optimizer.SimpleRewrites;
import de.peeeq.wurstscript.intermediateLang.optimizer.TempMerger;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImSetArrayTuple;
import de.peeeq.wurstscript.jassIm.ImSetTuple;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Pair;

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
		removeGarbage();
		new TempMerger(trans).optimize();
		new SimpleRewrites(trans).optimize();
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
		boolean changes = true;
		int iterations = 0;
		while (changes && iterations++ < 100) {
			changes = false;
			
			ImProg prog = trans.imProg();
			trans.calculateCallRelationsAndUsedVariables();
			
			// keep only used variables
			changes |= prog.getGlobals().retainAll(trans.getReadVariables());
			// keep only functions reachable from main and config
			changes |= prog.getFunctions().retainAll(trans.getUsedFunctions());
			
			for (ImFunction f: prog.getFunctions()) {
				// remove set statements to unread variables
				final List<Pair<ImStmt, ImStmt>> replacements = Lists.newArrayList();
				f.accept(new ImFunction.DefaultVisitor() {
					@Override
					public void visit(ImSet e) {
						if (!trans.getReadVariables().contains(e.getLeft())) {
							replacements.add(Pair.<ImStmt,ImStmt>create(e, e.getRight()));
						}
					}
					@Override
					public void visit(ImSetArrayTuple e) {
						if (!trans.getReadVariables().contains(e.getLeft())) {
							replacements.add(Pair.<ImStmt,ImStmt>create(e, e.getRight()));
						}
					}
					@Override
					public void visit(ImSetArray e) {
						if (!trans.getReadVariables().contains(e.getLeft())) {
							replacements.add(Pair.<ImStmt,ImStmt>create(e, e.getRight()));
						}
					}
					@Override
					public void visit(ImSetTuple e) {
						if (!trans.getReadVariables().contains(e.getLeft())) {
							replacements.add(Pair.<ImStmt,ImStmt>create(e, e.getRight()));
						}
					}
				});
				for (Pair<ImStmt, ImStmt> pair : replacements) {
					changes = true;
					pair.getB().setParent(null);
					pair.getA().replaceWith(pair.getB());
				}
				
				// keep only read local variables
				changes |= f.getLocals().retainAll(trans.getReadVariables());
			}
		}
	}
	
}
