package de.peeeq.wurstio.deps;

public class InvalidVersionStringException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public InvalidVersionStringException(Throwable ex, String msg) {
		super(msg, ex);
	}

}
