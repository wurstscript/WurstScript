package tests.wurstscript.tests;

import de.peeeq.wurstscript.WurstParser;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * A file is parsed with the SLL prediction first, which is much faster than the full LL prediction and either gives the
 * tree the full prediction gives or reports a syntax error; only a file which was not accepted is parsed again with the
 * full prediction, as every file was before. What a parse says must not depend on that: the same tree for a valid file,
 * and for a broken file the same diagnostics in the same order, each reported once.
 */
public class SllParsingTests {

    private enum Language {WURST, JASS, JURST}

    private record Outcome(String tree, List<String> diagnostics, int sllParses, int fallbacks) {
    }

    private static Outcome parse(Language language, String name, String source, boolean sllFirst) {
        WurstGuiLogger gui = new WurstGuiLogger();
        ErrorHandler errors = new ErrorHandler(gui);
        WurstParser parser = new WurstParser(errors, gui);
        parser.setSllFirst(sllFirst);
        StringReader reader = new StringReader(source);
        CompilationUnit cu = switch (language) {
            case WURST -> parser.parse(reader, name, false);
            case JASS -> parser.parseJass(reader, name, false);
            case JURST -> parser.parseJurst(reader, name, false);
        };
        List<String> diagnostics = new ArrayList<>();
        for (CompileError e : gui.getErrorsAndWarnings()) {
            diagnostics.add("gui " + e);
        }
        for (CompileError e : errors.getErrors()) {
            diagnostics.add("error " + e);
        }
        for (CompileError e : errors.getWarnings()) {
            diagnostics.add("warning " + e);
        }
        String tree = cu.getJassDecls() + "\n" + cu.getPackages();
        return new Outcome(tree, diagnostics, parser.getSllParses(), parser.getFallbacks());
    }

    private static List<Path> files(String folder, String... extensions) throws IOException {
        Path root = Path.of(folder);
        if (!Files.isDirectory(root)) {
            throw new SkipException("no " + folder);
        }
        try (Stream<Path> all = Files.walk(root)) {
            return all.filter(Files::isRegularFile)
                .filter(p -> {
                    for (String ext : extensions) {
                        if (p.getFileName().toString().endsWith(ext)) {
                            return true;
                        }
                    }
                    return false;
                })
                .sorted().toList();
        }
    }

    @Test
    public void validSourcesGiveTheTreeOfTheFullPredictionAndNeedNoSecondParse() throws IOException {
        int parsed = 0;
        int sllParses = 0;
        List<String> fellBack = new ArrayList<>();
        List<Path> wurst = files("temp/WurstStdlib2", ".wurst");
        assertTrue(wurst.size() > 100, "the standard library is checked out: " + wurst.size() + " files");
        for (Path file : wurst) {
            String source = Files.readString(file);
            Outcome full = parse(Language.WURST, file.toString(), source, false);
            Outcome sll = parse(Language.WURST, file.toString(), source, true);
            assertEquals(sll.diagnostics, full.diagnostics, "diagnostics of " + file);
            assertEquals(sll.tree, full.tree, "tree of " + file);
            parsed++;
            sllParses += sll.sllParses;
            if (sll.fallbacks > 0) {
                fellBack.add(file.toString());
            }
        }
        for (Path file : List.of(Path.of("src/main/resources/common.j"), Path.of("src/main/resources/blizzard.j"))) {
            String source = Files.readString(file);
            Outcome full = parse(Language.JASS, file.toString(), source, false);
            Outcome sll = parse(Language.JASS, file.toString(), source, true);
            assertEquals(sll.diagnostics, full.diagnostics, "diagnostics of " + file);
            assertEquals(sll.tree, full.tree, "tree of " + file);
            parsed++;
            sllParses += sll.sllParses;
            if (sll.fallbacks > 0) {
                fellBack.add(file.toString());
            }
        }
        for (Path file : files("src/test/resources", ".jurst")) {
            String source = Files.readString(file);
            Outcome full = parse(Language.JURST, file.toString(), source, false);
            Outcome sll = parse(Language.JURST, file.toString(), source, true);
            assertEquals(sll.diagnostics, full.diagnostics, "diagnostics of " + file);
            assertEquals(sll.tree, full.tree, "tree of " + file);
            parsed++;
            sllParses += sll.sllParses;
            if (sll.fallbacks > 0) {
                fellBack.add(file.toString());
            }
        }
        // A valid file which needs the context of its rules is parsed again, and gives the same tree (above). The pass
        // is only worth having while that is the exception.
        assertTrue(fellBack.size() * 10 <= parsed,
            fellBack.size() + " of " + parsed + " valid files were not accepted in the SLL pass: " + fellBack);
        assertEquals(sllParses + fellBack.size(), parsed, "every file is accepted in the SLL pass or parsed again");
    }

