package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Lowers loop-local method calls with exactly one possible implementation to ordinary function
 * calls on Lua.
 *
 * <p>The Lua emitter has always used the same direct-call fast path. Performing the lowering before
 * optimization exposes hot calls to the ordinary inliner without guessing about receiver types or
 * generated method names. Calls outside loops, and calls which can participate in virtual dispatch,
 * remain untouched to avoid broad code-shape churn for a speculative gain.
 */
public final class LuaMethodCallLowering {

    private LuaMethodCallLowering() {
    }

    public static int transform(ImProg prog) {
        return lowerLoopCalls(prog).size();
    }

    /** A method call and the direct function call that replaced it. */
    public record Lowered(ImMethodCall from, ImFunctionCall to) {
    }

    /**
     * Lowers every monomorphic method call which is inside a loop and reports each replacement.
     * Running it again after inlining finds the calls which inlining moved into a loop: a delegating
     * method such as {@code op_index -> get} inlined into a loop leaves the inner method call behind
     * in that loop.
     */
    public static List<Lowered> lowerLoopCalls(ImProg prog) {
        List<ImMethodCall> calls = new ArrayList<>();
        prog.accept(new ImProg.DefaultVisitor() {
            @Override
            public void visit(ImMethodCall call) {
                super.visit(call);
                if (isInsideLoop(call) && canLowerDirectly(call.getMethod())) {
                    calls.add(call);
                }
            }
        });

        List<Lowered> lowered = new ArrayList<>();
        for (ImMethodCall call : calls) {
            lowered.add(new Lowered(call, lower(call)));
        }
        return lowered;
    }

    private static boolean isInsideLoop(ImMethodCall call) {
        Element owner = call.getParent();
        while (owner != null && !(owner instanceof ImFunction)) {
            if (owner instanceof ImLoop || owner instanceof ImVarargLoop) {
                return true;
            }
            owner = owner.getParent();
        }
        return false;
    }

    public static boolean canLowerDirectly(ImMethod method) {
        return method != null
            && !method.getIsAbstract()
            && method.getImplementation() != null
            && method.getSubMethods().isEmpty();
    }

    private static ImFunctionCall lower(ImMethodCall call) {
        ImExpr receiver = call.getReceiver();
        receiver.setParent(null);
        ImExprs arguments = JassIm.ImExprs(receiver);
        arguments.addAll(call.getArguments().removeAll());
        ImFunctionCall direct = JassIm.ImFunctionCall(call.getTrace(), call.getMethod().getImplementation(),
            JassIm.ImTypeArguments(call.getTypeArguments().removeAll()), arguments,
            call.getTuplesEliminated(), CallType.NORMAL);
        call.replaceBy(direct);
        return direct;
    }
}
