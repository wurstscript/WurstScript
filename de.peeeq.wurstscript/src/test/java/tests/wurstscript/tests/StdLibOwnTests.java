package tests.wurstscript.tests;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.testng.Assert.assertTrue;

/**
 * Runs the standard library's own {@code @Test} functions.
 * <p>
 * A bump of the pinned version was otherwise only checked for still compiling, which is a weak
 * signal for a change whose point is behaviour, and a bad one for the parts of the library which
 * degrade quietly rather than failing — the multibyte detection in {@code String.wurst} concludes
 * the engine has no multibyte characters when it cannot find what it probes for, so a version where
 * it silently gave up would look exactly like one where it worked.
 * <p>
 * The library is a library: only imported packages are compiled in, so a program importing nothing
 * runs none of its tests and passes. The imports are therefore collected from the checkout rather
 * than written down, which also means a test file added by a later bump is picked up by being there
 * rather than by someone remembering.
 */
public class StdLibOwnTests extends WurstScriptTest {

    /**
     * Every package in the library whose name ends in {@code Tests}. Read from the file rather than
     * assumed from the path, because a package need not be named after the file holding it.
     */
    private static List<String> testPackages() throws IOException {
        Path root = new File(StdLib.getLib()).toPath();
        List<String> packages = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith("Tests.wurst"))::iterator) {
                for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("package ")) {
                        packages.add(trimmed.substring("package ".length()).trim());
                        break;
                    }
                }
            }
        }
        packages.sort(String::compareTo);
        return packages;
    }

    @Test
    public void standardLibraryTestsPass() throws IOException {
        List<String> packages = testPackages();
        assertTrue(packages.size() > 20,
            "expected the library to carry its test packages, found " + packages.size());

        List<String> program = new ArrayList<>();
        program.add("package test");
        for (String p : packages) {
            program.add("import " + p);
        }
        program.add("init");
        program.add("    skip");

        // One per test function in those packages. The floor is deliberately far below the real
        // count: it is there to catch the program holding none, not to be updated on every bump.
        test().withStdLib().expectAtLeastTests(100).lines(program.toArray(new String[0]));
    }

    /**
     * A failing library test says which one it was.
     * <p>
     * The name is printed to stdout, which a CI run does not keep, so the thrown message was all that
     * was left of it - and it carried the counts followed by every warning the library compiles with.
     * A failure on a runner one does not have therefore said that one test of four hundred and sixty
     * failed and nothing more, which is how a slow-runner timeout cost an afternoon to place.
     * <p>
     * The name has to arrive before the warnings, because a report which truncates a long message
     * keeps the front of it.
     */
    @Test
    public void aFailingLibraryTestIsNamed() {
        try {
            test().withStdLib().executeTests().lines(
                "package test",
                "import Wurstunit",
                "@Test function deliberatelyFails()",
                "    let one = 1",
                "    one.assertEquals(2)",
                "init",
                "    skip"
            );
        } catch (Error e) {
            String message = e.getMessage();
            assertTrue(message.contains("deliberatelyFails"),
                "the failure should name the test, but said:\n" + message);
            return;
        }
        throw new AssertionError("a failing library test should have failed the suite");
    }
}
