package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.TypeRef;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.types.NativeTypes;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.utils.Utils;


/**
 * this attribute finds the type definition for every tpye-reference
 *
 */
public class AttrTypeDef extends Attribute<TypeRef, TypeDef> {

	 
	public AttrTypeDef(Attributes attr) {
		super(attr);
	}

	@Override
	protected TypeDef calculate(TypeRef node) {
		String typeName = node.getTypeName();
		
		PscriptType nativeType = NativeTypes.nativeType(typeName, Utils.isJassCode(node));
		if (nativeType != null) {
			return null; // native types have no definitionPos
		}
		
		
		// find nearest packageDef
		PackageOrGlobal scope = attr.nearestPackage.get(node);
		if (scope instanceof WPackage) {
			WPackage pack = (WPackage) scope;
			for (WEntity elem : attr.packageElements.get(pack).get(typeName)) {
				if (elem instanceof TypeDef) {
					return (TypeDef) elem;
				}
			}
		}
		// search global scope:
		for (WEntity elem : attr.packageElements.get((PackageOrGlobal) Utils.getRoot(node)).get(typeName)) {
			if (elem instanceof TypeDef) {
				return (TypeDef) elem;
			}
		}
		attr.addError(node.getSource(), "Could not find TypeDef for " + typeName);		
		return null;		
	}



}
