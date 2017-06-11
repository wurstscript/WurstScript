package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.utils.Utils;


public class WurstTypeInt extends WurstTypePrimitive {

	private static final WurstTypeInt instance = new WurstTypeInt();

	// make constructor private as we only need one instance
	protected WurstTypeInt() {
		super("integer");
	}
	
	@Override
	public boolean isSubtypeOfIntern(WurstType other, Element location) {
		return other instanceof WurstTypeInt
				// in jass code we can use an int where a real is expected
				|| other instanceof WurstTypeReal && Utils.isJassCode(location);
	}


	public static WurstTypeInt instance() {
		return instance;
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImIntVal(0);
	}
	

}
