package de.peeeq.wurstscript.translation.imtojass;

import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ImAttrTypeRaw {


    public static ImType getTypeRaw(ImBoolVal e) {
        return WurstTypeBool.instance().imTranslateType();
    }

    public static ImType getTypeRaw(ImFuncRef e) {
        return WurstTypeCode.instance().imTranslateType();
    }

    public static ImType getTypeRaw(ImFunctionCall e) {
        ImType t = e.getFunc().getReturnType();
        t = substituteType(t, e.getTypeArguments(), e.getFunc().getTypeVariables());
        if (e.getTuplesEliminated()) {
            if (t instanceof ImTupleType) {
                ImTupleType tt = (ImTupleType) t;
                return tt.getTypes().get(0);
            }
        }
        return t;
    }

    public static ImType substituteType(ImType type, List<ImTypeArgument> generics, List<ImTypeVar> typeVars) {
        return type.match(new TypeRewriteMatcher() {

            @Override
            public ImType case_ImTypeVarRef(ImTypeVarRef t) {
                int index = typeVars.indexOf(t.getTypeVariable());
                if (index < 0) {
                    return t;
                } else if (index >= generics.size()) {
                    throw new RuntimeException("Could not find replacement for " + t + " when replacing " + typeVars + " with " + generics);
                }
                return generics.get(index).getType();
            }

        });
    }


    public static ImType getTypeRaw(ImIntVal e) {
        return WurstTypeInt.instance().imTranslateType();
    }

    public static ImType getTypeRaw(ImNull e) {
        return e.getType();
    }

    public static ImType getTypeRaw(ImOperatorCall e) {
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
                ImType leftType = e.getArguments().get(0).attrTypRaw();
                ImType rightType = e.getArguments().get(1).attrTypRaw();
                if (typeReal(leftType) || typeReal(rightType)) {
                    return WurstTypeReal.instance().imTranslateType();
                }
            }
            case UNARY_MINUS:
        }
        return e.getArguments().get(0).attrTypRaw();
    }

    private static boolean typeReal(ImType t) {
        if (t instanceof ImSimpleType) {
            ImSimpleType st = (ImSimpleType) t;
            return st.getTypename().equals("real");
        }
        return false;
    }

    public static ImType getTypeRaw(ImRealVal e) {
        return WurstTypeReal.instance().imTranslateType();
    }

    public static ImType getTypeRaw(ImStatementExpr e) {
        return e.getExpr().attrTypRaw();
    }

    public static ImType getTypeRaw(ImStringVal e) {
        return WurstTypeString.instance().imTranslateType();
    }

    public static ImType getTypeRaw(ImTupleSelection e) {
        ImTupleType tt = (ImTupleType) e.getTupleExpr().attrTypRaw();
        return tt.getTypes().get(e.getTupleIndex());
    }

    public static ImType getTypeRaw(ImVarAccess e) {
        return e.getVar().getType();
    }

    public static ImType getTypeRaw(ImVarArrayAccess e) {
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

    public static ImType getTypeRaw(ImTupleExpr imTupleExpr) {
        return imTupleExpr.getTupleType();
    }


    public static ImType getTypeRaw(ImMethodCall mc) {
        ImFunction func = mc.getMethod().getImplementation();
        ImType t = func.getReturnType();
        List<ImTypeArgument> typeArguments = new ArrayList<>();
        List<ImTypeVar> typeVariables = new ArrayList<>();
        ImType receiverType = mc.getReceiver().attrTypRaw();
        if (receiverType instanceof ImClassType) {
            ImClassType ct = (ImClassType) receiverType;
            typeArguments.addAll(ct.getTypeArguments());
            typeVariables.addAll(ct.getClassDef().getTypeVariables());
        }
        typeArguments.addAll(mc.getTypeArguments());
        typeVariables.addAll(func.getTypeVariables());
        t = substituteType(t, typeArguments, typeVariables);
        return t;
    }

    public static ImType getTypeRaw(ImMemberAccess e) {
        ImType t = e.getVar().getType();
        ImType receiverType1 = e.getReceiver().attrTypRaw();
        if (receiverType1 instanceof ImClassType) {
            ImClassType receiverType = (ImClassType) receiverType1;
            ImTypeArguments typeArgs = e.getTypeArguments();
            try {
                if (typeArgs.isEmpty()) {
                    typeArgs = receiverType.getTypeArguments();
                }
                t = substituteType(t, typeArgs, receiverType.getClassDef().getTypeVariables());

                if (!e.getIndexes().isEmpty()) {
                    if (t instanceof ImArrayType) {
                        ImArrayType at = (ImArrayType) t;
                        t = at.getEntryType();
                    } else if (t instanceof ImArrayTypeMulti) {
                        ImArrayTypeMulti at = (ImArrayTypeMulti) t;
                        t = at.getEntryType();
                    } else {
                        throw new RuntimeException("unhandled case: " + t);
                    }
                }
                return t;
            } catch (Exception ex) {
                throw new RuntimeException("Could not determine type of " + e + " with receiverType " + receiverType, ex);
            }
        }
        if (!e.getIndexes().isEmpty()) {
            ImArrayTypeMulti at = (ImArrayTypeMulti) t;
            t = at.getEntryType();
        }
        return t;
    }

    public static ImType getTypeRaw(ImAlloc imAlloc) {
        return imAlloc.getClazz();
    }

    public static ImType getTypeRaw(ImDealloc imDealloc) {
        return TypesHelper.imVoid();
    }

    public static ImType getTypeRaw(ImInstanceof imInstanceof) {
        return TypesHelper.imBool();
    }

    public static ImType getTypeRaw(ImTypeIdOfClass imTypeIdOfClass) {
        return TypesHelper.imInt();
    }

    public static ImType getTypeRaw(ImTypeIdOfObj imTypeIdOfObj) {
        return TypesHelper.imInt();
    }


    public static ImType getTypeRaw(ImGetStackTrace imGetStackTrace) {
        return TypesHelper.imString();
    }

    public static ImType getTypeRaw(ImCompiletimeExpr e) {
        return e.getExpr().attrTypRaw();
    }

    public static ImType getTypeRaw(ImTypeVarDispatch e) {
        return e.getTypeClassFunc().getReturnType();
    }

    public static ImType getTypeRaw(ImCast imCast) {
        return imCast.getToType();
    }

}
