package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.*;

public class EliminateLocalTypes {
    private static ImType localVarType = JassIm.ImSimpleType("localVarType");

    public static void eliminateLocalTypesProg(ImProg imProg, ImTranslator translator) {
        // Eliminates local types to be able to merge more locals in Lua.
        for (ImFunction f : ImHelper.calculateFunctionsOfProg(imProg)) {
            eliminateLocalTypesFunc(f, translator);
        }
    }

    private static void eliminateLocalTypesFunc(ImFunction f, final ImTranslator translator) {
        for(ImVar local : f.getLocals()) {
            ImType t = local.getType();
            if(t instanceof ImArrayLikeType) {
                // Arrays are initialized with the default value of the type, so type information needs to be preserved.
                continue;
            }
            if(t instanceof ImTupleType) {
                // Tuples use special access functions, so type information needs to be preserved.
                continue;
            }
            // Types of single values can be erased, because they have to be initialized with a specific value before they can be used.
            local.setType(localVarType);
        }
    }
}
