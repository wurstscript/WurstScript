package de.peeeq.wurstscript.attributes.symbols;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstType;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * TODO how to handle module instantiations? own scope or include?
 */
public class ExportingScopeLookup {


    private static Stream<ClassDef> innerClasses(ClassOrModuleOrModuleInstanciation c) {
        return Stream.concat(
                c.getInnerClasses().stream(),
                c.getModuleInstanciations().stream()
                        .flatMap(mi -> innerClasses(mi)));
    }

    public static Optional<ExportingScope> attrLookupScope(ExportingScope scope, String name) {
        return scope.match(new ExportingScope.Matcher<Optional<ExportingScope>>() {
            @Override
            public Optional<ExportingScope> case_InterfaceDef(InterfaceDef interfaceDef) {
                return Optional.empty();
            }

            @Override
            public Optional<ExportingScope> case_CompilationUnit(CompilationUnit cu) {
                for (WPackage p : cu.getPackages()) {
                    if (p.getName().equals(name)) {
                        return Optional.of(p);
                    }
                }
                return Optional.empty();
            }

            @Override
            public Optional<ExportingScope> case_WPackage(WPackage p) {
                for (WEntity entity : p.getElements()) {
                    if (entity instanceof ExportingScope
                            && entity instanceof AstElementWithNameId) {
                        AstElementWithNameId named = (AstElementWithNameId) entity;
                        if (named.getNameId().getName().equals(name)) {
                            return Optional.of((ExportingScope) named);
                        }
                    }
                }
                return Optional.empty();
            }

            @Override
            public Optional<ExportingScope> case_ClassDef(ClassDef c) {
                return innerClasses(c)
                        .filter(ic -> ic.getName().equals(name))
                        .map(Function.<ExportingScope>identity())
                        .findFirst();
            }

            @Override
            public Optional<ExportingScope> case_TupleDef(TupleDef tupleDef) {
                return Optional.empty();
            }

            @Override
            public Optional<ExportingScope> case_ModuleDef(ModuleDef moduleDef) {
                return Optional.empty();
            }
        });
    }

    public static Optional<TypeDef> attrLookupType(ExportingScope scope, String name) {
        return scope.match(new ExportingScope.Matcher<Optional<TypeDef>>() {
            @Override
            public Optional<TypeDef> case_InterfaceDef(InterfaceDef interfaceDef) {
                return Optional.empty();
            }

            @Override
            public Optional<TypeDef> case_CompilationUnit(CompilationUnit cu) {
                for (JassToplevelDeclaration decl : cu.getJassDecls()) {
                    if (decl instanceof NativeType) {
                        NativeType nt = (NativeType) decl;
                        if (nt.getName().equals(name)) {
                            return Optional.of(nt);
                        }
                    }
                }
                return Optional.empty();
            }

            @Override
            public Optional<TypeDef> case_WPackage(WPackage p) {
                for (WEntity e : p.getElements()) {
                    if (e instanceof TypeDef) {
                        TypeDef td = (TypeDef) e;
                        if (td.getName().equals(name)) {
                            return Optional.of(td);
                        }
                    }
                }
                return Optional.empty();
            }

            @Override
            public Optional<TypeDef> case_ClassDef(ClassDef c) {
                return innerClasses(c)
                        .filter(ic -> ic.getName().equals(name))
                        .map(Function.<TypeDef>identity())
                        .findFirst();
            }


            @Override
            public Optional<TypeDef> case_TupleDef(TupleDef tupleDef) {
                return Optional.empty();
            }

            @Override
            public Optional<TypeDef> case_ModuleDef(ModuleDef moduleDef) {
                return Optional.empty();
            }
        });
    }

    public static Optional<VarDef> attrLookupVariable(ExportingScope scope, String name) {
        return scope.match(new ExportingScope.Matcher<Optional<VarDef>>() {
            @Override
            public Optional<VarDef> case_InterfaceDef(InterfaceDef i) {
                return findVar(i.getVars());
            }

            private Optional<VarDef> findVar(List<? extends VarDef> vars) {
                return vars.stream()
                        .filter(v -> v.getName().equals(name))
                        .map(Function.<VarDef>identity())
                        .findFirst();
            }

            @Override
            public Optional<VarDef> case_CompilationUnit(CompilationUnit cu) {
                for (JassToplevelDeclaration decl : cu.getJassDecls()) {
                    if (decl instanceof JassGlobalBlock) {
                        JassGlobalBlock globals = (JassGlobalBlock) decl;
                        for (GlobalVarDef global : globals) {
                            if (global.getName().equals(name)) {
                                return Optional.of(global);
                            }
                        }
                    }
                }
                return Optional.empty();
            }

            @Override
            public Optional<VarDef> case_WPackage(WPackage p) {
                for (WEntity e : p.getElements()) {
                    if (e instanceof GlobalVarDef) {
                        GlobalVarDef g = (GlobalVarDef) e;
                        if (g.getName().equals(name)) {
                            return Optional.of(g);
                        }
                    }
                }
                return Optional.empty();
            }

            @Override
            public Optional<VarDef> case_ClassDef(ClassDef classDef) {
                return findVar(classDef.getVars());
            }


            @Override
            public Optional<VarDef> case_TupleDef(TupleDef tupleDef) {
                return findVar(tupleDef.getParameters());
            }

            @Override
            public Optional<VarDef> case_ModuleDef(ModuleDef moduleDef) {
                return findVar(moduleDef.getVars());
            }
        });
    }

    public static Optional<FunctionImplementation> attrLookupFunction(ExportingScope scope, WurstType receiverType, String name, List<WurstType> paramTypes) {
        return scope.match(new ExportingScope.Matcher<Optional<FunctionImplementation>>() {
            @Override
            public Optional<FunctionImplementation> case_InterfaceDef(InterfaceDef interfaceDef) {
                return null;
            }

            @Override
            public Optional<FunctionImplementation> case_CompilationUnit(CompilationUnit compilationUnit) {
                return null;
            }

            @Override
            public Optional<FunctionImplementation> case_WPackage(WPackage wPackage) {
                return null;
            }

            @Override
            public Optional<FunctionImplementation> case_ClassDef(ClassDef classDef) {
                return null;
            }


            @Override
            public Optional<FunctionImplementation> case_TupleDef(TupleDef tupleDef) {
                return null;
            }

            @Override
            public Optional<FunctionImplementation> case_ModuleDef(ModuleDef moduleDef) {
                return null;
            }
        });
    }
}
