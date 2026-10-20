package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.wurstscript.projectconfig.WurstProjectConfigData;
import de.peeeq.wurstio.Pjass;
import de.peeeq.wurstio.Pjass.Result;
import de.peeeq.wurstio.UtilsIO;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.jassinterpreter.JassInterpreter;
import de.peeeq.wurstio.jassinterpreter.ReflectionNativeProvider;
import de.peeeq.wurstio.languageserver.requests.RunTests;
import de.peeeq.wurstio.utils.FileUtils;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.luaAst.LuaCompilationUnit;
import de.peeeq.wurstscript.luaAst.*;
import de.peeeq.wurstscript.translation.lua.translation.LuaAssertions;
import de.peeeq.wurstscript.translation.lua.translation.LuaTranslator;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.translation.imtranslation.RecycleCodeGeneratorQueue;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.validation.GlobalCaches;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.Assert.fail;

public class WurstScriptTest {

    public static final String TEST_OUTPUT_PATH = "./test-output/";

    /**
     * Every translated program writes its IM as a {@code .jim} file, twice per optimisation setting, and only a
     * handful of tests read one: the rest paid for printing it. A test which reads one asks for it with
     * {@link TestConfig#imDump()}; {@code ./gradlew test -PimDumps} writes them all, to read a failing test's IM.
     */
    private static final boolean IM_DUMPS_FOR_ALL_TESTS = Boolean.getBoolean("wurst.test.imDumps");
    private boolean writeImDumps;

    /**
     * {@code -PallBackends} (master CI): every executing test which does not run Lua itself also runs the Lua
     * target after its Jass runs, and an exempt test ({@link TestConfig#luaExempt}) must fail there.
     * {@code -PallBackends=full} adds a Lua run with {@code -inline -localOptimizations}. Empty when off.
     */
    private static final String ALL_BACKENDS = System.getProperty("wurst.test.allBackends", "");
    private static final boolean ALL_BACKENDS_ON = !ALL_BACKENDS.isEmpty();
    private static final boolean ALL_BACKENDS_FULL = ALL_BACKENDS.equals("full");

    static {
        if (ALL_BACKENDS_ON && !ALL_BACKENDS.equals("lua") && !ALL_BACKENDS_FULL) {
            throw new IllegalStateException("wurst.test.allBackends is 'lua' or 'full', not '" + ALL_BACKENDS + "'");
        }
    }

    /** BACKLOG.md with its whitespace collapsed, read once, for the citations of {@link LuaExemption#KNOWN_LUA_DEFECT}. */
    private static volatile String backlogText;

    /** Generous enough for the slowest test program, short enough that a loop fails the run. */
    private static final int LUA_EXECUTION_TIMEOUT_SECONDS = 60;

    /**
     * How much of a program's output is kept for the failure message. Draining never stops - that
     * is what deadlocks the run - but retaining everything from a program that loops while printing
     * would exhaust the worker before the timeout fires.
     */
    private static final int RETAINED_OUTPUT_LIMIT = 64 * 1024;

    /** Overridden by the tests covering the runner itself, so they need seconds rather than a minute. */
    protected int luaExecutionTimeoutSeconds() {
        return LUA_EXECUTION_TIMEOUT_SECONDS;
    }
    private static volatile String resolvedLuaExecutable;
    private static volatile String resolvedLuacExecutable;
    private static volatile String extractedLuaWin;
    private static volatile String extractedLuaUnix;
    private static volatile String extractedLuacWin;
    private static volatile String extractedLuacUnix;

    /**
     * How many Wurst tests the last run executed. Tests are stripped by the time the second pass
     * happens, so this keeps the largest either saw. Zero means the program held none, which a
     * caller expecting some needs to hear about: "every test passed" and "there were no tests" are
     * otherwise the same green.
     */
    private int testsRun;

    @BeforeMethod(alwaysRun = true)
    public void _clearBefore() {
        GlobalCaches.clearAll();
        testsRun = 0;
    }

    @AfterMethod(alwaysRun = true)
    public void _clearAfter() {
        // Release all AST strong-refs held by caches so the GC can reclaim the
        // stdlib copy from the previous test before the next test allocates its own.
        GlobalCaches.clearAll();
    }

    class TestConfig {
        private final String name;
        private boolean withStdLib;
        private boolean executeProg;
        private boolean executeTests;
        private int minimumTestsExpected;
        private boolean executeProgOnlyAfterTransforms;
        private String expectedError;
        private String expectedWarning;

        private final List<File> inputFiles = new ArrayList<>();
        private final List<CU> additionalCompilationUnits = new ArrayList<>();
        private boolean stopOnFirstError = true;
        private boolean runCompiletimeFunctions;
        private boolean optimize;
        private boolean inline;
        private boolean localOptimizations;
        private boolean stacktraces;
        private boolean testLua = false;
        private boolean luaOnly = false;
        private boolean uncheckedDispatch = false;
        private boolean legacyJassTypeChecks = false;
        private boolean imDump = false;
        private LuaExemption luaExemption;
        private String luaExemptionDetail;

        TestConfig(String name) {
            this.name = name;
        }

        TestConfig withStdLib() {
            return withStdLib(true);
        }

        TestConfig withStdLib(boolean b) {
            this.withStdLib = b;
            if (withStdLib) {
                // stdlib needs compiletime functions
                this.runCompiletimeFunctions = true;
            }
            return this;
        }

        public TestConfig executeProg() {
            this.executeProg = true;
            return this;
        }

        public TestConfig executeTests() {
            this.executeTests = true;
            return this;
        }

        /** Fails when fewer than this many Wurst tests ran, so a program holding none is not green. */
        public TestConfig expectAtLeastTests(int minimum) {
            this.executeTests = true;
            this.minimumTestsExpected = minimum;
            return this;
        }

        public TestConfig executeTests(boolean b) {
            this.executeTests = b;
            return this;
        }

        /** Enables the IM inliner (-inline), which is a separate option from -opt. */
        TestConfig inline() {
            this.inline = true;
            return this;
        }

        /** Enables the local optimisations (-localOptimizations), which fold constants, also on the Lua run. */
        TestConfig localOptimizations() {
            this.localOptimizations = true;
            return this;
        }

        TestConfig optimize() {
            this.optimize = true;
            return this;
        }

        TestConfig executeProg(boolean b) {
            this.executeProg = b;
            return this;
        }

        TestConfig expectWarning(String expectedWarning) {
            this.expectedWarning = expectedWarning;
            return this;
        }

        /**
         * Emit stack traces, as a release build does by default.
         *
         * <p>Worth testing on the Lua path: stack-trace injection appends a parameter to every
         * affected function there, so a lowering which recognises a function by its exact
         * signature stops matching once it has run.
         */
        public TestConfig stacktraces() {
            this.stacktraces = true;
            return this;
        }

        public TestConfig executeProgOnlyAfterTransforms() {
            this.executeProgOnlyAfterTransforms = true;
            return this;
        }

        public TestConfig executeProgOnlyAfterTransforms(boolean b) {
            this.executeProgOnlyAfterTransforms = b;
            return this;
        }

        public TestConfig uncheckedDispatch() {
            return uncheckedDispatch(true);
        }

