package de.peeeq.wurstio.languageserver.requests;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import de.peeeq.wurstio.CompiletimeFunctionRunner;
import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstio.jassinterpreter.ReflectionNativeProvider;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;
import de.peeeq.wurstscript.intermediatelang.interpreter.ProgramState;
import de.peeeq.wurstscript.intermediatelang.interpreter.ProgramState.StackTrace;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.lsp4j.MessageType;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static de.peeeq.wurstio.CompiletimeFunctionRunner.FunctionFlagToRun.CompiletimeFunctions;

/**
 * Created by peter on 05.05.16.
 */
public class RunTests extends UserRequest<Object> {

    private final Optional<WFile> filename;
    private final int line;
    private final int column;
    private final Optional<String> testName;

    private List<ImFunction> successTests = Lists.newArrayList();
    private List<TestFailure> failTests = Lists.newArrayList();

    private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    static public class TestFailure {

        private ImFunction function;
        private final StackTrace stackTrace;
        private final String message;

        public TestFailure(ImFunction function, StackTrace stackTrace, String message) {
            Preconditions.checkNotNull(function);
            Preconditions.checkNotNull(stackTrace);
            Preconditions.checkNotNull(message);
            this.function = function;
            this.stackTrace = stackTrace;
            this.message = message;
        }

        public StackTrace getStackTrace() {
            return stackTrace;
        }

        public String getMessage() {
            return message;
        }

        public ImFunction getFunction() {
            return function;
        }

        public String getMessageWithStackFrame() {
            StringBuilder s = new StringBuilder(message);
            s.append("\n");
            stackTrace.appendTo(s);
            return s.toString();
        }

    }

    public RunTests(Optional<String> filename, int line, int column, Optional<String> testName) {
        this.filename = filename.map(fname -> WFile.create(fname));
        this.line = line;
        this.column = column;
        this.testName = testName;
    }


    @Override
    public Object execute(ModelManager modelManager) {
        if (modelManager.hasErrors()) {
            throw new RequestFailedException(MessageType.Error, "Fix errors in your code before running tests.");
        }

        WLogger.info("Starting tests " + filename + ", " + line + ", " + column);
        println("Running unit tests..\n");

        Optional<CompilationUnit> cu = filename.map(fname -> modelManager.getCompilationUnit(fname));
        WLogger.info("test.cu = " + Utils.printElement(cu));
        Optional<FuncDef> funcToTest = getFunctionToTest(cu);
        WLogger.info("test.funcToTest = " + Utils.printElement(funcToTest));


        ImTranslator translator = translateProg(modelManager);
        ImProg imProg = translator.getImProg();
        if (imProg == null) {
            println("Could not run tests, because program did not compile.\n");
            return "Could not translate program";
        }

        runTests(translator, imProg, funcToTest, cu);
        return "ok";
    }

    public static class TestResult {

        private final int passedTests;
        private final int totalTests;

        public TestResult(int passedTests, int totalTests) {
            this.passedTests = passedTests;
            this.totalTests = totalTests;
        }

        public int getPassedTests() {
            return passedTests;
        }

        public int getTotalTests() {
            return totalTests;
        }

    }

