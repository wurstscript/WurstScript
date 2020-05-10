package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.types.CallSignature;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeNamedScope;
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


    private final WFile filename;
    private final String buffer;
    private final int line;
    private final int column;

    public HoverInfo(TextDocumentPositionParams position, BufferManager bufferManager) {
        this.filename = WFile.create(position.getTextDocument().getUri());
        this.buffer = bufferManager.getBuffer(position.getTextDocument());
        this.line = position.getPosition().getLine() + 1;
        this.column = position.getPosition().getCharacter() + 1;
    }

    @Override
    public Hover execute(ModelManager modelManager) {
        CompilationUnit cu = modelManager.replaceCompilationUnitContent(filename, buffer, false);
        if (cu == null) {
            return new Hover(Collections.singletonList(Either.forLeft("File " + filename + " is not part of the project. Move it to the wurst folder.")));
        }
        Element e = Utils.getAstElementAtPos(cu, line, column, false).get();
        WLogger.info("hovering over " + Utils.printElement(e));
        List<Either<String, MarkedString>> desription = e.match(new Description());
        desription = addArgumentHint(e, desription);

        return new Hover(desription);
    }

    private List<Either<String, MarkedString>> addArgumentHint(Element e, List<Either<String, MarkedString>> desription) {
        try {
            if (e.getParent() instanceof Arguments) {
                Arguments args = (Arguments) e.getParent();
                int index = args.indexOf(e);
                if (args.getParent() instanceof FunctionCall) {
                    FunctionCall fc = (FunctionCall) args.getParent();
                    FuncLink f = fc.attrFuncLink();
                    WurstType parameterType = f.getParameterType(index);
                    String parameterName = f.getParameterName(index);
                    desription = Utils.append(desription, Either.forLeft("Parameter " + parameterType + " " + parameterName));
                }
            }
        } catch (Exception ex) {
            WLogger.info("Could not get argument hint");
            WLogger.info(ex);
        }
        return desription;
    }

    private static List<Either<String, MarkedString>> description(Element n) {
        return n.match(new Description());
    }

    static String descriptionString(Element n) {
        List<Either<String, MarkedString>> descr = description(n);
        StringBuilder res = new StringBuilder();
        for (Either<String, MarkedString> d : descr) {
            if (d.isLeft()) {
                res.append(d.getLeft());
            } else {
                res.append(d.getRight().getValue());
            }
            res.append("\n");
        }
        return res.toString().trim();
    }

    public static String getParameterString(AstElementWithParameters f) {
        StringBuilder descrhtml = new StringBuilder();
        boolean first = true;
        for (WParameter p : f.getParameters()) {
            if (!first) {
                descrhtml.append(", ");
            }
            descrhtml.append(type(p.attrTyp())).append(" ").append(p.getName());
            first = false;
        }
        return descrhtml.toString();
    }

    private static String type(WurstType wurstType) {
        return wurstType.toString();
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
            String returnTypeHtml = type(f.attrReturnTyp());
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

        private static String nearestScopeName(Element n) {
            if (n.attrNearestNamedScope() != null) {
                return Utils.printElement(n.attrNearestNamedScope());
            } else {
                return "Global";
            }
        }

        public List<Either<String, MarkedString>> description(NameDef n) {
            if (n == null) {
                return Collections.emptyList();
            }
            List<Either<String, MarkedString>> result = new ArrayList<>();
            String comment = n.attrComment();
            if (comment != null && !comment.isEmpty()) {
                result.add(Either.forLeft(comment));
            }
            if (n.attrIsConstant()) {
                if (n instanceof GlobalOrLocalVarDef) {
                    GlobalOrLocalVarDef v = (GlobalOrLocalVarDef) n;
                    VarInitialization initialExpr = v.getInitialExpr();
                    String initial = Utils.prettyPrint(initialExpr);
                    result.add(Either.forRight(new MarkedString("wurst", " = " + initial)));
                }
            }

            String additionalProposalInfo = type(n.attrTyp()) + " " + n.getName()
                    + " defined in " + nearestScopeName(n);
            result.add(Either.forLeft(additionalProposalInfo));
            return result;
        }


        public List<Either<String, MarkedString>> description(ConstructorDef f) {
            List<Either<String, MarkedString>> result = new ArrayList<>();
            String comment = f.attrComment();

            // TODO parse comment
            if (comment != null && !comment.isEmpty()) {
                result.add(Either.forLeft(comment));
            }

            String params = getParameterString(f);
            String functionDescription = "";

            ClassOrModule classOrModule = f.attrNearestClassOrModule();
            if (classOrModule != null) {
                functionDescription += classOrModule.getName();
            }
            functionDescription += "(" + params + ") ";
            result.add(Either.forRight(new MarkedString("wurst", functionDescription)));
            result.add(Either.forLeft("defined in " + nearestScopeName(f)));
            return result;
        }

        public List<Either<String, MarkedString>> description(NameRef nr) {
            NameLink nameDef = nr.attrNameLink();
            if (nameDef == null) {
                return string(nr.getVarName() + " is not defined yet.");
            }
            return nameDef.getDef().match(this);
        }

        public List<Either<String, MarkedString>> description(FuncRef fr) {
            FuncLink def = fr.attrFuncLink();
            if (def == null) {
                return string(fr.getFuncName() + " is not defined yet.");
            }
            return def.getDef().match(this);
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
            FunctionDefinition def = annotation.attrFuncDef();
            if (def != null) {
                return string(def.attrComment());
            }
            return string("This is an undefined annotation.");
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtExitwhen(StmtExitwhen stmtExitwhen) {
            return string("extiwhen: Exits the current loop when the condition is true");
        }

        @Override
        public List<Either<String, MarkedString>> case_ConstructorDef(ConstructorDef constr) {
            List<Either<String, MarkedString>> result = new ArrayList<>();
            NamedScope c = constr.attrNearestNamedScope();
            String comment = constr.attrComment();
            result.add(Either.forLeft(comment));


            String descr = "construct(" + getParameterString(constr) + ") "
                    + "defined in " + Utils.printElement(c);
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
            return string("File " + compilationUnit.getCuInfo().getFile());
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
        public List<Either<String, MarkedString>> case_ArrayInitializer(ArrayInitializer arrayInitializer) {
            return string("Initial values for the array.");
        }

        @Override
        public List<Either<String, MarkedString>> case_ModOverride(ModOverride modOverride) {
            return string("override: This function overrides an other function from a module or superclass");
        }

        @Override
        public List<Either<String, MarkedString>> case_SomeSuperConstructorCall(SomeSuperConstructorCall sc) {
            ConstructorDef constr = (ConstructorDef) sc.getParent();
            ConstructorDef superConstr = constr.attrSuperConstructor();
            if (superConstr == null) {
                return string("Calling an unknown super constructor");
            } else {
                return description(superConstr);
            }
        }

        @Override
        public List<Either<String, MarkedString>> case_LocalVarDef(LocalVarDef v) {
            return string("Local Variable " + v.getName() + " of type " + type(v.attrTyp()));
        }

        private List<Either<String, MarkedString>> string(String s) {
            return Collections.singletonList(Either.forLeft(s));
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
            return identifierWithTypeArgs.getParent().match(this);
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprMemberVarDotDot(ExprMemberVarDotDot exprMemberVarDotDot) {
            return description(exprMemberVarDotDot);
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprVarArrayAccess(ExprVarArrayAccess exprVarArrayAccess) {
            return description(exprVarArrayAccess);
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprMemberMethodDot(ExprMemberMethodDot exprMemberMethodDot) {
            return description(exprMemberMethodDot);
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprClosure(ExprClosure exprClosure) {
            return string("Closure with type " + exprClosure.attrTyp() + " (implements " + exprClosure.attrExpectedTypAfterOverloading() + ")");
        }

        @Override
        public List<Either<String, MarkedString>> case_SwitchCases(SwitchCases switchCases) {
            return string("A switch statement");
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprBinary(ExprBinary exprBinary) {
            FuncLink funcDef = exprBinary.attrFuncLink();
            if (funcDef != null) {
                return description(funcDef.getDef());
            }
            return string("A binary operation");
        }

        @Override
        public List<Either<String, MarkedString>> case_NoTypeExpr(NoTypeExpr noTypeExpr) {
            return string("not type");
        }

        @Override
        public List<Either<String, MarkedString>> case_ModuleUse(ModuleUse moduleUse) {
            return description(moduleUse.attrModuleDef());
        }


        @Override
        public List<Either<String, MarkedString>> case_ExprFunctionCall(ExprFunctionCall exprFunctionCall) {
            return description(exprFunctionCall);
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprBoolVal(ExprBoolVal exprBoolVal) {
            return string("A boolean value");
        }

        @Override
        public List<Either<String, MarkedString>> case_ModConstant(ModConstant modConstant) {
            return string("This modifiers ensures that the variable cannot change after initialization.");
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprTypeId(ExprTypeId exprTypeId) {
            return string("Get the typeId");
        }

        @Override
        public List<Either<String, MarkedString>> case_TypeExprList(TypeExprList typeExprList) {
            return string("A list of type-expressions");
        }

        @Override
        public List<Either<String, MarkedString>> case_FuncDefs(FuncDefs funcDefs) {
            return string("A list of function definitions");
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprNewObject(ExprNewObject exprNewObject) {
            ConstructorDef constr = exprNewObject.attrConstructorDef();
            if (constr == null) {
                return string("Constructor for " + exprNewObject.getTypeName() + " not defined yet.");
            }
            return description(constr);
        }

        @Override
        public List<Either<String, MarkedString>> case_VisibilityPrivate(VisibilityPrivate visibilityPrivate) {
            return string("Modifier private: This element can only be used in the current scope.");
        }

        @Override
        public List<Either<String, MarkedString>> case_VisibilityDefault(VisibilityDefault visibilityDefault) {
            return string("Default visibility.");
        }

        @Override
        public List<Either<String, MarkedString>> case_Arguments(Arguments arguments) {
            return string("List of arguments");
        }

        @Override
        public List<Either<String, MarkedString>> case_ModuleInstanciations(ModuleInstanciations moduleInstanciations) {
            return string("List of module instantiations.");
        }

        @Override
        public List<Either<String, MarkedString>> case_WShortParameters(WShortParameters wShortParameters) {
            return string("Parameters of anonymous function.");
        }

        @Override
        public List<Either<String, MarkedString>> case_SwitchDefaultCaseStatements(SwitchDefaultCaseStatements switchDefaultCaseStatements) {
            return string("Default statements of switch-statement");
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprStatementsBlock(ExprStatementsBlock exprStatementsBlock) {
            return string("A block of statements.");
        }

        @Override
        public List<Either<String, MarkedString>> case_ModuleUses(ModuleUses moduleUses) {
            return string("A list of module uses");
        }

        @Override
        public List<Either<String, MarkedString>> case_GlobalVarDef(GlobalVarDef globalVarDef) {
            return description(globalVarDef);
        }

        @Override
        public List<Either<String, MarkedString>> case_JassToplevelDeclarations(JassToplevelDeclarations jassToplevelDeclarations) {
            return string("A list of declarations.");
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprIncomplete(ExprIncomplete exprIncomplete) {
            return string("This expression is incomplete.");
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprMemberArrayVarDotDot(ExprMemberArrayVarDotDot exprMemberArrayVarDotDot) {
            return description(exprMemberArrayVarDotDot);
        }

        @Override
        public List<Either<String, MarkedString>> case_ConstructorDefs(ConstructorDefs constructorDefs) {
            return string("A list of constructors");
        }

        private List<Either<String, MarkedString>> typeExpr(TypeExpr t) {
            WurstType wt = t.attrTyp();
            if (wt == null) {
                return string("Type " + t);
            }
            if (wt instanceof WurstTypeNamedScope) {
                WurstTypeNamedScope wtn = (WurstTypeNamedScope) wt;
                return description(wtn.getDef());
            }
            return string(type(wt));
        }

        @Override
        public List<Either<String, MarkedString>> case_TypeExprArray(TypeExprArray t) {
            return typeExpr(t);
        }


        @Override
        public List<Either<String, MarkedString>> case_TypeExprSimple(TypeExprSimple t) {
            return typeExpr(t);
        }

        @Override
        public List<Either<String, MarkedString>> case_Modifiers(Modifiers modifiers) {
            return string("A list of modifiers");
        }

        @Override
        public List<Either<String, MarkedString>> case_ModAbstract(ModAbstract modAbstract) {
            return string("Abstract members are declarations without implementations. They must be implemented in concrete subtypes.");
        }

        @Override
        public List<Either<String, MarkedString>> case_WBlock(WBlock wBlock) {
            return string("A block.");
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtSkip(StmtSkip stmtSkip) {
            return string("The skip statement does nothing and can be used to fill an empty block.");
        }

        @Override
        public List<Either<String, MarkedString>> case_FuncDef(FuncDef funcDef) {
            return description(funcDef);
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprList(ExprList exprList) {
            return string("A list of expressions.");
        }

        @Override
        public List<Either<String, MarkedString>> case_NativeType(NativeType nativeType) {
            return description(nativeType);
        }

        @Override
        public List<Either<String, MarkedString>> case_NoTypeParamConstraints(NoTypeParamConstraints noTypeParamConstraints) {
            return string("No type parameter constraints given.");
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtForRangeUp(StmtForRangeUp stmtForRangeUp) {
            return string("Execute the body several times, counting up");
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtLoop(StmtLoop stmtLoop) {
            return string("Primitive loop statement");
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprStringVal(ExprStringVal exprStringVal) {
            return string("A string constant.");
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprNull(ExprNull exprNull) {
            return string("The null-reference");
        }

        @Override
        public List<Either<String, MarkedString>> case_ClassDefs(ClassDefs classDefs) {
            return string("A list of class definitions.");
        }

        @Override
        public List<Either<String, MarkedString>> case_ModuleInstanciation(ModuleInstanciation moduleInstanciation) {
            return string("A module instantiation.");
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprMemberArrayVarDot(ExprMemberArrayVarDot exprMemberArrayVarDot) {
            return description(exprMemberArrayVarDot);
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprFuncRef(ExprFuncRef exprFuncRef) {
            return description(exprFuncRef);
        }

        @Override
        public List<Either<String, MarkedString>> case_TypeParamDefs(TypeParamDefs typeParamDefs) {
            return string("A list of type parameters.");
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtForFrom(StmtForFrom stmtForFrom) {
            return string("The for-from loop takes an iterate and takes elements from the iterator until it is empty.");
        }

        @Override
        public List<Either<String, MarkedString>> case_Indexes(Indexes indexes) {
            return string("A list of indexes");
        }

        @Override
        public List<Either<String, MarkedString>> case_VisibilityPublicread(VisibilityPublicread visibilityPublicread) {
            return string("This variable can be read from everywhere but only written to in this scope.");
        }

        @Override
        public List<Either<String, MarkedString>> case_EnumMember(EnumMember enumMember) {
            return description(enumMember);
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprInstanceOf(ExprInstanceOf exprInstanceOf) {
            return string("The instanceof expression checks if an object is an instance of a given type.");
        }

        @Override
        public List<Either<String, MarkedString>> case_WurstModel(WurstModel wurstModel) {
            return string("The wurst model.");
        }

        @Override
        public List<Either<String, MarkedString>> case_Identifier(Identifier identifier) {
            return identifier.getParent().match(this);
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtWhile(StmtWhile stmtWhile) {
            return string("A while loop");
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprMemberMethodDotDot(ExprMemberMethodDotDot exprMemberMethodDotDot) {
            return description(exprMemberMethodDotDot);
        }

        @Override
        public List<Either<String, MarkedString>> case_TypeParamDef(TypeParamDef typeParamDef) {
            return description(typeParamDef);
        }

        @Override
        public List<Either<String, MarkedString>> case_IdentifierWithTypeParamDefs(IdentifierWithTypeParamDefs identifierWithTypeParamDefs) {
            return identifierWithTypeParamDefs.getParent().match(this);
        }

        @Override
        public List<Either<String, MarkedString>> case_GlobalVarDefs(GlobalVarDefs globalVarDefs) {
            return string("A list of global variables.");
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprThis(ExprThis exprThis) {
            return string("'this' refers to the current object (of type " + exprThis.attrTyp() + ")");
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtReturn(StmtReturn stmtReturn) {
            return string("Returns from the current function.");
        }

        @Override
        public List<Either<String, MarkedString>> case_WPackages(WPackages wPackages) {
            return string("A list of packages.");
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprIfElse(ExprIfElse exprIfElse) {
            return string("A conditional expression (condition ? ifTrue : ifFalse).");
        }

        @Override
        public List<Either<String, MarkedString>> case_WurstDoc(WurstDoc wurstDoc) {
            return wurstDoc.getParent().match(this);
        }

        @Override
        public List<Either<String, MarkedString>> case_NoSuperConstructorCall(NoSuperConstructorCall noSuperConstructorCall) {
            return string("no super constructor called");
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtIf(StmtIf stmtIf) {
            return string("An if-statement.");
        }

        @Override
        public List<Either<String, MarkedString>> case_WPackage(WPackage wPackage) {
            return description(wPackage);
        }

        @Override
        public List<Either<String, MarkedString>> case_OnDestroyDef(OnDestroyDef onDestroyDef) {
            return string("This is called when an object of this type is destroyed.");
        }

        @Override
        public List<Either<String, MarkedString>> case_ModVararg(ModVararg modVararg) {
            return string("Declares the parameter to be a array of variable length");
        }

        @Override
        public List<Either<String, MarkedString>> case_TypeExprResolved(TypeExprResolved typeExprResolved) {
            return typeExpr(typeExprResolved);
        }

        @Override
        public List<Either<String, MarkedString>> case_VisibilityPublic(VisibilityPublic visibilityPublic) {
            return string("This element can be used everywhere");
        }

        @Override
        public List<Either<String, MarkedString>> case_TopLevelDeclarations(TopLevelDeclarations topLevelDeclarations) {
            return string("A list of declarations.");
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtSet(StmtSet stmtSet) {
            return string("An assignment.");
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprDestroy(ExprDestroy exprDestroy) {
            return string("Destroys the given object.");
        }

        @Override
        public List<Either<String, MarkedString>> case_WEntities(WEntities wEntities) {
            return string("A list of entities");
        }

        @Override
        public List<Either<String, MarkedString>> case_ArraySizes(ArraySizes arraySizes) {
            return string("A list of array-sizes");
        }

        @Override
        public List<Either<String, MarkedString>> case_EnumDef(EnumDef enumDef) {
            return description(enumDef);
        }

        @Override
        public List<Either<String, MarkedString>> case_SwitchCase(SwitchCase switchCase) {
            return string("A case in a switch-statement");
        }

        @Override
        public List<Either<String, MarkedString>> case_EnumMembers(EnumMembers enumMembers) {
            return string("A list of enum-members.");
        }

        @Override
        public List<Either<String, MarkedString>> case_TypeExprThis(TypeExprThis typeExprThis) {
            return typeExpr(typeExprThis);
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprUnary(ExprUnary exprUnary) {
            return string("A unary expression");
        }

        @Override
        public List<Either<String, MarkedString>> case_JassGlobalBlock(JassGlobalBlock jassGlobalBlock) {
            return string("A block of global variables.");
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtForIn(StmtForIn stmtForIn) {
            return string("The for-in loop executes the loop body for every element in the given collection using its iterator method.");
        }

        @Override
        public List<Either<String, MarkedString>> case_InitBlock(InitBlock initBlock) {
            return string("The init block is called at the start of the program.");
        }

        @Override
        public List<Either<String, MarkedString>> case_WParameter(WParameter wParameter) {
            return description(wParameter);
        }

        @Override
        public List<Either<String, MarkedString>> case_WShortParameter(WShortParameter wShortParameter) {
            return description(wShortParameter);
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprMemberVarDot(ExprMemberVarDot exprMemberVarDot) {
            return description(exprMemberVarDot);
        }

        @Override
        public List<Either<String, MarkedString>> case_WParameters(WParameters wParameters) {
            return string("A list of parameters");
        }
    }

}
