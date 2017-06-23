package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;

/**
 * calculates wether a variable is a class member
 */
public class AttrIsClassMember {

    public static boolean calculate(WParameter v) {
        return false;
    }

    public static boolean calculate(GlobalVarDef v) {
        if (v.attrNearestNamedScope() instanceof StructureDef) {
            return !v.attrIsStatic();
        }
        return false;
    }

    public static boolean calculate(LocalVarDef v) {
        return false;
    }

    public static boolean calculate(FuncDef f) {
        if (f.attrNearestNamedScope() instanceof StructureDef) {
            return !f.attrIsStatic();
        }
        return false;
    }

    public static boolean calculate(ExtensionFuncDef f) {
        return false;
    }

    public static boolean calculate(NativeFunc nativeFunc) {
        return false;
    }

    public static boolean calculate(TupleDef t) {
        return false;
    }

}
