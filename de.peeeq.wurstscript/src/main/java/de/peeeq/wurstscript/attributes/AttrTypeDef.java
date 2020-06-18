package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.utils.Utils;

import java.util.Optional;

import org.eclipse.jdt.annotation.Nullable;


/**
 * this attribute finds the type definition for every tpye-reference
 */
public class AttrTypeDef {

    public static @Nullable TypeDef calculate(TypeExprResolved e) {
        return null;
    }


    public static @Nullable TypeDef calculate(TypeRef node) {
        WurstType scope = getTypeScope(node);
        String typeName = getTypeName(node);

        if (typeName == null) {
            // thistype has no typedef
            return null;
        }

        if (scope instanceof WurstTypeClass) {
            WurstTypeClass ct = (WurstTypeClass) scope;
            TypeDef r = ct.lookupInnerType(typeName);
            if (r == null) {
                node.addError("Could not find type " + typeName + " in class " + ct + ".");
            }
            return r;
        }

        WurstType nativeType = NativeTypes.nativeType(typeName, Utils.isJassCode(Optional.of(node)));
        if (nativeType != null) {
            return null; // native types have no definitionPos
        }
        return node.lookupType(typeName);
    }

    private static WurstType getTypeScope(TypeRef node) {
        if (node instanceof TypeExprSimple) {
            TypeExprSimple t = (TypeExprSimple) node;
            OptTypeExpr scopeType = t.getScopeType();
            if (scopeType instanceof TypeExpr) {
                return scopeType.attrTyp();
            }
        }
        return null;

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
