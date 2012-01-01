package de.peeeq.wurstscript.parser;

import java_cup.runtime.Symbol;

public class WSymbol extends Symbol{

	
	private int leftChar;
	private int line;
	private int columnStart;
	private int length;
	
	
	
	public WSymbol(int id, int leftChar, int line, int column, int length, Object o) {
		super(id, line, column, o);
		this.length = length;
		this.leftChar = leftChar;
		this.line = line;
		this.columnStart = column;
	}



	public int getLeftChar() {
		return leftChar;
	}



	public int getRightChar() {
		return leftChar + length;
	}



	public int getLine() {
		return line;
	}



	public int getColumnStart() {
		return columnStart;
	}



	public int getColumnEnd() {
		return columnStart + length;
	}



	public int getLength() {
		return length;
	}
	
	
	
	
	
}
