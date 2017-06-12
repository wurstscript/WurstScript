package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.JassIm;


public class WurstTypeNull extends WurstTypePrimitive {

	private static final WurstTypeNull instance = new WurstTypeNull();

	// make constructor private as we only need one instance
	protected WurstTypeNull() {
		super("null");
	}
	
	@Override
	public boolean isSubtypeOfIntern(WurstType other, Element location) {
		return other instanceof WurstTypeNull
				|| other instanceof WurstTypeHandle
				|| other instanceof WurstNativeType
				|| other instanceof WurstTypeString
				|| other instanceof WurstTypeCode
				|| other instanceof WurstTypeClass
				|| other instanceof WurstTypeInterface
				|| other instanceof WurstTypeModule
				|| other instanceof WurstTypeModuleInstanciation
				|| other instanceof WurstTypeTypeParam
				|| other instanceof WurstTypeBoundTypeParam;
	}


	public static WurstTypeNull instance() {
		return instance;
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImIntVal(0);
	}
	

}
