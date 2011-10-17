package de.peeeq.parseq.ast;

import java.util.List;

public class CaseDef {

	public final  List<Alternative> alternatives;
	public final  String supertype;

	public CaseDef(String supertype, List<Alternative> alternatives) {
		this.supertype = supertype;
		this.alternatives = alternatives;
	}

}
