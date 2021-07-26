package de.peeeq.wurstscript.translation.imoptimizer;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public class RestrictedCompressedNames {
    static Set<String> names = HashSet.of("div", "val", "var", "mod", "use", "new", "for", "in", "it", "to",
            "Sin", "Cos", "Tan", "Pow", "I2R", "R2I", "I2S", "R2S", "S2I", "S2R",
            "And", "Or", "Not", "or", "and", "not", "if", "set", "loop", "endif",
            "endfunction", "endloop", "globals", "endglobals", "local", "call", "debug",
            "main", "config", "alias");

    public static boolean contains(String name) {
        return names.contains(name);
    }
}
