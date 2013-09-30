package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.EnumDef;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;


public class WurstTypeEnum extends WurstTypeNamedScope {

	

	private EnumDef edef;

	public WurstTypeEnum(boolean isStaticRef, EnumDef edef) {
		super(isStaticRef);
		if (edef == null) throw new IllegalArgumentException();
		this.edef = edef;
	}

	@Override
	public EnumDef getDef() {
		return edef;
	}
	
	@Override
	public String getName() {
		return getDef().getName();
	}
	
	@Override
	public WurstType dynamic() {
		return new WurstTypeEnum(false, edef);
	}

	@Override
	public WurstType replaceTypeVars(List<WurstType> newTypes) {
		return this;
	}

	
	@Override
	public ImType imTranslateType() {
		return TypesHelper.imInt();
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImIntVal(0);
	}
	

	@Override
	public boolean isCastableToInt() {
		return true;
	}

}
