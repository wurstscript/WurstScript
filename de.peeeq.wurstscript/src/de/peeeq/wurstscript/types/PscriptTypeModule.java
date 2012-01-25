package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.NamedScope;


public class PscriptTypeModule extends PscriptTypeNamedScope {

	private ModuleDef moduleDef;

	public PscriptTypeModule(ModuleDef moduleDef, boolean isStaticRef) {
		super(isStaticRef);
		this.moduleDef = moduleDef;
	}

	public PscriptTypeModule(ModuleDef moduleDef2, List<PscriptType> newTypes) {
		super(newTypes);
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
	public PscriptType dynamic() {
		if (isStaticRef()) {
			return new PscriptTypeModule(moduleDef, false);
		}
		return this;
	}

	@Override
	public PscriptType replaceTypeVars(List<PscriptType> newTypes) {
		return new PscriptTypeModule(moduleDef, newTypes);
	}

	@Override
	public String[] jassTranslateType() {
		return PScriptTypeInt.instance().jassTranslateType();
	}
}
