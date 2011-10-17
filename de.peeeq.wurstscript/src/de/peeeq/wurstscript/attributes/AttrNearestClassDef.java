package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AST.SortPos;
import de.peeeq.wurstscript.ast.ClassDefPos;


/**
 * this attribute gives you the nearest package for a given element
 *
 */
public class AttrNearestClassDef extends Attribute<SortPos, ClassDefPos> {

	 
	public AttrNearestClassDef(Attributes attr) {
		super(attr);
	}

	@Override
	protected ClassDefPos calculate(SortPos node) {
		if (node == null) {
			return null;
		}
		if (node instanceof ClassDefPos) {
			return (ClassDefPos) node;
		}
		return get(node.parent());
	}


}