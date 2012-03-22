package de.peeeq.wurstscript.frotty.jassAttributes;

import de.peeeq.wurstscript.frotty.jassValidator.JassErrors;
import de.peeeq.wurstscript.jassAst.JassAstElement;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassProgs;
import de.peeeq.wurstscript.jassAst.JassStmtCall;

public class FunctionCall {

	public static JassFunction get(JassExprFunctionCall f) {
		String funcName = f.getFuncName();
		JassAstElement node = f.getParent();
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

	public static JassFunction get(JassStmtCall f) {
		String funcName = f.getFuncName();
		JassAstElement node = f.getParent();
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
