package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrInterface;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtojass.ImAttrType;
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
                FuncDef interfaceDefault = defaultOf(subCT, f);
                m = methodOfItsOwn(mClass, subCT, m, interfaceDefault == null ? subM : interfaceDefault);
            }
            OverrideUtils.addOverride(translator, f, mClass, m, subM, typeBinding);
        }

    }

    /**
     * A method of {@code imClass} itself for the abstract method of this interface, which the class implements with a
     * method it inherits from a class outside the interface ({@code C extends Base implements Omega}). The dispatch
     * over the implementations of the interface method follows the classes below the interface and takes at each the
     * method declared in that class, and so does the interpreter: Base is not on that path, so C gets a method of its
     * own with Base's implementation, which its subclasses inherit. The overrides below C are its sub-methods, as they
     * are Base's ({@link ImTranslator#linkOverridesBelow}). Where another interface of C gives m a default, the
     * default is the implementation: a default beats an inherited method (a call through that interface runs it on
     * every backend), and Lua binds one implementation for C to both.
     * <p>
     * Where neither C nor the class of the implementation is generic, the method runs that implementation's function.
     * Otherwise it gets a function of C which calls it ({@link #implementationOfItsOwn}).
     */
    private ImMethod methodOfItsOwn(ImClass imClass, WurstTypeClass classType, ImMethod inherited,
                                    FuncDef implementation) {
        ImFunction function = translator.getFuncFor(implementation);
        ClassOrInterface owner = implementation.attrNearestClassOrInterface();
        String name = inherited.getName();
        if (!imClass.getTypeVariables().isEmpty()
            || (owner != null && !translator.getClassFor(owner).getTypeVariables().isEmpty())) {
            ImFunction inheritedFunction = function;
            function = translator.bridgeFunction(imClass, implementation,
                () -> implementationOfItsOwn(imClass, classType, implementation, inheritedFunction, owner));
            name = function.getName();
        }
        ImMethod own = JassIm.ImMethod(inherited.getTrace(), translator.selfType(imClass), name,
            function, Lists.newArrayList(), new java.util.ArrayList<>(), "", false);
        imClass.getMethods().add(own);
        translator.linkOverridesBelow(own, inherited);
        return own;
    }

    /**
     * A function of {@code imClass} which runs the inherited implementation, for a class where one of the two is
     * generic. A method is specialised with the functions of its class, but the inherited function belongs to another
     * class and must be specialised for that class's type arguments as {@code imClass} sees them, which need not be
     * its own ({@code C<T:> extends Base<string>}). So the function calls the inherited one with this, as
     * {@code super.m()} does, and the specialisation finds Base's type arguments from the type of this. It takes the
     * inherited function's parameters with Base's type variables replaced by those arguments, and is vararg where that
     * function is. The translator adds it to the class ({@link ImTranslator#bridgeFunction}).
     */
    private ImFunction implementationOfItsOwn(ImClass imClass, WurstTypeClass classType, FuncDef implementation,
                                              ImFunction inherited, @org.eclipse.jdt.annotation.Nullable ClassOrInterface owner) {
        List<ImTypeVar> ownerVariables = owner == null
            ? Collections.emptyList() : translator.getClassFor(owner).getTypeVariables();
        List<ImTypeArgument> ownerArguments = owner == null
            ? Collections.emptyList() : typeArgumentsAsSeenFrom(classType, owner, ownerVariables);
        ImVar thisVar = JassIm.ImVar(implementation, translator.selfType(imClass), "this", false);
        ImVars parameters = JassIm.ImVars(thisVar);
        ImExprs arguments = JassIm.ImExprs(JassIm.ImVarAccess(thisVar));
        for (ImVar p : inherited.getParameters().subList(1, inherited.getParameters().size())) {
            ImVar parameter = JassIm.ImVar(p.getTrace(),
                ImAttrType.substituteType(p.getType(), ownerArguments, ownerVariables), p.getName(), false);
            parameters.add(parameter);
            arguments.add(JassIm.ImVarAccess(parameter));
        }
        ImType returnType = ImAttrType.substituteType(inherited.getReturnType(), ownerArguments, ownerVariables);
        ImExpr call = JassIm.ImFunctionCall(implementation, inherited, JassIm.ImTypeArguments(), arguments, false,
            CallType.NORMAL);
        ImStmts body = returnType instanceof ImVoid
            ? JassIm.ImStmts(call)
            : JassIm.ImStmts(JassIm.ImReturn(implementation, call));
        List<FunctionFlag> flags = new java.util.ArrayList<>();
        if (inherited.hasFlag(FunctionFlagEnum.IS_VARARG)) {
            flags.add(FunctionFlagEnum.IS_VARARG);
        }
        return JassIm.ImFunction(implementation, imClass.getName() + "_" + implementation.getName(),
            JassIm.ImTypeVars(), parameters, returnType, JassIm.ImVars(), body, flags);
    }

    /**
     * The type arguments for {@code variables}, the type variables of {@code owner}, a class or interface above
     * {@code classType}, as that class sees them: those of the type of {@code owner} among its supertypes, translated
     * as in the class's own functions. So in a static class inside a generic class, the parameter of that class is
     * the static class's captured variable, which no supertype binds.
     */
    private List<ImTypeArgument> typeArgumentsAsSeenFrom(WurstTypeClass classType, ClassOrInterface owner,
                                                         List<ImTypeVar> variables) {
        ArrayDeque<WurstTypeClassOrInterface> queue = new ArrayDeque<>();
        queue.add(classType);
        while (!queue.isEmpty()) {
            WurstTypeClassOrInterface type = queue.removeFirst();
            if (type.getDef() == owner) {
                List<ImTypeArgument> arguments = translateAsIn(classType.getDef(), type).getTypeArguments().removeAll();
                if (arguments.size() == variables.size()) {
                    return arguments;
                }
                break;
            }
            queue.addAll(type.directSupertypes());
        }
        throw new CompileError(classType.getDef(), "Could not find the type arguments of " + owner.getName()
            + " as " + classType.getDef().getName() + " sees it.");
    }

    /** {@code type} translated as in the functions of {@code c}. */
    private ImClassType translateAsIn(ClassDef c, WurstTypeClassOrInterface type) {
        Map<TypeParamDef, ImTypeVar> overrides = translator.getTypeVarOverridesForClass(c);
        translator.pushTypeVarOverrides(overrides);
        try {
            return (ImClassType) type.imTranslateType(translator);
        } finally {
            translator.popTypeVarOverrides(overrides);
        }
    }


    /**
     * The default which another interface of the class gives {@code abstractMethod}: a method of an interface which
     * is not generic, with the same name and parameter types and a body, the nearest first (the class's own
     * interfaces before its superclass's, as in {@link #typeBindingOf}); null if there is none.
     */
    private static @org.eclipse.jdt.annotation.Nullable FuncDef defaultOf(WurstTypeClass classType, FuncDef abstractMethod) {
        ArrayDeque<WurstTypeClassOrInterface> queue = new ArrayDeque<>();
        queue.add(classType);
        while (!queue.isEmpty()) {
            WurstTypeClassOrInterface type = queue.removeFirst();
            if (type instanceof WurstTypeInterface i && i.getDef().getTypeParameters().isEmpty()) {
                for (FuncDef candidate : i.getDef().getMethods()) {
                    if (candidate != abstractMethod && !candidate.attrHasEmptyBody()
                        && candidate.getName().equals(abstractMethod.getName())
                        && sameParameterTypes(candidate, abstractMethod)) {
                        return candidate;
                    }
                }
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
        return null;
    }

    private static boolean sameParameterTypes(FuncDef a, FuncDef b) {
        if (a.getParameters().size() != b.getParameters().size()) {
            return false;
        }
        for (int i = 0; i < a.getParameters().size(); i++) {
            if (!a.getParameters().get(i).attrTyp().equalsType(b.getParameters().get(i).attrTyp(), a)) {
                return false;
            }
        }
        return true;
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
