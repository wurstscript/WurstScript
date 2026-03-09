package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.JassDocService;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.AttrWurstDoc;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.parser.TriviaIndex;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeNamedScope;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        int offset = offsetAt(line, column);
        Optional<TriviaIndex.CommentTrivia> commentTrivia = cu.getCuInfo().getTriviaIndex().findCommentAtOffset(offset);
        if (commentTrivia.isPresent()) {
            return new Hover(Collections.emptyList());
        }
        Element e = Utils.getAstElementAtPos(cu, line, column, false).get();
        WLogger.debug("hovering over " + Utils.printElement(e));
        List<Either<String, MarkedString>> desription = e.match(new Description());
        desription = addArgumentHint(e, desription);

        return new Hover(desription);
    }

    private int offsetAt(int line, int column) {
        int currentLine = 1;
        int currentColumn = 1;
        for (int i = 0; i < buffer.length(); i++) {
            if (currentLine == line && currentColumn == column) {
                return i;
            }
            char c = buffer.charAt(i);
            if (c == '\n') {
                currentLine++;
                currentColumn = 1;
            } else {
                currentColumn++;
            }
        }
        return Math.max(0, buffer.length() - 1);
    }

    private List<Either<String, MarkedString>> addArgumentHint(Element e, List<Either<String, MarkedString>> desription) {
        try {
            if (e.getParent() instanceof Arguments) {
                Arguments args = (Arguments) e.getParent();
                int index = args.indexOf(e);
                if (args.getParent() instanceof FunctionCall) {
                    FunctionCall fc = (FunctionCall) args.getParent();
                    FuncLink f = fc.attrFuncLink();
                    if (f != null) {
                        WurstType parameterType = f.getParameterType(index);
                        String parameterName = f.getParameterName(index);
                        desription = Utils.append(desription, Either.forRight(new MarkedString("wurst", "parameter " + parameterType + " " + parameterName)));
                    }
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
            boolean docsFromJassdoc = false;
            if (comment == null || comment.isEmpty()) {
                comment = JassDocService.getInstance().documentationForFunction(f);
                docsFromJassdoc = comment != null && !comment.isEmpty();
            }
            if ((comment == null || comment.isEmpty()) && f instanceof FunctionImplementation) {
                comment = inferWrappedNativeDoc(f);
                docsFromJassdoc = comment != null && !comment.isEmpty();
            }

            // TODO parse comment
            if (comment != null && !comment.isEmpty()) {
                if (docsFromJassdoc) {
                    result.add(Either.forLeft("_JassDoc_"));
                }
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
            result.add(Either.forLeft(definedInText(f)));
            return result;
        }

        private @Nullable String inferWrappedNativeDoc(FunctionDefinition f) {
            return inferWrappedNativeDoc(f, new LinkedHashSet<>());
        }

        private @Nullable String inferWrappedNativeDoc(FunctionDefinition f, Set<FunctionDefinition> visited) {
            if (!visited.add(f)) {
                return null;
            }
            String directDoc = JassDocService.getInstance().documentationForFunction(f);
            if (directDoc != null && !directDoc.isEmpty()) {
                return directDoc;
            }
            if (!(f instanceof FunctionImplementation)) {
                return null;
            }
            Set<FunctionDefinition> callees = new LinkedHashSet<>();
            ((FunctionImplementation) f).getBody().accept(new Element.DefaultVisitor() {
                @Override
                public void visit(ExprFunctionCall exprFunctionCall) {
                    addCallee(exprFunctionCall.attrFuncDef());
                    super.visit(exprFunctionCall);
                }

                @Override
                public void visit(ExprMemberMethodDot exprMemberMethodDot) {
                    addCallee(exprMemberMethodDot.attrFuncDef());
                    super.visit(exprMemberMethodDot);
                }

                @Override
                public void visit(ExprMemberMethodDotDot exprMemberMethodDotDot) {
                    addCallee(exprMemberMethodDotDot.attrFuncDef());
                    super.visit(exprMemberMethodDotDot);
                }

                private void addCallee(FunctionDefinition def) {
                    if (def != null && def != f) {
                        callees.add(def);
                    }
                }
            });
            if (callees.size() != 1) {
                return null;
            }
            return inferWrappedNativeDoc(callees.iterator().next(), visited);
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
            boolean docsFromJassdoc = false;
            if (comment == null || comment.isEmpty()) {
                comment = JassDocService.getInstance().documentationForVariable(n);
                docsFromJassdoc = comment != null && !comment.isEmpty();
            }
            if (comment != null && !comment.isEmpty()) {
                if (docsFromJassdoc) {
                    result.add(Either.forLeft("_JassDoc_"));
                }
                result.add(Either.forLeft(comment));
            }

            String initializer = "";
            if (n instanceof GlobalOrLocalVarDef) {
                GlobalOrLocalVarDef v = (GlobalOrLocalVarDef) n;
                VarInitialization initialExpr = v.getInitialExpr();
                if (!(initialExpr instanceof NoExpr)) {
                    initializer = " = " + Utils.prettyPrint(initialExpr);
                }
            }

            if (n instanceof TypeParamDef) {
                result.add(Either.forRight(new MarkedString("wurst", "type parameter " + n.getName())));
            } else {
                result.add(Either.forRight(new MarkedString("wurst", type(n.attrTyp()) + " " + n.getName() + initializer)));
            }
            result.add(Either.forLeft(definedInText(n)));
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
            result.add(Either.forLeft(definedInText(f)));
            return result;
        }

        private static String definedInText(Element n) {
            String file = n.attrSource().getFile();
            if (file == null || file.isEmpty()) {
                return "defined in " + nearestScopeName(n);
            }
            File f = new File(file);
            String label = f.getName();
            String uri = f.toURI().toString();
            return "defined in [" + label + "](" + uri + ")";
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
            List<Either<String, MarkedString>> result = new ArrayList<>();
            WPackage imported = imp.attrImportedPackage();
            if (imported != null && imported.attrComment() != null && !imported.attrComment().isEmpty()) {
                result.add(Either.forLeft(imported.attrComment()));
            }
            result.add(Either.forRight(new MarkedString("wurst", "import " + imp.getPackagename())));
            return result;
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
            return string("exitwhen: exits the current loop when the condition is true.");
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtContinue(StmtContinue stmtContinue) {
            return string("continue: skips the rest of the current loop iteration.");
        }

        @Override
        public List<Either<String, MarkedString>> case_ConstructorDef(ConstructorDef constr) {
            return description(constr);
        }

        @Override
        public List<Either<String, MarkedString>> case_WImports(WImports wImports) {
            return Collections.emptyList();
        }

        @Override
        public List<Either<String, MarkedString>> case_WStatements(WStatements wStatements) {
            return Collections.emptyList();
        }

        @Override
        public List<Either<String, MarkedString>> case_CompilationUnit(CompilationUnit compilationUnit) {
            return string("File " + compilationUnit.getCuInfo().getFile());
        }

        @Override
        public List<Either<String, MarkedString>> case_SwitchStmt(SwitchStmt switchStmt) {
            return string("A switch statement executes branches based on an expression value.");
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
            return string("override: This function overrides another function from a module or superclass");
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
            return description(v);
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
            return Collections.emptyList();
        }

        @Override
        public List<Either<String, MarkedString>> case_FuncDefs(FuncDefs funcDefs) {
            return Collections.emptyList();
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
            return Collections.emptyList();
        }

        @Override
        public List<Either<String, MarkedString>> case_ModuleInstanciations(ModuleInstanciations moduleInstanciations) {
            return Collections.emptyList();
        }

        @Override
        public List<Either<String, MarkedString>> case_WShortParameters(WShortParameters wShortParameters) {
            return Collections.emptyList();
        }

        @Override
        public List<Either<String, MarkedString>> case_SwitchDefaultCaseStatements(SwitchDefaultCaseStatements switchDefaultCaseStatements) {
            return Collections.emptyList();
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprStatementsBlock(ExprStatementsBlock exprStatementsBlock) {
            return string("A block of statements.");
        }

        @Override
        public List<Either<String, MarkedString>> case_ModuleUses(ModuleUses moduleUses) {
            return Collections.emptyList();
        }

        @Override
        public List<Either<String, MarkedString>> case_GlobalVarDef(GlobalVarDef globalVarDef) {
            return description(globalVarDef);
        }

        @Override
        public List<Either<String, MarkedString>> case_JassToplevelDeclarations(JassToplevelDeclarations jassToplevelDeclarations) {
            return Collections.emptyList();
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
            return Collections.emptyList();
        }

        private List<Either<String, MarkedString>> typeExpr(TypeExpr t) {
            NameDef nameDef = t.tryGetNameDef();
            if (nameDef != null) {
                return description(nameDef);
            }
            WurstType wt = t.attrTyp();
            if (wt == null) {
                return Collections.singletonList(Either.forRight(new MarkedString("wurst", "type " + t)));
            }
            if (wt instanceof WurstTypeNamedScope) {
                WurstTypeNamedScope wtn = (WurstTypeNamedScope) wt;
                return description(wtn.getDef());
            }
            return Collections.singletonList(Either.forRight(new MarkedString("wurst", type(wt))));
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
            return string("Modifiers for this declaration.");
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
            return Collections.emptyList();
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
        public List<Either<String, MarkedString>> case_ExprArrayLength(ExprArrayLength exprArrayLength) {
            return List.of();
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
            return Collections.emptyList();
        }

        @Override
        public List<Either<String, MarkedString>> case_StmtForFrom(StmtForFrom stmtForFrom) {
            return string("The for-from loop repeatedly takes elements from an iterator until it is empty.");
        }

        @Override
        public List<Either<String, MarkedString>> case_Indexes(Indexes indexes) {
            return Collections.emptyList();
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
            return Collections.emptyList();
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
            return Collections.emptyList();
        }

        @Override
        public List<Either<String, MarkedString>> case_ExprIfElse(ExprIfElse exprIfElse) {
            return string("A conditional expression (condition ? ifTrue : ifFalse).");
        }

        @Override
        public List<Either<String, MarkedString>> case_WurstDoc(WurstDoc wurstDoc) {
            return string(AttrWurstDoc.normalizeHotdocComment(wurstDoc.getRawComment()));
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
            return string("Declares this parameter as a variable-length argument list.");
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
            return Collections.emptyList();
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
            return Collections.emptyList();
        }

        @Override
        public List<Either<String, MarkedString>> case_ArraySizes(ArraySizes arraySizes) {
            return Collections.emptyList();
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
            return Collections.emptyList();
        }

        @Override
        public List<Either<String, MarkedString>> case_TypeExprThis(TypeExprThis typeExprThis) {
            List<Either<String, MarkedString>> result = new ArrayList<>();
            result.add(Either.forLeft("'thistype' refers to the dynamic type of 'this' in the current context."));
            result.add(Either.forRight(new MarkedString("wurst", "resolved as " + type(typeExprThis.attrTyp()))));
            return result;
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
            return Collections.emptyList();
        }
    }

}
