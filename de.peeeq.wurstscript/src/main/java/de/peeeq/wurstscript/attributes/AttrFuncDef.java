package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameResolution;
import de.peeeq.wurstscript.attributes.names.Visibility;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.peeeq.wurstscript.attributes.AttrPossibleFunctionSignatures.*;
import static de.peeeq.wurstscript.attributes.names.NameResolution.lookupMemberFuncs;


/**
 * this attribute find the variable definition for every variable reference
 */
public class AttrFuncDef {

    // TODO just use the attr function signature to get the def

    public final static String overloadingPlus = "op_plus";
    public final static String overloadingMinus = "op_minus";
    public final static String overloadingMult = "op_mult";
    public final static String overloadingDiv = "op_divReal";

    public static FuncLink calculate(final ExprFuncRef node) {

        Collection<FuncLink> funcs;
        if (node.getScopeName().length() > 0) {
            TypeDef typeDef = node.lookupType(node.getScopeName());
            if (typeDef == null) {
                node.addError("Could not find type " + node.getScopeName() + ".");
                return null;
            }
            WurstType receiverType = typeDef.attrTyp();
            funcs = node.lookupMemberFuncs(receiverType, node.getFuncName());
        } else {
            funcs = node.lookupFuncs(node.getFuncName());
        }
        if (funcs.size() == 0) {
            node.addError("Could not find a function with name " + node.getFuncName());
            return null;
        }
        funcs = filterInvisible(node.getFuncName(), node, funcs);
        if (funcs.size() == 1) {
            return Utils.getFirst(funcs);
        }
        if (funcs.size() > 1) {
            node.addError("Reference to function " + node.getFuncName() + " is ambiguous. Alternatives are:\n" + Utils.printAlternatives(funcs));
        }
        return Utils.getFirst(funcs);
    }


    public static @Nullable FuncLink calculate(ExprBinary node) {
        return getExtensionFunction(node.getLeft(), node.getRight(), node.getOp());
    }

    public static @Nullable FuncLink calculate(final ExprMemberMethod node) {
        WurstType recvT = node.getLeft().attrTyp();
        var raw = NameResolution.lookupMemberFuncs(node, recvT, node.getFuncName(), /*showErrors=*/false);

        java.util.ArrayList<FuncLink> visible = new java.util.ArrayList<>(raw.size());
        java.util.ArrayList<FuncLink> hidden  = new java.util.ArrayList<>(raw.size());
        for (var f : raw) {
            if (isVisible(f)) visible.add(f); else hidden.add(f);
        }

        if (!raw.isEmpty() && visible.isEmpty()) {
            // Keep the classic diagnostic the tests look for:
            node.addError("The method " + node.getFuncName() + " is not visible here.");
            return null; // don’t leak a def to downstream passes/codegen
        }

        java.util.List<FuncLink> methods = new java.util.ArrayList<>();
        java.util.List<FuncLink> exts    = new java.util.ArrayList<>();
        for (var f : visible) {
            if (isExtension(f)) exts.add(f); else methods.add(f);
        }

        if (!exts.isEmpty()) {
            exts = keepMostSpecificReceivers(exts, FuncLink::getReceiverType, node);
        }

        java.util.ArrayList<FuncLink> cands = new java.util.ArrayList<>(methods.size() + exts.size());
        cands.addAll(methods);
        cands.addAll(exts);

        var argTypes = AttrFuncDef.argumentTypes(node);

        // Pass 1: exact matches
        java.util.ArrayList<FuncLink> exactLinks = new java.util.ArrayList<>();
        java.util.ArrayList<FunctionSignature> exactSigs = new java.util.ArrayList<>();
        for (var f : cands) {
            var sig = FunctionSignature.fromNameLink(f);
            var m = sig.matchAgainstArgs(argTypes, node);
            if (m != null) {
                exactLinks.add(f);
                exactSigs.add(m);
            }
        }
        if (!exactLinks.isEmpty()) {
            // methods vs others
            java.util.ArrayList<Integer> methodIdxs = new java.util.ArrayList<>();
            for (int i = 0; i < exactLinks.size(); i++) {
                if (!isExtension(exactLinks.get(i))) methodIdxs.add(i);
            }
            if (methodIdxs.size() > 1) {
                // filter method candidates by most specific receiver
                java.util.ArrayList<FunctionSignature> methSigs = new java.util.ArrayList<>();
                for (int i : methodIdxs) methSigs.add(exactSigs.get(i));
                methSigs = (java.util.ArrayList<FunctionSignature>) keepMostSpecificReceivers(
                    methSigs, FunctionSignature::getReceiverType, node
                );
                // pick the first of the survivors
                var chosenSig = methSigs.get(0);
                // find corresponding link
                for (int i = 0; i < exactSigs.size(); i++) {
                    if (exactSigs.get(i) == chosenSig) {
                        return exactLinks.get(i).withTypeArgBinding(node, chosenSig.getMapping());
                    }
                }
            } else if (methodIdxs.size() == 1) {
                int i = methodIdxs.get(0);
                return exactLinks.get(i).withTypeArgBinding(node, exactSigs.get(i).getMapping());
            } else {
                // no methods, only extensions exact → pick first (they were narrowed already)
                return exactLinks.get(0).withTypeArgBinding(node, exactSigs.get(0).getMapping());
            }
        }

        // Pass 2: best-effort (unchanged)
        int bestBad = Integer.MAX_VALUE;
        FuncLink best = null;
        FunctionSignature bestSig = null;
        for (var f : cands) {
            var sig = FunctionSignature.fromNameLink(f);
            var r = sig.tryMatchAgainstArgs(argTypes, node.getArgs(), node);
            if (r.getBadness() < bestBad) {
                bestBad = r.getBadness();
                best = f;
                bestSig = r.getSig();
            }
        }
        return best == null ? null : best.withTypeArgBinding(node, bestSig.getMapping());
    }



