package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstio.Pjass;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.languageserver.requests.BuildMap;
import de.peeeq.wurstio.languageserver.requests.RequestFailedException;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressReport;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.testng.annotations.Test;

import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.testng.Assert.*;

public class BuildDiagnosticsTests {
    @Test
    public void buildWarningsFromDependenciesRemainVisibleWhileOrdinaryWarningsStaySuppressed() {
        WurstGuiLogger gui = new WurstGuiLogger();
        WPos dependency = new WPos("_build/dependencies/Library/Main.wurst", LineOffsets.dummy, 0, 4);
        CompileError ordinary = new CompileError(dependency, "Unused variable", CompileError.ErrorType.WARNING);
        CompileError compatibility = new CompileError(dependency, "Jass save/load compatibility", CompileError.ErrorType.WARNING);
        gui.sendError(ordinary);
        assertTrue(gui.getWarningList().isEmpty());
        gui.sendBuildDiagnostic(compatibility);
        assertEquals(gui.getWarningList(), List.of(compatibility));
        assertEquals(gui.getErrorCount(), 0);
    }

    @Test
    public void tokenRequestsLetEditorPresentFailuresAndPreserveInformationalMessages() throws Exception {
        Path project = Files.createTempDirectory("wurst-build-failure");
        Files.writeString(project.resolve("wurst.build"), "projectName: Test\nwc3Patch: v2.0\n");
        BuildMap request = new BuildMap(null, WFile.create(project.toFile()), Optional.empty(), Optional.empty(), List.of());
        List<MessageParams> messages = new ArrayList<>();
        LanguageClient client = (LanguageClient) Proxy.newProxyInstance(LanguageClient.class.getClassLoader(),
            new Class<?>[]{LanguageClient.class}, (proxy, method, args) -> {
                if (method.getName().equals("showMessage")) {
                    messages.add((MessageParams) args[0]);
                }
                return null;
            });
        RequestFailedException error = new RequestFailedException(MessageType.Error, "Build failed");
        CompletableFuture<Object> legacy = new CompletableFuture<>();
        request.handleException(client, error, legacy);
        assertFalse(legacy.isCompletedExceptionally());
        assertEquals(messages.size(), 1);

        request.setWorkDoneToken(Either.forLeft("build-42"));
        CompletableFuture<Object> editor = new CompletableFuture<>();
        request.handleException(client, error, editor);
        assertTrue(editor.isCompletedExceptionally());
        assertEquals(messages.size(), 1, "the editor must receive only one failure notification");

        CompletableFuture<Object> warningFailure = new CompletableFuture<>();
        request.handleException(client, new RequestFailedException(MessageType.Warning, "On-disk compilation failed"), warningFailure);
        assertTrue(warningFailure.isCompletedExceptionally(), "a warning-level failure must not report a successful build");
        assertEquals(messages.size(), 1, "the editor owns the failure notification");

        CompletableFuture<Object> cancelled = new CompletableFuture<>();
        request.handleException(client, new RequestFailedException(MessageType.Info, "Run canceled."), cancelled);
        assertFalse(cancelled.isCompletedExceptionally());
        assertEquals(messages.get(1).getType(), MessageType.Info);
    }

    @Test
    public void editorProgressUsesRequestTokenAndEndsOnlyOnce() throws Exception {
        Path project = Files.createTempDirectory("wurst-lsp-progress");
        ModelManagerImpl manager = new ModelManagerImpl(project.toFile(), new BufferManager());
        List<ProgressParams> progress = new ArrayList<>();
        LanguageClient client = (LanguageClient) Proxy.newProxyInstance(LanguageClient.class.getClassLoader(),
            new Class<?>[]{LanguageClient.class}, (proxy, method, args) -> {
                if (method.getName().equals("notifyProgress")) {
                    progress.add((ProgressParams) args[0]);
                }
                return null;
            });
        Either<String, Integer> token = Either.forLeft("build-42");
        WurstGuiLsp gui = new WurstGuiLsp(manager, client, token, "Building Wurst map");
        gui.sendProgress("Running PJass");
        gui.sendError(new CompileError(new WPos("Main.wurst", LineOffsets.dummy, 0, 1), "Validation failed"));
        gui.sendFinished();
        gui.sendFinished();
        gui.sendProgress("Late progress");
        assertEquals(progress.size(), 3);
        for (ProgressParams event : progress) {
            assertEquals(event.getToken(), token);
        }
        WorkDoneProgressBegin begin = (WorkDoneProgressBegin) progress.get(0).getValue().getLeft();
        assertEquals(begin.getTitle(), "Building Wurst map");
        assertEquals(begin.getCancellable(), Boolean.FALSE);
        assertNull(begin.getPercentage());
        WorkDoneProgressReport report = (WorkDoneProgressReport) progress.get(1).getValue().getLeft();
        assertEquals(report.getMessage(), "Running PJass");
        assertNull(report.getPercentage());
        assertTrue(progress.get(2).getValue().getLeft() instanceof WorkDoneProgressEnd);

        new WurstGuiLsp(manager, client).sendFinished();
        assertEquals(progress.size(), 3, "clients without tokens keep log-only progress");
    }

