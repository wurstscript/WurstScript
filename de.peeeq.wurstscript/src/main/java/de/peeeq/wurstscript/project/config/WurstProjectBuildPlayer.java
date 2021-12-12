package de.peeeq.wurstscript.project.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@Metadata(mv = {1, 6, 0}, k = 1, xi = 48, d1 = {"\000*\n\002\030\002\n\002\020\000\n\000\n\002\020\b\n\000\n\002\020\016\n\000\n\002\030\002\n\000\n\002\030\002\n\000\n\002\020\013\n\002\b\030\b\b\030\0002\0020\001B5\022\006\020\002\032\0020\003\022\b\020\004\032\004\030\0010\005\022\b\020\006\032\004\030\0010\007\022\b\020\b\032\004\030\0010\t\022\b\020\n\032\004\030\0010\013\006\002\020\fJ\t\020\030\032\0020\003H\003J\013\020\031\032\004\030\0010\005H\003J\013\020\032\032\004\030\0010\007H\003J\013\020\033\032\004\030\0010\tH\003J\020\020\034\032\004\030\0010\013H\003\006\002\020\020JH\020\035\032\0020\0002\b\b\002\020\002\032\0020\0032\n\b\002\020\004\032\004\030\0010\0052\n\b\002\020\006\032\004\030\0010\0072\n\b\002\020\b\032\004\030\0010\t2\n\b\002\020\n\032\004\030\0010\013H\001\006\002\020\036J\023\020\037\032\0020\0132\b\020 \032\004\030\0010\001H\003J\t\020!\032\0020\003H\001J\t\020\"\032\0020\005H\001R\023\020\b\032\004\030\0010\t\006\b\n\000\032\004\b\r\020\016R\025\020\n\032\004\030\0010\013\006\n\n\002\020\021\032\004\b\017\020\020R\021\020\002\032\0020\003\006\b\n\000\032\004\b\022\020\023R\023\020\004\032\004\030\0010\005\006\b\n\000\032\004\b\024\020\025R\023\020\006\032\004\030\0010\007\006\b\n\000\032\004\b\026\020\027\006#"}, d2 = {"Lde/peeeq/wurstscript/project/config/WurstProjectBuildPlayer;", "", "id", "", "name", "", "race", "Lde/peeeq/wurstscript/project/config/Race;", "controller", "Lde/peeeq/wurstscript/project/config/Controller;", "fixedStartLoc", "", "(ILjava/lang/String;Lde/peeeq/wurstscript/project/config/Race;Lde/peeeq/wurstscript/project/config/Controller;Ljava/lang/Boolean;)V", "getController", "()Lde/peeeq/wurstscript/project/config/Controller;", "getFixedStartLoc", "()Ljava/lang/Boolean;", "Ljava/lang/Boolean;", "getId", "()I", "getName", "()Ljava/lang/String;", "getRace", "()Lde/peeeq/wurstscript/project/config/Race;", "component1", "component2", "component3", "component4", "component5", "copy", "(ILjava/lang/String;Lde/peeeq/wurstscript/project/config/Race;Lde/peeeq/wurstscript/project/config/Controller;Ljava/lang/Boolean;)Lde/peeeq/wurstscript/project/config/WurstProjectBuildPlayer;", "equals", "other", "hashCode", "toString", "wurstscript"})
public final class WurstProjectBuildPlayer {
    private final int id;

    @Nullable
    private final String name;

    @Nullable
    private final Race race;

    @Nullable
    private final Controller controller;

    @Nullable
    private final Boolean fixedStartLoc;

    public WurstProjectBuildPlayer(int id, @Nullable String name, @Nullable Race race, @Nullable Controller controller, @Nullable Boolean fixedStartLoc) {
        this.id = id;
        this.name = name;
        this.race = race;
        this.controller = controller;
        this.fixedStartLoc = fixedStartLoc;
    }

    public final int getId() {
        return this.id;
    }

    @Nullable
    public final String getName() {
        return this.name;
    }

    @Nullable
    public final Race getRace() {
        return this.race;
    }

    @Nullable
    public final Controller getController() {
        return this.controller;
    }

    @Nullable
    public final Boolean getFixedStartLoc() {
        return this.fixedStartLoc;
    }

    public final int component1() {
        return this.id;
    }

    @Nullable
    public final String component2() {
        return this.name;
    }

    @Nullable
    public final Race component3() {
        return this.race;
    }

    @Nullable
    public final Controller component4() {
        return this.controller;
    }

    @Nullable
    public final Boolean component5() {
        return this.fixedStartLoc;
    }

    @NotNull
    public final WurstProjectBuildPlayer copy(int id, @Nullable String name, @Nullable Race race, @Nullable Controller controller, @Nullable Boolean fixedStartLoc) {
        return new WurstProjectBuildPlayer(id, name, race, controller, fixedStartLoc);
    }

    @NotNull
    public String toString() {
        return "WurstProjectBuildPlayer(id=" + this.id + ", name=" + this.name + ", race=" + this.race + ", controller=" + this.controller + ", fixedStartLoc=" + this.fixedStartLoc + ")";
    }

    public int hashCode() {
        int result = Integer.hashCode(this.id);
        result = result * 31 + ((this.name == null) ? 0 : this.name.hashCode());
        result = result * 31 + ((this.race == null) ? 0 : this.race.hashCode());
        result = result * 31 + ((this.controller == null) ? 0 : this.controller.hashCode());
        return result * 31 + ((this.fixedStartLoc == null) ? 0 : this.fixedStartLoc.hashCode());
    }

    public boolean equals(@Nullable Object other) {
        if (this == other)
            return true;
        if (!(other instanceof WurstProjectBuildPlayer))
            return false;
        WurstProjectBuildPlayer wurstProjectBuildPlayer = (WurstProjectBuildPlayer) other;
        return (this.id != wurstProjectBuildPlayer.id) ? false : (!Intrinsics.areEqual(this.name, wurstProjectBuildPlayer.name) ? false : ((this.race != wurstProjectBuildPlayer.race) ? false : ((this.controller != wurstProjectBuildPlayer.controller) ? false : (!!Intrinsics.areEqual(this.fixedStartLoc, wurstProjectBuildPlayer.fixedStartLoc)))));
    }
}
