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
import de.peeeq.wurstscript.parser.WPos;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class TypeId {

    // sort classes by package, name, and source coordinate to get deterministic order; the line and column compare as
    // numbers, so a class on line 12 comes after one on line 9
    private static final Comparator<ImClass> class_comparator =
            Comparator.comparing(TypeId::packageName)
                    .thenComparing(ImClass::getName)
                    .thenComparing(TypeId::sourceFile)
                    .thenComparingInt(c -> sourcePosition(c, WPos::getLine))
                    .thenComparingInt(c -> sourcePosition(c, WPos::getStartColumn));

    public static Map<ImClass, Integer> calculate(ImProg prog) {
        return calculate(prog.getClasses(), TypeId::superClassDefs);
    }

    /**
     * Type ids for {@code classesToNumber}, numbered along the superclass edges {@code superClasses} gives, which
     * must lead to classes among them.
     */
    public static Map<ImClass, Integer> calculate(Collection<ImClass> classesToNumber,
                                                  Function<ImClass, List<ImClass>> superClasses) {
        AtomicInteger count = new AtomicInteger();
        Map<ImClass, Integer> result = Maps.newLinkedHashMap();

        List<ImClass> classes = classesToNumber.stream()
                .sorted(class_comparator)
                .collect(Collectors.toList());
        assignIds(count, result, classes, superClasses);
        return result;
    }

    /**
     * Type ids for {@code classes} which keep the ids in {@code before} and number the classes not in it after the
     * largest of them: compile-time code may have stored an id, so a class which had one keeps it. The checks and
     * dispatch built from type ids take a list of ids, so the ids need not be contiguous.
     */
    public static Map<ImClass, Integer> keeping(Map<ImClass, Integer> before, Collection<ImClass> classes) {
        return keeping(before, classes, TypeId::superClassDefs);
    }

    /** {@link #keeping(Map, Collection)} along the superclass edges {@code superClasses} gives. */
    public static Map<ImClass, Integer> keeping(Map<ImClass, Integer> before, Collection<ImClass> classes,
                                                Function<ImClass, List<ImClass>> superClasses) {
        Map<ImClass, Integer> result = Maps.newLinkedHashMap();
        List<ImClass> added = new java.util.ArrayList<>();
        for (ImClass c : classes) {
            Integer id = before.get(c);
            if (id != null) {
                result.put(c, id);
            } else {
                added.add(c);
            }
        }
        int offset = before.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        java.util.Set<ImClass> addedSet = new java.util.HashSet<>(added);
        Map<ImClass, Integer> addedIds = calculate(added, c -> superClasses.apply(c).stream()
                .filter(addedSet::contains).collect(Collectors.toList()));
        addedIds.forEach((c, id) -> result.put(c, offset + id));
        return result;
    }

    private static List<ImClass> superClassDefs(ImClass c) {
        return c.getSuperClasses().stream().map(ImClassType::getClassDef).collect(Collectors.toList());
    }

    private static String packageName(ImClass ic) {
        Element c = ic.attrTrace();
        if (c == null) {
            return "global";
        }
        @Nullable PackageOrGlobal nearestPackage = c.attrNearestPackage();
        if (nearestPackage instanceof WPackage wPackage) {
            return wPackage.getName();
        }
        return "global";
    }

    private static @Nullable WPos source(ImClass ic) {
        Element c = ic.attrTrace();
        return c == null ? null : c.attrSource();
    }

    private static String sourceFile(ImClass ic) {
        WPos pos = source(ic);
        if (pos == null) {
            return "";
        }
        String file = pos.getFile();
        return file != null ? new java.io.File(file).getName() : "";
    }

    /** A coordinate of the class's source position, or -1 for a class without one, which comes first. */
    private static int sourcePosition(ImClass ic, ToIntFunction<WPos> coordinate) {
        WPos pos = source(ic);
        return pos == null ? -1 : coordinate.applyAsInt(pos);
    }

    private static void assignIds(AtomicInteger count, Map<ImClass, Integer> result,
                                  List<ImClass> classes, Function<ImClass, List<ImClass>> superClasses) {
        Multimap<ImClass, ImClass> subClasses = calculateSubclasses(classes, superClasses);

        // start with the classes that do not have any super classes (preorder traversal)
        for (ImClass c : classes) {
            if (superClasses.apply(c).isEmpty()) {
                assignId(count, result, c, subClasses);
            }
        }
    }

    private static Multimap<ImClass, ImClass> calculateSubclasses(List<ImClass> classes,
                                                                Function<ImClass, List<ImClass>> superClasses) {
        Multimap<ImClass, ImClass> subClasses = LinkedHashMultimap.create();
        for (ImClass c : classes) {
            superClasses.apply(c).stream()
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
