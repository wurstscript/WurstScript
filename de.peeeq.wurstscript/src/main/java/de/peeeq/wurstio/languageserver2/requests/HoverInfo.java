package de.peeeq.wurstio.languageserver2.requests;

import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver2.BufferManager;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by peter on 24.04.16.
 */
public class HoverInfo extends UserRequest<Hover> {


    private final String filename;
    private final String buffer;
    private final int line;
    private final int column;

    public HoverInfo(TextDocumentPositionParams position, BufferManager bufferManager) {
        this.filename = position.getTextDocument().getUri();
        this.buffer = bufferManager.getBuffer(position.getTextDocument());
        this.line = position.getPosition().getLine() + 1;
        this.column = position.getPosition().getCharacter();
    }

    @Override
    public Hover execute(ModelManager modelManager) {
        CompilationUnit cu = modelManager.replaceCompilationUnitContent(filename, buffer, false);
        Element e = Utils.getAstElementAtPos(cu, line, column, false);
        WLogger.info("hovering over " + Utils.printElement(e));
        return new Hover(e.match(new Description()));
    }

    static class Description implements Element.Matcher<List<Either<String, MarkedString>>> {

        public List<Either<String, MarkedString>> description(FunctionDefinition f) {
            List<Either<String, MarkedString>> result = new ArrayList<>();
            String comment = f.attrComment();

            // TODO parse comment
            if (comment != null && !comment.isEmpty()) {
                result.add(Either.forLeft(comment));
            }

            String params = getParameterString(f);
            String returnTypeHtml = type(f.getReturnTyp().attrTyp());
            String functionDescription = "";

            String funcName = f.getName();
            if (f instanceof ExtensionFuncDef) {
                ExtensionFuncDef exf = (ExtensionFuncDef) f;
                funcName = type(exf.getExtendedType().attrTyp()) + "." + funcName;
            }
            functionDescription += "function " + funcName + "(" + params + ") ";
            if (!f.attrTyp().isVoid()) {
                functionDescription += "returns " + returnTypeHtml;
            }
            result.add(Either.forRight(new MarkedString("wurst", functionDescription)));
            result.add(Either.forLeft("defined in " + nearestScopeName(f)));
            return result;
        }

        public String getParameterString(AstElementWithParameters f) {
            StringBuilder descrhtml = new StringBuilder();
            boolean first = true;
            for (WParameter p : f.getParameters()) {
                if (!first) {
                    descrhtml.append(", ");
                }
                descrhtml.append(type(p.attrTyp()) + " " + p.getName());
                first = false;
            }
            String params = descrhtml.toString();
            return params;
        }

        private static String nearestScopeName(NameDef n) {
            if (n.attrNearestNamedScope() != null) {
                return Utils.printElement(n.attrNearestNamedScope());
            } else {
                return "Global";
            }
        }

        public List<Either<String, MarkedString>> description(NameDef n) {
            List<Either<String, MarkedString>> result = new ArrayList<>();
            String comment = n.attrComment();
            if (comment != null && !comment.isEmpty()) {
                result.add(Either.forLeft(comment));
            }

            String additionalProposalInfo = type(n.attrTyp()) + " " + n.getName()
                    + " defined in " + nearestScopeName(n);
            result.add(Either.forLeft(additionalProposalInfo));
            return result;
        }

        public  List<Either<String, MarkedString>> description(NameRef nr) {
            NameDef nameDef = nr.attrNameDef();
            if (nameDef == null) {
                return string(nr.getVarName() + " is not defined yet.");
            }
            return nameDef.match(this);
        }

