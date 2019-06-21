package de.peeeq.wurstscript.translation.imtojass;

import de.peeeq.wurstscript.jassIm.*;

public class TypeEquality {

    public static boolean isEqualType(ImArrayType a, ImType b) {
        if (b instanceof ImArrayType) {
            ImArrayType at = (ImArrayType) b;
            return at.getEntryType().equalsType(a.getEntryType());
        }
        return false;
    }

    public static boolean isEqualType(ImArrayTypeMulti a, ImType b) {
        if (b instanceof ImArrayTypeMulti) {
            ImArrayTypeMulti at = (ImArrayTypeMulti) b;
            // TODO check dimensions
            return at.getEntryType().equalsType(a.getEntryType());
        }
        return false;
    }

    public static boolean isEqualType(ImSimpleType a, ImType b) {
        if (b instanceof ImSimpleType) {
            ImSimpleType at = (ImSimpleType) b;
            // TODO check dimensions
            return at.getTypename().equals(a.getTypename());
        }
        return false;
    }


    public static boolean isEqualType(ImTupleType a, ImType b) {
        if (b instanceof ImTupleType) {
            ImTupleType at = (ImTupleType) b;
            if (at.getTypes().size() != a.getTypes().size()) {
                return false;
            }
            for (int i = 0; i < a.getTypes().size(); i++) {
                if (!a.getTypes().get(i).equalsType(at.getTypes().get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean isEqualType(ImVoid a, ImType b) {
        return b instanceof ImVoid;
    }


    public static boolean isEqualType(ImTypeVarRef t, ImType other) {
        if (other instanceof ImTypeVarRef) {
            ImTypeVarRef o = (ImTypeVarRef) other;
            return t.getTypeVariable() == o.getTypeVariable();
        }
        return false;
    }

    public static boolean isEqualType(ImClassType c, ImType other) {
        if (other instanceof ImClassType) {
            ImClassType oc = (ImClassType) other;
            if (c.getClassDef() != oc.getClassDef()) {
                return false;
            }
            if (c.getTypeArguments().size() != oc.getTypeArguments().size()) {
                return false;
            }
            for (int i = 0; i < c.getTypeArguments().size(); i++) {
                ImTypeArgument x = c.getTypeArguments().get(i);
                ImTypeArgument y = oc.getTypeArguments().get(i);
                if (!x.getType().equalsType(y.getType())) {
                    return false;
                }
                if (!x.getTypeClassBinding().equals(y.getTypeClassBinding())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean isEqualType(ImAnyType t, ImType other) {
        return other instanceof ImAnyType;
    }
}