    public static @Nullable FunctionDefinition calculateDef(final ExprMemberMethod node) {
        var fl = node.attrFuncLink();
        return fl == null ? null : fl.getDef();
    }


    public static @Nullable FuncLink calculate(final ExprFunctionCall node) {
        FuncLink result = searchFunction(node.getFuncName(), node, argumentTypes(node));

        if (result == null) {
            String funcName = node.getFuncName();
            if (funcName.startsWith("InitTrig_")
                    && node.attrNearestFuncDef() != null
                    && node.attrNearestFuncDef().getName().equals("InitCustomTriggers")) {
                // ignore missing InitTrig functions
            } else {
                node.addError("Could not resolve reference to called function " + funcName);
            }
        }
        return result;
    }

    private static @Nullable FuncLink getExtensionFunction(Expr left, Expr right, WurstOperator op) {
        String funcName = op.getOverloadingFuncName();
        if (funcName == null || nativeOperator(op, left.attrTyp(), right.attrTyp(), left)) {
            return null;
        }
        return searchMemberFunc(left, left.attrTyp(), funcName, Collections.singletonList(right.attrTyp()));
    }


    /**
     * checks if operator is a native operator like for 1+2
     * TODO also check which operator is used?
     *
     * @param op
     * @param term
     */
    private static boolean nativeOperator(WurstOperator op, WurstType leftType, WurstType rightType, Element term) {
        return
                // numeric
                ((leftType.isSubtypeOf(WurstTypeInt.instance(), term) || leftType.isSubtypeOf(WurstTypeReal.instance(), term))
                        && (rightType.isSubtypeOf(WurstTypeInt.instance(), term) || rightType.isSubtypeOf(WurstTypeReal.instance(), term)))
                        // strings
                        || (op == WurstOperator.PLUS && leftType instanceof WurstTypeString && rightType instanceof WurstTypeString);
    }


