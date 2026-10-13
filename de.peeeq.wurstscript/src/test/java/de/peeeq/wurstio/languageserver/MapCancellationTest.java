package de.peeeq.wurstio.languageserver;

import java.io.StringReader;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstio.CompiletimeFunctionRunner;
import org.wurstscript.projectconfig.WurstProjectConfigData;
import de.peeeq.wurstio.languageserver.requests.MapRequest;
import de.peeeq.wurstio.languageserver.requests.UserRequest;
import de.peeeq.wurstscript.intermediatelang.interpreter.RunStatement;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.utils.CompilationCancellation;
import org.eclipse.lsp4j.WorkDoneProgressCancelParams;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.lsp4j.services.LanguageClient;
import org.testng.annotations.Test;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.*;

public class MapCancellationTest {
    @Test
    public void progressCancellationWaitsForCleanupAndKeepsWorkerUsable() throws Exception {
        exerciseCancellation(false);
    }

    @Test
    public void rpcCancellationReachesRunningRequestWithoutReleasingMapGuard() throws Exception {
        exerciseCancellation(true);
    }

    private void exerciseCancellation(boolean rpc) throws Exception {
        var project = Files.createTempDirectory("wurst-map-cancellation");
        Files.writeString(project.resolve("wurst.build"), "projectName: Test\nwc3Patch: v2.0\n");
        WurstLanguageServer server = new WurstLanguageServer();
        LanguageWorker worker = server.worker();
        AtomicInteger messages = new AtomicInteger();
        worker.setLanguageClient((LanguageClient) Proxy.newProxyInstance(LanguageClient.class.getClassLoader(),
            new Class<?>[]{LanguageClient.class}, (proxy, method, args) -> {
                if (method.getName().equals("showMessage")) messages.incrementAndGet();
                return null;
            }));
        var field = LanguageWorker.class.getDeclaredField("modelManager");
        field.setAccessible(true);
        field.set(worker, new ModelManagerImpl(project.toFile(), new BufferManager()));
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch cleanup = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        MapRequest request = new MapRequest(server, Optional.empty(), List.of(), WFile.create(project),
            Optional.empty(), Optional.empty()) {
            @Override
            public Object execute(ModelManager manager) {
                entered.countDown();
                try {
                    while (true) {
                        CompilationCancellation.check();
                        Thread.onSpinWait();
                    }
                } finally {
                    cleanup.countDown();
                    try {
                        if (!release.await(5, TimeUnit.SECONDS)) throw new AssertionError("cleanup timed out");
                    } catch (InterruptedException e) {
                        throw new AssertionError("map cancellation must not interrupt the shared worker", e);
                    }
                }
            }
        };
        request.setWorkDoneToken(Either.forLeft("map-42"));
        try {
            var response = worker.handle(request);
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            var wrong = new WorkDoneProgressCancelParams();
            wrong.setToken(Either.forLeft("other-map"));
            server.cancelProgress(wrong);
            assertEquals(cleanup.getCount(), 1L);
            if (rpc) response.cancel(true);
            else {
                var cancel = new WorkDoneProgressCancelParams();
                cancel.setToken(request.getWorkDoneToken());
                server.cancelProgress(cancel);
            }
            assertTrue(cleanup.await(5, TimeUnit.SECONDS));
            if (!rpc) assertFalse(response.isDone(), "progress cancellation waits for cleanup");
            var overlap = worker.handle(request);
            assertTrue(overlap.isCompletedExceptionally(), "map guard stays reserved through cleanup");
            release.countDown();
            request.getFuture().handle((result, error) -> null).get(5, TimeUnit.SECONDS);
            if (!rpc) {
                ExecutionException failure = expectThrows(ExecutionException.class, () -> response.get(5, TimeUnit.SECONDS));
                assertTrue(failure.getCause() instanceof ResponseErrorException);
                assertEquals(((ResponseErrorException) failure.getCause()).getResponseError().getCode(),
                    ResponseErrorCode.RequestCancelled.getValue());
            }
            assertEquals(messages.get(), 0, "cancellation is not a build error");
            assertTrue(worker.handle(new UserRequest<Boolean>() {
                @Override
                public Boolean execute(ModelManager manager) {
                    CompilationCancellation.check();
                    return !Thread.currentThread().isInterrupted();
                }
            }).get(5, TimeUnit.SECONDS), "the next request must run normally");
        } finally {
            release.countDown();
            request.cancel();
            worker.stop();
        }
    }

