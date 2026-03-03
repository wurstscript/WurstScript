package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstio.languageserver.requests.*;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.prettyPrint.PrettyUtils;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either3;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        WLogger.debug("completion");
        return worker.handle(new GetCompletions(position, worker.getBufferManager())).thenApply(Either::forRight);
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
        WLogger.trace("resolveCompletionItem");
        return CompletableFuture.completedFuture(unresolved);
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams hoverParams) {
        return worker.handle(new HoverInfo(hoverParams, worker.getBufferManager()));
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(SignatureHelpParams helpParams) {
        WLogger.debug("signatureHelp");
        return worker.handle(new SignatureInfo(helpParams, worker.getBufferManager()));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(DefinitionParams definitionParams) {
        WLogger.debug("definition");
        return worker.handle(new GetDefinition(definitionParams, worker.getBufferManager(), GetDefinition.LookupType.DEFINITION));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> declaration(DeclarationParams params) {
        WLogger.debug("declaration");
        return worker.handle(new GetDefinition(params, worker.getBufferManager(), GetDefinition.LookupType.DECLARATION));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> typeDefinition(TypeDefinitionParams params) {
        WLogger.debug("typeDefinition");
        return worker.handle(new GetDefinition(params, worker.getBufferManager(), GetDefinition.LookupType.TYPE_DEFINITION));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> implementation(ImplementationParams params) {
        WLogger.debug("implementation");
        return worker.handle(new GetDefinition(params, worker.getBufferManager(), GetDefinition.LookupType.IMPLEMENTATION));
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
        WLogger.debug("references");
        return worker.handle(new GetUsages(params, worker.getBufferManager(), true))
                .thenApply((List<GetUsages.UsagesData> udList) ->
                {
                    List<Location> list = new ArrayList<>();
                    for (GetUsages.UsagesData usagesData : udList) {
                        Location location = usagesData.getLocation();
                        list.add(location);
                    }
                    return list;
                });
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(DocumentHighlightParams highlightParams) {
        WLogger.debug("documentHighlight");
        return worker.handle(new GetUsages(highlightParams, worker.getBufferManager(), false))
                .thenApply((List<GetUsages.UsagesData> udList) ->
                {
                    List<DocumentHighlight> list = new ArrayList<>();
                    for (GetUsages.UsagesData usagesData : udList) {
                        DocumentHighlight documentHighlight = usagesData.toDocumentHighlight();
                        list.add(documentHighlight);
                    }
                    return list;
                });
    }

    @Override
    public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(DocumentSymbolParams params) {
        return worker.handle(new DocumentSymbolRequest(params));
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
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
        WLogger.debug("formatting");

        if (worker.modelManager.hasErrors()) {
            throw new RequestFailedException(MessageType.Error, "Fix errors in your code before running.\n" + worker.modelManager.getFirstErrorDescription());
        }
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
        WLogger.trace("rangeFormatting");
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
        WLogger.trace("onTypeFormatting");
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
        WLogger.debug("rename");
        return worker.handle(new RenameRequest(params, worker.getBufferManager()));
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        WLogger.debug("didOpen");
        worker.handleOpen(params);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        WLogger.trace("didChange");
        worker.handleChange(params);

    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        WLogger.debug("didClose");
        worker.handleClose(params);
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        WLogger.debug("didSave");
        worker.handleSave(params);
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

    @Override
    public CompletableFuture<Either3<Range, PrepareRenameResult, PrepareRenameDefaultBehavior>> prepareRename(PrepareRenameParams params) {
        return worker.handle(new PrepareRenameRequest(params, worker.getBufferManager()));
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        return worker.handle(new SemanticTokensRequest(params, worker.getBufferManager()));
    }

    @Override
    public CompletableFuture<List<InlayHint>> inlayHint(InlayHintParams params) {
        return worker.handle(new InlayHintsRequest(params, worker.getBufferManager()));
    }

    @Override
    public CompletableFuture<InlayHint> resolveInlayHint(InlayHint unresolved) {
        return CompletableFuture.completedFuture(unresolved);
    }
}
