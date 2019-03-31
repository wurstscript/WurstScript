package de.peeeq.wurstscript.attributes.prettyPrint;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.utils.Utils;
import org.apache.commons.lang.StringUtils;

public class PrettyPrinter {

    public static void prettyPrint(Annotation e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(e.getAnnotationType());
    }

    public static void prettyPrint(Arguments e, Spacer spacer, StringBuilder sb, int indent) {
        commaSeparatedList(e, spacer, sb, indent);
    }

    public static void prettyPrint(ExprList e, Spacer spacer, StringBuilder sb, int indent) {
        commaSeparatedList(e, spacer, sb, indent);
    }

    private static void commaSeparatedList(Element e, Spacer spacer, StringBuilder sb, int indent) {
        for (int i = 0; i < e.size(); i++) {
            if (i > 0) {
                sb.append(",");
                spacer.addSpace(sb);
            }
            e.get(i).prettyPrint(spacer, sb, indent);
        }
    }

    public static void prettyPrint(ArraySizes e, Spacer spacer, StringBuilder sb, int indent) {
        commaSeparatedList(e, spacer, sb, indent);
    }

    public static void prettyPrint(ClassDef e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("\n");
        printIndent(sb, indent);
        e.getModifiers().prettyPrint(spacer, sb, indent);
        sb.append("class ");
        sb.append(e.getName());
        if (e.getExtendedClass() instanceof TypeExpr) {
            TypeExpr te = (TypeExpr) e.getExtendedClass();
            sb.append(" extends ");
            te.prettyPrint(spacer, sb, indent);
        }
        if (!e.getImplementsList().isEmpty()) {
            sb.append(" implements ");
            commaSeparatedList(e.getImplementsList(), spacer, sb, indent);
        }
        sb.append("\n");

        e.getModuleUses().prettyPrint(spacer, sb, indent + 1);
        e.getVars().prettyPrint(spacer, sb, indent + 1);
        e.getConstructors().prettyPrint(spacer, sb, indent + 1);
        e.getMethods().prettyPrint(spacer, sb, indent + 1);
    }

