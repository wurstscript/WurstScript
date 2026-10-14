package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.NativeType;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.eclipse.jdt.annotation.Nullable;

public class WurstNativeType extends WurstType {

    private String name;
    private WurstType superType;
    private @Nullable NativeType definition;

    private WurstNativeType() {
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        if (other instanceof WurstNativeType nt) {
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

    public static WurstNativeType instance(NativeType definition, WurstType superType) {
        WurstNativeType t = instance(definition.getName(), superType);
        t.definition = definition;
        return t;
    }

    @Override
    public ImType imTranslateType(ImTranslator tr) {
        // Native types keep their source identity, including their supertypes: a selected subtype
        // cannot emit a reference to a native declaration outside the compilation view.
        for (WurstType type = this; type instanceof WurstNativeType nativeType; type = nativeType.superType) {
            if (nativeType.definition != null) {
                tr.requireCompilationUnit(nativeType.definition);
            }
        }
        return JassIm.ImSimpleType(name);
    }

    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        return JassIm.ImNull(imTranslateType(tr));
    }

    @Override
    protected boolean isNullable() {
        return true;
    }

}
