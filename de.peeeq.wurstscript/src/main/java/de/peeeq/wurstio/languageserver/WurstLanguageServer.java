package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstscript.CompileTimeInfo;
import de.peeeq.wurstscript.WLogger;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class WurstLanguageServer implements org.eclipse.lsp4j.services.LanguageServer, LanguageClientAware {
    private WFile rootUri;
    private final de.peeeq.wurstio.languageserver.LanguageWorker languageWorker = new de.peeeq.wurstio.languageserver.LanguageWorker();
    private LanguageClient languageClient;
    private RemoteEndpoint remoteEndpoint;

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        System.err.println("Loading Wurst version " + CompileTimeInfo.version);
        setupLogger();
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        if (params.getRootUri() == null) {
            System.err.println("Workspace null. Make sure to open a valid project root using File->Open Folder, before opening code files.");
            return CompletableFuture.completedFuture(null);
        }
        WLogger.info("initialize " + params.getRootUri());
        rootUri = WFile.create(params.getRootUri());
        languageWorker.setRootPath(rootUri);

        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setCompletionProvider(new CompletionOptions(false, Collections.singletonList(".")));
        capabilities.setHoverProvider(true);
        capabilities.setDefinitionProvider(true);
        capabilities.setSignatureHelpProvider(new SignatureHelpOptions(Arrays.asList("(", ".")));
        capabilities.setDocumentHighlightProvider(true);
        capabilities.setReferencesProvider(true);
        capabilities.setExecuteCommandProvider(new ExecuteCommandOptions(WurstCommands.providedCommands()));
        capabilities.setRenameProvider(true);


        capabilities.setTextDocumentSync(Either.forLeft(TextDocumentSyncKind.Full));
        capabilities.setCodeActionProvider(true);
        capabilities.setDocumentSymbolProvider(true);
        capabilities.setWorkspaceSymbolProvider(true);
        capabilities.setDocumentFormattingProvider(true);
        capabilities.setColorProvider(true);
        capabilities.setCodeLensProvider(new CodeLensOptions(true));
        capabilities.setFoldingRangeProvider(true);


        InitializeResult res = new InitializeResult(capabilities);
        WLogger.info("initialization done: " + params.getRootUri());
        System.err.println("initialization done!");
        return CompletableFuture.completedFuture(res);
    }
    private void setupLogger() {
        WLogger.setLogger("languageServer");
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        WLogger.info("shutdown");
        languageWorker.stop();
        return CompletableFuture.completedFuture("ok");
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        WLogger.info("getTextDocumentService");
        return new WurstTextDocumentService(languageWorker);
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        WLogger.info("getWorkspaceService");
        return new WurstWorkspaceService(this);
    }

    @Override
    public void connect(LanguageClient client) {
        WLogger.info("connect to LanguageClient");
        this.languageClient = client;
        languageWorker.setLanguageClient(client);
    }

    public LanguageWorker worker() {
        return languageWorker;
    }

    public WFile getRootUri() {
        return rootUri;
    }

    public LanguageClient getLanguageClient() {
        return languageClient;
    }

    public ConfigProvider getConfigProvider() {
        return new ConfigProvider(languageClient);
    }

    public void setRemoteEndpoint(RemoteEndpoint remoteEndpoint) {
        this.remoteEndpoint = remoteEndpoint;
    }

    public RemoteEndpoint getRemoteEndpoint() {
        return remoteEndpoint;
    }
}
