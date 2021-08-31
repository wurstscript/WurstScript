package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.base.Preconditions;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.intermediatelang.optimizer.SideEffectAnalyzer;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.Replacer;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator.VarsForTupleResult;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Utils;

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

        Runnable removeOldGlobals = transformVars(imProg.getGlobals(), translator);
        for (ImFunction f : imProg.getFunctions()) {
            transformFunctionReturnsAndParameters(f, translator);
        }
        for (ImFunction f : imProg.getFunctions()) {
            eliminateTuplesFunc(f, translator);
        }
        removeOldGlobals.run();
        translator.assertProperties(AssertProperty.NOTUPLES);
    }

    private static void transformFunctionReturnsAndParameters(ImFunction f, ImTranslator translator) {
        transformVars(f.getParameters(), translator).run();
        translator.setOriginalReturnValue(f, f.getReturnType());
        f.setReturnType(getFirstType(f.getReturnType()));
    }


    private static void eliminateTuplesFunc(ImFunction f, final ImTranslator translator) {
        transformVars(f.getLocals(), translator).run();

        tryStep(f, translator, EliminateTuples::toTupleExpressions);
        tryStep(f, translator, EliminateTuples::normalizeTuplesInStatementExprs);
        tryStep(f, translator, EliminateTuples::removeTupleSelections);
        tryStep(f, translator, EliminateTuples::normalizeTuplesInStatementExprs);
        tryStep(f, translator, (stmts, translator1, fn) -> removeTupleExprs(0, stmts, translator1, fn));

    }

    private static void removeTupleSelections(ImStmts stmts, ImTranslator tr, ImFunction f) {
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
                        // if not the thing we want to return, just keep it in statements for side-effects
                        extractSideEffect(te, stmts);
                    } else { // if it is the part we want to return ...
                        result = extractSideEffect(te, stmts);
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
            public void visit(ImVarArrayAccess va) {
                super.visit(va);
                if (va.attrTyp() instanceof ImTupleType) {
                    ImExprs indexes = va.getIndexes();
                    ImExprs indexExprs = JassIm.ImExprs();
                    ImStmts stmts = JassIm.ImStmts();
                    boolean sideEffects = indexes.stream().anyMatch(SideEffectAnalyzer::quickcheckHasSideeffects);
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
            public void visit(ImFunctionCall fc) {
                super.visit(fc);
                if (translator.getOriginalReturnValue(fc.getFunc()) instanceof ImTupleType) {
                    Element parent = fc.getParent();
                    fc.setParent(null);

                    VarsForTupleResult returnVars = translator.getTupleTempReturnVarsFor(fc.getFunc());

                    ImVar firstVar = returnVars.allValuesStream().findFirst().get();

                    ImExpr newFc = returnVars.<ImExpr>map(
                            parts -> JassIm.ImTupleExpr(
                                    parts.collect(Collectors.toCollection(JassIm::ImExprs))),
                            var -> var == firstVar
                                    ? fc.copy()
                                    : JassIm.ImVarAccess(var)
                    );

                    replacer.replaceInParent(parent, fc, newFc);
                }
            }

        });
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
    private static void removeTupleExprs(int posHint, Element elem, ImTranslator translator, ImFunction f) {
        if (elem.getParent() == null) {
            throw new RuntimeException("elem not used: " + elem);
        }
        for (int i = 0; i < elem.size(); i++) {
            Element child = elem.get(i);
            removeTupleExprs(i, child, translator, f);
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
                    newElem = inSet(imSet, f);
                } else if (elem instanceof ImExprs) {
                    ImExprs exprs = (ImExprs) elem;
                    if (exprs.getParent() instanceof ImOperatorCall) {
                        ImOperatorCall opCall = (ImOperatorCall) exprs.getParent();
                        handleTupleInOpCall(replacer, opCall);
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

    private static void handleTupleInOpCall(Replacer replacer, ImOperatorCall opCall) {
        if (opCall.getParent() == null) {
            throw new RuntimeException("opCall not used: " + opCall);
        }
        ImTupleExpr left = (ImTupleExpr) opCall.getArguments().get(0);
        ImTupleExpr right = (ImTupleExpr) opCall.getArguments().get(1);
        WurstOperator op = opCall.getOp();

        List<ImExpr> componentComparisons = new ArrayList<>();
        for (int i = 0; i < left.getExprs().size(); i++) {
            ImExpr l = left.getExprs().get(i);
            ImExpr r = right.getExprs().get(i);
            l.setParent(null);
            r.setParent(null);
            componentComparisons.add(JassIm.ImOperatorCall(op, JassIm.ImExprs(l, r)));
        }

        ImExpr newExpr;
        if (op == WurstOperator.EQ) {
            // (x1,y1,z1) == (x2,y2,z2)
            // ==> x1 == x2 && y1 == y2 && z1 == z2
            newExpr = componentComparisons.stream()
                    .reduce((l, r) -> JassIm.ImOperatorCall(WurstOperator.AND, JassIm.ImExprs(l, r)))
                    .get();
        } else {
            assert op == WurstOperator.NOTEQ;
            // (x1,y1,z1) == (x2,y2,z2)
            // ==> x1 != x2 || y1 != y2 && z1 != z2
            newExpr = componentComparisons.stream()
                    .reduce((l, r) -> JassIm.ImOperatorCall(WurstOperator.OR, JassIm.ImExprs(l, r)))
                    .get();
        }
        replacer.replace(opCall, newExpr);
    }

    private static ImStatementExpr inSet(ImSet imSet, ImFunction f) {
        if (!(imSet.getLeft() instanceof ImTupleExpr && imSet.getRight() instanceof ImTupleExpr)) {
            throw new RuntimeException("invalid set statement:\n" + imSet);
        }
        ImTupleExpr left = (ImTupleExpr) imSet.getLeft();
        ImTupleExpr right = (ImTupleExpr) imSet.getRight();

        ImStmts stmts = JassIm.ImStmts();

        // 1) extract side effects from left expressions
        List<ImExpr> leftExprs = new ArrayList<>();
        for (ImExpr expr : left.getExprs()) {
            leftExprs.add(extractSideEffect(expr, stmts));
        }


        List<ImVar> tempVars = new ArrayList<>();
        // 2) assign right hand side to temporary variables:
        for (ImExpr expr : right.getExprs()) {
            ImVar temp = JassIm.ImVar(expr.attrTrace(), expr.attrTyp(), "tuple_temp", false);
            expr.setParent(null);
            stmts.add(JassIm.ImSet(expr.attrTrace(), JassIm.ImVarAccess(temp), expr));
            tempVars.add(temp);
            f.getLocals().add(temp);
        }
        // then assign right vars
        for (int i = 0; i < leftExprs.size(); i++) {
            ImLExpr leftE = (ImLExpr) leftExprs.get(i);
            leftE.setParent(null);
            stmts.add(JassIm.ImSet(imSet.getTrace(), leftE, JassIm.ImVarAccess(tempVars.get(i))));
        }
        return ImHelper.statementExprVoid(stmts);
    }

    private static ImStatementExpr inReturn(ImReturn parent, ImTupleExpr tupleExpr, ImTranslator translator, ImFunction f) {
        VarsForTupleResult returnVars1 = translator.getTupleTempReturnVarsFor(f);
        List<ImVar> returnVars = returnVars1.allValuesStream().collect(Collectors.toList());
        ImStmts stmts = JassIm.ImStmts();

        for (int i = 0; i < returnVars.size(); i++) {
            ImVar rv = returnVars.get(i);
            ImExpr te = tupleExpr.getExprs().get(i);
            te.setParent(null);
            stmts.add(JassIm.ImSet(parent.getTrace(), JassIm.ImVarAccess(rv), te));
        }
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


    private static ImExprs accessVars(List<ImVar> tempIndexes) {
        return tempIndexes.stream()
                .map(JassIm::ImVarAccess)
                .collect(Collectors.toCollection(JassIm::ImExprs));
    }


}
