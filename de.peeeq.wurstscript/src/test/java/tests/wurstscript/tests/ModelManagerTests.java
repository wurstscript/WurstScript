package tests.wurstscript.tests;

import com.google.common.collect.ImmutableSet;
import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.utils.FileUtils;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.hamcrest.core.IsNot;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;

public class ModelManagerTests {

    @Test
    public void test() throws IOException {
        File projectFolder = new File("./temp/testProject/");
        File wurstFolder = new File(projectFolder, "wurst");
        wurstFolder.mkdirs();

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


        writeFile(wurstFolder, "A.wurst", packageA_v1);
        writeFile(wurstFolder, "B.wurst", packageB_v1);
        writeFile(wurstFolder, "C.wurst", packageC_v1);
        writeFile(wurstFolder, "Wurst.wurst", "package Wurst\n");


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());

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

        // first build the project
        manager.buildProject();


        WFile fileA = WFile.create(new File(wurstFolder, "A.wurst"));
        WFile fileB = WFile.create(new File(wurstFolder, "B.wurst"));
        WFile fileC = WFile.create(new File(wurstFolder, "C.wurst"));

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

    private void writeFile(File wurstFolder, String filename, String content) throws IOException {
        FileUtils.write(content, new File(wurstFolder, filename));
    }

    private String string(String... lines) {
        return Utils.join(lines, "\n");
    }

}
