package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.SortPos;



/**
 * this attribute gives you the nearest funcdef for a given position
 *
 */
public class AttrNearestFuncDef extends Attribute<SortPos, FuncDef> {

	 
	public AttrNearestFuncDef(Attributes attr) {
		super(attr);
	}

	@Override
	protected FuncDef calculate(SortPos node) {
		if (node == null) {
			return null;
		}
		if (node instanceof FuncDef) {
			return (FuncDef) node;
		}
		return get(node.getParent());
	}


}