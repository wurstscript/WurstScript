package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.WurstTypeInt;

import java.util.*;

/**
 * This is responsible for adding the reference counting code to a program
 */
public class ReferenceCounting {

    private final ImTranslator tr;
    private Set<ImClass> refCountedClasses = new LinkedHashSet<>();
    private Set<ImClass> refCountedClassesMax = new LinkedHashSet<>();
    private Multimap<ImClass, ImClass> refcountSuper = HashMultimap.create();
    private Map<ImClass, ImVar> refcountVar = new HashMap<>();
    private ImProg prog;

    public ReferenceCounting(ImTranslator tr) {
        this.tr = tr;
    }

    public void transformProgram(ImProg prog) {
        System.out.println("Transforming program");
        this.prog = prog;
        buildIndex(prog);
        generateRefcountFields();

        // rewrite assignments
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImSet imSet) {
                ImVar v = imSet.getLeft();
                System.out.println(" visit " + v.getName() + ": " + v.getType() + " // " + v.getType().getClass());
                super.visit(imSet);
            }

            @Override
            public void visit(ImSetTuple imSetTuple) {
                super.visit(imSetTuple);
            }

            @Override
            public void visit(ImSetArray imSetArray) {
                super.visit(imSetArray);
            }

            @Override
            public void visit(ImSetArrayMulti imSetArrayMulti) {
                super.visit(imSetArrayMulti);
            }

            @Override
            public void visit(ImSetArrayTuple imSetArrayTuple) {
                super.visit(imSetArrayTuple);
            }
        });

        // rewrite endfunction


        // rewrite ondestroy


    }

    private void generateRefcountFields() {
        // add a refcount field to each max. refcount class
        for (ImClass c : refCountedClassesMax) {
            ImVar v = JassIm.ImVar(c.getTrace(), WurstTypeInt.instance().imTranslateType(tr), "refcount", false);
            c.getFields().add(v);
            refcountVar.put(c, v);

        }
    }

    private void buildIndex(ImProg prog) {
        // collect reference counted classes
        for (ImClass c : prog.getClasses()) {
            if (c.getClassFlags().contains(ClassFlag.managedRefCount)) {
                addRefCountedSubclasses(c);
            }
        }

        // find classes that already have a reference counted super-class:
        Multimap<ImClass, ImClass> directSuper = HashMultimap.create();
        Set<ImClass> subClasses = new HashSet<>();
        for (ImClass c : refCountedClasses) {
            for (ImClass sc : c.getSuperClasses()) {
                if (refCountedClasses.contains(sc)) {
                    subClasses.add(sc);
                    directSuper.put(sc, c);
                }
            }
        }

        refCountedClassesMax.addAll(refCountedClasses);
        refCountedClassesMax.removeAll(subClasses);

        // collect maximal classes in refcountSuper:
        for (ImClass c : refCountedClasses) {
            addRefcountSuper(c, c, directSuper);
        }
    }

    private void addRefcountSuper(ImClass orig, ImClass c, Multimap<ImClass, ImClass> directSuper) {
        Collection<ImClass> ds = directSuper.get(c);
        if (ds.isEmpty()) {
            refcountSuper.put(orig, c);
        }
        for (ImClass sc : ds) {
            addRefcountSuper(orig, sc, directSuper);
        }
    }

    private void addRefCountedSubclasses(ImClass c) {
        if (refCountedClasses.contains(c)) {
            return;
        }
        refCountedClasses.add(c);

        for (ImClass sc : prog.getClasses()) {
            if (sc.getSuperClasses().contains(c)) {
                addRefCountedSubclasses(c);
            }
        }
    }

}
