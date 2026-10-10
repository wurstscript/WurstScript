package de.peeeq.wurstscript.translation.imoptimizer;

import com.google.common.collect.Lists;
import de.peeeq.wurstio.TimeTaker;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.optimizer.ConstantAndCopyPropagation;
import de.peeeq.wurstscript.intermediatelang.optimizer.LocalPlayerAwareOptimizerPass;
import de.peeeq.wurstscript.intermediatelang.optimizer.LocalPlayerContextAnalyzer;
import de.peeeq.wurstscript.intermediatelang.optimizer.LocalMerger;
import de.peeeq.wurstscript.intermediatelang.optimizer.RedundantFieldStores;
import de.peeeq.wurstscript.intermediatelang.optimizer.SideEffectAnalyzer;
import de.peeeq.wurstscript.intermediatelang.optimizer.SimpleRewrites;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.Flatten;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.validation.NamePreservation;
import org.eclipse.jdt.annotation.Nullable;

import java.util.stream.Collectors;

import java.util.*;

public class ImOptimizer {

    private static final ArrayList<OptimizerPass> localPasses = new ArrayList<>();
    private static final HashMap<String, Integer> totalCount = new HashMap<>();

    static {
        localPasses.add(new SimpleRewrites());
        localPasses.add(new LocalMerger());
        localPasses.add(new ConstantAndCopyPropagation());
        // After the passes which use the local-player analysis: a pass which does not use it discards it, and none
        // after this one needs it again in the same sweep.
        localPasses.add(new RedundantFieldStores());
        localPasses.add(new GlobalsInliner());
        localPasses.add(new SimpleRewrites());
    }

    private final TimeTaker timeTaker;
    ImTranslator trans;

    public ImOptimizer(TimeTaker timeTaker, ImTranslator trans) {
        this.timeTaker = timeTaker;
        this.trans = trans;
    }

    /** Shortens the names. The pipelines remove the garbage right before, and what runs between only calls and sets. */
    public void optimize() {
        assertNoGarbage("optimize follows a removal");
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

        // Each sweep ends with a pass which flattens the program, and the removal keeps it flat.
        int optCount = runLocalOptimizationSweep();
        if (optCount > 0) {
            removeGarbage();
        }

        int cleanupCount = runLocalOptimizationSweep();
        if (cleanupCount > 0) {
            removeGarbage();
        }

        WLogger.info("=== Local optimization passes done! Opts: " + (optCount + cleanupCount) + " ===");
        totalCount.forEach((k, v) -> WLogger.info("== " + k + ":   " + v));
    }

