package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.DefLink;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.TypeLink;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.TextDocumentIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static de.peeeq.wurstio.languageserver.WurstCommands.WURST_PERFORM_CODE_ACTION;

/**
 *
 */
public class CodeActionRequest extends UserRequest<List<? extends Command>> {
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
    public List<Command> execute(ModelManager modelManager) {
        if (params.getContext().getDiagnostics().isEmpty()) {
            // if there are no compilation errors in this line,
            // we don't have to compute possible code actions
            return Collections.emptyList();
        }
        CompilationUnit cu = modelManager.replaceCompilationUnitContent(filename, buffer, false);
        // get element under cursor
        Element e = Utils.getAstElementAtPos(cu, line, column, false);


        WLogger.info("Code action on element " + Utils.printElementWithSource(e));
        if (e instanceof ExprNewObject) {
            ExprNewObject enew = (ExprNewObject) e;
            ConstructorDef constructorDef = enew.attrConstructorDef();
            if (constructorDef == null) {
                return handleMissingClass(modelManager, enew.getTypeName());
            }
        } else if (e instanceof FuncRef) {
            FuncRef fr = (FuncRef) e;
            FuncLink fd = fr.attrFuncLink();
            if (fd == null) {
                return handleMissingFunction(modelManager, fr);
            }

        } else if (e instanceof NameRef) {
            NameRef nr = (NameRef) e;
            NameLink nd = nr.attrNameLink();
            if (nd == null) {
                return handleMissingName(modelManager, nr);
            }

        }
        // TODO handle NameRef, FuncRef, TypeRef

        return Collections.emptyList();
    }

    private List<Command> handleMissingName(ModelManager modelManager, NameRef nr) {
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
                for (DefLink nameLink :  wPackage.attrExportedNameLinks().get(funcName)) {
                    if (nameLink.receiverCompatibleWith(receiverType, nr)) {
                        possibleImports.add(wPackage.getName());
                        continue withNextPackage;
                    }
                }
                for (TypeLink nameLink :  wPackage.attrExportedTypeNameLinks().get(funcName)) {
                    if (nameLink.receiverCompatibleWith(receiverType, nr)) {
                        possibleImports.add(wPackage.getName());
                        continue withNextPackage;
                    }
                }
            }
        }

        return makeImportCommands(possibleImports);

    }

    private List<Command> handleMissingFunction(ModelManager modelManager, FuncRef fr) {
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
                        if (nameLink.receiverCompatibleWith(receiverType, fr)) {
                            possibleImports.add(wPackage.getName());
                            continue withNextPackage;
                        }
                    }
                }
            }
        }

        return makeImportCommands(possibleImports);
    }

    private List<Command> handleMissingClass(ModelManager modelManager, String typeName) {
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

    private List<Command> makeImportCommands(List<String> possibleImports) {
        return possibleImports.stream()
                .map(this::makeImportCommand)
                .collect(Collectors.toList());
    }


    private Command makeImportCommand(String imp) {
        String title = "Import package " + imp;
        List<Object> arguments = Collections.singletonList(
                PerformCodeActionRequest.importPackageAction(
                        filename.getUriString(),
                        imp)
        );
        return new Command(title, WURST_PERFORM_CODE_ACTION, arguments);
    }
}
