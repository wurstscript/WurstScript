package de.peeeq.wurstio.jassinterpreter;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.intermediateLang.interpreter.ProgramState;
import de.peeeq.wurstscript.parser.WPos;

public class InterpreterException extends RuntimeException {
	private static final long serialVersionUID = 3387292080655779808L;
	
	private final ProgramState state;

	public InterpreterException(ProgramState g, String msg) {
		super(msg);
		this.state = g;
	}

	public InterpreterException(String msg) {
		super(msg);
		this.state = null;
	}

	@Override
	public String toString() {
		if (state == null) {
			return getMessage();
		}
		System.err.println(state.getLastStatement());
		AstElement tr = state.getLastStatement().attrTrace();
		System.err.println(tr);
		System.err.println(tr.attrSource());
		WPos pos = tr.attrSource();
		return "at " + pos.print() +":\n" + getMessage();
	}

}
