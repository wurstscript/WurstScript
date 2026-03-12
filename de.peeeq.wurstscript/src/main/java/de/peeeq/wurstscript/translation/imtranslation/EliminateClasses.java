package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtojass.TypeRewriteMatcher;
import de.peeeq.wurstscript.translation.imtojass.TypeRewriter;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.peeeq.wurstscript.types.TypesHelper.imInt;

/**
 * eliminate classes and dynamic method invocations
 */
public class EliminateClasses {

    public static final String TYPE_ID_TO_TYPE_NAME = "typeIdToTypeName";
    public static final String MAX_TYPE_ID = "maxTypeId";
    public static final String INSTANCE_COUNT = "instanceCount";
    public static final String MAX_INSTANCE_COUNT = "maxInstanceCount";
    private final ImTranslator translator;
    private final ImProg prog;
    private final Map<ImVar, ImVar> fieldToArray = Maps.newLinkedHashMap();
    private final Map<ImMethod, ImFunction> dispatchFuncs = Maps.newLinkedHashMap();
    private final Map<DispatchKey, ImFunction> narrowedDispatchFuncs = Maps.newLinkedHashMap();
    private final RecycleCodeGenerator recycleCodeGen = new RecycleCodeGeneratorQueue();
    private final boolean checkedDispatch;
    private final Set<String> specialNatives = ImmutableSet.of(
        TYPE_ID_TO_TYPE_NAME,
        MAX_TYPE_ID,
        INSTANCE_COUNT,
        MAX_INSTANCE_COUNT
    );
    private final Map<ImClass, Integer> classIds;
    private ImFunction typeIdToTypeNameFunc;
    private ImFunction maxTypeIdFunc;
    private ImFunction instanceCountFunc;
    private ImFunction maxInstanceCountFunc;

    public EliminateClasses(ImTranslator tr, ImProg prog, boolean checkedDispatch) {
        translator = tr;
        this.prog = prog;
        this.checkedDispatch = checkedDispatch;
        this.classIds = TypeId.calculate(prog);
    }

    public void eliminateClasses() {
        createReflectionFunctions();
        moveFunctionsOutOfClasses();

        for (ImClass c : prog.getClasses()) {
            eliminateClass(c);
        }

        // for each method, create a dispatch function
        for (ImMethod m : prog.getMethods()) {
            ImClass c = m.getMethodClass().getClassDef();
            createDispatchFunc(c, m);
        }

        for (ImFunction f : new ArrayList<>(prog.getFunctions())) {
            eliminateClassRelatedExprs(f.getBody());
        }

        prog.getClasses().clear();
        prog.getMethods().clear();

        eliminateClassTypes();
    }

    private void createReflectionFunctions() {
        typeIdToTypeNameFunc = accessClassManagementVar(
            "typeIdToTypeName",
            TypesHelper.imString(),
            JassIm.ImStringVal("unknown"),
            c -> JassIm.ImStringVal(calculateClassName(c)));
        instanceCountFunc = accessClassManagementVar(
            "instanceCount",
            TypesHelper.imInt(),
            JassIm.ImIntVal(0),
            c -> {
                ClassManagementVars m = translator.getClassManagementVarsFor(c);
                if (m == null) {
                    return JassIm.ImIntVal(0);
                }
                return JassIm.ImOperatorCall(WurstOperator.MINUS,
                    JassIm.ImExprs(
                        JassIm.ImVarAccess(m.maxIndex),
                        JassIm.ImVarAccess(m.freeCount)));
            });
        maxInstanceCountFunc = accessClassManagementVar(
            "maxInstanceCount",
            TypesHelper.imInt(),
            JassIm.ImIntVal(0),
            c -> {
                ClassManagementVars m = translator.getClassManagementVarsFor(c);
                if (m == null) {
                    return JassIm.ImIntVal(0);
                }
                return JassIm.ImVarAccess(m.maxIndex);
            });
        maxTypeIdFunc = maxTypeIdFunc();
    }

