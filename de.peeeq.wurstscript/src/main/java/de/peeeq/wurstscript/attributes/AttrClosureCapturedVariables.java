package de.peeeq.wurstscript.attributes;

import java.util.Map.Entry;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.ast.ExprThis;
import de.peeeq.wurstscript.ast.FunctionCall;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.types.WurstTypeArray;

public class AttrClosureCapturedVariables {

	public static ImmutableMultimap<Element, VarDef> calculate(ExprClosure e) {
		ImmutableMultimap.Builder<Element, VarDef> result = ImmutableMultimap.builder();
		// just use a visitor and select every local variable which is not defined in the
		// closure itself
		collect(result, e, e.getImplementation());
		
		// TODO Auto-generated method stub
		return result.build();
	}

	private static void collect(Builder<Element, VarDef> result, ExprClosure closure, Element e) {
		if (e instanceof ExprClosure) {
			ExprClosure innerClosure = (ExprClosure) e;
			for (Entry<Element, VarDef> entry : innerClosure.attrCapturedVariables().entries()) {
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
				assert def != null; // instanceof-checks ensure this, but eclipse does not get it
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
		return Ast.LocalVarDef(closure.getSource(), Ast.Modifiers(), Ast.NoTypeExpr(), Ast.Identifier(closure.getSource(), "this"), Ast.NoExpr());
	}

}
