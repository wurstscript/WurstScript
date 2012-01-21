package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NamedScope;


public class PscriptTypeModuleInstanciation extends PscriptTypeNamedScope {

	private ModuleInstanciation moduleInst;

	public PscriptTypeModuleInstanciation(ModuleInstanciation moduleInst, boolean isStaticRef) {
		super(isStaticRef);
		this.moduleInst = moduleInst;
	}

	public PscriptTypeModuleInstanciation(ModuleInstanciation moduleInst2, List<PscriptType> newTypes) {
		super(newTypes);
		moduleInst = moduleInst2;
	}

	@Override
	public boolean isSubtypeOf(PscriptType obj, AstElement location) {
		if (super.isSubtypeOf(obj, location)) {
			return true;
		}
		if (obj instanceof PscriptTypeNamedScope) {
			PscriptTypeNamedScope n = (PscriptTypeNamedScope) obj;
			return isParent(n);
		}
		return false;
	}

	/**
	 * check if n is a parent of this
	 */
	private boolean isParent(PscriptTypeNamedScope n) {
		NamedScope ns = this.getDef();
		while (true) {
			ns = ns.getParent().attrNearestNamedScope();
			if (ns == null) {
				return false;
			}
			if (ns == n.getDef()) {
				return true;
			}
		}
	}
	
	@Override
	public NamedScope getDef() {
		return moduleInst; 
	}
	
	@Override
	public String getName() {
		return getDef().getName() + printTypeParams() + " (module instanciation)";
	}
	
	@Override
	public PscriptType dynamic() {
		if (isStaticRef()) {
			return new PscriptTypeModuleInstanciation(moduleInst, false);
		}
		return this;
	}

	@Override
	public PscriptType replaceTypeVars(List<PscriptType> newTypes) {
		return new PscriptTypeModuleInstanciation(moduleInst, newTypes);
	}

}
