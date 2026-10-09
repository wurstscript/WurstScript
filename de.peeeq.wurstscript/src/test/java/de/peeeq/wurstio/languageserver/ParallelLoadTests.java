package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WurstModel;
import org.eclipse.lsp4j.Diagnostic;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * A load parses the files of the project and of its libraries ahead, by several threads, and takes the parse of
 * each file when it comes to it. Parsing ahead must change how long a load takes and nothing else: the same compilation
 * units in the same order, and the same diagnostics.
 */
public class ParallelLoadTests {

    private static final int PROJECT_FILES = 30;
    private static final int LIBRARY_FILES = 12;

    private static String two(int i) {
        return i < 10 ? "0" + i : "" + i;
    }

    private static void write(Path file, String contents) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, contents);
    }

    /**
     * Thirty packages (six of them in a subfolder) which import a library and the next package, twelve library
     * files which import two later ones, so that the libraries are reached over several levels, and a library file
     * which provides two packages, the second of which a project file imports without a file of its name.
     */
    static Path project(boolean withSyntaxError) throws IOException {
        Path root = Files.createTempDirectory("wurst-parallel-load");
        for (int i = 0; i < LIBRARY_FILES; i++) {
            StringBuilder source = new StringBuilder("package Dep" + two(i) + "\nimport NoWurst\n");
            StringBuilder result = new StringBuilder("1");
            for (int next : new int[]{i + 1, i + 3}) {
                if (next < LIBRARY_FILES) {
                    source.append("import Dep").append(two(next)).append("\n");
                    result.append(" + d").append(two(next)).append("()");
                }
            }
            source.append("public function d").append(two(i)).append("() returns int\n    return ").append(result).append("\n");
            write(root.resolve("_build/dependencies/Lib" + (i % 3) + "/wurst/Dep" + two(i) + ".wurst"), source.toString());
        }
        write(root.resolve("_build/dependencies/Lib0/wurst/Multi.wurst"),
            "package Multi\nimport NoWurst\npublic function multi() returns int\n    return 1\nendpackage\n\n"
                + "package MultiExtra\nimport NoWurst\npublic function multiExtra() returns int\n    return 2\n");
        for (int i = 0; i < PROJECT_FILES; i++) {
            StringBuilder source = new StringBuilder("package P" + two(i) + "\nimport NoWurst\n");
            // only the first library is imported by the project, the others are reached through it over several levels
            StringBuilder result = new StringBuilder("d00()");
            if (i == 0) {
                source.append("import Multi\n");
                result.append(" + multi()");
            }
            if (i == 5) {
                source.append("import MultiExtra\n");
                result.append(" + multiExtra()");
            }
            source.append("import Dep00\n");
            if (i + 1 < PROJECT_FILES) {
                source.append("import P").append(two(i + 1)).append("\n");
                result.append(" + p").append(two(i + 1)).append("()");
            }
            source.append("public function p").append(two(i)).append("() returns int\n    return ").append(result).append("\n");
            write(root.resolve("wurst/" + (i % 6 == 5 ? "sub/" : "") + "P" + two(i) + ".wurst"), source.toString());
        }
        if (withSyntaxError) {
            write(root.resolve("wurst/Broken.wurst"), "package Broken\nfunction ( 1 +\n");
        }
        return root;
    }

    private static final class Loaded {
        final ModelManagerImpl manager;
        final List<String> files = new ArrayList<>();
        final List<CompilationUnit> units = new ArrayList<>();
        /** file name -> the latest diagnostics published for it, files without any left out */
        final Map<String, List<String>> diagnostics = new TreeMap<>();

        Loaded(Path root, int threads) {
            manager = new ModelManagerImpl(root.toFile(), new BufferManager());
            manager.setParseThreads(threads);
            Map<String, List<String>> latest = new TreeMap<>();
            manager.onCompilationResult(params -> {
                List<String> lines = new ArrayList<>();
                for (Diagnostic d : params.getDiagnostics()) {
                    lines.add(d.toString());
                }
                Collections.sort(lines);
                latest.put(params.getUri(), lines);
            });
            manager.buildProject();
            for (Map.Entry<String, List<String>> e : latest.entrySet()) {
                if (!e.getValue().isEmpty()) {
                    diagnostics.put(e.getKey(), e.getValue());
                }
            }
            WurstModel model = manager.getModel();
            assertNotNull(model);
            for (CompilationUnit cu : model) {
                files.add(cu.getCuInfo().getFile());
                units.add(cu);
            }
        }
    }

    private static void assertSameLoad(Loaded sequential, Loaded parallel) {
        assertEquals(parallel.files, sequential.files, "the compilation units, in the order of the model");
        assertEquals(parallel.units.size(), sequential.units.size());
        for (int i = 0; i < sequential.units.size(); i++) {
            String file = sequential.files.get(i);
            if (file.endsWith("common.j") || file.endsWith("blizzard.j")) {
                continue; // from the jar, not parsed ahead
            }
            // the rendering of what is in the unit: structuralEquals does not hold between two parses, a unit's
            // CompilationUnitInfo is compared by identity
            CompilationUnit p = parallel.units.get(i);
            CompilationUnit s = sequential.units.get(i);
            assertEquals(p.getJassDecls().toString(), s.getJassDecls().toString(), "the Jass declarations of " + file);
            assertEquals(p.getPackages().toString(), s.getPackages().toString(), "the packages of " + file);
            assertTrue(s.getPackages().size() > 0 || s.getJassDecls().size() > 0 || file.endsWith("Broken.wurst"),
                "a unit with nothing in it makes the comparison empty: " + file);
        }
        assertEquals(parallel.diagnostics, sequential.diagnostics, "the diagnostics");
        assertEquals(parallel.manager.pendingAheadParses(), 0, "parses made ahead which were left over");
    }

    @Test
    public void aParallelLoadBuildsTheModelOfTheSequentialLoad() throws IOException {
        Path root = project(false);
        Loaded sequential = new Loaded(root, 1);
        Loaded parallel = new Loaded(root, 4);

        assertSameLoad(sequential, parallel);
        assertTrue(sequential.diagnostics.isEmpty(), "the project has no errors: " + sequential.diagnostics);
        // common.j and blizzard.j come from the jar; every other file was parsed ahead and taken from there
        int sources = parallel.units.size() - 2;
        assertEquals(sources, PROJECT_FILES + LIBRARY_FILES + 1, "the files of the project and the libraries it needs");
        assertEquals(sequential.manager.parsesTakenAhead(), 0, "one thread parses nothing ahead");
        assertEquals(parallel.manager.parsesTakenAhead(), sources, "every file was parsed ahead");
    }

    @Test
    public void aSyntaxErrorIsReportedTheSameWay() throws IOException {
        Path root = project(true);
        Loaded sequential = new Loaded(root, 1);
        Loaded parallel = new Loaded(root, 4);

        assertSameLoad(sequential, parallel);
        assertFalse(sequential.diagnostics.isEmpty(), "the broken file reports its errors");
        assertTrue(sequential.diagnostics.keySet().stream().anyMatch(f -> f.endsWith("Broken.wurst")),
            sequential.diagnostics.keySet().toString());
    }

    @Test
    public void aParseOfOtherTextIsNotTaken() throws IOException {
        Path root = project(false);
        ModelManagerImpl manager = new ModelManagerImpl(root.toFile(), new BufferManager());
        manager.setParseThreads(4);
        manager.buildProject();
        WFile file = WFile.create(root.resolve("wurst/P01.wurst").toFile());
        String changed = "package P01\npublic function p01() returns int\n    return 7\n\n"
            + "public function other() returns int\n    return 8\n";

        // a parse of the text before the change is lying around, and so is one of text which is not the file's
        Map<WFile, String> sources = new java.util.LinkedHashMap<>();
        sources.put(file, "package P01\npublic function stale() returns int\n    return 0\n");
        sources.put(WFile.create(root.resolve("wurst/P02.wurst").toFile()), "package P02\n");
        manager.parseAhead(sources);
        assertEquals(manager.pendingAheadParses(), 2);
        int before = manager.parsesTakenAhead();
        manager.syncCompilationUnitContent(file, changed);

        CompilationUnit cu = manager.getCompilationUnit(file);
        assertNotNull(cu);
        assertTrue(cu.toString().contains("other"), "the unit has the text which was synced: " + cu);
        assertFalse(cu.toString().contains("stale"), "the unit is not the parse made ahead: " + cu);
        assertEquals(manager.parsesTakenAhead(), before, "a parse of other text is not taken");
        assertEquals(manager.pendingAheadParses(), 1, "the parse of the file is dropped, the other stays for its load");
    }

    @Test
    public void aParseOfOtherTextWithTheSameHashIsNotTaken() throws IOException {
        // "Aa" and "BB" have the same hashCode, and so have two texts which differ only by them: a parse is taken for
        // the text it was made from, not for a text with the hash of it
        assertEquals("Aa".hashCode(), "BB".hashCode());
        Path root = project(false);
        ModelManagerImpl manager = new ModelManagerImpl(root.toFile(), new BufferManager());
        manager.setParseThreads(4);
        manager.buildProject();
        WFile file = WFile.create(root.resolve("wurst/P01.wurst").toFile());
        String parsedAhead = "package P01\nimport NoWurst\npublic function Aa() returns int\n    return 1\n";
        String synced = "package P01\nimport NoWurst\npublic function BB() returns int\n    return 1\n";
        assertEquals(parsedAhead.replace("Aa", "BB"), synced);

        Map<WFile, String> sources = new java.util.LinkedHashMap<>();
        sources.put(file, parsedAhead);
        manager.parseAhead(sources);
        assertEquals(manager.pendingAheadParses(), 1);
        int before = manager.parsesTakenAhead();
        manager.syncCompilationUnitContent(file, synced);

        CompilationUnit cu = manager.getCompilationUnit(file);
        assertNotNull(cu);
        assertTrue(cu.toString().contains("BB"), "the unit has the text which was synced: " + cu);
        assertFalse(cu.toString().contains("Aa"), "the unit is the parse of another text: " + cu);
        assertEquals(manager.parsesTakenAhead(), before, "a parse of other text is not taken");
        assertEquals(manager.pendingAheadParses(), 0);
    }

    @Test
    public void aFileWhichDidNotChangeIsNotParsedAheadAgain() throws IOException {
        Path root = project(false);
        ModelManagerImpl manager = new ModelManagerImpl(root.toFile(), new BufferManager());
        manager.setParseThreads(4);
        manager.buildProject();
        Map<WFile, String> sources = new java.util.LinkedHashMap<>();
        for (int i = 0; i < 4; i++) {
            File f = root.resolve("wurst/P" + two(i) + ".wurst").toFile();
            sources.put(WFile.create(f), Files.readString(f.toPath()));
        }

        manager.parseAhead(sources);

        assertEquals(manager.pendingAheadParses(), 0, "the model has these texts");
    }
}