    private record Broken(Language language, String name, String source) {
    }

    private static List<Broken> brokenSources() {
        List<Broken> all = new ArrayList<>();
        String[] wurst = {
            "package P\nfunction foo(\n",
            "package P\nfunction foo()\n    int x =\n",
            "package P\nclass A\n    function f()\n        if\n            skip\n",
            "package P\nfunction foo()\n    let s = \"unterminated\n",
            "package P\nfunction foo()\n    int x = 1 §\n",
            "package P\nfunction foo()\n    foo(1,,2)\n",
            "package P\nfunction foo()\n    /** doc */\n    skip\n",
            "package P\nclass A\n    construct(\n",
            "package P\nfunction foo() returns\n    return 1\n",
            "package P\nimport\n",
            "package P\nenum E\n    A\n    ,\n",
            "package P\nfunction foo()\n    for i in\n        skip\n",
            "package P\nfunction foo()\n    x = = 2\n",
            "package P\nfunction foo()\n    return ]\n",
            "package P\ninit\n    1 +\n",
            // a lexer error before the parse error, and one after it
            "package P\n§\nfunction a(\n",
            "package P\nfunction a(\nfunction b()\n    int x = 1 §\n",
            // tabs, which are a warning, in a file which is broken after them
            "package P\nfunction foo()\n\tskip\n\tfoo(\n",
            // not a file at all
            "this is not wurst at all\n\n\t\t  ((( ]]] \n",
            "",
            "\n\n\n",
        };
        for (int i = 0; i < wurst.length; i++) {
            all.add(new Broken(Language.WURST, "broken" + i + ".wurst", wurst[i]));
        }
        // too many syntax errors: the second parse is given up at the 16th, and the unit is empty (`TooManyErrorsException`)
        all.add(new Broken(Language.WURST, "many.wurst", "package P\nfunction foo()\n" + "    int x = = 2\n".repeat(40)));
        // a long valid prefix and an error at the very end
        StringBuilder longFile = new StringBuilder("package P\n");
        for (int i = 0; i < 300; i++) {
            longFile.append("function f").append(i).append("(int a) returns int\n    return a + ").append(i).append("\n");
        }
        all.add(new Broken(Language.WURST, "late.wurst", longFile + "function broken(\n"));
        String[] jass = {
            "function foo takes nothing returns nothing\n  call\nendfunction\n",
            "globals\n  integer x =\nendglobals\n",
            "function takes\n",
            "function foo takes nothing returns nothing\n  set x = §\nendfunction\n",
            "native foo takes integer returns\n",
        };
        for (int i = 0; i < jass.length; i++) {
            all.add(new Broken(Language.JASS, "broken" + i + ".j", jass[i]));
            all.add(new Broken(Language.JURST, "broken" + i + ".jurst", jass[i]));
        }
        return all;
    }

