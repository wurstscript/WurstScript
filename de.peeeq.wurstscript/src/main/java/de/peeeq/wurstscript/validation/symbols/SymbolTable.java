package de.peeeq.wurstscript.validation.symbols;

import com.google.common.collect.Multimap;

import java.util.Map;

/**
 * A symbol table for a compilation unit
 */
public class SymbolTable {
    private final Map<TypeName, TypeSymbol> types;
    private final Multimap<String, FunctionSymbol> functions;
    private final Map<String, VarSymbol> vars;

    public SymbolTable(Map<TypeName, TypeSymbol> types, Multimap<String, FunctionSymbol> functions, Map<String, VarSymbol> vars) {
        this.types = types;
        this.functions = functions;
        this.vars = vars;
    }

    public Map<TypeName, TypeSymbol> getTypes() {
        return types;
    }

    public Multimap<String, FunctionSymbol> getFunctions() {
        return functions;
    }

    public Map<String, VarSymbol> getVars() {
        return vars;
    }
}
