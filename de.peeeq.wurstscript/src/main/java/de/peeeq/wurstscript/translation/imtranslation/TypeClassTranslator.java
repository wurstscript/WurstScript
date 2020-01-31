package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.validation.WurstValidator;

import java.util.ArrayList;
import java.util.List;

public class TypeClassTranslator {


    public static void translateTypeClass(InstanceDecl instance, ImTranslator tr) {
        ImClass c = tr.getClassFor(instance);
        tr.getImProg().getClasses().add(c);

        WurstType implementedInterface = instance.getImplementedInterface().attrTyp();
        c.getSuperClasses().add((ImClassType) implementedInterface.imTranslateType(tr));

        for (TypeParamDef tp : instance.getTypeParameters()) {
            if (tp.getTypeParamConstraints() instanceof TypeParamConstraintList) {
                for (TypeParamConstraint constraint : ((TypeParamConstraintList) tp.getTypeParamConstraints())) {
                    ImVar v = tr.getTypeClassParamFor(constraint);
                    c.getFields().add(v);
                }
            }
        }

        for (FuncDef method : instance.getMethods()) {
            translateMethod(instance, c, method, implementedInterface, tr);
        }

    }

    private static void translateMethod(InstanceDecl instance, ImClass c, FuncDef funcDef, WurstType implementedInterface, ImTranslator tr) {
        ImFunction func = tr.getFuncFor(funcDef);
        func.getBody().addAll(tr.translateStatements(func, funcDef.getBody()));

        ImMethod m = tr.getMethodFor(funcDef);
        c.getMethods().add(m);

        m.setImplementation(func);

        List<FuncLink> superMethods = new ArrayList<>();
        implementedInterface.addMemberMethods(funcDef, funcDef.getName(), superMethods);
        for (FuncLink superMethod : superMethods) {
            if (WurstValidator.canOverride(funcDef.createFuncLink(instance), superMethod, false)) {
                OverrideUtils.addOverride(tr, (FuncDef) superMethod.getDef(), c, m, funcDef, implementedInterface.getTypeArgBinding());
            }
        }

    }
}
