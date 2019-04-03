package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.Visibility;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeInt;
import de.peeeq.wurstscript.types.WurstTypeReal;
import de.peeeq.wurstscript.types.WurstTypeString;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

import static de.peeeq.wurstscript.attributes.AttrFuncDef.filterAnnotation;

public class PossibleFuncDefs {


    public static ImmutableList<FuncLink> calculate(final ExprFuncRef node) {

        ImmutableCollection<FuncLink> funcs;
        if (node.getScopeName().length() > 0) {
            TypeDef typeDef = node.lookupType(node.getScopeName());
            if (typeDef == null) {
                node.addError("Could not find type " + node.getScopeName() + ".");
                return Utils.emptyList();
            }
            WurstType receiverType = typeDef.attrTyp();
            funcs = node.lookupMemberFuncs(receiverType, node.getFuncName());
        } else {
            funcs = node.lookupFuncs(node.getFuncName());
        }
        funcs = filterInvisible(node.getFuncName(), node, funcs);
        return ImmutableList.copyOf(funcs);
    }


    public static ImmutableCollection<FuncLink> calculate(final ExprMemberMethod node) {

        Expr left = node.getLeft();
        WurstType leftType = left.attrTyp();
        String funcName = node.getFuncName();

        return searchMemberFunc(node, leftType, funcName);
    }

    public static ImmutableCollection<FuncLink> calculate(final ExprFunctionCall node) {
        return searchFunction(node.getFuncName(), node);
    }

    private static ImmutableCollection<FuncLink> getExtensionFunction(Expr left, Expr right, WurstOperator op) {
        String funcName = op.getOverloadingFuncName();
        if (funcName == null || nativeOperator(left.attrTyp(), right.attrTyp(), left)) {
            return Utils.emptyList();
        }
        return searchMemberFunc(left, left.attrTyp(), funcName);
    }


    /**
     * checks if operator is a native operator like for 1+2
     * TODO also check which operator is used?
     *
     * @param term
     */
    private static boolean nativeOperator(WurstType leftType, WurstType rightType, Element term) {
        return
                // numeric
                ((leftType.isSubtypeOf(WurstTypeInt.instance(), term) || leftType.isSubtypeOf(WurstTypeReal.instance(), term))
                        && (rightType.isSubtypeOf(WurstTypeInt.instance(), term) || rightType.isSubtypeOf(WurstTypeReal.instance(), term)))
                        // strings
                        || (leftType instanceof WurstTypeString && rightType instanceof WurstTypeString);
    }


    private static ImmutableCollection<FuncLink> searchFunction(String funcName, @Nullable FuncRef node) {
        if (node == null) {
            return ImmutableList.of();
        }
        ImmutableCollection<FuncLink> funcs1 = node.lookupFuncs(funcName);
        if (funcs1.size() == 0) {
            if (funcName.startsWith("InitTrig_")) {
                // ignore error
                return ImmutableList.of();
            }
            node.addError("Reference to function " + funcName + " could not be resolved.");
            return ImmutableList.of();
        }
        ImmutableCollection<FuncLink> funcs2 = filterAnnotation(node, funcs1);

        // filter out the methods which are private somewhere else
        ImmutableCollection<FuncLink> funcs = filterInvisible(funcName, node, funcs2);
        if (funcs.size() <= 1) {
            return funcs;
        }

        funcs = filterByReceiverType(node, funcName, funcs);

        return funcs;
    }


    private static ImmutableCollection<FuncLink> searchMemberFunc(Expr node, WurstType leftType, String funcName) {
        ImmutableCollection<FuncLink> funcs1 = node.lookupMemberFuncs(leftType, funcName);
        if (funcs1.size() == 0) {
            return ImmutableList.of();
        }
        // filter out the methods which are private somewhere else
        ImmutableCollection<FuncLink> funcs = filterInvisible(funcName, node, funcs1);
        if (funcs.size() <= 1) {
            return funcs;
        }

        // chose method with most specific receiver type
        funcs = filterByReceiverType(node, funcName, funcs);
        return funcs;
    }


    private static ImmutableCollection<FuncLink> filterInvisible(String funcName, Element node, ImmutableCollection<FuncLink> funcs) {
        if (node.attrSource().getFile().equals("<REPL>")) {
            // no filtering of invisible names in repl:
            return funcs;
        }
        List<FuncLink> funcs2 = Lists.newArrayListWithCapacity(funcs.size());
        for (FuncLink nl : funcs) {
            if (!(nl.getVisibility() == Visibility.PRIVATE_OTHER
                    || nl.getVisibility() == Visibility.PROTECTED_OTHER)) {
                funcs2.add(nl);
            }
        }

        funcs2 = Utils.removedDuplicates(funcs2);

        if (funcs2.size() == 0) {
            node.addError("Function " + funcName + " is not visible here.");
            return ImmutableList.of(Utils.getFirst(funcs));
        }
        return ImmutableList.copyOf(funcs2);
    }


    private static ImmutableList<FuncLink> filterByReceiverType(Element node,
                                                                String funcName, ImmutableCollection<FuncLink> funcs) {
        ImmutableList.Builder<FuncLink> funcs3 = ImmutableList.builder();
        for (FuncLink f : funcs) {
            boolean existsMoreSpecific = false;
            WurstType f_receiverType = f.getReceiverType();
            if (f_receiverType != null) {
                for (FuncLink g : funcs) {
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
        ImmutableList<FuncLink> funcs4 = funcs3.build();
        if (funcs4.size() == 0) {
            node.addError("Function " + funcName + " has a wrong receiver type.");
            return ImmutableList.of(Utils.getFirst(funcs));
        }
        return funcs4;
    }

    public static ImmutableCollection<FuncLink> calculate(Annotation node) {
        return searchFunction(node.getFuncName(), node);
    }
}
