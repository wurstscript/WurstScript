package de.peeeq.parseq.grammars.ast;

public class AstElement {
	private AstElement parent;

	public AstElement getParent() {
		return parent;
	}
	
	void setParent(AstElement parent) {
		if (parent == null) throw new IllegalArgumentException();
		this.parent = parent;
	}
}
