package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.AttrConstantValue.ConstantValueCalculationException;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
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
        if (typeExprArray.getArraySize() instanceof NoExpr) {
            return new WurstTypeArray(typeExprArray.getBase().attrTyp().dynamic());
        } else { // otherwise it must be an Expr
            Expr arSize = (Expr) typeExprArray.getArraySize();
            // default is to have no array sizes:
            int[] sizes = {};
            // when there is an array size given, try to evaluate it:
            try {
                ILconst i = arSize.attrConstantValue();
                if (i instanceof ILconstInt) {
                    int val = ((ILconstInt) i).getVal();
                    sizes = new int[]{val};
                    if (val <= 0) {
                        arSize.addError("Array size must be at least 1");
                    }
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
