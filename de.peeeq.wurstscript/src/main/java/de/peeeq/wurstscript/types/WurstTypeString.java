package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.JassIm;
import fj.data.TreeMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;


public class WurstTypeString extends WurstTypePrimitive {

    private static final WurstTypeString instance = new WurstTypeString();

    // make constructor private as we only need one instance
    private WurstTypeString() {
        super("string");
    }

    @Override
    @Nullable TreeMap<TypeParamDef, WurstTypeBoundTypeParam> matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, Collection<TypeParamDef> typeParams, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping) {
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
    public ImExprOpt getDefaultValue() {
        return JassIm.ImStringVal("");
    }

}
