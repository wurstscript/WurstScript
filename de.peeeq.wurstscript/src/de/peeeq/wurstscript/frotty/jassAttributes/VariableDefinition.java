package de.peeeq.wurstscript.frotty.jassAttributes;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.frotty.jassValidator.JassErrors;
import de.peeeq.wurstscript.jassAst.JassArrayVar;
import de.peeeq.wurstscript.jassAst.JassAstElement;
import de.peeeq.wurstscript.jassAst.JassExprVarRef;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassProgs;
import de.peeeq.wurstscript.jassAst.JassStmtSet;
import de.peeeq.wurstscript.jassAst.JassStmtSetArray;
import de.peeeq.wurstscript.jassAst.JassVar;

public class VariableDefinition {

	public static @Nullable JassVar get(JassExprVarRef e) {
		String varName = e.getVarName();
		JassAstElement node = e.getParent();
		while (node != null) {
			if (node instanceof JassFunction) {
				JassFunction jassFunction = (JassFunction) node;
				JassVar v = getVariableMap(jassFunction).get(varName);
				if (v != null) {
					return v;
				}
			} else if (node instanceof JassProgs) {
				JassProgs jassProgs = (JassProgs) node;
				JassVar v = jassProgs.getGlobal(varName);
				if (v != null) {
					return v;
				}
			}
			node = node.getParent();
		}
		// error
		JassErrors.addError("Could not find variable '" + varName + "'.", e.getLine());
		return null;
	}

	public static Map<String, JassVar> getVariableMap(JassFunction f) {
		Map<String, JassVar> result = Maps.newLinkedHashMap();
		addVarsToMap(result, f.getLocals());
		addVarsToMap(result, f.getParams());
		return result ;
	}

	private static void addVarsToMap(Map<String, JassVar> result, List<? extends JassVar> vs) {
		for (JassVar v : vs) {
			JassVar oldVar = result.put(v.getName(), v);
			if (oldVar != null) {
				JassErrors.addError("Variable '" + v.getName() + "' is already declared in line " + oldVar.getLine() + ".", v.getLine());
			}
		}
		
	}

	

	public static @Nullable JassStmtSet get(JassStmtSet e) {
		String varName = e.getLeft();
		JassAstElement node = e.getParent();
		while (node != null) {
			JassVar v = null;
			if (node instanceof JassFunction) {
				JassFunction jassFunction = (JassFunction) node;
				v = getVariableMap(jassFunction).get(varName);
			} else if (node instanceof JassProgs) {
				JassProgs jassProgs = (JassProgs) node;
				v = jassProgs.getGlobal(varName);

			}
			if (v != null) {
				if ( v instanceof JassArrayVar) {
					JassErrors.addError("Variable '" + varName + "' is declared as an array.", e.getLine());
					return null;
				} else {
					return e;
				}
			}
			node = node.getParent();
		}
		// error

		JassErrors.addError("Could not find variable '" + varName + "'.", e.getLine());
		return null;
	}

	public static @Nullable JassStmtSetArray get(JassStmtSetArray e) {
		String varName = e.getLeft();
		JassAstElement node = e.getParent();
		while (node != null) {
			if (node instanceof JassFunction) {
				JassFunction jassFunction = (JassFunction) node;
				JassVar v = getVariableMap(jassFunction).get(varName);
				if (v != null && v instanceof JassArrayVar) {
					return e;
				}
			} else if (node instanceof JassProgs) {
				JassProgs jassProgs = (JassProgs) node;
				JassVar v = jassProgs.getGlobal(varName);
				if (v != null && v instanceof JassArrayVar) {
					return e;
				}
			}
			node = node.getParent();
		}
		// error
		JassErrors.addError("Could not find variable '" + varName + "'.", e.getLine());
		return null;
	}

}
