package de.peeeq.wurstscript.translation.lua.translation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.validation.TRVEHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Removes every class, function, method and global variable
 * that is not used from main or config
 */
public class RemoveGarbage {

    private static class Used {
        private final ImTranslator translator;
        private final Set<ImFunction> functions = new HashSet<>();
        private final Set<ImMethod> methods = new HashSet<>();
        // methods that will be added once the class is used:
        private final Multimap<ImClass, ImMethod> waitingMethods = HashMultimap.create();
        private final Set<ImClass> classes = new HashSet<>();
        private final Set<ImVar> vars = new HashSet<>();

        private Used(ImTranslator translator) {
            this.translator = translator;
        }

        public void addMethod(ImMethod m) {
            methods.add(m);
        }

        public void maybeVisitMethod(ImMethod m) {
            ImClass c = m.attrClass();
            if (classes.contains(c)) {
                visitMethod(m, this);
            } else {
                waitingMethods.put(c, m);
            }
        }

        public Set<ImFunction> getFunctions() {
            return functions;
        }

        public Set<ImMethod> getMethods() {
            return methods;
        }

        public Set<ImClass> getClasses() {
            return classes;
        }

        public Set<ImVar> getVars() {
            return vars;
        }

        public void addFunction(ImFunction f) {
            functions.add(f);
        }

        public void addVar(ImVar var) {
            vars.add(var);
        }

        public void addClass(ImClass c) {
            classes.add(c);
            ImClass nominalClass = translator.canonical(c);
            if (nominalClass != c) {
                // A targeted specialization has a distinct storage layout but keeps the source
                // class's nominal type id and instanceof identity. The canonical class is therefore
                // a real metadata dependency even when no source expression names it directly.
                visitClass(nominalClass, this);
            }
            Collection<ImMethod> imMethods = waitingMethods.get(c);
            Iterator<ImMethod> it = imMethods.iterator();
            while (it.hasNext()) {
                ImMethod m = it.next();
                visitMethod(m, this);
                it.remove();
            }
        }
    }

    public static void removeGarbage(ImProg prog, ImTranslator translator) {
        removePhantomGenericStaticInitializers(prog, translator);
        Used used = collectUsed(prog, translator);

        prog.getClasses().removeIf(c -> !used.getClasses().contains(c));
        prog.getGlobals().removeIf(g -> !used.getVars().contains(g) && !TRVEHelper.protectedVariables.contains(g.getName()));
        prog.getFunctions().removeIf(f -> !used.getFunctions().contains(f));
        prog.getMethods().removeIf(m -> !used.getMethods().contains(m));
        for (ImMethod m : prog.getMethods()) {
            m.getSubMethods().removeIf(sm -> !used.getMethods().contains(sm));
        }
        // A field of a specialised class is a copy which nothing refers to, an access made before
        // specialisation still naming the original's variable. It is live exactly when the field it
        // was copied from is; dropping it leaves an instance of the specialised class allocated with
        // no fields at all while the emitted code goes on reading them.
        for (ImClass c : prog.getClasses()) {
            c.getFields().removeIf(g -> !used.getVars().contains(g)
                && !used.getVars().contains(translator.canonical(g)));
            c.getFunctions().removeIf(f -> !used.getFunctions().contains(f));
            c.getMethods().removeIf(m -> !used.getMethods().contains(m));
            for (ImMethod m : c.getMethods()) {
                m.getSubMethods().removeIf(sm -> !used.getMethods().contains(sm));
            }
        }

    }

    private static Used collectUsed(ImProg prog, ImTranslator translator) {
        Used used = new Used(translator);
        for (ImFunction f : ImHelper.calculateFunctionsOfProg(prog)) {
            if (f.getName().equals("main")
                || f.getName().equals("config")) {
                visitFunction(f, used);
            }
        }
        return used;
    }

    public static void removePhantomGenericStaticInitializers(ImProg prog, ImTranslator translator) {
        while (removePhantomGenericStaticInitializersOnce(prog, translator,
            collectUsed(prog, translator))) {
            // Removing one initializer can make a generic static referenced by it unreachable.
        }
    }

    /**
     * A targeted generic specialization owns a copied static initializer. The erased initializer is
     * not an independent runtime instantiation: when no live code uses its original static, keeping
     * its side effects would initialize a phantom object and run the source initializer twice.
     */
    private static boolean removePhantomGenericStaticInitializersOnce(ImProg prog,
                                                                      ImTranslator translator,
                                                                      Used used) {
        Set<ImVar> originalsWithSpecializations = new LinkedHashSet<>();
        for (ImVar global : prog.getGlobals()) {
            ImTranslator.Specialisation specialization = translator.specialisationOf(global);
            if (specialization != null && specialization.original() instanceof ImVar original
                && translator.genericStaticOwnerOf(original) != null) {
                originalsWithSpecializations.add(original);
            }
        }
        for (ImVar original : originalsWithSpecializations) {
            List<ImSet> initializers = prog.getGlobalInits().get(original);
            if (hasUseOutsideOwnInitializer(original, initializers, used)) {
                continue;
            }
            initializers = prog.getGlobalInits().remove(original);
            if (initializers == null) {
                continue;
            }
            for (ImSet initializer : initializers) {
                if (!(initializer.getParent() instanceof ImStmts statements)) {
                    throw new IllegalStateException("Global initializer is not attached to an ImStmts node.");
                }
                statements.remove(initializer);
            }
            return true;
        }
        return false;
    }

