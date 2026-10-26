import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.peeeq.wurstio.TimeTaker;
import de.peeeq.wurstio.languageserver.*;
import de.peeeq.wurstio.languageserver.requests.*;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.gui.*;
import de.peeeq.wurstscript.translation.BackendWork;
import de.peeeq.wurstscript.translation.lua.translation.LuaHotReload;
import de.peeeq.wurstscript.utils.CompilationCancellation;
import org.wurstscript.projectconfig.WurstProjectConfigReader;

import java.io.File;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Offline committed-edit replay. All writes stay in the exported fixture; no game installation is discovered. */
public final class MapCommitHotReloadProfile {
    record Pair(String commit, String parent, String title, List<String> paths, boolean production,
                List<String> runtimePaths, List<String> testPaths, List<String> otherPaths) { }
    record Corpus(List<Pair> pairs) { }
    record Build(String script, LuaHotReload.Manifest manifest, BackendWork.Snapshot work, double millis, String phases) { }
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();

    private static final class Probe extends RunMap {
        Probe(Path root, List<String> args) {
            super(null, WFile.create(root), Optional.empty(), Optional.empty(), args,
                Optional.of(root.resolve("NO_GAME.exe").toString()));
        }
        Build compile(ModelManagerImpl manager, Path root) throws Exception {
            Path input = root.resolve("_build/commit-input.w3x");
            Files.copy(root.resolve("base.w3x"), input, StandardCopyOption.REPLACE_EXISTING);
            WurstGuiCliImpl gui = new WurstGuiCliImpl();
            long start = System.nanoTime();
            try (BackendWork work = BackendWork.measure()) {
                File[] output = new File[1];
                try {
                    CompilationCancellation.run(() -> false, () -> output[0] = compileScript(gui, manager, compileArgs, Optional.of(input.toFile()),
                        WurstProjectConfigReader.load(root.resolve("wurst.build")), false, root.resolve("wurst/war3map.j").toFile()));
                } catch (RuntimeException failure) {
                    if (gui.getErrorCount() != 0) throw new IllegalStateException(gui.getErrors().toString(), failure);
                    throw failure;
                }
                if (gui.getErrorCount() != 0) throw new IllegalStateException(gui.getErrors().toString());
                double millis = elapsed(start);
                return new Build(Files.readString(Objects.requireNonNull(output[0]).toPath()),
                    Objects.requireNonNull(luaHotReloadManifest), work.snapshot(), millis,
                    timeTaker instanceof TimeTaker.Recording recording ? recording.report() : "disabled");
            }
        }
    }

    private final Path fixture, root;
    private ModelManagerImpl manager;
    private Map<String, String> files = Map.of();
    private String config = "";
    private String arguments = "";

    private MapCommitHotReloadProfile(Path fixture) throws Exception {
        this.fixture = fixture; this.root = fixture.resolve("project");
        // A replay may be rerun in its own fixture. Remove stale source files through install, never leave
        // a later commit's added units in an earlier snapshot's model.
        if (Files.isDirectory(root.resolve("wurst"))) files = sources(root);
    }

    private static Map<String, String> sources(Path state) throws Exception {
        Map<String, String> result = new TreeMap<>();
        try (var paths = Files.walk(state.resolve("wurst"))) {
            for (Path path : paths.filter(Files::isRegularFile).sorted().toList()) {
                if (path.toString().endsWith(".wurst") || path.toString().endsWith(".jurst") || path.toString().endsWith(".j")) {
                    result.put(state.relativize(path).toString(), Files.readString(path));
                }
            }
        }
        return result;
    }

    private double install(String revision) throws Exception {
        long start = System.nanoTime();
        Path state = fixture.resolve("states").resolve(revision);
        Map<String, String> next = sources(state);
        String nextConfig = Files.readString(state.resolve("wurst.build"));
        String nextArguments = Files.readString(state.resolve("wurst_run.args"));
        boolean reload = manager == null || !config.equals(nextConfig) || !arguments.equals(nextArguments);
        Files.writeString(root.resolve("wurst.build"), nextConfig);
        Files.writeString(root.resolve("wurst_run.args"), nextArguments);
        ModelManager.Changes changes = ModelManager.Changes.empty();
        for (String name : files.keySet()) {
            if (next.containsKey(name)) continue;
            Path target = root.resolve(name).normalize();
            if (!target.startsWith(root.resolve("wurst"))) throw new IllegalArgumentException("Outside source tree: " + name);
            Files.deleteIfExists(target);
            if (!reload) changes = changes.mergeWith(manager.removeCompilationUnit(WFile.create(target)));
        }
        for (var entry : next.entrySet()) {
            if (Objects.equals(files.get(entry.getKey()), entry.getValue())) continue;
            Path target = root.resolve(entry.getKey()).normalize();
            if (!target.startsWith(root.resolve("wurst"))) throw new IllegalArgumentException("Outside source tree: " + entry.getKey());
            Files.createDirectories(target.getParent());
            Files.writeString(target, entry.getValue());
            if (!reload) changes = changes.mergeWith(manager.syncCompilationUnitContent(WFile.create(target), entry.getValue()));
        }
        files = next; config = nextConfig; arguments = nextArguments;
        if (reload) {
            if (manager != null) manager.clean();
            manager = new ModelManagerImpl(root.toFile(), new BufferManager());
            manager.buildProject();
        } else {
            manager.reconcile(changes);
        }
        return elapsed(start);
    }

