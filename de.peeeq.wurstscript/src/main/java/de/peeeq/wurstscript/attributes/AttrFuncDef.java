package de.peeeq.wurstscript.attributes;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.ArgTypes.ArgTypeCompatibility;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.Visibility;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeInt;
import de.peeeq.wurstscript.types.WurstTypeReal;
import de.peeeq.wurstscript.types.WurstTypeString;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * this attribute find the variable definition for every variable reference
 */
public class AttrFuncDef {

    // TODO just use the attr function signature to get the def

    public final static String overloadingPlus = "op_plus";
    public final static String overloadingMinus = "op_minus";
    public final static String overloadingMult = "op_mult";
    public final static String overloadingDiv = "op_divReal";

    public static @Nullable FunctionDefinition calculate(final ExprFuncRef node) {

        Collection<NameLink> funcs;
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
        try {
            funcs = filterInvisible(node.getFuncName(), node, funcs);
        } catch (EarlyReturn e) {
            return e.getFunc();
        }
        if (funcs.size() > 1) {
            node.addError("Reference to function " + node.getFuncName() + " is ambiguous. Alternatives are:\n" + Utils.printAlternatives(funcs));
        }
        NameLink nameLink = Utils.getFirst(funcs);
        if (nameLink.getNameDef() instanceof FunctionDefinition) {
            return (FunctionDefinition) nameLink.getNameDef();
        } else {
            // should never happen
            node.addError("impossibru");
            return null;
        }
    }


    public static @Nullable FunctionDefinition calculate(ExprBinary node) {
        return getExtensionFunction(node.getLeft(), node.getRight(), node.getOp());
    }

    public static @Nullable FunctionDefinition calculate(final ExprMemberMethod node) {

        Expr left = node.getLeft();
        WurstType leftType = left.attrTyp();
        String funcName = node.getFuncName();

        FunctionDefinition result = searchMemberFunc(node, leftType, funcName, ArgTypes.fromArguments(node.getArgs()));
        if (result == null) {
            node.addError("The method " + funcName + " is undefined for receiver of type " + leftType);
        }
        return result;
    }

