package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstio.languageserver.requests.UserRequest;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.NotificationMessage;
import org.eclipse.lsp4j.services.LanguageClient;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

public class InitialBuildProgressTest {
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
