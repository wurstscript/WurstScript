package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Maps;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.Element.DefaultVisitor;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.utils.Pair;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static de.peeeq.wurstscript.attributes.SmallHelpers.superArgs;
import static de.peeeq.wurstscript.jassIm.JassIm.*;

public class ClassTranslator {

    private ClassDef classDef;
    private ImTranslator translator;
    //	/** list of statements to initialize a new object **/
    final private List<Pair<ImVar, VarInitialization>> dynamicInits;
    private ImClass imClass;
    private ImProg prog;

    public ClassTranslator(ClassDef classDef, ImTranslator translator) {
        this.classDef = classDef;
        this.translator = translator;
        this.prog = translator.getImProg();
//		initStatements = translator.getInitStatement(classDef);
        dynamicInits = translator.getDynamicInits(classDef);
    }

    public static void translate(ClassDef classDef, ImTranslator translator) {
        new ClassTranslator(classDef, translator).translate();

        // translate inner classes:
        translateInnerClasses(classDef, translator);
    }

    private static void translateInnerClasses(ClassOrModuleOrModuleInstanciation mi, ImTranslator translator) {
        for (ModuleInstanciation mi2 : mi.getModuleInstanciations()) {
            translateInnerClasses(mi2, translator);
        }
        for (ClassDef innerClass : mi.getInnerClasses()) {
            translate(innerClass, translator);
        }
    }

    /**
     * translates the given classDef
     */
    private void translate() {
        imClass = translator.getClassFor(classDef);
        prog.getClasses().add(imClass);

        addSuperClasses();

        List<ClassDef> subClasses = translator.getSubClasses(classDef);

        // order is important here
        translateMethods(classDef, subClasses);
        translateVars(classDef);
        translateConstructors();
        createOnDestroyMethod();
        createDestroyMethod(subClasses);

    }


    private void addSuperClasses() {
        if (classDef.getExtendedClass() instanceof TypeExpr) {
            TypeExpr extended = (TypeExpr) classDef.getExtendedClass();
            addSuperClass(extended);
        }
        for (TypeExpr impl : classDef.getImplementsList()) {
            addSuperClass(impl);
        }

    }

    private void addSuperClass(TypeExpr extended) {
        if (extended.attrTypeDef() instanceof StructureDef) {
            StructureDef sc = (StructureDef) extended.attrTypeDef();
            imClass.getSuperClasses().add(translator.getClassFor(sc));
        }
    }

    private void createDestroyMethod(List<ClassDef> subClasses) {
        ImMethod m = translator.destroyMethod.getFor(classDef);
        imClass.getMethods().add(m);
        ImFunction f = translator.destroyFunc.getFor(classDef);

        // set sub methods
        for (ClassDef sc : subClasses) {
            ImMethod dm = translator.destroyMethod.getFor(sc);
            if (hasOwnDestroy(sc, classDef)) {
                m.getSubMethods().add(dm);
            }
        }

        Element trace = classDef.getOnDestroy();

        ImVar thisVar = f.getParameters().get(0);

        // call ondestroy methods
        ClassDef c = classDef;
        ImFunction scOnDestroy = translator.getFuncFor(c.getOnDestroy());
        f.getBody().add(ImFunctionCall(trace,
                scOnDestroy,
                ImExprs(ImVarAccess(thisVar)), false, CallType.NORMAL));

        // deallocate
        f.getBody().add(JassIm.ImDealloc(imClass, JassIm.ImVarAccess(thisVar)));
    }

    /**
     *
     */
    private boolean hasOwnDestroy(ClassDef sc, ClassDef classDef2) {
        if (sc == classDef2) {
            return false;
        }
        if (sc.getOnDestroy().attrHasEmptyBody()) {
            WurstTypeClass superClass = (WurstTypeClass) sc.getExtendedClass().attrTyp();
            return hasOwnDestroy(superClass.getClassDef(), classDef2);
        } else {
            return true;
        }
    }

    private void createOnDestroyMethod() {
        OnDestroyDef onDestroy = classDef.getOnDestroy();
        ImFunction f = translator.getFuncFor(onDestroy);
        addOnDestroyActions(f, f.getBody(), classDef, translator.getThisVar(onDestroy));
    }

