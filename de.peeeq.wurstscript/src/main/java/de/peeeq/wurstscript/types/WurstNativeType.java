package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.eclipse.jdt.annotation.Nullable;

public class WurstNativeType extends WurstType {

    private String name;
    private WurstType superType;

    private WurstNativeType() {
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        if (other instanceof WurstNativeType) {
            WurstNativeType nt = (WurstNativeType) other;
            if (nt.name.equals(name)) {
                return mapping;
            }
        }
        return superType.matchAgainstSupertypeIntern(other, location, mapping, variablePosition);
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

    public ImType imTranslateType() {
        return JassIm.ImSimpleType(name);
    }

    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        return JassIm.ImNull(imTranslateType());
    }

    @Override
    protected boolean isNullable() {
        return true;
    }

}
