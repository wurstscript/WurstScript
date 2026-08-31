package de.peeeq.wurstscript.translation.lua.translation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.validation.NamePreservation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
        /** Classes whose reachable runtime objects may dispatch virtual methods. */
        private final Set<ImClass> dispatchClasses = new HashSet<>();
        /** Classes which reachable code actually allocates, excluding nominal type-only references. */
        private final Set<ImClass> instantiatedClasses = new HashSet<>();
        private final Set<ImVar> vars = new HashSet<>();
        private final Set<ImSet> ignoredInitializers;

        private Used(ImTranslator translator, Set<ImSet> ignoredInitializers) {
            this.translator = translator;
            this.ignoredInitializers = ignoredInitializers;
        }

        public void addMethod(ImMethod m) {
            methods.add(m);
        }

        public void maybeVisitMethod(ImMethod m) {
            ImClass c = m.attrClass();
            if (dispatchClasses.contains(c)) {
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

        public Set<ImClass> getInstantiatedClasses() {
            return instantiatedClasses;
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

        public boolean addClass(ImClass c, boolean dispatchReachable) {
            boolean newClass = classes.add(c);
            boolean newDispatchClass = dispatchReachable && dispatchClasses.add(c);
            if (newClass) {
                ImClass nominalClass = translator.canonical(c);
                if (nominalClass != c) {
                    // A targeted specialization has a distinct storage layout but keeps the source
                    // class's nominal type id and instanceof identity. The canonical class is therefore
                    // a metadata dependency, not evidence that erased instances can dispatch.
                    visitClass(nominalClass, this, false);
                }
            }
            if (newDispatchClass) {
                Collection<ImMethod> imMethods = waitingMethods.get(c);
                Iterator<ImMethod> it = imMethods.iterator();
                while (it.hasNext()) {
                    ImMethod m = it.next();
                    visitMethod(m, this);
                    it.remove();
                }
            }
            return newClass || newDispatchClass;
        }

        public void addInstantiatedClass(ImClass c) {
            if (!instantiatedClasses.add(c)) {
                return;
            }
            for (ImClassType superClass : c.getSuperClasses()) {
                addInstantiatedClass(superClass.getClassDef());
            }
        }
    }

    public static void removeGarbage(ImProg prog, ImTranslator translator) {
        Used used = collectUsed(prog, translator);

        prog.getClasses().removeIf(c -> !used.getClasses().contains(c));
        prog.getGlobals().removeIf(g -> !used.getVars().contains(g) && !NamePreservation.isPreserved(g));
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
        return collectUsed(prog, translator, Collections.emptySet());
    }

    private static Used collectUsed(ImProg prog, ImTranslator translator,
                                    Set<ImSet> ignoredInitializers) {
        Used used = new Used(translator, ignoredInitializers);
        for (ImFunction f : ImHelper.calculateFunctionsOfProg(prog)) {
            if (f.getName().equals("main")
                || f.getName().equals("config")
                || NamePreservation.isPreserved(f)) {
                visitFunction(f, used);
            }
        }
        return used;
    }

    public static void removePhantomGenericStaticInitializers(ImProg prog, ImTranslator translator) {
        Map<ImVar, List<ImSet>> candidates = new LinkedHashMap<>();
        for (ImVar global : prog.getGlobals()) {
            ImTranslator.Specialisation specialization = translator.specialisationOf(global);
            if (specialization != null && specialization.original() instanceof ImVar original
                && translator.genericStaticOwnerOf(original) != null) {
                List<ImSet> initializers = prog.getGlobalInits().get(original);
                if (initializers != null) {
                    candidates.putIfAbsent(original, initializers);
                }
            }
        }

        // First ignore every erased initializer, then mark the originals referenced by real roots.
        // Re-enable initializers of marked originals until their transitive dependencies are marked.
        // This is graph reachability rather than deletion order, so unreachable initializer cycles
        // cannot keep themselves alive.
        Set<ImVar> liveOriginals = new LinkedHashSet<>();
        boolean changed;
        do {
            Set<ImSet> ignored = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Map.Entry<ImVar, List<ImSet>> candidate : candidates.entrySet()) {
                if (!liveOriginals.contains(candidate.getKey())) {
                    ignored.addAll(candidate.getValue());
                }
            }
            Used used = collectUsed(prog, translator, ignored);
            changed = false;
            for (ImVar original : candidates.keySet()) {
                ImClass owner = translator.genericStaticOwnerOf(original);
                boolean erasedInstantiationNeedsOriginal = used.getInstantiatedClasses().contains(owner)
                    && translator.hasErasedAllocationWithoutStaticSpecialization(owner, original);
                if ((used.getVars().contains(original) || erasedInstantiationNeedsOriginal)
                    && liveOriginals.add(original)) {
                    changed = true;
                }
            }
        } while (changed);

        for (Map.Entry<ImVar, List<ImSet>> candidate : candidates.entrySet()) {
            if (liveOriginals.contains(candidate.getKey())) {
                continue;
            }
            prog.getGlobalInits().remove(candidate.getKey());
            for (ImSet initializer : candidate.getValue()) {
                if (initializer.getParent() == null) {
                    continue;
                }
                if (!(initializer.getParent() instanceof ImStmts statements)) {
                    throw new IllegalStateException("Global initializer is not attached to an ImStmts node.");
                }
                statements.remove(initializer);
            }
        }
    }

    private static void visitFunction(ImFunction f, Used used) {
        if (used.getFunctions().contains(f)) {
            return;
        }
        used.addFunction(f);
        visitType(f.getReturnType(), used);
        f.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImSet e) {
                if (!used.ignoredInitializers.contains(e)) {
                    super.visit(e);
                }
            }

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
                used.addInstantiatedClass(e.getClazz().getClassDef());
                visitClass(e.getClazz().getClassDef(), used);
            }

            @Override
            public void visit(ImDealloc e) {
                super.visit(e);
                visitClass(e.getClazz().getClassDef(), used, false);
            }

            @Override
            public void visit(ImInstanceof e) {
                super.visit(e);
                visitClass(e.getClazz().getClassDef(), used, false);
            }

            @Override
            public void visit(ImTypeIdOfObj e) {
                super.visit(e);
                visitClass(e.getClazz().getClassDef(), used, false);
            }

            @Override
            public void visit(ImTypeIdOfClass e) {
                super.visit(e);
                visitClass(e.getClazz().getClassDef(), used, false);
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
        visitClass(m.getMethodClass().getClassDef(), used, false);
        if (m.getImplementation() != null) {
            // abstract methods can have no implementation
            visitFunction(m.getImplementation(), used);
        }
        for (ImMethod subMethod : m.getSubMethods()) {
            used.maybeVisitMethod(subMethod);
        }
    }

    private static void visitClass(ImClass c, Used used) {
        visitClass(c, used, true);
    }

    private static void visitClass(ImClass c, Used used, boolean dispatchReachable) {
        if (!used.addClass(c, dispatchReachable)) {
            return;
        }
        for (ImClassType superClass : c.getSuperClasses()) {
            visitClass(superClass.getClassDef(), used, dispatchReachable);
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
                visitClass(tt.getClassDef(), used, false);
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
