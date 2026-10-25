package de.peeeq.wurstio.languageserver.requests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.peeeq.wurstscript.translation.lua.translation.LuaHotReload;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The editor side of the Lua hot reload (docs/LUA_HOT_RELOAD.md). Running a map built with {@code -luaHotReload}
 * saves its manifest as the baseline; each reload compiles the project again, builds the payload against that
 * baseline and writes it where the running map polls for it. Payloads are cumulative: each one carries every
 * function changed since the map started, so a map which missed one still ends up running the current program.
 */
public final class LuaHotReloadSession {

    static final String SESSION_FOLDER = "luaHotReload";
    private static final String BASELINE_PREFIX = "baseline-";
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    /** The longest string one line of the preload file passes to the map. */
    public static final int PRELOAD_CHUNK = 1000;

    private LuaHotReloadSession() {
    }

    /**
     * The manifest last read from a baseline file. A baseline file is named by its content and never rewritten, so the
     * manifest read from it once stands for it: the reloads of a session in the language server parse it only once.
     */
    private static SoftReference<Map.Entry<Path, LuaHotReload.Manifest>> lastBaseline = new SoftReference<>(null);

    /** What the session remembers between reloads: the started map's manifest and what was sent to it. */
    private static final class State {
        /**
         * The file in the session's folder which holds the manifest of the started map, named by its content
         * ({@link LuaHotReloadSession#baselineFile(String)}). A large map's manifest is tens of megabytes, and no reload changes it, so the state
         * which each reload saves only names it.
         */
        String baselineFile;
        int version;
        List<String> reloaded = new ArrayList<>();
        /** The functions a version the map ran stored as a value, with the hash of the definition stored. */
        Map<String, String> capturedDefinitions = new LinkedHashMap<>();
        Map<String, List<String>> slots = new LinkedHashMap<>();
        Map<String, String> initializers = new LinkedHashMap<>();
        List<String> packageInits = new ArrayList<>();
        /** The initializers of globals a version the map ran declared as variables, which code may have written. */
        List<String> variables = new ArrayList<>();
        /** Every shape sent of each closure told apart by its order, by lineage. */
        Map<String, List<String>> groupShapes = new LinkedHashMap<>();
        /** How the last payload sent matched the closures of its program. */
        // Null means no layout was recorded (a new or older session); an empty list records removal of all closures.
        List<LuaHotReload.GroupMember> groupLayout;
        /** Where each closure a payload added kept its tables when it began. */
        Map<String, Map<String, String>> lineageOrigins = new LinkedHashMap<>();
        /** The order groups whose names a payload gave other tables. */
        List<String> permutedGroups = new ArrayList<>();
        Set<String> reboundSlots = new LinkedHashSet<>();
        Map<String, Integer> compiletimeGroups = new LinkedHashMap<>();
        Map<String, Map<String, String>> lineageTypes = new LinkedHashMap<>();
        Map<String, Set<String>> tableReaders = new LinkedHashMap<>();
    }

