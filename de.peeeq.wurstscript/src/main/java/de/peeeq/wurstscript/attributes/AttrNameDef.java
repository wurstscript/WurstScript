package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeEnum;
import de.peeeq.wurstscript.types.WurstTypeModule;
import org.eclipse.jdt.annotation.Nullable;

/**
 * this attribute find the variable definition for every variable reference
 */
public class AttrNameDef {


    public static NameLink calculate(ExprVarArrayAccess term) {
        return searchNameInScope(term.getVarName(), term);
    }

    public static NameLink calculate(ExprVarAccess term) {
        NameLink result = specialEnumLookupRules(term);
        if (result != null) {
            return result;
        }
        return searchNameInScope(term.getVarName(), term);
    }

    public static @Nullable NameLink specialEnumLookupRules(ExprVarAccess term) {
        NameLink result = null;
        Element parent = term.getParent();
        String varName = term.getVarName();
        if (parent instanceof ExprList) {
            parent = parent.getParent();
            if (parent instanceof SwitchCase) {
                SwitchStmt s = (SwitchStmt) parent.getParent().getParent();
                result = lookupEnumConst(varName, s.getExpr().attrTyp());
            }
        } else if (parent instanceof StmtSet) {
            StmtSet s = (StmtSet) parent;
            if (s.getRight() == term) {
                result = lookupEnumConst(varName, s.getUpdatedExpr().attrTyp());
            }
        } else if (parent instanceof GlobalOrLocalVarDef) {
            GlobalOrLocalVarDef v = (GlobalOrLocalVarDef) parent;
            result = lookupEnumConst(varName, v.getOptTyp().attrTyp());
        } else if (parent instanceof ExprBinary) {
            ExprBinary binary = (ExprBinary) parent;
            if (binary.getRight() == term) {
                result = lookupEnumConst(varName, binary.getLeft().attrTyp());
            }
        }
        return result;
    }

    public static @Nullable NameLink lookupEnumConst(String varName, WurstType t) {
        if (t instanceof WurstTypeEnum) {
            WurstTypeEnum e = (WurstTypeEnum) t;
            // if we expect an enum type we can as well directly look into the enum
            EnumDef eDef = e.getDef();
            return eDef.lookupMemberVar(e, varName, false);
        }
        return null;
    }

    public static NameLink calculate(ExprMemberVar term) {
        return memberVarCase(term.getLeft(), term.getVarName(), isWriteAccess(term), term);
    }

    public static @Nullable NameLink calculate(ExprMemberArrayVar term) {
        return memberVarCase(term.getLeft(), term.getVarName(), isWriteAccess(term), term);
    }


    protected static NameLink searchNameInScope(String varName, NameRef node) {
        boolean showErrors = !varName.startsWith("gg_");
        NameLink result = node.lookupVar(varName, showErrors);
        return result;
    }

    private static boolean isWriteAccess(final NameRef node) {
        if (node.getParent() instanceof StmtSet) {
            StmtSet stmtSet = (StmtSet) node.getParent();
            return stmtSet.getUpdatedExpr() == node;
        }
        return false;
    }

    private static @Nullable NameLink memberVarCase(Expr left, String varName, boolean writeAccess, Expr node) {
        WurstType receiverType = left.attrTyp();
        NameLink result = node.lookupMemberVar(receiverType, varName);
        if (result == null) {


            node.addError("Could not resolve reference to variable " + varName + " for receiver of type " +
                    receiverType + ".");

        }
        if (receiverType instanceof WurstTypeModule) {
            WurstTypeModule wurstTypeModule = (WurstTypeModule) receiverType;
            ModuleDef module = wurstTypeModule.getDef();
            if (!left.isSubtreeOf(module)) {
                node.addError("Can only reference module variables from within the module.");
            }
        }


        return result;
    }

    public static @Nullable NameDef tryGetNameDef(NameRef e) {
        NameLink link = e.attrNameLink();
        if (link == null) {
            return null;
        }
        return link.getDef();
    }

    public static @Nullable NameDef tryGetNameDef(NameDef e) {
        return e;
    }

    public static @Nullable NameDef tryGetNameDef(FuncRef e) {
        FuncLink link = e.attrFuncLink();
        if (link == null) {
            return null;
        }
        return link.getDef();
    }

    public static @Nullable NameDef tryGetNameDef(TypeRef e) {
        return e.attrTypeDef();
    }

    public static @Nullable NameDef tryGetNameDef(Element elem) {
        return null;
    }


    public static NameDef calculateDef(NameRef nameRef) {
        NameLink l = nameRef.attrNameLink();
        return l == null ? null : l.getDef();
    }
}
