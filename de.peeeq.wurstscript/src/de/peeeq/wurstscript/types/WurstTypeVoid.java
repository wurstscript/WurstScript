package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;


public class WurstTypeVoid extends WurstType {

	private static final WurstTypeVoid instance = new WurstTypeVoid();

	// make constructor private as we only need one instance
	private WurstTypeVoid() {}
	
	@Override
	public boolean isSubtypeOfIntern(WurstType other, AstElement location) {
		return other instanceof WurstTypeVoid;
	}

	@Override
	public String getName() {
		return "Void";
	}

	@Override
	public String getFullName() {
		return "Void";
	}

	public static WurstTypeVoid instance() {
		return instance;
	}

	@Override
	public ImType imTranslateType() {
		return JassIm.ImVoid();
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImNoExpr();
	}
	
	@Override
	public boolean isVoid() {
		return true;
	}

}