        @Override
        public List<Either<String, MarkedString>> case_NativeFunc(NativeFunc nativeFunc) {
            return description(nativeFunc);
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprEmpty(ExprEmpty exprEmpty) {
            return string("empty expression");
        }

        @Override
        public List<Either<String, MarkedString>> case_ModuleDef(ModuleDef moduleDef) {
            return description(moduleDef);
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtErr(StmtErr stmtErr) {
            return string("Error statement");
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprRealVal(ExprRealVal exprRealVal) {
            return string("The number " + exprRealVal.getValR() + " of type real.");
        }

        @Override
        public List<Either<String, MarkedString>> case_NoExpr(NoExpr noExpr) {
            return string("no expression");
        }

        @Override
        public List<Either<String, MarkedString>> case_StartFunctionStatement(StartFunctionStatement startFunctionStatement) {
            return string("start function call");
        }

        @Override
        public List<Either<String, MarkedString>> case_ModStatic(ModStatic modStatic) {
            return string("static: This function or variable is just like a function outside of a class. "
                    + "It is not bound to an instance. No dynamic dispatch is used.");
        }

        @Override
        public List<Either<String, MarkedString>> case_ClassDef(ClassDef classDef) {
            return description(classDef);
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprIntVal(ExprIntVal exprIntVal) {
            return string("integer constant");
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprCast(ExprCast e) {
            return string("Change the type of the left expression to " + type(e.getTyp().attrTyp()));
        }

        @Override
        public List<Either<String, MarkedString>> case_WImport(WImport imp) {
            WPackage imported = imp.attrImportedPackage();
            if (imported != null)
                return string(imported.attrComment());
            return string("import ...");
        }

        @Override
        public List<Either<String, MarkedString>> case_TupleDef(TupleDef tupleDef) {
            return description(tupleDef);
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprVarAccess(ExprVarAccess exprVarAccess) {
            return description(exprVarAccess);
        }

        @Override
        public List<Either<String, MarkedString>> case_InterfaceDef(InterfaceDef interfaceDef) {
            return description(interfaceDef);
        }

        @Override
        public List<Either<String, MarkedString>> case_VisibilityProtected(VisibilityProtected visibilityProtected) {
            return string("protected: can be used in subclasses and in the same package");
        }

        @Override
        public List<Either<String, MarkedString>> case_Annotation(Annotation annotation) {
            // TODO different annotations
            return string("This is an annotation.");
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtExitwhen(StmtExitwhen stmtExitwhen) {
            return string("extiwhen: Exits the current loop when the condition is true");
        }

        @Override
        public List<Either<String, MarkedString>> case_ConstructorDef(ConstructorDef constr) {
            List<Either<String, MarkedString>> result = new ArrayList<>();
            ClassDef c = constr.attrNearestClassDef();
            String comment = constr.attrComment();
            result.add(Either.forLeft(comment));


            String descr = "construct(" + getParameterString(constr) + ") "
                     + "defined in class " + c.getName();
            result.add(Either.forRight(new MarkedString("wurst", descr)));
            return result;
        }

        @Override
        public List<Either<String, MarkedString>> case_WImports(WImports wImports) {
            return string("imports");
        }

        @Override
        public List<Either<String, MarkedString>> case_WStatements(WStatements wStatements) {
            return string("statements");
        }

        @Override
        public List<Either<String, MarkedString>> case_CompilationUnit(CompilationUnit compilationUnit) {
            return string("File " + compilationUnit.getFile());
        }

        @Override
        public List<Either<String, MarkedString>> case_SwitchStmt(SwitchStmt switchStmt) {
            return string("A switch statement does different things depending on the value of an epxression.");
        }

        @Override
        public List<Either<String, MarkedString>> case_ExtensionFuncDef(ExtensionFuncDef extensionFuncDef) {
            return description(extensionFuncDef);
        }

        @Override
        public List<Either<String, MarkedString>> case_EndFunctionStatement(EndFunctionStatement endFunctionStatement) {
            return string("end function");
        }

        @Override
        public List<Either<String, MarkedString>> case_ModOverride(ModOverride modOverride) {
            return string("override: This function overrides an other function from a module or superclass");
        }

        @Override
        public List<Either<String, MarkedString>> case_LocalVarDef(LocalVarDef v) {
            return string("Local Variable " + v.getName() + " of type " + type(v.attrTyp()));
        }

        private List<Either<String, MarkedString>> string(String s) {
            return Collections.singletonList(Either.forLeft(s));
        }

        private String type(WurstType wurstType) {
            return wurstType.toString();
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtForRangeDown(StmtForRangeDown s) {
            return string("Do something for all " + s.getLoopVar().getName() + " counting from _ down to _");
        }

        @Override
        public List<Either<String, MarkedString>> case_NoDefaultCase(NoDefaultCase noDefaultCase) {
            return string("no default case");
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprSuper(ExprSuper exprSuper) {
            return string("super: refers to the super class (extends ...)  of this class");
        }

        @Override
        public List<Either<String, MarkedString>> case_IdentifierWithTypeArgs(IdentifierWithTypeArgs identifierWithTypeArgs) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprMemberVarDotDot(ExprMemberVarDotDot exprMemberVarDotDot) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprVarArrayAccess(ExprVarArrayAccess exprVarArrayAccess) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprMemberMethodDot(ExprMemberMethodDot exprMemberMethodDot) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprClosure(ExprClosure exprClosure) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_SwitchCases(SwitchCases switchCases) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprBinary(ExprBinary exprBinary) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_NoTypeExpr(NoTypeExpr noTypeExpr) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ModuleUse(ModuleUse moduleUse) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprFunctionCall(ExprFunctionCall exprFunctionCall) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprBoolVal(ExprBoolVal exprBoolVal) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ModConstant(ModConstant modConstant) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprTypeId(ExprTypeId exprTypeId) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_TypeExprList(TypeExprList typeExprList) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_FuncDefs(FuncDefs funcDefs) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprNewObject(ExprNewObject exprNewObject) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_VisibilityPrivate(VisibilityPrivate visibilityPrivate) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_VisibilityDefault(VisibilityDefault visibilityDefault) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_Arguments(Arguments arguments) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ModuleInstanciations(ModuleInstanciations moduleInstanciations) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_SwitchDefaultCaseStatements(SwitchDefaultCaseStatements switchDefaultCaseStatements) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprStatementsBlock(ExprStatementsBlock exprStatementsBlock) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ModuleUses(ModuleUses moduleUses) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_GlobalVarDef(GlobalVarDef globalVarDef) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_JassToplevelDeclarations(JassToplevelDeclarations jassToplevelDeclarations) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprIncomplete(ExprIncomplete exprIncomplete) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprMemberArrayVarDotDot(ExprMemberArrayVarDotDot exprMemberArrayVarDotDot) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ConstructorDefs(ConstructorDefs constructorDefs) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_TypeExprArray(TypeExprArray typeExprArray) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_TypeExprSimple(TypeExprSimple typeExprSimple) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_Modifiers(Modifiers modifiers) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ModAbstract(ModAbstract modAbstract) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_WBlock(WBlock wBlock) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtSkip(StmtSkip stmtSkip) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_FuncDef(FuncDef funcDef) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_NativeType(NativeType nativeType) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtForRangeUp(StmtForRangeUp stmtForRangeUp) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtLoop(StmtLoop stmtLoop) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprStringVal(ExprStringVal exprStringVal) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprNull(ExprNull exprNull) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ClassDefs(ClassDefs classDefs) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ModuleInstanciation(ModuleInstanciation moduleInstanciation) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprMemberArrayVarDot(ExprMemberArrayVarDot exprMemberArrayVarDot) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprFuncRef(ExprFuncRef exprFuncRef) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_TypeParamDefs(TypeParamDefs typeParamDefs) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtForFrom(StmtForFrom stmtForFrom) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_Indexes(Indexes indexes) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_VisibilityPublicread(VisibilityPublicread visibilityPublicread) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_EnumMember(EnumMember enumMember) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprInstanceOf(ExprInstanceOf exprInstanceOf) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_WurstModel(WurstModel wurstModel) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_Identifier(Identifier identifier) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtWhile(StmtWhile stmtWhile) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprMemberMethodDotDot(ExprMemberMethodDotDot exprMemberMethodDotDot) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_TypeParamDef(TypeParamDef typeParamDef) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_IdentifierWithTypeParamDefs(IdentifierWithTypeParamDefs identifierWithTypeParamDefs) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_GlobalVarDefs(GlobalVarDefs globalVarDefs) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprThis(ExprThis exprThis) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtReturn(StmtReturn stmtReturn) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_WPackages(WPackages wPackages) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_WurstDoc(WurstDoc wurstDoc) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtIf(StmtIf stmtIf) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_WPackage(WPackage wPackage) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_OnDestroyDef(OnDestroyDef onDestroyDef) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_TypeExprResolved(TypeExprResolved typeExprResolved) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_VisibilityPublic(VisibilityPublic visibilityPublic) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_TopLevelDeclarations(TopLevelDeclarations topLevelDeclarations) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtSet(StmtSet stmtSet) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprDestroy(ExprDestroy exprDestroy) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_WEntities(WEntities wEntities) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ArraySizes(ArraySizes arraySizes) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_EnumDef(EnumDef enumDef) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_SwitchCase(SwitchCase switchCase) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_EnumMembers(EnumMembers enumMembers) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_TypeExprThis(TypeExprThis typeExprThis) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprUnary(ExprUnary exprUnary) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_JassGlobalBlock(JassGlobalBlock jassGlobalBlock) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtForIn(StmtForIn stmtForIn) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_InitBlock(InitBlock initBlock) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_WParameter(WParameter wParameter) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprMemberVarDot(ExprMemberVarDot exprMemberVarDot) {
            return null;
        }

        @Override
        public List<Either<String, MarkedString>> case_WParameters(WParameters wParameters) {
            return null;
        }
    }

}
