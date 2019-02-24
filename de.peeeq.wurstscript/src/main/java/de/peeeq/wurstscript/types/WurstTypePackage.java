package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

import java.util.List;


public class WurstTypePackage extends WurstTypeNamedScope {


    private WPackage pack;

    public WurstTypePackage(WPackage pack) {
        super(true);
        if (pack == null) throw new IllegalArgumentException();
        this.pack = pack;
    }

    @Override
    public NamedScope getDef() {
        return pack;
    }

    @Override
    public String getName() {
        return getDef().getName() + printTypeParams() + " (package)";
    }

    @Override
    public WurstType dynamic() {
        return this;
    }

    @Override
    public WurstType replaceTypeVars(List<WurstTypeBoundTypeParam> newTypes) {
        return this;
    }

    @Override
    public ImType imTranslateType(ImTranslator tr) {
        throw new Error("not implemented");
    }

    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        throw new Error("not implemented");
    }

}
