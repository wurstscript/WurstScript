package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.CompileError;
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
    public static final VariableBinding EMPTY_MAPPING = new VariableBinding(TreeMap.empty(TypeParamOrd.instance()), List.nil(), List.nil());
    private final TreeMap<TypeParamDef, WurstTypeBoundTypeParam> binding;
    private final List<TypeParamDef> typeVariablesLeft;
    private final List<CompileError> errors;


    public VariableBinding(TreeMap<TypeParamDef, WurstTypeBoundTypeParam> binding, List<TypeParamDef> typeVariablesLeft, List<CompileError> errors) {
        this.binding = binding;
        this.typeVariablesLeft = typeVariablesLeft;
        this.errors = errors;
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
        return new VariableBinding(binding.set(v, b), typeVariablesLeft.removeAll(v::equals), errors);
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
        VariableBinding res = new VariableBinding(binding, typeVariablesLeft, errors.append(errors));
        for (P2<TypeParamDef, WurstTypeBoundTypeParam> e : other.binding) {
            res = res.set(e._1(), e._2());
        }
        return res;
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
        for (P2<TypeParamDef, WurstTypeBoundTypeParam> e : binding) {
            if (!first) {
                s.append(", ");
            }
            s.append(e._1().getName());
            s.append(" -> ");
            s.append(e._2().getBaseType());
            first = false;
        }
        s.append("]");
        if (errors.isNotEmpty()) {
            s.append("{");
            for (CompileError error : errors) {
                s.append(error);
            }
            s.append("}");
        }
        return s.toString();
    }

    public VariableBinding withTypeVariables(java.util.List<TypeParamDef> vars) {
        return new VariableBinding(binding, typeVariablesLeft.append(List.iterableList(vars)), errors);
    }

    public VariableBinding withTypeVariables(List<TypeParamDef> vars) {
        return new VariableBinding(binding, typeVariablesLeft.append(vars), errors);
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
        return new VariableBinding(binding, typeVariablesLeft, errors.cons(err));
    }

    public List<CompileError> getErrors() {
        return errors;
    }
}
