package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.TypeExprList;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import io.vavr.control.Option;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class WurstTypeTypeParam extends WurstType {

    private final TypeParamDef def;
    /**
     * True when this stands for the type parameter itself rather than a value of it, as in the
     * receiver of {@code T.toIndex(x)}. Only this form exposes the methods required by the bounds.
     */
    private final boolean staticRef;

    public WurstTypeTypeParam(TypeParamDef t) {
        this(t, false);
    }

    public WurstTypeTypeParam(TypeParamDef t, boolean staticRef) {
        this.def = t;
        this.staticRef = staticRef;
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        if (variablePosition == VariablePosition.LEFT) {
            Option<WurstTypeBoundTypeParam> binding = mapping.get(def);
            if (binding.isDefined()) {
                // already bound, use bound type
                return binding.get().matchAgainstSupertypeIntern(other, location, mapping, variablePosition);
            } else if (mapping.isVar(def)) {
                // not bound -> add mapping
                return mapping.set(def, new WurstTypeBoundTypeParam(def, other, location));
            }
        }
        if (other instanceof WurstTypeTypeParam) {
            WurstTypeTypeParam other2 = (WurstTypeTypeParam) other;
            if (other2.def == this.def) {
                // same type parameter, no change and match
                return mapping;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return def.getName();
    }

    @Override
    public String getFullName() {
        return getName() + " (type parameter line " + def.getSource().getLine() + ")";
    }

    public TypeParamDef getDef() {
        return def;
    }

    @Override
    public boolean isStaticRef() {
        return staticRef;
    }

    /** The same type parameter, seen as the type itself rather than as a value of it. */
    public WurstTypeTypeParam asStaticRef() {
        return staticRef ? this : new WurstTypeTypeParam(def, true);
    }

    @Override
    public VariableBinding getTypeArgBinding() {
        return VariableBinding.emptyMapping();
    }

    @Override
    public WurstType setTypeArgs(VariableBinding typeParamBounds) {
        if (typeParamBounds.contains(def)) {
            return typeParamBounds.get(def).get();
        }
        return this;
    }

    @Override
    public void addMemberMethods(Element node, String name, List<FuncLink> result) {
        if (!staticRef) {
            return;
        }
        // Bounds are ordered and an earlier one wins, but only over the same signature: two bounds
        // may require the very same operation, and offering both would make every call ambiguous.
        // Differently shaped overloads are not in competition, so later bounds still contribute
        // them and overload resolution picks between them as usual.
        List<FuncLink> supplied = new ArrayList<>();
        for (InterfaceDef bound : TypeClassConstraints.boundInterfaces(def)) {
            for (FuncDef method : bound.getMethods()) {
                if (!method.getName().equals(name)) {
                    continue;
                }
                FuncLink candidate = requirementLink(node, bound, method);
                if (!alreadySupplied(supplied, candidate, node)) {
                    supplied.add(candidate);
                }
            }
        }
        result.addAll(supplied);
    }

    /** True when an earlier bound already supplied a requirement of the same shape. */
    private static boolean alreadySupplied(List<FuncLink> supplied, FuncLink candidate, Element node) {
        for (FuncLink existing : supplied) {
            List<WurstType> a = existing.getParameterTypes();
            List<WurstType> b = candidate.getParameterTypes();
            if (a.size() != b.size()) {
                continue;
            }
            boolean same = true;
            for (int i = 0; i < a.size(); i++) {
                if (!a.get(i).equalsType(b.get(i), node)) {
                    same = false;
                    break;
                }
            }
            if (same) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Stream<FuncLink> getMemberMethods(Element node) {
        if (!staticRef) {
            return Stream.empty();
        }
        return TypeClassConstraints.boundInterfaces(def).stream()
                .flatMap(bound -> bound.getMethods().stream()
                        .map(method -> requirementLink(node, bound, method)));
    }

    /**
     * Exposes one interface method as a requirement of this type parameter: the interface's own
     * type parameter is substituted by this one, and the receiver becomes the type parameter, so
     * the call reads {@code T.f(args)} with the arguments exactly as declared.
     */
    private FuncLink requirementLink(Element node, InterfaceDef bound, FuncDef method) {
        TypeParamDef ifaceParam = bound.getTypeParameters().get(0);
        VariableBinding binding = VariableBinding.emptyMapping()
                .set(ifaceParam, new WurstTypeBoundTypeParam(ifaceParam, new WurstTypeTypeParam(def), node));
        return FuncLink.create(method, bound)
                .withTypeArgBinding(node, binding)
                .withReceiverType(this);
    }

    @Override
    public ImType imTranslateType(ImTranslator tr) {
        if (hasTypeConstraints()) {
            return JassIm.ImTypeVarRef(tr.getTypeVar(def));
        }
        return JassIm.ImAnyType();
    }

    /** Using the new template generics with type constraints*/
    private boolean hasTypeConstraints() {
        return def.getTypeParamConstraints() instanceof TypeExprList;
    }

    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        return JassIm.ImNull(this.imTranslateType(tr));
    }


    @Override
    public boolean isCastableToInt() {
        return !hasTypeConstraints();
    }

    @Override
    protected boolean isNullable() {
        return !hasTypeConstraints();
    }

}
