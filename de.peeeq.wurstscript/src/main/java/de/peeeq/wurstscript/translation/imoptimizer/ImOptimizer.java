package de.peeeq.wurstscript.translation.imoptimizer;

import com.google.common.collect.Lists;
import de.peeeq.wurstio.TimeTaker;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.optimizer.BranchMerger;
import de.peeeq.wurstscript.intermediatelang.optimizer.ConstantAndCopyPropagation;
import de.peeeq.wurstscript.intermediatelang.optimizer.DispatchCheckDeduplicator;
import de.peeeq.wurstscript.intermediatelang.optimizer.LocalPlayerAwareOptimizerPass;
import de.peeeq.wurstscript.intermediatelang.optimizer.LocalPlayerContextAnalyzer;
import de.peeeq.wurstscript.intermediatelang.optimizer.LocalMerger;
import de.peeeq.wurstscript.intermediatelang.optimizer.SideEffectAnalyzer;
import de.peeeq.wurstscript.intermediatelang.optimizer.SimpleRewrites;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.validation.NamePreservation;

import java.util.stream.Collectors;

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
        localPasses.add(new DispatchCheckDeduplicator());
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

    public int inlineLuaDivModHelpersWithinLocalBudget() {
        return new ImInliner(trans).inlineLuaDivModHelpersWithinLocalBudget();
    }
    public void localOptimizations() {
        totalCount.clear();

        removeGarbage();

        int optCount = runLocalOptimizationSweep();
        if (optCount > 0) {
            removeGarbage();
            trans.getImProg().flatten(trans);
        }

        int cleanupCount = runLocalOptimizationSweep();
        if (cleanupCount > 0) {
            removeGarbage();
            trans.getImProg().flatten(trans);
        }

        WLogger.info("=== Local optimization passes done! Opts: " + (optCount + cleanupCount) + " ===");
        totalCount.forEach((k, v) -> WLogger.info("== " + k + ":   " + v));
    }

    private int runLocalOptimizationSweep() {
        int optCount = 0;
        LocalPlayerContextAnalyzer localPlayerContextAnalyzer = null;
        for (OptimizerPass pass : localPasses) {
            int count;
            if (pass instanceof LocalPlayerAwareOptimizerPass) {
                if (localPlayerContextAnalyzer == null) {
                    localPlayerContextAnalyzer =
                        new LocalPlayerContextAnalyzer(trans.getImProg());
                }
                LocalPlayerContextAnalyzer analyzer = localPlayerContextAnalyzer;
                LocalPlayerAwareOptimizerPass localPlayerAwarePass =
                    (LocalPlayerAwareOptimizerPass) pass;
                count = timeTaker.measure(
                    pass.getName(),
                    () -> localPlayerAwarePass.optimize(trans, analyzer));
            } else {
                count = timeTaker.measure(pass.getName(), () -> pass.optimize(trans));
                // A general mutating pass may invalidate dependency edges.
                localPlayerContextAnalyzer = null;
            }
            optCount += count;
            totalCount.put(pass.getName(), totalCount.getOrDefault(pass.getName(), 0) + count);
        }
        return optCount;
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
            SideEffectAnalyzer sideEffectAnalyzer = new SideEffectAnalyzer(prog);

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

                // A field of a specialised class is a copy which nothing refers to, an access made
                // before specialisation still naming the original's variable. It is live exactly
                // when the field it was copied from is; dropping it leaves an instance allocated
                // with no fields while the emitted code goes on reading them.
                int classFieldsBefore = c.getFields().size();
                changes |= c.getFields().retainAll(c.getFields().stream()
                    .filter(field -> readVars.contains(field)
                        || readVars.contains(trans.canonical(field)))
                    .collect(Collectors.toSet()));
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
                            if (!readVars.contains(va.getVar()) && !NamePreservation.isPreserved(va.getVar())) {
                                List<ImExpr> sideEffects = collectSideEffects(e.getRight(), sideEffectAnalyzer);
                                replacements.add(Pair.create(e, sideEffects));
                            }
                        } else if (e.getLeft() instanceof ImVarArrayAccess) {
                            ImVarArrayAccess va = (ImVarArrayAccess) e.getLeft();
                            if (!readVars.contains(va.getVar()) && !NamePreservation.isPreserved(va.getVar())) {
                                List<ImExpr> exprs = new ArrayList<>();
                                for (ImExpr index : va.getIndexes()) {
                                    exprs.addAll(collectSideEffects(index, sideEffectAnalyzer));
                                }
                                exprs.addAll(collectSideEffects(e.getRight(), sideEffectAnalyzer));
                                replacements.add(Pair.create(e, exprs));
                            }
                        } else if (e.getLeft() instanceof ImTupleSelection) {
                            ImVar var = TypesHelper.getTupleVar((ImTupleSelection) e.getLeft());
                            if(var != null && !readVars.contains(var) && !NamePreservation.isPreserved(var)) {
                                List<ImExpr> sideEffects = collectSideEffects(e.getRight(), sideEffectAnalyzer);
                                replacements.add(Pair.create(e, sideEffects));
                            }
                        } else if(e.getLeft() instanceof ImMemberAccess) {
                            ImMemberAccess va = ((ImMemberAccess) e.getLeft());
                            if (!readVars.contains(va.getVar()) && !NamePreservation.isPreserved(va.getVar())) {
                                List<ImExpr> sideEffects = collectSideEffects(e.getRight(), sideEffectAnalyzer);
                                replacements.add(Pair.create(e, sideEffects));
                            }
                        }
                    }
                });

                Replacer replacer = new Replacer();
                for (Pair<ImStmt, List<ImExpr>> pair : replacements) {
                    changes = true;
                    ImExpr r;
                    if (pair.getB().isEmpty()) {
                        r = ImHelper.statementExprVoid(JassIm.ImStmts());
                    } else if (pair.getB().size() == 1) {
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

    private List<ImExpr> collectSideEffects(ImExpr expr, SideEffectAnalyzer analyzer) {
        if (expr == null) {
            return Collections.emptyList();
        }
        if (mayTrapAtRuntime(expr)) {
            return Collections.singletonList(expr);
        }
        if (analyzer.hasObservableSideEffects(expr, func -> func.isNative()
            && UselessFunctionCallsRemover.isFunctionWithoutSideEffect(func.getName()))) {
            return Collections.singletonList(expr);
        }
        return Collections.emptyList();
    }

    private boolean mayTrapAtRuntime(Element elem) {
        return mayTrapAtRuntime(elem, new HashMap<>(), new LinkedHashSet<>());
    }

    private boolean mayTrapAtRuntime(Element elem, Map<ImFunction, Boolean> functionCache, Set<ImFunction> inProgress) {
        if (elem instanceof ImFunctionCall) {
            ImFunction calledFunc = ((ImFunctionCall) elem).getFunc();
            if (functionMayTrapAtRuntime(calledFunc, functionCache, inProgress)) {
                return true;
            }
        } else if (elem instanceof ImMethodCall) {
            ImFunction calledFunc = ((ImMethodCall) elem).getMethod().getImplementation();
            if (calledFunc == null || functionMayTrapAtRuntime(calledFunc, functionCache, inProgress)) {
                return true;
            }
        }

        if (elem instanceof ImOperatorCall) {
            ImOperatorCall opCall = (ImOperatorCall) elem;
            WurstOperator op = opCall.getOp();
            if ((op == WurstOperator.DIV_INT || op == WurstOperator.MOD_INT || op == WurstOperator.JASS_MOD_INT)
                && opCall.getArguments().size() >= 2) {
                ImExpr denominator = opCall.getArguments().get(1);
                // Preserve integer div/mod unless denominator is provably non-zero.
                if (!(denominator instanceof ImIntVal) || ((ImIntVal) denominator).getValI() == 0) {
                    return true;
                }
            }
        }
        for (int i = 0; i < elem.size(); i++) {
            Element child = elem.get(i);
            if (mayTrapAtRuntime(child, functionCache, inProgress)) {
                return true;
            }
        }
        return false;
    }

    private boolean functionMayTrapAtRuntime(ImFunction function, Map<ImFunction, Boolean> functionCache, Set<ImFunction> inProgress) {
        if (function.isNative()) {
            return false;
        }

        Boolean cachedResult = functionCache.get(function);
        if (cachedResult != null) {
            return cachedResult;
        }

        if (!inProgress.add(function)) {
            // Recursive cycles are conservatively treated as potentially trapping.
            return true;
        }

        boolean mayTrap = mayTrapAtRuntime(function.getBody(), functionCache, inProgress);
        inProgress.remove(function);
        functionCache.put(function, mayTrap);
        return mayTrap;
    }
}
