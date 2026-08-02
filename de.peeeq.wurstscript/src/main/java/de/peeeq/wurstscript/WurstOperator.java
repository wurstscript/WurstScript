package de.peeeq.wurstscript;

import de.peeeq.wurstscript.attributes.AttrFuncDef;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassOpBinary;
import de.peeeq.wurstscript.jassAst.JassOpUnary;
import de.peeeq.wurstscript.luaAst.LuaAst;
import de.peeeq.wurstscript.luaAst.LuaOpBinary;
import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Supplier;

public enum WurstOperator {
    OR("or", 2),
    AND("and", 2),
    EQ("==", 2),
    NOTEQ("!=", 2),
    LESS_EQ("<=", 2),
    LESS("<", 2),
    GREATER_EQ(">=", 2),
    GREATER(">", 2),
    PLUS("+", 2),
    MINUS("-", 2),
    MULT("*", 2),
    DIV_REAL("/", 2),
    DIV_INT("div", 2),
    MOD_REAL("%", 2),
    MOD_INT("mod", 2),
    JASS_MOD_INT("%", 2),
    NOT("not", 1),
    UNARY_MINUS("-", 1);

    private final String rep;
    private final int numArgs;

    WurstOperator(String rep, int numArgs) {
        this.rep = rep;
        this.numArgs = numArgs;
    }


    public boolean isBinaryOp() {
        return numArgs == 2;
    }

    public boolean isUnaryOp() {
        return numArgs == 1;
    }

    @Override
    public String toString() {
        return rep;
    }

    public JassOpBinary jassTranslateBinary() {
        switch (this) {
            case AND:
                return JassAst.JassOpAnd();
            case DIV_INT:
                return JassAst.JassOpDiv();
            case DIV_REAL:
                return JassAst.JassOpDiv();
            case EQ:
                return JassAst.JassOpEquals();
            case GREATER:
                return JassAst.JassOpGreater();
            case GREATER_EQ:
                return JassAst.JassOpGreaterEq();
            case LESS:
                return JassAst.JassOpLess();
            case LESS_EQ:
                return JassAst.JassOpLessEq();
            case MINUS:
                return JassAst.JassOpMinus();
            case MOD_INT:
            case MOD_REAL:
                throw new Error("Cannot translate modulo");
            case JASS_MOD_INT:
                return JassAst.JassOpMod();
            case MULT:
                return JassAst.JassOpMult();
            case NOTEQ:
                return JassAst.JassOpUnequals();
            case OR:
                return JassAst.JassOpOr();
            case PLUS:
                return JassAst.JassOpPlus();
            case NOT:
            case UNARY_MINUS:
        }
        throw new Error("cannot translate " + this);
    }

    public LuaOpBinary luaTranslateBinary() {
        switch (this) {
            case AND:
                return LuaAst.LuaOpAnd();
            case DIV_REAL:
                return LuaAst.LuaOpDiv();
            case EQ:
                return LuaAst.LuaOpEquals();
            case GREATER:
                return LuaAst.LuaOpGreater();
            case GREATER_EQ:
                return LuaAst.LuaOpGreaterEq();
            case LESS:
                return LuaAst.LuaOpLess();
            case LESS_EQ:
                return LuaAst.LuaOpLessEq();
            case MINUS:
                return LuaAst.LuaOpMinus();
            case MOD_REAL:
                return LuaAst.LuaOpMod();
            case MOD_INT:
            case JASS_MOD_INT:
                throw new Error("Cannot translate modulo int");
            case MULT:
                return LuaAst.LuaOpMult();
            case NOTEQ:
                return LuaAst.LuaOpUnequals();
            case OR:
                return LuaAst.LuaOpOr();
            case PLUS:
                return LuaAst.LuaOpPlus();
            case NOT:
            case UNARY_MINUS:
        }
        throw new Error("cannot translate " + this);
    }

    public ILconst evaluateBinaryOperator(ILconst left,
                                          Supplier<ILconst> right) {
        switch (this) {
            case AND:
                return evaluateBooleanAnd((ILconstBool) left, right);
            case OR:
                return evaluateBooleanOr((ILconstBool) left, right);
            case DIV_INT:
                return new ILconstInt(((ILconstInt) left).getVal() / ((ILconstInt) right.get()).getVal());
            case DIV_REAL:
                return new ILconstReal(getReal(left) / getReal(right.get()));
            case EQ:
                return evaluateEquality(left, right.get(), false);
            case GREATER:
                return ((ILconstNum) left).greater((ILconstNum) right.get());
            case GREATER_EQ:
                return ((ILconstNum) left).greaterEq((ILconstNum) right.get());
            case LESS:
                return ((ILconstNum) left).less((ILconstNum) right.get());
            case LESS_EQ:
                return ((ILconstNum) left).lessEq((ILconstNum) right.get());
            case MINUS:
                return ((ILconstNum) left).sub((ILconstNum) right.get());
            case MOD_INT:
                return new ILconstInt(moduloInteger(((ILconstInt) left).getVal(), ((ILconstInt) right.get()).getVal()));
            case JASS_MOD_INT:
                return new ILconstInt(jassModuloInteger(((ILconstInt) left).getVal(), ((ILconstInt) right.get()).getVal()));
            case MOD_REAL:
                return new ILconstReal(moduloReal(getReal(left), getReal(right.get())));
            case MULT:
                return ((ILconstNum) left).mul((ILconstNum) right.get());
            case NOTEQ:
                return evaluateEquality(left, right.get(), true);
            case PLUS:
                return ((ILconstAddable) left).add((ILconstAddable) right.get());
            case NOT:
            case UNARY_MINUS:
                break;
        }
        throw new Error("cannot evaluate " + this);

    }

