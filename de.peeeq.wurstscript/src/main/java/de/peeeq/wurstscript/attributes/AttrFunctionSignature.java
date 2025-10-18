package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.FunctionSignature;
import de.peeeq.wurstscript.types.VariableBinding;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeUnknown;
import de.peeeq.wurstscript.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AttrFunctionSignature {

    public static FunctionSignature calculate(StmtCall fc) {
        Collection<FunctionSignature> sigs = fc.attrPossibleFunctionSignatures();
        FunctionSignature sig = filterSigs(sigs, argTypes(fc), fc);

        VariableBinding mapping = sig.getMapping();
        for (CompileError error : mapping.getErrors()) {
            fc.getErrorHandler().sendError(error);
        }

        // If any argument is a closure, let it be typed using the selected signature’s
        // expected parameter types before complaining about unbound type variables.
        boolean hasClosureArg = false;
        if (fc instanceof AstElementWithArgs) {
            for (Expr a : ((AstElementWithArgs) fc).getArgs()) {
                if (a instanceof ExprClosure) {
                    hasClosureArg = true;
                    break;
                }
            }
        }

        if (mapping.hasUnboundTypeVars() && !hasClosureArg) {
            fc.addError("Cannot infer type for type parameter " + mapping.printUnboundTypeVars());
        }

        return sig;
    }

    private static FunctionSignature filterSigs(
            Collection<FunctionSignature> sigs,
            List<WurstType> argTypes, StmtCall location) {
        if (sigs.isEmpty()) {
            if (!isInitTrigFunc(location)) {
                location.addError("Could not find " + name(location) + ".");
            }
            return FunctionSignature.empty;
        }

        List<FunctionSignature> candidates = filterByArgumentTypes(sigs, argTypes, location);
        if (candidates.isEmpty()) {
            // parameters match for no element, just return the first signature
            return Utils.getFirst(sigs);
        } else if (candidates.size() == 1) {
            return candidates.get(0);
        }

        candidates = filterByIfNotDefinedAnnotation(candidates);
        if (candidates.isEmpty()) {
            // parameters match for no element, just return the first signature
            return Utils.getFirst(sigs);
        } else if (candidates.size() == 1) {
            return candidates.get(0);
        }


        boolean b = true;
        for (WurstType t : argTypes) {
            if (t instanceof WurstTypeUnknown) {
                b = false;
                break;
            }
        }
        if (b) {
            // only show overloading error, if type for all arguments could be determined
            StringBuilder alternatives = new StringBuilder();
            for (FunctionSignature s : candidates) {
                alternatives.append("\n");
                alternatives.append(s.toString());
            }
            location.addError("Call to " + name(location) + " is ambiguous, alternatives are: " + alternatives);
        }
        return candidates.get(0);
    }

    private static List<FunctionSignature> filterByIfNotDefinedAnnotation(List<FunctionSignature> candidates) {
        List<FunctionSignature> list = new ArrayList<>();
        for (FunctionSignature sig : candidates) {
            if (!sig.hasIfNotDefinedAnnotation()) {
                list.add(sig);
            }
        }
        return list;
    }

    @NotNull
    private static List<FunctionSignature> filterByArgumentTypes(Collection<FunctionSignature> sigs, List<WurstType> argTypes, StmtCall location) {
        List<FunctionSignature> candidates = new ArrayList<>();
        for (FunctionSignature sig : sigs) {
            sig = sig.matchAgainstArgs(argTypes, location);
            if (sig != null) {
                candidates.add(sig);
            }
        }
        return candidates;
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
