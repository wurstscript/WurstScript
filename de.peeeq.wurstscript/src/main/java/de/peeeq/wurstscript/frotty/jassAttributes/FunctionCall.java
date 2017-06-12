package de.peeeq.wurstscript.frotty.jassAttributes;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.frotty.jassValidator.JassErrors;
import de.peeeq.wurstscript.jassAst.Element;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassProgs;
import de.peeeq.wurstscript.jassAst.JassStmtCall;

public class FunctionCall {

	public static @Nullable JassFunction get(JassExprFunctionCall f) {
		String funcName = f.getFuncName();
		Element node = f.getParent();
		while (node != null) {
			if (node instanceof JassProgs) {
				JassProgs jassProgs = (JassProgs) node;
				JassFunction v = jassProgs.getFunction(funcName);
				if (v != null) {
					return v;
				}
			}
			node = node.getParent();
		}
		JassErrors.addError("Could not find function '" + funcName + "'.", f.getLine());
		return null;
	}

	public static @Nullable JassFunction get(JassStmtCall f) {
		String funcName = f.getFuncName();
		Element node = f.getParent();
		while (node != null) {
			if (node instanceof JassProgs) {
				JassProgs jassProgs = (JassProgs) node;
				JassFunction v = jassProgs.getFunction(funcName);
				if (v != null) {
					return v;
				}
			}
			node = node.getParent();
		}
		JassErrors.addError("Could not find function '" + funcName + "'.", f.getLine());
		return null;
	}

}
