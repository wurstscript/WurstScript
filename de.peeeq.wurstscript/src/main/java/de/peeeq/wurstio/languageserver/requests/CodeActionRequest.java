package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.CompilationUnitInfo;
import de.peeeq.wurstscript.attributes.names.DefLink;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.TypeLink;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeClassOrInterface;
import de.peeeq.wurstscript.types.WurstTypeUnknown;
import de.peeeq.wurstscript.types.WurstTypeVoid;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.peeeq.wurstio.languageserver.WurstCommands.WURST_PERFORM_CODE_ACTION;

/**
 *
 */
public class CodeActionRequest extends UserRequest<List<Either<Command, CodeAction>>> {
    private final CodeActionParams params;
    private final WFile filename;
    private final String buffer;
    private final int line;
    private final int column;

    public CodeActionRequest(CodeActionParams params, BufferManager bufferManager) {
        this.params = params;

        TextDocumentIdentifier textDocument = params.getTextDocument();
        this.filename = WFile.create(textDocument.getUri());
        this.buffer = bufferManager.getBuffer(textDocument);
        this.line = params.getRange().getStart().getLine() + 1;
        this.column = params.getRange().getStart().getCharacter() + 1;
    }

    @Override
    public List<Either<Command, CodeAction>> execute(ModelManager modelManager) {
        if (params.getContext().getDiagnostics().isEmpty()) {
            // if there are no compilation errors in this line,
            // we don't have to compute possible code actions
            return Collections.emptyList();
        }
        CompilationUnit cu = modelManager.replaceCompilationUnitContent(filename, buffer, false);
        if (cu == null) {
            return Collections.emptyList();
        }
        // get element under cursor
        Optional<Element> e = Utils.getAstElementAtPos(cu, line, column, false);


        WLogger.info("Code action on element " + Utils.printElementWithSource(e));
        if (!e.isPresent()) {
            // TODO non simple TypeRef

            return Collections.emptyList();
        }

        if (e.get() instanceof ExprNewObject) {
            ExprNewObject enew = (ExprNewObject) e.get();
            ConstructorDef constructorDef = enew.attrConstructorDef();
            if (constructorDef == null) {
                return handleMissingClass(modelManager, enew.getTypeName());
            }
        } else if (e.get() instanceof FuncRef) {
            FuncRef fr = (FuncRef) e.get();
            FuncLink fd = fr.attrFuncLink();
            if (fd == null) {
                return handleMissingFunction(modelManager, fr);
            }

        } else if (e.get() instanceof NameRef) {
            NameRef nr = (NameRef) e.get();
            NameLink nd = nr.attrNameLink();
            if (nd == null) {
                return handleMissingName(modelManager, nr);
            }

        } else if (e.get() instanceof TypeExprSimple) {
            TypeExprSimple nr = (TypeExprSimple) e.get();
            TypeDef nd = nr.attrTypeDef();
            if (nd == null) {
                return handleMissingType(modelManager, nr.getTypeName());
            }

        }
        // TODO non simple TypeRef

        return Collections.emptyList();
    }

    private List<Either<Command, CodeAction>> handleMissingName(ModelManager modelManager, NameRef nr) {
        String funcName = nr.getVarName();
        WurstModel model = modelManager.getModel();
        List<String> possibleImports = new ArrayList<>();
        WurstType receiverType = null;
        if (nr instanceof ExprMember) {
            ExprMember m = (ExprMember) nr;
            receiverType = m.getLeft().attrTyp();
        }
        for (CompilationUnit cu : model) {
            withNextPackage:
            for (WPackage wPackage : cu.getPackages()) {
                for (DefLink nameLink : wPackage.attrExportedNameLinks().get(funcName)) {
                    if (definedInPackage(wPackage, nameLink) && nameLink.receiverCompatibleWith(receiverType, nr)) {
                        possibleImports.add(wPackage.getName());
                        continue withNextPackage;
                    }
                }
                for (TypeLink nameLink : wPackage.attrExportedTypeNameLinks().get(funcName)) {
                    if (definedInPackage(wPackage, nameLink) && nameLink.receiverCompatibleWith(receiverType, nr)) {
                        possibleImports.add(wPackage.getName());
                        continue withNextPackage;
                    }
                }
            }
        }

        return makeImportCommands(possibleImports);

    }

