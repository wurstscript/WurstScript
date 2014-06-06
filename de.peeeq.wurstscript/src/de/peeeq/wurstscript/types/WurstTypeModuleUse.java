package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;

public class WurstTypeModuleUse extends WurstTypeNamedScope {

	private final ModuleUse moduleUse;

	public WurstTypeModuleUse(ModuleUse moduleUse, List<WurstType> typeArgs) {
		super(typeArgs);
		this.moduleUse = moduleUse;
	}

	@Override
	public NamedScope getDef() {
		return (NamedScope) moduleUse;
	}

	@Override
	public WurstType replaceTypeVars(List<WurstType> newTypes) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	@Override
	public ImType imTranslateType() {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	@Override
	public ImExprOpt getDefaultValue() {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

}
