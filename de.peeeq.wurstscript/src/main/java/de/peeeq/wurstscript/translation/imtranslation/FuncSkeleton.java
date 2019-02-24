package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.types.TypesHelper;

public class FuncSkeleton {

    public static void create(ConstructorDef constr, ImTranslator translator, ImFunction f) {
        f.setReturnType(TypesHelper.imInt());
        ImHelper.translateParameters(constr.getParameters(), f.getParameters(), translator);
    }

    public static void create(ExtensionFuncDef funcDef, ImTranslator translator, ImFunction f) {
        // return type:
        f.setReturnType(funcDef.attrReturnTyp().imTranslateType(translator));
        // parameters
        ImVar thisVar = translator.getThisVar(funcDef);
        thisVar.setType(funcDef.getExtendedType().attrTyp().imTranslateType(translator));
        f.getParameters().add(thisVar);
        ImHelper.translateParameters(funcDef.getParameters(), f.getParameters(), translator);
    }

    public static void create(FuncDef funcDef, ImTranslator translator, ImFunction f) {
        // return type:
        f.setReturnType(funcDef.attrReturnTyp().imTranslateType(translator));
        // parameters
        if (funcDef.attrIsDynamicClassMember()) {
            ImVar thisVar = translator.getThisVar(funcDef);
            f.getParameters().add(thisVar);
        }
        ImHelper.translateParameters(funcDef.getParameters(), f.getParameters(), translator);
    }

    public static void create(InitBlock initBlock, ImTranslator translator, ImFunction f) {
    }

    public static void create(NativeFunc funcDef, ImTranslator translator, ImFunction f) {
        f.setReturnType(funcDef.attrReturnTyp().imTranslateType(translator));
        ImHelper.translateParameters(funcDef.getParameters(), f.getParameters(), translator);
    }

    public static void create(TupleDef tupleDef, ImTranslator translator, ImFunction f) {
        // TODO Auto-generated method stub
        throw new Error("not implemented");
    }

    public static void create(OnDestroyDef onDestroyDef, ImTranslator translator, ImFunction f) {
        f.setName(onDestroyDef.attrNearestStructureDef().getName() + "_onDestroy");
        f.getParameters().add(translator.getThisVar(onDestroyDef));
    }

    public static void create(ExprClosure e, ImTranslator tr, ImFunction f) {
        String name = "closure_impl";
        NamedScope scope1 = e.attrNearestNamedScope();
        if (scope1 != null) {
            name = scope1.getName() + "_" + name;
        }
        f.setName(name);
        f.getParameters().add(tr.getThisVar(e));
        for (WShortParameter p : e.getShortParameters()) {
            f.getParameters().add(tr.getVarFor(p));
        }
        f.setReturnType(e.getImplementation().attrTyp().imTranslateType(tr));

    }

}
