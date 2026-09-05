package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.base.Preconditions;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.intermediatelang.optimizer.SideEffectAnalyzer;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.Replacer;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator.VarsForTupleResult;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * a rewrite would return a combination of
 * - List of statements
 * - list of expressions
 * for expressions, returned expressions would never have a parent
 */
public class EliminateTuples {



    public static void eliminateTuplesProg(ImProg imProg, ImTranslator translator) {
        DiscardEvaluation discardEvaluation = new DiscardEvaluation(imProg);
        List<Runnable> removeOldVars = new ArrayList<>();
        removeOldVars.add(transformVars(imProg.getGlobals(), translator));
        for (ImClass c : imProg.getClasses()) {
            removeOldVars.add(transformVars(c.getFields(), translator));
        }

        shareTupleReturnSlotsAcrossOverrides(imProg, translator);
        List<ImFunction> functions = allFunctions(imProg);
        for (ImFunction f : functions) {
            transformFunctionReturnsAndParameters(f, translator);
        }
        for (ImFunction f : functions) {
            eliminateTuplesFunc(f, translator, discardEvaluation);
        }
        removeOldVars.forEach(Runnable::run);
        assertNoTuples(imProg);
    }

    private static void assertNoTuples(Element element) {
        AssertProperty.NOTUPLES.check(element);
        for (int i = 0; i < element.size(); i++) {
            assertNoTuples(element.get(i));
        }
    }

    private static List<ImFunction> allFunctions(ImProg prog) {
        LinkedHashSet<ImFunction> result = new LinkedHashSet<>(prog.getFunctions());
        for (ImClass c : prog.getClasses()) {
            result.addAll(c.getFunctions());
        }
        return new ArrayList<>(result);
    }

    /** Lua retains virtual methods, so all implementations in a dispatch group must write
     * additional tuple return components to the same scalar return slots. */
    private static void shareTupleReturnSlotsAcrossOverrides(ImProg prog, ImTranslator translator) {
        List<ImMethod> methods = new ArrayList<>();
        Set<ImMethod> knownMethods = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ImMethod method : prog.getMethods()) {
            if (knownMethods.add(method)) {
                methods.add(method);
            }
        }
        for (ImClass c : prog.getClasses()) {
            for (ImMethod method : c.getMethods()) {
                if (knownMethods.add(method)) {
                    methods.add(method);
                }
            }
        }
        for (int i = 0; i < methods.size(); i++) {
            for (ImMethod subMethod : methods.get(i).getSubMethods()) {
                if (knownMethods.add(subMethod)) {
                    methods.add(subMethod);
                }
            }
        }

        Map<ImMethod, ImMethod> parents = new IdentityHashMap<>();
        Map<ImFunction, ImMethod> methodForImplementation = new IdentityHashMap<>();
        for (ImMethod method : methods) {
            parents.put(method, method);
        }
        for (ImMethod method : methods) {
            for (ImMethod subMethod : method.getSubMethods()) {
                unionMethods(method, subMethod, parents);
            }
            ImFunction implementation = method.getImplementation();
            if (implementation != null) {
                ImMethod other = methodForImplementation.putIfAbsent(implementation, method);
                if (other != null) {
                    unionMethods(method, other, parents);
                }
            }
        }

        Map<ImMethod, List<ImMethod>> groupsByRoot = new IdentityHashMap<>();
        List<List<ImMethod>> groups = new ArrayList<>();
        for (ImMethod method : methods) {
            ImMethod root = findMethodRoot(method, parents);
            List<ImMethod> group = groupsByRoot.get(root);
            if (group == null) {
                group = new ArrayList<>();
                groupsByRoot.put(root, group);
                groups.add(group);
            }
            group.add(method);
        }

