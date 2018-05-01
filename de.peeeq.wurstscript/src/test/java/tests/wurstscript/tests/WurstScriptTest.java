package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import de.peeeq.wurstio.CompiletimeFunctionRunner;
import de.peeeq.wurstio.Pjass;
import de.peeeq.wurstio.Pjass.Result;
import de.peeeq.wurstio.UtilsIO;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.jassinterpreter.JassInterpreter;
import de.peeeq.wurstio.jassinterpreter.ReflectionNativeProvider;
import de.peeeq.wurstio.utils.FileReading;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.lua.translation.LuaTranslator;
import de.peeeq.wurstscript.luaAst.LuaCompilationUnit;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;
import org.testng.Assert;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static de.peeeq.wurstio.CompiletimeFunctionRunner.FunctionFlagToRun.Tests;

public class WurstScriptTest {

    private static final String TEST_OUTPUT_PATH = "./test-output/";
    private static final boolean testLua = false;
    private Map<String, String> inputs = Maps.newLinkedHashMap();

    protected boolean testOptimizer() {
        return true;
    }

    static class CU {
        final public String content;
        final public String name;

        public CU(String name, String content) {
            this.name = name;
            this.content = content;
        }
    }

    public CU compilationUnit(String name, String... input) {
        return new CU(name, Utils.join(input, "\n"));
    }

    public void testAssertOk(boolean excuteProg, boolean withStdLib, CU... units) {
        List<File> inputFiles = Collections.emptyList();
        inputs.clear();
        for (CU cu : units) {
            inputs.put(cu.name, cu.content);
        }
        String name = UtilsIO.getMethodName(2);
        testScript(inputFiles, inputs, name, excuteProg, withStdLib, false);
    }

    public void testAssertErrors(String errorMessage, boolean excuteProg, boolean withStdLib, CU... units) {
        List<File> inputFiles = Collections.emptyList();
        inputs.clear();
        for (CU cu : units) {
            inputs.put(cu.name, cu.content);
        }
        String name = UtilsIO.getMethodName(2);
        try {
            testScript(inputFiles, inputs, name, excuteProg, withStdLib, false);
            Assert.assertTrue(false, "No errors were discovered");
        } catch (CompileError e) {
            Assert.assertTrue(e.getMessage().contains(errorMessage), e.getMessage());
        }
    }


    public void testAssertOkLines(boolean executeProg, String... input) {
        String prog = Utils.join(input, "\n") + "\n";
        testAssertOk(UtilsIO.getMethodName(1), executeProg, prog);
    }

    public void testAssertErrorsLines(boolean executeProg, String errorMessage, String... input) {
        String prog = Utils.join(input, "\n") + "\n";
        testAssertErrors(UtilsIO.getMethodName(1), executeProg, prog, errorMessage);
    }

    public void testAssertOk(String name, boolean executeProg, String prog) {
        if (name.length() == 0) {
            name = UtilsIO.getMethodName(1);
        }
        testScript(name, prog, this.getClass().getSimpleName() + "_" + name, executeProg, false);
    }

    public void testAssertOkFile(File file, boolean executeProg) throws IOException {
        Reader reader = FileReading.getFileReader(file);
        testScript(Collections.singleton(file), null, file.getName(), executeProg, false, false);
        reader.close();
    }

    public void testAssertOkFileWithStdLib(File file, boolean executeProg) throws IOException {
        String input = Files.toString(file, Charsets.UTF_8);
        testScript(file.getAbsolutePath(), input, file.getName(), executeProg, true);
    }

    public void testAssertOkLinesWithStdLib(boolean executeProg, String... input) {
        String prog = Utils.join(input, "\n") + "\n";
        String name = UtilsIO.getMethodName(1);
        testScript(name, prog, this.getClass().getSimpleName() + "_" + name, executeProg, true);
    }

    public void testAssertErrorFileWithStdLib(File file, String errorMessage, boolean executeProg) throws IOException {
        try {
            String input = Files.toString(file, Charsets.UTF_8);
            testScript(file.getAbsolutePath(), input, file.getName(), executeProg, true);
        } catch (CompileError e) {
            Assert.assertTrue(e.getMessage().contains(errorMessage), e.toString());
        }
    }

    public void testAssertErrors(String name, boolean executeProg, String prog, String errorMessage) {
        name = UtilsIO.getMethodName(2);
        try {
            testScript(name, prog, this.getClass().getSimpleName() + "_" + name, executeProg, false);
            Assert.assertTrue(false, "No errors were discovered");
        } catch (CompileError e) {
            if (!e.getMessage().toLowerCase().contains(errorMessage.toLowerCase())) {
                throw e;
            }
        }


    }

