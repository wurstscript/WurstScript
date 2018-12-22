package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.JassIm;
import org.eclipse.jdt.annotation.Nullable;


public class WurstTypeHandle extends WurstTypePrimitive {

    private static final WurstTypeHandle instance = new WurstTypeHandle();

    // make constructor private as we only need one instance
    private WurstTypeHandle() {
        super("handle");
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        return other instanceof WurstTypeHandle ? mapping : null;
    }


    public static WurstTypeHandle instance() {
        return instance;
    }

    @Override
    public ImExprOpt getDefaultValue() {
        return JassIm.ImNull(imTranslateType());
    }


}
