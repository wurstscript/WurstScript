package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class ClosureTranslator {
    private final ExprClosure e;
    private final ImTranslator tr;
    private final ImFunction f;

    // local variables captured in the closure -> class variables
    private final Map<ImVar, ImVar> closureVars = Maps.newLinkedHashMap();
    // type arguments captured in the closure -> new type variables
    private Map<ImTypeVar, ImTypeVar> typeVars;
    private ImFunction impl;
    private ImClass c;

    public ClosureTranslator(ExprClosure e, ImTranslator tr, ImFunction f) {
        super();
        this.e = e;
        this.tr = tr;
        this.f = f;
    }


    public ImExpr translate() {
        if (e.attrExpectedTyp() instanceof WurstTypeCode) {
            return translateAnonFunc();
        } else {
            ImClass c = createClass();
            ImClassType ct = JassIm.ImClassType(c, getClassTypeArguments());
            closureThis().getVar().setType(ct);
            ImVar clVar = JassIm.ImVar(e, ct, "clVar", false);
            f.getLocals().add(clVar);
            ImStmts stmts = JassIm.ImStmts();
            // allocate closure
            stmts.add(JassIm.ImSet(e, JassIm.ImVarAccess(clVar), JassIm.ImAlloc(ct)));
            callSuperConstructor(clVar, stmts, c);
            // set closure vars
            for (Entry<ImVar, ImVar> entry : closureVars.entrySet()) {
                ImVar orig = entry.getKey();
                ImVar v = entry.getValue();
                stmts.add(JassIm.ImSet(e, JassIm.ImMemberAccess(e, JassIm.ImVarAccess(clVar), JassIm.ImTypeArguments(), v, JassIm.ImExprs()), JassIm.ImVarAccess(orig)));
            }
            return JassIm.ImStatementExpr(stmts, JassIm.ImVarAccess(clVar));
        }
    }

    private ImTypeArguments getClassTypeArguments() {
        ImTypeArguments res = JassIm.ImTypeArguments();
        for (ImTypeVar typeVar : typeVars.keySet()) {
            res.add(JassIm.ImTypeArgument(JassIm.ImTypeVarRef(typeVar), Collections.emptyMap()));
        }
        return res;
    }


    private void callSuperConstructor(ImVar clVar, ImStmts stmts, ImClass c) {
        WurstType t = e.attrExpectedTyp();
        if (t instanceof WurstTypeClass) {
            WurstTypeClass ct = (WurstTypeClass) t;
            ClassDef cd = ct.getClassDef();

            for (ConstructorDef constr : cd.getConstructors()) {
                if (constr.getParameters().isEmpty()) {
                    callSuperConstructor(clVar, stmts, c, constr);
                    return;
                }
            }
            throw new CompileError(e.attrErrorPos(), "Cannot construct closure. Superclass has no default constructor.");
        }
    }


    private void callSuperConstructor(ImVar clVar, ImStmts stmts, ImClass c, ConstructorDef constr) {
        ImFunction cn = tr.getConstructFunc(constr);
        ImExprs arguments = JassIm.ImExprs(JassIm.ImVarAccess(clVar));
        stmts.add(JassIm.ImFunctionCall(e, cn, JassIm.ImTypeArguments(), arguments, false, CallType.NORMAL));
    }


    private ImExpr translateAnonFunc() {
        impl = tr.getFuncFor(e);
        impl.getParameters().clear();
        ImExpr translated = e.getImplementation().imTranslateExpr(tr, impl);

        verifyTranslatedAnonfunc(translated);

        if (e.getImplementation().attrTyp() instanceof WurstTypeBool) {
            impl.getBody().add(JassIm.ImReturn(e, translated));
            impl.setReturnType(WurstTypeBool.instance().imTranslateType(tr));
        } else {
            impl.getBody().add(translated);
            impl.setReturnType(WurstTypeVoid.instance().imTranslateType(tr));
        }
        return JassIm.ImFuncRef(e, impl);
    }


    /**
     * checks that there are  no captured variables
     */
    private void verifyTranslatedAnonfunc(ImExpr translated) {
        translated.accept(new ImExpr.DefaultVisitor() {
            @Override
            public void visit(ImVarAccess va) {
                super.visit(va);
                if (isLocalToOtherFunc(va.getVar())) {
                    throw new CompileError(va.attrTrace().attrSource(), "Anonymous functions used as 'code' cannot capture variables. Captured " + va.getVar().getName());
                }
            }

            @Override
            public void visit(ImSet s) {
                super.visit(s);
                if (isLocalToOtherFunc(s.getLeft())) {
                    throw new CompileError(s.attrTrace().attrSource(), "Anonymous functions used as 'code' cannot capture variables. Captured " + s.getLeft());
                }
            }
        });
    }


    private ImClass createClass() {
        ImClassType superClass = getSuperClass();
        FuncDef superMethod = getSuperMethod();


        c = tr.getClassForClosure(e);
        c.setName(makeClassName(superClass));
        c.setSuperClasses(singletonList(superClass));
        //JassIm.ImClass(e, "Closure", JassIm.ImTypeVars(), fields, methods, functions, superClasses);
        tr.imProg().getClasses().add(c);

        impl = tr.getFuncFor(e);
        impl.setName(makeFuncName(superMethod));
        tr.getImProg().getFunctions().remove(impl);
        c.getFunctions().add(impl);
        ImClassType methodClass = JassIm.ImClassType(c, JassIm.ImTypeArguments());
        ImMethod m = JassIm.ImMethod(e, methodClass, superMethod.getName(), impl, JassIm.ImMethods(), false);
        c.getMethods().add(m);

        OverrideUtils.addOverrideClosure(tr, superMethod, m, e);


        ImExpr translated = e.getImplementation().imTranslateExpr(tr, impl);


        if (e.getImplementation().attrTyp().isVoid()) {
            impl.getBody().add(translated);
        } else {
            impl.getBody().add(JassIm.ImReturn(e, translated));
        }
        transformTranslated(translated);

        typeVars = rewriteTypeVars(c);
        return c;
    }

    private String makeClassName(ImClassType superClass) {
        String res =
                superClass.getClassDef().getName()
                        + "_line" + e.attrSource().getLine();
        return addScopeNames(res);
    }

    private String makeFuncName(FuncDef superClass) {
        String res = superClass.getName() + "_line" + e.attrSource().getLine();
        return addScopeNames(res);
    }

    private String addScopeNames(String res) {
        Element elem = e;
        while (elem != null) {
            if (elem instanceof NamedScope) {
                res = ((NamedScope) elem).getName() + "_" + res;
            }
            elem = elem.getParent();
        }
        return res;
    }

    /**
     * Replaces all captured type variables with new type variables.
     * And adds these type variables to the closure-class c.
     */
    private Map<ImTypeVar, ImTypeVar> rewriteTypeVars(ImClass c) {
        Map<ImTypeVar, ImTypeVar> result = new LinkedHashMap<>();
        c.accept(new ImClass.DefaultVisitor() {
            @Override
            public void visit(ImVar e) {
                super.visit(e);
                e.setType(rewriteType(e.getType()));
            }

            @Override
            public void visit(ImFunction e) {
                super.visit(e);
                e.setReturnType(rewriteType(e.getReturnType()));
            }

            @Override
            public void visit(ImNull e) {
                super.visit(e);
                e.setType(rewriteType(e.getType()));
            }

            @Override
            public void visit(ImTypeArgument e) {
                super.visit(e);
                e.setType(rewriteType(e.getType()));
            }

            @Override
            public void visit(ImClass e) {
                super.visit(e);
                List<ImClassType> newSuperClasses = e.getSuperClasses().stream()
                        .map(tt -> (ImClassType) rewriteType(tt))
                        .collect(Collectors.toList());
                e.setSuperClasses(newSuperClasses);
            }

            @Override
            public void visit(ImMethod e) {
                super.visit(e);
                e.setMethodClass((ImClassType) rewriteType(e.getMethodClass()));
            }

            @Override
            public void visit(ImAlloc e) {
                super.visit(e);
                e.setClazz((ImClassType) rewriteType(e.getClazz()));
            }

            @Override
            public void visit(ImDealloc e) {
                super.visit(e);
                e.setClazz((ImClassType) rewriteType(e.getClazz()));
            }

            @Override
            public void visit(ImInstanceof e) {
                super.visit(e);
                e.setClazz((ImClassType) rewriteType(e.getClazz()));
            }

            @Override
            public void visit(ImTypeIdOfObj e) {
                super.visit(e);
                e.setClazz((ImClassType) rewriteType(e.getClazz()));
            }

            @Override
            public void visit(ImTypeIdOfClass e) {
                super.visit(e);
                e.setClazz((ImClassType) rewriteType(e.getClazz()));
            }

            private ImType rewriteType(ImType type) {
                return type.match(new ImType.Matcher<ImType>() {
                    @Override
                    public ImType case_ImTupleType(ImTupleType t) {
                        return JassIm.ImTupleType(
                                t.getTypes().stream()
                                        .map(tt -> rewriteType(tt))
                                        .collect(Collectors.toList()),
                                t.getNames());
                    }

                    @Override
                    public ImType case_ImVoid(ImVoid t) {
                        return t;
                    }

                    @Override
                    public ImType case_ImClassType(ImClassType t) {
                        if (t.getTypeArguments().isEmpty()) {
                            return t;
                        }
                        return JassIm.ImClassType(
                                t.getClassDef(),
                                t.getTypeArguments().stream()
                                        .map(tt -> JassIm.ImTypeArgument(rewriteType(tt.getType()), tt.getTypeClassBinding()))
                                        .collect(Collectors.toCollection(JassIm::ImTypeArguments)));

                    }

                    @Override
                    public ImType case_ImArrayTypeMulti(ImArrayTypeMulti t) {
                        return JassIm.ImArrayTypeMulti(rewriteType(t.getEntryType()), t.getArraySize());
                    }

                    @Override
                    public ImType case_ImSimpleType(ImSimpleType t) {
                        return t;
                    }

                    @Override
                    public ImType case_ImArrayType(ImArrayType t) {
                        return JassIm.ImArrayType(rewriteType(t.getEntryType()));
                    }

                    @Override
                    public ImType case_ImTypeVarRef(ImTypeVarRef t) {
                        ImTypeVar oldTypevar = t.getTypeVariable();
                        ImTypeVar newTypevar = result.get(oldTypevar);
                        if (newTypevar == null) {
                            newTypevar = JassIm.ImTypeVar(oldTypevar.getName() + "_captured");
                            result.put(oldTypevar, newTypevar);
                            c.getTypeVariables().add(newTypevar);
                        }
                        return JassIm.ImTypeVarRef(newTypevar);
                    }
                });
            }
        });
        return result;
    }


    /**
     * all uses of local variables are changed to use the
     * class variables instead
     */
    private void transformTranslated(ImExpr t) {
        final List<ImVarAccess> vas = Lists.newArrayList();
        t.accept(new ImExpr.DefaultVisitor() {
            @Override
            public void visit(ImVarAccess va) {
                super.visit(va);
                if (isLocalToOtherFunc(va.getVar())) {
                    vas.add(va);
                }
            }


        });

        for (ImVarAccess va : vas) {
            ImVar v = getClosureVarFor(va.getVar());
            va.replaceBy(JassIm.ImMemberAccess(e, closureThis(), JassIm.ImTypeArguments(), v, JassIm.ImExprs()));
        }
    }

    private ImVarAccess closureThis() {
        return JassIm.ImVarAccess(tr.getThisVar(e));
    }


    private ImVar getClosureVarFor(ImVar var) {
        ImVar v = closureVars.get(var);
        if (v == null) {
            v = JassIm.ImVar(e, var.getType(), var.getName(), false);
            c.getFields().add(v);
            closureVars.put(var, v);
        }
        return v;
    }


    private boolean isLocalToOtherFunc(ImVar v) {
        if (v.getParent() == null
                || v.getParent().getParent() == null) {
            return false;
        }
        if (v.getParent().getParent() instanceof ImFunction) {
            boolean r = v.getParent().getParent() != impl;
            return r;
        }
        return false;
    }

    private boolean isLocalToOtherFunc(ImLExpr e) {
        if (e instanceof ImVarAccess) {
            return isLocalToOtherFunc(((ImVarAccess) e).getVar());
        } else if (e instanceof ImTupleSelection) {
            ImTupleSelection ts = (ImTupleSelection) e;
            return isLocalToOtherFunc((ImLExpr) ts.getTupleExpr());
        }
        return false;
    }


    private FuncDef getSuperMethod() {
        NameLink nl = e.attrClosureAbstractMethod();
        return (FuncDef) nl.getDef();
    }


    private ImClassType getSuperClass() {
        // since the expected type is just an approximation, we calculate the exact type here again:
        WurstTypeClassOrInterface t = (WurstTypeClassOrInterface) e.attrExpectedTyp();
        ClassOrInterface classDef = t.getDef();
        t = (WurstTypeClassOrInterface) classDef.attrTyp();
        fj.data.List<TypeParamDef> typeParameters = fj.data.List.iterableList(classDef.getTypeParameters());
        VariableBinding mapping = VariableBinding.emptyMapping()
                .withTypeVariables(typeParameters);
        WurstType closureType = e.attrTyp();
        VariableBinding mapping2 = closureType.matchAgainstSupertype(t, e, mapping, VariablePosition.RIGHT);
        if (mapping2 == null) {
            throw new CompileError(e, "Could not translate closure: type " + closureType + " does not match " + t);
        }
        WurstType t2 = t.setTypeArgs(mapping2);
        return (ImClassType) t2.imTranslateType(tr);
    }


}
