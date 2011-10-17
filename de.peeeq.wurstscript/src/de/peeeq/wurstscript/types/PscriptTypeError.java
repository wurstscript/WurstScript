package de.peeeq.wurstscript.types;

public class PscriptTypeError extends PscriptType {

	private String msg;

	public PscriptTypeError(String msg) {
		this.msg = msg;
	}

	@Override
	public boolean isSubtypeOf(PscriptType other) {
		return false;
	}


	@Override
	public String getName() {
		return "Error: " + msg;
	}

	@Override
	public String getFullName() {
		return getName();
	}

	@Override
	public String printJass() {
		return getName();
	}

}