    public TestResult runTests(ImTranslator translator, ImProg imProg, Optional<FuncDef> funcToTest, Optional<CompilationUnit> cu) {
        WurstGui gui = new TestGui();

        CompiletimeFunctionRunner cfr = new CompiletimeFunctionRunner(translator, imProg, Optional.empty(), null, gui,
            CompiletimeFunctions);
        ILInterpreter interpreter = cfr.getInterpreter();
        ProgramState globalState = cfr.getGlobalState();
        if (globalState == null) {
            globalState = new ProgramState(gui, imProg, true);
        }
        if (interpreter == null) {
            interpreter = new ILInterpreter(imProg, gui, Optional.empty(), globalState);
            interpreter.addNativeProvider(new ReflectionNativeProvider(interpreter));
        }

        redirectInterpreterOutput(globalState);

        // first run compiletime functions
        cfr.run();

        if (gui.getErrorCount() > 0) {
            for (CompileError compileError : gui.getErrorList()) {
                println(compileError.toString());
            }
            println("There were some problem while running compiletime expressions and functions.");
            return new TestResult(0, 1);
        }

        WLogger.info("Ran compiletime functions");


        for (ImFunction f : imProg.getFunctions()) {
            if (f.hasFlag(FunctionFlagEnum.IS_TEST)) {
                Element trace = f.attrTrace();

                if (cu.isPresent() && !Utils.elementContained(Optional.of(trace), cu.get())) {
                    continue;
                }
                if (funcToTest.isPresent() && trace != funcToTest.get()) {
                    continue;
                }


                String message = "Running <" + f.attrTrace().attrNearestPackage().tryGetNameDef().getName() + ":"
                        + f.attrTrace().attrErrorPos().getLine() + " - " + f.getName() + ">..";
                println(message);
                WLogger.info(message);
                try {
                    @Nullable ILInterpreter finalInterpreter = interpreter;
                    Callable<Void> run = () -> {
                        finalInterpreter.runVoidFunc(f, null);
                        // each test must finish it's own timers (otherwise, we would get strange results)
                        finalInterpreter.completeTimers();
                        return null;
                    };
                    RunnableFuture<Void> future = new FutureTask<>(run);
                    if (service != null && !service.isShutdown()) {
                        service.shutdownNow();
                    }
                    service = Executors.newSingleThreadScheduledExecutor();
                    service.execute(future);
                    try {
                        future.get(20, TimeUnit.SECONDS); // Wait 20 seconds for test to complete
                    } catch (TimeoutException ex) {
                        future.cancel(true);
                        throw new TestTimeOutException();
                    } catch (ExecutionException e) {
                        throw e.getCause();
                    }
                    service.shutdown();
                    service.awaitTermination(10, TimeUnit.SECONDS);
                    service = Executors.newSingleThreadScheduledExecutor();
                    if (gui.getErrorCount() > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (CompileError error : gui.getErrorList()) {
                            sb.append(error.toString()).append("\n");
                            println(error.getMessage());
                        }
                        gui.clearErrors();
                        TestFailure failure = new TestFailure(f, interpreter.getStackFrames(), sb.toString());
                        failTests.add(failure);
                    } else {
                        successTests.add(f);
                        println("\tOK!");
                    }
                } catch (TestSuccessException e) {
                    successTests.add(f);
                    println("\tOK!");
                } catch (TestFailException e) {
                    TestFailure failure = new TestFailure(f, interpreter.getStackFrames(), e.getMessage());
                    failTests.add(failure);
                    println("\tFAILED assertion:");
                    println("\t" + failure.getMessageWithStackFrame());
                } catch (TestTimeOutException e) {
                    failTests.add(new TestFailure(f, interpreter.getStackFrames(), e.getMessage()));
                    println("\tFAILED - TIMEOUT (This test did not complete in 20 seconds, it might contain an endless loop)");
                    println(interpreter.getStackFrames().toString());
                } catch (InterpreterException e) {
                    TestFailure failure = new TestFailure(f, interpreter.getStackFrames(), e.getMessage());
                    failTests.add(failure);
                    println("\t" + failure.getMessageWithStackFrame());
                } catch (Throwable e) {
                    failTests.add(new TestFailure(f, interpreter.getStackFrames(), e.toString()));
                    println("\tFAILED with exception: " + e.getClass() + " " + e.getLocalizedMessage());
                    println(interpreter.getStackFrames().toString());
                    println("Here are some compiler internals, that might help Wurst developers to debug this issue:");
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String sStackTrace = sw.toString();
                    println("\t" + e.getLocalizedMessage());
                    println("\t" + sStackTrace);
                }
            }
        }
        println("Tests succeeded: " + successTests.size() + "/" + (successTests.size() + failTests.size()));
        if (failTests.size() == 0) {
            println(">> All tests have passed successfully!");
        } else {
            println(">> " + failTests.size() + " Tests have failed!");
        }
        if (gui.getErrorCount() > 0) {
            println("There were some errors reported while running the tests.");
            for (CompileError error : gui.getErrorList()) {
                println(error.toString());
            }
        }

        WLogger.info("finished tests");
        return new TestResult(successTests.size(), successTests.size() + failTests.size());
    }


    private void redirectInterpreterOutput(ProgramState globalState) {
        OutputStream os = new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                if (b > 0) {
                    println("" + (char) b);
                }
            }

            @Override
            public void write(byte b[], int off, int len) throws IOException {
                println(new String(b, off, len));
            }


        };
        globalState.setOutStream(new PrintStream(os));
    }

    protected void println(String message) {
        print(message);
        print(System.lineSeparator());
    }

    protected void print(String message) {
        System.err.print(message);
    }

    private ImTranslator translateProg(ModelManager modelManager) {
        // need to run compiletime functions for running unit tests
        RunArgs runArgs = new RunArgs("-runcompiletimefunctions");
        ImTranslator imTranslator = new ImTranslator(modelManager.getModel(), false, runArgs);
        // will ignore udg_ variables which are not found
        imTranslator.setEclipseMode(true);
        imTranslator.translateProg();
        return imTranslator;
    }


    private Optional<FuncDef> getFunctionToTest(Optional<CompilationUnit> maybeCu) {
        if (testName.isPresent()) {
            int dotPos = testName.get().indexOf(".");
            String packageName = testName.get().substring(0, dotPos);
            String funcName = testName.get().substring(dotPos+1);
            Optional<FuncDef> testFunc = maybeCu.flatMap(cu ->
                cu.getPackages()
                .stream()
                .filter(p -> p.getName().equals(packageName))
                .flatMap(p -> p.getElements().stream())
                .filter(e -> e instanceof FuncDef)
                .map(e -> (FuncDef) e)
                .filter(f -> f.hasAnnotation("@test"))
                .filter(f -> f.getName().equals(funcName))
                .findFirst()
            );

            if (testFunc.isPresent()) {
                return testFunc;
            }
        }
        if (!filename.isPresent() || !maybeCu.isPresent() || line < 0) {
            return Optional.empty();
        }
        Optional<Element> e = Utils.getAstElementAtPos(maybeCu.get(), line, column, false);
        while (e.isPresent()) {
            if (e.get() instanceof FuncDef) {
                return e.map(el -> (FuncDef) el);
            }
            e = e.flatMap(el -> Optional.ofNullable(el.getParent()));
        }
        return null;
    }

    public List<TestFailure> getFailTests() {
        return failTests;
    }

    public class TestGui extends WurstGui {

        @Override
        public void sendProgress(String whatsRunningNow) {
            // ignore
        }

        @Override
        public void sendFinished() {
            // ignore
        }

        @Override
        public void showInfoMessage(String message) {
            println(message + "\n");
        }


    }

    private static class TestTimeOutException extends Throwable {

        @Override
        public String getMessage() {
            return "test failed with timeout (This test did not complete in 20 seconds, it might contain an endless loop)";
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }


    public static ScheduledExecutorService getService() {
        return service;
    }
}
