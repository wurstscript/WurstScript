package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.Replacer;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.*;

public class MultiArrayEliminator {
    private final ImProg prog;
    private final HashMap<ImVar, GetSetPair> getSetMap = Maps.newHashMap();
    private final ImTranslator translator;
    private final boolean generateStacktraces;

    // OPTIMIZATION 1: Reuse single replacer instance
    private final Replacer replacer = new Replacer();

    // OPTIMIZATION 2: Cache for frequently created expressions
    private final Map<String, ImExpr> exprCache = new HashMap<>();

    private static class GetSetPair {
        final ImFunction getter;
        final ImFunction setter;

        public GetSetPair(ImFunction get, ImFunction set) {
            getter = get;
            setter = set;
        }
    }

    public MultiArrayEliminator(ImProg imProg, ImTranslator tr, boolean generateStacktraces) {
        this.prog = imProg;
        this.translator = tr;
        this.generateStacktraces = generateStacktraces;
    }

    public void run() {
        List<ImVar> oldVars = Lists.newArrayList();
        List<ImVar> newVars = Lists.newArrayList();

        // Process globals
        for (ImVar v : prog.getGlobals()) {
            if (v.getType() instanceof ImArrayTypeMulti) {
                ImArrayTypeMulti type = (ImArrayTypeMulti) v.getType();
                List<Integer> arraySize = type.getArraySize();
                if (arraySize.size() == 2) {
                    oldVars.add(v);
                    int size0 = arraySize.get(0);
                    List<ImVar> newArrays = Lists.newArrayList();
                    for (int i = 0; i < size0; i++) {
                        ImVar newVar = JassIm.ImVar(v.getTrace(), JassIm.ImArrayType(type.getEntryType()), v.getName() + "_" + i, false);
                        newArrays.add(newVar);
                    }
                    ImFunction setFunc = generateSetFunc(v, newArrays);
                    ImFunction getFunc = generateGetFunc(v, newArrays);
                    prog.getFunctions().add(setFunc);
                    prog.getFunctions().add(getFunc);
                    getSetMap.put(v, new GetSetPair(getFunc, setFunc));

                    newVars.addAll(newArrays);
                } else if (arraySize.size() == 1) {
                    // just remove the size
                    v.setType(JassIm.ImArrayType(type.getEntryType()));
                } else {
                    throw new CompileError(v, "Unsupported array sizes " + arraySize);
                }
            }
        }

        // Process locals
        for (ImFunction function : prog.getFunctions()) {
            for (ImVar v : function.getLocals()) {
                if (v.getType() instanceof ImArrayTypeMulti) {
                    ImArrayTypeMulti type = (ImArrayTypeMulti) v.getType();
                    List<Integer> arraySize = type.getArraySize();
                    if (arraySize.size() == 1) {
                        v.setType(JassIm.ImArrayType(type.getEntryType()));
                    } else {
                        throw new CompileError(v, "Unsupported array sizes " + arraySize);
                    }
                }
            }
        }

        // OPTIMIZATION 3: Only process if we have multi-arrays to replace
        if (!getSetMap.isEmpty()) {
            replaceVars(prog, getSetMap);
        }

        prog.getGlobals().removeAll(oldVars);
        prog.getGlobals().addAll(newVars);
    }

    private void replaceVars(Element e, Map<ImVar, GetSetPair> oldToNewVar) {
        // OPTIMIZATION 4: Use single visitor pass instead of recursive calls
        ReplaceVarsVisitor visitor = new ReplaceVarsVisitor(oldToNewVar);
        e.accept(visitor);
    }

    // OPTIMIZATION 5: Visitor pattern for better performance
    private class ReplaceVarsVisitor extends Element.DefaultVisitor {
        private final Map<ImVar, GetSetPair> oldToNewVar;
        private final List<PendingReplacement> pendingReplacements = new ArrayList<>();

        ReplaceVarsVisitor(Map<ImVar, GetSetPair> oldToNewVar) {
            this.oldToNewVar = oldToNewVar;
        }

