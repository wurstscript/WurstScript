package de.peeeq.wurstio.languageserver;

import config.WurstProjectConfigData;
import de.peeeq.wurstscript.WLogger;
import net.moonlightflower.wc3libs.port.GameVersion;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static de.peeeq.wurstio.languageserver.ProjectConfigBuilder.FILE_NAME;

public final class WurstBuildConfig {

    public enum ScriptMode {
        LUA,
        JASS
    }

    public enum Wc3Patch {
        REFORGED,
        PRE_129
    }

    private final Optional<ScriptMode> scriptMode;
    private final Optional<Wc3Patch> wc3Patch;

    private WurstBuildConfig(Optional<ScriptMode> scriptMode, Optional<Wc3Patch> wc3Patch) {
        this.scriptMode = scriptMode;
        this.wc3Patch = wc3Patch;
    }

    public static WurstBuildConfig empty() {
        return new WurstBuildConfig(Optional.empty(), Optional.empty());
    }

    public static WurstBuildConfig fromWorkspaceRoot(WFile workspaceRoot) {
        return fromBuildFile(Path.of(workspaceRoot.toString(), FILE_NAME));
    }

    public static WurstBuildConfig fromProject(WurstProjectConfigData projectConfig, WFile workspaceRoot) {
        WurstBuildConfig fileConfig = fromWorkspaceRoot(workspaceRoot);
        Optional<ScriptMode> scriptMode = readEnumGetter(projectConfig, "getScriptMode", ScriptMode::valueOf)
            .or(fileConfig::scriptMode);
        Optional<Wc3Patch> wc3Patch = readEnumGetter(projectConfig, "getWc3Patch", WurstBuildConfig::parsePatchName)
            .or(fileConfig::wc3Patch);
        return new WurstBuildConfig(scriptMode, wc3Patch);
    }

    static WurstBuildConfig fromBuildFile(Path buildFile) {
        if (!Files.exists(buildFile)) {
            return empty();
        }
        Optional<ScriptMode> scriptMode = Optional.empty();
        Optional<Wc3Patch> wc3Patch = Optional.empty();
        try {
            for (String rawLine : Files.readAllLines(buildFile)) {
                String line = stripComment(rawLine).trim();
                if (line.isEmpty() || Character.isWhitespace(rawLine.charAt(0))) {
                    continue;
                }
                int colon = line.indexOf(':');
                if (colon < 0) {
                    continue;
                }
                String key = line.substring(0, colon).trim();
                String value = normalizeScalar(line.substring(colon + 1).trim());
                if (key.equals("scriptMode")) {
                    scriptMode = parseScriptMode(value);
                } else if (key.equals("wc3Patch")) {
                    wc3Patch = parsePatch(value);
                }
            }
        } catch (IOException e) {
            WLogger.warning("Could not read " + buildFile + " for build settings", e);
        }
        return new WurstBuildConfig(scriptMode, wc3Patch);
    }

    public Optional<ScriptMode> scriptMode() {
        return scriptMode;
    }

    public Optional<Wc3Patch> wc3Patch() {
        return wc3Patch;
    }

    public Wc3Patch wc3PatchOrReforged() {
        return wc3Patch.orElse(Wc3Patch.REFORGED);
    }

    public GameVersion fallbackGameVersion() {
        if (wc3PatchOrReforged() == Wc3Patch.PRE_129) {
            return new GameVersion("1.28");
        }
        return GameVersion.VERSION_1_32;
    }

    public List<String> applyToCompileArgs(List<String> compileArgs) {
        if (!scriptMode.isPresent()) {
            return compileArgs;
        }
        List<String> result = new ArrayList<>();
        for (String arg : compileArgs) {
            if (!"-lua".equals(arg)) {
                result.add(arg);
            }
        }
        if (scriptMode.get() == ScriptMode.LUA) {
            result.add("-lua");
        }
        return result;
    }

    public boolean shouldUseReforgedLaunchArgs(Optional<GameVersion> detectedVersion) {
        return detectedVersion
            .map(version -> version.compareTo(GameVersion.VERSION_1_32) >= 0)
            .orElse(wc3PatchOrReforged() == Wc3Patch.REFORGED);
    }

    public boolean shouldUseClassicWindowArg(Optional<GameVersion> detectedVersion) {
        return detectedVersion
            .map(version -> version.compareTo(GameVersion.VERSION_1_31) < 0)
            .orElse(wc3PatchOrReforged() == Wc3Patch.PRE_129);
    }

    public boolean shouldCopyRunMapToWarcraftMapDir(Optional<GameVersion> detectedVersion) {
        return detectedVersion
            .map(version -> version.compareTo(GameVersion.VERSION_1_32) < 0)
            .orElse(wc3PatchOrReforged() == Wc3Patch.PRE_129);
    }

    public boolean shouldUseInstallDirForMaps(Optional<GameVersion> detectedVersion) {
        return detectedVersion
            .map(version -> version.compareTo(new GameVersion("1.27.9")) <= 0)
            .orElse(wc3PatchOrReforged() == Wc3Patch.PRE_129);
    }

    private static String stripComment(String line) {
        int commentStart = line.indexOf('#');
        return commentStart >= 0 ? line.substring(0, commentStart) : line;
    }

    private static String normalizeScalar(String value) {
        String result = value;
        if ((result.startsWith("\"") && result.endsWith("\""))
            || (result.startsWith("'") && result.endsWith("'"))) {
            result = result.substring(1, result.length() - 1);
        }
        return result.trim();
    }

    private static Optional<ScriptMode> parseScriptMode(String value) {
        String normalized = value.toUpperCase(Locale.ROOT);
        try {
            return Optional.of(ScriptMode.valueOf(normalized));
        } catch (IllegalArgumentException e) {
            WLogger.warning("Ignoring unknown scriptMode in wurst.build: " + value);
            return Optional.empty();
        }
    }

    private static Optional<Wc3Patch> parsePatch(String value) {
        try {
            return Optional.of(parsePatchName(value));
        } catch (IllegalArgumentException e) {
            WLogger.warning("Ignoring unknown wc3Patch in wurst.build: " + value);
            return Optional.empty();
        }
    }

    private static Wc3Patch parsePatchName(String value) {
        String normalized = value.toUpperCase(Locale.ROOT)
            .replace(".", "_")
            .replace("-", "_");
        if (normalized.equals("PRE1_29")) {
            normalized = "PRE_129";
        }
        return Wc3Patch.valueOf(normalized);
    }

    private interface EnumParser<T> {
        T parse(String value);
    }

    private static <T> Optional<T> readEnumGetter(WurstProjectConfigData projectConfig, String getterName, EnumParser<T> parser) {
        try {
            Method getter = projectConfig.getClass().getMethod(getterName);
            Object value = getter.invoke(projectConfig);
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of(parser.parse(value.toString()));
        } catch (NoSuchMethodException ignored) {
            return Optional.empty();
        } catch (Exception e) {
            WLogger.warning("Could not read " + getterName + " from wurst.build config", e);
            return Optional.empty();
        }
    }
}
