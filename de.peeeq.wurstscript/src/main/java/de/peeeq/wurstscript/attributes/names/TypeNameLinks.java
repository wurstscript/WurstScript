package de.peeeq.wurstscript.attributes.names;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import de.peeeq.wurstscript.ast.*;

public class TypeNameLinks {

    public static ImmutableMultimap<String, TypeLink> calculate(ClassOrModuleOrModuleInstanciation c) {
        ImmutableMultimap.Builder<String, TypeLink> result = ImmutableSetMultimap.builder();
        addTypeParametersIfAny(result, c);
        for (ClassDef innerClass : c.getInnerClasses()) {
            result.put(innerClass.getName(), TypeLink.create(innerClass, c));
        }
        WScope nextScope = c.attrNextScope();
        if (nextScope != null) {
            result.put(c.getName(), TypeLink.create(c, nextScope));
        }
        return result.build();
    }

    public static ImmutableMultimap<String, TypeLink> calculate(CompilationUnit cu) {
        ImmutableMultimap.Builder<String, TypeLink> result = ImmutableSetMultimap.builder();
        addJassTypes(result, cu);
        // TODO are packages types?
//        addPackages(result, cu);
        return result.build();
    }

    public static ImmutableMultimap<String, TypeLink> calculate(AstElementWithBody c) {
        ImmutableMultimap.Builder<String, TypeLink> result = ImmutableSetMultimap.builder();
        WScope s = (WScope) c;
        addTypeParametersIfAny(result, s);
        return result.build();
    }


    public static ImmutableMultimap<String, TypeLink> calculate(EnumDef e) {
        return ImmutableMultimap.of();
    }

    public static ImmutableMultimap<String, TypeLink> calculate(InterfaceDef i) {
        ImmutableMultimap.Builder<String, TypeLink> result = ImmutableSetMultimap.builder();
        addTypeParametersIfAny(result, i);
        return result.build();
    }

    public static ImmutableMultimap<String, TypeLink> calculate(NativeFunc nativeFunc) {
        return ImmutableMultimap.of();
    }

    public static ImmutableMultimap<String, TypeLink> calculate(TupleDef t) {
        return ImmutableMultimap.of();
    }

    public static ImmutableMultimap<String, TypeLink> calculate(WPackage p) {
        ImmutableMultimap.Builder<String, TypeLink> result = ImmutableSetMultimap.builder();
        for (WImport imp : p.getImports()) {
            WPackage importedPackage = imp.attrImportedPackage();
            if (importedPackage == null) {
                continue;
            }
            result.putAll(importedPackage.attrExportedTypeNameLinks());
        }
        return result.build();
    }

    public static ImmutableMultimap<String, TypeLink> calculate(WEntities wEntities) {
        ImmutableMultimap.Builder<String, TypeLink> result = ImmutableSetMultimap.builder();
        for (WEntity e : wEntities) {
            if (e instanceof TypeDef) {
                TypeDef n = (TypeDef) e;
                result.put(n.getName(), TypeLink.create(n, wEntities));
            }
        }
        return result.build();
    }

    public static ImmutableMultimap<String, TypeLink> calculate(WurstModel model) {
        ImmutableMultimap.Builder<String, TypeLink> result = ImmutableSetMultimap.builder();
        for (CompilationUnit cu : model) {
            result.putAll(cu.attrTypeNameLinks());
        }
        return result.build();
    }

    public static ImmutableMultimap<String, TypeLink> calculate(WStatements statements) {
        return ImmutableMultimap.of();
    }

    private static void addTypeParametersIfAny(ImmutableMultimap.Builder<String, TypeLink> result, WScope c) {
        if (c instanceof AstElementWithTypeParameters) {
            AstElementWithTypeParameters wtp = (AstElementWithTypeParameters) c;
            for (TypeParamDef i : wtp.getTypeParameters()) {
                result.put(i.getName(), TypeLink.create(i, c));
            }
        }

    }

    private static void addJassTypes(ImmutableMultimap.Builder<String, TypeLink> result, CompilationUnit cu) {
        for (JassToplevelDeclaration jd : cu.getJassDecls()) {
            if (jd instanceof TypeDef) {
                TypeDef def = (TypeDef) jd;
                result.put(def.getName(), TypeLink.create(def, cu));
            }
        }
    }

//    private static void addPackages(ImmutableMultimap.Builder<String, TypeLink> result, CompilationUnit cu) {
//        for (WPackage p : cu.getPackages()) {
//            result.put(p.getName(), TypeLink.create(p, cu));
//        }
//    }

    public static ImmutableMultimap<String, TypeLink> calculate(ExprClosure exprClosure) {
        return ImmutableMultimap.of();
    }
}