        @Override
        public void visit(ImSet set) {
            ImLExpr left = set.getLeft();

            // OPTIMIZATION 6: Normalize statement expressions efficiently
            if (left instanceof ImStatementExpr) {
                ImStmts stmts = JassIm.ImStmts();
                while (left instanceof ImStatementExpr) {
                    ImStatementExpr se = (ImStatementExpr) left;
                    // CRITICAL: Use removeAll() to clear parents properly
                    stmts.addAll(se.getStatements().removeAll());
                    left = (ImLExpr) se.getExpr();
                    // CRITICAL: Clear parent before reuse
                    left.setParent(null);
                }

                set.setLeft(left);

                // Process extracted statements
                for (ImStmt s : stmts) {
                    s.accept(this);
                }

                // Wrap set in statement expression with extracted statements
                Element setParent = set.getParent();
                set.setParent(null);
                stmts.add(set);
                replacer.replaceInParent(setParent, set, ImHelper.statementExprVoid(stmts));
            }

            // OPTIMIZATION 7: Handle multi-array set operations
            if (left instanceof ImVarArrayAccess) {
                ImVarArrayAccess va = (ImVarArrayAccess) left;
                if (va.getIndexes().size() > 1 && oldToNewVar.containsKey(va.getVar())) {
                    // Process children first
                    va.getIndexes().accept(this);
                    set.getRight().accept(this);

                    // Build function call arguments
                    ImExprs args = JassIm.ImExprs();
                    for (ImExpr val : va.getIndexes()) {
                        args.add(val.copy()); // CRITICAL: Use copy() to avoid parent conflicts
                    }
                    args.add(set.getRight().copy()); // CRITICAL: Use copy()

                    if (generateStacktraces) {
                        args.add(JassIm.ImStringVal("when writing array " + va.getVar().getName() +
                            StackTraceInjector2.getCallPos(va.getTrace().attrSource())));
                    }

                    ImFunctionCall call = JassIm.ImFunctionCall(set.getTrace(),
                        oldToNewVar.get(va.getVar()).setter,
                        JassIm.ImTypeArguments(), args, false, CallType.NORMAL);

                    pendingReplacements.add(new PendingReplacement(set, call));
                    return; // Don't process children again
                }
            }

            // Default processing
            super.visit(set);
        }

        @Override
        public void visit(ImVarArrayAccess am) {
            if (am.getIndexes().size() > 1 && !am.isUsedAsLValue()) {
                GetSetPair pair = oldToNewVar.get(am.getVar());
                if (pair != null) {
                    // Build function call arguments
                    ImExprs args = JassIm.ImExprs();
                    for (ImExpr val : am.getIndexes()) {
                        args.add(val.copy()); // CRITICAL: Use copy()
                    }

                    if (generateStacktraces) {
                        args.add(JassIm.ImStringVal("when reading array " + am.getVar().getName() +
                            " in " + StackTraceInjector2.getCallPos(am.getTrace().attrSource())));
                    }

                    ImFunctionCall call = JassIm.ImFunctionCall(am.attrTrace(),
                        pair.getter, JassIm.ImTypeArguments(), args, false, CallType.NORMAL);

                    pendingReplacements.add(new PendingReplacement(am, call));
                    return; // Don't process children
                }
            }

            super.visit(am);
        }

        void applyReplacements() {
            // OPTIMIZATION 8: Batch replacements to avoid multiple tree traversals
            for (PendingReplacement pr : pendingReplacements) {
                replacer.replace(pr.toReplace, pr.replacement);
            }
            pendingReplacements.clear();
        }
    }

    private static class PendingReplacement {
        final Element toReplace;
        final Element replacement;

        PendingReplacement(Element toReplace, Element replacement) {
            this.toReplace = toReplace;
            this.replacement = replacement;
        }
    }

