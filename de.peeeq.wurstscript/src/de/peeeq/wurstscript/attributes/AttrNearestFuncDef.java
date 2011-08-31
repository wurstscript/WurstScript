package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AST.SortPos;
import de.peeeq.wurstscript.ast.FuncDefPos;


/**
 * this attribute gives you the nearest funcdef for a given position
 *
 */
public class AttrNearestFuncDef extends Attribute<SortPos, FuncDefPos> {

	 
	public AttrNearestFuncDef(Attributes attr) {
		super(attr);
	}

	@Override
	protected FuncDefPos calculate(SortPos node) {
		if (node == null) {
			return null;
		}
		if (node instanceof FuncDefPos) {
			return (FuncDefPos) node;
		}
		return get(node.parent());
	}


}