    /**
     * A session without the editor, for a map built with {@code -build ... -luaHotReload}, which leaves its manifest in
     * {@code <build dir>/luaHotReload/manifest.json}:
     * <ul>
     *   <li>{@code start <build dir> <CustomMapData>}: the map of the last build is the one which runs; the payload files
     *   of an earlier session are deleted;</li>
     *   <li>{@code send <build dir> <CustomMapData>}: writes the next payload, which takes the running map to the program
     *   of the last build, where the map polls for it;</li>
     *   <li>{@code prepare <build dir> <CustomMapData>}: stages the last build and sets old payloads aside before launch;</li>
     *   <li>{@code activate <build dir> <CustomMapData>}: commits a prepared launch and discards the old payloads;</li>
     *   <li>{@code abort <build dir> <CustomMapData>}: drops a prepared launch and restores the old payloads;</li>
     *   <li>{@code <baseline manifest.json> <current manifest.json> <output .pld>}: the first payload a map running the
     *   first manifest would get for the second, written as a preload file.</li>
     * </ul>
     * A payload the edit cannot carry exits with 1 and prints the reasons.
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 3 && Set.of("start", "send", "prepare", "activate", "abort").contains(args[0])) {
            Path buildDir = Path.of(args[1]);
            Path customMapData = Path.of(args[2]);
            if (args[0].equals("activate")) {
                completeLaunch(buildDir, customMapData);
                System.out.println("session activated");
                return;
            }
            if (args[0].equals("abort")) {
                abortLaunch(buildDir, customMapData);
                System.out.println("launch aborted; previous session restored");
                return;
            }
            LuaHotReload.Manifest manifest = readManifest(buildDir.resolve(SESSION_FOLDER).resolve("manifest.json"));
            if (args[0].equals("prepare")) {
                prepareLaunch(buildDir, customMapData, manifest);
                System.out.println("session prepared");
                return;
            }
            if (args[0].equals("start")) {
                prepareLaunch(buildDir, customMapData, manifest);
                completeLaunch(buildDir, customMapData);
                System.out.println("session started");
                return;
            }
            try {
                Sent sent = send(buildDir, customMapData, manifest);
                System.out.println("payload " + sent.payload().version() + " (" + String.join(", ", sent.payload().functions())
                    + ") written to " + sent.file());
                sent.payload().warnings().forEach(w -> System.out.println("warning: " + w));
            } catch (LuaHotReload.Rejected e) {
                System.out.println(e.getMessage());
                System.exit(1);
            }
            return;
        }
        if (args.length != 3) {
            System.err.println("usage: start|send|prepare|activate|abort <build dir> <CustomMapData>"
                + " | <baseline manifest.json> <current manifest.json> <output .pld>");
            System.exit(2);
        }
        LuaHotReload.Manifest baseline = readManifest(Path.of(args[0]));
        LuaHotReload.Manifest current = readManifest(Path.of(args[1]));
        try {
            LuaHotReload.Payload payload = LuaHotReload.payload(baseline, current, Set.of(), 1);
            Files.writeString(Path.of(args[2]), preloadFile(payload.source()), StandardCharsets.UTF_8);
            System.out.println("payload " + payload.version() + ": " + String.join(", ", payload.functions()));
        } catch (LuaHotReload.Rejected e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /** Leaves the manifest of a build where {@link #main} reads it. */
    public static void writeManifest(Path buildDir, LuaHotReload.Manifest manifest) throws IOException {
        Path file = buildDir.resolve(SESSION_FOLDER).resolve("manifest.json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, GSON.toJson(manifest), StandardCharsets.UTF_8);
    }

    private static LuaHotReload.Manifest readManifest(Path file) throws IOException {
        return GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), LuaHotReload.Manifest.class);
    }

    /** Records the manifest of a map which is being started; earlier payloads do not apply to it. */
    public static void start(Path buildDir, LuaHotReload.Manifest manifest) throws IOException {
        stage(buildDir, manifest);
        activate(buildDir);
    }

    /** Preserve the active session while hiding its payloads from a map which is about to launch. */
    public static void prepareLaunch(Path buildDir, Path customMapData, LuaHotReload.Manifest manifest) throws IOException {
        stage(buildDir, manifest);
        try {
            setAsideDeliveries(customMapData);
        } catch (IOException e) {
            try {
                unstage(buildDir);
            } catch (IOException cleanup) {
                e.addSuppressed(cleanup);
            }
            throw e;
        }
    }

    /** Called only after the launcher succeeds; errors after activation must not restore obsolete payloads. */
    public static void completeLaunch(Path buildDir, Path customMapData) throws IOException {
        activate(buildDir);
        discardDeliveries(customMapData);
    }

    /** Attempt both rollback steps, preserving diagnostics if either fails. Safe when no launch was prepared. */
    public static void abortLaunch(Path buildDir, Path customMapData) throws IOException {
        try {
            restoreDeliveries(customMapData);
        } catch (IOException e) {
            try {
                unstage(buildDir);
            } catch (IOException cleanup) {
                e.addSuppressed(cleanup);
            }
            throw e;
        }
        unstage(buildDir);
    }

    /**
     * Writes the session of a map about to be launched beside the current one, which stays in effect: a launch which
     * fails leaves the map that is still running its session. {@link #activate} makes the staged one current, as one
     * rename, once the map is launched; {@link #unstage} drops it.
     */
    public static void stage(Path buildDir, LuaHotReload.Manifest manifest) throws IOException {
        String json = GSON.toJson(manifest);
        Path baseline = buildDir.resolve(SESSION_FOLDER).resolve(baselineFile(json));
        if (!Files.exists(baseline)) {
            writeAtomically(baseline, json);
        }
        State state = new State();
        state.baselineFile = baseline.getFileName().toString();
        writeAtomically(stagedFile(buildDir), GSON.toJson(state));
    }

    public static void activate(Path buildDir) throws IOException {
        Files.move(stagedFile(buildDir), stateFile(buildDir), java.nio.file.StandardCopyOption.ATOMIC_MOVE,
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Files.deleteIfExists(pendingFile(buildDir));
        deleteOtherBaselines(buildDir);
    }

    public static void unstage(Path buildDir) throws IOException {
        Files.deleteIfExists(stagedFile(buildDir));
        deleteOtherBaselines(buildDir);
    }

    /** The name of the file which holds a baseline manifest of this JSON text: its SHA-256. */
    private static String baselineFile(String json) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(json.getBytes(StandardCharsets.UTF_8));
            return BASELINE_PREFIX + HexFormat.of().formatHex(hash) + ".json";
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Deletes the baseline files which neither the current nor a staged session names. */
    private static void deleteOtherBaselines(Path buildDir) throws IOException {
        Set<String> used = new LinkedHashSet<>();
        for (Path stateFile : List.of(stateFile(buildDir), stagedFile(buildDir))) {
            if (Files.exists(stateFile)) {
                State state = GSON.fromJson(Files.readString(stateFile, StandardCharsets.UTF_8), State.class);
                if (state.baselineFile != null) {
                    used.add(state.baselineFile);
                }
            }
        }
        Path folder = buildDir.resolve(SESSION_FOLDER);
        if (!Files.isDirectory(folder)) {
            return;
        }
        try (Stream<Path> files = Files.list(folder)) {
            for (Path file : files.toList()) {
                String name = file.getFileName().toString();
                if (name.startsWith(BASELINE_PREFIX) && name.endsWith(".json") && !used.contains(name)) {
                    Files.delete(file);
                }
            }
        }
    }

    /** The started map's manifest, which the session's state names. */
    private static LuaHotReload.Manifest baseline(Path buildDir, State state) throws IOException, LuaHotReload.Rejected {
        Path file = state.baselineFile == null ? null : buildDir.resolve(SESSION_FOLDER).resolve(state.baselineFile);
        if (file == null || !Files.exists(file)) {
            throw new LuaHotReload.Rejected(List.of("the manifest of the map started with hot run is missing:"
                + " start the map with hot run again"));
        }
        Path key = file.toAbsolutePath().normalize();
        synchronized (LuaHotReloadSession.class) {
            Map.Entry<Path, LuaHotReload.Manifest> last = lastBaseline.get();
            if (last != null && last.getKey().equals(key)) {
                return last.getValue();
            }
        }
        LuaHotReload.Manifest manifest = readManifest(file);
        synchronized (LuaHotReloadSession.class) {
            lastBaseline = new SoftReference<>(Map.entry(key, manifest));
        }
        return manifest;
    }

    private static Path stagedFile(Path buildDir) {
        return buildDir.resolve(SESSION_FOLDER).resolve("session-staged.json");
    }

    /** A payload being written: recorded before its file, so a session which could not record it can still account for it. */
    private static final class Pending {
        int version;
        List<String> functions = new ArrayList<>();
        Map<String, String> capturedDefinitions = new LinkedHashMap<>();
        Map<String, List<String>> slots = new LinkedHashMap<>();
        Map<String, String> initializers = new LinkedHashMap<>();
        List<String> packageInits = new ArrayList<>();
        List<String> variables = new ArrayList<>();
        Map<String, String> groupShapes = new LinkedHashMap<>();
        List<LuaHotReload.GroupMember> groupLayout;
        Map<String, Map<String, String>> lineageOrigins = new LinkedHashMap<>();
        List<String> permutedGroups = new ArrayList<>();
        Set<String> reboundSlots = new LinkedHashSet<>();
        Map<String, Integer> compiletimeGroups = new LinkedHashMap<>();
        Map<String, Map<String, String>> lineageTypes = new LinkedHashMap<>();
        Map<String, Set<String>> tableReaders = new LinkedHashMap<>();
    }

    /** A payload and the file it was written to. */
    public record Sent(LuaHotReload.Payload payload, Path file) {
    }

    /**
     * What the editor shows of a payload's warnings, as one message: an edit which removes a class warns once for each
     * of its methods, which would be as many notifications. Null for a payload without warnings.
     */
    public static String warningNote(LuaHotReload.Payload payload) {
        List<String> warnings = payload.warnings();
        if (warnings.isEmpty()) {
            return null;
        }
        if (warnings.size() == 1) {
            return warnings.get(0);
        }
        return "Hot reload payload " + payload.version() + " applies with " + warnings.size() + " notes:\n"
            + String.join("\n", warnings);
    }

    /**
     * Writes the next payload for the running map where its poll reads it, under the game's CustomMapData folder, and
     * only then records it as sent: a payload which could not be written leaves the session as it was, so the next
     * attempt takes the number the map is waiting for.
     */
    public static Sent send(Path buildDir, Path customMapData, LuaHotReload.Manifest current)
        throws IOException, LuaHotReload.Rejected {
        if (Files.exists(stagedFile(buildDir))) {
            throw new LuaHotReload.Rejected(List.of("a hot reload launch is prepared: activate or abort it before sending a payload"));
        }
        Path file = stateFile(buildDir);
        if (!Files.exists(file)) {
            throw new LuaHotReload.Rejected(List.of("no map was started with hot run (or -luaHotReload): start the map with hot run first"));
        }
        State state = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), State.class);
        recoverPending(buildDir, customMapData, state);
        LuaHotReload.Payload payload = LuaHotReload.payload(baseline(buildDir, state), current, new LuaHotReload.History(
            new LinkedHashSet<>(state.reloaded), orEmpty(state.capturedDefinitions), orEmpty(state.slots),
            orEmpty(state.initializers), new LinkedHashSet<>(orEmpty(state.packageInits)),
            new LinkedHashSet<>(orEmpty(state.variables)), orEmpty(state.groupShapes),
            state.groupLayout, orEmpty(state.lineageOrigins),
            new LinkedHashSet<>(orEmpty(state.permutedGroups)),
            state.reboundSlots == null ? Set.of() : state.reboundSlots, orEmpty(state.lineageTypes), orEmpty(state.tableReaders), orEmpty(state.compiletimeGroups)), state.version + 1);
        Pending pending = new Pending();
        pending.version = payload.version();
        pending.functions = payload.functions();
        pending.capturedDefinitions = new LinkedHashMap<>(payload.captured());
        pending.slots = new LinkedHashMap<>(payload.slots());
        pending.initializers = new LinkedHashMap<>(payload.initializers());
        pending.packageInits = new ArrayList<>(payload.packageInits());
        pending.variables = new ArrayList<>(payload.variables());
        pending.groupShapes = new LinkedHashMap<>(payload.groupShapes());
        pending.groupLayout = new ArrayList<>(payload.groupLayout());
        pending.lineageOrigins = new LinkedHashMap<>(payload.lineageOrigins());
        pending.permutedGroups = new ArrayList<>(payload.permutedGroups());
        pending.reboundSlots = new LinkedHashSet<>(payload.reboundSlots());
        pending.compiletimeGroups = new LinkedHashMap<>(payload.compiletimeGroups());
        pending.lineageTypes = new LinkedHashMap<>(payload.lineageTypes());
        pending.tableReaders = new LinkedHashMap<>(payload.tableReaders());
        writeAtomically(pendingFile(buildDir), GSON.toJson(pending));
        Path delivered = deliver(customMapData, payload);
        record(state, pending);
        save(buildDir, state);
        Files.deleteIfExists(pendingFile(buildDir));
        return new Sent(payload, delivered);
    }

    /**
     * A payload whose file was written but which the session could not record (the build stopped, or the state could not
     * be saved) may already be in the map, which reads each file once: the session takes it as sent, so the next payload
     * gets the next number and carries its functions again. One whose file is missing never reached the map.
     */
    private static void recoverPending(Path buildDir, Path customMapData, State state) throws IOException {
        Path file = pendingFile(buildDir);
        if (!Files.exists(file)) {
            return;
        }
        Pending pending = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), Pending.class);
        Path delivered = customMapData.resolve(LuaHotReload.preloadFile(pending.version).replace('\\', '/'));
        if (pending.version > state.version && Files.exists(delivered)) {
            record(state, pending);
            save(buildDir, state);
        }
        Files.delete(file);
    }

    private static void record(State state, Pending sent) {
        Set<String> reloaded = new LinkedHashSet<>(state.reloaded);
        reloaded.addAll(sent.functions);
        state.reloaded = new ArrayList<>(reloaded);
        // The first definition stored is the one the stored value keeps.
        Map<String, String> captured = new LinkedHashMap<>(orEmpty(state.capturedDefinitions));
        orEmpty(sent.capturedDefinitions).forEach(captured::putIfAbsent);
        state.capturedDefinitions = captured;
        Map<String, List<String>> slots = new LinkedHashMap<>(orEmpty(state.slots));
        for (Map.Entry<String, List<String>> entry : orEmpty(sent.slots).entrySet()) {
            Set<String> bindings = new LinkedHashSet<>(slots.getOrDefault(entry.getKey(), List.of()));
            bindings.addAll(entry.getValue());
            slots.put(entry.getKey(), List.copyOf(bindings));
        }
        state.slots = slots;
        Map<String, String> initializers = new LinkedHashMap<>(orEmpty(state.initializers));
        orEmpty(sent.initializers).forEach(initializers::putIfAbsent);
        state.initializers = initializers;
        Set<String> packageInits = new LinkedHashSet<>(orEmpty(state.packageInits));
        packageInits.addAll(orEmpty(sent.packageInits));
        state.packageInits = new ArrayList<>(packageInits);
        Set<String> variables = new LinkedHashSet<>(orEmpty(state.variables));
        variables.addAll(orEmpty(sent.variables));
        state.variables = new ArrayList<>(variables);
        // The map runs one of the shapes sent, depending on which payloads it installed: keep them all.
        Map<String, List<String>> groupShapes = new LinkedHashMap<>(orEmpty(state.groupShapes));
        for (Map.Entry<String, String> entry : orEmpty(sent.groupShapes).entrySet()) {
            Set<String> shapes = new LinkedHashSet<>(groupShapes.getOrDefault(entry.getKey(), List.of()));
            shapes.add(entry.getValue());
            groupShapes.put(entry.getKey(), List.copyOf(shapes));
        }
        state.groupShapes = groupShapes;
        // The next payload matches its closures with those of this one.
        state.groupLayout = sent.groupLayout == null ? null : new ArrayList<>(sent.groupLayout);
        Map<String, Map<String, String>> origins = new LinkedHashMap<>(orEmpty(state.lineageOrigins));
        orEmpty(sent.lineageOrigins).forEach(origins::putIfAbsent);
        state.lineageOrigins = origins;
        Set<String> permuted = new LinkedHashSet<>(orEmpty(state.permutedGroups));
        permuted.addAll(orEmpty(sent.permutedGroups));
        state.permutedGroups = new ArrayList<>(permuted);
        Set<String> reboundSlots = new LinkedHashSet<>(state.reboundSlots == null ? Set.of() : state.reboundSlots);
        reboundSlots.addAll(sent.reboundSlots == null ? Set.of() : sent.reboundSlots);
        state.reboundSlots = reboundSlots;
        Map<String, Integer> compiletimeGroups = new LinkedHashMap<>(orEmpty(state.compiletimeGroups));
        orEmpty(sent.compiletimeGroups).forEach(compiletimeGroups::putIfAbsent);
        state.compiletimeGroups = compiletimeGroups;
        Map<String, Map<String, String>> types = new LinkedHashMap<>(orEmpty(state.lineageTypes));
        orEmpty(sent.lineageTypes).forEach((lineage, fields) -> {
            Map<String, String> combined = new LinkedHashMap<>(types.getOrDefault(lineage, Map.of()));
            fields.forEach(combined::putIfAbsent);
            types.put(lineage, combined);
        });
        state.lineageTypes = types;
        Map<String, Set<String>> readers = new LinkedHashMap<>(orEmpty(state.tableReaders));
        orEmpty(sent.tableReaders).forEach((name, lineages) -> {
            Set<String> combined = new java.util.TreeSet<>(readers.getOrDefault(name, Set.of()));
            combined.addAll(lineages);
            readers.put(name, combined);
        });
        state.tableReaders = readers;
        state.version = sent.version;
    }

    /** A list Gson left null because an older session file did not have it. */
    private static List<String> orEmpty(List<String> list) {
        return list == null ? List.of() : list;
    }

    /** A map Gson left null because an older session file did not have it. */
    private static <V> Map<String, V> orEmpty(Map<String, V> map) {
        return map == null ? Map.of() : map;
    }

    private static Path pendingFile(Path buildDir) {
        return buildDir.resolve(SESSION_FOLDER).resolve("pending.json");
    }

    /**
     * Writes the payload's file by renaming a complete temporary file: the game reads a file once, so a poll which met a
     * file half written would never see the rest.
     */
    private static Path deliver(Path customMapData, LuaHotReload.Payload payload) throws IOException {
        Path file = customMapData.resolve(LuaHotReload.preloadFile(payload.version()).replace('\\', '/'));
        writeAtomically(file, preloadFile(payload.source()));
        return file;
    }

    /** Writes a complete temporary file and renames it, so no reader and no interrupted write leaves a partial file. */
    private static void writeAtomically(Path file, String text) throws IOException {
        Files.createDirectories(file.getParent());
        Path partial = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(partial, text, StandardCharsets.UTF_8);
        Files.move(partial, file, java.nio.file.StandardCopyOption.ATOMIC_MOVE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Moves the payload files of the current session aside before a map is launched: the new map's poll begins again at
     * the first file, and a payload of another session must not reach it. If the launch fails the map which is still
     * running may not have read them yet, so {@link #restoreDeliveries} puts them back; once the new map is launched
     * {@link #discardDeliveries} deletes them.
     */
    public static void setAsideDeliveries(Path customMapData) throws IOException {
        Path folder = deliveryFolder(customMapData);
        Path aside = folder.resolve(SET_ASIDE);
        if (Files.exists(aside.resolve(SUPERSEDED))) {
            // Left by a launch which succeeded: the files of a session no map runs any more.
            deleteFolder(aside);
        } else {
            // Left by a restore which was interrupted: the running map may not have read them yet.
            restoreDeliveries(customMapData);
        }
        if (!Files.isDirectory(folder)) {
            return;
        }
        Files.createDirectories(aside);
        try {
            for (Path file : deliveries(folder)) {
                Files.move(file, aside.resolve(file.getFileName()));
            }
        } catch (IOException e) {
            // A file the running map has not read must not stay hidden: put back what was moved.
            try {
                restoreDeliveries(customMapData);
            } catch (IOException cleanup) {
                e.addSuppressed(cleanup);
            }
            throw e;
        }
    }

    /** Puts the files {@link #setAsideDeliveries} moved back, after a launch which failed. */
    public static void restoreDeliveries(Path customMapData) throws IOException {
        Path folder = deliveryFolder(customMapData);
        Path aside = folder.resolve(SET_ASIDE);
        if (!Files.isDirectory(aside)) {
            return;
        }
        if (Files.exists(aside.resolve(SUPERSEDED))) {
            // Activation already committed. Cleanup may be retried, but these files must never reach the new map.
            deleteFolder(aside);
            return;
        }
        for (Path file : deliveries(aside)) {
            Files.move(file, folder.resolve(file.getFileName()), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        deleteFolder(aside);
    }

    /**
     * Deletes the files {@link #setAsideDeliveries} moved, once the new map is launched. The folder is marked first, so
     * a deletion which is interrupted leaves files the next launch knows to delete rather than restore.
     */
    public static void discardDeliveries(Path customMapData) throws IOException {
        Path aside = deliveryFolder(customMapData).resolve(SET_ASIDE);
        if (!Files.isDirectory(aside)) {
            return;
        }
        Files.writeString(aside.resolve(SUPERSEDED), "");
        deleteFolder(aside);
    }

    private static final String SET_ASIDE = "hotreload-set-aside";
    private static final String SUPERSEDED = "superseded";

    private static Path deliveryFolder(Path customMapData) {
        return customMapData.resolve(LuaHotReload.PRELOAD_FILE_PREFIX.replace('\\', '/')).getParent();
    }

    private static List<Path> deliveries(Path folder) throws IOException {
        try (java.util.stream.Stream<Path> files = Files.list(folder)) {
            return files.filter(file -> {
                String name = file.getFileName().toString();
                return Files.isRegularFile(file) && name.startsWith("hotreload-")
                    && (name.endsWith(".pld") || name.endsWith(".pld.tmp"));
            }).sorted().toList();
        }
    }

    private static void deleteFolder(Path folder) throws IOException {
        if (!Files.isDirectory(folder)) {
            return;
        }
        for (Path file : deliveries(folder)) {
            Files.delete(file);
        }
        Files.deleteIfExists(folder.resolve(SUPERSEDED));
        Files.delete(folder);
    }

    /**
     * A preload file whose Lua section hands {@code payload} to the map through the tooltip of
     * {@link LuaHotReload#CHANNEL_ABILITY}, as the map's poll expects. The layout is the one the game writes with
     * {@code PreloadGenEnd}, with the user-code markers of a Lua map; whether {@code Preloader} runs that section, and
     * in which environment, is what the in-game probe establishes (docs/LUA_HOT_RELOAD.md).
     */
    public static String preloadFile(String payload) {
        StringBuilder sb = new StringBuilder();
        sb.append("function PreloadFiles takes nothing returns nothing\n");
        sb.append("//!beginusercode\n");
        // The marker tells the map's poll that the tooltip holds a payload rather than the ability's own text.
        sb.append("local p = {\"").append(LuaHotReload.PAYLOAD_MARKER).append("\"}\n");
        for (int i = 0; i < payload.length(); ) {
            int end = Math.min(payload.length(), i + PRELOAD_CHUNK);
            // A chunk ends on a whole character: half a surrogate pair cannot be written as UTF-8.
            if (end < payload.length() && Character.isHighSurrogate(payload.charAt(end - 1))) {
                end--;
            }
            sb.append("p[#p + 1] = \"");
            appendEscaped(sb, payload.substring(i, end));
            sb.append("\"\n");
            i = end;
        }
        sb.append("BlzSetAbilityTooltip(FourCC(\"").append(LuaHotReload.CHANNEL_ABILITY).append("\"), table.concat(p), 0)\n");
        sb.append("//!endusercode\n");
        sb.append("endfunction\n");
        return sb.toString();
    }

    /**
     * A Lua quoted-string body on one line. A {@code //!} is written {@code //\033}, so the payload cannot hold a
     * marker which would end the Lua section early.
     */
    private static void appendEscaped(StringBuilder sb, String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                default -> {
                    if (c < 0x20 || c == 0x7f || (c == '!' && i >= 2 && s.startsWith("//", i - 2))) {
                        sb.append(String.format(Locale.ROOT, "\\%03d", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
    }

    private static Path stateFile(Path buildDir) {
        return buildDir.resolve(SESSION_FOLDER).resolve("session.json");
    }

    private static void save(Path buildDir, State state) throws IOException {
        // A payload may already be out: a half-written state would lose the number the map waits for next.
        writeAtomically(stateFile(buildDir), GSON.toJson(state));
    }
}
