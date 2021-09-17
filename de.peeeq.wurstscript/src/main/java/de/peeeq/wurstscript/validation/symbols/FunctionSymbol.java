package de.peeeq.wurstscript.validation.symbols;

import java.util.List;
import java.util.Objects;

public class FunctionSymbol {
    private final List<String> typeVarNames;
    private final String name;
    private final ParameterInfo parameters;
    private final TypeSymbol returnType;

    public FunctionSymbol(List<String> typeVarNames, String name, ParameterInfo parameters, TypeSymbol returnType) {
        this.typeVarNames = typeVarNames;
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
    }

    public List<String> getTypeVarNames() {
        return typeVarNames;
    }

    public String getName() {
        return name;
    }

    public ParameterInfo getParameters() {
        return parameters;
    }

    public TypeSymbol getReturnType() {
        return returnType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionSymbol that = (FunctionSymbol) o;
        return Objects.equals(typeVarNames, that.typeVarNames) && Objects.equals(name, that.name) && Objects.equals(parameters, that.parameters) && Objects.equals(returnType, that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeVarNames, name, parameters, returnType);
    }

    static class ParameterInfo {
        private final boolean isVararg;
        private final List<Param> parameters;

        public ParameterInfo(boolean isVararg, List<Param> parameters) {
            this.isVararg = isVararg;
            this.parameters = parameters;
        }

        public boolean isVararg() {
            return isVararg;
        }

        public List<Param> getParameters() {
            return parameters;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParameterInfo that = (ParameterInfo) o;
            return isVararg == that.isVararg && Objects.equals(parameters, that.parameters);
        }

        @Override
        public int hashCode() {
            return Objects.hash(isVararg, parameters);
        }

        static class Param {
            private final TypeSymbol type;
            private final String name;

            public Param(TypeSymbol type, String name) {
                this.type = type;
                this.name = name;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Param param = (Param) o;
                return Objects.equals(type, param.type) && Objects.equals(name, param.name);
            }

            @Override
            public int hashCode() {
                return Objects.hash(type, name);
            }

            public TypeSymbol getType() {
                return type;
            }

            public String getName() {
                return name;
            }
        }
    }

}
