package tests.wurstscript.tests;

import de.peeeq.wurstio.TimeTaker;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.requests.MapRequest;
import de.peeeq.wurstio.utils.FileUtils;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.luaAst.LuaCompilationUnit;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * The incremental model is only as good as its change propagation: after any sequence of edits the
 * model a language server keeps must say what a build of the same files from scratch says, and
 * compiling it in place must produce the script the fresh build produces.
 * <p>
 * A seeded simulation edits a small project of Wurst packages and Jass files (functions, globals,
 * calls, imports, new, deleted, renamed and moved files). Every state is built from scratch first,
 * as the expectation. Only then is the same sequence replayed on one long-lived {@link ModelManagerImpl}
 * with the calls the language worker makes (sync, remove, reconcile), because a build from scratch
 * clears the shared lookup caches and would hide a stale cache entry of the incremental model.
 * <p>
 * Only states whose packages are unique, sit in a file of their name and import packages which exist
 * are compared. A build from scratch stops before validation when it finds anything else (an import
 * which does not resolve, a package defined twice), so its report is not comparable with the one of a
 * model that validates the files it re-checks. The sequence passes through such states all the same:
 * the model has to recover from them.
 */
public class IncrementalModelOracleTests {

    private static final int STEPS = 24;

    /**
     * The seeds which found something once: 10, 24 and 40 (files edited while an import did not resolve were
     * never checked) and 74 (the other definition of a duplicated package kept its error).
     * INCREMENTAL_ORACLE_SEEDS=n runs seeds 1 to n instead, to look for more.
     */
    @DataProvider(name = "seeds")
    public Object[][] seeds() {
        String more = System.getenv("INCREMENTAL_ORACLE_SEEDS");
        if (more == null) {
            return new Object[][]{{10L}, {24L}, {40L}, {74L}};
        }
        int count = Integer.parseInt(more);
        Object[][] seeds = new Object[count][];
        for (int i = 0; i < count; i++) {
            seeds[i] = new Object[]{(long) (i + 1)};
        }
        return seeds;
    }

    @Test(dataProvider = "seeds")
    public void incrementalModelMatchesFreshBuild(long seed) throws Exception {
        File project = new File("./temp/incrementalOracle" + seed + "/");
        newCleanFolder(project);

        // 1. what a build from scratch says about every state
        Simulation sim = new Simulation(seed);
        Map<String, String> onDisk = new TreeMap<>();
        List<Observation> expected = new ArrayList<>();
        List<String> history = new ArrayList<>();
        history.add(sim.lastEdit);
        applyToDisk(project, onDisk, sim.render());
        expected.add(freshObservation(project));
        int compared = 1;
        int compiled = expected.get(0).script != null ? 1 : 0;
        for (int step = 1; step <= STEPS; step++) {
            sim.step();
            history.add(sim.lastEdit);
            applyToDisk(project, onDisk, sim.render());
            if (sim.structurallyValid()) {
                Observation fresh = freshObservation(project);
                expected.add(fresh);
                compared++;
                if (fresh.script != null) {
                    compiled++;
                }
            } else {
                expected.add(null);
            }
        }
        System.out.println("incremental oracle, seed " + seed + ": " + compared + " of " + (STEPS + 1)
            + " states compared, " + compiled + " of them compiled");
        assertTrue(compared >= 3, "too few comparable states: " + compared + " in " + history);
        assertTrue(compiled >= 1, "too few error free states: " + compiled + " in " + history);

        // 2. the same sequence on one model, the way the language worker drives it
        newCleanFolder(project);
        sim = new Simulation(seed);
        onDisk.clear();
        applyToDisk(project, onDisk, sim.render());
        ModelManagerImpl manager = new ModelManagerImpl(project, new BufferManager());
        DiagnosticsLog log = new DiagnosticsLog(manager);
        manager.buildProject();
        assertSameObservation(seed, 0, history, expected.get(0), observe(project, manager, log, true));

        Random how = new Random(seed * 31 + 7);
        for (int step = 1; step <= STEPS; step++) {
            sim.step();
            Map<String, String> next = sim.render();
            ModelManager.Changes changes = ModelManager.Changes.empty();
            for (Map.Entry<String, String> file : next.entrySet()) {
                if (file.getValue().equals(onDisk.get(file.getKey()))) {
                    continue;
                }
                File f = new File(project, file.getKey());
                FileUtils.write(file.getValue(), f);
                // an editor buffer is synced by content, a file change on disk by path
                changes = changes.mergeWith(how.nextBoolean()
                    ? manager.syncCompilationUnitContent(WFile.create(f), file.getValue())
                    : manager.syncCompilationUnit(WFile.create(f)));
            }
            for (String path : onDisk.keySet()) {
                if (!next.containsKey(path)) {
                    File f = new File(project, path);
                    if (!f.delete()) {
                        throw new IOException("could not delete " + f);
                    }
                    changes = changes.mergeWith(manager.removeCompilationUnit(WFile.create(f)));
                }
            }
            onDisk.clear();
            onDisk.putAll(next);
            manager.reconcile(changes);
            if (expected.get(step) != null) {
                assertSameObservation(seed, step, history, expected.get(step), observe(project, manager, log, true));
            }
        }
    }

