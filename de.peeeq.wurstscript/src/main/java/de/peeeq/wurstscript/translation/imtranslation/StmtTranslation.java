package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.AttrFuncDef;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.OtherLink;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeArray;
import de.peeeq.wurstscript.types.WurstTypeVararg;
import org.eclipse.jdt.annotation.Nullable;

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
                stmts.add(ImSet(s, ImVarArrayAccess(s, v, ImExprs(JassIm.ImIntVal(i))), translatedExpr));
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

    public static ImStmt translate(StmtContinue s, ImTranslator t, ImFunction f) {
        ImVar continueFlag = t.currentContinueFlag();
        if (continueFlag == null) {
            throw new CompileError(s.getSource(), "Continue is not allowed outside of loop statements.");
        }
        return ImSet(s, ImVarAccess(continueFlag), JassIm.ImBoolVal(true));
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
            imBody.addAll(translateLoopBody(t, f, s.getBody(), s));

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
        List<ImStmt> result = com.google.common.collect.Lists.newArrayList();

        Optional<FuncLink> iteratorFuncOpt = forIn.attrIteratorFunc();
        Optional<FuncLink> nextFuncOpt = forIn.attrGetNextFunc();
        Optional<FuncLink> hasNextFuncOpt = forIn.attrHasNextFunc();

        if (iteratorFuncOpt.isPresent() && nextFuncOpt.isPresent() && hasNextFuncOpt.isPresent()) {
            FuncLink iteratorFunc = iteratorFuncOpt.get();
            FuncLink nextFunc = nextFuncOpt.get();
            FuncLink hasNextFunc = hasNextFuncOpt.get();

            // Type of loop variable (element type S):
            WurstType elemType = forIn.getLoopVar().attrTyp();

            // IM functions
            ImFunction iteratorFuncIm = t.getFuncFor(iteratorFunc.getDef());
            ImFunction nextFuncIm     = t.getFuncFor(nextFunc.getDef());
            ImFunction hasNextFuncIm  = t.getFuncFor(hasNextFunc.getDef());

            // Translate receiver (iteration target):
            ImExprs iterationTargetList;
            if (itrType.isStaticRef()) {
                iterationTargetList = ImExprs();
            } else {
                ImExpr iterationTargetIm = iterationTarget.imTranslateExpr(t, f);
                iterationTargetList = JassIm.ImExprs(iterationTargetIm);
            }

            // --- CONCRETE type argument for Iterator<S> and its methods ---
            ImType elemImType = elemType.imTranslateType(t);
            ImTypeArguments iterTypeArgs = JassIm.ImTypeArguments(
                JassIm.ImTypeArgument(elemImType, java.util.Collections.emptyMap())
            );

            // call XX.iterator()<S>()
            ImFunctionCall iteratorCall = ImFunctionCall(
                forIn, iteratorFuncIm, iterTypeArgs, iterationTargetList, false, CallType.NORMAL
            );

            // Materialize a concrete IM class type for the iterator local (Iterator<S>)
            ImType iteratorImType;
            WurstType retWT = iteratorFunc.getReturnType().normalize();
            if (retWT instanceof de.peeeq.wurstscript.types.WurstTypeClass) {
                de.peeeq.wurstscript.types.WurstTypeClass rtc = (de.peeeq.wurstscript.types.WurstTypeClass) retWT;
                de.peeeq.wurstscript.ast.ClassDef rtClassDef = rtc.getClassDef();
                ImClass imIterClass = t.getClassFor(rtClassDef);
                iteratorImType = JassIm.ImClassType(imIterClass, iterTypeArgs.copy());
            } else {
                // fallback – should not happen for a well-formed iterator()
                iteratorImType = retWT.imTranslateType(t);
            }

            // locals: iterator and loopVar
            ImVar iteratorVar = JassIm.ImVar(forIn.getLoopVar(), iteratorImType, "iterator", false);
            f.getLocals().add(iteratorVar);
            f.getLocals().add(t.getVarFor(forIn.getLoopVar()));

            // init iterator
            result.add(ImSet(forIn, ImVarAccess(iteratorVar), iteratorCall));

            ImStmts imBody = ImStmts();

            // exitwhen not iterator.hasNext()<S>()
            imBody.add(ImExitwhen(
                forIn,
                JassIm.ImOperatorCall(
                    de.peeeq.wurstscript.WurstOperator.NOT,
                    JassIm.ImExprs(
                        ImFunctionCall(
                            forIn, hasNextFuncIm, iterTypeArgs.copy(),
                            JassIm.ImExprs(JassIm.ImVarAccess(iteratorVar)),
                            false, CallType.NORMAL
                        )
                    )
                )
            ));

            // elem = iterator.next()<S>()
            ImFunctionCall nextCall = ImFunctionCall(
                forIn, nextFuncIm, iterTypeArgs.copy(),
                JassIm.ImExprs(JassIm.ImVarAccess(iteratorVar)),
                false, CallType.NORMAL
            );

            ImExpr nextCallWrapped = de.peeeq.wurstscript.translation.imtranslation.ExprTranslation
                .wrapTranslation(forIn, t, nextCall, nextFunc.getReturnType(), elemType);

            imBody.add(ImSet(forIn, ImVarAccess(t.getVarFor(forIn.getLoopVar())), nextCallWrapped));

            // loop body
            imBody.addAll(translateLoopBody(t, f, forIn.getBody(), forIn));

            // optional close()<S>()
            Optional<FuncLink> closeFunc = forIn.attrCloseFunc();
            closeFunc.ifPresent(funcLink -> {
                imBody.accept(new de.peeeq.wurstscript.jassIm.Element.DefaultVisitor() {
                    @Override
                    public void visit(ImReturn imReturn) {
                        super.visit(imReturn);
                        imReturn.replaceBy(
                            ImHelper.statementExprVoid(
                                JassIm.ImStmts(
                                    ImFunctionCall(
                                        forIn, t.getFuncFor(funcLink.getDef()),
                                        iterTypeArgs.copy(),
                                        JassIm.ImExprs(JassIm.ImVarAccess(iteratorVar)),
                                        false, CallType.NORMAL
                                    ),
                                    imReturn.copy()
                                )
                            )
                        );
                    }
                });
            });

            result.add(ImLoop(forIn, imBody));

            // close after loop
            closeFunc.ifPresent(nameLink ->
                result.add(
                    ImFunctionCall(
                        forIn, t.getFuncFor(nameLink.getDef()),
                        iterTypeArgs.copy(),
                        JassIm.ImExprs(JassIm.ImVarAccess(iteratorVar)),
                        false, CallType.NORMAL
                    )
                )
            );
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
        imBody.addAll(translateLoopBody(t, f, body, trace));
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
        return ImLoop(s, ImStmts(translateLoopBody(t, f, s.getBody(), s)));
    }


    public static ImStmt translate(StmtReturn s, ImTranslator t, ImFunction f) {
        return ImReturn(s, s.getReturnedObj().imTranslateExprOpt(t, f));
    }


    public static ImStmt translate(StmtSet s, ImTranslator t, ImFunction f) {
        ImStmt overloadedIndexSet = translateOverloadedIndexSet(s, t, f);
        if (overloadedIndexSet != null) {
            return overloadedIndexSet;
        }
        ImLExpr updated = s.getUpdatedExpr().imTranslateExprLvalue(t, f);
        ImExpr right = s.getRight().imTranslateExpr(t, f);
        return ImSet(s, updated, right);
    }

    private static @Nullable ImStmt translateOverloadedIndexSet(StmtSet s, ImTranslator t, ImFunction f) {
        if (!(s.getUpdatedExpr() instanceof NameRef) || !(s.getUpdatedExpr() instanceof AstElementWithIndexes)) {
            return null;
        }
        NameRef left = (NameRef) s.getUpdatedExpr();
        AstElementWithIndexes withIndexes = (AstElementWithIndexes) s.getUpdatedExpr();
        if (withIndexes.getIndexes().size() != 1) {
            return null;
        }
        NameLink link = left.attrNameLink();
        if (link == null || link.getTyp() instanceof WurstTypeArray) {
            return null;
        }
        FuncLink setOverload = AttrFuncDef.getIndexSetOperator(
                left,
                link.getTyp(),
                withIndexes.getIndexes().get(0).attrTyp(),
                s.getRight().attrTyp());
        if (setOverload == null) {
            return null;
        }
        if (link instanceof OtherLink || !(link.getDef() instanceof VarDef)) {
            throw new CompileError(s.getSource(), "Could not resolve assignment receiver for overloaded [] assignment.");
        }
        VarDef varDef = (VarDef) link.getDef();
        ImVar receiverVar = t.getVarFor(varDef);
        ImExpr receiver;
        if (left.attrImplicitParameter() instanceof Expr) {
            Expr implicit = (Expr) left.attrImplicitParameter();
            receiver = JassIm.ImMemberAccess(left, implicit.imTranslateExpr(t, f), JassIm.ImTypeArguments(), receiverVar, JassIm.ImExprs());
        } else {
            receiver = ImVarAccess(receiverVar);
        }
        ImExpr index = withIndexes.getIndexes().get(0).imTranslateExpr(t, f);
        ImExpr value = s.getRight().imTranslateExpr(t, f);
        ImFunction calledFunc = t.getFuncFor(setOverload.getDef());
        return ImFunctionCall(s, calledFunc, ImTypeArguments(), ImExprs(receiver, index, value), false, CallType.NORMAL);
    }


    public static ImStmt translate(StmtWhile s, ImTranslator t, ImFunction f) {
        List<ImStmt> body = Lists.newArrayList();
        // exitwhen not while_condition
        body.add(ImExitwhen(s.getCond(), ImOperatorCall(WurstOperator.NOT, ImExprs(s.getCond().imTranslateExpr(t, f)))));
        body.addAll(translateLoopBody(t, f, s.getBody(), s));
        return ImLoop(s, ImStmts(body));
    }

    private static ImVar createContinueFlagVar(Element trace, ImFunction f) {
        ImVar continueFlag = JassIm.ImVar(trace, TypesHelper.imBool(), "continueFlag_" + f.getLocals().size(), false);
        f.getLocals().add(continueFlag);
        return continueFlag;
    }

    private static List<ImStmt> translateLoopBodyWithContinue(ImTranslator t, ImFunction f, List<WStatement> body, ImVar continueFlag, Element trace) {
        List<ImStmt> guardedBody = Lists.newArrayList();
        guardedBody.add(ImSet(trace, ImVarAccess(continueFlag), JassIm.ImBoolVal(false)));
        t.pushContinueFlag(continueFlag);
        try {
            for (WStatement s : body) {
                ImStmt translated = s.imTranslateStmt(t, f);
                ImExpr guard = ImOperatorCall(WurstOperator.NOT, ImExprs(ImVarAccess(continueFlag)));
                guardedBody.add(ImIf(trace, guard, ImStmts(translated), ImStmts()));
            }
        } finally {
            t.popContinueFlag();
        }
        return guardedBody;
    }

    private static List<ImStmt> translateLoopBody(ImTranslator t, ImFunction f, List<WStatement> body, Element trace) {
        if (!hasContinueForCurrentLoop(body)) {
            return t.translateStatements(f, body);
        }
        ImVar continueFlag = createContinueFlagVar(trace, f);
        return translateLoopBodyWithContinue(t, f, body, continueFlag, trace);
    }

    private static boolean hasContinueForCurrentLoop(List<WStatement> body) {
        for (WStatement statement : body) {
            if (statement instanceof StmtContinue) {
                return true;
            }
            if (statement instanceof StmtLoop
                || statement instanceof StmtWhile
                || statement instanceof StmtForIn
                || statement instanceof StmtForFrom
                || statement instanceof StmtForRangeUp
                || statement instanceof StmtForRangeDown) {
                // Continue inside nested loops should not trigger guarding for the outer loop.
                continue;
            }
            if (statement instanceof WBlock && hasContinueForCurrentLoop(((WBlock) statement).getBody())) {
                return true;
            }
            if (statement instanceof StmtIf) {
                StmtIf stmtIf = (StmtIf) statement;
                if (hasContinueForCurrentLoop(stmtIf.getThenBlock()) || hasContinueForCurrentLoop(stmtIf.getElseBlock())) {
                    return true;
                }
            }
            if (statement instanceof SwitchStmt) {
                SwitchStmt switchStmt = (SwitchStmt) statement;
                for (SwitchCase switchCase : switchStmt.getCases()) {
                    if (hasContinueForCurrentLoop(switchCase.getStmts())) {
                        return true;
                    }
                }
                if (switchStmt.getSwitchDefault() instanceof SwitchDefaultCaseStatements) {
                    SwitchDefaultCaseStatements defaultCase = (SwitchDefaultCaseStatements) switchStmt.getSwitchDefault();
                    if (hasContinueForCurrentLoop(defaultCase.getStmts())) {
                        return true;
                    }
                }
            }
        }
        return false;
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
