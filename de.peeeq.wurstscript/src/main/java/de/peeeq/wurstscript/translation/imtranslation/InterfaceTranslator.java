package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.VariableBinding;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeClassOrInterface;
import de.peeeq.wurstscript.types.WurstTypeInterface;
import de.peeeq.wurstscript.types.WurstTypeNamedScope;

import java.util.ArrayDeque;
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
        ImTypeArguments typeArgs = JassIm.ImTypeArguments();
        for (ImTypeVar tv : imClass.getTypeVariables()) {
            ImTypeArgument imTypeArgument = JassIm.ImTypeArgument(JassIm.ImTypeVarRef(tv), Collections.emptyMap());
            typeArgs.add(imTypeArgument);
        }
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

        // set sub methods
        Map<ClassDef, FuncDef> subClasses2 = translator.getClassesWithImplementation(subClasses, f);
        for (Entry<ClassDef, FuncDef> subE : subClasses2.entrySet()) {
            ClassDef subC = subE.getKey();
            WurstTypeClass subCT = subC.attrTypC();

            VariableBinding typeBinding = typeBindingOf(subCT);

            FuncDef subM = subE.getValue();
            ImMethod m = translator.getMethodFor(subM);

            ImClass mClass = translator.getClassFor(subC);
            if (f.attrHasEmptyBody() && !subClasses.contains(subM.attrNearestClassDef())) {
                m = methodOfItsOwn(mClass, m);
            }
            OverrideUtils.addOverride(translator, f, mClass, m, subM, typeBinding);
        }

    }

    /**
     * A method of {@code imClass} itself for the abstract method of this interface, which the class implements with a
     * method it inherits from a class outside the interface ({@code C extends Base implements Omega}). The dispatch
     * over the implementations of the interface method follows the classes below the interface and takes at each the
     * method declared in that class, and so does the interpreter: Base is not on that path, so C gets a method of its
     * own with Base's implementation, which its subclasses inherit.
     */
    private ImMethod methodOfItsOwn(ImClass imClass, ImMethod inherited) {
        ImMethod own = JassIm.ImMethod(inherited.getTrace(), translator.selfType(imClass), inherited.getName(),
            inherited.getImplementation(), JassIm.ImMethods(), new java.util.ArrayList<>(), "", false);
        imClass.getMethods().add(own);
        return own;
    }

    /**
     * The type arguments a class gives this interface where it implements it: itself, through an interface which
     * extends this one, or through a superclass (the instances are the subclasses of an implementing class too). The
     * nearest declaration binds, and at the same distance the class's own interfaces before its superclass, so a
     * class which implements {@code I<real>} itself is not bound by the {@code I<int>} of its superclass. The override
     * converts its arguments from their index with them.
     */
    private VariableBinding typeBindingOf(WurstTypeClass classType) {
        ArrayDeque<WurstTypeClassOrInterface> queue = new ArrayDeque<>();
        queue.add(classType);
        while (!queue.isEmpty()) {
            WurstTypeClassOrInterface type = queue.removeFirst();
            if (type instanceof WurstTypeInterface i && i.getDef() == interfaceDef) {
                return i.getTypeArgBinding();
            }
            if (type instanceof WurstTypeClass c) {
                queue.addAll(c.implementedInterfaces());
                WurstTypeClass extended = c.extendedClass();
                if (extended != null) {
                    queue.add(extended);
                }
            } else {
                queue.addAll(type.directSupertypes());
            }
        }
        return VariableBinding.emptyMapping();
    }

}
