package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.ast.AstElementWithNameId;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImVar;

/**
 *
 */
public class NormalizeNames {
    /**
     * uses the original names when possible instead of the mangled names
     */
    public static void normalizeNames(ImProg prog) {
        Multimap<ImMethod, ImMethod> superMethods = calculateSuperMethods(prog);
        for (ImClass c : prog.getClasses()) {
            for (ImVar g : c.getFields()) {
                normalizeName(g);
            }
            for (ImMethod m : c.getMethods()) {
                Element trace = m.attrTrace();
                if (trace instanceof AstElementWithNameId astElementWithNameId) {
                    m.setName(astElementWithNameId.getNameId().getName());
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

        }
    }

    private static void normalizeName(ImVar g) {
        Element trace = g.attrTrace();
        if (trace instanceof AstElementWithNameId astElementWithNameId) {
            g.setName(astElementWithNameId.getNameId().getName());
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
