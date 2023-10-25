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
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
        WLogger.info("completion");
        return worker.handle(new GetCompletions(position, worker.getBufferManager())).thenApply(Either::forRight);
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
        WLogger.info("resolveCompletionItem");
        return null;
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams hoverParams) {
        WLogger.info("hover");
        return worker.handle(new HoverInfo(hoverParams, worker.getBufferManager()));
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(SignatureHelpParams helpParams) {
        WLogger.info("signatureHelp");
        return worker.handle(new SignatureInfo(helpParams));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(DefinitionParams definitionParams) {
        WLogger.info("definition");
        return worker.handle(new GetDefinition(definitionParams, worker.getBufferManager()));
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
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(DocumentHighlightParams highlightParams) {
        WLogger.info("documentHighlight");
        return worker.handle(new GetUsages(highlightParams, worker.getBufferManager(), false))
                .thenApply((List<GetUsages.UsagesData> udList) ->
                        udList.stream()
                                .map(GetUsages.UsagesData::toDocumentHighlight)
                                .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(DocumentSymbolParams params) {
        return worker.handle(new DocumentSymbolRequest(params));
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
        WLogger.info("codeAction");
        return worker.handle(new CodeActionRequest(params, worker.getBufferManager()));
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        return worker.handle(new CodeLensRequest.GetCodeLens(params, worker. getBufferManager()));
    }

    @Override
    public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
        return worker.handle(new CodeLensRequest.Resolve(unresolved));
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
        WLogger.info("formatting");
        TextDocumentIdentifier doc = params.getTextDocument();
        String buffer = worker.getBufferManager().getBuffer(doc);

        String ending = doc.getUri().substring(doc.getUri().lastIndexOf("."));
        String clean = PrettyUtils.pretty(buffer, ending);

        String[] lines = buffer.split("\n");
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
        return worker.handle(new RenameRequest(params, worker.getBufferManager()));
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

    @Override
    public CompletableFuture<List<ColorInformation>> documentColor(DocumentColorParams params) {
        return worker.handle(new Colors.DocumentColorRequest(params));
    }

    @Override
    public CompletableFuture<List<ColorPresentation>> colorPresentation(ColorPresentationParams params) {
        return worker.handle(new Colors.ColorPresentationRequest(params));
    }

    @Override
    public CompletableFuture<List<FoldingRange>> foldingRange(FoldingRangeRequestParams params) {
        return worker.handle(new FoldingRangeRequest(params));
    }
}
