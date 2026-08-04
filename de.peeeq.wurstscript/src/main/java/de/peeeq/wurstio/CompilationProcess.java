package de.peeeq.wurstio;

import org.wurstscript.projectconfig.WurstProjectConfigData;
import de.peeeq.wurstio.benchmark.BenchmarkOptions;
import de.peeeq.wurstio.benchmark.BenchmarkResult;
import de.peeeq.wurstio.benchmark.BenchmarkWorkerOutput;
import de.peeeq.wurstio.benchmark.RunBenchmarks;
import de.peeeq.wurstio.languageserver.requests.RunTests;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.utils.FileUtils;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILStackFrame;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Supplier;

/**
 *
 */
public class CompilationProcess {

    private final WurstGui gui;
    private final RunArgs runArgs;
    private final TimeTaker timeTaker;

    public CompilationProcess(WurstGui gui, RunArgs runArgs) {
        this.gui = gui;
        this.runArgs = runArgs;
        if (runArgs.isMeasureTimes()) {
            this.timeTaker = new TimeTaker.Recording();
        } else {
            this.timeTaker = new TimeTaker.Default();
        }
    }

    @Nullable CharSequence doCompilation(@Nullable MpqEditor mpqEditor, boolean isProd) throws IOException {
        return doCompilation(mpqEditor, null, isProd);
    }

    @Nullable CharSequence doCompilation(@Nullable MpqEditor mpqEditor, @Nullable File projectFolder, boolean isProd) throws IOException {
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(timeTaker, projectFolder, gui, mpqEditor, runArgs);
        gui.sendProgress("Check input map");
        if (mpqEditor != null && !mpqEditor.canWrite()) {
            WLogger.severe("The supplied map is invalid/corrupted/protected and Wurst cannot write to it.\n" +
                    "Please supply a valid .w3x input map that can be opened in the world editor.");
        }

        for (String file : runArgs.getFiles()) {
            compiler.loadFiles(file);
        }
        WurstModel model = timeTaker.measure("parse files",
            compiler::parseFiles);

        if (gui.getErrorCount() > 0) {
            return null;
        }
        if (model == null) {
            return null;
        }

        timeTaker.measure("Typecheck program",
                () -> compiler.checkProg(model));

        if (gui.getErrorCount() > 0) {
            return null;
        }

        timeTaker.measure("Translate program to Im",
                () -> compiler.translateProgToIm(model));

        if (gui.getErrorCount() > 0) {
            return null;
        }

        if (runArgs.isRunBenchmarks()) {
            timeTaker.measure("Run benchmark worker", () -> runBenchmarks(compiler));
            return null;
        }

        if (runArgs.isRunTests()) {
            timeTaker.measure("Run tests",
                    () -> runTests(compiler.getImTranslator(), compiler, runArgs.getTestTimeout(), runArgs.getTestFilter()));
        }

        timeTaker.measure("Run compiletime functions", () ->compiler.runCompiletime(WurstProjectConfigData.empty(), isProd, false));

        JassProg jassProg = timeTaker.measure("Transform program to Jass",
            compiler::transformProgToJass);

        if (jassProg == null || gui.getErrorCount() > 0) {
            return null;
        }

        boolean withSpace;
        withSpace = !runArgs.isOptimize();

        gui.sendProgress("Printing Jass");

        JassPrinter printer = new JassPrinter(withSpace, jassProg);
        CharSequence mapScript = timeTaker.measure("Print Jass",
            (Supplier<String>) printer::printProg);

        // output to file
        File outputMapscript = timeTaker.measure("Print Jass",
                () -> writeMapscript(mapScript));

        if (!runArgs.isDisablePjass() && !runArgs.isLegacyJassTypeChecks()) {
            boolean pjassError = timeTaker.measure("Run PJass",
                    () -> runPjass(outputMapscript));
            if (pjassError) return null;
        }
        timeTaker.printReport();
        return mapScript;
    }

    private void runBenchmarks(WurstCompilerJassImpl compiler) {
        try {
            RunBenchmarks runner = new RunBenchmarks();
            Path output = Paths.get(runArgs.getBenchmarkOutput());
            if (runArgs.isBenchmarkList()) {
                BenchmarkWorkerOutput.writeDiscovery(
                    output,
                    runner.discover(compiler.getImProg(), Optional.ofNullable(runArgs.getBenchmarkFilter())));
            } else {
                BenchmarkResult result = runner.run(
                    compiler.getImTranslator(),
                    compiler.getImProg(),
                    runArgs.getBenchmarkName(),
                    new BenchmarkOptions(runArgs.getBenchmarkWarmup(), runArgs.getBenchmarkIterations(), 1_000_000L));
                BenchmarkWorkerOutput.writeResult(output, result);
            }
        } catch (Throwable e) {
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            gui.sendError(new CompileError(
                null,
                "Benchmark worker failed: " + message,
                CompileError.ErrorType.ERROR,
                e));
        }
    }

    private boolean runPjass(File outputMapscript) {
        File commonJ = new File(outputMapscript.getParent(), "common.j");
        File blizzJ = new File(outputMapscript.getParent(), "blizzard.j");

        Pjass.Result pJassResult;
        if (commonJ.exists() && blizzJ.exists()) {
            pJassResult = Pjass.runPjass(outputMapscript, commonJ.getAbsolutePath(), blizzJ.getAbsolutePath());
        } else {
            pJassResult = Pjass.runPjass(outputMapscript);
        }
        WLogger.info(pJassResult.getMessage());
        if (!pJassResult.isOk()) {
            for (CompileError err : pJassResult.getErrors()) {
                gui.sendError(err);
            }
            return true;
        }
        return false;
    }

    private File writeMapscript(CharSequence mapScript) {
        gui.sendProgress("Writing output file");
        File outputMapscript;
        if (runArgs.getOutFile() != null) {
            outputMapscript = new File(runArgs.getOutFile());
        } else {
            outputMapscript = new File("./temp/output.j");
        }
        outputMapscript.getParentFile().mkdirs();
        try {
            FileUtils.write(mapScript, outputMapscript);
            return outputMapscript;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runTests(ImTranslator translator, WurstCompilerJassImpl compiler, int testTimeout, Optional<String> testFilter) {
        PrintStream out = System.out;
        // tests
        gui.sendProgress("Running tests");
        if (!runArgs.isCompactOutput()) {
            System.out.println("Running tests");
        }
        RunTests runTests = new RunTests(Optional.empty(), 0, 0, Optional.empty(), testTimeout, testFilter, runArgs.isCompactOutput()) {
            @Override
            protected void print(String message) {
                out.print(message);
            }
        };
        runTests.runTests(translator, compiler.getImProg(), Optional.empty(), Optional.empty());

        for (RunTests.TestFailure e : runTests.getFailTests()) {
            gui.sendError(new CompileError(e.getFunction(), e.getMessage()));
            if (runArgs.isGui()) {
                // when using graphical user interface, send stack trace to GUI
                for (ILStackFrame sf : Utils.iterateReverse(e.getStackTrace().getStackFrames())) {
                    gui.sendError(sf.makeCompileError());
                }
            }
        }

        if (!runArgs.isCompactOutput()) {
            System.out.println("Finished running tests");
        }
    }
}
