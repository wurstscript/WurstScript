package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.eclipse.jdt.annotation.Nullable;


public class WurstTypeBool extends WurstTypePrimitive {

    private static final WurstTypeBool instance = new WurstTypeBool();

    // make constructor private as we only need one instance
    private WurstTypeBool() {
        super("boolean");
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        return other instanceof WurstTypeBool ? mapping : null;
    }

    public static WurstTypeBool instance() {
        return instance;
    }

    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        return JassIm.ImBoolVal(false);
    }


}
