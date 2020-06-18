package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Utils;

import java.util.Optional;

import org.eclipse.jdt.annotation.Nullable;


public class WurstTypeInt extends WurstTypePrimitive {

    private static final WurstTypeInt instance = new WurstTypeInt();

    // make constructor private as we only need one instance
    protected WurstTypeInt() {
        super("integer");
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping,
            VariablePosition variablePosition) {
        return (other instanceof WurstTypeInt
                // in jass code we can use an int where a real is expected
                || other instanceof WurstTypeReal && Utils.isJassCode(Optional.ofNullable(location))) ? mapping : null;
    }


    public static WurstTypeInt instance() {
        return instance;
    }

    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        return JassIm.ImIntVal(0);
    }

    @Override
    public String toString() {
        return "int";
    }
}