    private List<String> args() {
        List<String> result = new ArrayList<>(WurstCommands.getCompileArgs(WFile.create(root), false));
        // Castle Fight's Jenkinsfile applies exactly this public-compiler adjustment.
        // Never silently filter arbitrary unknown compiler flags from a replay.
        if (Boolean.getBoolean("profile.publicCompiler")) result.remove("-luaEncapsulation");
        result.add("-luaHotReload"); result.add("-hotreload");
        if (Boolean.getBoolean("profile.measure")) result.add("-measure");
        RunArgs run = new RunArgs(result);
        if (!run.isLua() || run.isInline() || run.isOptimize() || run.isLocalOptimizations()) {
            throw new IllegalArgumentException("Corpus requires Lua development flags: " + result);
        }
        return result;
    }

    private void run() throws Exception {
        Corpus corpus = JSON.fromJson(Files.readString(fixture.resolve("corpus.json")), Corpus.class);
        int limit = Integer.parseInt(System.getProperty("profile.commitLimit", Integer.toString(corpus.pairs.size())));
        if (limit <= 0) throw new IllegalArgumentException("profile.commitLimit must be positive");
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Pair pair : corpus.pairs) {
            if (rows.size() >= limit) break;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("commit", pair.commit); row.put("parent", pair.parent); row.put("title", pair.title);
            row.put("production", pair.production); row.put("paths", pair.paths);
            row.put("runtimePaths", pair.runtimePaths); row.put("testPaths", pair.testPaths); row.put("otherPaths", pair.otherPaths);
            row.put("publicCompilerAdjustment", Boolean.getBoolean("profile.publicCompiler"));
            int clockRetries = 0;
            while (true) {
                LocalDateTime clockWindow = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
                try {
                    // Build the expected child before creating the parent's warmed model. The reference cannot
                    // inherit any retained translator, body artifact or AST lookup from the measured edit.
                    if (manager != null) manager.clean();
                    manager = null;
                    install(pair.commit);
                    if (manager.hasErrors()) throw new IllegalStateException(manager.getFirstErrorDescription());
                    Build expected = new Probe(root, args()).compile(manager, root);
                    manager.clean();
                    manager = null;
                    install(pair.parent);
                    if (manager.hasErrors()) throw new IllegalStateException(manager.getFirstErrorDescription());
                    List<String> beforeArgs = args();
                    Build before = new Probe(root, beforeArgs).compile(manager, root);
                    Path session = root.resolve("_build/commit-session"), delivery = root.resolve("_build/commit-delivery");
                    LuaHotReloadSession.start(session, before.manifest);
                    long total = System.nanoTime();
                    row.put("reconcileMillis", install(pair.commit));
                    List<String> afterArgs = args(); row.put("args", afterArgs);
                    Build after = new Probe(root, afterArgs).compile(manager, root);
                    if (!expected.script.equals(after.script) || !expected.manifest.equals(after.manifest)) {
                        // getBuildDate() intentionally observes wall time. Retry the complete independent oracle and
                        // warmed edit if they straddled a minute; never normalize a generated value or accept a mismatch.
                        if (!clockWindow.equals(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)) && clockRetries++ < 2) continue;
                        Files.writeString(fixture.resolve("expected.lua"), expected.script);
                        Files.writeString(fixture.resolve("actual.lua"), after.script);
                        Files.writeString(fixture.resolve("expected-manifest.json"), JSON.toJson(expected.manifest));
                        Files.writeString(fixture.resolve("actual-manifest.json"), JSON.toJson(after.manifest));
                        throw new IllegalStateException("Fresh/retained output differs for " + pair.commit);
                    }
                    row.put("freshOracle", "equal Lua and manifest");
                    row.put("clockRetries", clockRetries);
                    row.put("compileMillis", after.millis); row.put("backendWork", after.work);
                    row.put("phases", after.phases);
                    row.put("frontend", manager.incrementalImTranslation().statistics());
                    row.put("backendReason", manager.incrementalImTranslation().scalarBackendReason());
                    long sending = System.nanoTime();
                    try {
                        var sent = LuaHotReloadSession.send(session, delivery, after.manifest);
                        row.put("status", "accepted"); row.put("payloadBytes", Files.size(sent.file()));
                        row.put("functions", sent.payload().functions().size()); row.put("warnings", sent.payload().warnings());
                        // Acceptance only establishes that the payload can be delivered. Warnings can describe
                        // initialization/state changes that require restart; do not count those as full commit coverage.
                        row.put("coverage", sent.payload().warnings().isEmpty() ? "accepted without warnings" : "accepted with state limitations");
                    } catch (LuaHotReload.Rejected rejected) {
                        row.put("status", "restart"); row.put("reasons", rejected.getReasons());
                    }
                    row.put("sendMillis", elapsed(sending)); row.put("totalMillis", elapsed(total));
                } catch (Exception failure) {
                    row.put("status", "build-failed"); row.put("error", failure.toString());
                    failure.printStackTrace();
                    if (manager != null) manager.clean();
                    manager = null;
                }
                break;
            }
            rows.add(row);
            Files.writeString(fixture.resolve("results.json"), JSON.toJson(rows));
            System.out.println("COMMIT_REPLAY " + new Gson().toJson(row));
            if (row.get("status").equals("build-failed") && !Boolean.getBoolean("profile.continueOnError")) {
                throw new IllegalStateException("Commit replay failed; see " + fixture.resolve("results.json"));
            }
        }
    }

    private static double elapsed(long start) { return (System.nanoTime() - start) / 1_000_000.0; }

    public static void main(String[] args) throws Exception {
        new MapCommitHotReloadProfile(Path.of(args[0]).toAbsolutePath().normalize()).run();
    }
}
