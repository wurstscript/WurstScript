package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;

/**
 * What can raise a Lua error when it is evaluated, and so stop the thread at that point.
 * <p>
 * A pass which removes or moves an evaluation must not move it past such a point: the code after it would then run
 * where it did not, or a value would be left which the thread never saw replaced. Beyond what stops the thread on Jass
 * too ({@link Flatten#mayStopTheThread}), Lua raises where Jass reads a default through an object which is null or
 * freed: an array field is {@code storage[o][i]}, which indexes nil, an object's type id is read from its class
 * descriptor, which is nil, and a field read gives nil, on which arithmetic and orderings raise, as does a table
 * write under a nil key. A nil read can reach an operand through any variable, so whether an operand is nil is not
 * traced: arithmetic, an ordering and an array write count as raising unless their operands are literals.
 * <p>
 * The answer is decided by the kind of each node, and a kind not named here counts as raising until it is shown not
 * to.
 */
public final class LuaTraps {

    private LuaTraps() {
    }

    /**
     * Whether evaluating {@code e}, an expression or a statement which is not a nested block, can raise. It cannot
     * when it consists only of constants, variable reads, field reads without an index, array reads, allocations, type
     * ids of a class, instanceof tests and tuples, of {@code ==}, {@code !=}, {@code and}, {@code or} and {@code not},
     * of arithmetic and orderings on literals, and of assignments to a variable or a field. Anything else can: a call,
     * a deallocation (a double free fails), a division, arithmetic or an ordering on a value which is not a literal (it
     * may be nil), an array write under an index which is not a literal, a type id read through an object, a cast, an
     * array field read. A field write through an object which may be null raises too; that is for the caller to tell.
     */
    public static boolean mayRaise(Element e) {
        if (e instanceof ImIntVal || e instanceof ImRealVal || e instanceof ImStringVal || e instanceof ImBoolVal
            || e instanceof ImFuncRef || e instanceof ImNull || e instanceof ImVarAccess || e instanceof ImAlloc
            || e instanceof ImTypeIdOfClass) {
            return false;
        } else if (e instanceof ImMemberAccess access) {
            return !access.getIndexes().isEmpty() || mayRaise(access.getReceiver());
        } else if (e instanceof ImVarArrayAccess access) {
            return anyMayRaise(access.getIndexes());
        } else if (e instanceof ImInstanceof instanceOf) {
            // isInstanceOf answers false for an object without a class descriptor.
            return mayRaise(instanceOf.getObj());
        } else if (e instanceof ImTupleExpr tuple) {
            return anyMayRaise(tuple.getExprs());
        } else if (e instanceof ImTupleSelection selection) {
            return mayRaise(selection.getTupleExpr());
        } else if (e instanceof ImOperatorCall call) {
            if (Flatten.mayStopTheThread(call)) {
                return true;
            }
            if (!neverRaisesOnNil(call.getOp())) {
                for (ImExpr operand : call.getArguments()) {
                    if (!(operand instanceof ImConst)) {
                        return true;
                    }
                }
            }
            return anyMayRaise(call.getArguments());
        } else if (e instanceof ImSet set) {
            if (set.getLeft() instanceof ImVarArrayAccess target) {
                for (ImExpr index : target.getIndexes()) {
                    if (!(index instanceof ImConst)) {
                        return true;
                    }
                }
            }
            return mayRaise(set.getLeft()) || mayRaise(set.getRight());
        }
        return true;
    }

    private static boolean anyMayRaise(ImExprs exprs) {
        for (ImExpr e : exprs) {
            if (mayRaise(e)) {
                return true;
            }
        }
        return false;
    }

    private static boolean neverRaisesOnNil(WurstOperator op) {
        return op == WurstOperator.EQ || op == WurstOperator.NOTEQ || op == WurstOperator.AND
            || op == WurstOperator.OR || op == WurstOperator.NOT;
    }
}
