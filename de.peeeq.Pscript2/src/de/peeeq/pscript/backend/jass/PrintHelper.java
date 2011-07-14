package de.peeeq.pscript.backend.jass;

public class PrintHelper {

	private StringBuilder sb;
	private int indentLevel = 0;

	public PrintHelper(StringBuilder sb) {
		this.sb = sb;
	}

	protected void println() {
		sb.append("\n");		
	}

	protected void incIndent() {
		indentLevel++;
	}

	protected void decIndent() {
		indentLevel--;
	}

	protected void println(String string) {
		sb.append(string);
		println();
	}

	protected void printIndent() {
		for (int i=0; i<indentLevel; i++) {
			sb.append("   ");
		}		
	}

	protected void print(String string) {
		sb.append(string);
	}

}