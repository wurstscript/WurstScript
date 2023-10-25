package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SmallHelpers {

    public static boolean hasEmptyBody(FunctionLike f) {
        return f.getBody().size() <= 2;
    }

    public static @Nullable StmtReturn getReturnStatement(ExprStatementsBlock e) {
        if (e.getBody().size() <= 2) {
            return null;
        }
        WStatement lastStatement = e.getBody().get(e.getBody().size() - 2);
        if (lastStatement instanceof StmtReturn) {
            return (StmtReturn) lastStatement;
        }
        return null;
    }

    public static boolean isModuleUseTypeArg(TypeExpr e) {
        Optional<ModuleUse> mUse = Utils.getNearestByType(Optional.of(e), ModuleUse.class);
        return mUse.map(moduleUse -> moduleUse.isSubtreeOf(e)).orElse(false);
    }

    public static boolean isSubtreeOf(@Nullable Element subtree, Element of) {
        while (subtree != null) {
            if (subtree == of) return true;
            subtree = subtree.getParent();
        }
        return false;
    }

    public static boolean isStructureDefTypeParam(TypeParamDef tp) {
        Optional<StructureDef> sDef = Utils.getNearestByType(Optional.of(tp), StructureDef.class);
        if (!sDef.isPresent()) {
            return false;
        }
        if (sDef.get() instanceof AstElementWithTypeParameters) {
            return tp.isSubtreeOf(((AstElementWithTypeParameters) sDef.get()).getTypeParameters());
        }
        return false;
    }

    public static String getName(NameDef nd) {
        return nd.getNameId().getName();
    }


    public static String getVarName(ExprMemberArrayVar e) {
        return e.getVarNameId().getName();
    }

    public static String getVarName(ExprMemberVar e) {
        return e.getVarNameId().getName();
    }


    public static String getVarName(ExprMemberMethod e) {
        return e.getFuncNameId().getName();
    }

    public static String getVarName(NameRef e) {
        return e.getVarNameId().getName();
    }

    public static String getFuncName(FuncRef e) {
        return e.getFuncNameId().getName();
    }

    public static String getPackagename(WImport i) {
        return i.getPackagenameId().getName();
    }

    public static String getTypeName(ExprNewObject e) {
        return e.getTypeNameId().getName();
    }

    public static String getModuleName(ModuleUse m) {
        return m.getModuleNameId().getName();
    }

    public static List<Expr> superArgs(ConstructorDef constr) {
        return constr.getSuperConstructorCall().match(new SuperConstructorCall.Matcher<List<Expr>>() {
            @Override
            public List<Expr> case_NoSuperConstructorCall(NoSuperConstructorCall s) {
                return Collections.emptyList();
            }

            @Override
            public List<Expr> case_SomeSuperConstructorCall(SomeSuperConstructorCall s) {
                return s.getSuperArgs();
            }
        });
    }

}
