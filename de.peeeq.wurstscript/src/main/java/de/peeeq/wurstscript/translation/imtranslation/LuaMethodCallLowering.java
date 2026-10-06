package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Lowers method calls with exactly one possible implementation to ordinary function calls on Lua.
 *
 * <p>The Lua emitter has always used the same direct-call fast path. Performing the lowering before
 * optimization exposes every such call to the ordinary inliner, which only looks at function calls:
 * a getter called outside a loop used to reach it as a method call and stay a call, however small.
 * Calls which can participate in virtual dispatch remain untouched.
 */
public final class LuaMethodCallLowering {

    private LuaMethodCallLowering() {
    }

    /** Lowers every monomorphic method call and returns how many there were. */
    public static int transform(ImProg prog) {
        List<ImMethodCall> calls = new ArrayList<>();
        prog.accept(new ImProg.DefaultVisitor() {
            @Override
            public void visit(ImMethodCall call) {
                super.visit(call);
                if (canLowerDirectly(call.getMethod())) {
                    calls.add(call);
                }
            }
        });

        for (ImMethodCall call : calls) {
            lower(call);
        }
        return calls.size();
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
