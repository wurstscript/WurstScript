package de.peeeq.wurstscript.attributes.names;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeInterface;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.validation.WurstValidator;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class NameLinks {

    static private class OverrideCheckResult {
        // does this override some other function
        boolean doesOverride = false;

        // overrides a function from a class or module
        // (for interfaces override modifier is optional)
        boolean requiresOverrideMod = false;

        // errors for functions with same name that it does not override
        io.vavr.collection.List<String> overrideErrors = io.vavr.collection.List.empty();

        public void addError(String error) {
            this.overrideErrors = overrideErrors.prepend(error);
        }
    }

    public static ImmutableMultimap<String, DefLink> calculate(ClassOrModuleOrModuleInstanciation c) {
        Multimap<String, DefLink> result = HashMultimap.create();
        addDefinedNames(result, c);
        Map<String, Map<FuncLink, OverrideCheckResult>> overrideCheckResults = initOverrideMap(result);
        addNamesFromUsedModuleInstantiations(c, result, overrideCheckResults);

        if (c instanceof ClassDef) {
            WurstTypeClass classType = ((ClassDef) c).attrTypC();
            addNamesFormSuperClass(result, classType, overrideCheckResults);
            addNamesFromImplementedInterfaces(result, classType, overrideCheckResults);
        }

        reportOverrideErrors(overrideCheckResults);
        return ImmutableMultimap.copyOf(result);
    }

    @NotNull
    private static Map<String, Map<FuncLink, OverrideCheckResult>> initOverrideMap(Multimap<String, DefLink> result) {
        Map<String, Map<FuncLink, OverrideCheckResult>> overrideCheckResults = new Hashtable<>();
        for (DefLink link : result.values()) {
            if (link instanceof FuncLink) {
                Map<FuncLink, OverrideCheckResult> map = overrideCheckResults.computeIfAbsent(link.getName(),
                        s -> new HashMap<>());
                map.put((FuncLink) link, new OverrideCheckResult());
            }
        }
        return overrideCheckResults;
    }

    private static void reportOverrideErrors(Map<String, Map<FuncLink, OverrideCheckResult>> overrideCheckResults) {
        // report override errors
        for (Map<FuncLink, OverrideCheckResult> map : overrideCheckResults.values()) {
            for (Entry<FuncLink, OverrideCheckResult> e : map.entrySet()) {
                FunctionDefinition f = e.getKey().getDef();
                OverrideCheckResult check = e.getValue();
                if (f.attrIsOverride() && !check.doesOverride) {
                    StringBuilder msg = new StringBuilder("Function " + f.getName() + " does not override anything.");
                    for (String overrideError : check.overrideErrors) {
                        msg.append("\n  ");
                        msg.append(overrideError);
                    }
                    f.addError(msg.toString());
                } else if (!f.attrIsOverride() && check.doesOverride) {
                    if (check.requiresOverrideMod) {
                        f.addError("Function " + f.getName() + " must be marked with the 'override' modifier.");
                    } else {
                        f.addWarning("Function " + f.getName() + " should be marked with the 'override' modifier.");
                    }
                }

            }
        }
    }

    public static ImmutableMultimap<String, DefLink> calculate(InterfaceDef i) {
        Multimap<String, DefLink> result = HashMultimap.create();
        addDefinedNames(result, i, i.getMethods());
        Map<String, Map<FuncLink, OverrideCheckResult>> overrideCheckResults = initOverrideMap(result);
        addNamesFromExtendedInterfaces(result, i.attrTypI(), overrideCheckResults);
        reportOverrideErrors(overrideCheckResults);
        return ImmutableMultimap.copyOf(result);
    }

    private static void addNamesFromExtendedInterfaces(Multimap<String, DefLink> result, WurstTypeInterface iType, Map<String, Map<FuncLink, OverrideCheckResult>> overrideCheckResults) {
        for (WurstTypeInterface superI : iType.extendedInterfaces()) {
            addNewNameLinks(result, overrideCheckResults, superI.nameLinks(), false);
        }
    }


    private static void addNamesFromImplementedInterfaces(Multimap<String, DefLink> result, WurstTypeClass classDef, Map<String, Map<FuncLink, OverrideCheckResult>> overrideCheckResults) {
        for (WurstTypeInterface interfaceType : classDef.implementedInterfaces()) {
            addNewNameLinks(result, overrideCheckResults, interfaceType.nameLinks(), false);
        }
    }

    private static void addNamesFormSuperClass(Multimap<String, DefLink> result, WurstTypeClass c, Map<String, Map<FuncLink, OverrideCheckResult>> overrideCheckResults) {
        @Nullable WurstTypeClass superClass = c.extendedClass();
        if (superClass != null) {
            addNewNameLinks(result, overrideCheckResults, superClass.nameLinks(), false);
        }
    }

    private static void addNewNameLinks(Multimap<String, DefLink> result, Map<String, Map<FuncLink, OverrideCheckResult>> overrideCheckResults, ImmutableMultimap<String, DefLink> newNameLinks, boolean allowStaticOverride) {
        for (Entry<String, DefLink> e : newNameLinks.entries()) {
            DefLink def = e.getValue();
            if (!def.getVisibility().isInherited()) {
                continue;
            }
            String name = e.getKey();
            boolean isOverridden = false;
            if (def instanceof FuncLink) {
                FuncLink func = (FuncLink) def;

                // check if function is overridden by any other function in
                Map<FuncLink, OverrideCheckResult> otherFuncs = overrideCheckResults.getOrDefault(name, Collections.emptyMap());
                for (Entry<FuncLink, OverrideCheckResult> e2 : otherFuncs.entrySet()) {
                    FuncLink otherFunc = e2.getKey();
                    OverrideCheckResult checkResult = e2.getValue();

                    String error = WurstValidator.checkOverride(otherFunc, func, allowStaticOverride);
                    if (error == null) {
                        checkResult.doesOverride = true;
                        if (!(func.getReceiverType() instanceof WurstTypeInterface)) {
                            checkResult.requiresOverrideMod = true;
                        }
                        isOverridden = true;
                    } else {
                        checkResult.addError(error);
                    }


                }

            }
            if (!isOverridden) {
                result.put(name, def.hidingPrivate());
            }
        }
    }


    public static ImmutableMultimap<String, DefLink> calculate(CompilationUnit cu) {
        ImmutableMultimap.Builder<String, DefLink> result = ImmutableSetMultimap.builder();
        addJassNames(result, cu);
        addPackages(result, cu);
        return result.build();
    }

    public static ImmutableMultimap<String, DefLink> calculate(AstElementWithBody c) {
        ImmutableMultimap.Builder<String, DefLink> result = ImmutableSetMultimap.builder();
        WScope s = (WScope) c;
        addVarDefIfAny(result, s);
        addParametersIfAny(result, s);
        return result.build();
    }


    private static void addVarDefIfAny(Builder<String, DefLink> result, WScope s) {
        if (s instanceof LoopStatementWithVarDef) {
            LoopStatementWithVarDef l = (LoopStatementWithVarDef) s;
            result.put(l.getLoopVar().getName(), VarLink.create(l.getLoopVar(), s));
        }
    }

    public static ImmutableMultimap<String, DefLink> calculate(EnumDef e) {
        ImmutableMultimap.Builder<String, DefLink> result = ImmutableSetMultimap.builder();
        addDefinedNames(result, e, e.getMembers());
        return result.build();
    }


    public static ImmutableMultimap<String, DefLink> calculate(@SuppressWarnings("unused") NativeFunc nativeFunc) {
        ImmutableMultimap.Builder<String, DefLink> result = ImmutableSetMultimap.builder();
        return result.build();
    }

    public static ImmutableMultimap<String, DefLink> calculate(TupleDef t) {
        ImmutableMultimap.Builder<String, DefLink> result = ImmutableSetMultimap.builder();
        addDefinedNames(result, t, t.getParameters());
        return result.build();
    }

    public static ImmutableMultimap<String, DefLink> calculate(WPackage p) {
        ImmutableMultimap.Builder<String, DefLink> result = ImmutableSetMultimap.builder();
        for (WImport imp : p.getImports()) {
            if (imp.getPackagename().equals("NoWurst")) {
                continue;
            }
            WPackage importedPackage = imp.attrImportedPackage();
            if (importedPackage == null) {
                WLogger.info("could not resolve import: " + Utils.printElementWithSource(Optional.of(imp)));
                continue;
            }
            if (p.getName().equals("WurstREPL")) {
                // the REPL is special and can use all names
                result.putAll(importedPackage.getElements().attrNameLinks());
                result.putAll(importedPackage.attrNameLinks());
            } else {
                // normal packages can only use the exported names of a package
                result.putAll(importedPackage.attrExportedNameLinks());
            }
        }

        return result.build();
    }


    public static ImmutableMultimap<String, DefLink> calculate(WEntities wEntities) {
        ImmutableMultimap.Builder<String, DefLink> result = ImmutableSetMultimap.builder();
        for (WEntity e : wEntities) {
            if (e instanceof NameDef) {
                NameDef n = (NameDef) e;
                addNameDefDefLink(result, n, wEntities);
            }
            if (e instanceof WScope && !(e instanceof ModuleDef)) {
                WScope scope = (WScope) e;
                List<TypeParamDef> typeParams;
                if (scope instanceof AstElementWithTypeParameters) {
                    typeParams = ((AstElementWithTypeParameters) scope).getTypeParameters();
                } else {
                    typeParams = Collections.emptyList();
                }
                addHidingPrivate(result, scope.attrNameLinks(), typeParams);
            }
        }
        return result.build();
    }

    public static ImmutableMultimap<String, DefLink> calculate(WurstModel model) {
        ImmutableMultimap.Builder<String, DefLink> result = ImmutableSetMultimap.builder();
        for (CompilationUnit cu : model) {
            result.putAll(cu.attrNameLinks());
        }
        return result.build();
    }

    public static ImmutableMultimap<String, DefLink> calculate(WStatements statements) {
        ImmutableMultimap.Builder<String, DefLink> result = ImmutableSetMultimap.builder();
        for (WStatement s : statements) {
            if (s instanceof LocalVarDef) {
                LocalVarDef var = (LocalVarDef) s;
                result.put(var.getName(), VarLink.create(var, statements));
            }
        }
        return result.build();
    }

    private static void addParametersIfAny(Builder<String, DefLink> result, WScope s) {
        if (s instanceof AstElementWithParameters) {
            AstElementWithParameters withParams = (AstElementWithParameters) s;
            for (WParameter p : withParams.getParameters()) {
                result.put(p.getName(), VarLink.create(p, s));
            }
        }

    }

    private static void addPackages(Builder<String, DefLink> result, CompilationUnit cu) {
        for (WPackage p : cu.getPackages()) {
            result.put(p.getName(), PackageLink.create(p, cu));
        }
    }

    private static void addJassNames(Builder<String, DefLink> result, CompilationUnit cu) {
        for (JassToplevelDeclaration jd : cu.getJassDecls()) {
            if (jd instanceof NameDef) {
                NameDef def = (NameDef) jd;
                addNameDefDefLink(result, def, cu);
            } else if (jd instanceof JassGlobalBlock) {
                JassGlobalBlock jassGlobalBlock = (JassGlobalBlock) jd;
                addDefinedNames(result, cu, jassGlobalBlock);
            }
        }
    }


    private static void addNameDefDefLink(Consumer<DefLink> result, NameDef def, WScope scope) {
        if (def instanceof VarDef) {
            result.accept(VarLink.create(((VarDef) def), scope));
        } else if (def instanceof FunctionDefinition) {
            result.accept(FuncLink.create(((FunctionDefinition) def), scope));
        } else if (def instanceof WPackage) {
            result.accept(PackageLink.create(((WPackage) def), scope));
        } else if (def instanceof TypeDef) {
            result.accept(TypeDefLink.create(((TypeDef) def), scope));
        } else if (def instanceof EnumMember) {
            result.accept(VarLink.create(((EnumMember) def), scope));
        }
    }

    private static void addNameDefDefLink(Builder<String, DefLink> result, NameDef def, WScope scope) {
        addNameDefDefLink(l -> result.put(l.getName(), l), def, scope);
    }

    private static void addNameDefDefLink(Multimap<String, DefLink> result, NameDef def, WScope scope) {
        addNameDefDefLink(l -> result.put(l.getName(), l), def, scope);
    }


    private static void addNamesFromUsedModuleInstantiations(ClassOrModuleOrModuleInstanciation c,
                                                             Multimap<String, DefLink> result, Map<String, Map<FuncLink, OverrideCheckResult>> overrideCheckResults) {
        for (ModuleInstanciation m : c.getModuleInstanciations()) {
            addNewNameLinks(result, overrideCheckResults, m.attrNameLinks(), true);
        }
    }

    private static void addDefinedNames(Multimap<String, DefLink> result, ClassOrModuleOrModuleInstanciation c) {
        addDefinedNames(result, c, c.getMethods());
        addDefinedNames(result, c, c.getVars());
        addDefinedNames(result, c, c.getModuleInstanciations());
        addDefinedNames(result, c, c.getInnerClasses());
    }

    private static void addDefinedNames(Builder<String, DefLink> result, WScope definedIn, List<? extends NameDef> slots) {
        for (NameDef n : slots) {
            addNameDefDefLink(result, n, definedIn);
        }
    }

    private static void addDefinedNames(Multimap<String, DefLink> result, WScope definedIn, List<? extends NameDef> slots) {
        for (NameDef n : slots) {
            addNameDefDefLink(result, n, definedIn);
        }
    }


    public static void addHidingPrivate(Builder<String, DefLink> result, Multimap<String, DefLink> adding, List<TypeParamDef> typeParams) {
        for (Entry<String, DefLink> e : adding.entries()) {
            if (e.getValue().getVisibility() == Visibility.LOCAL) {
                continue;
            }
            result.put(e.getKey(), e.getValue().hidingPrivate().withGenericTypeParams(typeParams));
        }

    }

    public static void addHidingPrivate(Multimap<String, DefLink> result, Multimap<String, DefLink> adding) {
        for (Entry<String, DefLink> e : adding.entries()) {
            if (e.getValue().getVisibility() == Visibility.LOCAL) {
                continue;
            }
            result.put(e.getKey(), e.getValue().hidingPrivate());
        }

    }

    public static void addHidingPrivateAndProtected(ImmutableMultimap.Builder<String, DefLink> r, Multimap<String, ? extends DefLink> adding) {
        for (Entry<String, ? extends DefLink> e : adding.entries()) {
            if (e.getValue().getVisibility() == Visibility.LOCAL) {
                continue;
            }
            r.put(e.getKey(), e.getValue().hidingPrivateAndProtected());
        }
    }

    public static void addTypesHidingPrivateAndProtected(ImmutableMultimap.Builder<String, TypeLink> r, Multimap<String, TypeLink> adding) {
        for (Entry<String, TypeLink> e : adding.entries()) {
            if (e.getValue().getVisibility() == Visibility.LOCAL) {
                continue;
            }
            r.put(e.getKey(), e.getValue().hidingPrivateAndProtected());
        }
    }

    public static ImmutableMultimap<String, DefLink> calculate(ExprClosure e) {
        ImmutableMultimap.Builder<String, DefLink> result = ImmutableSetMultimap.builder();
        for (WShortParameter p : e.getShortParameters()) {
            result.put(p.getName(), VarLink.create(p, e));
        }
        return result.build();
    }

}
