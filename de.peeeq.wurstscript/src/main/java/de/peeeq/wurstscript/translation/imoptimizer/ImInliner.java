package de.peeeq.wurstscript.translation.imoptimizer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.optimizer.LocalPlayerContextAnalyzer;
import de.peeeq.wurstscript.intermediatelang.optimizer.LocalMerger;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.*;
import de.peeeq.wurstscript.translation.imtranslation.purity.Pure;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.*;

import static de.peeeq.wurstscript.jassIm.JassIm.ImStatementExpr;
import static de.peeeq.wurstscript.jassIm.JassIm.ImStmts;
import static de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum.IS_VARARG;

public class ImInliner {
    private static final String FORCEINLINE = "@inline";
    private static final String NOINLINE = "@noinline";

    private static final double THRESHOLD_MODIFIER_CONSTANT_ARG = 2;
    private static final int DEFAULT_ALWAYS_INLINE_SIZE = 20;
    /** Just above the largest measured ordinary Lua leaf: unit_getAbilityLevel at 63 IM nodes. */
    private static final int LUA_ALWAYS_INLINE_SIZE = 64;
    /** Leave room below Lua's hard 200-local limit for backend-introduced locals. */
    private static final int LUA_INLINE_REGISTER_BUDGET = 190;
    /** Rebuild CFG liveness after expansions large enough to invalidate the incremental estimate. */
    private static final int LUA_LIVENESS_REFRESH_INLINE_SIZE = 256;

    private static final Set<String> dontInline = Sets.newLinkedHashSet();
    /** Read when the inliner is made, not once per JVM, so that a test can switch the decision log on. */
    private final boolean logDecisions = Boolean.getBoolean("wurst.inliner.log");
    private final ImTranslator translator;
    private final ImProg prog;
    private final Set<ImFunction> inlinableFunctions = Sets.newLinkedHashSet();
    private final Map<ImFunction, Integer> callCounts = Maps.newLinkedHashMap();
    private final Map<ImFunction, Integer> funcSizes = Maps.newLinkedHashMap();
    /** What the decision log reports for a caller which is not tracked in {@link #funcSizes}; never read by a decision. */
    private final Map<ImFunction, Integer> untrackedCallerSizes = Maps.newIdentityHashMap();
    /** The numbers the decision log gives its callees; never read by a decision. */
    private final Map<ImFunction, Integer> calleeIds = Maps.newIdentityHashMap();
    private final Set<ImFunction> done = Sets.newLinkedHashSet();
    private final Map<ImFunction, Boolean> containsFuncRefCache = Maps.newLinkedHashMap();
    private final Map<ImFunction, LuaRegisterBudget> luaRegisterBudgets = Maps.newLinkedHashMap();
    private final Map<ImFunction, LuaPressure> luaRegisterPressure = Maps.newLinkedHashMap();
    private final double inlineTreshold = 50;
    private LocalPlayerContextAnalyzer localPlayerContextAnalyzer;

    static {
        dontInline.add("SetPlayerAllianceStateAllyBJ");
        dontInline.add("InitBlizzard");
        dontInline.add("error");
    }

    public ImInliner(ImTranslator translator) {
        this.translator = translator;
        this.prog = translator.getImProg();
    }

    public void doInlining() {
        prog.flatten(translator);
        localPlayerContextAnalyzer = new LocalPlayerContextAnalyzer(prog);
        collectInlinableFunctions();
        rateInlinableFunctions();
        inlineFunctions();
    }

    /**
     * Retry the tiny compiler-owned arithmetic wrappers after local allocation has reduced the
     * caller. The late check builds the locality analysis and uses the same allocation classes as
     * the local merger, so it cannot push Lua over the hard local-variable limit. The budget of a
     * function (its liveness and register pressure) and the analysis it consults are made when the first
     * call to one of the wrappers is found: nothing is inlined before that, so what they read is what
     * the start of this pass left, and a program with no such call builds neither.
     */
    public int inlineLuaDivModHelpersWithinLocalBudget() {
        if (!translator.isLuaTarget()) {
            return 0;
        }
        prog.flatten(translator);
        int changed = 0;
        for (ImFunction function : sortedFunctions(ImHelper.calculateFunctionsOfProg(prog))) {
            changed += inlineLuaDivModHelpers(function, function, new LuaRegisterBudget[1]);
        }
        return changed;
    }

    private int inlineLuaDivModHelpers(ImFunction function, Element element, LuaRegisterBudget[] budgetOfFunction) {
        int changed = 0;
        for (int i = 0; i < element.size(); i++) {
            Element child = element.get(i);
            if (child instanceof ImFunctionCall call && isLuaDivModHelper(call.getFunc())) {
                ImFunction callee = call.getFunc();
                if (budgetOfFunction[0] == null) {
                    budgetOfFunction[0] = new LuaRegisterBudget(function);
                }
                LuaRegisterBudget budget = budgetOfFunction[0];
                if (budget.fits(call, callee)) {
                    budget.recordInline(call, callee);
                    inlineCall(function, element, i, call);
                    changed++;
                    child = element.get(i);
                }
            }
            changed += inlineLuaDivModHelpers(function, child, budgetOfFunction);
        }
        return changed;
    }

    /** The locality analysis of the program as the inlining found it; made on first use when no one asked for it before. */
    private LocalPlayerContextAnalyzer localPlayerAnalysis() {
        if (localPlayerContextAnalyzer == null) {
            localPlayerContextAnalyzer = new LocalPlayerContextAnalyzer(prog);
        }
        return localPlayerContextAnalyzer;
    }

    private void inlineFunctions() {
        for (ImFunction f : sortedFunctions(ImHelper.calculateFunctionsOfProg(prog))) {
            inlineFunctions(f);
        }
    }

    private void inlineFunctions(ImFunction f) {
        if (done.contains(f)) {
            return;
        }
        done.add(f);
        // first inline functions called from this function
        for (ImFunction called : sortedFunctions(translator.getCalledFunctions().get(f))) {
            inlineFunctions(called);
        }
        boolean[] changed = new boolean[]{false};
        inlineFunctions(f, f, 0, f.getBody(), changed, Collections.emptyMap());
    }

