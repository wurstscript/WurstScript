package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.StructureDef;

import java.util.List;

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
