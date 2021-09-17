package de.peeeq.wurstscript.validation.symbols;

import com.google.common.collect.ImmutableList;

import java.util.Objects;

public final class PackagePath {
    private final ImmutableList<String> path;

    public PackagePath(ImmutableList<String> path) {
        this.path = path;
    }

    public PackagePath(String ... path) {
        this(ImmutableList.copyOf(path));
    }

    public ImmutableList<String> path() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        PackagePath that = (PackagePath) obj;
        return Objects.equals(this.path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return "PackagePath[" +
            "path=" + path + ']';
    }

}
