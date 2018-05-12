package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import fj.data.TreeMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;

public class WurstTypeUnion extends WurstType {

    WurstType typeA;
    WurstType typeB;

    private WurstTypeUnion(WurstType typeA, WurstType typeB) {
        this.typeA = typeA;
        this.typeB = typeB;
    }

    public static WurstType create(WurstType a, WurstType b, Element loc) {
        if (a instanceof WurstTypeUnknown) return b;
        if (b instanceof WurstTypeUnknown) return a;
        // TODO simplify types
		if (a.isSubtypeOf(b, loc)) return b;
		if (b.isSubtypeOf(a, loc)) return a;
        return new WurstTypeUnion(a, b);
    }

    @Override
    @Nullable TreeMap<TypeParamDef, WurstTypeBoundTypeParam> matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, Collection<TypeParamDef> typeParams, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping) {
        mapping = typeA.matchAgainstSupertype(other, location, typeParams, mapping);
        if (mapping == null) {
            return null;
        }
        return typeB.matchAgainstSupertype(other, location, typeParams, mapping);
    }

    @Override
    public String getName() {
        return "(" + typeA.getName() + " or " + typeB.getName() + ")";
    }

    @Override
    public String getFullName() {
        return "(" + typeA.getFullName() + " or " + typeB.getFullName() + ")";
    }

    @Override
    public ImType imTranslateType() {
        // TODO union of typeA and typeB
        return typeA.imTranslateType();
    }

    @Override
    public ImExprOpt getDefaultValue() {
        return typeA.getDefaultValue();
    }

    public WurstType getTypeA() {
        return typeA;
    }

    public WurstType getTypeB() {
        return typeB;
    }

}
