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
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeClassOrInterface;
import de.peeeq.wurstscript.types.WurstTypeUnknown;
import de.peeeq.wurstscript.types.WurstTypeVoid;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 */
public class CodeActionRequest extends UserRequest<List<Either<Command, CodeAction>>> {
    private static final String CONSTANT_LOCAL_LET_WARNING = "Constant local variables should be defined using 'let'.";
    private static final String UNUSED_IMPORT_WARNING_SUFFIX = " is never used";
    private static final String REDUNDANT_IMPORT_MIDDLE = " can be removed, because it is already included in ";
    private static final String MIXED_INDENT_WARNING = "Mixing tabs and spaces for indentation.";
    private static final Pattern NEVER_READ_NAME = Pattern.compile("^The .*?\\b([A-Za-z_][A-Za-z0-9_]*) is never read\\.");
    private final WFile filename;
    private final String buffer;
    private final int line;
    private final int column;
    private final List<Diagnostic> diagnostics;

    public CodeActionRequest(CodeActionParams params, BufferManager bufferManager) {
        TextDocumentIdentifier textDocument = params.getTextDocument();
        this.filename = WFile.create(textDocument.getUri());
        this.buffer = bufferManager.getBuffer(textDocument);
        this.line = params.getRange().getStart().getLine() + 1;
        this.column = params.getRange().getStart().getCharacter() + 1;
        this.diagnostics = params.getContext() == null
                ? Collections.emptyList()
                : params.getContext().getDiagnostics();
    }