    private void addOnDestroyActions(ImFunction f, List<ImStmt> addTo, ClassOrModuleInstanciation c, ImVar thisVar) {
        // translate ondestroy statements
        List<ImStmt> stmts = translator.translateStatements(f, c.getOnDestroy().getBody());
        replaceThisExpr(stmts, translator.getThisVar(c.getOnDestroy()), thisVar);
        addTo.addAll(stmts);

        // add onDestroy actions from modules
        for (ModuleInstanciation mi : c.getModuleInstanciations()) {
            addOnDestroyActions(f, addTo, mi, thisVar);
        }

        if (c instanceof ClassDef) {
            ClassDef cd = (ClassDef) c;
            WurstTypeClass ct = cd.attrTypC();
            if (ct.extendedClass() != null) {
                // call onDestroy of super class
                ImFunction onDestroy = translator.getFuncFor(ct.extendedClass().getClassDef().getOnDestroy());
                addTo.add(ImFunctionCall(c,
                        onDestroy,
                        ImExprs(ImVarAccess(thisVar)), false, CallType.NORMAL));
            }
        }
    }

    private void replaceThisExpr(List<ImStmt> stmts, final ImVar oldThis, final ImVar newThis) {
        if (oldThis == newThis) {
            return;
        }
        DefaultVisitor replacer = new DefaultVisitor() {
            @Override
            public void visit(ImVarAccess v) {
                super.visit(v);
                if (v.getVar() == oldThis) {
                    v.setVar(newThis);
                }
            }

        };
        for (ImStmt s : stmts) {
            s.accept(replacer);
        }

    }

    private void translateConstructors() {
        // translate constructors of module instantiations:
        for (ModuleInstanciation mi : classDef.getModuleInstanciations()) {
            translateModuleConstructors(mi);
        }

        for (ConstructorDef c : classDef.getConstructors()) {
            translateConstructor(c);
        }

    }


    private void translateModuleConstructors(ModuleInstanciation mi) {
        for (ModuleInstanciation child : mi.getModuleInstanciations()) {
            translateModuleConstructors(child);
        }

        for (ConstructorDef c : mi.getConstructors()) {
            translateModuleConstructor(c, mi);
        }
    }


    private void translateVars(ClassOrModuleInstanciation c) {
        for (ModuleInstanciation mi : c.getModuleInstanciations()) {
            translateVars(mi);
        }
        for (GlobalVarDef v : c.getVars()) {
            translateVar(v);
        }
    }

    private void translateVar(GlobalVarDef s) {
        ImVar v = translator.getVarFor(s);
        if (s.attrIsDynamicClassMember()) {
            // for dynamic class members create an array
            ImType t = s.attrTyp().imTranslateType();
            v.setType(ImHelper.toArray(t));
            dynamicInits.add(Pair.create(v, s.getInitialExpr()));
        } else { // static class member
            translator.addGlobalInitalizer(v, classDef.attrNearestPackage(), s.getInitialExpr());
        }
        translator.addGlobal(v);
    }

    private void translateMethods(ClassOrModuleInstanciation c, List<ClassDef> subClasses) {
        for (FuncDef f : c.getMethods()) {
            translateMethod(f, subClasses);
        }
        for (ModuleInstanciation mi : c.getModuleInstanciations()) {
            translateMethods(mi, subClasses);
        }
    }

    private void translateMethod(FuncDef s, List<ClassDef> subClasses) {
        ImFunction f = createStaticCallFunc(s);
        if (s.attrIsStatic()) {
            // static method
        } else {
            // dynamic method
            ImMethod m = translator.getMethodFor(s);
            imClass.getMethods().add(m);
            m.setImplementation(f);
            m.setIsAbstract(s.attrIsAbstract());
            // set sub methods
            Map<ClassDef, FuncDef> subClasses2 = translator.getClassesWithImplementation(subClasses, s);
            for (Entry<ClassDef, FuncDef> subE : subClasses2.entrySet()) {
                FuncDef subM = subE.getValue();
                ClassDef subC = subE.getKey();

                WurstTypeClass ct = getExtendedClassType(subC);
                VariableBinding typeBinding;
                if (ct == null) {
                    typeBinding = VariableBinding.emptyMapping();
                } else {
                    typeBinding = ct.getTypeArgBinding();
                }

                ImClass subClass = translator.getClassFor(subC);
                ImMethod subMethod = translator.getMethodFor(subM);


                OverrideUtils.addOverride(translator, s, subClass, subMethod, subM, typeBinding);
//				m.getSubMethods().add(translator.getMethodFor(subM));
            }
        }
    }

    private WurstTypeClass getExtendedClassType(ClassDef subC) {
        if (subC.getExtendedClass() == null) {
            return null;
        }
        WurstType t = subC.getExtendedClass().attrTyp();
        if (t instanceof WurstTypeClass) {
            WurstTypeClass ct = (WurstTypeClass) t;
            ClassDef superClass = ct.getClassDef();
            if (superClass == classDef) {
                return ct;
            } else {
                WurstTypeClass t2 = getExtendedClassType(superClass);
                assert t2 != null;
                t2.setTypeArgs(ct.getTypeArgBinding());
                return t2;
            }

        }
        return null;
    }

