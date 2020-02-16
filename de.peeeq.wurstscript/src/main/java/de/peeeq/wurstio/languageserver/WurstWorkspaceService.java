package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstio.languageserver.requests.SymbolInformationRequest;
import de.peeeq.wurstscript.WLogger;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class WurstWorkspaceService implements WorkspaceService {

    private WurstLanguageServer server;

    public WurstWorkspaceService(WurstLanguageServer server) {
        this.server = server;
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
        WLogger.info("symbol");
        return server.worker().handle(new SymbolInformationRequest(params));
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        WLogger.trace("didChangeConfiguration");

    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        WLogger.trace("didChangeWatchedFiles");
        server.worker().handleFileChanged(params);
    }

    @Override
    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        return WurstCommands.execute(server, params);
    }
}
