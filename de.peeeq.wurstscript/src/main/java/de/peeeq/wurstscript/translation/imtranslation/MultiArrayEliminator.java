package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiArrayEliminator {
    private ImProg prog;
    private HashMap<ImVar, GetSetPair> getSetMap = Maps.newHashMap();
    private ImTranslator translator;
    private final boolean generateStacktraces;

    private class GetSetPair {
        ImFunction getter;
        ImFunction setter;

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
        for (ImFunction function : prog.getFunctions()) {
            for (ImVar v : function.getLocals()) {
                if (v.getType() instanceof ImArrayTypeMulti) {
                    ImArrayTypeMulti type = (ImArrayTypeMulti) v.getType();
                    List<Integer> arraySize = type.getArraySize();
                    if (arraySize.size() == 1) {
                        // just remove the size
                        v.setType(JassIm.ImArrayType(type.getEntryType()));
                    } else {
                        throw new CompileError(v, "Unsupported array sizes " + arraySize);
                    }
                }
            }
        }

        replaceVars(prog, getSetMap);
        prog.getGlobals().addAll(newVars);
        prog.getGlobals().removeAll(oldVars);

    }

    private void replaceVars(Element e, Map<ImVar, GetSetPair> oldToNewVar) {

        if (e instanceof ImSet) {
            ImSet set = (ImSet) e;

            // normalize statement expression on left hand side
            ImStmts stmts = JassIm.ImStmts();
            ImLExpr left = set.getLeft();
            while (left instanceof ImStatementExpr) {
                ImStatementExpr se = (ImStatementExpr) left;
                stmts.addAll(se.getStatements().removeAll());
                left = (ImLExpr) se.getExpr();
                left.setParent(null);
            }
            if (left != set.getLeft()) {
                set.setLeft(left);

                // replace vars in statements:
                for (ImStmt s : stmts) {
                    replaceVars(s, oldToNewVar);
                }

                // move statements around set-statement
                Element setParent = set.getParent();
                set.setParent(null);
                stmts.add(set);
                Utils.replace(setParent, set, ImHelper.statementExprVoid(stmts));
            }


            if (left instanceof ImVarArrayAccess) {
                ImVarArrayAccess va = (ImVarArrayAccess) left;
                if (va.getIndexes().size() > 1) {
                    if (getSetMap.containsKey(va.getVar())) {
                        // process children (but not the updatedExpr):
                        replaceVars(va.getIndexes(), oldToNewVar);
                        replaceVars(set.getRight(), oldToNewVar);
                        ImExprs args = JassIm.ImExprs();
                        for (ImExpr val : va.getIndexes()) {
                            args.add(val.copy());
                        }
                        args.add(set.getRight().copy());

                        if (generateStacktraces) {
                            args.add(JassIm.ImStringVal("when writing array " + va.getVar().getName() + StackTraceInjector2.getCallPos(va.getTrace().attrSource())));
                        }

                        set.replaceBy(JassIm.ImFunctionCall(set.getTrace(), getSetMap.get(va.getVar()).setter, JassIm.ImTypeArguments(), args, false, CallType.NORMAL));
                        return;
                    }
                }
            }
        }
        // process children
        for (int i = 0; i < e.size(); i++) {
            replaceVars(e.get(i), oldToNewVar);
        }

        if (e instanceof ImVarArrayAccess) {
            ImVarArrayAccess am = (ImVarArrayAccess) e;
            if (am.getIndexes().size() > 1) {
                if (am.isUsedAsLValue()) {
                    throw new CompileError(am.attrTrace().attrSource(), "Invalid multi array access " + e);
                }
                ImExprs args = JassIm.ImExprs();
                for (ImExpr val : am.getIndexes()) {
                    args.add(val.copy());
                }
                if (generateStacktraces) {
                    args.add(JassIm.ImStringVal("when reading array " + am.getVar().getName() + " in " + StackTraceInjector2.getCallPos(am.getTrace().attrSource())));
                }
                if (getSetMap.containsKey(am.getVar())) {
                    am.replaceBy(JassIm.ImFunctionCall(am.attrTrace(), getSetMap.get(am.getVar()).getter, JassIm.ImTypeArguments(), args, false, CallType.NORMAL));
                }
            }
        }

    }


    private ImFunction generateSetFunc(ImVar aVar, List<ImVar> newArrays) {
        ImArrayTypeMulti mtype = (ImArrayTypeMulti) aVar.getType();
        ImVars locals = JassIm.ImVars();
        ImVar instanceId = JassIm.ImVar(aVar.getTrace(), TypesHelper.imInt(), "instanceId", false);
        ImVar arrayIndex = JassIm.ImVar(aVar.getTrace(), TypesHelper.imInt(), "arrayIndex", false);
        ImVar value = JassIm.ImVar(aVar.getTrace(), mtype.getEntryType(), "value", false);
        ImFunctionCall error = imError(aVar, "Index out of Bounds");
        ImStmts thenBlock = JassIm.ImStmts(error);
        ImStmts elseBlock = JassIm.ImStmts();
        generateBinSearchSet(elseBlock, instanceId, arrayIndex, value, newArrays, 0, newArrays.size() - 1, aVar.getTrace());
        ImExpr highCond = JassIm.ImOperatorCall(WurstOperator.GREATER_EQ, JassIm.ImExprs(JassIm.ImVarAccess(arrayIndex), JassIm.ImIntVal(mtype.getArraySize().get(0))));
        ImExpr lowCond = JassIm.ImOperatorCall(WurstOperator.LESS, JassIm.ImExprs(JassIm.ImVarAccess(arrayIndex), JassIm.ImIntVal(0)));
        ImExpr condition = JassIm.ImOperatorCall(WurstOperator.OR, JassIm.ImExprs(lowCond, highCond));
        ImStmts body = JassIm.ImStmts(JassIm.ImIf(aVar.getTrace(),
                condition, thenBlock, elseBlock));

        ImFunction setFunc = JassIm.ImFunction(aVar.getTrace(), aVar.getName() + "_set", JassIm.ImTypeVars(), JassIm.ImVars(instanceId, arrayIndex, value), JassIm.ImVoid(), locals, body, Lists.<FunctionFlag>newArrayList());
        if (generateStacktraces) {
            ImVar stackPos = JassIm.ImVar(aVar.getTrace(), TypesHelper.imString(), "stackPos", false);
            setFunc.getParameters().add(stackPos);
            if (error.getFunc().getParameters().size() == 2) {
                error.getArguments().add(JassIm.ImVarAccess(stackPos));
            }
        }
        return setFunc;
    }

    private ImFunctionCall imError(ImVar aVar, String msg) {
        return translator.imError(aVar.getTrace(), JassIm.ImStringVal(msg));
    }


    private void generateBinSearchSet(ImStmts stmts, ImVar indexVar1, ImVar indexVar2, ImVar value, List<ImVar> newArrays, int start,
                                      int end, de.peeeq.wurstscript.ast.Element trace) {
        if (start == end) {
            stmts.add(JassIm.ImSet(value.getTrace(), JassIm.ImVarArrayAccess(trace, newArrays.get(start), JassIm.ImExprs((ImExpr) JassIm.ImVarAccess(indexVar1))), JassIm.ImVarAccess(value)));
        } else {
            int mid = (start + end) / 2;
            ImStmts thenBlock = JassIm.ImStmts();
            ImStmts elseBlock = JassIm.ImStmts();
            ImExpr condition = JassIm
                    .ImOperatorCall(
                            WurstOperator.LESS_EQ,
                            JassIm.ImExprs(JassIm.ImVarAccess(indexVar2),
                                    JassIm.ImIntVal(mid)));
            stmts.add(JassIm.ImIf(value.getTrace(), condition, thenBlock,
                    elseBlock));

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
        generateBinSearchGet(elseBlock, instanceId, arrayIndex, returnVal, newArrays, 0, newArrays.size() - 1, aVar.getTrace());
        ImExpr highCond = JassIm.ImOperatorCall(WurstOperator.GREATER_EQ, JassIm.ImExprs(JassIm.ImVarAccess(arrayIndex), JassIm.ImIntVal(mtype.getArraySize().get(0))));
        ImExpr lowCond = JassIm.ImOperatorCall(WurstOperator.LESS, JassIm.ImExprs(JassIm.ImVarAccess(arrayIndex), JassIm.ImIntVal(0)));
        ImExpr condition = JassIm.ImOperatorCall(WurstOperator.OR, JassIm.ImExprs(lowCond, highCond));
        ImStmts body = JassIm.ImStmts(JassIm.ImIf(aVar.getTrace(),
                condition, thenBlock, elseBlock),
                JassIm.ImReturn(returnVal.getTrace(), JassIm.ImVarAccess(returnVal)));

        ImFunction getFunc = JassIm.ImFunction(aVar.getTrace(), aVar.getName() + "_get", JassIm.ImTypeVars(), JassIm.ImVars(instanceId, arrayIndex), mtype.getEntryType(), locals, body, Lists.<FunctionFlag>newArrayList());
        if (generateStacktraces) {
            ImVar stackPos = JassIm.ImVar(aVar.getTrace(), TypesHelper.imString(), "stackPos", false);
            getFunc.getParameters().add(stackPos);
            if (error.getFunc().getParameters().size() == 2) {
                error.getArguments().add(JassIm.ImVarAccess(stackPos));
            }
        }
        return getFunc;
    }


    private void generateBinSearchGet(ImStmts stmts, ImVar indexVar1, ImVar indexVar2, ImVar resultVar, List<ImVar> newArrays, int start,
                                      int end, de.peeeq.wurstscript.ast.Element trace) {
        if (start == end) {
            stmts.add(JassIm.ImSet(resultVar.getTrace(), JassIm.ImVarAccess(resultVar), JassIm.ImVarArrayAccess(trace, newArrays.get(start), JassIm.ImExprs((ImExpr) JassIm.ImVarAccess(indexVar1)))));
        } else {
            int mid = (start + end) / 2;
            ImStmts thenBlock = JassIm.ImStmts();
            ImStmts elseBlock = JassIm.ImStmts();
            ImExpr condition = JassIm
                    .ImOperatorCall(
                            WurstOperator.LESS_EQ,
                            JassIm.ImExprs(JassIm.ImVarAccess(indexVar2),
                                    JassIm.ImIntVal(mid)));
            stmts.add(JassIm.ImIf(resultVar.getTrace(), condition, thenBlock,
                    elseBlock));

            generateBinSearchGet(thenBlock, indexVar1, indexVar2, resultVar, newArrays, start, mid, trace);
            generateBinSearchGet(elseBlock, indexVar1, indexVar2, resultVar, newArrays, mid + 1, end, trace);
        }
    }
}
