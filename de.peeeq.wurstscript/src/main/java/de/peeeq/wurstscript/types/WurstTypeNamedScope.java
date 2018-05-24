package de.peeeq.wurstscript.types;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.*;
import fj.data.TreeMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;
import java.util.stream.Stream;

public abstract class WurstTypeNamedScope extends WurstType {

    private final boolean isStaticRef;
    // TODO change this to a list of TypeParamDef and add typeMapping?
    private final List<WurstTypeBoundTypeParam> typeParameters;
    private final TreeMap<TypeParamDef, WurstTypeBoundTypeParam> typeMapping;



    public WurstTypeNamedScope(List<WurstTypeBoundTypeParam> typeParameters, boolean isStaticRef) {
        this.isStaticRef = isStaticRef;
        this.typeParameters = typeParameters;
        this.typeMapping = emptyMapping();
    }

    public WurstTypeNamedScope(List<WurstTypeBoundTypeParam> typeParameters) {
        this.isStaticRef = false;
        this.typeParameters = typeParameters;
        this.typeMapping = emptyMapping();
    }


    public WurstTypeNamedScope(boolean isStaticRef) {
        this.isStaticRef = isStaticRef;
        this.typeParameters = Collections.emptyList();
        this.typeMapping = emptyMapping();
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
    @Nullable TreeMap<TypeParamDef, WurstTypeBoundTypeParam> matchAgainstSupertypeIntern(WurstType obj, @Nullable Element location, Collection<TypeParamDef> typeParams, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping) {
        if (obj instanceof WurstTypeNamedScope) {
            WurstTypeNamedScope other = (WurstTypeNamedScope) obj;
            if (other.getDef() == this.getDef()) {
                return matchTypeParams(getTypeParameters(), other.getTypeParameters(), location, typeParams, mapping);
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
    public TreeMap<TypeParamDef, WurstTypeBoundTypeParam> getTypeArgBinding() {
        return typeMapping;
    }

    private WurstTypeBoundTypeParam normalizeType(WurstTypeBoundTypeParam bt, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> b) {
        return bt.setTypeArgs(b);
    }

    @Override
    public WurstType setTypeArgs(TreeMap<TypeParamDef, WurstTypeBoundTypeParam> typeParamBounds) {
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


    protected @Nullable TreeMap<TypeParamDef, WurstTypeBoundTypeParam> matchTypeParams(List<WurstTypeBoundTypeParam> list, List<WurstTypeBoundTypeParam> list2, @Nullable Element location, Collection<TypeParamDef> typeParams, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping) {
        if (list.size() != list2.size()) {
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            WurstType thisTp = list.get(i).normalize();
            WurstType otherTp = list2.get(i).normalize();
            mapping = thisTp.matchTypes(otherTp, location, typeParams, mapping);
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

    @Override
    public void addMemberMethods(Element node, String name,
                                 List<FuncLink> result) {
        NamedScope scope = getDef();
        if (scope instanceof ModuleDef) {
            // cannot access functions from outside of module
        } else if (scope != null) {
            TreeMap<TypeParamDef, WurstTypeBoundTypeParam> typeArgBinding = getTypeArgBinding();
            for (DefLink n : scope.attrNameLinks().get(name)) {
                WurstType receiverType = n.getReceiverType();
                if (n instanceof FuncLink
                        && receiverType != null
                        && receiverType.isSupertypeOf(this, node)) {
                    FuncLink f = (FuncLink) n;
                    result.add(f.hidingPrivateAndProtected().withTypeArgBinding(node, typeArgBinding));
                }
            }
        }
    }

    @Override
    public Stream<NameLink> getMemberMethods(Element node) {
        NamedScope scope = getDef();
        if (scope instanceof ModuleDef) {
            // cannot access functions from outside of module
        } else if (scope != null) {
            return scope.attrNameLinks()
                    .values()
                    .stream()
                    .filter(n -> {
                        WurstType receiverType = n.getReceiverType();
                        return n.getType() == NameLinkType.FUNCTION
                                && receiverType != null
                                && receiverType.isSupertypeOf(this, node);
                    })
                    .map(NameLink::hidingPrivateAndProtected);
        }
        return Stream.empty();
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


}