    @NotNull
    private ImFunction maxTypeIdFunc() {
        ImVars parameters = JassIm.ImVars();
        int maxTypeId = calculateMaxTypeId(classIds);
        ImFunction f = JassIm.ImFunction(prog.getTrace(), "maxTypeId", JassIm.ImTypeVars(), parameters, imInt(), JassIm.ImVars(), JassIm.ImStmts(JassIm.ImReturn(prog.getTrace(), JassIm.ImIntVal(maxTypeId))), Collections.emptyList());
        prog.getFunctions().add(f);
        return f;
    }

    public static int calculateMaxTypeId(ImProg prog) {
        return calculateMaxTypeId(TypeId.calculate(prog));
    }

    private static int calculateMaxTypeId(Map<ImClass, Integer> classId) {
        boolean seen = false;
        int best = 0;
        for (Integer x : classId.values()) {
            int i = x;
            if (!seen || i > best) {
                seen = true;
                best = i;
            }
        }
        return seen ? best : -1;
    }

    @NotNull
    private ImFunction accessClassManagementVar(String funcName, ImType returnType, ImExpr defaultReturn, Function<ImClass, ImExpr> makeAccess) {
        Element trace = prog.getTrace();
        ImVar typeId = JassIm.ImVar(trace, TypesHelper.imInt(), "typeId", false);
        ImVars parameters = JassIm.ImVars(typeId);
        ImVars locals = JassIm.ImVars();
        int maxTypeId = calculateMaxTypeId(classIds);
        ImClass[] typeIdToClass = new ImClass[maxTypeId + 1];
        for (Map.Entry<ImClass, Integer> e : classIds.entrySet()) {
            typeIdToClass[e.getValue()] = e.getKey();
        }
        ImStmts body = generateBinarySearch(1, maxTypeId, typeId, typeIdToClass, makeAccess);
        body.add(
            JassIm.ImReturn(trace, defaultReturn)
        );

        ImFunction f = JassIm.ImFunction(trace, funcName, JassIm.ImTypeVars(), parameters, returnType, locals, body, Collections.emptyList());
        prog.getFunctions().add(f);
        return f;
    }

    private ImStmts generateBinarySearch(int lower, int upper, ImVar typeId, ImClass[] typeIdToClass, Function<ImClass, ImExpr> makeAccess) {
        if (lower > upper) {
            return JassIm.ImStmts();
        } else if (lower == upper) {
            ImClass c = typeIdToClass[lower];
            if (c == null) {
                return JassIm.ImStmts();
            }
            return JassIm.ImStmts(JassIm.ImReturn(prog.getTrace(), makeAccess.apply(c)));
        } else {
            int mid = lower + (upper - lower) / 2;
            return
                JassIm.ImStmts(JassIm.ImIf(prog.getTrace(),
                    JassIm.ImOperatorCall(WurstOperator.LESS_EQ,
                        JassIm.ImExprs(JassIm.ImVarAccess(typeId), JassIm.ImIntVal(mid))),
                    generateBinarySearch(lower, mid, typeId, typeIdToClass, makeAccess),
                    generateBinarySearch(mid + 1, upper, typeId, typeIdToClass, makeAccess)));

        }
    }

    public static String calculateClassName(ImClass c) {
        Element trace = c.attrTrace();
        if (trace instanceof ClassOrInterface) {
            ClassOrInterface t = (ClassOrInterface) trace;
            return makeName(t);
        }
        return c.getName();
    }

    private static String makeName(ClassOrInterface t) {
        ClassOrInterface parent = t.getParent().attrNearestClassOrInterface();
        if (parent != null) {
            return makeName(parent) + "." + t.getName();
        }
        PackageOrGlobal p = t.attrNearestPackage();
        if (p instanceof WPackage) {
            return ((WPackage) p).getName() + "." + t.getName();
        }
        return t.getName();
    }

    private void eliminateClassTypes() {
        TypeRewriter.rewriteTypes(prog, this::eliminateClassTypes);
    }

