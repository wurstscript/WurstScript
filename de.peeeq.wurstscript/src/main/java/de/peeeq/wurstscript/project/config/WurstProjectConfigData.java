package de.peeeq.wurstscript.project.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@Metadata(mv = {1, 6, 0}, k = 1, xi = 48, d1 = {"\0002\n\002\030\002\n\002\020\000\n\000\n\002\020\016\n\000\n\002\030\002\n\002\030\002\n\000\n\002\030\002\n\002\b\016\n\002\020\013\n\002\b\002\n\002\020\b\n\002\b\002\b\b\030\0002\0020\001B1\022\006\020\002\032\0020\003\022\030\b\002\020\004\032\022\022\004\022\0020\0030\005j\b\022\004\022\0020\003`\006\022\b\b\002\020\007\032\0020\b\006\002\020\tJ\t\020\022\032\0020\003H\003J\031\020\023\032\022\022\004\022\0020\0030\005j\b\022\004\022\0020\003`\006H\003J\t\020\024\032\0020\bH\003J7\020\025\032\0020\0002\b\b\002\020\002\032\0020\0032\030\b\002\020\004\032\022\022\004\022\0020\0030\005j\b\022\004\022\0020\003`\0062\b\b\002\020\007\032\0020\bH\001J\023\020\026\032\0020\0272\b\020\030\032\004\030\0010\001H\003J\t\020\031\032\0020\032H\001J\t\020\033\032\0020\003H\001R\021\020\007\032\0020\b\006\b\n\000\032\004\b\n\020\013R!\020\004\032\022\022\004\022\0020\0030\005j\b\022\004\022\0020\003`\006\006\b\n\000\032\004\b\f\020\rR\032\020\002\032\0020\003X\016\006\016\n\000\032\004\b\016\020\017\"\004\b\020\020\021\006\034"}, d2 = {"Lde/peeeq/wurstscript/project/config/WurstProjectConfigData;", "", "projectName", "", "dependencies", "Ljava/util/ArrayList;", "Lkotlin/collections/ArrayList;", "buildMapData", "Lde/peeeq/wurstscript/project/config/WurstProjectBuildMapData;", "(Ljava/lang/String;Ljava/util/ArrayList;Lde/peeeq/wurstscript/project/config/WurstProjectBuildMapData;)V", "getBuildMapData", "()Lde/peeeq/wurstscript/project/config/WurstProjectBuildMapData;", "getDependencies", "()Ljava/util/ArrayList;", "getProjectName", "()Ljava/lang/String;", "setProjectName", "(Ljava/lang/String;)V", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "wurstscript"})
public final class WurstProjectConfigData {
    @NotNull
    private final ArrayList<String> dependencies;
    @NotNull
    private final WurstProjectBuildMapData buildMapData;
    @NotNull
    private String projectName;

    public WurstProjectConfigData(@NotNull String projectName, @NotNull ArrayList<String> dependencies, @NotNull WurstProjectBuildMapData buildMapData) {
        this.projectName = projectName;
        this.dependencies = dependencies;
        this.buildMapData = buildMapData;
    }

    @NotNull
    public final String getProjectName() {
        return this.projectName;
    }

    public final void setProjectName(@NotNull String name) {
        this.projectName = name;
    }

    @NotNull
    public final ArrayList<String> getDependencies() {
        return this.dependencies;
    }

    @NotNull
    public final WurstProjectBuildMapData getBuildMapData() {
        return this.buildMapData;
    }

    @NotNull
    public final String component1() {
        return this.projectName;
    }

    @NotNull
    public final ArrayList<String> component2() {
        return this.dependencies;
    }

    @NotNull
    public final WurstProjectBuildMapData component3() {
        return this.buildMapData;
    }

    @NotNull
    public final WurstProjectConfigData copy(@NotNull String projectName, @NotNull ArrayList<String> dependencies, @NotNull WurstProjectBuildMapData buildMapData) {
        Intrinsics.checkNotNullParameter(projectName, "projectName");
        Intrinsics.checkNotNullParameter(dependencies, "dependencies");
        Intrinsics.checkNotNullParameter(buildMapData, "buildMapData");
        return new WurstProjectConfigData(projectName, dependencies, buildMapData);
    }

    @NotNull
    public String toString() {
        return "WurstProjectConfigData(projectName=" + this.projectName + ", dependencies=" + this.dependencies + ", buildMapData=" + this.buildMapData + ")";
    }

    public int hashCode() {
        int result = this.projectName.hashCode();
        result = result * 31 + this.dependencies.hashCode();
        return result * 31 + this.buildMapData.hashCode();
    }

    public boolean equals(@Nullable Object other) {
        if (this == other)
            return true;
        if (!(other instanceof WurstProjectConfigData))
            return false;
        WurstProjectConfigData wurstProjectConfigData = (WurstProjectConfigData) other;
        return !Intrinsics.areEqual(this.projectName, wurstProjectConfigData.projectName) ? false : (!Intrinsics.areEqual(this.dependencies, wurstProjectConfigData.dependencies) ? false : (!!Intrinsics.areEqual(this.buildMapData, wurstProjectConfigData.buildMapData)));
    }
}
