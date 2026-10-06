package de.peeeq.wurstio.languageserver;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.peeeq.wurstio.languageserver.requests.SemanticTokensRequest;
import de.peeeq.wurstscript.CompileTimeInfo;
import de.peeeq.wurstscript.WLogger;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.*;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class WurstLanguageServer implements LanguageServer, LanguageClientAware {
    private WFile rootUri;
    private final de.peeeq.wurstio.languageserver.LanguageWorker languageWorker;
    private LanguageClient languageClient;
    private RemoteEndpoint remoteEndpoint;
    private boolean initialBuildStatusSupported;
    private boolean workDoneProgressSupported;
    private boolean initialBuildProgressActive;
    private boolean initialized;
    private volatile boolean stopped;
    private static final String INITIAL_BUILD_TOKEN = "wurst/initialBuild";

    public WurstLanguageServer() {
        System.setErr(createFilteredErr());
        this.languageWorker = new de.peeeq.wurstio.languageserver.LanguageWorker();
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        System.err.println("Loading Wurst version " + CompileTimeInfo.version);
        setupLogger();
        if (params.getRootUri() == null) {
            System.err.println("Workspace null. Make sure to open a valid project root using File->Open Folder, before opening code files.");
            return CompletableFuture.completedFuture(null);
        }
        WLogger.info("initialize " + params.getRootUri());
        rootUri = WFile.create(params.getRootUri());
        ClientCapabilities clientCapabilities = params.getCapabilities();
        JsonElement experimental = new Gson().toJsonTree(clientCapabilities == null ? null : clientCapabilities.getExperimental());
        initialBuildStatusSupported = experimental.isJsonObject()
                && new JsonPrimitive(true).equals(experimental.getAsJsonObject().get("wurstInitialBuildStatus"));
        workDoneProgressSupported = clientCapabilities != null && clientCapabilities.getWindow() != null
                && Boolean.TRUE.equals(clientCapabilities.getWindow().getWorkDoneProgress());

        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setExperimental(Map.of("wurstInitialBuildStatus", initialBuildStatusSupported));
        capabilities.setCompletionProvider(new CompletionOptions(true, Collections.singletonList(".")));
        capabilities.setHoverProvider(true);
        capabilities.setDefinitionProvider(true);
        capabilities.setDeclarationProvider(true);
        capabilities.setTypeDefinitionProvider(true);
        capabilities.setImplementationProvider(true);
        capabilities.setSignatureHelpProvider(new SignatureHelpOptions(Arrays.asList("(", ".")));
        capabilities.setDocumentHighlightProvider(true);
        capabilities.setReferencesProvider(true);
        capabilities.setExecuteCommandProvider(new ExecuteCommandOptions(WurstCommands.providedCommands()));
        capabilities.setRenameProvider(new RenameOptions(true));

        TextDocumentSyncOptions textSync = new TextDocumentSyncOptions();
        textSync.setOpenClose(true);
        textSync.setChange(TextDocumentSyncKind.Incremental);
        textSync.setSave(true);
        capabilities.setTextDocumentSync(textSync);

        CodeActionOptions codeActionOptions = new CodeActionOptions(List.of(
                CodeActionKind.QuickFix,
                CodeActionKind.Refactor,
                CodeActionKind.RefactorExtract
        ));
        capabilities.setCodeActionProvider(codeActionOptions);
        capabilities.setDocumentSymbolProvider(true);
        capabilities.setWorkspaceSymbolProvider(true);
        capabilities.setDocumentFormattingProvider(true);
        capabilities.setColorProvider(true);
        capabilities.setCodeLensProvider(new CodeLensOptions(true));
        capabilities.setFoldingRangeProvider(true);
        SemanticTokensLegend semanticLegend = new SemanticTokensLegend(
                SemanticTokensRequest.TOKEN_TYPES,
                SemanticTokensRequest.TOKEN_MODIFIERS
        );
        SemanticTokensWithRegistrationOptions semanticTokensOptions =
                new SemanticTokensWithRegistrationOptions(semanticLegend, true, false);
        capabilities.setSemanticTokensProvider(semanticTokensOptions);
        capabilities.setInlayHintProvider(new InlayHintRegistrationOptions());


        InitializeResult res = new InitializeResult(capabilities);
        WLogger.info("initialization done: " + params.getRootUri());
        return CompletableFuture.completedFuture(res);
    }

    @Override
    public synchronized void initialized(InitializedParams params) {
        if (initialized || stopped || rootUri == null) return;
        initialized = true;
        languageWorker.setInitialBuildListener(success -> {
            if (stopped) return;
            if (initialBuildProgressActive) {
                WorkDoneProgressEnd end = new WorkDoneProgressEnd();
                end.setMessage(success ? "Workspace loaded" : "Workspace build failed; see compiler log");
                languageClient.notifyProgress(new ProgressParams(Either.forLeft(INITIAL_BUILD_TOKEN), Either.forLeft(end)));
            }
            reportInitialBuildStatus(success ? "ready" : "failed");
        });
        reportInitialBuildStatus("loading");
        if (!workDoneProgressSupported) {
            languageWorker.setRootPath(rootUri);
            return;
        }
        // This is a server-to-client request: it does not flush delayed document opens.
        // A broken/rejecting progress UI must not prevent the workspace build.
        languageClient.createProgress(new WorkDoneProgressCreateParams(Either.forLeft(INITIAL_BUILD_TOKEN)))
                .orTimeout(5, TimeUnit.SECONDS).whenComplete((ignored, error) -> {
                    if (stopped) return;
                    if (error == null) {
                        WorkDoneProgressBegin begin = new WorkDoneProgressBegin();
                        begin.setTitle("Loading WurstScript workspace");
                        begin.setCancellable(false);
                        languageClient.notifyProgress(new ProgressParams(Either.forLeft(INITIAL_BUILD_TOKEN), Either.forLeft(begin)));
                        initialBuildProgressActive = true;
                    } else {
                        WLogger.warning("Initial workspace progress unavailable; continuing without progress UI.");
                    }
                    languageWorker.setRootPath(rootUri);
                });
    }

    private void reportInitialBuildStatus(String state) {
        if (initialBuildStatusSupported) {
            // Standard work-done progress has no success/failure field. Keep readiness typed
            // rather than making clients parse human-readable progress messages.
            remoteEndpoint.notify("wurst/initialBuildStatus", Map.of("state", state));
        }
    }
    private void setupLogger() {
        WLogger.setLogger("languageServer");
        Logger.getLogger(RemoteEndpoint.class.getName()).setLevel(Level.SEVERE);
        Logger.getLogger("org.eclipse.lsp4j.jsonrpc.RemoteEndpoint").setLevel(Level.SEVERE);
    }

    private PrintStream createFilteredErr() {
        PrintStream rawErr = new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8);
        OutputStream filteringOutput = new OutputStream() {
            private final StringBuilder lineBuffer = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                char c = (char) b;
                if (c == '\n') {
                    flushLine();
                    rawErr.write('\n');
                    rawErr.flush();
                } else if (c != '\r') {
                    lineBuffer.append(c);
                }
            }

            @Override
            public void flush() throws IOException {
                flushLine();
                rawErr.flush();
            }

            private void flushLine() throws IOException {
                if (lineBuffer.isEmpty()) {
                    return;
                }
                String line = lineBuffer.toString();
                lineBuffer.setLength(0);
                if (shouldSuppressErrLine(line)) {
                    return;
                }
                rawErr.write(line.getBytes(StandardCharsets.UTF_8));
            }
        };
        return new PrintStream(filteringOutput, true, StandardCharsets.UTF_8);
    }

    private boolean shouldSuppressErrLine(String line) {
        return line.startsWith("WARNING: A restricted method in java.lang.System has been called")
            || line.startsWith("WARNING: java.lang.System::load has been called by org.sqlite.SQLiteJDBCLoader")
            || line.startsWith("WARNING: Use --enable-native-access=ALL-UNNAMED")
            || line.startsWith("WARNING: Restricted methods will be blocked in a future release");
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        WLogger.info("shutdown");
        stopped = true;
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
