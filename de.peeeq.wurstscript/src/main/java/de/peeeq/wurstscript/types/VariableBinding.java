package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.CompileError;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.SortedSet;
import io.vavr.collection.HashMap;
import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Spliterators;

/**
 * VariableBinding
 */
public class VariableBinding implements Iterable<Tuple2<TypeParamDef, WurstTypeBoundTypeParam>> {
    public static final VariableBinding EMPTY_MAPPING = new VariableBinding(HashMap.empty(), List.empty(), List.empty());
    private final HashMap<TypeParamDef, WurstTypeBoundTypeParam> binding;
    private final List<TypeParamDef> typeVariablesLeft;
    private final List<CompileError> errors;


    public VariableBinding(HashMap<TypeParamDef, WurstTypeBoundTypeParam> binding, List<TypeParamDef> typeVariablesLeft, List<CompileError> errors) {
        this.binding = binding;
        this.typeVariablesLeft = typeVariablesLeft;
        this.errors = errors;
    }

    @NotNull
    public static VariableBinding emptyMapping() {
        return EMPTY_MAPPING;
    }

    public boolean contains(TypeParamDef v) {
        return binding.containsKey(v);
    }


    public Option<WurstTypeBoundTypeParam> get(TypeParamDef v) {
        return binding.get(v);
    }

    public VariableBinding set(TypeParamDef v, WurstTypeBoundTypeParam b) {
        return new VariableBinding(binding.put(v, b), typeVariablesLeft.removeAll(v::equals), errors);
    }

    public boolean isEmpty() {
        return binding.isEmpty();
    }

    public VariableBinding union(VariableBinding other) {
        if (this.isEmpty() && this.errors.isEmpty()) {
            return other;
        } else if (other.isEmpty() && other.errors.isEmpty()) {
            return this;
        }
        VariableBinding res = new VariableBinding(binding, typeVariablesLeft, errors.appendAll(other.errors));
        for (Tuple2<TypeParamDef, WurstTypeBoundTypeParam> e : other.binding) {
            res = res.set(e._1(), e._2());
        }
        return res;
    }


    public Set<TypeParamDef> keys() {
        return binding.keySet();
    }

    @NotNull
    @Override
    public Iterator<Tuple2<TypeParamDef, WurstTypeBoundTypeParam>> iterator() {
        return binding.iterator();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        boolean first;
        if (hasUnboundTypeVars()) {
            s.append("<");
            first = true;
            for (TypeParamDef t : typeVariablesLeft) {
                if (!first) {
                    s.append(", ");
                }
                s.append(t.getName() + " line " + t.getSource().getLine());
                first = false;
            }
            s.append(">");
        }

        s.append("[");
        first = true;
        for (Tuple2<TypeParamDef, WurstTypeBoundTypeParam> e : binding) {
            if (!first) {
                s.append(", ");
            }
            s.append(e._1().getName());
            s.append(" -> ");
            s.append(e._2().getBaseType());
            first = false;
        }
        s.append("]");
        if (!errors.isEmpty()) {
            s.append("{");
            for (CompileError error : errors) {
                s.append(error);
            }
            s.append("}");
        }
        return s.toString();
    }

    public VariableBinding withTypeVariables(Iterable<TypeParamDef> vars) {
        return new VariableBinding(binding, typeVariablesLeft.appendAll(vars), errors);
    }

    public boolean isVar(TypeParamDef def) {
        return typeVariablesLeft.exists(def::equals);
    }

    public boolean hasUnboundTypeVars() {
        return !typeVariablesLeft.isEmpty();
    }

    public String printUnboundTypeVars() {
        StringBuilder res = new StringBuilder();
        for (TypeParamDef t : typeVariablesLeft) {
            if (res.length() > 0) {
                res.append(", ");
            }
            res.append(t.getName());
        }
        return res.toString();
    }

    public VariableBinding withError(CompileError err) {
        return new VariableBinding(binding, typeVariablesLeft, errors.prepend(err));
    }

    public List<CompileError> getErrors() {
        return errors;
    }
}