    private ImType eliminateClassTypes(ImType imType) {
        return imType.match(new TypeRewriteMatcher() {
            @Override
            public ImType case_ImClassType(ImClassType t) {
                return imInt();
            }
        });
    }


    /**
     * Move all the functions out of classes and into the global program
     */
    private void moveFunctionsOutOfClasses() {
        for (ImClass c : prog.getClasses()) {
            prog.getFunctions().addAll(c.getFunctions().removeAll());
        }
    }


    private void eliminateClass(ImClass c) {
        // for each field, create a global array variable
        for (ImVar f : c.getFields()) {
            ImType type = ImHelper.toArray(f.getType());
            ImVar v = JassIm
                .ImVar(f.getTrace(), type, f.getName(), false);
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


        ImFunction df = JassIm.ImFunction(m.getTrace(), "dispatch_" + c.getName() + "_" + m.getName(), JassIm.ImTypeVars(), m
            .getImplementation().getParameters().copy(), m
            .getImplementation().getReturnType(), JassIm.ImVars(), JassIm
            .ImStmts(), flags);
        prog.getFunctions().add(df);
        dispatchFuncs.put(m, df);

        WLogger.trace(() -> "[DISPATCH] register method=" + m.getName() + "@" + System.identityHashCode(m)
            + " impl=" + m.getImplementation().getName() + "@" + System.identityHashCode(m.getImplementation())
            + " sig=" + m.toString()
            + " df=" + df.getName());

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
        ImExpr typeId = JassIm.ImVarArrayAccess(m.getTrace(), mVars.typeId, JassIm.ImExprs(JassIm.ImVarAccess(thisVar)));

        // ckeck if destroyed or nullpointer
        if (checkedDispatch) {
            Element trace = m.attrTrace();
            String methodName = getMethodName(m);

            df.getBody().add(
                // if typeId[this] == 0
                JassIm.ImIf(
                    df.getTrace(), JassIm.ImOperatorCall(WurstOperator.EQ, JassIm.ImExprs(
                                typeId.copy(), JassIm.ImIntVal(0)
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
                .get(start).getB().getImplementation(), JassIm.ImTypeArguments(), arguments, false, CallType.NORMAL);
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
                    JassIm.ImExprs(typeId.copy(),
                        JassIm.ImIntVal(ranges.get(mid).getA().end)));
            stmts.add(JassIm.ImIf(df.getTrace(), condition, thenBlock,
                elseBlock));

            createDispatch(df, thenBlock, resultVar, typeId, ranges, start, mid);
            createDispatch(df, elseBlock, resultVar, typeId, ranges, mid + 1,
                end);
        }
    }

    private static final class DispatchKey {
        private final ImMethod method;
        private final ImClass receiverClass;

        private DispatchKey(ImMethod method, ImClass receiverClass) {
            this.method = method;
            this.receiverClass = receiverClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DispatchKey)) return false;
            DispatchKey that = (DispatchKey) o;
            return method == that.method && receiverClass == that.receiverClass;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(method) * 31 + System.identityHashCode(receiverClass);
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
        return calculateTypeIdRanges(c, methods, null);
    }

    private List<Pair<IntRange, ImMethod>> calculateTypeIdRanges(ImClass c,
                                                                 List<ImMethod> methods,
                                                                 ImMethod initialCurrent) {
        Map<Integer, ImMethod> typeIdToMethod = Maps.newLinkedHashMap();
        calculateTypeIdToMethodHelper(c, methods, initialCurrent, typeIdToMethod);

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

    private ImMethod resolveCurrentMethodForClass(ImClass receiverClass, List<ImMethod> methods) {
        ImMethod best = null;
        for (ImMethod m : methods) {
            ImClass mc = m.attrClass();
            if (mc == null) {
                continue;
            }
            if (!receiverClass.isSubclassOf(mc)) {
                continue;
            }
            if (best == null || mc.isSubclassOf(best.attrClass())) {
                best = m;
            }
        }
        return best;
    }

    private ImFunction getOrCreateNarrowedDispatch(ImMethod baseMethod, ImClass receiverClass) {
        DispatchKey key = new DispatchKey(baseMethod, receiverClass);
        ImFunction existing = narrowedDispatchFuncs.get(key);
        if (existing != null) {
            return existing;
        }

        List<ImMethod> methods = Lists.newArrayList();
        addSubMethods(baseMethod, methods);
        ImMethod initialCurrent = resolveCurrentMethodForClass(receiverClass, methods);
        List<Pair<IntRange, ImMethod>> ranges = calculateTypeIdRanges(receiverClass, methods, initialCurrent);

        ImFunction impl = baseMethod.getImplementation();
        List<FunctionFlag> flags = new ArrayList<>();
        if (impl.hasFlag(FunctionFlagEnum.IS_VARARG)) {
            flags.add(FunctionFlagEnum.IS_VARARG);
        }

        ImFunction df = JassIm.ImFunction(
            baseMethod.getTrace(),
            "dispatch_narrow_" + receiverClass.getName() + "_" + baseMethod.getMethodClass().getClassDef().getName() + "_" + baseMethod.getName(),
            JassIm.ImTypeVars(),
            impl.getParameters().copy(),
            impl.getReturnType(),
            JassIm.ImVars(),
            JassIm.ImStmts(),
            flags
        );
        prog.getFunctions().add(df);
        narrowedDispatchFuncs.put(key, df);

        ImType returnType = df.getReturnType();
        if (ranges.isEmpty()) {
            if (!(returnType instanceof ImVoid)) {
                ImExpr rv = ImHelper.defaultValueForComplexType(returnType);
                df.getBody().add(JassIm.ImReturn(df.getTrace(), rv));
            }
            return df;
        }

        ImVar resultVar;
        if (returnType instanceof ImVoid) {
            resultVar = null;
        } else {
            resultVar = JassIm.ImVar(df.getTrace(), returnType, baseMethod.getName() + "_result", false);
            df.getLocals().add(resultVar);
        }

        ImClass dispatchRoot = baseMethod.getMethodClass().getClassDef();
        ClassManagementVars mVars = translator.getClassManagementVarsFor(dispatchRoot);
        ImVar thisVar = df.getParameters().get(0);
        ImExpr typeId = JassIm.ImVarArrayAccess(baseMethod.getTrace(), mVars.typeId, JassIm.ImExprs(JassIm.ImVarAccess(thisVar)));

        if (checkedDispatch) {
            Element trace = baseMethod.attrTrace();
            String methodName = getMethodName(baseMethod);
            df.getBody().add(
                JassIm.ImIf(
                    df.getTrace(), JassIm.ImOperatorCall(WurstOperator.EQ, JassIm.ImExprs(
                        typeId.copy(), JassIm.ImIntVal(0)
                    )),
                    JassIm.ImStmts(JassIm.ImIf(df.getTrace(), JassIm.ImOperatorCall(WurstOperator.EQ, JassIm.ImExprs(
                            JassIm.ImVarAccess(thisVar), JassIm.ImIntVal(0)
                        )),
                        JassIm.ImStmts(translator.imError(trace, JassIm.ImStringVal("Nullpointer exception when calling " + dispatchRoot.getName() + "." + methodName))),
                        JassIm.ImStmts(translator.imError(trace, JassIm.ImStringVal("Called " + dispatchRoot.getName() + "." + methodName + " on invalid object.")))
                    )),
                    JassIm.ImStmts()
                )
            );
        }

        createDispatch(df, df.getBody(), resultVar, typeId, ranges, 0, ranges.size() - 1);
        if (resultVar != null) {
            df.getBody().add(
                JassIm.ImReturn(df.getTrace(),
                    JassIm.ImVarAccess(resultVar)));
        }
        return df;
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
            typeIdToMethod.put(typeIdOf(c), current);
        }
        // process subclasses:
        for (ImClass sc : c.attrSubclasses()) {
            calculateTypeIdToMethodHelper(sc, methods, current, typeIdToMethod);
        }
    }

    private void eliminateClassRelatedExprs(de.peeeq.wurstscript.jassIm.Element body) {
        final List<ImMemberAccess> mas = Lists.newArrayList();
        final List<ImMethodCall> mcs = Lists.newArrayList();
        final List<ImAlloc> allocs = Lists.newArrayList();
        final List<ImDealloc> deallocs = Lists.newArrayList();
        final List<ImInstanceof> instaneofs = Lists.newArrayList();
        final List<ImTypeIdOfObj> typeIdObjs = Lists.newArrayList();
        final List<ImTypeIdOfClass> typeIdClasses = Lists.newArrayList();
        final List<ImFunctionCall> nativeCalls = Lists.newArrayList();
        body.accept(new de.peeeq.wurstscript.jassIm.Element.DefaultVisitor() {
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

            @Override
            public void visit(ImFunctionCall fc) {
                super.visit(fc);
                ImFunction func = fc.getFunc();
                if (!func.hasFlag(FunctionFlagEnum.IS_NATIVE)) {
                    return;
                }
                if (specialNatives.contains(func.getName())) {
                    nativeCalls.add(fc);
                }
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
        for (ImFunctionCall nativeCall : nativeCalls) {
            rewriteObjectNative(nativeCall);
        }
    }

    private void rewriteObjectNative(ImFunctionCall nativeCall) {
        ImFunction func = nativeCall.getFunc();
        switch (func.getName()) {
            case TYPE_ID_TO_TYPE_NAME:
                nativeCall.setFunc(typeIdToTypeNameFunc);
                break;
            case MAX_TYPE_ID:
                nativeCall.setFunc(maxTypeIdFunc);
                break;
            case INSTANCE_COUNT:
                nativeCall.setFunc(instanceCountFunc);
                break;
            case MAX_INSTANCE_COUNT:
                nativeCall.setFunc(maxInstanceCountFunc);
                break;
            default:
                throw new RuntimeException("unhandled case: " + func.getName());
        }
    }

    private void replaceTypeIdOfObj(ImTypeIdOfObj e) {
        ImVar typeIdVar = translator.getClassManagementVarsFor(e.getClazz().getClassDef()).typeId;
        ImExpr obj = e.getObj();
        obj.setParent(null);
        e.replaceBy(JassIm.ImVarArrayAccess(e.attrTrace(), typeIdVar, JassIm.ImExprs(obj)));
    }

    private void replaceTypeIdOfClass(ImTypeIdOfClass e) {
        e.replaceBy(JassIm.ImIntVal(typeIdOf(e.getClazz().getClassDef())));
    }

    private void replaceInstanceof(ImInstanceof e) {
        ImFunction f = e.getNearestFunc();
        List<ImClass> allSubClasses = getAllSubclasses(e.getClazz().getClassDef());
        List<Integer> subClassIds = new ArrayList<>();
        for (ImClass allSubClass : allSubClasses) {
            subClassIds.add(typeIdOf(allSubClass));
        }
        List<IntRange> idRanges = IntRange.createFromIntList(subClassIds);
        ImExpr obj = e.getObj();
        obj.setParent(null);
        ImVar typeIdVar = translator.getClassManagementVarsFor(e.getClazz().getClassDef()).typeId;

        ImExpr objTypeId = JassIm.ImVarArrayAccess(e.attrTrace(), typeIdVar, JassIm.ImExprs(obj));

        boolean useTempVar = idRanges.size() >= 2 || idRanges.get(0).start < idRanges.get(0).end;
        ImVar tempVar = null;
        ImExpr objTypeIdExpr = objTypeId;
        if (useTempVar) {
            // use temporary variable
            tempVar = JassIm.ImVar(e.attrTrace(), imInt(), "instanceOfTemp", false);
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
            return JassIm.ImOperatorCall(WurstOperator.EQ, JassIm.ImExprs(obj.copy(), JassIm.ImIntVal(range.start)));
        } else {
            return JassIm.ImOperatorCall(WurstOperator.AND, JassIm.ImExprs(
                JassIm.ImOperatorCall(WurstOperator.GREATER_EQ, JassIm.ImExprs(obj.copy(), JassIm.ImIntVal(range.start))),
                JassIm.ImOperatorCall(WurstOperator.LESS_EQ, JassIm.ImExprs(obj.copy(), JassIm.ImIntVal(range.end)))));
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

    private int typeIdOf(ImClass c) {
        Integer id = classIds.get(c);
        if (id != null) {
            return id;
        }
        Integer fallback = TypeId.calculate(prog).get(c);
        if (fallback != null) {
            return fallback;
        }
        throw new CompileError(c, "Could not resolve type id for class " + c.getName() + ".");
    }

    private void replaceDealloc(ImDealloc e) {
        ImFunction deallocFunc = translator.deallocFunc.getFor(e.getClazz().getClassDef());
        ImExpr obj = e.getObj();
        obj.setParent(null);
        e.replaceBy(JassIm.ImFunctionCall(e.attrTrace(), deallocFunc, JassIm.ImTypeArguments(), JassIm.ImExprs(obj), false, CallType.NORMAL));

    }

    private void replaceAlloc(ImAlloc e) {
        ImFunction allocFunc = translator.allocFunc.getFor(e.getClazz().getClassDef());
        e.replaceBy(JassIm.ImFunctionCall(e.attrTrace(), allocFunc, JassIm.ImTypeArguments(), JassIm.ImExprs(), false, CallType.NORMAL));
    }

    private void replaceMethodCall(ImMethodCall mc) {
        ImExpr receiver = mc.getReceiver();
        ImType receiverType = receiver.attrTyp();
        receiver.setParent(null);

        ImExprs arguments = JassIm.ImExprs(receiver);
        arguments.addAll(mc.getArguments().removeAll());

        ImMethod method = mc.getMethod();
        // Fast path: with unchecked dispatch, a monomorphic method call can be lowered
        // directly to its implementation function.
        if (!checkedDispatch
            && !method.getIsAbstract()
            && method.getSubMethods().isEmpty()) {
            mc.replaceBy(JassIm.ImFunctionCall(
                mc.getTrace(),
                method.getImplementation(),
                JassIm.ImTypeArguments(),
                arguments,
                false,
                CallType.NORMAL));
            return;
        }

        ImFunction dispatch = null;
        if (receiverType instanceof ImClassType) {
            ImClass receiverClass = ((ImClassType) receiverType).getClassDef();
            ImClass methodClass = method.getMethodClass().getClassDef();
            if (receiverClass != methodClass && receiverClass.isSubclassOf(methodClass)) {
                dispatch = getOrCreateNarrowedDispatch(method, receiverClass);
            }
        }
        if (dispatch == null) {
            dispatch = dispatchFuncs.get(method);
        }
        if (dispatch == null) {
            throw new CompileError(mc.attrTrace().attrSource(), "Could not find dispatch for " + method.getName());
        }
        mc.replaceBy(JassIm.ImFunctionCall(mc.getTrace(), dispatch, JassIm.ImTypeArguments(), arguments, false, CallType.NORMAL));

    }

    private void replaceMemberAccess(ImMemberAccess ma) {
        ImExpr receiver = ma.getReceiver();
        receiver.setParent(null);

        ImVar fieldArray = fieldToArray.get(ma.getVar());
        if (fieldArray == null) {
            throw new CompileError(ma, "Could not find field array for " + ma);
        }
        ImExprs indexes = JassIm.ImExprs(receiver);
        indexes.addAll(ma.getIndexes().removeAll());
        ma.replaceBy(JassIm.ImVarArrayAccess(ma.attrTrace(), fieldArray, indexes));

    }

}
