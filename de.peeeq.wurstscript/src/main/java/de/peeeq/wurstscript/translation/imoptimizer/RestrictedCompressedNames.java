package de.peeeq.wurstscript.translation.imoptimizer;

public class RestrictedCompressedNames {
    static String names[] = {"div", "val", "var", "mod", "use", "new", "for", "in", "it", "to",
            "Sin", "Cos", "Tan", "Pow", "I2R", "R2I", "I2S", "R2S", "S2I", "S2R",
            "And", "Or", "Not", "or", "and", "not", "if", "set"};

    public static boolean contains(String s) {
        for (String name : names) {
            if (name.equals(s)) {
                return true;
            }
        }
        return false;
    }
}
