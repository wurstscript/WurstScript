package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import fj.data.TreeMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;

public class WurstNativeType extends WurstType {

    private String name;
    private WurstType superType;

    private WurstNativeType() {
    }

    @Override
    @Nullable TreeMap<TypeParamDef, WurstTypeBoundTypeParam> matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, Collection<TypeParamDef> typeParams, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping) {
        if (other instanceof WurstNativeType) {
            WurstNativeType nt = (WurstNativeType) other;
            if (nt.name.equals(name)) {
                return mapping;
            }
        }
        return superType.matchAgainstSupertypeIntern(other, location, typeParams, mapping);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFullName() {
        return name;
    }

    public static WurstNativeType instance(String name, WurstType superType) {
        WurstNativeType t = new WurstNativeType();
        t.name = name;
        t.superType = superType.dynamic();
        return t;
    }

    @Override
    public ImType imTranslateType(ImTranslator tr) {
        return JassIm.ImSimpleType(name);
    }

    @Override
    public ImExprOpt getDefaultValue() {
        return JassIm.ImNull();
    }

}