    private ImFunction inlineFunctions(ImFunction f, Element parent, int parentI, Element e, boolean[] changed, Map<ImFunction, Integer> alreadyInlined) {
        // TODO maybe it would be smarter to first optimize the parameters and then try to optimize the call itself ...
        if (e instanceof ImFunctionCall call) {
            ImFunction called = call.getFunc();
            // a call to itself never runs the chain, as before
            Refusal refusal = f == called ? Refusal.RECURSIVE : refusal(f, call, called);
            boolean canInline = refusal == null;
            if (logDecisions) {
                String msg = "[INLINER] caller=" + f.getName() + " callee=" + called.getName() + " decision=" + (canInline ? "inline" : "keep") +
                    " size=" + getFuncSize(called) + " rating=" + getRating(called) +
                    // Only a budget the inlining has already built: asking for one here would build it from the
                    // body as it is now, earlier than a build without the log does, and a substitution that
                    // follows does not refresh it, so the log could change what is inlined.
                    (translator.isLuaTarget() && inlinableFunctions.contains(called) && luaRegisterBudgets.containsKey(f)
                        ? " projectedLuaRegisters=" + luaRegisterBudgets.get(f).projectedPressure(call, called)
                        : "") +
                    (canInline ? "" : " reason=" + reasonText(refusal, f, call, called)) +
                    " calleeId=" + calleeIdForLog(called) +
                    " calls=" + getCallCount(called) + " args=" + call.getArguments().size() +
                    " constArg=" + hasConstantArgument(call) + " loopDepth=" + loopDepth(call) +
                    " callerSize=" + callerSizeForLog(f);
                WLogger.info(msg);
                System.out.println(msg);
            }
            if (canInline) {
                if (alreadyInlined.getOrDefault(called, 0) < 5) { // check maximum to ensure termination
                    if (translator.isLuaTarget() && canSubstitute(call, called)) {
                        inlineBySubstitution(parent, parentI, call);
                        changed[0] = true;
                        funcSizes.put(f, estimateSize(f));
                        return called;
                    }
                    if (translator.isLuaTarget()) {
                        getLuaRegisterBudget(f).recordInline(call, called);
                    }
                    inlineCall(f, parent, parentI, call);
                    if (translator.isLuaTarget()
                        && getFuncSize(called) >= LUA_LIVENESS_REFRESH_INLINE_SIZE) {
                        getLuaRegisterBudget(f).refresh();
                    }
//					translator.removeCallRelation(f, called); // XXX is it safe to remove this call relation?
                    changed[0] = true;
                    int newSize = estimateSize(f);
                    funcSizes.put(f, newSize);
                    return called;
                }
            }
        }
        for (int i = 0; i < e.size(); i++) {
            Map<ImFunction, Integer> alreadyInlined2 = alreadyInlined;
            while (true) {
                Element child = e.get(i);
                ImFunction inlined = inlineFunctions(f, e, i, child, changed, alreadyInlined2);
                if (inlined == null) {
                    break;
                }
                // otherwise check the same expression again, but remember what we already inlined and how often:
                if (alreadyInlined2 == alreadyInlined) {
                    alreadyInlined2 = new HashMap<>(alreadyInlined);
                }
                alreadyInlined2.put(inlined, 1 + alreadyInlined.getOrDefault(inlined, 0));
            }
        }
        return null;
    }

