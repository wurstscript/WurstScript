package de.peeeq.wurstscript.attributes;

import java.util.Set;

import com.google.common.collect.Sets;

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

}
