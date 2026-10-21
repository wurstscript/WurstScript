package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrInterface;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.names.DefLink;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameResolution;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtojass.ImAttrType;
import de.peeeq.wurstscript.types.VariableBinding;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeClassOrInterface;
import de.peeeq.wurstscript.types.WurstTypeInterface;
import de.peeeq.wurstscript.validation.WurstValidator;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
            if (subM.attrNearestClassDef() == null) {
                // the default of an interface extending this one, which the class takes for f
                m = methodOfItsOwn(mClass, subCT, m, subM);
            } else if (f.attrHasEmptyBody() && !subClasses.contains(subM.attrNearestClassDef())) {
                WurstTypeClass adopter = adopterOf(subCT, subM.attrNearestClassDef(), subClasses);
                FuncDef interfaceDefault = WurstValidator.subInterfaceDefaultOf(adopter.getClassDef(), f);
                if (interfaceDefault == null) {
                    interfaceDefault = defaultOf(adopter, f);
                }
                m = methodOfItsOwn(mClass, subCT, m, interfaceDefault == null ? subM : interfaceDefault);
            }
            OverrideUtils.addOverride(translator, f, mClass, m, subM, typeBinding);
        }

    }

    /**
     * The class which adopted the method {@code classType} inherits from {@code owner} as its implementation of this
     * interface: the highest class from {@code classType} up to, not including, {@code owner} which implements the
     * interface. Its interfaces decide whether a default beats the inherited method, and the classes below it inherit
     * that choice, as the validator expects ({@code WurstValidator.findImplementationLink}). The default is that of a
     * sub-interface, generic or not, else one of another interface ({@link #defaultOf}).
     */
    private static WurstTypeClass adopterOf(WurstTypeClass classType, ClassDef owner, List<ClassDef> instances) {
        WurstTypeClass adopter = classType;
        for (WurstTypeClass t = classType.extendedClass(); t != null && t.getClassDef() != owner; t = t.extendedClass()) {
            if (instances.contains(t.getClassDef())) {
                adopter = t;
            }
        }
        return adopter;
    }

    /**
     * A method of {@code imClass} itself for the abstract method of this interface, which the class implements with a
     * method it inherits from a class outside the interface ({@code C extends Base implements Omega}). The dispatch
     * over the implementations of the interface method follows the classes below the interface and takes at each the
     * method declared in that class, and so does the interpreter: Base is not on that path, so C gets a method of its
     * own with Base's implementation, which its subclasses inherit. The overrides below C are its sub-methods, as they
     * are Base's ({@link ImTranslator#linkOverridesBelow}). Where another interface of C gives m a default, the
     * default is the implementation: a default beats an inherited method (a call through that interface runs it on
     * every backend), and Lua binds one implementation for C to both. C also gets a method of its own where it takes m
     * from the default of an interface extending this one, which belongs to that interface: that default is the
     * implementation the validator chose ({@code WurstValidator.findImplementationLink}), not the first default found.
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
     * function is. Type parameters the implementation declares itself ({@code id<T:>}) are not Base's: the function
     * declares a copy of each and passes them to the implementation, so a call through the interface specialises it
     * with the call's type arguments, whatever Base's are. The translator adds it to the class
     * ({@link ImTranslator#bridgeFunction}).
     */
    private ImFunction implementationOfItsOwn(ImClass imClass, WurstTypeClass classType, FuncDef implementation,
                                              ImFunction inherited, @org.eclipse.jdt.annotation.Nullable ClassOrInterface owner) {
        List<ImTypeVar> variables = new java.util.ArrayList<>();
        List<ImTypeArgument> substitutes = new java.util.ArrayList<>();
        if (owner != null) {
            List<ImTypeVar> ownerVariables = translator.getClassFor(owner).getTypeVariables();
            variables.addAll(ownerVariables);
            substitutes.addAll(typeArgumentsAsSeenFrom(classType, owner, ownerVariables));
        }
        // The implementation's own type parameters are not the class's: the function declares them again and passes
        // them on, as a call through the interface gives them.
        ImTypeVars ownVariables = JassIm.ImTypeVars();
        ImTypeArguments forwarded = JassIm.ImTypeArguments();
        for (ImTypeVar inheritedVariable : inherited.getTypeVariables()) {
            ImTypeVar ownVariable = translator.copyTypeVar(inheritedVariable);
            ownVariables.add(ownVariable);
            variables.add(inheritedVariable);
            substitutes.add(JassIm.ImTypeArgument(JassIm.ImTypeVarRef(ownVariable), Collections.emptyMap()));
            forwarded.add(JassIm.ImTypeArgument(JassIm.ImTypeVarRef(ownVariable), Collections.emptyMap()));
        }
        ImVar thisVar = JassIm.ImVar(implementation, translator.selfType(imClass), "this", false);
        ImVars parameters = JassIm.ImVars(thisVar);
        ImExprs arguments = JassIm.ImExprs(JassIm.ImVarAccess(thisVar));
        for (ImVar p : inherited.getParameters().subList(1, inherited.getParameters().size())) {
            ImVar parameter = JassIm.ImVar(p.getTrace(),
                ImAttrType.substituteType(p.getType(), substitutes, variables), p.getName(), false);
            parameters.add(parameter);
            arguments.add(JassIm.ImVarAccess(parameter));
        }
        ImType returnType = ImAttrType.substituteType(inherited.getReturnType(), substitutes, variables);
        ImExpr call = JassIm.ImFunctionCall(implementation, inherited, forwarded, arguments, false,
            CallType.NORMAL);
        ImStmts body = returnType instanceof ImVoid
            ? JassIm.ImStmts(call)
            : JassIm.ImStmts(JassIm.ImReturn(implementation, call));
        List<FunctionFlag> flags = new java.util.ArrayList<>();
        if (inherited.hasFlag(FunctionFlagEnum.IS_VARARG)) {
            flags.add(FunctionFlagEnum.IS_VARARG);
        }
        return JassIm.ImFunction(implementation, imClass.getName() + "_" + implementation.getName(),
            ownVariables, parameters, returnType, JassIm.ImVars(), body, flags);
    }

    /**
     * The type arguments for {@code variables}, the type variables of {@code owner}, a class or interface above
     * {@code classType}, as that class sees them.
     */
    private List<ImTypeArgument> typeArgumentsAsSeenFrom(WurstTypeClass classType, ClassOrInterface owner,
                                                         List<ImTypeVar> variables) {
        VariableBinding binding = VariableBinding.emptyMapping();
        ArrayDeque<WurstTypeClassOrInterface> queue = new ArrayDeque<>();
        queue.add(classType);
        while (!queue.isEmpty()) {
            WurstTypeClassOrInterface type = queue.removeFirst();
            if (type.getDef() == owner) {
                binding = type.getTypeArgBinding();
                break;
            }
            queue.addAll(type.directSupertypes());
        }
        List<ImTypeArgument> arguments = new java.util.ArrayList<>();
        for (ImTypeVar variable : variables) {
            TypeParamDef parameter = translator.getTypeParamDef(variable);
            if (parameter == null) {
                throw new CompileError(classType.getDef(), "Could not find the type argument of " + owner.getName()
                    + " for " + variable.getName() + " as " + classType.getDef().getName() + " sees it.");
            }
            // A parameter no supertype binds is one of an enclosing generic class, which a static class inside it
            // captures: the class sees it as it is, as its own functions do.
            ImType type = binding.get(parameter).map(bound -> bound.imTranslateType(translator))
                .getOrElse(() -> JassIm.ImTypeVarRef(translator.getTypeVar(parameter)));
            arguments.add(JassIm.ImTypeArgument(type, Collections.emptyMap()));
        }
        return arguments;
    }


    /**
     * The default which another interface of the class gives {@code abstractMethod}: a method of an interface which
     * is not generic, with the same name and a body, which could override the abstract method as the class sees it
     * ({@link WurstValidator#canOverride}: its own type parameters, parameter and return types), the nearest first
     * (the class's own interfaces before its superclass's, as in {@link #typeBindingOf}); null if there is none.
     */
    private static @org.eclipse.jdt.annotation.Nullable FuncDef defaultOf(WurstTypeClass classType, FuncDef abstractMethod) {
        DefLink bound = NameResolution.matchDefLinkReceiver(
            FuncLink.create(abstractMethod, abstractMethod.getParent().attrNearestScope()).withConfigDef(), classType,
            abstractMethod, false);
        if (!(bound instanceof FuncLink abstractLink)) {
            return null;
        }
        ArrayDeque<WurstTypeClassOrInterface> queue = new ArrayDeque<>();
        queue.add(classType);
        while (!queue.isEmpty()) {
            WurstTypeClassOrInterface type = queue.removeFirst();
            if (type instanceof WurstTypeInterface i && i.getDef().getTypeParameters().isEmpty()) {
                for (FuncDef candidate : i.getDef().getMethods()) {
                    if (candidate != abstractMethod && !candidate.attrHasEmptyBody()
                        && candidate.getName().equals(abstractMethod.getName())
                        && WurstValidator.canOverride(
                            FuncLink.create(candidate, candidate.getParent().attrNearestScope()).withConfigDef(),
                            abstractLink, false)) {
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
