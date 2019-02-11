package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImClassType;
import de.peeeq.wurstscript.jassIm.ImProg;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TypeId {

    // sort classes by name to get deterministic order
    private static final Comparator<ImClass> class_comparator =
            Comparator.comparing(ImClass::getName)
                    .thenComparing(TypeId::packageName);

    // sort class types by name to get deterministic order
    private static final Comparator<ImClassType> classtype_comparator =
            Comparator.comparing((ImClassType ct) -> ct.getClassDef().getName())
                    .thenComparing(ct -> TypeId.packageName(ct.getClassDef()));

    public static Map<ImClass, Integer> calculate(ImProg prog) {
        AtomicInteger count = new AtomicInteger();
        Map<ImClass, Integer> result = Maps.newLinkedHashMap();

        List<ImClass> classes = prog.getClasses().stream()
                .sorted(class_comparator)
                .collect(Collectors.toList());
        assignIds(count, result, classes);
        return result;
    }

    private static String packageName(ImClass ic) {
        Element c = ic.attrTrace();
        @Nullable PackageOrGlobal nearestPackage = c.attrNearestPackage();
        if (nearestPackage instanceof WPackage) {
            return ((WPackage) nearestPackage).getName();
        }
        return "global";
    }

    private static void assignIds(AtomicInteger count, Map<ImClass, Integer> result,
                                  List<ImClass> classes) {
        Multimap<ImClass, ImClass> subClasses = calculateSubclasses(classes);

        // start with the classes that do not have any super classes (preorder traversal)
        for (ImClass c : classes) {
            if (c.getSuperClasses().isEmpty()) {
                assignId(count, result, c, subClasses);
            }
        }
    }

    private static Multimap<ImClass, ImClass> calculateSubclasses(List<ImClass> classes) {
        Multimap<ImClass, ImClass> subClasses = LinkedHashMultimap.create();
        for (ImClass c : classes) {
            c.getSuperClasses().stream()
                    .map(ImClassType::getClassDef)
                    .sorted(class_comparator).forEach(superClass ->
                    subClasses.put(superClass, c)
            );
        }
        return subClasses;
    }

    /**
     * preorder traversal and assign ids
     */
    private static void assignId(AtomicInteger count, Map<ImClass, Integer> result,
                                 ImClass c, Multimap<ImClass, ImClass> subClasses) {
        if (!result.containsKey(c)) {
            result.put(c, count.incrementAndGet());
            // assign ids to subclasses:
            for (ImClass sub : subClasses.get(c)) {
                assignId(count, result, sub, subClasses);
            }
        }
    }

    public static int get(ImClass c) {
        Integer res = c.attrProg().attrTypeId().get(c);
        if (res == null) {
            throw new CompileError(c, "Could not get type-id for " + c.getName() + ImPrinter.smallHash(c));
        }
        return res;
    }

    public static boolean isSubclass(ImClass c, ImClass other) {
        if (c == other) {
            return true;
        }
        for (ImClassType sc : c.getSuperClasses()) {
            if (sc.getClassDef().isSubclassOf(other)) {
                return true;
            }
        }
        return false;
    }

}
