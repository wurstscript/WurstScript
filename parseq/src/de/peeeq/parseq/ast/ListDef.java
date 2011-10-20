package de.peeeq.parseq.ast;

public class ListDef implements AstBaseTypeDefinition {

	public final  String name;
	public final  String itemType;

	public ListDef(String name, String itemType) {
		this.name = name;
		this.itemType = itemType;
	}

	@Override
	public String getName() {
		return name;
	}

	
	@Override
	public String toString() {
		return name + " * " + itemType;
	}
	
}
