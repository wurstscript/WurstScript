package de.peeeq.wurstscript.project.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Metadata(mv = {1, 6, 0}, k = 1, xi = 48, d1 = {"\000\"\n\002\030\002\n\002\020\000\n\000\n\002\020\016\n\002\b\022\n\002\020\013\n\002\b\002\n\002\020\b\n\002\b\002\b\b\030\0002\0020\001B7\022\b\b\002\020\002\032\0020\003\022\b\b\002\020\004\032\0020\003\022\b\b\002\020\005\032\0020\003\022\b\b\002\020\006\032\0020\003\022\b\b\002\020\007\032\0020\003\006\002\020\bJ\t\020\017\032\0020\003H\003J\t\020\020\032\0020\003H\003J\t\020\021\032\0020\003H\003J\t\020\022\032\0020\003H\003J\t\020\023\032\0020\003H\003J;\020\024\032\0020\0002\b\b\002\020\002\032\0020\0032\b\b\002\020\004\032\0020\0032\b\b\002\020\005\032\0020\0032\b\b\002\020\006\032\0020\0032\b\b\002\020\007\032\0020\003H\001J\023\020\025\032\0020\0262\b\020\027\032\004\030\0010\001H\003J\t\020\030\032\0020\031H\001J\t\020\032\032\0020\003H\001R\021\020\004\032\0020\003\006\b\n\000\032\004\b\t\020\nR\021\020\002\032\0020\003\006\b\n\000\032\004\b\013\020\nR\021\020\006\032\0020\003\006\b\n\000\032\004\b\f\020\nR\021\020\007\032\0020\003\006\b\n\000\032\004\b\r\020\nR\021\020\005\032\0020\003\006\b\n\000\032\004\b\016\020\n\006\033"}, d2 = {"Lde/peeeq/wurstscript/project/config/WurstProjectBuildLoadingScreenData;", "", "model", "", "background", "title", "subTitle", "text", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getBackground", "()Ljava/lang/String;", "getModel", "getSubTitle", "getText", "getTitle", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "", "other", "hashCode", "", "toString", "wurstscript"})
public final class WurstProjectBuildLoadingScreenData {
    @NotNull
    private final String model;

    @NotNull
    private final String background;

    @NotNull
    private final String title;

    @NotNull
    private final String subTitle;

    @NotNull
    private final String text;

    public WurstProjectBuildLoadingScreenData(@NotNull String model, @NotNull String background, @NotNull String title, @NotNull String subTitle, @NotNull String text) {
        this.model = model;
        this.background = background;
        this.title = title;
        this.subTitle = subTitle;
        this.text = text;
    }

    public WurstProjectBuildLoadingScreenData() {
        this(null, null, null, null, null);
    }

    @NotNull
    public final String getModel() {
        return this.model;
    }

    @NotNull
    public final String getBackground() {
        return this.background;
    }

    @NotNull
    public final String getTitle() {
        return this.title;
    }

    @NotNull
    public final String getSubTitle() {
        return this.subTitle;
    }

    @NotNull
    public final String getText() {
        return this.text;
    }

    @NotNull
    public final String component1() {
        return this.model;
    }

    @NotNull
    public final String component2() {
        return this.background;
    }

    @NotNull
    public final String component3() {
        return this.title;
    }

    @NotNull
    public final String component4() {
        return this.subTitle;
    }

    @NotNull
    public final String component5() {
        return this.text;
    }

    @NotNull
    public final WurstProjectBuildLoadingScreenData copy(@NotNull String model, @NotNull String background, @NotNull String title, @NotNull String subTitle, @NotNull String text) {
        Intrinsics.checkNotNullParameter(model, "model");
        Intrinsics.checkNotNullParameter(background, "background");
        Intrinsics.checkNotNullParameter(title, "title");
        Intrinsics.checkNotNullParameter(subTitle, "subTitle");
        Intrinsics.checkNotNullParameter(text, "text");
        return new WurstProjectBuildLoadingScreenData(model, background, title, subTitle, text);
    }

    @NotNull
    public String toString() {
        return "WurstProjectBuildLoadingScreenData(model=" + this.model + ", background=" + this.background + ", title=" + this.title + ", subTitle=" + this.subTitle + ", text=" + this.text + ")";
    }

    public int hashCode() {
        int result = this.model.hashCode();
        result = result * 31 + this.background.hashCode();
        result = result * 31 + this.title.hashCode();
        result = result * 31 + this.subTitle.hashCode();
        return result * 31 + this.text.hashCode();
    }

    public boolean equals(@Nullable Object other) {
        if (this == other)
            return true;
        if (!(other instanceof WurstProjectBuildLoadingScreenData))
            return false;
        WurstProjectBuildLoadingScreenData wurstProjectBuildLoadingScreenData = (WurstProjectBuildLoadingScreenData) other;
        return !Intrinsics.areEqual(this.model, wurstProjectBuildLoadingScreenData.model) ? false : (!Intrinsics.areEqual(this.background, wurstProjectBuildLoadingScreenData.background) ? false : (!Intrinsics.areEqual(this.title, wurstProjectBuildLoadingScreenData.title) ? false : (!Intrinsics.areEqual(this.subTitle, wurstProjectBuildLoadingScreenData.subTitle) ? false : (!!Intrinsics.areEqual(this.text, wurstProjectBuildLoadingScreenData.text)))));
    }
}
