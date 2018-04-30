package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AttrFunctionSignature {

    public static FunctionSignature calculate(StmtCall fc) {
        Collection<FunctionSignature> sigs = fc.attrPossibleFunctionSignatures();
        return filterSigs(sigs, fc.attrTypeParameterBindings(), argTypes(fc), fc);
    }

    private static FunctionSignature filterSigs(
            Collection<FunctionSignature> sigs,
            Map<TypeParamDef, WurstTypeBoundTypeParam> typeParameterBindings,
            List<WurstType> argTypes, StmtCall location) {
        if (sigs.isEmpty()) {
            if (!isInitTrigFunc(location)) {
                location.addError("Could not find " + name(location) + ".");
            }
            return FunctionSignature.empty;
        }

        List<FunctionSignature> candidates = new ArrayList<>();
        for (FunctionSignature sig : sigs) {
            sig = sig.setTypeArgs(location, typeParameterBindings);
            FunctionSignature finalSig = sig;
            if (sig.isVararg()) {
                WurstType varargType = ((WurstTypeVararg)finalSig.getParamTypes().get(0)).getBaseType();
                if (argTypes.stream().filter(type -> type.isSubtypeOf(varargType, location)).count() == argTypes.size()) {
                    candidates.add(sig);
                }
            } else if (paramTypesMatch(sig, argTypes, location)) {
                candidates.add(sig);
            }
        }
        if (candidates.isEmpty()) {
            // parameters match for no element, just return the first signature
            return Utils.getFirst(sigs);
        } else if (candidates.size() > 1) {
            if (argTypes.stream().noneMatch(t -> t instanceof WurstTypeUnknown)) {
                // only show overloading error, if type for all arguments could be determined
                StringBuilder alternatives = new StringBuilder();
                for (FunctionSignature s : candidates) {
                    if (alternatives.length() > 0) {
                        alternatives.append(", ");
                    }
                    alternatives.append(s.toString());
                }
                location.addError("Call to " + name(location) + " is ambiguous, alternatives are: " + alternatives);
            }
        }
        return candidates.get(0);
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

    private static boolean paramTypesMatch(FunctionSignature sig, List<WurstType> argTypes, Element location) {
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
