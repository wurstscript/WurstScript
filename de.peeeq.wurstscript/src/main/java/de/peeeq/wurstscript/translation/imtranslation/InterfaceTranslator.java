package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.VariableBinding;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeInterface;
import de.peeeq.wurstscript.types.WurstTypeNamedScope;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class InterfaceTranslator {

    private final InterfaceDef interfaceDef;
    private final ImTranslator translator;
    private final ImClass imClass;

    public InterfaceTranslator(InterfaceDef interfaceDef, ImTranslator translator) {
        this.interfaceDef = interfaceDef;
        this.translator = translator;
        imClass = translator.getClassFor(interfaceDef);
    }

    public void translate() {
        translator.getImProg().getClasses().add(imClass);

        // set super-classes
        for (TypeExpr ext : interfaceDef.getExtendsList()) {
            imClass.getSuperClasses().add((ImClassType) ext.attrTyp().imTranslateType(translator));
        }

        // create dispatch methods
        for (FuncDef f : interfaceDef.getMethods()) {
            translateInterfaceFuncDef(f);
        }

        // add destroy method
        addDestroyMethod();
    }

    public void addDestroyMethod() {
        ImMethod m = translator.destroyMethod.getFor(interfaceDef);
        imClass.getMethods().add(m);

        List<ClassDef> subClasses = Lists.newArrayList(translator.getInterfaceInstances(interfaceDef));

        // set sub methods
        for (ClassDef sc : subClasses) {
            ImMethod dm = translator.destroyMethod.getFor(sc);
            m.getSubMethods().add(dm);
        }

        // deallocate
        ImFunction f = translator.destroyFunc.getFor(interfaceDef);
        ImVar thisVar = f.getParameters().get(0);
        f.getBody().add(JassIm.ImDealloc(interfaceDef, imClassType(), JassIm.ImVarAccess(thisVar)));
    }

    private ImClassType imClassType() {
        ImTypeArguments typeArgs = imClass.getTypeVariables().stream()
                .map(tv -> JassIm.ImTypeArgument(JassIm.ImTypeVarRef(tv), Collections.emptyMap()))
                .collect(Collectors.toCollection(JassIm::ImTypeArguments));
        return JassIm.ImClassType(imClass, typeArgs);
    }

    private void translateInterfaceFuncDef(FuncDef f) {
        ImMethod imMeth = translator.getMethodFor(f);
        ImFunction imFunc = translator.getFuncFor(f);
        imClass.getMethods().add(imMeth);

        // translate implementation
        if (f.attrHasEmptyBody()) {
            imMeth.setIsAbstract(true);
        } else {
            // there is a default implementation
            imFunc.getBody().addAll(translator.translateStatements(imFunc, f.getBody()));
        }


        List<ClassDef> subClasses = Lists.newArrayList(translator.getInterfaceInstances(interfaceDef));
        // TODO also add extended interfaces

        // set sub methods
        Map<ClassDef, FuncDef> subClasses2 = translator.getClassesWithImplementation(subClasses, f);
        for (Entry<ClassDef, FuncDef> subE : subClasses2.entrySet()) {
            ClassDef subC = subE.getKey();
            WurstTypeClass subCT = subC.attrTypC();
            ImmutableCollection<WurstTypeInterface> interfaces = subCT.implementedInterfaces();

            VariableBinding typeBinding =
                    interfaces.stream()
                            .filter(t -> t.getDef() == interfaceDef)
                            .map(WurstTypeNamedScope::getTypeArgBinding)
                            .findFirst()
                            .orElse(VariableBinding.emptyMapping());

            FuncDef subM = subE.getValue();
            ImMethod m = translator.getMethodFor(subM);

            ImClass mClass = translator.getClassFor(subC);
            OverrideUtils.addOverride(translator, f, mClass, m, subM, typeBinding);
        }

    }

}
