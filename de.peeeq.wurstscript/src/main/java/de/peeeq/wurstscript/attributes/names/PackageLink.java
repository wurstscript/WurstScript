package de.peeeq.wurstscript.attributes.names;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;

import java.util.Collections;
import java.util.Map;


public class PackageLink extends DefLink {
    private final WPackage def;

    public PackageLink(Visibility visibility, WScope definedIn, WPackage def) {
        super(visibility, definedIn, Collections.emptyList(), null);
        this.def = def;
    }

    public static PackageLink create(WPackage def, WScope definedIn) {
        return new PackageLink(calcVisibility(definedIn, def), definedIn, def);
    }

    @Override
    public String getName() {
        return def.getName();
    }

    @Override
    public WPackage getDef() {
        return def;
    }

    @Override
    public PackageLink withVisibility(Visibility newVis) {
        return new PackageLink(newVis, getDefinedIn(), def);
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
    public PackageLink withTypeArgBinding(Element context, Map<TypeParamDef, WurstTypeBoundTypeParam> binding) {
        // packages do not have type paramaters
        return this;
    }


    public PackageLink hidingPrivate() {
        return (PackageLink) super.hidingPrivate();
    }

    public PackageLink hidingPrivateAndProtected() {
        return (PackageLink) super.hidingPrivateAndProtected();
    }

}
