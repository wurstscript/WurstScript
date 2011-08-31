package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.PackageOrGlobalPos;
import de.peeeq.wurstscript.ast.TypeDefPos;
import de.peeeq.wurstscript.ast.TypeRefPos;
import de.peeeq.wurstscript.ast.WEntityPos;
import de.peeeq.wurstscript.ast.WPackagePos;
import de.peeeq.wurstscript.types.NativeTypes;
import de.peeeq.wurstscript.types.PscriptType;


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
		
		PscriptType nativeType = NativeTypes.nativeType(typeName);
		if (nativeType != null) {
			return null;
		}
		
		
		// find nearest packageDef
		PackageOrGlobalPos scope = attr.nearestPackage.get(node);
		if (scope instanceof WPackagePos) {
			WPackagePos pack = (WPackagePos) scope;
			for (WEntityPos elem : attr.packageElements.get(pack).get(typeName)) {
				if (elem instanceof TypeDefPos) {
					return (TypeDefPos) elem;
				}
			}
		}
		// search global scope:
		for (WEntityPos elem : attr.packageElements.get(node.root()).get(typeName)) {
			if (elem instanceof TypeDefPos) {
				return (TypeDefPos) elem;
			}
		}
		attr.addError(node.source(), "Could not find TypeDef for " + typeName);		
		return null;		
	}



}
