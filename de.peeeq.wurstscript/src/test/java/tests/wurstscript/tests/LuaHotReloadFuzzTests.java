package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.requests.LuaHotReloadSession;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.translation.imtranslation.IncrementalImTranslation;
import de.peeeq.wurstscript.translation.lua.translation.LuaHotReload;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.testng.AssertJUnit.assertTrue;

/**
 * Randomised differential test of the Lua hot reload (docs/LUA_HOT_RELOAD.md).
 *
 * <p>Each seed makes a program (two packages; functions; constants and array literals; variables; classes with
 * fields, methods, inheritance and overrides; an interface implemented by classes and by closures kept in a global
 * array; objects made by {@code init} and kept in globals; reload hooks) and a chain of edits of it, each made of random
 * operations which hot reload supports (body edits, tuned constants, additions of functions, classes, methods,
 * overrides, fields, globals, closures, hooks and packages), operations it applies with a warning (a changed initial
 * value of a variable or init block, a removed function or override) and operations it must refuse (a field's or a
 * global's type changed). The base program runs in the test Lua; each edit's payload is made by the session, as the
 * editor makes it, and installed into that running script. After each one:
 *
 * <ul>
 *     <li>computing the payload throws nothing but {@link LuaHotReload.Rejected}, and an edit with a change the docs
 *     say needs a restart is refused;</li>
 *     <li>an accepted payload installs without a Lua error;</li>
 *     <li>what does not depend on the state made before the reload (functions called, constants read, objects and
 *     closures made, reload hooks run) prints exactly what a fresh run of the edited program prints;</li>
 *     <li>objects and closures made before the reload run the edited bodies: they print what the same objects print in
 *     a fresh run of the edited program whose constructors and initial values are the base program's, as the running
 *     map's objects were made by them;</li>
 *     <li>the globals and fields the map had keep their values.</li>
 * </ul>
 *
 * <p>Every observation is a printed line, so a failure is a readable difference. {@code WURST_FUZZ_PROGRAMS},
 * {@code WURST_FUZZ_SEED} and {@code WURST_FUZZ_EDITS} (environment) run a larger or different campaign; a failing seed
 * leaves its sources, scripts and payloads in {@code test-output/fuzz-hotreload/<seed>}. A third of the programs are
 * built with {@code -stacktraces}. {@code WURST_FUZZ_INSERT_ANONYMOUS} also inserts anonymous closures before others
 * told apart by their order, which hot reload does not yet tell apart (see {@link #INSERT_ANONYMOUS}).
 */
public class LuaHotReloadFuzzTests extends WurstScriptTest {

    private static final RunArgs HOT_RELOAD = new RunArgs().with("-lua", "-luaHotReload", "-runcompiletimefunctions");
    private static final String OUT = "test-output/fuzz-hotreload";

    @Test
    public void reloadedProgramsBehaveLikeTheEditedProgram() throws Exception {
        int programs = envInt("WURST_FUZZ_PROGRAMS", 10);
        long seed = envLong("WURST_FUZZ_SEED", 20261024L);
        int edits = envInt("WURST_FUZZ_EDITS", 3);
        String lua;
        try {
            lua = getLuaExecutable();
        } catch (IllegalStateException e) {
            throw new SkipException("Skipped the fuzzer, which runs the scripts in Lua: " + e.getMessage());
        }
        List<String> failures = new ArrayList<>();
        Stats stats = new Stats();
        for (int i = 0; i < programs; i++) {
            long programSeed = seed + i;
            String failure = runChain(lua, programSeed, edits, stats);
            if (failure != null) {
                failures.add("seed " + programSeed + ": " + failure);
            }
        }
        System.out.println("[hot reload fuzz] programs " + programs + " from seed " + seed + ", " + stats
            + ", failures " + failures.size());
        assertTrue(failures.size() + " of " + programs + " programs failed:\n" + String.join("\n\n", failures),
            failures.isEmpty());
        // The campaign only says something if most payloads apply.
        assertTrue("too few payloads applied: " + stats, stats.applied * 2 >= stats.steps);
    }

    /** Counts of what a campaign did. */
    static final class Stats {
        int steps;
        int applied;
        int refusedAsExpected;
        int refusedMayRefuse;
        int warned;
        final Map<String, Integer> ops = new TreeMap<>();

        @Override
        public String toString() {
            return "steps " + steps + ", applied " + applied + " (with warnings " + warned + "), refused as expected "
                + refusedAsExpected + ", refused where refusing is allowed " + refusedMayRefuse + ", edit operations " + ops;
        }
    }

