package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.OptExpr;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

public class CallSignature {
    private final @Nullable Expr receiver;
    private final @Nullable WurstType receiverTypeHint; // <-- NEW
    private final List<Expr> arguments;

    public CallSignature(@Nullable Expr receiver, @Nullable WurstType receiverTypeHint, List<Expr> arguments) {
        this.receiver = receiver;
        this.receiverTypeHint = receiverTypeHint;
        this.arguments = arguments;
    }

    public CallSignature(@Nullable OptExpr optExpr, @Nullable WurstType receiverTypeHint, List<Expr> arguments) {
        this((optExpr instanceof Expr) ? (Expr) optExpr : null, receiverTypeHint, arguments);
    }

    public @Nullable Expr getReceiver() { return receiver; }
    public @Nullable WurstType getReceiverTypeHint() { return receiverTypeHint; } // <-- NEW
    public List<Expr> getArguments() { return arguments; }

    public void checkSignatureCompatibility(FunctionSignature sig, String funcName, Element pos) {
        if (sig.isEmpty()) return;

        Expr l_receiver = receiver;
        if (l_receiver != null) {
            if (sig.getReceiverType() == null) {
                l_receiver.addError("No receiver expected for function " + funcName + ".");
            } else if (!l_receiver.attrTyp().isSubtypeOf(sig.getReceiverType(), l_receiver)) {
                l_receiver.addError("Incompatible receiver type at call to function " + funcName + ".\n" +
                    "Found " + l_receiver.attrTyp() + " but expected " + sig.getReceiverType());
            }
        }
        if (getArguments().size() > sig.getMaxNumParams()) {
            if (sig.getMaxNumParams() == 0) {
                pos.addError("Too many arguments. Function " + funcName + " takes no parameter.");
            } else if (sig.getMaxNumParams() == 1) {
                pos.addError("Too many arguments. Function " + funcName + " only takes one parameter.");
            } else {
                pos.addError("Too many arguments. Function " + funcName + " only takes " + sig.getParamTypes().size()
                        + " parameters.");
            }
        } else if (getArguments().size() < sig.getMinNumParams()) {
            pos.addError("Not enough arguments. Function " + funcName + " requires the following arguments: " + sig.getParameterDescription());
        } else {
            for (int i = 0; i < getArguments().size(); i++) {
                if (!getArguments().get(i).attrTyp().isSubtypeOf(sig.getParamType(i), pos)) {
                    getArguments().get(i).addError("Wrong parameter type when calling " + funcName + ".\n"
                            + "Found " + getArguments().get(i).attrTyp() + " but expected " + sig.getParamType(i) + " " + sig.getParamName(i));
                }
            }
        }

    }

}
