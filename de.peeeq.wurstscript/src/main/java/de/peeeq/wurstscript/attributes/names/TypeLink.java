package de.peeeq.wurstscript.attributes.names;

import com.google.common.collect.ImmutableList;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import de.peeeq.wurstscript.types.WurstTypeDeferred;

import java.util.Collections;
import java.util.List;
import java.util.Map;


public class TypeLink extends NameLink {
    private final WurstType type;
    private final TypeDef def;

    public TypeLink(Visibility visibility, WScope definedIn, List<TypeParamDef> typeParams, TypeDef def, WurstType type) {
        super(visibility, definedIn, typeParams);
        this.def = def;
        this.type = type;
    }

    public static TypeLink create(TypeDef def, WScope definedIn) {
        List<TypeParamDef> typeParams = Collections.emptyList();
        if (def instanceof AstElementWithTypeParameters) {
            typeParams = ImmutableList.copyOf(((AstElementWithTypeParameters) def).getTypeParameters());
        }
        // create deferred type to avoid cyclcic dependencies
        WurstType type = new WurstTypeDeferred(def::attrTyp);
        return new TypeLink(calcVisibility(definedIn, def), definedIn, typeParams, def, type);
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
        return new TypeLink(newVis, getDefinedIn(), typeParams, def, type);
    }

    @Override
    public boolean receiverCompatibleWith(WurstType receiverType, Element location) {
        return receiverType == null;
    }

    @Override
    public TypeLink withTypeArgBinding(Element context, Map<TypeParamDef, WurstTypeBoundTypeParam> binding) {
        // TODO
        return this;
    }

    @Override
    public WurstType getTyp() {
        // TODO add as attribute / handle type args
        return def.attrTyp();
    }


    public TypeLink hidingPrivate() {
        return (TypeLink) super.hidingPrivate();
    }

    public TypeLink hidingPrivateAndProtected() {
        return (TypeLink) super.hidingPrivateAndProtected();
    }

}