    private boolean definedInPackage(WPackage wPackage, NameLink nameLink) {
        NameDef def = nameLink.getDef();
        if (def != null) {
            return def.attrNearestPackage() == wPackage;
        }
        return false;
    }

    private List<Either<Command, CodeAction>> handleMissingFunction(ModelManager modelManager, FuncRef fr) {
        String funcName = fr.getFuncName();
        WurstType receiverType = null;
        if (fr instanceof ExprMember) {
            ExprMember m = (ExprMember) fr;
            receiverType = m.getLeft().attrTyp();
        }
        WurstModel model = modelManager.getModel();
        List<String> possibleImports = new ArrayList<>();
        for (CompilationUnit cu : model) {
            withNextPackage:
            for (WPackage wPackage : cu.getPackages()) {
                for (NameLink nameLink : wPackage.attrExportedNameLinks().get(funcName)) {
                    if (nameLink.getDef() instanceof FunctionDefinition) {
                        if (definedInPackage(wPackage, nameLink) && nameLink.receiverCompatibleWith(receiverType, fr)) {
                            possibleImports.add(wPackage.getName());
                            continue withNextPackage;
                        }
                    }
                }
            }
        }

        return Utils.concatLists(makeImportCommands(possibleImports), makeCreateFunctionQuickfix(fr));
    }

    private List<Either<Command, CodeAction>> makeCreateFunctionQuickfix(FuncRef fr) {
        class M implements FuncRef.MatcherVoid {
            private List<String> parameterTypes = Collections.emptyList();
            private List<String> parameterNames = Collections.emptyList();
            private int line;
            private int indent;
            private WFile targetFile = filename;
            private String receiverType = "";
            private WurstType returnType = WurstTypeVoid.instance();
            private boolean isAnnotation = false;


            @Override
            public void case_ExprFuncRef(ExprFuncRef e) {
                getInsertPos(fr);
            }

            @Override
            public void case_Annotation(Annotation annotation) {
                isAnnotation = true;
                getInsertPos(fr);
            }

            private void getInsertPos(Element fr) {
                Element elem = fr;
                while (elem != null) {
                    if (elem instanceof FunctionLike) {
                        WPos source = elem.attrSource();
                        line = source.getEndLine();
                        indent = source.getStartColumn() - 1;
                        break;
                    } else if (elem instanceof WPackage) {
                        WPos source = elem.attrSource();
                        line = source.getEndLine();
                        indent = 0;
                        break;
                    } else if (elem instanceof FuncDefs) {
                        FuncDefs funcDefs = (FuncDefs) elem;
                        if (!funcDefs.isEmpty()) {
                            WPos source = Utils.getLast(funcDefs).attrSource();
                            line = source.getEndLine();
                            indent = source.getStartColumn() - 1;
                            break;
                        }
                    } else if (elem instanceof NamedScope) {
                        WPos source = elem.attrSource();
                        line = source.getEndLine();
                        indent = source.getStartColumn() - 1 + 4;
                        break;
                    }
                    elem = elem.getParent();
                }
            }

            @Override
            public void case_ExprMemberMethodDotDot(ExprMemberMethodDotDot e) {
                case_Member(e);
                returnType = e.attrExpectedTyp();
            }

            @Override
            public void case_ExprMemberMethodDot(ExprMemberMethodDot e) {
                case_Member(e);
            }

            private void case_Member(ExprMemberMethod e) {
                WurstType leftType = e.getLeft().attrTyp();
                if (leftType instanceof WurstTypeClassOrInterface) {
                    getInsertPos(((WurstTypeClassOrInterface) leftType).getDef().getMethods());
                } else {
                    getInsertPos(e);
                    receiverType = leftType + ".";
                }
                addParametertypes(e.getArgs());
                returnType = e.attrExpectedTyp();
            }

            private void addParametertypes(Arguments args) {
                parameterTypes = args.stream()
                        .map(Expr::attrTyp)
                        .map(WurstType::toPrettyString)
                        .collect(Collectors.toList());
                parameterNames = args.stream()
                        .map(this::deriveParameterName)
                        .collect(Collectors.toCollection(ArrayList::new));
                makeUnique(parameterNames);
            }

            private void makeUnique(List<String> names) {
                for (int i = 0; i < names.size(); i++) {
                    for (int j = i + 1; j < names.size(); j++) {
                        if (names.get(i).equals(names.get(j))) {
                            names.set(i, names.get(i) + i);
                        }
                    }
                }
            }

            private String deriveParameterName(Expr expr) {
                if (expr instanceof NameRef) {
                    return ((NameRef) expr).getVarName();
                } else if (expr instanceof FuncRef) {
                    return ((FuncRef) expr).getFuncName();
                }
                String res = Utils.prettyPrint(expr).replaceAll("[^a-zA-Z]+", "");
                if (res.length() > 10) {
                    res = res.substring(0, 10);
                } else if (res.isEmpty()) {
                    res = "arg";
                }
                return res;
            }

            @Override
            public void case_ExprFunctionCall(ExprFunctionCall e) {
                getInsertPos(fr);
                addParametertypes(e.getArgs());
            }


            public void indent(StringBuilder sb) {
                CompilationUnitInfo.IndentationMode indentationMode = fr.attrCompilationUnit().getCuInfo().getIndentationMode();
                indentationMode.appendIndent(sb, indentationMode.countIndents(indent) + 1);
            }
        }
        M m = new M();
        fr.match(m);

        String title = "Create function " + fr.getFuncName();
        StringBuilder code = new StringBuilder("\n");
        m.indent(code);
        if (m.isAnnotation) {
            code.append("@annotation ");
        }
        code.append("function ");
        code.append(m.receiverType);
        code.append(fr.getFuncName());
        code.append("(");
        for (int i = 0; i < m.parameterTypes.size(); i++) {
            if (i > 0) {
                code.append(", ");
            }
            code.append(m.parameterTypes.get(i));
            code.append(" ");
            code.append(m.parameterNames.get(i));
        }
        code.append(")");

        if (!(m.returnType instanceof WurstTypeVoid || m.returnType instanceof WurstTypeUnknown)) {
            code.append(" returns ");
            code.append(m.returnType);
        }
        code.append("\n");


        List<Object> arguments = Collections.singletonList(
                PerformCodeActionRequest.insertCodeAction(
                        m.targetFile.getUriString(),
                        m.line,
                        code.toString())
        );
        return Collections.singletonList(Either.forLeft(new Command(title, WURST_PERFORM_CODE_ACTION, arguments)));
    }


