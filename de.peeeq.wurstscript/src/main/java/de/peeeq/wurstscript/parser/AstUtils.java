package de.peeeq.wurstscript.parser;

import de.peeeq.wurstscript.ast.*;

/**
 *
 */
public class AstUtils {

    public boolean containsSuperCall(WStatements stmts) {
        boolean res[] = {false};
        stmts.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ExprFunctionCall c) {
                res[0] = true;
            }
        });
        return res[0];
    }
}
