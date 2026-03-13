package de.peeeq.wurstscript.translation.imoptimizer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.*;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.*;

import static de.peeeq.wurstscript.jassIm.JassIm.ImStatementExpr;
import static de.peeeq.wurstscript.jassIm.JassIm.ImStmts;
import static de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum.IS_VARARG;

public class ImInliner {
    private static final String FORCEINLINE = "@inline";
    private static final String NOINLINE = "@noinline";

    private static final double THRESHOLD_MODIFIER_CONSTANT_ARG = 2;

    private static final Set<String> dontInline = Sets.newLinkedHashSet();
    private static final boolean LOG_INLINER = Boolean.getBoolean("wurst.inliner.log");
    private final ImTranslator translator;
    private final ImProg prog;
    private final Set<ImFunction> inlinableFunctions = Sets.newLinkedHashSet();
    private final Map<ImFunction, Integer> callCounts = Maps.newLinkedHashMap();
    private final Map<ImFunction, Integer> funcSizes = Maps.newLinkedHashMap();
    private final Set<ImFunction> done = Sets.newLinkedHashSet();
    private final Map<ImFunction, Boolean> containsFuncRefCache = Maps.newLinkedHashMap();
    private final double inlineTreshold = 50;

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
        collectInlinableFunctions();
        rateInlinableFunctions();
        inlineFunctions();
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
                    (canInline ? "" : " reason=" + skipReason(f, call, called));
                WLogger.info(msg);
                System.out.println(msg);
            }
            if (canInline) {
                if (alreadyInlined.getOrDefault(called, 0) < 5) { // check maximum to ensure termination
                    inlineCall(f, parent, parentI, call);
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
        if (translator.isLuaTarget() && !maxOneReturn(f)) {
            return "lua_multi_return_inline_disabled";
        }
        if (translator.isLuaTarget() && containsFuncRef(f)) {
            return "lua_callback_funcref_barrier";
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
            ImStmt transformed = rewriteStmtForEarlyReturn(s, doneVar, retVar);
            ImExpr notDone = JassIm.ImOperatorCall(de.peeeq.wurstscript.WurstOperator.NOT, JassIm.ImExprs(JassIm.ImVarAccess(doneVar)));
            rewritten.add(JassIm.ImIf(s.attrTrace(), notDone, JassIm.ImStmts(transformed), JassIm.ImStmts()));
        }
        return rewritten;
    }

    private ImStmt rewriteStmtForEarlyReturn(ImStmt s, ImVar doneVar, ImVar retVar) {
        if (s instanceof ImReturn) {
            ImReturn r = (ImReturn) s;
            ImStmts b = JassIm.ImStmts();
            if (retVar != null && r.getReturnValue() instanceof ImExpr) {
                ImExpr rv = (ImExpr) r.getReturnValue();
                rv.setParent(null);
                b.add(JassIm.ImSet(r.getTrace(), JassIm.ImVarAccess(retVar), rv));
            }
            b.add(JassIm.ImSet(r.getTrace(), JassIm.ImVarAccess(doneVar), JassIm.ImBoolVal(true)));
            return ImHelper.statementExprVoid(b);
        } else if (s instanceof ImIf) {
            ImIf imIf = (ImIf) s;
            ImStmts thenBlock = rewriteForEarlyReturns(imIf.getThenBlock().copy(), doneVar, retVar);
            ImStmts elseBlock = rewriteForEarlyReturns(imIf.getElseBlock().copy(), doneVar, retVar);
            return JassIm.ImIf(imIf.getTrace(), imIf.getCondition().copy(), thenBlock, elseBlock);
        } else if (s instanceof ImLoop) {
            ImLoop l = (ImLoop) s;
            ImStmts loopBody = JassIm.ImStmts();
            loopBody.add(JassIm.ImExitwhen(l.getTrace(), JassIm.ImVarAccess(doneVar)));
            loopBody.addAll(rewriteForEarlyReturns(l.getBody().copy(), doneVar, retVar).removeAll());
            return JassIm.ImLoop(l.getTrace(), loopBody);
        } else if (s instanceof ImVarargLoop) {
            ImVarargLoop l = (ImVarargLoop) s;
            ImStmts loopBody = JassIm.ImStmts();
            loopBody.add(JassIm.ImExitwhen(l.getTrace(), JassIm.ImVarAccess(doneVar)));
            loopBody.addAll(rewriteForEarlyReturns(l.getBody().copy(), doneVar, retVar).removeAll());
            return JassIm.ImVarargLoop(l.getTrace(), loopBody, l.getLoopVar());
        }
        // Keep tree ownership valid when rewrapping statements into new blocks.
        return s.copy();
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
        if (size < 20) {
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
        if (translator.isLuaTarget() && !maxOneReturn(f)) {
            // Conservative safety: Lua inliner multi-return rewriting is not yet fully robust
            // across all lowered patterns. Keep call semantics intact for now.
            return false;
        }
        if (translator.isLuaTarget() && containsFuncRef(f)) {
            // Functions that build callback refs are lowered with Lua-specific wrappers/xpcall.
            // Keeping them as standalone calls avoids callback context/vararg scope breakage.
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
                && !isRecursive(f);
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
