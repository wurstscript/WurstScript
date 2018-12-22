package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.JassIm;
import org.eclipse.jdt.annotation.Nullable;


public class WurstTypeIntLiteral extends WurstTypePrimitive {

    private static final WurstTypeIntLiteral instance = new WurstTypeIntLiteral();

    // make constructor private as we only need one instance
    protected WurstTypeIntLiteral() {
        super("integer-literal");
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        return (other instanceof WurstTypeIntLiteral
                || other instanceof WurstTypeInt
                || other instanceof WurstTypeReal) ? mapping : null;
    }


    public static WurstTypeIntLiteral instance() {
        return instance;
    }

    @Override
    public ImExprOpt getDefaultValue() {
        return JassIm.ImIntVal(0);
    }

    @Override
    public ImSimpleType imTranslateType() {
        return TypesHelper.imInt();
    }


}
