package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.eclipse.jdt.annotation.Nullable;


public class WurstTypeVoid extends WurstType {

    private static final WurstTypeVoid instance = new WurstTypeVoid();

    // make constructor private as we only need one instance
    private WurstTypeVoid() {
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        if (other instanceof WurstTypeVoid) {
            return mapping;
        }
        return null;
    }

    @Override
    public String getName() {
        return "Void";
    }

    @Override
    public String getFullName() {
        return "Void";
    }

    public static WurstTypeVoid instance() {
        return instance;
    }

    @Override
    public ImType imTranslateType(ImTranslator tr) {
        return JassIm.ImVoid();
    }

    public ImType imTranslateType() {
        return JassIm.ImVoid();
    }

    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        return JassIm.ImNoExpr();
    }

    @Override
    public boolean isVoid() {
        return true;
    }

    @Override
    protected boolean isNullable() {
        return false;
    }

}
