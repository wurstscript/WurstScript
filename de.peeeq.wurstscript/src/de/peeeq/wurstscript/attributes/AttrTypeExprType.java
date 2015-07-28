package de.peeeq.wurstscript.attributes;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.NoExpr;
import de.peeeq.wurstscript.ast.NoTypeExpr;
import de.peeeq.wurstscript.ast.OptTypeExpr;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeExprArray;
import de.peeeq.wurstscript.ast.TypeExprResolved;
import de.peeeq.wurstscript.ast.TypeExprSimple;
import de.peeeq.wurstscript.ast.TypeExprThis;
import de.peeeq.wurstscript.attributes.AttrConstantValue.ConstantValueCalculationException;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.types.NativeTypes;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeArray;
import de.peeeq.wurstscript.types.WurstTypeNamedScope;
import de.peeeq.wurstscript.types.WurstTypeUnknown;
import de.peeeq.wurstscript.types.WurstTypeVoid;
import de.peeeq.wurstscript.utils.Utils;

/**
 * this attribute gives you the type for a type expr
 * 
 */
public class AttrTypeExprType {

	
	public static WurstType calculate(TypeExprSimple node) {
		WurstType baseType = getBaseType(node);
		if (node.getTypeArgs().size() > 0) {
			if (baseType instanceof WurstTypeNamedScope) {
				WurstTypeNamedScope ns = (WurstTypeNamedScope) baseType;
				List<WurstType> newTypes = node.getTypeArgs().stream()
						.map((TypeExpr te) -> te.attrTyp().dynamic())
						.collect(Collectors.toList());
				return ns.replaceTypeVars(newTypes);
			} else {
				node.addError("Type " + baseType + " cannot have type args");
			}
		}
		return baseType;
	}
	
	
	public static WurstType calculate(TypeExprThis node) {
		WurstType scopeType = node.getScopeType().attrTyp();
		AstElement scope;
		if (scopeType instanceof WurstTypeNamedScope) {
			WurstTypeNamedScope wtns = (WurstTypeNamedScope) scopeType;
			scope = wtns.getDef();
		} else {
			scope = node;
		}
		return AttrExprType.caclulateThistype(scope, false, "thistype");
//		ClassOrModule n = node.attrNearestClassOrModule();
//		if (n == null) {
//			node.addError("'thistype' can only be used in classes and modules.");
//			return WurstTypeUnknown.instance();
//		}
//		return n.match(new ClassOrModule.Matcher<WurstType>() {
//
//			@Override
//			public WurstType case_ClassDef(ClassDef classDef) {
//				WurstTypeClass t = (WurstTypeClass) classDef.attrTyp();
//				t.gett
//			}
//
//			@Override
//			public WurstType case_ModuleDef(ModuleDef moduleDef) {
//				return new WurstTypeModule(moduleDef, true);
//			}
//
//		});
	}
	
	public static WurstType calculate(NoTypeExpr optType) {
		return WurstTypeVoid.instance();
	}
	
	
	public static WurstType calculate(TypeExprArray typeExprArray) {
		if( typeExprArray.getArraySize() instanceof NoExpr) {
			return new WurstTypeArray(typeExprArray.getBase().attrTyp().dynamic());
		} else { // otherwise it must be an Expr
			Expr arSize = (Expr) typeExprArray.getArraySize();
			System.out.println("has getArraySize " + typeExprArray.getArraySize());
			// default is to have no array sizes:
			int[] sizes = {};
			// when there is an array size given, try to evaluate it:
			try {
				ILconst i = arSize.attrConstantValue();
				if (i instanceof ILconstInt) {
					sizes = new int[] {((ILconstInt) i).getVal()};
				} else {
					arSize.addError("Array sizes should be integer...");
				}
			} catch (ConstantValueCalculationException e) {
				arSize.addError("Array size is not a constant expression.");
			}
			return new WurstTypeArray(typeExprArray.getBase().attrTyp().dynamic(), sizes);
		}
	}

	private static WurstType getBaseType(TypeExprSimple node) {
		final String typename = node.getTypeName();
		final boolean isJassCode = Utils.isJassCode(node);
		TypeDef t = node.attrTypeDef();
		if (t == null) {
			WurstType nativeType = NativeTypes.nativeType(typename, isJassCode);
			if (nativeType != null) {
				return nativeType;
			}
			node.addError("Unknown type " + typename);
			return new WurstTypeUnknown(typename);
		}
		return t.attrTyp();
	}

	public static WurstType normalizedType(OptTypeExpr e) {
		return e.attrTypRaw().normalize();
	}

	public static WurstType calculate(TypeExprResolved e) {
		return e.getResolvedType();
	}

	

}
