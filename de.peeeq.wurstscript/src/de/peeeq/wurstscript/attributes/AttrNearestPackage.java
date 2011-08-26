package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AST.SortPos;
import de.peeeq.wurstscript.ast.WPackagePos;


/**
 * this attribute gives you the nearest package for a given element
 *
 */
public class AttrNearestPackage extends Attribute<SortPos, WPackagePos> {

	 
	public AttrNearestPackage(Attributes attr) {
		super(attr);
	}

	@Override
	protected WPackagePos calculate(SortPos node) {
		if (node == null) {
			return null;
		}
		if (node instanceof WPackagePos) {
			return (WPackagePos) node;
		}
		return get(node.parent());
	}


}