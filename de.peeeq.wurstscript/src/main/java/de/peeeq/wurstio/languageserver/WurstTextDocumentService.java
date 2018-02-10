package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstio.languageserver.requests.*;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.prettyPrint.PrettyUtils;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 *
 */
public class WurstTextDocumentService implements TextDocumentService {
    private final LanguageWorker worker;

    public WurstTextDocumentService(LanguageWorker worker) {
        this.worker = worker;
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(TextDocumentPositionParams position) {
        WLogger.info("completion");
        return worker.handle(new GetCompletions(position, worker.getBufferManager())).thenApply(Either::forRight);
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
        WLogger.info("resolveCompletionItem");
        return null;
    }

    @Override
    public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
        WLogger.info("hover");
        return worker.handle(new HoverInfo(position, worker.getBufferManager()));
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
        WLogger.info("signatureHelp");
        return worker.handle(new SignatureInfo(position, worker.getBufferManager()));
    }

    @Override
    public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams position) {
        WLogger.info("definition");
        return worker.handle(new GetDefinition(position, worker.getBufferManager()));
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
        WLogger.info("references");
        return worker.handle(new GetUsages(params, worker.getBufferManager(), true))
                .thenApply((List<GetUsages.UsagesData> udList) ->
                        udList.stream()
                                .map(GetUsages.UsagesData::getLocation)
                                .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams position) {
        WLogger.info("documentHighlight");
        return worker.handle(new GetUsages(position, worker.getBufferManager(), false))
                .thenApply((List<GetUsages.UsagesData> udList) ->
                        udList.stream()
                                .map(GetUsages.UsagesData::toDocumentHighlight)
                                .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(DocumentSymbolParams params) {
        return worker.handle(new SymbolInformationRequest(params));
    }

    @Override
    public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
        WLogger.info("codeAction");
        return worker.handle(new CodeActionRequest(params, worker.getBufferManager()));
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        WLogger.info("codeLens");
        return null;
    }

    @Override
    public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
        WLogger.info("resolveCodeLens");
        return null;
    }

    private static String readFile(String filename) {
        String everything = "";
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            everything = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return everything;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
        WLogger.info("formatting");
        String path = params.getTextDocument().getUri().replaceFirst("file:/", "");
        WLogger.info("path:" + path);
        String before = readFile(path);
        String clean = PrettyUtils.pretty(path);

        String[] lines = before.split("\n");
        Range range = new Range(new Position(0, 0), new Position(lines.length, lines[lines.length-1].length()));
        TextEdit textEdit = new TextEdit(range, clean);

        List<TextEdit> edits = new ArrayList<>();
        edits.add(textEdit);
        return CompletableFuture.completedFuture(edits);
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
        WLogger.info("rangeFormatting");
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
        WLogger.info("onTypeFormatting");
        return null;
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
        WLogger.info("rename");
        return null;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        WLogger.info("didOpen");

    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        WLogger.info("didChange");
        worker.handleChange(params);

    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        WLogger.info("didClose");

    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        WLogger.info("didSave");

    }
}
