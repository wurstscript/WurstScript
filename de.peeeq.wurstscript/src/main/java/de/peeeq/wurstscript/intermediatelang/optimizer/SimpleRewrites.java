package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class SimpleRewrites {
    public int totalRewrites = 0;
    private final ImProg prog;
    private final ImTranslator trans;
    private boolean showRewrites = false;

    public SimpleRewrites(ImTranslator trans) {
        this.prog = trans.getImProg();
        this.trans = trans;
    }

    public void optimize(boolean showRewrites) {
        optimizeElement(prog);
        // we need to flatten the program, because we introduced new
        // StatementExprs
        this.showRewrites = showRewrites;
        prog.flatten(trans);
        removeUnreachableCode(prog);
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
            optimizeElement(opc.getArguments());
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
        lookback.getCondition().setParent(null);
        lookback.getCondition().replaceBy(JassIm.ImOperatorCall(WurstOperator.OR, JassIm.ImExprs(lookback.getCondition(), element.getCondition())));
        element.replaceBy(JassIm.ImNull());
        totalRewrites++;
    }

    private void optimizeExitwhen(ImExitwhen imExitwhen) {
        ImExpr expr = imExitwhen.getCondition();
        if (expr instanceof ImBoolVal) {
            boolean b = ((ImBoolVal) expr).getValB();
            if (!b) {
                imExitwhen.replaceBy(JassIm.ImNull());
                totalRewrites++;
            }
        }

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
                int i1 = ((ImIntVal) left).getValI();
                int i2 = ((ImIntVal) right).getValI();
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
            } else if (left instanceof ImRealVal && right instanceof ImRealVal) {
                float f1 = Float.parseFloat(((ImRealVal) left).getValR());
                float f2 = Float.parseFloat(((ImRealVal) right).getValR());
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
        format.setMinimumFractionDigits(0);
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
                imIf.replaceBy(JassIm.ImStatementExpr(imIf.getThenBlock().copy(), JassIm.ImNull()));
                totalRewrites++;
            } else {
                if (!imIf.getElseBlock().isEmpty()) {
                    imIf.replaceBy(JassIm.ImStatementExpr(imIf.getElseBlock().copy(), JassIm.ImNull()));
                    totalRewrites++;
                } else {
                    imIf.replaceBy(JassIm.ImNull());
                    totalRewrites++;
                }
            }
        }
    }

    /**
     * Optimizes
     *
     *   set x = x ⊕ c1
     *   set x = x ⊕ c2
     *
     * into:
     *
     *   set x  = (x ⊕ c1) ⊕ c2
     *
     * like code that is created by the branch merger
     */
    private void optimizeConsecutiveSet(ImSet imSet, ImSet imSet2) {
        ImVar leftVar1 = imSet.getLeft();
        ImVar leftVar2 = imSet2.getLeft();

        ImExpr rightExpr1 = imSet.getRight();
        ImExpr rightExpr2 = imSet2.getRight();

        if (leftVar1 == leftVar2) {
            if (rightExpr1 instanceof ImOperatorCall && rightExpr2 instanceof ImOperatorCall) {
                ImOperatorCall rightOpCall1 = (ImOperatorCall) rightExpr1;
                ImOperatorCall rightOpCall2 = (ImOperatorCall) rightExpr2;
                if (rightOpCall1.getArguments().size() == 2 && rightOpCall2.getArguments().size() == 2) {
                    if (rightOpCall1.getArguments().get(0) instanceof ImVarAccess && rightOpCall2.getArguments().get(0) instanceof ImVarAccess) {
                        ImVarAccess imVarAccess1 = (ImVarAccess) rightOpCall1.getArguments().get(0);
                        ImVarAccess imVarAccess2 = (ImVarAccess) rightOpCall2.getArguments().get(0);
                        if (imVarAccess1.getVar() == leftVar1 && imVarAccess2.getVar() == leftVar2) {
                            if (rightOpCall1.getArguments().get(1) instanceof ImConst && rightOpCall2.getArguments().get(1) instanceof ImConst) {
                                rightOpCall1.setParent(null);
                                imVarAccess2.replaceBy(rightOpCall1);
                                imSet.replaceBy(JassIm.ImNull());
                                totalRewrites++;
                            }
                        }

                    }
                }
            }
        }
    }

}
