package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;

public final class FlattenAttributes {
    private FlattenAttributes() {}

    // Tiny structural check mirroring exprToStatements behavior for AND/OR RHS.
    private static boolean rhsWouldEmitStatements(ImExpr rhs) {
        // Fast positives that become statements:
        if (rhs instanceof ImFunctionCall)  return true;
        if (rhs instanceof ImMethodCall)    return true;
        if (rhs instanceof ImDealloc)       return true;
        if (rhs instanceof ImStatementExpr) return true;

        // Recurse shallowly
        for (int i = 0, n = rhs.size(); i < n; i++) {
            Element child = rhs.get(i);
            if (child instanceof ImExpr && rhsWouldEmitStatements((ImExpr) child)) return true;
        }
        return false;
    }

    public static boolean needsShortCircuitLowering(ImOperatorCall oc) {
        WurstOperator op = oc.getOp();
        if (op != WurstOperator.AND && op != WurstOperator.OR) return false;
        ImExprs args = oc.getArguments();
        if (args.size() < 2) return false;
        ImExpr rhs = args.get(1);
        // Only lower AND/OR when RHS itself would emit statements.
        return rhsWouldEmitStatements(rhs);
    }
}
