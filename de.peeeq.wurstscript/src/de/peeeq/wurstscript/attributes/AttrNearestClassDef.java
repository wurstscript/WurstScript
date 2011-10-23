package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.SortPos;



/**
 * this attribute gives you the nearest package for a given element
 *
 */
public class AttrNearestClassDef extends Attribute<SortPos, ClassDef> {

	 
	public AttrNearestClassDef(Attributes attr) {
		super(attr);
	}

	@Override
	protected ClassDef calculate(SortPos node) {
		if (node == null) {
			return null;
		}
		if (node instanceof ClassDef) {
			return (ClassDef) node;
		}
		return get(node.getParent());
	}


}