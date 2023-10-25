package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.types.CallSignature;

public class AttrCallSignature {

    public static CallSignature calculate(ExprFunctionCall c) {
        Expr receiver = null;
        if (c.attrImplicitParameter() instanceof Expr) {
            receiver = (Expr) c.attrImplicitParameter();
        }
        return new CallSignature(receiver, c.getArgs());
    }

    public static CallSignature calculate(ExprMemberMethod c) {
        return new CallSignature(c.attrImplicitParameter(), c.getArgs());
    }

    public static CallSignature calculate(ExprNewObject c) {
        return new CallSignature(null, c.getArgs());
    }

}
