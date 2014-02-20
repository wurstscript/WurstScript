package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.OptExpr;

public class CallSignature {
	private final Expr receiver;
	private final List<Expr> arguments;
	
	public CallSignature(OptExpr optExpr, List<Expr> arguments) {
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

	public Expr getReceiver() {
		return receiver;
	}
	
	public void checkSignatureCompatibility(FunctionSignature sig, String funcName, AstElement pos) {
		if (sig.isEmpty()) {
			return;
		}
		if (receiver != null) {
			if (sig.getReceiverType() == null) {
				receiver.addError("No receiver expected for function " + funcName + ".");
			} else if (!receiver.attrTyp().isSubtypeOf(sig.getReceiverType(), receiver)) {
				receiver.addError("Incompatible receiver type at call to function " + funcName + ".\n" +
						"Found " + receiver.attrTyp() + " but expected " + sig.getReceiverType());
			}
		}
		if (getArguments().size() > sig.getParamTypes().size()) {
			pos.addError("Too many arguments. Function " + funcName + " only takes " + sig.getParamTypes().size() 
					+ " parameters.");
			return;
		} else if (getArguments().size() < sig.getParamTypes().size()) { 
			pos.addError("Not enough arguments. Function " + funcName + " requires " + sig.getParamTypes().size() 
					+ " parameters.");
		} else {
			for (int i=0; i<getArguments().size(); i++) {
				if (!getArguments().get(i).attrTyp().isSubtypeOf(sig.getParamTypes().get(i), pos)) {
					getArguments().get(i).addError("Wrong parameter type when calling " + funcName + ".\n"
							+ "Found " + getArguments().get(i).attrTyp() + " but expected " + sig.getParamTypes().get(i));
				}
			}
		}
		
	}
	
}
