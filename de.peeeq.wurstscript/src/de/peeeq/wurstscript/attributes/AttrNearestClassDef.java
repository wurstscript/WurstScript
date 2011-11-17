package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;



/**
 * this attribute gives you the nearest package for a given element
 *
 */
public class AttrNearestClassDef {
	
	public static  ClassDef calculate(AstElement node) {
		if (node == null) {
			return null;
		}
		if (node instanceof ClassDef) {
			return (ClassDef) node;
		}
		if (node.getParent() == null) {
			return null;
		}
		return node.getParent().attrNearestClassDef();
	}


}