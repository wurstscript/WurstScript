package de.peeeq.wurstscript;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleExpander {

    private ModuleExpander() {
    }

    public static void expandModules(CompilationUnit cu) {
        for (WPackage t : cu.getPackages()) {
            expandModules(t);
        }
    }

    private static void expandModules(WPackage p) {
        for (WEntity e : p.getElements()) {
            if (e instanceof ClassOrModule) {
                expandModules((ClassOrModule) e);
            }
        }
    }

    public static ModuleInstanciations expandModules(ClassOrModule m) {
        return expandModules(m, new ArrayList<>());
    }

    private static ModuleInstanciations expandModules(ClassOrModule m, List<ClassOrModule> visited) {
        if (m.getP_moduleInstanciations().size() > 0 || m.getModuleUses().isEmpty()) {
            return m.getP_moduleInstanciations();
        }

        Preconditions.checkNotNull(m);
        if (visited.contains(m)) {
            throw new CompileError(m.getSource(), "Cyclic module dependencies: " +
                visited.stream().map(ClassOrModule::getName).sorted().collect(Collectors.joining(", ")));
        }
        visited.add(m);



        for (ModuleUse moduleUse : m.getModuleUses()) {
            ModuleDef usedModule = moduleUse.attrModuleDef();
            if (usedModule == null) {
                moduleUse.addError("not found");
                continue;
            }
            ModuleInstanciations usedModuleInst = expandModules(usedModule, visited);


            int numTypeArgs = moduleUse.getTypeArgs().size();
            if (numTypeArgs < usedModule.getTypeParameters().size()) {
                moduleUse.addError("Missing type arguments for module "
                        + moduleUse.getModuleName() + ".");
            } else if (numTypeArgs > usedModule.getTypeParameters().size()) {
                moduleUse.addError("Too many type arguments for module "
                        + moduleUse.getModuleName() + ".");
            }

            List<Pair<WurstType, WurstType>> typeReplacements = Lists.newArrayList();
            for (int i = 0; i < numTypeArgs; i++) {
                typeReplacements.add(Pair.create(usedModule.getTypeParameters().get(i).attrTyp(), moduleUse.getTypeArgs().get(i).attrTyp()));
            }

            WPos source = moduleUse.getSource().artificial();
            WPos idSource = moduleUse.getModuleNameId().getSource().artificial();
            ModuleInstanciation mi = Ast.ModuleInstanciation(source, Ast.Modifiers(),
                    Ast.Identifier(idSource, usedModule.getName()),
                    smartCopy(usedModule.getInnerClasses(), typeReplacements),
                    smartCopy(usedModule.getMethods(), typeReplacements),
                    smartCopy(usedModule.getVars(), typeReplacements),
                    smartCopy(usedModule.getConstructors(), typeReplacements),
                    smartCopy(usedModuleInst, typeReplacements),
                    smartCopy(usedModule.getModuleUses(), typeReplacements),
                    smartCopy(usedModule.getOnDestroy(), typeReplacements));

            if (mi.getConstructors().isEmpty()) {
                // add default constructor:
                mi.getConstructors().add(Ast.ConstructorDef(
                        source,
                        Ast.Modifiers(),
                        Ast.WParameters(),
                        Ast.NoSuperConstructorCall(),
                        Ast.WStatements(
                                Ast.StartFunctionStatement(source),
                                Ast.EndFunctionStatement(source)
                        )));
            }

            m.getP_moduleInstanciations().add(mi);

        }
        return m.getP_moduleInstanciations();
    }

    public static ModuleInstanciations expandModules(ModuleInstanciation mi) {
        return mi.getP_moduleInstanciations();
    }

    public static <T extends Element> T smartCopy(T e, List<Pair<WurstType, WurstType>> typeReplacements) {
        List<Pair<ImmutableList<Integer>, TypeExpr>> replacementsByPath = Lists.newArrayList();
        calcReplacementsByPath(typeReplacements, replacementsByPath, e, ImmutableList.emptyList());


        Element copy = e.copy();

        // Do the type replacements
        for (Pair<ImmutableList<Integer>, TypeExpr> rep : replacementsByPath) {
            doReplacement(copy, rep.getA(), rep.getB().copy());
        }

        @SuppressWarnings("unchecked")
        T t = (T) copy;
        return t;
    }

    private static void doReplacement(Element e, ImmutableList<Integer> a, TypeExpr newTypeExpr) {
        if (a.size() == 1) {
            e.set(a.head(), newTypeExpr);
        } else if (a.size() > 1) {
            doReplacement(e.get(a.head()), a.tail(), newTypeExpr);
        }
    }

    private static void calcReplacementsByPath(List<Pair<WurstType, WurstType>> typeReplacements, List<Pair<ImmutableList<Integer>, TypeExpr>> replacementsByPath, Element e, ImmutableList<Integer> pos) {
        if (e instanceof TypeExpr) {
            TypeExpr typeExpr = (TypeExpr) e;
            for (Pair<WurstType, WurstType> rep : typeReplacements) {
                if (typeExpr.attrTyp().equalsType(rep.getA(), e)) {
                    WPos source = typeExpr.getSource();
                    replacementsByPath.add(Pair.create(pos, Ast.TypeExprResolved(source, rep.getB())));
                }
            }
        }
        // children:
        for (int i = 0; i < e.size(); i++) {
            calcReplacementsByPath(typeReplacements, replacementsByPath, e.get(i), pos.appBack(i));
        }
    }

}
