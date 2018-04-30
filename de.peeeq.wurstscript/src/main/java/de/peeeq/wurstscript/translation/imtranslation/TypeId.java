package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImClasses;
import de.peeeq.wurstscript.jassIm.ImProg;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeId {

    public static Map<ImClass, Integer> calculate(ImProg prog) {
        AtomicInteger count = new AtomicInteger();
        Map<ImClass, Integer> result = Maps.newLinkedHashMap();
        // sort classes by name to get deterministic order
        Comparator<ImClass> cmp = Comparator.comparing(ImClass::getName).thenComparing(TypeId::packageName);
        List<ImClass> classes = prog.getClasses().stream()
                .sorted(cmp)
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
        Multimap<ImClass, ImClass> subClasses = HashMultimap.create();
        for (ImClass c : classes) {
            for (ImClass superClass : c.getSuperClasses()) {
                subClasses.put(superClass, c);
            }
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
        return c.attrProg().attrTypeId().get(c);
    }

    public static boolean isSubclass(ImClass c, ImClass other) {
        if (c == other) {
            return true;
        }
        for (ImClass sc : c.getSuperClasses()) {
            if (sc.isSubclassOf(other)) {
                return true;
            }
        }
        return false;
    }

}