        public TestConfig uncheckedDispatch(boolean b) {
            this.uncheckedDispatch = b;
            return this;
        }

        /** Writes the translated IM to {@code test-output/<name>.jim}, for a test which reads it. */
        public TestConfig imDump() {
            this.imDump = true;
            return this;
        }

        public TestConfig legacyJassTypeChecks() {
            return legacyJassTypeChecks(true);
        }

        public TestConfig legacyJassTypeChecks(boolean b) {
            this.legacyJassTypeChecks = b;
            return this;
        }

        TestConfig expectError(String expectedError) {
            this.expectedError = expectedError;
            return this;
        }

        CompilationResult lines(String... lines) {
            String testName = UtilsIO.getMethodName(WurstScriptTest.class.getName());
            additionalCompilationUnits.add(new CU(testName, Utils.join(lines, "\n") + "\n"));
            return run();
        }

        CompilationResult compilationUnits(CU... units) {
            additionalCompilationUnits.addAll(Arrays.asList(units));
            return run();
        }

        CompilationResult run() {
            try {
                CompilationResult res = testScript();
                if (minimumTestsExpected > 0 && testsRun < minimumTestsExpected) {
                    fail("expected at least " + minimumTestsExpected + " Wurst tests to run, but "
                        + testsRun + " did. A program which holds no tests passes every one of them,"
                        + " so this would otherwise be green while checking nothing.");
                }
                if (expectedError != null) {
                    if (res.getGui().getErrorCount() == 0) {
                        fail("No errors were discovered");
                    } else {
                        List<CompileError> errors = res.getGui().getErrorList();
                        if (errors.stream()
                                .noneMatch(e -> e.getMessage().toLowerCase().contains(expectedError.toLowerCase()))) {
                            for (CompileError error : errors) {
                                System.err.println("Unexpected error:" + error);
                            }
                            throw new RuntimeException("Unexpected error", errors.get(0));
                        }
                    }
                }
                if (expectedWarning != null) {
                    List<CompileError> warnings = res.getGui().getWarningList();
                    if (warnings.isEmpty()) {
                        fail("No warnings were discovered");
                    } else if (warnings.stream()
                        .noneMatch(w -> w.getMessage().toLowerCase().contains(expectedWarning.toLowerCase()))) {
                        for (CompileError w : warnings) {
                            System.err.println("Unexpected warning:" + w);
                        }
                        throw new RuntimeException("Unexpected warning", warnings.get(0));
                    }
                }
                return res;
            } catch (CompileError e) {
                if (expectedError != null) {
                    if (!e.getMessage().toLowerCase().contains(expectedError.toLowerCase())) {
                        throw e;
                    }
                    return null;
                }
                throw e;
            }
        }

        private CompilationResult testScript() {
            if (testLua && luaExemption != null) {
                throw new IllegalStateException("a test which runs Lua cannot also be exempt from it ("
                    + luaExemption + ")");
            }
            writeImDumps = imDump || IM_DUMPS_FOR_ALL_TESTS;
            pjassPending.clear();
            RunArgs runArgs = new RunArgs();
            RecycleCodeGeneratorQueue.setTestMode = true;
            if (withStdLib) {
                runArgs = runArgs.with("-lib", StdLib.getLib());
            }
            if (uncheckedDispatch) {
                runArgs = runArgs.with("-uncheckedDispatch");
            }
            if (runCompiletimeFunctions) {
                runArgs = runArgs.with("-runcompiletimefunctions");
            }
            if (optimize) {
                runArgs = runArgs.with("-opt");
            }
            if (inline) {
                runArgs = runArgs.with("-inline");
            }
            if (localOptimizations) {
                runArgs = runArgs.with("-localOptimizations");
            }
            if (legacyJassTypeChecks) {
                runArgs.setLegacyJassTypeChecks(true);
            }

            WurstGui gui = new WurstGuiCliImpl();
            WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, runArgs);
            if (stopOnFirstError) {
                compiler.getErrorHandler().enableUnitTestMode();
            }


            WurstModel model = parseFiles(inputFiles, additionalCompilationUnits, withStdLib, compiler);

            if (stopOnFirstError && !gui.getErrorList().isEmpty()) {
                throw gui.getErrorList().get(0);
            }


            // check prog
            compiler.checkProg(model);
            if (stopOnFirstError && !gui.getErrorList().isEmpty()) {
                throw gui.getErrorList().get(0);
            }


            if (stopOnFirstError && name.toLowerCase().contains("warning") && !gui.getWarningList().isEmpty()) {
                // report warnings based on naming convention
                throw gui.getWarningList().get(0);
            }

            if (!gui.getErrorList().isEmpty()) {
                // errors in type-checker -> return
                return new CompilationResult(model, gui);
            }


            if (!(testLua && luaOnly)) {
                // translate with different options:
                testWithoutInliningAndOptimization(name, executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms, runArgs);

                testWithLocalOptimizations(name, executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms, runArgs);

                testWithInlining(name, executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms, runArgs);

                testWithInliningAndOptimizations(name, executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms, runArgs);

                testWithInliningAndOptimizationsAndStacktraces(name, executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms, runArgs);

                if (legacyJassTypeChecks) {
                    // a legacy target is not checked with pjass, as in a build
                    pjassPending.clear();
                } else {
                    runPendingPjass();
                }
            }

            if (testLua) {
                // test lua translation
                runArgs = stacktraces ? runArgs.with("-lua", "-stacktraces") : runArgs.with("-lua");
                compiler.setRunArgs(runArgs);
                translateAndTestLua(name, executeProg, gui, model, compiler);
            } else if (executeProg && ALL_BACKENDS_ON) {
                runLuaForAllBackends(gui, model, compiler, runArgs);
            }

            RecycleCodeGeneratorQueue.setTestMode = false;

            return new CompilationResult(model, gui);
        }

        /**
         * The Lua runs {@code -PallBackends} adds after the Jass runs of an executing test which does not run Lua
         * itself. They must pass; for an exempt test one of them must fail, and with
         * {@link LuaExemption#MISSING_TEST_NATIVE} that failure must name the native.
         */
        private void runLuaForAllBackends(WurstGui gui, WurstModel model, WurstCompilerJassImpl compiler, RunArgs runArgs) {
            RunArgs plain = stacktraces ? runArgs.with("-lua", "-stacktraces") : runArgs.with("-lua");
            Map<String, RunArgs> variants = new LinkedHashMap<>();
            variants.put("", plain);
            if (ALL_BACKENDS_FULL) {
                variants.put("_inlopt", plain.with("-inline", "-localOptimizations"));
            }
            for (Entry<String, RunArgs> variant : variants.entrySet()) {
                String env = "-PallBackends Lua" + (variant.getKey().isEmpty() ? "" : " with -inline -localOptimizations");
                setCurrentTestEnv(env);
                compiler.setRunArgs(variant.getValue());
                Throwable failure;
                try {
                    translateAndTestLua(name + variant.getKey(), true, gui, model, compiler);
                    continue;
                } catch (org.testng.SkipException | VirtualMachineError e) {
                    throw e;
                } catch (RuntimeException | Error e) {
                    failure = e;
                }
                if (luaExemption == null) {
                    throw new AssertionError(env + ": the test holds on Jass and fails on Lua. Fix the Lua"
                        + " translation, or say why with luaExempt(LuaExemption...).", failure);
                }
                // Lua names the nil it called in quotes ("global 'R2S'"), as the generated fallbacks name theirs.
                if (luaExemption == LuaExemption.MISSING_TEST_NATIVE && !mentions(failure, "'" + luaExemptionDetail + "'")) {
                    throw new AssertionError(env + ": exempt because the Lua test runtime lacks "
                        + luaExemptionDetail + ", but the Lua run failed without naming it.", failure);
                }
                System.out.println(env + ": failed as its exemption (" + luaExemption + ") says: " + failure);
                return;
            }
            if (luaExemption != null) {
                throw new AssertionError("-PallBackends: luaExempt(" + luaExemption
                    + (luaExemptionDetail == null ? "" : ", \"" + luaExemptionDetail + "\"")
                    + ") is stale, the test passes on Lua. Remove the exemption and run Lua with"
                    + " testLua(true).luaOnly(false).");
            }
        }

