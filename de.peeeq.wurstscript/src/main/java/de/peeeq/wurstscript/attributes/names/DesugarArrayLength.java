package de.peeeq.wurstscript.attributes.names;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.types.WurstTypeArray;

/**
 * Rewrites `x.length` member-var accesses into the dedicated ExprArrayLength(x) node.
 * This is purely syntactic; type checking will validate later.
 */
public final class DesugarArrayLength extends Element.DefaultVisitor {
    public void run(WurstModel model) { if (model != null) {
        model.accept(this);
    } }

    @Override public void visit(ExprMemberVarDot e) {
        if (e.getLeft() instanceof ExprVarAccess va && isLength(e.getVarNameId())) {
            if (va.attrTyp() instanceof WurstTypeArray) {
                // Use a COPY of the left expression to avoid re-parenting the same node
                Expr leftCopy = e.getLeft().copy();
                e.replaceBy(Ast.ExprArrayLength(e.attrSource(), leftCopy));
                return; // don't descend into the replaced subtree
            }
        }
        super.visit(e);
    }

    @Override public void visit(ExprMemberVarDotDot e) {
        if (e instanceof ImVar && isLength(e.getVarNameId())) {
            Expr leftCopy = e.getLeft().copy();
            e.replaceBy(Ast.ExprArrayLength(e.attrSource(), leftCopy));
            return;
        }
        super.visit(e);
    }

    private static boolean isLength(Identifier id) {
        String n = id.getName();
        return n != null && n.equals("length");
    }
}
