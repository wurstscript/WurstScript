package de.peeeq.wurstio.languageserver.requests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.peeeq.wurstscript.translation.lua.translation.LuaHotReload;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
    static final int PRELOAD_CHUNK = 1000;

    private LuaHotReloadSession() {
    }

    /** What the session remembers between reloads: the started map's manifest and what was sent to it. */
    private static final class State {
        LuaHotReload.Manifest baseline;
        int version;
        List<String> reloaded = new ArrayList<>();
    }

    /** Records the manifest of a map which is being started; earlier payloads do not apply to it. */
    public static void start(Path buildDir, LuaHotReload.Manifest manifest) throws IOException {
        State state = new State();
        state.baseline = manifest;
        save(buildDir, state);
    }

    /** The next payload for the running map, recorded as sent. */
    public static LuaHotReload.Payload nextPayload(Path buildDir, LuaHotReload.Manifest current)
        throws IOException, LuaHotReload.Rejected {
        Path file = stateFile(buildDir);
        if (!Files.exists(file)) {
            throw new LuaHotReload.Rejected(List.of("no map was started with -luaHotReload since the build folder was cleaned"));
        }
        State state = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), State.class);
        LuaHotReload.Payload payload = LuaHotReload.payload(state.baseline, current,
            new LinkedHashSet<>(state.reloaded), state.version + 1);
        Set<String> reloaded = new LinkedHashSet<>(state.reloaded);
        reloaded.addAll(payload.functions());
        state.reloaded = new ArrayList<>(reloaded);
        state.version = payload.version();
        save(buildDir, state);
        return payload;
    }

    /** Writes {@code payload} where the map's poll reads it, under the game's CustomMapData folder. */
    public static Path deliver(Path customMapData, LuaHotReload.Payload payload) throws IOException {
        Path file = customMapData.resolve(LuaHotReload.PRELOAD_FILE.replace('\\', '/'));
        Files.createDirectories(file.getParent());
        Files.writeString(file, preloadFile(payload.source()), StandardCharsets.UTF_8);
        return file;
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
        sb.append("local p = {}\n");
        for (int i = 0; i < payload.length(); i += PRELOAD_CHUNK) {
            sb.append("p[#p + 1] = \"");
            appendEscaped(sb, payload.substring(i, Math.min(payload.length(), i + PRELOAD_CHUNK)));
            sb.append("\"\n");
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
        Path file = stateFile(buildDir);
        Files.createDirectories(file.getParent());
        Files.writeString(file, GSON.toJson(state), StandardCharsets.UTF_8);
    }
}
