package de.peeeq.wurstscript.attributes;

import java.util.Set;

import com.google.common.collect.Sets;

import de.peeeq.wurstscript.jassAst.JassAstElement;
import de.peeeq.wurstscript.jassAst.JassFuncRef;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassVar;

public class JassAttributes {

	public static Set<JassVar> attrIgnoredVariables(JassProg jassProgImpl) {
		return Sets.newHashSet();
	}

	public static Set<JassFunction> attrIgnoredFunctions(JassProg jassProgImpl) {
		return Sets.newHashSet();
	}

	public static JassFunction attrFuncDef(JassFuncRef f) {
		String funcName = f.getFuncName();
		JassProg root = getRoot(f);
		for (JassFunction func : root.getFunctions()) {
			if (func.getName().equals(funcName)) {
				return func;
			}
		}
		
		return null;
	}

	private static JassProg getRoot(JassAstElement e) {
		while (e != null) {
			if (e instanceof JassProg) {
				return (JassProg) e;
			}
			e = e.getParent();
		}
		throw new Error("Not attached to root.");
	}

}