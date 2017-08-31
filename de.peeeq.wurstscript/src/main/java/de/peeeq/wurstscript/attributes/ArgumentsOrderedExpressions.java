package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.CallSignature;
import de.peeeq.wurstscript.types.FunctionSignature;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

/**
 *
 */
public class ArgumentsOrderedExpressions {
    public static List<Expr> getOrderedExpressions(StmtCall call) {
        Arguments arguments = call.getArgs();
        CallSignature callSignature = call.attrCallSignature();
        FunctionSignature functionSignature = call.attrFunctionSignature();
        FunctionDefinition f = call.attrFuncDef();
        return callSignature.getOrderedExpressions(functionSignature, arguments, f.getParameters());
    }
}
