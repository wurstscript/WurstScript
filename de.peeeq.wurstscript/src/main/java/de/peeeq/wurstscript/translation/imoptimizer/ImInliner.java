package de.peeeq.wurstscript.translation.imoptimizer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.optimizer.LocalPlayerContextAnalyzer;
import de.peeeq.wurstscript.intermediatelang.optimizer.LocalMerger;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.*;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.*;
import java.util.stream.Collectors;

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
    private static final boolean LOG_INLINER = Boolean.getBoolean("wurst.inliner.log");
    private final ImTranslator translator;
    private final ImProg prog;
    private final Set<ImFunction> inlinableFunctions = Sets.newLinkedHashSet();
    private final Map<ImFunction, Integer> callCounts = Maps.newLinkedHashMap();
    private final Map<ImFunction, Integer> funcSizes = Maps.newLinkedHashMap();
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
     * caller. The late check rebuilds the locality analysis and uses the same allocation classes as
     * the local merger, so it cannot push Lua over the hard local-variable limit.
     */
    public int inlineLuaDivModHelpersWithinLocalBudget() {
        if (!translator.isLuaTarget()) {
            return 0;
        }
        prog.flatten(translator);
        localPlayerContextAnalyzer = new LocalPlayerContextAnalyzer(prog);
        int changed = 0;
        for (ImFunction function : sortedFunctions(ImHelper.calculateFunctionsOfProg(prog))) {
            LuaRegisterBudget budget = new LuaRegisterBudget(function);
            changed += inlineLuaDivModHelpers(function, function, budget);
        }
        return changed;
    }

    private int inlineLuaDivModHelpers(ImFunction function, Element element, LuaRegisterBudget budget) {
        int changed = 0;
        for (int i = 0; i < element.size(); i++) {
            Element child = element.get(i);
            if (child instanceof ImFunctionCall call && isLuaDivModHelper(call.getFunc())) {
                ImFunction callee = call.getFunc();
                if (budget.fits(call, callee)) {
                    budget.recordInline(call, callee);
                    inlineCall(function, element, i, call);
                    changed++;
                    child = element.get(i);
                }
            }
            changed += inlineLuaDivModHelpers(function, child, budget);
        }
        return changed;
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
        if (e instanceof ImFunctionCall) {
            ImFunctionCall call = (ImFunctionCall) e;
            ImFunction called = call.getFunc();
            boolean canInline = f != called && shouldInline(f, call, called);
            if (LOG_INLINER) {
                String msg = "[INLINER] caller=" + f.getName() + " callee=" + called.getName() + " decision=" + (canInline ? "inline" : "keep") +
                    " size=" + getFuncSize(called) + " rating=" + getRating(called) +
                    (translator.isLuaTarget() && inlinableFunctions.contains(called)
                        ? " projectedLuaRegisters=" + getLuaRegisterBudget(f).projectedPressure(call, called)
                        : "") +
                    (canInline ? "" : " reason=" + skipReason(f, call, called));
                WLogger.info(msg);
                System.out.println(msg);
            }
            if (canInline) {
                if (alreadyInlined.getOrDefault(called, 0) < 5) { // check maximum to ensure termination
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

    private String skipReason(ImFunction caller, ImFunctionCall call, ImFunction f) {
        if (f.isNative()) {
            return "native";
        }
        if (call.getCallType() == CallType.EXECUTE) {
            return "execute_call";
        }
        if (translator.isLuaTarget() && containsFuncRef(f)) {
            return "lua_callback_funcref_barrier";
        }
        if (localPlayerContextAnalyzer.functionInliningIsLocalPlayerSensitive(f)) {
            return "local_player_context_barrier";
        }
        if (!inlinableFunctions.contains(f)) {
            return "not_in_inlinable_set";
        }
        if (isRecursive(f)) {
            return "recursive";
        }
        double threshold = inlineTreshold;
        for (ImExpr arg : call.getArguments()) {
            if (arg instanceof ImConst) {
                threshold *= THRESHOLD_MODIFIER_CONSTANT_ARG;
                break;
            }
        }
        double rating = getRating(f);
        if (rating >= threshold) {
            return "rating_too_high(" + rating + ">=" + threshold + ")";
        }
        if (translator.isLuaTarget() && !getLuaRegisterBudget(caller).fits(call, f)) {
            return "lua_register_budget(" + getLuaRegisterBudget(caller).projectedPressure(call, f)
                + ">" + LUA_INLINE_REGISTER_BUDGET + ")";
        }
        return "unknown";
    }

    private void inlineCall(ImFunction f, Element parent, int parentI, ImFunctionCall call) {
        ImFunction called = call.getFunc();
        if (called == f) {
            throw new Error("cannot inline self.");
        }
        List<ImStmt> prefixStmts = Lists.newArrayList();
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
            prefixStmts.add(JassIm.ImSet(arg.attrTrace(), JassIm.ImVarAccess(tempVar), arg));
        }
        // add locals
        for (ImVar l : called.getLocals()) {
            ImVar newL = JassIm.ImVar(l.getTrace(), l.getType(), l.getName(), false);
            f.getLocals().add(newL);
            varSubtitutions.put(l, newL);
        }
        // add body and replace params with tempvars
        List<ImStmt> copiedBody = Lists.newArrayList();
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

        List<ImStmt> stmts = Lists.newArrayList();
        stmts.addAll(prefixStmts);

        ImExpr newExpr = null;
        if (maxOneReturn(called)) {
            // Fast path for existing single-return shape.
            stmts.addAll(copiedBody);
            if (!stmts.isEmpty()) {
                ImStmt lastStmt = stmts.get(stmts.size() - 1);
                if (lastStmt instanceof ImReturn) {
                    ImReturn ret = (ImReturn) lastStmt;
                    stmts.remove(stmts.size() - 1);
                    ImExprOpt valOpt = ret.getReturnValue();
                    if (valOpt instanceof ImExpr) {
                        ImExpr val = (ImExpr) valOpt.copy();
                        ImHelper.replaceVar(val, varSubtitutions);
                        newExpr = ImStatementExpr(ImStmts(stmts), val);
                    }
                }
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

            ImStmts rewritten = rewriteForEarlyReturns(JassIm.ImStmts(copiedBody), doneVar, retVar);
            stmts.addAll(rewritten.removeAll());

            if (retVar != null) {
                // Set fallback return value only on paths where the inlined body did not execute any return.
                // Keeping this write close to the final read avoids dead-store removal creating uninitialized JASS locals.
                ImExpr notDone = JassIm.ImOperatorCall(de.peeeq.wurstscript.WurstOperator.NOT, JassIm.ImExprs(JassIm.ImVarAccess(doneVar)));
                stmts.add(JassIm.ImIf(call.attrTrace(), notDone,
                    JassIm.ImStmts(JassIm.ImSet(call.attrTrace(), JassIm.ImVarAccess(retVar),
                        ImHelper.defaultValueForComplexType(called.getReturnType()))),
                    JassIm.ImStmts()));
                newExpr = ImStatementExpr(ImStmts(stmts), JassIm.ImVarAccess(retVar));
            }
        }
        if (newExpr == null) {
            newExpr = ImHelper.statementExprVoid(ImStmts(stmts));
        }
        parent.set(parentI, newExpr);

    }

    private ImStmts rewriteForEarlyReturns(ImStmts body, ImVar doneVar, ImVar retVar) {
        ImStmts rewritten = JassIm.ImStmts();
        for (ImStmt s : body) {
            ImStmts transformed = rewriteStmtForEarlyReturn(s, doneVar, retVar);
            ImExpr notDone = JassIm.ImOperatorCall(de.peeeq.wurstscript.WurstOperator.NOT, JassIm.ImExprs(JassIm.ImVarAccess(doneVar)));
            rewritten.add(JassIm.ImIf(s.attrTrace(), notDone, transformed, JassIm.ImStmts()));
        }
        return rewritten;
    }

    private ImStmts rewriteStmtForEarlyReturn(ImStmt s, ImVar doneVar, ImVar retVar) {
        if (s instanceof ImReturn) {
            ImReturn r = (ImReturn) s;
            ImStmts b = JassIm.ImStmts();
            if (retVar != null && r.getReturnValue() instanceof ImExpr) {
                ImExpr rv = (ImExpr) r.getReturnValue();
                rv.setParent(null);
                b.add(JassIm.ImSet(r.getTrace(), JassIm.ImVarAccess(retVar), rv));
            }
            b.add(JassIm.ImSet(r.getTrace(), JassIm.ImVarAccess(doneVar), JassIm.ImBoolVal(true)));
            return b;
        } else if (s instanceof ImIf) {
            ImIf imIf = (ImIf) s;
            ImStmts thenBlock = rewriteForEarlyReturns(imIf.getThenBlock().copy(), doneVar, retVar);
            ImStmts elseBlock = rewriteForEarlyReturns(imIf.getElseBlock().copy(), doneVar, retVar);
            return JassIm.ImStmts(JassIm.ImIf(imIf.getTrace(), imIf.getCondition().copy(), thenBlock, elseBlock));
        } else if (s instanceof ImLoop) {
            ImLoop l = (ImLoop) s;
            ImStmts loopBody = JassIm.ImStmts();
            loopBody.add(JassIm.ImExitwhen(l.getTrace(), JassIm.ImVarAccess(doneVar)));
            loopBody.addAll(rewriteForEarlyReturns(l.getBody().copy(), doneVar, retVar).removeAll());
            return JassIm.ImStmts(JassIm.ImLoop(l.getTrace(), loopBody));
        } else if (s instanceof ImVarargLoop) {
            ImVarargLoop l = (ImVarargLoop) s;
            ImStmts loopBody = JassIm.ImStmts();
            loopBody.add(JassIm.ImExitwhen(l.getTrace(), JassIm.ImVarAccess(doneVar)));
            loopBody.addAll(rewriteForEarlyReturns(l.getBody().copy(), doneVar, retVar).removeAll());
            return JassIm.ImStmts(JassIm.ImVarargLoop(l.getTrace(), loopBody,
                JassIm.ImVarargLoopVars(l.getLoopVars().stream()
                    .map(v -> JassIm.ImVarargLoopVar(v.getVar()))
                    .collect(Collectors.toList()))));
        }
        // Keep tree ownership valid when rewrapping statements into new blocks.
        return JassIm.ImStmts(s.copy());
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
            if (flag instanceof FunctionFlagAnnotation) {
                if (((FunctionFlagAnnotation) flag).getAnnotation().equals(FORCEINLINE)) {
                    return 1;
                } else if (((FunctionFlagAnnotation) flag).getAnnotation().equals(NOINLINE)) {
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

    private boolean shouldInline(ImFunction caller, ImFunctionCall call, ImFunction f) {
        if (f.isNative() || call.getCallType() == CallType.EXECUTE) {
            return false;
        }
        if (translator.isLuaTarget() && containsFuncRef(f)) {
            // Functions that build callback refs are lowered with Lua-specific wrappers/xpcall.
            // Keeping them as standalone calls avoids callback context/vararg scope breakage.
            return false;
        }
        if (localPlayerContextAnalyzer.functionInliningIsLocalPlayerSensitive(f)) {
            // Keep the call boundary around GetLocalPlayer-dependent code.
            // Inlining is normally context-preserving, but future local
            // rewrites must not gain an opportunity to move its body.
            return false;
        }
        if (isLuaTypeCastingCompatFunction(f)) {
            // In Lua these compat wrappers are rewritten to object index helpers.
            // If they are inlined beforehand, old TypeCasting bodies leak through.
            return false;
        }

        double threshold = inlineTreshold;
        for (ImExpr arg : call.getArguments()) {
            if (arg instanceof ImConst) {
                threshold *= THRESHOLD_MODIFIER_CONSTANT_ARG;
                break;
            }
        }
//		WLogger.info("Should I inline function " + f.getName() + "?");
//		WLogger.info("	ininable: " + inlinableFunctions.contains(f));
//		WLogger.info("	rating: " + getRating(f));
        return inlinableFunctions.contains(f)
                && getRating(f) < threshold
                && !isRecursive(f)
                && (!translator.isLuaTarget()
                    || getLuaRegisterBudget(caller).fits(call, f));
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

    private LuaPressure pressureOf(Iterable<ImVar> variables) {
        LuaPressure result = new LuaPressure();
        for (ImVar variable : variables) {
            boolean localPlayerDependent = localPlayerContextAnalyzer != null
                && localPlayerContextAnalyzer.isLocalPlayerDependent(variable);
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
            return maxOneReturn(callee)
                ? 0
                : 1 + (callee.getReturnType() instanceof ImVoid
                    ? 0
                    : ImHelper.flattenedJassArity(callee.getReturnType()));
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
        if (e instanceof ImFunctionCall) {
            ImFunctionCall call = (ImFunctionCall) e;
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
