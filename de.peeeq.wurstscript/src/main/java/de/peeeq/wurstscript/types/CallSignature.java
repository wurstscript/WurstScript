package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.OptExpr;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

public class CallSignature {
    private final @Nullable Expr receiver;
    private final List<Expr> arguments;

    public CallSignature(@Nullable OptExpr optExpr, List<Expr> arguments) {
        if (optExpr instanceof Expr) {
            this.receiver = (Expr) optExpr;
        } else {
            this.receiver = null;
        }
        this.arguments = arguments;
    }

    public List<Expr> getArguments() {
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
        if (getArguments().size() > sig.getParamTypes().size() && !sig.isVararg()) {
            if (sig.getParamTypes().size() == 0) {
                pos.addError("Too many arguments. Function " + funcName + " takes no parameter.");
            } else if (sig.getParamTypes().size() < 2) {
                pos.addError("Too many arguments. Function " + funcName + " only takes " + sig.getParamTypes().size()
                        + " parameter.");
            } else {
                pos.addError("Too many arguments. Function " + funcName + " only takes " + sig.getParamTypes().size()
                        + " parameters.");
            }
        } else if (getArguments().size() < sig.getParamTypes().size()) {
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
