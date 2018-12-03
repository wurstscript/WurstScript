package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.AstElementWithNameId;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * eliminate classes and dynamic method invocations
 */
public class EliminateClasses {

    private final ImTranslator translator;
    private final ImProg prog;
    private final Map<ImVar, ImVar> fieldToArray = Maps.newLinkedHashMap();
    private final Map<ImMethod, ImFunction> dispatchFuncs = Maps.newLinkedHashMap();
    private final RecycleCodeGenerator recycleCodeGen = new RecycleCodeGeneratorQueue();
    private boolean checkedDispatch;

    public EliminateClasses(ImTranslator tr, ImProg prog, boolean checkedDispatch) {
        translator = tr;
        this.prog = prog;
        this.checkedDispatch = checkedDispatch;
    }

    public void eliminateClasses() {


        for (ImClass c : prog.getClasses()) {
            eliminateClass(c);
        }

        for (ImFunction f : prog.getFunctions()) {
            eliminateClassRelatedExprs(f);
        }

        prog.getClasses().clear();
    }

    private void eliminateClass(ImClass c) {
        // for each field, create a global array variable
        for (ImVar f : c.getFields()) {
            ImVar v = JassIm
                    .ImVar(f.getTrace(), JassIm.ImArrayType(f.getType()), f.getName(), false);
            prog.getGlobals().add(v);
            fieldToArray.put(f, v);
        }
        // for each method, create a dispatch function
        for (ImMethod m : c.getMethods()) {
            createDispatchFunc(c, m);
        }

        // create management functions
        recycleCodeGen.createAllocFunc(translator, prog, c);
        recycleCodeGen.createDeallocFunc(translator, prog, c);
    }


    public void createDispatchFunc(ImClass c, ImMethod m) {
        List<ImMethod> methods = Lists.newArrayList();
        addSubMethods(m, methods);

        List<Pair<IntRange, ImMethod>> ranges = calculateTypeIdRanges(c,
                methods);


        List<FunctionFlag> flags = new ArrayList<>();
        if (m.getImplementation().hasFlag(FunctionFlagEnum.IS_VARARG)) {
            // if implementation has varargs, dispatch also needs varargs
            flags.add(FunctionFlagEnum.IS_VARARG);
        }


        ImFunction df = JassIm.ImFunction(m.getTrace(), "dispatch_" + c.getName() + "_" + m.getName(), m
                .getImplementation().getParameters().copy(), m
                .getImplementation().getReturnType(), JassIm.ImVars(), JassIm
                .ImStmts(), flags);
        prog.getFunctions().add(df);
        dispatchFuncs.put(m, df);


        ImType returnType = df.getReturnType();
        if (ranges.isEmpty()) {
            // no implementations for method
            if (!(returnType instanceof ImVoid)) {
                // return default value if it is not void
                ImExpr rv = ImHelper.defaultValueForComplexType(returnType);
                df.getBody().add(JassIm.ImReturn(df.getTrace(), rv));
            }
            return;
        }


        ImVar resultVar;
        if (returnType instanceof ImVoid) {
            resultVar = null;
        } else {
            resultVar = JassIm.ImVar(df.getTrace(), returnType, m.getName()
                    + "_result", false);
            df.getLocals().add(resultVar);
        }

        ClassManagementVars mVars = translator.getClassManagementVarsFor(c);
        ImVar thisVar = df.getParameters().get(0);
        ImExpr typeId = JassIm.ImVarArrayAccess(m.getTrace(), mVars.typeId, JassIm.ImExprs((ImExpr) JassIm.ImVarAccess(thisVar)));

        // ckeck if destroyed or nullpointer
        if (checkedDispatch) {
            Element trace = m.attrTrace();
            String methodName = getMethodName(m);

            df.getBody().add(
                    // if typeId[this] == 0
                    JassIm.ImIf(
                            df.getTrace(), JassIm.ImOperatorCall(WurstOperator.EQ, JassIm.ImExprs(
                                    (ImExpr) typeId.copy(), JassIm.ImIntVal(0)
                            )),
                            // then
                            // if this == 0
                            JassIm.ImStmts(JassIm.ImIf(df.getTrace(), JassIm.ImOperatorCall(WurstOperator.EQ, JassIm.ImExprs(
                                    JassIm.ImVarAccess(thisVar), JassIm.ImIntVal(0)
                                    )),
                                    // then error(NPE)
                                    JassIm.ImStmts(translator.imError(trace, JassIm.ImStringVal("Nullpointer exception when calling " + c.getName() + "." + methodName)))
                                    ,
                                    // else error(unallocated)
                                    JassIm.ImStmts(translator.imError(trace, JassIm.ImStringVal("Called " + c.getName() + "." + methodName + " on invalid object.")))
                            ))

                            , JassIm.ImStmts())
            );
        }


        createDispatch(df, df.getBody(), resultVar, typeId, ranges, 0,
                ranges.size() - 1);
        if (resultVar != null) {
            df.getBody().add(
                    JassIm.ImReturn(df.getTrace(),
                            JassIm.ImVarAccess(resultVar)));
        }
    }

