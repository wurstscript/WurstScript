package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
    }

    @Test
    public void closureTypeIdOrderIsDeterministic() {
        CompilationResult res = test()
            .setStopOnFirstError(false)
            .executeProg(false)
            .lines(
                "package test",
                "interface Callback",
                "    function call()",
                "function run(Callback cb)",
                "    cb.call()",
                "init",
                "    run(() -> begin",
                "        int x = 1",
                "    end)",
                "    run(() -> begin",
                "        int y = 2",
                "    end)"
            );

        de.peeeq.wurstscript.translation.imtranslation.ImTranslator tr =
            new de.peeeq.wurstscript.translation.imtranslation.ImTranslator(res.getModel(), false, new de.peeeq.wurstscript.RunArgs());
        de.peeeq.wurstscript.jassIm.ImProg prog = tr.translateProg();
        java.util.Map<de.peeeq.wurstscript.jassIm.ImClass, Integer> typeIds = de.peeeq.wurstscript.translation.imtranslation.TypeId.calculate(prog);
        java.util.List<Integer> ids = new java.util.ArrayList<>(typeIds.values());
        for (int i = 0; i < ids.size(); i++) {
            assertEquals((int) ids.get(i), i + 1);
        }
    }

    @Test
    public void packageFunctionAndClosureNamingIsDeterministic() throws IOException {
        test().testLua(true).compilationUnits(
            compilationUnit("PkgA.wurst",
                "package PkgA",
                "interface Callback",
                "    function run()",
                "public function update()",
                "    Callback cb = () -> begin",
                "        int x = 1",
                "    end",
                "    cb.run()",
                "init",
                "    update()"
            ),
            compilationUnit("PkgB.wurst",
                "package PkgB",
                "public function update()",
                "init",
                "    update()"
            )
        );

        String output = Files.toString(new File("test-output/lua/DeterministicChecks_packageFunctionAndClosureNamingIsDeterministic.lua"), Charsets.UTF_8);
        AssertJUnit.assertTrue(output.contains("PkgA__update"));
        AssertJUnit.assertTrue(output.contains("PkgB__update"));
        AssertJUnit.assertTrue(output.contains("Callback_L"));
    }

}
