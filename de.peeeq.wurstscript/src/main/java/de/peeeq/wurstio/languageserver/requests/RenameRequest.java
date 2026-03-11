package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManager;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class RenameRequest extends UserRequest<WorkspaceEdit> {
    private final RenameParams params;
    private final BufferManager bufferManager;

    public RenameRequest(RenameParams params, BufferManager bufferManager) {
        this.params = params;
        this.bufferManager = bufferManager;
    }

    @Override
    public WorkspaceEdit execute(ModelManager modelManager) {
        TextDocumentPositionParams pos = new TextDocumentPositionParams(params.getTextDocument(), params.getPosition());
        GetUsages getUsages = new GetUsages(pos, bufferManager, true);
        List<GetUsages.UsagesData> usages = getUsages.execute(modelManager);


        List<Either<TextDocumentEdit, ResourceOperation>> edits = new ArrayList<>();
        Map<String, List<GetUsages.UsagesData>> usageMap = usages.stream().collect(Collectors.groupingBy(u -> u.getLocation().getUri()));

        for (Map.Entry<String, List<GetUsages.UsagesData>> e : usageMap.entrySet()) {
            String uri = e.getKey();
            VersionedTextDocumentIdentifier textDocument = new VersionedTextDocumentIdentifier();
            textDocument.setUri(uri);
            textDocument.setVersion(bufferManager.getTextDocumentVersion(de.peeeq.wurstio.languageserver.WFile.create(uri)));
            List<TextEdit> fileEdits = new ArrayList<>();
            Set<String> seenRanges = new LinkedHashSet<>();
            for (GetUsages.UsagesData usage : e.getValue()) {
                Range range = usage.getRange();
                String key = range.getStart().getLine() + ":" + range.getStart().getCharacter() + "-"
                        + range.getEnd().getLine() + ":" + range.getEnd().getCharacter();
                if (seenRanges.add(key)) {
                    fileEdits.add(new TextEdit(range, params.getNewName()));
                }
            }
            edits.add(Either.forLeft(new TextDocumentEdit(textDocument, fileEdits)));
        }

        return new WorkspaceEdit(edits);
    }
}