    private int runLocalOptimizationSweep() {
        int optCount = 0;
        LocalPlayerContextAnalyzer localPlayerContextAnalyzer = null;
        for (OptimizerPass pass : localPasses) {
            int count;
            if (pass instanceof LocalPlayerAwareOptimizerPass localPlayerAwarePass) {
                if (localPlayerContextAnalyzer == null) {
                    localPlayerContextAnalyzer =
                        new LocalPlayerContextAnalyzer(trans.getImProg());
                }
                LocalPlayerContextAnalyzer analyzer = localPlayerContextAnalyzer;
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


    /**
     * Removes what nothing reads. A flat program stays flat: what an assignment to an unread variable leaves behind
     * is put in its place in the statement list it was in, so the flatten which used to follow each removal has
     * nothing to do. (In a unit test this is checked.)
     *
     * @return whether anything was removed
     */
    public boolean removeGarbage() {
        boolean wasFlat = trans.isUnitTestMode() && trans.isFlat();
        boolean changed = removeGarbageWithFacts();
        if (wasFlat && !trans.isFlat()) {
            throw new AssertionError("The garbage removal made a flat program not flat");
        }
        return changed;
    }

    /**
     * For a place where a garbage removal would find nothing, because nothing which can make garbage ran since the
     * last one. The removal is not run: in a unit test it is, and a program for which it removes something is a bug
     * in this claim, which fails the test.
     */
    public void assertNoGarbage(String why) {
        if (trans.isUnitTestMode() && removeGarbage()) {
            throw new AssertionError("The garbage removal found garbage where " + why);
        }
    }

    private boolean removeGarbageWithFacts() {
        // The rounds change one function at a time and say which, so the analysis keeps what it read of the others.
        trans.rememberFunctionFacts();
        try {
            return removeGarbageInRounds();
        } finally {
            trans.forgetFunctionFacts();
        }
    }

    /**
     * How many things the first round could remove: the global variables, fields, functions, locals and assignments
     * which the program had. Every round which changes something removes one of them (an assignment which is replaced
     * leaves statements which are not assignments to an unread variable which go: the variables a flatten makes are
     * read, except the local of a division which may stop the thread, and an assignment of that to a local stays), so
     * the rounds cannot be more than that. A program which needs more never settles, which is a bug, and is reported.
     * The real programs measured take up to ten rounds (castle fight: nine). -1 before the first round counted them.
     */
    private long removableThings = -1;

    /** The functions the rounds look at: the program's, then those of the classes. Fixed while no function is removed. */
    private List<ImFunction> roundFunctions = List.of();
    /** The functions in which the last round replaced assignments, which is all the program has lost since; null before the first round. */
    private @Nullable List<ImFunction> changedInLastRound;

    /**
     * Removes what nothing reads, until there is nothing left to remove.
     * <p>
     * The first round analyses the whole program. A later round starts from what the round before it replaced: the
     * assignments it dropped are all the program lost, so the functions it changed are looked at again and the
     * variables they read no more are the only ones which can have become unread (a chain of assignments to unread
     * variables takes a round for each link). It analyses the program again only when a changed function lost a call.
     * That is the result of analysing the whole program every round, without the walk over the program: the removal
     * of a program like castle fight takes 6 to 9 rounds, most of which removed a handful of assignments.
     * <p>
     * It runs until nothing is left: a chain of assignments to unread variables is as long as it is (a small program
     * of tuples has one of more than ten), and a removal which stopped early left the rest for a second removal. A
     * round which changes something removes an assignment, a variable or a function, so the rounds end, and there are
     * no more of them than the program had things to remove ({@link #removableThings}). A chain takes a round for each
     * link, each of which looks at the functions which changed and no more, so the cost of a chain grows with the
     * square of its length (the programs measured need at most ten rounds; see BACKLOG.md for the numbers of long chains
     * and what would make them linear).
     */
    private boolean removeGarbageInRounds() {
        boolean changes = true;
        boolean anyChanges = false;
        int rounds = 0;
        // whether the analysis of the translator is that of the program as it is now
        boolean currentAnalysis = false;
        changedInLastRound = null;
        removableThings = -1;
        while (changes) {
            // the first round counts what there is to remove (garbageRound), which the rounds after it cannot exceed
            if (++rounds > 1 && removableThings >= 0 && rounds > removableThings + 1) {
                throw new IllegalStateException("The garbage removal does not end: it still changes something in round "
                    + rounds + ", but the program had only " + removableThings + " things to remove");
            }
            List<ImVar> newlyUnread = changedInLastRound == null ? null : trans.refreshReadVariables(changedInLastRound);
            boolean incremental = newlyUnread != null;
            if (!incremental) {
                trans.calculateCallRelationsAndReadVariables();
            }
            changes = garbageRound(incremental, newlyUnread);
            currentAnalysis = !incremental && !changes;
            anyChanges |= changes;
        }
        if (!currentAnalysis) {
            // The call relation and the sets the translator keeps are the ones of an earlier round.
            Set<ImVar> maintainedReads = null;
            Set<ImFunction> maintainedUsed = null;
            if (trans.isUnitTestMode()) {
                maintainedReads = new HashSet<>(trans.getReadVariables());
                maintainedUsed = new HashSet<>(trans.getUsedFunctions());
            }
            trans.calculateCallRelationsAndReadVariables();
            if (maintainedReads != null) {
                // The rounds after the first only look at what the round before changed: what they leave is
                // what a round over the whole program leaves.
                if (!maintainedReads.equals(trans.getReadVariables()) || !maintainedUsed.equals(trans.getUsedFunctions())) {
                    throw new AssertionError("The rounds which looked at the changed functions only ended with other read variables"
                        + " or used functions than an analysis of the program");
                }
                if (garbageRound(false, null)) {
                    throw new AssertionError("The rounds which looked at the changed functions only left garbage which a"
                        + " round over the whole program removes");
                }
            }
        }
        return anyChanges;
    }


    /**
     * One round: removes the variables and functions which nothing reads or reaches, and replaces the assignments to
     * unread variables by what they do besides assigning.
     *
     * @param incremental     the translator's read variables were brought in line with the functions which the round
     *                        before changed, rather than calculated again: nothing is unreachable which was not, and
     *                        only the variables in newlyUnread are unread which were not
     * @param newlyUnread     the variables which nothing reads since the round before (incremental only)
     * @return whether anything was removed or replaced
     */
    private boolean garbageRound(boolean incremental, @Nullable List<ImVar> newlyUnread) {
        ImProg prog = trans.imProg();
        final Set<ImVar> readVars = trans.getReadVariables();
        final Set<ImFunction> usedFuncs = trans.getUsedFunctions();
        SideEffectAnalyzer sideEffectAnalyzer = new SideEffectAnalyzer(prog);
        boolean changes = false;
        boolean variablesLost = !incremental || !newlyUnread.isEmpty();

        // what the first round can remove, for the bound on the rounds (removableThings)
        boolean countThings = removableThings < 0;
        long things = 0;
        if (countThings) {
            things += prog.getGlobals().size() + prog.getFunctions().size();
            for (ImClass c : prog.getClasses()) {
                things += c.getFields().size() + c.getFunctions().size();
            }
        }

        if (variablesLost) {
            // keep only used variables
            changes |= prog.getGlobals().retainAll(readVars);
        }

        if (!incremental) {
            // keep only functions reachable from main and config
            changes |= prog.getFunctions().retainAll(usedFuncs);

            // also consider class functions
            List<ImFunction> allFunctions = new ArrayList<>(prog.getFunctions());
            for (ImClass c : prog.getClasses()) {
                changes |= c.getFunctions().retainAll(usedFuncs);
                allFunctions.addAll(c.getFunctions());
            }
            roundFunctions = allFunctions;
        }

        if (variablesLost) {
            for (ImClass c : prog.getClasses()) {
                // A field of a specialised class is a copy which nothing refers to, an access made
                // before specialisation still naming the original's variable. It is live exactly
                // when the field it was copied from is; dropping it leaves an instance allocated
                // with no fields while the emitted code goes on reading them.
                changes |= c.getFields().retainAll(c.getFields().stream()
                    .filter(field -> readVars.contains(field)
                        || readVars.contains(trans.canonical(field)))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
            }
        }

        // Functions which can have an assignment to replace: all of them in the first round, and in a later one the
        // changed ones, and every other function only when a variable became unread.
        Set<ImFunction> changedFunctions = null;
        if (incremental) {
            changedFunctions = Collections.newSetFromMap(new IdentityHashMap<>());
            changedFunctions.addAll(changedInLastRound);
        }
        boolean scanAllFunctions = !incremental || !newlyUnread.isEmpty();
        List<ImFunction> replacedIn = new ArrayList<>();
        for (ImFunction f : roundFunctions) {
            boolean changedBefore = changedFunctions != null && changedFunctions.contains(f);
            if (!scanAllFunctions && !changedBefore) {
                continue;
            }
            // remove set statements to unread variables
            final List<Pair<ImStmt, List<ImExpr>>> replacements = Lists.newArrayList();
            // the unread locals of the assignments which stay because they evaluate a division which may stop the thread
            final Set<ImVar> keptLocals = new LinkedHashSet<>();
            if (countThings) {
                things += trans.setStatementsOf(f).size() + f.getLocals().size();
            }
            for (ImSet e : trans.setStatementsOf(f)) {
                if (e.getLeft() instanceof ImVarAccess) {
                    ImVarAccess va = (ImVarAccess) e.getLeft();
                    if (!readVars.contains(va.getVar()) && !NamePreservation.isPreserved(va.getVar())) {
                        if (va.getVar().getParent() == f.getLocals() && Flatten.mayStopTheThread(e.getRight())) {
                            // What it does besides assigning is the division, and the statement which evaluates
                            // that is an assignment to a local (a flatten makes one): this one.
                            keptLocals.add(va.getVar());
                        } else {
                            List<ImExpr> sideEffects = collectSideEffects(e.getRight(), sideEffectAnalyzer);
                            replacements.add(Pair.create(e, sideEffects));
                        }
                    }
                } else if (e.getLeft() instanceof ImVarArrayAccess) {
                    ImVarArrayAccess va = (ImVarArrayAccess) e.getLeft();
                    if (!readVars.contains(va.getVar()) && !NamePreservation.isPreserved(va.getVar())) {
                        replacements.add(Pair.create(e, effectsOfAssignment(e, sideEffectAnalyzer)));
                    }
                } else if (e.getLeft() instanceof ImTupleSelection) {
                    ImVar var = TypesHelper.getTupleVar((ImTupleSelection) e.getLeft());
                    if(var != null && !readVars.contains(var) && !NamePreservation.isPreserved(var)) {
                        replacements.add(Pair.create(e, effectsOfAssignment(e, sideEffectAnalyzer)));
                    }
                } else if(e.getLeft() instanceof ImMemberAccess) {
                    ImMemberAccess va = ((ImMemberAccess) e.getLeft());
                    if (!readVars.contains(va.getVar()) && !NamePreservation.isPreserved(va.getVar())) {
                        replacements.add(Pair.create(e, effectsOfAssignment(e, sideEffectAnalyzer)));
                    }
                }
            }

            // keep only read local variables. A local is read by the function it belongs to only, so after the first
            // round it can only be unread in a function which lost code. This is before the replacements, which can
            // make locals of their own (a flatten saves the arguments in front of one which has statements): the
            // variables which were read when the round was analysed are the ones to keep, those are not among them.
            if (!incremental || changedBefore) {
                changes |= keptLocals.isEmpty()
                    ? f.getLocals().retainAll(readVars)
                    : f.getLocals().removeIf(v -> !readVars.contains(v) && !keptLocals.contains(v));
            }

            if (!replacements.isEmpty()) {
                changes = true;
                replaceByEffects(f, replacements);
                trans.functionChanged(f);
                replacedIn.add(f);
            }
        }
        changedInLastRound = replacedIn;
        if (countThings) {
            removableThings = things;
        }
        return changes;
    }

    /**
     * Puts what each of the assignments does besides assigning in its place, in the statement list it is in, as
     * statements. A function which was flat stays flat: nothing is wrapped in a statement expression for a later
     * flatten to unwrap, and the effects which are not statements are made into some as a flatten makes them.
     */
    private void replaceByEffects(ImFunction f, List<Pair<ImStmt, List<ImExpr>>> replacements) {
        // one pass over each statement list, however many of its assignments go (the initialiser of a large package
        // is a list of thousands)
        Map<ImStmts, Map<ImStmt, List<ImStmt>>> byList = new IdentityHashMap<>();
        for (Pair<ImStmt, List<ImExpr>> pair : replacements) {
            if (!(pair.getA().getParent() instanceof ImStmts list)) {
                throw new IllegalStateException("An assignment which is not in a statement list: " + pair.getA());
            }
            // The effects are parts of the assignment, which goes, and they are statements now: an expression which is
            // one (`a and f()`, a call with a statement expression for an argument, `10 div d`) becomes what a flatten
            // makes of it, the statements which the backends translate (an if, the call with its arguments in
            // variables, the assignment of the division to a local).
            List<ImStmt> statements = new ArrayList<>(pair.getB().size());
            for (ImExpr effect : pair.getB()) {
                effect.setParent(null);
                effect.flatten(trans, f).intoStatements(statements, trans, f);
            }
            byList.computeIfAbsent(list, l -> new IdentityHashMap<>()).put(pair.getA(), statements);
        }
        for (Map.Entry<ImStmts, Map<ImStmt, List<ImStmt>>> entry : byList.entrySet()) {
            entry.getKey().replaceEach(entry.getValue());
        }
    }

    /**
     * What an assignment to an array element, a field or a tuple component does besides assigning: the effects of
     * evaluating its target (the receiver, then the indexes, as for an element of an array), then those of the value.
     */
    private List<ImExpr> effectsOfAssignment(ImSet assignment, SideEffectAnalyzer analyzer) {
        List<ImExpr> effects = new ArrayList<>();
        collectTargetEffects(assignment.getLeft(), analyzer, effects);
        effects.addAll(collectSideEffects(assignment.getRight(), analyzer));
        return effects;
    }

    private void collectTargetEffects(ImExpr target, SideEffectAnalyzer analyzer, List<ImExpr> effects) {
        if (target instanceof ImTupleSelection selection) {
            collectTargetEffects(selection.getTupleExpr(), analyzer, effects);
        } else if (target instanceof ImMemberAccess access) {
            effects.addAll(collectSideEffects(access.getReceiver(), analyzer));
            for (ImExpr index : access.getIndexes()) {
                effects.addAll(collectSideEffects(index, analyzer));
            }
        } else if (target instanceof ImVarArrayAccess access) {
            for (ImExpr index : access.getIndexes()) {
                effects.addAll(collectSideEffects(index, analyzer));
            }
        } else if (!(target instanceof ImVarAccess)) {
            effects.addAll(collectSideEffects(target, analyzer));
        }
    }

    private List<ImExpr> collectSideEffects(ImExpr expr, SideEffectAnalyzer analyzer) {
        if (expr == null) {
            return Collections.emptyList();
        }
        if (expr instanceof ImFunctionCall call && trans.isTrapFreeLuaIntrinsicCall(call)) {
            // Prints as a pure operator; only its operands can still matter.
            List<ImExpr> operandEffects = new ArrayList<>();
            for (ImExpr argument : call.getArguments()) {
                operandEffects.addAll(collectSideEffects(argument, analyzer));
            }
            return operandEffects;
        }
        if (mayTrapAtRuntime(expr)) {
            return Collections.singletonList(expr);
        }
        if (analyzer.hasObservableSideEffects(expr, func -> func.isNative()
            && (SideEffectFreeNatives.isFunctionWithoutSideEffect(func.getName())
                || trans.isLuaKeyedMapRead(func)))) {
            return Collections.singletonList(expr);
        }
        return Collections.emptyList();
    }

    private boolean mayTrapAtRuntime(Element elem) {
        return mayTrapAtRuntime(elem, new HashMap<>(), new LinkedHashSet<>());
    }

    private boolean mayTrapAtRuntime(Element elem, Map<ImFunction, Boolean> functionCache, Set<ImFunction> inProgress) {
        if (elem instanceof ImFunctionCall imFunctionCall) {
            ImFunction calledFunc = imFunctionCall.getFunc();
            if (functionMayTrapAtRuntime(calledFunc, functionCache, inProgress)) {
                return true;
            }
        } else if (elem instanceof ImMethodCall imMethodCall) {
            ImFunction calledFunc = imMethodCall.getMethod().getImplementation();
            if (calledFunc == null || functionMayTrapAtRuntime(calledFunc, functionCache, inProgress)) {
                return true;
            }
        }

        // Preserve integer div/mod unless the divisor is provably non-zero.
        if (elem instanceof ImExpr expr && Flatten.mayStopTheThread(expr)) {
            return true;
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
