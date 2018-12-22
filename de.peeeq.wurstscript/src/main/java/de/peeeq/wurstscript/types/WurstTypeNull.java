package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.utils.Utils;
import fj.data.TreeMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;


public class WurstTypeNull extends WurstTypePrimitive {

    private static final WurstTypeNull instance = new WurstTypeNull();

    // make constructor private as we only need one instance
    protected WurstTypeNull() {
        super("null");
    }

    @Override
    @Nullable VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, Collection<TypeParamDef> typeParams, VariableBinding mapping) {
        if (other instanceof WurstTypeNull
                || other instanceof WurstTypeHandle
                || other instanceof WurstNativeType
                || other instanceof WurstTypeString
                || other instanceof WurstTypeCode
                || other instanceof WurstTypeClass
                || other instanceof WurstTypeInterface
                || other instanceof WurstTypeModule
                || other instanceof WurstTypeModuleInstanciation
                || other instanceof WurstTypeTypeParam
                || other instanceof WurstTypeBoundTypeParam
                || Utils.isJassCode(location) && (other instanceof WurstTypeInt || other instanceof WurstTypeIntLiteral)) {
            return mapping;
        }
        return null;
    }


    public static WurstTypeNull instance() {
        return instance;
    }

    @Override
    public ImExprOpt getDefaultValue() {
        return JassIm.ImIntVal(0);
    }


}
