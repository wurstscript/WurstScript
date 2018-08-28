package de.peeeq.wurstio.languageserver.requests;

import com.google.gson.JsonObject;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.WurstLanguageServer;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.Collections;
import java.util.List;

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
        WLogger.info("code action " + args);
        if (args.isEmpty()) {
            throw new RuntimeException("No arguments given.");
        }
        JsonObject action = (JsonObject) args.get(0);
        switch (action.get("type").getAsString()) {
            case IMPORT_PACKAGE:
                return addImport(modelManager, action.get("uriString").getAsString(), action.get("import").getAsString());

        }
        throw new RuntimeException("Unhandled action: " + action);
    }

    private Object addImport(ModelManager modelManager, String fileUri, String importName) {
        WFile file = WFile.create(fileUri);
        CompilationUnit cu = modelManager.getCompilationUnit(file);

        Position pos = new Position(0, 0);

        if (!cu.getPackages().isEmpty()) {
            WPackage p = cu.getPackages().get(0);
            int line = p.getNameId().getSource().getLine();
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

    public static JsonObject importPackageAction(String uriString, String imp) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", IMPORT_PACKAGE);
        jsonObject.addProperty("uriString", uriString);
        jsonObject.addProperty("import", imp);
        return jsonObject;
    }
}
