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
import de.peeeq.wurstscript.lua.translation.LuaTranslator;
import de.peeeq.wurstscript.luaAst.LuaCompilationUnit;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Utils;
import org.testng.Assert;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.google.common.io.Files.asCharSink;
import static org.testng.Assert.fail;

public class WurstScriptTest {

    private static final String TEST_OUTPUT_PATH = "./test-output/";
    private static final boolean testLua = false;

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

        TestConfig(String name) {
            this.name = name;
        }

        TestConfig withStdLib() {
            this.withStdLib = true;
            return this;
        }

        TestConfig withStdLib(boolean b) {
            this.withStdLib = b;
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

        TestConfig executeProg(boolean b) {
            this.executeProg = b;
            return this;
        }

        public TestConfig executeProgOnlyAfterTransforms() {
            this.executeProgOnlyAfterTransforms = true;
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
            Map<String, String> inputs = additionalCompilationUnits.stream()
                    .collect(Collectors.toMap(cu -> cu.name, cu -> cu.content));

            try {
                WurstModel model = testScript(inputFiles, inputs, name, executeProg, withStdLib, executeTests, executeProgOnlyAfterTransforms);
                if (expectedError != null) {
                    fail("No errors were discovered");
                }
                return new CompilationResult(model);
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

        public void file(File file) throws IOException {
            String content = Files.toString(file, StandardCharsets.UTF_8);
            additionalCompilationUnits.add(new CU(file.getName(), content));
            run();
        }

        public TestConfig withCu(CU cu) {
            additionalCompilationUnits.add(cu);
            return this;
        }
    }

    static class CompilationResult {
        private final WurstModel model;

        public CompilationResult(WurstModel model) {

            this.model = model;
        }

        public WurstModel getModel() {
            return model;
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

    WurstModel testScript(String inputName, String input, String name, boolean executeProg, boolean withStdLib) {
        return test().executeProg(executeProg).withStdLib(withStdLib).withCu(compilationUnit(inputName, name)).run().getModel();
    }


    WurstModel testScript(Iterable<File> inputFiles, Map<String, String> inputs, String name, boolean executeProg, boolean withStdLib, boolean
            executeTests, boolean executeProgOnlyAfterTransforms) {
        RunArgs runArgs = new RunArgs("-lib", StdLib.getLib());
        WurstGui gui = new WurstGuiCliImpl();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui, null, runArgs);
        compiler.getErrorHandler().enableUnitTestMode();
        WurstModel model = parseFiles(inputFiles, inputs, withStdLib, compiler);


        if (!gui.getErrorList().isEmpty()) {
            throw gui.getErrorList().get(0);
        }


        // check prog
        compiler.checkProg(model);
        if (!gui.getErrorList().isEmpty()) {
            throw gui.getErrorList().get(0);
        }


        if (name.toLowerCase().contains("warning") && !gui.getWarningList().isEmpty()) {
            // report warnings based on naming convention
            throw gui.getWarningList().get(0);
        }

        // translate with different options:

        testWithoutInliningAndOptimization(name, executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);

        testWithLocalOptimizations(name, executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);

        testWithInlining(name, executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);

        testWithInliningAndOptimizations(name, executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);

        if (testLua && !withStdLib) {
            // test lua translation
            compiler.setRunArgs(new RunArgs("-lua"));
            translateAndTestLua(name, executeProg, gui, model);
        }
        return model;

    }

    private void testWithInliningAndOptimizations(String name, boolean executeProg, boolean executeTests, WurstGui gui,
                                                  WurstCompilerJassImpl compiler, WurstModel model, boolean executeProgOnlyAfterTransforms) throws Error {
        // test with inlining and local optimization
        compiler.setRunArgs(new RunArgs("-inline", "-localOptimizations"));
        translateAndTest(name + "_inlopt", executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);
    }

    private void testWithInlining(String name, boolean executeProg, boolean executeTests, WurstGui gui,
                                  WurstCompilerJassImpl compiler, WurstModel model, boolean executeProgOnlyAfterTransforms) throws Error {
        // test with inlining
        compiler.setRunArgs(new RunArgs("-inline"));
        translateAndTest(name + "_inl", executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);
    }

    private void testWithLocalOptimizations(String name, boolean executeProg, boolean executeTests, WurstGui gui,
                                            WurstCompilerJassImpl compiler, WurstModel model, boolean executeProgOnlyAfterTransforms) throws Error {
        // test with local optimization
        compiler.setRunArgs(new RunArgs("-localOptimizations"));
        translateAndTest(name + "_opt", executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);
    }

    private void testWithoutInliningAndOptimization(String name, boolean executeProg, boolean executeTests,
                                                    WurstGui gui, WurstCompilerJassImpl compiler, WurstModel model, boolean executeProgOnlyAfterTransforms)
            throws Error {
        // test without inlining and optimization
        translateAndTest(name, executeProg, executeTests, gui, compiler, model, executeProgOnlyAfterTransforms);
    }

    private void translateAndTestLua(String name, boolean executeProg, WurstGui gui, WurstModel model) {
        try {
            name = name.replaceAll("[^a-zA-Z0-9_]", "_");

            ImTranslator imTranslator = new ImTranslator(model, true);
            ImProg imProg = imTranslator.translateProg();

            LuaTranslator luaTranslator = new LuaTranslator(imProg);
            LuaCompilationUnit luaCode = luaTranslator.translate();
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
                executeTests(gui, imProg);
            }
            if (executeProg) {
                executeImProg(gui, imProg);
            }
        }

        JassProg prog = compiler.transformProgToJass();
        if (gui.getErrorCount() > 0) {
            throw gui.getErrorList().get(0);
        }

        if (executeTests) {
            executeTests(gui, imProg);
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
        if (inputFiles == null) {
            inputFiles = Collections.emptyList();
        }
        if (inputs == null) {
            inputs = Collections.emptyMap();
        }

        if (withStdLib) {
            compiler.loadFiles(getFile("common.j"), getFile("blizzard.j"));
        }
        for (File input : inputFiles) {
            compiler.loadFiles(input);
        }
        for (Entry<String, String> input : inputs.entrySet()) {
            compiler.loadReader(input.getKey(), new StringReader(input.getValue()));
        }
        return compiler.parseFiles();
    }


    private void runPjass(File outputFile) throws Error {
        Result pJassResult = Pjass.runPjass(outputFile);
        WLogger.info(pJassResult.getMessage());
        if (!pJassResult.isOk() && !pJassResult.getMessage().equals("IO Exception")) {
            throw new Error(pJassResult.getMessage());
        }
    }

    private void executeImProg(WurstGui gui, ImProg imProg) throws TestFailException {
        try {
            // run the interpreter on the intermediate language
            ILInterpreter interpreter = new ILInterpreter(imProg, gui, null, false);
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

    private void executeTests(WurstGui gui, ImProg imProg) {
        RunTests runTests = new RunTests(null, 0, 0);
        RunTests.TestResult res = runTests.runTests(imProg, null, null);
        if (res.getPassedTests() < res.getTotalTests()) {
            throw new Error("tests failed");
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
            StringBuilder sb = new StringBuilder();
            prog.print(sb, 0);
            FileUtils.write(sb, outputFile);
        } catch (IOException e) {
            throw new Error("IOException, could not write jass file " + outputFile + "\n" + gui.getErrors());
        }
        return outputFile;
    }


}
