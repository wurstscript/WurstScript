package de.peeeq.wurstscript.validation.symbols;

import com.google.common.collect.ImmutableList;

import java.util.Objects;

/** Type name inside a package (singleton list unless it is a nested class) */
public final class TypeName {
    private final ImmutableList<String> path;

    public TypeName(String path) {
        this(ImmutableList.of(path));
    }

    public TypeName(ImmutableList<String> path) {
        this.path = path;
    }

    public ImmutableList<String> path() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        TypeName that = (TypeName) obj;
        return Objects.equals(this.path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return "TypeName[" +
            "path=" + path + ']';
    }

}
