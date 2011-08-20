package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AST.SortPos;
import de.peeeq.wurstscript.ast.TypeDefPos;
import de.peeeq.wurstscript.ast.TypeRefPos;
import de.peeeq.wurstscript.ast.WEntityPos;
import de.peeeq.wurstscript.ast.WPackagePos;


/**
 * this attribute finds the type definition for every tpye-reference
 *
 */
public class AttrTypeDef extends Attribute<TypeRefPos, TypeDefPos> {

	 
	public AttrTypeDef(Attributes attr) {
		super(attr);
	}

	@Override
	protected TypeDefPos calculate(TypeRefPos node) {
		String typeName = node.typeName().term();
		
		// find nearest packageDef
		WPackagePos pack = findNearestPackage(node);
		
		for (WEntityPos elem : attr.packageElements.get(pack).get(typeName)) {
			if (elem instanceof TypeDefPos) {
				return (TypeDefPos) elem;
			}
		}
		return null;		
	}

	private WPackagePos findNearestPackage(SortPos node) {
		while (node != null) {
			if (node instanceof WPackagePos) {
				return (WPackagePos) node;
			}
			node = node.parent();
		}
			
		return null;
	}



}
