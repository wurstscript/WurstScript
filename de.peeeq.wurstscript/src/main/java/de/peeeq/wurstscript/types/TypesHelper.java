package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.jassIm.*;

public class TypesHelper {

    private static final ImSimpleType intType = WurstTypeInt.instance().imTranslateType();

    private static final ImSimpleType realType = WurstTypeReal.instance().imTranslateType();


    public static ImSimpleType imInt() {
        return intType;
    }

    public static ImSimpleType imReal() {
        return realType;
    }

    public static ImSimpleType imString() {
        return WurstTypeString.instance().imTranslateType();
    }

    public static ImType imVoid() {
        return WurstTypeVoid.instance().imTranslateType();
    }


    public static ImType imBool() {
        return WurstTypeBool.instance().imTranslateType();
    }

    public static ImType imHashTable() {
        return JassIm.ImSimpleType("hashtable");
    }

    public static ImArrayType imStringArray() {
        return JassIm.ImArrayType(imString());
    }

    public static boolean typeContainsTuples(ImType vt) {
        return vt instanceof ImTupleType
                || vt instanceof ImArrayType imArrayType && typeContainsTuples(imArrayType.getEntryType())
                || vt instanceof ImArrayTypeMulti imArrayTypeMulti && typeContainsTuples(imArrayTypeMulti.getEntryType());
    }
    
    public static ImVar getSimpleAndPureTupleVar(ImTupleSelection ts) {
        ImExpr te = ts;
        while(te instanceof ImTupleSelection) {
            te = ((ImTupleSelection) te).getTupleExpr();
        }
        if(te instanceof ImVarAccess) {
            ImVar var = ((ImVarAccess) te).getVar();
            if(var.isGlobal()) {
                return null;
            }
            return var;
        }
        return null;
    }
    
    public static ImVar getTupleVar(ImTupleSelection ts) {
        ImExpr te = ts;
        while(te instanceof ImTupleSelection) {
            te = ((ImTupleSelection) te).getTupleExpr();
        }
        if(te instanceof ImVarAccess) {
            return ((ImVarAccess) te).getVar();
        }
        if(te instanceof ImVarArrayAccess) {
            return ((ImVarArrayAccess) te).getVar();
        }
        if(te instanceof ImMemberAccess) {
            return ((ImMemberAccess) te).getVar();
        }
        throw new Error("not implemented");
    }

    public static boolean isIntType(ImType t) {
        if (t instanceof ImSimpleType imSimpleType) {
            return imSimpleType.getTypename().equals("integer");
        }
        return false;
    }

    public static boolean isRealType(ImType t) {
        if (t instanceof ImSimpleType imSimpleType) {
            return imSimpleType.getTypename().equals("real");
        }
        return false;
    }

    public static boolean isBoolType(ImType t) {
        if (t instanceof ImSimpleType imSimpleType) {
            return imSimpleType.getTypename().equals("boolean");
        }
        return false;
    }

    public static boolean isStringType(ImType t) {
        if (t instanceof ImSimpleType imSimpleType) {
            return imSimpleType.getTypename().equals("string");
        }
        return false;
    }

    public static ImSimpleType imTrigger() {
        return JassIm.ImSimpleType("trigger");
    }




}
