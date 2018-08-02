package de.peeeq.wurstscript.translation.imoptimizer;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.optimizer.*;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImOptimizer {
    private int totalFunctionsRemoved = 0;
    private int totalGlobalsRemoved = 0;

    private static final ArrayList<OptimizerPass> localPasses = new ArrayList<>();
    private static final HashMap<String, Integer> totalCount = new HashMap<>();

    static {
        localPasses.add(new ConstantAndCopyPropagation());
        localPasses.add(new UselessFunctionCallsRemover());
        localPasses.add(new GlobalsInliner());
        localPasses.add(new BranchMerger());
        localPasses.add(new SimpleRewrites());
        localPasses.add(new TempMerger());
        localPasses.add(new LocalMerger());
    }


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
        GlobalsInliner globalsInliner = new GlobalsInliner();
        globalsInliner.optimize(trans);
        ImInliner inliner = new ImInliner(trans);
        inliner.doInlining();
        trans.assertProperties();
        // remove garbage, because inlined functions can be removed
        removeGarbage();
    }

    private int optCount = 1;

    public void localOptimizations() {
        totalCount.clear();
        removeGarbage();

        int finalItr = 0;
        for (int i = 1; i <= 10 && optCount > 0; i++) {
            optCount = 0;
            localPasses.forEach(pass -> {
                int count = pass.optimize(trans);
                optCount += count;
                totalCount.put(pass.getName(), totalCount.getOrDefault(pass.getName(), 0) + count);
            });
            trans.getImProg().flatten(trans);
            removeGarbage();
            finalItr = i;
            WLogger.info("=== Optimization pass: " + i + " opts: " + optCount + " ===");
        }
        WLogger.info("=== Local optimizations done! Ran " + finalItr + " passes. ===");
        totalCount.forEach((k, v) -> {
            WLogger.info("== " + k + ":   " + v);
        });
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
            ImProg prog = trans.imProg();
            trans.calculateCallRelationsAndUsedVariables();

            // keep only used variables
            int globalsBefore = prog.getGlobals().size();
            changes = prog.getGlobals().retainAll(trans.getReadVariables());
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
                        super.visit(e);
                        if (!trans.getReadVariables().contains(e.getLeft())) {
                            replacements.add(Pair.<ImStmt, ImStmt>create(e, e.getRight()));
                        }
                    }

                    @Override
                    public void visit(ImSetArrayTuple e) {
                        super.visit(e);
                        if (!trans.getReadVariables().contains(e.getLeft())) {
                            replacements.add(Pair.<ImStmt, ImStmt>create(e, e.getRight()));
                        }
                    }

                    @Override
                    public void visit(ImSetArray e) {
                        super.visit(e);
                        if (!trans.getReadVariables().contains(e.getLeft())) {
                            replacements.add(Pair.<ImStmt, ImStmt>create(e, e.getRight()));
                        }
                    }

                    @Override
                    public void visit(ImSetTuple e) {
                        super.visit(e);
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
