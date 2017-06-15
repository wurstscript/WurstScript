package de.peeeq.wurstio.languageserver2;

import de.peeeq.wurstscript.WLogger;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class WurstWorkspaceService implements WorkspaceService {
    private final LanguageWorker worker;

    public WurstWorkspaceService(LanguageWorker worker) {
        this.worker = worker;
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
        WLogger.info("symbol");
        return null;
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        WLogger.info("didChangeConfiguration");

    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        WLogger.info("didChangeWatchedFiles");
        worker.handleFileChanged(params);
    }
}
