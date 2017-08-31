package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.CallSignature;

public class AttrCallSignature {

    public static CallSignature calculate(ExprFunctionCall c) {
        Expr receiver = null;
        if (c.attrImplicitParameter() instanceof Expr) {
            receiver = (Expr) c.attrImplicitParameter();
        }
        return new CallSignature(receiver, ArgTypes.fromArguments(c.getArgs()));
    }

    public static CallSignature calculate(ExprMemberMethod c) {
        Expr receiver = c.getLeft();
        if (receiver.attrTyp().isStaticRef()) {
            // if we use a static ref like A.foo()
            // then there is no real receiver
            receiver = null;
        }
        return new CallSignature(c.attrImplicitParameter(), ArgTypes.fromArguments(c.getArgs()));
    }

    public static CallSignature calculate(ExprNewObject c) {
        return new CallSignature(null, ArgTypes.fromArguments(c.getArgs()));
    }


}
