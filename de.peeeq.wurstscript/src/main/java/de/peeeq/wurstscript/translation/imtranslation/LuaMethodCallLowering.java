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

        for (ImMethodCall call : calls) {
            lower(call);
        }
        return calls.size();
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

    private static void lower(ImMethodCall call) {
        ImExpr receiver = call.getReceiver();
        receiver.setParent(null);
        ImExprs arguments = JassIm.ImExprs(receiver);
        arguments.addAll(call.getArguments().removeAll());
        call.replaceBy(JassIm.ImFunctionCall(call.getTrace(), call.getMethod().getImplementation(),
            JassIm.ImTypeArguments(call.getTypeArguments().removeAll()), arguments,
            call.getTuplesEliminated(), CallType.NORMAL));
    }
}
