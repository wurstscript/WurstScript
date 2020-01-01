package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstio.languageserver.requests.*;
import de.peeeq.wurstio.languageserver.requests.CodeLensRequest;
import de.peeeq.wurstscript.WLogger;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

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
    public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
        WLogger.info("hover");
        return worker.handle(new HoverInfo(position, worker.getBufferManager()));
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
        WLogger.info("signatureHelp");
        return worker.handle(new SignatureInfo(position));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(TextDocumentPositionParams position) {
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
        return null;
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