    private static void assertSameObservation(long seed, int step, List<String> history,
                                              Observation expected, Observation actual) {
        String context = "seed " + seed + " after step " + step + " (" + history.get(step) + ")\n"
            + "edits so far: " + history.subList(0, step + 1);
        assertEquals(actual.diagnostics, expected.diagnostics,
            "diagnostics differ, " + context + "\n" + describeDifference(expected.diagnostics, actual.diagnostics));
        assertEquals(actual.script, expected.script, "emitted Lua differs, " + context);
    }

    private static String describeDifference(Map<String, List<String>> expected, Map<String, List<String>> actual) {
        StringBuilder sb = new StringBuilder();
        Set<String> files = new TreeSet<>(expected.keySet());
        files.addAll(actual.keySet());
        for (String file : files) {
            List<String> e = expected.getOrDefault(file, Collections.emptyList());
            List<String> a = actual.getOrDefault(file, Collections.emptyList());
            if (!e.equals(a)) {
                sb.append("in ").append(file).append("\n  fresh build says:   ").append(e.stream()
                    .map(d -> d.replaceAll("\\s+", " ")).toList())
                    .append("\n  incremental model:  ").append(a.stream().map(d -> d.replaceAll("\\s+", " ")).toList())
                    .append("\n");
            }
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------- observations

    private static final class Observation {
        /** file name -> sorted diagnostics, files without any left out */
        final Map<String, List<String>> diagnostics;
        /** the Lua a compilation emits, or why it could not compile; null while the project has errors */
        final String script;

        Observation(Map<String, List<String>> diagnostics, String script) {
            this.diagnostics = diagnostics;
            this.script = script;
        }
    }

    private static Observation freshObservation(File project) {
        ModelManagerImpl manager = new ModelManagerImpl(project, new BufferManager());
        DiagnosticsLog log = new DiagnosticsLog(manager);
        manager.buildProject();
        return observe(project, manager, log, false);
    }

    /**
     * @param reuseCheck compile like a run does, which does not check a model again that the manager has
     *                   checked completely; a fresh build is compiled with a check of its own as the reference
     */
    private static Observation observe(File project, ModelManagerImpl manager, DiagnosticsLog log, boolean reuseCheck) {
        Map<String, List<String>> diagnostics = log.current();
        boolean hasErrors = log.hasErrors();
        return new Observation(diagnostics, hasErrors ? null : compile(project, manager, reuseCheck));
    }

    /** Compiles the managed model itself, as a run does. */
    private static String compile(File project, ModelManagerImpl manager, boolean reuseCheck) {
        try {
            WurstGui gui = new WurstGuiLogger();
            RunArgs runArgs = new RunArgs("-lua");
            WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(new TimeTaker.Default(), project, gui, null,
                runArgs);
            WurstModel model = manager.getModel();
            if (reuseCheck) {
                MapRequest.checkModel(manager, compiler, model, runArgs);
            } else {
                compiler.checkProg(model);
            }
            if (gui.getErrorCount() > 0) {
                return "check errors: " + gui.getErrorList();
            }
            compiler.translateProgToIm(model);
            if (gui.getErrorCount() > 0) {
                return "translation errors: " + gui.getErrorList();
            }
            LuaCompilationUnit lua = compiler.transformProgToLua();
            StringBuilder sb = new StringBuilder();
            lua.print(sb, 0);
            return sb.toString();
        } catch (RuntimeException e) {
            return "exception: " + e;
        }
    }

    /** The diagnostics a client would show: the latest publication of each file. */
    private static final class DiagnosticsLog {
        private final Map<String, List<String>> latest = new TreeMap<>();
        private final Set<String> errorFiles = new TreeSet<>();

        DiagnosticsLog(ModelManagerImpl manager) {
            manager.onCompilationResult((PublishDiagnosticsParams params) -> {
                String file = WFile.create(params.getUri()).getUriString();
                file = file.substring(file.lastIndexOf('/') + 1);
                List<String> lines = new ArrayList<>();
                boolean errors = false;
                for (Diagnostic d : params.getDiagnostics()) {
                    lines.add(d.toString());
                    errors |= d.getSeverity() == DiagnosticSeverity.Error;
                }
                Collections.sort(lines);
                latest.put(file, lines);
                if (errors) {
                    errorFiles.add(file);
                } else {
                    errorFiles.remove(file);
                }
            });
        }

        Map<String, List<String>> current() {
            Map<String, List<String>> result = new TreeMap<>();
            for (Map.Entry<String, List<String>> e : latest.entrySet()) {
                if (!e.getValue().isEmpty()) {
                    result.put(e.getKey(), e.getValue());
                }
            }
            return result;
        }

        boolean hasErrors() {
            return !errorFiles.isEmpty();
        }
    }

    // ---------------------------------------------------------------- the project on disk

    private static void newCleanFolder(File f) throws IOException {
        FileUtils.deleteRecursively(f);
        java.nio.file.Files.createDirectories(new File(f, "wurst").toPath());
    }

    /** Writes what changed and deletes what is gone, so the folder holds exactly {@code next}. */
    private static void applyToDisk(File project, Map<String, String> onDisk, Map<String, String> next)
        throws IOException {
        for (Map.Entry<String, String> file : next.entrySet()) {
            if (!file.getValue().equals(onDisk.get(file.getKey()))) {
                FileUtils.write(file.getValue(), new File(project, file.getKey()));
            }
        }
        for (String path : new ArrayList<>(onDisk.keySet())) {
            if (!next.containsKey(path) && !new File(project, path).delete()) {
                throw new IOException("could not delete " + path);
            }
        }
        onDisk.clear();
        onDisk.putAll(next);
    }

    // ---------------------------------------------------------------- the simulated project

    private enum UseKind {CALL, READ, WRITE}

    private record Use(UseKind kind, String name, int arity) {
    }

    private static final class Fn {
        int arity;
        final List<Use> uses = new ArrayList<>();

        Fn(int arity) {
            this.arity = arity;
        }
    }

    private static final class Pkg {
        String name;
        final Set<String> imports = new TreeSet<>();
        final Map<String, Fn> funcs = new TreeMap<>();
        final Set<String> globals = new TreeSet<>();
        int noise;

        Pkg(String name) {
            this.name = name;
        }
    }

    private static final class JassFile {
        final Map<String, Fn> funcs = new TreeMap<>();
        final Set<String> globals = new TreeSet<>();
        int noise;
    }

    private static final class Simulation {
        private static final String[] PACKAGES = {"PkgA", "PkgB", "PkgC", "PkgD", "PkgE"};
        private static final String[] FUNCS = {"f0", "f1", "f2", "f3"};
        private static final String[] GLOBALS = {"g0", "g1", "g2"};
        private static final String[] JASS_FUNCS = {"JassF0", "JassF1", "JassF2"};
        private static final String[] JASS_GLOBALS = {"jassG0", "jassG1"};
        private static final String[] JASS_FILES = {"war3map.j", "extra.j", "other.j"};
        private static final String[] STEMS = {"PkgA", "PkgB", "PkgC", "PkgD", "PkgE", "Moved1", "Moved2"};

        private final Random rnd;
        /** file stem -> the package declared in it */
        private final Map<String, Pkg> files = new TreeMap<>();
        private final Map<String, JassFile> jass = new TreeMap<>();
        String lastEdit = "initial project";

        Simulation(long seed) {
            rnd = new Random(seed);
            Pkg a = new Pkg("PkgA");
            a.imports.add("PkgB");
            Fn fa = new Fn(0);
            fa.uses.add(new Use(UseKind.CALL, "f1", 1));
            fa.uses.add(new Use(UseKind.CALL, "JassF0", 1));
            a.funcs.put("f0", fa);
            files.put("PkgA", a);

            Pkg b = new Pkg("PkgB");
            b.imports.add("PkgC");
            Fn fb = new Fn(1);
            fb.uses.add(new Use(UseKind.CALL, "f2", 0));
            fb.uses.add(new Use(UseKind.WRITE, "g1", 0));
            b.funcs.put("f1", fb);
            b.globals.add("g0");
            files.put("PkgB", b);

            Pkg c = new Pkg("PkgC");
            c.funcs.put("f2", new Fn(0));
            c.globals.add("g1");
            files.put("PkgC", c);

            JassFile war3map = new JassFile();
            Fn j0 = new Fn(1);
            j0.uses.add(new Use(UseKind.CALL, "JassF1", 0));
            war3map.funcs.put("JassF0", j0);
            war3map.funcs.put("JassF1", new Fn(0));
            war3map.globals.add("jassG0");
            jass.put("war3map.j", war3map);
        }

        // ---- rendering

        Map<String, String> render() {
            Map<String, String> result = new TreeMap<>();
            result.put("wurst/Wurst.wurst", "package Wurst\n");
            for (Map.Entry<String, Pkg> e : files.entrySet()) {
                result.put("wurst/" + e.getKey() + ".wurst", renderPackage(e.getValue()));
            }
            for (Map.Entry<String, JassFile> e : jass.entrySet()) {
                result.put("wurst/" + e.getKey(), renderJass(e.getValue()));
            }
            return result;
        }

        private static String renderPackage(Pkg p) {
            StringBuilder sb = new StringBuilder("package " + p.name + "\n");
            for (String i : p.imports) {
                sb.append("import ").append(i).append("\n");
            }
            sb.append("// ").append(p.noise).append("\n");
            for (String g : p.globals) {
                sb.append("public int ").append(g).append(" = 0\n");
            }
            for (Map.Entry<String, Fn> f : p.funcs.entrySet()) {
                sb.append("public function ").append(f.getKey()).append("(");
                for (int i = 0; i < f.getValue().arity; i++) {
                    sb.append(i > 0 ? ", " : "").append("int a").append(i);
                }
                sb.append(") returns int\n");
                int n = 0;
                for (Use u : f.getValue().uses) {
                    sb.append("    ");
                    switch (u.kind) {
                        case CALL -> sb.append("let u").append(n).append(" = ").append(u.name).append("(")
                            .append(String.join(", ", Collections.nCopies(u.arity, "0"))).append(")");
                        case READ -> sb.append("let u").append(n).append(" = ").append(u.name);
                        case WRITE -> sb.append(u.name).append(" = ").append(n);
                    }
                    sb.append("\n");
                    n++;
                }
                sb.append("    return 0\n");
            }
            return sb.toString();
        }

        private static String renderJass(JassFile j) {
            StringBuilder sb = new StringBuilder("globals\n");
            for (String g : j.globals) {
                sb.append("    integer ").append(g).append(" = 0\n");
            }
            sb.append("endglobals\n// ").append(j.noise).append("\n");
            for (Map.Entry<String, Fn> f : j.funcs.entrySet()) {
                sb.append("function ").append(f.getKey()).append(" takes ");
                if (f.getValue().arity == 0) {
                    sb.append("nothing");
                }
                for (int i = 0; i < f.getValue().arity; i++) {
                    sb.append(i > 0 ? ", " : "").append("integer a").append(i);
                }
                sb.append(" returns integer\n");
                int uses = f.getValue().uses.size();
                for (int n = 0; n < uses; n++) {
                    sb.append("    local integer v").append(n).append("\n");
                }
                int n = 0;
                for (Use u : f.getValue().uses) {
                    sb.append("    set ");
                    switch (u.kind) {
                        case CALL -> sb.append("v").append(n).append(" = ").append(u.name).append("(")
                            .append(String.join(", ", Collections.nCopies(u.arity, "0"))).append(")");
                        case READ -> sb.append("v").append(n).append(" = ").append(u.name);
                        case WRITE -> sb.append(u.name).append(" = ").append(n);
                    }
                    sb.append("\n");
                    n++;
                }
                sb.append("    return 0\nendfunction\n");
            }
            return sb.toString();
        }

        boolean structurallyValid() {
            Set<String> names = new TreeSet<>();
            for (Map.Entry<String, Pkg> e : files.entrySet()) {
                if (!names.add(e.getValue().name) || !e.getKey().equals(e.getValue().name)) {
                    return false;
                }
            }
            for (Pkg p : files.values()) {
                if (!names.containsAll(p.imports)) {
                    return false;
                }
            }
            return true;
        }

        // ---- edits

        void step() {
            for (int attempt = 0; attempt < 100; attempt++) {
                String description = edit(rnd.nextInt(28));
                if (description != null) {
                    lastEdit = description;
                    return;
                }
            }
            throw new IllegalStateException("no applicable edit");
        }

        private <T> T pick(List<T> list) {
            return list.isEmpty() ? null : list.get(rnd.nextInt(list.size()));
        }

        private String pickStem() {
            return pick(new ArrayList<>(files.keySet()));
        }

        private String pickJassFile() {
            return pick(new ArrayList<>(jass.keySet()));
        }

        private Use randomUse(boolean fromJass) {
            int r = rnd.nextInt(10);
            if (fromJass) {
                // mostly Jass names, now and then one only Wurst declares
                if (r < 5) {
                    return new Use(UseKind.CALL, JASS_FUNCS[rnd.nextInt(JASS_FUNCS.length)], rnd.nextInt(3));
                } else if (r < 8) {
                    return new Use(rnd.nextBoolean() ? UseKind.READ : UseKind.WRITE,
                        JASS_GLOBALS[rnd.nextInt(JASS_GLOBALS.length)], 0);
                }
                return new Use(UseKind.CALL, FUNCS[rnd.nextInt(FUNCS.length)], rnd.nextInt(3));
            }
            if (r < 4) {
                return new Use(UseKind.CALL, FUNCS[rnd.nextInt(FUNCS.length)], rnd.nextInt(3));
            } else if (r < 6) {
                return new Use(UseKind.CALL, JASS_FUNCS[rnd.nextInt(JASS_FUNCS.length)], rnd.nextInt(3));
            } else if (r < 8) {
                return new Use(rnd.nextBoolean() ? UseKind.READ : UseKind.WRITE,
                    GLOBALS[rnd.nextInt(GLOBALS.length)], 0);
            }
            return new Use(rnd.nextBoolean() ? UseKind.READ : UseKind.WRITE,
                JASS_GLOBALS[rnd.nextInt(JASS_GLOBALS.length)], 0);
        }

        /** Returns what was done, or null when this kind of edit does not apply now. */
        private String edit(int kind) {
            switch (kind) {
                case 0, 1 -> {
                    String stem = pickStem();
                    if (stem == null) return null;
                    String f = FUNCS[rnd.nextInt(FUNCS.length)];
                    Pkg p = files.get(stem);
                    if (p.funcs.containsKey(f)) return null;
                    int arity = rnd.nextInt(3);
                    p.funcs.put(f, new Fn(arity));
                    return "add function " + f + "/" + arity + " to " + stem;
                }
                case 2 -> {
                    String stem = pickStem();
                    if (stem == null) return null;
                    String f = pick(new ArrayList<>(files.get(stem).funcs.keySet()));
                    if (f == null) return null;
                    files.get(stem).funcs.remove(f);
                    return "remove function " + f + " from " + stem;
                }
                case 3 -> {
                    String stem = pickStem();
                    if (stem == null) return null;
                    String f = pick(new ArrayList<>(files.get(stem).funcs.keySet()));
                    if (f == null) return null;
                    Fn fn = files.get(stem).funcs.get(f);
                    fn.arity = (fn.arity + 1 + rnd.nextInt(2)) % 3;
                    return "change arity of " + f + " in " + stem + " to " + fn.arity;
                }
                case 4, 5, 6 -> {
                    String stem = pickStem();
                    if (stem == null) return null;
                    String f = pick(new ArrayList<>(files.get(stem).funcs.keySet()));
                    if (f == null) return null;
                    Use use = randomUse(false);
                    files.get(stem).funcs.get(f).uses.add(use);
                    return "add use " + use + " to " + f + " in " + stem;
                }
                case 7 -> {
                    String stem = pickStem();
                    if (stem == null) return null;
                    String f = pick(new ArrayList<>(files.get(stem).funcs.keySet()));
                    if (f == null || files.get(stem).funcs.get(f).uses.isEmpty()) return null;
                    List<Use> uses = files.get(stem).funcs.get(f).uses;
                    Use removed = uses.remove(rnd.nextInt(uses.size()));
                    return "remove use " + removed + " from " + f + " in " + stem;
                }
                case 8, 9 -> {
                    String stem = pickStem();
                    if (stem == null) return null;
                    String other = PACKAGES[rnd.nextInt(PACKAGES.length)];
                    Pkg p = files.get(stem);
                    if (other.equals(p.name) || !p.imports.add(other)) return null;
                    return "import " + other + " in " + stem;
                }
                case 10 -> {
                    String stem = pickStem();
                    if (stem == null) return null;
                    String i = pick(new ArrayList<>(files.get(stem).imports));
                    if (i == null) return null;
                    files.get(stem).imports.remove(i);
                    return "remove import " + i + " from " + stem;
                }
                case 11 -> {
                    String stem = pickStem();
                    if (stem == null) return null;
                    String g = GLOBALS[rnd.nextInt(GLOBALS.length)];
                    if (!files.get(stem).globals.add(g)) return null;
                    return "add global " + g + " to " + stem;
                }
                case 12 -> {
                    String stem = pickStem();
                    if (stem == null) return null;
                    String g = pick(new ArrayList<>(files.get(stem).globals));
                    if (g == null) return null;
                    files.get(stem).globals.remove(g);
                    return "remove global " + g + " from " + stem;
                }
                case 13 -> {
                    String stem = STEMS[rnd.nextInt(STEMS.length)];
                    if (files.containsKey(stem)) return null;
                    // sometimes a name another file declares already
                    String name = rnd.nextInt(6) == 0 ? PACKAGES[rnd.nextInt(PACKAGES.length)]
                        : stem.startsWith("Pkg") ? stem : PACKAGES[rnd.nextInt(PACKAGES.length)] + "X";
                    Pkg p = new Pkg(name);
                    p.funcs.put(FUNCS[rnd.nextInt(FUNCS.length)], new Fn(rnd.nextInt(3)));
                    files.put(stem, p);
                    return "add file " + stem + " declaring " + name;
                }
                case 14 -> {
                    String stem = pickStem();
                    if (stem == null || files.size() < 2) return null;
                    files.remove(stem);
                    return "delete file " + stem;
                }
                case 15 -> {
                    String stem = pickStem();
                    if (stem == null) return null;
                    String name = PACKAGES[rnd.nextInt(PACKAGES.length)];
                    if (files.get(stem).name.equals(name)) return null;
                    files.get(stem).name = name;
                    return "rename package of " + stem + " to " + name;
                }
                case 16 -> {
                    String stem = pickStem();
                    String to = STEMS[rnd.nextInt(STEMS.length)];
                    if (stem == null || files.containsKey(to)) return null;
                    files.put(to, files.remove(stem));
                    return "move " + stem + " to file " + to;
                }
                case 17 -> {
                    String stem = pickStem();
                    if (stem == null) return null;
                    files.get(stem).noise++;
                    return "touch " + stem;
                }
                case 18 -> {
                    String name = pickJassFile();
                    if (name == null) return null;
                    String f = JASS_FUNCS[rnd.nextInt(JASS_FUNCS.length)];
                    if (jass.get(name).funcs.containsKey(f)) return null;
                    int arity = rnd.nextInt(3);
                    jass.get(name).funcs.put(f, new Fn(arity));
                    return "add jass function " + f + "/" + arity + " to " + name;
                }
                case 19 -> {
                    String name = pickJassFile();
                    if (name == null) return null;
                    String f = pick(new ArrayList<>(jass.get(name).funcs.keySet()));
                    if (f == null) return null;
                    jass.get(name).funcs.remove(f);
                    return "remove jass function " + f + " from " + name;
                }
                case 20 -> {
                    String name = pickJassFile();
                    if (name == null) return null;
                    String f = pick(new ArrayList<>(jass.get(name).funcs.keySet()));
                    if (f == null) return null;
                    Fn fn = jass.get(name).funcs.get(f);
                    fn.arity = (fn.arity + 1 + rnd.nextInt(2)) % 3;
                    return "change arity of jass function " + f + " in " + name + " to " + fn.arity;
                }
                case 21 -> {
                    String name = pickJassFile();
                    if (name == null) return null;
                    String f = pick(new ArrayList<>(jass.get(name).funcs.keySet()));
                    if (f == null) return null;
                    Use use = randomUse(true);
                    jass.get(name).funcs.get(f).uses.add(use);
                    return "add use " + use + " to jass function " + f + " in " + name;
                }
                case 22 -> {
                    String name = pickJassFile();
                    if (name == null) return null;
                    String g = JASS_GLOBALS[rnd.nextInt(JASS_GLOBALS.length)];
                    if (rnd.nextBoolean()) {
                        if (!jass.get(name).globals.add(g)) return null;
                        return "add jass global " + g + " to " + name;
                    }
                    if (!jass.get(name).globals.remove(g)) return null;
                    return "remove jass global " + g + " from " + name;
                }
                case 23 -> {
                    String name = pickJassFile();
                    if (name == null) return null;
                    jass.get(name).noise++;
                    return "touch jass file " + name;
                }
                case 24 -> {
                    String name = JASS_FILES[rnd.nextInt(JASS_FILES.length)];
                    if (rnd.nextBoolean()) {
                        if (jass.containsKey(name)) return null;
                        JassFile j = new JassFile();
                        j.funcs.put(JASS_FUNCS[rnd.nextInt(JASS_FUNCS.length)], new Fn(rnd.nextInt(3)));
                        jass.put(name, j);
                        return "add jass file " + name;
                    }
                    if (!jass.containsKey(name) || jass.size() < 2) return null;
                    jass.remove(name);
                    return "delete jass file " + name;
                }
                default -> {
                    return repair();
                }
            }
        }

        /**
         * Removes what does not resolve and imports what is needed, so that error free states keep
         * coming up. Those are the ones a compilation is compared on.
         */
        private String repair() {
            Map<String, Integer> jassArity = new TreeMap<>();
            Set<String> jassGlobals = new TreeSet<>();
            for (JassFile j : jass.values()) {
                for (Map.Entry<String, Fn> f : j.funcs.entrySet()) {
                    jassArity.put(f.getKey(), f.getValue().arity);
                }
                jassGlobals.addAll(j.globals);
            }
            // package names must be unique, and a file is named after its package
            Set<String> seen = new TreeSet<>();
            for (Map.Entry<String, Pkg> e : files.entrySet()) {
                if (!seen.add(e.getValue().name)) {
                    e.getValue().name = e.getValue().name + "Z" + e.getKey();
                    seen.add(e.getValue().name);
                }
            }
            List<Pkg> packages = new ArrayList<>(files.values());
            files.clear();
            Map<String, Pkg> byName = new TreeMap<>();
            for (Pkg p : packages) {
                files.put(p.name, p);
                byName.put(p.name, p);
            }
            for (Pkg p : files.values()) {
                p.imports.removeIf(i -> !byName.containsKey(i) || i.equals(p.name));
                for (Fn fn : p.funcs.values()) {
                    fn.uses.removeIf(u -> !resolves(p, byName, jassArity, jassGlobals, u));
                }
            }
            for (Map.Entry<String, JassFile> j : jass.entrySet()) {
                for (Fn fn : j.getValue().funcs.values()) {
                    fn.uses.removeIf(u -> u.kind == UseKind.CALL
                        ? !(jassArity.containsKey(u.name) && jassArity.get(u.name) == u.arity)
                        : !jassGlobals.contains(u.name));
                }
            }
            return "repair";
        }

        private static boolean resolves(Pkg p, Map<String, Pkg> byName, Map<String, Integer> jassArity,
                                        Set<String> jassGlobals, Use u) {
            if (u.kind == UseKind.CALL) {
                if (jassArity.containsKey(u.name)) {
                    return jassArity.get(u.name) == u.arity;
                }
                List<Pkg> scope = new ArrayList<>();
                scope.add(p);
                for (String i : p.imports) {
                    scope.add(byName.get(i));
                }
                int found = 0;
                boolean arityOk = true;
                for (Pkg q : scope) {
                    Fn fn = q.funcs.get(u.name);
                    if (fn != null) {
                        found++;
                        arityOk &= fn.arity == u.arity;
                    }
                }
                return found == 1 && arityOk;
            }
            if (jassGlobals.contains(u.name)) {
                return true;
            }
            int found = p.globals.contains(u.name) ? 1 : 0;
            for (String i : p.imports) {
                found += byName.get(i).globals.contains(u.name) ? 1 : 0;
            }
            return found == 1;
        }
    }
}
