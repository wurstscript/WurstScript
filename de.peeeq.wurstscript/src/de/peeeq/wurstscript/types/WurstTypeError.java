package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;

public class WurstTypeError extends WurstType {

	private String msg;

	public WurstTypeError(String msg) {
		this.msg = msg;
	}

	@Override
	public boolean isSubtypeOf(WurstType other, AstElement location) {
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

	@Override
	public ImType imTranslateType() {
		throw new Error("not implemented");
	}

	@Override
	public ImExprOpt getDefaultValue() {
		throw new Error("not implemented");
	}

}
