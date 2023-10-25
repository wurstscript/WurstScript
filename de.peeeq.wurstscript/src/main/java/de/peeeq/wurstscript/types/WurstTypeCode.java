package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.eclipse.jdt.annotation.Nullable;


public class WurstTypeCode extends WurstTypePrimitive {

    private static final WurstTypeCode instance = new WurstTypeCode();

    // make constructor private as we only need one instance
    private WurstTypeCode() {
        super("code");
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        return other instanceof WurstTypeCode ? mapping : null;
    }

    public static WurstTypeCode instance() {
        return instance;
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
