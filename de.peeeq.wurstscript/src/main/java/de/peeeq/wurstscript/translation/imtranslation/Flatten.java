package de.peeeq.wurstscript.translation.imtranslation;

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
import de.peeeq.wurstscript.jassIm.ImOperatorCall;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImSetArrayMulti;
import de.peeeq.wurstscript.jassIm.ImSetArrayTuple;
import de.peeeq.wurstscript.jassIm.ImSetTuple;
import de.peeeq.wurstscript.jassIm.ImStatementExpr;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImTupleExpr;
import de.peeeq.wurstscript.jassIm.ImTupleSelection;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayMultiAccess;
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


    public static class Result {

        final List<ImStmt> stmts;
        final ImExpr expr;

        public Result(List<ImStmt> stmts, ImExpr epxr) {
            this.stmts = stmts;
            this.expr = epxr;
        }

        public Result(ImExpr epxr) {
            this.stmts = Collections.emptyList();
            this.expr = epxr;
        }

        public Result(List<ImStmt> stmts) {
            this.stmts = stmts;
            this.expr = JassIm.ImNull();
        }

        public void intoStatements(List<ImStmt> result, ImTranslator t, ImFunction f) {
            result.addAll(stmts);
            exprToStatements(result, expr, t, f);
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

    private static void exprToStatements(List<ImStmt> result, Element e, ImTranslator t, ImFunction f) {
        if (e instanceof ImFunctionCall) {
            result.add(((ImStmt) e).copy());
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
        Result e = s.getRight().flatten(t, f);
        List<ImStmt> stmts = Lists.newArrayList(e.stmts);
        stmts.add(JassIm.ImSet(s.getTrace(), s.getLeft(), e.expr));
        return new Result(stmts);
    }


    public static Result flatten(ImSetTuple s, ImTranslator t, ImFunction f) {
        Result e = s.getRight().flatten(t, f);
        List<ImStmt> stmts = Lists.newArrayList(e.stmts);
        stmts.add(ImSetTuple(s.getTrace(), s.getLeft(), s.getTupleIndex(), e.expr));
        return new Result(stmts);
    }

    public static Result flatten(ImSetArray s, ImTranslator t, ImFunction f) {
        MultiResult res = flattenExprs(t, f, s.getIndex(), s.getRight());
        res.stmts.add(JassIm.ImSetArray(s.getTrace(), s.getLeft(), res.expr(0), res.expr(1)));
        return new Result(res.stmts);
    }

    public static Result flatten(ImSetArrayTuple s, ImTranslator t, ImFunction f) {
        MultiResult res = flattenExprs(t, f, s.getIndex(), s.getRight());
        res.stmts.add(JassIm.ImSetArrayTuple(s.getTrace(), s.getLeft(), res.expr(0), s.getTupleIndex(), res.expr(1)));
        return new Result(res.stmts);
    }


    public static Result flatten(ImFunctionCall e, ImTranslator t, ImFunction f) {
        MultiResult r = flattenExprs(t, f, e.getArguments());
        return new Result(r.stmts, JassIm.ImFunctionCall(e.getTrace(), e.getFunc(), ImExprs(r.exprs), e.getTuplesEliminated(), e.getCallType()));
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
                    ImVar tempVar = JassIm.ImVar(e.attrTrace(), WurstTypeBool.instance().imTranslateType(), "andLeft", false);
                    f.getLocals().add(tempVar);
                    ImStmts thenBlock = JassIm.ImStmts();
                    // if left is true then check right
                    thenBlock.addAll(right.stmts);
                    thenBlock.add(JassIm.ImSet(trace, tempVar, right.expr));
                    // else the result is false
                    ImStmts elseBlock = JassIm.ImStmts(JassIm.ImSet(trace, tempVar, JassIm.ImBoolVal(false)));
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
                    ImVar tempVar = JassIm.ImVar(trace, WurstTypeBool.instance().imTranslateType(), "andLeft", false);
                    f.getLocals().add(tempVar);
                    // if left is true then result is ture
                    ImStmts thenBlock = JassIm.ImStmts(JassIm.ImSet(trace, tempVar, JassIm.ImBoolVal(true)));
                    // else check right
                    ImStmts elseBlock = JassIm.ImStmts();
                    elseBlock.addAll(right.stmts);
                    elseBlock.add(JassIm.ImSet(trace, tempVar, right.expr));
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


    public static Result flatten(ImTupleExpr e, ImTranslator t, ImFunction f) {
        MultiResult r = flattenExprs(t, f, e.getExprs());
        return new Result(r.stmts, JassIm.ImTupleExpr(ImExprs(r.exprs)));
    }


    public static Result flatten(ImTupleSelection e, ImTranslator t, ImFunction f) {
        Result r = e.getTupleExpr().flatten(t, f);
        return new Result(r.stmts, JassIm.ImTupleSelection(r.expr, e.getTupleIndex()));
    }


    public static Result flatten(ImVarAccess e, ImTranslator t, ImFunction f) {
        e.setParent(null);
        return new Result(e);
    }


    public static Result flatten(ImVarArrayAccess e, ImTranslator t, ImFunction f) {
        Result index = e.getIndex().flatten(t, f);
        return new Result(index.stmts, ImVarArrayAccess(e.getVar(), index.expr));
    }


    public static void flattenFunc(ImFunction f, ImTranslator translator) {
        ImStmts newBody = flattenStatements(f.getBody(), translator, f);
        f.setBody(newBody);
    }

    public static void flattenProg(ImProg imProg, ImTranslator translator) {
        for (ImFunction f : imProg.getFunctions()) {
            f.flatten(translator);
        }
        translator.assertProperties(AssertProperty.FLAT);
    }

    public static Result flatten(ImCompiletimeExpr e, ImTranslator translator, ImFunction f) {
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
                stmts.add(JassIm.ImSet(e.attrTrace(), tempVar, r.expr));
                newExprs.add(JassIm.ImVarAccess(tempVar));
            }
        }
        return new MultiResult(stmts, newExprs);
    }


    public static Result flatten(ImClassRelatedExpr e,
                                 ImTranslator translator, ImFunction f) {
        throw new RuntimeException("Eliminate method calls before calling flatten.");
    }


    public static Result flatten(ImSetArrayMulti imSetArrayMulti,
                                 ImTranslator translator, ImFunction f) {
        throw new Error("not implemented");
    }


    public static Result flatten(ImVarArrayMultiAccess imVarArrayMultiAccess,
                                 ImTranslator translator, ImFunction f) {
        throw new Error("not implemented");
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
