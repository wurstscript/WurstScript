package de.peeeq.wurstio.languageserver;

import config.WurstProjectConfigData;
import de.peeeq.wurstscript.WLogger;
import net.moonlightflower.wc3libs.port.GameVersion;
import org.wurstscript.projectconfig.Wc3PatchTarget;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static de.peeeq.wurstio.languageserver.ProjectConfigBuilder.FILE_NAME;

public final class WurstBuildConfig {

    public enum ScriptMode {
        LUA,
        JASS
    }

    public enum Wc3Patch {
        REFORGED,
        CLASSIC,
        PRE_129
    }

    private final org.wurstscript.projectconfig.WurstBuildConfig sharedConfig;

    private WurstBuildConfig(org.wurstscript.projectconfig.WurstBuildConfig sharedConfig) {
        this.sharedConfig = sharedConfig == null ? org.wurstscript.projectconfig.WurstBuildConfig.empty() : sharedConfig;
    }

    public static WurstBuildConfig empty() {
        return new WurstBuildConfig(org.wurstscript.projectconfig.WurstBuildConfig.empty());
    }

    public static WurstBuildConfig fromWorkspaceRoot(WFile workspaceRoot) {
        if (workspaceRoot == null) {
            return empty();
        }
        return fromBuildFile(Path.of(workspaceRoot.toString(), FILE_NAME));
    }

    public static WurstBuildConfig fromProject(WurstProjectConfigData projectConfig, WFile workspaceRoot) {
        WurstBuildConfig fileConfig = fromWorkspaceRoot(workspaceRoot);
        if (projectConfig == null) {
            return fileConfig;
        }
        Optional<org.wurstscript.projectconfig.ScriptMode> scriptMode = readStringGetter(projectConfig, "getScriptMode")
            .flatMap(WurstBuildConfig::parseSharedScriptMode)
            .or(fileConfig.sharedConfig::scriptMode);
        Optional<Wc3PatchTarget> wc3Patch = readStringGetter(projectConfig, "getWc3Patch")
            .flatMap(Wc3PatchTarget::parse)
            .or(fileConfig.sharedConfig::wc3Patch);
        return new WurstBuildConfig(new org.wurstscript.projectconfig.WurstBuildConfig(scriptMode, wc3Patch));
    }

    static WurstBuildConfig fromBuildFile(Path buildFile) {
        try {
            return new WurstBuildConfig(org.wurstscript.projectconfig.WurstBuildConfig.fromBuildFile(buildFile));
        } catch (IOException e) {
            WLogger.warning("Could not read " + buildFile + " for build settings", e);
            return empty();
        }
    }

    public Optional<ScriptMode> scriptMode() {
        return sharedConfig.scriptMode().map(mode -> ScriptMode.valueOf(mode.name()));
    }

    public Optional<Wc3Patch> wc3Patch() {
        return sharedConfig.wc3Patch().map(WurstBuildConfig::patchKind);
    }

    public Optional<String> wc3PatchName() {
        return sharedConfig.wc3Patch().map(Wc3PatchTarget::name);
    }

    public Optional<GameVersion> configuredGameVersion() {
        return sharedConfig.wc3Patch()
            .map(Wc3PatchTarget::gameVersion)
            .map(GameVersion::new);
    }

    public Wc3Patch wc3PatchOrReforged() {
        return wc3Patch().orElse(Wc3Patch.REFORGED);
    }

    /**
     * Whether the configured target is a patch before 1.24. These legacy patches ship
     * Blizzard common.j/blizzard.j with return-type mismatches the Jass VM tolerates,
     * so Jass type checks are relaxed and PJass is skipped for them.
     */
    public boolean isPre124() {
        return configuredGameVersion()
            .map(version -> version.compareTo(new GameVersion("1.24")) < 0)
            .orElse(false);
    }

    public GameVersion fallbackGameVersion() {
        return configuredGameVersion().orElse(GameVersion.VERSION_1_32);
    }

    public List<String> applyToCompileArgs(List<String> compileArgs) {
        return sharedConfig.applyToCompileArgs(compileArgs);
    }

    public boolean shouldUseReforgedLaunchArgs(Optional<GameVersion> detectedVersion) {
        return sharedConfig.shouldUseReforgedLaunchArgs(versionString(detectedVersion));
    }

    public boolean shouldUseClassicWindowArg(Optional<GameVersion> detectedVersion) {
        return sharedConfig.shouldUseClassicWindowArg(versionString(detectedVersion));
    }

    public boolean shouldCopyRunMapToWarcraftMapDir(Optional<GameVersion> detectedVersion) {
        return sharedConfig.shouldCopyRunMapToWarcraftMapDir(versionString(detectedVersion));
    }

    public boolean shouldUseInstallDirForMaps(Optional<GameVersion> detectedVersion) {
        Optional<GameVersion> effectiveVersion = detectedVersion == null ? Optional.empty() : detectedVersion;
        return effectiveVersion.or(this::configuredGameVersion)
            .map(version -> version.compareTo(new GameVersion("1.27.9")) <= 0)
            .orElse(false);
    }

    private static Optional<String> versionString(Optional<GameVersion> version) {
        return version == null ? Optional.empty() : version.map(GameVersion::toString);
    }

    private static Optional<org.wurstscript.projectconfig.ScriptMode> parseSharedScriptMode(String value) {
        try {
            return Optional.of(org.wurstscript.projectconfig.ScriptMode.valueOf(value.trim().toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }

    private static Wc3Patch patchKind(Wc3PatchTarget target) {
        if (target.kind() == Wc3PatchTarget.Kind.PRE_129) {
            return Wc3Patch.PRE_129;
        }
        if (target.kind() == Wc3PatchTarget.Kind.CLASSIC) {
            return Wc3Patch.CLASSIC;
        }
        return Wc3Patch.REFORGED;
    }

    private static Optional<String> readStringGetter(WurstProjectConfigData projectConfig, String getterName) {
        try {
            Method getter = projectConfig.getClass().getMethod(getterName);
            Object value = getter.invoke(projectConfig);
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of(value.toString());
        } catch (NoSuchMethodException ignored) {
            return Optional.empty();
        } catch (Exception e) {
            WLogger.debug("Could not read " + getterName + " from wurst.build config: " + e);
            return Optional.empty();
        }
    }
}
