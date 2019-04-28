package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.ast.AstElementWithNameId;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.jassIm.*;

/**
 *
 */
public class NormalizeNames {
    /**
     * uses the original names when possible instead of the mangled names
     */
    public static void normalizeNames(ImProg prog) {
        Multimap<ImMethod, ImMethod> superMethods = calculateSuperMethods(prog);
//        for (ImVar g : prog.getGlobals()) {
//            normalizeName(g);
//        }
//        for (ImFunction f : prog.getFunctions()) {
//            normalizeName(f);
//        }
        for (ImClass c : prog.getClasses()) {
            for (ImVar g : c.getFields()) {
                normalizeName(g);
            }
//            for (ImFunction f : c.getFunctions()) {
//                normalizeName(f);
//            }
            for (ImMethod m : c.getMethods()) {
                Element trace = m.attrTrace();
                if (trace instanceof AstElementWithNameId) {
                    m.setName(((AstElementWithNameId) trace).getNameId().getName());
                }
                if (trace instanceof OnDestroyDef) {
                    m.setName("__onDestroy");
                }
                // if this has no supermethods, make sure all submethods
                // have the same name
                if (superMethods.get(m).isEmpty()) {
                    for (ImMethod subMethod : m.getSubMethods()) {
                        subMethod.setName(m.getName());
                    }
                }

            }

//            Element trace = c.attrTrace();
//            if (trace instanceof AstElementWithNameId) {
//                c.setName(((AstElementWithNameId) trace).getNameId().getName());
//            }
        }
    }

    private static void normalizeName(ImVar g) {
        Element trace = g.attrTrace();
        if (trace instanceof AstElementWithNameId) {
            g.setName(((AstElementWithNameId) trace).getNameId().getName());
        }
    }

    private static Multimap<ImMethod, ImMethod> calculateSuperMethods(ImProg prog) {
        ImmutableMultimap.Builder<ImMethod, ImMethod> builder = ImmutableMultimap.builder();
        for (ImClass c : prog.getClasses()) {
            for (ImMethod m : c.getMethods()) {
                for (ImMethod subMethod : m.getSubMethods()) {
                    builder.put(subMethod, m);
                }
            }
        }
        return builder.build();
    }
}
