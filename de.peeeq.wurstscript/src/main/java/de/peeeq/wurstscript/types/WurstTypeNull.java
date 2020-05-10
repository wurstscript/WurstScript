package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Utils;

import java.util.Optional;

import org.eclipse.jdt.annotation.Nullable;


public class WurstTypeNull extends WurstType {

    private static final WurstTypeNull instance = new WurstTypeNull();

    private WurstTypeNull() { }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping,
            VariablePosition variablePosition) {
        if (other.isNullable()) {
            return mapping;
        }
        if (Utils.isJassCode(Optional.of(location))
                && (other instanceof WurstTypeInt || other instanceof WurstTypeIntLiteral)) {
            return mapping;
        }

        return null;
    }

    @Override
    public String getName() {
        return "null";
    }

    @Override
    public String getFullName() {
        return "null";
    }

    @Override
    public ImType imTranslateType(ImTranslator tr) {
        return JassIm.ImAnyType();
    }


    public static WurstTypeNull instance() {
        return instance;
    }

    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        return JassIm.ImIntVal(0);
    }

    @Override
    protected boolean isNullable() {
        return true;
    }

}
