package de.peeeq.pscript.intermediateLang.interpreter;


public class TestFailException extends Error {

	private String test;
	
	public TestFailException(String name) {
		test = name;
	}
	
	public String getVal() {
		return test;
	}

}
