package de.peeeq.wurstscript.validation.symbols;

public class VarSymbol {
    private final String name;
    private final TypeSymbol symbol;

    public VarSymbol(String name, TypeSymbol symbol) {
        this.name = name;
        this.symbol = symbol;
    }
}
