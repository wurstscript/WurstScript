package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ClosureTranslator {
    private final ExprClosure e;
    private final ImTranslator tr;
    private final ImFunction f;

    private final Map<ImVar, ImVar> closureVars = Maps.newLinkedHashMap();
    private ImFunction impl;

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
            ImVar clVar = JassIm.ImVar(e, WurstTypeInt.instance().imTranslateType(), "clVar", false);
            f.getLocals().add(clVar);
            ImStmts stmts = JassIm.ImStmts();
            // allocate closure
            stmts.add(JassIm.ImSet(e, JassIm.ImVarAccess(clVar), JassIm.ImAlloc(c)));
            callSuperConstructor(clVar, stmts, c);
            // set closure vars
            for (Entry<ImVar, ImVar> entry : closureVars.entrySet()) {
                ImVar orig = entry.getKey();
                ImVar v = entry.getValue();
                stmts.add(JassIm.ImSet(e, JassIm.ImVarArrayAccess(e, v, JassIm.ImExprs((ImExpr) JassIm.ImVarAccess(clVar))), JassIm.ImVarAccess(orig)));
            }
            return JassIm.ImStatementExpr(stmts, JassIm.ImVarAccess(clVar));
        }
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
        stmts.add(JassIm.ImFunctionCall(e, cn, arguments, false, CallType.NORMAL));
    }


    private ImExpr translateAnonFunc() {
        impl = tr.getFuncFor(e);
        impl.getParameters().clear();
        ImExpr translated = e.getImplementation().imTranslateExpr(tr, impl);

        verifyTranslatedAnonfunc(translated);

        if (e.getImplementation().attrTyp() instanceof WurstTypeBool) {
            impl.getBody().add(JassIm.ImReturn(e, translated));
            impl.setReturnType(WurstTypeBool.instance().imTranslateType());
        } else {
            impl.getBody().add(translated);
            impl.setReturnType(WurstTypeVoid.instance().imTranslateType());
        }
        return JassIm.ImFuncRef(impl);
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
        ImClass superClass = getSuperClass();
        FuncDef superMethod = getSuperMethod();


        ImVars fields = JassIm.ImVars();
        ImMethods methods = JassIm.ImMethods();
        List<ImClass> superClasses = java.util.Collections.singletonList(superClass);
        ImClass c = JassIm.ImClass(e, "Closure", fields, methods, superClasses);
        tr.imProg().getClasses().add(c);

//		ImVars parameters = JassIm.ImVars();
//		parameters.add(tr.getThisVar(e));
//		for (WParameter p : e.getParameters()) {
//			parameters.add(tr.getVarFor(p));
//		}
//		ImType returnType = e.getImplementation().attrTyp().imTranslateType();
//		if (returnType == null) {
//			WLogger.info(e.attrTyp());
//			returnType = JassIm.ImVoid();
//		}
//		ImVars locals = JassIm.ImVars();
//		ImStmts body = JassIm.ImStmts();
//		List<FunctionFlag> flags = Collections.emptyList();
//		ImFunction impl JassIm.ImFunction(e, superMethod.getName(), parameters, returnType, locals, body, flags);
        impl = tr.getFuncFor(e);
        ImMethod m = JassIm.ImMethod(e, superMethod.getName(), impl, JassIm.ImMethods(), false);
        c.getMethods().add(m);

        OverrideUtils.addOverrideClosure(tr, superMethod, m, e);


        ImExpr translated = e.getImplementation().imTranslateExpr(tr, impl);


        if (e.getImplementation().attrTyp().isVoid()) {
            impl.getBody().add(translated);
        } else {
            impl.getBody().add(JassIm.ImReturn(e, translated));
        }
        transformTranslated(translated);
        return c;
    }


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
            va.replaceBy(JassIm.ImVarArrayAccess(e, v, JassIm.ImExprs(closureThis())));
        }
    }

    private ImExpr closureThis() {
        return JassIm.ImVarAccess(tr.getThisVar(e));
    }


    private ImVar getClosureVarFor(ImVar var) {
        ImVar v = closureVars.get(var);
        if (v == null) {
            v = JassIm.ImVar(e, JassIm.ImArrayType(var.getType()), var.getName(), false);
            tr.imProg().getGlobals().add(v);
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


    private ImClass getSuperClass() {
        WurstType t = e.attrExpectedTyp();
        if (t instanceof WurstTypeInterface) {
            WurstTypeInterface it = (WurstTypeInterface) t;
            return tr.getClassFor(it.getDef());
        } else if (t instanceof WurstTypeClass) {
            WurstTypeClass ct = (WurstTypeClass) t;
            return tr.getClassFor(ct.getDef());
        }
        throw new CompileError(e.getSource(), "Could not get super class for closure");
    }


}
