package de.peeeq.wurstscript.attributes.prettyPrint;

import org.eclipse.jdt.annotation.NonNull;

import de.peeeq.wurstscript.ast.*;

public class PrettyPrinter {

	public static void prettyPrint(Annotation e, Spacer spacer, StringBuilder sb, int indent) {
		sb.append(e.getAnnotationType());
	}

	public static void prettyPrint(Arguments e, Spacer spacer, StringBuilder sb, int indent) {
		commaSeparatedList(e, spacer, sb, indent);
	}

	private static void commaSeparatedList(AstElement e, Spacer spacer, StringBuilder sb, int indent) {
		for (int i = 0; i < e.size(); i++) {
			if (i > 0) {
				sb.append(",");
				spacer.addSpace(sb, e.get(i - 1), e.get(i));
			}
			e.get(i).prettyPrint(spacer, sb, indent);
		}
	}

	public static void prettyPrint(ArraySizes e, Spacer spacer, StringBuilder sb, int indent) {
		commaSeparatedList(e, spacer, sb, indent);
	}

	public static void prettyPrint(ClassDef e, Spacer spacer, StringBuilder sb, int indent) {
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
		sb.append("\n\n");
		
		for (ModuleUse u : e.getModuleUses()) {
			
		}
		
	}

	private static void printIndent(StringBuilder sb, int indent) {
		for (int i=0; i<indent; i++) {
			sb.append("\t");
		}
	}

	public static void prettyPrint(CompilationUnit e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ConstructorDef e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ConstructorDefs e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(EndFunctionStatement e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(EnumDef e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(EnumMember e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(EnumMembers e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprBinary e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprBoolVal e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprCast e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprClosure e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprDestroy e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprEmpty e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprFuncRef e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprFunctionCall e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprIncomplete e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprInstanceOf e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprIntVal e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprMemberArrayVarDot e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprMemberArrayVarDotDot e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprMemberMethodDot e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprMemberMethodDotDot e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprMemberVarDot e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprMemberVarDotDot e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprNewObject e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprNull e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprRealVal e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprStatementsBlock e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprStringVal e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprSuper e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprThis e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprTypeId e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprUnary e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprVarAccess e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExprVarArrayAccess e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ExtensionFuncDef e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(FuncDef e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(FuncDefs e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(FuncSignature e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(GlobalVarDef e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(GlobalVarDefs e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(IdentifierWithTypeArgs e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(IdentifierWithTypeParamDefs e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(Indexes e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(InitBlock e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(InterfaceDef e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(JassGlobalBlock e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(JassToplevelDeclarations e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(LocalVarDef e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ModAbstract e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ModConstant e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(Modifiers e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ModOverride e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ModStatic e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(ModuleDef e, Spacer spacer, StringBuilder sb, int indent) {
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
	}

	public static void prettyPrint(StmtLoop e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(StmtReturn e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(StmtSet e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(StmtSkip e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(StmtWhile e, Spacer spacer, StringBuilder sb, int indent) {
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
	}

	public static void prettyPrint(TypeExprList e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(TypeExprResolved e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(TypeExprSimple e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(TypeExprThis e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(TypeParamDef e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(TypeParamDefs e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(VisibilityDefault e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(VisibilityPrivate e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(VisibilityProtected e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(VisibilityPublic e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(VisibilityPublicread e, Spacer spacer, StringBuilder sb, int indent) {
	}

	public static void prettyPrint(WBlock wBlock, Spacer spacer, StringBuilder sb, int indent) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void prettyPrint(WEntities wEntities, Spacer spacer, StringBuilder sb, int indent) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void prettyPrint(WImport wImport, Spacer spacer, StringBuilder sb, int indent) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void prettyPrint(WurstModel wurstModel, Spacer spacer, StringBuilder sb, int indent) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void prettyPrint(WurstDoc wurstDoc, Spacer spacer, StringBuilder sb, int indent) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void prettyPrint(WStatements wStatements, Spacer spacer, StringBuilder sb, int indent) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void prettyPrint(WParameters wParameters, Spacer spacer, StringBuilder sb, int indent) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void prettyPrint(WParameter wParameter, Spacer spacer, StringBuilder sb, int indent) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void prettyPrint(WPackages wPackages, Spacer spacer, StringBuilder sb, int indent) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void prettyPrint(WPackage wPackage, Spacer spacer, StringBuilder sb, int indent) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void prettyPrint(WImports wImports, Spacer spacer, StringBuilder sb, int indent) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void prettyPrint(ClassDefs classDefs, Spacer spacer, StringBuilder sb, int indent) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

}
