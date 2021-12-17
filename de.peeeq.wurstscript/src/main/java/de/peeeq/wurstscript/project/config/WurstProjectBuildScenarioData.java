package de.peeeq.wurstscript.project.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Metadata(mv = {1, 6, 0}, k = 1, xi = 48, d1 = {"\000*\n\002\030\002\n\002\020\000\n\000\n\002\020\016\n\002\b\002\n\002\030\002\n\002\b\r\n\002\020\013\n\002\b\002\n\002\020\b\n\002\b\002\b\b\030\0002\0020\001B#\022\b\b\002\020\002\032\0020\003\022\b\b\002\020\004\032\0020\003\022\b\020\005\032\004\030\0010\006\006\002\020\007J\t\020\017\032\0020\003H\003J\t\020\020\032\0020\003H\003J\013\020\021\032\004\030\0010\006H\003J)\020\022\032\0020\0002\b\b\002\020\002\032\0020\0032\b\b\002\020\004\032\0020\0032\n\b\002\020\005\032\004\030\0010\006H\001J\023\020\023\032\0020\0242\b\020\025\032\004\030\0010\001H\003J\t\020\026\032\0020\027H\001J\t\020\030\032\0020\003H\001R\021\020\002\032\0020\003\006\b\n\000\032\004\b\b\020\tR\034\020\005\032\004\030\0010\006X\016\006\016\n\000\032\004\b\n\020\013\"\004\b\f\020\rR\021\020\004\032\0020\003\006\b\n\000\032\004\b\016\020\t\006\031"}, d2 = {"Lde/peeeq/wurstscript/project/config/WurstProjectBuildScenarioData;", "", "description", "", "suggestedPlayers", "loadingScreen", "Lde/peeeq/wurstscript/project/config/WurstProjectBuildLoadingScreenData;", "(Ljava/lang/String;Ljava/lang/String;Lde/peeeq/wurstscript/project/config/WurstProjectBuildLoadingScreenData;)V", "getDescription", "()Ljava/lang/String;", "getLoadingScreen", "()Lde/peeeq/wurstscript/project/config/WurstProjectBuildLoadingScreenData;", "setLoadingScreen", "(Lde/peeeq/wurstscript/project/config/WurstProjectBuildLoadingScreenData;)V", "getSuggestedPlayers", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "wurstscript"})
public final class WurstProjectBuildScenarioData {
    @NotNull
    private final String description;

    @NotNull
    private final String suggestedPlayers;

    @Nullable
    private WurstProjectBuildLoadingScreenData loadingScreen;

    public WurstProjectBuildScenarioData(@NotNull String description, @NotNull String suggestedPlayers, @Nullable WurstProjectBuildLoadingScreenData loadingScreen) {
        this.description = description;
        this.suggestedPlayers = suggestedPlayers;
        this.loadingScreen = loadingScreen;
    }

    @NotNull
    public final String getDescription() {
        return this.description;
    }

    @NotNull
    public final String getSuggestedPlayers() {
        return this.suggestedPlayers;
    }

    @Nullable
    public final WurstProjectBuildLoadingScreenData getLoadingScreen() {
        return this.loadingScreen;
    }

    public final void setLoadingScreen(@Nullable WurstProjectBuildLoadingScreenData data) {
        this.loadingScreen = data;
    }

    @NotNull
    public final String component1() {
        return this.description;
    }

    @NotNull
    public final String component2() {
        return this.suggestedPlayers;
    }

    @Nullable
    public final WurstProjectBuildLoadingScreenData component3() {
        return this.loadingScreen;
    }

    @NotNull
    public final WurstProjectBuildScenarioData copy(@NotNull String description, @NotNull String suggestedPlayers, @Nullable WurstProjectBuildLoadingScreenData loadingScreen) {
        Intrinsics.checkNotNullParameter(description, "description");
        Intrinsics.checkNotNullParameter(suggestedPlayers, "suggestedPlayers");
        return new WurstProjectBuildScenarioData(description, suggestedPlayers, loadingScreen);
    }

    @NotNull
    public String toString() {
        return "WurstProjectBuildScenarioData(description=" + this.description + ", suggestedPlayers=" + this.suggestedPlayers + ", loadingScreen=" + this.loadingScreen + ")";
    }

    public int hashCode() {
        int result = this.description.hashCode();
        result = result * 31 + this.suggestedPlayers.hashCode();
        return result * 31 + ((this.loadingScreen == null) ? 0 : this.loadingScreen.hashCode());
    }

    public boolean equals(@Nullable Object other) {
        if (this == other)
            return true;
        if (!(other instanceof WurstProjectBuildScenarioData))
            return false;
        WurstProjectBuildScenarioData wurstProjectBuildScenarioData = (WurstProjectBuildScenarioData) other;
        return !Intrinsics.areEqual(this.description, wurstProjectBuildScenarioData.description) ? false : (!Intrinsics.areEqual(this.suggestedPlayers, wurstProjectBuildScenarioData.suggestedPlayers) ? false : (!!Intrinsics.areEqual(this.loadingScreen, wurstProjectBuildScenarioData.loadingScreen)));
    }
}