        public void file(File file) throws IOException {
            try {
                String content = Files.asCharSource(file, StandardCharsets.UTF_8).read();
                additionalCompilationUnits.add(new CU(file.getName(), content));
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("Failed to open file "
                    + file.getAbsolutePath()
                    + " - can read? "
                    + file.canRead());
            }
            run();
        }

        public TestConfig withCu(CU cu) {
            additionalCompilationUnits.add(cu);
            return this;
        }

        public TestConfig setStopOnFirstError(boolean b) {
            stopOnFirstError = b;
            return this;
        }

        public TestConfig withInputFiles(Iterable<File> inputFiles) {
            for (File f : inputFiles) {
                this.inputFiles.add(f);
            }
            return this;
        }

        public TestConfig withInputs(Map<String, String> inputs) {
            for (Entry<String, String> e : inputs.entrySet()) {
                additionalCompilationUnits.add(new CU(e.getKey(), e.getValue()));
            }
            return this;
        }

        public TestConfig runCompiletimeFunctions(boolean b) {
            this.runCompiletimeFunctions = b;
            return this;
        }

        public TestConfig testLua(boolean b) {
            if (!b) {
                throw new IllegalArgumentException("testLua(false) opts out of Lua without saying why:"
                    + " use luaExempt(LuaExemption...)");
            }
            this.testLua = b;
            this.luaOnly = b;
            return this;
        }

        /**
         * This executing test does not hold on Lua, for {@code reason}. It runs no Lua, except under
         * {@code -PallBackends}, where its Lua run must fail, so a stale exemption fails the run.
         */
        public TestConfig luaExempt(LuaExemption reason) {
            return luaExempt(reason, null);
        }

        /**
         * {@link #luaExempt(LuaExemption)} for a reason which takes a detail: the name of the missing native,
         * or the title of the BACKLOG.md item, exactly as it is written there in bold.
         */
        public TestConfig luaExempt(LuaExemption reason, String detail) {
            if (reason.needsDetail() != (detail != null)) {
                throw new IllegalArgumentException(reason + (reason.needsDetail()
                    ? " needs a detail: what it is about, as recorded in " + reason.record()
                    : " takes no detail: its record is " + reason.record()));
            }
            if (reason == LuaExemption.KNOWN_LUA_DEFECT) {
                requireBacklogItem(detail);
            }
            this.luaExemption = reason;
            this.luaExemptionDetail = detail;
            return this;
        }

