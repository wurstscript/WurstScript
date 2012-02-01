package de.peeeq.parseq.ast;

import java.util.List;

import com.google.common.collect.Lists;

public class CaseDef  implements AstEntityDefinition {

	public final  List<Alternative> alternatives;
	public final  String name;

	public CaseDef(String supertype, List<Alternative> alternatives) {
		this.name = supertype;
		this.alternatives = alternatives;
	}

	public CaseDef(String name) {
		this.name = name;
		alternatives = Lists.newArrayList();
	}

	@Override
	public String getName() {
		return name;
	}

	
	@Override
	public String toString() {
		String result = name + " = ";
		boolean first = true;
		for (Alternative a : alternatives) {
			if (!first) result += " | ";
			result += a.name;
			first = false;
		}
		return result;
	}

	public void addAlternative(String alternative) {
		alternatives.add(new Alternative(alternative));
	}
}
