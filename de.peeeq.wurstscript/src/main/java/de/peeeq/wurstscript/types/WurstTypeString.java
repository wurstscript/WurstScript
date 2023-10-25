package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.eclipse.jdt.annotation.Nullable;


public class WurstTypeString extends WurstTypePrimitive {

    private static final WurstTypeString instance = new WurstTypeString();

    // make constructor private as we only need one instance
    private WurstTypeString() {
        super("string");
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        return other instanceof WurstTypeString ? mapping : null;
    }


    @Override
    public String getName() {
        return "string";
    }

    @Override
    public String getFullName() {
        return "string";
    }

    public static WurstTypeString instance() {
        return instance;
    }

    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        return JassIm.ImStringVal("");
    }


    @Override
    protected boolean isNullable() {
        return true;
    }

}
