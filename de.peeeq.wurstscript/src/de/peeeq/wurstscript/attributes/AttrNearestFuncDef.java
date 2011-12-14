package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.FunctionImplementation;



/**
 * this attribute gives you the nearest funcdef for a given position
 *
 */
public class AttrNearestFuncDef {
	
	public static  FunctionImplementation calculate(AstElement node) {
		if (node == null) {
			return null;
		}
		if (node instanceof FunctionImplementation) {
			return (FunctionImplementation) node;
		}
		if (node.getParent() == null) {
			return null;
		}
		return node.getParent().attrNearestFuncDef();
	}


}