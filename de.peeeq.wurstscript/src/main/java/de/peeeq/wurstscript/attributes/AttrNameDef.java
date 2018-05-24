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


    public static NameDef calculate(ExprVarArrayAccess term) {
        return searchNameInScope(term.getVarName(), term);
    }

    public static NameDef calculate(ExprVarAccess term) {
        NameDef result = specialEnumLookupRules(term);
        if (result != null) {
            return result;
        }
        return searchNameInScope(term.getVarName(), term);
    }

    public static @Nullable NameDef specialEnumLookupRules(ExprVarAccess term) {
        NameDef result = null;
        Element parent = term.getParent();
        if (parent instanceof SwitchCase) {
            SwitchStmt s = (SwitchStmt) parent.getParent().getParent();
            result = lookupEnumConst(term.getVarName(), s.getExpr().attrTyp());
        } else if (parent instanceof StmtSet) {
            StmtSet s = (StmtSet) parent;
            if (s.getRight() == term) {
                result = lookupEnumConst(term.getVarName(), s.getUpdatedExpr().attrTyp());
            }
        } else if (parent instanceof GlobalOrLocalVarDef) {
            GlobalOrLocalVarDef v = (GlobalOrLocalVarDef) parent;
            result = lookupEnumConst(term.getVarName(), v.getOptTyp().attrTyp());
        }
        return result;
    }

    public static @Nullable NameDef lookupEnumConst(String varName, WurstType t) {
        if (t instanceof WurstTypeEnum) {
            WurstTypeEnum e = (WurstTypeEnum) t;
            // if we expect an enum type we can as well directly look into the enum
            EnumDef eDef = e.getDef();
            return eDef.lookupMemberVar(e, varName, false);
        }
        return null;
    }

    public static @Nullable NameDef calculate(ExprMemberVar term) {
        return memberVarCase(term.getLeft(), term.getVarName(), isWriteAccess(term), term);
    }

    public static @Nullable NameDef calculate(ExprMemberArrayVar term) {
        return memberVarCase(term.getLeft(), term.getVarName(), isWriteAccess(term), term);
    }


    protected static NameDef searchNameInScope(String varName, NameRef node) {
        boolean showErrors = !varName.startsWith("gg_");
        NameDef result = node.lookupVar(varName, showErrors);
        return result;
    }

    private static boolean isWriteAccess(final NameRef node) {
        boolean writeAccess1 = false;
        if (node.getParent() instanceof StmtSet) {
            StmtSet stmtSet = (StmtSet) node.getParent();
            if (stmtSet.getUpdatedExpr() == node) {
                writeAccess1 = true;
            }
        }
        final boolean writeAccess = writeAccess1;
        return writeAccess;
    }

    private static @Nullable NameDef memberVarCase(Expr left, String varName, boolean writeAccess, Expr node) {
        WurstType receiverType = left.attrTyp();
        NameDef result = node.lookupMemberVar(receiverType, varName);
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
        NameLink link = e.attrNameDef();
        if (link == null) {
            return null;
        }
        return link.getDef();
    }

    public static @Nullable NameDef tryGetNameDef(NameDef e) {
        return e;
    }

    public static @Nullable NameDef tryGetNameDef(FuncRef e) {
        FuncLink link = e.attrFuncDef();
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


}
