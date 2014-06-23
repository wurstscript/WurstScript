package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.JassIm;


public class WurstTypeIntLiteral extends WurstTypePrimitive {

	private static final WurstTypeIntLiteral instance = new WurstTypeIntLiteral();

	// make constructor private as we only need one instance
	protected WurstTypeIntLiteral() {
		super("integer-literal");
	}
	
	@Override
	public boolean isSubtypeOfIntern(WurstType other, AstElement location) {
		if (other instanceof WurstTypeFreeTypeParam) {
			return true;
		}
		return other instanceof WurstTypeIntLiteral
			|| other instanceof WurstTypeInt
			|| other instanceof WurstTypeReal;
	}


	public static WurstTypeIntLiteral instance() {
		return instance;
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImIntVal(0);
	}
	
	@Override
	public ImSimpleType imTranslateType() {
		return JassIm.ImSimpleType("integer");
	}
	

}
