package de.peeeq.parseq.ast;

import java.util.List;

public class ConstructorDef {

	public final List<Parameter> parameters;
	public final String name;

	public ConstructorDef(String name, List<Parameter> parameters) {
		this.name = name;
		this.parameters = parameters;
	}

}