    public static List<WurstType> argumentTypes(StmtCall node) {
        List<WurstType> result = Lists.newArrayList();
        for (Expr arg : node.getArgs()) {
            WurstType argType;
            if (arg instanceof ExprClosure) {
                // for closures, we only calculate the type, if all argument types are specified:
                ExprClosure closure = (ExprClosure) arg;
                boolean b = true;
                for (WShortParameter wShortParameter : closure.getShortParameters()) {
                    if (!(wShortParameter.getTypOpt() instanceof TypeExpr)) {
                        b = false;
                        break;
                    }
                }
                if (b) {
                    argType = arg.attrTyp();
                } else {
                    WurstType expected = arg.attrExpectedTyp();

                    List<WurstType> paramTypes = new ArrayList<>();
                    boolean hasInferredType = false;
                    int pIndex = 0;
                    for (WShortParameter p : closure.getShortParameters()) {
                        if (p.getTypOpt() instanceof TypeExpr) {
                            paramTypes.add(p.getTypOpt().attrTyp());
                        } else {
                            WurstType pt = AttrVarDefType.getParameterTypeFromClosureType(p, pIndex, expected, false);
                            paramTypes.add(pt);
                            if (pt instanceof WurstTypeInfer) {
                                hasInferredType = true;
                            }
                        }
                        pIndex++;
                    }
                    if (hasInferredType) {
                        WurstType bodyType = closure.getImplementation().attrTyp();
                        if (bodyType == null || bodyType instanceof WurstTypeUnknown) {
                            bodyType = WurstTypeInfer.instance();
                        }
                        argType = new WurstTypeClosure(paramTypes, bodyType);
                    } else {
                        // if there are no unknown types for the argument, then it should be safe to directly calculate the type
                        argType = arg.attrTyp();
                    }
                }
            } else {
                argType = arg.attrTyp();
            }
            result.add(argType);
        }
        return result;
    }

    /** very simple calculation of argument types without using expected types in closures */
    public static List<WurstType> argumentTypesPre(StmtCall node) {
        List<WurstType> result = Lists.newArrayList();
        for (Expr arg : node.getArgs()) {
            WurstType argType;
            if (arg instanceof ExprClosure) {
                // for closures, we only calculate the type, if all argument types are specified:
                ExprClosure closure = (ExprClosure) arg;
                boolean b = true;
                for (WShortParameter wShortParameter : closure.getShortParameters()) {
                    if (!(wShortParameter.getTypOpt() instanceof TypeExpr)) {
                        b = false;
                        break;
                    }
                }
                if (b) {
                    argType = arg.attrTyp();
                } else {
                    List<WurstType> paramTypes = new ArrayList<>();
                    for (WShortParameter p : closure.getShortParameters()) {
                        if (p.getTypOpt() instanceof TypeExpr) {
                            paramTypes.add(p.getTypOpt().attrTyp());
                        } else {
                            paramTypes.add(WurstTypeInfer.instance());
                        }
                    }
                    WurstType resultType = WurstTypeInfer.instance();
                    argType = new WurstTypeClosure(paramTypes, resultType);
                }
            } else {
                argType = arg.attrTyp();
            }
            result.add(argType);
        }
        return result;
    }


    private static FuncLink searchFunction(String funcName, @Nullable FuncRef node, List<WurstType> argumentTypes) {
        if (node == null) {
            return null;
        }
        ImmutableCollection<FuncLink> funcs1 = node.lookupFuncs(funcName);
        if (funcs1.size() == 0) {
            if (funcName.startsWith("InitTrig_")) {
                // ignore error
                return null;
            }
            if (node instanceof Annotation) {
                node.addWarning("Annotation " + funcName + " is not defined.");
            } else {
                node.addError("Reference to function " + funcName + " could not be resolved.");
            }
            return null;
        } else if (funcs1.size() == 1) {
            return Utils.getFirst(funcs1);
        }
        // distinguish between annotation functions and others
        List<FuncLink> funcs = filterAnnotation(node, funcs1);
        if (funcs.size() == 1) {
            return Utils.getFirst(funcs);
        }

        // filter out the methods which are private somewhere else
        funcs = filterInvisible(funcName, node, funcs);
        if (funcs.size() == 1) {
            return Utils.getFirst(funcs);
        }

        funcs = filterByReceiverType(node, funcName, funcs);
        if (funcs.size() == 1) {
            return Utils.getFirst(funcs);
        }

        funcs = filterByParameters(node, argumentTypes, funcs);
        if (funcs.size() == 1) {
            return Utils.getFirst(funcs);
        }

        funcs = ignoreWithIfNotDefinedAnnotation(node, funcs);
        if (funcs.size() == 1) {
            return Utils.getFirst(funcs);
        }

        funcs = useLocalPackageIfPossible(node, funcs);
        if (funcs.size() == 1) {
            return Utils.getFirst(funcs);
        }

        node.addError("Call to function " + funcName + " is ambiguous. Alternatives are:\n"
                + Utils.printAlternatives(funcs));
        return Utils.getFirst(funcs);
    }

