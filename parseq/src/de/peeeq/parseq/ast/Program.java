package de.peeeq.parseq.ast;

import java.util.LinkedList;
import java.util.List;

public class Program {

	
	public final List<ListDef> listDefs = new LinkedList<ListDef>();
	public final List<CaseDef> caseDefs = new LinkedList<CaseDef>();
	public final List<ConstructorDef> constructorDefs = new LinkedList<ConstructorDef>();
	private List<String> packageParts;
	
	public void addListDef(String name, String itemType) {
		listDefs.add(new ListDef(name, itemType));
	}

	public void addCaseDef(String supertype, List<Alternative> alternatives) {
		caseDefs.add(new CaseDef(supertype, alternatives));
	}

	public void addConstructor(String name, List<Parameter> parameters) {
		constructorDefs.add(new ConstructorDef(name, parameters));
	}

	public void setPackage(List<String> parts) {
		this.packageParts = parts;
	}
	
}
