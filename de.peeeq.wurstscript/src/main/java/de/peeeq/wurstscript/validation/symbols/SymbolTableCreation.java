package de.peeeq.wurstscript.validation.symbols;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.ast.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * We create symbol tables in two phases:
 * <p>
 * 1. We parse each compilation unit independently.
 * 2. We resolve type symbols
 */
public class SymbolTableCreation {


    public static ImmutableList<PackageSymbolTable> getSymbols(CompilationUnit cu) {
        ImmutableList.Builder<PackageSymbolTable> result = ImmutableList.<PackageSymbolTable>builder();
        for (WPackage p : cu.getPackages()) {
            result.add(getPackageSymbols(p));
        }
        if (!cu.getJassDecls().isEmpty()) {
            result.add(getToplevelSymbols(cu.getJassDecls()));
        }
        return result.build();
    }

    private static PackageSymbolTable getToplevelSymbols(JassToplevelDeclarations jassDecls) {
        Map<TypeName, TypeDefSymbol> types = new LinkedHashMap<>();
        Multimap<String, FunctionSymbol> functions = HashMultimap.create();
        Map<String, VarSymbol> vars = new LinkedHashMap<>();
        PackagePath packageName = new PackagePath();

        for (JassToplevelDeclaration jassDecl : jassDecls) {
            jassDecl.match(new JassToplevelDeclaration.MatcherVoid() {
                @Override
                public void case_JassGlobalBlock(JassGlobalBlock jassGlobalBlock) {
                    for (GlobalVarDef g : jassGlobalBlock) {
                        vars.put(g.getName(), new VarSymbol(g.getName(), parseType(g.getOptTyp())));
                    }
                }

                @Override
                public void case_NativeFunc(NativeFunc nativeFunc) {
                    functions.put(nativeFunc.getName(), parseFunctionSymbol(nativeFunc.getName(), nativeFunc.getParameters(), nativeFunc.getReturnTyp()));
                }

                @Override
                public void case_FuncDef(FuncDef funcDef) {
                    functions.put(funcDef.getName(), parseFunctionSymbol(funcDef.getName(), funcDef.getParameters(), funcDef.getReturnTyp()));
                }

                @Override
                public void case_NativeType(NativeType nativeType) {
                    addNativeType(nativeType, types, packageName);
                }

                @Override
                public void case_TupleDef(TupleDef tupleDef) {
                    addTupleDef(tupleDef, types, packageName);
                }
            });
        }


        return new PackageSymbolTable(packageName, types, functions, vars);
    }

    private static void addNativeType(NativeType nativeType, Map<TypeName, TypeDefSymbol> types, PackagePath packageName) {
        List<TypeSymbol> superTypes;
        if (nativeType.getOptTyp() instanceof TypeExpr) {
            superTypes = ImmutableList.of(parseType(nativeType.getOptTyp()));
        } else {
            superTypes = ImmutableList.of();
        }

        TypeName typeName = new TypeName(nativeType.getName());
        types.put(typeName, new TypeDefSymbol(
            ImmutableList.of(),
            new TypeSymbol.ScopedType(true, packageName, typeName, ImmutableList.of()),
            superTypes,
            ImmutableList.of()
        ));
    }

    private static void addTupleDef(TupleDef tupleDef, Map<TypeName, TypeDefSymbol> types, PackagePath packageName) {
        TypeName typeName = new TypeName(tupleDef.getName());
        List<TypeSymbol> superTypes = ImmutableList.of();
        types.put(typeName, new TypeDefSymbol(
            ImmutableList.of(),
            new TypeSymbol.ScopedType(true, packageName, typeName, ImmutableList.of()),
            superTypes,
            ImmutableList.of()
        ));
    }

    private static FunctionSymbol parseFunctionSymbol(String name, WParameters parameters, OptTypeExpr returnTyp) {
        return null;
    }

    private static FunctionSymbol parseFunctionSymbol(NativeFunc nativeFunc) {
        return null;
    }

    private static TypeSymbol parseType(OptTypeExpr optTyp) {
        return null;
    }

    private static PackageSymbolTable getPackageSymbols(WPackage p) {
        
        return null;
    }


}
