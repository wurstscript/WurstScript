package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.EnumDef;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

import java.util.List;


public class WurstTypeEnum extends WurstTypeNamedScope {


    private final EnumDef edef;

    public WurstTypeEnum(boolean isStaticRef, EnumDef edef, VariableBinding captured) {
        super(isStaticRef, captured);
        if (edef == null) throw new IllegalArgumentException();
        this.edef = edef;
    }

    public WurstTypeEnum(boolean isStaticRef, EnumDef edef) {
        super(isStaticRef);
        if (edef == null) throw new IllegalArgumentException();
        this.edef = edef;
    }

    @Override
    public EnumDef getDef() {
        return edef;
    }

    @Override
    public WurstType replaceTypeVarsWithCaptured(List<WurstTypeBoundTypeParam> newTypes, VariableBinding newCaptured) {
        return new WurstTypeEnum(isStaticRef(), edef, newCaptured);
    }

    @Override
    public String getName() {
        return getDef().getName();
    }

    @Override
    public WurstType dynamic() {
        return new WurstTypeEnum(false, edef);
    }

    @Override
    public WurstType replaceTypeVars(List<WurstTypeBoundTypeParam> newTypes) {
        return this;
    }


    @Override
    public ImType imTranslateType(ImTranslator tr) {
        return TypesHelper.imInt();
    }

    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        return JassIm.ImIntVal(0);
    }


    @Override
    public boolean isCastableToInt() {
        return true;
    }

}
