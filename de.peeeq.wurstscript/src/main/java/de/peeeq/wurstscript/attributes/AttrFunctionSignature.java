package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.funcs.FuncSig;
import de.peeeq.wurstscript.types.FunctionSignature;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import de.peeeq.wurstscript.types.WurstTypeUnknown;
import de.peeeq.wurstscript.utils.Utils;

import java.util.*;

public class AttrFunctionSignature {

    public static Optional<FuncSig> calculate(StmtCall fc) {
        Collection<FuncSig> sigs = fc.attrPossibleFunctionSignatures();
        return filterSigs(sigs, argTypes(fc), fc);
    }

    private static Optional<FuncSig> filterSigs(
            Collection<FuncSig> sigs,
            List<WurstType> argTypes, StmtCall location) {
        if (sigs.isEmpty()) {
            if (!isInitTrigFunc(location)) {
                location.addError("Could not find " + name(location) + ".");
            }
            return Optional.empty();
        }

        List<FuncSig> candidates = new ArrayList<>();
        for (FuncSig sig : sigs) {
            if (paramTypesMatch(sig, argTypes, location)) {
                candidates.add(sig);
            }
        }
        if (candidates.isEmpty()) {
            // return first match from original
            return Utils.getFirstOption(sigs);
        }
        if (candidates.size() > 1) {
            if (argTypes.stream().noneMatch(t -> t instanceof WurstTypeUnknown)) {
                // only show overloading error, if type for all arguments could be determined
                StringBuilder alternatives = new StringBuilder();
                for (FuncSig s : candidates) {
                    if (alternatives.length() > 0) {
                        alternatives.append(", ");
                    }
                    alternatives.append(s.toString());
                }
                location.addError("Call to " + name(location) + " is ambiguous, alternatives are: " + alternatives);
            }
        }
        return Utils.getFirstOption(candidates);

    }

    private static boolean isInitTrigFunc(StmtCall e) {
        if (e instanceof ExprFunctionCall) {
            ExprFunctionCall e2 = (ExprFunctionCall) e;
            return e2.getFuncName().startsWith("InitTrig_");
        }
        return false;
    }

    private static String name(StmtCall s) {
        if (s instanceof ExprNewObject) {
            ExprNewObject e = (ExprNewObject) s;
            return "constructor for " + e.getTypeName();
        } else if (s instanceof FunctionCall) {
            FunctionCall e = (FunctionCall) s;
            return "function " + e.getFuncName();
        }
        return Utils.printElement(s);
    }

    private static boolean paramTypesMatch(FuncSig sig, List<WurstType> argTypes, Element location) {
        return paramTypesMatch(sig.getParamTypes(), argTypes, location);
    }

    private static boolean paramTypesMatch(List<WurstType> paramTypes, List<WurstType> argTypes, Element location) {
        if (paramTypes.size() != argTypes.size()) {
            return false;
        }
        for (int i = 0; i < paramTypes.size(); i++) {
            if (!argTypes.get(i).isSubtypeOf(paramTypes.get(i), location)) {
                return false;
            }
        }
        return true;
    }

    private static List<WurstType> argTypes(AstElementWithArgs fc) {
        List<WurstType> result = new ArrayList<>();
        for (Expr arg : fc.getArgs()) {
            result.add(arg.attrTyp());
        }
        return result;
    }


}
