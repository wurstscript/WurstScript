package de.peeeq.wurstscript.validation.symbols;

import java.util.List;
import java.util.Objects;

public class TypeDefSymbol {
    private final List<String> typeParameters;
    private final TypeSymbol symbol;
    private final List<TypeSymbol> superTypes;
    private final List<TypeSymbol> usedModules;

    public TypeDefSymbol(List<String> typeParameters, TypeSymbol symbol, List<TypeSymbol> superTypes, List<TypeSymbol> usedModules) {
        this.typeParameters = typeParameters;
        this.symbol = symbol;
        this.superTypes = superTypes;
        this.usedModules = usedModules;
    }

    public List<String> getTypeParameters() {
        return typeParameters;
    }

    public TypeSymbol getSymbol() {
        return symbol;
    }

    public List<TypeSymbol> getSuperTypes() {
        return superTypes;
    }

    public List<TypeSymbol> getUsedModules() {
        return usedModules;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeDefSymbol that = (TypeDefSymbol) o;
        return typeParameters.equals(that.typeParameters) && symbol.equals(that.symbol) && superTypes.equals(that.superTypes) && usedModules.equals(that.usedModules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeParameters, symbol, superTypes, usedModules);
    }
}
