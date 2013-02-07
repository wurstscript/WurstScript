package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;


public class WurstTypeModuleInstanciation extends WurstTypeNamedScope {

	private ModuleInstanciation moduleInst;

	public WurstTypeModuleInstanciation(ModuleInstanciation moduleInst, boolean isStaticRef) {
		super(isStaticRef);
		if (moduleInst == null) throw new IllegalArgumentException();
		this.moduleInst = moduleInst;
	}

	public WurstTypeModuleInstanciation(ModuleInstanciation moduleInst2, List<WurstType> newTypes) {
		super(newTypes);
		if (moduleInst2 == null) throw new IllegalArgumentException();
		moduleInst = moduleInst2;
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

	/**
	 * check if n is a parent of this
	 */
	boolean isParent(WurstTypeNamedScope n) {
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
	public WurstType dynamic() {
		if (isStaticRef()) {
			return new WurstTypeModuleInstanciation(moduleInst, false);
		}
		return this;
	}

	@Override
	public WurstType replaceTypeVars(List<WurstType> newTypes) {
		return new WurstTypeModuleInstanciation(moduleInst, newTypes);
	}

	@Override
	public String[] jassTranslateType() {
		return WurstTypeInt.instance().jassTranslateType();
	}
	
	@Override
	public ImType imTranslateType() {
		return TypesHelper.imInt();
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImNull();
	}

}
