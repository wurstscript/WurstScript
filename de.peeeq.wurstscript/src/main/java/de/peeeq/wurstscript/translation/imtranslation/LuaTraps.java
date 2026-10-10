package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;

import java.util.function.Predicate;

/**
 * What can raise a Lua error when it is evaluated, and so stop the thread at that point.
 * <p>
 * A pass which removes or moves an evaluation must not move it past such a point: the code after it would then run
 * where it did not, or a value would be left which the thread never saw replaced. Beyond what stops the thread on Jass
 * too ({@link Flatten#mayStopTheThread}), Lua raises where Jass reads a default through an object which is null or
 * freed: an array field is {@code storage[o][i]}, which indexes nil, an object's type id is read from its class
 * descriptor, which is nil, and a field read gives nil, on which arithmetic and orderings raise.
 * <p>
 * The answer is decided by the kind of each node, and a kind not named here can raise, so a new one is safe until it
 * is shown not to.
 */
public final class LuaTraps {

    private LuaTraps() {
    }

    /**
     * Whether evaluating {@code e}, an expression or a statement which is not a nested block, can raise. It cannot
     * when it consists only of constants, variable reads, field reads without an index, array reads, allocations, type
     * ids of a class, instanceof tests and tuples, and of operators other than a division, and arithmetic or an
     * ordering on a value which may be nil: a field read, or a local which {@code maybeNil} says holds one. Anything
     * else can: a call, a deallocation (a double free fails), a type id read through an object, a cast, an array field
     * read.
     */
    public static boolean mayRaise(Element e, Predicate<ImVar> maybeNil) {
        if (e instanceof ImIntVal || e instanceof ImRealVal || e instanceof ImStringVal || e instanceof ImBoolVal
            || e instanceof ImFuncRef || e instanceof ImNull || e instanceof ImVarAccess || e instanceof ImAlloc
            || e instanceof ImTypeIdOfClass) {
            return false;
        } else if (e instanceof ImMemberAccess access) {
            return !access.getIndexes().isEmpty() || mayRaise(access.getReceiver(), maybeNil);
        } else if (e instanceof ImVarArrayAccess access) {
            return anyMayRaise(access.getIndexes(), maybeNil);
        } else if (e instanceof ImInstanceof instanceOf) {
            // isInstanceOf answers false for an object without a class descriptor.
            return mayRaise(instanceOf.getObj(), maybeNil);
        } else if (e instanceof ImTupleExpr tuple) {
            return anyMayRaise(tuple.getExprs(), maybeNil);
        } else if (e instanceof ImTupleSelection selection) {
            return mayRaise(selection.getTupleExpr(), maybeNil);
        } else if (e instanceof ImOperatorCall call) {
            if (Flatten.mayStopTheThread(call)) {
                return true;
            }
            if (!neverRaisesOnNil(call.getOp())) {
                for (ImExpr operand : call.getArguments()) {
                    if (mayBeNil(operand) || (operand instanceof ImVarAccess v && maybeNil.test(v.getVar()))) {
                        return true;
                    }
                }
            }
            return anyMayRaise(call.getArguments(), maybeNil);
        } else if (e instanceof ImSet set) {
            return mayRaise(set.getLeft(), maybeNil) || mayRaise(set.getRight(), maybeNil);
        }
        return true;
    }

    private static boolean anyMayRaise(ImExprs exprs, Predicate<ImVar> maybeNil) {
        for (ImExpr e : exprs) {
            if (mayRaise(e, maybeNil)) {
                return true;
            }
        }
        return false;
    }

    /** Whether the expression {@code e} reads a value which is nil when its object is null: a field read. */
    public static boolean mayBeNil(ImExpr e) {
        return e instanceof ImMemberAccess;
    }

    private static boolean neverRaisesOnNil(WurstOperator op) {
        return op == WurstOperator.EQ || op == WurstOperator.NOTEQ || op == WurstOperator.AND
            || op == WurstOperator.OR || op == WurstOperator.NOT;
    }
}
