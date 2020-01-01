package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeVararg;

import java.util.List;
import java.util.Optional;

import static de.peeeq.wurstscript.jassIm.JassIm.*;

public class StmtTranslation {

    public static ImStmt translate(Expr s, ImTranslator t, ImFunction f) {
        return s.imTranslateExpr(t, f);
    }

    public static ImStmt translate(LocalVarDef s, ImTranslator t, ImFunction f) {
        ImVar v = t.getVarFor(s);
        f.getLocals().add(v);
        if (s.getInitialExpr() instanceof Expr) {
            Expr inital = (Expr) s.getInitialExpr();
            return ImSet(s, ImVarAccess(v), inital.imTranslateExpr(t, f));
        } else if (s.getInitialExpr() instanceof ArrayInitializer) {
            ArrayInitializer ai = (ArrayInitializer) s.getInitialExpr();
            ImStmts stmts = ImStmts();
            for (int i = 0; i < ai.getValues().size(); i++) {
                Expr expr = ai.getValues().get(i);
                ImExpr translatedExpr = expr.imTranslateExpr(t, f);
                stmts.add(ImSet(s, ImVarArrayAccess(s, v, ImExprs((ImExpr) JassIm.ImIntVal(i))), translatedExpr));
            }
            return ImHelper.statementExprVoid(stmts);
        } else {
            return ImHelper.nullExpr();
        }
    }


    public static ImStmt translate(StmtErr s, ImTranslator t, ImFunction f) {
        throw new CompileError(s.getSource(), "Source contains errors.");
    }


    public static ImStmt translate(StmtExitwhen s, ImTranslator t, ImFunction f) {
        return ImExitwhen(s, s.getCond().imTranslateExpr(t, f));
    }


    public static ImStmt translate(StmtForFrom s, ImTranslator t, ImFunction f) {
        Expr iterationTarget = s.getIn();
        // Type of loop Variable:
        WurstType loopVarType = s.getLoopVar().attrTyp();
        List<ImStmt> result = Lists.newArrayList();
        Optional<FuncLink> nextFuncOpt = s.attrGetNextFunc();
        Optional<FuncLink> hasNextFuncOpt = s.attrHasNextFunc();
        if (nextFuncOpt.isPresent() && hasNextFuncOpt.isPresent()) {
            FuncLink nextFunc = nextFuncOpt.get();
            FuncLink hasNextFunc = hasNextFuncOpt.get();

            // get the iterator function in the intermediate language
            ImFunction nextFuncIm = t.getFuncFor(nextFunc.getDef());
            ImFunction hasNextFuncIm = t.getFuncFor(hasNextFunc.getDef());

            f.getLocals().add(t.getVarFor(s.getLoopVar()));

            ImExprs fromTarget;
            if (iterationTarget.attrTyp().isStaticRef()) {
                fromTarget = ImExprs();
            } else {
                // store from-expression in variable, so that it is only evaluated once
                ImExpr iterationTargetTr = iterationTarget.imTranslateExpr(t, f);
                ImVar fromVar = ImVar(s, iterationTargetTr.attrTyp(), "from", false);
                f.getLocals().add(fromVar);
                result.add(ImSet(s, ImVarAccess(fromVar), iterationTargetTr));
                fromTarget = JassIm.ImExprs(ImVarAccess(fromVar));
            }

            ImStmts imBody = ImStmts();
            // exitwhen not #hasNext()
            imBody.add(ImExitwhen(s, JassIm.ImOperatorCall(WurstOperator.NOT, JassIm.ImExprs(ImFunctionCall(s, hasNextFuncIm, ImTypeArguments(), fromTarget, false, CallType
                    .NORMAL)))));
            // elem = next()
            ImFunctionCall nextCall = ImFunctionCall(s, nextFuncIm, ImTypeArguments(), fromTarget.copy(), false, CallType.NORMAL);

            WurstType nextReturn = nextFunc.getReturnType();
            ImExpr nextCallWrapped = ExprTranslation.wrapTranslation(s, t, nextCall, nextReturn, loopVarType);

            imBody.add(ImSet(s, ImVarAccess(t.getVarFor(s.getLoopVar())), nextCallWrapped));

            imBody.addAll(t.translateStatements(f, s.getBody()));

            result.add(ImLoop(s, imBody));
        }

        return ImHelper.statementExprVoid(ImStmts(result));
    }


