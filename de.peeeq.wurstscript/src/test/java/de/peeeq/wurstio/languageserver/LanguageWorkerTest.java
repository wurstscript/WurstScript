package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import org.eclipse.lsp4j.*;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class LanguageWorkerTest {

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

    private static class CountingModelManager implements ModelManager {
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
