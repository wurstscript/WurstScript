package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeClassOrInterface;

import javax.sound.midi.Receiver;
import java.util.stream.Collectors;

public class TypeClassTranslator {

    public static void translateClassOrInterface(ClassOrInterface def, ImTranslator tr) {
        ImClass dict = tr.getTypeClassStructFor(def);
        tr.imProg().getClasses().add(dict);

        WurstTypeClassOrInterface wurstType = (WurstTypeClassOrInterface) def.attrTyp();

        // add super types:
        for (WurstTypeClassOrInterface directSupertype : wurstType.directSupertypes()) {
            ImClassType imSuperType = directSupertype.imTranslateToTypeClass(tr);
            dict.getSuperClasses().add(imSuperType);
        }

        // add methods
        for (FuncDef method : def.getMethods()) {
            ImMethod imMethod = tr.getTypeClassMethodFor(method);
            dict.getMethods().add(imMethod);

            // TODO add overrides


        }

        // add functions
        for (FuncDef method : def.getMethods()) {
            ImFunction imFunc = tr.getTypeClassFuncFor(method);
            dict.getFunctions().add(imFunc);

            ImTypeArguments typeArgs = imFunc.getTypeVariables().stream()
                .map(tv -> JassIm.ImTypeArgument(JassIm.ImTypeVarRef(tv)))
                .collect(Collectors.toCollection(JassIm::ImTypeArguments));
            ImExprs args = imFunc.getParameters().stream()
                .skip(1) // skip dict-this parameter
                .map(JassIm::ImVarAccess)
                .collect(Collectors.toCollection(JassIm::ImExprs));


            ImExpr result;
            // add method call for the real method
            if (method.attrIsStatic()) {
                ImFunction originalFunc = tr.getFuncFor(method);
                result = JassIm.ImFunctionCall(method, originalFunc, typeArgs, args, false, CallType.NORMAL);
            } else {
                ImMethod m = tr.getMethodFor(method);
                ImExpr receiver = args.remove(0);
                result = JassIm.ImMethodCall(method, m, typeArgs, receiver, args, false);

            }
            if (imFunc.getReturnType() instanceof ImVoid) {
                imFunc.getBody().add(result);
            } else {
                imFunc.getBody().add(JassIm.ImReturn(method, result));
            }
        }
    }
}