    private String getMethodName(ImMethod m) {
        Element trace = m.attrTrace();
        String methodName = m.getName();
        if (trace instanceof AstElementWithNameId) {
            methodName = ((AstElementWithNameId) trace).getNameId().getName();
        }
        return methodName;
    }

    private void createDispatch(ImFunction df, ImStmts stmts, ImVar resultVar,
                                ImExpr typeId, List<Pair<IntRange, ImMethod>> ranges, int start,
                                int end) {
        if (start == end) {
            ImExprs arguments = JassIm.ImExprs();
            for (ImVar p : df.getParameters()) {
                arguments.add(JassIm.ImVarAccess(p));
            }
            // only one method, call it
            ImFunctionCall call = JassIm.ImFunctionCall(df.getTrace(), ranges
                    .get(start).getB().getImplementation(), arguments, false, CallType.NORMAL);
            if (resultVar == null) {
                stmts.add(call);
            } else {
                stmts.add(JassIm.ImSet(df.getTrace(), JassIm.ImVarAccess(resultVar), call));
            }
        } else {
            int mid = (start + end) / 2;
            ImStmts thenBlock = JassIm.ImStmts();
            ImStmts elseBlock = JassIm.ImStmts();
            ImExpr condition = JassIm
                    .ImOperatorCall(
                            WurstOperator.LESS_EQ,
                            JassIm.ImExprs((ImExpr) typeId.copy(),
                                    JassIm.ImIntVal(ranges.get(mid).getA().end)));
            stmts.add(JassIm.ImIf(df.getTrace(), condition, thenBlock,
                    elseBlock));

            createDispatch(df, thenBlock, resultVar, typeId, ranges, start, mid);
            createDispatch(df, elseBlock, resultVar, typeId, ranges, mid + 1,
                    end);
        }
    }

    private void addSubMethods(ImMethod m, List<ImMethod> methods) {
        if (!m.getIsAbstract()) {
            methods.add(m);
        }
        for (ImMethod mm : m.getSubMethods()) {
            addSubMethods(mm, methods);
        }
    }

    /**
     * returns a mapping from classdefs to functions into a mapping from typeid
     * ranges to functions
     * <p>
     * the resulting list is sorted by the intrange and the intervals are
     * disjunct
     */
    private List<Pair<IntRange, ImMethod>> calculateTypeIdRanges(ImClass c,
                                                                 List<ImMethod> methods) {
        Map<Integer, ImMethod> typeIdToMethod = Maps.newLinkedHashMap();
        calculateTypeIdToMethodHelper(c, methods, null, typeIdToMethod);

        int min = Integer.MAX_VALUE;
        int max = 0;
        for (int i : typeIdToMethod.keySet()) {
            min = Math.min(min, i);
            max = Math.max(max, i);
        }

        List<Pair<IntRange, ImMethod>> result = Lists.newArrayList();

        for (int i = min; i <= max; i++) {
            ImMethod f = typeIdToMethod.get(i);
            if (f == null) {
                continue;
            }
            int j = i;
            while (typeIdToMethod.get(j) == f) {
                j++;
            }
            result.add(Pair.create(new IntRange(i, j - 1), f));
            i = j - 1;
        }
        return result;
    }

