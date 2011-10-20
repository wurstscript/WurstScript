package de.peeeq.parseq;

public class Symbol {

	private int line, column;
	private TokenType typ;
	private Object data;
	
	public Symbol(TokenType typ, int line, int column, Object data) {
		this.line = line;
		this.column = column;
		this.typ = typ;
		this.data = data;
	}
	
	public Symbol(TokenType typ, int line, int column) {
		this.line = line;
		this.column = column;
		this.typ = typ;
		this.data = null;
	}

	public boolean typeEquals(TokenType expected) {
		return typ.equals(expected);
	}
	
	@Override
	public String toString() {
		return typ + (data==null ? "" : " '" +data+"' ") +" (line " + line + ":" + column + ")";
	}

	public String getString() {
		return (String )data;
	}

	public TokenType getType() {
		return typ;
	}
	
}
