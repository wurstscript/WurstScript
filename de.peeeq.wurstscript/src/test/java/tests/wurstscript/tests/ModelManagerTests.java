package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import de.peeeq.wurstio.languageserver.CompilationResult;
import de.peeeq.wurstio.languageserver.ExternCompileError;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstscript.utils.Utils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.core.IsNot;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder);

        // keep error messages in a map:
        Map<String, String> results = new HashMap<>();
        manager.onCompilationResult((CompilationResult res) -> {

            String errors = res.getErrors().stream()
                    .map(e -> e.toString())
                    .collect(Collectors.joining("\n"));

            results.put(res.getFilename(), errors);

            for (ExternCompileError err : res.getErrors()) {
                System.out.println("   err: " + err);
            }
        });

        // first build the project
        manager.buildProject();


        String fileA = new File(wurstFolder, "A.wurst").getCanonicalPath();
        String fileB = new File(wurstFolder, "B.wurst").getCanonicalPath();
        String fileC = new File(wurstFolder, "C.wurst").getCanonicalPath();

        assertThat(results.get(fileA), CoreMatchers.containsString("Reference to function b could not be resolved"));
        assertThat(results.get(fileA), CoreMatchers.containsString("Reference to function c could not be resolved"));
        assertThat(results.get(fileB), CoreMatchers.containsString("Reference to function c could not be resolved"));
        assertEquals("", results.get(fileC));

        // no assume we fix package B
        String packageB_v2 = packageB_v1.replace("b_old", "b");
        results.clear();
        manager.syncCompilationUnitContent("wurst/B.wurst", packageB_v2);
        // the change of B should trigger rechecks of A and B, but not of C
        assertEquals(ImmutableSet.of(fileA, fileB), results.keySet());

        // reference to function b should be found now, other errors still the same
        assertThat(results.get(fileA), new IsNot<>(CoreMatchers.containsString("Reference to function b could not be resolved")));
        assertThat(results.get(fileA), CoreMatchers.containsString("Reference to function c could not be resolved"));
        assertThat(results.get(fileB), CoreMatchers.containsString("Reference to function c could not be resolved"));

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
        Files.write(content, new File(wurstFolder, filename), Charsets.UTF_8);

    }

    private String string(String... lines) {
        return Utils.join(lines, "\n");
    }

}
