package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.jassIm.ImCompiletimeExpr;
import de.peeeq.wurstscript.jassIm.ImExitwhen;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImGetStackTrace;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImLoop;
import de.peeeq.wurstscript.jassIm.ImMemberAccess;
import de.peeeq.wurstscript.jassIm.ImOperatorCall;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImStatementExpr;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImTupleExpr;
import de.peeeq.wurstscript.jassIm.ImTupleSelection;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayAccess;
import de.peeeq.wurstscript.jassIm.ImVarargLoop;
import de.peeeq.wurstscript.translation.imtranslation.purity.Pure;
import de.peeeq.wurstscript.types.WurstTypeBool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.peeeq.wurstscript.jassIm.JassIm.*;

/**
 * flattening expressions and statements
 * after flattening there will be no more StatementExprs
 * for expressions there might be a StatementExpr on the top level
 * <p>
 * TODO wait, its not that easy: you have to make sure that the execution order is not changed for functions and global variables
 * <p>
 * e.g. take
 * <p>
 * y = x + StatementExpr(setX(4), 2)
 * <p>
 * this should be translated to:
 * <p>
 * temp = x
 * setX(4)
 * y = temp + 2
 * <p>
 * <p>
 * alternative: relax language semantics
 */
public class Flatten {


    public static Result flatten(ImTypeVarDispatch imTypeVarDispatch, ImTranslator translator, ImFunction f) {
        throw new RuntimeException("called too early");
    }

    public static Result flatten(ImCast imCast, ImTranslator translator, ImFunction f) {
        Result res = imCast.getExpr().flatten(translator, f);
        return new Result(res.stmts, ImCast(res.expr, imCast.getToType()));
    }

    public static Result flatten(ImTypeIdOfObj e, ImTranslator translator, ImFunction f) {
        Result o = e.getObj().flatten(translator, f);
        return new Result(o.stmts, ImTypeIdOfObj(o.expr, e.getClazz()));
    }

    public static Result flatten(ImTypeIdOfClass e, ImTranslator translator, ImFunction f) {
        e.setParent(null);
        return new Result(e);
    }

    public static Result flatten(ImDealloc d, ImTranslator translator, ImFunction f) {
        Result o = d.getObj().flatten(translator, f);
        return new Result(o.stmts, ImDealloc(d.getTrace(), d.getClazz(), o.expr));
    }

    public static Result flatten(ImInstanceof e, ImTranslator translator, ImFunction f) {
        Result res = e.getObj().flatten(translator, f);
        return new Result(res.stmts, ImInstanceof(res.expr, e.getClazz()));
    }

    public static Result flatten(ImAlloc e, ImTranslator translator, ImFunction f) {
        e.setParent(null);
        return new Result(e);
    }


    public static class Result {

        List<ImStmt> stmts;
        final ImExpr expr;

        public Result(List<ImStmt> stmts, ImExpr expr) {
            Preconditions.checkArgument(expr.getParent() == null, "expression must not have a parent");
            Preconditions.checkArgument(stmts.stream().allMatch(s -> s.getParent() == null), "statement must not have a parent");
            this.stmts = stmts;
            this.expr = expr;
        }

        public Result(ImExpr expr) {
            this(Collections.emptyList(), expr);
        }

        public Result(List<ImStmt> stmts) {
            this(stmts, ImHelper.nullExpr());
        }

        public void intoStatements(List<ImStmt> result, ImTranslator t, ImFunction f) {
            result.addAll(stmts);
            exprToStatements(result, expr, t, f);
        }

        public List<ImStmt> getStmts() {
            return stmts;
        }

        public ImExpr getExpr() {
            return expr;
        }

        public ImStatementExpr toStatementExpr() {
            return JassIm.ImStatementExpr(JassIm.ImStmts(stmts), expr);
        }

        public void addStmts(List<ImStmt> stmts) {
            if (stmts.isEmpty()) {
                return;
            }
            this.stmts = new ArrayList<>(this.stmts);
            this.stmts.addAll(stmts);
        }
    }

    public static class ResultL extends Result {
        public ResultL(List<ImStmt> stmts, ImLExpr expr) {
            super(stmts, expr);
        }

        public ResultL(ImLExpr expr) {
            super(expr);
        }

        @Override
        public ImLExpr getExpr() {
            return (ImLExpr) super.getExpr();
        }
    }