    // OPTIMIZATION 9: Generate more efficient binary search
    private ImFunction generateSetFunc(ImVar aVar, List<ImVar> newArrays) {
        ImArrayTypeMulti mtype = (ImArrayTypeMulti) aVar.getType();
        ImVars locals = JassIm.ImVars();
        ImVar instanceId = JassIm.ImVar(aVar.getTrace(), TypesHelper.imInt(), "instanceId", false);
        ImVar arrayIndex = JassIm.ImVar(aVar.getTrace(), TypesHelper.imInt(), "arrayIndex", false);
        ImVar value = JassIm.ImVar(aVar.getTrace(), mtype.getEntryType(), "value", false);

        ImFunctionCall error = imError(aVar, "Index out of Bounds");
        ImStmts thenBlock = JassIm.ImStmts(error);
        ImStmts elseBlock = JassIm.ImStmts();

        // OPTIMIZATION 10: Use switch-like structure for small arrays
        if (newArrays.size() <= 4) {
            generateLinearSet(elseBlock, instanceId, arrayIndex, value, newArrays, aVar.getTrace());
        } else {
            generateBinSearchSet(elseBlock, instanceId, arrayIndex, value, newArrays, 0, newArrays.size() - 1, aVar.getTrace());
        }

        ImExpr highCond = JassIm.ImOperatorCall(WurstOperator.GREATER_EQ,
            JassIm.ImExprs(JassIm.ImVarAccess(arrayIndex), JassIm.ImIntVal(mtype.getArraySize().get(0))));
        ImExpr lowCond = JassIm.ImOperatorCall(WurstOperator.LESS,
            JassIm.ImExprs(JassIm.ImVarAccess(arrayIndex), JassIm.ImIntVal(0)));
        ImExpr condition = JassIm.ImOperatorCall(WurstOperator.OR, JassIm.ImExprs(lowCond, highCond));

        ImStmts body = JassIm.ImStmts(JassIm.ImIf(aVar.getTrace(), condition, thenBlock, elseBlock));

        ImFunction setFunc = JassIm.ImFunction(aVar.getTrace(), aVar.getName() + "_set",
            JassIm.ImTypeVars(), JassIm.ImVars(instanceId, arrayIndex, value),
            JassIm.ImVoid(), locals, body, Lists.newArrayList());

        if (generateStacktraces) {
            ImVar stackPos = JassIm.ImVar(aVar.getTrace(), TypesHelper.imString(), "stackPos", false);
            setFunc.getParameters().add(stackPos);
            if (error.getFunc().getParameters().size() == 2) {
                error.getArguments().add(JassIm.ImVarAccess(stackPos));
            }
        }
        return setFunc;
    }

    // Similar optimizations for generateGetFunc...

    private void generateLinearSet(ImStmts stmts, ImVar indexVar1, ImVar indexVar2, ImVar value,
                                   List<ImVar> newArrays, de.peeeq.wurstscript.ast.Element trace) {
        // Generate if-else chain for small arrays (faster than binary search for n <= 4)
        for (int i = 0; i < newArrays.size(); i++) {
            ImExpr condition = JassIm.ImOperatorCall(WurstOperator.EQ,
                JassIm.ImExprs(JassIm.ImVarAccess(indexVar2), JassIm.ImIntVal(i)));
            ImStmts thenBlock = JassIm.ImStmts(
                JassIm.ImSet(value.getTrace(),
                    JassIm.ImVarArrayAccess(trace, newArrays.get(i),
                        JassIm.ImExprs(JassIm.ImVarAccess(indexVar1))),
                    JassIm.ImVarAccess(value)));
            ImStmts elseBlock = JassIm.ImStmts();
            stmts.add(JassIm.ImIf(trace, condition, thenBlock, elseBlock));
            stmts = elseBlock; // Chain the if-else statements
        }
    }

    private ImFunctionCall imError(ImVar aVar, String msg) {
        return translator.imError(aVar.getTrace(), JassIm.ImStringVal(msg));
    }

    private void generateBinSearchSet(ImStmts stmts, ImVar indexVar1, ImVar indexVar2, ImVar value, List<ImVar> newArrays, int start,
                                      int end, de.peeeq.wurstscript.ast.Element trace) {
        if (start == end) {
            stmts.add(JassIm.ImSet(value.getTrace(),
                JassIm.ImVarArrayAccess(trace, newArrays.get(start),
                    JassIm.ImExprs(JassIm.ImVarAccess(indexVar1))),
                JassIm.ImVarAccess(value)));
        } else {
            int mid = (start + end) / 2;
            ImStmts thenBlock = JassIm.ImStmts();
            ImStmts elseBlock = JassIm.ImStmts();
            ImExpr condition = JassIm.ImOperatorCall(WurstOperator.LESS_EQ,
                JassIm.ImExprs(JassIm.ImVarAccess(indexVar2), JassIm.ImIntVal(mid)));
            stmts.add(JassIm.ImIf(value.getTrace(), condition, thenBlock, elseBlock));

            generateBinSearchSet(thenBlock, indexVar1, indexVar2, value, newArrays, start, mid, trace);
            generateBinSearchSet(elseBlock, indexVar1, indexVar2, value, newArrays, mid + 1, end, trace);
        }
    }