    private List<Either<Command, CodeAction>> handleMissingType(ModelManager modelManager, String typeName) {
        WurstModel model = modelManager.getModel();
        List<String> possibleImports = new ArrayList<>();
        for (CompilationUnit cu : model) {
            for (WPackage wPackage : cu.getPackages()) {
                if (!wPackage.attrExportedTypeNameLinks().get(typeName).isEmpty()) {
                    possibleImports.add(wPackage.getName());
                }
            }
        }

        return makeImportCommands(possibleImports);
    }

    private List<Either<Command, CodeAction>> handleMissingClass(ModelManager modelManager, String typeName) {
        // TODO this is not optimal yet: We are only looking in the current model,
        // which only includes files which are in the current project or already imported
        // in the current project. So this will for example miss packages from the standard library
        // that are not yet imported into the project.

        WurstModel model = modelManager.getModel();
        List<String> possibleImports = new ArrayList<>();
        for (CompilationUnit cu : model) {
            withNextPackage:
            for (WPackage wPackage : cu.getPackages()) {
                for (NameLink nameLink : wPackage.attrExportedTypeNameLinks().get(typeName)) {
                    if (nameLink.getDef() instanceof ClassDef) {
                        possibleImports.add(wPackage.getName());
                        continue withNextPackage;
                    }
                }
            }
        }

        return makeImportCommands(possibleImports);
    }

    private List<Either<Command, CodeAction>> makeImportCommands(List<String> possibleImports) {
        return possibleImports.stream()
                .map(this::makeImportCommand)
                .collect(Collectors.toList());
    }


    private Either<Command, CodeAction> makeImportCommand(String imp) {
        String title = "Import package " + imp;
        List<Object> arguments = Collections.singletonList(
                PerformCodeActionRequest.importPackageAction(
                        filename.getUriString(),
                        imp)
        );
        return Either.forLeft(new Command(title, WURST_PERFORM_CODE_ACTION, arguments));
    }
}
