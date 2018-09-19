package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiArrayEliminator {
    private ImProg prog;
    private HashMap<ImVar, GetSetPair> getSetMap = Maps.newHashMap();
    private ImTranslator translator;

    private class GetSetPair {
        ImFunction getter;
        ImFunction setter;

        public GetSetPair(ImFunction get, ImFunction set) {
            getter = get;
            setter = set;
        }
    }

    public MultiArrayEliminator(ImProg imProg, ImTranslator tr) {
        this.prog = imProg;
        this.translator = tr;
    }

    public void run() {
        List<ImVar> oldVars = Lists.newArrayList();
        List<ImVar> newVars = Lists.newArrayList();
        for (ImVar v : prog.getGlobals()) {
            if (v.getType() instanceof ImArrayTypeMulti) {
                oldVars.add(v);
                ImArrayTypeMulti type = (ImArrayTypeMulti) v.getType();
                List<Integer> arraySize = type.getArraySize();
                if (arraySize.size() == 2) {
                    int size0 = arraySize.get(0);
                    List<ImVar> newArrays = Lists.newArrayList();
                    for (int i = 0; i < size0; i++) {
                        ImVar newVar = JassIm.ImVar(v.getTrace(), JassIm.ImArrayType(type.getTypename()), v.getName() + "_" + i, false);
                        newArrays.add(newVar);
                    }
                    ImFunction setFunc = generateSetFunc(v, newArrays);
                    ImFunction getFunc = generateGetFunc(v, newArrays);
                    prog.getFunctions().add(setFunc);
                    prog.getFunctions().add(getFunc);
                    getSetMap.put(v, new GetSetPair(getFunc, setFunc));

                    newVars.addAll(newArrays);
                }
            }
        }
        replaceVars(prog, getSetMap);
        prog.getGlobals().addAll(newVars);
        prog.getGlobals().removeAll(oldVars);

    }

    private void replaceVars(Element e, Map<ImVar, GetSetPair> oldToNewVar) {
        // process children
        for (int i = 0; i < e.size(); i++) {
            replaceVars(e.get(i), oldToNewVar);
        }

        if (e instanceof ImSetArrayMulti) {
            ImSetArrayMulti sm = (ImSetArrayMulti) e;
            ImExprs args = JassIm.ImExprs();
            for (ImExpr val : sm.getIndices()) {
                args.add((ImExpr) val.copy());
            }
            args.add((ImExpr) ((ImSetArrayMulti) e).getRight().copy());
            if (getSetMap.containsKey(sm.getLeft())) {
                sm.replaceBy(JassIm.ImFunctionCall(sm.getTrace(), getSetMap.get(sm.getLeft()).setter, args, false, CallType.NORMAL));
            }

        } else if (e instanceof ImVarArrayMultiAccess) {
            ImVarArrayMultiAccess am = (ImVarArrayMultiAccess) e;
            ImExprs args = JassIm.ImExprs();
            args.add((ImExpr) am.getIndex1().copy());
            args.add((ImExpr) am.getIndex2().copy());
            if (getSetMap.containsKey(am.getVar())) {
                am.replaceBy(JassIm.ImFunctionCall(am.attrTrace(), getSetMap.get(am.getVar()).getter, args, false, CallType.NORMAL));
            }

        }

    }

    private ImFunction generateSetFunc(ImVar aVar, List<ImVar> newArrays) {
        ImArrayTypeMulti mtype = (ImArrayTypeMulti) aVar.getType();
        ImVars locals = JassIm.ImVars();
        ImVar instanceId = JassIm.ImVar(aVar.getTrace(), TypesHelper.imInt(), "instanceId", false);
        ImVar arrayIndex = JassIm.ImVar(aVar.getTrace(), TypesHelper.imInt(), "arrayIndex", false);
        ImVar value = JassIm.ImVar(aVar.getTrace(), JassIm.ImSimpleType(mtype.getTypename()), "value", false);
        ImStmts thenBlock = JassIm.ImStmts(translator.imError(aVar.getTrace(), JassIm.ImStringVal("Index out of Bounds")));
        ImStmts elseBlock = JassIm.ImStmts();
        generateBinSearchSet(elseBlock, instanceId, arrayIndex, value, newArrays, 0, newArrays.size() - 1);
        ImExpr highCond = JassIm.ImOperatorCall(WurstOperator.GREATER_EQ, JassIm.ImExprs(JassIm.ImVarAccess(arrayIndex), JassIm.ImIntVal(mtype.getArraySize().get(0))));
        ImExpr lowCond = JassIm.ImOperatorCall(WurstOperator.LESS, JassIm.ImExprs(JassIm.ImVarAccess(arrayIndex), JassIm.ImIntVal(0)));
        ImExpr condition = JassIm.ImOperatorCall(WurstOperator.OR, JassIm.ImExprs(lowCond, highCond));
        ImStmts body = JassIm.ImStmts(JassIm.ImIf(aVar.getTrace(),
                condition, thenBlock, elseBlock));
        ImFunction setFunc = JassIm.ImFunction(aVar.getTrace(), aVar.getName() + "_set", JassIm.ImVars(instanceId, arrayIndex, value), JassIm.ImVoid(), locals, body, Lists.<FunctionFlag>newArrayList());
        return setFunc;
    }


    private void generateBinSearchSet(ImStmts stmts, ImVar indexVar1, ImVar indexVar2, ImVar value, List<ImVar> newArrays, int start,
                                      int end) {
        if (start == end) {
            stmts.add(JassIm.ImSetArray(value.getTrace(), newArrays.get(start), JassIm.ImVarAccess(indexVar1), JassIm.ImVarAccess(value)));
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

            generateBinSearchSet(thenBlock, indexVar1, indexVar2, value, newArrays, start, mid);
            generateBinSearchSet(elseBlock, indexVar1, indexVar2, value, newArrays, mid + 1, end);
        }
    }

    private ImFunction generateGetFunc(ImVar aVar, List<ImVar> newArrays) {
        ImArrayTypeMulti mtype = (ImArrayTypeMulti) aVar.getType();
        ImVar returnVal = JassIm.ImVar(aVar.getTrace(), JassIm.ImSimpleType(mtype.getTypename()), "returnVal", false);
        ImVars locals = JassIm.ImVars(returnVal);
        ImVar instanceId = JassIm.ImVar(aVar.getTrace(), TypesHelper.imInt(), "index1", false);
        ImVar arrayIndex = JassIm.ImVar(aVar.getTrace(), TypesHelper.imInt(), "index2", false);
        ImStmts thenBlock = JassIm.ImStmts(translator.imError(aVar.getTrace(), JassIm.ImStringVal("Index out of Bounds")));
        ImStmts elseBlock = JassIm.ImStmts();
        generateBinSearchGet(elseBlock, instanceId, arrayIndex, returnVal, newArrays, 0, newArrays.size() - 1);
        ImExpr highCond = JassIm.ImOperatorCall(WurstOperator.GREATER_EQ, JassIm.ImExprs(JassIm.ImVarAccess(arrayIndex), JassIm.ImIntVal(mtype.getArraySize().get(0))));
        ImExpr lowCond = JassIm.ImOperatorCall(WurstOperator.LESS, JassIm.ImExprs(JassIm.ImVarAccess(arrayIndex), JassIm.ImIntVal(0)));
        ImExpr condition = JassIm.ImOperatorCall(WurstOperator.OR, JassIm.ImExprs(lowCond, highCond));
        ImStmts body = JassIm.ImStmts(JassIm.ImIf(aVar.getTrace(),
                condition, thenBlock, elseBlock),
                JassIm.ImReturn(returnVal.getTrace(), JassIm.ImVarAccess(returnVal)));
        ImFunction getFunc = JassIm.ImFunction(aVar.getTrace(), aVar.getName() + "_get", JassIm.ImVars(instanceId, arrayIndex), JassIm.ImSimpleType(mtype.getTypename()), locals, body, Lists.<FunctionFlag>newArrayList());
        return getFunc;
    }


    private void generateBinSearchGet(ImStmts stmts, ImVar indexVar1, ImVar indexVar2, ImVar resultVar, List<ImVar> newArrays, int start,
                                      int end) {
        if (start == end) {
            stmts.add(JassIm.ImSet(resultVar.getTrace(), resultVar, JassIm.ImVarArrayAccess(newArrays.get(start), JassIm.ImVarAccess(indexVar1))));
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

            generateBinSearchGet(thenBlock, indexVar1, indexVar2, resultVar, newArrays, start, mid);
            generateBinSearchGet(elseBlock, indexVar1, indexVar2, resultVar, newArrays, mid + 1, end);
        }
    }
}
