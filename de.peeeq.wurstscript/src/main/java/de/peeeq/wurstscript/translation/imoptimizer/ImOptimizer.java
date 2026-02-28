package de.peeeq.wurstscript.translation.imoptimizer;

import com.google.common.collect.Lists;
import de.peeeq.wurstio.TimeTaker;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.optimizer.BranchMerger;
import de.peeeq.wurstscript.intermediatelang.optimizer.ConstantAndCopyPropagation;
import de.peeeq.wurstscript.intermediatelang.optimizer.LocalMerger;
import de.peeeq.wurstscript.intermediatelang.optimizer.SimpleRewrites;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.validation.TRVEHelper;

import java.util.*;

public class ImOptimizer {
    private int totalFunctionsRemoved = 0;
    private int totalGlobalsRemoved = 0;

    private static final ArrayList<OptimizerPass> localPasses = new ArrayList<>();
    private static final HashMap<String, Integer> totalCount = new HashMap<>();

    static {
        localPasses.add(new SimpleRewrites());
        localPasses.add(new LocalMerger());
        localPasses.add(new BranchMerger());
        localPasses.add(new ConstantAndCopyPropagation());
        localPasses.add(new UselessFunctionCallsRemover());
        localPasses.add(new GlobalsInliner());
        localPasses.add(new SimpleRewrites());
    }

    private final TimeTaker timeTaker;
    ImTranslator trans;

    public ImOptimizer(TimeTaker timeTaker, ImTranslator trans) {
        this.timeTaker = timeTaker;
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
            for (OptimizerPass pass : localPasses) {
                int count = timeTaker.measure(pass.getName(), () -> pass.optimize(trans));
                optCount += count;
                totalCount.put(pass.getName(), totalCount.getOrDefault(pass.getName(), 0) + count);
            }

            if (optCount > 0) {
                removeGarbage();
                trans.getImProg().flatten(trans);
            }

            finalItr = i;
            WLogger.info("=== Optimization pass: " + i + " opts: " + optCount + " ===");
        }
        WLogger.info("=== Local optimizations done! Ran " + finalItr + " passes. ===");
        totalCount.forEach((k, v) -> WLogger.info("== " + k + ":   " + v));
    }

    public void doNullsetting() {
        NullSetter ns = new NullSetter(trans);
        ns.optimize();
        trans.assertProperties();
    }

    public boolean removeGarbage() {
        boolean changes = true;
        boolean anyChanges = false;
        int iterations = 0;
        while (changes && iterations++ < 10) {
            ImProg prog = trans.imProg();
            trans.calculateCallRelationsAndReadVariables();
            final Set<ImVar> readVars = trans.getReadVariables();
            final Set<ImFunction> usedFuncs = trans.getUsedFunctions();

            // keep only used variables
            int globalsBefore = prog.getGlobals().size();
            changes = prog.getGlobals().retainAll(readVars);
            int globalsAfter = prog.getGlobals().size();
            int globalsRemoved = globalsBefore - globalsAfter;
            totalGlobalsRemoved += globalsRemoved;

            // keep only functions reachable from main and config
            int functionsBefore = prog.getFunctions().size();
            changes |= prog.getFunctions().retainAll(usedFuncs);
            int functionsAfter = prog.getFunctions().size();
            int functionsRemoved = functionsBefore - functionsAfter;
            totalFunctionsRemoved += functionsRemoved;

            // also consider class functions
            Set<ImFunction> allFunctions = new HashSet<>(prog.getFunctions());
            for (ImClass c : prog.getClasses()) {
                int classFunctionsBefore = c.getFunctions().size();
                changes |= c.getFunctions().retainAll(usedFuncs);
                int classFunctionsAfter = c.getFunctions().size();
                totalFunctionsRemoved += classFunctionsBefore - classFunctionsAfter;
                allFunctions.addAll(c.getFunctions());

                int classFieldsBefore = c.getFields().size();
                changes |= c.getFields().retainAll(readVars);
                int classFieldsAfter = c.getFields().size();
                totalGlobalsRemoved += classFieldsBefore - classFieldsAfter;
            }

            for (ImFunction f : allFunctions) {
                // remove set statements to unread variables
                final List<Pair<ImStmt, List<ImExpr>>> replacements = Lists.newArrayList();
                f.accept(new ImFunction.DefaultVisitor() {
                    @Override
                    public void visit(ImSet e) {
                        super.visit(e);
                        if (e.getLeft() instanceof ImVarAccess) {
                            ImVarAccess va = (ImVarAccess) e.getLeft();
                            if (!readVars.contains(va.getVar()) && !TRVEHelper.protectedVariables.contains(va.getVar().getName())) {
                                replacements.add(Pair.create(e, Collections.singletonList(e.getRight())));
                            }
                        } else if (e.getLeft() instanceof ImVarArrayAccess) {
                            ImVarArrayAccess va = (ImVarArrayAccess) e.getLeft();
                            if (!readVars.contains(va.getVar()) && !TRVEHelper.protectedVariables.contains(va.getVar().getName())) {
                                // IMPORTANT: removeAll() clears parent references
                                List<ImExpr> exprs = va.getIndexes().removeAll();
                                exprs.add(e.getRight());
                                replacements.add(Pair.create(e, exprs));
                            }
                        } else if (e.getLeft() instanceof ImTupleSelection) {
                            ImVar var = TypesHelper.getTupleVar((ImTupleSelection) e.getLeft());
                            if(var != null && !readVars.contains(var) && !TRVEHelper.protectedVariables.contains(var.getName())) {
                                replacements.add(Pair.create(e, Collections.singletonList(e.getRight())));
                            }
                        } else if(e.getLeft() instanceof ImMemberAccess) {
                            ImMemberAccess va = ((ImMemberAccess) e.getLeft());
                            if (!readVars.contains(va.getVar()) && !TRVEHelper.protectedVariables.contains(va.getVar().getName())) {
                                replacements.add(Pair.create(e, Collections.singletonList(e.getRight())));
                            }
                        }
                    }
                });

                Replacer replacer = new Replacer();
                for (Pair<ImStmt, List<ImExpr>> pair : replacements) {
                    changes = true;
                    ImExpr r;
                    if (pair.getB().size() == 1) {
                        r = pair.getB().get(0);
                        // CRITICAL: Clear parent before reusing the node
                        r.setParent(null);
                    } else {
                        // CRITICAL: Create proper list wrapper for multiple expressions
                        List<ImStmt> stmts = new ArrayList<>();
                        for (ImExpr expr : pair.getB()) {
                            // Clear parent for each expression
                            expr.setParent(null);
                            stmts.add(expr);
                        }
                        r = ImHelper.statementExprVoid(JassIm.ImStmts(stmts));
                    }
                    replacer.replace(pair.getA(), r);
                }

                // keep only read local variables
                changes |= f.getLocals().retainAll(readVars);
            }
            anyChanges |= changes;
        }
        return anyChanges;
    }
}
