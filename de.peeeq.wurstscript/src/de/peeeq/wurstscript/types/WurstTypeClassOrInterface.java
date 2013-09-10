package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;

public abstract class WurstTypeClassOrInterface extends WurstTypeNamedScope {

	public WurstTypeClassOrInterface(List<WurstType> typeParameters,
			boolean isStaticRef) {
		super(typeParameters, isStaticRef);
	}

	public WurstTypeClassOrInterface(List<WurstType> newTypes) {
		super(newTypes);
	}

	
	@Override
	public abstract StructureDef getDef();
	
}