    /** Runs one seed's chain of edits; the failure, or null. */
    private String runChain(String lua, long seed, int edits, Stats stats) throws Exception {
        Random r = new Random(seed);
        // A third of the programs are built with -stacktraces, where every call passes its source position.
        RunArgs args = r.nextInt(3) == 0 ? HOT_RELOAD.with("-stacktraces") : HOT_RELOAD;
        Prog base = new Generator(r).base();
        Path dir = Path.of(OUT, Long.toString(seed));
        deleteRecursively(dir);
        Files.createDirectories(dir);
        List<String> report = new ArrayList<>();
        Build baseBuild;
        try {
            baseBuild = build(base, false, args);
        } catch (Throwable t) {
            writeUnits(dir, "base", base);
            return "the generated base program does not compile: " + t + "\n" + base.render(false);
        }
        writeUnits(dir, "base", base);
        write(dir, "base.lua", baseBuild.script);
        Path session = dir.resolve("session");
        Path delivery = dir.resolve("delivery");
        LuaHotReloadSession.start(session, baseBuild.manifest);

        List<Step> steps = new ArrayList<>();
        Prog current = base;
        for (int k = 1; k <= edits; k++) {
            Generator g = new Generator(r);
            Edit edit = g.edit(current);
            Step step = new Step(k, edit);
            stats.steps++;
            for (String op : edit.ops) {
                stats.ops.merge(op.replaceAll(" [A-Za-z]*\\d+(\\.\\w+)?$", ""), 1, Integer::sum);
            }
            String edited = edit.prog.render(false);
            writeUnits(dir, "edit" + k, edit.prog);
            write(dir, "edit" + k + "_ops.txt", String.join("\n", edit.ops) + "\n");
            Build e;
            Build reference;
            try {
                e = build(edit.prog, false, args);
                reference = edit.mustRefuse ? null : build(edit.prog, true, args);
            } catch (Throwable t) {
                return "the generated edit " + k + " " + edit.ops + " does not compile: " + t + "\n" + edited;
            }
            write(dir, "edit" + k + ".lua", e.script);
            try {
                LuaHotReloadSession.Sent sent = LuaHotReloadSession.send(session, delivery, e.manifest);
                if (edit.mustRefuse) {
                    return "edit " + k + " " + edit.ops + " must be refused, but its payload was made"
                        + reproduction(dir, k);
                }
                step.payload = sent.payload();
                write(dir, "edit" + k + "_payload.lua", step.payload.source());
                if (!step.payload.warnings().isEmpty()) {
                    stats.warned++;
                    write(dir, "edit" + k + "_warnings.txt", String.join("\n", step.payload.warnings()) + "\n");
                }
            } catch (LuaHotReload.Rejected rejected) {
                write(dir, "edit" + k + "_rejected.txt", rejected.getMessage() + "\n");
                if (edit.mustRefuse) {
                    stats.refusedAsExpected++;
                    // The edit is undone, as a developer would, and the next one starts from the program before it.
                    continue;
                }
                if (edit.mayRefuse || movesAnonymousSource(base, steps, edit.prog)
                    && rejected.getReasons().stream().allMatch(reason -> reason.contains(" now has the body ")
                        && reason.endsWith(" had: they moved; restart the map"))) {
                    stats.refusedMayRefuse++;
                    continue;
                }
                report.add("edit " + k + " " + edit.ops + " was refused: " + rejected.getMessage() + reproduction(dir, k));
                // Every later payload would carry the same reason.
                break;
            } catch (RuntimeException | Error t) {
                return "making the payload of edit " + k + " " + edit.ops + " threw " + stackTrace(t)
                    + reproduction(dir, k);
            }
            stats.applied++;
            step.fresh = run(lua, dir, "edit" + k + "_fresh", e.script,
                List.of(), hooksCall(e.manifest) + section("new") + call(e.manifest, "probeNew"));
            step.reference = run(lua, dir, "edit" + k + "_reference", reference.script,
                List.of(), section("old") + call(reference.manifest, "probeOld"));
            step.manifest = e.manifest;
            steps.add(step);
            current = edit.prog;
        }
        if (!steps.isEmpty()) {
            // The map: the base program, then each payload which was made, with the probes of its version after it.
            StringBuilder driver = new StringBuilder(section("before") + call(baseBuild.manifest, "probeState"));
            List<Path> payloads = new ArrayList<>();
            for (Step step : steps) {
                Path payload = dir.resolve("edit" + step.k + "_payload.lua");
                payloads.add(payload);
                driver.append(section("apply " + step.k))
                    .append("do local f = assert(io.open('").append(luaPath(payload)).append("', 'rb'))")
                    .append(" local source = f:read('a') f:close()")
                    .append(" local ok, applied, err = pcall(" + LuaHotReload.APPLY_FUNCTION + ", source)")
                    .append(" io.write('#result ', tostring(ok and applied), ' ', tostring(ok and err or applied), '\\n') end\n")
                    .append(section("state " + step.k)).append(call(step.manifest, "probeState"))
                    .append(section("old " + step.k)).append(call(step.manifest, "probeOld"))
                    .append(section("new " + step.k)).append(call(step.manifest, "probeNew"));
            }
            String map = run(lua, dir, "map", baseBuild.script, payloads, driver.toString());
            Map<String, String> sections = sections(map);
            String before = sections.get("before");
            if (before == null || before.contains("#error")) {
                return "the base program fails before any reload:\n" + map;
            }
            for (Step step : steps) {
                String apply = sections.getOrDefault("apply " + step.k, "");
                String result = apply.lines().filter(l -> l.startsWith("#result")).findFirst().orElse("#result missing");
                if (!result.startsWith("#result true")) {
                    report.add("the payload of edit " + step.k + " " + step.edit.ops + " did not install: " + result
                        + reproduction(dir, step.k));
                    break;
                }
                // The editor warns of what it knows; the map of what depends on the payloads it installed.
                if (step.edit.mustWarn && step.payload.warnings().isEmpty() && !apply.contains("#msg ")) {
                    report.add("edit " + step.k + " " + step.edit.ops + " applies without the warning it needs"
                        + reproduction(dir, step.k));
                }
                String hooks = apply.lines().filter(l -> !l.startsWith("#")).reduce("", (a, b) -> a + b + "\n");
                compare(report, step, dir, "the reload hooks", sections(step.fresh).getOrDefault("hooks", ""), hooks);
                compare(report, step, dir, "the state the map had", before, sections.get("state " + step.k));
                compare(report, step, dir, "objects and closures made before the reload",
                    sections(step.reference).get("old"), sections.get("old " + step.k));
                compare(report, step, dir, "functions, constants and new objects",
                    sections(step.fresh).get("new"), sections.get("new " + step.k));
            }
        }
        if (report.isEmpty()) {
            deleteRecursively(dir);
            return null;
        }
        return String.join("\n", report);
    }

    /** Reusing a sibling's historical body is indistinguishable from moving that anonymous closure. */
    private static boolean movesAnonymousSource(Prog base, List<Step> steps, Prog next) {
        List<Prog> versions = new ArrayList<>(List.of(base));
        steps.forEach(step -> versions.add(step.edit.prog));
        List<Clo> past = versions.stream().flatMap(p -> p.closures.stream()).filter(c -> !c.bound).toList();
        for (Clo now : next.closures) {
            if (now.bound || past.stream().anyMatch(old -> old.slot == now.slot && old.body.equals(now.body))) {
                continue;
            }
            if (past.stream().anyMatch(old -> old.slot != now.slot && old.body.equals(now.body)
                && next.closures.stream().noneMatch(kept -> !kept.bound && kept.slot == old.slot
                    && kept.body.equals(old.body)))) {
                return true;
            }
        }
        return false;
    }

    private static void compare(List<String> report, Step step, Path dir, String what, String expected, String actual)
        throws IOException {
        if (expected == null || actual == null || expected.contains("#error") || !expected.equals(actual)) {
            report.add("edit " + step.k + " " + step.edit.ops + ": " + what + " differ: "
                + firstDifference(expected, actual) + reproduction(dir, step.k));
        }
    }

    private static String reproduction(Path dir, int k) {
        return "\n  reproduction: " + dir.resolve("base.wurst") + " and " + dir.resolve("edit" + k + ".wurst")
            + " (the edits before it: edit1.wurst and on)";
    }

    private static String firstDifference(String expected, String actual) {
        if (expected == null || actual == null) {
            return "expected " + expected + "\nbut got " + actual;
        }
        String[] e = expected.split("\n", -1);
        String[] a = actual.split("\n", -1);
        for (int i = 0; i < Math.max(e.length, a.length); i++) {
            String el = i < e.length ? e[i] : "<end>";
            String al = i < a.length ? a[i] : "<end>";
            if (!el.equals(al) || el.startsWith("#error")) {
                return "line " + i + ": expected '" + el + "' but got '" + al + "'";
            }
        }
        return "?";
    }

    /** One edit of a chain and what its runs printed. */
    static final class Step {
        final int k;
        final Edit edit;
        LuaHotReload.Payload payload;
        LuaHotReload.Manifest manifest;
        String fresh;
        String reference;

        Step(int k, Edit edit) {
            this.k = k;
            this.edit = edit;
        }
    }

    record Build(String script, LuaHotReload.Manifest manifest) {
    }

    private Build build(Prog p, boolean reference, RunArgs args) {
        List<CU> units = new ArrayList<>();
        for (Map.Entry<String, String> unit : p.units(reference).entrySet()) {
            units.add(new CU(unit.getKey(), unit.getValue()));
        }
        // Exercise the private graph fork against an independent, uncached reference compilation.
        LuaHotReloadBuild b = compileLuaHotReload(false, args,
            reference ? null : new IncrementalImTranslation(),
            units.toArray(CU[]::new));
        return new Build(b.script(), b.manifest());
    }

    private static String section(String name) {
        return "io.write('#" + name + "\\n')\n";
    }

    /** Calls the function {@code source} of the build, printing what it raises. */
    private static String call(LuaHotReload.Manifest m, String source) {
        return "__fuzz_call('" + luaName(m, source) + "')\n";
    }

    /** Calls the reload hooks in the order a payload calls them. */
    private static String hooksCall(LuaHotReload.Manifest m) {
        StringBuilder sb = new StringBuilder(section("hooks"));
        for (String hook : m.hooks()) {
            sb.append("__fuzz_call('").append(hook).append("')\n");
        }
        return sb.toString();
    }

    private static String luaName(LuaHotReload.Manifest m, String source) {
        return m.functions().keySet().stream().filter(n -> n.matches(Pattern.quote(source) + "_[0-9a-f]{12}")).findFirst()
            .orElseThrow(() -> new AssertionError("no function " + source + " in " + m.functions().keySet()));
    }

