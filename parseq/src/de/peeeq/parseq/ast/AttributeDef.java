package de.peeeq.parseq.ast;

import java.util.List;

public class AttributeDef {

	public final String typ;
	public final String attr;
	public final String comment;
	public final String returns;
	public final String implementedBy;
	public final List<Parameter> parameters;

	public AttributeDef(List<Parameter> parameters, String typ, String attr, String comment, String returns2,
			String implementedBy2) {
		this.typ = typ;
		this.attr = attr;
		this.comment = comment;
		this.returns = returns2;
		this.implementedBy = implementedBy2;
		this.parameters = parameters;
	}

}
