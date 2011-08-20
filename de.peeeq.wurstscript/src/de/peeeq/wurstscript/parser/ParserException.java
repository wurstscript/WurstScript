package de.peeeq.wurstscript.parser;

public class ParserException extends Error {

	private String msg;

	public ParserException(String msg) {
		this.msg = msg;
	}
	
	@Override
	public String toString() {
		return msg;
	}
}
