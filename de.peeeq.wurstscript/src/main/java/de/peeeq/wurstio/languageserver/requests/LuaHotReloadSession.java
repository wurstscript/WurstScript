package de.peeeq.wurstio.languageserver.requests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.peeeq.wurstscript.translation.lua.translation.LuaHotReload;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The editor side of the Lua hot reload (docs/LUA_HOT_RELOAD.md). Running a map built with {@code -luaHotReload}
 * saves its manifest as the baseline; each reload compiles the project again, builds the payload against that
 * baseline and writes it where the running map polls for it. Payloads are cumulative: each one carries every
 * function changed since the map started, so a map which missed one still ends up running the current program.
 */
public final class LuaHotReloadSession {

    static final String SESSION_FOLDER = "luaHotReload";
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    /** The longest string one line of the preload file passes to the map. */
    public static final int PRELOAD_CHUNK = 1000;

    private LuaHotReloadSession() {
    }

    /** What the session remembers between reloads: the started map's manifest and what was sent to it. */
    private static final class State {
        LuaHotReload.Manifest baseline;
        int version;
        List<String> reloaded = new ArrayList<>();
        /** The functions a version the map ran stored as a value, with the hash of the definition stored. */
        Map<String, String> capturedDefinitions = new LinkedHashMap<>();
    }

    /**
     * Builds the payload which takes a script with the first manifest to the program of the second and writes it as
     * a preload file: {@code <baseline manifest.json> <current manifest.json> <output .pld>}. Each build with
     * {@code -luaHotReload} leaves its manifest in {@code _build/luaHotReload/manifest.json}.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("usage: <baseline manifest.json> <current manifest.json> <output .pld>");
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

    /**
     * Writes the session of a map about to be launched beside the current one, which stays in effect: a launch which
     * fails leaves the map that is still running its session. {@link #activate} makes the staged one current, as one
     * rename, once the map is launched; {@link #unstage} drops it.
     */
    public static void stage(Path buildDir, LuaHotReload.Manifest manifest) throws IOException {
        State state = new State();
        state.baseline = manifest;
        writeAtomically(stagedFile(buildDir), GSON.toJson(state));
    }

    public static void activate(Path buildDir) throws IOException {
        Files.move(stagedFile(buildDir), stateFile(buildDir), java.nio.file.StandardCopyOption.ATOMIC_MOVE,
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Files.deleteIfExists(pendingFile(buildDir));
    }

    public static void unstage(Path buildDir) throws IOException {
        Files.deleteIfExists(stagedFile(buildDir));
    }

    private static Path stagedFile(Path buildDir) {
        return buildDir.resolve(SESSION_FOLDER).resolve("session-staged.json");
    }

    /** A payload being written: recorded before its file, so a session which could not record it can still account for it. */
    private static final class Pending {
        int version;
        List<String> functions = new ArrayList<>();
        Map<String, String> capturedDefinitions = new LinkedHashMap<>();
    }

    /** A payload and the file it was written to. */
    public record Sent(LuaHotReload.Payload payload, Path file) {
    }

    /**
     * Writes the next payload for the running map where its poll reads it, under the game's CustomMapData folder, and
     * only then records it as sent: a payload which could not be written leaves the session as it was, so the next
     * attempt takes the number the map is waiting for.
     */
    public static Sent send(Path buildDir, Path customMapData, LuaHotReload.Manifest current)
        throws IOException, LuaHotReload.Rejected {
        Path file = stateFile(buildDir);
        if (!Files.exists(file)) {
            throw new LuaHotReload.Rejected(List.of("no map was started with hot run (or -luaHotReload): start the map with hot run first"));
        }
        State state = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), State.class);
        recoverPending(buildDir, customMapData, state);
        LuaHotReload.Payload payload = LuaHotReload.payload(state.baseline, current, new LuaHotReload.History(
            new LinkedHashSet<>(state.reloaded), orEmpty(state.capturedDefinitions)), state.version + 1);
        Pending pending = new Pending();
        pending.version = payload.version();
        pending.functions = payload.functions();
        pending.capturedDefinitions = new LinkedHashMap<>(payload.captured());
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
        state.version = sent.version;
    }

    /** A list Gson left null because an older session file did not have it. */
    private static List<String> orEmpty(List<String> list) {
        return list == null ? List.of() : list;
    }

    /** A map Gson left null because an older session file did not have it. */
    private static Map<String, String> orEmpty(Map<String, String> map) {
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
            restoreDeliveries(customMapData);
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
