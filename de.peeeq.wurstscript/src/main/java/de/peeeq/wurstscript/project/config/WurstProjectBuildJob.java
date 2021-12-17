package de.peeeq.wurstscript.project.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Metadata(mv = {1, 6, 0}, k = 1, xi = 48, d1 = {"\000,\n\002\030\002\n\002\020\000\n\000\n\002\020\016\n\000\n\002\030\002\n\002\030\002\n\002\b\t\n\002\020\013\n\002\b\002\n\002\020\b\n\002\b\002\b\b\030\0002\0020\001B)\022\b\b\002\020\002\032\0020\003\022\030\b\002\020\004\032\022\022\004\022\0020\0030\005j\b\022\004\022\0020\003`\006\006\002\020\007J\t\020\f\032\0020\003H\003J\031\020\r\032\022\022\004\022\0020\0030\005j\b\022\004\022\0020\003`\006H\003J-\020\016\032\0020\0002\b\b\002\020\002\032\0020\0032\030\b\002\020\004\032\022\022\004\022\0020\0030\005j\b\022\004\022\0020\003`\006H\001J\023\020\017\032\0020\0202\b\020\021\032\004\030\0010\001H\003J\t\020\022\032\0020\023H\001J\t\020\024\032\0020\003H\001R!\020\004\032\022\022\004\022\0020\0030\005j\b\022\004\022\0020\003`\006\006\b\n\000\032\004\b\b\020\tR\021\020\002\032\0020\003\006\b\n\000\032\004\b\n\020\013\006\025"}, d2 = {"Lde/peeeq/wurstscript/project/config/WurstProjectBuildJob;", "", "name", "", "args", "Ljava/util/ArrayList;", "Lkotlin/collections/ArrayList;", "(Ljava/lang/String;Ljava/util/ArrayList;)V", "getArgs", "()Ljava/util/ArrayList;", "getName", "()Ljava/lang/String;", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "wurstscript"})
public final class WurstProjectBuildJob {
    @NotNull
    private final String name;

    @NotNull
    private final ArrayList<String> args;

    public WurstProjectBuildJob(@NotNull String name, @NotNull ArrayList<String> args) {
        this.name = name;
        this.args = args;
    }

    public WurstProjectBuildJob() {
        this(null, null);
    }

    @NotNull
    public final String getName() {
        return this.name;
    }

    @NotNull
    public final ArrayList<String> getArgs() {
        return this.args;
    }

    @NotNull
    public final String component1() {
        return this.name;
    }

    @NotNull
    public final ArrayList<String> component2() {
        return this.args;
    }

    @NotNull
    public final WurstProjectBuildJob copy(@NotNull String name, @NotNull ArrayList<String> args) {
        Intrinsics.checkNotNullParameter(name, "name");
        Intrinsics.checkNotNullParameter(args, "args");
        return new WurstProjectBuildJob(name, args);
    }

    @NotNull
    public String toString() {
        return "WurstProjectBuildJob(name=" + this.name + ", args=" + this.args + ")";
    }

    public int hashCode() {
        int result = this.name.hashCode();
        return result * 31 + this.args.hashCode();
    }

    public boolean equals(@Nullable Object other) {
        if (this == other)
            return true;
        if (!(other instanceof WurstProjectBuildJob))
            return false;
        WurstProjectBuildJob wurstProjectBuildJob = (WurstProjectBuildJob) other;
        return !Intrinsics.areEqual(this.name, wurstProjectBuildJob.name) ? false : (!!Intrinsics.areEqual(this.args, wurstProjectBuildJob.args));
    }
}