    private ImFunction generateGetFunc(ImVar aVar, List<ImVar> newArrays) {
        ImArrayTypeMulti mtype = (ImArrayTypeMulti) aVar.getType();
        ImVar returnVal = JassIm.ImVar(aVar.getTrace(), mtype.getEntryType(), "returnVal", false);
        ImVars locals = JassIm.ImVars(returnVal);
        ImVar instanceId = JassIm.ImVar(aVar.getTrace(), TypesHelper.imInt(), "index1", false);
        ImVar arrayIndex = JassIm.ImVar(aVar.getTrace(), TypesHelper.imInt(), "index2", false);

        ImFunctionCall error = imError(aVar, "Index out of Bounds");
        ImStmts thenBlock = JassIm.ImStmts(error);
        ImStmts elseBlock = JassIm.ImStmts();

        // Use linear search for small arrays
        if (newArrays.size() <= 4) {
            generateLinearGet(elseBlock, instanceId, arrayIndex, returnVal, newArrays, aVar.getTrace());
        } else {
            generateBinSearchGet(elseBlock, instanceId, arrayIndex, returnVal, newArrays, 0, newArrays.size() - 1, aVar.getTrace());
        }

        ImExpr highCond = JassIm.ImOperatorCall(WurstOperator.GREATER_EQ,
            JassIm.ImExprs(JassIm.ImVarAccess(arrayIndex), JassIm.ImIntVal(mtype.getArraySize().get(0))));
        ImExpr lowCond = JassIm.ImOperatorCall(WurstOperator.LESS,
            JassIm.ImExprs(JassIm.ImVarAccess(arrayIndex), JassIm.ImIntVal(0)));
        ImExpr condition = JassIm.ImOperatorCall(WurstOperator.OR, JassIm.ImExprs(lowCond, highCond));

        ImStmts body = JassIm.ImStmts(
            JassIm.ImIf(aVar.getTrace(), condition, thenBlock, elseBlock),
            JassIm.ImReturn(returnVal.getTrace(), JassIm.ImVarAccess(returnVal)));

        ImFunction getFunc = JassIm.ImFunction(aVar.getTrace(), aVar.getName() + "_get",
            JassIm.ImTypeVars(), JassIm.ImVars(instanceId, arrayIndex),
            mtype.getEntryType(), locals, body, Lists.newArrayList());

        if (generateStacktraces) {
            ImVar stackPos = JassIm.ImVar(aVar.getTrace(), TypesHelper.imString(), "stackPos", false);
            getFunc.getParameters().add(stackPos);
            if (error.getFunc().getParameters().size() == 2) {
                error.getArguments().add(JassIm.ImVarAccess(stackPos));
            }
        }
        return getFunc;
    }

    private void generateLinearGet(ImStmts stmts, ImVar indexVar1, ImVar indexVar2, ImVar resultVar,
                                   List<ImVar> newArrays, de.peeeq.wurstscript.ast.Element trace) {
        for (int i = 0; i < newArrays.size(); i++) {
            ImExpr condition = JassIm.ImOperatorCall(WurstOperator.EQ,
                JassIm.ImExprs(JassIm.ImVarAccess(indexVar2), JassIm.ImIntVal(i)));
            ImStmts thenBlock = JassIm.ImStmts(
                JassIm.ImSet(resultVar.getTrace(),
                    JassIm.ImVarAccess(resultVar),
                    JassIm.ImVarArrayAccess(trace, newArrays.get(i),
                        JassIm.ImExprs(JassIm.ImVarAccess(indexVar1)))));
            ImStmts elseBlock = JassIm.ImStmts();
            stmts.add(JassIm.ImIf(trace, condition, thenBlock, elseBlock));
            stmts = elseBlock;
        }
    }

    private void generateBinSearchGet(ImStmts stmts, ImVar indexVar1, ImVar indexVar2, ImVar resultVar, List<ImVar> newArrays, int start,
                                      int end, de.peeeq.wurstscript.ast.Element trace) {
        if (start == end) {
            stmts.add(JassIm.ImSet(resultVar.getTrace(),
                JassIm.ImVarAccess(resultVar),
                JassIm.ImVarArrayAccess(trace, newArrays.get(start),
                    JassIm.ImExprs(JassIm.ImVarAccess(indexVar1)))));
        } else {
            int mid = (start + end) / 2;
            ImStmts thenBlock = JassIm.ImStmts();
            ImStmts elseBlock = JassIm.ImStmts();
            ImExpr condition = JassIm.ImOperatorCall(WurstOperator.LESS_EQ,
                JassIm.ImExprs(JassIm.ImVarAccess(indexVar2), JassIm.ImIntVal(mid)));
            stmts.add(JassIm.ImIf(resultVar.getTrace(), condition, thenBlock, elseBlock));

            generateBinSearchGet(thenBlock, indexVar1, indexVar2, resultVar, newArrays, start, mid, trace);
            generateBinSearchGet(elseBlock, indexVar1, indexVar2, resultVar, newArrays, mid + 1, end, trace);
        }
    }
}
