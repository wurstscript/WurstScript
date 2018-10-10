package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.base.Preconditions;
import de.peeeq.datastructures.ImmutableTree;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.NoExpr;
import de.peeeq.wurstscript.intermediatelang.optimizer.SideEffectAnalyzer;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.*;
import java.util.stream.Collectors;

import static de.peeeq.wurstscript.jassIm.JassIm.ImVarAccess;

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

    1. replace tuples with tuple-expression

    - Variable access
        a --> <a_1, a_2, a_3>
    - Function calls
        f() --> <f(), temp_return1, temp_return2>
    - Tuple selections
        <e_1, e_2, e_3>.2 --> {e_1; temp = e_2; e_3 >> temp}
        <e_1, e_2, e_3>.3 --> {e_1; e_2 >> e_3}
    - ...

    2. Normalize Tuples in statement-expressions (move to first tuple param)

        Normalize
        {stmts >> <e1,e2,e3>}
        becomes <{stmts >> e1}, e2, e3}


    3. Remove tuple expressions


      - In parameters: Just flatten
      - Assignments: Become several assignments
      -

     */

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


    public static ImStmt eliminateTuples(ImExpr e, ImTranslator translator, ImFunction f) {
        Result res = e.eliminateTuplesExpr(translator, f);
        return res.toExpr();
    }


    public static ImStmt eliminateTuples(ImReturn s, ImTranslator translator, ImFunction f) {
        if (s.getReturnValue() instanceof NoExpr) {
            return s;
        }
        ImExpr returnValue = (ImExpr) s.getReturnValue();
        Result res = returnValue.eliminateTuplesExpr(translator, f);
        if (res.exprCount() == 1) {
            s.setReturnValue(res.toExpr());
            return s;
        }

        // otherwise, if we have more than one expression use temp-return values
        List<ImVar> tempReturnVars = translator.getTupleTempReturnVarsFor(f);
        ImStmts statements = JassIm.ImStmts(res.stmts);
        for (int i = 0; i < tempReturnVars.size(); i++) {
            statements.add(JassIm.ImSet(s.getTrace(), ImVarAccess(tempReturnVars.get(i)), copyExpr(res.get(i))));
        }
        return JassIm.ImStatementExpr(
                statements,
                JassIm.ImVarAccess(tempReturnVars.get(0))
        );
    }


    private static ImExpr copyExpr(ImExpr e) {
        if (e.getParent() == null) {
            return e;
        }
        return e.copy();
    }

    public static Result eliminateTuplesExpr(ImAlloc e, ImTranslator translator, ImFunction f) {
        return new Result(e);
    }

    public static ImStmt eliminateTuples(ImIf s, ImTranslator translator, ImFunction f) {
        return eliminateTuples2(s, translator, f);
    }

    public static ImStmt eliminateTuples(ImExitwhen e, ImTranslator translator, ImFunction f) {
        return eliminateTuples2(e, translator, f);
    }


    public static ImStmt eliminateTuples(ImLoop e, ImTranslator translator, ImFunction f) {
        return eliminateTuples2(e, translator, f);
    }

    public static Result eliminateTuplesExpr(ImConst e, ImTranslator translator, ImFunction f) {
        e.setParent(null);
        // constants cannot contain tuples and thus do not change
        return new Result(e);
    }

    public static Result eliminateTuplesExpr(ImClassRelatedExpr e, ImTranslator translator, ImFunction f) {
        throw new RuntimeException("Must execute method elemination first.");
    }

    public static Result eliminateTuplesExpr(ImMemberAccess e, ImTranslator translator, ImFunction f) {
        throw new RuntimeException("Must execute method elemination first.");
    }

    public static ResultL eliminateTuplesExprL(ImMemberAccess e, ImTranslator translator, ImFunction f) {
        throw new RuntimeException("Must execute method elemination first.");
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

    public static Result eliminateTuples2Result(ImExpr e, ImTranslator translator, ImFunction f) {
        for (int i = 0; i < e.size(); i++) {
            Element c = e.get(i);
            Element newC = eliminateTuplesDispatch(c, translator, f);
            if (newC != c) {
                e.set(i, newC);
            }
        }
        return new Result(e);
    }

    private static Element eliminateTuplesDispatch(Element e, ImTranslator translator, ImFunction f) {
        if (e instanceof ImExprOpt) {
            ImExprOpt imExprOpt = (ImExprOpt) e;
            return imExprOpt.eliminateTuplesExprOpt(translator, f).toExpr();
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

    public static Result eliminateTuplesExpr(ImTupleExpr te, ImTranslator translator, ImFunction f) {
        List<ImExpr> exprs = new ArrayList<>();
        for (ImExpr e : te.getExprs()) {
            Result er = e.eliminateTuplesExpr(translator, f);
            er.addToExprs(exprs);
        }
        return new Result(Collections.emptyList(), exprs);
    }

    public static ResultL eliminateTuplesExprL(ImTupleLExpr te, ImTranslator translator, ImFunction f) {
        List<ImLExpr> exprs = new ArrayList<>();
        List<ImStmt> stmts = new ArrayList<>();
        for (ImLExpr e : te.getLexprs()) {
            ResultL er = e.eliminateTuplesExprL(translator, f);
            stmts.addAll(er.getStmts());
            exprs.addAll(er.getLExprs());
        }
        return new ResultL(stmts, exprs);
    }

    public static Result eliminateTuplesExpr(ImCompiletimeExpr e, ImTranslator translator, ImFunction f) {
        return eliminateTuples2Result(e, translator, f);
    }

    public static ResultL eliminateTuplesExprL(ImVarArrayAccess v, ImTranslator translator, ImFunction f) {
        List<ImStmt> stmts = new ArrayList<>();
        for (int i = 0; i < v.getIndexes().size(); i++) {
            ImExpr index = v.getIndexes().get(i);
            Result ir = index.eliminateTuplesExpr(translator, f);
            stmts.addAll(ir.stmts);
            v.getIndexes().set(i, ir.getOnlyExpr());
        }
        ImmutableTree<ImVar> vars = translator.getVarsForTuple(v.getVar());
        if (vars.isLeaf()) {
            v.setParent(null);
            return new ResultL(stmts, Collections.singletonList(v));
        } else {
            // store indexes to temporary variables:
            List<ImVar> indexVars = new ArrayList<>();
            for (ImExpr index : v.getIndexes()) {
                index.setParent(null);
                ImVar tempIndex = JassIm.ImVar(v.attrTrace(), index.attrTyp(), "temp_index", false);
                indexVars.add(tempIndex);
                stmts.add(JassIm.ImSet(v.attrTrace(), JassIm.ImVarAccess(tempIndex), index));
            }
            f.getLocals().addAll(indexVars);

            List<ImLExpr> exprs = new ArrayList<>();
            for (ImmutableTree<ImVar> var : vars) {
                ImExprs indexes = indexVars.stream()
                        .map(JassIm::ImVarAccess)
                        .collect(Collectors.toCollection(JassIm::ImExprs));
                exprs.add(JassIm.ImVarArrayAccess(var.getOnlyEment(), indexes));
            }
            return new ResultL(stmts, exprs);
        }
    }

    public static ResultL eliminateTuplesExprL(ImTupleSelection e, ImTranslator translator, ImFunction f) {
        // TODO does this translate nested tuples correctly?
        ResultL resultL = e.getTupleExpr().eliminateTuplesExprL(translator, f);
        return new ResultL(resultL.getStmts(), Collections.singletonList(resultL.get(e.getTupleIndex())));
    }

    public static Result eliminateTuplesExpr(ImFunctionCall e, ImTranslator translator, ImFunction f) {
        ImFunction calledFunc = e.getFunc();
        List<ImStmt> stmts = new ArrayList<>();
        ListIterator<ImExpr> it = e.getArguments().listIterator();
        while (it.hasNext()) {
            ImExpr arg = it.next();
            it.remove();
            Result argR = arg.eliminateTuplesExpr(translator, f);
            stmts.addAll(argR.stmts);
            argR.getExprs().forEach(it::add);
        }
        List<ImVar> tupleReturnVars = translator.getTupleTempReturnVarsFor(calledFunc);
        e.setParent(null);
        if (tupleReturnVars.isEmpty()) {
            return new Result(stmts, Collections.singletonList(e));
        } else {
            List<ImExpr> exprs = new ArrayList<>();
            exprs.add(e);
            for (int i = 1; i < tupleReturnVars.size(); i++) {
                exprs.add(JassIm.ImVarAccess(tupleReturnVars.get(i)));
            }
            return new Result(stmts, exprs);
        }
    }

    public static Result eliminateTuplesExpr(ImNoExpr imNoExpr, ImTranslator translator, ImFunction f) {
        return new Result(Collections.emptyList(), Collections.emptyList());
    }

    public static Result eliminateTuplesExpr(ImStatementExpr se, ImTranslator translator, ImFunction f) {
        List<ImStmt> stmts = new ArrayList<>();
        for (ImStmt s : se.getStatements()) {
            ImStmt sr = s.eliminateTuples(translator, f);
            stmts.add(sr);
        }
        Result r = se.getExpr().eliminateTuplesExpr(translator, f);
        stmts.addAll(r.stmts);
        return new Result(stmts, r.exprs);
    }

    public static ImStmt eliminateTuples(ImVarargLoop loop, ImTranslator translator, ImFunction f) {
        return eliminateTuples2(loop, translator, f);
    }

    public static ImStmt eliminateTuples(ImSet set, ImTranslator translator, ImFunction f) {
        ResultL resultL = set.getLeft().eliminateTuplesExprL(translator, f);
        Result resultR = set.getRight().eliminateTuplesExpr(translator, f);
        List<ImStmt> stmts = new ArrayList<>();
        stmts.addAll(resultL.getStmts());
        stmts.addAll(resultR.getStmts());
        for (int i = 0; i < resultL.exprCount(); i++) {
            ImLExpr l = resultL.get(i);
            ImExpr r = resultR.get(i);
            stmts.add(JassIm.ImSet(set.getTrace(), l , r));
        }
        if (stmts.size() == 1) {
            return stmts.get(0);
        } else {
            return JassIm.ImStatementExpr(
                    JassIm.ImStmts(stmts),
                    JassIm.ImNull()
            );
        }
    }



    public static class Result {

        private final List<ImStmt> stmts;
        private final List<ImExpr> exprs;

        public Result(List<ImStmt> stmts, List<ImExpr> exprs) {
            Preconditions.checkNotNull(stmts);
            Preconditions.checkNotNull(exprs);
            for (ImExpr expr : exprs) {
                Preconditions.checkArgument(expr.getParent() == null, "expression parent must be null");
                Preconditions.checkArgument(!SideEffectAnalyzer.quickcheckHasSideeffects(expr), "expression must have no side effects", expr);
            }
            this.stmts = stmts;
            this.exprs = exprs;
        }

        public Result(ImExpr epxr) {
            this(Collections.emptyList(), Collections.singletonList(epxr));
        }

        public Result(List<ImStmt> stmts) {
            this.stmts = stmts;
            this.exprs = Collections.emptyList();
        }


        public List<ImStmt> getStmts() {
            return stmts;
        }

        public List<ImExpr> getExprs() {
            return exprs;
        }

        public ImExpr get(int i) {
            return exprs.get(i);
        }

        public int exprCount() {
            return exprs.size();
        }

        public ImExpr toExpr() {
            Preconditions.checkArgument(exprs.size() == 1);
            if (stmts.isEmpty()) {
                return get(0);
            }
            return JassIm.ImStatementExpr(JassIm.ImStmts(stmts), get(0));
        }

        public void addToExprs(List<ImExpr> list) {
            for (int i = 0; i < exprs.size(); i++) {
                ImExpr e = exprs.get(i);
                if (i == 0 && !stmts.isEmpty()) {
                    e.setParent(null);
                    e = JassIm.ImStatementExpr(
                            JassIm.ImStmts(stmts),
                            e
                    );
                }
                list.add(e);
            }
        }

        public ImExpr getOnlyExpr() {
            Preconditions.checkArgument(exprs.size() == 1);
            return exprs.get(0);
        }
    }

    public static class ResultL extends Result {
        @SuppressWarnings({"unchecked", "rawtypes"})
        public ResultL(List<ImStmt> stmts, List<ImLExpr> exprs) {
            super(stmts, (List) exprs);
        }

        public ResultL(ImLExpr expr) {
            this(Collections.emptyList(), Collections.singletonList(expr));
        }

        @Override
        public ImLExpr get(int i) {
            return (ImLExpr) getExprs().get(i);
        }

        public List<ImLExpr> getLExprs() {
            //noinspection unchecked,rawtypes
            return (List) getExprs();
        }
    }

    public static Result eliminateTuplesExpr(ImOperatorCall e, ImTranslator translator, ImFunction f) {
        // eliminate tuple expressions in arguments
        eliminateTuplesInArgs(e, translator, f);

        if (e.getArguments().size() > 2) {
            // if we get more than 2 arguments, we are comparing tuples
            // e.g. (a_x,a_y) == (b_x,b_y) or !=
            List<ImExpr> arguments = e.getArguments().removeAll();
            // how many components to the tuples have?
            int size = arguments.size() / 2;
            // for == we generate: a_x == b_x and a_y == b_y
            // for != we generate: a_x != b_x or a_y != b_y
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
            return new Result(result);
        }

        return new Result(e);
    }

    private static void eliminateTuplesInArgs(ImCall e, ImTranslator translator, ImFunction f) {
        ListIterator<ImExpr> it = e.getArguments().listIterator();
        while (it.hasNext()) {
            ImExpr arg = it.next();
            Result newArg = arg.eliminateTuplesExpr(translator, f);
            if (newArg.exprCount() <= 1) {
                it.set(newArg.toExpr());
            } else {
                it.remove();
                it.add(JassIm.ImStatementExpr(
                        JassIm.ImStmts(newArg.stmts),
                        newArg.get(0)
                ));
                for (int i = 1; i < newArg.exprCount(); i++) {
                    it.add(newArg.get(i));
                }
            }
        }
    }

    public static Result eliminateTuplesExpr(ImGetStackTrace e, ImTranslator translator, ImFunction f) {
        return new Result(e);
    }

    public static Result eliminateTuplesExpr(ImLExpr e, ImTranslator translator, ImFunction f) {
        return e.eliminateTuplesExprL(translator, f);
    }

    public static ResultL eliminateTuplesExprL(ImVarAccess e, ImTranslator translator, ImFunction f) {
        ImVar v = e.getVar();
        ImmutableTree<ImVar> varsForTuple = translator.getVarsForTuple(v);
        if (varsForTuple.size() > 1 || varsForTuple.getOnlyEment() != v) {
            return makeVarAccessToTuple(varsForTuple);
        }
        return new ResultL(e);
    }

    private static ResultL makeVarAccessToTuple(ImmutableTree<ImVar> varsForTuple) {
        if (varsForTuple.isLeaf()) {
            return new ResultL(ImVarAccess(varsForTuple.getOnlyEment()));
        } else {
            List<ImLExpr> res = varsForTuple.allValues().stream()
                    .map(JassIm::ImVarAccess)
                    .collect(Collectors.toList());
            return new ResultL(Collections.emptyList(), res);
        }
    }
}
