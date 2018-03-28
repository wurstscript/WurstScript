package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.funcs.FuncSig;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.Visibility;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeInt;
import de.peeeq.wurstscript.types.WurstTypeReal;
import de.peeeq.wurstscript.types.WurstTypeString;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.List;


/**
 * finds functions to call (before overloading resolution)
 * <p>
 * already filters out invisible functions
 */
public class PossibleFuncDefs {


    public static ImmutableCollection<FuncSig> calculate(final ExprFuncRef node) {

        ImmutableCollection<NameLink> funcs;
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
        ImmutableList.Builder<FuncSig> result = ImmutableList.builder();
        for (NameLink nameLink : funcs) {
            result.add(FuncSig.fromFunc((FunctionDefinition) nameLink.getNameDef()));
        }
        return result.build();
    }


    public static ImmutableCollection<FuncSig> calculate(final ExprMemberMethod node) {

        Expr left = node.getLeft();
        WurstType leftType = left.attrTyp();
        String funcName = node.getFuncName();

        return searchMemberFunc(node, leftType, funcName);
    }

    public static ImmutableCollection<FuncSig> calculate(final ExprFunctionCall node) {
        return searchFunction(node.getFuncName(), node);
    }


    private static ImmutableCollection<FuncSig> searchFunction(String funcName, @Nullable FuncRef node) {
        if (node == null) {
            return ImmutableList.of();
        }
        ImmutableCollection<NameLink> funcs1 = node.lookupFuncs(funcName);
        if (funcs1.size() == 0) {
            if (funcName.startsWith("InitTrig_")) {
                // ignore error
                return ImmutableList.of();
            }
            node.addError("Reference to function " + funcName + " could not be resolved.");
            return ImmutableList.of();
        }
        try {
            // filter out the methods which are private somewhere else
            ImmutableCollection<NameLink> funcs = filterInvisible(funcName, node, funcs1);

            funcs = filterByReceiverType(node, funcName, funcs);

            return nameLinksToFunctionDefinition(funcs);
        } catch (EarlyReturn e) {
            return ImmutableList.of(FuncSig.fromFunc(e.getFunc()));
        }
    }


    private static ImmutableCollection<FuncSig> searchMemberFunc(Expr node, WurstType leftType, String funcName) {
        ImmutableCollection<NameLink> funcs1 = node.lookupMemberFuncs(leftType, funcName);
        if (funcs1.size() == 0) {
            return ImmutableList.of();
        }
        try {
            // filter out the methods which are private somewhere else
            ImmutableCollection<NameLink> funcs = filterInvisible(funcName, node, funcs1);

            // chose method with most specific receiver type
            funcs = filterByReceiverType(node, funcName, funcs);
            return nameLinksToFunctionDefinition(funcs);
        } catch (EarlyReturn e) {
            return ImmutableList.of(FuncSig.fromFunc(e.getFunc())); // TODO Bind type parameters from receiver
        }
    }


    private static ImmutableCollection<FuncSig> nameLinksToFunctionDefinition(
            ImmutableCollection<NameLink> funcs) {
        return funcs.stream()
                .map(nl -> FuncSig.fromFunc((FunctionDefinition) nl.getNameDef()))
                .collect(Utils.toImmutableList());
    }


    private static ImmutableCollection<NameLink> filterInvisible(String funcName, Element node, ImmutableCollection<NameLink> funcs) {
        if (node.attrSource().getFile().equals("<REPL>")) {
            // no filtering of invisible names in repl:
            return funcs;
        }
        List<NameLink> funcs2 = Lists.newArrayListWithCapacity(funcs.size());
        for (NameLink nl : funcs) {
            if (!(nl.getVisibility() == Visibility.PRIVATE_OTHER
                    || nl.getVisibility() == Visibility.PROTECTED_OTHER)
                    && nl.getNameDef() instanceof FunctionDefinition) {
                funcs2.add(nl);
            }
        }

        if (funcs2.size() == 0) {
            node.addError("Function " + funcName + " is not visible here.");
            return funcs;
        }

        return ImmutableList.copyOf(Utils.removedDuplicates(funcs2));
    }


    private static ImmutableList<NameLink> filterByReceiverType(Element node,
                                                                String funcName, ImmutableCollection<NameLink> funcs) throws EarlyReturn {
        ImmutableList.Builder<NameLink> funcs3 = ImmutableList.builder();
        for (NameLink f : funcs) {
            boolean existsMoreSpecific = false;
            WurstType f_receiverType = f.getReceiverType();
            if (f_receiverType != null) {
                for (NameLink g : funcs) {
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
        ImmutableList<NameLink> funcs4 = funcs3.build();
        if (funcs4.size() == 0) {
            node.addError("Function " + funcName + " has a wrong receiver type.");
            throw new EarlyReturn(firstFunc(funcs));
        } else if (funcs.size() == 1) {
            throw new EarlyReturn(firstFunc(funcs4));
        }
        return funcs4;
    }

    private static FunctionDefinition firstFunc(Collection<NameLink> funcs1) {
        NameLink nl = Utils.getFirst(funcs1);
        if (nl.getNameDef() instanceof FunctionDefinition) {
            return (FunctionDefinition) nl.getNameDef();
        }
        throw new Error("Collection of funcs was empty");
    }

}