    @Override
    public List<Either<Command, CodeAction>> execute(ModelManager modelManager) {
        if (diagnostics.isEmpty()) {
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
        List<Either<Command, CodeAction>> actions = new ArrayList<>();
        actions.addAll(makeWarningQuickFixes(e));
        if (!e.isPresent()) {
            // TODO non simple TypeRef
            return actions;
        }

        if (e.get() instanceof ExprNewObject) {
            ExprNewObject enew = (ExprNewObject) e.get();
            ConstructorDef constructorDef = enew.attrConstructorDef();
            if (constructorDef == null) {
                actions.addAll(handleMissingClass(modelManager, enew.getTypeName()));
                return actions;
            }
        } else if (e.get() instanceof FuncRef) {
            FuncRef fr = (FuncRef) e.get();
            FuncLink fd = fr.attrFuncLink();
            if (fd == null) {
                actions.addAll(handleMissingFunction(modelManager, fr));
                return actions;
            }

        } else if (e.get() instanceof NameRef) {
            NameRef nr = (NameRef) e.get();
            NameLink nd = nr.attrNameLink();
            if (nd == null) {
                actions.addAll(handleMissingName(modelManager, nr));
                return actions;
            }

        } else if (e.get() instanceof TypeExprSimple) {
            TypeExprSimple nr = (TypeExprSimple) e.get();
            TypeDef nd = nr.attrTypeDef();
            if (nd == null) {
                actions.addAll(handleMissingType(modelManager, nr.getTypeName()));
                return actions;
            }

        } else if (e.get() instanceof ModuleUse) {
            ModuleUse mu = (ModuleUse) e.get();
            ModuleDef def = mu.attrModuleDef();
            if (def == null) {
                actions.addAll(handleMissingModule(modelManager, mu.getModuleNameId().getName()));
                return actions;
            }
        }

        return actions;
    }

    private List<Either<Command, CodeAction>> makeWarningQuickFixes(Optional<Element> e) {
        List<Either<Command, CodeAction>> result = new ArrayList<>();

        if (e.isPresent() && hasDiagnosticMessage(msg -> msg.contains(CONSTANT_LOCAL_LET_WARNING))) {
            findNearest(e.get(), LocalVarDef.class).ifPresent(localVar -> {
                int line0 = localVar.attrSource().getLine() - 1;
                int startChar = Math.max(0, localVar.attrSource().getStartColumn() - 1);
                int varPos = findKeywordInLine(line0, startChar, "var");
                if (varPos >= 0) {
                    TextEdit edit = new TextEdit(
                        new Range(new Position(line0, varPos), new Position(line0, varPos + 3)),
                        "let"
                    );
                    result.add(Either.forRight(makeQuickFix(
                        "Use 'let' for constant local",
                        workspaceEdit(filename.getUriString(), edit)
                    )));
                }
            });
        }

        if (hasDiagnosticMessage(msg -> msg.startsWith("The import ") && msg.endsWith(UNUSED_IMPORT_WARNING_SUFFIX))) {
            int line0 = e.flatMap(elem -> findNearest(elem, WImport.class))
                .map(imp -> imp.attrSource().getLine() - 1)
                .orElseGet(() -> diagnostics.get(0).getRange().getStart().getLine());
            String importName = e.flatMap(elem -> findNearest(elem, WImport.class))
                .map(WImport::getPackagename)
                .orElse("import");
            TextEdit edit = new TextEdit(lineDeletionRange(line0), "");
            result.add(Either.forRight(makeQuickFix(
                "Remove unused import " + importName,
                workspaceEdit(filename.getUriString(), edit)
            )));
        }

        if (hasDiagnosticMessage(msg -> msg.startsWith("The import ") && msg.contains(REDUNDANT_IMPORT_MIDDLE))) {
            int line0 = e.flatMap(elem -> findNearest(elem, WImport.class))
                .map(imp -> imp.attrSource().getLine() - 1)
                .orElseGet(() -> diagnostics.get(0).getRange().getStart().getLine());
            String importName = firstDiagnosticMessage(msg -> msg.startsWith("The import ") && msg.contains(REDUNDANT_IMPORT_MIDDLE))
                .map(msg -> {
                    int start = "The import ".length();
                    int end = msg.indexOf(REDUNDANT_IMPORT_MIDDLE);
                    return end > start ? msg.substring(start, end) : "import";
                })
                .orElse("import");
            TextEdit edit = new TextEdit(lineDeletionRange(line0), "");
            result.add(Either.forRight(makeQuickFix(
                "Remove redundant import " + importName,
                workspaceEdit(filename.getUriString(), edit)
            )));
        }

        firstDiagnosticMessage(msg -> msg.startsWith("The ") && msg.contains(" is never read. If intentional, prefix with \"_\""))
            .ifPresent(msg -> {
                Matcher m = NEVER_READ_NAME.matcher(msg);
                if (!m.find()) {
                    return;
                }
                String name = m.group(1);
                Diagnostic d = firstDiagnostic(msg2 -> msg.equals(msg2));
                if (d == null) {
                    return;
                }
                int line0 = d.getRange().getStart().getLine();
                int col0 = d.getRange().getStart().getCharacter();
                String lineText = getLine(line0);
                if (lineText == null) {
                    return;
                }
                if (col0 < lineText.length() && lineText.charAt(col0) == '_') {
                    return;
                }
                TextEdit edit = new TextEdit(new Range(new Position(line0, col0), new Position(line0, col0)), "_");
                result.add(Either.forRight(makeQuickFix(
                    "Prefix '" + name + "' with '_'",
                    workspaceEdit(filename.getUriString(), edit)
                )));
            });

        firstDiagnosticMessage(msg -> msg.contains(MIXED_INDENT_WARNING)).ifPresent(msg -> {
            Diagnostic d = firstDiagnostic(msg2 -> msg2.contains(MIXED_INDENT_WARNING));
            if (d == null) {
                return;
            }
            int line0 = d.getRange().getStart().getLine();
            String lineText = getLine(line0);
            if (lineText == null) {
                return;
            }
            int end = 0;
            while (end < lineText.length()) {
                char c = lineText.charAt(end);
                if (c == ' ' || c == '\t') {
                    end++;
                } else {
                    break;
                }
            }
            if (end == 0) {
                return;
            }
            String indent = lineText.substring(0, end);
            String normalized = indent.replace("\t", "    ");
            if (indent.equals(normalized)) {
                return;
            }
            TextEdit edit = new TextEdit(new Range(new Position(line0, 0), new Position(line0, end)), normalized);
            result.add(Either.forRight(makeQuickFix(
                "Normalize indentation on this line",
                workspaceEdit(filename.getUriString(), edit)
            )));
        });

        return result;
    }

    private Optional<String> firstDiagnosticMessage(Predicate<String> matcher) {
        return diagnostics.stream()
            .map(Diagnostic::getMessage)
            .filter(msg -> msg != null && !msg.isEmpty())
            .filter(matcher)
            .findFirst();
    }

    private Diagnostic firstDiagnostic(Predicate<String> matcher) {
        return diagnostics.stream()
            .filter(d -> d.getMessage() != null && matcher.test(d.getMessage()))
            .findFirst()
            .orElse(null);
    }

    private String getLine(int line0) {
        String[] lines = buffer.split("\\r?\\n", -1);
        if (line0 < 0 || line0 >= lines.length) {
            return null;
        }
        return lines[line0];
    }

    private boolean hasDiagnosticMessage(Predicate<String> matcher) {
        return diagnostics.stream()
            .map(Diagnostic::getMessage)
            .filter(msg -> msg != null && !msg.isEmpty())
            .anyMatch(matcher);
    }

    private int findKeywordInLine(int line0, int startChar, String keyword) {
        String[] lines = buffer.split("\\r?\\n", -1);
        if (line0 < 0 || line0 >= lines.length) {
            return -1;
        }
        String l = lines[line0];
        if (startChar >= l.length()) {
            return -1;
        }
        int idx = l.indexOf(keyword, startChar);
        if (idx < 0) {
            return -1;
        }
        if (idx > 0 && Character.isJavaIdentifierPart(l.charAt(idx - 1))) {
            return -1;
        }
        int end = idx + keyword.length();
        if (end < l.length() && Character.isJavaIdentifierPart(l.charAt(end))) {
            return -1;
        }
        return idx;
    }

    private Range lineDeletionRange(int line0) {
        String[] lines = buffer.split("\\r?\\n", -1);
        if (line0 < 0 || line0 >= lines.length) {
            Position p = new Position(Math.max(line0, 0), 0);
            return new Range(p, p);
        }
        Position start = new Position(line0, 0);
        if (line0 + 1 < lines.length) {
            return new Range(start, new Position(line0 + 1, 0));
        }
        return new Range(start, new Position(line0, lines[line0].length()));
    }

    private <T extends Element> Optional<T> findNearest(Element e, Class<T> clazz) {
        Element elem = e;
        while (elem != null) {
            if (clazz.isInstance(elem)) {
                return Optional.of(clazz.cast(elem));
            }
            elem = elem.getParent();
        }
        return Optional.empty();
    }

    private List<Either<Command, CodeAction>> handleMissingName(ModelManager modelManager, NameRef nr) {
        String funcName = nr.getVarName();
        WurstModel model = modelManager.getModel();
        Set<String> possibleImports = new LinkedHashSet<>();
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
        addDependencyPackageFallback(modelManager, possibleImports, funcName);

        return makeImportCommands(possibleImports, modelManager);

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
        Set<String> possibleImports = new LinkedHashSet<>();
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
        addDependencyPackageFallback(modelManager, possibleImports, funcName);

        return Utils.concatLists(makeImportCommands(possibleImports, modelManager), makeCreateFunctionQuickfix(fr));
    }

    private List<Either<Command, CodeAction>> makeCreateFunctionQuickfix(FuncRef fr) {
        class M implements FuncRef.MatcherVoid {
            private List<String> parameterTypes = Collections.emptyList();
            private List<String> parameterNames = Collections.emptyList();
            private int insertLine0;
            private int declarationIndentLevels = 0;
            private final WFile targetFile = filename;
            private String receiverType = "";
            private WurstType returnType = WurstTypeVoid.instance();
            private boolean isAnnotation = false;


            @Override
            public void case_ExprFuncRef(ExprFuncRef e) {
                setPackageInsertPos(fr);
            }

            @Override
            public void case_Annotation(Annotation annotation) {
                isAnnotation = true;
                setPackageInsertPos(fr);
            }

            private void setPackageInsertPos(Element where) {
                PackageOrGlobal p = where.attrNearestPackage();
                if (p instanceof WPackage) {
                    // Insert right before endpackage.
                    insertLine0 = Math.max(0, p.attrSource().getEndLine() - 1);
                } else {
                    insertLine0 = 0;
                }
                declarationIndentLevels = 0;
            }

            private void setClassInsertPos(ClassOrInterface classDef) {
                // Insert right after class declaration. This is stable and avoids placing the stub inside method bodies.
                insertLine0 = classDef.attrSource().getLine();
                int memberIndentColumns = Math.max(0, classDef.attrSource().getStartColumn() - 1) + 4;
                CompilationUnitInfo.IndentationMode indentationMode = fr.attrCompilationUnit().getCuInfo().getIndentationMode();
                declarationIndentLevels = indentationMode.countIndents(memberIndentColumns);
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
                    setClassInsertPos(((WurstTypeClassOrInterface) leftType).getDef());
                } else {
                    setPackageInsertPos(e);
                    receiverType = leftType.toPrettyString() + ".";
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
                Optional<ClassDef> classDef = findNearest(e, ClassDef.class);
                if (classDef.isPresent()) {
                    setClassInsertPos(classDef.get());
                } else {
                    setPackageInsertPos(fr);
                }
                addParametertypes(e.getArgs());
            }


            public void appendDeclarationIndent(StringBuilder sb) {
                CompilationUnitInfo.IndentationMode indentationMode = fr.attrCompilationUnit().getCuInfo().getIndentationMode();
                indentationMode.appendIndent(sb, declarationIndentLevels);
            }

            public void appendBodyIndent(StringBuilder sb) {
                CompilationUnitInfo.IndentationMode indentationMode = fr.attrCompilationUnit().getCuInfo().getIndentationMode();
                indentationMode.appendIndent(sb, declarationIndentLevels + 1);
            }
        }
        M m = new M();
        fr.match(m);

        String title = "Create function " + fr.getFuncName();
        StringBuilder code = new StringBuilder("\n");
        m.appendDeclarationIndent(code);
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
        m.appendBodyIndent(code);
        code.append("skip\n");


        Range range = new Range(new Position(m.insertLine0, 0), new Position(m.insertLine0, 0));
        TextEdit textEdit = new TextEdit(range, code.toString());
        WorkspaceEdit edit = workspaceEdit(m.targetFile.getUriString(), textEdit);
        CodeAction action = makeQuickFix(title, edit);
        action.setIsPreferred(true);
        return Collections.singletonList(Either.forRight(action));
    }


    private List<Either<Command, CodeAction>> handleMissingType(ModelManager modelManager, String typeName) {
        WurstModel model = modelManager.getModel();
        Set<String> possibleImports = new LinkedHashSet<>();
        for (CompilationUnit cu : model) {
            for (WPackage wPackage : cu.getPackages()) {
                if (!wPackage.attrExportedTypeNameLinks().get(typeName).isEmpty()) {
                    possibleImports.add(wPackage.getName());
                }
            }
        }
        addDependencyPackageFallback(modelManager, possibleImports, typeName);

        return makeImportCommands(possibleImports, modelManager);
    }

    private List<Either<Command, CodeAction>> handleMissingClass(ModelManager modelManager, String typeName) {
        // TODO this is not optimal yet: We are only looking in the current model,
        // which only includes files which are in the current project or already imported
        // in the current project. So this will for example miss packages from the standard library
        // that are not yet imported into the project.

        WurstModel model = modelManager.getModel();
        Set<String> possibleImports = new LinkedHashSet<>();
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
        addDependencyPackageFallback(modelManager, possibleImports, typeName);

        return makeImportCommands(possibleImports, modelManager);
    }

    private List<Either<Command, CodeAction>> handleMissingModule(ModelManager modelManager, String moduleName) {

        WurstModel model = modelManager.getModel();
        Set<String> possibleImports = new LinkedHashSet<>();
        for (CompilationUnit cu : model) {
            withNextPackage:
            for (WPackage wPackage : cu.getPackages()) {
                for (NameLink nameLink : wPackage.attrExportedTypeNameLinks().get(moduleName)) {
                    if (nameLink.getDef() instanceof ModuleDef) {
                        possibleImports.add(wPackage.getName());
                        continue withNextPackage;
                    }
                }
            }
        }
        addDependencyPackageFallback(modelManager, possibleImports, moduleName);

        return makeImportCommands(possibleImports, modelManager);
    }



    private void addDependencyPackageFallback(ModelManager modelManager, Set<String> possibleImports, String unresolvedName) {
        for (File dep : modelManager.getDependencyWurstFiles()) {
            String libName = Utils.getLibName(dep);
            if (libName.equals(unresolvedName)) {
                possibleImports.add(libName);
            }
        }
    }

    private List<Either<Command, CodeAction>> makeImportCommands(Collection<String> possibleImports, ModelManager modelManager) {
        return possibleImports.stream()
                .map(imp -> makeImportAction(imp, modelManager))
                .collect(Collectors.toList());
    }

    private Either<Command, CodeAction> makeImportAction(String imp, ModelManager modelManager) {
        String title = "Import package " + imp;
        CompilationUnit cu = modelManager.getCompilationUnit(filename);
        Position pos = new Position(0, 0);
        if (cu != null && !cu.getPackages().isEmpty()) {
            WPackage p = cu.getPackages().get(0);
            int line = p.getNameId().getSource().getLine();
            for (WImport importStatement : p.getImports()) {
                line = Math.max(line, importStatement.getPackagenameId().getSource().getLine());
            }
            pos.setLine(line);
        }
        TextEdit textEdit = new TextEdit(new Range(pos, pos), "import " + imp + "\n");
        WorkspaceEdit edit = workspaceEdit(filename.getUriString(), textEdit);
        return Either.forRight(makeQuickFix(title, edit));
    }

    private WorkspaceEdit workspaceEdit(String uri, TextEdit textEdit) {
        Map<String, List<TextEdit>> changes = new LinkedHashMap<>();
        changes.put(uri, Collections.singletonList(textEdit));
        return new WorkspaceEdit(changes);
    }

    private CodeAction makeQuickFix(String title, WorkspaceEdit edit) {
        CodeAction action = new CodeAction(title);
        action.setKind(CodeActionKind.QuickFix);
        action.setEdit(edit);
        action.setDiagnostics(diagnostics);
        return action;
    }
}
