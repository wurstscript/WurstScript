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
public class AttrTypeDef {
	
	public static  TypeDef calculate(TypeRef node) {
		String typeName = node.getTypeName();
		
		PscriptType nativeType = NativeTypes.nativeType(typeName, Utils.isJassCode(node));
		if (nativeType != null) {
			return null; // native types have no definitionPos
		}
		
		
		// find nearest packageDef
		PackageOrGlobal scope =  node.attrNearestPackage();
		if (scope instanceof WPackage) {
			WPackage pack = (WPackage) scope;
			for (WEntity elem : pack.attrPackageElements().get(typeName)) {
				if (elem instanceof TypeDef) {
					return (TypeDef) elem;
				}
			}
		}
		// search global scope:
		for (WEntity elem : ((PackageOrGlobal) Utils.getRoot(node)).attrPackageElements().get(typeName)) {
			if (elem instanceof TypeDef) {
				return (TypeDef) elem;
			}
		}
		attr.addError(node.getSource(), "Could not find TypeDef for " + typeName);		
		return null;		
	}



}
