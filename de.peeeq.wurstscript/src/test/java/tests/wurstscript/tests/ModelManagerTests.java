package tests.wurstscript.tests;

import com.google.common.collect.ImmutableSet;
import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.utils.FileUtils;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.FunctionCall;
import de.peeeq.wurstscript.ast.FunctionDefinition;
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
    public void test() throws IOException {
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
        manager.syncCompilationUnitContent(fileB, packageB_v2);
        // the change of B should trigger rechecks of A and B, but not of C
        assertEquals(ImmutableSet.of(fileA, fileB), results.keySet());

        // reference to function b should be found now, other errors still the same
        assertThat(results.get(fileA), new IsNot<>(containsString("Reference to function b could not be resolved")));
        assertThat(results.get(fileA), containsString("Reference to function c could not be resolved"));
        assertThat(results.get(fileB), containsString("Reference to function c could not be resolved"));

        // no we fix package C:
        String packageC_v2 = packageC_v1.replace("c_old", "c");
        results.clear();
        manager.syncCompilationUnitContent(fileC, packageC_v2);

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
                System.out.println("   err: " + err);
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

}
