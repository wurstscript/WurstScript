import com.google.gson.*;
import de.peeeq.wurstio.languageserver.*;
import de.peeeq.wurstio.languageserver.requests.*;
import de.peeeq.wurstscript.gui.*;
import de.peeeq.wurstscript.translation.BackendWork;
import de.peeeq.wurstscript.translation.lua.translation.LuaHotReload;
import org.wurstscript.projectconfig.WurstProjectConfigReader;

import java.io.File;
import java.nio.file.*;
import java.util.*;

/** Offline edits of existing map functions. Run only against an isolated exported fixture. */
public final class SymbolHotReloadProfile {
    private static final class ClockBoundary extends RuntimeException { }
    record Edit(String file, String before, String after) { }
    record Build(String script, LuaHotReload.Manifest manifest, BackendWork.Snapshot work, double millis) { }
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static class Probe extends RunMap {
        Probe(Path root) {
            super(null, WFile.create(root), Optional.empty(), Optional.empty(), arguments(root),
                Optional.of(root.resolve("NO_GAME.exe").toString()));
        }
        Build compile(ModelManagerImpl manager, Path root) throws Exception {
            Path input = root.resolve("_build/symbol-input.w3x");
            Files.copy(root.resolve("base.w3x"), input, StandardCopyOption.REPLACE_EXISTING);
            WurstGuiCliImpl gui = new WurstGuiCliImpl(); long start = System.nanoTime();
            try (BackendWork work = BackendWork.measure()) {
                File output = compileScript(gui, manager, compileArgs, Optional.of(input.toFile()),
                    WurstProjectConfigReader.load(root.resolve("wurst.build")), false, root.resolve("wurst/war3map.j").toFile());
                if (gui.getErrorCount() != 0) throw new IllegalStateException(gui.getErrors().toString());
                return new Build(Files.readString(output.toPath()), luaHotReloadManifest, work.snapshot(), elapsed(start));
            }
        }
    }
    private static List<String> arguments(Path root) {
        var args = new ArrayList<>(WurstCommands.getCompileArgs(WFile.create(root), false));
        if (Boolean.getBoolean("profile.publicCompiler")) args.remove("-luaEncapsulation");
        args.add("-luaHotReload"); args.add("-hotreload"); return args;
    }
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]).toAbsolutePath();
        if (!root.getFileName().toString().equals("project") || !Files.isRegularFile(root.getParent().resolve("corpus.json")))
            throw new IllegalArgumentException("Use an isolated exported map-commit fixture/project");
        Files.createDirectories(root.resolve("_build"));
        try (var channel = java.nio.channels.FileChannel.open(root.resolve("_build/symbol-profile.lock"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             var lock = channel.tryLock()) {
            if (lock == null) throw new IllegalStateException("Another symbol profile owns this fixture");
            for (int attempt = 0; ; attempt++) {
                try { run(args); break; }
                catch (ClockBoundary boundary) {
                    if (attempt == 2) throw new IllegalStateException("Repeated build-date minute boundaries", boundary);
                    System.out.println("SYMBOL_RETRY build-date minute changed; repeating the complete oracle and edit");
                }
            }
        }
    }
    private static void run(String[] args) throws Exception {
        Path root = Path.of(args[0]).toAbsolutePath(), spec = Path.of(args[1]);
        Edit edit = JSON.fromJson(Files.readString(spec), Edit.class);
        Path file = root.resolve(edit.file).normalize();
        if (!file.startsWith(root.resolve("wurst"))) throw new IllegalArgumentException("Edit outside fixture source tree");
        String original = Files.readString(file);
        if (original.indexOf(edit.before) < 0 || original.indexOf(edit.before) != original.lastIndexOf(edit.before))
            throw new IllegalArgumentException("Edit must match exactly once: " + edit.before);
        String changed = original.replace(edit.before, edit.after);
        ModelManagerImpl manager = new ModelManagerImpl(root.toFile(), new BufferManager());
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            manager.buildProject();
            int second = java.time.LocalDateTime.now().getSecond();
            if (second > 8) Thread.sleep((62L - second) * 1000);
            var clockWindow = java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
            Build before = new Probe(root).compile(manager, root);
            before = new Probe(root).compile(manager, root); // untimed warm-up, identical for both compiler variants
            System.out.println("SYMBOL_BASELINE " + before.millis + " " + manager.incrementalImTranslation().scalarBackendReason());
            Path session = root.resolve("_build/symbol-session"), delivery = root.resolve("_build/symbol-delivery");
            LuaHotReloadSession.start(session, before.manifest);
            int runs = Integer.getInteger("profile.runs", 1);
            for (int run = 0; run < runs; run++) {
                String text = run % 2 == 0 ? changed : original;
                long start = System.nanoTime(); Files.writeString(file, text);
                manager.reconcile(manager.syncCompilationUnitContent(WFile.create(file), text));
                double reconcile = elapsed(start);
                Build after = new Probe(root).compile(manager, root);
                if (!Boolean.getBoolean("profile.baseline")) {
                    JsonObject work = JSON.toJsonTree(after.work).getAsJsonObject();
                    if (work.get("runtimeLoweredFunctions").getAsInt() != 1 || after.work.emittedFunctions() != 1) {
                        if (!clockWindow.equals(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES))) throw new ClockBoundary();
                        throw new AssertionError("Edit did not take the incremental runtime path: " + after.work + " "
                            + manager.incrementalImTranslation().scalarBackendReason());
                    }
                }
                var sent = LuaHotReloadSession.send(session, delivery, after.manifest);
                double total = elapsed(start);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("run", run); row.put("reconcileMillis", reconcile); row.put("compileMillis", after.millis);
                row.put("baseline", Boolean.getBoolean("profile.baseline"));
                row.put("totalMillis", total); row.put("work", after.work);
                row.put("reason", manager.incrementalImTranslation().scalarBackendReason());
                row.put("payloadFunctions", sent.payload().functions().size());
                ModelManagerImpl fresh = new ModelManagerImpl(root.toFile(), new BufferManager());
                try {
                    fresh.buildProject(); Build expected = new Probe(root).compile(fresh, root);
                    if (!expected.script.equals(after.script) || !expected.manifest.equals(after.manifest)) {
                        Files.writeString(root.resolve("_build/symbol-expected.lua"), expected.script);
                        Files.writeString(root.resolve("_build/symbol-actual.lua"), after.script);
                        if (!clockWindow.equals(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES))) throw new ClockBoundary();
                        throw new AssertionError("Fresh output differs within the same clock window");
                    }
                    row.put("freshOracle", "equal Lua and manifest"); row.put("freshCompileMillis", expected.millis);
                } finally { fresh.clean(); }
                results.add(row); System.out.println("SYMBOL_PROFILE " + JSON.toJson(row));
                Files.writeString(root.resolve("_build/symbol-results.json"), JSON.toJson(results));
            }
        } finally { manager.clean(); Files.writeString(file, original); }
    }
    private static double elapsed(long start) { return (System.nanoTime() - start) / 1e6; }
}
