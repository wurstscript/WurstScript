package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import de.peeeq.datastructures.ImmutableTree;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import static de.peeeq.wurstscript.jassIm.JassIm.ImVarAccess;

/**
 * a rewrite would return a combination of
 * - List of statements
 * - list of expressions
 * for expressions, returned expressions would never have a parent
 */
public class EliminateTuples {

    public static void eliminateTuplesProg(ImProg imProg, ImTranslator translator) {

        transformVars(imProg.getGlobals(), translator);
        for (ImFunction f : imProg.getFunctions()) {
            f.eliminateTuples(translator);
        }


        for (ImFunction f : imProg.getFunctions()) {
            transformStatements(f, f.getBody(), translator);
        }

        translator.assertProperties(AssertProperty.NOTUPLES);
    }


    public static void eliminateTuplesFunc(ImFunction f, final ImTranslator translator) {
        // transform parameters
        transformVars(f.getParameters(), translator);
        transformVars(f.getLocals(), translator);

        translator.setOriginalReturnValue(f, f.getReturnType());

        f.setReturnType(getFirstType(f.getReturnType()));
        f.getBody().accept(new ImStmts.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall e) {
                super.visit(e);
                // use temp return valus instead of tuples
                List<ImVar> tempVars = translator.getTupleTempReturnVarsFor(e.getFunc());
                if (tempVars.size() > 1) {
                    Element parent = e.getParent();
                    int parentIndex = -1;
                    for (int i = 0; i < parent.size(); i++) {
                        if (parent.get(i) == e) {
                            parentIndex = i;
                            parent.set(i, JassIm.ImNull()); // dummy
                            break;
                        }
                    }

                    ImExprs exprs = JassIm.ImExprs(e);
                    for (int i = 1; i < tempVars.size(); i++) {
                        exprs.add(ImVarAccess(tempVars.get(i)));
                    }
                    ImExpr newExpr = JassIm.ImTupleExpr(exprs);
                    parent.set(parentIndex, newExpr);
                }
                e.setTuplesEliminated(true);
                e.clearAttributes();
            }

        });
    }


    private static ImType getFirstType(ImType t) {
        if (t instanceof ImTupleType) {
            ImTupleType tt = (ImTupleType) t;
            return getFirstType(tt.getTypes().get(0));
        }
        return t;
    }

    private static void transformStatements(ImFunction f, ImStmts stmts,
                                            ImTranslator translator) {
        ListIterator<ImStmt> it = stmts.listIterator();
        while (it.hasNext()) {
            ImStmt s = it.next();
            ImStmt newS = s.eliminateTuples(translator, f);
            if (newS instanceof ImStatementExpr) {
                ImStatementExpr se = (ImStatementExpr) newS;
                if (se.getExpr() instanceof ImTupleExpr) {
                    ImTupleExpr t = (ImTupleExpr) se.getExpr();
                    ImStmts tupleExprs = JassIm.ImStmts();
                    tupleExprs.addAll(t.getExprs().removeAll());
                    newS = JassIm.ImStatementExpr(tupleExprs, JassIm.ImNull());
                }
            }
            if (newS != s) {
                // element changed, replace it
                it.set(newS);
            }
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

    /**
     * eliminates tuples in all subexpressions.
     * Use this for all ast-elements which are not directly related to tuple-elimination
     */
    public static <T extends Element> T eliminateTuples2(T e, ImTranslator translator, ImFunction f) {
        for (int i = 0; i < e.size(); i++) {
            Element c = e.get(i);
            Element newC = eliminateTuplesDispatch(c, translator, f);
            if (newC != c) {
                e.set(i, newC);
            }
        }
        return e;
    }

    private static Element eliminateTuplesDispatch(Element e,
                                                   ImTranslator translator, ImFunction f) {
        if (e instanceof ImExprOpt) {
            ImExprOpt imExprOpt = (ImExprOpt) e;
            return imExprOpt.eliminateTuplesExprOpt(translator, f);
        } else if (e instanceof ImStmt) {
            ImStmt stmt = (ImStmt) e;
            return stmt.eliminateTuples(translator, f);
        } else if (e instanceof ImStmts) {
            ImStmts stmts = (ImStmts) e;
            transformStatements(f, stmts, translator);
            return stmts;
        }
        return eliminateTuples2(e, translator, f);
    }


    public static ImStmt eliminateTuples(ImExitwhen e, ImTranslator translator, ImFunction f) {
        return eliminateTuples2(e, translator, f);
    }

    public static ImStmt eliminateTuples(ImIf e, ImTranslator translator, ImFunction f) {
        return eliminateTuples2(e, translator, f);
    }

    public static ImStmt eliminateTuples(ImLoop e, ImTranslator translator, ImFunction f) {
        return eliminateTuples2(e, translator, f);
    }

    public static ImStmt eliminateTuples(ImExpr e, ImTranslator translator, ImFunction f) {
        ImExpr e2 = e.eliminateTuplesExpr(translator, f);
        if (e2 instanceof ImTupleExpr) {
            ImTupleExpr te = (ImTupleExpr) e2;
            ImStmts stmts = JassIm.ImStmts();
            stmts.addAll(te.getExprs().removeAll());
            return JassIm.ImStatementExpr(stmts, JassIm.ImNull());
        }
        return e2;
    }

    public static ImStmt eliminateTuples(ImSet e, ImTranslator translator, ImFunction f) {
        Flatten.Result left = e.getLeft().eliminateTuplesExpr(translator, f).flatten(translator, f);
        ImExpr right = e.getRight().eliminateTuplesExpr(translator, f);


        e.setLeft((ImLExpr) left.expr);
        e.setRight(right);
        if (!left.stmts.isEmpty()) {
            ImStmts stmts = JassIm.ImStmts(left.stmts);
            stmts.add(e);
            return JassIm.ImStatementExpr(stmts, JassIm.ImNull());
        }
        return e;
    }

    /**
     * makes a smart copy -- only copies if e currently has no parent
     */
    private static ImExpr copyExpr(ImExpr e) {
        if (e.getParent() == null) {
            return e;
        }
        return e.copy();
    }


    /**
     * If the expression is wrapped in a tuple, only keep the single expression
     */
    private static ImExpr removeTupleWrapper(ImExpr newExpr) {
        if (newExpr instanceof ImTupleExpr) {
            ImTupleExpr te = (ImTupleExpr) newExpr;
            if (te.getExprs().size() > 1) {
                throw new Error();
            }
            newExpr = te.getExprs().remove(0);
        }
        return newExpr;
    }

    private static ImExpr elimStatementExpr(ImStmts statements, ImExpr expr,
                                            ImTranslator translator, ImFunction f) {
        while (expr instanceof ImStatementExpr) {
            ImStatementExpr right = (ImStatementExpr) expr;
            List<ImStmt> ss = right.getStatements().removeAll();
            for (ImStmt s : ss) {
                statements.add(s.eliminateTuples(translator, f));
            }
            expr = right.getExpr();
        }
        return expr;
    }


    public static ImExprOpt eliminateTuplesExpr(ImNoExpr e, ImTranslator translator, ImFunction f) {
        return e;
    }


    public static ImExpr eliminateTuplesExpr(ImTupleExpr e, ImTranslator translator, ImFunction f) {
        ListIterator<ImExpr> it = e.getExprs().listIterator();
        while (it.hasNext()) {
            ImExpr expr = it.next();
            ImExpr newExpr = expr.eliminateTuplesExpr(translator, f);
            if (newExpr instanceof ImTupleExpr) {
                ImTupleExpr te = (ImTupleExpr) newExpr;
                it.remove();
                for (ImExpr child : te.getExprs()) {
                    it.add(copyExpr(child));
                }
            } else if (newExpr != expr) {
                it.set(newExpr);
            }
        }
        if (e.getExprs().size() == 1) {
            ImExpr r = e.getExprs().get(0);
            r.setParent(null);
            return r;
        }
        return e;
    }

    public static ImExpr eliminateTuplesExpr(ImTupleSelection e, ImTranslator translator, ImFunction f) {
//		System.out.println("selecting " + e.getTupleIndex() + " from " + e.getTupleExpr());
        IntRange range;

//		WLogger.info("tuple selection = " + e);
        if (e.getTupleExpr() instanceof ImVarAccess) {
            ImVarAccess varAccess = (ImVarAccess) e.getTupleExpr();
            if (varAccess.attrTyp() instanceof ImTupleType) {
                ImTupleType tt = (ImTupleType) varAccess.attrTyp();
                range = getTupleIndexRange(tt, e.getTupleIndex());
//				System.out.println("range = " + range);
            } else {
                throw new Error("problem with " + varAccess + "\n" +
                        "has type " + varAccess.attrTyp());
            }
            ImVar v = varAccess.getVar();
            ImmutableTree<ImVar> vars = translator.getVarsForTuple(v);
            ImmutableList<ImVar> allVars = vars.allValues(); // TODO probably should not use allVars and do this differently
//			WLogger.info("is a var, selecting range " + range + " from vars " + vars);
            if (range.size() == 1) {
                return ImVarAccess(allVars.get(range.start));
            } else {
                ImExprs exprs = JassIm.ImExprs();
                for (int i : range) {
                    exprs.add(ImVarAccess(allVars.get(i)));
                }
                return JassIm.ImTupleExpr(exprs);
            }
        }

        ImExpr tupleExpr = e.getTupleExpr().eliminateTuplesExpr(translator, f);
//		System.out.println("translated: " + tupleExpr);
//		System.out.println("back to selecting " + e.getTupleIndex() + " from " + e.getTupleExpr());
        if (tupleExpr.attrTyp() instanceof ImTupleType) {
            ImTupleType tt = (ImTupleType) tupleExpr.attrTyp();
            range = getTupleIndexRange(tt, e.getTupleIndex());
//			System.out.println("tupleExpr-range: " + range);
        } else {
            if (e.getTupleIndex() == 0) {
                return tupleExpr.copy();
            }
            throw new CompileError(e.attrTrace().attrSource(), "problem with " + tupleExpr + "\n" +
                    "has type " + tupleExpr.attrTyp());
        }


        ImVar tempVar = JassIm.ImVar(e.attrTrace(), tupleExpr.attrTyp(), "tempTupleSelectionResult", false);
        ImmutableTree<ImVar> vars = translator.getVarsForTuple(tempVar);
        ImmutableList<ImVar> allVars = vars.allValues();
        f.getLocals().addAll(allVars);

        ImStmts statements = JassIm.ImStmts();
        statements.add(
                JassIm.ImSet(tupleExpr.attrTrace(), ImVarAccess(tempVar), copyExpr(tupleExpr))
                        .eliminateTuples(translator, f)
        );

        ImExpr result;
        if (range.size() == 1) {
            result = ImVarAccess(allVars.get(range.start));
        } else {
            ImExprs exprs = JassIm.ImExprs();
            for (int i : range) {
//				System.out.println("adding range ." + i);
                exprs.add(ImVarAccess(allVars.get(i)));
            }
            result = JassIm.ImTupleExpr(exprs);
        }


        return JassIm.ImStatementExpr(statements, result);
    }

    private static IntRange getTupleIndexRange(ElementWithTypes tt, int index) {
//		System.out.println("get tuple index range " + tt + " # " + index);
        int start = 0;
        for (int i = 0; i < index; i++) {
            ImType t = tt.getTypes().get(i);
            start += getTypeSize(t);
        }
        int end = start + getTypeSize(tt.getTypes().get(index));
//		System.out.println("result  = " + start + "..<" + end);
        return new IntRange(start, end);
    }

    private static int getTypeSize(ImType imType) {
        return imType.match(new ImType.Matcher<Integer>() {


            @Override
            public Integer case_ImSimpleType(ImSimpleType t) {
                return 1;
            }

            @Override
            public Integer case_ImVoid(ImVoid t) {
                return 0;
            }

            @Override
            public Integer case_ImArrayType(ImArrayType t) {
                return 1;
            }

            @Override
            public Integer case_ImTupleType(ImTupleType tt) {
                int sum = 0;
                for (ImType t : tt.getTypes()) {
                    sum += getTypeSize(t);
                }
                return sum;
            }

            @Override
            public Integer case_ImArrayTypeMulti(
                    ImArrayTypeMulti imArrayTypeMulti) {
                return 1;
            }


        });
    }

    public static ImExpr eliminateTuplesExpr(ImVarAccess e, ImTranslator translator, ImFunction f) {
        ImVar v = e.getVar();
        ImmutableTree<ImVar> varsForTuple = translator.getVarsForTuple(v);
        if (varsForTuple.size() > 1 || varsForTuple.getOnlyEment() != v) {
            return makeVarAccessToTuple(varsForTuple);
        }
        return eliminateTuples2(e, translator, f);
    }

    private static ImExpr makeVarAccessToTuple(ImmutableTree<ImVar> varsForTuple) {
        if (varsForTuple.isLeaf()) {
            return ImVarAccess(varsForTuple.getOnlyEment());
        } else {
            ImExprs exprs = JassIm.ImExprs();
            for (ImmutableTree<ImVar> t : varsForTuple) {
                exprs.add(makeVarAccessToTuple(t));
            }
            return JassIm.ImTupleExpr(exprs);
        }
    }


    public static ImExpr eliminateTuplesExpr(ImVarArrayAccess e, ImTranslator translator, ImFunction f) {
        ImVar v = e.getVar();
        ImmutableTree<ImVar> varsForTuple = translator.getVarsForTuple(v);
        if (varsForTuple.size() > 1 || varsForTuple.getOnlyEment() != v) {

            List<ImVar> tempIndexes = new ArrayList<>(e.getIndexes().size());
            ImStmts statements = JassIm.ImStmts();
            for (ImExpr ie : e.getIndexes()) {
                ImVar tempIndex = JassIm.ImVar(e.attrTrace(), ie.attrTyp(), "tempIndex", false);
                f.getLocals().add(tempIndex);
                tempIndexes.add(tempIndex);
                statements.add(JassIm.ImSet(e.attrTrace(), ImVarAccess(tempIndex), copyExpr(ie.eliminateTuplesExpr(translator, f))));
            }

            ImExpr tupleExpr = makeArrayAccessToTuple(varsForTuple, tempIndexes);
            return JassIm.ImStatementExpr(statements, tupleExpr);
        }
        return eliminateTuples2(e, translator, f);
    }

    private static ImExpr makeArrayAccessToTuple(ImmutableTree<ImVar> varsForTuple, List<ImVar> tempIndexes) {
        if (varsForTuple.isLeaf()) {
            return JassIm.ImVarArrayAccess(varsForTuple.getOnlyEment(), accessVars(tempIndexes));
        } else {
            ImExprs exprs = JassIm.ImExprs();
            for (ImmutableTree<ImVar> t : varsForTuple) {
                exprs.add(makeArrayAccessToTuple(t, tempIndexes));
            }
            return JassIm.ImTupleExpr(exprs);
        }
    }

    private static ImExprs accessVars(List<ImVar> tempIndexes) {
        return tempIndexes.stream()
                .map(JassIm::ImVarAccess)
                .collect(Collectors.toCollection(JassIm::ImExprs));
    }


    public static ImExpr eliminateTuplesExpr(ImConst e, ImTranslator translator, ImFunction f) {
        // constants cannot contain tuples and thus do not change
        return e;
    }

    public static ImExpr eliminateTuplesExpr(ImStatementExpr imStatementExpr,
                                             ImTranslator translator, ImFunction f) {
        ImStatementExpr e = eliminateTuples2(imStatementExpr, translator, f);
        if (e.getExpr() instanceof ImStatementExpr) {
            ImStatementExpr se = (ImStatementExpr) e.getExpr();
            e.getStatements().addAll(se.getStatements().removeAll());
            ImExpr see = se.getExpr();
            see.setParent(null);
            e.setExpr(see);
        }
        return e;
    }


    public static ImExpr eliminateTuplesExpr(ImFunctionCall e, ImTranslator translator, ImFunction f) {
        // eliminate tuple expressions in arguments
        eliminateTuplesInArgs(e, translator, f);
        return e;
    }

    public static ImExpr eliminateTuplesExpr(ImOperatorCall e, ImTranslator translator, ImFunction f) {
        // eliminate tuple expressions in arguments
        eliminateTuplesInArgs(e, translator, f);

        if (e.getArguments().size() > 2) {
            List<ImExpr> arguments = e.getArguments().removeAll();
            int size = arguments.size() / 2;
            WurstOperator logicOp;
            WurstOperator compareOp = e.getOp();
            if (compareOp == WurstOperator.EQ) {
                logicOp = WurstOperator.AND;
            } else if (compareOp == WurstOperator.NOTEQ) {
                logicOp = WurstOperator.OR;
            } else {
                throw new Error("unsupported tuple operator " + e);
            }
            ImExpr result = null;
            for (int i = 0; i < size; i++) {
                ImExpr left = arguments.get(i);
                ImExpr right = arguments.get(size + i);
                ImOperatorCall comparison = JassIm.ImOperatorCall(compareOp, JassIm.ImExprs(left, right));
                if (result == null) {
                    result = comparison;
                } else {
                    result = JassIm.ImOperatorCall(logicOp, JassIm.ImExprs(result, comparison));
                }
            }
            return result;
        }

        return e;
    }

    private static void eliminateTuplesInArgs(ImCall e,
                                              ImTranslator translator, ImFunction f) {
        ListIterator<ImExpr> it = e.getArguments().listIterator();
        while (it.hasNext()) {
            ImExpr arg = it.next();
            ImStmts stmts = JassIm.ImStmts();
            ImExpr newArg = arg.eliminateTuplesExpr(translator, f);
            newArg = elimStatementExpr(stmts, newArg, translator, f);
            if (newArg instanceof ImTupleExpr) {
                ImTupleExpr te = (ImTupleExpr) newArg;
                it.remove();
                int i = 0;
                for (ImExpr child : te.getExprs()) {
                    ImExpr newArg2 = copyExpr(child);
                    if (i == 0 && !stmts.isEmpty()) {
                        newArg2 = JassIm.ImStatementExpr(stmts, newArg2);
                    }
                    it.add(newArg2);
                    i++;
                }
            } else if (newArg != arg) {
                if (!stmts.isEmpty()) {
                    newArg = JassIm.ImStatementExpr(stmts, copyExpr(newArg));
                }
                it.set(copyExpr(newArg));
            }
        }
    }

    public static ImStmt eliminateTuples(ImReturn e, ImTranslator translator, ImFunction f) {
        ImExprOpt ret1 = e.getReturnValue().eliminateTuplesExprOpt(translator, f);
        if (ret1 instanceof ImNoExpr) {
            // returns nothing
            return e;
        }
        ImExpr ret = (ImExpr) ret1;
        ImStmts statements = JassIm.ImStmts();
        ImExpr retExpr = elimStatementExpr(statements, ret, translator, f);
        ImExpr result;
        if (retExpr instanceof ImTupleExpr) {
            ImTupleExpr te = (ImTupleExpr) retExpr;
            List<ImVar> tempReturnVars = translator.getTupleTempReturnVarsFor(f);
            if (tempReturnVars.size() == 1) {
                result = copyExpr(te.getExprs().get(0));
            } else {
                for (int i = 0; i < tempReturnVars.size(); i++) {
                    statements.add(JassIm.ImSet(e.getTrace(), ImVarAccess(tempReturnVars.get(i)), copyExpr(te.getExprs().get(i))));
                }
                result = ImVarAccess(tempReturnVars.get(0));
            }
        } else {
            result = copyExpr(retExpr);
        }
        statements.add(JassIm.ImReturn(e.attrTrace(), result));
        return JassIm.ImStatementExpr(statements, JassIm.ImNull());
    }


    public static ImExpr eliminateTuplesExpr(ImClassRelatedExpr e,
                                             ImTranslator translator, ImFunction f) {
        throw new RuntimeException("Must execute method elemination first.");
    }


    private static ImExprs readVariables(List<ImVar> tempIndexes) {
        return tempIndexes.stream()
                .map(JassIm::ImVarAccess)
                .collect(Collectors.toCollection(JassIm::ImExprs));
    }


    public static ImExpr eliminateTuplesExpr(ImGetStackTrace e,
                                             ImTranslator translator, ImFunction f) {
        return e;
    }


    public static ImExpr eliminateTuplesExpr(ImCompiletimeExpr e, ImTranslator translator, ImFunction f) {
        return eliminateTuples2(e, translator, f);
    }

    public static ImStmt eliminateTuples(ImVarargLoop imVarargLoop, ImTranslator translator, ImFunction f) {
        return imVarargLoop;
    }
}
