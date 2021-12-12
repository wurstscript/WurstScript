package de.peeeq.wurstscript.project.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Metadata(mv = {1, 6, 0}, k = 1, xi = 48, d1 = {"\000D\n\002\030\002\n\002\020\000\n\000\n\002\020\016\n\002\b\003\n\002\030\002\n\000\n\002\030\002\n\000\n\002\030\002\n\002\030\002\n\002\030\002\n\000\n\002\030\002\n\002\b\025\n\002\020\013\n\002\b\002\n\002\020\b\n\002\b\002\b\b\030\0002\0020\001Bk\022\b\b\002\020\002\032\0020\003\022\b\b\002\020\004\032\0020\003\022\b\b\002\020\005\032\0020\003\022\b\b\002\020\006\032\0020\007\022\b\b\002\020\b\032\0020\t\022\030\b\002\020\n\032\022\022\004\022\0020\f0\013j\b\022\004\022\0020\f`\r\022\030\b\002\020\016\032\022\022\004\022\0020\0170\013j\b\022\004\022\0020\017`\r\006\002\020\020J\t\020\034\032\0020\003H\003J\t\020\035\032\0020\003H\003J\t\020\036\032\0020\003H\003J\t\020\037\032\0020\007H\003J\t\020 \032\0020\tH\003J\031\020!\032\022\022\004\022\0020\f0\013j\b\022\004\022\0020\f`\rH\003J\031\020\"\032\022\022\004\022\0020\0170\013j\b\022\004\022\0020\017`\rH\003Jo\020#\032\0020\0002\b\b\002\020\002\032\0020\0032\b\b\002\020\004\032\0020\0032\b\b\002\020\005\032\0020\0032\b\b\002\020\006\032\0020\0072\b\b\002\020\b\032\0020\t2\030\b\002\020\n\032\022\022\004\022\0020\f0\013j\b\022\004\022\0020\f`\r2\030\b\002\020\016\032\022\022\004\022\0020\0170\013j\b\022\004\022\0020\017`\rH\001J\023\020$\032\0020%2\b\020&\032\004\030\0010\001H\003J\t\020'\032\0020(H\001J\t\020)\032\0020\003H\001R\021\020\005\032\0020\003\006\b\n\000\032\004\b\021\020\022R\021\020\004\032\0020\003\006\b\n\000\032\004\b\023\020\022R!\020\016\032\022\022\004\022\0020\0170\013j\b\022\004\022\0020\017`\r\006\b\n\000\032\004\b\024\020\025R\021\020\002\032\0020\003\006\b\n\000\032\004\b\026\020\022R\021\020\b\032\0020\t\006\b\n\000\032\004\b\027\020\030R!\020\n\032\022\022\004\022\0020\f0\013j\b\022\004\022\0020\f`\r\006\b\n\000\032\004\b\031\020\025R\021\020\006\032\0020\007\006\b\n\000\032\004\b\032\020\033\006*"}, d2 = {"Lde/peeeq/wurstscript/project/config/WurstProjectBuildMapData;", "", "name", "", "fileName", "author", "scenarioData", "Lde/peeeq/wurstscript/project/config/WurstProjectBuildScenarioData;", "optionsFlags", "Lde/peeeq/wurstscript/project/config/WurstProjectBuildOptionFlagsData;", "players", "Ljava/util/ArrayList;", "Lde/peeeq/wurstscript/project/config/WurstProjectBuildPlayer;", "Lkotlin/collections/ArrayList;", "forces", "Lde/peeeq/wurstscript/project/config/WurstProjectBuildForce;", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lde/peeeq/wurstscript/project/config/WurstProjectBuildScenarioData;Lde/peeeq/wurstscript/project/config/WurstProjectBuildOptionFlagsData;Ljava/util/ArrayList;Ljava/util/ArrayList;)V", "getAuthor", "()Ljava/lang/String;", "getFileName", "getForces", "()Ljava/util/ArrayList;", "getName", "getOptionsFlags", "()Lde/peeeq/wurstscript/project/config/WurstProjectBuildOptionFlagsData;", "getPlayers", "getScenarioData", "()Lde/peeeq/wurstscript/project/config/WurstProjectBuildScenarioData;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "", "other", "hashCode", "", "toString", "wurstscript"})
public final class WurstProjectBuildMapData {
    @NotNull
    private final String name;

    @NotNull
    private final String fileName;

    @NotNull
    private final String author;

    @NotNull
    private final WurstProjectBuildScenarioData scenarioData;

    @NotNull
    private final WurstProjectBuildOptionFlagsData optionsFlags;

    @NotNull
    private final ArrayList<WurstProjectBuildPlayer> players;

    @NotNull
    private final ArrayList<WurstProjectBuildForce> forces;

