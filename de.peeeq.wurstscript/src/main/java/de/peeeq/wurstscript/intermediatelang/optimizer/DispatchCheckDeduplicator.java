package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.UselessFunctionCallsRemover;
import de.peeeq.wurstscript.translation.imoptimizer.OptimizerPass;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

/**
 * Collapses consecutive identical dispatch safety checks into one.
 *
 * This targets the repetitive pattern emitted by checked dispatch after
 * inlining, where several method calls on the same receiver produce adjacent
 * copies of the same guard.
 */
public class DispatchCheckDeduplicator implements OptimizerPass {

    private int rewrites;
    private final IdentityHashMap<ImFunction, IdentityHashMap<ImVar, Boolean>> mayWriteTypeIdMemo = new IdentityHashMap<>();
    private SideEffectAnalyzer sideEffectAnalyzer;

    @Override
    public int optimize(ImTranslator trans) {
        rewrites = 0;
        mayWriteTypeIdMemo.clear();
        ImProg prog = trans.getImProg();
        sideEffectAnalyzer = new SideEffectAnalyzer(prog);
        for (ImFunction f : prog.getFunctions()) {
            optimizeStmts(f.getBody());
        }
        prog.flatten(trans);
        return rewrites;
    }

    @Override
    public String getName() {
        return "Dispatch Check Dedup";
    }

    private void optimizeStmts(ImStmts stmts) {
        for (ImStmt s : new ArrayList<>(stmts)) {
            if (s instanceof ImIf) {
                ImIf imIf = (ImIf) s;
                optimizeStmts(imIf.getThenBlock());
                optimizeStmts(imIf.getElseBlock());
            } else if (s instanceof ImLoop) {
                optimizeStmts(((ImLoop) s).getBody());
            } else if (s instanceof ImVarargLoop) {
                optimizeStmts(((ImVarargLoop) s).getBody());
            }
        }

        int i = 0;
        while (i < stmts.size()) {
            GuardPattern first = extractDispatchGuard(stmts.get(i));
            if (first == null) {
                i++;
                continue;
            }

            int j = i + 1;
            while (j < stmts.size()) {
                ImStmt s = stmts.get(j);
                GuardPattern next = extractDispatchGuard(s);
                if (next != null) {
                    if (first.sameGuardAs(next)) {
                        stmts.remove(j);
                        rewrites++;
                        continue;
                    }
                    // Different guard: keep scanning only if statement cannot invalidate this guard.
                    if (invalidatesGuard(s, first)) {
                        break;
                    }
                    j++;
                    continue;
                }
                if (invalidatesGuard(s, first)) {
                    break;
                }
                j++;
            }

            i++;
        }
    }

    private boolean invalidatesGuard(ImStmt s, GuardPattern guard) {
        if (mayWriteTypeIdFromElement(s, guard.failedCond.typeIdVar)) {
            return true;
        }
        if (s instanceof ImSet) {
            ImSet set = (ImSet) s;
            ImLExpr left = set.getLeft();
            if (left instanceof ImVarAccess) {
                ImVar v = ((ImVarAccess) left).getVar();
                return v == guard.failedCond.receiverVar || v == guard.failedCond.typeIdVar;
            }
            if (left instanceof ImVarArrayAccess) {
                ImVar v = ((ImVarArrayAccess) left).getVar();
                return v == guard.failedCond.typeIdVar;
            }
            if (left instanceof ImMemberAccess) {
                ImVar v = ((ImMemberAccess) left).getVar();
                return v == guard.failedCond.typeIdVar;
            }
            return false;
        }
        if (s instanceof ImFunctionCall) {
            ImFunction f = ((ImFunctionCall) s).getFunc();
            return mayWriteTypeId(f, guard.failedCond.typeIdVar);
        }
        if (s instanceof ImMethodCall) {
            ImMethod m = ((ImMethodCall) s).getMethod();
            return mayWriteTypeId(m.getImplementation(), guard.failedCond.typeIdVar);
        }
        if (s instanceof ImDealloc || s instanceof ImAlloc) {
            return true;
        }
        if (s instanceof ImIf || s instanceof ImLoop || s instanceof ImVarargLoop
            || s instanceof ImReturn || s instanceof ImExitwhen) {
            return true;
        }
        return false;
    }

    private boolean mayWriteTypeIdFromElement(Element elem, ImVar typeIdVar) {
        if (elem == null) {
            return false;
        }
        if (sideEffectAnalyzer.directlySetVariables(elem).contains(typeIdVar)) {
            return true;
        }
        for (ImFunction called : sideEffectAnalyzer.calledFunctions(elem)) {
            if (mayWriteTypeId(called, typeIdVar)) {
                return true;
            }
        }
        return false;
    }

    private boolean mayWriteTypeId(ImFunction f, ImVar typeIdVar) {
        if (f == null) {
            return true;
        }
        if (f.isNative()) {
            return !UselessFunctionCallsRemover.isFunctionWithoutSideEffect(f.getName());
        }
        if (f.isExtern()) {
            return true;
        }
        IdentityHashMap<ImVar, Boolean> byTypeId = mayWriteTypeIdMemo.computeIfAbsent(f, k -> new IdentityHashMap<>());
        Boolean memo = byTypeId.get(typeIdVar);
        if (memo != null) {
            return memo;
        }
        boolean result = mayWriteTypeIdUsingAnalysis(f, typeIdVar);
        byTypeId.put(typeIdVar, result);
        return result;
    }

