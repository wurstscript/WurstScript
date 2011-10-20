package de.peeeq.parseq.ast;

import java.util.List;

public class ConstructorDef  implements AstBaseTypeDefinition {

	public final List<Parameter> parameters;
	public final String name;

	public ConstructorDef(String name, List<Parameter> parameters) {
		this.name = name;
		this.parameters = parameters;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		String result = name + "(";
		boolean first = true;
		for (Parameter p : parameters) {
			if (!first) {
				result += ", ";
			}
			result += p;
			first = false;
		}
		result +=")";
		return result;
	}
	
	
}
