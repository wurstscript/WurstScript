package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.SortPos;



/**
 * this attribute gives you the nearest package for a given element
 *
 */
public class AttrNearestPackage extends Attribute<SortPos, PackageOrGlobal> {

	 
	public AttrNearestPackage(Attributes attr) {
		super(attr);
	}

	@Override
	protected PackageOrGlobal calculate(SortPos node) {
		if (node == null) {
			return null;
		}
		if (node instanceof PackageOrGlobal) {
			return (PackageOrGlobal) node;
		}
		return get(node.getParent());
	}


}