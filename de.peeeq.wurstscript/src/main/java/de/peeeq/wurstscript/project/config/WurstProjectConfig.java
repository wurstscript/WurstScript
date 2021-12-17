package de.peeeq.wurstscript.project.config;

import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Metadata(mv = {1, 6, 0}, k = 1, xi = 48, d1 = {"\000\030\n\002\030\002\n\002\020\000\n\002\b\002\n\002\030\002\n\000\n\002\030\002\n\000\b\002\030\0002\0020\001B\007\b\002\006\002\020\002J\020\020\003\032\004\030\0010\0042\006\020\005\032\0020\006\006\007"}, d2 = {"Lde/peeeq/wurstscript/project/config/WurstProjectConfig;", "", "()V", "loadProject", "Lde/peeeq/wurstscript/project/config/WurstProjectConfigData;", "buildFile", "Ljava/nio/file/Path;", "wurstscript"})
public final class WurstProjectConfig {
    @NotNull
    public static final WurstProjectConfig INSTANCE = new WurstProjectConfig();

    @Nullable
    public final WurstProjectConfigData loadProject(@NotNull Path buildFile) throws IOException {
        Intrinsics.checkNotNullParameter(buildFile, "buildFile");
        if (Files.exists(buildFile) && StringsKt.equals(buildFile.getFileName().toString(), "wurst.build", true)) {
            WurstProjectConfigData config = YamlHelper.INSTANCE.loadProjectConfig(buildFile);
            Path projectRoot = buildFile.getParent();
            if ((((CharSequence) config.getProjectName()).length() == 0)) {
                config.setProjectName(String.valueOf((projectRoot == null) ? null : projectRoot.getFileName()));
            }
            return config;
        }
        return null;
    }
}
