package tests.wurstscript.tests;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.ast.WurstModel;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class BenchmarkAnnotationTests extends WurstScriptTest {

    private static final String BENCHMARK_ERROR =
        "@benchmark functions must be package-level, parameterless functions returning int.";
    private static final String COMBINATION_ERROR =
        "@benchmark cannot be combined with @compiletime.";
    private static final String EXECUTION_ERROR =
        "@benchmark functions must be executable by the compiletime IL interpreter; native, extern, and compiletimenative declarations are not supported.";

    private static final String VALID = """
        package Bench
        @benchmark function classify() returns int
            return 42
        """;

    @Test
    public void validBenchmarkGetsDedicatedImFlag() {
        Compilation compilation = compile(VALID);
        assertTrue(compilation.gui.getErrorList().isEmpty(), compilation.gui.getErrors());

        ImFunction function = compilation.imProg.getFunctions().stream()
            .filter(f -> f.getName().contains("classify"))
            .findFirst()
            .orElseThrow();
        assertTrue(function.hasFlag(FunctionFlagEnum.IS_BENCHMARK));
        assertFalse(function.hasFlag(FunctionFlagEnum.IS_TEST));
    }

    @Test
    public void benchmarkRejectsParameters() {
        assertBenchmarkError("""
            package Bench
            @benchmark function withParameter(int value) returns int
                return value
            """, BENCHMARK_ERROR);
    }

    @Test
    public void benchmarkRejectsNothingReturn() {
        assertBenchmarkError("""
            package Bench
            @benchmark function noReturn()
            """, BENCHMARK_ERROR);
    }

    @Test
    public void benchmarkRejectsNonIntReturn() {
        assertBenchmarkError("""
            package Bench
            @benchmark function returnsString() returns string
                return "wrong"
            """, BENCHMARK_ERROR);
    }

    @Test
    public void benchmarkRejectsClassMethods() {
        assertBenchmarkError("""
            package Bench
            class C
                @benchmark
                function classFunction() returns int
                    return 42
            """, BENCHMARK_ERROR);
    }

    @Test
    public void benchmarkCanCombineWithTest() {
        Compilation compilation = compile("""
            package Bench
            @benchmark @test function testBenchmark() returns int
                return 42
            """);
        assertTrue(compilation.gui.getErrorList().isEmpty(), compilation.gui.getErrors());

        ImFunction function = compilation.imProg.getFunctions().stream()
            .filter(f -> f.getName().contains("testBenchmark"))
            .findFirst()
            .orElseThrow();
        assertTrue(function.hasFlag(FunctionFlagEnum.IS_TEST));
        assertTrue(function.hasFlag(FunctionFlagEnum.IS_BENCHMARK));
    }

    @Test
    public void benchmarkCannotCombineWithCompiletime() {
        assertBenchmarkError("""
            package Bench
            @benchmark @compiletime function compiletimeBenchmark() returns int
                return 42
            """, COMBINATION_ERROR);
    }

    @Test
    public void benchmarkRejectsNativeDeclaration() {
        assertBenchmarkError("""
            package Bench
            @benchmark native nativeBenchmark() returns int
            """, EXECUTION_ERROR);
    }

    @Test
    public void benchmarkRejectsExternDeclaration() {
        assertBenchmarkError("""
            package Bench
            @benchmark @extern function externBenchmark() returns int
                return 42
            """, EXECUTION_ERROR);
    }

    @Test
    public void benchmarkRejectsCompiletimeNativeDeclaration() {
        assertBenchmarkError("""
            package Bench
            @benchmark @compiletimenative function nativeBenchmark() returns int
                return 42
            """, EXECUTION_ERROR);
    }

    private void assertBenchmarkError(String source, String expectedMessage) {
        Compilation compilation = compile(source);
        assertTrue(compilation.gui.getErrors().contains(expectedMessage), compilation.gui.getErrors());
    }

    private Compilation compile(String source) {
        WurstGuiCliImpl gui = new WurstGuiCliImpl();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, new RunArgs());
        WurstModel model = parseFiles(null,
            Collections.singletonList(new CU("benchmark", Utils.join(source.lines().toList(), "\n") + "\n")),
            false,
            compiler);
        compiler.checkProg(model);
        ImProg imProg = null;
        if (gui.getErrorList().isEmpty()) {
            imProg = compiler.translateProgToIm(model);
            assertNotNull(imProg);
        }
        return new Compilation(gui, imProg);
    }

    private record Compilation(WurstGuiCliImpl gui, ImProg imProg) {
    }
}
