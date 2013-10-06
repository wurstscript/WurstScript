package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;


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
	public boolean isSubtypeOfIntern(WurstType obj, AstElement location) {
		if (super.isSubtypeOfIntern(obj, location)) {
			return true;
		}
		if (obj instanceof WurstTypeModuleInstanciation) {
			WurstTypeModuleInstanciation n = (WurstTypeModuleInstanciation) obj;
			return n.isParent(this);
		}
		return false;
	}

	@Override
	public ModuleDef getDef() {
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
	public ImType imTranslateType() {
		return TypesHelper.imInt();
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImNull();
	}
	
	@Override
	public boolean isCastableToInt() {
		return true;
	}
}