    static ImmutableList<FuncLink> filterAnnotation(FuncRef node, ImmutableCollection<FuncLink> funcs1) {
        Predicate<FuncLink> filter;
        if (node instanceof Annotation) {
            filter = f -> f.getDef().hasAnnotation("@annotation");
        } else {
            filter = f -> !f.getDef().hasAnnotation("@annotation");
        }
        ImmutableList<FuncLink> res = funcs1.stream()
                .filter(filter)
                .collect(Utils.toImmutableList());
        if (res.isEmpty()) {
            return ImmutableList.copyOf(funcs1);
        }
        return res;
    }

    private static List<FuncLink> ignoreWithIfNotDefinedAnnotation(FuncRef node, List<FuncLink> funcs) {
        List<FuncLink> list = new ArrayList<>();
        for (FuncLink fl : funcs) {
            if (!fl.hasIfNotDefinedAnnotation()) {
                list.add(fl);
            }
        }
        return list;
    }


    private static List<FuncLink> useLocalPackageIfPossible(FuncRef node,
                                                            List<FuncLink> funcs) {
        int localCount = 0;
        FuncLink local = null;
        PackageOrGlobal myPackage = node.attrNearestPackage();
        for (FuncLink n : funcs) {
            if (n.getDef().attrNearestPackage() == myPackage) {
                local = n;
                localCount++;
            }
        }
        if (localCount == 0) {
            return funcs;
        } else if (localCount == 1) {
            return ImmutableList.of(local);
        }
        List<FuncLink> result = Lists.newArrayList();
        for (FuncLink n : funcs) {
            if (n.getDef().attrNearestPackage() == myPackage) {
                result.add(n);
            }
        }
        return result;
    }


    private static @Nullable FuncLink searchMemberFunc(Expr node, WurstType leftType, String funcName, List<WurstType> argumentTypes) {
        Collection<FuncLink> funcs1 = node.lookupMemberFuncs(leftType, funcName);
        if (funcs1.size() == 0) {
            return null;
        }
        if (funcs1.size() == 1) {
            return Utils.getFirst(funcs1);
        }
        // filter out the methods which are private somewhere else
        List<FuncLink> funcs = filterInvisible(funcName, node, funcs1);
        if (funcs.size() == 1) {
            return Utils.getFirst(funcs);
        }

        // chose method with most specific receiver type
        funcs = filterByReceiverType(node, funcName, funcs);
        if (funcs.size() == 1) {
            return Utils.getFirst(funcs);
        }

        funcs = filterByParameters(node, argumentTypes, funcs);
        if (funcs.size() == 1) {
            return Utils.getFirst(funcs);
        }

        node.addError("Call to function " + funcName + " is ambiguous. Alternatives are:\n" + Utils.printAlternatives(funcs));
        return Utils.getFirst(funcs);
    }


    private static List<FuncLink> filterByParameters(Element node,
                                                     List<WurstType> argumentTypes, List<FuncLink> funcs) {
        // filter out methods with wrong number of params
        funcs = filterByParamaeterNumber(argumentTypes, funcs);
        if (funcs.size() == 1) {
            return funcs;
        }

        // filter out methods for which the arguments have wrong types
        funcs = filterByParameterTypes(node, argumentTypes, funcs);
        return funcs;
    }


    private static List<FuncLink> filterByParameterTypes(
            Element node, List<WurstType> argumentTypes, List<FuncLink> funcs3) {
        List<FuncLink> funcs4 = Lists.newArrayListWithCapacity(funcs3.size());
        nextFunc:
        for (FuncLink f : funcs3) {
            VariableBinding mapping = f.getVariableBinding();
            for (int i = 0; i < argumentTypes.size(); i++) {
                // TODO use matching here
                WurstType at = argumentTypes.get(i);
                WurstType pt = f.getParameterType(i);
                VariableBinding m2 = at.matchAgainstSupertype(pt, node, mapping, VariablePosition.RIGHT);
                if (m2 == null) {
                    continue nextFunc;
                }
                mapping = m2;
            }
            funcs4.add(f);
        }
        if (funcs4.size() == 0) {
            return ImmutableList.of(Utils.getFirst(funcs3));
        } else if (funcs4.size() == 1) {
            return ImmutableList.of(Utils.getFirst(funcs4));
        } else {// if some argument type could not be determined, we don't want errors here, just take the first one
            for (WurstType t : argumentTypes) {
                if (t instanceof WurstTypeUnknown) {
                    return ImmutableList.of(Utils.getFirst(funcs4));
                }
            }
        }
        return funcs4;
    }


