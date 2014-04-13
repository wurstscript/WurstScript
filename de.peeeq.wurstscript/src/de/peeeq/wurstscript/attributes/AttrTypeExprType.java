package de.peeeq.wurstscript.attributes;

import java.util.List;

import com.google.common.base.Function;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprIntVal;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.NoExpr;
import de.peeeq.wurstscript.ast.NoTypeExpr;
import de.peeeq.wurstscript.ast.OptTypeExpr;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeExprArray;
import de.peeeq.wurstscript.ast.TypeExprResolved;
import de.peeeq.wurstscript.ast.TypeExprSimple;
import de.peeeq.wurstscript.ast.TypeExprThis;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImVar;
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
				List<WurstType> newTypes = Utils.map(node.getTypeArgs() , new Function<TypeExpr, WurstType>() {

					@Override
					public WurstType apply(TypeExpr input) {
						return input.attrTyp().dynamic();
					}
					
				});
				return ns.replaceTypeVars(newTypes);
			} else {
				node.addError("Type " + baseType + " cannot have type args");
			}
		}
		return baseType;
	}
	
	public static WurstType calculate(TypeExprThis node) {
		return AttrExprType.caclulateThistype(node, false, "thistype");
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
		} else if ( typeExprArray.getArraySize() instanceof Expr) {
			System.out.println("has getArraySize " + typeExprArray.getArraySize());
			if ( typeExprArray.getArraySize() instanceof ExprIntVal) {
				System.out.println("is Int Val");
				ExprIntVal val = (ExprIntVal) typeExprArray.getArraySize().get(0);
				ExprIntVal val2 = (ExprIntVal) typeExprArray.getArraySize().get(1);
				System.out.println("val: " + val.toString() + " val2: " + val2.toString());
				int[] sizes = { val.getValI() };
				return new WurstTypeArray(typeExprArray.getBase().attrTyp().dynamic(), sizes);
			}else if ( typeExprArray.getArraySize() instanceof ExprVarAccess) {
				AstElement parent = typeExprArray.getParent();
				while(!( parent instanceof ImProg)) {
					parent = parent.getParent();
				}
				ImProg prog = (ImProg) parent;
				System.out.println("is Var Access");
				ExprVarAccess va = (ExprVarAccess) typeExprArray.getArraySize();
				for(ImVar v : prog.getGlobals()) {
					if (v.getName().equals(va.getVarName())){
						sizes = { v.}
					}
				}
				System.out.println("val: " + val.toString() + " val2: " + val2.toString());
				int[] ;
				return new WurstTypeArray(typeExprArray.getBase().attrTyp().dynamic(), sizes);
			}
		}
		return null;
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
