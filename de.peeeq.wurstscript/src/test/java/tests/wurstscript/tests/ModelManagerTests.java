package tests.wurstscript.tests;

import com.google.common.collect.ImmutableSet;
import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.utils.FileUtils;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.hamcrest.CoreMatchers;
import org.hamcrest.core.IsNot;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ModelManagerTests {

    @Test
    public void test() throws IOException, InterruptedException {
        File projectFolder = new File("./temp/testProject/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);

        String packageA_v1 = string(
            "package A",
            "import B",
            "import C",
            "public function a()",
            "	b()",
            "	c()"
        );


        String packageB_v1 = string(
            "package B",
            "import C",
            "public function b_old()",
            "	c()"
        );


        String packageC_v1 = string(
            "package C",
            "public function c_old()"
        );


        WFile fileA = WFile.create(new File(wurstFolder, "A.wurst"));
        WFile fileB = WFile.create(new File(wurstFolder, "B.wurst"));
        WFile fileC = WFile.create(new File(wurstFolder, "C.wurst"));
        WFile fileWurst = WFile.create(new File(wurstFolder, "Wurst.wurst"));

        writeFile(fileA, packageA_v1);
        writeFile(fileB, packageB_v1);
        writeFile(fileC, packageC_v1);
        writeFile(fileWurst, "package Wurst\n");


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());

        // keep error messages in a map:
        Map<WFile, String> results = keepErrorsInMap(manager);

        // first build the project
        manager.buildProject();


        assertThat(results.get(fileA), containsString("Reference to function b could not be resolved"));
        assertThat(results.get(fileA), containsString("Reference to function c could not be resolved"));
        assertThat(results.get(fileB), containsString("Reference to function c could not be resolved"));
        assertEquals("", results.get(fileC));

        // no assume we fix package B
        String packageB_v2 = packageB_v1.replace("b_old", "b");
        results.clear();
        ModelManager.Changes changes = manager.syncCompilationUnitContent(fileB, packageB_v2);
        manager.reconcile(changes);
        // the change of B should trigger rechecks of A and B, but not of C
        assertEquals(ImmutableSet.of(fileA, fileB), results.keySet());

        // reference to function b should be found now, other errors still the same
        assertThat(results.get(fileA), new IsNot<>(containsString("Reference to function b could not be resolved")));
        assertThat(results.get(fileA), containsString("Reference to function c could not be resolved"));
        assertThat(results.get(fileB), containsString("Reference to function c could not be resolved"));

        // no we fix package C:
        String packageC_v2 = packageC_v1.replace("c_old", "c");
        results.clear();
        changes = manager.syncCompilationUnitContent(fileC, packageC_v2);
        manager.reconcile(changes);

        // the change of B should trigger rechecks of A, B, and C
        assertEquals(ImmutableSet.of(fileA, fileB, fileC), results.keySet());
        // there should be no more errors
        assertEquals("", results.get(fileA));
        assertEquals("", results.get(fileB));
        assertEquals("", results.get(fileC));


    }

    @Test
    public void movingFiles() throws IOException { // #712
        File projectFolder = new File("./temp/testProject2/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);
        File subFolderX = new File(wurstFolder, "x");
        newCleanFolder(subFolderX);
        File subFolderY = new File(wurstFolder, "y");
        newCleanFolder(subFolderY);


        String packageA = string(
            "package A",
            "import Test",
            "init",
            "    foo()"
        );


        String packageTest = string(
            "package Test",
            "public function foo()"
        );

        WFile fileA = WFile.create(new File(wurstFolder, "A.wurst"));
        WFile fileTest1 = WFile.create(new File(subFolderX, "Test.wurst"));
        WFile fileTest2 = WFile.create(new File(subFolderY, "Test.wurst"));
        WFile fileWurst = WFile.create(new File(wurstFolder, "Wurst.wurst"));


        writeFile(fileA, packageA);
        writeFile(fileTest1, packageTest);
        writeFile(fileWurst, "package Wurst\n");


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());
        Map<WFile, String> results = keepErrorsInMap(manager);


        // first build the project
        manager.buildProject();


        {
            // check that jump to decl in package A works correctly
            CompilationUnit cu = manager.getCompilationUnit(fileA);
            FunctionCall functionCallFoo = (FunctionCall) Utils.getAstElementAtPos(cu, 4, 5, false).get();
            FunctionDefinition def = functionCallFoo.attrFuncDef();
            assertEquals(WFile.create(def.attrSource().getFile()), fileTest1);
        }

        // no move the file to folder y
        fileTest1.getFile().delete();
        writeFile(fileTest2, packageTest);
        manager.removeCompilationUnit(fileTest1);
        manager.syncCompilationUnit(fileTest2);


        {
            // check that jump to decl in package A works correctly after the change
            CompilationUnit cu = manager.getCompilationUnit(fileA);
            FunctionCall functionCallFoo = (FunctionCall) Utils.getAstElementAtPos(cu, 4, 5, false).get();
            FunctionDefinition def = functionCallFoo.attrFuncDef();
            assertNotNull(def);
            assertEquals(WFile.create(def.attrSource().getFile()), fileTest2);
        }

    }


    @Test
    public void renamePackage() throws IOException {
        File projectFolder = new File("./temp/testProject2/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);


        String packageA = string(
            "package A",
            "import Test",
            "init",
            "    foo()"
        );


        String packageTest = string(
            "package Test",
            "public function foo()"
        );

        String packageTest2 = string(
            "package Testttt",
            "public function foo()"
        );

        WFile fileA = WFile.create(new File(wurstFolder, "A.wurst"));
        WFile fileTest = WFile.create(new File(wurstFolder, "Test.wurst"));
        WFile fileWurst = WFile.create(new File(wurstFolder, "Wurst.wurst"));


        writeFile(fileA, packageA);
        writeFile(fileTest, packageTest);
        writeFile(fileWurst, "package Wurst\n");


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());
        Map<WFile, String> errors = keepErrorsInMap(manager);


        // first build the project
        manager.buildProject();
        assertEquals(errors.get(fileA), "");


        // no move the file to folder y
        writeFile(fileTest, packageTest2);
        manager.syncCompilationUnit(fileTest);

        assertThat(errors.get(fileA), CoreMatchers.containsString("The import 'Test' could not be resolved."));


    }

    @NotNull
    private Map<WFile, String> keepErrorsInMap(ModelManagerImpl manager) {
        // keep error messages in a map:
        Map<WFile, String> results = new HashMap<>();
        manager.onCompilationResult((PublishDiagnosticsParams res) -> {

            String errors = res.getDiagnostics().stream()
                .map(e -> e.toString())
                .collect(Collectors.joining("\n"));

            results.put(WFile.create(res.getUri()), errors);

            for (Diagnostic err : res.getDiagnostics()) {
                System.out.println("   " + err.getSeverity() + " in " + res.getUri() + ", line " + err.getRange().getStart().getLine()  + "\n     " + err.getMessage());
            }
        });
        return results;
    }

    private void newCleanFolder(File f) throws IOException {
        FileUtils.deleteRecursively(f);
        Files.createDirectories(f.toPath());
    }

    private void writeFile(WFile f, String content) throws IOException {
        FileUtils.write(content, f.getFile());
    }


    private String string(String... lines) {
        return Utils.join(lines, "\n");
    }


    @Test
    public void changeModule() throws IOException {
        File projectFolder = new File("./temp/testProject2/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);


        String packageT1 = string(
            "package T1",
            "import T2",
            "init",
            "    new B()",
            "class B",
            "    use A"
        );


        String packageT2 = string(
            "package T2",
            "int x = 5",
            "public module A",
            "    construct()",
            "        x = 6"
        );

        String packageT2updated = string(
            "package T2",
            "int x = 5",
            "public module A",
            "    construct()",
            "        x = 7"
        );

        WFile fileA = WFile.create(new File(wurstFolder, "T1.wurst"));
        WFile fileTest = WFile.create(new File(wurstFolder, "T2.wurst"));
        WFile fileWurst = WFile.create(new File(wurstFolder, "Wurst.wurst"));


        writeFile(fileA, packageT1);
        writeFile(fileTest, packageT2);
        writeFile(fileWurst, "package Wurst\n");


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());
        Map<WFile, String> errors = keepErrorsInMap(manager);


        // first build the project
        manager.buildProject();
        assertEquals(errors.get(fileA), "");


        // now, update the module in package T2
        writeFile(fileTest, packageT2updated);
        manager.syncCompilationUnit(fileTest);

        assertEquals(errors.get(fileA), "");
        assertEquals(errors.get(fileTest), "");

        WurstModel model = manager.getModel();
        assertNotNull(model);

        // check that module instantiation contains updated constructor with "x = 7"
        model.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ClassDef c) {
                for (ModuleInstanciation mi : c.getModuleInstanciations()) {
                    String s = Utils.prettyPrint(mi.getConstructors());
                    assertThat(s, CoreMatchers.containsString("x = 7"));
                }
            }
        });

    }

    @Test
    public void changeModuleAbstractMethod() throws IOException {
        File projectFolder = new File("./temp/testProject2/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);


        String packageT1 = string(
            "package T1",
            "import T2",
            "class CustomHero",
            "    use CustomHeroXpModule",
            "    int xp",
            "    override function getXp() returns int",
            "        return this.xp"
        );


        String packageT2 = string(
            "package T2",
            "public module CustomHeroXpModule",
            "    abstract function getXp() returns int"
        );

        String packageT2updated = string(
            "package T2",
            "public module CustomHeroXpModule",
            "    abstract function getXpOld() returns int"
        );

        WFile fileT1 = WFile.create(new File(wurstFolder, "T1.wurst"));
        WFile fileT2 = WFile.create(new File(wurstFolder, "T2.wurst"));
        WFile fileWurst = WFile.create(new File(wurstFolder, "Wurst.wurst"));


        writeFile(fileT1, packageT1);
        writeFile(fileT2, packageT2);
        writeFile(fileWurst, "package Wurst\n");


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());
        Map<WFile, String> errors = keepErrorsInMap(manager);


        // first build the project
        manager.buildProject();
        assertEquals(errors.get(fileT1), "");


        // now, update the module in package T2
        writeFile(fileT2, packageT2updated);
        manager.syncCompilationUnit(fileT2);

        // now, there should be errors in class CustomHero in fileA
        assertThat(errors.get(fileT1), CoreMatchers.containsString("Non-abstract class CustomHero must implement the following functions:\\n    function getXpOld() returns int"));
        assertEquals(errors.get(fileT2), "");
    }


    @Test
    public void moduleErrorAtInit() throws IOException {
        File projectFolder = new File("./temp/testProject2/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);


        String packageT1 = string(
            "package T1",
            "import T2",
            "class C",
            "    use M",
            "init",
            "    new C.foo()"
        );


        String packageT2 = string(
            "package T2",
            "public module Moo",
            "    function foo() returns int",
            "        return 1"
        );

        String packageT2updated = string(
            "package T2",
            "public module M",
            "    function foo() returns int",
            "        return 1"
        );

        WFile fileT1 = WFile.create(new File(wurstFolder, "T1.wurst"));
        WFile fileT2 = WFile.create(new File(wurstFolder, "T2.wurst"));
        WFile fileWurst = WFile.create(new File(wurstFolder, "Wurst.wurst"));


        writeFile(fileT1, packageT1);
        writeFile(fileT2, packageT2);
        writeFile(fileWurst, "package Wurst\n");


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());
        Map<WFile, String> errors = keepErrorsInMap(manager);


        // first build the project
        manager.buildProject();
        assertThat(errors.get(fileT1), CoreMatchers.containsString("Could not find type M."));
        assertEquals(errors.get(fileT2), "");


        // now, update the module in package T2
        writeFile(fileT2, packageT2updated);
        manager.syncCompilationUnit(fileT2);

        // now, the errors should be resolved
        assertEquals(errors.get(fileT1), "");
        assertEquals(errors.get(fileT2), "");
    }

    @Test
    public void moduleTransitive() throws IOException {
        File projectFolder = new File("./temp/testProject2/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);


        String packageT1 = string(
            "package T1",
            "import T2",
            "init",
            "    new C.foo()"
        );

        String packageT2 = string(
            "package T2",
            "import T3",
            "public class C extends D"
        );

        String packageT3 = string(
            "package T3",
            "import T4",
            "public class D",
            "    use M"
        );

        String packageT4 = string(
            "package T4",
            "public module M",
            "    function foo() returns int",
            "        return 1"
        );

        String packageT4updated = string(
            "package T4",
            "public module M",
            "    function bar() returns int",
            "        return 1"
        );

        WFile fileT1 = WFile.create(new File(wurstFolder, "T1.wurst"));
        WFile fileT2 = WFile.create(new File(wurstFolder, "T2.wurst"));
        WFile fileT3 = WFile.create(new File(wurstFolder, "T3.wurst"));
        WFile fileT4 = WFile.create(new File(wurstFolder, "T4.wurst"));
        WFile fileWurst = WFile.create(new File(wurstFolder, "Wurst.wurst"));


        writeFile(fileT1, packageT1);
        writeFile(fileT2, packageT2);
        writeFile(fileT3, packageT3);
        writeFile(fileT4, packageT4);
        writeFile(fileWurst, "package Wurst\n");


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());
        Map<WFile, String> errors = keepErrorsInMap(manager);


        // first build the project
        manager.buildProject();
        // no errors expected
        assertEquals(errors.get(fileT1), "");
        assertEquals(errors.get(fileT2), "");
        assertEquals(errors.get(fileT3), "");
        assertEquals(errors.get(fileT4), "");


        // now, update the module in package T4
        writeFile(fileT4, packageT4updated);
        manager.syncCompilationUnit(fileT4);

        // now, the function foo in package T1 can no longer be called
        assertThat(errors.get(fileT1), CoreMatchers.containsString("Could not find function foo"));
    }

    @Test
    public void keepTypeErrorsWhileEditing() throws IOException {
        File projectFolder = new File("./temp/testProject2/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);


        String packageT1 = string(
            "package T1",
            "init",
            "    foo()"
        );

        String packageT1updated = string(
            "package T1",
            "init",
            "    foo()",
            "    if ("
        );

        WFile fileT1 = WFile.create(new File(wurstFolder, "T1.wurst"));
        WFile fileWurst = WFile.create(new File(wurstFolder, "Wurst.wurst"));


        writeFile(fileT1, packageT1);
        writeFile(fileWurst, "package Wurst\n");


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());
        Map<WFile, String> errors = keepErrorsInMap(manager);


        // first build the project
        manager.buildProject();
        // should show the type error
        assertThat(errors.get(fileT1), CoreMatchers.containsString("Reference to function foo could not be resolved."));


        // now, update package T1 and introduce a syntax error
        ModelManager.Changes changes = manager.syncCompilationUnitContent(fileT1, packageT1updated);
        manager.reconcile(changes);

        // now the errors should contain the syntax error and the type error
        assertThat(errors.get(fileT1), CoreMatchers.containsString("Reference to function foo could not be resolved."));
        assertThat(errors.get(fileT1), CoreMatchers.containsString("extraneous input '(' expecting NL"));
    }


}
