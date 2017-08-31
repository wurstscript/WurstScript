package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.ArgTypes;
import de.peeeq.wurstscript.attributes.ParamTypes;
import de.peeeq.wurstscript.attributes.ParamTypes.ParamInfo;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

public class CallSignature {
    private final @Nullable Expr receiver;
    private final ArgTypes arguments;

    public CallSignature(@Nullable OptExpr optExpr, ArgTypes arguments) {
        if (optExpr instanceof Expr) {
            this.receiver = (Expr) optExpr;
        } else {
            this.receiver = null;
        }
        this.arguments = arguments;
    }

    public ArgTypes getArguments() {
        return arguments;
    }

    public @Nullable Expr getReceiver() {
        return receiver;
    }

    public void checkSignatureCompatibility(FunctionSignature sig, String funcName, Element pos) {
        if (sig.isEmpty()) {
            return;
        }
        Expr l_receiver = receiver;
        if (l_receiver != null) {
            if (sig.getReceiverType() == null) {
                l_receiver.addError("No receiver expected for function " + funcName + ".");
            } else if (!l_receiver.attrTyp().isSubtypeOf(sig.getReceiverType(), l_receiver)) {
                l_receiver.addError("Incompatible receiver type at call to function " + funcName + ".\n" +
                        "Found " + l_receiver.attrTyp() + " but expected " + sig.getReceiverType());
            }
        }
        arguments.checkCall(sig.getParamTypes(), funcName, pos);
    }

    public List<Expr> getOrderedExpressions(FunctionSignature functionSignature, Arguments args, WParameters parameters) {
        Expr[] result = new Expr[parameters.size()];
        ParamTypes paramTypes = functionSignature.getParamTypes();
        for (int i = 0; i < args.size(); i++) {
            Argument arg = args.get(i);
            OptIdentifier argNameId = arg.getArgName();
            ParamInfo param;
            if (argNameId instanceof Identifier) {
                String argName = ((Identifier) argNameId).getName();
                param = paramTypes.getParam(argName)
                        .orElseThrow(() -> new RuntimeException("Param " + argName + " not found"));
            } else {
                param = paramTypes.getParam(i);
            }
            result[param.getIndex()] = arg.getExpr();
        }

        for (int i = 0; i < result.length; i++) {
            if (result[i] == null) {
                // for missing parameters there must be a default value:
                result[i] = (Expr) parameters.get(i).getDefaultValue();
            }
        }
        return Arrays.asList(result);
    }
}
