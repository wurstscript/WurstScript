package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;

public class PscriptTypeError extends PscriptType {

	private String msg;

	public PscriptTypeError(String msg) {
		this.msg = msg;
	}

	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
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
	public String[] jassTranslateType() {
		throw new Error("not implemented");
	}

}
