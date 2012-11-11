package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.types.CallSignature;
import de.peeeq.wurstscript.types.WurstTypeNamedScope;

public class AttrCallSignature {

	public static CallSignature calculate(ExprFunctionCall c) {
		Expr receiver = null;
		if (c.attrImplicitParameter() instanceof Expr) {
			receiver = (Expr) c.attrImplicitParameter();
		}
		return new CallSignature(receiver, c.getArgs());
	}

	public static CallSignature calculate(ExprMemberMethod c) {
		Expr receiver = c.getLeft();
		if (receiver.attrTyp() instanceof WurstTypeNamedScope) {
			WurstTypeNamedScope t = (WurstTypeNamedScope) receiver.attrTyp();
			if (t.isStaticRef()) {
				receiver = null;
			}
		}
		return new CallSignature(receiver, c.getArgs());
	}

	public static CallSignature calculate(ExprNewObject c) {
		return new CallSignature(null, c.getArgs());
	}

}
