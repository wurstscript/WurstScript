package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstio.languageserver.requests.UserRequest;
import de.peeeq.wurstscript.WLogger;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.NotificationMessage;
import org.eclipse.lsp4j.services.LanguageClient;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

public class InitialBuildProgressTest {
    private java.io.PrintStream errorStream;
    private java.util.logging.Level rpcLogLevel;

    @BeforeMethod
    public void rememberProcessState() {
        errorStream = System.err;
        rpcLogLevel = java.util.logging.Logger.getLogger(RemoteEndpoint.class.getName()).getLevel();
    }

    @AfterMethod(alwaysRun = true)
    public void restoreProcessState() {
        // Real server initialization switches the compiler's default global logger.
        WLogger.setLogger("default");
        System.setErr(errorStream);
        java.util.logging.Logger.getLogger(RemoteEndpoint.class.getName()).setLevel(rpcLogLevel);
    }

    @Test
    public void reportsStandardProgressAndStructuredReadiness() throws Exception {
        checkBuild(true, true, false, false, false);
    }

    @Test
    public void readinessDoesNotRequireProgressUi() throws Exception {
        checkBuild(false, true, false, false, false);
    }

    @Test
    public void rejectedProgressUiDoesNotBlockBuild() throws Exception {
        checkBuild(true, true, true, false, false);
    }

    @Test
    public void missingProjectReportsFailedAndClosesProgress() throws Exception {
        checkBuild(true, true, false, true, false);
    }

    @Test
    public void legacyClientRetainsRequestBarrierWithoutCustomNotifications() throws Exception {
        checkBuild(false, false, false, false, false);
    }

    @Test
    public void timedOutProgressUiDoesNotBlockBuild() throws Exception {
        checkBuild(true, true, false, false, true);
    }