    public static class MultiResult {

        final List<ImStmt> stmts;
        final List<ImExpr> exprs;

        public MultiResult(List<ImStmt> stmts, List<ImExpr> exprs) {
            this.stmts = stmts;
            this.exprs = exprs;
        }


        public ImExpr expr(int i) {
            return exprs.get(i);
        }

    }

    public static class MultiResultL extends MultiResult {

        public MultiResultL(List<ImStmt> stmts, List<ImLExpr> exprs) {
            super(stmts, ImmutableList.copyOf(exprs));
        }

        public ImLExpr expr(int i) {
            return (ImLExpr) super.expr(i);
        }

        public List<ImLExpr> getLExprs() {
            //noinspection unchecked,rawtypes
            return (List) exprs;
        }

    }

    private static void exprToStatements(List<ImStmt> result, Element e, ImTranslator t, ImFunction f) {
        if (e instanceof ImFunctionCall) {
            Result res = flatten((ImFunctionCall) e, t, f);
            result.addAll(res.stmts);
            result.add(res.expr);
        } else if (e instanceof ImDealloc) {
            Result res = flatten((ImDealloc) e, t, f);
            result.addAll(res.stmts);
            result.add(res.expr);
        } else if (e instanceof ImMethodCall) {
            Result res = flatten((ImMethodCall) e, t, f);
            result.addAll(res.stmts);
            result.add(res.expr);
        } else if (e instanceof ImStatementExpr) {
            ImStatementExpr e2 = (ImStatementExpr) e;
            flattenStatementsInto(result, e2.getStatements(), t, f);
            exprToStatements(result, e2, t, f);
        } else if (e instanceof ImOperatorCall &&
                (((ImOperatorCall) e).getOp() == WurstOperator.AND
                        || ((ImOperatorCall) e).getOp() == WurstOperator.OR)) {
            // short circuiting operators have to be handled in a special way:
            // we translate them to if statements when necessary
            ImOperatorCall oc = (ImOperatorCall) e;
            ImStmts rightStmts = JassIm.ImStmts();
            ImExpr right = oc.getArguments().get(1);
            ImExpr left = oc.getArguments().get(0);
            exprToStatements(rightStmts, right, t, f);
            if (rightStmts.isEmpty()) {
                // if no statements for righthand side, then just use left hand side as result
                exprToStatements(result, left, t, f);
            } else {
                // if righthandside contains some statements, transform the whole thing to an if statement:
                if (oc.getOp() == WurstOperator.AND) {
                    result.add(JassIm.ImIf(e.attrTrace(), left.copy(), rightStmts, JassIm.ImStmts()));
                } else { // WurstOperator.OR
                    result.add(JassIm.ImIf(e.attrTrace(), left.copy(), JassIm.ImStmts(), rightStmts));
                }
            }
        } else {
            // visit children:
            for (int i = 0; i < e.size(); i++) {
                exprToStatements(result, e.get(i), t, f);
            }
        }
    }


    private static ImStmts flattenStatements(ImStmts statements, ImTranslator t, ImFunction f) {
        ImStmts result = ImStmts();
        flattenStatementsInto(result, statements, t, f);
        return result;
    }

    private static void flattenStatementsInto(List<ImStmt> result, ImStmts statements, ImTranslator t, ImFunction f) {
        for (ImStmt s : statements) {
            s.flatten(t, f).intoStatements(result, t, f);
        }
    }

    public static Result flatten(ImExitwhen s, ImTranslator t, ImFunction f) {
        Result cond = s.getCondition().flatten(t, f);
        List<ImStmt> stmts = Lists.newArrayList(cond.stmts);
        stmts.add(ImExitwhen(s.getTrace(), cond.expr));
        return new Result(stmts);
    }


    public static Result flatten(ImIf s, ImTranslator t, ImFunction f) {
        Result cond = s.getCondition().flatten(t, f);
        List<ImStmt> stmts = Lists.newArrayList(cond.stmts);
        stmts.add(
                JassIm.ImIf(s.getTrace(), cond.expr,
                        flattenStatements(s.getThenBlock(), t, f),
                        flattenStatements(s.getElseBlock(), t, f)));
        return new Result(stmts);
    }


    public static Result flatten(ImLoop s, ImTranslator t, ImFunction f) {
        return new Result(Collections.singletonList(
                JassIm.ImLoop(s.getTrace(), flattenStatements(s.getBody(), t, f))));
    }

