package de.peeeq.wurstscript.attributes;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.TypeExprArray;
import de.peeeq.wurstscript.ast.TypeExprResolved;
import de.peeeq.wurstscript.ast.TypeExprSimple;
import de.peeeq.wurstscript.ast.TypeExprThis;
import de.peeeq.wurstscript.ast.TypeRef;
import de.peeeq.wurstscript.types.NativeTypes;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.utils.Utils;


/**
 * this attribute finds the type definition for every tpye-reference
 *
 */
public class AttrTypeDef {

	public static @Nullable  TypeDef calculate(TypeExprResolved e) {
		return null;
	}
	
	
	public static @Nullable  TypeDef calculate(TypeRef node) {
		String typeName = getTypeName(node);

		if (typeName == null) {
			// thistype has no typedef
			return null;
		}

		WurstType nativeType = NativeTypes.nativeType(typeName, Utils.isJassCode(node));
		if (nativeType != null) {
			return null; // native types have no definitionPos
		}
		return node.lookupType(typeName);
	}

	private static @Nullable String getTypeName(TypeRef node) {
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
			public @Nullable String case_TypeExprThis(TypeExprThis typeExprThis) {
				return null;
			}

			@Override
			public @Nullable String case_TypeExprArray(TypeExprArray typeExprArray) {
				return null;
			}

			@Override
			public String case_TypeExprResolved(TypeExprResolved t) {
				return t.attrTyp().getName();
			}

		});
	}



}
