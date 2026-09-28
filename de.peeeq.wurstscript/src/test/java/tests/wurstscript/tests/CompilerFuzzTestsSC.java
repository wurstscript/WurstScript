package tests.wurstscript.tests;

import org.junit.runner.RunWith;
import org.testng.Assert;
import org.testng.annotations.Test;
import smallcheck.SmallCheckRunner;
import smallcheck.annotations.From;
import smallcheck.annotations.Property;
import smallcheck.generators.SeriesGen;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RunWith(SmallCheckRunner.class)
public class CompilerFuzzTestsSC extends WurstScriptTest {

    /**
     * How many structurally distinct programs each generator can actually produce.
     *
     * <p>buildRandomSingleProgram varies its shape with six bits of the seed - indent style,
     * tuple, interface, module, loop, callback - so there are 2^6 shapes; the rest of the seed
     * only renames packages and changes integer literals, which reaches no new compiler path.
     * Budgets above these counts recompile the same shapes under different names.
     */
    private static final int SINGLE_PROGRAM_SHAPES = 64;
    private static final int CROSS_PACKAGE_SHAPES = 24;

    @Property(maxInvocations = SINGLE_PROGRAM_SHAPES)
    public void generatedProgramsAreCrashFree(@From(RandomProgram.class) Program program) {
        CompilationResult result = runProgram(program);

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getGui());
    }

    @Property(maxInvocations = SINGLE_PROGRAM_SHAPES)
    public void generatedProgramsCompileForBothBackends(@From(RandomProgram.class) Program program) {
        assertCompilesForBothBackends(program, "generatedProgramsCompileForBothBackends");
    }

    @Test
    public void generatedCorpusCompilesForBothBackends() {
        new RandomProgram().generate(0)
            .forEach(p -> assertCompilesForBothBackends(p, "generatedCorpusCompilesForBothBackends"));
    }

    /**
     * Both callers used to let test() name the output after this helper, so they shared one file
     * under ./test-output/. They run in different Gradle forks - the @Test under TestNG, the
     * @Property under SmallCheckViaJUnitCoreTestNG - so the two JVMs raced on that file and pjass
     * intermittently parsed a spliced result, reporting word fragments as undefined types. Naming
     * the output after the calling test keeps them apart.
     */
    private void assertCompilesForBothBackends(Program program, String testName) {
        CompilationResult result = testNamed(testName)
            .setStopOnFirstError(false)
            .executeProg(false)
            .testLua(true)
            .luaOnly(false)
            .compilationUnits(asCompilationUnits(program));

        Assert.assertTrue(result.getGui().getErrorList().isEmpty(),
            "generated program produced compiler diagnostics: " + result.getGui().getErrorList()
                + "\nsource:\n" + String.join("\n---\n", program.sources));
    }

    /**
     * Line endings are a lexer detail: the same source written with LF and with CRLF has to emit
     * byte-identical Jass. This previously only asserted that compiling the CRLF variant returned
     * non-null, which no realistic bug would violate.
     */
    @Property(maxInvocations = SINGLE_PROGRAM_SHAPES)
    public void newlineStyleDoesNotAffectEmittedCode(@From(RandomProgram.class) Program program) {
        String alternateNewline = "\n".equals(program.newline) ? "\r\n" : "\n";

        String fromOriginal = compileAndReadJass(program, "newlineOriginal");
        String fromAlternate = compileAndReadJass(program.withNewline(alternateNewline), "newlineAlternate");

        Assert.assertEquals(fromAlternate, fromOriginal,
            "line ending style changed the emitted Jass\nsource:\n" + String.join("\n---\n", program.sources));
    }

    /**
     * The same source compiled twice in one process has to emit the same script. DeterministicChecks
     * pins this for a few hand-written programs; this runs it across every generated shape.
     */
    @Property(maxInvocations = SINGLE_PROGRAM_SHAPES)
    public void compilingTwiceEmitsIdenticalCode(@From(RandomProgram.class) Program program) {
        String first = compileAndReadJass(program, "determinismFirst");
        String second = compileAndReadJass(program, "determinismSecond");

        Assert.assertEquals(second, first,
            "recompiling the same source emitted different Jass\nsource:\n" + String.join("\n---\n", program.sources));
    }

    /**
     * Compiles {@code program} and returns the unoptimised Jass it emitted. The output name is
     * explicit so two compilations inside one property do not overwrite each other's file.
     */
    private String compileAndReadJass(Program program, String outputName) {
        CompilationResult result = testNamed(outputName)
            .setStopOnFirstError(false)
            .executeProg(false)
            .compilationUnits(asCompilationUnits(program));

        Assert.assertTrue(result.getGui().getErrorList().isEmpty(),
            "generated program produced compiler diagnostics: " + result.getGui().getErrorList()
                + "\nsource:\n" + String.join("\n---\n", program.sources));

        File emitted = new File(TEST_OUTPUT_PATH + getClass().getSimpleName() + "_" + outputName + "_no_opts.j");
        try {
            return Files.readString(emitted.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("could not read emitted script " + emitted, e);
        }
    }

    @Property(maxInvocations = CROSS_PACKAGE_SHAPES)
    public void crossPackageProgramsAreCrashFree(@From(CrossPackageProgram.class) Program program) {
        CompilationResult result = runProgram(program);

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getGui());
    }

    @Test
    public void deepNestedControlFlowIsCrashFree() {
        CompilationResult result = runProgram(new Program(
            new String[]{"deep_nested.wurst"},
            new String[]{buildDeepNestedControlFlow("\n")},
            "\n"
        ));

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getGui());
    }

    @Test
    public void crossPackageImportChainDoesNotCrash() {
        String[] names = {"chain_support.wurst", "chain_main.wurst"};
        String[] sources = {
            buildChainSupportPackage("\n"),
            buildChainMainPackage("\n")
        };
        CompilationResult result = runProgram(new Program(names, sources, "\n"));

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getGui());
    }

    private CompilationResult runProgram(Program program) {
        return test()
            .setStopOnFirstError(false)
            .executeProg(false)
            .compilationUnits(asCompilationUnits(program));
    }

    private CU[] asCompilationUnits(Program program) {
        CU[] units = new CU[program.sources.length];
        for (int i = 0; i < program.sources.length; i++) {
            units[i] = new CU(program.unitNames[i], program.sources[i]);
        }
        return units;
    }

    private static String replaceNewline(String input, String newline) {
        return input.replace("\r\n", "\n").replace("\r", "\n").replace("\n", newline);
    }

    private static String join(List<String> lines, String newline) {
        return String.join(newline, lines);
    }

    private static String indent(int count) {
        return " ".repeat(count);
    }

    private static String buildDeepNestedControlFlow(String newline) {
        List<String> lines = new ArrayList<>();
        lines.add("package DeepNested");
        lines.add("init");
        lines.add(indent(4) + "int marker = 0");
        for (int depth = 0; depth < 150; depth++) {
            String base = " ".repeat(4 + depth * 4);
            lines.add(base + "if marker == " + depth);
            lines.add(base + "    marker = marker + 1");
            if (depth % 4 == 0) {
                lines.add(base + "    int j = " + depth);
                lines.add(base + "    while j > 0");
                lines.add(base + "        j = j - 1");
                lines.add(base + "        marker = marker + j");
            }
        }
        lines.add(indent(4) + "if marker >= 0");
        lines.add(indent(8) + "marker = marker");
        return join(lines, newline);
    }

    private static String buildChainSupportPackage(String newline) {
        List<String> lines = Arrays.asList(
            "package ChainSupport",
            "public class Loader",
            "    int value",
            "    construct(int value)",
            "        this.value = value",
            "    public function withLoader(Loader loader) returns int",
            "        return loader.value + 1",
            "public interface ChainMarker",
            "    function mark(int value) returns int"
        );
        return String.join(newline, lines);
    }

    private static String buildChainMainPackage(String newline) {
        List<String> lines = Arrays.asList(
            "package ChainMain",
            "import ChainSupport",
            "class Adapter implements ChainMarker",
            "    function mark(int value) returns int",
            "        return value + 1",
            "function callMarker(ChainMarker marker, int value) returns int",
            "    return marker.mark(value)",
            "init",
            "    let loader = new Loader(3)",
            "    let marker = new Adapter()",
            "    int result = withLoader(loader)",
            "    if callMarker(marker, result) == 5",
            "        result = result + 1",
            "    else",
            "        result = result - 1"
        );
        return String.join(newline, lines);
    }

    private static String buildRandomSingleProgram(int seed, String newline) {
        String indent = ((seed & 1) == 0) ? "    " : "  ";
        String pkg = "FuzzPkg_" + Math.abs(seed % 997);
        List<String> lines = new ArrayList<>();
        lines.add("package " + pkg);
        lines.add("int base = " + (seed % 41 + 2));

        boolean includeTuple = (seed & 2) != 0;
        boolean includeInterface = (seed & 4) != 0;
        boolean includeModule = (seed & 8) != 0;
        boolean includeLoop = (seed & 16) != 0;
        boolean includeCallback = (seed & 32) != 0;

        if (includeTuple) {
            lines.add("tuple Pair(int left, int right)");
        }

        lines.add("function plus(int a, int b) returns int");
        lines.add(indent + "return a + b");

        if (includeInterface) {
            lines.add("interface IHandler");
            lines.add(indent + "function process(int value) returns int");
        }

        lines.add("class Counter");
        lines.add(indent + "int value");
        lines.add(indent + "construct(int start)");
        lines.add(indent + indent + "value = start");
        lines.add(indent + "function inc() returns int");
        lines.add(indent + indent + "value += 1");
        lines.add(indent + indent + "return value");

        if (includeModule) {
            lines.add("module Shared");
            lines.add(indent + "int sharedValue = base");
            lines.add(indent + "function bump(int value) returns int");
            lines.add(indent + indent + "return value + sharedValue");
        }

        if (includeInterface) {
            lines.add("class Sink implements IHandler");
            lines.add(indent + "function process(int value) returns int");
            lines.add(indent + indent + "return value + base");
        }

        if (includeCallback) {
            lines.add("interface IntCallback");
            lines.add(indent + "function apply(int value) returns int");
            lines.add("function invoke(IntCallback callback, int value) returns int");
            lines.add(indent + "return callback.apply(value)");
        }

        if (includeModule) {
            lines.add("class SharedUser");
            lines.add(indent + "use Shared");
            lines.add(indent + "function shifted(int value) returns int");
            lines.add(indent + indent + "return bump(value)");
        }

        lines.add("init");
        lines.add(indent + "let counter = new Counter(base)");
        lines.add(indent + "int total = plus(base, counter.inc())");

        if (includeCallback) {
            lines.add(indent + "IntCallback cb = value -> value + 1");
            lines.add(indent + "total = invoke(cb, total)");
        }

        if (includeInterface) {
            lines.add(indent + "IHandler handler = new Sink()");
            lines.add(indent + "total = handler.process(total)");
        }

        if (includeTuple) {
            lines.add(indent + "let pair = Pair(base, total)");
            lines.add(indent + "total = plus(pair.left, pair.right)");
        }

        if (includeModule) {
            lines.add(indent + "let user = new SharedUser()");
            lines.add(indent + "total = user.shifted(total)");
        }

        if (includeLoop) {
            lines.add(indent + "int sum = 0");
            lines.add(indent + "for int i = 0 to 4");
            lines.add(indent + indent + "sum = sum + i");
            lines.add(indent + "while sum < 4");
            lines.add(indent + indent + "sum = sum + 1");
            lines.add(indent + "total = total + sum");
        }

        lines.add(indent + "if total > 0");
        lines.add(indent + indent + "total = total");
        lines.add(indent + "else");
        lines.add(indent + indent + "total = 0");
        return join(lines, newline);
    }

    private static String buildRandomSupportPackage(int seed, String newline, String pkg) {
        String indent = "    ";
        List<String> lines = new ArrayList<>();
        lines.add("package " + pkg);
        lines.add("public class Value");
        lines.add(indent + "int data");
        lines.add(indent + "construct(int data)");
        lines.add(indent + indent + "this.data = data");
        lines.add(indent + "public function valuePlus(int amount) returns int");
        lines.add(indent + indent + "return data + amount");
        lines.add(indent + "public function data() returns int");
        lines.add(indent + indent + "return data");
        return join(lines, newline);
    }

    private static String buildRandomMainPackage(int seed, String newline, String supportPackage) {
        String pkg = "FuzzMain_" + Math.abs(seed % 997);
        String indent = "    ";
        List<String> lines = new ArrayList<>();
        lines.add("package " + pkg);
        lines.add("import " + supportPackage);
        lines.add("interface Visitor");
        lines.add(indent + "function visit(int value) returns int");
        lines.add("function run(Visitor visitor, int value) returns int");
        lines.add(indent + "return visitor.visit(value)");
        lines.add("class Delegate implements Visitor");
        lines.add(indent + "public function visit(int value) returns int");
        lines.add(indent + indent + "return value + 1");
        lines.add("init");
        lines.add(indent + "let value = new Value(" + (seed % 17 + 1) + ")");
        lines.add(indent + "let delegate = new Delegate()");
        lines.add(indent + "int result = run(delegate, value.value())");
        lines.add(indent + "let pair = value.valuePlus(result)");
        lines.add(indent + "if pair > 0");
        lines.add(indent + indent + "result = result + pair");
        return join(lines, newline);
    }

    public static class RandomProgram extends SeriesGen<Program> {
        @Override
        public Stream<Program> generate(int depth) {
            int firstSeed = Math.max(0, depth) * 64 + 17_213;
            return IntStream.range(0, 64).mapToObj(offset -> {
                int seed = firstSeed + offset;
                String newline = ((seed & 4) == 0) ? "\n" : "\r\n";
                String source = buildRandomSingleProgram(seed, newline);
                return new Program(new String[]{"random_fuzz.wurst"}, new String[]{source}, newline);
            });
        }
    }

    public static class CrossPackageProgram extends SeriesGen<Program> {
        @Override
        public Stream<Program> generate(int depth) {
            int firstSeed = Math.max(0, depth) * 24 + 17_213;
            return IntStream.range(0, 24).mapToObj(offset -> {
                int seed = firstSeed + offset;
                String newline = ((seed & 16) == 0) ? "\r\n" : "\n";
                String supportPkg = "ChainLib" + Math.abs(seed % 999);
                String mainPkg = "ChainUse" + Math.abs(seed % 999);
                String supportSource = buildRandomSupportPackage(seed, newline, supportPkg);
                String mainSource = buildRandomMainPackage(seed, newline, supportPkg);
                return new Program(
                    new String[]{"support_" + supportPkg + ".wurst", "main_" + mainPkg + ".wurst"},
                    new String[]{supportSource, mainSource},
                    newline
                );
            });
        }
    }

    private static class Program {
        final String[] unitNames;
        final String[] sources;
        final String newline;

        private Program(String[] unitNames, String[] sources, String newline) {
            this.unitNames = unitNames;
            this.sources = sources;
            this.newline = newline;
        }

        private Program withNewline(String newline) {
            String[] replaced = Arrays.stream(sources)
                .map(source -> replaceNewline(source, newline))
                .toArray(String[]::new);
            return new Program(unitNames, replaced, newline);
        }
    }
}
