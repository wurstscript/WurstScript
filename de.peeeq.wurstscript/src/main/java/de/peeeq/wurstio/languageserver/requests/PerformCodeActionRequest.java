package de.peeeq.wurstio.languageserver.requests;

import com.google.common.collect.ImmutableMap;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.WurstLanguageServer;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.parser.WPos;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class PerformCodeActionRequest extends UserRequest<Object> {
    public static final String IMPORT_PACKAGE = "IMPORT_PACKAGE";
    private final WurstLanguageServer server;
    private final ExecuteCommandParams params;
    private final List<Object> args;

    public PerformCodeActionRequest(WurstLanguageServer server, ExecuteCommandParams params) {
        this.server = server;
        this.params = params;
        this.args = params.getArguments();
    }

    @Override
    public Object execute(ModelManager modelManager) {
        System.err.println("code action " + args);
        if (args.isEmpty()) {
            throw new RuntimeException("No arguments given.");
        }
        Map<?, ?> action = (Map<?,?>) args.get(0);
        switch ((String) action.get("type")) {
            case IMPORT_PACKAGE:
                return addImport(modelManager, (String) action.get("uriString"), (String) action.get("import"));

        }
        throw new RuntimeException("Unhandled action: " + action);
    }

    private Object addImport(ModelManager modelManager, String fileUri, String importName) {
        WFile file = WFile.create(fileUri);
        CompilationUnit cu = modelManager.getCompilationUnit(file);

        Position pos = new Position(0,0);

        if (!cu.getPackages().isEmpty()) {
            WPackage p = cu.getPackages().get(0);
            int line = p.getNameId().getSource().getLine() + 1;
            for (WImport imp : p.getImports()) {
                line = Math.max(line, imp.getPackagenameId().getSource().getLine());
            }
            pos.setLine(line);
        }

        Range range = new Range(pos, pos);
        TextEdit textEdit = new TextEdit(range, "import " + importName + "\n");
        List<TextEdit> textEdits = Collections.singletonList(textEdit);
        return applyTextEdits(file, textEdits);
    }

    private Object applyTextEdits(WFile file, List<TextEdit> textEdits) {
        LanguageClient languageClient = server.getLanguageClient();
        int version = server.worker().getBufferManager().getTextDocumentVersion(file);
        VersionedTextDocumentIdentifier textDocument = new VersionedTextDocumentIdentifier(version);
        textDocument.setUri(file.getUriString());

        TextDocumentEdit change = new TextDocumentEdit(textDocument, textEdits);
        List<TextDocumentEdit> documentChanges = Collections.singletonList(change);
        languageClient.applyEdit(new ApplyWorkspaceEditParams(new WorkspaceEdit(documentChanges)));
        return "ok";
    }

    public static Map<String, String> importPackageAction(String uriString, String imp) {
        return ImmutableMap.of(
                "type", IMPORT_PACKAGE,
                "uriString", uriString,
                "import", imp
        );
    }
}
