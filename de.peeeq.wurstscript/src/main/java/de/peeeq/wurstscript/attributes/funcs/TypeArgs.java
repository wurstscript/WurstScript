package de.peeeq.wurstscript.attributes.funcs;

import de.peeeq.wurstscript.ast.FunctionCall;
import de.peeeq.wurstscript.types.WurstType;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class TypeArgs {
    /**
     * get the type args of a signature based on a concrete function call
     */
    public static FuncSig getTypeArgs(FuncSig sig, FunctionCall fc) {
        if (sig.getTypeParams().isEmpty()) {
            // no type arguments to find:
            return sig;
        }
        if (fc.getTypeArgs().isEmpty()) {
            // if there are type arguments give, use them:
            sig = sig.withTypeArgs(fc.getTypeArgs());

        }
        return sig;
    }
}