        for (List<ImMethod> group : groups) {
            VarsForTupleResult shared = null;
            for (ImMethod method : group) {
                ImFunction implementation = method.getImplementation();
                if (implementation != null
                    && translator.getOriginalReturnValue(implementation) instanceof ImTupleType) {
                    shared = translator.getTupleTempReturnVarsFor(implementation);
                    break;
                }
            }
            if (shared == null) {
                continue;
            }
            for (ImMethod method : group) {
                ImFunction implementation = method.getImplementation();
                if (implementation != null
                    && translator.getOriginalReturnValue(implementation) instanceof ImTupleType) {
                    translator.setTupleTempReturnVarsFor(implementation, shared);
                }
            }
        }
    }

    private static ImMethod findMethodRoot(ImMethod method, Map<ImMethod, ImMethod> parents) {
        ImMethod parent = parents.get(method);
        if (parent != method) {
            parent = findMethodRoot(parent, parents);
            parents.put(method, parent);
        }
        return parent;
    }

    private static void unionMethods(ImMethod left, ImMethod right, Map<ImMethod, ImMethod> parents) {
        ImMethod leftRoot = findMethodRoot(left, parents);
        ImMethod rightRoot = findMethodRoot(right, parents);
        if (leftRoot != rightRoot) {
            parents.put(rightRoot, leftRoot);
        }
    }

    private static void transformFunctionReturnsAndParameters(ImFunction f, ImTranslator translator) {
        preserveVarargParameter(f);
        transformVars(f.getParameters(), translator).run();
        translator.setOriginalReturnValue(f, f.getReturnType());
        f.setReturnType(getFirstType(f.getReturnType()));
    }

    /**
     * A Lua vararg is represented by one placeholder parameter which the backend renames to `...`.
     * Keep that single parameter even when each source element is a tuple; calls are flattened, and
     * the vararg loop regroups those scalar values into the loop variable's scalar leaves.
     */
    private static void preserveVarargParameter(ImFunction f) {
        if (!f.hasFlag(FunctionFlagEnum.IS_VARARG) || f.getParameters().isEmpty()) {
            return;
        }
        ImVar parameter = f.getParameters().getLast();
        if (TypesHelper.typeContainsTuples(parameter.getType())) {
            parameter.setType(getFirstType(parameter.getType()).copy());
        }
    }


    private static void eliminateTuplesFunc(ImFunction f, final ImTranslator translator,
                                            DiscardEvaluation discardEvaluation) {
        transformVars(f.getLocals(), translator).run();

        tryStep(f, translator, EliminateTuples::toTupleExpressions);
        tryStep(f, translator, EliminateTuples::normalizeTuplesInStatementExprs);
        tryStep(f, translator, (stmts, tr, fn) ->
            removeTupleSelections(stmts, tr, fn, discardEvaluation));
        tryStep(f, translator, EliminateTuples::normalizeTuplesInStatementExprs);
        tryStep(f, translator, (stmts, translator1, fn) ->
            removeTupleExprs(0, stmts, translator1, fn, discardEvaluation));

    }

    private static void removeTupleSelections(ImStmts stmts, ImTranslator tr, ImFunction f,
                                              DiscardEvaluation discardEvaluation) {
        Replacer replacer = new Replacer();
        stmts.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImTupleSelection ts) {
                super.visit(ts);

                if (!(ts.getTupleExpr() instanceof ImTupleExpr)) {
                    throw new CompileError(ts.attrTrace().attrSource(), "Wrong tuple selection: " + ts);
                }

                ImTupleExpr tupleExpr = (ImTupleExpr) ts.getTupleExpr();

                int ti = ts.getTupleIndex();

                ImStmts stmts = JassIm.ImStmts();
                ImExpr result = null;

                assert ti >= 0;
                if (ti >= tupleExpr.getExprs().size()) {
                    throw new RuntimeException("invalid selection: " + ts);
                }
                for (int i = 0; i < tupleExpr.getExprs().size(); i++) {
                    ImExpr te = tupleExpr.getExprs().get(i);
                    de.peeeq.wurstscript.ast.Element trace = te.attrTrace();
                    te.setParent(null);
                    if (i != ti) {
                        // Constructing a tuple evaluates every component. A read can be free of
                        // side effects and still fail (for example, a member access on null), so
                        // only values proven trivial to evaluate may disappear here.
                        ImExpr remaining = extractSideEffect(te, stmts);
                        retainDiscardedValue(remaining, stmts, tr, discardEvaluation);
                    } else { // if it is the part we want to return ...
                        ImExpr selected = extractSideEffect(te, stmts);
                        if (i < tupleExpr.getExprs().size() - 1 && !ts.isUsedAsLValue()) {
                            // Later tuple components still have to run, but the selected value is
                            // evaluated at its original position in the tuple's left-to-right order.
                            result = captureSelectedValue(selected, stmts, f);
                        } else {
                            result = selected;
                        }
                    }
                }
                assert result != null;
                ImStatementExpr replacement1 = JassIm.ImStatementExpr(stmts, result);
                ImLExpr replacement2 = normalizeStatementExpr(replacement1, tr);
                if (replacement2 == null) {
                    replacer.replace(ts, replacement1);
                } else {
                    replacer.replace(ts, replacement2);
                }
            }
        });
    }

    private static ImExpr captureSelectedValue(ImExpr selected, ImStmts stmts, ImFunction f) {
        if (selected instanceof ImTupleExpr tuple) {
            ImExprs captured = JassIm.ImExprs();
            for (ImExpr component : tuple.getExprs()) {
                component.setParent(null);
                captured.add(captureSelectedValue(extractSideEffect(component, stmts), stmts, f));
            }
            return JassIm.ImTupleExpr(captured);
        }
        ImVar temp = JassIm.ImVar(selected.attrTrace(), selected.attrTyp(), "tupleSelection", false);
        f.getLocals().add(temp);
        selected.setParent(null);
        stmts.add(JassIm.ImSet(selected.attrTrace(), JassIm.ImVarAccess(temp), selected));
        return JassIm.ImVarAccess(temp);
    }

    private static void retainDiscardedValue(ImExpr value, ImStmts stmts, ImTranslator tr,
                                             DiscardEvaluation discardEvaluation) {
        if (value instanceof ImTupleExpr tuple) {
            for (ImExpr component : tuple.getExprs()) {
                component.setParent(null);
                retainDiscardedValue(extractSideEffect(component, stmts), stmts, tr,
                    discardEvaluation);
            }
            return;
        }
        if (isSafelyDiscardable(value)) {
            return;
        }
        if (SideEffectAnalyzer.quickcheckHasSideeffects(value)) {
            value.setParent(null);
            stmts.add(value);
        } else if (tr.isLuaTarget()) {
            value.setParent(null);
            stmts.add(discardEvaluation.call(value));
        }
    }

    private static boolean isSafelyDiscardable(ImExpr value) {
        if (value instanceof ImBoolVal
            || value instanceof ImIntVal
            || value instanceof ImRealVal
            || value instanceof ImStringVal
            || value instanceof ImNull
            || value instanceof ImVarAccess
            || value instanceof ImFuncRef) {
            return true;
        }
        if (value instanceof ImOperatorCall operatorCall
            && isTotalOperator(operatorCall.getOp())) {
            return operatorCall.getArguments().stream()
                .allMatch(EliminateTuples::isSafelyDiscardable);
        }
        return false;
    }

    /**
     * Operators other than integer division and modulo are total for well-typed scalar IM values.
     * Keeping their unused results in a Lua discard sink only repeats pure scalar work and prevents
     * tuple-component DCE. Division and modulo stay conservative because a zero divisor can fail.
     */
    private static boolean isTotalOperator(WurstOperator operator) {
        return switch (operator) {
            case DIV_INT, MOD_INT, MOD_REAL, JASS_MOD_INT -> false;
            default -> true;
        };
    }

    /**
     * Lua must evaluate unused tuple components which can still trap. Passing such a value to a
     * tiny non-native sink makes argument evaluation explicit and keeps later optimizers from
     * deleting it as an unread local assignment. One sink is shared by every scalar IM type.
     */
    private static final class DiscardEvaluation {
        private final ImProg prog;
        private final List<DiscardFunction> functionsByType = new ArrayList<>();

        private record DiscardFunction(ImType type, ImFunction function) {
        }

        private DiscardEvaluation(ImProg prog) {
            this.prog = prog;
        }

        private ImFunctionCall call(ImExpr value) {
            ImType type = value.attrTyp();
            ImFunction sink = functionsByType.stream()
                .filter(entry -> entry.type().equalsType(type))
                .map(DiscardFunction::function)
                .findFirst()
                .orElseGet(() -> createSink(value, type));
            return JassIm.ImFunctionCall(value.attrTrace(), sink, JassIm.ImTypeArguments(),
                JassIm.ImExprs(value), false, CallType.NORMAL);
        }

        private ImFunction createSink(ImExpr value, ImType type) {
            ImVar parameter = JassIm.ImVar(value.attrTrace(), type.copy(), "value", false);
            ImFunction sink = JassIm.ImFunction(value.attrTrace(),
                "__wurst_tuple_discard_" + functionsByType.size(), JassIm.ImTypeVars(),
                JassIm.ImVars(parameter), JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(),
                Collections.emptyList());
            functionsByType.add(new DiscardFunction(type.copy(), sink));
            prog.getFunctions().add(sink);
            return sink;
        }
    }

    interface Step {
        void apply(ImStmts e, ImTranslator t, ImFunction f);
    }

    private static void tryStep(ImFunction f, final ImTranslator translator, Step step) {
        String before = f.toString();
        try {
            step.apply(f.getBody(), translator, f);
//            translator.assertProperties(Collections.emptySet(), f.getBody());
        } catch (Throwable t) {
            throw new RuntimeException("\n//// Before -----------\n" + before
                    + "\n\n// After -------------------\n" + f, t);
        }

    }


    private static Runnable transformVars(ImVars vars, ImTranslator translator) {
        Set<ImVar> varsToRemove = new LinkedHashSet<>();
        ListIterator<ImVar> it = vars.listIterator();
        while (it.hasNext()) {
            ImVar v = it.next();
            Preconditions.checkNotNull(v.getParent(), "null parent: " + v);
            if (TypesHelper.typeContainsTuples(v.getType())) {
                VarsForTupleResult varsForTuple = translator.getVarsForTuple(v);
                varsToRemove.add(v);
                for (ImVar nv : varsForTuple.allValues()) {
                    it.add(nv);
                }
            }
        }
        return () -> vars.removeAll(varsToRemove);
    }


    private static ImType getFirstType(ImType t) {
        if (t instanceof ImTupleType) {
            ImTupleType tt = (ImTupleType) t;
            return getFirstType(tt.getTypes().get(0));
        }
        return t;
    }

    /**
     * 1. replace tuples with tuple-expression
     * <p>
     * - Variable access
     * a --> <a_1, a_2, a_3>
     * - Function calls
     * f() --> <f(), temp_return1, temp_return2>
     * - Tuple selections
     * <e_1, e_2, e_3>.2 --> {e_1; temp = e_2; e_3 >> temp}
     * <e_1, e_2, e_3>.3 --> {e_1; e_2 >> e_3}
     * - ...
     */
    private static void toTupleExpressions(ImStmts body, ImTranslator translator, ImFunction f) {
        Replacer replacer = new Replacer();
        body.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImTupleSelection selection) {
                ImExpr selectedStorage = selectTupleStorageComponent(selection, translator, f);
                if (selectedStorage != null) {
                    replacer.replace(selection, selectedStorage);
                    selectedStorage.accept(this);
                    return;
                }
                super.visit(selection);
            }

            @Override
            public void visit(ImNull n) {
                // Expand null<⦅T1, T2, ...⦆>  ==>  <null<T1>, null<T2>, ...>
                ImType t = n.getType(); // or n.attrTyp() if that's the established source of truth
                if (t instanceof ImTupleType) {
                    ImTupleType tt = (ImTupleType) t;

                    ImExprs parts = JassIm.ImExprs();
                    for (ImType elemT : tt.getTypes()) {
                        parts.add(JassIm.ImNull(elemT.copy()));
                    }

                    ImTupleExpr replacement = JassIm.ImTupleExpr(parts);
                    // Replace node in-place:
                    Replacer replacer = new Replacer();
                    replacer.replace(n, replacement);
                } else {
                    super.visit(n);
                }
            }


            @Override
            public void visit(ImVarAccess va) {
                if (va.attrTyp() instanceof ImTupleType) {
                    ImVar v = va.getVar();
                    VarsForTupleResult vars = translator.getVarsForTuple(v);
                    ImExpr expr = vars.<ImExpr>map(
                            parts -> JassIm.ImTupleExpr(
                                    parts.collect(Collectors.toCollection(JassIm::ImExprs))),
                            JassIm::ImVarAccess
                    );
                    replacer.replace(va, expr);
                }
            }

            @Override
            public void visit(ImVarargLoop loop) {
                super.visit(loop);
                Preconditions.checkState(loop.getLoopVars().size() == 1,
                    "Expected one vararg loop variable before tuple elimination.");
                ImVar loopVar = loop.getLoopVars().get(0).getVar();
                if (TypesHelper.typeContainsTuples(loopVar.getType())) {
                    loop.setLoopVars(JassIm.ImVarargLoopVars(
                        translator.getTupleScalarVars(loopVar).stream()
                            .map(JassIm::ImVarargLoopVar)
                            .collect(Collectors.toList())));
                }
            }

            @Override
            public void visit(ImVarArrayAccess va) {
                super.visit(va);
                if (va.attrTyp() instanceof ImTupleType) {
                    ImExprs indexes = va.getIndexes();
                    ImExprs indexExprs = JassIm.ImExprs();
                    ImStmts stmts = JassIm.ImStmts();
                    boolean sideEffects = indexes.stream()
                        .anyMatch(SideEffectAnalyzer::quickcheckHasSideeffects);
                    for (ImExpr ie : indexes) {
                        if (sideEffects) {
                            // use temp variables if there are side effects
                            ImVar tempIndex = JassIm.ImVar(ie.attrTrace(), TypesHelper.imInt(), "tempIndex", false);
                            indexExprs.add(JassIm.ImVarAccess(tempIndex));
                            f.getLocals().add(tempIndex);
                            ie.setParent(null);
                            stmts.add(JassIm.ImSet(va.attrTrace(), JassIm.ImVarAccess(tempIndex), ie));
                        } else {
                            ie.setParent(null);
                            indexExprs.add(ie);
                        }
                    }

                    ImVar v = va.getVar();
                    VarsForTupleResult vars = translator.getVarsForTuple(v);
                    ImExpr expr = vars.<ImExpr>map(
                            parts -> JassIm.ImTupleExpr(
                                    parts.collect(Collectors.toCollection(JassIm::ImExprs))),
                            var -> JassIm.ImVarArrayAccess(va.getTrace(), var, indexExprs.copy())
                    );
                    if (stmts.isEmpty()) {
                        replacer.replace(va, expr);
                    } else {
                        replacer.replace(va,
                                JassIm.ImStatementExpr(stmts,
                                        expr));
                    }
                }
            }

            @Override
            public void visit(ImMemberAccess ma) {
                super.visit(ma);
                if (ma.attrTyp() instanceof ImTupleType) {
                    ImStmts stmts = JassIm.ImStmts();
                    boolean indexesAreEffectful = ma.getIndexes().stream()
                        .anyMatch(SideEffectAnalyzer::quickcheckHasSideeffects);
                    ImExpr receiver = captureOnceIfNeeded(ma.getReceiver(), "tupleReceiver", stmts,
                        f, indexesAreEffectful);
                    ImExprs indexes = captureIndexesOnceIfNeeded(ma.getIndexes(), stmts, f);
                    VarsForTupleResult vars = translator.getVarsForTuple(ma.getVar());
                    ImExpr replacement = vars.<ImExpr>map(
                        parts -> JassIm.ImTupleExpr(parts.collect(Collectors.toCollection(JassIm::ImExprs))),
                        var -> JassIm.ImMemberAccess(ma.getTrace(), receiver.copy(), ma.getTypeArguments().copy(),
                            var, indexes.copy()));
                    if (!stmts.isEmpty()) {
                        replacement = JassIm.ImStatementExpr(stmts, replacement);
                    }
                    replacer.replace(ma, replacement);
                }
            }


            @Override
            public void visit(ImFunctionCall fc) {
                super.visit(fc);
                if (translator.getOriginalReturnValue(fc.getFunc()) instanceof ImTupleType) {
                    Element parent = fc.getParent();
                    fc.setParent(null);

                    VarsForTupleResult returnVars = translator.getTupleTempReturnVarsFor(fc.getFunc());

                    ImVar firstVar = returnVars.allValuesStream().findFirst().get();

                    ImExpr newFc = returnVars.map(
                            parts -> JassIm.ImTupleExpr(
                                    parts.collect(Collectors.toCollection(JassIm::ImExprs))),
                            var -> var == firstVar
                                    ? fc.copy()
                                    : JassIm.ImVarAccess(var)
                    );

                    replacer.replaceInParent(parent, fc, newFc);
                }
            }

            @Override
            public void visit(ImMethodCall mc) {
                super.visit(mc);
                ImFunction implementation = mc.getMethod().getImplementation();
                if (implementation != null && translator.getOriginalReturnValue(implementation) instanceof ImTupleType) {
                    Element parent = mc.getParent();
                    mc.setParent(null);
                    VarsForTupleResult returnVars = translator.getTupleTempReturnVarsFor(implementation);
                    ImVar firstVar = returnVars.allValuesStream().findFirst().get();
                    ImExpr newCall = returnVars.map(
                        parts -> JassIm.ImTupleExpr(parts.collect(Collectors.toCollection(JassIm::ImExprs))),
                        var -> var == firstVar ? mc.copy() : JassIm.ImVarAccess(var));
                    replacer.replaceInParent(parent, mc, newCall);
                }
            }

        });
    }

    /**
     * Select tuple storage before expanding it. Expanding first turns one array/member read into
     * reads of every scalar backing variable, which then have to be preserved through discard
     * calls because those reads can fail. A source-level field read or write only needs the
     * selected backing component.
     */
    private static @org.eclipse.jdt.annotation.Nullable ImExpr selectTupleStorageComponent(
            ImTupleSelection selection, ImTranslator translator, ImFunction f) {
        List<Integer> componentPath = new ArrayList<>();
        ImExpr storage = selection;
        while (storage instanceof ImTupleSelection current) {
            componentPath.add(current.getTupleIndex());
            storage = current.getTupleExpr();
        }
        Collections.reverse(componentPath);

        ImExpr expanded;
        ImStmts prelude = JassIm.ImStmts();
        if (storage instanceof ImVarAccess access && access.attrTyp() instanceof ImTupleType) {
            VarsForTupleResult selected = selectTupleComponent(
                translator.getVarsForTuple(access.getVar()), componentPath);
            if (selected == null) {
                return null;
            }
            expanded = selected.<ImExpr>map(
                parts -> JassIm.ImTupleExpr(parts.collect(Collectors.toCollection(JassIm::ImExprs))),
                JassIm::ImVarAccess);
        } else if (storage instanceof ImVarArrayAccess access
                && access.attrTyp() instanceof ImTupleType) {
            ImExprs indexes = captureIndexesOnceIfNeeded(access.getIndexes(), prelude, f);
            VarsForTupleResult selected = selectTupleComponent(
                translator.getVarsForTuple(access.getVar()), componentPath);
            if (selected == null) {
                return null;
            }
            expanded = selected.<ImExpr>map(
                parts -> JassIm.ImTupleExpr(parts.collect(Collectors.toCollection(JassIm::ImExprs))),
                var -> JassIm.ImVarArrayAccess(access.getTrace(), var, indexes.copy()));
        } else if (storage instanceof ImMemberAccess access
                && access.attrTyp() instanceof ImTupleType) {
            boolean indexesAreEffectful = access.getIndexes().stream()
                .anyMatch(SideEffectAnalyzer::quickcheckHasSideeffects);
            ImExpr receiver = captureOnceIfNeeded(access.getReceiver(), "tupleReceiver", prelude,
                f, indexesAreEffectful);
            ImExprs indexes = captureIndexesOnceIfNeeded(access.getIndexes(), prelude, f);
            VarsForTupleResult selected = selectTupleComponent(
                translator.getVarsForTuple(access.getVar()), componentPath);
            if (selected == null) {
                return null;
            }
            expanded = selected.<ImExpr>map(
                parts -> JassIm.ImTupleExpr(parts.collect(Collectors.toCollection(JassIm::ImExprs))),
                var -> JassIm.ImMemberAccess(access.getTrace(), receiver.copy(),
                    access.getTypeArguments().copy(), var, indexes.copy()));
        } else {
            return null;
        }

        if (prelude.isEmpty()) {
            return expanded;
        }
        return JassIm.ImStatementExpr(prelude, expanded);
    }

    private static @org.eclipse.jdt.annotation.Nullable VarsForTupleResult selectTupleComponent(
            VarsForTupleResult tuple, List<Integer> componentPath) {
        VarsForTupleResult selected = tuple;
        for (int index : componentPath) {
            if (!(selected instanceof ImTranslator.TupleResult tupleResult)
                    || index < 0 || index >= tupleResult.getItems().size()) {
                return null;
            }
            selected = tupleResult.getItems().get(index);
        }
        return selected;
    }

    private static ImExpr captureOnceIfNeeded(ImExpr expr, String name, ImStmts stmts, ImFunction f,
                                              boolean forceCapture) {
        if (!forceCapture && !SideEffectAnalyzer.quickcheckHasSideeffects(expr)) {
            return expr;
        }
        ImVar temp = JassIm.ImVar(expr.attrTrace(), expr.attrTyp(), name, false);
        f.getLocals().add(temp);
        expr.setParent(null);
        stmts.add(JassIm.ImSet(expr.attrTrace(), JassIm.ImVarAccess(temp), expr));
        return JassIm.ImVarAccess(temp);
    }

    private static ImExprs captureIndexesOnceIfNeeded(ImExprs original, ImStmts stmts, ImFunction f) {
        boolean capture = original.stream().anyMatch(SideEffectAnalyzer::quickcheckHasSideeffects);
        ImExprs result = JassIm.ImExprs();
        for (ImExpr index : original) {
            if (capture) {
                ImVar temp = JassIm.ImVar(index.attrTrace(), index.attrTyp(), "tupleIndex", false);
                f.getLocals().add(temp);
                index.setParent(null);
                stmts.add(JassIm.ImSet(index.attrTrace(), JassIm.ImVarAccess(temp), index));
                result.add(JassIm.ImVarAccess(temp));
            } else {
                result.add(index.copy());
            }
        }
        return result;
    }


    /**
     * Normalize Tuples in statement-expressions (move to first tuple param)
     * {stmts >> <e1,e2,e3>}
     * becomes <{stmts >> e1}, e2, e3}
     */
    private static void normalizeTuplesInStatementExprs(ImStmts body, ImTranslator translator, ImFunction f) {
        Replacer replacer = new Replacer();
        body.accept(new Element.DefaultVisitor() {

            @Override
            public void visit(ImStatementExpr se) {
                super.visit(se);
                ImTupleExpr newExpr = normalizeStatementExpr(se, translator);
                if (newExpr != null) {
                    replacer.replace(se, newExpr);
                    newExpr.getExprs().get(0).accept(this);
                }
            }
        });
    }

    private static ImTupleExpr normalizeStatementExpr(ImStatementExpr se, ImTranslator translator) {
        if (se.getExpr() instanceof ImTupleExpr) {
            ImTupleExpr te = (ImTupleExpr) se.getExpr();
            translator.assertProperties(Collections.emptySet(), te);
            ImStmts seStmts = se.getStatements();
            seStmts.setParent(null);
            ImExpr firstExpr = te.getExprs().remove(0);
            ImStatementExpr newStatementExpr = JassIm.ImStatementExpr(seStmts, firstExpr);
            te.getExprs().add(0, newStatementExpr);
            te.setParent(null);
            translator.assertProperties(Collections.emptySet(), te.getExprs());
            return te;
        }
        return null;
    }

    /**
     * Remove tuple expressions
     * - In parameters: Just flatten
     * - Assignments: Become several assignments
     * - In Return: Use temp returns
     */
    private static void removeTupleExprs(int posHint, Element elem, ImTranslator translator,
                                         ImFunction f, DiscardEvaluation discardEvaluation) {
        if (elem.getParent() == null) {
            throw new RuntimeException("elem not used: " + elem);
        }
        for (int i = 0; i < elem.size(); i++) {
            Element child = elem.get(i);
            removeTupleExprs(i, child, translator, f, discardEvaluation);
        }
        Replacer replacer = new Replacer();
        for (int i = 0; i < elem.size(); i++) {
            Element child = elem.get(i);

            if (child instanceof ImTupleExpr) {
                ImTupleExpr tupleExpr = (ImTupleExpr) child;

                Element newElem;
                if (elem instanceof ImTupleSelection) {
                    newElem = inTupleSelection((ImTupleSelection) elem, tupleExpr, f);
                } else if (elem instanceof ImReturn) {
                    newElem = inReturn((ImReturn) elem, tupleExpr, translator, f);
                } else if (elem instanceof ImSet) {
                    ImSet imSet = (ImSet) elem;
                    newElem = inSet(imSet, translator, f);
                } else if (elem instanceof ImExprs) {
                    ImExprs exprs = (ImExprs) elem;
                    if (exprs.getParent() instanceof ImOperatorCall) {
                        ImOperatorCall opCall = (ImOperatorCall) exprs.getParent();
                        handleTupleInOpCall(replacer, opCall, f, discardEvaluation);
                        return;
                    } else if (exprs.getParent() instanceof ImFunctionCall
                        || exprs.getParent() instanceof ImMethodCall) {
                        ImExpr call = (ImExpr) exprs.getParent();
                        replacer.replace(call, stageTupleCallArguments(call, exprs, f));
                        return;
                    } else {
                        // in function arguments, other tuples
                        // just flatten tuples
                        exprs.remove(i);
                        List<ImExpr> tupleExprs = tupleExpr.getExprs().removeAll();
                        exprs.addAll(i, tupleExprs);
                        i--;
                    }
                    continue;
                } else if (elem instanceof ImStmts) {
                    ImStmts stmts = (ImStmts) elem;
                    stmts.remove(i);
                    List<ImExpr> tupleExprs = tupleExpr.getExprs().removeAll();
                    stmts.addAll(i, tupleExprs);
                    i--;
                    continue;
                } else {
                    throw new CompileError(tupleExpr.attrTrace().attrSource(), "Unhandled tuple position: " + elem.getClass().getSimpleName() + " // " + elem);
                }
                replacer.hintPosition(posHint);
                replacer.replace(elem, newElem);
                // since we replaced elem we are done
                // the new element should have no more tuple expressions

                return;
            }

        }

    }

    private static ImStatementExpr stageTupleCallArguments(ImExpr call, ImExprs arguments,
                                                            ImFunction f) {
        ImStmts evaluation = JassIm.ImStmts();
        boolean forceOrder = arguments.stream().anyMatch(EliminateTuples::needsOrderedCapture);
        if (call instanceof ImMethodCall methodCall) {
            forceOrder |= needsOrderedCapture(methodCall.getReceiver());
        }

        // A dynamic receiver is evaluated before the arguments in the source program. Keep it in
        // the same ordered prelude as the flattened tuple components.
        if (call instanceof ImMethodCall methodCall) {
            ImExpr receiver = methodCall.getReceiver();
            receiver.setParent(null);
            OrderedBundle receiverBundle = lowerBundle(receiver, f, forceOrder,
                "tuple_argument_receiver");
            receiverBundle.appendPreludeTo(evaluation);
            if (receiverBundle.values.size() != 1) {
                throw new CompileError(call.attrTrace(), "A method receiver cannot be a tuple.");
            }
            methodCall.setReceiver(receiverBundle.values.getFirst());
        }

        List<ImExpr> originalArguments = arguments.removeAll();
        for (ImExpr argument : originalArguments) {
            argument.setParent(null);
            OrderedBundle argumentBundle = lowerBundle(argument, f, forceOrder, "tuple_argument");
            argumentBundle.appendPreludeTo(evaluation);
            arguments.addAll(argumentBundle.values);
        }

        // Keep the original node in place until Replacer has found its parent. The detached copy is
        // the scalar-only call evaluated after the complete left-to-right argument prelude.
        return JassIm.ImStatementExpr(evaluation, (ImExpr) call.copy());
    }

    private static void handleTupleInOpCall(Replacer replacer, ImOperatorCall opCall, ImFunction f,
                                            DiscardEvaluation discardEvaluation) {
        if (opCall.getParent() == null) {
            throw new RuntimeException("opCall not used: " + opCall);
        }
        ImTupleExpr left = (ImTupleExpr) opCall.getArguments().get(0);
        ImTupleExpr right = (ImTupleExpr) opCall.getArguments().get(1);
        WurstOperator op = opCall.getOp();

        ImStmts evaluation = JassIm.ImStmts();
        boolean forceOrder = needsOrderedCapture(left) || needsOrderedCapture(right);
        List<ImExpr> leftComponents = captureTupleComponents(left, evaluation, f, forceOrder,
            discardEvaluation);
        List<ImExpr> rightComponents = captureTupleComponents(right, evaluation, f, forceOrder,
            discardEvaluation);
        if (leftComponents.size() != rightComponents.size()) {
            throw new CompileError(opCall.attrTrace(), "Cannot compare tuples with different arity.");
        }

        List<ImExpr> componentComparisons = new ArrayList<>();
        for (int i = 0; i < leftComponents.size(); i++) {
            ImExpr l = leftComponents.get(i);
            ImExpr r = rightComponents.get(i);
            componentComparisons.add(JassIm.ImOperatorCall(op, JassIm.ImExprs(l, r)));
        }

        ImExpr newExpr;
        if (op == WurstOperator.EQ) {
            // (x1,y1,z1) == (x2,y2,z2)
            // ==> x1 == x2 && y1 == y2 && z1 == z2
            boolean seen = false;
            ImExpr acc = null;
            for (ImExpr componentComparison : componentComparisons) {
                if (!seen) {
                    seen = true;
                    acc = componentComparison;
                } else {
                    acc = JassIm.ImOperatorCall(WurstOperator.AND, JassIm.ImExprs(acc, componentComparison));
                }
            }
            newExpr = (seen ? Optional.of(acc) : Optional.<ImExpr>empty())
                    .get();
        } else {
            assert op == WurstOperator.NOTEQ;
            // (x1,y1,z1) == (x2,y2,z2)
            // ==> x1 != x2 || y1 != y2 && z1 != z2
            boolean seen = false;
            ImExpr acc = null;
            for (ImExpr componentComparison : componentComparisons) {
                if (!seen) {
                    seen = true;
                    acc = componentComparison;
                } else {
                    acc = JassIm.ImOperatorCall(WurstOperator.OR, JassIm.ImExprs(acc, componentComparison));
                }
            }
            newExpr = (seen ? Optional.of(acc) : Optional.<ImExpr>empty())
                    .get();
        }
        replacer.replace(opCall, JassIm.ImStatementExpr(evaluation, newExpr));
    }

    private static List<ImExpr> captureTupleComponents(ImTupleExpr tuple, ImStmts evaluation,
                                                       ImFunction f, boolean forceOrder,
                                                       DiscardEvaluation discardEvaluation) {
        OrderedBundle bundle = lowerBundle(tuple, f, forceOrder, "tuple_compare", true);
        bundle.appendPreludeTo(evaluation);
        for (int i = 0; i < bundle.values.size(); i++) {
            if (bundle.requiresEagerEvaluation.get(i)) {
                evaluation.add(discardEvaluation.call(bundle.values.get(i).copy()));
            }
        }
        return bundle.values;
    }

    private static ImStatementExpr inSet(ImSet imSet, ImTranslator translator, ImFunction f) {
        registerConcreteTupleStorage(imSet.getLeft(), imSet.getRight(), translator);
        registerConcreteTupleStorage(imSet.getRight(), imSet.getLeft(), translator);
        if (!(imSet.getLeft() instanceof ImTupleExpr) && imSet.getRight() instanceof ImTupleExpr) {
            ImTupleExpr expanded = expandTupleStorageAccess(imSet.getLeft(), translator);
            if (expanded != null) {
                imSet.setLeft(expanded);
            }
        }
        if (!(imSet.getRight() instanceof ImTupleExpr) && imSet.getLeft() instanceof ImTupleExpr
            && imSet.getRight() instanceof ImLExpr) {
            ImTupleExpr expanded = expandTupleStorageAccess((ImLExpr) imSet.getRight(), translator);
            if (expanded != null) {
                imSet.setRight(expanded);
            }
        }
        if (!(imSet.getLeft() instanceof ImTupleExpr && imSet.getRight() instanceof ImTupleExpr)) {
            throw new RuntimeException("invalid set statement:\n" + imSet
                + "\nleft type=" + imSet.getLeft().attrTyp()
                + " right type=" + imSet.getRight().attrTyp());
        }
        ImTupleExpr left  = (ImTupleExpr) imSet.getLeft();
        ImTupleExpr right = (ImTupleExpr) imSet.getRight();

        ImStmts stmts = JassIm.ImStmts();

        // 1) Flatten LHS into L-values (recursively), hoisting side-effects
        List<ImLExpr> lhsLeaves = new ArrayList<>();
        for (ImExpr e : left.getExprs()) {
            flattenLhsTuple(e, lhsLeaves, stmts, f);
        }

        // 2) Capture RHS leaves at their original positions. Assignment always forces capture for
        // non-immutable leaves: this preserves swaps/aliasing even when the RHS itself is pure.
        OrderedBundle rhs = lowerBundle(right, f, true, "tuple_assignment");
        rhs.appendPreludeTo(stmts);
        List<ImExpr> rhsLeaves = rhs.values;

        // 3) Pad / normalize RHS arity to match LHS arity (needed for nested tuples + null<TUPLE>)
        for (int i = rhsLeaves.size(); i < lhsLeaves.size(); i++) {
            // default for the target component's type
            ImType targetT = lhsLeaves.get(i).attrTyp();
            rhsLeaves.add(ImHelper.defaultValueForComplexType(targetT));
        }

        if (rhsLeaves.size() != lhsLeaves.size()) {
            throw new RuntimeException("Tuple arity mismatch in set: LHS has "
                + lhsLeaves.size() + " leaves, RHS has " + rhsLeaves.size()
                + "\nLHS=" + left + "\nRHS=" + right);
        }

        // 4) Publish only after every RHS component has been captured.
        for (int i = 0; i < lhsLeaves.size(); i++) {
            ImLExpr l = lhsLeaves.get(i);
            ImExpr r = rhsLeaves.get(i);
            if (r instanceof ImNull) {
                r = ImHelper.defaultValueForComplexType(l.attrTyp());
            }
            l.setParent(null);
            r.setParent(null);
            stmts.add(JassIm.ImSet(imSet.getTrace(), l, r));
        }

        return ImHelper.statementExprVoid(stmts);
    }

    private static void registerConcreteTupleStorage(ImExpr storage, ImExpr value,
                                                     ImTranslator translator) {
        if (!(value.attrTyp() instanceof ImTupleType tupleType)) {
            return;
        }
        ImVar var;
        ImType concreteType;
        if (storage instanceof ImVarAccess access) {
            var = access.getVar();
            concreteType = tupleType.copy();
        } else if (storage instanceof ImVarArrayAccess access) {
            var = access.getVar();
            concreteType = JassIm.ImArrayType(tupleType.copy());
        } else if (storage instanceof ImMemberAccess access) {
            var = access.getVar();
            if (var.getType() instanceof ImArrayType) {
                concreteType = JassIm.ImArrayType(tupleType.copy());
            } else {
                concreteType = tupleType.copy();
            }
        } else {
            return;
        }
        translator.getVarsForTuple(var, concreteType);
    }

    private static @org.eclipse.jdt.annotation.Nullable ImTupleExpr expandTupleStorageAccess(
            ImLExpr left, ImTranslator translator) {
        if (left instanceof ImVarAccess access) {
            ImExpr expanded = translator.getVarsForTuple(access.getVar()).<ImExpr>map(
                parts -> JassIm.ImTupleExpr(parts.collect(Collectors.toCollection(JassIm::ImExprs))),
                JassIm::ImVarAccess);
            return expanded instanceof ImTupleExpr ? (ImTupleExpr) expanded : null;
        }
        if (left instanceof ImVarArrayAccess access) {
            ImExpr expanded = translator.getVarsForTuple(access.getVar()).<ImExpr>map(
                parts -> JassIm.ImTupleExpr(parts.collect(Collectors.toCollection(JassIm::ImExprs))),
                var -> JassIm.ImVarArrayAccess(access.getTrace(), var, access.getIndexes().copy()));
            return expanded instanceof ImTupleExpr ? (ImTupleExpr) expanded : null;
        }
        if (left instanceof ImMemberAccess access) {
            ImExpr expanded = translator.getVarsForTuple(access.getVar()).<ImExpr>map(
                parts -> JassIm.ImTupleExpr(parts.collect(Collectors.toCollection(JassIm::ImExprs))),
                var -> JassIm.ImMemberAccess(access.getTrace(), access.getReceiver().copy(),
                    access.getTypeArguments().copy(), var, access.getIndexes().copy()));
            return expanded instanceof ImTupleExpr ? (ImTupleExpr) expanded : null;
        }
        return null;
    }

    /** Flatten LHS recursively into addressable leaves (ImLExpr), hoisting side-effects */
    private static void flattenLhsTuple(ImExpr e, List<ImLExpr> out, ImStmts sideStmts, ImFunction f) {
        ImExpr x = extractSideEffect(e, sideStmts);
        if (x instanceof ImTupleExpr) {
            for (ImExpr sub : ((ImTupleExpr) x).getExprs()) {
                flattenLhsTuple(sub, out, sideStmts, f);
            }
        } else {
            out.add(captureLvalueAddress((ImLExpr) x, sideStmts, f));
        }
    }

    /** Capture the address-bearing parts of an lvalue before evaluating the assignment RHS. */
    private static ImLExpr captureLvalueAddress(ImLExpr lvalue, ImStmts stmts, ImFunction f) {
        if (lvalue instanceof ImMemberAccess access) {
            ImExpr receiver = access.getReceiver();
            receiver.setParent(null);
            access.setReceiver(captureValue(receiver, "tuple_lvalue_receiver", stmts, f));
            captureLvalueIndexes(access.getIndexes(), stmts, f);
        } else if (lvalue instanceof ImVarArrayAccess access) {
            captureLvalueIndexes(access.getIndexes(), stmts, f);
        }
        return lvalue;
    }

    private static void captureLvalueIndexes(ImExprs indexes, ImStmts stmts, ImFunction f) {
        for (int i = 0; i < indexes.size(); i++) {
            ImExpr index = indexes.get(i);
            if (isImmutableValue(index)) {
                continue;
            }
            index.setParent(null);
            indexes.set(i, captureValue(index, "tuple_lvalue_index", stmts, f));
        }
    }

    private static ImExpr captureValue(ImExpr value, String name, ImStmts stmts, ImFunction f) {
        ImVar temp = JassIm.ImVar(value.attrTrace(), value.attrTyp(), name, false);
        f.getLocals().add(temp);
        stmts.add(JassIm.ImSet(value.attrTrace(), JassIm.ImVarAccess(temp), value));
        return JassIm.ImVarAccess(temp);
    }

    /**
     * A tuple value after lowering: statements which must run in source order, followed by its
     * identity-free scalar components. When ordering matters, each mutable/effectful component is
     * captured as soon as it is reached; a later component can therefore neither change an earlier
     * read nor overwrite a shared tuple-return slot.
     */
    private static final class OrderedBundle {
        private final ImStmts prelude = JassIm.ImStmts();
        private final List<ImExpr> values = new ArrayList<>();
        private final List<Boolean> requiresEagerEvaluation = new ArrayList<>();

        private void appendPreludeTo(ImStmts target) {
            for (ImStmt statement : prelude.removeAll()) {
                statement.setParent(null);
                target.add(statement);
            }
        }
    }

    private static OrderedBundle lowerBundle(ImExpr expression, ImFunction f, boolean forceOrder,
                                             String temporaryName) {
        return lowerBundle(expression, f, forceOrder, temporaryName, false);
    }

    private static OrderedBundle lowerBundle(ImExpr expression, ImFunction f, boolean forceOrder,
                                             String temporaryName,
                                             boolean capturePotentiallyFailingValues) {
        OrderedBundle result = new OrderedBundle();
        lowerBundleInto(expression, f, forceOrder || needsOrderedCapture(expression), temporaryName,
            capturePotentiallyFailingValues, result);
        return result;
    }

    private static void lowerBundleInto(ImExpr expression, ImFunction f, boolean capture,
                                        String temporaryName, boolean capturePotentiallyFailingValues,
                                        OrderedBundle result) {
        ImExpr value = extractSideEffect(expression, result.prelude);
        if (value instanceof ImNull nullValue && nullValue.getType() instanceof ImTupleType) {
            lowerBundleInto(ImHelper.defaultValueForComplexType(nullValue.getType()), f, capture,
                temporaryName, capturePotentiallyFailingValues, result);
            return;
        }
        if (value instanceof ImTupleExpr tuple) {
            for (ImExpr component : new ArrayList<>(tuple.getExprs())) {
                lowerBundleInto(component, f, capture, temporaryName,
                    capturePotentiallyFailingValues, result);
            }
            return;
        }

        value.setParent(null);
        boolean requiresEagerEvaluation = capturePotentiallyFailingValues
            && !isSafelyDiscardable(value)
            && !SideEffectAnalyzer.quickcheckHasSideeffects(value);
        if ((capture || requiresEagerEvaluation) && !isImmutableValue(value)) {
            result.values.add(captureValue(value, temporaryName, result.prelude, f));
        } else {
            result.values.add(value);
        }
        result.requiresEagerEvaluation.add(requiresEagerEvaluation);
    }

    private static boolean needsOrderedCapture(ImExpr expression) {
        return SideEffectAnalyzer.quickcheckHasSideeffects(expression);
    }

    private static boolean isImmutableValue(ImExpr expression) {
        return expression instanceof ImBoolVal
            || expression instanceof ImIntVal
            || expression instanceof ImRealVal
            || expression instanceof ImStringVal
            || expression instanceof ImNull
            || expression instanceof ImFuncRef
            || expression instanceof ImTypeIdOfClass;
    }

    private static ImStatementExpr inReturn(ImReturn parent, ImTupleExpr tupleExpr,
                                            ImTranslator translator, ImFunction f) {
        // flat list of return temps, already created by translator:
        List<ImVar> returnVars = translator.getTupleTempReturnVarsFor(f)
            .allValuesStream().collect(Collectors.toList());

        ImStmts stmts = JassIm.ImStmts();

        // 1) Lower and, where necessary, capture each component at its original evaluation point.
        OrderedBundle result = lowerBundle(tupleExpr, f, false, "tuple_return");
        result.appendPreludeTo(stmts);

        // Sanity:
        if (result.values.size() != returnVars.size()) {
            throw new CompileError(parent.getTrace(),
                "Cannot return tuple with " + result.values.size()
                    + " element(s) from function expecting " + returnVars.size() + " element(s)");
        }

        // 2) Publish only after the complete ordered bundle has been evaluated. Effectful bundles
        // have already captured mutable reads and shared return slots in their prelude.
        for (int i = 0; i < returnVars.size(); i++) {
            ImVar rv = returnVars.get(i);
            ImExpr rhs = result.values.get(i);
            rhs.setParent(null);

            if (rhs instanceof ImNull) {
                rhs = ImHelper.defaultValueForComplexType(rv.getType());
            }
            stmts.add(JassIm.ImSet(parent.getTrace(), JassIm.ImVarAccess(returnVars.get(i)),
                rhs));
        }

        // 3) Return the first component slot
        stmts.add(JassIm.ImReturn(parent.getTrace(), JassIm.ImVarAccess(returnVars.get(0))));
        return ImHelper.statementExprVoid(stmts);
    }

    private static Element inTupleSelection(ImTupleSelection ts, ImTupleExpr tupleExpr, ImFunction f) {
        assert ts.getTupleExpr() == tupleExpr;

        int ti = ts.getTupleIndex();

        ImStmts stmts = JassIm.ImStmts();
        ImExpr result = null;


        for (int i = 0; i < tupleExpr.getExprs().size(); i++) {
            ImExpr te = tupleExpr.getExprs().get(i);
            de.peeeq.wurstscript.ast.Element trace = te.attrTrace();
            te.setParent(null);
            if (i != ti) {
                // if not the thing we want to return, just keep it in statements for side-effects
                stmts.add(te);
            } else { // if it is the part we want to return ...
                if (i == tupleExpr.getExprs().size() - 1) {
                    // last expression of tuple
                    result = te;
                } else {
                    if (ts.isUsedAsLValue()) {
                        // if this is used as L-value we cannot use temporary variables, so just
                        // use the current expression as result.
                        // This assumes that the expression te cannot be influenced by subsequent expressions
                        // TODO maybe this assumption should be validated ...
                        result = extractSideEffect(te, stmts);
                    } else {
                        ImVar temp = JassIm.ImVar(trace, te.attrTyp(), "tupleSelection", false);
                        f.getLocals().add(temp);
                        stmts.add(JassIm.ImSet(trace, JassIm.ImVarAccess(temp), te));
                        result = JassIm.ImVarAccess(temp);
                    }
                }
            }
        }
        assert result != null;

        return JassIm.ImStatementExpr(stmts, result);
    }

    /**
     * extracts all side effects into the list of statements
     */
    private static ImExpr extractSideEffect(ImExpr e, List<ImStmt> into) {
        if (e instanceof ImStatementExpr) {
            ImStatementExpr se = (ImStatementExpr) e;
            for (ImStmt s : se.getStatements()) {
                s.setParent(null);
                into.add(s);
            }
            ImExpr expr = se.getExpr();
            expr.setParent(null);
            return extractSideEffect(expr, into);
        } else if (e instanceof ImTupleExpr) {
            ImTupleExpr te = (ImTupleExpr) e;
            if (!te.getExprs().isEmpty()) {
                ImExpr firstExpr = te.getExprs().get(0);
                ImExpr newFirstExpr = extractSideEffect(firstExpr, into);
                if (newFirstExpr != firstExpr) {
                    te.getExprs().set(0, newFirstExpr);
                }
            }
        }
        return e;
    }
}