    public static Result flatten(ImReturn s, ImTranslator t, ImFunction f) {
        if (s.getReturnValue() instanceof ImExpr) {
            ImExpr ret = (ImExpr) s.getReturnValue();
            Result result = ret.flatten(t, f);
            List<ImStmt> stmts = Lists.newArrayList(result.stmts);
            stmts.add(ImReturn(s.getTrace(), result.expr));
            return new Result(stmts);
        } else {
            s.setParent(null);
            return new Result(Collections.singletonList(s));
        }
    }


    public static Result flatten(ImSet s, ImTranslator t, ImFunction f) {
        Result l = s.getLeft().flatten(t, f);
        Result r = s.getRight().flatten(t, f);
        List<ImStmt> stmts = Lists.newArrayList(l.stmts);
        stmts.addAll(r.stmts);
        stmts.add(JassIm.ImSet(s.getTrace(), (ImLExpr) l.expr, r.expr));
        return new Result(stmts);
    }


    public static Result flatten(ImFunctionCall e, ImTranslator t, ImFunction f) {
        MultiResult r = flattenExprs(t, f, e.getArguments());
        return new Result(r.stmts, ImFunctionCall(e.getTrace(), e.getFunc(), e.getTypeArguments().copy(), ImExprs(r.exprs), e.getTuplesEliminated(), e.getCallType()));
    }

    public static Result flatten(ImMethodCall e, ImTranslator t, ImFunction f) {
        Result recR = e.getReceiver().flatten(t, f);
        MultiResult argsR = flattenExprs(t, f, e.getArguments());
        Result res = new Result(recR.stmts, ImMethodCall(e.getTrace(), e.getMethod(), e.getTypeArguments().copy(), recR.expr, ImExprs(argsR.exprs), e.getTuplesEliminated()));
        res.addStmts(argsR.stmts);
        return res;
    }

    public static Result flatten(ImOperatorCall e, ImTranslator t, ImFunction f) {
        // TODO special case and, or
        de.peeeq.wurstscript.ast.Element trace = e.attrTrace();
        switch (e.getOp()) {
            case AND: {
                Result left = e.getArguments().get(0).flatten(t, f);
                Result right = e.getArguments().get(1).flatten(t, f);

                if (right.stmts.isEmpty()) {
                    return new Result(left.stmts, JassIm.ImOperatorCall(WurstOperator.AND, ImExprs(left.expr, right.expr)));
                } else {
                    ArrayList<ImStmt> stmts = Lists.newArrayList(left.stmts);
                    ImVar tempVar = JassIm.ImVar(e.attrTrace(), WurstTypeBool.instance().imTranslateType(t), "andLeft", false);
                    f.getLocals().add(tempVar);
                    ImStmts thenBlock = JassIm.ImStmts();
                    // if left is true then check right
                    thenBlock.addAll(right.stmts);
                    thenBlock.add(ImSet(trace, ImVarAccess(tempVar), right.expr));
                    // else the result is false
                    ImStmts elseBlock = JassIm.ImStmts(ImSet(trace, ImVarAccess(tempVar), JassIm.ImBoolVal(false)));
                    stmts.add(ImIf(trace, left.expr, thenBlock, elseBlock));
                    return new Result(stmts, JassIm.ImVarAccess(tempVar));
                }
            }
            case OR: {
                Result left = e.getArguments().get(0).flatten(t, f);
                Result right = e.getArguments().get(1).flatten(t, f);

                if (right.stmts.isEmpty()) {
                    return new Result(left.stmts, JassIm.ImOperatorCall(WurstOperator.OR, ImExprs(left.expr, right.expr)));
                } else {
                    ArrayList<ImStmt> stmts = Lists.newArrayList(left.stmts);
                    ImVar tempVar = JassIm.ImVar(trace, WurstTypeBool.instance().imTranslateType(t), "andLeft", false);
                    f.getLocals().add(tempVar);
                    // if left is true then result is ture
                    ImStmts thenBlock = JassIm.ImStmts(ImSet(trace, ImVarAccess(tempVar), JassIm.ImBoolVal(true)));
                    // else check right
                    ImStmts elseBlock = JassIm.ImStmts();
                    elseBlock.addAll(right.stmts);
                    elseBlock.add(ImSet(trace, ImVarAccess(tempVar), right.expr));
                    stmts.add(ImIf(trace, left.expr, thenBlock, elseBlock));
                    return new Result(stmts, JassIm.ImVarAccess(tempVar));
                }
            }
            default:
                MultiResult r = flattenExprs(t, f, e.getArguments());
                return new Result(r.stmts, JassIm.ImOperatorCall(e.getOp(), ImExprs(r.exprs)));
        }
    }