    /** Runs {@code script}'s main and then {@code then}; what it printed. */
    private String run(String lua, Path dir, String name, String script, List<Path> payloads, String then)
        throws IOException, InterruptedException {
        Path scriptFile = dir.resolve(name + "_script.lua");
        Files.writeString(scriptFile, script, StandardCharsets.UTF_8);
        String runtime = "src/test/resources/luaruntime/";
        String driver = "dofile('" + runtime + "wc3shim.lua') dofile('" + runtime + "common.j.lua') dofile('" + runtime
            + "blizzard.j.lua')\n"
            + "fuzzOut = function(s) io.write(s, '\\n') end\n"
            + "dofile('" + luaPath(scriptFile) + "')\n"
            + "BJDebugMsg = function(s) io.write('#msg ', tostring(s), '\\n') end\n"
            + "function __fuzz_call(name) local f = rawget(_G, name) if not f then io.write('#error no function ', name, '\\n') return end"
            + " local ok, err = pcall(f) if not ok then io.write('#error ', tostring(err), '\\n') end end\n"
            + "do local ok, err = pcall(main) if not ok then io.write('#error main: ', tostring(err), '\\n') end end\n"
            + then;
        Path driverFile = dir.resolve(name + "_driver.lua");
        Files.writeString(driverFile, driver, StandardCharsets.UTF_8);
        File output = dir.resolve(name + "_output.txt").toFile();
        Process p = new ProcessBuilder(lua, driverFile.toString()).redirectErrorStream(true).redirectOutput(output).start();
        boolean finished = p.waitFor(30, TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly().waitFor(10, TimeUnit.SECONDS);
        }
        String text = Files.readString(output.toPath(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        if (!finished) {
            return text + "\n#error timeout\n";
        }
        return p.exitValue() == 0 ? text : text + "\n#error exit " + p.exitValue() + "\n";
    }

    private static String luaPath(Path p) {
        return p.toString().replace('\\', '/');
    }

    /** The output split at the lines {@code #name}; each section's lines. */
    private static Map<String, String> sections(String output) {
        Map<String, String> result = new LinkedHashMap<>();
        String current = "";
        StringBuilder sb = new StringBuilder();
        for (String line : output.split("\n")) {
            if (line.startsWith("#") && !line.startsWith("#error") && !line.startsWith("#result") && !line.startsWith("#msg")) {
                result.put(current, sb.toString());
                current = line.substring(1);
                sb = new StringBuilder();
            } else {
                sb.append(line).append("\n");
            }
        }
        result.put(current, sb.toString());
        return result;
    }

    /** Writes the program's units as {@code <prefix>.wurst} (the main package) and {@code <prefix>_<unit>}. */
    private static void writeUnits(Path dir, String prefix, Prog p) throws IOException {
        for (Map.Entry<String, String> unit : p.units(false).entrySet()) {
            write(dir, unit.getKey().equals("Fuzz.wurst") ? prefix + ".wurst" : prefix + "_" + unit.getKey(), unit.getValue());
        }
    }

    private static void write(Path dir, String name, String text) throws IOException {
        Files.writeString(dir.resolve(name), text, StandardCharsets.UTF_8);
    }

    private static String stackTrace(Throwable t) {
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        try (var paths = Files.walk(dir)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        }
    }

    private static int envInt(String name, int fallback) {
        String value = System.getenv(name);
        return value == null ? fallback : Integer.parseInt(value);
    }

    private static long envLong(String name, long fallback) {
        String value = System.getenv(name);
        return value == null ? fallback : Long.parseLong(value);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // The program model

    /** A function of one {@code int} parameter {@code x}; a leaf (in the library package) calls only leaves. */
    record Fn(String name, double rank, String body) {
    }

    /** A constant: an int expression, or a string made from one. */
    record Const(String name, boolean string, boolean let, String expr) {
    }

    /** A constant array literal. */
    record Arr(String name, List<Integer> entries) {
    }

    /** A variable; {@code tuned} once its initial value changed: the map keeps the value it has, a fresh run does not. */
    record Var(String name, String type, String init, boolean base, boolean tuned) {
    }

    /** A closure made by {@code makeOps} into {@code ops[b + slot]}, bound to a local or written directly. */
    record Clo(int slot, boolean bound, String body, boolean baseCaptures, boolean late) {
        Clo(int slot, boolean bound, String body) {
            this(slot, bound, body, captures(body), false);
        }

        /** Whether the body reads the parameter {@code s} of makeOps, which the closure captures. */
        static boolean captures(String body) {
            return Pattern.compile("(?<![\\w.])s(?!\\w)").matcher(body).find();
        }
    }

    /** A named closure owner whose old-generic bool argument requires a conversion bridge. */
    record Bridge(String name, int offset) {
    }

    record Snapshot(String name, int value) {
    }

    /** An object made by init into a global, with a field set after construction. */
    record Old(String global, String cls, int arg, String field, int value) {
    }

    record Hook(String name, String body) {
    }

    /** A package an edit added: a variable its init counts in, and a function. */
    record Pkg(String name, String body) {
    }

    static final class Field {
        final String name;
        String type;
        String init;
        final boolean base;
        final String baseInit;

        Field(String name, String type, String init, boolean base) {
            this.name = name;
            this.type = type;
            this.init = init;
            this.base = base;
            this.baseInit = init;
        }

        Field(Field f) {
            this.name = f.name;
            this.type = f.type;
            this.init = f.init;
            this.base = f.base;
            this.baseInit = f.baseInit;
        }
    }

    static final class Cls {
        final String name;
        final String parent;
        final boolean op;
        final boolean base;
        final int superOffset;
        final List<Field> fields = new ArrayList<>();
        /** Assignments of the constructor, {@code field = expression in p}. */
        final List<String> ctor = new ArrayList<>();
        final List<String> baseCtor = new ArrayList<>();
        final LinkedHashMap<String, String> methods = new LinkedHashMap<>();

        Cls(String name, String parent, boolean op, boolean base, int superOffset) {
            this.name = name;
            this.parent = parent;
            this.op = op;
            this.base = base;
            this.superOffset = superOffset;
        }

        Cls(Cls c) {
            this(c.name, c.parent, c.op, c.base, c.superOffset);
            for (Field f : c.fields) {
                fields.add(new Field(f));
            }
            ctor.addAll(c.ctor);
            baseCtor.addAll(c.baseCtor);
            methods.putAll(c.methods);
        }
    }

    static final class Prog {
        final List<Fn> leaves = new ArrayList<>();
        final List<Fn> funcs = new ArrayList<>();
        final List<Arr> arrays = new ArrayList<>();
        final List<Const> consts = new ArrayList<>();
        final List<Var> vars = new ArrayList<>();
        final List<Cls> classes = new ArrayList<>();
        final List<Clo> closures = new ArrayList<>();
        final List<Bridge> bridges = new ArrayList<>();
        final List<Snapshot> snapshots = new ArrayList<>();
        boolean unreadSnapshots;
        final List<Old> olds = new ArrayList<>();
        final List<Hook> hooks = new ArrayList<>();
        final List<Pkg> pkgs = new ArrayList<>();
        final List<String> initExtras = new ArrayList<>();
        /** What probeState prints: the variables and fields of the base program's objects, fixed at the base. */
        final List<String> stateProbe = new ArrayList<>();
        int capture;
        int names;
        /** The body of the method calc of the generic class Gen, and the type arguments probeNew makes it with. */
        String genericBody = "0";
        final List<String> genericTypes = new ArrayList<>();
        int arrayProbe;

        Prog() {
        }

        Prog(Prog p) {
            leaves.addAll(p.leaves);
            funcs.addAll(p.funcs);
            arrays.addAll(p.arrays);
            consts.addAll(p.consts);
            vars.addAll(p.vars);
            for (Cls c : p.classes) {
                classes.add(new Cls(c));
            }
            closures.addAll(p.closures);
            bridges.addAll(p.bridges);
            snapshots.addAll(p.snapshots);
            unreadSnapshots = p.unreadSnapshots;
            olds.addAll(p.olds);
            hooks.addAll(p.hooks);
            pkgs.addAll(p.pkgs);
            initExtras.addAll(p.initExtras);
            stateProbe.addAll(p.stateProbe);
            capture = p.capture;
            genericBody = p.genericBody;
            genericTypes.addAll(p.genericTypes);
            names = p.names;
            arrayProbe = p.arrayProbe;
        }

        String fresh(String prefix) {
            return prefix + (names++);
        }

        Cls cls(String name) {
            for (Cls c : classes) {
                if (c.name.equals(name)) {
                    return c;
                }
            }
            throw new IllegalStateException(name);
        }

        /** The class and its ancestors, the class first. */
        List<Cls> chain(Cls c) {
            List<Cls> result = new ArrayList<>();
            for (Cls k = c; k != null; k = k.parent == null ? null : cls(k.parent)) {
                result.add(k);
            }
            return result;
        }

        boolean implementsOp(Cls c) {
            return chain(c).stream().anyMatch(k -> k.op);
        }

        /** The methods an object of the class has, by name, in a stable order. */
        SortedSet<String> methodNames(Cls c) {
            SortedSet<String> names = new TreeSet<>();
            for (Cls k : chain(c)) {
                names.addAll(k.methods.keySet());
            }
            return names;
        }

        boolean inherits(Cls c, String method) {
            return chain(c).stream().skip(1).anyMatch(k -> k.methods.containsKey(method)) || method.equals("apply");
        }

        List<Field> allFields(Cls c) {
            List<Field> result = new ArrayList<>();
            for (Cls k : chain(c)) {
                result.addAll(k.fields);
            }
            return result;
        }

        List<Cls> subclasses(Cls c) {
            return classes.stream().filter(k -> c.name.equals(k.parent)).toList();
        }

        /** The compilation units; {@code reference}: with the base program's construction and initial values. */
        Map<String, String> units(boolean reference) {
            Map<String, String> units = new LinkedHashMap<>();
            StringBuilder lib = new StringBuilder("package FuzzLib\n");
            for (Fn f : leaves) {
                lib.append("public function ").append(f.name).append("(int x) returns int\n    return ").append(f.body).append("\n");
            }
            units.put("FuzzLib.wurst", lib.toString());
            for (Pkg p : pkgs) {
                units.put(p.name + ".wurst", "package " + p.name + "\nimport FuzzLib\npublic int " + p.name + "Runs = 0\n"
                    + "public function " + p.name + "Fn(int x) returns int\n    return " + p.body + "\n"
                    + "init\n    " + p.name + "Runs += 1\n");
            }
            units.put("Fuzz.wurst", render(reference));
            return units;
        }

        String render(boolean reference) {
            StringBuilder sb = new StringBuilder("package Fuzz\nimport FuzzLib\n");
            for (Pkg p : pkgs) {
                sb.append("import ").append(p.name).append("\n");
            }
            sb.append("native fuzzOut(string s)\nnative I2S(int i) returns string\nnative R2S(real r) returns string\n");
            sb.append("@annotation function onHotReload()\n");
            sb.append("interface Op\n    function apply(int x) returns int\n");
            sb.append("interface BoolAction<T>\n    function run(T flag) returns int\n")
                .append("function booleanToIndex(bool b) returns int\n    return b ? 1 : 0\n")
                .append("function booleanFromIndex(int i) returns bool\n    return i != 0\n");
            for (Bridge bridge : bridges) {
                sb.append("function ").append(bridge.name).append("(int seed) returns BoolAction<bool>\n")
                    .append("    BoolAction<bool> action = (bool b) -> seed + ").append(bridge.offset)
                    .append(" + (b ? 1 : 0)\n    return action\n");
            }
            sb.append("class SnapshotNode\n    int value\n    SnapshotNode child\n    construct(int v)\n        value = v\n")
                .append("SnapshotNode snapshotCached = null\n")
                .append("function compiletime(SnapshotNode n) returns SnapshotNode\n    return n\n")
                .append("function makeSnapshot(int value) returns SnapshotNode\n    if snapshotCached == null\n")
                .append("        snapshotCached = new SnapshotNode(5)\n    let root = new SnapshotNode(value)\n")
                .append("    root.child = snapshotCached\n    return root\n");
            for (Snapshot snapshot : snapshots) {
                sb.append("constant ").append(snapshot.name).append(" = compiletime(makeSnapshot(")
                    .append(snapshot.value).append("))\n");
            }
            if (unreadSnapshots) {
                sb.append("class UnreadSnapshotNode\n    int value\n    UnreadSnapshotNode child\n")
                    .append("UnreadSnapshotNode unreadCached = null\n")
                    .append("function compiletime(UnreadSnapshotNode n) returns UnreadSnapshotNode\n    return n\n")
                    .append("function makeUnreadSnapshot() returns UnreadSnapshotNode\n    if unreadCached == null\n")
                    .append("        unreadCached = new UnreadSnapshotNode\n    let root = new UnreadSnapshotNode\n")
                    .append("    root.child = unreadCached\n    return root\n")
                    .append("constant unreadFirst = compiletime(makeUnreadSnapshot())\n")
                    .append("constant unreadSecond = compiletime(makeUnreadSnapshot())\n");
            }
            for (Arr a : arrays) {
                sb.append("constant int array ").append(a.name).append(" = [")
                    .append(String.join(", ", a.entries.stream().map(String::valueOf).toList())).append("]\n");
            }
            for (Const c : consts) {
                if (c.string) {
                    sb.append("constant string ").append(c.name).append(" = \"k\" + I2S(").append(c.expr).append(")\n");
                } else if (c.let) {
                    sb.append("let ").append(c.name).append(" = ").append(c.expr).append("\n");
                } else {
                    sb.append("constant int ").append(c.name).append(" = ").append(c.expr).append("\n");
                }
            }
            for (Var v : vars) {
                sb.append(v.type).append(" ").append(v.name).append(" = ").append(v.init).append("\n");
            }
            sb.append("Op array ops\nint fuzzNever = 0\n");
            for (Old o : olds) {
                sb.append(o.cls).append(" ").append(o.global).append("\n");
            }
            sb.append("Gen<int> gen0\n");
            for (Fn f : funcs) {
                sb.append("function ").append(f.name).append("(int x) returns int\n    return ").append(f.body).append("\n");
            }
            for (Cls c : classes) {
                sb.append("class ").append(c.name);
                if (c.parent != null) {
                    sb.append(" extends ").append(c.parent);
                } else if (c.op) {
                    sb.append(" implements Op");
                }
                sb.append("\n");
                boolean old = reference && c.base;
                for (Field f : c.fields) {
                    sb.append("    ").append(f.type).append(" ").append(f.name);
                    String init = !reference ? f.init : f.base ? f.baseInit : null;
                    if (init != null) {
                        sb.append(" = ").append(init);
                    }
                    sb.append("\n");
                }
                sb.append("    construct(int p)\n");
                if (c.parent != null) {
                    sb.append("        super(p + ").append(c.superOffset).append(")\n");
                }
                for (String a : old ? c.baseCtor : c.ctor) {
                    sb.append("        ").append(a).append("\n");
                }
                for (Map.Entry<String, String> m : c.methods.entrySet()) {
                    sb.append("    ").append(inherits(c, m.getKey()) ? "override " : "").append("function ")
                        .append(m.getKey()).append("(int x) returns int\n        return ").append(m.getValue()).append("\n");
                }
            }
            sb.append("class Gen<T:>\n    T val\n    int k\n    function calc(int x) returns int\n        return ")
                .append(genericBody).append("\n");
            sb.append("function makeOps(int s, int b)\n");
            for (Clo c : closures) {
                if (c.bound) {
                    sb.append("    Op cl").append(c.slot).append(" = (int x) -> ").append(c.body).append("\n");
                    sb.append("    ops[b + ").append(c.slot).append("] = cl").append(c.slot).append("\n");
                } else {
                    sb.append("    ops[b + ").append(c.slot).append("] = (int x) -> ").append(c.body).append("\n");
                }
            }
            for (Hook h : hooks) {
                sb.append("@onHotReload function ").append(h.name).append("()\n    fuzzOut(\"").append(h.name)
                    .append(" \" + I2S(").append(h.body).append("))\n");
            }
            sb.append("function probeState()\n");
            for (String line : stateProbe) {
                sb.append("    ").append(line).append("\n");
            }
            sb.append("    fuzzOut(\"end\")\n");
            sb.append("function probeOld()\n");
            for (Old o : olds) {
                Cls c = cls(o.cls);
                for (String m : methodNames(c)) {
                    sb.append("    fuzzOut(\"").append(o.global).append(".").append(m).append(" \" + I2S(")
                        .append(o.global).append(".").append(m).append("(2)))\n");
                }
                for (Field f : allFields(c)) {
                    sb.append("    fuzzOut(\"").append(o.global).append(".").append(f.name).append(" \" + ")
                        .append(f.type.equals("int") ? "I2S(" : "R2S(").append(o.global).append(".").append(f.name).append("))\n");
                }
            }
            sb.append("    fuzzOut(\"gen0 \" + I2S(gen0.val) + \" \" + I2S(gen0.calc(2)))\n");
            for (Clo c : closures) {
                if (c.slot < capture && !c.late) {
                    sb.append("    fuzzOut(\"ops").append(c.slot).append(" \" + I2S(ops[").append(c.slot).append("].apply(3)))\n");
                }
            }
            sb.append("    fuzzOut(\"end\")\n");
            sb.append("function probeNew()\n");
            for (Fn f : leaves) {
                sb.append("    fuzzOut(\"").append(f.name).append(" \" + I2S(").append(f.name).append("(3)))\n");
            }
            for (Fn f : funcs) {
                sb.append("    fuzzOut(\"").append(f.name).append(" \" + I2S(").append(f.name).append("(3)))\n");
            }
            for (Bridge bridge : bridges) {
                sb.append("    let ").append(bridge.name).append("Value = ").append(bridge.name).append("(3)\n")
                    .append("    fuzzOut(\"").append(bridge.name).append(" \" + I2S(").append(bridge.name)
                    .append("Value.run(true)))\n    destroy ").append(bridge.name).append("Value\n");
            }
            for (Snapshot snapshot : snapshots) {
                sb.append("    fuzzOut(\"").append(snapshot.name).append(" \" + I2S(").append(snapshot.name)
                    .append(".value) + \" \" + I2S(").append(snapshot.name).append(".child.value) + \" \" + I2S(")
                    .append(snapshot.name).append(".child == ").append(snapshots.getFirst().name)
                    .append(".child ? 1 : 0))\n");
            }
            for (Const c : consts) {
                sb.append("    fuzzOut(\"").append(c.name).append(" \" + ").append(c.string ? c.name : "I2S(" + c.name + ")").append(")\n");
            }
            for (Arr a : arrays) {
                for (int i = 0; i < arrayProbe; i++) {
                    sb.append("    fuzzOut(\"").append(a.name).append("[").append(i).append("] \" + I2S(").append(a.name)
                        .append("[").append(i).append("]))\n");
                }
            }
            for (Var v : vars) {
                if (!v.base && !v.tuned && v.type.equals("int")) {
                    sb.append("    fuzzOut(\"").append(v.name).append(" \" + I2S(").append(v.name).append("))\n");
                }
            }
            for (Pkg p : pkgs) {
                sb.append("    fuzzOut(\"").append(p.name).append(" \" + I2S(").append(p.name).append("Runs) + \" \" + I2S(")
                    .append(p.name).append("Fn(3)))\n");
            }
            for (Cls c : classes) {
                sb.append("    let n").append(c.name).append(" = new ").append(c.name).append("(4)\n");
                for (String m : methodNames(c)) {
                    sb.append("    fuzzOut(\"new ").append(c.name).append(".").append(m).append(" \" + I2S(n").append(c.name)
                        .append(".").append(m).append("(2)))\n");
                }
                for (Field f : allFields(c)) {
                    sb.append("    fuzzOut(\"new ").append(c.name).append(".").append(f.name).append(" \" + ")
                        .append(f.type.equals("int") ? "I2S(" : "R2S(").append("n").append(c.name).append(".").append(f.name).append("))\n");
                }
                if (implementsOp(c)) {
                    sb.append("    Op op").append(c.name).append(" = n").append(c.name).append("\n");
                    sb.append("    fuzzOut(\"new ").append(c.name).append(" as Op \" + I2S(op").append(c.name).append(".apply(5)))\n");
                }
            }
            for (String t : genericTypes) {
                String v = "gen" + t;
                sb.append("    let ").append(v).append(" = new Gen<").append(t).append(">\n");
                sb.append("    ").append(v).append(".k = 2\n");
                sb.append("    ").append(v).append(".val = ").append(t.equals("int") ? "4" : t.equals("real") ? "4.5" : "\"four\"").append("\n");
                sb.append("    fuzzOut(\"new Gen<").append(t).append("> \" + ")
                    .append(t.equals("int") ? "I2S(" + v + ".val)" : t.equals("real") ? "R2S(" + v + ".val)" : v + ".val")
                    .append(" + \" \" + I2S(").append(v).append(".calc(2)))\n");
            }
            sb.append("    makeOps(7, 100)\n");
            for (Clo c : closures) {
                sb.append("    fuzzOut(\"new ops").append(c.slot).append(" \" + I2S(ops[100 + ").append(c.slot)
                    .append("].apply(3)))\n");
            }
            sb.append("    fuzzOut(\"end\")\n");
            sb.append("init\n");
            if (unreadSnapshots) {
                sb.append("    unreadSecond.child.value = 10\n");
            }
            sb.append("    makeOps(").append(capture == 0 ? 1 : 5).append(", 0)\n");
            sb.append("    ").append(snapshots.getFirst().name).append(".child.value = 10\n");
            sb.append("    gen0 = new Gen<int>\n    gen0.val = 6\n    gen0.k = 3\n");
            for (Old o : olds) {
                sb.append("    ").append(o.global).append(" = new ").append(o.cls).append("(").append(o.arg).append(")\n");
                if (o.field != null) {
                    sb.append("    ").append(o.global).append(".").append(o.field).append(" = ").append(o.value).append("\n");
                }
            }
            if (!reference) {
                for (String s : initExtras) {
                    sb.append("    ").append(s).append("\n");
                }
            }
            // The test calls the probes; a call nothing makes keeps them, and what they call, in the build.
            sb.append("    if fuzzNever == 1\n        probeState()\n        probeOld()\n        probeNew()\n");
            return sb.toString();
        }
    }

    /** An edited program, the operations which made it and what hot reload must do with it. */
    record Edit(Prog prog, List<String> ops, boolean mustRefuse, boolean mayRefuse, boolean mustWarn) {
    }

    // ---------------------------------------------------------------------------------------------------------------
    // The generator

    /** {@code WURST_FUZZ_INSERT_ANONYMOUS}: also insert anonymous closures before others of their order group. */
    static final boolean INSERT_ANONYMOUS = System.getenv("WURST_FUZZ_INSERT_ANONYMOUS") != null;

    static final class Generator {
        private final Random r;

        Generator(Random r) {
            this.r = r;
        }

        private <T> T pick(List<T> list) {
            return list.get(r.nextInt(list.size()));
        }

        /** Atoms and callable functions an expression may use. */
        record Scope(List<String> atoms, List<String> calls) {
        }

        String expr(Scope s, int depth) {
            int k = r.nextInt(10);
            if (depth <= 0 || k < 3) {
                if (!s.atoms.isEmpty() && r.nextInt(10) < 7) {
                    return pick(s.atoms);
                }
                return Integer.toString(r.nextInt(10));
            }
            if (k < 5 && !s.calls.isEmpty()) {
                return pick(s.calls) + "(" + expr(s, depth - 1) + ")";
            }
            switch (r.nextInt(3)) {
                case 0:
                    return "(" + expr(s, depth - 1) + " + " + expr(s, depth - 1) + ")";
                case 1:
                    return "(" + expr(s, depth - 1) + " - " + expr(s, depth - 1) + ")";
                default:
                    return "(" + expr(s, depth - 1) + " * " + (1 + r.nextInt(3)) + ")";
            }
        }

        /** An expression which differs from {@code old}. */
        String other(Scope s, String old) {
            for (int i = 0; i < 6; i++) {
                String e = expr(s, 3);
                if (!e.equals(old)) {
                    return e;
                }
            }
            return "(" + old + " + 1)";
        }

        List<String> intConsts(Prog p, int upTo) {
            List<String> result = new ArrayList<>();
            for (int i = 0; i < upTo && i < p.consts.size(); i++) {
                if (!p.consts.get(i).string) {
                    result.add(p.consts.get(i).name);
                }
            }
            for (Arr a : p.arrays) {
                result.add(a.name + "[" + r.nextInt(3) + "]");
            }
            return result;
        }

        Scope leafScope(Prog p, double rank) {
            return new Scope(List.of("x"), p.leaves.stream().filter(f -> f.rank < rank).map(Fn::name).toList());
        }

        Scope funcScope(Prog p, double rank) {
            List<String> atoms = new ArrayList<>(List.of("x", "x"));
            atoms.addAll(intConsts(p, p.consts.size()));
            List<String> calls = new ArrayList<>(p.leaves.stream().map(Fn::name).toList());
            calls.addAll(p.funcs.stream().filter(f -> f.rank < rank).map(Fn::name).toList());
            return new Scope(atoms, calls);
        }

        Scope methodScope(Prog p, Cls c, String method) {
            List<String> atoms = new ArrayList<>(List.of("x", "x"));
            for (Field f : p.allFields(c)) {
                // A spare field is there to have its type changed, which an edit must refuse.
                if (!f.name.startsWith("spare")) {
                    atoms.add(f.name);
                }
            }
            atoms.addAll(intConsts(p, p.consts.size()));
            if (method != null && p.chain(c).stream().skip(1).anyMatch(k -> k.methods.containsKey(method))) {
                atoms.add("super." + method + "(x)");
            }
            List<String> calls = new ArrayList<>(p.leaves.stream().map(Fn::name).toList());
            calls.addAll(p.funcs.stream().map(Fn::name).toList());
            return new Scope(atoms, calls);
        }

        Scope genericScope(Prog p) {
            List<String> atoms = new ArrayList<>(List.of("x", "k"));
            atoms.addAll(intConsts(p, p.consts.size()));
            List<String> calls = new ArrayList<>(p.leaves.stream().map(Fn::name).toList());
            calls.addAll(p.funcs.stream().map(Fn::name).toList());
            return new Scope(atoms, calls);
        }

        Scope closureScope(Prog p) {
            List<String> atoms = new ArrayList<>(List.of("x", "s"));
            atoms.addAll(intConsts(p, p.consts.size()));
            List<String> calls = new ArrayList<>(p.leaves.stream().map(Fn::name).toList());
            calls.addAll(p.funcs.stream().map(Fn::name).toList());
            return new Scope(atoms, calls);
        }

        Scope constScope(Prog p, int index) {
            return new Scope(intConsts(p, index), p.leaves.stream().map(Fn::name).toList());
        }

        Scope hookScope(Prog p) {
            List<String> atoms = new ArrayList<>(intConsts(p, p.consts.size()));
            List<String> calls = new ArrayList<>(p.leaves.stream().map(Fn::name).toList());
            calls.addAll(p.funcs.stream().map(Fn::name).toList());
            return new Scope(atoms, calls);
        }

        Prog base() {
            Prog p = new Prog();
            for (int i = 0, n = 2 + r.nextInt(2); i < n; i++) {
                double rank = r.nextDouble();
                p.leaves.add(new Fn(p.fresh("h"), rank, expr(leafScope(p, rank), 2)));
            }
            for (int i = 0, n = 1 + r.nextInt(2); i < n; i++) {
                p.arrays.add(new Arr(p.fresh("KA"), entries()));
            }
            p.arrayProbe = 6;
            for (int i = 0, n = 2 + r.nextInt(3); i < n; i++) {
                addConst(p);
            }
            for (int i = 0, n = 1 + r.nextInt(2); i < n; i++) {
                p.vars.add(new Var(p.fresh("v"), "int", Integer.toString(r.nextInt(20)), true, false));
            }
            p.vars.add(new Var(p.fresh("spare"), "int", "1", true, false));
            for (int i = 0, n = 2 + r.nextInt(3); i < n; i++) {
                double rank = r.nextDouble();
                p.funcs.add(new Fn(p.fresh("f"), rank, expr(funcScope(p, rank), 3)));
            }
            for (int i = 0, n = 2 + r.nextInt(3); i < n; i++) {
                addClass(p, true);
            }
            p.genericTypes.add("int");
            p.genericBody = expr(genericScope(p), 3);
            p.stateProbe.add("fuzzOut(\"gen0 \" + I2S(gen0.val) + \" \" + I2S(gen0.k))");
            p.capture = 2 + r.nextInt(3);
            for (int i = 0; i < p.capture; i++) {
                p.closures.add(new Clo(i, r.nextBoolean(), expr(closureScope(p), 3)));
            }
            for (Cls c : p.classes) {
                Old o = new Old(p.fresh("o"), c.name, 1 + r.nextInt(9), null, 0);
                List<Field> fields = p.allFields(c);
                if (!fields.isEmpty() && r.nextBoolean()) {
                    Field f = pick(fields);
                    if (!f.name.startsWith("spare")) {
                        o = new Old(o.global, o.cls, o.arg, f.name, 10 + r.nextInt(20));
                    }
                }
                p.olds.add(o);
            }
            if (r.nextBoolean()) {
                p.hooks.add(new Hook(p.fresh("hook"), expr(hookScope(p), 2)));
            }
            for (Var v : p.vars) {
                if (v.name.startsWith("spare")) {
                    continue;
                }
                p.stateProbe.add("fuzzOut(\"" + v.name + " \" + I2S(" + v.name + "))");
            }
            for (Old o : p.olds) {
                for (Field f : p.allFields(p.cls(o.cls))) {
                    if (f.name.startsWith("spare")) {
                        continue;
                    }
                    p.stateProbe.add("fuzzOut(\"" + o.global + "." + f.name + " \" + " + (f.type.equals("int") ? "I2S(" : "R2S(")
                        + o.global + "." + f.name + "))");
                }
            }
            p.bridges.add(new Bridge(p.fresh("bridge"), r.nextInt(10)));
            p.snapshots.add(new Snapshot(p.fresh("snapshot"), r.nextInt(10)));
            p.unreadSnapshots = r.nextBoolean();
            p.stateProbe.add("fuzzOut(\"snapshot shared \" + I2S(" + p.snapshots.getFirst().name + ".child.value))");
            return p;
        }

        List<Integer> entries() {
            List<Integer> entries = new ArrayList<>();
            for (int i = 0, n = 1 + r.nextInt(4); i < n; i++) {
                entries.add(r.nextInt(30));
            }
            return entries;
        }

        void addConst(Prog p) {
            int kind = r.nextInt(6);
            String name = p.fresh("K");
            String e = expr(constScope(p, p.consts.size()), 2);
            p.consts.add(new Const(name, kind == 0, kind == 1, e));
        }

        void addClass(Prog p, boolean base) {
            List<Cls> parents = p.classes;
            Cls parent = !parents.isEmpty() && r.nextInt(3) > 0 ? pick(parents) : null;
            Cls c = new Cls(p.fresh("C"), parent == null ? null : parent.name, parent == null && r.nextBoolean(), base,
                r.nextInt(5));
            for (int i = 0, n = r.nextInt(3); i < n; i++) {
                addField(p, c, base);
            }
            if (base && r.nextInt(3) == 0) {
                c.fields.add(new Field(p.fresh("spareField"), "int", "2", true));
            }
            if (c.op) {
                c.methods.put("apply", "0");
            }
            for (int i = 0, n = r.nextInt(3); i < n; i++) {
                c.methods.put(p.fresh("m"), "0");
            }
            if (parent != null) {
                for (String m : p.methodNames(parent)) {
                    if (r.nextBoolean()) {
                        c.methods.put(m, "0");
                    }
                }
            }
            p.classes.add(c);
            for (String m : new ArrayList<>(c.methods.keySet())) {
                c.methods.put(m, expr(methodScope(p, c, m), 3));
            }
            c.baseCtor.addAll(c.ctor);
        }

        void addField(Prog p, Cls c, boolean base) {
            Field f = new Field(p.fresh("fl"), "int", r.nextBoolean() ? Integer.toString(r.nextInt(10)) : null, base);
            c.fields.add(f);
            if (r.nextBoolean()) {
                c.ctor.add(f.name + " = " + ctorExpr());
            }
        }

        String ctorExpr() {
            return "p * " + (1 + r.nextInt(3)) + " + " + r.nextInt(10);
        }

        /** An edit of {@code before}: one to four operations. */
        Edit edit(Prog before) {
            Prog p = new Prog(before);
            List<String> ops = new ArrayList<>();
            boolean mustRefuse = false;
            boolean mayRefuse = false;
            boolean mustWarn = false;
            int n = 1 + r.nextInt(4);
            for (int i = 0; i < n; i++) {
                int k = r.nextInt(100);
                if (k < 12) {
                    int idx = r.nextInt(p.funcs.size());
                    Fn f = p.funcs.get(idx);
                    p.funcs.set(idx, new Fn(f.name, f.rank, other(funcScope(p, f.rank), f.body)));
                    ops.add("function body " + f.name);
                } else if (k < 18) {
                    int idx = r.nextInt(p.leaves.size());
                    Fn f = p.leaves.get(idx);
                    p.leaves.set(idx, new Fn(f.name, f.rank, other(leafScope(p, f.rank), f.body)));
                    ops.add("library function body " + f.name);
                } else if (k < 30) {
                    List<Cls> withMethods = p.classes.stream().filter(c -> !c.methods.isEmpty()).toList();
                    if (withMethods.isEmpty()) {
                        continue;
                    }
                    Cls c = pick(withMethods);
                    String m = pick(new ArrayList<>(c.methods.keySet()));
                    c.methods.put(m, other(methodScope(p, c, m), c.methods.get(m)));
                    ops.add("method body " + c.name + "." + m);
                } else if (k < 38) {
                    int idx = r.nextInt(p.closures.size());
                    Clo c = p.closures.get(idx);
                    String body = other(closureScope(p), c.body);
                    // A closure made before the reload has no value of a variable it did not capture then.
                    boolean late = c.late || c.slot < p.capture && !c.baseCaptures && Clo.captures(body);
                    p.closures.set(idx, new Clo(c.slot, c.bound, body, c.baseCaptures, late));
                    if (late && !c.late) {
                        // As a field added to a class: objects made before read its default (docs/LUA_HOT_RELOAD.md).
                        ops.add("closure starts capturing " + c.slot);
                    } else {
                        ops.add("closure body " + c.slot);
                    }
                } else if (k < 44) {
                    int idx = r.nextInt(p.consts.size());
                    Const c = p.consts.get(idx);
                    p.consts.set(idx, new Const(c.name, c.string, c.let, other(constScope(p, idx), c.expr)));
                    ops.add("constant " + c.name);
                } else if (k < 48) {
                    int idx = r.nextInt(p.arrays.size());
                    Arr a = p.arrays.get(idx);
                    p.arrays.set(idx, new Arr(a.name, entries()));
                    ops.add("array literal " + a.name);
                } else if (k < 52) {
                    double rank = r.nextDouble();
                    Fn f = new Fn(p.fresh("f"), rank, expr(funcScope(p, rank), 3));
                    p.funcs.add(r.nextInt(p.funcs.size() + 1), f);
                    ops.add("added function " + f.name);
                } else if (k < 54) {
                    double rank = r.nextDouble();
                    Fn f = new Fn(p.fresh("h"), rank, expr(leafScope(p, rank), 2));
                    p.leaves.add(r.nextInt(p.leaves.size() + 1), f);
                    ops.add("added library function " + f.name);
                } else if (k < 57) {
                    addConst(p);
                    ops.add("added constant " + p.consts.get(p.consts.size() - 1).name);
                } else if (k < 61) {
                    addClass(p, false);
                    ops.add("added class " + p.classes.get(p.classes.size() - 1).name);
                } else if (k < 65) {
                    Cls c = pick(p.classes);
                    String m = p.fresh("m");
                    c.methods.put(m, "0");
                    c.methods.put(m, expr(methodScope(p, c, m), 3));
                    ops.add("added method " + c.name + "." + m);
                } else if (k < 69) {
                    List<String[]> candidates = new ArrayList<>();
                    for (Cls c : p.classes) {
                        if (c.parent != null) {
                            for (String m : p.methodNames(p.cls(c.parent))) {
                                if (!c.methods.containsKey(m)) {
                                    candidates.add(new String[] {c.name, m});
                                }
                            }
                        }
                    }
                    if (candidates.isEmpty()) {
                        continue;
                    }
                    String[] o = pick(candidates);
                    Cls c = p.cls(o[0]);
                    c.methods.put(o[1], "0");
                    c.methods.put(o[1], expr(methodScope(p, c, o[1]), 3));
                    ops.add("added override " + c.name + "." + o[1]);
                } else if (k < 72) {
                    List<String[]> candidates = new ArrayList<>();
                    for (Cls c : p.classes) {
                        for (String m : c.methods.keySet()) {
                            if (c.parent != null && p.methodNames(p.cls(c.parent)).contains(m)) {
                                candidates.add(new String[] {c.name, m});
                            }
                        }
                    }
                    if (candidates.isEmpty()) {
                        continue;
                    }
                    String[] o = pick(candidates);
                    p.cls(o[0]).methods.remove(o[1]);
                    ops.add("removed override " + o[0] + "." + o[1]);
                } else if (k < 76) {
                    Cls c = pick(p.classes);
                    addField(p, c, false);
                    ops.add("added field " + c.name + "." + c.fields.get(c.fields.size() - 1).name);
                } else if (k < 79) {
                    List<Cls> withCtor = p.classes.stream().filter(c -> !c.ctor.isEmpty()).toList();
                    if (withCtor.isEmpty()) {
                        continue;
                    }
                    Cls c = pick(withCtor);
                    int idx = r.nextInt(c.ctor.size());
                    String a = c.ctor.get(idx);
                    c.ctor.set(idx, a.substring(0, a.indexOf(" = ")) + " = " + ctorExpr() + " + 10");
                    ops.add("constructor " + c.name);
                } else if (k < 81) {
                    Var v = new Var(p.fresh("v"), "int", Integer.toString(r.nextInt(20)), false, false);
                    p.vars.add(v);
                    ops.add("added variable " + v.name);
                } else if (k < 83) {
                    List<Var> ints = p.vars.stream().filter(v -> v.type.equals("int") && !v.name.startsWith("spare")).toList();
                    Var v = pick(ints);
                    String initial;
                    do {
                        initial = Integer.toString(20 + r.nextInt(20));
                    } while (initial.equals(v.init));
                    p.vars.set(p.vars.indexOf(v), new Var(v.name, v.type, initial, v.base, true));
                    ops.add("initial value of variable " + v.name);
                    // The map keeps the value of a variable it has; one this edit adds starts with the new value.
                    mustWarn |= before.vars.stream().anyMatch(b -> b.name.equals(v.name));
                } else if (k < 86) {
                    if (p.hooks.isEmpty() || r.nextBoolean()) {
                        Hook h = new Hook(p.fresh("hook"), expr(hookScope(p), 2));
                        p.hooks.add(h);
                        ops.add("added hook " + h.name);
                    } else {
                        int idx = r.nextInt(p.hooks.size());
                        Hook h = p.hooks.get(idx);
                        p.hooks.set(idx, new Hook(h.name, other(hookScope(p), h.body)));
                        ops.add("hook body " + h.name);
                    }
                } else if (k < 89) {
                    int slot = p.closures.stream().mapToInt(Clo::slot).max().orElse(0) + 1;
                    int at = r.nextInt(p.closures.size() + 1);
                    boolean bound = r.nextBoolean();
                    if (!bound && !INSERT_ANONYMOUS) {
                        // An anonymous closure inserted before others told apart by their order, whose bodies the edit
                        // also changes, is not refused, and objects made before then run other closures' bodies
                        // (BACKLOG.md, "closures inserted among others told apart by their order").
                        at = p.closures.size();
                    }
                    p.closures.add(at, new Clo(slot, bound, expr(closureScope(p), 3)));
                    ops.add((at == p.closures.size() - 1 ? "appended" : "inserted") + (bound ? " bound" : "")
                        + " closure " + slot);
                    // Mixed body edits and additions within one anonymous order group may be ambiguous, even when
                    // the generator intended an append: the compiler cannot distinguish it from an insertion.
                    if (!bound) {
                        mayRefuse = true;
                    }
                } else if (k < 90) {
                    List<String> missing = new ArrayList<>(List.of("string", "real"));
                    missing.removeAll(p.genericTypes);
                    if (!missing.isEmpty() && r.nextBoolean()) {
                        String t = pick(missing);
                        p.genericTypes.add(t);
                        ops.add("new type argument " + t);
                    } else {
                        p.genericBody = other(genericScope(p), p.genericBody);
                        ops.add("generic method body");
                    }
                } else if (k < 91) {
                    List<Fn> unused = p.funcs.stream().filter(f -> !referenced(p, f.name)).toList();
                    if (unused.isEmpty() || p.funcs.size() < 2) {
                        continue;
                    }
                    Fn f = pick(unused);
                    p.funcs.remove(f);
                    ops.add("removed function " + f.name);
                } else if (k < 93) {
                    Pkg pkg = new Pkg(p.fresh("FuzzAdded"), expr(new Scope(List.of("x"), p.leaves.stream().map(Fn::name).toList()), 2));
                    p.pkgs.add(pkg);
                    ops.add("added package " + pkg.name);
                } else if (k < 95) {
                    p.initExtras.add(pick(p.funcs).name + "(" + r.nextInt(5) + ")");
                    ops.add("init block");
                    mustWarn = true;
                } else if (k < 97) {
                    List<Var> spare = p.vars.stream().filter(v -> v.name.startsWith("spare")).toList();
                    Var v = spare.get(0);
                    // A refused edit is undone, so the spare storage is always an int before an edit.
                    p.vars.set(p.vars.indexOf(v), new Var(v.name, "real", "1.", v.base, v.tuned));
                    ops.add("type of variable " + v.name);
                    mustRefuse = true;
                } else {
                    List<Cls> spare = p.classes.stream().filter(c -> c.fields.stream().anyMatch(f -> f.name.startsWith("spare"))).toList();
                    if (spare.isEmpty()) {
                        continue;
                    }
                    Cls c = pick(spare);
                    Field f = c.fields.stream().filter(x -> x.name.startsWith("spare")).findFirst().orElseThrow();
                    f.type = "real";
                    f.init = "2.";
                    ops.add("type of field " + c.name + "." + f.name);
                    mustRefuse = true;
                }
            }
            if (ops.isEmpty()) {
                int idx = r.nextInt(p.funcs.size());
                Fn f = p.funcs.get(idx);
                p.funcs.set(idx, new Fn(f.name, f.rank, other(funcScope(p, f.rank), f.body)));
                ops.add("function body " + f.name);
            }
            p.arrayProbe = Math.max(p.arrayProbe, p.arrays.stream().mapToInt(a -> a.entries.size()).max().orElse(0) + 1);
            // Only the final edit matters: several operations may add and then remove a capture again. The late bit
            // remembers that objects of this lineage predate its capture, including across earlier edits.
            mustWarn |= p.closures.stream().anyMatch(c -> c.late && Clo.captures(c.body)
                && before.closures.stream().anyMatch(old -> old.slot == c.slot && !old.late));
            if (r.nextInt(4) == 0) {
                p.bridges.add(0, new Bridge(p.fresh("bridge"), r.nextInt(10)));
                ops.add("added old-generic bridge " + p.bridges.getFirst().name);
            } else if (r.nextInt(4) == 0) {
                int index = r.nextInt(p.bridges.size());
                Bridge old = p.bridges.get(index);
                p.bridges.set(index, new Bridge(old.name, old.offset + 1 + r.nextInt(10)));
                ops.add("old-generic bridge body " + old.name);
            }
            if (r.nextInt(8) == 0) {
                boolean first = r.nextBoolean();
                Snapshot snapshot = new Snapshot(p.fresh("snapshot"), r.nextInt(10));
                p.snapshots.add(first ? 0 : p.snapshots.size(), snapshot);
                ops.add((first ? "prepended" : "appended") + " shared snapshot " + snapshot.name);
                mustRefuse |= first;
            }
            return new Edit(p, ops, mustRefuse, mayRefuse, mustWarn && !mustRefuse);
        }

        boolean referenced(Prog p, String name) {
            Pattern call = Pattern.compile("\\b" + Pattern.quote(name) + "\\(");
            for (Fn f : p.funcs) {
                if (call.matcher(f.body).find()) {
                    return true;
                }
            }
            for (Cls c : p.classes) {
                for (String body : c.methods.values()) {
                    if (call.matcher(body).find()) {
                        return true;
                    }
                }
            }
            for (Clo c : p.closures) {
                if (call.matcher(c.body).find()) {
                    return true;
                }
            }
            for (Hook h : p.hooks) {
                if (call.matcher(h.body).find()) {
                    return true;
                }
            }
            if (call.matcher(p.genericBody).find()) {
                return true;
            }
            for (String s : p.initExtras) {
                if (call.matcher(s).find()) {
                    return true;
                }
            }
            return false;
        }
    }
}
