package de.peeeq.wurstscript.validation.symbols;

import com.google.common.collect.ImmutableList;

import java.util.Objects;

public interface TypeSymbol {


    final class ScopedType implements TypeSymbol {
        private final boolean isStatic;
        private final PackagePath path;
        private final TypeName name;
        private final ImmutableList<TypeSymbol> typeArgs;

        public ScopedType(boolean isStatic, PackagePath path, TypeName name, ImmutableList<TypeSymbol> typeArgs) {
            this.isStatic = isStatic;
            this.path = path;
            this.name = name;
            this.typeArgs = typeArgs;
        }

        public PackagePath path() {
            return path;
        }

        public TypeName name() {
            return name;
        }

        public ImmutableList<TypeSymbol> typeArgs() {
            return typeArgs;
        }

        public boolean isStatic() {
            return isStatic;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            ScopedType that = (ScopedType) obj;
            return this.isStatic == that.isStatic &&
                Objects.equals(this.path, that.path) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.typeArgs, that.typeArgs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(isStatic, path, name, typeArgs);
        }

        @Override
        public String toString() {
            return "ScopedType[" +
                "path=" + path + ", " +
                "name=" + name + ", " +
                "typeArgs=" + typeArgs + ']';
        }


    }

    final class TypeParamRef implements TypeSymbol {
        private final String name;

        public TypeParamRef(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            TypeParamRef that = (TypeParamRef) obj;
            return Objects.equals(this.name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return "TypeParamRef[" +
                "name=" + name + ']';
        }


    }




}