    private static boolean hasConstantArgument(ImFunctionCall call) {
        for (ImExpr arg : call.getArguments()) {
            if (arg instanceof ImConst) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAnnotation(ImFunction f, String annotation) {
        for (FunctionFlag flag : f.getFlags()) {
            if (flag instanceof FunctionFlagAnnotation functionFlagAnnotation
                && functionFlagAnnotation.getAnnotation().equals(annotation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A number for a function, in the order the log first meets it. Unlike an identity hash code it is
     * never shared by two functions, which is the point of logging it: names are not unique.
     */
    private int calleeIdForLog(ImFunction f) {
        Integer id = calleeIds.get(f);
        if (id == null) {
            id = calleeIds.size();
            calleeIds.put(f, id);
        }
        return id;
    }

    /**
     * The calling function's size for the decision log. Sizes are tracked for the inline candidates
     * only, so a caller which is none (the global initialiser, a vararg function, a package
     * initialiser) is measured when asked, until its first inlining starts tracking it.
     */
    private int callerSizeForLog(ImFunction f) {
        Integer tracked = funcSizes.get(f);
        // measured once, not per decision: a caller with many refused calls would otherwise be rescanned for each
        return tracked != null ? tracked : untrackedCallerSizes.computeIfAbsent(f, this::estimateSize);
    }

    /** How many loops of the calling function enclose the call; only the decision log asks. */
    private static int loopDepth(ImFunctionCall call) {
        int depth = 0;
        for (Element e = call.getParent(); e != null && !(e instanceof ImFunction); e = e.getParent()) {
            if (e instanceof ImLoop || e instanceof ImVarargLoop) {
                depth++;
            }
        }
        return depth;
    }

    private void inlineCall(ImFunction f, Element parent, int parentI, ImFunctionCall call) {
        ImFunction called = call.getFunc();
        if (called == f) {
            throw new Error("cannot inline self.");
        }
        ImStmts stmts = JassIm.ImStmts();
        // save arguments to temp vars:
        List<ImExpr> args = call.getArguments().removeAll();
        Map<ImVar, ImVar> varSubtitutions = Maps.newLinkedHashMap();
        for (int pi = 0; pi < called.getParameters().size(); pi++) {
            ImVar param = called.getParameters().get(pi);
            ImExpr arg = args.get(pi);
            ImVar tempVar = JassIm.ImVar(arg.attrTrace(), param.getType(), param.getName(), false);
            f.getLocals().add(tempVar);
            varSubtitutions.put(param, tempVar);
            // set temp var
            stmts.add(JassIm.ImSet(arg.attrTrace(), JassIm.ImVarAccess(tempVar), arg));
        }
        // add locals
        for (ImVar l : called.getLocals()) {
            ImVar newL = JassIm.ImVar(l.getTrace(), l.getType(), l.getName(), false);
            f.getLocals().add(newL);
            varSubtitutions.put(l, newL);
        }
        // add body and replace params with tempvars
        ImStmts copiedBody = JassIm.ImStmts();
        for (int i = 0; i < called.getBody().size(); i++) {
            ImStmt s = called.getBody().get(i).copy();
            ImHelper.replaceVar(s, varSubtitutions);

            s.accept(new ImStmt.DefaultVisitor() {
                @Override
                public void visit(ImFunctionCall called) {
                    super.visit(called);
                    // we have another call to this function, so increment the count
                    incCallCount(called.getFunc());
                }
            });


            copiedBody.add(s);
        }

        ImExpr newExpr = null;
        if (maxOneReturn(called)) {
            // Fast path for existing single-return shape.
            stmts.addAllMoved(copiedBody);
            if (!stmts.isEmpty()) {
                ImStmt lastStmt = stmts.get(stmts.size() - 1);
                if (lastStmt instanceof ImReturn ret) {
                    stmts.remove(stmts.size() - 1);
                    ImExprOpt valOpt = ret.getReturnValue();
                    if (valOpt instanceof ImExpr val) {
                        ret.setReturnValue(JassIm.ImNoExpr());
                        newExpr = ImStatementExpr(stmts, val);
                    }
                }
            }
        } else if (returnsCanBeStructured(called)) {
            // Guard-clause shape: move what follows an early return into the other branch, so
            // every return ends its path and needs no done flag.
            ImVar retVar = null;
            if (!(called.getReturnType() instanceof ImVoid)) {
                retVar = JassIm.ImVar(call.attrTrace(), called.getReturnType().copy(), "inlineRet", false);
                f.getLocals().add(retVar);
            }
            stmts.addAllMoved(structureReturns(copiedBody, retVar));
            if (retVar != null) {
                newExpr = ImStatementExpr(stmts, JassIm.ImVarAccess(retVar));
            }
        } else {
            // Multi-return path: rewrite returns to done-flag + optional return temp.
            ImVar doneVar = JassIm.ImVar(call.attrTrace(), TypesHelper.imBool(), "inlineDone", false);
            f.getLocals().add(doneVar);
            stmts.add(JassIm.ImSet(call.attrTrace(), JassIm.ImVarAccess(doneVar), JassIm.ImBoolVal(false)));

            ImVar retVar = null;
            if (!(called.getReturnType() instanceof ImVoid)) {
                retVar = JassIm.ImVar(call.attrTrace(), called.getReturnType().copy(), "inlineRet", false);
                f.getLocals().add(retVar);
            }

            stmts.addAllMoved(rewriteForEarlyReturns(copiedBody, doneVar, retVar));

            if (retVar != null) {
                // Set fallback return value only on paths where the inlined body did not execute any return.
                // Keeping this write close to the final read avoids dead-store removal creating uninitialized JASS locals.
                ImExpr notDone = JassIm.ImOperatorCall(de.peeeq.wurstscript.WurstOperator.NOT, JassIm.ImExprs(JassIm.ImVarAccess(doneVar)));
                stmts.add(JassIm.ImIf(call.attrTrace(), notDone,
                    JassIm.ImStmts(JassIm.ImSet(call.attrTrace(), JassIm.ImVarAccess(retVar),
                        ImHelper.defaultValueForComplexType(called.getReturnType()))),
                    JassIm.ImStmts()));
                newExpr = ImStatementExpr(stmts, JassIm.ImVarAccess(retVar));
            }
        }
        if (newExpr == null) {
            newExpr = ImHelper.statementExprVoid(stmts);
        }
        parent.set(parentI, newExpr);

    }

    /**
     * A callee whose whole body is one return of an effect-free expression, or one store of one: a
     * getter or a setter. A call of it is expanded by putting each argument where its parameter is
     * read, which declares no local and leaves nothing for a later pass to clean up.
     */
    private static final class Substitutable {
        private final ImStmt statement;
        private final boolean isStore;
        private final int[] uses;
        /** The reads of the parameters in the order the body evaluates them. */
        private final List<ParameterUse> order = new ArrayList<>();

        private Substitutable(ImStmt statement, boolean isStore, int parameterCount) {
            this.statement = statement;
            this.isStore = isStore;
            this.uses = new int[parameterCount];
        }
    }

    /**
     * One read of a parameter. {@code conditional}: it sits where an {@code and}/{@code or} may not
     * evaluate it. {@code stateReadBefore}: the body has already read a variable, field or array
     * element by then, which an argument that writes could have changed.
     */
    private record ParameterUse(int parameter, boolean conditional, boolean stateReadBefore) {
    }

    private final Map<ImFunction, Substitutable> substitutables = Maps.newLinkedHashMap();

    private Substitutable substitutable(ImFunction f) {
        if (!substitutables.containsKey(f)) {
            substitutables.put(f, analyseSubstitutable(f));
        }
        return substitutables.get(f);
    }

    private Substitutable analyseSubstitutable(ImFunction f) {
        if (!f.getLocals().isEmpty() || f.getBody().size() != 1 || f.getReturnType() instanceof ImTupleType) {
            return null;
        }
        for (ImVar parameter : f.getParameters()) {
            if (parameter.getType() instanceof ImTupleType) {
                return null;
            }
        }
        ImStmt statement = f.getBody().get(0);
        Substitutable result;
        if (statement instanceof ImReturn ret && ret.getReturnValue() instanceof ImExpr value
            && isEffectFree(value)) {
            result = new Substitutable(statement, false, f.getParameters().size());
            collectParameterUses(value, f.getParameters(), result, false, new boolean[1]);
        } else if (statement instanceof ImSet set && isStoreTarget(set.getLeft())
            && isEffectFree(set.getLeft()) && isEffectFree(set.getRight())) {
            result = new Substitutable(statement, true, f.getParameters().size());
            collectParameterUses(set, f.getParameters(), result, false, new boolean[1]);
        } else {
            return null;
        }
        return result;
    }

    private static boolean isStoreTarget(ImLExpr target) {
        return target instanceof ImVarArrayAccess
            || target instanceof ImMemberAccess
            || (target instanceof ImVarAccess access && access.getVar().isGlobal());
    }

    /**
     * Whether evaluating {@code e} changes nothing: constants and reads, with no allocation, statement
     * or call inside other than a call of a native which only reads, and no division that may abort or cast that numbers a handle.
     * Such an expression may be evaluated later, or not at all.
     */
    private boolean isEffectFree(Element e) {
        if (e instanceof ImFunctionCall call) {
            ImFunction target = call.getFunc();
            if (!target.isNative() || localPlayerAnalysis().isLocalPlayerSource(target)
                || !(UselessFunctionCallsRemover.isFunctionWithoutSideEffect(target.getName())
                    || translator.isLuaKeyedMapRead(target))) {
                return false;
            }
        } else if (e instanceof ImExpr) {
            boolean allowed = e instanceof ImConst
                ? !(e instanceof ImFuncRef)
                : e instanceof ImVarAccess || e instanceof ImVarArrayAccess || e instanceof ImMemberAccess
                    || e instanceof ImTupleSelection || (e instanceof ImOperatorCall op && !mayAbort(op))
                    || (e instanceof ImCast cast && !numbersItsOperand(cast)) || e instanceof ImInstanceof
                    || e instanceof ImTypeIdOfObj || e instanceof ImTypeIdOfClass;
            if (!allowed) {
                return false;
            }
        } else if (e instanceof ImStmt) {
            return false;
        }
        for (int i = 0; i < e.size(); i++) {
            if (!isEffectFree(e.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * A cast to int which the Lua backend implements by numbering a handle or string on first sight.
     * Later numbers depend on it, so it must not be dropped as unused. Instance ids and old generics
     * convert without state.
     */
    private static boolean numbersItsOperand(ImCast cast) {
        ImType from = cast.getFromType();
        return TypesHelper.isIntType(cast.getToType())
            && !(from instanceof ImClassType) && !(from instanceof ImAnyType);
    }

    /**
     * An integer division or remainder whose divisor is not a non-zero literal: evaluating it may
     * abort, on purpose in {@code I2S(1 div 0)}, so it must not be dropped as unused.
     */
    private static boolean mayAbort(ImOperatorCall op) {
        de.peeeq.wurstscript.WurstOperator o = op.getOp();
        if ((o == de.peeeq.wurstscript.WurstOperator.DIV_INT || o == de.peeeq.wurstscript.WurstOperator.MOD_INT
            || o == de.peeeq.wurstscript.WurstOperator.JASS_MOD_INT) && op.getArguments().size() >= 2) {
            return !(op.getArguments().get(1) instanceof ImIntVal divisor) || divisor.getValI() == 0;
        }
        return false;
    }

    /** Records the reads of the parameters under {@code e}, children before the node that reads them. */
    private static void collectParameterUses(Element e, ImVars parameters, Substitutable result,
                                             boolean conditional, boolean[] stateRead) {
        if (e instanceof ImOperatorCall op
            && (op.getOp() == de.peeeq.wurstscript.WurstOperator.AND || op.getOp() == de.peeeq.wurstscript.WurstOperator.OR)) {
            for (int i = 0; i < op.getArguments().size(); i++) {
                collectParameterUses(op.getArguments().get(i), parameters, result, conditional || i > 0, stateRead);
            }
            return;
        }
        for (int i = 0; i < e.size(); i++) {
            collectParameterUses(e.get(i), parameters, result, conditional, stateRead);
        }
        if (e instanceof ImVarAccess access) {
            int parameter = parameters.indexOf(access.getVar());
            if (parameter >= 0) {
                result.uses[parameter]++;
                result.order.add(new ParameterUse(parameter, conditional, stateRead[0]));
            } else if (access.getVar().isGlobal()) {
                stateRead[0] = true;
            }
        } else if (e instanceof ImVarArrayAccess || e instanceof ImMemberAccess || e instanceof ImFunctionCall) {
            // a native which only reads still reads state
            stateRead[0] = true;
        }
    }

    /**
     * Whether the call can be expanded by substitution without changing what it computes. An argument
     * is evaluated exactly once, before the body reads anything, so it may move to its parameter only
     * where that stays true: an argument that changes state (a call) must be read once, unconditionally,
     * in argument order, before the body reads any state it could change; an effect-free argument may
     * be dropped when unused, and repeated when it is a constant or a variable.
     */
    private boolean canSubstitute(ImFunctionCall call, ImFunction f) {
        Substitutable shape = substitutable(f);
        ImExprs args = call.getArguments();
        if (shape == null || args.size() != shape.uses.length) {
            return false;
        }
        boolean[] changesState = new boolean[args.size()];
        boolean anyChangesState = false;
        for (int i = 0; i < args.size(); i++) {
            ImExpr arg = args.get(i);
            if (arg.attrTyp() instanceof ImTupleType) {
                return false;
            }
            changesState[i] = !isEffectFree(arg);
            anyChangesState |= changesState[i];
        }
        if (anyChangesState && shape.isStore) {
            return false;
        }
        for (int i = 0; i < args.size(); i++) {
            if (anyChangesState) {
                if (shape.uses[i] != 1) {
                    return false;
                }
            } else if (shape.uses[i] > 1 && !(args.get(i) instanceof ImConst || args.get(i) instanceof ImVarAccess)) {
                return false;
            }
        }
        if (anyChangesState) {
            int previous = -1;
            for (ParameterUse use : shape.order) {
                if (use.conditional() || use.parameter() < previous
                    || (changesState[use.parameter()] && use.stateReadBefore())) {
                    return false;
                }
                previous = use.parameter();
            }
        }
        return true;
    }

    private void inlineBySubstitution(Element parent, int parentI, ImFunctionCall call) {
        ImFunction called = call.getFunc();
        Substitutable shape = substitutable(called);
        List<ImExpr> args = call.getArguments().removeAll();
        ImExpr expanded;
        if (shape.isStore) {
            ImSet store = (ImSet) shape.statement.copy();
            substituteArguments(store, called.getParameters(), args);
            expanded = ImHelper.statementExprVoid(ImStmts(store));
        } else {
            ImExpr value = (ImExpr) ((ImReturn) shape.statement).getReturnValue().copy();
            expanded = (ImExpr) substituteArguments(value, called.getParameters(), args);
        }
        parent.set(parentI, expanded);
    }

    /** Puts each argument in place of the reads of its parameter under {@code root}; returns the new root. */
    private static Element substituteArguments(Element root, ImVars parameters, List<ImExpr> args) {
        List<ImVarAccess> reads = new ArrayList<>();
        root.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImVarAccess access) {
                super.visit(access);
                if (parameters.contains(access.getVar())) {
                    reads.add(access);
                }
            }
        });
        boolean[] placed = new boolean[args.size()];
        Element result = root;
        for (ImVarAccess read : reads) {
            int parameter = parameters.indexOf(read.getVar());
            ImExpr arg = args.get(parameter);
            ImExpr replacement = placed[parameter] ? arg.copy() : arg;
            placed[parameter] = true;
            if (read == root) {
                result = replacement;
            } else {
                read.replaceBy(replacement);
            }
        }
        return result;
    }

    /**
     * Whether every return of {@code f} can end its path, without a done flag: the returns sit in
     * an if whose other branch, or whatever follows it, takes over, and the body returns on every
     * path when it has a value. A return inside a loop, or an if that falls through on both sides
     * with more statements after it, would need that code twice, so those keep the flag.
     */
    private boolean returnsCanBeStructured(ImFunction f) {
        List<ImStmt> body = f.getBody();
        return (f.getReturnType() instanceof ImVoid || alwaysReturns(body)) && endsInTailReturns(body, 0);
    }

    private boolean alwaysReturns(List<ImStmt> stmts) {
        for (ImStmt s : stmts) {
            if (s instanceof ImReturn) {
                return true;
            }
            if (s instanceof ImIf imIf
                && alwaysReturns(imIf.getThenBlock()) && alwaysReturns(imIf.getElseBlock())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Each if that holds a return moves what follows it into one branch, copying that suffix, and
     * nests the rest of the function one level deeper. A long run of guard clauses would copy the
     * suffix once per guard and nest as deep as it is long, so past a small depth the function keeps
     * the flat done-flag form, which is linear.
     */
    private static final int MAX_STRUCTURED_RETURN_DEPTH = 16;

    private boolean endsInTailReturns(List<ImStmt> stmts, int depth) {
        for (int i = 0; i < stmts.size(); i++) {
            ImStmt s = stmts.get(i);
            if (s instanceof ImReturn) {
                return true;
            }
            if (!hasReturn(s)) {
                continue;
            }
            if (!(s instanceof ImIf imIf)) {
                return false;
            }
            if (depth >= MAX_STRUCTURED_RETURN_DEPTH) {
                return false;
            }
            List<ImStmt> rest = stmts.subList(i + 1, stmts.size());
            boolean thenReturns = alwaysReturns(imIf.getThenBlock());
            boolean elseReturns = alwaysReturns(imIf.getElseBlock());
            if (!thenReturns && !elseReturns && !rest.isEmpty()) {
                return false;
            }
            return endsInTailReturns(thenReturns ? imIf.getThenBlock() : concat(imIf.getThenBlock(), rest), depth + 1)
                && endsInTailReturns(elseReturns ? imIf.getElseBlock() : concat(imIf.getElseBlock(), rest), depth + 1);
        }
        return true;
    }

    private static List<ImStmt> concat(List<ImStmt> a, List<ImStmt> b) {
        List<ImStmt> result = new ArrayList<>(a.size() + b.size());
        result.addAll(a);
        result.addAll(b);
        return result;
    }

    /** Consumes the copied body, moving its suffix into the continuing branch without copying its nodes. */
    private ImStmts structureReturns(ImStmts body, ImVar retVar) {
        List<ImStmt> stmts = body.removeAll();
        ImStmts result = JassIm.ImStmts();
        for (int i = 0; i < stmts.size(); i++) {
            ImStmt s = stmts.get(i);
            if (s instanceof ImReturn r) {
                if (retVar != null && r.getReturnValue() instanceof ImExpr value) {
                    r.setReturnValue(JassIm.ImNoExpr());
                    result.add(JassIm.ImSet(r.getTrace(), JassIm.ImVarAccess(retVar), value));
                }
                return result;
            }
            if (!hasReturn(s)) {
                result.add(s);
                continue;
            }
            ImIf imIf = (ImIf) s;
            ImStmts rest = JassIm.ImStmts(stmts.subList(i + 1, stmts.size()));
            boolean thenReturns = alwaysReturns(imIf.getThenBlock());
            boolean elseReturns = alwaysReturns(imIf.getElseBlock());
            // The admission check rules out a nonempty suffix needed by both branches.
            if (!thenReturns) {
                imIf.getThenBlock().addAllMoved(rest);
            } else if (!elseReturns) {
                imIf.getElseBlock().addAllMoved(rest);
            }
            ImStmts thenBlock = structureReturns(imIf.getThenBlock(), retVar);
            ImStmts elseBlock = structureReturns(imIf.getElseBlock(), retVar);
            if (thenBlock.isEmpty() && !elseBlock.isEmpty()) {
                ImExpr condition = imIf.getCondition();
                imIf.setCondition(JassIm.ImBoolVal(false));
                imIf.setCondition(JassIm.ImOperatorCall(de.peeeq.wurstscript.WurstOperator.NOT, JassIm.ImExprs(condition)));
                ImStmts swap = thenBlock;
                thenBlock = elseBlock;
                elseBlock = swap;
            }
            imIf.setThenBlock(thenBlock);
            imIf.setElseBlock(elseBlock);
            result.add(imIf);
            return result;
        }
        return result;
    }

    private ImStmts rewriteForEarlyReturns(ImStmts body, ImVar doneVar, ImVar retVar) {
        ImStmts rewritten = JassIm.ImStmts();
        ImStmts segment = JassIm.ImStmts();
        // Blocks are entered only while live: their parent guards them, and a returning loop
        // checks done at its header. Only a return within this block can require another guard.
        boolean needsGuard = false;
        for (ImStmt s : body.removeAll()) {
            if (!hasReturn(s)) {
                // Preserve whole return-free branches and loops, including their exitwhens.
                segment.add(s);
                continue;
            }
            segment.addAllMoved(rewriteStmtForEarlyReturn(s, doneVar, retVar));
            appendEarlyReturnSegment(rewritten, segment, needsGuard, doneVar);
            segment = JassIm.ImStmts();
            needsGuard = true;
        }
        appendEarlyReturnSegment(rewritten, segment, needsGuard, doneVar);
        return rewritten;
    }

    /** A segment ends at the next possible return, so one entry check covers all its statements. */
    private void appendEarlyReturnSegment(ImStmts rewritten, ImStmts segment, boolean needsGuard, ImVar doneVar) {
        if (segment.isEmpty()) {
            return;
        }
        if (needsGuard) {
            ImExpr notDone = JassIm.ImOperatorCall(de.peeeq.wurstscript.WurstOperator.NOT,
                JassIm.ImExprs(JassIm.ImVarAccess(doneVar)));
            rewritten.add(JassIm.ImIf(segment.get(0).attrTrace(), notDone, segment, JassIm.ImStmts()));
        } else {
            rewritten.addAllMoved(segment);
        }
    }

    private ImStmts rewriteStmtForEarlyReturn(ImStmt s, ImVar doneVar, ImVar retVar) {
        if (s instanceof ImReturn r) {
            ImStmts b = JassIm.ImStmts();
            if (retVar != null && r.getReturnValue() instanceof ImExpr) {
                ImExpr rv = (ImExpr) r.getReturnValue();
                r.setReturnValue(JassIm.ImNoExpr());
                b.add(JassIm.ImSet(r.getTrace(), JassIm.ImVarAccess(retVar), rv));
            }
            b.add(JassIm.ImSet(r.getTrace(), JassIm.ImVarAccess(doneVar), JassIm.ImBoolVal(true)));
            return b;
        } else if (s instanceof ImIf imIf) {
            imIf.setThenBlock(rewriteForEarlyReturns(imIf.getThenBlock(), doneVar, retVar));
            imIf.setElseBlock(rewriteForEarlyReturns(imIf.getElseBlock(), doneVar, retVar));
            return JassIm.ImStmts(imIf);
        } else if (s instanceof ImLoop l) {
            ImStmts loopBody = rewriteForEarlyReturns(l.getBody(), doneVar, retVar);
            loopBody.addFront(JassIm.ImExitwhen(l.getTrace(), JassIm.ImVarAccess(doneVar)));
            l.setBody(loopBody);
            return JassIm.ImStmts(l);
        } else if (s instanceof ImVarargLoop l) {
            ImStmts loopBody = rewriteForEarlyReturns(l.getBody(), doneVar, retVar);
            loopBody.addFront(JassIm.ImExitwhen(l.getTrace(), JassIm.ImVarAccess(doneVar)));
            l.setBody(loopBody);
            return JassIm.ImStmts(l);
        }
        return JassIm.ImStmts(s);
    }

    private void rateInlinableFunctions() {
        List<Map.Entry<ImFunction, ImFunction>> edges = new ArrayList<>(translator.getCalledFunctions().entries());
        edges.sort((a, b) -> {
            int c = functionSortKey(a.getKey()).compareTo(functionSortKey(b.getKey()));
            if (c != 0) return c;
            return functionSortKey(a.getValue()).compareTo(functionSortKey(b.getValue()));
        });
        for (Map.Entry<ImFunction, ImFunction> edge : edges) {
            // For bloat control we need how often a function is used (incoming edges),
            // not how many calls it performs itself (outgoing edges).
            incCallCount(edge.getValue());
        }
        for (ImFunction f : sortedFunctions(inlinableFunctions)) {
            int size = estimateSize(f);
            funcSizes.put(f, size);
        }
    }

    private double getRating(ImFunction f) {
        if (f.isNative() || !inlinableFunctions.contains(f) || dontInline.contains(f.getName())) {
            return Double.MAX_VALUE;
        }

        for (FunctionFlag flag : f.getFlags()) {
            if (flag instanceof FunctionFlagAnnotation functionFlagAnnotation) {
                if (functionFlagAnnotation.getAnnotation().equals(FORCEINLINE)) {
                    return 1;
                } else if (functionFlagAnnotation.getAnnotation().equals(NOINLINE)) {
                    return Double.MAX_VALUE;
                }
            }
        }

        double size = getFuncSize(f);
        int alwaysInlineSize = translator.isLuaTarget()
            ? LUA_ALWAYS_INLINE_SIZE
            : DEFAULT_ALWAYS_INLINE_SIZE;
        if (size < alwaysInlineSize) {
            // always inline small functions
            return 1;
        }

        double callCount = getCallCount(f);
        double rating = size * (callCount - 1);
        return rating;
    }

    private int getFuncSize(ImFunction f) {
        Integer size = funcSizes.get(f);
        if (size != null) {
            return size;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    /** Why a call is not inlined, in the order the checks are made. The log reads the same answer the decision does. */
    private enum Refusal {
        NATIVE("native"),
        EXECUTE_CALL("execute_call"),
        LUA_CALLBACK_FUNCREF_BARRIER("lua_callback_funcref_barrier"),
        LOCAL_PLAYER_CONTEXT_BARRIER("local_player_context_barrier"),
        LUA_TYPECASTING_COMPAT("lua_typecasting_compat"),
        NOT_IN_INLINABLE_SET("not_in_inlinable_set"),
        /** getRating answers Double.MAX_VALUE for these two, which would read as a body that is too big. */
        DONT_INLINE_NAME("dont_inline_name"),
        NOINLINE_ANNOTATION("noinline_annotation"),
        RATING_TOO_HIGH("rating_too_high"),
        RECURSIVE("recursive"),
        LUA_REGISTER_BUDGET("lua_register_budget");

        private final String label;

        Refusal(String label) {
            this.label = label;
        }
    }

    private double inlineThreshold(ImFunctionCall call) {
        return hasConstantArgument(call) ? inlineTreshold * THRESHOLD_MODIFIER_CONSTANT_ARG : inlineTreshold;
    }

    /** The first reason the call must stay a call, or null when it may be inlined. */
    private Refusal refusal(ImFunction caller, ImFunctionCall call, ImFunction f) {
        if (f.isNative()) {
            return Refusal.NATIVE;
        }
        if (call.getCallType() == CallType.EXECUTE) {
            return Refusal.EXECUTE_CALL;
        }
        if (translator.isLuaTarget() && containsFuncRef(f)) {
            // Functions that build callback refs are lowered with Lua-specific wrappers/xpcall.
            // Keeping them as standalone calls avoids callback context/vararg scope breakage.
            return Refusal.LUA_CALLBACK_FUNCREF_BARRIER;
        }
        if (localPlayerAnalysis().functionInliningIsLocalPlayerSensitive(f)) {
            // Keep the call boundary around GetLocalPlayer-dependent code.
            // Inlining is normally context-preserving, but future local
            // rewrites must not gain an opportunity to move its body.
            return Refusal.LOCAL_PLAYER_CONTEXT_BARRIER;
        }
        if (isLuaTypeCastingCompatFunction(f)) {
            // In Lua these compat wrappers are rewritten to object index helpers.
            // If they are inlined beforehand, old TypeCasting bodies leak through.
            return Refusal.LUA_TYPECASTING_COMPAT;
        }
        if (!inlinableFunctions.contains(f)) {
            return Refusal.NOT_IN_INLINABLE_SET;
        }
        if (getRating(f) >= inlineThreshold(call)) {
            if (dontInline.contains(f.getName())) {
                return Refusal.DONT_INLINE_NAME;
            }
            if (hasAnnotation(f, NOINLINE)) {
                return Refusal.NOINLINE_ANNOTATION;
            }
            return Refusal.RATING_TOO_HIGH;
        }
        if (isRecursive(f)) {
            return Refusal.RECURSIVE;
        }
        // a substituted body declares no local, so it cannot cross the register budget
        if (translator.isLuaTarget() && !canSubstitute(call, f) && !getLuaRegisterBudget(caller).fits(call, f)) {
            return Refusal.LUA_REGISTER_BUDGET;
        }
        return null;
    }

    /** The refusal as the decision log writes it, with the numbers the two size checks compared. */
    private String reasonText(Refusal refusal, ImFunction caller, ImFunctionCall call, ImFunction f) {
        switch (refusal) {
            case RATING_TOO_HIGH:
                return refusal.label + "(" + getRating(f) + ">=" + inlineThreshold(call) + ")";
            case LUA_REGISTER_BUDGET:
                // the budget exists: it was just asked whether the call fits
                return refusal.label + "(" + getLuaRegisterBudget(caller).projectedPressure(call, f)
                    + ">" + LUA_INLINE_REGISTER_BUDGET + ")";
            default:
                return refusal.label;
        }
    }

    private boolean isLuaDivModHelper(ImFunction function) {
        return function == translator.luaIntDivFunc
            || function == translator.luaModIntFunc
            || function == translator.luaModRealFunc;
    }

    private static int backendGeneratedLuaLocals(ImFunction function) {
        int[] result = {0};
        function.getBody().accept(new ImStmts.DefaultVisitor() {
            @Override
            public void visit(ImVarargLoop loop) {
                result[0] += 2; // Lua translation introduces __args and __i for each retained loop.
                super.visit(loop);
            }
        });
        return result[0];
    }

    private LuaRegisterBudget getLuaRegisterBudget(ImFunction function) {
        return luaRegisterBudgets.computeIfAbsent(function, LuaRegisterBudget::new);
    }

    private LuaPressure estimateLuaRegisterPressure(ImFunction function) {
        LuaPressure cached = luaRegisterPressure.get(function);
        if (cached != null) {
            return cached;
        }
        Map<ImStmt, io.vavr.collection.Set<ImVar>> liveness = new LocalMerger().calculateLiveness(function);
        LuaPressure pressure = estimateLuaRegisterPressure(function, liveness);
        luaRegisterPressure.put(function, pressure);
        return pressure;
    }

    private LuaPressure estimateLuaRegisterPressure(ImFunction function,
        Map<ImStmt, io.vavr.collection.Set<ImVar>> liveness) {
        LuaPressure maximum = pressureOf(function.getParameters());
        for (Map.Entry<ImStmt, io.vavr.collection.Set<ImVar>> entry : liveness.entrySet()) {
            java.util.Set<ImVar> active = Collections.newSetFromMap(new IdentityHashMap<>());
            active.addAll(entry.getValue().toJavaSet());
            collectReadLocals(entry.getKey(), active);
            maximum.keepMaximums(pressureOf(active));
        }
        return maximum;
    }

    private static void collectReadLocals(ImStmt statement, java.util.Set<ImVar> result) {
        if (statement instanceof ImVarargLoop) {
            // The loop body has separate liveness entries. Counting all of its reads at the
            // header would make sequential temporaries appear simultaneously live.
            return;
        }
        statement.accept(new ImStmt.DefaultVisitor() {
            @Override
            public void visit(ImVarAccess access) {
                super.visit(access);
                if (!access.getVar().isGlobal()) {
                    result.add(access.getVar());
                }
            }
        });
    }

    private static int statementExpressionResultSlots(ImStmt statement) {
        int[] result = {0};
        statement.accept(new ImStmt.DefaultVisitor() {
            @Override
            public void visit(ImStatementExpr expression) {
                super.visit(expression);
                ImType type = expression.getExpr().attrTyp();
                if (!(type instanceof ImVoid)) {
                    result[0] += ImHelper.flattenedJassArity(type);
                }
            }
        });
        return result[0];
    }

    private static int argumentStagingSlots(ImFunctionCall call) {
        int result = 0;
        Element current = call;
        while (current != null) {
            if (current != call && current instanceof ImStmt) {
                break;
            }
            Element parent = current.getParent();
            if (parent instanceof ImExprs expressions) {
                int currentIndex = -1;
                for (int i = 0; i < expressions.size(); i++) {
                    if (expressions.get(i) == current) {
                        currentIndex = i;
                        break;
                    }
                }
                for (int i = 0; i < currentIndex; i++) {
                    ImExpr earlier = expressions.get(i);
                    if (!(earlier.attrPurity() instanceof Pure)) {
                        result += ImHelper.flattenedJassArity(earlier.attrTyp());
                    }
                }
                current = expressions.getParent();
            } else {
                current = parent;
            }
        }
        return result;
    }

    private LuaPressure pressureOf(Iterable<ImVar> variables) {
        LuaPressure result = new LuaPressure();
        for (ImVar variable : variables) {
            boolean localPlayerDependent = localPlayerAnalysis().isLocalPlayerDependent(variable);
            result.add(variable.getType() + "|local=" + localPlayerDependent,
                ImHelper.flattenedJassArity(variable.getType()));
        }
        return result;
    }

    private static final class LuaPressure {
        private final Map<String, Integer> slotsByTypeAndLocality = new LinkedHashMap<>();

        private LuaPressure copy() {
            LuaPressure result = new LuaPressure();
            result.slotsByTypeAndLocality.putAll(slotsByTypeAndLocality);
            return result;
        }

        private void add(String key, int slots) {
            slotsByTypeAndLocality.merge(key, slots, Integer::sum);
        }

        private void addConcurrent(LuaPressure other) {
            for (Map.Entry<String, Integer> entry : other.slotsByTypeAndLocality.entrySet()) {
                add(entry.getKey(), entry.getValue());
            }
        }

        private void keepMaximums(LuaPressure other) {
            for (Map.Entry<String, Integer> entry : other.slotsByTypeAndLocality.entrySet()) {
                slotsByTypeAndLocality.merge(entry.getKey(), entry.getValue(), Math::max);
            }
        }

        private int total() {
            int result = 0;
            for (int slots : slotsByTypeAndLocality.values()) {
                result += slots;
            }
            return result;
        }
    }

    private final class LuaRegisterBudget {
        private final ImFunction function;
        private Map<ImStmt, io.vavr.collection.Set<ImVar>> liveness;
        private LuaPressure peakPressure;
        private int backendLocals;
        private int declarationsWithoutAllocation;

        private LuaRegisterBudget(ImFunction function) {
            this.function = function;
            liveness = new LocalMerger().calculateLiveness(function);
            LuaPressure cachedPressure = luaRegisterPressure.get(function);
            if (cachedPressure == null) {
                cachedPressure = estimateLuaRegisterPressure(function, liveness);
                luaRegisterPressure.put(function, cachedPressure);
            }
            peakPressure = cachedPressure.copy();
            backendLocals = backendGeneratedLuaLocals(function);
            declarationsWithoutAllocation = flattenedDeclarationCount(function.getParameters())
                + flattenedDeclarationCount(function.getLocals())
                + backendLocals;
        }

        private boolean fits(ImFunctionCall call, ImFunction callee) {
            if (!translator.getRunArgs().isLocalOptimizations()) {
                return declarationsWithoutAllocation + declarationsAddedByInline(callee)
                    + argumentStagingSlots(call)
                    <= LUA_INLINE_REGISTER_BUDGET;
            }
            return projectedPressure(call, callee) <= LUA_INLINE_REGISTER_BUDGET
                - backendLocals - backendGeneratedLuaLocals(callee);
        }

        private int declarationsAddedByInline(ImFunction callee) {
            return flattenedDeclarationCount(callee.getParameters())
                + flattenedDeclarationCount(callee.getLocals()) + inlineControlLocals(callee)
                + backendGeneratedLuaLocals(callee);
        }

        private int inlineControlLocals(ImFunction callee) {
            if (maxOneReturn(callee)) {
                return 0;
            }
            int resultSlots = callee.getReturnType() instanceof ImVoid
                ? 0
                : ImHelper.flattenedJassArity(callee.getReturnType());
            // The structured shape has a result variable, but no done flag.
            return returnsCanBeStructured(callee) ? resultSlots : 1 + resultSlots;
        }

        private int flattenedDeclarationCount(ImVars variables) {
            int result = 0;
            for (int i = 0; i < variables.size(); i++) {
                result += ImHelper.flattenedJassArity(variables.get(i).getType());
            }
            return result;
        }

        private void recordInline(ImFunctionCall call, ImFunction callee) {
            peakPressure.keepMaximums(pressureDuringInline(call, callee));
            backendLocals += backendGeneratedLuaLocals(callee);
            declarationsWithoutAllocation += declarationsAddedByInline(callee);
            declarationsWithoutAllocation += argumentStagingSlots(call);
            // Callers are processed after their callees. Publish the expanded pressure so a
            // later caller budgets the body it will actually copy, not the pre-inline callee.
            luaRegisterPressure.put(function, peakPressure.copy());
        }

        private void refresh() {
            liveness = new LocalMerger().calculateLiveness(function);
            peakPressure = estimateLuaRegisterPressure(function, liveness);
            backendLocals = backendGeneratedLuaLocals(function);
            luaRegisterPressure.put(function, peakPressure.copy());
        }

        private int projectedPressure(ImFunctionCall call, ImFunction callee) {
            LuaPressure projected = peakPressure.copy();
            projected.keepMaximums(pressureDuringInline(call, callee));
            return projected.total();
        }

        private LuaPressure pressureDuringInline(ImFunctionCall call, ImFunction callee) {
            LuaPressure concurrent = pressureAt(call);
            concurrent.addConcurrent(estimateLuaRegisterPressure(callee));
            int earlyReturnLocals = inlineControlLocals(callee);
            if (earlyReturnLocals > 0) {
                // These synthetic values cannot be classified by the source locality analysis.
                concurrent.add("inline-control", earlyReturnLocals);
            }
            int stagedArguments = argumentStagingSlots(call);
            if (stagedArguments > 0) {
                concurrent.add("argument-staging", stagedArguments);
            }
            return concurrent;
        }

        private LuaPressure pressureAt(Element element) {
            Element current = element;
            while (current != null) {
                if (current instanceof ImStmt statement) {
                    io.vavr.collection.Set<ImVar> live = liveness.get(statement);
                    if (live != null) {
                        java.util.Set<ImVar> active = Collections.newSetFromMap(new IdentityHashMap<>());
                        active.addAll(live.toJavaSet());
                        collectReadLocals(statement, active);
                        LuaPressure pressure = pressureOf(active);
                        int stagedResults = statementExpressionResultSlots(statement);
                        if (stagedResults > 0) {
                            // Flattening stages each already-inlined sibling result until the
                            // surrounding expression consumes it. The pre-inline liveness map
                            // cannot contain those future backend temporaries yet.
                            pressure.add("statement-expression-results", stagedResults);
                        }
                        return pressure;
                    }
                }
                current = current.getParent();
            }
            // Unknown synthetic shape: remain conservative rather than risking a whole-function spill.
            return peakPressure.copy();
        }
    }

    private boolean isRecursive(ImFunction f) {
        return containsCallTo(f, f.getBody());
    }

    private boolean containsCallTo(ImFunction f, Element e) {
        if (e instanceof ImFunctionCall call) {
            if (call.getFunc() == f) {
                return true;
            }
        }
        // children
        for (int i = 0; i < e.size(); i++) {
            if (containsCallTo(f, e.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsFuncRef(ImFunction f) {
        if (f == null) {
            return false;
        }
        Boolean cached = containsFuncRefCache.get(f);
        if (cached != null) {
            return cached;
        }
        boolean result = containsFuncRef(f.getBody());
        containsFuncRefCache.put(f, result);
        return result;
    }

    private boolean containsFuncRef(Element e) {
        if (e instanceof ImFuncRef) {
            return true;
        }
        for (int i = 0; i < e.size(); i++) {
            if (containsFuncRef(e.get(i))) {
                return true;
            }
        }
        return false;
    }

    private int estimateSize(ImFunction f) {
        int[] r = new int[]{0};
        estimateSize(f.getBody(), r);
        return r[0];
    }

    private void estimateSize(Element e, int[] r) {
        for (int i = 0; i < e.size(); i++) {
            r[0]++;
            estimateSize(e.get(i), r);
        }
    }

    private void incCallCount(ImFunction f) {
        int count = getCallCount(f);
        count++;
        callCounts.put(f, count);
    }

    private int getCallCount(ImFunction f) {
        Integer r = callCounts.get(f);
        if (r == null) {
            return 0;
        }
        return r;
    }

    private void collectInlinableFunctions() {
        for (ImFunction f : sortedFunctions(ImHelper.calculateFunctionsOfProg(prog))) {
            if (isInlineCandidate(f)) {
                inlinableFunctions.add(f);
            }
        }
        // Some call targets can survive in the call graph but not in prog/classes lists.
        for (ImFunction f : sortedFunctions(translator.getCalledFunctions().values())) {
            if (isInlineCandidate(f)) {
                inlinableFunctions.add(f);
            }
        }
    }

    private List<ImFunction> sortedFunctions(Collection<ImFunction> functions) {
        List<ImFunction> r = new ArrayList<>(functions);
        r.sort(Comparator.comparing(this::functionSortKey));
        return r;
    }

    private String functionSortKey(ImFunction f) {
        if (f == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(f.getName()).append("|");
        sb.append(f.getReturnType()).append("|");
        for (ImVar p : f.getParameters()) {
            sb.append(p.getType()).append(",");
        }
        return sb.toString();
    }

    private boolean isInlineCandidate(ImFunction f) {
        if (f.hasFlag(FunctionFlagEnum.IS_COMPILETIME_NATIVE) || f.hasFlag(FunctionFlagEnum.IS_NATIVE)) {
            // do not inline natives
            return false;
        }
        if (isLuaTypeCastingCompatFunction(f)) {
            return false;
        }
        if (f == translator.getGlobalInitFunc()) {
            return false;
        }
        if (f.hasFlag(IS_VARARG)) {
            // do not inline vararg functions
            // this is only relevant for lua, because in JASS they are eliminated before inlining
            return false;
        }
        if (translator.luaInitFunctions.containsKey(f)) {
            // Lua package init functions must stay as ImFunctionCall nodes so StmtTranslation
            // can wrap them in xpcall. Inlining removes the call site and loses the guard.
            return false;
        }
        return true;
    }

    private boolean isLuaTypeCastingCompatFunction(ImFunction f) {
        if (!translator.isLuaTarget() || f == null) {
            return false;
        }
        de.peeeq.wurstscript.ast.Element trace = f.attrTrace();
        if (trace instanceof de.peeeq.wurstscript.ast.FuncDef fd
            && fd.attrNearestPackage() instanceof de.peeeq.wurstscript.ast.WPackage p
            && "TypeCasting".equals(p.getName())) {
            String name = fd.getName();
            return name.endsWith("FromIndex") || name.endsWith("ToIndex");
        }
        return false;
    }

    private boolean maxOneReturn(ImFunction f) {
        return maxOneReturn(f.getBody());
    }

    private boolean maxOneReturn(ImStmts body) {
        if (body.size() == 0) {
            return true;
        }
        for (int i = 0; i < body.size() - 1; i++) {
            if (hasReturn(body.get(i))) {
                return false;
            }
        }
        if (body.get(body.size() - 1) instanceof ImReturn) {
            return true;
        } else return !hasReturn(body.get(body.size() - 1));
    }

    private boolean hasReturn(final ImStmt s) {
        final boolean[] r = new boolean[]{false};
        s.accept(new ImStmt.DefaultVisitor() {
            @Override
            public void visit(ImReturn rs) {
                super.visit(rs);
                r[0] = true;
            }
        });
        return r[0];
    }

}
