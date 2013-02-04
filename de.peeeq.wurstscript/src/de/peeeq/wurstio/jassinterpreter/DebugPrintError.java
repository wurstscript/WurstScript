package de.peeeq.wurstio.jassinterpreter;

public class DebugPrintError extends Error {
	public DebugPrintError(String val) {
		super(val);
	}

	private static final long serialVersionUID = 629417346498474872L;
	
	@Override
	public String toString() {
		return "Wurst Error: " + getMessage();
	}

}
