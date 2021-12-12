package de.peeeq.wurstscript.project.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import kotlin.Metadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Metadata(mv = {1, 6, 0}, k = 1, xi = 48, d1 = {"\000\036\n\002\030\002\n\002\020\000\n\000\n\002\020\013\n\002\b\024\n\002\020\b\n\000\n\002\020\016\n\000\b\b\030\0002\0020\001B7\022\b\b\002\020\002\032\0020\003\022\b\b\002\020\004\032\0020\003\022\b\b\002\020\005\032\0020\003\022\b\b\002\020\006\032\0020\003\022\b\b\002\020\007\032\0020\003\006\002\020\bJ\t\020\017\032\0020\003H\003J\t\020\020\032\0020\003H\003J\t\020\021\032\0020\003H\003J\t\020\022\032\0020\003H\003J\t\020\023\032\0020\003H\003J;\020\024\032\0020\0002\b\b\002\020\002\032\0020\0032\b\b\002\020\004\032\0020\0032\b\b\002\020\005\032\0020\0032\b\b\002\020\006\032\0020\0032\b\b\002\020\007\032\0020\003H\001J\023\020\025\032\0020\0032\b\020\026\032\004\030\0010\001H\003J\t\020\027\032\0020\030H\001J\t\020\031\032\0020\032H\001R\021\020\002\032\0020\003\006\b\n\000\032\004\b\t\020\nR\021\020\004\032\0020\003\006\b\n\000\032\004\b\013\020\nR\021\020\006\032\0020\003\006\b\n\000\032\004\b\f\020\nR\021\020\007\032\0020\003\006\b\n\000\032\004\b\r\020\nR\021\020\005\032\0020\003\006\b\n\000\032\004\b\016\020\n\006\033"}, d2 = {"Lde/peeeq/wurstscript/project/config/WurstProjectBuildForceFlags;", "", "allied", "", "alliedVictory", "sharedVision", "sharedControl", "sharedControlAdvanced", "(ZZZZZ)V", "getAllied", "()Z", "getAlliedVictory", "getSharedControl", "getSharedControlAdvanced", "getSharedVision", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "other", "hashCode", "", "toString", "", "wurstscript"})
public final class WurstProjectBuildForceFlags {
    private final boolean allied;

    private final boolean alliedVictory;

    private final boolean sharedVision;

    private final boolean sharedControl;

    private final boolean sharedControlAdvanced;

    public WurstProjectBuildForceFlags(boolean allied, boolean alliedVictory, boolean sharedVision, boolean sharedControl, boolean sharedControlAdvanced) {
        this.allied = allied;
        this.alliedVictory = alliedVictory;
        this.sharedVision = sharedVision;
        this.sharedControl = sharedControl;
        this.sharedControlAdvanced = sharedControlAdvanced;
    }

    public WurstProjectBuildForceFlags() {
        this(false, false, false, false, false);
    }

    public final boolean getAllied() {
        return this.allied;
    }

    public final boolean getAlliedVictory() {
        return this.alliedVictory;
    }

    public final boolean getSharedVision() {
        return this.sharedVision;
    }

    public final boolean getSharedControl() {
        return this.sharedControl;
    }

    public final boolean getSharedControlAdvanced() {
        return this.sharedControlAdvanced;
    }

    public final boolean component1() {
        return this.allied;
    }

    public final boolean component2() {
        return this.alliedVictory;
    }

    public final boolean component3() {
        return this.sharedVision;
    }

    public final boolean component4() {
        return this.sharedControl;
    }

    public final boolean component5() {
        return this.sharedControlAdvanced;
    }

    @NotNull
    public final WurstProjectBuildForceFlags copy(boolean allied, boolean alliedVictory, boolean sharedVision, boolean sharedControl, boolean sharedControlAdvanced) {
        return new WurstProjectBuildForceFlags(allied, alliedVictory, sharedVision, sharedControl, sharedControlAdvanced);
    }

    @NotNull
    public String toString() {
        return "WurstProjectBuildForceFlags(allied=" + this.allied + ", alliedVictory=" + this.alliedVictory + ", sharedVision=" + this.sharedVision + ", sharedControl=" + this.sharedControl + ", sharedControlAdvanced=" + this.sharedControlAdvanced + ")";
    }

    public int hashCode() {
        if (this.allied) ;
        int result = 1;
        if (this.alliedVictory) ;
        result = result * 31 + 1;
        if (this.sharedVision) ;
        result = result * 31 + 1;
        if (this.sharedControl) ;
        result = result * 31 + 1;
        if (this.sharedControlAdvanced) ;
        return result * 31 + 1;
    }

    public boolean equals(@Nullable Object other) {
        if (this == other)
            return true;
        if (!(other instanceof WurstProjectBuildForceFlags))
            return false;
        WurstProjectBuildForceFlags wurstProjectBuildForceFlags = (WurstProjectBuildForceFlags) other;
        return (this.allied != wurstProjectBuildForceFlags.allied) ? false : ((this.alliedVictory != wurstProjectBuildForceFlags.alliedVictory) ? false : ((this.sharedVision != wurstProjectBuildForceFlags.sharedVision) ? false : ((this.sharedControl != wurstProjectBuildForceFlags.sharedControl) ? false : (!(this.sharedControlAdvanced != wurstProjectBuildForceFlags.sharedControlAdvanced)))));
    }
}
