package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;

/**
 * the exact type is not known but it will be whatever you want it to be ;)
 * (used for the buildin/native functions, where we cannot check the types) 
 */
public class WurstTypeInfer extends WurstType {

	private static WurstType instance = new WurstTypeInfer();

	private WurstTypeInfer() {}
	
	@Override
	public boolean isSubtypeOfIntern(WurstType other, Element location) {
		return true;
	}

	@Override
	public String getName() {
		return "<Infer type>";
	}

	@Override
	public String getFullName() {
		return getName();
	}

	public static WurstType instance() {
		return instance ;
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
