package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.jassIm.ImType;


public class WurstTypeModule extends WurstTypeNamedScope {

	private ModuleDef moduleDef;

	public WurstTypeModule(ModuleDef moduleDef, boolean isStaticRef) {
		super(isStaticRef);
		if (moduleDef == null) throw new IllegalArgumentException();
		this.moduleDef = moduleDef;
	}

	public WurstTypeModule(ModuleDef moduleDef2, List<WurstType> newTypes) {
		super(newTypes);
		if (moduleDef2 == null) throw new IllegalArgumentException();
		moduleDef = moduleDef2;
	}

	@Override
	public NamedScope getDef() {
		return moduleDef;
	}
	
	@Override
	public String getName() {
		return getDef().getName() + printTypeParams() + " (module)";
	}

	@Override
	public WurstType dynamic() {
		if (isStaticRef()) {
			return new WurstTypeModule(moduleDef, false);
		}
		return this;
	}

	@Override
	public WurstType replaceTypeVars(List<WurstType> newTypes) {
		return new WurstTypeModule(moduleDef, newTypes);
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
