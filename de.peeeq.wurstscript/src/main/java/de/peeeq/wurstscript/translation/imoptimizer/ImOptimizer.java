package de.peeeq.wurstscript.translation.imoptimizer;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.optimizer.ConstantAndCopyPropagation;
import de.peeeq.wurstscript.intermediatelang.optimizer.LocalMerger;
import de.peeeq.wurstscript.intermediatelang.optimizer.SimpleRewrites;
import de.peeeq.wurstscript.intermediatelang.optimizer.TempMerger;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Pair;

import java.util.List;

public class ImOptimizer {
    private int totalFunctionsRemoved = 0;
    private int totalGlobalsRemoved = 0;

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
        TempMerger tempMerger = new TempMerger(trans);
        ConstantAndCopyPropagation cpOpt = new ConstantAndCopyPropagation(trans);
        SimpleRewrites simpleRewrites = new SimpleRewrites(trans);
        LocalMerger localMerger = new LocalMerger(trans);
        UselessFunctionCallsRemover functionCallsRemover = new UselessFunctionCallsRemover(trans);
        GlobalsInliner globalsInliner = new GlobalsInliner(trans);
        removeGarbage();
        int deltaV = 1;
        int finalItr = 0;
        for (int i = 0; i <= 10 && deltaV > 0; i++) {
            deltaV = 0;
            int startV = tempMerger.totalMerged;
            tempMerger.optimize();
            int endV = tempMerger.totalMerged;
            deltaV += (endV - startV);
            startV = cpOpt.totalPropagated;
            cpOpt.optimize();
            endV = cpOpt.totalPropagated;
            deltaV += (endV - startV);
            startV = simpleRewrites.totalRewrites;
            simpleRewrites.optimize(false);
            endV = simpleRewrites.totalRewrites;
            deltaV += (endV - startV);
            WLogger.info("optimized: " + (endV - startV));
            startV = localMerger.totalLocalsMerged;
            localMerger.optimize();
            endV = localMerger.totalLocalsMerged;
            deltaV += (endV - startV);
            startV = functionCallsRemover.totalCallsRemoved;
            functionCallsRemover.optimize();
            endV = functionCallsRemover.totalCallsRemoved;
            deltaV += (endV - startV);
            startV = globalsInliner.obsoleteCount;
            globalsInliner.inlineGlobals();
            endV = globalsInliner.obsoleteCount;
            deltaV += (endV - startV);
            trans.getImProg().flatten(trans);
            removeGarbage();
            finalItr = i;
        }
        WLogger.info("=== Local optimizations done! Ran " + finalItr + " passes. ===");
        WLogger.info("== Temp vars merged:   " + tempMerger.totalMerged);
        WLogger.info("== Vars propagated:    " + cpOpt.totalPropagated);
        WLogger.info("== Rewrites:           " + simpleRewrites.totalRewrites);
        WLogger.info("== Locals merged:      " + localMerger.totalLocalsMerged);
        WLogger.info("== Calls removed:      " + functionCallsRemover.totalCallsRemoved);
        WLogger.info("== Globals Inlined:    " + globalsInliner.obsoleteCount);
        WLogger.info("== Globals removed:    " + totalGlobalsRemoved);
        WLogger.info("== Functions removed:  " + totalFunctionsRemoved);
    }

    public void doNullsetting() {
        NullSetter ns = new NullSetter(trans);
        ns.optimize();
        trans.assertProperties();
    }

    public void removeGarbage() {
        boolean changes = true;
        int iterations = 0;
        while (changes && iterations++ < 10) {
            changes = false;
            ImProg prog = trans.imProg();
            trans.calculateCallRelationsAndUsedVariables();

            // keep only used variables
            int globalsBefore = prog.getGlobals().size();
            changes |= prog.getGlobals().retainAll(trans.getReadVariables());
            int globalsAfter = prog.getGlobals().size();
            int globalsRemoved = globalsBefore - globalsAfter;
            totalGlobalsRemoved += globalsRemoved;
            // keep only functions reachable from main and config
            int functionsBefore = prog.getFunctions().size();
            changes |= prog.getFunctions().retainAll(trans.getUsedFunctions());
            int functionsAfter = prog.getFunctions().size();
            int functionsRemoved = functionsBefore - functionsAfter;
            totalFunctionsRemoved += functionsRemoved;
            for (ImFunction f : prog.getFunctions()) {
                // remove set statements to unread variables
                final List<Pair<ImStmt, ImStmt>> replacements = Lists.newArrayList();
                f.accept(new ImFunction.DefaultVisitor() {
                    @Override
                    public void visit(ImSet e) {
                        if (!trans.getReadVariables().contains(e.getLeft())) {
                            replacements.add(Pair.<ImStmt, ImStmt>create(e, e.getRight()));
                        }
                    }

                    @Override
                    public void visit(ImSetArrayTuple e) {
                        if (!trans.getReadVariables().contains(e.getLeft())) {
                            replacements.add(Pair.<ImStmt, ImStmt>create(e, e.getRight()));
                        }
                    }

                    @Override
                    public void visit(ImSetArray e) {
                        if (!trans.getReadVariables().contains(e.getLeft())) {
                            replacements.add(Pair.<ImStmt, ImStmt>create(e, e.getRight()));
                        }
                    }

                    @Override
                    public void visit(ImSetTuple e) {
                        if (!trans.getReadVariables().contains(e.getLeft())) {
                            replacements.add(Pair.<ImStmt, ImStmt>create(e, e.getRight()));
                        }
                    }
                });
                for (Pair<ImStmt, ImStmt> pair : replacements) {
                    changes = true;
                    pair.getB().setParent(null);
                    pair.getA().replaceBy(pair.getB());
                }

                // keep only read local variables
                changes |= f.getLocals().retainAll(trans.getReadVariables());
            }
        }
    }

}
