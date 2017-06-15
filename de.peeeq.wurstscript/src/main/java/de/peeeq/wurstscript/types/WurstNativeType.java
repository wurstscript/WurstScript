package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;

public class WurstNativeType extends WurstType {

    private String name;
    private WurstType superType;

    private WurstNativeType() {
    }

    @Override
    public boolean isSubtypeOfIntern(WurstType other, Element location) {
        if (other instanceof WurstNativeType) {
            return ((WurstNativeType) other).name.equals(name)
                    || superType.isSubtypeOfIntern(other, location);
        }
        return superType.isSubtypeOfIntern(other, location);
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
    public ImType imTranslateType() {
        return JassIm.ImSimpleType(name);
    }

    @Override
    public ImExprOpt getDefaultValue() {
        return JassIm.ImNull();
    }

}
