package de.peeeq.wurstscript.types;

import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.attributes.names.DefLink;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.translation.imtranslation.OverrideUtils;

import java.util.List;

public abstract class WurstTypeClassOrInterface extends WurstTypeNamedScope {

    public WurstTypeClassOrInterface(List<WurstTypeBoundTypeParam> typeParameters,
                                     boolean isStaticRef) {
        super(typeParameters, isStaticRef);
    }

    public WurstTypeClassOrInterface(List<WurstTypeBoundTypeParam> newTypes) {
        super(newTypes);
    }


    @Override
    public abstract StructureDef getDef();


    @Override
    public boolean canBeUsedInInstanceOf() {
        return true;
    }


    @Override
    public boolean isCastableToInt() {
        return true;
    }

    /**
     * if this type has a single abstract method, return it.
     * Otherwise return null;
     */
    public FuncLink findSingleAbstractMethod(Element context) {
        Multimap<String, DefLink> nameLinks = getDef().attrNameLinks();
        FuncLink abstractMethod = null;
        withNextNameLink:
        for (NameLink nl : nameLinks.values()) {
            if (nl instanceof FuncLink
                    && nl.getDef().attrIsAbstract()) {
                if (abstractMethod != null) {
                    // there is more than one abstract function
                    // --> closure cannot implement this
                    return null;
                }
                for (DefLink other : nameLinks.get(nl.getName())) {
                    if (other != nl && other.getDef().attrIsOverride() && !other.getDef().attrIsAbstract()) {
                        // the abstract method is overridden, so it is not really abstract
                        // TODO check: why are we including overridden methods anyway?
                        continue withNextNameLink;
                    }
                }

                abstractMethod = ((FuncLink) nl);
            }
        }
        if (abstractMethod != null) {
            return abstractMethod.withTypeArgBinding(context, getTypeArgBinding());
        }
        return null;
    }
}
