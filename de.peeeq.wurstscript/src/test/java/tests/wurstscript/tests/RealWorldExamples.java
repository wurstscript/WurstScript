package tests.wurstscript.tests;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RealWorldExamples extends WurstScriptTest {

    private static final String TEST_DIR = "./testscripts/concept/";
    private static final String BUG_DIR = "./testscripts/realbugs/";

    @Override
    protected boolean testOptimizer() {
        return true;
    }


    @Test
    public void arrayindex() throws IOException {
        // see bug #96
        super.testAssertOkFileWithStdLib(new File(BUG_DIR + "arrayindex.wurst"), false);
    }

    @Test
    public void linkedHashMap() throws IOException { // see bug #478
        Iterable<File> inputFiles = Arrays.asList(new File(BUG_DIR + "LinkedHashMap.wurst"));

        Map<String, String> inputs = Collections.emptyMap();
        String name = "linkedHashMap";
        boolean executeProg = false;
        boolean withStdLib = true;
        boolean executeTests = true;

        new TestConfig(name)
                .withStdLib(withStdLib)
                .executeTests(executeTests)
                .executeProgOnlyAfterTransforms(true)
                .executeProg(executeProg)
                .withInputFiles(inputFiles)
                .withInputs(inputs)
                .run()
                .getModel();
    }

    @Test
    public void module() throws IOException {
        super.testAssertOkFileWithStdLib(new File(BUG_DIR + "module.wurst"), false);
    }

    @Test
    public void testLists() throws IOException {
        super.testAssertOkFileWithStdLib(new File(TEST_DIR + "ListsTest.wurst"), false);
    }


    @Test
    public void testEditorVars() throws IOException {
        // we expect an error here, but only in the translation phase
        testAssertErrorFileWithStdLib(new File(TEST_DIR + "EditorVariables.wurst"), "Translation Error: Could not find definition of gg_", false);
    }

    @Test
    public void setNullTests() throws IOException {
        super.testAssertOkFileWithStdLib(new File(TEST_DIR + "SetNullTests.wurst"), false);
    }

    @Test
    public void setFrottyBugKnockbackNull() throws IOException {
        super.testAssertOkFileWithStdLib(new File(TEST_DIR + "knockback.wurst"), false);
    }

    @Test
    public void setFrottyBugEscaperData() throws IOException {
        super.testAssertOkFileWithStdLib(new File(TEST_DIR + "escaperdata.wurst"), false);
    }


    @Test
    public void setFrottyBugVector() throws IOException {
        super.testAssertOkFileWithStdLib(new File(TEST_DIR + "vector.wurst"), false);
    }

    @Test
    public void test_war3map() throws IOException {
        super.testAssertOkFileWithStdLib(new File(TEST_DIR + "test_war3map.wurst"), false);
    }

    @Test
    public void frottyTupleBug() throws IOException {
        super.testAssertOkFileWithStdLib(new File(TEST_DIR + "tupleBug.wurst"), false);
    }

    @Test
    public void optimizerNew() throws IOException {
        super.testAssertOkFileWithStdLib(new File(TEST_DIR + "optimizerNewTests.wurst"), false);
    }

    @Test
    public void staticCallback() throws IOException {
        super.testAssertOkFileWithStdLib(new File(TEST_DIR + "staticCallback.wurst"), false);
    }

    @Test
    public void nonStaticCallback() throws IOException {
        super.testAssertErrorFileWithStdLib(new File(TEST_DIR + "nonStaticCallback.wurst"), "without parameters", false);
    }

    @Test
    @Ignore // TODO fix test
    public void criggesInitOrder1() throws IOException {
        super.testAssertErrorFileWithStdLib(new File(TEST_DIR + "CriggesInitOrder1.wurst"), "not yet initialized", false);
    }

    @Test
    @Ignore // TODO fix test
    public void criggesInitOrder2() throws IOException {
        super.testAssertErrorFileWithStdLib(new File(TEST_DIR + "CriggesInitOrder2.wurst"), "used before it is initialized", false);
    }

    @Test
    public void blubber() throws IOException {
        super.testAssertOkFileWithStdLib(new File(TEST_DIR + "HashListSetBug.wurst"), false);
    }


    @Test
    public void test_stdlib() {
        List<File> inputs = Lists.newLinkedList();
        // TODO set config
        RunArgs runArgs = RunArgs.defaults();
        runArgs.addLibs(Sets.newHashSet(StdLib.getLib()));
        WurstCompilerJassImpl comp = new WurstCompilerJassImpl(null, new WurstGuiCliImpl(), null, runArgs);
        for (File f : comp.getLibs().values()) {
            WLogger.info("Adding file: " + f);
            inputs.add(f);
        }

        new TestConfig("stdlib")
                .withStdLib(true)
                .executeTests(true)
                .executeProgOnlyAfterTransforms(false)
                .executeProg(false)
                .withInputFiles(inputs)
                .run()
                .getModel();

    }

    @Test
    public void nullClosureBug() throws IOException { // See 852
        test().withStdLib()
            .executeProg(false)
            .runCompiletimeFunctions(true)
            .file(new File(BUG_DIR + "nullclosurebug.wurst"));
    }

}