        public TestConfig luaOnly(boolean b) {
            this.luaOnly = b;
            return this;
        }
    }

    static class CompilationResult {
        private final WurstModel model;
        private final WurstGui gui;

        public CompilationResult(WurstModel model, WurstGui gui) {

            this.model = model;
            this.gui = gui;
        }

        public WurstModel getModel() {
            return model;
        }

        public WurstGui getGui() {
            return gui;
        }
    }

    static class CU {
        final String content;
        final String name;

        CU(String name, String content) {
            this.name = name;
            this.content = content;
        }
    }

    CU compilationUnit(String name, String... input) {
        return new CU(name, Utils.join(input, "\n"));
    }

    public TestConfig test() {
        String name = UtilsIO.getMethodName(WurstScriptTest.class.getName());
        name = this.getClass().getSimpleName() + "_" + name;
        return new TestConfig(name);
    }

    /**
     * Like {@link #test()} but with an explicit name for the files written under
     * {@link #TEST_OUTPUT_PATH}.
     *
     * <p>{@link #test()} names those files after the first frame outside WurstScriptTest, which is
     * the *helper* whenever a test reaches it through one of its own methods. That is deliberate -
     * DeterministicChecks relies on one test method producing several differently named outputs -
     * but it means two test methods sharing a helper get the same name, and therefore the same
     * file. Gradle runs test classes in parallel forks, so when those two methods live in
     * different suites the JVMs race on one .j file and pjass parses a spliced result.
     *
     * <p>Pass an explicit name in that situation. {@code name} is prefixed with the test class,
     * exactly as {@link #test()} does.
     */
    public TestConfig testNamed(String name) {
        return new TestConfig(this.getClass().getSimpleName() + "_" + name);
    }

    /**
     * Fails unless BACKLOG.md holds an item titled {@code title}, so a {@link LuaExemption#KNOWN_LUA_DEFECT}
     * exemption outlives its backlog item in no run, with or without {@code -PallBackends}.
     */
    private static void requireBacklogItem(String title) {
        String backlog = backlogText;
        if (backlog == null) {
            Path file = Path.of("..", "BACKLOG.md");
            try {
                backlog = java.nio.file.Files.readString(file, StandardCharsets.UTF_8).replaceAll("\\s+", " ");
            } catch (IOException e) {
                throw new UncheckedIOException("cannot read " + file.toAbsolutePath()
                    + " for a KNOWN_LUA_DEFECT exemption", e);
            }
            backlogText = backlog;
        }
        if (!backlog.contains("**" + title.replaceAll("\\s+", " ") + "**")) {
            fail("luaExempt(KNOWN_LUA_DEFECT, \"" + title + "\") cites no BACKLOG.md item: give the item's bold"
                + " title, and remove the exemption with the item when the defect is fixed");
        }
    }

    /** Whether the message of {@code failure} or of one of its causes contains {@code text}. */
    private static boolean mentions(Throwable failure, String text) {
        for (Throwable t = failure; t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (t.getMessage() != null && t.getMessage().contains(text)) {
                return true;
            }
        }
        return false;
    }

    /** The Lua written for {@code testName}, a test of this class compiled with {@code testLua(true)}. */
    protected String compiledLua(String testName) throws IOException {
        File luaFile = new File(TEST_OUTPUT_PATH, "lua/" + getClass().getSimpleName() + "_" + testName + ".lua");
        return java.nio.file.Files.readString(luaFile.toPath());
    }

    void testAssertOk(boolean excuteProg, boolean withStdLib, CU... units) {
        test().executeProg(excuteProg).withStdLib(withStdLib).compilationUnits(units);
    }


    public void testAssertOkLines(boolean executeProg, String... input) {
        test().executeProg(executeProg).lines(input);
    }

    public void testAssertErrorsLines(boolean executeProg, String errorMessage, String... input) {
        test().executeProg(executeProg).expectError(errorMessage).lines(input);
    }

    public void testAssertErrorsLinesWithStdLib(boolean executeProg, String errorMessage, String... input) {
        test().withStdLib().executeProg(executeProg).expectError(errorMessage).lines(input);
    }

    protected void testAssertOk(String name, boolean executeProg, String prog) {
        testAssertOkLines(executeProg, prog);
    }

    void testAssertOkFile(File file, boolean executeProg) throws IOException {
        test().executeProg(executeProg).file(file);

    }

    void testAssertOkFileWithStdLib(File file, boolean executeProg) throws IOException {
        test().withStdLib().executeProg(executeProg).file(file);
    }

    void testAssertOkLinesWithStdLib(boolean executeProg, String... input) {
        test().withStdLib().executeProg(executeProg).lines(input);
    }

    void testAssertErrorFileWithStdLib(File file, String errorMessage, boolean executeProg) throws IOException {
        test().withStdLib().executeProg(executeProg).expectError(errorMessage).file(file);
    }

    void testAssertErrors(String name, boolean executeProg, String prog, String errorMessage) {
        test().executeProg(executeProg).expectError(errorMessage).lines(prog);
    }

    public void testAssertWarningsLines(boolean executeProg, String warningMessage, String... input) {
        test()
            .setStopOnFirstError(false)
            .executeProg(executeProg)
            .expectWarning(warningMessage)
            .lines(input);
    }

    public void testAssertWarningsLinesWithStdLib(boolean executeProg, String warningMessage, String... input) {
        test()
            .withStdLib()
            .setStopOnFirstError(false)
            .executeProg(executeProg)
            .expectWarning(warningMessage)
            .lines(input);
    }

    protected WurstModel testScript(String name, boolean executeProg, String prog) {
        return test().executeProg(executeProg).lines(prog).getModel();
    }


    private void testWithInliningAndOptimizations(String name, boolean executeProg, boolean executeTests, WurstGui gui,
                                                  WurstCompilerJassImpl compiler, WurstModel model, boolean executeProgOnlyAfterTransforms, RunArgs runArgs) throws Error {
        // test with inlining and local optimization
        setCurrentTestEnv("With Inlining and Optimizations");
        compiler.setRunArgs(runArgs.with("-inline", "-localOptimizations"));
        translateAndTest(name + "_inlopt", executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);
    }

    private void testWithInliningAndOptimizationsAndStacktraces(String name, boolean executeProg, boolean executeTests, WurstGui gui,
                                                  WurstCompilerJassImpl compiler, WurstModel model, boolean executeProgOnlyAfterTransforms, RunArgs runArgs) throws Error {
        // test with inlining and local optimization
        setCurrentTestEnv("With Inlining, Optimizations and Stacktraces");
        compiler.setRunArgs(runArgs.with("-inline", "-localOptimizations", "-stacktraces"));
        translateAndTest(name + "_stacktraceinlopt", executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);
    }

    private void testWithInlining(String name, boolean executeProg, boolean executeTests, WurstGui gui
            , WurstCompilerJassImpl compiler, WurstModel model, boolean executeProgOnlyAfterTransforms
            , RunArgs runArgs) throws Error {
        // test with inlining
        setCurrentTestEnv("With Inlining");
        compiler.setRunArgs(runArgs.with("-inline"));
        translateAndTest(name + "_inl", executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);
    }

    private void testWithLocalOptimizations(String name, boolean executeProg, boolean executeTests, WurstGui gui,
                                            WurstCompilerJassImpl compiler, WurstModel model, boolean executeProgOnlyAfterTransforms, RunArgs runArgs) throws Error {
        // test with local optimization
        setCurrentTestEnv("With Local Optimizations");
        compiler.setRunArgs(runArgs.with("-localOptimizations"));
        translateAndTest(name + "_opt", executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);
    }

    private void testWithoutInliningAndOptimization(String name, boolean executeProg, boolean executeTests,
                                                    WurstGui gui, WurstCompilerJassImpl compiler, WurstModel model, boolean executeProgOnlyAfterTransforms, RunArgs runArgs)
            throws Error {
        compiler.setRunArgs(runArgs);
        // test without inlining and optimization
        setCurrentTestEnv("No opts");
        translateAndTest(name + "_no_opts", executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);
    }

    private void translateAndTestLua(String name, boolean executeProg, WurstGui gui, WurstModel model, WurstCompilerJassImpl compiler) {
        try {
            name = name.replaceAll("[^a-zA-Z0-9_]", "_");

            compiler.translateProgToIm(model);

            compiler.runCompiletime(WurstProjectConfigData.empty(), false, false);

            LuaCompilationUnit luaCode = compiler.transformProgToLua();
            LuaTranslator luaTranslator = compiler.getLuaTranslator();
            List<String> initFunctionNames = luaTranslator == null ? List.of() : luaTranslator.initFunctionNames();
            LuaAssertions.assertNamesAreValidIdentifiers(luaCode);
            checkLuaRootPurity(luaCode);
            StringBuilder sb = new StringBuilder();
            luaCode.print(sb, 0);

            File luaDir = new File(TEST_OUTPUT_PATH, "lua");
            luaDir.mkdirs();
            File luaFile = new File(luaDir, name + ".lua");
            String luaScript = sb.toString();

            // replace builtin lua functions

            FileUtils.write(luaScript, luaFile);

            // run with lua -l SimpleStatementTests_testIf1 -e 'main()'
            String luacExecutable = getLuacExecutable();
            checkLuaSyntax(luacExecutable, luaFile);

            if (executeProg) {
                String luaExecutable;
                try {
                    luaExecutable = getLuaExecutable();
                } catch (IllegalStateException e) {
                    throw new org.testng.SkipException(
                        "Skipped Lua execution (translation and luac syntax check still ran): " + e.getMessage());
                }
                // Preload the WC3 Lua runtime (Reforged blizzard.j dump + native shim)
                // when available, so tests execute against real BJ implementations.
                // The generated script only installs fallbacks for natives that are
                // still undefined afterwards.
                StringBuilder chunk = new StringBuilder();
                File runtimeDir = new File("src/test/resources/luaruntime");
                if (new File(runtimeDir, "wc3shim.lua").exists()) {
                    for (String runtimeFile : new String[]{"wc3shim.lua", "common.j.lua", "blizzard.j.lua"}) {
                        chunk.append("dofile('")
                            .append(new File(runtimeDir, runtimeFile).getPath().replace('\\', '/'))
                            .append("');");
                    }
                }
                chunk.append("dofile('").append(luaFile.getPath().replace('\\', '/')).append("');");
                // The generated script reports a package whose init raised, through the program's error
                // function, and goes on without it. The harness wraps each init function to see the raise
                // itself, whatever the error function prints, and re-raises it unchanged.
                chunk.append("local __wurst_testInits = {");
                for (String initFunctionName : initFunctionNames) {
                    chunk.append("'").append(initFunctionName).append("',");
                }
                chunk.append("};");
                chunk.append("for _, n in ipairs(__wurst_testInits) do local f = rawget(_G, n); if f then"
                    + " _G[n] = function(...) local r = table.pack(pcall(f, ...));"
                    + " if not r[1] then __wurst_testInitFailed = true; error(r[2], 0) end"
                    + " return table.unpack(r, 2, r.n) end end end;");
                // Success is read off stdout, so testSuccess has to print. A program with no
                // standard library gets a generated fallback which does; one with the library gets
                // the library's own, which is empty - so without this, a test on that target can
                // only ever be reported as not having succeeded, whatever it did.
                chunk.append("testSuccess = function() if __wurst_testInitFailed then print('__wurst_initFailed') end"
                    + " print('testSuccess') os.exit() end;");
                // Callback adapters can consume the error. Record an independent failure signal
                // before raising it so a later testSuccess cannot make the Java harness pass.
                chunk.append("testFail = function(message) print('__wurst_testFail'); error(message, 0) end;");
                chunk.append("main()");
                String[] args = {
                    luaExecutable,
                    "-e", chunk.toString()
                };
                Process p = Runtime.getRuntime().exec(args);
                StringBuilder errors = new StringBuilder();
                StringBuilder output = new StringBuilder();
                // Both pipes must be drained while the program runs, and the wait must end: a
                // generated program that loops forever would otherwise hang the whole suite,
                // and one that fills the stdout pipe would deadlock against a stderr-first read.
                java.util.concurrent.atomic.AtomicBoolean sawTestSuccess =
                    new java.util.concurrent.atomic.AtomicBoolean(false);
                java.util.concurrent.atomic.AtomicBoolean sawTestFail =
                    new java.util.concurrent.atomic.AtomicBoolean(false);
                java.util.concurrent.atomic.AtomicBoolean sawInitFailure =
                    new java.util.concurrent.atomic.AtomicBoolean(false);
                Thread outCollector = collectStreamAsync(p.getInputStream(), output,
                    java.util.Map.of("testSuccess", sawTestSuccess, "__wurst_testFail", sawTestFail,
                        "__wurst_initFailed", sawInitFailure));
                Thread errCollector = collectStreamAsync(p.getErrorStream(), errors);
                if (!p.waitFor(luaExecutionTimeoutSeconds(), TimeUnit.SECONDS)) {
                    p.destroyForcibly();
                    throw new Error(currentTestEnv + ": Lua program did not terminate within "
                        + luaExecutionTimeoutSeconds() + "s: " + luaFile.getName());
                }
                outCollector.join();
                errCollector.join();

                if (errors.length() > 0) {
                    System.err.print(errors);
                    throw new TestFailException(errors.toString());
                }

                if (sawTestFail.get()) {
                    throw new Error(currentTestEnv + ": Lua testFail was called\nLua output:\n" + output);
                }
                if (sawInitFailure.get()) {
                    throw new Error(currentTestEnv + ": a package failed to initialize\nLua output:\n" + output);
                }
                if (!sawTestSuccess.get()) {
                    throw new Error(currentTestEnv + ": Succeed function not called\nLua output:\n" + output);
                }
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    protected void checkLuaSyntax(String luacExecutable, File luaFile) throws IOException {
        Path outFile = java.nio.file.Files.createTempFile("wurst-luac-", ".out");
        outFile.toFile().deleteOnExit();
        ProcessBuilder pb = new ProcessBuilder(
            luacExecutable,
            "-o",
            outFile.toAbsolutePath().toString(),
            luaFile.getPath()
        );
        Process p = pb.start();
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        Thread outCollector = collectStreamAsync(p.getInputStream(), stdout);
        Thread errCollector = collectStreamAsync(p.getErrorStream(), stderr);
        boolean finished;
        try {
            finished = p.waitFor(20, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                throw new IOException("Lua syntax check timed out for " + luaFile.getName());
            }
            outCollector.join();
            errCollector.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted during Lua syntax check for " + luaFile.getName(), e);
        }
        if (p.exitValue() != 0 || stderr.length() > 0) {
            throw new IOException("Lua syntax check failed for " + luaFile.getName() + " using '" + luacExecutable + "'.\n"
                + "exit=" + p.exitValue() + "\n"
                + (stderr.length() > 0 ? "stderr:\n" + stderr : "")
                + (stdout.length() > 0 ? "\nstdout:\n" + stdout : ""));
        }
    }

    private Thread collectStreamAsync(InputStream stream, StringBuilder out) {
        return collectStreamAsync(stream, out, java.util.Collections.emptyMap());
    }

    /**
     * Drains {@code stream} into {@code out}, keeping only the first {@link #RETAINED_OUTPUT_LIMIT}
     * characters. Reading never stops (that would block the process and deadlock the run), but
     * a program that loops while printing would fill the heap before the timeout could fire.
     * <p>
     * Whatever the caller is looking for is recognised here rather than read back out of
     * {@code out} afterwards, because it may arrive after the limit: a program that prints a
     * million lines and then succeeds has still succeeded, and one that prints them and then fails
     * has still failed. Each line in {@code watchedLines} sets its flag when the program prints it.
     */
    private Thread collectStreamAsync(InputStream stream, StringBuilder out,
                                      java.util.Map<String, java.util.concurrent.atomic.AtomicBoolean> watchedLines) {
        int longestWatchedLine = 0;
        for (String watched : watchedLines.keySet()) {
            longestWatchedLine = Math.max(longestWatchedLine, watched.length());
        }
        final int maxWatchedLength = longestWatchedLine;
        Thread t = new Thread(() -> {
            // Fixed-size chunks rather than lines: a line is only bounded by what the program
            // chose to print, and reading one materialises all of it before any limit here could
            // apply. Nothing below holds more than the retained limit plus one chunk.
            char[] chunk = new char[8 * 1024];
            StringBuilder pendingLine = new StringBuilder();
            boolean lineIsLongerThanWatched = false;
            boolean truncated = false;
            try (Reader input = new InputStreamReader(stream)) {
                int read;
                while ((read = input.read(chunk)) >= 0) {
                    if (!watchedLines.isEmpty()) {
                        for (int i = 0; i < read; i++) {
                            char c = chunk[i];
                            if (c == '\n') {
                                if (!lineIsLongerThanWatched) {
                                    java.util.concurrent.atomic.AtomicBoolean saw = watchedLines.get(pendingLine.toString());
                                    if (saw != null) {
                                        saw.set(true);
                                    }
                                }
                                pendingLine.setLength(0);
                                lineIsLongerThanWatched = false;
                            } else if (c != '\r' && !lineIsLongerThanWatched) {
                                // Only ever as long as the longest line being looked for; past that
                                // the line cannot be any of them, so there is no reason to keep it.
                                if (pendingLine.length() == maxWatchedLength) {
                                    lineIsLongerThanWatched = true;
                                    pendingLine.setLength(0);
                                } else {
                                    pendingLine.append(c);
                                }
                            }
                        }
                    }
                    int room = RETAINED_OUTPUT_LIMIT - out.length();
                    if (room > 0) {
                        out.append(chunk, 0, Math.min(read, room));
                    }
                    // Said as soon as anything is dropped, including when that happens part way
                    // through the last chunk there is: otherwise the output ends at exactly the
                    // limit and reads as though that were all the program had to say.
                    if (read > room && !truncated) {
                        truncated = true;
                        out.append("\n... further output dropped after ")
                            .append(RETAINED_OUTPUT_LIMIT).append(" characters\n");
                    }
                }
            } catch (IOException ignored) {
            }
        });
        t.setDaemon(true);
        t.start();
        return t;
    }

    protected String getLuaExecutable() {
        if (resolvedLuaExecutable != null) {
            return resolvedLuaExecutable;
        }

        File bundledLuaWin = new File("src/test/resources/lua53.exe");
        File bundledLuaWinPlain = new File("src/test/resources/lua.exe");
        File bundledLuaUnix = new File("src/test/resources/lua53");
        String osName = System.getProperty("os.name", "").toLowerCase();
        boolean isWindows = osName.contains("win");

        List<String> candidates = new ArrayList<>();
        if (isWindows) {
            if (bundledLuaWin.exists()) {
                candidates.add(bundledLuaWin.getPath());
            }
            if (bundledLuaWinPlain.exists()) {
                candidates.add(bundledLuaWinPlain.getPath());
            }
            String cpWin = getOrExtractBundledLuaFromClasspath("lua53.exe", true, false);
            if (cpWin != null) {
                candidates.add(cpWin);
            }
            if (bundledLuaUnix.exists()) {
                candidates.add(bundledLuaUnix.getPath());
            }
            candidates.add("lua53.exe");
            candidates.add("lua");
        } else {
            // Prefer the distribution's versioned Lua 5.3 binary when present.
            // The checked-in portable binary may depend on an older system
            // readline ABI on newer Linux runner images.
            candidates.add("lua5.3");
            if (bundledLuaUnix.exists()) {
                // best effort in case execute bit was lost by checkout settings
                // (e.g. core.filemode false on some environments)
                bundledLuaUnix.setExecutable(true);
                if (bundledLuaUnix.canExecute()) {
                    candidates.add(bundledLuaUnix.getPath());
                }
            }
            String cpUnix = getOrExtractBundledLuaFromClasspath("lua53", false, false);
            if (cpUnix != null) {
                candidates.add(cpUnix);
            }
            candidates.add("lua53");
            candidates.add("lua");
        }

        for (String candidate : candidates) {
            if (isWorkingLuaExecutable(candidate)) {
                resolvedLuaExecutable = candidate;
                return candidate;
            }
        }

        throw new IllegalStateException(
            "No working Lua executable found. Tried: " + String.join(", ", candidates)
        );
    }

    protected String getLuacExecutable() {
        if (resolvedLuacExecutable != null) {
            return resolvedLuacExecutable;
        }

        File bundledLuacWin = new File("src/test/resources/luac53.exe");
        File bundledLuacUnix = new File("src/test/resources/luac53");
        String osName = System.getProperty("os.name", "").toLowerCase();
        boolean isWindows = osName.contains("win");

        List<String> candidates = new ArrayList<>();
        if (isWindows) {
            if (bundledLuacWin.exists()) {
                candidates.add(bundledLuacWin.getPath());
            }
            // allow legacy typo filename if present
            File legacyWin = new File("src/test/resources/luac32.exe");
            if (legacyWin.exists()) {
                candidates.add(legacyWin.getPath());
            }
            String cpWin = getOrExtractBundledLuaFromClasspath("luac53.exe", true, true);
            if (cpWin != null) {
                candidates.add(cpWin);
            }
            String cpLegacyWin = getOrExtractBundledLuaFromClasspath("luac32.exe", true, true);
            if (cpLegacyWin != null) {
                candidates.add(cpLegacyWin);
            }
            candidates.add("luac53.exe");
            candidates.add("luac.exe");
            candidates.add("luac");
        } else {
            candidates.add("luac5.3");
            if (bundledLuacUnix.exists()) {
                bundledLuacUnix.setExecutable(true);
                if (bundledLuacUnix.canExecute()) {
                    candidates.add(bundledLuacUnix.getPath());
                }
            }
            String cpUnix = getOrExtractBundledLuaFromClasspath("luac53", false, true);
            if (cpUnix != null) {
                candidates.add(cpUnix);
            }
            candidates.add("luac53");
            candidates.add("luac");
        }

        for (String candidate : candidates) {
            if (isWorkingLuacExecutable(candidate)) {
                resolvedLuacExecutable = candidate;
                return candidate;
            }
        }

        throw new IllegalStateException(
            "No working luac executable found. Tried: " + String.join(", ", candidates)
        );
    }

    private boolean isWorkingLuaExecutable(String executable) {
        ProcessBuilder pb = new ProcessBuilder(executable, "-e", "os.exit(0)");
        Process p;
        try {
            p = pb.start();
        } catch (IOException e) {
            return false;
        }
        try {
            boolean finished = p.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                return false;
            }
            return p.exitValue() == 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private boolean isWorkingLuacExecutable(String executable) {
        ProcessBuilder pb = new ProcessBuilder(executable, "-v");
        Process p;
        try {
            p = pb.start();
        } catch (IOException e) {
            return false;
        }
        try {
            boolean finished = p.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                return false;
            }
            return p.exitValue() == 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private String getOrExtractBundledLuaFromClasspath(String resourceName, boolean windowsBinary, boolean luacBinary) {
        try {
            if (windowsBinary && luacBinary && extractedLuacWin != null) {
                return extractedLuacWin;
            }
            if (windowsBinary && !luacBinary && extractedLuaWin != null) {
                return extractedLuaWin;
            }
            if (!windowsBinary && luacBinary && extractedLuacUnix != null) {
                return extractedLuacUnix;
            }
            if (!windowsBinary && !luacBinary && extractedLuaUnix != null) {
                return extractedLuaUnix;
            }
            InputStream in = WurstScriptTest.class.getClassLoader().getResourceAsStream(resourceName);
            if (in == null) {
                return null;
            }
            String suffix = windowsBinary ? ".exe" : "";
            Path temp = java.nio.file.Files.createTempFile("wurst-lua53-", suffix);
            temp.toFile().deleteOnExit();
            try (InputStream src = in) {
                java.nio.file.Files.copy(src, temp, StandardCopyOption.REPLACE_EXISTING);
            }
            if (!windowsBinary) {
                temp.toFile().setExecutable(true);
            }
            String path = temp.toAbsolutePath().toString();
            if (windowsBinary && luacBinary) {
                extractedLuacWin = path;
            } else if (windowsBinary) {
                extractedLuaWin = path;
            } else if (luacBinary) {
                extractedLuacUnix = path;
            } else {
                extractedLuaUnix = path;
            }
            return path;
        } catch (IOException e) {
            return null;
        }
    }

    private void checkLuaRootPurity(LuaCompilationUnit luaCode) {
        Set<String> forbiddenRootCalls = Set.of(
            "CreateTrigger", "CreateTimer", "CreateUnit", "DestroyTrigger", "TimerStart",
            "CreateTimerBJ", "StartTimerBJ", "GetLastCreatedTimerBJ",
            "InitHashtable", "SaveInteger", "SaveBoolean", "SaveReal", "SaveStr",
            "LoadInteger", "LoadBoolean", "LoadReal", "LoadStr", "FlushChildHashtable"
        );

        for (LuaStatement stmt : luaCode) {
            if (stmt instanceof LuaChunkLocal local) {
                // A main-chunk local is initialised at the root like a global.
                stmt = local.getDefinition();
            }
            if (stmt instanceof LuaVariable) {
                LuaExprOpt initialValue = ((LuaVariable) stmt).getInitialValue();
                if (initialValue instanceof LuaExpr) {
                    assertNoForbiddenRootCall((LuaExpr) initialValue, forbiddenRootCalls);
                }
            } else if (stmt instanceof LuaAssignment) {
                assertNoForbiddenRootCall(((LuaAssignment) stmt).getRight(), forbiddenRootCalls);
            } else if (stmt instanceof LuaExprFunctionCallByName) {
                String funcName = ((LuaExprFunctionCallByName) stmt).getFuncName();
                if (forbiddenRootCalls.contains(funcName)) {
                    throw new Error("Lua root purity violation: forbidden root call to " + funcName);
                }
            } else if (stmt instanceof LuaExprFunctionCall) {
                String funcName = ((LuaExprFunctionCall) stmt).getFunc().getName();
                if (forbiddenRootCalls.contains(funcName)) {
                    throw new Error("Lua root purity violation: forbidden root call to " + funcName);
                }
            }
        }
    }

    private void assertNoForbiddenRootCall(LuaExpr expr, Set<String> forbiddenRootCalls) {
        if (expr instanceof LuaExprFunctionCallByName) {
            String name = ((LuaExprFunctionCallByName) expr).getFuncName();
            if (forbiddenRootCalls.contains(name)) {
                throw new Error("Lua root purity violation: forbidden root call to " + name);
            }
            for (LuaExpr arg : ((LuaExprFunctionCallByName) expr).getArguments()) {
                assertNoForbiddenRootCall(arg, forbiddenRootCalls);
            }
        } else if (expr instanceof LuaExprFunctionCall) {
            String name = ((LuaExprFunctionCall) expr).getFunc().getName();
            if (forbiddenRootCalls.contains(name)) {
                throw new Error("Lua root purity violation: forbidden root call to " + name);
            }
            for (LuaExpr arg : ((LuaExprFunctionCall) expr).getArguments()) {
                assertNoForbiddenRootCall(arg, forbiddenRootCalls);
            }
        } else if (expr instanceof LuaExprFunctionCallE) {
            LuaExprFunctionCallE call = (LuaExprFunctionCallE) expr;
            assertNoForbiddenRootCall(call.getFuncExpr(), forbiddenRootCalls);
            for (LuaExpr arg : call.getArguments()) {
                assertNoForbiddenRootCall(arg, forbiddenRootCalls);
            }
        } else if (expr instanceof LuaExprArrayAccess) {
            LuaExprArrayAccess a = (LuaExprArrayAccess) expr;
            assertNoForbiddenRootCall(a.getLeft(), forbiddenRootCalls);
            for (LuaExpr index : a.getIndexes()) {
                assertNoForbiddenRootCall(index, forbiddenRootCalls);
            }
        } else if (expr instanceof LuaExprBinary) {
            LuaExprBinary b = (LuaExprBinary) expr;
            assertNoForbiddenRootCall(b.getLeftExpr(), forbiddenRootCalls);
            assertNoForbiddenRootCall(b.getRight(), forbiddenRootCalls);
        } else if (expr instanceof LuaExprUnary) {
            assertNoForbiddenRootCall(((LuaExprUnary) expr).getRight(), forbiddenRootCalls);
        } else if (expr instanceof LuaExprFieldAccess) {
            assertNoForbiddenRootCall(((LuaExprFieldAccess) expr).getReceiver(), forbiddenRootCalls);
        } else if (expr instanceof LuaTableConstructor) {
            for (LuaTableField field : ((LuaTableConstructor) expr).getTableFields()) {
                if (field instanceof LuaTableNamedField) {
                    assertNoForbiddenRootCall(((LuaTableNamedField) field).getVal(), forbiddenRootCalls);
                } else if (field instanceof LuaTableExprField) {
                    assertNoForbiddenRootCall(((LuaTableExprField) field).getFieldKey(), forbiddenRootCalls);
                    assertNoForbiddenRootCall(((LuaTableExprField) field).getVal(), forbiddenRootCalls);
                } else if (field instanceof LuaTableSingleField) {
                    assertNoForbiddenRootCall(((LuaTableSingleField) field).getVal(), forbiddenRootCalls);
                }
            }
        }
    }

    private void translateAndTest(String name, boolean executeProg,
                                  boolean executeTests, WurstGui gui, WurstCompilerJassImpl compiler,
                                  WurstModel model, boolean executeProgOnlyAfterTransforms) throws Error {
        ImProg imProg = compiler.translateProgToIm(model);


        if (gui.getErrorCount() > 0) {
            throw gui.getErrorList().get(0);
        }


        writeJassImProg(name, gui, imProg);
        if (!executeProgOnlyAfterTransforms) {
            // we want to test that the interpreter works correctly before transforming the program in the translation step
            if (executeTests) {
                testsRun = Math.max(testsRun, executeTests(gui, compiler.getImTranslator(), imProg));
            }
            if (executeProg) {
                WLogger.info("Executing imProg before jass transformation");
                String currentEnv = currentTestEnv;
                setCurrentTestEnv("ImProg before jass transformation");
                executeImProg(gui, imProg);
                currentTestEnv = currentEnv;
            }
        }

        compiler.runCompiletime(WurstProjectConfigData.empty(), false, false);
        JassProg prog = compiler.transformProgToJass();
        writeJassImProg(name, gui, imProg);
        if (gui.getErrorCount() > 0) {
            throw gui.getErrorList().get(0);
        }

        if (executeTests) {
            testsRun = Math.max(testsRun, executeTests(gui, compiler.getImTranslator(), imProg));
        }
        if (executeProg) {
            WLogger.info("Executing imProg after jass transformation");
            String currentEnv = currentTestEnv;
            setCurrentTestEnv(currentTestEnv + "-ImProg");
            executeImProg(gui, imProg);
            currentTestEnv = currentEnv;
        }


        if (gui.getErrorCount() > 0) {
            throw gui.getErrorList().get(0);
        }
        Assert.assertNotNull(prog);

        File outputFile = writeJassProg(name, gui, prog);

        pjassPending.add(outputFile);

        if (executeProg) {
            String currentEnv = currentTestEnv;
            setCurrentTestEnv(currentTestEnv + "-JassProg");
            executeJassProg(prog);
            currentTestEnv = currentEnv;
        }
    }

    /** The number of non-overlapping occurrences of {@code needle} in {@code text}. */
    protected static int countOccurrences(String text, String needle) {
        int count = 0;
        for (int at = text.indexOf(needle); at >= 0; at = text.indexOf(needle, at + needle.length())) {
            count++;
        }
        return count;
    }

    private static File getFile(String name) {
        return new File(WurstScriptTest.class.getClassLoader().getResource(name).getFile());
    }

    WurstModel parseFiles(Iterable<File> inputFiles,
                          Map<String, String> inputs, boolean withStdLib,
                          WurstCompilerJassImpl compiler) {
        List<CU> inputList = inputs.entrySet().stream()
                .map(e -> new CU(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        return parseFiles(inputFiles, inputList, withStdLib, compiler);
    }

    WurstModel parseFiles(Iterable<File> inputFiles,
                          List<CU> inputs, boolean withStdLib,
                          WurstCompilerJassImpl compiler) {
        if (inputFiles == null) {
            inputFiles = Collections.emptyList();
        }
        if (inputs == null) {
            inputs = Collections.emptyList();
        }

        if (withStdLib) {
            compiler.loadFiles(getFile("common.j"), getFile("blizzard.j"));
        }
        for (File input : inputFiles) {
            compiler.loadFiles(input);
        }
        for (CU input : inputs) {
            compiler.loadReader(input.name, new StringReader(input.content));
        }
        return compiler.parseFiles();
    }


    /**
     * Scripts already checked by pjass in this JVM, by content hash.
     *
     * <p>About a third of the scripts this suite emits are byte-identical to one already checked
     * (the same source compiled under several optimisation levels, and near-identical fixtures
     * within a test class), and pjass is a pure function of its input, so checking those again
     * cannot tell us anything new.
     *
     * <p>Only successes are recorded: a failure re-runs so the reported message names the file the
     * caller actually passed.
     */
    private static final Set<String> pjassCheckedScripts = ConcurrentHashMap.newKeySet();

    /** The scripts a test has emitted and pjass has not checked yet. */
    private final List<File> pjassPending = new ArrayList<>();

    /**
     * Checks the scripts the test has emitted so far with one pjass process. Starting pjass and
     * parsing common.j and blizzard.j (~15k lines) once per script was most of what the tests that
     * emit little spend, and most of that on Windows, where CreateProcess is an order of magnitude
     * dearer than fork/exec.
     */
    private void runPendingPjass() throws Error {
        List<File> files = new ArrayList<>();
        List<String> digests = new ArrayList<>();
        for (File file : pjassPending) {
            String digest = scriptDigest(file);
            if (digest != null && (pjassCheckedScripts.contains(digest) || digests.contains(digest))) {
                continue;
            }
            files.add(file);
            digests.add(digest);
        }
        pjassPending.clear();
        if (files.isEmpty()) {
            return;
        }
        List<Result> results = Pjass.runPjassEach(files);
        for (int i = 0; i < files.size(); i++) {
            Result pJassResult = results.get(i);
            WLogger.info(pJassResult.getMessage());
            if (!pJassResult.isOk() && !pJassResult.getMessage().equals("IO Exception")) {
                throw new Error(pJassResult.getMessage() + pJassResult.getErrors());
            }
            // Only a real pass may be recorded. An "IO Exception" result is deliberately not fatal, but
            // it means pjass never validated this script - caching it would make every later identical
            // script skip validation too, after a failure that may well have been transient.
            if (digests.get(i) != null && pJassResult.isOk()) {
                pjassCheckedScripts.add(digests.get(i));
            }
        }
    }

    /** Content hash of a generated script, or null if it cannot be read - in which case pjass runs. */
    private static String scriptDigest(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(java.nio.file.Files.readAllBytes(file.toPath())));
        } catch (IOException | NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static String currentTestEnv = "";

    public static void setCurrentTestEnv(String env) {
        currentTestEnv = env;
        System.out.println("Current test environment: " + currentTestEnv);
    }

    private void executeImProg(WurstGui gui, ImProg imProg) throws TestFailException {
        try {
            // run the interpreter on the intermediate language
            ILInterpreter interpreter = new ILInterpreter(imProg, gui, Optional.empty(), false);
            interpreter.addNativeProvider(new ReflectionNativeProvider(interpreter));
            interpreter.executeFunction("main", null);
        } catch (TestSuccessException e) {
            System.out.println(currentTestEnv + ": Suceed function called!");
            return;
        }
        throw new Error(currentTestEnv + ": Succeed function not called");
    }

    private void executeJassProg(JassProg prog)
            throws TestFailException {
        try {
            // run the interpreter with the optimized program
            JassInterpreter interpreter = new JassInterpreter();
            interpreter.trace(true);
            interpreter.loadProgram(prog);
            interpreter.runProgram();
        } catch (TestSuccessException e) {
            return;
        }
        throw new Error(currentTestEnv + ": Succeed function not called");
    }

    /** @return how many tests ran, so a caller can tell "all passed" from "there were none". */
    private int executeTests(WurstGui gui, ImTranslator translator, ImProg imProg) {
        RunTests runTests = new RunTests(Optional.empty(), 0, 0, Optional.empty());
        RunTests.TestResult res = runTests.runTests(translator, imProg, Optional.empty(), Optional.empty());
        if (res.getPassedTests() < res.getTotalTests()) {
            throw new Error("tests failed: " + res.getPassedTests() + " / " + res.getTotalTests() + "\n" +
                    describeFailures(runTests) + gui.getErrors());
        }
        return res.getTotalTests();
    }

    /**
     * Names the tests which failed, and says how each one did.
     * <p>
     * The count alone does not say which of several hundred it was, and the name is only printed to
     * stdout, which a CI run does not keep - so a failure on a runner one does not have says that one
     * test of the library failed and nothing else. Ahead of the warnings deliberately: a library
     * compiles with hundreds of them, and a report which truncates a long message cuts off the end,
     * which is where the name would otherwise sit.
     */
    private static String describeFailures(RunTests runTests) {
        List<RunTests.TestFailure> failures = runTests.getFailTests();
        if (failures.isEmpty()) {
            // The counts disagreed without a recorded failure, which is itself worth saying rather
            // than leaving a blank where the explanation belongs.
            return "no failure was recorded, which is the thing to look at\n";
        }
        StringBuilder sb = new StringBuilder();
        for (RunTests.TestFailure failure : failures) {
            sb.append("FAILED ").append(failure.getFunction().getName());
            String message = failure.getMessage();
            if (message != null && !message.isEmpty()) {
                sb.append(": ").append(message);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * writes a jass prog to a file
     */
    private File writeJassProg(String name, WurstGui gui, JassProg prog) throws Error {
        File outputFile = new File(TEST_OUTPUT_PATH + name + ".j");
        new File(TEST_OUTPUT_PATH).mkdirs();
        try {
            StringBuilder sb = new StringBuilder();
            new JassPrinter(true, prog).printProg(sb);

            FileUtils.write(sb, outputFile);
        } catch (IOException e) {
            throw new Error("IOException, could not write jass file " + outputFile + "\n" + gui.getErrors());
        }
        return outputFile;
    }

    /**
     * writes the IM of a program to a file, if the test or the run asked for IM dumps
     */
    private void writeJassImProg(String name, WurstGui gui, ImProg prog) throws Error {
        if (!writeImDumps) {
            return;
        }
        File outputFile = new File(TEST_OUTPUT_PATH + name + ".jim");
        new File(TEST_OUTPUT_PATH).mkdirs();
        try {
            try (Writer w = Files.newWriter(outputFile, Charsets.UTF_8)) {
                prog.print(w, 0);
            }
        } catch (IOException e) {
            throw new Error("IOException, could not write jass file " + outputFile + "\n" + gui.getErrors());
        }
    }


}
