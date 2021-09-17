package de.peeeq.wurstscript.validation.symbols;

import com.google.common.collect.Multimap;

import java.util.Map;

/**
 * A symbol table for a compilation unit
 *
 * A Wurst module can export the following elements:
 *
 * - Types
 *    - Tuples
 *    - Classes
 *    - Interfaces
 *    - Modules
 * - Variables
 * - Functions
 *
 */
public class PackageSymbolTable {
    private final PackagePath packageName;
    private final Map<TypeName, TypeDefSymbol> types;
    private final Multimap<String, FunctionSymbol> functions;
    private final Map<String, VarSymbol> vars;

    public PackageSymbolTable(PackagePath packageName, Map<TypeName, TypeDefSymbol> types, Multimap<String, FunctionSymbol> functions, Map<String, VarSymbol> vars) {
        this.packageName = packageName;
        this.types = types;
        this.functions = functions;
        this.vars = vars;
    }

    public PackagePath getPackageName() {
        return packageName;
    }

    public Map<TypeName, TypeDefSymbol> getTypes() {
        return types;
    }

    public Multimap<String, FunctionSymbol> getFunctions() {
        return functions;
    }

    public Map<String, VarSymbol> getVars() {
        return vars;
    }
}