    private boolean mayWriteTypeIdUsingAnalysis(ImFunction f, ImVar typeIdVar) {
        Set<ImFunction> reachable = new HashSet<>();
        reachable.add(f);
        reachable.addAll(sideEffectAnalyzer.calledFunctions(f.getBody()));
        for (ImFunction g : reachable) {
            if (g == null) {
                return true;
            }
            if (g.isExtern()) {
                return true;
            }
            if (g.isNative()) {
                if (!UselessFunctionCallsRemover.isFunctionWithoutSideEffect(g.getName())) {
                    return true;
                }
                continue;
            }
            if (sideEffectAnalyzer.directlySetVariables(g.getBody()).contains(typeIdVar)) {
                return true;
            }
        }
        return false;
    }

    private GuardPattern extractDispatchGuard(ImStmt stmt) {
        if (!(stmt instanceof ImIf)) {
            return null;
        }
        ImIf outer = (ImIf) stmt;
        if (!outer.getElseBlock().isEmpty() || outer.getThenBlock().size() != 1) {
            return null;
        }
        GuardCond failed = parseTypeIdZeroCond(outer.getCondition());
        if (failed == null) {
            return null;
        }

        ImStmt innerStmt = outer.getThenBlock().get(0);
        if (!(innerStmt instanceof ImIf)) {
            return null;
        }
        ImIf inner = (ImIf) innerStmt;
        if (inner.getThenBlock().size() != 1 || inner.getElseBlock().size() != 1) {
            return null;
        }
        if (!isReceiverZeroCond(inner.getCondition(), failed.receiverVar)) {
            return null;
        }

        ErrorCall nullErr = parseSingleErrorCall(inner.getThenBlock().get(0));
        ErrorCall invalidErr = parseSingleErrorCall(inner.getElseBlock().get(0));
        if (nullErr == null || invalidErr == null) {
            return null;
        }

        return new GuardPattern(failed, nullErr, invalidErr);
    }

    private static GuardCond parseTypeIdZeroCond(ImExpr expr) {
        if (!(expr instanceof ImOperatorCall)) {
            return null;
        }
        ImOperatorCall op = (ImOperatorCall) expr;
        if (op.getOp() != WurstOperator.EQ || op.getArguments().size() != 2) {
            return null;
        }
        ImExpr a = op.getArguments().get(0);
        ImExpr b = op.getArguments().get(1);
        GuardCond c = parseTypeIdEqZero(a, b);
        if (c != null) {
            return c;
        }
        return parseTypeIdEqZero(b, a);
    }

    private static GuardCond parseTypeIdEqZero(ImExpr left, ImExpr right) {
        if (!(right instanceof ImIntVal) || ((ImIntVal) right).getValI() != 0) {
            return null;
        }
        if (!(left instanceof ImVarArrayAccess)) {
            return null;
        }
        ImVarArrayAccess aa = (ImVarArrayAccess) left;
        if (aa.getIndexes().size() != 1 || !(aa.getIndexes().get(0) instanceof ImVarAccess)) {
            return null;
        }
        ImVar receiver = ((ImVarAccess) aa.getIndexes().get(0)).getVar();
        return new GuardCond(aa.getVar(), receiver);
    }

    private static boolean isReceiverZeroCond(ImExpr expr, ImVar receiver) {
        if (!(expr instanceof ImOperatorCall)) {
            return false;
        }
        ImOperatorCall op = (ImOperatorCall) expr;
        if (op.getOp() != WurstOperator.EQ || op.getArguments().size() != 2) {
            return false;
        }
        return isReceiverEqZero(op.getArguments().get(0), op.getArguments().get(1), receiver)
            || isReceiverEqZero(op.getArguments().get(1), op.getArguments().get(0), receiver);
    }

    private static boolean isReceiverEqZero(ImExpr left, ImExpr right, ImVar receiver) {
        return left instanceof ImVarAccess
            && ((ImVarAccess) left).getVar() == receiver
            && right instanceof ImIntVal
            && ((ImIntVal) right).getValI() == 0;
    }

    private static ErrorCall parseSingleErrorCall(ImStmt stmt) {
        if (!(stmt instanceof ImFunctionCall)) {
            return null;
        }
        ImFunctionCall fc = (ImFunctionCall) stmt;
        if (fc.getArguments().size() != 1 || !(fc.getArguments().get(0) instanceof ImStringVal)) {
            return null;
        }
        return new ErrorCall(fc.getFunc(), ((ImStringVal) fc.getArguments().get(0)).getValS());
    }

    private static final class GuardPattern {
        private final GuardCond failedCond;
        private final ErrorCall nullError;
        private final ErrorCall invalidError;

        private GuardPattern(GuardCond failedCond, ErrorCall nullError, ErrorCall invalidError) {
            this.failedCond = failedCond;
            this.nullError = nullError;
            this.invalidError = invalidError;
        }

        private boolean sameGuardAs(GuardPattern other) {
            return failedCond.typeIdVar == other.failedCond.typeIdVar
                && failedCond.receiverVar == other.failedCond.receiverVar
                && nullError.sameAs(other.nullError)
                && invalidError.sameAs(other.invalidError);
        }
    }

    private static final class GuardCond {
        private final ImVar typeIdVar;
        private final ImVar receiverVar;

        private GuardCond(ImVar typeIdVar, ImVar receiverVar) {
            this.typeIdVar = typeIdVar;
            this.receiverVar = receiverVar;
        }
    }

    private static final class ErrorCall {
        private final ImFunction func;
        private final String message;

        private ErrorCall(ImFunction func, String message) {
            this.func = func;
            this.message = message;
        }

        private boolean sameAs(ErrorCall other) {
            return func == other.func && Objects.equals(message, other.message);
        }
    }
}
