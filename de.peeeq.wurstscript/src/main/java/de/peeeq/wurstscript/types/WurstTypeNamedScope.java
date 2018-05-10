package de.peeeq.wurstscript.types;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.DefLink;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.NameLinkType;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class WurstTypeNamedScope extends WurstType {

    private final boolean isStaticRef;
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
    public boolean isSubtypeOfIntern(WurstType obj, @Nullable Element location) {
        if (obj instanceof WurstTypeTypeParam) {
            return false;
        }
        if (obj instanceof WurstTypeNamedScope) {
            WurstTypeNamedScope other = (WurstTypeNamedScope) obj;
            if (other.getDef() == this.getDef()) {
                return checkTypeParametersEqual(getTypeParameters(), other.getTypeParameters(), location);
            }
        }
        return false;
    }

    public List<WurstTypeBoundTypeParam> getTypeParameters() {
        return typeParameters;
    }


    @Nullable Map<TypeParamDef, WurstType> cache_typeParamBounds;

    private Map<TypeParamDef, WurstType> getTypeParamBounds() {
        Map<TypeParamDef, WurstType> cache = cache_typeParamBounds;
        if (cache == null) {
            cache_typeParamBounds = cache = Maps.newLinkedHashMap();
            NamedScope def = getDef();
            if (def instanceof AstElementWithTypeParameters) {
                AstElementWithTypeParameters wtp = (AstElementWithTypeParameters) def;
                TypeParamDefs tps = wtp.getTypeParameters();
                for (int index = 0; index < typeParameters.size(); index++) {
                    cache.put(tps.get(index), typeParameters.get(index));
                }
            }
        }
        return cache;
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

//	@Override
//	public  PscriptType replaceBoundTypeVars(PscriptType t) {
//		if (t instanceof PscriptTypeTypeParam) {
//			PscriptTypeTypeParam tpt = (PscriptTypeTypeParam) t;
//			PscriptType s = getTypeParamBounds().get(tpt.getDef());
//			if (s != null) {
//				return s;
//			}
//		} else if (t instanceof PscriptTypeNamedScope) {
//			PscriptTypeNamedScope ns = (PscriptTypeNamedScope) t;
//			return ns.replaceTypeVars(getTypeParamBounds());
//		}
//		return t;
//	}


    @Override
    public Map<TypeParamDef, WurstTypeBoundTypeParam> getTypeArgBinding() {

        NamedScope def2 = getDef();
        if (def2 instanceof AstElementWithTypeParameters) {
            AstElementWithTypeParameters def = (AstElementWithTypeParameters) def2;
            Map<TypeParamDef, WurstTypeBoundTypeParam> result = Maps.newLinkedHashMap();
            for (int i = 0; i < typeParameters.size(); i++) {
                WurstType t = typeParameters.get(i);
                TypeParamDef tDef = def.getTypeParameters().get(i);
                result.put(tDef, new WurstTypeBoundTypeParam(tDef, t, def2));
            }
            if (def instanceof ClassDef) {
                ClassDef c = (ClassDef) def;
                c.attrExtendedClass(); // to protect against the case where interface extends itself

                // type binding for extended class
                result.putAll(c.getExtendedClass().attrTyp()
                        .getTypeArgBinding());
                // type binding for implemented interfaces:
                for (WurstTypeInterface i : c.attrImplementedInterfaces()) {
                    result.putAll(i.getTypeArgBinding());
                }
            } else if (def instanceof InterfaceDef) {
                InterfaceDef i = (InterfaceDef) def;
                // type binding for implemented interfaces:
                for (WurstTypeInterface ii : i.attrExtendedInterfaces()) {
                    result.putAll(ii.getTypeArgBinding());
                }
            }
            normalizeTypeArgsBinding(result);
            return result;
        }
        return super.getTypeArgBinding();
    }

    private void normalizeTypeArgsBinding(Map<TypeParamDef, WurstTypeBoundTypeParam> b) {
        List<TypeParamDef> keys = Lists.newArrayList(b.keySet());
        for (TypeParamDef p : keys) {
            WurstTypeBoundTypeParam t = b.get(p);
            b.put(p, normalizeType(t, b));
        }
    }

    private WurstTypeBoundTypeParam normalizeType(WurstTypeBoundTypeParam bt, Map<TypeParamDef, WurstTypeBoundTypeParam> b) {
        return bt.setTypeArgs(b);
    }

    @Override
    public WurstType setTypeArgs(Map<TypeParamDef, WurstTypeBoundTypeParam> typeParamBounds) {
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


    protected boolean checkTypeParametersEqual(List<WurstTypeBoundTypeParam> list, List<WurstTypeBoundTypeParam> list2, @Nullable Element location) {
        if (list.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            WurstType thisTp = list.get(i).normalize();
            WurstType otherTp = list2.get(i).normalize();
            if (otherTp instanceof WurstTypeTypeParam) {
                // free type params can later be bound to the right type
                continue;
            }
            if (!thisTp.equalsType(otherTp, location)) {
                return false;
            }
        }
        return true;
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
            Map<TypeParamDef, WurstTypeBoundTypeParam> typeArgBinding = getTypeArgBinding();
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