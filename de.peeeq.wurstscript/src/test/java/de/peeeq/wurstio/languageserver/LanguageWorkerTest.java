package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import org.eclipse.lsp4j.*;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Comparator;
import de.peeeq.wurstio.languageserver.requests.UserRequest;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class LanguageWorkerTest {

    @DataProvider
    public Object[][] coldCompletionDocumentationSymbols() {
        return new Object[][] {
                {"DisplayTextToPlayer", "DisplayTextToPl", false},
                {"PLAYER_COLOR_RED", "PLAYER_COLOR_R", false},
                {"DisplayTextToPlayer", "DisplayTextToPl", true},
                {"PLAYER_COLOR_RED", "PLAYER_COLOR_R", true}
        };
    }

    @Test(dataProvider = "coldCompletionDocumentationSymbols")
    public void firstNativeResolveWaitsForColdDatabaseWithoutBlockingWorker(String symbol, String prefix,
                                                                          boolean removeDeclaration) throws Exception {
        Path root = Files.createTempDirectory("wurst-cold-completion-docs");
        Path wurst = Files.createDirectories(root.resolve("wurst"));
        Path source = wurst.resolve("test.wurst");
        Files.writeString(source, "package test\ninit\n    " + prefix + "\n");
        Files.writeString(wurst.resolve("Wurst.wurst"), "package Wurst\n");
        Path database = root.resolve("jassdoc.db");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE docs(name TEXT, documentation TEXT)");
            statement.execute("INSERT INTO docs VALUES ('" + symbol + "', 'cold database documentation')");
        }
        String previous = System.getProperty("WURST_JASSDOC_DB_PATH");
        LanguageWorker worker = new LanguageWorker();
        worker.setLanguageClient((org.eclipse.lsp4j.services.LanguageClient) java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{org.eclipse.lsp4j.services.LanguageClient.class},
                (proxy, method, args) -> null));
        WurstTextDocumentService service = new WurstTextDocumentService(worker);
        try {
            System.setProperty("WURST_JASSDOC_DB_PATH", database.toString());
            worker.setRootPath(WFile.create(root));
            CompletionList completions = service.completion(new CompletionParams(
                    new TextDocumentIdentifier(source.toUri().toString()), new Position(2, 4 + prefix.length())))
                    .get(10, TimeUnit.SECONDS).getRight();
            CompletionItem item = completions.getItems().stream().filter(candidate -> symbol.equals(candidate.getLabel()))
                    .findFirst().orElseThrow();
            java.util.concurrent.CompletableFuture<CompletionItem> pending;
            JassDocService docs = JassDocService.getInstance();
            synchronized (docs) {
                docs.clearCacheForTests();
                // Hold the initialization lock so a cold DB cannot become ready by chance.
                pending = service.resolveCompletionItem(item);
                assertTrue(worker.handle(new UserRequest<Boolean>() {
                    @Override
                    public Boolean execute(ModelManager modelManager) { return true; }
                }).get(2, TimeUnit.SECONDS), "A DB lookup must not occupy the language worker");
                assertFalse(pending.isDone(), "The first resolve must await documentation rather than return an empty item");
                if (removeDeclaration) {
                    worker.handle(new UserRequest<Boolean>() {
                        @Override
                        public Boolean execute(ModelManager modelManager) {
                            CompilationUnit common = modelManager.getModel().stream()
                                    .filter(cu -> cu.getCuInfo().getFile().endsWith("common.j")).findFirst().orElseThrow();
                            modelManager.removeCompilationUnit(WFile.create(common.getCuInfo().getFile()));
                            return true;
                        }
                    }).get(2, TimeUnit.SECONDS);
                }
            }
            CompletionItem resolved = pending.get(5, TimeUnit.SECONDS);
            if (removeDeclaration) {
                assertNull(resolved.getDocumentation(), "A delayed lookup must revalidate the declaration on the worker");
            } else {
                assertEquals(resolved.getDocumentation().getRight().getKind(), "markdown");
                assertTrue(resolved.getDocumentation().getRight().getValue().contains("cold database documentation"));
            }
        } finally {
            worker.stop();
            // Drain any initialization task even when a deliberate regression makes resolve return early.
            JassDocService.getInstance().documentationForAsync(new JassDocService.LookupKey(symbol,
                    JassDocService.SymbolKind.FUNCTION, "common.j")).get(5, TimeUnit.SECONDS);
            JassDocService.getInstance().clearCacheForTests();
            if (previous == null) {
                System.clearProperty("WURST_JASSDOC_DB_PATH");
            } else {
                System.setProperty("WURST_JASSDOC_DB_PATH", previous);
            }
            try (var paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
    }

    @Test
    public void failedModelInitializationRejectsRequests() throws Exception {
        LanguageWorker worker = new LanguageWorker();
        AtomicInteger failures = new AtomicInteger();
        worker.setLanguageClient((org.eclipse.lsp4j.services.LanguageClient) java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{org.eclipse.lsp4j.services.LanguageClient.class},
                (proxy, method, args) -> null));
        worker.setInitialBuildListener(success -> { assertFalse(success); failures.incrementAndGet(); });
        try {
            // Exercise initialization failure without adding a production-only factory hook.
            var init = LanguageWorker.class.getDeclaredMethod("doInit", WFile.class);
            init.setAccessible(true);
            init.invoke(worker, new Object[]{null});
            var request = worker.handle(new UserRequest<Boolean>() {
                @Override
                public Boolean execute(ModelManager modelManager) { throw new AssertionError("failed initialization must not run requests"); }
            });
            expectThrows(ExecutionException.class, () -> request.get(2, TimeUnit.SECONDS));
            assertEquals(failures.get(), 1);
        } finally {
            worker.stop();
        }
    }

    @Test
    public void initialBuildReportsCompletionOnlyAfterBuildReturns() throws Exception {
        Path tmp = Files.createTempDirectory("wurst-lw-initial-build");
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicReference<Boolean> completed = new AtomicReference<>();
        LanguageWorker worker = new LanguageWorker();
        worker.modelManager = new CountingModelManager(tmp.toFile()) {
            @Override
            public void buildProject() {
                entered.countDown();
                try {
                    assertTrue(release.await(5, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        };
        worker.setInitialBuildListener(completed::set);
        try {
            worker.setRootPath(WFile.create(tmp.toFile()));
            assertTrue(entered.await(2, TimeUnit.SECONDS));
            assertEquals(completed.get(), null, "readiness must not precede the full build");
            release.countDown();
            assertTrue(waitUntil(() -> completed.get() != null, 2000));
            assertEquals(completed.get(), Boolean.TRUE);
        } finally {
            release.countDown();
            worker.stop();
        }
    }

    @Test
    public void initialBuildExceptionReportsFailure() throws Exception {
        Path tmp = Files.createTempDirectory("wurst-lw-initial-failure");
        AtomicReference<Boolean> completed = new AtomicReference<>();
        LanguageWorker worker = new LanguageWorker();
        worker.modelManager = new CountingModelManager(tmp.toFile()) {
            @Override
            public void buildProject() { throw new IllegalStateException("initial build failed"); }
        };
        worker.setInitialBuildListener(completed::set);
        try {
            worker.setRootPath(WFile.create(tmp.toFile()));
            assertTrue(waitUntil(() -> completed.get() != null, 2000));
            assertEquals(completed.get(), Boolean.FALSE, "an exception must not report readiness");
        } finally {
            worker.stop();
        }
    }

    @Test
    public void watcherChangedForOpenFileIsIgnored() throws Exception {
        Path tmp = Files.createTempDirectory("wurst-lw-open");
        File file = tmp.resolve("wurst").resolve("Main.wurst").toFile();
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), "package Main\n");
        WFile wFile = WFile.create(file);

        LanguageWorker worker = new LanguageWorker();
        CountingModelManager mm = new CountingModelManager(tmp.toFile());
        worker.modelManager = mm;

        try {
            worker.handleOpen(new DidOpenTextDocumentParams(new TextDocumentItem(
                wFile.getUriString(),
                "wurst",
                1,
                "package Main\n"
            )));
            assertTrue(waitUntil(() -> mm.syncContentCalls.get() >= 1, 2000), "open should trigger reconcile");

            mm.syncFileCalls.set(0);

            worker.handleFileChanged(new DidChangeWatchedFilesParams(Collections.singletonList(
                new FileEvent(wFile.getUriString(), FileChangeType.Changed)
            )));

            Thread.sleep(250);
            assertEquals(mm.syncFileCalls.get(), 0, "watcher changed event must not resync open files");
        } finally {
            worker.stop();
        }
    }

    @Test
    public void watcherChangedForMissingFileRemovesCompilationUnit() throws Exception {
        Path tmp = Files.createTempDirectory("wurst-lw-missing-file");
        File wurstFolder = tmp.resolve("wurst").toFile();
        //noinspection ResultOfMethodCallIgnored
        wurstFolder.mkdirs();
        Files.writeString(tmp.resolve("wurst").resolve("Wurst.wurst"), "package Wurst\nendpackage\n");
        File packageFile = tmp.resolve("wurst").resolve("Unit_config.wurst").toFile();
        File mainFile = tmp.resolve("wurst").resolve("Main.wurst").toFile();
        Files.writeString(packageFile.toPath(), "package Unit_config\nendpackage\n");
        Files.writeString(mainFile.toPath(), "package Main\nimport Unit_config\nendpackage\n");
        WFile wFile = WFile.create(packageFile);
        WFile mainWFile = WFile.create(mainFile);

        LanguageWorker worker = new LanguageWorker();
        ModelManagerImpl mm = new ModelManagerImpl(tmp.toFile(), worker.getBufferManager());
        AtomicReference<String> mainDiagnostics = new AtomicReference<>("");
        mm.onCompilationResult(params -> {
            if (WFile.create(params.getUri()).equals(mainWFile)) {
                mainDiagnostics.set(params.getDiagnostics().stream()
                    .map(Diagnostic::getMessage)
                    .collect(Collectors.joining("\n")));
            }
        });
        mm.buildProject();
        assertFalse(hasMissingImportDiagnostic(mainDiagnostics.get(), "Unit_config"),
            "fixture import should resolve before the package file is removed: " + mainDiagnostics.get());
        worker.modelManager = mm;

        try {
            Files.delete(packageFile.toPath());
            worker.handleFileChanged(new DidChangeWatchedFilesParams(Collections.singletonList(
                new FileEvent(wFile.getUriString(), FileChangeType.Changed)
            )));

            assertTrue(waitUntil(() -> mm.getCompilationUnit(wFile) == null, 2000),
                "watcher update for a missing file should remove its compilation unit");
            assertTrue(waitUntil(() -> hasMissingImportDiagnostic(mainDiagnostics.get(), "Unit_config"), 8000),
                "the dependent missing-import diagnostic should be reported after reconciliation");
        } finally {
            worker.stop();
        }
    }

    @Test
    public void watcherChangedForMissingJassFileRechecksDependents() throws Exception {
        Path tmp = Files.createTempDirectory("wurst-lw-missing-jass");
        File wurstFolder = tmp.resolve("wurst").toFile();
        //noinspection ResultOfMethodCallIgnored
        wurstFolder.mkdirs();
        Files.writeString(tmp.resolve("wurst").resolve("Wurst.wurst"), "package Wurst\nendpackage\n");
        File jassFile = tmp.resolve("wurst").resolve("helpers.j").toFile();
        File mainFile = tmp.resolve("wurst").resolve("Main.wurst").toFile();
        Files.writeString(jassFile.toPath(),
            "function watcherJassHelper takes nothing returns nothing\nendfunction\n");
        Files.writeString(mainFile.toPath(),
            "package Main\ninit\n    watcherJassHelper()\nendpackage\n");
        WFile wFile = WFile.create(jassFile);
        WFile mainWFile = WFile.create(mainFile);

        LanguageWorker worker = new LanguageWorker();
        ModelManagerImpl mm = new ModelManagerImpl(tmp.toFile(), worker.getBufferManager());
        AtomicReference<String> mainDiagnostics = new AtomicReference<>("");
        mm.onCompilationResult(params -> {
            if (WFile.create(params.getUri()).equals(mainWFile)) {
                mainDiagnostics.set(params.getDiagnostics().stream()
                    .map(Diagnostic::getMessage)
                    .collect(Collectors.joining("\n")));
            }
        });
        mm.buildProject();
        assertFalse(mainDiagnostics.get().contains("watcherJassHelper"),
            "fixture Jass function should resolve before the file is removed");
        worker.modelManager = mm;

        try {
            Files.delete(jassFile.toPath());
            worker.handleFileChanged(new DidChangeWatchedFilesParams(Collections.singletonList(
                new FileEvent(wFile.getUriString(), FileChangeType.Changed)
            )));

            assertTrue(waitUntil(() -> mm.getCompilationUnit(wFile) == null, 2000),
                "watcher update for a missing Jass file should remove its compilation unit");
            assertTrue(waitUntil(() -> mainDiagnostics.get().contains("watcherJassHelper"), 8000),
                "dependent Wurst files should be fully rechecked after a Jass file is removed; diagnostics: "
                    + mainDiagnostics.get() + "; first model error: " + mm.getFirstErrorDescription());
        } finally {
            worker.stop();
        }
    }

    @Test
    public void dependencyDidChangeUsesIncrementalSync() throws Exception {
        Path tmp = Files.createTempDirectory("wurst-lw-dep-reconcile");
        File file = tmp.resolve("_build").resolve("dependencies").resolve("depA").resolve("wurst").resolve("Lib.wurst").toFile();
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), "package Lib\n");
        WFile wFile = WFile.create(file);

        LanguageWorker worker = new LanguageWorker();
        CountingModelManager mm = new CountingModelManager(tmp.toFile());
        worker.modelManager = mm;

        try {
            worker.handleOpen(new DidOpenTextDocumentParams(new TextDocumentItem(
                wFile.getUriString(),
                "wurst",
                1,
                "package Lib\n"
            )));

            assertTrue(waitUntil(() -> mm.syncContentCalls.get() >= 1, 2000), "dependency open should sync content");

            VersionedTextDocumentIdentifier td = new VersionedTextDocumentIdentifier(wFile.getUriString(), 2);
            TextDocumentContentChangeEvent ch = new TextDocumentContentChangeEvent();
            ch.setText("package Lib\n// edit\n");
            worker.handleChange(new DidChangeTextDocumentParams(td, Collections.singletonList(ch)));

            assertTrue(waitUntil(() -> mm.syncContentCalls.get() >= 1, 2000), "dependency didChange should sync content");
            assertEquals(mm.cleanCalls.get(), 0, "dependency didChange must not clean model");
        } finally {
            worker.stop();
        }
    }

    @Test
    public void dependencyWatcherChangedSyncsIncrementally() throws Exception {
        Path tmp = Files.createTempDirectory("wurst-lw-dep-changed");
        File file = tmp.resolve("_build").resolve("dependencies").resolve("depB").resolve("wurst").resolve("Lib.wurst").toFile();
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), "package Lib\n");
        WFile wFile = WFile.create(file);

        LanguageWorker worker = new LanguageWorker();
        CountingModelManager mm = new CountingModelManager(tmp.toFile());
        worker.modelManager = mm;

        try {
            worker.handleFileChanged(new DidChangeWatchedFilesParams(Collections.singletonList(
                new FileEvent(wFile.getUriString(), FileChangeType.Changed)
            )));

            assertTrue(waitUntil(() -> mm.syncDependencyCalls.get() >= 1, 2000), "dependency watcher changes should sync dependency state");
            assertEquals(mm.cleanCalls.get(), 0, "dependency watcher changes should not clean");
            assertEquals(mm.buildCalls.get(), 0, "dependency watcher changes should not full rebuild");
        } finally {
            worker.stop();
        }
    }

    @Test
    public void closingDependencyFileDoesNotTriggerRebuild() throws Exception {
        Path tmp = Files.createTempDirectory("wurst-lw-dep-close");
        File file = tmp.resolve("_build").resolve("dependencies").resolve("depC").resolve("wurst").resolve("Lib.wurst").toFile();
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), "package Lib\n");
        WFile wFile = WFile.create(file);

        LanguageWorker worker = new LanguageWorker();
        CountingModelManager mm = new CountingModelManager(tmp.toFile());
        worker.modelManager = mm;

        try {
            worker.handleOpen(new DidOpenTextDocumentParams(new TextDocumentItem(
                wFile.getUriString(),
                "wurst",
                1,
                "package Lib\n"
            )));
            assertTrue(waitUntil(() -> mm.syncContentCalls.get() >= 1, 2000), "open should sync content");

            worker.handleClose(new DidCloseTextDocumentParams(new TextDocumentIdentifier(wFile.getUriString())));

            Thread.sleep(250);
            assertEquals(mm.cleanCalls.get(), 0, "closing dependency file must not clean");
            assertEquals(mm.buildCalls.get(), 0, "closing dependency file must not rebuild");
            assertTrue(mm.syncFileCalls.get() <= 1, "closing dependency file should only use normal sync path");
        } finally {
            worker.stop();
        }
    }

    @Test
    public void closingCoreBuildJassFileDoesNotTriggerUpdate() throws Exception {
        Path tmp = Files.createTempDirectory("wurst-lw-commonj-close");
        File file = tmp.resolve("_build").resolve("common.j").toFile();
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), "globals\nendglobals\n");
        WFile wFile = WFile.create(file);

        LanguageWorker worker = new LanguageWorker();
        CountingModelManager mm = new CountingModelManager(tmp.toFile());
        worker.modelManager = mm;

        try {
            worker.handleOpen(new DidOpenTextDocumentParams(new TextDocumentItem(
                wFile.getUriString(),
                "jass",
                1,
                "globals\nendglobals\n"
            )));
            assertTrue(waitUntil(() -> mm.syncContentCalls.get() >= 1, 2000), "opening common.j should sync content");

            worker.handleClose(new DidCloseTextDocumentParams(new TextDocumentIdentifier(wFile.getUriString())));

            Thread.sleep(250);
            assertTrue(mm.syncFileCalls.get() <= 1, "closing common.j should only use normal sync path");
            assertEquals(mm.cleanCalls.get(), 0, "closing common.j must not clean");
            assertEquals(mm.buildCalls.get(), 0, "closing common.j must not rebuild");
        } finally {
            worker.stop();
        }
    }

    @Test
    public void saveTriggersImmediateReconcile() throws Exception {
        Path tmp = Files.createTempDirectory("wurst-lw-save-reconcile");
        File file = tmp.resolve("wurst").resolve("Main.wurst").toFile();
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), "package Main\n");
        WFile wFile = WFile.create(file);

        LanguageWorker worker = new LanguageWorker();
        CountingModelManager mm = new CountingModelManager(tmp.toFile());
        worker.modelManager = mm;

        try {
            worker.handleOpen(new DidOpenTextDocumentParams(new TextDocumentItem(
                wFile.getUriString(), "wurst", 1, "package Main\n"
            )));
            assertTrue(waitUntil(() -> mm.syncContentCalls.get() >= 1, 2000), "open should sync content");

            VersionedTextDocumentIdentifier td = new VersionedTextDocumentIdentifier(wFile.getUriString(), 2);
            TextDocumentContentChangeEvent ch = new TextDocumentContentChangeEvent();
            ch.setText("package Main\n// edit\n");
            worker.handleChange(new DidChangeTextDocumentParams(td, Collections.singletonList(ch)));
            assertTrue(waitUntil(() -> mm.syncContentCalls.get() >= 2, 2000), "change should sync content");

            mm.reconcileCalls.set(0);
            long saveStart = System.currentTimeMillis();
            worker.handleSave(new DidSaveTextDocumentParams(new TextDocumentIdentifier(wFile.getUriString())));

            assertTrue(waitUntil(() -> mm.reconcileCalls.get() >= 1, 800), "save should trigger fast reconcile");
            long elapsed = System.currentTimeMillis() - saveStart;
            assertTrue(elapsed < 1000, "save reconcile should not wait for long debounce");
        } finally {
            worker.stop();
        }
    }

    private static boolean waitUntil(BooleanSupplier condition, long timeoutMs) throws InterruptedException {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (condition.getAsBoolean()) {
                return true;
            }
            Thread.sleep(20);
        }
        return condition.getAsBoolean();
    }

    private static boolean hasMissingImportDiagnostic(String diagnostics, String packageName) {
        return diagnostics.contains("import '" + packageName + "' could not be resolved")
            || diagnostics.contains("Could not find imported package " + packageName);
    }

    private static class CountingModelManager implements ModelManager {
        @Override
        public void reportBuildDiagnostics(List<CompileError> diagnostics) {
            throw new UnsupportedOperationException("This worker test does not run map builds.");
        }
        final AtomicInteger cleanCalls = new AtomicInteger();
        final AtomicInteger buildCalls = new AtomicInteger();
        final AtomicInteger syncFileCalls = new AtomicInteger();
        final AtomicInteger syncContentCalls = new AtomicInteger();
        final AtomicInteger refreshDependencyCalls = new AtomicInteger();
        final AtomicInteger syncDependencyCalls = new AtomicInteger();
        final AtomicInteger reconcileCalls = new AtomicInteger();
        final File projectPath;

        private CountingModelManager(File projectPath) {
            this.projectPath = projectPath;
        }

        @Override
        public Changes removeCompilationUnit(WFile filename) {
            return Changes.empty();
        }

        @Override
        public void retainCompilationUnits(WurstModel model, Predicate<CompilationUnit> keep) {
        }

        @Override
        public void clean() {
            cleanCalls.incrementAndGet();
        }

        @Override
        public List<CompileError> getParseErrors() {
            return Collections.emptyList();
        }

        @Override
        public void onCompilationResult(Consumer<PublishDiagnosticsParams> f) {
        }

        @Override
        public void buildProject() {
            buildCalls.incrementAndGet();
        }

        @Override
        public void loadProject() {
            throw new UnsupportedOperationException("The worker builds the project in one go.");
        }

        @Override
        public void checkProject(WurstGui gui) {
            throw new UnsupportedOperationException("The worker builds the project in one go.");
        }

        @Override
        public void refreshDependencies() {
            refreshDependencyCalls.incrementAndGet();
        }

        @Override
        public Changes syncDependencyCompilationUnits() {
            syncDependencyCalls.incrementAndGet();
            return Changes.empty();
        }

        @Override
        public Changes syncCompilationUnit(WFile changedFilePath) {
            syncFileCalls.incrementAndGet();
            return Changes.empty();
        }

        @Override
        public Changes syncCompilationUnitContent(WFile filename, String contents) {
            syncContentCalls.incrementAndGet();
            return new Changes(Collections.singletonList(filename), Collections.emptyList());
        }

        @Override
        public CompilationUnit replaceCompilationUnitContent(WFile filename, String buffer, boolean reportErrors) {
            return null;
        }

        @Override
        public Set<File> getDependencyWurstFiles() {
            return Collections.emptySet();
        }

        @Override
        public CompilationUnit getCompilationUnit(WFile filename) {
            return null;
        }

        @Override
        public WurstModel getModel() {
            return null;
        }

        @Override
        public boolean hasErrors() {
            return false;
        }

        @Override
        public File getProjectPath() {
            return projectPath;
        }

        @Override
        public String getFirstErrorDescription() {
            return "";
        }

        @Override
        public void reconcile(Changes changes) {
            reconcileCalls.incrementAndGet();
        }
    }
}