    @Test
    public void editorGuiPublishesErrorsAndClearsThemForTheNextBuild() throws Exception {
        Path project = Files.createTempDirectory("wurst-lsp-build-gui");
        ModelManagerImpl manager = new ModelManagerImpl(project.toFile(), new BufferManager());
        List<PublishDiagnosticsParams> reports = new ArrayList<>();
        manager.onCompilationResult(reports::add);
        LanguageClient client = (LanguageClient) Proxy.newProxyInstance(LanguageClient.class.getClassLoader(),
            new Class<?>[]{LanguageClient.class}, (proxy, method, args) -> null);
        WurstGuiLsp gui = new WurstGuiLsp(manager, client);
        WPos position = new WPos(project.resolve("Main.wurst").toString(), LineOffsets.dummy, 0, 4);
        gui.sendError(new CompileError(position, "Generated Jass failed validation"));
        gui.sendError(new CompileError(position, "Save/load compatibility", CompileError.ErrorType.WARNING));
        gui.sendFinished();

        assertEquals(reports.size(), 1);
        assertEquals(reports.get(0).getDiagnostics().size(), 2);
        assertEquals(reports.get(0).getDiagnostics().get(0).getSeverity(), DiagnosticSeverity.Error);
        assertEquals(reports.get(0).getDiagnostics().get(1).getSeverity(), DiagnosticSeverity.Warning);
        assertFalse(manager.hasErrors(), "build errors must not prevent another build of the editor model");

        WurstGuiLsp nextBuild = new WurstGuiLsp(manager, client);
        nextBuild.sendFinished();
        assertEquals(reports.size(), 2);
        assertTrue(reports.get(1).getDiagnostics().isEmpty());
    }

    @Test
    public void pjassDoesNotMapBaseFileErrorsToWurstAndKeepsGeneratedFallbackLocations() throws Exception {
        Path dir = Files.createTempDirectory("wurst-pjass-locations");
        Path jass = dir.resolve("output.j");
        Path common = dir.resolve("common.j");
        Files.writeString(jass, "function main takes nothing returns nothing\ncall missing()\nendfunction\n");
        Files.writeString(common, "native invalid takes nothing returns missing\n");
        Pjass.Result result = new Pjass.Result(jass.toFile(), false,
            common + ":1: Undefined type missing\n" + jass + ":2: Undefined function missing\n");
        WPos original = new WPos(dir.resolve("Main.wurst").toString(), LineOffsets.dummy, 0, 4);
        List<CompileError> mapped = result.getDiagnostics(Map.of(1, original));

        assertEquals(mapped.size(), 2);
        assertEquals(mapped.get(0).getSource().getFile(), common.toString());
        assertEquals(mapped.get(0).getSource().getLine(), 1);
        assertEquals(mapped.get(1).getSource().getFile(), jass.toString());
        assertEquals(mapped.get(1).getSource().getLine(), 2);
        assertEquals(mapped.get(1).getSource().getStartColumn(), 1);
        assertSame(result.getDiagnostics(Map.of(2, original)).get(1).getSource(), original);
    }

