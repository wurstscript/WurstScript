package de.peeeq.wurstscript.jassinterpreter;


public class TestSuccessException extends Error {
	static public final TestSuccessException instance = new TestSuccessException();
	
	private TestSuccessException() {
	}
	
}
