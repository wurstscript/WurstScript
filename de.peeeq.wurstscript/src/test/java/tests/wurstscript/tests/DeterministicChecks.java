package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;


/**
 * These tests are supposed to check, whether the compiler is determinisitic
 */
public class DeterministicChecks extends WurstScriptTest {


    @Test
    public void simple() throws IOException {
        ErrorHandler.outputTestSource = true;
        run(this::exampleCode, "exampleCode_no_opts");
        ErrorHandler.outputTestSource = false;
    }

    private void run(Runnable example, String name) throws IOException {
        example.run();
        File exampleFile1 = new File("test-output/DeterministicChecks_"+ name + ".j");
        String script1 = Files.toString(exampleFile1, Charsets.UTF_8);
        Files.move(exampleFile1, new File("test-output/det1.j"));
        Files.move(new File("test-output/im 1.im"), new File("test-output/im1.j"));
        example.run();
        String script2 = Files.toString(exampleFile1, Charsets.UTF_8);
        Files.move(exampleFile1, new File("test-output/det2.j"));
        Files.move(new File("test-output/im 1.im"), new File("test-output/im2.j"));
        assertEquals(script1, script2);
    }

    /**
     * The same source has to emit the same script whatever was compiled before it. Names of
     * generated temporaries used to be counted per thread and never reset, so a program compiled
     * alone got {@code temp0} and the same program compiled after other work got {@code temp70} —
     * which is what made generated Jass impossible to compare across runs. Compiling something
     * else in between is the part that matters here; two runs on their own would agree either way.
     * The inlining configuration is the one that emits a temporary for this source.
     */
    @Test
    public void temporaryNamesDoNotDependOnEarlierCompilations() throws IOException {
        ErrorHandler.outputTestSource = true;
        try {
            usesTemporaries();
            String first = Files.toString(
                new File("test-output/DeterministicChecks_usesTemporaries_inl.j"), Charsets.UTF_8);

            exampleCode();
            cycleExample();

            usesTemporaries();
            String afterOtherWork = Files.toString(
                new File("test-output/DeterministicChecks_usesTemporaries_inl.j"), Charsets.UTF_8);

            assertEquals(first, afterOtherWork);
        } finally {
            ErrorHandler.outputTestSource = false;
        }
    }

    /** Nested calls in one expression are what makes the flattener allocate temporaries. */
    private void usesTemporaries() {
        testAssertOkLines(false,
            "package test",
            "native testSuccess()",
            "function f(int x) returns int",
            "    return x + 1",
            "init",
            "    if f(f(1)) + f(f(2)) == 8",
            "        testSuccess()"
        );
    }

    private void exampleCode() {
        testAssertOkLines(false,
            "package test",
            "native testSuccess()",
            "interface I",
            "    function foo() returns int",
            "class B implements I",
            "    function foo() returns int",
            "        return 2",
            "class C implements I",
            "    function foo() returns int",
            "        return 3",
            "init",
            "    I i1 = new B()",
            "    I i2 = new C()",
            "    if i1.foo() == 2 and i2.foo() == 3",
            "        testSuccess()"
        );
    }

    @Test
    public void cyclicFunctionCall() throws IOException {
        ErrorHandler.outputTestSource = true;
        run(this::cycleExample, "cycleExample_no_opts");
        ErrorHandler.outputTestSource = false;
    }

    private void cycleExample() {
        testAssertOkLines(false,
            "package test",
            "native testSuccess()",
            "function a(int i) returns int",
            "    if i == 0",
            "       return 0",
            "    return b(i div 2)",
            "function b(int i) returns int",
            "    if i == 0",
            "       return 0",
            "    return c(i div 2)",
            "function c(int i) returns int",
            "    if i == 0",
            "       return 0",
            "    return a(i div 2)",
            "init",
            "    if a(42) == 0",
            "        testSuccess()"
        );
    }

