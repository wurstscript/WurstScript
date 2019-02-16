package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.eclipse.jdt.annotation.Nullable;

/**
 * the exact type is not known but it will be whatever you want it to be ;)
 * (used for the builtin/native functions, where we cannot check the types)
 */
public class WurstTypeInfer extends WurstType {

    private static WurstType instance = new WurstTypeInfer();

    private WurstTypeInfer() {
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        return mapping;
    }

    @Override
    public String getName() {
        return "<Infer type>";
    }

    @Override
    public String getFullName() {
        return getName();
    }

    public static WurstType instance() {
        return instance;
    }

    @Override
    public ImType imTranslateType(ImTranslator tr) {
        throw new Error("not implemented");
    }

    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        throw new Error("not implemented");
    }

    @Override
    protected boolean isNullable() {
        return false;
    }


}
