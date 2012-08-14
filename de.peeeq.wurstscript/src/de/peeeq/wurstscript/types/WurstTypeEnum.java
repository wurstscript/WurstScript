package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.EnumDef;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.ImType;


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
		return getDef().getName()  + " (enum)";
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
	public String[] jassTranslateType() {
		return WurstTypeInt.instance().jassTranslateType();
	}
	
	@Override
	public ImType imTranslateType() {
		return TypesHelper.imInt();
	}

}