    @Test
    public void test_var_merge() throws IOException {
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            test().executeProg(true).lines(
                "package test",
                "native testSuccess()",
                "native println(string s)",
                "function foo(string p_msg, string p_pos) returns string",
                "    var msg = p_msg",
                "    var pos = p_pos",
                "    pos = msg",
                "    msg = \"\"",
                "    for i = 1 to 3",
                "        msg += \"x\"",
                "    return pos + msg",
                "init",
                "    let s = foo(\"a\", \"b\")",
                "    if s == \"axxx\"",
                "        testSuccess()",
                "");

            String output = Files.toString(new File("./test-output/DeterministicChecks_test_var_merge_opt.j"), Charsets.UTF_8);
            counts.put(output, counts.getOrDefault(output, 0) + 1);
        }
        //System.out.println("counts = " + counts.values());
        AssertJUnit.assertEquals(1, counts.size());
        //System.out.println(counts.keySet());
        // Interesting note: LocalMerger seems to switch the order in the return line and sometimes rewrites the return to
        // return p_msg + p_pos
    }

    @Test
    public void functionAndGlobalSortOrderIsDeterministic() {
        CompilationResult res1 = test()
            .setStopOnFirstError(false)
            .executeProg(false)
            .testLua(true)
            .lines(
                "package test",
                "int zVar = 1",
                "int aVar = 2",
                "function zFunc() returns int",
                "    return zVar",
                "function aFunc() returns int",
                "    return aVar",
                "init",
                "    aFunc()",
                "    zFunc()"
            );

        CompilationResult res2 = test()
            .setStopOnFirstError(false)
            .executeProg(false)
            .testLua(true)
            .lines(
                "package test",
                "function aFunc() returns int",
                "    return aVar",
                "int aVar = 2",
                "function zFunc() returns int",
                "    return zVar",
                "int zVar = 1",
                "init",
                "    aFunc()",
                "    zFunc()"
            );

        de.peeeq.wurstscript.translation.imtranslation.ImTranslator tr1 =
            new de.peeeq.wurstscript.translation.imtranslation.ImTranslator(res1.getModel(), false, new de.peeeq.wurstscript.RunArgs());
        de.peeeq.wurstscript.jassIm.ImProg prog1 = tr1.translateProg();

        de.peeeq.wurstscript.translation.imtranslation.ImTranslator tr2 =
            new de.peeeq.wurstscript.translation.imtranslation.ImTranslator(res2.getModel(), false, new de.peeeq.wurstscript.RunArgs());
        de.peeeq.wurstscript.jassIm.ImProg prog2 = tr2.translateProg();

        java.util.List<String> funcs1 = prog1.getFunctions().stream()
            .map(de.peeeq.wurstscript.jassIm.ImFunction::getName)
            .filter(name -> name.contains("Func"))
            .collect(java.util.stream.Collectors.toList());
        java.util.List<String> funcs2 = prog2.getFunctions().stream()
            .map(de.peeeq.wurstscript.jassIm.ImFunction::getName)
            .filter(name -> name.contains("Func"))
            .collect(java.util.stream.Collectors.toList());
        assertEquals(funcs1, funcs2);

        java.util.List<String> globals1 = prog1.getGlobals().stream()
            .map(de.peeeq.wurstscript.jassIm.ImVar::getName)
            .filter(name -> name.endsWith("Var"))
            .collect(java.util.stream.Collectors.toList());
        java.util.List<String> globals2 = prog2.getGlobals().stream()
            .map(de.peeeq.wurstscript.jassIm.ImVar::getName)
            .filter(name -> name.endsWith("Var"))
            .collect(java.util.stream.Collectors.toList());
        assertEquals(2, globals1.size());
        assertEquals(globals1, globals2);
    }

    @Test
    public void multiPackageShuffledCompilationUnitOrderIsBitExact() throws IOException {
        CU cuA = compilationUnit("PkgA.wurst",
            "package PkgA",
            "public interface Formatter",
            "    function format(string s) returns string",
            "public class UpperFormatter implements Formatter",
            "    override function format(string s) returns string",
            "        return s",
            "public int counter = 0",
            "public function inc()",
            "    counter++"
        );
        CU cuB = compilationUnit("PkgB.wurst",
            "package PkgB",
            "import PkgA",
            "public class FancyFormatter extends UpperFormatter",
            "    override function format(string s) returns string",
            "        return super.format(s) + \"!\"",
            "public function runAction(Formatter f, string text) returns string",
            "    inc()",
            "    return f.format(text)"
        );
        CU cuC = compilationUnit("PkgC.wurst",
            "package PkgC",
            "public interface Transformer",
            "    function transform(int x) returns int",
            "public function applyTransformer(int val, Transformer t) returns int",
            "    return t.transform(val)"
        );
        CU cuD = compilationUnit("PkgD.wurst",
            "package PkgD",
            "import PkgB",
            "import PkgC",
            "public function compute(int val) returns int",
            "    Transformer t = (int x) -> begin",
            "        return x * 2 + 1",
            "    end",
            "    return applyTransformer(val, t)"
        );
        CU cuE = compilationUnit("PkgE.wurst",
            "package PkgE",
            "import PkgA",
            "import PkgB",
            "import PkgD",
            "native testSuccess()",
            "init",
            "    FancyFormatter ff = new FancyFormatter()",
            "    let resStr = runAction(ff, \"test\")",
            "    let resNum = compute(10)",
            "    if resStr == \"test!\" and resNum == 21 and counter == 1",
            "        testSuccess()",
            "    destroy ff"
        );

        // Pass 1 in order [A, B, C, D, E]
        test().testLua(true).executeProg().compilationUnits(cuA, cuB, cuC, cuD, cuE);
        File outFile = new File("test-output/lua/DeterministicChecks_multiPackageShuffledCompilationUnitOrderIsBitExact.lua");
        String outputStd1 = Files.toString(outFile, Charsets.UTF_8);
        String hashStd1 = Hashing.sha256().hashString(outputStd1, Charsets.UTF_8).toString();

        // Pass 2 in shuffled order [D, A, E, C, B]
        test().testLua(true).executeProg().compilationUnits(cuD, cuA, cuE, cuC, cuB);
        String outputStd2 = Files.toString(outFile, Charsets.UTF_8);
        String hashStd2 = Hashing.sha256().hashString(outputStd2, Charsets.UTF_8).toString();

        assertEquals(hashStd1, hashStd2, "SHA-256 hash must be identical across shuffled compilation unit order");
        assertEquals(outputStd1, outputStd2, "Output must be bit-for-bit identical across shuffled compilation unit order");
    }

    /**
     * Every closure of one interface method written in one function becomes a class with the same IM
     * name, so no name-based sort key tells those classes apart. The Lua backend binds one dispatch
     * slot on each of them; the order of those assignments used to be the iteration order of a
     * {@code HashSet} of identity-hashed classes, which differs between compilations (and JVMs), while
     * every name and number in the script stayed the same. Compiling the same program repeatedly, with
     * the compilation units in a different order each time, must give one script.
     */
    @Test
    public void classesSharingAnImNameBindTheirDispatchSlotInProgramOrder() throws IOException {
        String[] types = {"int", "real", "string", "boolean"};
        String[] literals = {"1", "1.5", "\"s\"", "true"};
        List<String> library = new ArrayList<>(List.of(
            "package Shapes",
            "public interface Predicate<T:>",
            "    function test(T t) returns boolean",
            "public class Box<T:>",
            "    T value",
            "    construct(T value)",
            "        this.value = value",
            "    function matches(Predicate<T> p) returns boolean",
            "        let result = p.test(value)",
            "        destroy p",
            "        return result",
            "public class Foo",
            "public class Impl implements Predicate<Foo>",
            "    function test(Foo value) returns boolean",
            "        return value != null"));
        // A class beside the closures makes the family heterogeneous, which is what gives the
        // closures a canonical slot of their own.
        List<String> main = new ArrayList<>(List.of(
            "package Main",
            "import Shapes",
            "native testSuccess()",
            "init",
            "    int successes = 0",
            "    let ordinary = new Box<Foo>(new Foo())",
            "    if ordinary.matches(x -> x != null)",
            "        successes++",
            "    let ordinaryImpl = new Box<Foo>(new Foo())",
            "    if ordinaryImpl.matches(new Impl())",
            "        successes++"));
        int count = 0;
        for (int a = 0; a < types.length; a++) {
            for (int b = 0; b < 2; b++) {
                String tuple = "shape" + count;
                library.add("public tuple " + tuple + "(" + types[a] + " a, " + types[b] + " b)");
                main.add("    let box" + count + " = new Box<" + tuple + ">(" + tuple + "("
                    + literals[a] + ", " + literals[b] + "))");
                main.add("    if box" + count + ".matches(x -> x.a == " + literals[a] + ")");
                main.add("        successes++");
                count++;
            }
        }
        main.add("    if successes == " + (count + 2));
        main.add("        testSuccess()");
        CU shapesUnit = new CU("Shapes.wurst", String.join("\n", library));
        CU mainUnit = new CU("Main.wurst", String.join("\n", main));

        compileClosureReceivers(shapesUnit, mainUnit);
        String first = readClosureReceiversLua();
        // the premise: the closures (one per tuple, one over Foo) and Impl are bound to one slot
        assertEquals(countOccurrences(first, ".__wurst_dispatch_test = "), count + 2, first);

        for (int pass = 0; pass < 5; pass++) {
            if (pass % 2 == 0) {
                compileClosureReceivers(mainUnit, shapesUnit);
            } else {
                compileClosureReceivers(shapesUnit, mainUnit);
            }
            assertEquals(readClosureReceiversLua(), first,
                "Lua output must not depend on the identity hashes of the receiver classes (pass " + pass + ")");
        }
    }

    private void compileClosureReceivers(CU... units) {
        test().testLua(true).executeProg().compilationUnits(units);
    }

    private String readClosureReceiversLua() throws IOException {
        return Files.toString(new File("test-output/lua/DeterministicChecks_compileClosureReceivers.lua"),
            Charsets.UTF_8);
    }

    private static int countOccurrences(String text, String part) {
        int n = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + part.length())) {
            n++;
        }
        return n;
    }

}
