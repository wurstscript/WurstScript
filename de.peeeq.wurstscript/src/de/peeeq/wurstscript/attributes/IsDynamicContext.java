package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithExtendedClass;
import de.peeeq.wurstscript.ast.AstElementWithExtendsList;
import de.peeeq.wurstscript.ast.AstElementWithImplementsList;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.OnDestroyDef;

/**
 * a dynamic context is a place where we have a 'this' variable 
 */
public class IsDynamicContext {

	public static boolean calculate(AstElement origElem) {
		AstElement elem = origElem;
		while (elem != null) {
			if (elem instanceof FuncDef) {
				FuncDef funcDef = (FuncDef) elem;
				return funcDef.attrIsDynamicClassMember();
			} else if (elem instanceof ConstructorDef) {
				return true;
			} else if (elem instanceof OnDestroyDef) {
				return true;
			} else if (elem instanceof GlobalVarDef) {
				GlobalVarDef g = (GlobalVarDef) elem;
				return g.attrIsDynamicClassMember();
			}
			if (elem instanceof AstElementWithExtendedClass) {
				AstElementWithExtendedClass a = (AstElementWithExtendedClass) elem;
				if (origElem.isSubtreeOf(a.getExtendedClass())) {
					return true;
				}
			}
			if (elem instanceof AstElementWithExtendsList) {
				AstElementWithExtendsList a = (AstElementWithExtendsList) elem;
				if (origElem.isSubtreeOf(a.getExtendsList())) {
					return true;
				}
			}
			if (elem instanceof AstElementWithImplementsList) {
				AstElementWithImplementsList a = (AstElementWithImplementsList) elem;
				if (origElem.isSubtreeOf(a.getImplementsList())) {
					return true;
				}
			} else if (elem instanceof ModuleUse) {
				ModuleUse mu = (ModuleUse) elem;
				if (origElem.isSubtreeOf(mu.getTypeArgs())) {
					return true;
				}
			}
			elem = elem.getParent();
		}
		return false;
	}
}
