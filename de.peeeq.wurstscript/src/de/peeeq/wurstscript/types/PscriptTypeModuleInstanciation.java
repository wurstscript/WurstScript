package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NamedScope;


public class PscriptTypeModuleInstanciation extends PscriptTypeNamedScope {

	private ModuleInstanciation moduleInst;

	public PscriptTypeModuleInstanciation(ModuleInstanciation moduleInst, boolean isStaticRef) {
		super(isStaticRef);
		this.moduleInst = moduleInst;
	}

	@Override
	public NamedScope getDef() {
		return moduleInst;
	}

}
