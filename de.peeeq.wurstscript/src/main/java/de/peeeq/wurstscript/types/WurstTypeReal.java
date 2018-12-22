package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.JassIm;
import fj.data.TreeMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;


public class WurstTypeReal extends WurstTypePrimitive {

    private static final WurstTypeReal instance = new WurstTypeReal();

    // make constructor private as we only need one instance
    private WurstTypeReal() {
        super("real");
    }

    @Override
    @Nullable VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, Collection<TypeParamDef> typeParams, VariableBinding mapping) {
        return other instanceof WurstTypeReal ? mapping : null;
    }


    public static WurstTypeReal instance() {
        return instance;
    }

    @Override
    public ImExprOpt getDefaultValue() {
        return JassIm.ImRealVal("0.");
    }


}