    public static ImStmt translate(StmtForIn forIn, ImTranslator t, ImFunction f) {
        Expr iterationTarget = forIn.getIn();
        WurstType itrType = iterationTarget.attrTyp();
        if (itrType instanceof WurstTypeVararg) {
            return case_StmtForVararg(forIn, t, f);
        }
        List<ImStmt> result = Lists.newArrayList();

        Optional<FuncLink> iteratorFuncOpt = forIn.attrIteratorFunc();
        Optional<FuncLink> nextFuncOpt = forIn.attrGetNextFunc();
        Optional<FuncLink> hasNextFuncOpt = forIn.attrHasNextFunc();
        if (iteratorFuncOpt.isPresent() && nextFuncOpt.isPresent() && hasNextFuncOpt.isPresent()) {
            FuncLink iteratorFunc = iteratorFuncOpt.get();
            FuncLink nextFunc = nextFuncOpt.get();
            FuncLink hasNextFunc = hasNextFuncOpt.get();

            // Type of loop Variable:
            WurstType loopVarType = forIn.getLoopVar().attrTyp();

            // get the iterator function in the intermediate language
            ImFunction iteratorFuncIm = t.getFuncFor(iteratorFunc.getDef());
            ImFunction nextFuncIm = t.getFuncFor(nextFunc.getDef());
            ImFunction hasNextFuncIm = t.getFuncFor(hasNextFunc.getDef());

            // translate target:
            ImExprs iterationTargetList;
            if (forIn.getIn().attrTyp().isStaticRef()) {
                iterationTargetList = ImExprs();
            } else {
                ImExpr iterationTargetIm = forIn.getIn().imTranslateExpr(t, f);
                iterationTargetList = JassIm.ImExprs(iterationTargetIm);
            }

            // call XX.iterator()
            ImFunctionCall iteratorCall = ImFunctionCall(forIn, iteratorFuncIm, ImTypeArguments(), iterationTargetList, false, CallType.NORMAL);
            // create IM-variable for iterator
            ImVar iteratorVar = JassIm.ImVar(forIn.getLoopVar(), iteratorCall.attrTyp(), "iterator", false);

            f.getLocals().add(iteratorVar);
            f.getLocals().add(t.getVarFor(forIn.getLoopVar()));
            // create code for initializing iterator:

            ImSet setIterator = ImSet(forIn, ImVarAccess(iteratorVar), iteratorCall);

            result.add(setIterator);

            ImStmts imBody = ImStmts();
            // exitwhen not #hasNext()
            imBody.add(ImExitwhen(forIn, JassIm.ImOperatorCall(WurstOperator.NOT, JassIm.ImExprs(ImFunctionCall(forIn, hasNextFuncIm, ImTypeArguments(), JassIm.ImExprs
                    (JassIm
                            .ImVarAccess(iteratorVar)), false, CallType.NORMAL)))));
            // elem = next()
            ImFunctionCall nextCall = ImFunctionCall(forIn, nextFuncIm, ImTypeArguments(), JassIm.ImExprs(JassIm.ImVarAccess(iteratorVar)), false, CallType.NORMAL);
            WurstType nextReturn = nextFunc.getReturnType();
            ImExpr nextCallWrapped = ExprTranslation.wrapTranslation(forIn, t, nextCall, nextReturn, loopVarType);

            imBody.add(ImSet(forIn, ImVarAccess(t.getVarFor(forIn.getLoopVar())), nextCallWrapped));

            imBody.addAll(t.translateStatements(f, forIn.getBody()));

            Optional<FuncLink> closeFunc = forIn.attrCloseFunc();
            closeFunc.ifPresent(funcLink -> {

                // close iterator before each return
                imBody.accept(new de.peeeq.wurstscript.jassIm.Element.DefaultVisitor() {
                    @Override
                    public void visit(ImReturn imReturn) {
                        super.visit(imReturn);
                        imReturn.replaceBy(ImHelper.statementExprVoid(JassIm.ImStmts(ImFunctionCall(forIn, t.getFuncFor(funcLink.getDef()), ImTypeArguments(), JassIm
                                .ImExprs(JassIm.ImVarAccess(iteratorVar)), false, CallType.NORMAL), imReturn.copy())));
                    }

                });

            });

            result.add(ImLoop(forIn, imBody));
            // close iterator after loop
            closeFunc.ifPresent(nameLink -> result.add(ImFunctionCall(forIn, t.getFuncFor(nameLink.getDef()), ImTypeArguments(), JassIm.ImExprs(JassIm
                    .ImVarAccess(iteratorVar)), false, CallType.NORMAL)));

        }


        return ImHelper.statementExprVoid(ImStmts(result));
    }

