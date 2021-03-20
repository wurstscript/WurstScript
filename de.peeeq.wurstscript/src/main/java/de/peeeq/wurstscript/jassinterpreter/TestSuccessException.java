package de.peeeq.wurstscript.jassinterpreter;

public class TestSuccessException extends Error {
  public static final TestSuccessException instance = new TestSuccessException();

  private TestSuccessException() {}
}
