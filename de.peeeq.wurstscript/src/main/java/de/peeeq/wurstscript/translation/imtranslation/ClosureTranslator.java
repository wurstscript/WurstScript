package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtojass.TypeRewriteMatcher;
import de.peeeq.wurstscript.translation.imtojass.TypeRewriter;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.Utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
    private ImVar capturedThisField;
    // the outermost closure whose class is being created around this one, which captures the type variables
    // of both, or null
    private final ClosureTranslator outermost;
    // the closures written inside this one, at any depth, in the order they were translated; only of an outermost one
    private final List<ClosureTranslator> nested = new ArrayList<>();

    public ClosureTranslator(ExprClosure e, ImTranslator tr, ImFunction f) {
        super();
        this.e = e;
        this.tr = tr;
        this.f = f;
        ExprClosure enclosingClosure = e.getParent() == null ? null : e.getParent().attrNearestExprClosure();
        ClosureTranslator enclosing = enclosingClosure == null ? null : tr.closuresInTranslation().get(enclosingClosure);
        this.outermost = enclosing == null ? null : enclosing.outermost == null ? enclosing : enclosing.outermost;
    }


    public ImExpr translate() {
        if (e.attrExpectedTypAfterOverloading() instanceof WurstTypeCode) {
            return translateAnonFunc();
        } else {
            ImClass c = createClass();
            ImClassType ct = JassIm.ImClassType(c, getClassTypeArguments());
            ImVar clVar = JassIm.ImVar(e, ct, "clVar", false);
            f.getLocals().add(clVar);
            ImStmts stmts = JassIm.ImStmts();
            // allocate closure
            stmts.add(JassIm.ImSet(e, JassIm.ImVarAccess(clVar), JassIm.ImAlloc(e, ct)));
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
        WurstType t = e.attrExpectedTypAfterOverloading();
        if (t instanceof WurstTypeClass ct) {
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
        impl.setName("code_" + makeNameSuffix());
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
                    throw new CompileError(bestCaptureErrorPos(va),
                        "Anonymous functions used as 'code' cannot capture variables. Captured "
                            + va.getVar().getName() + closureDebugContext());
                }
            }

            @Override
            public void visit(ImSet s) {
                super.visit(s);
                if (isLocalToOtherFunc(s.getLeft())) {
                    throw new CompileError(bestCaptureErrorPos(s),
                        "Anonymous functions used as 'code' cannot capture variables. Captured "
                            + s.getLeft() + closureDebugContext());
                }
            }
        });
    }

    private String closureDebugContext() {
        String pos = "<unknown>";
        WPos p = e.attrErrorPos();
        if (isUsableSource(p)) {
            pos = p.printShort();
        }
        String nearestFunc = e.attrNearestFuncDef() == null ? "<none>" : e.attrNearestFuncDef().getName();
        String nearestPkg = e.attrNearestPackage() == null ? "<none>" : Utils.printElement(e.attrNearestPackage());
        return " [closure=" + pos
            + ", expectedType=" + e.attrExpectedTypAfterOverloading()
            + ", closureType=" + e.attrTyp()
            + ", nearestFunc=" + nearestFunc
            + ", nearestPackage=" + nearestPkg + "]";
    }

    private WPos bestCaptureErrorPos(ImStmt s) {
        WPos src = s.attrTrace().attrSource();
        if (isUsableSource(src)) {
            return src;
        }
        return e.attrErrorPos();
    }

    private WPos bestCaptureErrorPos(ImVarAccess va) {
        WPos src = va.attrTrace().attrSource();
        if (isUsableSource(src)) {
            return src;
        }
        return e.attrErrorPos();
    }

    private boolean isUsableSource(WPos pos) {
        return pos != null && pos.getLine() > 0 && pos.getFile() != null && !pos.getFile().isEmpty();
    }


    private ImClass createClass() {
        ImClassType superClass = getSuperClass();
        FuncDef superMethod = getSuperMethod();


        c = tr.getClassForClosure(e);
        c.setName(makeClassName(superClass));
        c.setSuperClasses(singletonList(superClass));
        //JassIm.ImClass(e, "Closure", JassIm.ImTypeVars(), fields, methods, functions, superClasses);
        tr.imProg().getClasses().add(c);

        tr.closuresInTranslation().put(e, this);
        impl = tr.getFuncFor(e);
        impl.setName(makeFuncName(superMethod));
        tr.getImProg().getFunctions().remove(impl);
        c.getFunctions().add(impl);
        ImClassType methodClass = JassIm.ImClassType(c, JassIm.ImTypeArguments());
        ImMethod m = JassIm.ImMethod(e, methodClass, superMethod.getName(), impl, JassIm.ImMethods(), Lists.newArrayList(), "", false);
        c.getMethods().add(m);

        OverrideUtils.addOverrideClosure(tr, superMethod, m, e);


        ImExpr translated;
        boolean propagatesExpectedType = ExprTranslation.isCompositeExpectedTypeExpression(e.getImplementation());
        if (propagatesExpectedType) {
            translated = ExprTranslation.translateWithExpectedType(
                e.getImplementation(), tr, impl, superMethod.attrReturnType());
        } else {
            translated = e.getImplementation().imTranslateExpr(tr, impl);
            WurstType translatedType = e.getImplementation().attrTypRaw();
            // A fromIndex conversion already decoded the expression. Keep other
            // generic bindings: Lua still needs them to restore primitive defaults
            // when adapting an erased result to a concrete closure return type.
            if (translatedType instanceof WurstTypeBoundTypeParam boundType
                && boundType.getFromIndex() != null) {
                translatedType = translatedType.normalize();
            }
            translated = ExprTranslation.wrapTranslation(e.getImplementation(), tr, translated,
                translatedType, superMethod.attrReturnType());
        }


        if (e.getImplementation().attrTyp().isVoid()) {
            impl.getBody().add(translated);
        } else {
            impl.getBody().add(JassIm.ImReturn(e, translated));
        }
        captureEnclosingThis();
        transformTranslated(translated);
        tr.closuresInTranslation().remove(e);

        if (outermost != null) {
            // Allocated in the enclosing closure without type arguments, which the outermost one gives it.
            outermost.nested.add(this);
            typeVars = Collections.emptyMap();
        } else {
            typeVars = rewriteTypeVars();
        }
        return c;
    }

    private String makeClassName(ImClassType superClass) {
        String name = superClass.getClassDef().getName() + makeNameSuffix();
        if (tr.getRunArgs().isLuaHotReload()) {
            // A local/global binding anchors this source closure independently of sibling translation order.
            // Include outer bindings too, so identically named locals in different enclosing closures stay apart.
            for (Element scope = e.getParent(); scope != null; scope = scope.getParent()) {
                if (scope instanceof GlobalOrLocalVarDef variable) {
                    String binding = variable.getName();
                    name += "_bound" + binding.length() + "_" + binding;
                } else if (scope instanceof TranslatedToImFunction callable && !(scope instanceof ExprClosure)) {
                    // Resolve the actual source owner; an enclosing closure's generated implementation is not its
                    // owner identity. Parameter types distinguish overloads, including same-named imported classes.
                    name += "_owner" + hotReloadOwnerIdentity(callable);
                }
            }
        }
        return name;
    }

    private String hotReloadOwnerIdentity(TranslatedToImFunction callable) {
        StringBuilder identity = new StringBuilder(callable instanceof ConstructorDef ? "constructor"
            : callable instanceof OnDestroyDef ? "destructor" : callable instanceof ExtensionFuncDef ? "extension"
            : callable instanceof InitBlock ? "init" : "function");
        // The function's own name: a function is no named scope, so without it two functions of one signature in one
        // package or class gave their closures one identity, told apart by their order across both functions.
        if (callable instanceof FunctionDefinition function) {
            identity.append("named").append(function.getName().length()).append(':').append(function.getName());
        }
        for (Element scope = callable; scope != null; scope = scope.getParent()) {
            if (scope instanceof NamedScope named) {
                String segment = named.getName();
                identity.append(segment.length()).append(':').append(segment);
            }
        }
        if (callable instanceof InitBlock) {
            // An init block is translated into its package's init function, which has no signature: the package is
            // the whole identity.
            return Hashing.sha256().hashString(identity, StandardCharsets.UTF_8).toString();
        }
        // A constructor's body is its construct function; getFuncFor would make another one from the same parameters.
        ImFunction owner = callable instanceof ConstructorDef constructor ? tr.getConstructFunc(constructor) : tr.getFuncFor(callable);
        identity.append('<').append(owner.getTypeVariables().size()).append('>')
            .append(owner.hasFlag(FunctionFlagEnum.IS_VARARG) ? "vararg" : "fixed");
        for (ImVar parameter : owner.getParameters()) {
            String type = LuaDispatchPreparation.qualifiedTypeKey(parameter.getType());
            identity.append(type.length()).append(':').append(type);
        }
        String result = LuaDispatchPreparation.qualifiedTypeKey(owner.getReturnType());
        identity.append("returns:").append(result.length()).append(':').append(result);
        return Hashing.sha256().hashString(identity, StandardCharsets.UTF_8).toString();
    }

    private String makeNameSuffix() {
        return makeNameSuffix(true);
    }

    /**
     * The scopes and calls which enclose the closure, innermost first. With {@code collapseRepeats},
     * a name which directly follows an equal one is left out, so a closure passed to a call inside
     * a closure passed to the same call is named after that call once rather than once per level.
     * <p>
     * A method call whose receiver holds the closure is not named: it comes later in a call chain such as
     * {@code new Wave().modifier(x -> x + 1).hp(100)}, so adding or removing it must not rename the closure (which a
     * Lua hot reload would take for another closure), and a long chain must not lengthen the name.
     */
    private String makeNameSuffix(boolean collapseRepeats) {
        StringBuilder sb = new StringBuilder();
        String previous = null;
        Element child = null;
        Element elem = this.e;
        while (elem != null) {
            String name = null;
            if (elem instanceof NamedScope) {
                name = ((NamedScope) elem).getName();
            } else if (elem instanceof ExprMemberMethod call && call.getLeft() == child) {
                // entered through the receiver: a later call of the chain
            } else if (elem instanceof AstElementWithFuncName) {
                name = ((AstElementWithFuncName) elem).getFuncNameId().getName();
            }
            if (name != null) {
                if (!collapseRepeats || !name.equals(previous)) {
                    sb.append("_");
                    sb.append(name);
                }
                previous = name;
            }
            child = elem;
            elem = elem.getParent();
        }
        return sb.toString();
    }

    private String makeFuncName(FuncDef superClass) {
        String name = superClass.getName();
        // LuaDispatchPreparation reads a semantic name back out of the implementation's name: what
        // precedes the first underscore, or the whole name when it starts with one - suffix included.
        // Dispatch aliases are built from that string, so such a name keeps the suffix it always had.
        return name + makeNameSuffix(!name.startsWith("_"));
    }

    /**
     * Replaces all captured type variables with new type variables of the closure's class, for the closure and the
     * closures written inside it, and returns the variables of the closure's own class.
     * <p>
     * A closure written in another one is created first, while the class of the enclosing closure has no type
     * variables yet: the inner closure names that class without type arguments, as the type of the enclosing
     * {@code this} it captures, and the enclosing closure allocates the inner one without them. So the closures
     * written inside each other capture the same type variables, all those any of them uses, in the same order, and
     * each class names another of them with its own variables.
     */
    private Map<ImTypeVar, ImTypeVar> rewriteTypeVars() {
        List<ImClass> classes = new ArrayList<>();
        classes.add(c);
        for (ClosureTranslator n : nested) {
            classes.add(n.c);
        }
        Set<ImTypeVar> captured = new LinkedHashSet<>();
        for (ImClass k : classes) {
            TypeRewriter.rewriteTypes(k, t -> {
                collectTypeVars(t, captured);
                return t;
            });
        }
        List<Map<ImTypeVar, ImTypeVar>> newVarsByClass = new ArrayList<>();
        for (ImClass k : classes) {
            Map<ImTypeVar, ImTypeVar> newVars = new LinkedHashMap<>();
            ImTypeArguments ownArguments = JassIm.ImTypeArguments();
            for (ImTypeVar oldTypevar : captured) {
                ImTypeVar newTypevar = JassIm.ImTypeVar(oldTypevar.getName() + "_captured");
                // The same parameter as the one it captures: what the closure's body reads of the enclosing
                // class, such as a static, belongs to the instantiation the closure was made for.
                tr.recordSpecialisation(newTypevar, oldTypevar);
                newVars.put(oldTypevar, newTypevar);
                k.getTypeVariables().add(newTypevar);
                ownArguments.add(JassIm.ImTypeArgument(JassIm.ImTypeVarRef(newTypevar), Collections.emptyMap()));
            }
            TypeRewriter.rewriteTypes(k, t -> rewriteType(classes, ownArguments, newVars, t));
            newVarsByClass.add(newVars);
        }
        return newVarsByClass.get(0);
    }

    private static void collectTypeVars(ImType type, Set<ImTypeVar> captured) {
        type.match(new TypeRewriteMatcher() {
            @Override
            public ImType case_ImTypeVarRef(ImTypeVarRef t) {
                captured.add(t.getTypeVariable());
                return t;
            }
        });
    }

    private static ImType rewriteType(List<ImClass> classes, ImTypeArguments ownArguments,
                                      Map<ImTypeVar, ImTypeVar> newVars, ImType type) {
        return type.match(new TypeRewriteMatcher() {

            @Override
            public ImType case_ImClassType(ImClassType t) {
                if (classes.contains(t.getClassDef())) {
                    return JassIm.ImClassType(t.getClassDef(), ownArguments.copy());
                }
                return super.case_ImClassType(t);
            }


            @Override
            public ImType case_ImTypeVarRef(ImTypeVarRef t) {
                return JassIm.ImTypeVarRef(newVars.get(t.getTypeVariable()));
            }
        });
    }


    /**
     * all uses of local variables are changed to use the
     * class variables instead
     */
    private void transformTranslated(ImExpr t) {
        final List<ImVarAccess> vas = Lists.newArrayList();
        final List<ImVarAccess> thisVarAccessesToRewrite = Lists.newArrayList();
        final List<ImMemberAccess> receiversToRewrite = Lists.newArrayList();
        ImVar closureThisVar = tr.getThisVar(e);

        t.accept(new ImExpr.DefaultVisitor() {
            @Override
            public void visit(ImVarAccess va) {
                super.visit(va);
                if (isLocalToOtherFunc(va.getVar())) {
                    vas.add(va);
                    return;
                }
                if (capturedThisField != null && va.getVar() == closureThisVar) {
                    // Explicit `this` used inside a closure (e.g. "var cur = this")
                    // must refer to captured outer this, not the synthetic closure instance.
                    if (!isReceiverInMemberAccess(va)) {
                        thisVarAccessesToRewrite.add(va);
                    }
                }
            }

            @Override
            public void visit(ImMemberAccess ma) {
                super.visit(ma);
                if (capturedThisField != null && ma.getReceiver() instanceof ImVarAccess) {
                    ImVar recvVar = ((ImVarAccess) ma.getReceiver()).getVar();
                    if (recvVar == closureThisVar && capturesFromEnclosingThis(owningClass(ma.getVar()))) {
                        receiversToRewrite.add(ma);
                    }
                }
            }

        });

        for (ImVarAccess va : vas) {
            ImVar v = getClosureVarFor(va.getVar());
            va.replaceBy(JassIm.ImMemberAccess(e, closureThis(), JassIm.ImTypeArguments(), v, JassIm.ImExprs()));
        }

        for (ImVarAccess va : thisVarAccessesToRewrite) {
            va.replaceBy(JassIm.ImMemberAccess(e, closureThis(), JassIm.ImTypeArguments(), capturedThisField, JassIm.ImExprs()));
        }

        for (ImMemberAccess ma : receiversToRewrite) {
            ma.setReceiver(JassIm.ImMemberAccess(e, closureThis(), JassIm.ImTypeArguments(), capturedThisField, JassIm.ImExprs()));
        }
    }

    private boolean isReceiverInMemberAccess(ImVarAccess va) {
        if (va.getParent() instanceof ImMemberAccess ma) {
            return ma.getReceiver() == va;
        }
        return false;
    }

    private void captureEnclosingThis() {
        ImVar outerThis = getEnclosingThisVar();
        if (outerThis != null) {
            capturedThisField = getClosureVarFor(outerThis);
        }
    }

    private ImVar getEnclosingThisVar() {
        if (f != null && !f.getParameters().isEmpty()) {
            ImVar param = f.getParameters().get(0);
            if ("this".equals(param.getName()) && param.getType() instanceof ImClassType) {
                return param;
            }
        }
        return null;
    }

    private ImClass owningClass(ImVar var) {
        if (var.getParent() != null && var.getParent().getParent() instanceof ImClass) {
            return (ImClass) var.getParent().getParent();
        }
        return null;
    }

    private boolean capturesFromEnclosingThis(ImClass owner) {
        if (owner == null || capturedThisField == null) {
            return false;
        }
        if (!(capturedThisField.getType() instanceof ImClassType)) {
            return false;
        }

        ImClass capturedClass = ((ImClassType) capturedThisField.getType()).getClassDef();
        return capturedClass == owner || capturedClass.isSubclassOf(owner);
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
        if (e instanceof ImVarAccess imVarAccess) {
            return isLocalToOtherFunc(imVarAccess.getVar());
        } else if (e instanceof ImTupleSelection ts) {
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
        WurstTypeClassOrInterface t = (WurstTypeClassOrInterface) e.attrExpectedTypAfterOverloading();
        ClassOrInterface classDef = t.getDef();
        t = (WurstTypeClassOrInterface) classDef.attrTyp();
        VariableBinding mapping = VariableBinding.emptyMapping()
                .withTypeVariables(classDef.getTypeParameters());
        WurstType closureType = e.attrTyp();
        VariableBinding mapping2 = closureType.matchAgainstSupertype(t, e, mapping, VariablePosition.RIGHT);
        if (mapping2 == null) {
            throw new CompileError(e, "Could not translate closure: type " + closureType + " does not match " + t);
        }
        WurstType t2 = t.setTypeArgs(mapping2);
        return (ImClassType) t2.imTranslateType(tr);
    }


}
