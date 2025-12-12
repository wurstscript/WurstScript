package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.types.CallSignature;
import de.peeeq.wurstscript.types.WurstType;

public class AttrCallSignature {

    public static CallSignature calculate(ExprFunctionCall c) {
        Expr receiver = (c.attrImplicitParameter() instanceof Expr) ? (Expr) c.attrImplicitParameter() : null;
        WurstType hint = receiver != null ? receiver.attrTyp() : null;
        return new CallSignature(receiver, hint, c.getArgs());
    }

    public static CallSignature calculate(ExprMemberMethod c) {
        // Always keep the instantiated left type as hint (e.g. OtherBox<int>)
        WurstType hint = c.getLeft().attrTyp();

        if (c.getLeft().attrTyp().isStaticRef()) {
            // static ref like OtherBox<int>.staticItr()
            return new CallSignature(null, hint, c.getArgs());
        }

        // instance call: keep real receiver expression AND hint
        return new CallSignature(c.attrImplicitParameter(), hint, c.getArgs());
    }

    public static CallSignature calculate(ExprNewObject c) {
        return new CallSignature((Expr) null, null, c.getArgs());
    }
}
