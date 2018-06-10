package de.peeeq.wurstscript.attributes.names;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;

import java.util.Map;


public class TypeLink extends NameLink {
    private final TypeDef def;

    public TypeLink(Visibility visibility, WScope definedIn, TypeDef def) {
        super(visibility, definedIn);
        this.def = def;
    }

    public static TypeLink create(TypeDef def, WScope definedIn) {
        return new TypeLink(calcVisibility(definedIn, def), definedIn, def);
    }

    @Override
    public String getName() {
        return def.getName();
    }

    @Override
    public TypeDef getDef() {
        return def;
    }

    @Override
    public NameLink withVisibility(Visibility newVis) {
        return new TypeLink(newVis, getDefinedIn(), def);
    }

    @Override
    public boolean receiverCompatibleWith(WurstType receiverType, Element location) {
        return receiverType == null;
    }

    @Override
    public NameLink withTypeArgBinding(Element context, Map<TypeParamDef, WurstTypeBoundTypeParam> binding) {
        // If we had dynamic inner-classes, it might be necessary to adapt them here,
        // but currently type links are not parameterized
        return this;
    }


    public TypeLink hidingPrivate() {
        return (TypeLink) super.hidingPrivate();
    }

    public TypeLink hidingPrivateAndProtected() {
        return (TypeLink) super.hidingPrivateAndProtected();
    }

}
