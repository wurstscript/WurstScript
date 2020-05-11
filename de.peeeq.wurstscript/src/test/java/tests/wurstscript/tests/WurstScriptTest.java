package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
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
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Utils;
import org.testng.Assert;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.testng.Assert.fail;

public class WurstScriptTest {

    private static final String TEST_OUTPUT_PATH = "./test-output/";

    protected boolean testOptimizer() {
        return true;
    }

    protected boolean printDebugScripts() {
        return false;
    }

    class TestConfig {
        private String name;
        private boolean withStdLib;
        private boolean executeProg;
        private boolean executeTests;
        private boolean executeProgOnlyAfterTransforms;
        private String expectedError;
        private List<File> inputFiles = new ArrayList<>();
        private List<CU> additionalCompilationUnits = new ArrayList<>();
        private boolean stopOnFirstError = true;
        private boolean runCompiletimeFunctions;
        private boolean testLua = false;

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

        public TestConfig executeTests(boolean b) {
            this.executeTests = b;
            return this;
        }

        TestConfig executeProg(boolean b) {
            this.executeProg = b;
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
            RunArgs runArgs = new RunArgs();
            if (withStdLib) {
                runArgs = runArgs.with("-lib", StdLib.getLib());
            }
            if (runCompiletimeFunctions) {
                runArgs = runArgs.with("-runcompiletimefunctions");
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

            // translate with different options:

            testWithoutInliningAndOptimization(name, executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms, runArgs);

            testWithLocalOptimizations(name, executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms, runArgs);

            testWithInlining(name, executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms, runArgs);

            testWithInliningAndOptimizations(name, executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms, runArgs);

            testWithInliningAndOptimizationsAndStacktraces(name, executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms, runArgs);

            if (testLua && executeProg && !withStdLib) {
                // test lua translation
                runArgs = runArgs.with("-lua");
                compiler.setRunArgs(runArgs);
                translateAndTestLua(name, executeProg, gui, model, compiler);
            }

            return new CompilationResult(model, gui);
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
            this.testLua = b;
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

    protected WurstModel testScript(String name, boolean executeProg, String prog) {
        return test().executeProg(executeProg).lines(prog).getModel();
    }


    private void testWithInliningAndOptimizations(String name, boolean executeProg, boolean executeTests, WurstGui gui,
                                                  WurstCompilerJassImpl compiler, WurstModel model, boolean executeProgOnlyAfterTransforms, RunArgs runArgs) throws Error {
        // test with inlining and local optimization
        compiler.setRunArgs(runArgs.with("-inline", "-localOptimizations"));
        translateAndTest(name + "_inlopt", executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);
    }

    private void testWithInliningAndOptimizationsAndStacktraces(String name, boolean executeProg, boolean executeTests, WurstGui gui,
                                                  WurstCompilerJassImpl compiler, WurstModel model, boolean executeProgOnlyAfterTransforms, RunArgs runArgs) throws Error {
        // test with inlining and local optimization
        compiler.setRunArgs(runArgs.with("-inline", "-localOptimizations", "-stacktraces"));
        translateAndTest(name + "_stacktraceinlopt", executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);
    }

    private void testWithInlining(String name, boolean executeProg, boolean executeTests, WurstGui gui
            , WurstCompilerJassImpl compiler, WurstModel model, boolean executeProgOnlyAfterTransforms
            , RunArgs runArgs) throws Error {
        // test with inlining
        compiler.setRunArgs(runArgs.with("-inline"));
        translateAndTest(name + "_inl", executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);
    }

    private void testWithLocalOptimizations(String name, boolean executeProg, boolean executeTests, WurstGui gui,
                                            WurstCompilerJassImpl compiler, WurstModel model, boolean executeProgOnlyAfterTransforms, RunArgs runArgs) throws Error {
        // test with local optimization
        compiler.setRunArgs(runArgs.with("-localOptimizations"));
        translateAndTest(name + "_opt", executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);
    }

    private void testWithoutInliningAndOptimization(String name, boolean executeProg, boolean executeTests,
                                                    WurstGui gui, WurstCompilerJassImpl compiler, WurstModel model, boolean executeProgOnlyAfterTransforms, RunArgs runArgs)
            throws Error {
        compiler.setRunArgs(runArgs);
        // test without inlining and optimization
        translateAndTest(name, executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);
    }

    private void translateAndTestLua(String name, boolean executeProg, WurstGui gui, WurstModel model, WurstCompilerJassImpl compiler) {
        try {
            name = name.replaceAll("[^a-zA-Z0-9_]", "_");

            compiler.translateProgToIm(model);

            compiler.runCompiletime();

            LuaCompilationUnit luaCode = compiler.transformProgToLua();
            StringBuilder sb = new StringBuilder();
            luaCode.print(sb, 0);

            File luaDir = new File(TEST_OUTPUT_PATH, "lua");
            luaDir.mkdirs();
            File luaFile = new File(luaDir, name + ".lua");
            String luaScript = sb.toString();

            // replace builtin lua functions

            FileUtils.write(luaScript, luaFile);

            // run with lua -l SimpleStatementTests_testIf1 -e 'main()'

            if (executeProg) {
                String line;
                String[] args = {
                    "lua",
                    "-l", luaFile.getPath().replace(".lua", ""),
                    "-e", "main()"
                };
                Process p = Runtime.getRuntime().exec(args);
                StringBuilder errors = new StringBuilder();
                StringBuilder output = new StringBuilder();
                try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    while ((line = input.readLine()) != null) {
                        System.err.println(line);
                        errors.append(line);
                        errors.append("\n");
                    }
                }

                if (errors.length() > 0) {
                    throw new TestFailException(errors.toString());
                }

                boolean success = false;
                try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    while ((line = input.readLine()) != null) {
                        if (line.equals("testSuccess")) {
                            success = true;
                        }
                        output.append(line);
                        output.append("\n");
                    }
                }
                if (!success) {
                    throw new Error("Succeed function not called");
                }
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
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
                executeTests(gui, compiler.getImTranslator(), imProg);
            }
            if (executeProg) {
                executeImProg(gui, imProg);
            }
        }

        compiler.runCompiletime();
        JassProg prog = compiler.transformProgToJass();
        writeJassImProg(name, gui, imProg);
        if (gui.getErrorCount() > 0) {
            throw gui.getErrorList().get(0);
        }

        if (executeTests) {
            executeTests(gui, compiler.getImTranslator(), imProg);
        }
        if (executeProg) {
            executeImProg(gui, imProg);
        }


        if (gui.getErrorCount() > 0) {
            throw gui.getErrorList().get(0);
        }
        Assert.assertNotNull(prog);

        File outputFile = writeJassProg(name, gui, prog);

        // run pjass:
        runPjass(outputFile);

        if (executeProg) {
            executeJassProg(prog);
        }
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


    private void runPjass(File outputFile) throws Error {
        Result pJassResult = Pjass.runPjass(outputFile);
        WLogger.info(pJassResult.getMessage());
        if (!pJassResult.isOk() && !pJassResult.getMessage().equals("IO Exception")) {
            throw new Error(pJassResult.getMessage() + pJassResult.getErrors());
        }
    }

    private void executeImProg(WurstGui gui, ImProg imProg) throws TestFailException {
        try {
            // run the interpreter on the intermediate language
            ILInterpreter interpreter = new ILInterpreter(imProg, gui, Optional.empty(), false);
            interpreter.addNativeProvider(new ReflectionNativeProvider(interpreter));
            interpreter.executeFunction("main", null);
        } catch (TestSuccessException e) {
            return;
        }
        throw new Error("Succeed function not called");
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
        throw new Error("Succeed function not called");
    }

    private void executeTests(WurstGui gui, ImTranslator translator, ImProg imProg) {
        RunTests runTests = new RunTests(Optional.empty(), 0, 0, Optional.empty());
        RunTests.TestResult res = runTests.runTests(translator, imProg, Optional.empty(), Optional.empty());
        if (res.getPassedTests() < res.getTotalTests()) {
            throw new Error("tests failed: " + res.getPassedTests() + " / " + res.getTotalTests() + "\n" +
                    gui.getErrors());
        }
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
     * writes a jass prog to a file
     */
    private File writeJassImProg(String name, WurstGui gui, ImProg prog) throws Error {
        File outputFile = new File(TEST_OUTPUT_PATH + name + ".jim");
        new File(TEST_OUTPUT_PATH).mkdirs();
        try {
            try (Writer w = Files.newWriter(outputFile, Charsets.UTF_8)) {
                prog.print(w, 0);
            }
        } catch (IOException e) {
            throw new Error("IOException, could not write jass file " + outputFile + "\n" + gui.getErrors());
        }
        return outputFile;
    }


}