    @Test
    public void aBrokenSourceReportsWhatTheFullPredictionReports() {
        int withErrors = 0;
        int fellBack = 0;
        java.util.EnumMap<Language, Integer> fellBackPerLanguage = new java.util.EnumMap<>(Language.class);
        for (Broken b : brokenSources()) {
            Outcome full = parse(b.language, b.name, b.source, false);
            Outcome sll = parse(b.language, b.name, b.source, true);
            assertEquals(sll.diagnostics, full.diagnostics, "diagnostics of " + b.name + ": " + b.source);
            assertEquals(sll.tree, full.tree, "tree of " + b.name);
            assertEquals(sll.sllParses + sll.fallbacks, 1, "the SLL pass was tried, and the file is counted once: " + b.name);
            if (!full.diagnostics.isEmpty()) {
                withErrors++;
            }
            if (sll.fallbacks > 0) {
                // a source which was parsed again reports what the full prediction reports (above), so it reports something
                assertFalse(full.diagnostics.isEmpty(), "a source was parsed again but has no diagnostic: " + b.name);
                fellBack++;
                fellBackPerLanguage.merge(b.language, 1, Integer::sum);
            }
        }
        // a source with only a lexer error is accepted in the SLL pass (the lexer reports it once, as it always did)
        for (Language language : Language.values()) {
            assertTrue(fellBackPerLanguage.getOrDefault(language, 0) >= 2,
                "broken " + language + " sources which were parsed again: " + fellBackPerLanguage);
        }
        assertTrue(withErrors >= 20, "the broken sources report errors: " + withErrors);
        assertTrue(fellBack >= 15 && fellBack <= withErrors, "sources parsed again: " + fellBack + " of " + withErrors + " with errors");
    }

    @Test
    public void aSourceWhichTheErrorLimitStopsIsStillCounted() {
        String source = "package P\nfunction foo()\n" + "    int x = = 2\n".repeat(40);
        Outcome full = parse(Language.WURST, "many.wurst", source, false);
        Outcome sll = parse(Language.WURST, "many.wurst", source, true);

        assertEquals(full.diagnostics.size(), 16, "the limit is reached, so the unit is empty");
        assertEquals(sll.diagnostics, full.diagnostics);
        assertEquals(sll.tree, full.tree);
        assertEquals(sll.fallbacks, 1, "the SLL pass did not accept it and the second parse was started");
        assertEquals(sll.sllParses, 0);
    }

    @Test
    public void aSourceWhichTheLexerErrorLimitStopsInTheSllPassIsInNeitherCount() {
        // the lexer reads while the SLL pass runs, and its 16th error ends the parse before the pass has decided
        String source = "package P\nfunction foo()\n" + "    int x = 1 §\n".repeat(40);
        Outcome full = parse(Language.WURST, "lexer-many.wurst", source, false);
        Outcome sll = parse(Language.WURST, "lexer-many.wurst", source, true);

        assertEquals(full.diagnostics.size(), 16);
        assertEquals(sll.diagnostics, full.diagnostics);
        assertEquals(sll.tree, full.tree);
        assertEquals(sll.sllParses + sll.fallbacks, 0);
    }

    @Test
    public void aLexerErrorIsReportedOnce() {
        String source = "package P\nfunction foo()\n    int x = 1 §\n    int y = 2\n";
        Outcome full = parse(Language.WURST, "lexer.wurst", source, false);
        Outcome sll = parse(Language.WURST, "lexer.wurst", source, true);

        assertFalse(full.diagnostics.isEmpty(), "the character is reported");
        assertEquals(sll.diagnostics, full.diagnostics);
        assertEquals(sll.diagnostics.stream().filter(d -> d.contains("§")).count(),
            full.diagnostics.stream().filter(d -> d.contains("§")).count(), "reported as often as before");
    }

    @Test
    public void oneParserParsesManyFilesAndCountsThem() throws IOException {
        WurstGuiLogger gui = new WurstGuiLogger();
        WurstParser parser = new WurstParser(new ErrorHandler(gui), gui);
        parser.parse(new StringReader("package A\nfunction a()\n    skip\n"), "a.wurst", false);
        parser.parse(new StringReader("package B\nfunction b(\n"), "b.wurst", false);
        parser.parse(new StringReader("package C\nfunction c()\n    skip\n"), "c.wurst", false);

        assertEquals(parser.getSllParses(), 2);
        assertEquals(parser.getFallbacks(), 1);
    }
}
