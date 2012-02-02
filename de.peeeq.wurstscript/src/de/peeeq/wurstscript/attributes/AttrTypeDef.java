package de.peeeq.wurstscript.attributes;

import java.util.List;

import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.TypeExprArray;
import de.peeeq.wurstscript.ast.TypeExprSimple;
import de.peeeq.wurstscript.ast.TypeExprThis;
import de.peeeq.wurstscript.ast.TypeRef;
import de.peeeq.wurstscript.types.NativeTypes;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.utils.Utils;


/**
 * this attribute finds the type definition for every tpye-reference
 *
 */
public class AttrTypeDef {

	public static  TypeDef calculate(TypeRef node) {
		String typeName = getTypeName(node);

		if (typeName == null) {
			// thistype has no typedef
			return null;
		}

		PscriptType nativeType = NativeTypes.nativeType(typeName, Utils.isJassCode(node));
		if (nativeType != null) {
			return null; // native types have no definitionPos
		}

//		List<TypeDef> typeDefs = NameResolution.searchTypedName(TypeDef.class, typeName, node);
//		if (typeDefs.size() == 0) {
//			attr.addError(node.getSource(), "Could not find TypeDef for " + typeName);
//			return null;
//		} else {
//			return typeDefs.get(0);
//		}
		// TODO ambiguous type decls
		
		List<NameDef> typeDefs = NameResolution.searchTypedName(NameDef.class, typeName, node);
		if (typeDefs.size() == 0) {
			attr.addError(node.getSource(), "Could not find TypeDef for " + typeName);
			return null;
		} else {
			NameDef def = typeDefs.get(0);
			if (def instanceof TypeDef) {
				return (TypeDef) def;
			} else {
				attr.addError(node.getSource(), "The type name '" + typeName + "' refers to the element '" +
						Utils.printElement(def) + "' which is not a type definition.");
				return null;
			}
		}
		
	}

	private static String getTypeName(TypeRef node) {
		return node.match(new TypeRef.Matcher<String>() {

			@Override
			public String case_TypeExprSimple(TypeExprSimple typeExprSimple) {
				return typeExprSimple.getTypeName();
			}

			@Override
			public String case_ExprNewObject(ExprNewObject exprNewObject) {
				return exprNewObject.getTypeName();
			}

			@Override
			public String case_TypeExprThis(TypeExprThis typeExprThis) {
				return null;
			}

			@Override
			public String case_TypeExprArray(TypeExprArray typeExprArray) {
				return null;
			}
		});
	}



}
