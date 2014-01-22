package de.peeeq.parseq.grammars.ast;


public abstract class Production extends AstElement {

	public abstract void print(StringBuilder sb);
	
	public abstract ProdType getType();
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		print(sb);
		return getClass().getSimpleName() + "{" + sb.toString() +"}";
	}
}
