package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

public class EliminateLocalTypes {
    private static ImType localVarType = JassIm.ImSimpleType("localVarType");

    public static void eliminateLocalTypesProg(ImProg imProg, ImTranslator translator) {
        // While local types are still there, perform transformation specifically for strings:
        // null string -> ""
        // string1 + string2 -> stringConcat(string1, string2)
        transformStrings(imProg, translator);
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
    
    private static void transformStrings(ImProg imProg, ImTranslator translator) {
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
        });
    }
}
