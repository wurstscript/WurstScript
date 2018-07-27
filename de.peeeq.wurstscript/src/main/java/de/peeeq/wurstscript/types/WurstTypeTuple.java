package de.peeeq.wurstscript.types;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.jassIm.*;
import fj.data.TreeMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.List;


public class WurstTypeTuple extends WurstType {

    private TupleDef tupleDef;


    public WurstTypeTuple(TupleDef tupleDef) {
        Preconditions.checkNotNull(tupleDef);
        this.tupleDef = tupleDef;
    }

    @Override
    @Nullable TreeMap<TypeParamDef, WurstTypeBoundTypeParam> matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, Collection<TypeParamDef> typeParams, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping) {
        if (other instanceof WurstTypeTuple) {
            WurstTypeTuple otherTuple = (WurstTypeTuple) other;
            if (tupleDef == otherTuple.tupleDef) {
                return mapping;
            }
        }
        return null;
    }


    public TupleDef getTupleDef() {
        return tupleDef;
    }

    @Override
    public String getName() {
        return tupleDef.getName();
    }

    @Override
    public String getFullName() {
        return getName();
    }


    @Override
    public ImType imTranslateType() {
        List<ImType> types = Lists.newArrayList();
        List<String> names = Lists.newArrayList();
        for (WParameter p : tupleDef.getParameters()) {
            ImType pt = p.attrTyp().imTranslateType();
            if (pt instanceof ImTupleType) {
                ImTupleType ptt = (ImTupleType) pt;
                // add flattened
                for (int i = 0; i < ptt.getTypes().size(); i++) {
                    types.add(ptt.getTypes().get(i));
                    names.add(p.getName() + "_" + ptt.getNames().get(i));
                }
            } else {
                types.add(pt);
                names.add(p.getName());
            }
        }
        return JassIm.ImTupleType(types, names);
    }

    @Override
    public ImExprOpt getDefaultValue() {
        ImExprs exprs = JassIm.ImExprs();
        for (WParameter p : tupleDef.getParameters()) {
            exprs.add((ImExpr) p.attrTyp().getDefaultValue());
        }
        return JassIm.ImTupleExpr(exprs);
    }

    @Override
    public boolean structuralEquals(WurstType other) {
        return other == this
                || other instanceof WurstTypeTuple
                && tupleDef.equals(((WurstTypeTuple) other).tupleDef);
    }
}
