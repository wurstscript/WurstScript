package de.peeeq.wurstscript.attributes;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.ast.*;

public class ReadVariables {

    public static ImmutableList<NameDef> calculate(WStatement e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(StmtSet e) {
        return e.getRight().attrReadVariables()
                .cons(generic(e.getUpdatedExpr()));

    }

    public static ImmutableList<NameDef> calculate(LocalVarDef e) {
        return generic(e);
    }

    private static ImmutableList<NameDef> generic(Element e) {
        ImmutableList<NameDef> r = ImmutableList.emptyList();
        for (int i = 0; i < e.size(); i++) {
            if (e.get(i) instanceof HasReadVariables) {
                HasReadVariables child = (HasReadVariables) e.get(i);
                r = r.cons(child.attrReadVariables());
            } else {
                r = r.cons(generic(e.get(i)));
            }
        }
        return r;
    }

    public static ImmutableList<NameDef> calculate(ExprVarAccess e) {
        if (e.attrNameLink() != null) {
            return ImmutableList.of(e.attrNameDef());
        } else {
            return ImmutableList.emptyList();
        }
    }

    public static ImmutableList<NameDef> calculate(ExprVarArrayAccess e) {
        ImmutableList<NameDef> r = ImmutableList.emptyList();
        if (e.attrNameLink() != null) {
            r = ImmutableList.of(e.attrNameDef());
        }
        r = r.cons(generic(e.getIndexes()));
        return r;
    }

    public static ImmutableList<NameDef> calculate(ExprMemberArrayVar e) {
        ImmutableList<NameDef> r = ImmutableList.emptyList();
        if (e.attrNameLink() != null) {
            r = ImmutableList.of(e.attrNameDef());
        }
        r = r.cons(e.getLeft().attrReadVariables());
        r = r.cons(generic(e.getIndexes()));
        return r;
    }

    public static ImmutableList<NameDef> calculate(ExprBinary e) {
        return e.getLeft().attrReadVariables()
                .cons(e.getRight().attrReadVariables());
    }

    public static ImmutableList<NameDef> calculate(ExprCast e) {
        return e.getExpr().attrReadVariables();
    }

    public static ImmutableList<NameDef> calculate(ExprIncomplete e) {
        return ImmutableList.emptyList();
    }

    public static ImmutableList<NameDef> calculate(ExprInstanceOf e) {
        return e.getExpr().attrReadVariables();
    }

    public static ImmutableList<NameDef> calculate(ExprUnary e) {
        return e.getRight().attrReadVariables();
    }

    public static ImmutableList<NameDef> calculate(ExprBoolVal e) {
        return ImmutableList.emptyList();
    }

    public static ImmutableList<NameDef> calculate(ExprIntVal e) {
        return ImmutableList.emptyList();
    }

    public static ImmutableList<NameDef> calculate(ExprNull e) {
        return ImmutableList.emptyList();
    }

    public static ImmutableList<NameDef> calculate(ExprRealVal e) {
        return ImmutableList.emptyList();
    }

    public static ImmutableList<NameDef> calculate(ExprStringVal e) {
        return ImmutableList.emptyList();
    }

    public static ImmutableList<NameDef> calculate(ExprSuper e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(ExprThis e) {
        return ImmutableList.emptyList();
    }

    public static ImmutableList<NameDef> calculate(ExprFuncRef exprFuncRef) {
        return ImmutableList.emptyList();
    }

    public static ImmutableList<NameDef> calculate(ExprTypeId e) {
        return e.getLeft().attrReadVariables();
    }

    public static ImmutableList<NameDef> calculate(ExprClosure e) {
        return e.getImplementation().attrReadVariables();
    }

    public static ImmutableList<NameDef> calculate(ExprStatementsBlock e) {
        // TODO not sure what to do here
        return generic(e);
//		 return ImmutableList.emptyList();
    }

    public static ImmutableList<NameDef> calculate(ExprDestroy e) {
        return e.getDestroyedObj().attrReadVariables();
    }

    public static ImmutableList<NameDef> calculate(FunctionImplementation e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(ClassDef e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(CompilationUnit e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(ConstructorDef e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(EnumDef e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(InitBlock e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(InterfaceDef e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(ModuleDef e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(ModuleInstanciation e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(NativeFunc e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(OnDestroyDef e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(TupleDef e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(WEntities e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(WPackage e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(WurstModel e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(WStatements e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(ExprEmpty exprEmpty) {
        return ImmutableList.emptyList();
    }

    public static ImmutableList<NameDef> calculate(ExprIfElse e) {
        return generic(e);
    }

    public static ImmutableList<NameDef> calculate(ExprArrayLength exprArrayLength) {
        return ImmutableList.emptyList();
    }
}
