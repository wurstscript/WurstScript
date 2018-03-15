package de.peeeq.wurstscript.types;

import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.NameLinkType;
import org.eclipse.jdt.annotation.Nullable;

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
    public @Nullable NameLink findSingleAbstractMethod() {
        Multimap<String, NameLink> nameLinks = getDef().attrNameLinks();
        NameLink abstractMethod = null;
        for (NameLink nl : nameLinks.values()) {
            if (nl.getType() == NameLinkType.FUNCTION
                    && nl.getNameDef().attrIsAbstract()) {
                if (abstractMethod != null) {
                    // there is more than one abstract function
                    // --> closure cannot implement this
                    return null;
                }
                abstractMethod = nl;
            }
        }
        return abstractMethod;
    }
}
