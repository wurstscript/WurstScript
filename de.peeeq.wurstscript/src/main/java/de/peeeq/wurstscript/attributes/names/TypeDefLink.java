package de.peeeq.wurstscript.attributes.names;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import de.peeeq.wurstscript.types.WurstTypePackage;
import fj.data.TreeMap;

import java.util.Collections;
import java.util.List;


public class TypeDefLink extends DefLink {
    private final TypeDef def;

    public TypeDefLink(Visibility visibility, WScope definedIn, TypeDef def) {
        super(visibility, definedIn, Collections.emptyList(), null);
        this.def = def;
    }

    public static TypeDefLink create(TypeDef def, WScope definedIn) {
        return new TypeDefLink(calcVisibility(definedIn, def), definedIn, def);
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
    public TypeDefLink withVisibility(Visibility newVis) {
        return new TypeDefLink(newVis, getDefinedIn(), def);
    }

    @Override
    public boolean receiverCompatibleWith(WurstType receiverType, Element location) {
        return receiverType == null;
    }

    @Override
    public NameLinkType getType() {
        return null;
    }

    @Override
    public TypeDefLink withTypeArgBinding(Element context, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> binding) {
        // packages do not have type paramaters
        return this;
    }

    @Override
    public DefLink withGenericTypeParams(List<TypeParamDef> typeParams) {
        return this;
    }

    @Override
    public WurstType getTyp() {
        return def.attrTyp();
    }

    @Override
    public TypeDefLink withDef(NameDef def) {
        return new TypeDefLink(getVisibility(), getDefinedIn(), (TypeDef) def);
    }


    public TypeDefLink hidingPrivate() {
        return (TypeDefLink) super.hidingPrivate();
    }

    public TypeDefLink hidingPrivateAndProtected() {
        return (TypeDefLink) super.hidingPrivateAndProtected();
    }

}