    private void calculateTypeIdToMethodHelper(ImClass c,
                                               List<ImMethod> methods, ImMethod current,
                                               Map<Integer, ImMethod> typeIdToMethod) {
        for (ImMethod m : methods) {
            if (m.attrClass() == c) {
                current = m;
                break;
            }
        }
        if (current != null) {
            typeIdToMethod.put(c.attrTypeId(), current);
        }
        // process subclasses:
        for (ImClass sc : c.attrSubclasses()) {
            calculateTypeIdToMethodHelper(sc, methods, current, typeIdToMethod);
        }
    }

    private void eliminateClassRelatedExprs(ImFunction f) {
        final List<ImMemberAccess> mas = Lists.newArrayList();
        final List<ImMethodCall> mcs = Lists.newArrayList();
        final List<ImAlloc> allocs = Lists.newArrayList();
        final List<ImDealloc> deallocs = Lists.newArrayList();
        final List<ImInstanceof> instaneofs = Lists.newArrayList();
        final List<ImTypeIdOfObj> typeIdObjs = Lists.newArrayList();
        final List<ImTypeIdOfClass> typeIdClasses = Lists.newArrayList();
        f.getBody().accept(new ImStmts.DefaultVisitor() {
            @Override
            public void visit(ImMemberAccess e) {
                super.visit(e);
                mas.add(e);
            }

            @Override
            public void visit(ImMethodCall e) {
                super.visit(e);
                mcs.add(e);
            }

            @Override
            public void visit(ImAlloc e) {
                super.visit(e);
                allocs.add(e);
            }

            @Override
            public void visit(ImDealloc e) {
                super.visit(e);
                deallocs.add(e);
            }

            @Override
            public void visit(ImInstanceof e) {
                super.visit(e);
                instaneofs.add(e);
            }

            @Override
            public void visit(ImTypeIdOfClass e) {
                super.visit(e);
                typeIdClasses.add(e);
            }

            @Override
            public void visit(ImTypeIdOfObj e) {
                super.visit(e);
                typeIdObjs.add(e);
            }
        });
        for (ImMemberAccess ma : mas) {
            replaceMemberAccess(ma);
        }
        for (ImMethodCall mc : mcs) {
            replaceMethodCall(mc);
        }
        for (ImAlloc imAlloc : allocs) {
            replaceAlloc(imAlloc);
        }
        for (ImDealloc imDealloc : deallocs) {
            replaceDealloc(imDealloc);
        }
        for (ImInstanceof e : instaneofs) {
            replaceInstanceof(e);
        }
        for (ImTypeIdOfClass e : typeIdClasses) {
            replaceTypeIdOfClass(e);
        }
        for (ImTypeIdOfObj e : typeIdObjs) {
            replaceTypeIdOfObj(e);
        }
    }

    private void replaceTypeIdOfObj(ImTypeIdOfObj e) {
        ImVar typeIdVar = translator.getClassManagementVarsFor(e.getClazz()).typeId;
        ImExpr obj = e.getObj();
        obj.setParent(null);
        e.replaceBy(JassIm.ImVarArrayAccess(e.attrTrace(), typeIdVar, JassIm.ImExprs(obj)));
    }

    private void replaceTypeIdOfClass(ImTypeIdOfClass e) {
        e.replaceBy(JassIm.ImIntVal(e.getClazz().attrTypeId()));
    }

