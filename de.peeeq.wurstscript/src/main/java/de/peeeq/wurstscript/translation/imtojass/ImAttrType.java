package de.peeeq.wurstscript.translation.imtojass;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.*;

import java.util.List;

public class ImAttrType {

    public static ImType getType(ImBoolVal e) {
        return WurstTypeBool.instance().imTranslateType();
    }

    public static ImType getType(ImFuncRef e) {
        return WurstTypeCode.instance().imTranslateType();
    }

    public static ImType getType(ImFunctionCall e) {
        ImType t = e.getFunc().getReturnType();
        if (e.getTuplesEliminated()) {
            if (t instanceof ImTupleType) {
                ImTupleType tt = (ImTupleType) t;
                return tt.getTypes().get(0);
            }
        }
        return t;
    }

    public static ImType getType(ImIntVal e) {
        return WurstTypeInt.instance().imTranslateType();
    }

    public static ImType getType(ImNull e) {
        return e.getType();
    }

    public static ImType getType(ImOperatorCall e) {
        switch (e.getOp()) {
            case MOD_REAL:
                return WurstTypeReal.instance().imTranslateType();
            case DIV_INT:
            case MOD_INT:
                return WurstTypeInt.instance().imTranslateType();
            case AND:
            case OR:
            case EQ:
            case NOTEQ:
            case GREATER_EQ:
            case GREATER:
            case LESS:
            case LESS_EQ:
            case NOT:
                return WurstTypeBool.instance().imTranslateType();
            case DIV_REAL:
            case PLUS:
            case MINUS:
            case MULT: {
                ImType leftType = e.getArguments().get(0).attrTyp();
                ImType rightType = e.getArguments().get(1).attrTyp();
                if (typeReal(leftType) || typeReal(rightType)) {
                    return WurstTypeReal.instance().imTranslateType();
                }
            }
            case UNARY_MINUS:
        }
        return e.getArguments().get(0).attrTyp();
    }

    private static boolean typeReal(ImType t) {
        if (t instanceof ImSimpleType) {
            ImSimpleType st = (ImSimpleType) t;
            return st.getTypename().equals("real");
        }
        return false;
    }

    public static ImType getType(ImRealVal e) {
        return WurstTypeReal.instance().imTranslateType();
    }

    public static ImType getType(ImStatementExpr e) {
        return e.getExpr().attrTyp();
    }

    public static ImType getType(ImStringVal e) {
        return WurstTypeString.instance().imTranslateType();
    }

    public static ImType getType(ImTupleSelection e) {
        ImTupleType tt = (ImTupleType) e.getTupleExpr().attrTyp();
        return tt.getTypes().get(e.getTupleIndex());
    }

    public static ImType getType(ImVarAccess e) {
        return e.getVar().getType();
    }

    public static ImType getType(ImVarArrayAccess e) {
        ImType ar = e.getVar().getType();
        if (ar instanceof ImArrayType) {
            ImArrayType t = (ImArrayType) ar;
            return t.getEntryType();
        } else if (ar instanceof ImArrayTypeMulti) {
            ImArrayTypeMulti t = (ImArrayTypeMulti) ar;
            return t.getEntryType();
        }
        return ar;
    }

    public static ImType getType(ImTupleExpr imTupleExpr) {
        List<ImType> types = Lists.newArrayList();
        List<String> names = Lists.newArrayList();
        int i = 1;
        for (ImExpr e : imTupleExpr.getExprs()) {
            types.add(e.attrTyp());
            names.add("" + i++);
        }
        return JassIm.ImTupleType(types, names);
    }


    public static ImType getType(ImMethodCall mc) {
        return mc.getMethod().getImplementation().getReturnType();
    }

    public static ImType getType(ImMemberAccess e) {
        return e.getVar().getType();
    }

    public static ImType getType(ImAlloc imAlloc) {
        return TypesHelper.imInt();
    }

    public static ImType getType(ImDealloc imDealloc) {
        return TypesHelper.imVoid();
    }

    public static ImType getType(ImInstanceof imInstanceof) {
        return TypesHelper.imBool();
    }

    public static ImType getType(ImTypeIdOfClass imTypeIdOfClass) {
        return TypesHelper.imInt();
    }

    public static ImType getType(ImTypeIdOfObj imTypeIdOfObj) {
        return TypesHelper.imInt();
    }


    public static ImType getType(ImGetStackTrace imGetStackTrace) {
        return TypesHelper.imString();
    }

    public static ImType getType(ImCompiletimeExpr e) {
        return e.getExpr().attrTyp();
    }
}
