package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import fj.data.Option;
import fj.data.TreeMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class WurstTypeTypeParam extends WurstType {

    private TypeParamDef def;

    public WurstTypeTypeParam(TypeParamDef t) {
        this.def = t;
    }

    @Override
    @Nullable TreeMap<TypeParamDef, WurstTypeBoundTypeParam> matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, Collection<TypeParamDef> typeParams, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping) {
        Option<WurstTypeBoundTypeParam> binding = mapping.get(def);
        if (binding.isSome()) {
            // already bound, use bound type
            return binding.some().matchAgainstSupertypeIntern(other, location, typeParams, mapping);
        } else if (other instanceof WurstTypeTypeParam) {
            WurstTypeTypeParam other2 = (WurstTypeTypeParam) other;
            if (other2.def == this.def) {
                // same type parameter, no change and match
                return mapping;
            }
        } else if (typeParams.contains(def)) {
            // not bound -> add mapping
            return mapping.set(def, new WurstTypeBoundTypeParam(def, other, location));
        }
        return null;
    }

    @Override
    public String getName() {
        return def.getName();
    }

    @Override
    public String getFullName() {
        return getName() + " (type parameter)";
    }

    public TypeParamDef getDef() {
        return def;
    }

    @Override
    public Map<TypeParamDef, WurstTypeBoundTypeParam> getTypeArgBinding() {
        return Collections.emptyMap();
    }

    @Override
    public WurstType setTypeArgs(Map<TypeParamDef, WurstTypeBoundTypeParam> typeParamBounds) {
        if (typeParamBounds.containsKey(def)) {
            return typeParamBounds.get(def);
        }
        return this;
    }

    @Override
    public ImType imTranslateType() {
        return TypesHelper.imInt();
    }

    @Override
    public ImExprOpt getDefaultValue() {
        return JassIm.ImNull();
    }


    @Override
    public boolean isCastableToInt() {
        return true;
    }

}
