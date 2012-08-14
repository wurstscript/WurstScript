package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.EnumDef;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.ImType;


public class PScriptTypeEnum extends PscriptTypeNamedScope {

	

	private EnumDef edef;

	public PScriptTypeEnum(boolean isStaticRef, EnumDef edef) {
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
	public PscriptType dynamic() {
		return new PScriptTypeEnum(false, edef);
	}

	@Override
	public PscriptType replaceTypeVars(List<PscriptType> newTypes) {
		return this;
	}

	@Override
	public String[] jassTranslateType() {
		return PScriptTypeInt.instance().jassTranslateType();
	}
	
	@Override
	public ImType imTranslateType() {
		return TypesHelper.imInt();
	}

}
