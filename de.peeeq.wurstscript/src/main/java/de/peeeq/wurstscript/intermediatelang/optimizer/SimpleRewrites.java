package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.OptimizerPass;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.types.TypesHelper;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

public class SimpleRewrites implements OptimizerPass {
    private SideEffectAnalyzer sideEffectAnalysis;
    private int totalRewrites = 0;
    private final boolean showRewrites = false;
    /** Jass {@code ==} on reals has a tolerance ({@link WurstOperator#JASS_REAL_EQUALITY_TOLERANCE}) and Jass
     *  does not read every real literal exactly ({@link #foldRealForJass}); Lua is exact in both, and its
     *  reals are doubles ({@link #foldRealForLua}). */
    private boolean jassTarget;

    private static boolean isNumberLiteral(ImExpr e) {
        return e instanceof ImIntVal || e instanceof ImRealVal;
    }

    @Override
    public int optimize(ImTranslator trans) {
        ImProg prog = trans.getImProg();
        this.sideEffectAnalysis = new SideEffectAnalyzer(prog);
        this.jassTarget = !trans.isLuaTarget();
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
                if (stmts.size() > 1) {
                    removeUnreachableCode(stmts);
                }
            }
        });
    }

    private void removeUnreachableCode(ImStmts stmts) {
        boolean reachable = true;
        for (int i = 0; i < stmts.size(); ) {
            ImStmt s = stmts.get(i);

            if (!reachable) {
                stmts.remove(i);
                totalRewrites++;
            } else {
                // Check various ways code becomes unreachable
                if (s instanceof ImReturn) {
                    reachable = false;
                } else if (s instanceof ImExitwhen) {
                    ImExitwhen exitwhen = (ImExitwhen) s;
                    if (exitwhen.getCondition() instanceof ImBoolVal) {
                        boolean exits = ((ImBoolVal) exitwhen.getCondition()).getValB();
                        if (exits) {
                            reachable = false;
                        }
                    }
                } else if (s instanceof ImIf) {
                    // Check for "if true then return" patterns
                    ImIf ifStmt = (ImIf) s;
                    if (ifStmt.getCondition() instanceof ImBoolVal) {
                        boolean condition = ((ImBoolVal) ifStmt.getCondition()).getValB();
                        if (condition && endsWithReturn(ifStmt.getThenBlock())) {
                            reachable = false;
                        } else if (!condition && endsWithReturn(ifStmt.getElseBlock())) {
                            reachable = false;
                        }
                    }
                }
                i++;
            }
        }
    }

    private boolean endsWithReturn(ImStmts block) {
        if (block.isEmpty()) return false;
        ImStmt last = block.get(block.size() - 1);
        return last instanceof ImReturn;
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
                wasViable = replaceBoolTerm(opc, right, b1, true);
            } else if (right instanceof ImBoolVal) {
                boolean b2 = ((ImBoolVal) right).getValB();
                wasViable = replaceBoolTerm(opc, left, b2, false);
            } else if (isNumberLiteral(left) && isNumberLiteral(right)) {
                // If any side is real (or the op is a real op), fold as real; otherwise fold as int.
                boolean foldAsReal =
                    (left instanceof ImRealVal) ||
                        (right instanceof ImRealVal) ||
                        opc.getOp() == WurstOperator.DIV_REAL ||
                        opc.getOp() == WurstOperator.MOD_REAL;

                if (foldAsReal) {
                    wasViable = optimizeRealRealMixed(opc, wasViable, left, right);
                } else if (left instanceof ImIntVal && right instanceof ImIntVal) {
                    wasViable = optimizeIntInt(opc, wasViable, (ImIntVal) left, (ImIntVal) right);
                } else {
                    wasViable = false; // unknown numeric combo
                }
            } else if (left instanceof ImStringVal) {
                // Fold "" + expr  =>  expr
                if (opc.getOp() == WurstOperator.PLUS
                    && ((ImStringVal) left).getValS().isEmpty()) {
                    right.setParent(null);
                    opc.replaceBy(right);
                    wasViable = true;
                } else {
                    wasViable = false;
                }
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
            } else {
                wasViable = false;
            }
        }

        // Unary
        else {
            ImExpr expr = opc.getArguments().get(0);
            if (opc.getOp() == WurstOperator.UNARY_MINUS && expr instanceof ImIntVal) {
                int v = ((ImIntVal) expr).getValI();
                if (v != Integer.MIN_VALUE && v <= 0) {
                    opc.replaceBy(JassIm.ImIntVal(-v));
                } else {
                    wasViable = false;
                }
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
                        if (isJassRealEquality(inner)) {
                            // not (a == b) is not a != b here, nor the other way round
                            wasViable = false;
                        } else {
                            opc.replaceBy(JassIm.ImOperatorCall(oppositeOperator(inner.getOp()), JassIm.ImExprs(inner.getArguments().removeAll())));
                        }
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
                WLogger.info("opcall rewrite: " + opc);
            }
        }

    }

    /** An == or != between reals on the Jass target, where == has a tolerance and != has none. */
    private boolean isJassRealEquality(ImOperatorCall opc) {
        return jassTarget
            && (opc.getOp() == WurstOperator.EQ || opc.getOp() == WurstOperator.NOTEQ)
            && opc.getArguments().stream().anyMatch(arg -> TypesHelper.isRealType(arg.attrTyp()));
    }

    /** The literal with the most decimals, and the largest one, measured to read exactly in Jass. */
    private static final int MAX_EXACT_JASS_REAL_DECIMALS = 5;
    private static final BigDecimal MAX_EXACT_JASS_REAL = new BigDecimal("2147483520");

    /**
     * Folds a real operation for Jass only where the game computes exactly the folded value.
     * Measured on the 3.0.0 client, the Jass literal parser does not round to the nearest float:
     * {@code 0.1} reads one float high and {@code 1.1} one float low, while short exact binary
     * fractions such as {@code 0.5}, {@code 0.75}, {@code 123456.78125} and {@code 2147483520.0}
     * read exactly. So both operands must be such literals, and an arithmetic result must be exact
     * and printable as one; otherwise the map would compute a different float than the folded one.
     * Two distinct such operands differ by at least 1/32, so the tolerance of Jass {@code ==} never
     * decides a folded comparison.
     */
    private boolean foldRealForJass(ImOperatorCall opc, ImExpr left, ImExpr right) {
        BigDecimal a = exactJassReal(left);
        BigDecimal b = exactJassReal(right);
        if (a == null || b == null) {
            return false;
        }
        int cmp = a.compareTo(b);
        BigDecimal exact;
        switch (opc.getOp()) {
            case GREATER:
                opc.replaceBy(JassIm.ImBoolVal(cmp > 0));
                return true;
            case GREATER_EQ:
                opc.replaceBy(JassIm.ImBoolVal(cmp >= 0));
                return true;
            case LESS:
                opc.replaceBy(JassIm.ImBoolVal(cmp < 0));
                return true;
            case LESS_EQ:
                opc.replaceBy(JassIm.ImBoolVal(cmp <= 0));
                return true;
            case EQ:
                opc.replaceBy(JassIm.ImBoolVal(cmp == 0));
                return true;
            case NOTEQ:
                opc.replaceBy(JassIm.ImBoolVal(cmp != 0));
                return true;
            case PLUS:
                exact = a.add(b);
                break;
            case MINUS:
                exact = a.subtract(b);
                break;
            case MULT:
                exact = a.multiply(b);
                break;
            case DIV_INT:
            case DIV_REAL:
                if (b.signum() == 0) {
                    return false;
                }
                try {
                    exact = a.divide(b);
                } catch (ArithmeticException nonTerminating) {
                    return false;
                }
                break;
            default:
                // MOD_REAL becomes ModuloReal, several float operations in game
                return false;
        }
        if (exact.signum() == 0 && (a.signum() < 0 || b.signum() < 0 || opc.getOp() == WurstOperator.MINUS)) {
            // the sign of this zero depends on the game's rounding
            return false;
        }
        if (!isExactJassReal(exact)) {
            return false;
        }
        BigDecimal plain = exact.stripTrailingZeros();
        opc.replaceBy(JassIm.ImRealVal((plain.scale() < 1 ? plain.setScale(1) : plain).toPlainString()));
        return true;
    }

    /** The value of a number literal that Jass reads exactly, or null; see {@link #foldRealForJass}. */
    private static @org.eclipse.jdt.annotation.Nullable BigDecimal exactJassReal(ImExpr e) {
        if (e instanceof ImIntVal i) {
            // up to 2^24 an int converts to a real exactly
            return Math.abs((long) i.getValI()) <= (1 << 24) ? BigDecimal.valueOf(i.getValI()) : null;
        }
        if (e instanceof ImRealVal r) {
            String text = r.getValR();
            int dot = text.indexOf('.');
            if (dot >= 0 && text.length() - dot - 1 > MAX_EXACT_JASS_REAL_DECIMALS) {
                return null;
            }
            try {
                BigDecimal value = new BigDecimal(text);
                return isExactJassReal(value) ? value : null;
            } catch (NumberFormatException notDecimal) {
                return null;
            }
        }
        return null;
    }

    private static boolean isExactJassReal(BigDecimal value) {
        return value.abs().compareTo(MAX_EXACT_JASS_REAL) <= 0
            && value.stripTrailingZeros().scale() <= MAX_EXACT_JASS_REAL_DECIMALS
            && new BigDecimal(value.floatValue()).compareTo(value) == 0;
    }

    private boolean optimizeRealRealMixed(ImOperatorCall opc, boolean wasViable, ImExpr left, ImExpr right) {
        if (jassTarget) {
            return foldRealForJass(opc, left, right);
        }
        return foldRealForLua(opc, left, right);
    }

    /**
     * Folds a real operation for Lua, whose reals are doubles: Lua reads a literal as the nearest
     * double and computes in double, and measured on the 3.0.0 client the game's natives (R2I)
     * also receive that double unrounded. So the fold computes in double too and prints a literal
     * which reads back as exactly the result; computing in 32-bit floats instead would make a map
     * compute something else wherever an expression happened to be folded.
     */
    private boolean foldRealForLua(ImOperatorCall opc, ImExpr left, ImExpr right) {
        double a = asDouble(left);
        double b = asDouble(right);
        double result;
        switch (opc.getOp()) {
            case GREATER:
                opc.replaceBy(JassIm.ImBoolVal(a > b));
                return true;
            case GREATER_EQ:
                opc.replaceBy(JassIm.ImBoolVal(a >= b));
                return true;
            case LESS:
                opc.replaceBy(JassIm.ImBoolVal(a < b));
                return true;
            case LESS_EQ:
                opc.replaceBy(JassIm.ImBoolVal(a <= b));
                return true;
            case EQ:
                opc.replaceBy(JassIm.ImBoolVal(a == b));
                return true;
            case NOTEQ:
                opc.replaceBy(JassIm.ImBoolVal(a != b));
                return true;
            case PLUS:
                result = a + b;
                break;
            case MINUS:
                result = a - b;
                break;
            case MULT:
                result = a * b;
                break;
            case DIV_REAL:
                if (b == 0) {
                    return false;
                }
                result = a / b;
                break;
            default:
                // MOD_REAL and DIV_INT are lowered to helper calls before this pass runs on Lua
                return false;
        }
        String literal = luaRealLiteral(result);
        if (literal == null) {
            return false;
        }
        opc.replaceBy(JassIm.ImRealVal(literal));
        return true;
    }

    private static double asDouble(ImExpr e) {
        if (e instanceof ImRealVal r) {
            return Double.parseDouble(r.getValR());
        }
        return ((ImIntVal) e).getValI();
    }

    /**
     * The shortest literal which Lua reads back as exactly {@code value}, or null when there is none.
     * A non-finite result (an overflow to infinity, or NaN) has no literal, so the operation is left
     * for the game. The literal always has a point or an exponent, so Lua reads it as a float.
     */
    private static @org.eclipse.jdt.annotation.Nullable String luaRealLiteral(double value) {
        if (!Double.isFinite(value)) {
            return null;
        }
        // the fewest digits which read back as the same double
        String scientific = Double.toString(value);
        if (scientific.indexOf('E') < 0) {
            return scientific;
        }
        String plain = new BigDecimal(scientific).stripTrailingZeros().toPlainString();
        if (plain.indexOf('.') < 0) {
            plain += ".0";
        }
        return plain.length() <= scientific.length() ? plain : scientific;
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
                    resultVal = WurstOperator.moduloInteger(i1, i2);
                    isArithmetic = true;
                }
                break;
            case JASS_MOD_INT:
                if (i2 != 0) {
                    resultVal = WurstOperator.jassModuloInteger(i1, i2);
                    isArithmetic = true;
                }
                break;
            case DIV_INT:
                if (i2 != 0) {
                    resultVal = i1 / i2;
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
            opc.replaceBy(JassIm.ImIntVal(resultVal));
        } else {
            wasViable = false;
        }
        return wasViable;
    }

    private boolean replaceBoolTerm(ImOperatorCall opc, ImExpr expr, boolean constant, boolean constantOnLeft) {
        switch (opc.getOp()) {
            case OR:
                if (constant) {
                    if (!constantOnLeft) {
                        // x or true still evaluates x. Replacing the expression
                        // with true would discard calls, traps, allocations, and
                        // local-player-dependent reads.
                        return false;
                    }
                    opc.replaceBy(JassIm.ImBoolVal(true));
                } else {
                    expr.setParent(null);
                    opc.replaceBy(expr);
                }
                break;
            case AND:
                if (constant) {
                    expr.setParent(null);
                    opc.replaceBy(expr);
                } else {
                    if (!constantOnLeft) {
                        // x and false still evaluates x.
                        return false;
                    }
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
