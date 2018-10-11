package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.base.Preconditions;
import de.peeeq.datastructures.ImmutableTree;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.intermediatelang.optimizer.SideEffectAnalyzer;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * a rewrite would return a combination of
 * - List of statements
 * - list of expressions
 * for expressions, returned expressions would never have a parent
 */
public class EliminateTuples {

    /*
    TODO this could be simplified:

    Visit and rewrite only the expressions that must be changed:



    2. Normalize Tuples in statement-expressions (move to first tuple param)

        Normalize
        {stmts >> <e1,e2,e3>}
        becomes <{stmts >> e1}, e2, e3}


    3. Remove tuple expressions


      - In parameters: Just flatten
      - Assignments: Become several assignments
      - In Return: Use temp returns

     */

    public static void eliminateTuplesProg(ImProg imProg, ImTranslator translator) {

        transformVars(imProg.getGlobals(), translator);
        for (ImFunction f : imProg.getFunctions()) {
            eliminateTuplesFunc(f, translator);
        }
        translator.assertProperties(AssertProperty.NOTUPLES);
    }


    private static void eliminateTuplesFunc(ImFunction f, final ImTranslator translator) {
        // transform parameters
        transformVars(f.getParameters(), translator);
        transformVars(f.getLocals(), translator);

        translator.setOriginalReturnValue(f, f.getReturnType());

        f.setReturnType(getFirstType(f.getReturnType()));


        tryStep(f, translator, EliminateTuples::toTupleExpressions);
        tryStep(f, translator, EliminateTuples::normalizeTuplesInStatementExprs);
        tryStep(f, translator, EliminateTuples::removeTupleExprs);

    }

    interface Step {
        void apply(ImStmts e, ImTranslator t, ImFunction f);
    }

    private static void tryStep(ImFunction f, final ImTranslator translator, Step step) {
        String before = f.toString();
        try {
            step.apply(f.getBody(), translator, f);
        } catch (Throwable t) {
            throw new RuntimeException("\n//// Before -----------\n" + before
                    + "\n\n// After -------------------\n" + f, t);
        }

    }


    private static void transformVars(ImVars vars, ImTranslator translator) {
        ListIterator<ImVar> it = vars.listIterator();
        while (it.hasNext()) {
            ImVar v = it.next();
            Preconditions.checkNotNull(v.getParent(), "null parent: " + v);
            if (TypesHelper.typeContainsTuples(v.getType())) {
                ImmutableTree<ImVar> varsForTuple = translator.getVarsForTuple(v);
                it.remove();
                for (ImVar nv : varsForTuple.allValues()) {
                    it.add(nv);
                }
            }
        }
        for (ImVar v : vars) {
            if (v.getType() instanceof ImTupleType) {
                throw new Error("still contains a bad var: " + v + " in: \n" + v.getParent().getParent());
            }
        }
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
        body.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImVarAccess va) {
                if (va.attrTyp() instanceof ImTupleType) {
                    ImVar v = va.getVar();
                    ImmutableTree<ImVar> vars = translator.getVarsForTuple(v);

                    ImExprs exprs = vars.allValues().stream()
                            .map(JassIm::ImVarAccess).collect(Collectors.toCollection(JassIm::ImExprs));
                    va.replaceBy(JassIm.ImTupleExpr(exprs));
                }
            }

            @Override
            public void visit(ImVarArrayAccess va) {
                super.visit(va);
                if (va.attrTyp() instanceof ImTupleType) {
                    ImExprs indexes = va.getIndexes();
                    List<ImVar> tempIndexes = new ArrayList<>();
                    ImStmts stmts = JassIm.ImStmts();
                    for (ImExpr ie : indexes) {
                        ImVar tempIndex = JassIm.ImVar(ie.attrTrace(), TypesHelper.imInt(), "tempIndex", false);
                        tempIndexes.add(tempIndex);
                        f.getLocals().add(tempIndex);
                        ie.setParent(null);
                        stmts.add(JassIm.ImSet(va.attrTrace(), JassIm.ImVarAccess(tempIndex), ie));
                    }

                    ImVar v = va.getVar();
                    ImmutableTree<ImVar> vars = translator.getVarsForTuple(v);
                    ImExprs exprs = vars.allValues().stream()
                            .map(arrayVar -> JassIm.ImVarArrayAccess(arrayVar, accessVars(tempIndexes)))
                            .collect(Collectors.toCollection(JassIm::ImExprs));
                    va.replaceBy(
                            JassIm.ImStatementExpr(stmts,
                                    JassIm.ImTupleExpr(exprs)));
                }
            }

