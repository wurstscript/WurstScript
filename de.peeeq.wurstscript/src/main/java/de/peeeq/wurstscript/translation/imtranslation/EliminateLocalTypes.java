package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

public class EliminateLocalTypes {
    private static final ImType localSimpleType = JassIm.ImSimpleType("localSimpleType");
    private static final ImType localIntType = JassIm.ImSimpleType("localSimpleTypeInt");
    private static final ImType localRealType = JassIm.ImSimpleType("localSimpleTypeReal");
    private static final ImType localBoolType = JassIm.ImSimpleType("localSimpleTypeBool");
    private static final ImType localStringType = JassIm.ImSimpleType("localSimpleTypeString");

    public static void eliminateLocalTypesProg(ImProg imProg, ImTranslator translator) {
        // While local types are still there, perform transformation, such that the lua translator does not need to know variable types
        // null string -> "" (avoids type dependency in null translation)
        // String concatenation was already lowered by LuaNativeLowering before optimization.
        // int castTo int -> remove cast (avoids type dependency in cast translation)
        transformProgram(imProg, translator);
        // Eliminates local types to be able to merge more locals in Lua.
        for (ImFunction f : ImHelper.calculateFunctionsOfProg(imProg)) {
            eliminateLocalTypesFunc(f, translator);
        }
    }

    private static void eliminateLocalTypesFunc(ImFunction f, final ImTranslator translator) {
        for(ImVar local : f.getLocals()) {
            ImType t = local.getType();
            if(t instanceof ImSimpleType) {
                // Keep primitive domains separate so later local merging cannot
                // unify e.g. number/bool/string temporaries into one slot.
                local.setType(canonicalizeSimpleLocalType((ImSimpleType) t));
            }
        }
    }

    private static ImType canonicalizeSimpleLocalType(ImSimpleType t) {
        if (TypesHelper.isIntType(t)) {
            return localIntType;
        }
        if (TypesHelper.isRealType(t)) {
            return localRealType;
        }
        if (TypesHelper.isBoolType(t)) {
            return localBoolType;
        }
        if (TypesHelper.isStringType(t)) {
            return localStringType;
        }
        return localSimpleType;
    }

    private static void transformProgram(ImProg imProg, ImTranslator translator) {
        imProg.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImNull imNull) {
                super.visit(imNull);
                if(TypesHelper.isStringType(imNull.getType())) {
                    imNull.replaceBy(JassIm.ImStringVal(""));
                }
            }

            @Override
            public void visit(ImCast imCast) {
                super.visit(imCast);
                if(TypesHelper.isIntType(imCast.getExpr().attrTyp()) && TypesHelper.isIntType(imCast.getToType())) {
                    imCast.replaceBy(imCast.getExpr().copy());
                }
            }
        });
    }
}