    private ImFunction createStaticCallFunc(FuncDef funcDef) {
        ImFunction f = translator.getFuncFor(funcDef);
        f.getBody().addAll(translator.translateStatements(f, funcDef.getBody()));
        // TODO add return for abstract function
        if (funcDef.attrIsAbstract() && !(funcDef.attrReturnType() instanceof WurstTypeVoid)) {
            f.getBody().add(ImReturn(funcDef, funcDef.attrReturnType().getDefaultValue()));
        }
        return f;
    }


    private void translateConstructor(ConstructorDef constr) {
        createNewFunc(constr);
        createConstructFunc(constr);
    }


    private void createNewFunc(ConstructorDef constr) {
        ConstructorDef trace = constr;
        ImFunction f = translator.getConstructNewFunc(constr);
        Map<ImVar, ImVar> varReplacements = Maps.newLinkedHashMap();

        for (WParameter p : constr.getParameters()) {
            ImVar imP = ImVar(p, p.attrTyp().imTranslateType(), p.getName(), false);
            varReplacements.put(translator.getVarFor(p), imP);
            f.getParameters().add(imP);
        }


        ImVar thisVar = JassIm.ImVar(constr, TypesHelper.imInt(), "this", false);
        varReplacements.put(translator.getThisVar(constr), thisVar);
        f.getLocals().add(thisVar);

        // allocate class
        f.getBody().add(ImSet(trace, ImVarAccess(thisVar), JassIm.ImAlloc(imClass)));

        // call user defined constructor code:
        ImFunction constrFunc = translator.getConstructFunc(constr);
        ImExprs arguments = ImExprs(ImVarAccess(thisVar));
        for (ImVar a : f.getParameters()) {
            arguments.add(ImVarAccess(a));
        }
        f.getBody().add(ImFunctionCall(trace, constrFunc, arguments, false, CallType.NORMAL));


        // return this
        f.getBody().add(ImReturn(trace, ImVarAccess(thisVar)));

    }


    private void createConstructFunc(ConstructorDef constr) {
        ConstructorDef trace = constr;
        ImFunction f = translator.getConstructFunc(constr);
        ImVar thisVar = translator.getThisVar(constr);
        ConstructorDef superConstr = constr.attrSuperConstructor();
        if (superConstr != null) {
            // call super constructor
            ImFunction superConstrFunc = translator.getConstructFunc(superConstr);
            ImExprs arguments = ImExprs(ImVarAccess(thisVar));
            for (Expr a : superArgs(constr)) {
                arguments.add(a.imTranslateExpr(translator, f));
            }
            f.getBody().add(ImFunctionCall(trace, superConstrFunc, arguments, false, CallType.NORMAL));
        }
        // initialize vars
        for (Pair<ImVar, VarInitialization> i : translator.getDynamicInits(classDef)) {
            ImVar v = i.getA();
            if (i.getB() instanceof Expr) {
                Expr e = (Expr) i.getB();
                ImStmt s = ImSet(trace, ImVarArrayAccess(trace, v, ImExprs((ImExpr) ImVarAccess(thisVar))), e.imTranslateExpr(translator, f));
                f.getBody().add(s);
            } else if (i.getB() instanceof ArrayInitializer) {
                ArrayInitializer ai = (ArrayInitializer) i.getB();
                int index = 0;
                for (Expr e : ai.getValues()) {
                    ImStmt s = ImSet(trace, ImVarArrayAccess(trace, v, ImExprs(ImVarAccess(thisVar), JassIm.ImIntVal(index))), e.imTranslateExpr(translator, f));
                    f.getBody().add(s);
                    index++;
                }
            }
        }
        // add initializers from modules
        for (ModuleInstanciation mi : classDef.getModuleInstanciations()) {
            addModuleInits(f, mi, thisVar);
        }
        // constructor user code
        f.getBody().addAll(translator.translateStatements(f, constr.getBody()));
    }

    private void addModuleInits(ImFunction f, ModuleInstanciation mi, ImVar thisVar) {
        // call constructors of used modules:
        for (ConstructorDef c : mi.getConstructors()) {
            ImFunction moduleConstr = translator.getConstructFunc(c);
            f.getBody().add(JassIm.ImFunctionCall(c, moduleConstr, JassIm.ImExprs(JassIm.ImVarAccess(thisVar)), false, CallType.NORMAL));
        }
    }


    private void translateModuleConstructor(ConstructorDef constr, ModuleInstanciation mi) {
        ImFunction f = translator.getConstructFunc(constr);
        ImVar thisVar = translator.getThisVar(constr);
        // first call constructors of children:
        for (ModuleInstanciation child : mi.getModuleInstanciations()) {
            addModuleInits(f, child, thisVar);
        }

        // constructor user code
        f.getBody().addAll(translator.translateStatements(f, constr.getBody()));


    }


}
