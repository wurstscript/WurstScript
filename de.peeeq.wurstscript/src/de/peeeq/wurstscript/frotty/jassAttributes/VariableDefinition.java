package de.peeeq.wurstscript.frotty.jassAttributes;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.frotty.jassValidator.JassErrors;
import de.peeeq.wurstscript.jassAst.JassAstElement;
import de.peeeq.wurstscript.jassAst.JassExprVarRef;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassProgs;
import de.peeeq.wurstscript.jassAst.JassScope;
import de.peeeq.wurstscript.jassAst.JassSimpleVars;
import de.peeeq.wurstscript.jassAst.JassVar;

public class VariableDefinition {

	public static JassVar get(JassExprVarRef e) {
		String varName = e.getVarName();
		JassAstElement node = e.getParent();
		while (node != null) {
			if (node instanceof JassFunction) {
				System.out.println("In Jasfunc");
				JassFunction jassFunction = (JassFunction) node;
				JassVar v = getVariableMap(jassFunction).get(varName);
				System.out.println(v);
				if (v != null) {
					return v;
				}
			} else if (node instanceof JassProgs) {
				System.out.println("In Jassprog");
				JassProgs jassProgs = (JassProgs) node;
				JassVar v = jassProgs.getGlobal(varName);
				System.out.println(v);
				if (v != null) {
					return v;
				}
			}
			node = node.getParent();
		}
		// error
		JassErrors.addError("Could not find variable " + varName + ".", e.getLine());
		return null;
	}

	public static Map<String, JassVar> getVariableMap(JassFunction f) {
		Map<String, JassVar> result = Maps.newHashMap();
		addVarsToMap(result, f.getLocals());
		addVarsToMap(result, f.getParams());
		return result ;
	}

	private static void addVarsToMap(Map<String, JassVar> result, List<? extends JassVar> vs) {
		for (JassVar v : vs) {
			JassVar oldVar = result.put(v.getName(), v);
			if (oldVar != null) {
				JassErrors.addError("Variable " + v.getName() + " is already declared in line " + oldVar.getLine() + ".", v.getLine());
			}
		}
		
	}

	public static Map<String, JassVar> getVariableMap(JassProg p) {
		Map<String, JassVar> result = Maps.newHashMap();
		addVarsToMap(result, p.getGlobals());
		return result ;
	}
}
