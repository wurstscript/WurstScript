package de.peeeq.wurstscript.types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.CheckHelper;
import de.peeeq.wurstscript.attributes.names.DefLink;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import fj.data.TreeMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

    /**
     * Level in the type hierarchy.
     * Root is at 0, subtypes one below max supertype
     */
    protected int level() {
        return getDef().attrLevel();
    }

    @Override
    public boolean canBeUsedInInstanceOf() {
        return true;
    }


    @Override
    public boolean isCastableToInt() {
        return true;
    }


    public abstract ImmutableList<? extends WurstTypeClassOrInterface> directSupertypes();


    public ImmutableList<? extends WurstTypeClassOrInterface> transitiveSupertypes() {
        ImmutableList.Builder<WurstTypeClassOrInterface> builder = ImmutableList.builder();
        for (WurstTypeClassOrInterface st : directSupertypes()) {
            builder.add(st);
            builder.addAll(st.transitiveSupertypes());
        }
        return builder.build();
    }

    public ImmutableList<WurstTypeInterface> transitiveSuperInterfaces() {
        return transitiveSupertypes().stream()
                .filter(t -> t instanceof WurstTypeInterface)
                .map(t -> (WurstTypeInterface) t)
                .collect(ImmutableList.toImmutableList());
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

                for (DefLink other : nameLinks.get(nl.getName())) {
                    if (other != nl
                            && other.getDef().attrIsOverride()
                            && !other.getDef().attrIsAbstract()
                            && other instanceof FuncLink
                            && CheckHelper.isRefinement(this.getTypeArgBinding(), ((FuncLink) other).getDef(), ((FuncLink) nl).getDef())) {
                        // the abstract method is overridden, so it is not really abstract
                        // TODO check: why are we including overridden methods anyway?
                        continue withNextNameLink;
                    }
                }
                if (abstractMethod != null) {
                    // there is more than one abstract function
                    // --> closure cannot implement this
                    return null;
                }
                abstractMethod = ((FuncLink) nl);
            }
        }
        if (abstractMethod != null) {
            return abstractMethod.withTypeArgBinding(context, getTypeArgBinding());
        }
        return null;
    }


    @Override
    @Nullable TreeMap<TypeParamDef, WurstTypeBoundTypeParam> matchAgainstSupertypeIntern(WurstType obj, @Nullable Element location, Collection<TypeParamDef> typeParams, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping) {
        // direct match
        TreeMap<TypeParamDef, WurstTypeBoundTypeParam> superMapping = super.matchAgainstSupertypeIntern(obj, location, typeParams, mapping);
        if (superMapping != null) {
            return superMapping;
        }
        // if no direct match, try to match super-types:
        // OPT this could be optimized -- only do this if obj is an interface type
        for (WurstTypeClassOrInterface implementedInterface : directSupertypes()) {

            TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping2 = implementedInterface.matchAgainstSupertype(obj, location, typeParams, mapping);
            if (mapping2 != null) {
                return mapping2;
            }
        }
        return null;
    }

    @Override
    public final ImType imTranslateType(ImTranslator tr) {
        ImClass c = tr.getClassFor(getDef());
        return JassIm.ImClassType(c);
    }

}