    @Test
    public void pjassWarningsUseOriginalSourceAndWarningSeverity() throws Exception {
        Path dir = Files.createTempDirectory("wurst-build-diagnostics");
        Path jass = dir.resolve("output.j");
        Files.writeString(jass, "function main takes nothing returns nothing\n"
            + "local string s = \"" + "x".repeat(1024) + "\"\nendfunction\n");
        WPos original = new WPos(dir.resolve("Main.wurst").toString(), LineOffsets.dummy, 4, 12);
        Pjass.Result result = Pjass.runPjass(jass.toFile());
        List<CompileError> diagnostics = result.getDiagnostics(Map.of(2, original));

        assertEquals(diagnostics.size(), 1);
        assertSame(diagnostics.get(0).getSource(), original);
        assertEquals(diagnostics.get(0).getErrorType(), CompileError.ErrorType.WARNING);
        assertFalse(diagnostics.get(0).getMessage().contains("bug in the Wurst Compiler"));
    }

    @Test
    public void printerRetainsSourcePositionsThroughTranslationAndLocalMerging() throws Exception {
        Path dir = Files.createTempDirectory("wurst-jass-sourcemap");
        String filename = dir.resolve("Main.wurst").toString();
        WurstGuiLogger gui = new WurstGuiLogger();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, new RunArgs());
        compiler.loadReader(filename, new StringReader("package Main\nnative useString(string s)\n"
            + "init\n    string s = \"hello\"\n    if true\n        useString(s)\n"));
        var model = compiler.parseFiles();
        compiler.checkProg(model);
        assertEquals(gui.getErrorCount(), 0, gui.getErrors());
        compiler.translateProgToIm(model);
        var prog = compiler.transformProgToJass();

        for (boolean withSpace : new boolean[]{true, false}) {
            JassPrinter printer = new JassPrinter(withSpace, prog);
            String output = printer.printProg();
            Map<Integer, WPos> sourceMap = printer.getSourceMap();
            String[] lines = output.split("\n");
            boolean sawLocal = false;
            boolean sawCall = false;
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].contains("\"hello\"")) {
                    assertNotNull(sourceMap.get(i + 1), lines[i]);
                    assertEquals(sourceMap.get(i + 1).getFile(), filename);
                    assertEquals(sourceMap.get(i + 1).getLine(), 4);
                    sawLocal = true;
                }
                if (lines[i].strip().startsWith("call useString")) {
                    assertNotNull(sourceMap.get(i + 1), lines[i]);
                    assertEquals(sourceMap.get(i + 1).getLine(), 6);
                    sawCall = true;
                }
            }
            assertTrue(sawLocal);
            assertTrue(sawCall);
        }
    }

    @Test
    public void buildMarkersPublishAndClearWithoutReplacingTypeErrors() throws Exception {
        Path project = Files.createTempDirectory("wurst-build-markers");
        Path wurst = Files.createDirectory(project.resolve("wurst"));
        Path source = wurst.resolve("Main.wurst");
        Files.writeString(wurst.resolve("Wurst.wurst"), "package Wurst\n");
        Files.writeString(source, "package Main\nfunction f() returns int\n    return missing\n");
        ModelManagerImpl manager = new ModelManagerImpl(project.toFile(), new BufferManager());
        List<PublishDiagnosticsParams> reports = new ArrayList<>();
        manager.onCompilationResult(reports::add);
        manager.buildProject();
        assertTrue(reports.stream().flatMap(r -> r.getDiagnostics().stream())
            .anyMatch(d -> d.getMessage().contains("missing")), reports.toString());
        reports.clear();

        WPos position = new WPos(source.toString(), LineOffsets.dummy, 0, 4);
        manager.reportBuildDiagnostics(List.of(new CompileError(position, "Build warning", CompileError.ErrorType.WARNING)));
        assertEquals(reports.size(), 1);
        assertTrue(reports.get(0).getDiagnostics().stream().anyMatch(d -> d.getMessage().equals("Build warning")
            && d.getSeverity() == DiagnosticSeverity.Warning));
        assertTrue(reports.get(0).getDiagnostics().stream().anyMatch(d -> d.getMessage().contains("missing")));

        reports.clear();
        manager.reportBuildDiagnostics(List.of());
        assertEquals(reports.size(), 1);
        assertFalse(reports.get(0).getDiagnostics().stream().anyMatch(d -> d.getMessage().equals("Build warning")));
        assertTrue(reports.get(0).getDiagnostics().stream().anyMatch(d -> d.getMessage().contains("missing")));
    }
}
