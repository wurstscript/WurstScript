package de.peeeq.wurstio;

public class AbortCompilationException extends RuntimeException {
	private static final long serialVersionUID = 4984246286230018420L;

	public AbortCompilationException(String msg) {
		super(msg);
	}
}