    /**
     * Translate a for in vararg loop. Unlike the other for loops we don't need
     * an iterator etc. because the loop is unrolled in the VarargEliminator
     */
    private static ImStmt case_StmtForVararg(StmtForIn s, ImTranslator t, ImFunction f) {
        List<ImStmt> result = Lists.newArrayList();
        ImVar loopVar = t.getVarFor(s.getLoopVar());

        result.add(ImVarargLoop(s, ImStmts(t.translateStatements(f, s.getBody())), loopVar));

        f.getLocals().add(loopVar);
        return ImHelper.statementExprVoid(ImStmts(result));
    }


    public static ImStmt translate(StmtForRangeUp s, ImTranslator t, ImFunction f) {
        return case_StmtForRange(t, f, s.getLoopVar(), s.getTo(), s.getStep(), s.getBody(), WurstOperator.PLUS,
                WurstOperator.GREATER, s);
    }


    public static ImStmt translate(StmtForRangeDown s, ImTranslator t, ImFunction f) {
        return case_StmtForRange(t, f, s.getLoopVar(), s.getTo(), s.getStep(), s.getBody(),
                WurstOperator.MINUS, WurstOperator.LESS, s);
    }

    private static ImStmt case_StmtForRange(ImTranslator t, ImFunction f, LocalVarDef loopVar,
                                            Expr to, Expr step, WStatements body, WurstOperator opStep, WurstOperator opCompare, Element trace) {
        ImVar imLoopVar = t.getVarFor(loopVar);
        f.getLocals().add(imLoopVar);

        Expr from = (Expr) loopVar.getInitialExpr();
        ImExpr fromExpr = from.imTranslateExpr(t, f);
        List<ImStmt> result = Lists.newArrayList();
        result.add(ImSet(loopVar, ImVarAccess(imLoopVar), fromExpr));

        ImExpr toExpr = addCacheVariableSmart(t, f, result, to, TypesHelper.imInt());
        ImExpr stepExpr = addCacheVariableSmart(t, f, result, step, TypesHelper.imInt());

        ImStmts imBody = ImStmts();
        // exitwhen imLoopVar > toExpr
        imBody.add(ImExitwhen(trace, ImOperatorCall(opCompare, ImExprs(ImVarAccess(imLoopVar), toExpr))));
        // loop body:
        imBody.addAll(t.translateStatements(f, body));
        // set imLoopVar = imLoopVar + stepExpr
        imBody.add(ImSet(trace, ImVarAccess(imLoopVar), ImOperatorCall(opStep, ImExprs(ImVarAccess(imLoopVar), stepExpr))));
        result.add(ImLoop(trace, imBody));
        return ImHelper.statementExprVoid(ImStmts(result));
    }


    private static ImExpr addCacheVariableSmart(ImTranslator t, ImFunction f, List<ImStmt> result, Expr toCache, ImType type) {
        ImExpr r = toCache.imTranslateExpr(t, f);
        if (r instanceof ImConst) {
            return r;
        }
        ImVar tempVar = JassIm.ImVar(toCache, type, "temp", false);
        f.getLocals().add(tempVar);
        result.add(ImSet(toCache, ImVarAccess(tempVar), r));
        return ImVarAccess(tempVar);
    }

    public static ImStmt translate(StmtIf s, ImTranslator t, ImFunction f) {
        return ImIf(s, s.getCond().imTranslateExpr(t, f), ImStmts(t.translateStatements(f, s.getThenBlock())), ImStmts(t.translateStatements(f, s
                .getElseBlock())));
    }


    public static ImStmt translate(StmtLoop s, ImTranslator t, ImFunction f) {
        return ImLoop(s, ImStmts(t.translateStatements(f, s.getBody())));
    }


    public static ImStmt translate(StmtReturn s, ImTranslator t, ImFunction f) {
        return ImReturn(s, s.getReturnedObj().imTranslateExprOpt(t, f));
    }


    public static ImStmt translate(StmtSet s, ImTranslator t, ImFunction f) {
        ImLExpr updated = s.getUpdatedExpr().imTranslateExprLvalue(t, f);
        ImExpr right = s.getRight().imTranslateExpr(t, f);
        return ImSet(s, updated, right);
    }


