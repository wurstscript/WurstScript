package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

public class EliminateLocalTypes {
    private static final ImType localSimpleType = JassIm.ImSimpleType("localSimpleType");

    public static void eliminateLocalTypesProg(ImProg imProg, ImTranslator translator) {
        // While local types are still there, perform transformation, such that the lua translator does not need to know variable types
        // null string -> "" (avoids type dependency in null translation)
        // string1 + string2 -> stringConcat(string1, string2) (avoids type dependency in operator translation)
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
                // Simple types can always be merged.
                local.setType(localSimpleType);
            }
        }
    }

    private static void transformProgram(ImProg imProg, ImTranslator translator) {
        imProg.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImOperatorCall imOperatorCall) {
                super.visit(imOperatorCall);
                ImExprs args = imOperatorCall.getArguments();
                if (imOperatorCall.getOp() == WurstOperator.PLUS) {
                    if(args.size() == 2 && TypesHelper.isStringType(args.get(0).attrTyp()) && TypesHelper.isStringType(args.get(1).attrTyp()) ) {
                        imOperatorCall.replaceBy(JassIm.ImFunctionCall(imOperatorCall.attrTrace(), translator.stringConcatFunc, JassIm.ImTypeArguments(), imOperatorCall.getArguments().copy(), false, CallType.NORMAL));
                    }
                }
            }

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