    public static Result flatten(ImConst e, ImTranslator t, ImFunction f) {
        e.setParent(null);
        return new Result(e);
    }


    public static Result flatten(ImStatementExpr e, ImTranslator t, ImFunction f) {
        List<ImStmt> stmts = Lists.newArrayList();
        flattenStatementsInto(stmts, e.getStatements(), t, f);
        Result r = e.getExpr().flatten(t, f);
        stmts.addAll(r.stmts);
        return new Result(stmts, r.expr);
    }

    public static ResultL flattenL(ImStatementExpr e, ImTranslator t, ImFunction f) {
        List<ImStmt> stmts = Lists.newArrayList();
        flattenStatementsInto(stmts, e.getStatements(), t, f);
        ResultL r = ((ImLExpr) e.getExpr()).flattenL(t, f);
        stmts.addAll(r.stmts);
        return new ResultL(stmts, r.getExpr());
    }


    public static Result flatten(ImTupleExpr e, ImTranslator t, ImFunction f) {
        MultiResult r = flattenExprs(t, f, e.getExprs());
        return new Result(r.stmts, JassIm.ImTupleExpr(ImExprs(r.exprs)));
    }

    public static ResultL flattenL(ImTupleExpr e, ImTranslator t, ImFunction f) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        List<ImLExpr> exprs = (List) e.getExprs();
        MultiResultL r = flattenExprsL(t, f, exprs);
        ImExprs newExprs = ImExprs();
        newExprs.addAll(r.getLExprs());
        return new ResultL(r.stmts, JassIm.ImTupleExpr(newExprs));
    }


    public static Result flatten(ImTupleSelection e, ImTranslator t, ImFunction f) {
        return flattenL(e, t, f);
    }

    public static ResultL flattenL(ImTupleSelection e, ImTranslator t, ImFunction f) {
        Result r = e.getTupleExpr().flatten(t, f);
        ImLExpr tupleExpr;
        List<ImStmt> stmts;
        if (r.expr instanceof ImLExpr) {
            tupleExpr = (ImLExpr) r.expr;
            stmts = r.stmts;
        } else {
            // in the unlikely event that this is not an l-value (e.g. foo().x)
            // we create a temporary variable and store the result there
            ImVar v = JassIm.ImVar(e.attrTrace(), r.expr.attrTyp(), "tuple_temp", false);
            f.getLocals().add(v);
            stmts = new ArrayList<>(r.stmts);
            stmts.add(JassIm.ImSet(e.attrTrace(), ImVarAccess(v), r.expr));
            tupleExpr = JassIm.ImVarAccess(v);
        }
        return new ResultL(stmts, JassIm.ImTupleSelection(tupleExpr, e.getTupleIndex()));
    }

    public static ResultL flattenL(ImVarAccess e, ImTranslator t, ImFunction f) {
        e.setParent(null);
        return new ResultL(e);
    }


    public static ResultL flatten(ImLExpr e, ImTranslator t, ImFunction f) {
        return e.flattenL(t, f);
    }

    public static ResultL flattenL(ImVarArrayAccess e, ImTranslator t, ImFunction f) {
        MultiResult indexes = flattenExprs(t, f, e.getIndexes());
        return new ResultL(indexes.stmts, ImVarArrayAccess(e.getTrace(), e.getVar(), ImExprs(indexes.exprs)));
    }


    public static void flattenFunc(ImFunction f, ImTranslator translator) {
        ImStmts newBody = flattenStatements(f.getBody(), translator, f);
        f.setBody(newBody);
    }

    public static void flattenProg(ImProg imProg, ImTranslator translator) {
        for (ImFunction f : imProg.getFunctions()) {
            f.flatten(translator);
        }
        for (ImClass c : imProg.getClasses()) {
            for (ImFunction f : c.getFunctions()) {
                f.flatten(translator);
            }
        }

        translator.assertProperties(AssertProperty.FLAT);
    }

    public static Result flatten(ImCompiletimeExpr e, ImTranslator translator, ImFunction f) {
        e.setParent(null);
        return new Result(e);
    }


    private static MultiResult flattenExprs(ImTranslator t, ImFunction f, ImExpr... exprs) {
        return flattenExprs(t, f, Arrays.asList(exprs));
    }

    private static MultiResult flattenExprs(ImTranslator t, ImFunction f, List<ImExpr> exprs) {
        // TODO optimize this function to use less temporary variables
        List<ImStmt> stmts = Lists.newArrayList();
        List<ImExpr> newExprs = Lists.newArrayList();
        List<Result> results = Lists.newArrayList();
        int withStmts = -1;
        for (int i = 0; i < exprs.size(); i++) {
            Result r = exprs.get(i).flatten(t, f);
            results.add(r);
            if (!r.stmts.isEmpty()) {
                withStmts = i;
            }
        }
        for (int i = 0; i < exprs.size(); i++) {
            ImExpr e = exprs.get(i);
            Result r = results.get(i);

            stmts.addAll(r.stmts);
            if (r.expr.attrPurity() instanceof Pure
                    || i >= withStmts) {
                newExprs.add(r.expr);
            } else {
                ImVar tempVar = JassIm.ImVar(e.attrTrace(), r.expr.attrTyp(), "temp", false);
                f.getLocals().add(tempVar);
                stmts.add(ImSet(e.attrTrace(), ImVarAccess(tempVar), r.expr));
                newExprs.add(JassIm.ImVarAccess(tempVar));
            }
        }
        return new MultiResult(stmts, newExprs);
    }

    private static MultiResultL flattenExprsL(ImTranslator t, ImFunction f, List<ImLExpr> exprs) {
        // TODO optimize this function to use less temporary variables
        List<ImStmt> stmts = Lists.newArrayList();
        List<ImLExpr> newExprs = Lists.newArrayList();
        List<ResultL> results = Lists.newArrayList();
        int withStmts = -1;
        for (int i = 0; i < exprs.size(); i++) {
            ResultL r = exprs.get(i).flattenL(t, f);
            results.add(r);
            if (!r.stmts.isEmpty()) {
                withStmts = i;
            }
        }
        for (int i = 0; i < exprs.size(); i++) {
            ImExpr e = exprs.get(i);
            ResultL r = results.get(i);

            stmts.addAll(r.stmts);
            if (r.expr.attrPurity() instanceof Pure
                    || i >= withStmts) {
                newExprs.add(r.getExpr());
            } else {
                ImVar tempVar = JassIm.ImVar(e.attrTrace(), r.expr.attrTyp(), "temp", false);
                f.getLocals().add(tempVar);
                stmts.add(ImSet(e.attrTrace(), ImVarAccess(tempVar), r.expr));
                newExprs.add(JassIm.ImVarAccess(tempVar));
            }
        }
        return new MultiResultL(stmts, newExprs);
    }


    public static Result flatten(ImMemberAccess e,
                                 ImTranslator t, ImFunction f) {
        Result rr = e.getReceiver().flatten(t, f);
        MultiResult ir = flattenExprs(t, f, e.getIndexes());
        Result res = new Result(rr.stmts, ImMemberAccess(e.getTrace(), rr.expr, e.getTypeArguments().copy(), e.getVar(), ImExprs(ir.exprs)));
        res.addStmts(ir.stmts);
        return res;
    }

    public static ResultL flattenL(ImMemberAccess e,
                                   ImTranslator t, ImFunction f) {
        Result rr = e.getReceiver().flatten(t, f);
        MultiResult ir = flattenExprs(t, f, e.getIndexes());
        ResultL res = new ResultL(rr.stmts, ImMemberAccess(e.getTrace(), rr.expr, e.getTypeArguments(), e.getVar(), ImExprs(ir.exprs)));
        res.addStmts(ir.stmts);
        return res;
    }


    public static Result flatten(ImGetStackTrace e, ImTranslator translator,
                                 ImFunction f) {
        e.setParent(null);
        return new Result(e);
    }


    public static Result flatten(ImVarargLoop s, ImTranslator translator, ImFunction f) {
        return new Result(Collections.singletonList(
                JassIm.ImVarargLoop(s.getTrace(), flattenStatements(s.getBody(), translator, f), s.getLoopVar())));
    }



}
