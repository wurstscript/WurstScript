package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.NamedScope;


public class PscriptTypeModule extends PscriptTypeNamedScope {

	private ModuleDef moduleDef;

	public PscriptTypeModule(ModuleDef moduleDef, boolean isStaticRef) {
		super(isStaticRef);
		this.moduleDef = moduleDef;
	}

	@Override
	public NamedScope getDef() {
		return moduleDef;
	}
	
	@Override
	public String getName() {
		return getDef().getName() + " (module)";
	}

}