            @Override
            public void visit(ImFunctionCall fc) {
                super.visit(fc);
                if (fc.attrTyp() instanceof ImTupleType) {
                    Element parent = fc.getParent();
                    fc.setParent(null);

                    List<ImVar> returnVars = translator.getTupleTempReturnVarsFor(fc.getFunc());

                    ImExprs exprs = accessVars(returnVars);
                    exprs.set(0, fc);

                    ImExpr newFc = JassIm.ImTupleExpr(exprs);
                    Utils.replace(parent, fc, newFc);
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
        body.accept(new Element.DefaultVisitor() {

            @Override
            public void visit(ImStatementExpr se) {
                super.visit(se);
                if (se.getExpr() instanceof ImTupleExpr) {
                    ImTupleExpr te = (ImTupleExpr) se.getExpr();
                    ImStmts seStmts = se.getStatements();
                    seStmts.setParent(null);
                    ImExpr firstExpr = te.getExprs().get(0);
                    firstExpr.setParent(null);
                    te.getExprs().set(0, JassIm.ImStatementExpr(seStmts, firstExpr));
                    te.setParent(null);
                    se.replaceBy(te);
                }
            }
        });
    }

    /**
     * Remove tuple expressions
     * - In parameters: Just flatten
     * - Assignments: Become several assignments
     * - In Return: Use temp returns
     */
    private static void removeTupleExprs(Element elem, ImTranslator translator, ImFunction f) {
        for (int i = 0; i < elem.size(); i++) {
            Element child = elem.get(i);
            removeTupleExprs(child, translator, f);
        }
        for (int i = 0; i < elem.size(); i++) {
            Element child = elem.get(i);

            if (child instanceof ImTupleExpr) {
                ImTupleExpr tupleExpr = (ImTupleExpr) child;

                Element newElem = null;
                if (elem instanceof ImTupleSelection) {
                    newElem = inTupleSelection((ImTupleSelection) elem, tupleExpr, f);
                } else if (elem instanceof ImReturn) {
                    newElem = inReturn((ImReturn) elem, tupleExpr, translator, f);
                } else if (elem instanceof ImSet) {
                    ImSet imSet = (ImSet) elem;
                    newElem = inSet(imSet);
                } else if (elem instanceof ImExprs) {
                    ImExprs exprs = (ImExprs) elem;
                    exprs.remove(i);
                    List<ImExpr> tupleExprs = tupleExpr.getExprs().removeAll();
                    exprs.addAll(i, tupleExprs);
                    i--;
                    continue;
                } else {
                    throw new CompileError(tupleExpr.attrTrace().attrSource(), "Unhandled tuple position: " + elem.getClass().getSimpleName() + " // " + elem);
                }
                elem.replaceBy(newElem);
                // since we replaced elem we are done
                // the new element should have no more tuple expressions

                return;
            }

        }

    }

    private static ImStatementExpr inSet(ImSet imSet) {
        if (!(imSet.getLeft() instanceof ImTupleExpr && imSet.getRight() instanceof ImTupleExpr)) {
            throw new RuntimeException("invalid set statement:\n" + imSet);
        }
        ImTupleExpr left = (ImTupleExpr) imSet.getLeft();
        ImTupleExpr right = (ImTupleExpr) imSet.getRight();

        ImStmts stmts = JassIm.ImStmts();
        for (int i = 0; i < left.getExprs().size(); i++) {
            ImLExpr leftE = (ImLExpr) left.getExprs().get(i);
            ImExpr rightE = right.getExprs().get(i);
            leftE.setParent(null);
            rightE.setParent(null);
            stmts.add(JassIm.ImSet(imSet.getTrace(), leftE, rightE));
        }
        return JassIm.ImStatementExpr(stmts, JassIm.ImNull());
    }

    private static ImStatementExpr inReturn(ImReturn parent, ImTupleExpr tupleExpr, ImTranslator translator, ImFunction f) {
        List<ImVar> returnVars = translator.getTupleTempReturnVarsFor(f);
        ImStmts stmts = JassIm.ImStmts();

        for (int i = 0; i < returnVars.size(); i++) {
            ImVar rv = returnVars.get(i);
            ImExpr te = tupleExpr.getExprs().get(i);
            te.setParent(null);
            stmts.add(JassIm.ImSet(parent.getTrace(), JassIm.ImVarAccess(rv), te));
        }
        stmts.add(JassIm.ImReturn(parent.getTrace(), JassIm.ImVarAccess(returnVars.get(0))));

        return JassIm.ImStatementExpr(stmts, JassIm.ImNull());
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
                        result = te;
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


    private static ImExprs accessVars(List<ImVar> tempIndexes) {
        return tempIndexes.stream()
                .map(JassIm::ImVarAccess)
                .collect(Collectors.toCollection(JassIm::ImExprs));
    }


}
