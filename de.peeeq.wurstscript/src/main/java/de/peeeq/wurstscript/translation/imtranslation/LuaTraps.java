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
 * freed: an array field is {@code storage[o][i]}, which indexes nil, and a field read gives nil, on which arithmetic
 * and orderings raise.
 */
public final class LuaTraps {

    private LuaTraps() {
    }

    /**
     * Whether evaluating {@code e} can raise: it contains a call (the callee can), a deallocation (a double free
     * fails), an operation which stops the thread on Jass too, a read of an array field, or arithmetic or an ordering
     * on a value which may be nil: a field read, or a local which {@code maybeNil} says holds one.
     */
    public static boolean mayRaise(Element e, Predicate<ImVar> maybeNil) {
        boolean[] raises = {false};
        e.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall call) {
                raises[0] = true;
            }

            @Override
            public void visit(ImMethodCall call) {
                raises[0] = true;
            }

            @Override
            public void visit(ImDealloc dealloc) {
                raises[0] = true;
            }

            @Override
            public void visit(ImMemberAccess access) {
                super.visit(access);
                if (!access.getIndexes().isEmpty()) {
                    raises[0] = true;
                }
            }

            @Override
            public void visit(ImOperatorCall call) {
                super.visit(call);
                if (Flatten.mayStopTheThread(call)) {
                    raises[0] = true;
                } else if (!neverRaisesOnNil(call.getOp())) {
                    for (ImExpr operand : call.getArguments()) {
                        if (operand instanceof ImMemberAccess
                            || (operand instanceof ImVarAccess v && maybeNil.test(v.getVar()))) {
                            raises[0] = true;
                        }
                    }
                }
            }
        });
        return raises[0];
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