    public WurstModel testScript(String name, boolean executeProg, String prog) {
        if (name.length() == 0) {
            name = UtilsIO.getMethodName(1);
        }
        return testScript(name, prog, this.getClass().getSimpleName() + "_" + name, executeProg, false);
    }

    protected WurstModel testScript(String inputName, String input, String name, boolean executeProg, boolean withStdLib) {
        inputs.clear();
        inputs.put(inputName, input);
        return testScript(null, inputs, name, executeProg, withStdLib, false);
    }

    protected WurstModel testScript(Iterable<File> inputFiles, Map<String, String> inputs, String name, boolean executeProg, boolean withStdLib, boolean
            executeTests) {
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

        testWithoutInliningAndOptimization(name, executeProg, executeTests, gui, compiler, model);

        testWithLocalOptimizations(name, executeProg, executeTests, gui, compiler, model);

        testWithInlining(name, executeProg, executeTests, gui, compiler, model);

        testWithInliningAndOptimizations(name, executeProg, executeTests, gui, compiler, model);

        if (testLua && !withStdLib) {
            // test lua translation
            compiler.setRunArgs(new RunArgs("-lua"));
            translateAndTestLua(name, executeProg, gui, model);
        }
        return model;

    }

    private void testWithInliningAndOptimizations(String name, boolean executeProg, boolean executeTests, WurstGui gui,
                                                  WurstCompilerJassImpl compiler, WurstModel model) throws Error {
        // test with inlining and local optimization
        compiler.setRunArgs(new RunArgs("-inline", "-localOptimizations"));
        translateAndTest(name + "_inlopt", executeProg, executeTests, gui, compiler, model);
    }

    private void testWithInlining(String name, boolean executeProg, boolean executeTests, WurstGui gui,
                                  WurstCompilerJassImpl compiler, WurstModel model) throws Error {
        // test with inlining
        compiler.setRunArgs(new RunArgs("-inline"));
        translateAndTest(name + "_inl", executeProg, executeTests, gui, compiler, model);
    }

    private void testWithLocalOptimizations(String name, boolean executeProg, boolean executeTests, WurstGui gui,
                                            WurstCompilerJassImpl compiler, WurstModel model) throws Error {
        // test with local optimization
        compiler.setRunArgs(new RunArgs("-localOptimizations"));
        translateAndTest(name + "_opt", executeProg, executeTests, gui, compiler, model);
    }

    private void testWithoutInliningAndOptimization(String name, boolean executeProg, boolean executeTests,
                                                    WurstGui gui, WurstCompilerJassImpl compiler, WurstModel model)
            throws Error {
        // test without inlining and optimization
        translateAndTest(name, executeProg, executeTests, gui, compiler, model);
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

            Files.write(luaScript, luaFile, Charsets.UTF_8);

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
                                  WurstModel model) throws Error {
        ImProg imProg = compiler.translateProgToIm(model);


        if (gui.getErrorCount() > 0) {
            throw gui.getErrorList().get(0);
        }


        writeJassImProg(name, gui, imProg);
        // TODO enable tests below:
        // we want to test that the interpreter works correctly before transforming the program in the translation step
//        if (executeTests) {
//            executeTests(gui, imProg);
//        }
//        if (executeProg) {
//            executeImProg(gui, imProg);
//        }


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

    protected WurstModel parseFiles(Iterable<File> inputFiles,
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
//				interpreter.addNativeProvider(new CompiletimeNatives((ProgramStateIO) interpreter.getGlobalState()));
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
            interpreter.executeFunction("main");
        } catch (TestSuccessException e) {
            return;
        }
        throw new Error("Succeed function not called");
    }

    private void executeTests(WurstGui gui, ImProg imProg) {
        CompiletimeFunctionRunner cfr = new CompiletimeFunctionRunner(imProg, null, null, gui, Tests);
        cfr.run();
        WLogger.info("Successfull tests: " + cfr.getSuccessTests().size());
        int failedTestCount = cfr.getFailTests().size();
        WLogger.info("Failed tests: " + failedTestCount);
        if (failedTestCount == 0) {
            return;
        }
        for (Entry<ImFunction, Pair<ImStmt, String>> e : cfr.getFailTests().entrySet()) {
            Assert.assertFalse(true, Utils.printElementWithSource(e.getKey().attrTrace()) + " " + e.getValue().getB()
                    + "\n" + "at " + Utils.printElementWithSource(e.getValue().getA().attrTrace()));
        }
        throw new Error("tests failed");
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

            Files.write(sb.toString(), outputFile, Charsets.UTF_8);
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

            Files.write(sb.toString(), outputFile, Charsets.UTF_8);
        } catch (IOException e) {
            throw new Error("IOException, could not write jass file " + outputFile + "\n" + gui.getErrors());
        }
        return outputFile;
    }


}