    @Test
    public void formattingWaitsForProgressAndBuildAndKeepsBothDocuments() throws Exception {
        Path project = Files.createTempDirectory("wurst-early-formatting");
        Path sources = Files.createDirectories(project.resolve("wurst"));
        Files.writeString(sources.resolve("Wurst.wurst"), "package Wurst\nendpackage\n");
        Path first = sources.resolve("Main.wurst");
        Path second = sources.resolve("Second.wurst");
        Files.writeString(first, "package Main\nendpackage\n");
        Files.writeString(second, "package Second\nendpackage\n");
        CompletableFuture<Void> creation = new CompletableFuture<>();
        RecordingClient client = new RecordingClient(false) {
            @Override
            public CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params) { return creation; }
        };
        WurstLanguageServer server = new WurstLanguageServer();
        server.connect(client);
        try {
            InitializeParams params = new InitializeParams();
            params.setRootUri(project.toUri().toString());
            ClientCapabilities capabilities = new ClientCapabilities();
            WindowClientCapabilities window = new WindowClientCapabilities();
            window.setWorkDoneProgress(true);
            capabilities.setWindow(window);
            params.setCapabilities(capabilities);
            server.initialize(params).join();
            server.initialized(new InitializedParams());
            var documents = server.getTextDocumentService();
            String unsaved = "package Main\nfunction unsavedFormatting()\n    skip\nendpackage\n";
            documents.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(first.toUri().toString(), "wurst", 2, unsaved)));
            documents.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(second.toUri().toString(), "wurst", 2, "\n\n")));
            var firstFormat = documents.formatting(new DocumentFormattingParams(
                    new TextDocumentIdentifier(first.toUri().toString()), new FormattingOptions(4, true)));
            var secondFormat = documents.formatting(new DocumentFormattingParams(
                    new TextDocumentIdentifier(second.toUri().toString()), new FormattingOptions(4, true)));
            assertFalse(firstFormat.isDone(), "formatting must wait while progress creation is pending");
            assertFalse(secondFormat.isDone(), "formatting one document must not cancel another");
            creation.complete(null);
            List<? extends TextEdit> firstEdits = firstFormat.get(5, TimeUnit.SECONDS);
            assertEquals(firstEdits.size(), 1);
            assertTrue(firstEdits.getFirst().getNewText().contains("unsavedFormatting"));
            assertEquals(firstEdits.getFirst().getRange().getEnd(), new Position(4, 0));
            List<? extends TextEdit> blankEdits = secondFormat.get(5, TimeUnit.SECONDS);
            assertEquals(blankEdits.size(), 1);
            assertEquals(blankEdits.getFirst().getRange().getEnd(), new Position(2, 0));
        } finally {
            server.shutdown().join();
        }
    }

    @Test
    public void shutdownWhileCreatingProgressDoesNotStartWorkerLater() throws Exception {
        WurstLanguageServer server = new WurstLanguageServer();
        CompletableFuture<Void> creation = new CompletableFuture<>();
        RecordingClient client = new RecordingClient(false) {
            @Override
            public CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params) { return creation; }
        };
        server.connect(client);
        try {
            InitializeParams params = new InitializeParams();
            params.setRootUri(Files.createTempDirectory("wurst-stopped-progress").toUri().toString());
            ClientCapabilities capabilities = new ClientCapabilities();
            WindowClientCapabilities window = new WindowClientCapabilities();
            window.setWorkDoneProgress(true);
            capabilities.setWindow(window);
            params.setCapabilities(capabilities);
            server.initialize(params).join();
            server.initialized(new InitializedParams());
            assertNull(server.worker().modelManager);
            server.shutdown().join();
            creation.complete(null);
            assertNull(server.worker().modelManager, "late progress creation must not revive a stopped server");
            assertTrue(client.progress.isEmpty());
        } finally {
            server.shutdown().join();
        }
    }

    private void checkBuild(boolean progress, boolean readiness, boolean rejectProgress, boolean failBuild, boolean stallProgress) throws Exception {
        Path project = Files.createTempDirectory("wurst-initial-progress");
        if (failBuild) {
            Files.delete(project);
        } else {
            Path source = Files.createDirectories(project.resolve("wurst")).resolve("Main.wurst");
            // Normal source diagnostics are still a completed workspace load.
            Files.writeString(source, "package Main\nimport MissingPackage\nendpackage\n");
        }
        WurstLanguageServer server = new WurstLanguageServer();
        RecordingClient client = new RecordingClient(rejectProgress);
        client.stallProgress = stallProgress;
        List<Message> notifications = new CopyOnWriteArrayList<>();
        server.setRemoteEndpoint(new RemoteEndpoint(notifications::add, new Endpoint() {
            @Override
            public CompletableFuture<?> request(String method, Object parameter) {
                return CompletableFuture.failedFuture(new UnsupportedOperationException(method));
            }

            @Override
            public void notify(String method, Object parameter) {}
        }));
        server.connect(client);
        try {
            InitializeParams params = new InitializeParams();
            params.setRootUri(project.toUri().toString());
            ClientCapabilities capabilities = new ClientCapabilities();
            WindowClientCapabilities window = new WindowClientCapabilities();
            window.setWorkDoneProgress(progress);
            capabilities.setWindow(window);
            capabilities.setExperimental(Map.of("wurstInitialBuildStatus", readiness));
            params.setCapabilities(capabilities);
            InitializeResult result = server.initialize(params).get(2, TimeUnit.SECONDS);
            assertEquals(result.getCapabilities().getExperimental(), Map.of("wurstInitialBuildStatus", readiness));
            assertTrue(notifications.isEmpty(), "initialization must not emit readiness before initialized");
            assertNull(server.worker().modelManager, "the build must wait for initialized");
            server.initialized(new InitializedParams());
            server.initialized(new InitializedParams());
            // The worker answers requests only after its initial build, even for legacy clients.
            assertTrue(server.worker().handle(new UserRequest<Boolean>() {
                @Override
                public Boolean execute(ModelManager modelManager) { return true; }
            }).get(10, TimeUnit.SECONDS));
            assertEquals(client.creates, progress ? 1 : 0, "duplicate initialized must not restart progress");
            assertEquals(client.progress.size(), progress && !rejectProgress && !stallProgress ? 2 : 0);
            if (!client.progress.isEmpty()) {
                assertTrue(client.progress.get(0).getValue().getLeft() instanceof WorkDoneProgressBegin);
                assertTrue(client.progress.get(1).getValue().getLeft() instanceof WorkDoneProgressEnd);
                assertEquals(client.progress.get(0).getToken(), client.progress.get(1).getToken());
                assertFalse(((WorkDoneProgressBegin) client.progress.get(0).getValue().getLeft()).getCancellable());
            }
            List<Object> states = notifications.stream().map(message -> {
                NotificationMessage notification = (NotificationMessage) message;
                assertEquals(notification.getMethod(), "wurst/initialBuildStatus");
                return notification.getParams();
            }).toList();
            assertEquals(states, readiness
                    ? List.of(Map.of("state", "loading"), Map.of("state", failBuild ? "failed" : "ready"))
                    : List.of());
        } finally {
            server.shutdown().join();
        }
    }

    private static class RecordingClient implements LanguageClient {
        private final boolean rejectProgress;
        private final List<ProgressParams> progress = new CopyOnWriteArrayList<>();
        private int creates;
        private boolean stallProgress;

        RecordingClient(boolean rejectProgress) { this.rejectProgress = rejectProgress; }

        @Override
        public CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params) {
            creates++;
            if (stallProgress) return new CompletableFuture<>();
            return rejectProgress ? CompletableFuture.failedFuture(new IllegalStateException("no progress UI"))
                    : CompletableFuture.completedFuture(null);
        }

        @Override
        public void notifyProgress(ProgressParams params) { progress.add(params); }

        @Override
        public void telemetryEvent(Object object) {}

        @Override
        public void publishDiagnostics(PublishDiagnosticsParams params) {}

        @Override
        public void showMessage(MessageParams params) {}

        @Override
        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams params) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void logMessage(MessageParams params) {}
    }
}