    public static @Nullable FunctionDefinition calculate(final ExprFunctionCall node) {
        String funcName = node.getFuncName();
        if (funcName.equals("super")) {
            ClassDef c = node.attrNearestClassDef();
            if (c == null) {
                node.addError("Can only use super-calls in class-constructors.");
                return null;
            } else {
                ClassDef ec = c.attrExtendedClass();
                if (ec == null) {
                    node.addError("Could not find super-class for super-call.");
                    return null;
                } else {
                    funcName = ec.getName() + "$construct";
                }
            }
        }

        FunctionDefinition result = searchFunction(funcName, node, ArgTypes.fromArguments(node.getArgs()));

        if (result == null) {
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

    public static FunctionDefinition calculate(ExprNewObject node) {
        // TODO constructor name
        return searchFunction(node.getFuncName(), node, ArgTypes.fromArguments(node.getArgs()));
    }


    private static @Nullable FunctionDefinition getExtensionFunction(Expr left, Expr right, WurstOperator op) {
        String funcName = op.getOverloadingFuncName();
        if (funcName == null || nativeOperator(op, left.attrTyp(), right.attrTyp(), left)) {
            return null;
        }
        return searchMemberFunc(left, left.attrTyp(), funcName, ArgTypes.fromExprs(right));
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


    private static @Nullable FunctionDefinition searchFunction(String funcName, @Nullable FuncRef node, ArgTypes argumentTypes) {
        if (node == null) {
            return null;
        }
        Collection<NameLink> funcs1 = node.lookupFuncs(funcName);
        if (funcs1.size() == 0) {
            if (funcName.startsWith("InitTrig_")) {
                // ignore error
                return null;
            }
            node.addError("Reference to function " + funcName + " could not be resolved.");
            return null;
        }
        try {
            // filter out the methods which are private somewhere else
            List<NameLink> funcs = filterInvisible(funcName, node, funcs1);

            funcs = filterByReceiverType(node, funcName, funcs);

            funcs = filterByParameters(node, argumentTypes, funcs);

            funcs = useLocalPackageIfPossible(node, funcs);

            if (funcs.size() > 1) {
                node.addError("Call to function " + funcName + " is ambiguous. Alternatives are:\n "
                        + Utils.printAlternatives(funcs));
            }
            return firstFunc(funcs);
        } catch (EarlyReturn e) {
            return e.getFunc();
        }
    }


    private static List<NameLink> useLocalPackageIfPossible(FuncRef node,
                                                            List<NameLink> funcs) throws EarlyReturn {
        int localCount = 0;
        NameLink local = null;
        PackageOrGlobal myPackage = node.attrNearestPackage();
        for (NameLink n : funcs) {
            if (n.getNameDef().attrNearestPackage() == myPackage) {
                local = n;
                localCount++;
            }
        }
        if (localCount == 0) {
            return funcs;
        } else if (localCount == 1) {
            if (local == null) throw new Error("impossible");
            throw new EarlyReturn((FunctionDefinition) local.getNameDef());
        }
        List<NameLink> result = Lists.newArrayList();
        for (NameLink n : funcs) {
            if (n.getNameDef().attrNearestPackage() == myPackage) {
                result.add(n);
            }
        }
        return result;
    }


    private static @Nullable FunctionDefinition searchMemberFunc(Expr node, WurstType leftType, String funcName, ArgTypes arguments) {
        Collection<NameLink> funcs1 = node.lookupMemberFuncs(leftType, funcName);
        if (funcs1.size() == 0) {
            return null;
        }
        try {
            // filter out the methods which are private somewhere else
            List<NameLink> funcs = filterInvisible(funcName, node, funcs1);

            // chose method with most specific receiver type
            funcs = filterByReceiverType(node, funcName, funcs);
            funcs = filterByParameters(node, arguments, funcs);

            if (funcs.size() > 1) {
                node.addError("Call to function " + funcName + " is ambiguous. Alternatives are:\n" + Utils.printAlternatives(funcs));
            }
            return firstFunc(funcs);
        } catch (EarlyReturn e) {
            return e.getFunc();
        }
    }


    private static List<NameLink> filterByParameters(Element node, ArgTypes argumentTypes, List<NameLink> funcs)
            throws EarlyReturn {
        if (funcs.size() <= 1) {
            return funcs;
        }

        ArgTypeCompatibility maxComp = ArgTypeCompatibility.WRONG_NUMBER_OF_ARGS;

        List<NameLink> result = new ArrayList<>();
        for (NameLink func : funcs) {
            ArgTypeCompatibility funcComp = argumentTypes.compatibilityWith(func.getParameterTypes());
            if (funcComp.compareTo(maxComp) > 0) {
                maxComp = funcComp;
                result.clear();
            }
            if (funcComp.compareTo(maxComp) >= 0) {
                result.add(func);
            }
        }
        return result;
    }


    private static List<NameLink> filterInvisible(String funcName, Element node, Collection<NameLink> funcs1) throws EarlyReturn {
        if (node.attrSource().getFile().equals("<REPL>")) {
            // no filtering of invisible names in repl:
            return Lists.newArrayList(funcs1);
        }
        List<NameLink> funcs2 = Lists.newArrayListWithCapacity(funcs1.size());
        for (NameLink nl : funcs1) {
            if (!(nl.getVisibility() == Visibility.PRIVATE_OTHER
                    || nl.getVisibility() == Visibility.PROTECTED_OTHER)
                    && nl.getNameDef() instanceof FunctionDefinition) {
                funcs2.add(nl);
            }
        }

        funcs2 = Utils.removedDuplicates(funcs2);

        if (funcs2.size() == 0) {
            node.addError("Function " + funcName + " is not visible here.");
            throw new EarlyReturn(firstFunc(funcs1));
        } else if (funcs2.size() == 1) {
            throw new EarlyReturn(firstFunc(funcs2));
        }
        return funcs2;
    }


    private static List<NameLink> filterByReceiverType(Element node,
                                                       String funcName, List<NameLink> funcs2) throws EarlyReturn {
        List<NameLink> funcs3 = Lists.newArrayListWithCapacity(funcs2.size());
        for (NameLink f : funcs2) {
            boolean existsMoreSpecific = false;
            WurstType f_receiverType = f.getReceiverType();
            if (f_receiverType != null) {
                for (NameLink g : funcs2) {
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
            throw new EarlyReturn(firstFunc(funcs2));
        } else if (funcs2.size() == 1) {
            throw new EarlyReturn(firstFunc(funcs3));
        }
        return funcs3;
    }

    private static FunctionDefinition firstFunc(Collection<NameLink> funcs1) {
        NameLink nl = Utils.getFirst(funcs1);
        if (nl.getNameDef() instanceof FunctionDefinition) {
            return (FunctionDefinition) nl.getNameDef();
        }
        throw new Error("Collection of funcs was empty");
    }



}
