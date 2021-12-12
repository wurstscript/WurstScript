package de.peeeq.wurstscript.project.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import kotlin.Metadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Metadata(mv = {1, 6, 0}, k = 1, xi = 48, d1 = {"\000\036\n\002\030\002\n\002\020\000\n\000\n\002\020\013\n\002\b\027\n\002\020\b\n\000\n\002\020\016\n\000\b\b\030\0002\0020\001BA\022\b\b\002\020\002\032\0020\003\022\b\b\002\020\004\032\0020\003\022\b\b\002\020\005\032\0020\003\022\b\b\002\020\006\032\0020\003\022\b\b\002\020\007\032\0020\003\022\b\b\002\020\b\032\0020\003\006\002\020\tJ\t\020\021\032\0020\003H\003J\t\020\022\032\0020\003H\003J\t\020\023\032\0020\003H\003J\t\020\024\032\0020\003H\003J\t\020\025\032\0020\003H\003J\t\020\026\032\0020\003H\003JE\020\027\032\0020\0002\b\b\002\020\002\032\0020\0032\b\b\002\020\004\032\0020\0032\b\b\002\020\005\032\0020\0032\b\b\002\020\006\032\0020\0032\b\b\002\020\007\032\0020\0032\b\b\002\020\b\032\0020\003H\001J\023\020\030\032\0020\0032\b\020\031\032\004\030\0010\001H\003J\t\020\032\032\0020\033H\001J\t\020\034\032\0020\035H\001R\021\020\004\032\0020\003\006\b\n\000\032\004\b\n\020\013R\021\020\002\032\0020\003\006\b\n\000\032\004\b\f\020\013R\021\020\005\032\0020\003\006\b\n\000\032\004\b\r\020\013R\021\020\006\032\0020\003\006\b\n\000\032\004\b\016\020\013R\021\020\007\032\0020\003\006\b\n\000\032\004\b\017\020\013R\021\020\b\032\0020\003\006\b\n\000\032\004\b\020\020\013\006\036"}, d2 = {"Lde/peeeq/wurstscript/project/config/WurstProjectBuildOptionFlagsData;", "", "hideMinimapPreview", "", "forcesFixed", "maskedAreasPartiallyVisible", "showWavesOnCliffShores", "showWavesOnRollingShores", "useItemClassificationSystem", "(ZZZZZZ)V", "getForcesFixed", "()Z", "getHideMinimapPreview", "getMaskedAreasPartiallyVisible", "getShowWavesOnCliffShores", "getShowWavesOnRollingShores", "getUseItemClassificationSystem", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "other", "hashCode", "", "toString", "", "wurstscript"})
public final class WurstProjectBuildOptionFlagsData {
    private final boolean hideMinimapPreview;

    private final boolean forcesFixed;

    private final boolean maskedAreasPartiallyVisible;

    private final boolean showWavesOnCliffShores;

    private final boolean showWavesOnRollingShores;

    private final boolean useItemClassificationSystem;

    public WurstProjectBuildOptionFlagsData(boolean hideMinimapPreview, boolean forcesFixed, boolean maskedAreasPartiallyVisible, boolean showWavesOnCliffShores, boolean showWavesOnRollingShores, boolean useItemClassificationSystem) {
        this.hideMinimapPreview = hideMinimapPreview;
        this.forcesFixed = forcesFixed;
        this.maskedAreasPartiallyVisible = maskedAreasPartiallyVisible;
        this.showWavesOnCliffShores = showWavesOnCliffShores;
        this.showWavesOnRollingShores = showWavesOnRollingShores;
        this.useItemClassificationSystem = useItemClassificationSystem;
    }

    public WurstProjectBuildOptionFlagsData() {
        this(false, false, false, false, false, false);
    }

    public final boolean getHideMinimapPreview() {
        return this.hideMinimapPreview;
    }

    public final boolean getForcesFixed() {
        return this.forcesFixed;
    }

    public final boolean getMaskedAreasPartiallyVisible() {
        return this.maskedAreasPartiallyVisible;
    }

    public final boolean getShowWavesOnCliffShores() {
        return this.showWavesOnCliffShores;
    }

    public final boolean getShowWavesOnRollingShores() {
        return this.showWavesOnRollingShores;
    }

    public final boolean getUseItemClassificationSystem() {
        return this.useItemClassificationSystem;
    }

    public final boolean component1() {
        return this.hideMinimapPreview;
    }

    public final boolean component2() {
        return this.forcesFixed;
    }

    public final boolean component3() {
        return this.maskedAreasPartiallyVisible;
    }

    public final boolean component4() {
        return this.showWavesOnCliffShores;
    }

    public final boolean component5() {
        return this.showWavesOnRollingShores;
    }

    public final boolean component6() {
        return this.useItemClassificationSystem;
    }

    @NotNull
    public final WurstProjectBuildOptionFlagsData copy(boolean hideMinimapPreview, boolean forcesFixed, boolean maskedAreasPartiallyVisible, boolean showWavesOnCliffShores, boolean showWavesOnRollingShores, boolean useItemClassificationSystem) {
        return new WurstProjectBuildOptionFlagsData(hideMinimapPreview, forcesFixed, maskedAreasPartiallyVisible, showWavesOnCliffShores, showWavesOnRollingShores, useItemClassificationSystem);
    }

    @NotNull
    public String toString() {
        return "WurstProjectBuildOptionFlagsData(hideMinimapPreview=" + this.hideMinimapPreview + ", forcesFixed=" + this.forcesFixed + ", maskedAreasPartiallyVisible=" + this.maskedAreasPartiallyVisible + ", showWavesOnCliffShores=" + this.showWavesOnCliffShores + ", showWavesOnRollingShores=" + this.showWavesOnRollingShores + ", useItemClassificationSystem=" + this.useItemClassificationSystem + ")";
    }

    public int hashCode() {
        int result = 0;
        if (this.hideMinimapPreview)
            result = 1;
        if (this.forcesFixed)
            result = result * 31 + 1;
        if (this.maskedAreasPartiallyVisible)
            result = result * 31 + 1;
        if (this.showWavesOnCliffShores)
            result = result * 31 + 1;
        if (this.showWavesOnRollingShores)
            result = result * 31 + 1;
        if (this.useItemClassificationSystem)
            result = result * 31 + 1;
        return result;
    }

    public boolean equals(@Nullable Object other) {
        if (this == other)
            return true;
        if (!(other instanceof WurstProjectBuildOptionFlagsData))
            return false;
        WurstProjectBuildOptionFlagsData wurstProjectBuildOptionFlagsData = (WurstProjectBuildOptionFlagsData) other;
        return (this.hideMinimapPreview != wurstProjectBuildOptionFlagsData.hideMinimapPreview) ? false : ((this.forcesFixed != wurstProjectBuildOptionFlagsData.forcesFixed) ? false : ((this.maskedAreasPartiallyVisible != wurstProjectBuildOptionFlagsData.maskedAreasPartiallyVisible) ? false : ((this.showWavesOnCliffShores != wurstProjectBuildOptionFlagsData.showWavesOnCliffShores) ? false : ((this.showWavesOnRollingShores != wurstProjectBuildOptionFlagsData.showWavesOnRollingShores) ? false : (!(this.useItemClassificationSystem != wurstProjectBuildOptionFlagsData.useItemClassificationSystem))))));
    }
}