    public static ImStmt translate(StmtWhile s, ImTranslator t, ImFunction f) {
        List<ImStmt> body = Lists.newArrayList();
        // exitwhen not while_condition
        body.add(ImExitwhen(s.getCond(), ImOperatorCall(WurstOperator.NOT, ImExprs(s.getCond().imTranslateExpr(t, f)))));
        body.addAll(t.translateStatements(f, s.getBody()));
        return ImLoop(s, ImStmts(body));
    }

    public static ImStmt translate(StmtSkip s, ImTranslator translator, ImFunction f) {
        return ImHelper.nullExpr();
    }

    public static ImStmt translate(SwitchStmt switchStmt, ImTranslator t, ImFunction f) {
        List<ImStmt> result = Lists.newArrayList();
        ImType type = switchStmt.getExpr().attrTyp().imTranslateType(t);
        ImExpr tempVar = addCacheVariableSmart(t, f, result, switchStmt.getExpr(), type);
        // generate ifs
        // leerer Block:
        //ImStmts();
        // if else
        //ImIf(trace, condition, thenBlock, elseBlock);
        // vergleich
        //ImOperatorCall(Ast.OpEquals(), ImExprs(a,b))

        ImIf lastIf = null;
        SwitchCase cse;
        for (int i = 0; i < switchStmt.getCases().size(); i++) {
            cse = switchStmt.getCases().get(i);
            if (lastIf == null) {
                lastIf = ImIf(switchStmt, translateSwitchCase(cse, tempVar, f, t), ImStmts(t
                        .translateStatements(f, cse.getStmts())), ImStmts());
                result.add(lastIf);
            } else if (i == switchStmt.getCases().size() - 1
                    && switchStmt.getSwitchDefault() instanceof NoDefaultCase
                    && switchStmt.calculateHandlesAllCases()) {
                // if this is the last case and all cases are covered, then just add
                // the code to the else statement without checking the condition:
                lastIf.setElseBlock(ImStmts(t.translateStatements(f, cse.getStmts())));
            } else {
                ImIf tmp = ImIf(switchStmt, translateSwitchCase(cse, tempVar, f, t), ImStmts
                        (t.translateStatements(f, cse.getStmts())), ImStmts());
                lastIf.setElseBlock(ImStmts(tmp));
                lastIf = tmp;
            }
        }

        if (lastIf == null) {
            throw new CompileError(switchStmt.attrSource(), "No cases in switch?");
        }

//		WLogger.info("it is a " + switchStmt.getSwitchDefault().getClass());
        if (switchStmt.getSwitchDefault() instanceof SwitchDefaultCaseStatements) {

//			WLogger.info("indeed it is");
            SwitchDefaultCaseStatements dflt = (SwitchDefaultCaseStatements) switchStmt.getSwitchDefault();
            lastIf.setElseBlock(ImStmts(t.translateStatements(f, dflt.getStmts())));
        } else if (switchStmt.getSwitchDefault() instanceof NoDefaultCase) {
//			WLogger.info("wtf?");
        }


        return ImHelper.statementExprVoid(ImStmts(result));
    }

    /**
     * translate the expressions of a switch case to
     * <p>
     * case x | y | z
     * <p>
     * is translated to
     * <p>
     * tempVar == x or tempVar == y or tempVar == z
     */
    private static ImExpr translateSwitchCase(SwitchCase cse, ImExpr tempVar, ImFunction f, ImTranslator t) {
        return cse.getExpressions()
                .stream()
                .<ImExpr>map(e -> ImOperatorCall(WurstOperator.EQ, ImExprs(tempVar.copy(), e.imTranslateExpr(t, f))))
                .reduce((x, y) -> ImOperatorCall(WurstOperator.OR, ImExprs(x, y)))
                .orElseGet(() -> JassIm.ImBoolVal(true));
    }

    public static ImStmt translate(EndFunctionStatement endFunctionStatement, ImTranslator translator, ImFunction f) {
        return ImHelper.nullExpr();
    }

    public static ImStmt translate(StartFunctionStatement startFunctionStatement, ImTranslator translator, ImFunction f) {
        return ImHelper.nullExpr();
    }

    public static ImStmt translate(WBlock block, ImTranslator translator, ImFunction f) {
        ImStmts stmts = ImStmts();
        for (WStatement s : block.getBody()) {
            stmts.add(s.imTranslateStmt(translator, f));
        }
        return ImHelper.statementExprVoid(stmts);
    }


}
