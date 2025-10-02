package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.WurstTypeBool;

/**
 * Flattens short-circuiting operators AND and OR into If-statements.
 * This is done to prepare the code for the inliner, ensuring that side-effects
 * in the right-hand-side of the operator are only executed when they should be.
 */
public class FlattenImOperatorCalls {

    public static void flatten(ImFunction f) {
        // We can visit the function body directly, as we are replacing elements in-place.
        flatten(f.getBody(), f);
    }

    private static void flatten(Element e, ImFunction f) {
        // Recurse first to handle nested operators
        for (int i = 0; i < e.size(); i++) {
            flatten(e.get(i), f);
        }

        // Now check if the current element is an operator we need to flatten
        if (e instanceof ImOperatorCall) {
            ImOperatorCall opCall = (ImOperatorCall) e;
            WurstOperator op = opCall.getOp();

            if (op == WurstOperator.AND || op == WurstOperator.OR) {
                // This operator needs to be flattened into an if-statement.
                // The replacement must be a Statement-Expression.
                ImExpr newExpr = flattenOperatorCall(opCall, f);
                e.replaceBy(newExpr);
            }
        }
    }

    private static ImExpr flattenOperatorCall(ImOperatorCall opCall, ImFunction f) {
        de.peeeq.wurstscript.ast.Element trace = opCall.attrTrace();
        ImExpr left = opCall.getArguments().get(0);
        ImExpr right = opCall.getArguments().get(1);

        // Create a temporary variable to hold the result of the boolean expression.
        ImVar tempVar = JassIm.ImVar(trace, WurstTypeBool.instance().imTranslateType(null), "short_circuit_temp", false);
        f.getLocals().add(tempVar);

        ImIf ifStmt;
        if (opCall.getOp() == WurstOperator.AND) {
            // Translate 'left and right' into:
            // if left then
            //   temp = right
            // else
            //   temp = false
            ImStmts thenBlock = JassIm.ImStmts(JassIm.ImSet(trace, JassIm.ImVarAccess(tempVar), right.copy()));
            ImStmts elseBlock = JassIm.ImStmts(JassIm.ImSet(trace, JassIm.ImVarAccess(tempVar), JassIm.ImBoolVal(false)));
            ifStmt = JassIm.ImIf(trace, left.copy(), thenBlock, elseBlock);
        } else { // WurstOperator.OR
            // Translate 'left or right' into:
            // if left then
            //   temp = true
            // else
            //   temp = right
            ImStmts thenBlock = JassIm.ImStmts(JassIm.ImSet(trace, JassIm.ImVarAccess(tempVar), JassIm.ImBoolVal(true)));
            ImStmts elseBlock = JassIm.ImStmts(JassIm.ImSet(trace, JassIm.ImVarAccess(tempVar), right.copy()));
            ifStmt = JassIm.ImIf(trace, left.copy(), thenBlock, elseBlock);
        }

        // The final result is a statement-expression containing the if-statement,
        // which evaluates to the value of the temporary variable.
        return JassIm.ImStatementExpr(JassIm.ImStmts(ifStmt), JassIm.ImVarAccess(tempVar));
    }
}
