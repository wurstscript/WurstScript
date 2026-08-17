package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.TypeExprList;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import io.vavr.control.Option;
import org.eclipse.jdt.annotation.Nullable;

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
        // The parameter stands for itself, so a requirement keeps the shape it was declared with.
        TypeClassConstraints.addRequirementMethods(def, new WurstTypeTypeParam(def), this, node, name, result);
    }

    @Override
    public Stream<FuncLink> getMemberMethods(Element node) {
        if (!staticRef) {
            return Stream.empty();
        }
        return TypeClassConstraints.requirementMethods(def, new WurstTypeTypeParam(def), this, node);
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
