package de.peeeq.wurstscript.jassinterpreter;


public class TestFailException extends Error {

    private final String test;

    public TestFailException(String name) {
        test = name;
    }

    public String getVal() {
        return test;
    }

    @Override
    public String toString() {
        return getMessage();
    }

    @Override
    public String getMessage() {
        return "Test failed: " + test;
    }
}