    private static void printIndent(StringBuilder sb, int indent) {
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            for (int i = 0; i < indent; i++) {
                sb.append("    ");
            }
        }
    }

    public static void prettyPrint(CompilationUnit e, Spacer spacer, StringBuilder sb, int indent) {
        prettyPrint(e.getPackages(), spacer, sb, indent);
    }

    public static void prettyPrint(ConstructorDef e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("\n");
        printIndent(sb, indent);
        e.getModifiers().prettyPrint(spacer, sb, indent);
        sb.append("construct");
        e.getParameters().prettyPrint(spacer, sb, indent);
        sb.append("\n");
        e.getSuperConstructorCall().prettyPrint(spacer, sb, indent);
        e.getBody().prettyPrint(spacer, sb, indent + 1);
    }

    public static void prettyPrint(ConstructorDefs e, Spacer spacer, StringBuilder sb, int indent) {
        for (ConstructorDef constructorDef : e) {
            if (!constructorDef.getParameters().isEmpty()
                    || constructorDef.getSuperConstructorCall() instanceof SomeSuperConstructorCall
                    || constructorDef.getBody().size() > 2) {
                constructorDef.prettyPrint(spacer, sb, indent);
            }
        }
    }

    public static void prettyPrint(EndFunctionStatement e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(EnumDef e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("\n");
        printIndent(sb, indent);
        e.getModifiers().prettyPrint(spacer, sb, indent);
        sb.append("enum");
        spacer.addSpace(sb);
        sb.append(e.getName());
        sb.append("\n");
        e.getMembers().prettyPrint(spacer, sb, indent + 1);
    }

    public static void prettyPrint(EnumMember e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        e.getModifiers().prettyPrint(spacer, sb, indent);
        sb.append(e.getName());
    }

    public static void prettyPrint(EnumMembers e, Spacer spacer, StringBuilder sb, int indent) {
        for (EnumMember enumMember : e) {
            enumMember.prettyPrint(spacer, sb, indent);
            sb.append("\n");
        }
    }

    public static void prettyPrint(ExprBinary e, Spacer spacer, StringBuilder sb, int indent) {
        e.getLeft().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append(e.getOp().toString());
        spacer.addSpace(sb);
        e.getRight().prettyPrint(spacer, sb, indent);
    }

    public static void prettyPrint(ExprBoolVal e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(String.valueOf(e.getValB()));
    }

    public static void prettyPrint(ExprCast e, Spacer spacer, StringBuilder sb, int indent) {
        e.getExpr().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append("castTo");
        spacer.addSpace(sb);
        e.getTyp().prettyPrint(spacer, sb, indent);
    }

    public static void prettyPrint(ExprClosure e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("(");
        e.getShortParameters().prettyPrint(spacer, sb, indent);
        sb.append(")");
        spacer.addSpace(sb);
        sb.append("->");
        spacer.addSpace(sb);
        e.getImplementation().prettyPrint(spacer, sb, indent);
    }

    public static void prettyPrint(ExprDestroy e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("destroy");
        spacer.addSpace(sb);
        e.getDestroyedObj().prettyPrint(spacer, sb, indent);
    }

    public static void prettyPrint(ExprEmpty e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(ExprFuncRef e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("function");
        spacer.addSpace(sb);
        if (!StringUtils.isBlank(e.getScopeName())) {
            sb.append(e.getScopeName());
            sb.append(".");
        }
        sb.append(e.getFuncName());
    }

    public static void prettyPrint(ExprFunctionCall e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append(e.getFuncName());
        sb.append("(");
        e.getArgs().prettyPrint(spacer, sb, indent);
        sb.append(")");
    }

    public static void prettyPrint(ExprIncomplete e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(ExprInstanceOf e, Spacer spacer, StringBuilder sb, int indent) {
        e.getExpr().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append("instanceof");
        spacer.addSpace(sb);
        e.getTyp().prettyPrint(spacer, sb, indent);
    }

    public static void prettyPrint(ExprIntVal e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(e.getValI());
    }

    public static void prettyPrint(ExprMemberArrayVarDot e, Spacer spacer, StringBuilder sb, int indent) {
        e.getLeft().prettyPrint(spacer, sb, indent);
        sb.append(".");
        sb.append(e.getVarName());
        sb.append("[");
        e.getIndexes().prettyPrint(spacer, sb, indent);
        sb.append("]");
    }

    public static void prettyPrint(ExprMemberArrayVarDotDot e, Spacer spacer, StringBuilder sb, int indent) {
        e.getLeft().prettyPrint(spacer, sb, indent);
        sb.append("..");
        sb.append(e.getVarName());
        sb.append("[");
        e.getIndexes().prettyPrint(spacer, sb, indent);
        sb.append("]");
    }

    public static void prettyPrint(ExprMemberMethodDot e, Spacer spacer, StringBuilder sb, int indent) {
        e.getLeft().prettyPrint(spacer, sb, indent);
        sb.append(".");
        sb.append(e.getFuncName());
        sb.append("(");
        e.getArgs().prettyPrint(spacer, sb, indent);
        sb.append(")");
    }

    public static void prettyPrint(ExprMemberMethodDotDot e, Spacer spacer, StringBuilder sb, int indent) {
        e.getLeft().prettyPrint(spacer, sb, indent);
        sb.append("..");
        sb.append(e.getFuncName());
        sb.append("(");
        e.getArgs().prettyPrint(spacer, sb, indent);
        sb.append(")");
        if (!(e.getParent() instanceof ExprMemberMethodDotDot)) {
            sb.append("\n");
        }
    }

    public static void prettyPrint(ExprMemberVarDot e, Spacer spacer, StringBuilder sb, int indent) {
        e.getLeft().prettyPrint(spacer, sb, indent);
        sb.append(".");
        sb.append(e.getVarName());
    }

    public static void prettyPrint(ExprMemberVarDotDot e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        e.getLeft().prettyPrint(spacer, sb, indent);
        sb.append("..");
        sb.append(e.getVarName());
    }

    public static void prettyPrint(ExprNewObject e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("new");
        spacer.addSpace(sb);
        sb.append(e.getTypeName());
        e.getTypeArgs().prettyPrint(spacer, sb, indent);
        sb.append("(");
        e.getArgs().prettyPrint(spacer, sb, indent);
        sb.append(")");
    }

    public static void prettyPrint(ExprNull e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("null");
    }

    public static void prettyPrint(ExprRealVal e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(e.getValR());
    }

    public static void prettyPrint(ExprStatementsBlock e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(ExprStringVal e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(Utils.escapeString(e.getValS()));
    }

    public static void prettyPrint(ExprSuper e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("super");
    }

    public static void prettyPrint(ExprThis e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("this");
    }

    public static void prettyPrint(ExprTypeId e, Spacer spacer, StringBuilder sb, int indent) {
        e.getLeft().prettyPrint(spacer, sb, indent);
        sb.append(".typeId");
    }

    public static void prettyPrint(ExprUnary e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(e.getOpU().toString());
        spacer.addSpace(sb);
        e.getRight().prettyPrint(spacer, sb, indent);
    }

    public static void prettyPrint(ExprVarAccess e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(e.getVarName());
    }

    public static void prettyPrint(ExprVarArrayAccess e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append(e.getVarName());
        sb.append("[");
        e.getIndexes().prettyPrint(spacer, sb, indent);
        sb.append("]");
    }

    public static void prettyPrint(ExtensionFuncDef e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        e.getModifiers().prettyPrint(spacer, sb, indent);
        sb.append("function");
        spacer.addSpace(sb);
        e.getExtendedType().prettyPrint(spacer, sb, indent);
        sb.append(".");
        sb.append(e.getName());
        sb.append("(");
        e.getParameters().prettyPrint(spacer, sb, indent);
        sb.append(")");
        sb.append("\n");
        e.getBody().prettyPrint(spacer, sb, indent + 1);
    }

    public static void prettyPrint(FuncDef e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("\n");
        printIndent(sb, indent);
        e.getModifiers().prettyPrint(spacer, sb, indent);
        sb.append("function");
        spacer.addSpace(sb);
        e.getTypeParameters().prettyPrint(spacer, sb, indent);
        sb.append(e.getName());
        e.getParameters().prettyPrint(spacer, sb, indent);
        if (!(e.getReturnTyp() instanceof NoTypeExpr)) {
            spacer.addSpace(sb);
            sb.append("returns");
            spacer.addSpace(sb);
            e.getReturnTyp().prettyPrint(spacer, sb, indent);
        }
        sb.append("\n");
        e.getBody().prettyPrint(spacer, sb, indent + 1);
        sb.append("\n");
    }

    public static void prettyPrint(FuncDefs e, Spacer spacer, StringBuilder sb, int indent) {
        for (FuncDef funcDef : e) {
            funcDef.prettyPrint(spacer, sb, indent);
        }
    }


    public static void prettyPrint(GlobalVarDef e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        e.getModifiers().prettyPrint(spacer, sb, indent);
        e.getOptTyp().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append(e.getName());

        if (!(e.getInitialExpr() instanceof NoExpr)) {
            sb.append(" = ");
            e.getInitialExpr().prettyPrint(spacer, sb, indent);
        }
        sb.append("\n");
    }

    public static void prettyPrint(GlobalVarDefs e, Spacer spacer, StringBuilder sb, int indent) {
        for (GlobalVarDef varDef : e) {
            prettyPrint(varDef, spacer, sb, indent);
        }
    }

    public static void prettyPrint(IdentifierWithTypeArgs e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(IdentifierWithTypeParamDefs e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(Indexes e, Spacer spacer, StringBuilder sb, int indent) {
        for (Expr expr : e) {
            expr.prettyPrint(spacer, sb, indent);
        }
    }

    public static void prettyPrint(InitBlock e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("init\n");
        e.getBody().prettyPrint(spacer, sb, indent + 1);
    }

    public static void prettyPrint(InterfaceDef e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("interface");
        spacer.addSpace(sb);
        sb.append(e.getName());
        sb.append("\n");
        e.getVars().prettyPrint(spacer, sb, indent);
        e.getConstructors().prettyPrint(spacer, sb, indent);
        e.getMethods().prettyPrint(spacer, sb, indent);
    }

    public static void prettyPrint(JassGlobalBlock e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(JassToplevelDeclarations e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(LocalVarDef e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        if ((e.getOptTyp() instanceof NoTypeExpr)) {
            sb.append("var");
        } else {
            e.getOptTyp().prettyPrint(spacer, sb, indent);
        }
        spacer.addSpace(sb);
        sb.append(e.getName());
        if (!(e.getInitialExpr() instanceof NoExpr)) {
            sb.append(" = ");
            e.getInitialExpr().prettyPrint(spacer, sb, indent);
        }
        sb.append("\n");
    }

    public static void prettyPrint(ModAbstract e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("abstract");
    }

    public static void prettyPrint(ModConstant e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("constant");
    }

    public static void prettyPrint(ModVararg e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("vararg");
    }

    public static void prettyPrint(Modifiers e, Spacer spacer, StringBuilder sb, int indent) {
        for (Modifier modifier : e) {
            modifier.prettyPrint(spacer, sb, indent);
            sb.append(" ");
        }
    }

    public static void prettyPrint(ModOverride e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("override");
    }

    public static void prettyPrint(ModStatic e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("static");
    }

    public static void prettyPrint(ModuleDef e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("modules");
        spacer.addSpace(sb);
        e.getVars().prettyPrint(spacer, sb, indent);
        e.getConstructors().prettyPrint(spacer, sb, indent);
        e.getMethods().prettyPrint(spacer, sb, indent);
    }

    public static void prettyPrint(ModuleInstanciation e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(ModuleInstanciations e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(ModuleUse e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(ModuleUses e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(NativeFunc e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(NativeType e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(NoDefaultCase e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(NoExpr e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(NoTypeExpr e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(OnDestroyDef e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(StartFunctionStatement e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(StmtErr e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(StmtExitwhen e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(StmtForFrom e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(StmtForIn e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(StmtForRangeDown e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(StmtForRangeUp e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(StmtIf e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("if");
        spacer.addSpace(sb);
        e.getCond().prettyPrint(spacer, sb, indent);
        sb.append("\n");
        e.getThenBlock().prettyPrint(spacer, sb, indent + 1);
        sb.append("\n");
        if (e.getElseBlock().size() > 0) {
            printIndent(sb, indent);
            sb.append("else\n");
            e.getElseBlock().prettyPrint(spacer, sb, indent + 1);
        }
    }

    public static void prettyPrint(StmtLoop e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(StmtReturn e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("return");
        spacer.addSpace(sb);
        e.getReturnedObj().prettyPrint(spacer, sb, indent);
        sb.append("\n");
    }

    public static void prettyPrint(StmtSet e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        e.getUpdatedExpr().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append("=");
        spacer.addSpace(sb);
        e.getRight().prettyPrint(spacer, sb, indent);
        sb.append("\n");
    }

    public static void prettyPrint(StmtSkip e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("skip");
        sb.append("\n");
    }

    public static void prettyPrint(StmtWhile e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("while");
        spacer.addSpace(sb);
    }

    public static void prettyPrint(SwitchCase e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(SwitchCases e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(SwitchDefaultCaseStatements e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(SwitchStmt e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(TopLevelDeclarations e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(TupleDef e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(TypeExprArray e, Spacer spacer, StringBuilder sb, int indent) {
        e.getBase().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append("array");
    }

    public static void prettyPrint(TypeExprList e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(TypeExprResolved e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(TypeExprSimple e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(e.getTypeName());
        e.getTypeArgs().prettyPrint(spacer, sb, indent);
    }

    public static void prettyPrint(TypeExprThis e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("thistype");
    }

    public static void prettyPrint(TypeParamDef e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(TypeParamDefs e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(VisibilityDefault e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(VisibilityPrivate e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("private");
    }

    public static void prettyPrint(VisibilityProtected e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("protected");
    }

    public static void prettyPrint(VisibilityPublic e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("public");
    }

    public static void prettyPrint(VisibilityPublicread e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("publicread");
    }

    public static void prettyPrint(WBlock wBlock, Spacer spacer, StringBuilder sb, int indent) {
        // TODO Auto-generated method stub
        throw new Error("not implemented");
    }

    public static void prettyPrint(WEntities wEntities, Spacer spacer, StringBuilder sb, int indent) {
        for (WEntity entity : wEntities) {
            entity.prettyPrint(spacer, sb, indent);
        }
    }

    public static void prettyPrint(WImport wImport, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("import");
        spacer.addSpace(sb);
        sb.append(wImport.getIsInitLater() ? "initlater" : "");
        spacer.addSpace(sb);
        sb.append(wImport.getIsPublic() ? "public" : "");
        spacer.addSpace(sb);
        sb.append(wImport.getPackagename());
        sb.append("\n");
    }

    public static void prettyPrint(WurstModel wurstModel, Spacer spacer, StringBuilder sb, int indent) {
        for (CompilationUnit compilationUnit : wurstModel) {
            compilationUnit.prettyPrint(spacer, sb, indent);
        }
    }

    public static void prettyPrint(WurstDoc wurstDoc, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append(wurstDoc.getRawComment());
        sb.append("\n");
    }

    public static void prettyPrint(WStatements wStatements, Spacer spacer, StringBuilder sb, int indent) {
        for (WStatement wStatement : wStatements) {
            wStatement.prettyPrint(spacer, sb, indent);
        }
    }

    public static void prettyPrint(WParameters wParameters, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("(");
        String prefix = "";
        for (WParameter wParameter : wParameters) {
            sb.append(prefix);
            prefix = ",";
            spacer.addSpace(sb);
            wParameter.prettyPrint(spacer, sb, indent);
        }
        sb.append(")");
    }

    public static void prettyPrint(WShortParameters wParameters, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("(");
        String prefix = "";
        for (WShortParameter wParameter : wParameters) {
            sb.append(prefix);
            prefix = ",";
            spacer.addSpace(sb);
            wParameter.prettyPrint(spacer, sb, indent);
        }
        sb.append(")");
    }

    public static void prettyPrint(WParameter wParameter, Spacer spacer, StringBuilder sb, int indent) {
        wParameter.getTyp().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append(wParameter.getName());
    }

    public static void prettyPrint(WShortParameter wParameter, Spacer spacer, StringBuilder sb, int indent) {
        OptTypeExpr t = wParameter.getTypOpt();
        if (t instanceof TypeExpr) {
            t.prettyPrint(spacer, sb, indent);
            spacer.addSpace(sb);
        }
        sb.append(wParameter.getName());
    }

    public static void prettyPrint(WPackages wPackages, Spacer spacer, StringBuilder sb, int indent) {
        for (WPackage wPackage : wPackages) {
            wPackage.prettyPrint(spacer, sb, indent);
        }
    }

    public static void prettyPrint(WPackage wPackage, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("package");
        spacer.addSpace(sb);
        sb.append(wPackage.getName());
        sb.append("\n\n");
        wPackage.getImports().prettyPrint(spacer, sb, indent);
        wPackage.getElements().prettyPrint(spacer, sb, indent);
    }

    public static void prettyPrint(WImports wImports, Spacer spacer, StringBuilder sb, int indent) {
        for (WImport wImport : wImports) {
            if (!wImport.getPackagename().equals("Wurst")) {
                prettyPrint(wImport, spacer, sb, indent);
            }
        }
    }

    public static void prettyPrint(ClassDefs classDefs, Spacer spacer, StringBuilder sb, int indent) {
        for (ClassDef classDef : classDefs) {
            classDef.prettyPrint(spacer, sb, indent);
        }
    }

    public static void prettyPrint(Identifier identifier, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(identifier.getName());
    }

    public static void prettyPrint(ExprIfElse e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("(");
        e.getCond().prettyPrint(spacer, sb, indent+1);
        sb.append(" ? ");
        e.getIfTrue().prettyPrint(spacer, sb, indent+1);
        sb.append(" : ");
        e.getIfFalse().prettyPrint(spacer, sb, indent+1);
        sb.append(")");
    }

    public static void prettyPrint(ArrayInitializer arrayInitializer, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("[");
        arrayInitializer.getValues().prettyPrint(spacer, sb, indent);
        sb.append("]");
    }

    public static void prettyPrint(NoSuperConstructorCall noSuperConstructorCall, Spacer spacer, StringBuilder sb, int indent) {
        // nothing
    }

    public static void prettyPrint(SomeSuperConstructorCall c, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent + 1);
        sb.append("super(");
        c.getSuperArgs().prettyPrint(spacer, sb, indent);
        sb.append(")\n");

    }

    public static void prettyPrint(NoTypeParamConstraints noTypeParamConstraints, Spacer spacer, StringBuilder sb, int indent) {
        // nothing
    }
}
