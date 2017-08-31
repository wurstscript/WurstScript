package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.FunctionSignature;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
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
            ArgTypes argTypes, StmtCall location) {
        if (sigs.isEmpty()) {
            if (!isInitTrigFunc(location)) {
                location.addError("Could not find " + name(location) + ".");
            }
            return FunctionSignature.empty;
        }

        List<FunctionSignature> candidates = new ArrayList<>();
        for (FunctionSignature sig : sigs) {
            sig = sig.setTypeArgs(location, typeParameterBindings);
            if (paramTypesMatch(sig, argTypes, location)) {
                candidates.add(sig);
            }
        }
        if (candidates.isEmpty()) {
            // parameters match for no element, just return the first signature
            return Utils.getFirst(sigs);
        } else if (candidates.size() > 1) {
            StringBuilder alternatives = new StringBuilder();
            for (FunctionSignature s : candidates) {
                if (alternatives.length() > 0) {
                    alternatives.append(", ");
                }
                alternatives.append(s.toString());
            }
            location.addError("Call to " + name(location) + " is ambiguous, alternatives are: " + alternatives);

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

    private static boolean paramTypesMatch(FunctionSignature sig, ArgTypes argTypes, Element location) {
        return paramTypesMatch(sig.getParamTypes(), argTypes, location);
    }

    private static boolean paramTypesMatch(ParamTypes paramTypes, ArgTypes argTypes, Element location) {
        return argTypes.compatibilityWith(paramTypes) == ArgTypes.ArgTypeCompatibility.FULL_COMPATIBILITY;
    }

    private static ArgTypes argTypes(AstElementWithArgs fc) {
        return ArgTypes.fromArguments(fc.getArgs());
    }


}