    public WurstProjectBuildMapData(@NotNull String name, @NotNull String fileName, @NotNull String author, @NotNull WurstProjectBuildScenarioData scenarioData, @NotNull WurstProjectBuildOptionFlagsData optionsFlags, @NotNull ArrayList<WurstProjectBuildPlayer> players, @NotNull ArrayList<WurstProjectBuildForce> forces) {
        this.name = name;
        this.fileName = fileName;
        this.author = author;
        this.scenarioData = scenarioData;
        this.optionsFlags = optionsFlags;
        this.players = players;
        this.forces = forces;
    }

    public WurstProjectBuildMapData() {
        this(null, null, null, null, null, null, null);
    }

    @NotNull
    public final String getName() {
        return this.name;
    }

    @NotNull
    public final String getFileName() {
        return this.fileName;
    }

    @NotNull
    public final String getAuthor() {
        return this.author;
    }

    @NotNull
    public final WurstProjectBuildScenarioData getScenarioData() {
        return this.scenarioData;
    }

    @NotNull
    public final WurstProjectBuildOptionFlagsData getOptionsFlags() {
        return this.optionsFlags;
    }

    @NotNull
    public final ArrayList<WurstProjectBuildPlayer> getPlayers() {
        return this.players;
    }

    @NotNull
    public final ArrayList<WurstProjectBuildForce> getForces() {
        return this.forces;
    }

    @NotNull
    public final String component1() {
        return this.name;
    }

    @NotNull
    public final String component2() {
        return this.fileName;
    }

    @NotNull
    public final String component3() {
        return this.author;
    }

    @NotNull
    public final WurstProjectBuildScenarioData component4() {
        return this.scenarioData;
    }

    @NotNull
    public final WurstProjectBuildOptionFlagsData component5() {
        return this.optionsFlags;
    }

    @NotNull
    public final ArrayList<WurstProjectBuildPlayer> component6() {
        return this.players;
    }

    @NotNull
    public final ArrayList<WurstProjectBuildForce> component7() {
        return this.forces;
    }

    @NotNull
    public final WurstProjectBuildMapData copy(@NotNull String name, @NotNull String fileName, @NotNull String author, @NotNull WurstProjectBuildScenarioData scenarioData, @NotNull WurstProjectBuildOptionFlagsData optionsFlags, @NotNull ArrayList<WurstProjectBuildPlayer> players, @NotNull ArrayList<WurstProjectBuildForce> forces) {
        Intrinsics.checkNotNullParameter(name, "name");
        Intrinsics.checkNotNullParameter(fileName, "fileName");
        Intrinsics.checkNotNullParameter(author, "author");
        Intrinsics.checkNotNullParameter(scenarioData, "scenarioData");
        Intrinsics.checkNotNullParameter(optionsFlags, "optionsFlags");
        Intrinsics.checkNotNullParameter(players, "players");
        Intrinsics.checkNotNullParameter(forces, "forces");
        return new WurstProjectBuildMapData(name, fileName, author, scenarioData, optionsFlags, players, forces);
    }

    @NotNull
    public String toString() {
        return "WurstProjectBuildMapData(name=" + this.name + ", fileName=" + this.fileName + ", author=" + this.author + ", scenarioData=" + this.scenarioData + ", optionsFlags=" + this.optionsFlags + ", players=" + this.players + ", forces=" + this.forces + ")";
    }

    public int hashCode() {
        int result = this.name.hashCode();
        result = result * 31 + this.fileName.hashCode();
        result = result * 31 + this.author.hashCode();
        result = result * 31 + this.scenarioData.hashCode();
        result = result * 31 + this.optionsFlags.hashCode();
        result = result * 31 + this.players.hashCode();
        return result * 31 + this.forces.hashCode();
    }

    public boolean equals(@Nullable Object other) {
        if (this == other)
            return true;
        if (!(other instanceof WurstProjectBuildMapData))
            return false;
        WurstProjectBuildMapData wurstProjectBuildMapData = (WurstProjectBuildMapData) other;
        return !Intrinsics.areEqual(this.name, wurstProjectBuildMapData.name) ? false : (!Intrinsics.areEqual(this.fileName, wurstProjectBuildMapData.fileName) ? false : (!Intrinsics.areEqual(this.author, wurstProjectBuildMapData.author) ? false : (!Intrinsics.areEqual(this.scenarioData, wurstProjectBuildMapData.scenarioData) ? false : (!Intrinsics.areEqual(this.optionsFlags, wurstProjectBuildMapData.optionsFlags) ? false : (!Intrinsics.areEqual(this.players, wurstProjectBuildMapData.players) ? false : (!!Intrinsics.areEqual(this.forces, wurstProjectBuildMapData.forces)))))));
    }
}
