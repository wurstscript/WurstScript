package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import fj.data.TreeMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;


public class WurstTypeVoid extends WurstType {

    private static final WurstTypeVoid instance = new WurstTypeVoid();

    // make constructor private as we only need one instance
    private WurstTypeVoid() {
    }

    @Override
    @Nullable TreeMap<TypeParamDef, WurstTypeBoundTypeParam> matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, Collection<TypeParamDef> typeParams, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping) {
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
    public ImType imTranslateType() {
        return JassIm.ImVoid();
    }

    @Override
    public ImExprOpt getDefaultValue() {
        return JassIm.ImNoExpr();
    }

    @Override
    public boolean isVoid() {
        return true;
    }

    @Override
    public boolean structuralEquals(WurstType other) {
        return this == other;
    }

}