    private static ILconstBool evaluateBooleanAnd(ILconstBool left, Supplier<ILconst> right) {
        if (!left.getVal()) {
            if (left.isRuntimeValKnown() && !left.getRuntimeVal()) {
                return ILconstBool.FALSE;
            }
            return ILconstBool.withUnknownRuntimeValue(false);
        }
        ILconstBool rightBool = (ILconstBool) right.get();
        boolean value = rightBool.getVal();
        if (left.isRuntimeValKnown()) {
            if (!left.getRuntimeVal()) {
                return ILconstBool.withRuntimeValue(value, false);
            }
            if (rightBool.isRuntimeValKnown()) {
                return ILconstBool.withRuntimeValue(value, rightBool.getRuntimeVal());
            }
        } else if (rightBool.isRuntimeValKnown() && !rightBool.getRuntimeVal()) {
            return ILconstBool.withRuntimeValue(value, false);
        }
        return ILconstBool.withUnknownRuntimeValue(value);
    }

    private static ILconstBool evaluateBooleanOr(ILconstBool left, Supplier<ILconst> right) {
        if (left.getVal()) {
            if (left.isRuntimeValKnown() && left.getRuntimeVal()) {
                return ILconstBool.TRUE;
            }
            return ILconstBool.withUnknownRuntimeValue(true);
        }
        ILconstBool rightBool = (ILconstBool) right.get();
        boolean value = rightBool.getVal();
        if (left.isRuntimeValKnown()) {
            if (left.getRuntimeVal()) {
                return ILconstBool.withRuntimeValue(value, true);
            }
            if (rightBool.isRuntimeValKnown()) {
                return ILconstBool.withRuntimeValue(value, rightBool.getRuntimeVal());
            }
        } else if (rightBool.isRuntimeValKnown() && rightBool.getRuntimeVal()) {
            return ILconstBool.withRuntimeValue(value, true);
        }
        return ILconstBool.withUnknownRuntimeValue(value);
    }

    private static ILconstBool evaluateEquality(ILconst left, ILconst right, boolean negated) {
        boolean value = left.equals(right) != negated;
        if (left instanceof ILconstBool && right instanceof ILconstBool) {
            ILconstBool leftBool = (ILconstBool) left;
            ILconstBool rightBool = (ILconstBool) right;
            if (leftBool.isRuntimeValKnown() && rightBool.isRuntimeValKnown()) {
                boolean runtimeValue = (leftBool.getRuntimeVal() == rightBool.getRuntimeVal()) != negated;
                return ILconstBool.withRuntimeValue(value, runtimeValue);
            }
            return ILconstBool.withUnknownRuntimeValue(value);
        }
        return ILconstBool.instance(value);
    }

    /**
     * Reference semantics for Wurst's integer {@code mod}: matches Blizzard.j's
     * ModuloInteger (truncated remainder, plus divisor if the remainder is
     * negative). All constant folding, interpretation, and backends must agree
     * with this.
     */
    public static int moduloInteger(int a, int b) {
        int r = a % b;
        if (r < 0) {
            r += b;
        }
        return r;
    }

    /** Native Jass {@code %}: integer-only remainder truncated toward zero. */
    public static int jassModuloInteger(int a, int b) {
        return a % b;
    }

    /** Reference semantics for Wurst's real {@code mod}; see {@link #moduloInteger}. */
    public static float moduloReal(float a, float b) {
        float r = a % b;
        if (r < 0) {
            r += b;
        }
        return r;
    }

    private static float getReal(ILconst c) {
        if (c instanceof ILconstReal) {
            return ((ILconstReal) c).getVal();
        } else if (c instanceof ILconstInt) {
            return ((ILconstInt) c).getVal();
        }
        throw new Error();
    }

    @SuppressWarnings("incomplete-switch")
    public ILconst evaluateUnaryOperator(ILconst e) {
        switch (this) {
            case NOT:
                return ((ILconstBool) e).negate();
            case UNARY_MINUS:
                return ((ILconstNum) e).negate();
        }
        throw new Error("cannot evaluate " + this);
    }

    @SuppressWarnings("incomplete-switch")
    public @Nullable String getOverloadingFuncName() {
        switch (this) {
            case PLUS:
                return AttrFuncDef.overloadingPlus;
            case MINUS:
                return AttrFuncDef.overloadingMinus;
            case DIV_REAL:
                return AttrFuncDef.overloadingDiv;
            case MULT:
                return AttrFuncDef.overloadingMult;
        }
        return null;
    }

    @SuppressWarnings("incomplete-switch")
    public JassOpUnary jassTranslateUnary() {
        switch (this) {
            case NOT:
                return JassAst.JassOpNot();
            case UNARY_MINUS:
                return JassAst.JassOpMinus();
        }
        throw new Error("unhandled operator " + this);
    }

    /**
     * an operator is lazy if it evaluates its second argument only
     * when its first argument evaluates to a certain value
     */
    public boolean isLazy() {
        return this == OR || this == AND;
    }


}
