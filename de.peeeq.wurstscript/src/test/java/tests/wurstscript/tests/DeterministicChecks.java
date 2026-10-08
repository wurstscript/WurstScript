package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


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

    /**
     * A class which implements an interface through a module resolves {@code v.write(x)} to the same
     * function whatever order the compiler happens to see its candidates in. The candidates are the
     * module's implementation and the interface's declaration, and they live in hash sets keyed by
     * identity, so the order differs between compilations of the same source.
     */
    @Test
    public void interfaceImplementedByModuleResolvesTheSameCallEveryCompilation() throws IOException {
        File outFile = new File("test-output/lua/DeterministicChecks_moduleImplementedInterface.lua");
        String first = null;
        for (int run = 0; run < 3; run++) {
            moduleImplementedInterface();
            String output = Files.toString(outFile, Charsets.UTF_8);
            if (first == null) {
                first = output;
            } else {
                assertEquals(output, first, "compilation " + run + " of the same source must emit the same Lua");
            }
        }
    }

    private void moduleImplementedInterface() {
        int classes = 14;
        List<String> lines = new ArrayList<>(List.of(
            "package test",
            "native testSuccess()",
            "interface Codec",
            "    function write(int x) returns int",
            "    function read(int x) returns int",
            "module Lifecycle",
            "    abstract function write(int x) returns int",
            "    abstract function read(int x) returns int",
            "    function roundTrip(int x) returns int",
            "        return read(write(x))",
            "module Fields",
            "    use Lifecycle",
            "    int stored = 0",
            "    override function write(int x) returns int",
            "        stored = x",
            "        return x + 1",
            "    override function read(int x) returns int",
            "        return x - 1 + stored"));
        for (int i = 0; i < classes; i++) {
            lines.add("class C" + i + " implements Codec");
            lines.add("    use Fields");
            lines.add("function call" + i + "(C" + i + " v) returns int");
            lines.add("    return v.write(" + i + ") + v.read(" + i + ")");
        }
        lines.add("init");
        lines.add("    int sum = 0");
        for (int i = 0; i < classes; i++) {
            lines.add("    sum += call" + i + "(new C" + i + "())");
        }
        lines.add("    if sum != 0");
        lines.add("        testSuccess()");
        test().testLua(true).executeProg().lines(lines.toArray(new String[0]));
    }

    /**
     * A call on a class type binds to the module's implementation rather than to the interface
     * function, which is what the type checker chooses. The call must still reach an override in a
     * subclass, through the class type as well as through the interface, on both targets.
     */
    @Test
    public void callBoundToModuleImplementationStillReachesSubclassOverride() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Codec",
            "    function write(int x) returns int",
            "module Lifecycle",
            "    abstract function write(int x) returns int",
            "module Fields",
            "    use Lifecycle",
            "    override function write(int x) returns int",
            "        return x + 1",
            "class Base implements Codec",
            "    use Fields",
            "class Derived extends Base",
            "    override function write(int x) returns int",
            "        return x + 100",
            "function viaBase(Base b) returns int",
            "    return b.write(1)",
            "function viaInterface(Codec c) returns int",
            "    return c.write(1)",
            "init",
            "    if viaBase(new Base()) == 2 and viaBase(new Derived()) == 101",
            "        if viaInterface(new Base()) == 2 and viaInterface(new Derived()) == 101",
            "            testSuccess()"
        );
    }

    /**
     * The sub-methods of a method are the overrides in the classes which extend or implement its
     * owner, and the backends bind dispatch slots in their order. The classes of one owner were kept
     * in hash sets keyed by identity, so the same source listed them in a different order every time
     * it was compiled.
     */
    @Test
    public void subMethodOrderIsTheSameEveryCompilation() {
        List<String> first = subMethodOrder();
        for (int run = 1; run < 3; run++) {
            assertEquals(subMethodOrder(), first, "compilation " + run + " of the same source must order sub-methods alike");
        }
    }

    private List<String> subMethodOrder() {
        int classes = 14;
        List<String> lines = new ArrayList<>(List.of(
            "package test",
            "interface Shape",
            "    function area() returns int",
            "class Base",
            "    function size() returns int",
            "        return 0"));
        for (int i = 0; i < classes; i++) {
            lines.add("class Shape" + i + " implements Shape");
            lines.add("    override function area() returns int");
            lines.add("        return " + i);
            lines.add("class Derived" + i + " extends Base");
            lines.add("    override function size() returns int");
            lines.add("        return " + i);
        }
        CompilationResult res = test().setStopOnFirstError(false).executeProg(false).lines(lines.toArray(new String[0]));
        return subMethodOrder(res);
    }

    private List<String> subMethodOrder(CompilationResult res) {
        ImProg prog = new ImTranslator(res.getModel(), false, new RunArgs()).translateProg();
        List<String> order = new ArrayList<>();
        for (ImClass c : prog.getClasses()) {
            for (ImMethod m : c.getMethods()) {
                if (!m.getSubMethods().isEmpty()) {
                    order.add(c.getName() + "." + m.getName() + " -> "
                        + m.getSubMethods().stream().map(ImMethod::getName).collect(Collectors.joining(", ")));
                }
            }
        }
        assertTrue(order.size() >= 2, "expected sub-methods for Shape.area and Base.size: " + order);
        return order;
    }

    /**
     * Sibling implementors and subclasses which live in different compilation units are ordered by
     * their package, name and position, not by the order the units were handed to the compiler: the
     * sub-methods of a method, which the backends bind dispatch slots in, come out the same.
     */
    @Test
    public void subMethodOrderDoesNotDependOnCompilationUnitOrder() {
        CU shapes = compilationUnit("PkgShape.wurst",
            "package PkgShape",
            "public interface Shape",
            "    function area() returns int",
            "public class Base",
            "    function size() returns int",
            "        return 0");
        List<CU> siblings = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            siblings.add(compilationUnit("PkgS" + i + ".wurst",
                "package PkgS" + i,
                "import PkgShape",
                "public class Shape" + i + " implements Shape",
                "    override function area() returns int",
                "        return " + i,
                "public class Derived" + i + " extends Base",
                "    override function size() returns int",
                "        return " + i));
        }
        List<CU> forward = new ArrayList<>();
        forward.add(shapes);
        forward.addAll(siblings);
        List<CU> shuffled = new ArrayList<>(List.of(siblings.get(3), siblings.get(1), shapes, siblings.get(2), siblings.get(0)));

        List<String> inOrder = subMethodOrder(test().setStopOnFirstError(false).executeProg(false)
            .compilationUnits(forward.toArray(new CU[0])));
        List<String> reordered = subMethodOrder(test().setStopOnFirstError(false).executeProg(false)
            .compilationUnits(shuffled.toArray(new CU[0])));
        assertEquals(reordered, inOrder, "sub-method order must not depend on the order of the compilation units");
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

}
