package de.peeeq.wurstscript.types;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.*;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;
import java.util.stream.Stream;

public abstract class WurstTypeNamedScope extends WurstType {

    private final boolean isStaticRef;
    // TODO change this to a list of TypeParamDef and add typeMapping?
    private final List<WurstTypeBoundTypeParam> typeParameters;



    public WurstTypeNamedScope(List<WurstTypeBoundTypeParam> typeParameters, boolean isStaticRef) {
        this.isStaticRef = isStaticRef;
        this.typeParameters = typeParameters;
    }

    public WurstTypeNamedScope(List<WurstTypeBoundTypeParam> typeParameters) {
        this.isStaticRef = false;
        this.typeParameters = typeParameters;
    }


    public WurstTypeNamedScope(boolean isStaticRef) {
        this.isStaticRef = isStaticRef;
        this.typeParameters = Collections.emptyList();
    }

    @Override
    public String getName() {
        NamedScope def = getDef();
        if (def == null) {
            return "not found";
        }
        return def.getName();
    }

    public abstract @Nullable NamedScope getDef();

    @Override
    public String getFullName() {
        return getName();
    }

    @Override
    public boolean isStaticRef() {
        return isStaticRef;
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType obj, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        if (obj instanceof WurstTypeNamedScope) {
            WurstTypeNamedScope other = (WurstTypeNamedScope) obj;
            if (other.getDef() == this.getDef()) {
                return matchTypeParams(getTypeParameters(), other.getTypeParameters(), location, mapping, variablePosition);
            }
        }
        return null;
    }

    public List<WurstTypeBoundTypeParam> getTypeParameters() {
        return typeParameters;
    }


    protected String printTypeParams() {
        if (typeParameters.size() == 0) {
            return "";
        }
        StringBuilder s = new StringBuilder("<");
        for (int i = 0; i < typeParameters.size(); i++) {
            if (i > 0) {
                s.append(", ");
            }
            s.append(typeParameters.get(i).getName());
        }
        return s + ">";
    }



    @Override
    public VariableBinding getTypeArgBinding() {
        VariableBinding res = VariableBinding.emptyMapping();
        for (WurstTypeBoundTypeParam tp : typeParameters) {
            res = res.set(tp.getTypeParamDef(), tp);
        }
        return res;
    }

    @Override
    public WurstType setTypeArgs(@NonNull VariableBinding typeParamBounds) {
        List<WurstTypeBoundTypeParam> newTypes = Lists.newArrayList();
        for (WurstTypeBoundTypeParam t : typeParameters) {
            newTypes.add(t.setTypeArgs(typeParamBounds));
        }
        return replaceTypeVars(newTypes);
    }

    abstract public WurstType replaceTypeVars(List<WurstTypeBoundTypeParam> newTypes);

    public WurstType replaceTypeVarsUsingTypeArgs(TypeExprList typeArgs) {
        if (typeArgs.isEmpty()) {
            // TODO replace with unknown types?
            return this;
        }
        List<WurstTypeBoundTypeParam> typeParams = new ArrayList<>();

        if (typeArgs.size() != typeParameters.size()) {
            typeArgs.addError("Expected " + typeParameters.size() + " type arguments, but got " + typeArgs.size());
        }

        for (int i = 0; i < typeArgs.size() && i < typeParameters.size(); i++) {
            WurstTypeBoundTypeParam tp = typeParameters.get(i);
            TypeParamDef tpDef = tp.getTypeParamDef();
            TypeExpr typeArg = typeArgs.get(i);
            WurstType baseType = typeArg.attrTyp().dynamic();
            typeParams.add(new WurstTypeBoundTypeParam(tpDef, baseType, typeArg));
        }

//		List<WurstType> newTypes = node.getTypeArgs().stream()
//				.map((TypeExpr te) -> te.attrTyp().dynamic())
//				.collect(Collectors.toList());

        return replaceTypeVars(typeParams);
    }


    protected VariableBinding matchTypeParams(List<WurstTypeBoundTypeParam> list, List<WurstTypeBoundTypeParam> list2, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {

        if (list.size() != list2.size()) {
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            WurstType thisTp = list.get(i).normalize();
            WurstType otherTp = list2.get(i).normalize();
            mapping = thisTp.matchTypes(otherTp, location, mapping, variablePosition);
            if (mapping == null) {
                return null;
            }
        }
        return mapping;
    }

    @Override
    public boolean allowsDynamicDispatch() {
        // dynamic dispatch is possible if this is not a static reference
        return !isStaticRef();
    }

    /**
     * get the name links available in this class or interface
     * This includes inherited names
     */
    public ImmutableMultimap<String, DefLink> nameLinks() {
        ImmutableMultimap<String, DefLink> res = getDef().attrNameLinks();
        VariableBinding binding = getTypeArgBinding();
        if (!binding.isEmpty()) {
            // OPT maybe cache this
            ImmutableMultimap.Builder<String, DefLink> resBuilder = ImmutableMultimap.builder();
            for (Map.Entry<String, DefLink> e : res.entries()) {
                resBuilder.put(e.getKey(), e.getValue().withTypeArgBinding(getDef(), binding));
            }
            return resBuilder.build();
        }
        return res;
    }

    public ImmutableCollection<DefLink> nameLinks(String name) {
        return nameLinks().get(name);
    }

    @Override
    public void addMemberMethods(Element node, String name,
                                 List<FuncLink> result) {
        for (DefLink defLink : nameLinks(name)) {
            if (defLink instanceof FuncLink) {
                FuncLink f = (FuncLink) defLink;
                if (f.getVisibility().isPublic()) {
                    result.add(f);
                } else if (f.getVisibility().isInherited()) {
                    // for protected members:
                    NamedScope def = getDef();
                    if (def != null && node.attrNearestPackage() != def.attrNearestPackage()) {
                        // if in different package, check if we are in a subclass:
                        ClassDef nearestClass = node.attrNearestClassDef();
                        if (nearestClass == null
                                || !nearestClass.attrTypC().isSubtypeOf(this, node)) {
                            // if not in a subclass, change to not visible
                            f = f.withVisibility(Visibility.PROTECTED_OTHER);
                        }
                    }
                    result.add(f);
                }
            }
        }
    }

    @Override
    public Stream<FuncLink> getMemberMethods(Element node) {
        return nameLinks().values().stream()
                .filter(n -> {
                    WurstType receiverType = n.getReceiverType();
                    return n instanceof FuncLink
                            && receiverType != null;
                }).map(n -> (FuncLink) n);
    }

    @Override
    public boolean isNestedInside(WurstType other) {
        if (other instanceof WurstTypeNamedScope) {
            WurstTypeNamedScope wtns = (WurstTypeNamedScope) other;
            NamedScope scope = wtns.getDef();
            Element node = this.getDef();
            while (node != null) {
                if (node == scope) {
                    return true;
                }
                node = node.getParent();
            }
        }
        return false;
    }

    @Override
    protected boolean isNullable() {
        return true;
    }


}