    @Test
    public void canceledQueuedMapNeverExecutesAndScopeDoesNotLeak() throws Exception {
        var project = Files.createTempDirectory("wurst-queued-map-cancel");
        Files.writeString(project.resolve("wurst.build"), "projectName: Test\nwc3Patch: v2.0\n");
        AtomicInteger executions = new AtomicInteger();
        MapRequest request = new MapRequest(null, Optional.empty(), List.of(), WFile.create(project),
            Optional.empty(), Optional.empty()) {
            @Override
            public Object execute(ModelManager manager) { executions.incrementAndGet(); return "ok"; }
        };
        LanguageWorker worker = new LanguageWorker();
        request.setWorkDoneToken(Either.forLeft("early-cancel"));
        try {
            worker.cancelMapRequest(request.getWorkDoneToken());
            var response = worker.handle(request);
            assertFalse(request.getFuture().isDone());
            request.run(null);
            expectThrows(ExecutionException.class, () -> response.get(5, TimeUnit.SECONDS));
            assertTrue(request.getFuture().isCompletedExceptionally());
            assertEquals(executions.get(), 0);
            CompilationCancellation.check();
        } finally {
            worker.stop();
        }
    }

    @Test
    public void emptyInterpreterLoopChecksCancellation() {
        AtomicInteger checks = new AtomicInteger();
        expectThrows(CancellationException.class, () -> CompilationCancellation.run(
            () -> checks.incrementAndGet() == 3,
            () -> RunStatement.run(JassIm.ImLoop(Ast.NoExpr(), JassIm.ImStmts()), null, null)));
        assertEquals(checks.get(), 3);
        CompilationCancellation.check();
    }

    @Test
    public void compiletimeCancellationIsNotReportedAsInterpreterFailure() {
        var gui = new WurstGuiLogger();
        var compiler = new WurstCompilerJassImpl(null, gui, null,
            new RunArgs("-runcompiletimefunctions"));
        compiler.loadReader("Main.wurst", new StringReader(
            "package Main\n@compiletime\nfunction spin()\n    while true\n        skip\n"));
        var model = compiler.parseFiles();
        assertTrue(compiler.checkProg(model), gui.getErrors());
        compiler.translateProgToIm(model);
        AtomicInteger checks = new AtomicInteger();
        try (var runner = new CompiletimeFunctionRunner(compiler.getImTranslator(),
            compiler.getImProg(), Optional.empty(), null, gui,
            CompiletimeFunctionRunner.FunctionFlagToRun.CompiletimeFunctions,
            WurstProjectConfigData.empty(), false, false)) {
            expectThrows(CancellationException.class, () -> CompilationCancellation.run(
                () -> checks.incrementAndGet() >= 5, runner::run));
            assertEquals(gui.getErrorCount(), 0, "cancel must not leave bogus Problems diagnostics");
            assertTrue(runner.getFailTests().isEmpty(), "cancel is not a failed compiletime function");
        }
        CompilationCancellation.check();
    }