    private static boolean hasUseOutsideOwnInitializer(ImVar global, List<ImSet> initializers,
                                                       Used used) {
        Set<ImSet> initializerSet = initializers == null
            ? Collections.emptySet()
            : Collections.newSetFromMap(new IdentityHashMap<>());
        if (initializers != null) {
            initializerSet.addAll(initializers);
        }
        boolean[] found = {false};
        for (ImFunction function : used.getFunctions()) {
            function.accept(new Element.DefaultVisitor() {
                @Override
                public void visit(ImVarAccess access) {
                    if (access.getVar() == global && !isInsideInitializer(access, initializerSet)) {
                        found[0] = true;
                    }
                    super.visit(access);
                }

                @Override
                public void visit(ImVarArrayAccess access) {
                    if (access.getVar() == global && !isInsideInitializer(access, initializerSet)) {
                        found[0] = true;
                    }
                    super.visit(access);
                }
            });
            if (found[0]) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInsideInitializer(Element access, Set<ImSet> initializers) {
        Element current = access;
        while (current.getParent() instanceof Element parent) {
            if (parent instanceof ImSet set && initializers.contains(set)) {
                return true;
            }
            current = parent;
        }
        return false;
    }

    private static void visitFunction(ImFunction f, Used used) {
        if (used.getFunctions().contains(f)) {
            return;
        }
        used.addFunction(f);

        visitType(f.getReturnType(), used);
        f.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall e) {
                super.visit(e);
                visitFunction(e.getFunc(), used);
            }

            @Override
            public void visit(ImVar e) {
                super.visit(e);
                visitType(e.getType(), used);
            }

            @Override
            public void visit(ImFuncRef e) {
                super.visit(e);
                visitFunction(e.getFunc(), used);
            }

            @Override
            public void visit(ImCast e) {
                super.visit(e);
                visitType(e.getToType(), used);
            }

            @Override
            public void visit(ImVarAccess e) {
                super.visit(e);
                used.addVar(e.getVar());
            }

            @Override
            public void visit(ImVarArrayAccess e) {
                super.visit(e);
                used.addVar(e.getVar());
            }

            @Override
            public void visit(ImAlloc e) {
                super.visit(e);
                visitClass(e.getClazz().getClassDef(), used);
            }

            @Override
            public void visit(ImDealloc e) {
                super.visit(e);
                visitClass(e.getClazz().getClassDef(), used);
            }

            @Override
            public void visit(ImInstanceof e) {
                super.visit(e);
                visitClass(e.getClazz().getClassDef(), used);
            }

            @Override
            public void visit(ImTypeIdOfObj e) {
                super.visit(e);
                visitClass(e.getClazz().getClassDef(), used);
            }

            @Override
            public void visit(ImTypeIdOfClass e) {
                super.visit(e);
                visitClass(e.getClazz().getClassDef(), used);
            }

            @Override
            public void visit(ImMethodCall e) {
                super.visit(e);
                visitMethod(e.getMethod(), used);
            }

            @Override
            public void visit(ImMemberAccess e) {
                super.visit(e);
                used.addVar(e.getVar());
            }
        });
    }

    private static void visitMethod(ImMethod m, Used used) {
        if (used.getMethods().contains(m)) {
            return;
        }
        used.addMethod(m);
        visitClass(m.getMethodClass().getClassDef(), used);
        if (m.getImplementation() != null) {
            // abstract methods can have no implementation
            visitFunction(m.getImplementation(), used);
        }
        for (ImMethod subMethod : m.getSubMethods()) {
            used.maybeVisitMethod(subMethod);
        }
    }

    private static void visitClass(ImClass c, Used used) {
        if (used.getClasses().contains(c)) {
            return;
        }
        used.addClass(c);
        for (ImClassType superClass : c.getSuperClasses()) {
            visitClass(superClass.getClassDef(), used);
        }
    }

    private static void visitType(ImType t, Used used) {
        t.match(new ImType.MatcherVoid() {

            @Override
            public void case_ImAnyType(ImAnyType imAnyType) {

            }

            @Override
            public void case_ImTupleType(ImTupleType tt) {
                for (ImType type : tt.getTypes()) {
                    visitType(type, used);
                }
            }

            @Override
            public void case_ImTypeVarRef(ImTypeVarRef tt) {

            }

            @Override
            public void case_ImVoid(ImVoid tt) {

            }

            @Override
            public void case_ImSimpleType(ImSimpleType tt) {

            }

            @Override
            public void case_ImArrayTypeMulti(ImArrayTypeMulti tt) {
                visitType(tt.getEntryType(), used);
            }

            @Override
            public void case_ImClassType(ImClassType tt) {
                visitClass(tt.getClassDef(), used);
                for (ImTypeArgument ta : tt.getTypeArguments()) {
                    visitType(ta.getType(), used);
                }
            }

            @Override
            public void case_ImArrayType(ImArrayType tt) {
                visitType(tt.getEntryType(), used);
            }
        });

    }
}
