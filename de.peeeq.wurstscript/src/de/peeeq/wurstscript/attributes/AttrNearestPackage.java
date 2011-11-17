package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.PackageOrGlobal;



/**
 * this attribute gives you the nearest package for a given element
 *
 */
public class AttrNearestPackage {
	
	public static  PackageOrGlobal calculate(AstElement node) {
		if (node == null) {
			return null;
		}
		if (node instanceof PackageOrGlobal) {
			return (PackageOrGlobal) node;
		}
		return node.getParent().attrNearestPackage();
	}


}