    @Test
    public void cancelDuringOutputPublicationStillFinalizesTheMap() throws Exception {
        var project = Files.createTempDirectory("wurst-cancel-map-finalization");
        Files.writeString(project.resolve("wurst.build"), "projectName: Test\nwc3Patch: v2.0\n");
        var cached = project.resolve("cached.w3x").toFile();
        Files.copy(java.nio.file.Path.of("testscripts/mpq/test.w3x"), cached.toPath());
        try (var mpq = de.peeeq.wurstio.mpq.MpqEditorFactory.getEditor(Optional.of(cached))) {
            mpq.insertFile("wurst_cache_manifest.txt", new byte[]{1});
            mpq.insertFile("wurst_object_cache.txt", new byte[]{2});
        }
        java.util.concurrent.atomic.AtomicBoolean canceled = new java.util.concurrent.atomic.AtomicBoolean();
        java.io.File[] output = new java.io.File[1];
        class Request extends MapRequest {
            Request() {
                super(null, Optional.of(cached), List.of(), WFile.create(project), Optional.empty(), Optional.empty());
            }

            @Override
            public Object execute(ModelManager manager) { return null; }

            @Override
            protected java.io.File getBuildDir() { return project.toFile(); }

            @Override
            protected java.io.File getCachedMapFile() { return cached; }

            @Override
            protected CompilationResult compileScript(ModelManager manager, de.peeeq.wurstscript.gui.WurstGui gui,
                Optional<java.io.File> map, WurstProjectConfigData config, java.io.File buildDir, boolean production) {
                return new CompilationResult();
            }

            @Override
            protected void injectMapData(de.peeeq.wurstscript.gui.WurstGui gui, Optional<java.io.File> map,
                CompilationResult result) {}

            @Override
            protected java.io.File ensureWritableBuildOutput(java.io.File target, boolean finalWrite) {
                output[0] = target;
                return target;
            }

            void build(de.peeeq.wurstscript.gui.WurstGui gui) throws Exception {
                executeBuildMapPipeline(null, gui, WurstProjectConfigData.empty());
            }
        }
        Request request = new Request();
        var gui = new WurstGuiLogger() {
            @Override
            public void sendProgress(String message) {
                if (message.equals("Finalizing map")) canceled.set(true);
                CompilationCancellation.check();
            }
        };
        expectThrows(CancellationException.class, () -> CompilationCancellation.run(canceled::get, () -> {
            try { request.build(gui); }
            catch (CancellationException e) { throw e; }
            catch (Exception e) { throw new RuntimeException(e); }
        }));
        try (var mpq = de.peeeq.wurstio.mpq.MpqEditorFactory.getEditor(Optional.of(output[0]))) {
            assertFalse(mpq.hasFile("wurst_cache_manifest.txt"), "cancellation must not publish an unfinalized map");
            assertFalse(mpq.hasFile("wurst_object_cache.txt"));
            assertTrue(mpq.hasFile("war3map.j"), "the finalized map must remain readable");
        }
        CompilationCancellation.check();
    }

    @Test
    public void mapTestExecutorInheritsCancellationWithoutReportingFailedTests() {
        var gui = new WurstGuiLogger();
        var compiler = new WurstCompilerJassImpl(null, gui, null, new RunArgs());
        compiler.loadReader("Main.wurst", new StringReader(
            "package Main\n@test\nfunction spin()\n    while true\n        skip\n"));
        var model = compiler.parseFiles();
        assertTrue(compiler.checkProg(model), gui.getErrors());
        compiler.translateProgToIm(model);
        Thread owner = Thread.currentThread();
        AtomicInteger schedulerChecks = new AtomicInteger();
        var tests = new de.peeeq.wurstio.languageserver.requests.RunTests(Optional.empty(), 0, 0, Optional.empty(), 2);
        expectThrows(CancellationException.class, () -> CompilationCancellation.run(
            () -> Thread.currentThread() != owner && schedulerChecks.incrementAndGet() >= 5,
            () -> tests.runTests(compiler.getImTranslator(), compiler.getImProg(), Optional.empty(), Optional.empty())));
        assertTrue(schedulerChecks.get() >= 5, "the executor must observe the captured cancellation signal");
        assertTrue(tests.getFailTests().isEmpty(), "cancellation must not be recorded as a failed test");
        CompilationCancellation.check();
    }

}
