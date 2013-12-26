package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.ast.ExprThis;
import de.peeeq.wurstscript.ast.FunctionCall;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.types.WurstTypeArray;

public class AttrClosureCapturedVariables {

	public static Multimap<AstElement, VarDef> calculate(ExprClosure e) {
		final Multimap<AstElement, VarDef> result = HashMultimap.create();
		// just use a visitor and select every local variable which is not defined in the
		// closure itself
		collect(result, e, e.getImplementation());
		
		// TODO Auto-generated method stub
		return result;
	}

	private static void collect(Multimap<AstElement, VarDef> result, ExprClosure closure, AstElement e) {
		if (e instanceof ExprClosure) {
			ExprClosure innerClosure = (ExprClosure) e;
			for (Entry<AstElement, VarDef> entry : innerClosure.attrCapturedVariables().entries()) {
				VarDef v = entry.getValue();
				if (v.attrNearestExprClosure() != closure) {
					result.put(entry.getKey(), v);
				}
			}
			return;
		}
		if (e instanceof NameRef) {
			NameRef nr = (NameRef) e;
			NameDef def = nr.attrNameDef();
			if (def instanceof LocalVarDef || def instanceof WParameter) {
				VarDef v = (VarDef) def;
				if (v.attrNearestExprClosure() != closure) {
					result.put(nr, v);
					if (v.attrTyp() instanceof WurstTypeArray) {
						nr.addError("Closures cannot capture local array variables.");
					}
				}
			}
			if (nr.attrImplicitParameter() instanceof ExprThis) {
				result.put(nr, dummyThisVar(closure));
			}
		} else if (e instanceof FunctionCall) {
			FunctionCall fc = (FunctionCall) e;
			if (fc.attrImplicitParameter() instanceof ExprThis) {
				result.put(e, dummyThisVar(closure));
			}
		} else if (e instanceof ExprThis) {
			result.put(e, dummyThisVar(closure));
		}
		// visit children
		for (int i=0; i<e.size(); i++) {
			collect(result, closure, e.get(i));
		}
	}

	private static LocalVarDef dummyThisVar(ExprClosure closure) {
		return Ast.LocalVarDef(closure.getSource(), Ast.Modifiers(), Ast.NoTypeExpr(), "this", Ast.NoExpr());
	}

}