    private void replaceInstanceof(ImInstanceof e) {
        ImFunction f = e.getNearestFunc();
        List<ImClass> allSubClasses = getAllSubclasses(e.getClazz());
        List<Integer> subClassIds = allSubClasses.stream()
                .map(ImClass::attrTypeId)
                .collect(Collectors.toList());
        List<IntRange> idRanges = IntRange.createFromIntList(subClassIds);
        ImExpr obj = e.getObj();
        obj.setParent(null);
        ImVar typeIdVar = translator.getClassManagementVarsFor(e.getClazz()).typeId;

        ImExpr objTypeId = JassIm.ImVarArrayAccess(e.attrTrace(), typeIdVar, JassIm.ImExprs(obj));

        boolean useTempVar = idRanges.size() >= 2 || idRanges.get(0).start < idRanges.get(0).end;
        ImVar tempVar = null;
        ImExpr objTypeIdExpr = objTypeId;
        if (useTempVar) {
            // use temporary variable
            tempVar = JassIm.ImVar(e.attrTrace(), TypesHelper.imInt(), "instanceOfTemp", false);
            f.getLocals().add(tempVar);
            objTypeIdExpr = JassIm.ImVarAccess(tempVar);
        }
        ImExpr newExpr = null;
        for (IntRange intRange : idRanges) {
            newExpr = or(newExpr, inRange(objTypeIdExpr, intRange));
        }
        if (useTempVar) {
            newExpr = JassIm.ImStatementExpr(JassIm.ImStmts(
                    JassIm.ImSet(f.getTrace(), JassIm.ImVarAccess(tempVar), objTypeId)
            ), newExpr);
        }
        e.replaceBy(newExpr);
    }

    private ImExpr or(ImExpr a, ImExpr b) {
        if (a == null && b == null) return null;
        if (a == null) return b;
        if (b == null) return a;
        return JassIm.ImOperatorCall(WurstOperator.OR, JassIm.ImExprs(a, b));
    }

    private ImExpr inRange(ImExpr obj, IntRange range) {
        if (range.start == range.end) {
            return JassIm.ImOperatorCall(WurstOperator.EQ, JassIm.ImExprs((ImExpr) obj.copy(), JassIm.ImIntVal(range.start)));
        } else {
            return JassIm.ImOperatorCall(WurstOperator.AND, JassIm.ImExprs(
                    JassIm.ImOperatorCall(WurstOperator.GREATER_EQ, JassIm.ImExprs((ImExpr) obj.copy(), JassIm.ImIntVal(range.start))),
                    JassIm.ImOperatorCall(WurstOperator.LESS_EQ, JassIm.ImExprs((ImExpr) obj.copy(), JassIm.ImIntVal(range.end)))));
        }
    }

    private List<ImClass> getAllSubclasses(ImClass clazz) {
        List<ImClass> result = Lists.newArrayList();
        getAllSubclassesH(result, clazz);
        return result;
    }

    private void getAllSubclassesH(List<ImClass> result, ImClass clazz) {
        result.add(clazz);
        for (ImClass sc : clazz.attrSubclasses()) {
            getAllSubclassesH(result, sc);
        }
    }

    private void replaceDealloc(ImDealloc e) {
        ImFunction deallocFunc = translator.deallocFunc.getFor(e.getClazz());
        ImExpr obj = e.getObj();
        obj.setParent(null);
        e.replaceBy(JassIm.ImFunctionCall(e.attrTrace(), deallocFunc, JassIm.ImExprs(obj), false, CallType.NORMAL));

    }

    private void replaceAlloc(ImAlloc e) {
        ImFunction allocFunc = translator.allocFunc.getFor(e.getClazz());
        e.replaceBy(JassIm.ImFunctionCall(e.attrTrace(), allocFunc, JassIm.ImExprs(), false, CallType.NORMAL));
    }

    private void replaceMethodCall(ImMethodCall mc) {
        ImExpr receiver = mc.getReceiver();
        receiver.setParent(null);

        ImExprs arguments = JassIm.ImExprs(receiver);
        arguments.addAll(mc.getArguments().removeAll());

        ImFunction dispatch = dispatchFuncs.get(mc.getMethod());
        if (dispatch == null) {
            throw new CompileError(mc.attrTrace().attrSource(), "Could not find dispatch for " + mc.getMethod().getName());
        }
        mc.replaceBy(JassIm.ImFunctionCall(mc.getTrace(),
                dispatch, arguments, false, CallType.NORMAL));

    }

    private void replaceMemberAccess(ImMemberAccess ma) {
        ImExpr receiver = ma.getReceiver();
        receiver.setParent(null);

        ma.replaceBy(JassIm.ImVarArrayAccess(ma.attrTrace(), fieldToArray.get(ma.getVar()), JassIm.ImExprs(receiver)));

    }

}
