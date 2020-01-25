package de.peeeq.wurstscript.translation.imtranslation;

/**
 * optimizes classes, dynamic dispatch, etc.
 * before running EliminateClasses
 */
public class ClassesOptimizer {
    public static void optimizeProg(ImTranslator tr) {
        optimizeStatelessClasses(tr);

        optimizeDispatch(tr);
    }

    /**
     * Search for classes without attributes and replace them with
     * a single-instance.
     *
     * This is a necessary optimization for the translation of type classes,
     * as otherwise there would be memory leaks.
     * A typeclass dict is always a class without attributes.
     */
    private static void optimizeStatelessClasses(ImTranslator tr) {
        // TODO implement optimization
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
