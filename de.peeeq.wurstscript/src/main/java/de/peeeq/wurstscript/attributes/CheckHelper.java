package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.types.VariableBinding;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import io.vavr.collection.TreeMap;

import java.util.Map;
import java.util.Optional;

public class CheckHelper {

    /**
     * check if the signature of "f" is a refinement of the signature of "of"
     *
     * Returns an error if it is not a refinement
     */
    public static Optional<String> checkIfIsRefinement(VariableBinding typeParamMapping, FunctionDefinition f, FunctionDefinition of, String errorMessage) {
        String funcName = f.getName();
        // check static-ness
        if (f.attrIsStatic() && !of.attrIsStatic()) {
            return Optional.of("Function " + funcName + " must not be static.");
        }
        if (!f.attrIsStatic() && of.attrIsStatic()) {
            return Optional.of("Function " + funcName + " must be static.");
        }
        // check returntype
        WurstType f_type = getRealType(f, typeParamMapping, f.attrReturnTyp());
        WurstType of_type = getRealType(f, typeParamMapping, of.attrReturnTyp());
        if (!f_type.isSubtypeOf(of_type, f)) {
            return Optional.of(errorMessage + funcName + ": The return type is " + f_type +
                    " but it should be " + of_type + ".");
        }

        // check parameter count
        int f_count = f.getParameters().size();
        int of_count = of.getParameters().size();
        // check parameters
        if (f_count != of_count) {
            return Optional.of(errorMessage + funcName + ": The number of parameters of function " + funcName + " must be equal to " + of_count +
                    ", as defined by the overriden function.");
        }
        int i = 0;
        for (WParameter f_p : f.getParameters()) {
            WParameter of_p = of.getParameters().get(i);
            WurstType f_p_type = getRealType(f, typeParamMapping, f_p.attrTyp());
            WurstType of_p_type = getRealType(f, typeParamMapping, of_p.attrTyp());
            if (!f_p_type.isSupertypeOf(of_p_type, f)) {
                return Optional.of(errorMessage + funcName + ": The type of parameter " + f_p.getName() + " is " + f_p_type +
                        " but it should be " + of_p_type);
            }
            i++;
        }
        return Optional.empty();
    }

    private static WurstType getRealType(Element context, VariableBinding typeParamMapping, WurstType t) {
        return t.setTypeArgs(typeParamMapping);
    }


    public static boolean isRefinement(VariableBinding typeParamMapping, FunctionDefinition f, FunctionDefinition of) {
        return !checkIfIsRefinement(typeParamMapping, f, of, "").isPresent();
    }
}
