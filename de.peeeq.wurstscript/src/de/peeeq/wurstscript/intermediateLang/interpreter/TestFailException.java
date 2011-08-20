package de.peeeq.wurstscript.intermediateLang.interpreter;


public class TestFailException extends Error {

	private String test;
	
	public TestFailException(String name) {
		test = name;
	}
	
	public String getVal() {
		return test;
	}

}
