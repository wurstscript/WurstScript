package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.StructureDef;

public abstract class WurstTypeClassOrInterface extends WurstTypeNamedScope {

	public WurstTypeClassOrInterface(List<WurstTypeBoundTypeParam> typeParameters,
			boolean isStaticRef) {
		super(typeParameters, isStaticRef);
	}

	public WurstTypeClassOrInterface(List<WurstTypeBoundTypeParam> newTypes) {
		super(newTypes);
	}

	
	@Override
	public abstract StructureDef getDef();
	
	
	@Override
	public boolean canBeUsedInInstanceOf() {
		return true;
	}
	

	@Override
	public boolean isCastableToInt() {
		return true;
	}
}
