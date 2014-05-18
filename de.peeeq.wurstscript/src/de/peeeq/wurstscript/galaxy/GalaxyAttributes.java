package de.peeeq.wurstscript.galaxy;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyAstElement;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyFunction;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyProg;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyVar;

public class GalaxyAttributes {

	public static Map<GalaxyAstElement, String> attrComments(
			GalaxyProg galaxyProg) {
		return Maps.newHashMap();
	}

	public static Set<GalaxyFunction> attrIgnoredFunctions(GalaxyProg galaxyProg) {
		return Sets.newHashSet();
	}

	public static Set<GalaxyVar> attrIgnoredVariables(GalaxyProg galaxyProg) {
		return Sets.newHashSet();
	}

}
