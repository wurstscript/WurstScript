package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * optimizes classes, dynamic dispatch, etc.
 * before running EliminateClasses
 *
 * Precondition: Generics eliminated
 */
public class ClassesOptimizer {
    public static void optimizeProg(ImTranslator tr) {
        optimizeStatelessClasses(tr);

        optimizeDispatch(tr);
    }

    /**
     * Search for classes without attributes and replace them with
     * a single-instance.
     * If the class has no attributes, the only way to distinguish different
     * instances of the class is by using equality or casting to int.
     *
     * This is a necessary optimization for the translation of type classes,
     * as otherwise there would be memory leaks.
     * A typeclass dict is always a class without attributes.
     */
    private static void optimizeStatelessClasses(ImTranslator tr) {
        ImProg prog = tr.getImProg();

        Multimap<ImClass, ImClass> subClasses = transitiveReflexiveSubclasses(prog);

        Set<ImClass> statelessClasses = prog.getClasses().stream()
            .filter(c -> c.getFields().isEmpty()
                && subClasses.get(c).size() == 1)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        Multimap<ImClass, ImAlloc> allocs = LinkedHashMultimap.create();
        Multimap<ImClass, ImDealloc> deAllocs = LinkedHashMultimap.create();
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImAlloc imAlloc) {
                super.visit(imAlloc);
                ImClass classDef = imAlloc.getClazz().getClassDef();
                if (statelessClasses.contains(classDef)) {
                    allocs.put(classDef, imAlloc);
                }
            }

            @Override
            public void visit(ImDealloc deAlloc) {
                super.visit(deAlloc);
                ImClass classDef = deAlloc.getClazz().getClassDef();
                if (statelessClasses.contains(classDef)) {
                    deAllocs.put(classDef, deAlloc);
                }
            }

            @Override
            public void visit(ImOperatorCall c) {
                WurstOperator op = c.getOp();
                if (op == WurstOperator.EQ || op == WurstOperator.NOTEQ) {
                    for (ImExpr arg : c.getArguments()) {
                        markNotStateless(arg.attrTyp());
                    }
                }
            }

            private void markNotStateless(ImType attrTyp) {
                if (attrTyp instanceof ImClassType) {
                    ImClassType ct = (ImClassType) attrTyp;
                    markNotStateless(ct.getClassDef());
                }
            }

            private void markNotStateless(ImClass classDef) {
                statelessClasses.removeAll(subClasses.get(classDef));
            }

            @Override
            public void visit(ImCast c) {
                if (c.getToType().equalsType(TypesHelper.imInt())) {
                    markNotStateless(c.getExpr().attrTyp());
                }
            }
        });

        for (ImClass c : statelessClasses) {
            Collection<ImAlloc> cAllocs = allocs.get(c);
            if (cAllocs.isEmpty()) {
                continue;
            }
            // create singleton variable
            ImClassType classType = JassIm.ImClassType(c, JassIm.ImTypeArguments());
            ImVar singletonVar = JassIm.ImVar(c.getTrace(), classType, c.getName() + "_singleton", Collections.emptyList());
            tr.addGlobalWithInitalizer(singletonVar, JassIm.ImAlloc(c.getTrace(), classType));

            // replace all allocations with singleton variable
            for (ImAlloc cAlloc : cAllocs) {
                cAlloc.replaceBy(JassIm.ImVarAccess(singletonVar));
            }
            // remove deAllocs
            for (ImDealloc deAlloc : deAllocs.get(c)) {
                deAlloc.replaceBy(JassIm.ImNull(JassIm.ImVoid()));
            }

        }
    }

    /**
     * map from class to all subclasses and itself
     */
    private static Multimap<ImClass, ImClass> transitiveReflexiveSubclasses(ImProg prog) {
        LinkedHashMultimap<ImClass, ImClass> res = LinkedHashMultimap.create();
        for (ImClass c : prog.getClasses()) {
            addSuperClasses(res, c, c);
        }

        return res;
    }

    private static void addSuperClasses(LinkedHashMultimap<ImClass, ImClass> res, ImClass subClass, ImClass superClass) {
        res.put(superClass, subClass);
        for (ImClassType superSuperClass : superClass.getSuperClasses()) {
            addSuperClasses(res, subClass, superSuperClass.getClassDef());
        }
    }

    /**
     * Analyze the program and find places where the receiver of a method
     * is guaranteed to not be null or de-allocated.
     * Mark these method calls as unchecked dispatches.
     * Unchecked dispatches are later translated to simplified code: If
     * there are no sub-methods, they can be directly translated to
     * function calls.
     */
    private static void optimizeDispatch(ImTranslator tr) {
        // TODO implement optimization
    }
}
