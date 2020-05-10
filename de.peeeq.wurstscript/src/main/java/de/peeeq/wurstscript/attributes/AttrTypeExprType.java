package de.peeeq.wurstscript.attributes;

import java.util.Optional;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.utils.Utils;

/**
 * this attribute gives you the type for a type expr
 */
public class AttrTypeExprType {


    public static WurstType calculate(TypeExprSimple node) {
        WurstType baseType = getBaseType(node);
        if (node.getTypeArgs().size() > 0) {
            if (baseType instanceof WurstTypeNamedScope) {
                WurstTypeNamedScope ns = (WurstTypeNamedScope) baseType;
                return ns.replaceTypeVarsUsingTypeArgs(node.getTypeArgs());
            } else {
                node.addError("Type " + baseType + " cannot have type args");
            }
        }
        return baseType;
    }


    public static WurstType calculate(TypeExprThis node) {
        WurstType scopeType = node.getScopeType().attrTyp();
        Element scope;
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
        WurstType baseType = typeExprArray.getBase().attrTyp().dynamic();
        if (typeExprArray.getArraySize() instanceof NoExpr) {
            return new WurstTypeArray(baseType);
        } else { // otherwise it must be an Expr
            Expr arSize = (Expr) typeExprArray.getArraySize();
            return new WurstTypeArray(baseType, arSize);
        }
    }

    private static WurstType getBaseType(TypeExprSimple node) {
        final String typename = node.getTypeName();
        final boolean isJassCode = Utils.isJassCode(Optional.of(node));
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