    private static List<FuncLink> filterByParamaeterNumber(List<WurstType> argumentTypes, List<FuncLink> funcs2) {
        List<FuncLink> funcs3 = Lists.newArrayListWithCapacity(funcs2.size());
        for (FuncLink f : funcs2) {
            if (f.getParameterTypes().size() == argumentTypes.size()
                    || (f.getParameterTypes().size() == 1 && f.getParameterTypes().get(0) instanceof WurstTypeVararg)) {
                funcs3.add(f);
            }
        }
        if (funcs3.size() == 0) {
            return Collections.singletonList(Utils.getFirst(funcs2));
        }
        return funcs3;
    }


    private static List<FuncLink> filterInvisible(String funcName, Element node, Collection<FuncLink> funcs1) {
        if (node.attrSource().getFile().equals("<REPL>")) {
            // no filtering of invisible names in repl:
            return Lists.newArrayList(funcs1);
        }
        List<FuncLink> funcs2 = Lists.newArrayListWithCapacity(funcs1.size());
        for (FuncLink nl : funcs1) {
            if (!(nl.getVisibility() == Visibility.PRIVATE_OTHER
                    || nl.getVisibility() == Visibility.PROTECTED_OTHER)) {
                funcs2.add(nl);
            }
        }

        funcs2 = Utils.removedDuplicates(funcs2);

        if (funcs2.size() == 0) {
            node.addError("Function " + funcName + " is not visible here.");
            return ImmutableList.of(Utils.getFirst(funcs1));
        }
        return funcs2;
    }


    private static List<FuncLink> filterByReceiverType(Element node,
                                                       String funcName, List<FuncLink> funcs2) {
        List<FuncLink> funcs3 = Lists.newArrayListWithCapacity(funcs2.size());
        for (FuncLink f : funcs2) {
            boolean existsMoreSpecific = false;
            WurstType f_receiverType = f.getReceiverType();
            if (f_receiverType != null) {
                for (FuncLink g : funcs2) {
                    if (f != g) {
                        WurstType g_receiverType = g.getReceiverType();
                        if (g_receiverType != null
                                && g_receiverType.isSubtypeOf(f_receiverType, node)
                                && !g_receiverType.equalsType(f_receiverType, node)) {
                            existsMoreSpecific = true;
                            break;
                        }
                    }
                }
            }
            if (!existsMoreSpecific) {
                funcs3.add(f);
            }
        }

        if (funcs3.size() == 0) {
            node.addError("Function " + funcName + " dfopsdfmpso.");
            return ImmutableList.of(Utils.getFirst(funcs2));
        }
        return funcs3;
    }


    public static FunctionDefinition calculateDef(FuncRef e) {
        FuncLink f = e.attrFuncLink();
        return f == null ? null : f.getDef();
    }

    public static FunctionDefinition calculateDef(ExprBinary e) {
        FuncLink f = e.attrFuncLink();
        return f == null ? null : f.getDef();
    }

    public static FuncLink calculate(Annotation node) {
        List<WurstType> argumentTypes = new ArrayList<>();
        for (Expr expr : node.getArgs()) {
            WurstType attrTyp = expr.attrTyp();
            argumentTypes.add(attrTyp);
        }
        FuncLink result = searchFunction(node.getFuncName(), node, argumentTypes);
        if (result == null) {
            return null;
        }
        FunctionDefinition def = result.getDef();
        if (def != null && !def.hasAnnotation("@annotation")) {
            node.addWarning("The function " + def.getName() + " must be annotated with @annotation to be usable as an annotation.");
        }
        return result;
    }
}
