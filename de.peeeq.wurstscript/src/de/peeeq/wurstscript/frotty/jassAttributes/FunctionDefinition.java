package de.peeeq.wurstscript.frotty.jassAttributes;

import de.peeeq.wurstscript.jassAst.JassAstElement;
import de.peeeq.wurstscript.jassAst.JassExprFuncRef;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassScope;
import de.peeeq.wurstscript.jassAst.JassVar;

public class FunctionDefinition {

	public static JassFunction get(JassExprFuncRef f) {
		String varName = f.getFuncName();
		JassAstElement node = f.getParent();
		while (node != null) {
			if (node instanceof JassProg) {
				JassProg jassProg = (JassProg) node;
				JassFunction v = jassProg.getFunctions().get(1);
				if (v != null) {
					return v;
				}
			}
			node = node.getParent();
		}
		return null;
	}

}
