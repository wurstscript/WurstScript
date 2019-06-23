package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.OptimizerPass;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.types.TypesHelper;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class SimpleRewrites implements OptimizerPass {
    private SideEffectAnalyzer sideEffectAnalysis;
    private int totalRewrites = 0;
    private boolean showRewrites = false;

    @Override
    public int optimize(ImTranslator trans) {
        ImProg prog = trans.getImProg();
        this.sideEffectAnalysis = new SideEffectAnalyzer(prog);
        totalRewrites = 0;
        optimizeElement(prog);
        // we need to flatten the program, because we introduced new
        // StatementExprs
        prog.flatten(trans);
        removeUnreachableCode(prog);
        return totalRewrites;
    }

    @Override
    public String getName() {
        return "Simple Rewrites";
    }

    private void removeUnreachableCode(ImProg prog) {
        prog.accept(new ImProg.DefaultVisitor() {
            @Override
            public void visit(ImStmts stmts) {
                super.visit(stmts);
                removeUnreachableCode(stmts);
            }
        });
    }

    private void removeUnreachableCode(ImStmts stmts) {
        Iterator<ImStmt> it = stmts.iterator();
        boolean reachable = true;
        while (it.hasNext()) {
            ImStmt s = it.next();
            if (reachable) {
                if (s instanceof ImReturn) {
                    reachable = false;
                } else if (s instanceof ImExitwhen) {
                    ImExitwhen imExitwhen = (ImExitwhen) s;
                    ImExpr expr = imExitwhen.getCondition();
                    if (expr instanceof ImBoolVal) {
                        boolean b = ((ImBoolVal) expr).getValB();
                        if (b) {
                            // found "exitwhen true"
                            reachable = false;
                        }
                    }
                }
            } else {
                totalRewrites++;
                it.remove();
            }
        }
    }

    /**
     * Recursively optimizes the element
     */
    private void optimizeElement(Element elem) {
        // optimize children:
        for (int i = 0; i < elem.size(); i++) {
            optimizeElement(elem.get(i));
            if (i > 0) {
                Element lookback = elem.get(i - 1);
                if (elem.get(i) instanceof ImExitwhen && lookback instanceof ImExitwhen) {
                    optimizeConsecutiveExitWhen((ImExitwhen) lookback, (ImExitwhen) elem.get(i));
                }

                if (elem.get(i) instanceof ImSet && lookback instanceof ImSet) {
                    optimizeConsecutiveSet((ImSet) lookback, (ImSet) elem.get(i));
                }
            }
        }
        if (elem instanceof ImOperatorCall) {
            ImOperatorCall opc = (ImOperatorCall) elem;
            optimizeOpCall(opc);
        } else if (elem instanceof ImIf) {
            ImIf imIf = (ImIf) elem;
            optimizeIf(imIf);
        } else if (elem instanceof ImExitwhen) {
            ImExitwhen imExitwhen = (ImExitwhen) elem;
            optimizeExitwhen(imExitwhen);
        }

    }

    private void optimizeConsecutiveExitWhen(ImExitwhen lookback, ImExitwhen element) {
        element.getCondition().setParent(null);
        lookback.setCondition(JassIm.ImOperatorCall(WurstOperator.OR, JassIm.ImExprs(lookback.getCondition().copy(), element.getCondition())));
        element.replaceBy(ImHelper.nullExpr());
        totalRewrites++;
    }

    private void optimizeExitwhen(ImExitwhen imExitwhen) {
        ImExpr expr = imExitwhen.getCondition();
        if (expr instanceof ImBoolVal) {
            boolean b = ((ImBoolVal) expr).getValB();
            if (!b) {
                imExitwhen.replaceBy(ImHelper.nullExpr());
                totalRewrites++;
            }
        }

    }

    /**
     * Rewrites if statements that only contain an exitwhen statement
     * so that the if's condition is combined with the exitwhen
     * <p>
     * if expr1
     * exitwhen expr2
     * <p>
     * to:
     * <p>
     * exitwhen expr1 and expr2
     */
    private void optimizeIfExitwhen(ImIf imIf) {
        ImExitwhen imStmt = (ImExitwhen) imIf.getThenBlock().get(0);
        imStmt.getCondition().setParent(null);
        imIf.getCondition().setParent(null);

        imStmt.setCondition((JassIm.ImOperatorCall(WurstOperator.AND, JassIm.ImExprs(imIf.getCondition(), imStmt.getCondition()))));
        imStmt.setParent(null);
        imIf.replaceBy(imStmt);
        totalRewrites++;
    }

    private void optimizeOpCall(ImOperatorCall opc) {
        // Binary
        boolean wasViable = true;
        if (opc.getArguments().size() > 1) {
            ImExpr left = opc.getArguments().get(0);
            ImExpr right = opc.getArguments().get(1);
            if (left instanceof ImBoolVal && right instanceof ImBoolVal) {
                boolean b1 = ((ImBoolVal) left).getValB();
                boolean b2 = ((ImBoolVal) right).getValB();
                boolean result;
                switch (opc.getOp()) {
                    case OR:
                        result = b1 || b2;
                        break;
                    case AND:
                        result = b1 && b2;
                        break;
                    case EQ:
                        result = b1 == b2;
                        break;
                    case NOTEQ:
                        result = b1 != b2;
                        break;
                    default:
                        result = false;
                        break;
                }
                opc.replaceBy(JassIm.ImBoolVal(result));
            } else if (left instanceof ImBoolVal) {
                boolean b1 = ((ImBoolVal) left).getValB();
                wasViable = replaceBoolTerm(opc, right, b1);
            } else if (right instanceof ImBoolVal) {
                boolean b2 = ((ImBoolVal) right).getValB();
                wasViable = replaceBoolTerm(opc, left, b2);
            } else if (left instanceof ImIntVal && right instanceof ImIntVal) {
                wasViable = optimizeIntInt(opc, wasViable, (ImIntVal) left, (ImIntVal) right);
            } else if (left instanceof ImRealVal && right instanceof ImRealVal) {
                wasViable = optimizeRealReal(opc, wasViable, (ImRealVal) left, (ImRealVal) right);
            } else if (right instanceof ImStringVal) {
                if (left instanceof ImStringVal) {
                    wasViable = optimizeStringString(opc, (ImStringVal) left, (ImStringVal) right);
                } else if (((ImStringVal) right).getValS().equalsIgnoreCase("") && opc.getOp() == WurstOperator.PLUS) {
                    left.setParent(null);
                    opc.replaceBy(left);
                    wasViable = true;
                } else {
                    wasViable = false;
                }
            } else if (opc.getOp() == WurstOperator.PLUS
                    && (left.attrTyp().equalsType(TypesHelper.imInt()) || left.attrTyp().equalsType(TypesHelper.imReal()))
                    && left.structuralEquals(right)) {
                // x + x ---> 2*x
                if (!sideEffectAnalysis.hasSideEffects(left)) {
                    opc.setOp(WurstOperator.MULT);
                    right.replaceBy(JassIm.ImIntVal(2));
                    wasViable = true;
                }
            } else {
                wasViable = false;
            }
        }

        // Unary
        else {
            ImExpr expr = opc.getArguments().get(0);
            if (opc.getOp() == WurstOperator.UNARY_MINUS && expr instanceof ImIntVal) {
                ImIntVal imIntVal = (ImIntVal) expr;
                if (imIntVal.getValI() <= 0) {
                    int inverseVal = imIntVal.getValI() * -1;
                    ImIntVal newVal = JassIm.ImIntVal(inverseVal);
                    opc.replaceBy(newVal);
                }
                wasViable = false;
            } else if (expr instanceof ImBoolVal) {
                boolean b1 = ((ImBoolVal) expr).getValB();
                boolean result;
                switch (opc.getOp()) {
                    case NOT:
                        result = !b1;
                        break;
                    default:
                        result = false;
                        break;
                }
                opc.replaceBy(JassIm.ImBoolVal(result));
            } else if (opc.getOp() == WurstOperator.NOT && expr instanceof ImOperatorCall) {
                // optimize negation of some operators
                ImOperatorCall inner = (ImOperatorCall) expr;
                switch (inner.getOp()) {
                    case NOT:
                        opc.replaceBy(inner.getArguments().remove(0));
                        break;
                    case EQ:
                    case NOTEQ:
                    case LESS:
                    case LESS_EQ:
                    case GREATER:
                    case GREATER_EQ:
                        opc.replaceBy(JassIm.ImOperatorCall(oppositeOperator(inner.getOp()), JassIm.ImExprs(inner.getArguments().removeAll())));
                        break;
                    case OR:
                    case AND:
                        // DeMorgan not(a and b) => not a or not b; not(a or b) => not a and not b
                        List<ImExpr> args = inner.getArguments().removeAll();
                        ImExprs imExprs = JassIm.ImExprs();
                        args.forEach((e) ->
                                imExprs.add(JassIm.ImOperatorCall(WurstOperator.NOT, JassIm.ImExprs(e.copy()))));

                        ImOperatorCall opCall = JassIm.ImOperatorCall(oppositeOperator(inner.getOp()), imExprs);
                        opc.replaceBy(opCall);
                        break;
                    default:
                        wasViable = false;
                        break;
                }
            } else {
                wasViable = false;
            }
        }
        if (wasViable) {
            totalRewrites++;
            if (showRewrites) {
                WLogger.info("opcall rewrite: " + opc.toString());
            }
        }

    }

    private boolean optimizeStringString(ImOperatorCall opc, ImStringVal left, ImStringVal right) {
        String f1 = left.getValS();
        String f2 = right.getValS();
        switch (opc.getOp()) {
            case PLUS:
                opc.replaceBy(JassIm.ImStringVal(f1 + f2));
                return true;
            default:
                break;
        }
        return false;
    }

    private boolean optimizeRealReal(ImOperatorCall opc, boolean wasViable, ImRealVal left, ImRealVal right) {
        float f1 = Float.parseFloat(left.getValR());
        float f2 = Float.parseFloat(right.getValR());
        boolean isConditional = false;
        boolean isArithmetic = false;
        boolean result = false;
        float resultVal = 0;
        switch (opc.getOp()) {
            case GREATER:
                result = f1 > f2;
                isConditional = true;
                break;
            case GREATER_EQ:
                result = f1 >= f2;
                isConditional = true;
                break;
            case LESS:
                result = f1 < f2;
                isConditional = true;
                break;
            case LESS_EQ:
                result = f1 <= f2;
                isConditional = true;
                break;
            case EQ:
                result = f1 == f2;
                isConditional = true;
                break;
            case NOTEQ:
                result = f1 != f2;
                isConditional = true;
                break;
            case PLUS:
                resultVal = f1 + f2;
                isArithmetic = true;
                break;
            case MINUS:
                resultVal = f1 - f2;
                isArithmetic = true;
                break;
            case MULT:
                resultVal = f1 * f2;
                isArithmetic = true;
                break;
            case MOD_REAL:
                if (f2 != 0) {
                    resultVal = f1 % f2;
                    isArithmetic = true;
                }
                break;
            case DIV_INT:
                if (f2 != 0) {
                    resultVal = f1 / f2;
                    isArithmetic = true;
                }
                break;
            case DIV_REAL:
                if (f2 != 0) {
                    resultVal = f1 / f2;
                    isArithmetic = true;
                }
                break;
            default:
                result = false;
                isConditional = false;
                isArithmetic = false;
                break;
        }
        if (isConditional) {
            opc.replaceBy(JassIm.ImBoolVal(result));
        } else if (isArithmetic) {
            // convert result to string, using 4 decimal digits
            String s = floatToStringWithDecimalDigits(resultVal, 4);
            // String s = new BigDecimal(resultVal).toPlainString();
            // check if the string representation is exact
            if (Float.parseFloat(s) == resultVal) {
                opc.replaceBy(JassIm.ImRealVal(s));
            } else {
                s = floatToStringWithDecimalDigits(resultVal, 9);
                if (Float.parseFloat(s) == resultVal) {
                    opc.replaceBy(JassIm.ImRealVal(s));
                } else {
                    wasViable = false;
                }
            }
        } else {
            wasViable = false;
        }
        return wasViable;
    }

    private boolean optimizeIntInt(ImOperatorCall opc, boolean wasViable, ImIntVal left, ImIntVal right) {
        int i1 = left.getValI();
        int i2 = right.getValI();
        boolean isConditional = false;
        boolean isArithmetic = false;
        boolean result = false;
        int resultVal = 0;
        switch (opc.getOp()) {
            case GREATER:
                result = i1 > i2;
                isConditional = true;
                break;
            case GREATER_EQ:
                result = i1 >= i2;
                isConditional = true;
                break;
            case LESS:
                result = i1 < i2;
                isConditional = true;
                break;
            case LESS_EQ:
                result = i1 <= i2;
                isConditional = true;
                break;
            case EQ:
                result = i1 == i2;
                isConditional = true;
                break;
            case NOTEQ:
                result = i1 != i2;
                isConditional = true;
                break;
            case PLUS:
                resultVal = i1 + i2;
                isArithmetic = true;
                break;
            case MINUS:
                resultVal = i1 - i2;
                isArithmetic = true;
                break;
            case MULT:
                resultVal = i1 * i2;
                isArithmetic = true;
                break;
            case MOD_INT:
                if (i2 != 0) {
                    resultVal = i1 % i2;
                    isArithmetic = true;
                }
                break;
            case MOD_REAL:
                float f1 = i1;
                float f2 = i2;
                if (f2 != 0) {
                    float resultF = f1 % f2;
                    opc.replaceBy(JassIm.ImRealVal(String.valueOf(resultF)));
                }
                break;
            case DIV_INT:
                if (i2 != 0) {
                    resultVal = i1 / i2;
                    isArithmetic = true;
                }
                break;
            case DIV_REAL:
                float f3 = i1;
                float f4 = i2;
                if (f4 != 0) {
                    float resultF = f3 / f4;
                    opc.replaceBy(JassIm.ImRealVal(String.valueOf(resultF)));
                }
                break;
            default:
                result = false;
                isConditional = false;
                isArithmetic = false;
                break;
        }
        if (isConditional) {
            opc.replaceBy(JassIm.ImBoolVal(result));
        } else if (isArithmetic) {
            opc.replaceBy(JassIm.ImIntVal(resultVal));
        } else {
            wasViable = false;
        }
        return wasViable;
    }

    private boolean replaceBoolTerm(ImOperatorCall opc, ImExpr expr, boolean b2) {
        switch (opc.getOp()) {
            case OR:
                if (b2) {
                    opc.replaceBy(JassIm.ImBoolVal(true));
                } else {
                    expr.setParent(null);
                    opc.replaceBy(expr);
                }
                break;
            case AND:
                if (b2) {
                    expr.setParent(null);
                    opc.replaceBy(expr);
                } else {
                    opc.replaceBy(JassIm.ImBoolVal(false));
                }
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * returns the opposite of an operator
     */
    private WurstOperator oppositeOperator(WurstOperator op) {
        switch (op) {
            case EQ:
                return WurstOperator.NOTEQ;
            case GREATER:
                return WurstOperator.LESS_EQ;
            case GREATER_EQ:
                return WurstOperator.LESS;
            case LESS:
                return WurstOperator.GREATER_EQ;
            case LESS_EQ:
                return WurstOperator.GREATER;
            case NOTEQ:
                return WurstOperator.EQ;
            case AND:
                return WurstOperator.OR;
            case OR:
                return WurstOperator.AND;
            default:
                throw new Error("operator " + op + " does not have an opposite.");
        }
    }

    private static String floatToStringWithDecimalDigits(float resultVal, int digits) {
        DecimalFormat format = new DecimalFormat();
        // use a fixed locale, so that it does not randomly replace a dot by
        // comma on German PCs
        // hope this works
        format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        format.setMinimumIntegerDigits(1);
        format.setMaximumFractionDigits(digits);
        format.setMinimumFractionDigits(1);
        format.setGroupingUsed(false);
        String s = format.format(resultVal);
        return s;
    }

    private void optimizeIf(ImIf imIf) {
        if (imIf.getThenBlock().isEmpty() && imIf.getElseBlock().isEmpty()) {
            totalRewrites++;
            imIf.replaceBy(imIf.getCondition().copy());
        } else if (imIf.getCondition() instanceof ImBoolVal) {
            ImBoolVal boolVal = (ImBoolVal) imIf.getCondition();
            if (boolVal.getValB()) {
                // we have something like 'if true ...'
                // replace the if statement with the then-block
                // we have to use ImStatementExpr to get multiple statements
                // into one statement as needed
                // for the replaceBy function
                // we need to copy the thenBlock because otherwise it would have
                // two parents (we have not removed it from the old if-block)
                imIf.replaceBy(ImHelper.statementExprVoid(imIf.getThenBlock().copy()));
                totalRewrites++;
            } else {
                if (!imIf.getElseBlock().isEmpty()) {
                    imIf.replaceBy(ImHelper.statementExprVoid(imIf.getElseBlock().copy()));
                    totalRewrites++;
                } else {
                    imIf.replaceBy(ImHelper.nullExpr());
                    totalRewrites++;
                }
            }
        } else if (imIf.getElseBlock().isEmpty() && imIf.getThenBlock().size() == 1 && imIf.getThenBlock().get(0) instanceof ImExitwhen) {
            optimizeIfExitwhen(imIf);
        }
    }

    /**
     * Optimizes
     * <p>
     * set x = expr1
     * set x = x ⊕ expr2
     * <p>
     * into:
     * <p>
     * set x  = expr1 ⊕ expr2
     * <p>
     * like code that is created by the branch merger
     */
    private void optimizeConsecutiveSet(ImSet imSet1, ImSet imSet2) {
        ImVar leftVar1;
        if (imSet1.getLeft() instanceof ImVarAccess) {
            leftVar1 = ((ImVarAccess) imSet1.getLeft()).getVar();
        } else {
            return;
        }
        ImVar leftVar2;
        if (imSet2.getLeft() instanceof ImVarAccess) {
            leftVar2 = ((ImVarAccess) imSet2.getLeft()).getVar();
        } else {
            return;
        }

        ImExpr rightExpr1 = imSet1.getRight();
        ImExpr rightExpr2 = imSet2.getRight();

        if (leftVar1 == leftVar2) {
            if (rightExpr2 instanceof ImOperatorCall) {
                ImOperatorCall rightOpCall2 = (ImOperatorCall) rightExpr2;
                if (rightOpCall2.getArguments().size() == 2) {
                    if (rightOpCall2.getArguments().get(0) instanceof ImVarAccess) {
                        ImVarAccess imVarAccess2 = (ImVarAccess) rightOpCall2.getArguments().get(0);
                        if (imVarAccess2.getVar() == leftVar2) {
                            if (sideEffectAnalysis.cannotUseVar(rightOpCall2.getArguments().get(1), leftVar1)) {
                                rightExpr1.setParent(null);
                                imVarAccess2.replaceBy(rightExpr1);
                                imSet1.replaceBy(ImHelper.nullExpr());
                                totalRewrites++;
                            }
                        }
                    }
                }
            }
        }
    }

}
