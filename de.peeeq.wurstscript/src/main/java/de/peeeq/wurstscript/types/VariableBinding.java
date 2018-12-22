package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.TypeParamDef;
import fj.P2;
import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/**
 * VariableBinding
 */
public class VariableBinding implements Iterable<P2<TypeParamDef, WurstTypeBoundTypeParam>> {
    public static final VariableBinding EMPTY_MAPPING = new VariableBinding(TreeMap.empty(TypeParamOrd.instance()));
    private final TreeMap<TypeParamDef, WurstTypeBoundTypeParam> binding;


    public VariableBinding(TreeMap<TypeParamDef, WurstTypeBoundTypeParam> binding) {
        this.binding = binding;
    }

    @NotNull
    public static VariableBinding emptyMapping() {
        return EMPTY_MAPPING;
    }

    public boolean contains(TypeParamDef v) {
        return binding.contains(v);
    }


    public Option<WurstTypeBoundTypeParam> get(TypeParamDef v) {
        return binding.get(v);
    }

    public VariableBinding set(TypeParamDef v, WurstTypeBoundTypeParam b) {
        return new VariableBinding(binding.set(v, b));
    }

    public boolean isEmpty() {
        return binding.isEmpty();
    }

    public VariableBinding union(VariableBinding other) {
        if (this.isEmpty()) {
            return other;
        } else if (other.isEmpty()) {
            return this;
        }
        return new VariableBinding(binding.union(other.binding));
    }


    public List<TypeParamDef> keys() {
        return binding.keys();
    }

    @NotNull
    @Override
    public Iterator<P2<TypeParamDef, WurstTypeBoundTypeParam>> iterator() {
        return binding.iterator();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[");
        for (P2<TypeParamDef, WurstTypeBoundTypeParam> e : binding) {
            if (s.length() > 1) {
                s.append(", ");
            }
            s.append(e._1().getName());
            s.append(" -> ");
            s.append(e._2().getBaseType());
        }
        s.append("]");
        return s.toString();
    }
}