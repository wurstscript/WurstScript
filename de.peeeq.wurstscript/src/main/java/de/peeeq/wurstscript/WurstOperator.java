package de.peeeq.wurstscript;

import de.peeeq.wurstscript.attributes.AttrFuncDef;
import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassOpBinary;
import de.peeeq.wurstscript.jassAst.JassOpUnary;
import de.peeeq.wurstscript.luaAst.LuaAst;
import de.peeeq.wurstscript.luaAst.LuaOpBinary;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
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

    /**
     * Refuses to compare the stand-in for a type parameter's default, whichever side it is on.
     * <p>
     * The stand-in exists because the default of a value is computed by a static attribute, which
     * cannot see what the parameter is bound to. It answers "equal" only for another stand-in, so
     * comparing one against a real value is a wrong answer rather than an error. Doing this here
     * rather than in the value itself keeps it independent of operand order: only the left operand
     * gets asked, so `0 == unresolved` would otherwise go quietly false while `unresolved == 0`
     * complained.
     */
    private static void rejectUnresolvedDefault(ILconst left, ILconst right) {
        ILconstUnsafeDefault unresolved = left instanceof ILconstUnsafeDefault leftDefault ? leftDefault
            : right instanceof ILconstUnsafeDefault rightDefault ? rightDefault : null;
        if (unresolved == null || (left instanceof ILconstUnsafeDefault && right instanceof ILconstUnsafeDefault)) {
            return;
        }
        throw new InterpreterException("The default value of type parameter "
            + unresolved.getTypeVariable().getName()
            + " is not known here, so it cannot be compared to "
            + (unresolved == left ? right : left).print() + ".");
    }

    public ILconst evaluateBinaryOperator(ILconst left,
                                          Supplier<ILconst> right) {
        switch (this) {
            case AND:
                return ILconstBool.instance(((ILconstBool) left).getVal() && ((ILconstBool) right.get()).getVal());
            case OR:
                return ILconstBool.instance(((ILconstBool) left).getVal() || ((ILconstBool) right.get()).getVal());
            case DIV_INT:
                return new ILconstInt(((ILconstInt) left).getVal() / ((ILconstInt) right.get()).getVal());
            case DIV_REAL:
                return new ILconstReal(divideReal(getReal(left), getReal(right.get())));
            case EQ: {
                ILconst rightVal = right.get();
                rejectUnresolvedDefault(left, rightVal);
                return ILconstBool.instance(jassEquals(left, rightVal));
            }
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
            case NOTEQ: {
                ILconst rightVal = right.get();
                rejectUnresolvedDefault(left, rightVal);
                return ILconstBool.instance(!sameValue(left, rightVal));
            }
            case PLUS:
                return ((ILconstAddable) left).add((ILconstAddable) right.get());
            case NOT:
            case UNARY_MINUS:
                break;
        }
        throw new Error("cannot evaluate " + this);

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

    /**
     * Reference semantics for Wurst's real {@code mod}: Blizzard.j's ModuloReal, which a real modulo is on both
     * targets, {@code a - I2R(R2I(a / b)) * b} plus b when that is negative, each step in float as in the game.
     * That is not the exact remainder: the quotient rounds, and for a quotient past 2^24 the result can leave
     * [0, b) altogether. Measured on the 3.0.0 client, Jass and the same formula in the game's Lua agree on every
     * probed operand; math.fmod and Lua's float % do not (360 % 7 is 3.0000153 with either).
     */
    public static float moduloReal(float a, float b) {
        float quotient = divideReal(a, b);
        float r = a - (float) realToInt(quotient) * b;
        if (r < 0) {
            r += b;
        }
        return r;
    }

    /**
     * A real division. Measured on the 3.0.0 client, a real division by zero stops the thread in Jass ({@code 1. / 0.},
     * {@code 0. / 0.} and ModuloReal by 0 alike), so it is the same error as an integer one here, not an infinity.
     */
    public static float divideReal(float a, float b) {
        if (b == 0) {
            throw new ArithmeticException("/ by zero");
        }
        return a / b;
    }

    /**
     * Jass's R2I, measured on the 3.0.0 client: truncates, and saturates outside the 32-bit range (a computed 5000000320
     * is 2147483647, its negative -2147483648, a computed 1e20 2147483647), as Java's cast does. A real literal past
     * 2^31 is read wrapped by Jass (the literal 1e10 is 1410065408.), which R2I of it shows, not R2I itself.
     */
    public static int realToInt(double value) {
        return (int) value;
    }

    /**
     * Measured on the 3.0.0 client: Jass {@code ==} on two reals is true when they differ by at most
     * this much ({@code 1.0 == 1.0009} is true, {@code 1.0 == 1.0011} is false), while {@code !=} and
     * the orderings compare exactly. So for reals {@code a != b} is not {@code not (a == b)}. The cutoff
     * lies between the measured 0.0009 and 0.0011; the exact boundary was not measured. Lua compares
     * exactly with both operators.
     */
    public static final float JASS_REAL_EQUALITY_TOLERANCE = 0.001f;

    /** Jass {@code ==} on two reals; see {@link #JASS_REAL_EQUALITY_TOLERANCE}. */
    public static boolean jassRealEquals(float a, float b) {
        return a == b || Math.abs(a - b) <= JASS_REAL_EQUALITY_TOLERANCE;
    }

    /** Jass {@code ==}: reals, also inside tuples, by {@link #jassRealEquals}, everything else by {@link #sameValue}. */
    public static boolean jassEquals(ILconst left, ILconst right) {
        if (left instanceof ILconstReal l && right instanceof ILconstReal r) {
            return jassRealEquals(l.getVal(), r.getVal());
        }
        if (left instanceof ILconstTuple l && right instanceof ILconstTuple r) {
            List<ILconst> leftValues = l.values();
            List<ILconst> rightValues = r.values();
            if (leftValues.size() != rightValues.size()) {
                return false;
            }
            for (int i = 0; i < leftValues.size(); i++) {
                if (!jassEquals(leftValues.get(i), rightValues.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return sameValue(left, right);
    }

    /**
     * {@code ==} on two values other than reals and tuples. An object is also equal to the integer id which stands
     * for it. An old generic erases its value to that id when it is stored ({@code castTo int}), and the value leaves
     * the generic without a cast back, so before class elimination the interpreter meets the id where the program
     * compares the object. Everything else which takes a class value already reads the id as its object
     * ({@code ProgramState.toObject}), and on both targets an object is its id, so the comparison is by id too.
     */
    private static boolean sameValue(ILconst left, ILconst right) {
        if (left instanceof ILconstObject object && right instanceof ILconstInt id) {
            return object.getObjectId() == id.getVal();
        }
        if (left instanceof ILconstInt id && right instanceof ILconstObject object) {
            return object.getObjectId() == id.getVal();
        }
        return left.equals(right);
    }

    private static float getReal(ILconst c) {
        if (c instanceof ILconstReal iLconstReal) {
            return iLconstReal.getVal();
        } else if (c instanceof ILconstInt iLconstInt) {
            return iLconstInt.getVal();
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
