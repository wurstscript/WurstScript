package de.peeeq.pscript.intermediateLang.interpreter;


public class TestSuccessException extends Error {
	static public final TestSuccessException instance = new TestSuccessException();
	
	private TestSuccessException() {
	}
	
}
