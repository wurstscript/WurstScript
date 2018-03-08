package de.peeeq.wurstscript.attributes.names;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeInterface;
import de.peeeq.wurstscript.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NameLinks {

    public static ImmutableMultimap<String, NameLink> calculate(ClassOrModuleOrModuleInstanciation c) {
        ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
        addNamesFromUsedModuleInstantiations(c, result);
        addDefinedNames(result, c);
        if (c instanceof ClassDef) {
            ClassDef classDef = (ClassDef) c;
            addNamesFormSuperClass(result, classDef);
            addNamesFromImplementedInterfaces(result, classDef);
        }
        return result.build();
    }

    public static ImmutableMultimap<String, NameLink> calculate(CompilationUnit cu) {
        ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
        addJassNames(result, cu);
        addPackages(result, cu);
        return result.build();
    }

    public static ImmutableMultimap<String, NameLink> calculate(AstElementWithBody c) {
        ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
        WScope s = (WScope) c;
        addVarDefIfAny(result, s);
        addParametersIfAny(result, s);
        return result.build();
    }


    private static void addVarDefIfAny(Builder<String, NameLink> result, WScope s) {
        if (s instanceof LoopStatementWithVarDef) {
            LoopStatementWithVarDef l = (LoopStatementWithVarDef) s;
            result.put(l.getLoopVar().getName(), l.getLoopVar().createNameLink(s));
        }
    }

    public static ImmutableMultimap<String, NameLink> calculate(EnumDef e) {
        ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
        addDefinedNames(result, e, e.getMembers());
        return result.build();
    }

    public static ImmutableMultimap<String, NameLink> calculate(InterfaceDef i) {
        ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
        addDefinedNames(result, i, i.getMethods());
        return result.build();
    }

    public static ImmutableMultimap<String, NameLink> calculate(NativeFunc nativeFunc) {
        ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
        return result.build();
    }

    public static ImmutableMultimap<String, NameLink> calculate(TupleDef t) {
        ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
        addDefinedNames(result, t, t.getParameters());
        return result.build();
    }

    public static ImmutableMultimap<String, NameLink> calculate(WPackage p) {
        ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
        for (WImport imp : p.getImports()) {
            if (imp.getPackagename().equals("NoWurst")) {
                continue;
            }
            WPackage importedPackage = imp.attrImportedPackage();
            if (importedPackage == null) {
                WLogger.info("could not resolve import: " + Utils.printElementWithSource(imp));
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


    public static ImmutableMultimap<String, NameLink> calculate(WEntities wEntities) {
        ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
        for (WEntity e : wEntities) {
            if (e instanceof NameDef) {
                NameDef n = (NameDef) e;
                result.put(n.getName(), n.createNameLink(wEntities));
            }
            if (e instanceof WScope && !(e instanceof ModuleDef)) {
                WScope scope = (WScope) e;
                addHidingPrivate(result, scope.attrNameLinks());
            }
        }
        return result.build();
    }

    public static ImmutableMultimap<String, NameLink> calculate(WurstModel model) {
        ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
        for (CompilationUnit cu : model) {
            result.putAll(cu.attrNameLinks());
        }
        return result.build();
    }

    public static ImmutableMultimap<String, NameLink> calculate(WStatements statements) {
        ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
        for (WStatement s : statements) {
            if (s instanceof LocalVarDef) {
                LocalVarDef var = (LocalVarDef) s;
                result.put(var.getName(), var.createNameLink(statements));
            }
        }
        return result.build();
    }

    private static void addParametersIfAny(Builder<String, NameLink> result, WScope s) {
        if (s instanceof AstElementWithParameters) {
            AstElementWithParameters withParams = (AstElementWithParameters) s;
            for (WParameter p : withParams.getParameters()) {
                result.put(p.getName(), p.createNameLink(s));
            }
        }

    }

    private static void addPackages(Builder<String, NameLink> result, CompilationUnit cu) {
        for (WPackage p : cu.getPackages()) {
            result.put(p.getName(), p.createNameLink(cu));
        }
    }

    private static void addJassNames(Builder<String, NameLink> result, CompilationUnit cu) {
        for (JassToplevelDeclaration jd : cu.getJassDecls()) {
            if (jd instanceof NameDef) {
                NameDef def = (NameDef) jd;
                result.put(def.getName(), def.createNameLink(cu));
            } else if (jd instanceof JassGlobalBlock) {
                JassGlobalBlock jassGlobalBlock = (JassGlobalBlock) jd;
                addDefinedNames(result, cu, jassGlobalBlock);
            }
        }
    }

    private static void addNamesFromImplementedInterfaces(Builder<String, NameLink> result, ClassDef classDef) {
        for (WurstTypeInterface interfaceType : classDef.attrImplementedInterfaces()) {
            Map<TypeParamDef, WurstTypeBoundTypeParam> binding = interfaceType.getTypeArgBinding();
            InterfaceDef i = interfaceType.getInterfaceDef();
            for (Entry<String, NameLink> e : i.attrNameLinks().entries()) {
                result.put(e.getKey(), e.getValue().withTypeArgBinding(classDef, binding));
            }
        }
    }

    private static void addNamesFormSuperClass(Builder<String, NameLink> result, ClassDef classDef) {
        if (classDef.getExtendedClass().attrTyp() instanceof WurstTypeClass) {
            WurstTypeClass wurstTypeClass = (WurstTypeClass) classDef.getExtendedClass().attrTyp();
            ClassDef extendedClass = wurstTypeClass.getClassDef();
            Map<TypeParamDef, WurstTypeBoundTypeParam> binding = wurstTypeClass.getTypeArgBinding();
            addHidingPrivate(result, extendedClass.attrNameLinks(), binding, classDef);
        }
    }


    private static void addNamesFromUsedModuleInstantiations(ClassOrModuleOrModuleInstanciation c,
                                                             Builder<String, NameLink> result) {
        for (ModuleInstanciation m : c.getModuleInstanciations()) {
            addHidingPrivate(result, m.attrNameLinks());
        }
    }

    private static void addDefinedNames(Builder<String, NameLink> result, ClassOrModuleOrModuleInstanciation c) {
        addDefinedNames(result, c, c.getMethods());
        addDefinedNames(result, c, c.getVars());
        addDefinedNames(result, c, c.getModuleInstanciations());
    }

    private static void addDefinedNames(Builder<String, NameLink> result, WScope definedIn, List<? extends NameDef> slots) {
        for (NameDef n : slots) {
            result.put(n.getName(), n.createNameLink(definedIn));
        }
    }


    public static void addHidingPrivate(Builder<String, NameLink> result, Multimap<String, NameLink> adding) {
        for (Entry<String, NameLink> e : adding.entries()) {
            if (e.getValue().getVisibility() == Visibility.LOCAL) {
                continue;
            }
            result.put(e.getKey(), e.getValue().hidingPrivate());
        }

    }

    private static void addHidingPrivate(Builder<String, NameLink> result,
                                         Multimap<String, NameLink> adding,
                                         Map<TypeParamDef, WurstTypeBoundTypeParam> binding,
                                         Element context) {
        for (Entry<String, NameLink> e : adding.entries()) {
            if (e.getValue().getVisibility() == Visibility.LOCAL) {
                continue;
            }
            result.put(e.getKey(), e.getValue().withTypeArgBinding(context, binding).hidingPrivate());
        }

    }

    public static void addHidingPrivateAndProtected(ImmutableMultimap.Builder<String, NameLink> r, Multimap<String, NameLink> adding) {
        for (Entry<String, NameLink> e : adding.entries()) {
            if (e.getValue().getVisibility() == Visibility.LOCAL) {
                continue;
            }
            r.put(e.getKey(), e.getValue().hidingPrivateAndProtected());
        }
    }

    public static ImmutableMultimap<String, NameLink> calculate(ExprClosure e) {
        ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
        for (WShortParameter p : e.getShortParameters()) {
            result.put(p.getName(), p.createNameLink(e));
        }
        return result.build();
    }

}