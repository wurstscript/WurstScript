package de.peeeq.wurstscript.types;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithTypeParameters;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.TypeParamDefs;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.NameLinkType;

public abstract class WurstTypeNamedScope extends WurstType {

	private final boolean isStaticRef;
	private final List<WurstType> typeParameters;
	
	
	public WurstTypeNamedScope(List<WurstType> typeParameters, boolean isStaticRef) {
		this.isStaticRef = isStaticRef;
		this.typeParameters = typeParameters;
	}

	public WurstTypeNamedScope(List<WurstType> typeParameters) {
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
	public boolean isSubtypeOfIntern(WurstType obj, @Nullable AstElement location) {
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

	public List<WurstType> getTypeParameters() {
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
		String s = "<";
		for (int i=0; i<typeParameters.size(); i++) {
			if (i > 0) {
				s += ", ";
			}
			s += typeParameters.get(i).getName();
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
	public Map<TypeParamDef, WurstType> getTypeArgBinding() {
		
		NamedScope def2 = getDef();
		if (def2 instanceof AstElementWithTypeParameters) {
			AstElementWithTypeParameters def = (AstElementWithTypeParameters) def2;
			Map<TypeParamDef, WurstType> result = Maps.newLinkedHashMap();
			for (int i=0; i<typeParameters.size(); i++) {
				WurstType t = typeParameters.get(i);
				TypeParamDef tDef = def.getTypeParameters().get(i);
				result.put(tDef, t);
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
			return result ;
		}
		return super.getTypeArgBinding();
	}

	private void normalizeTypeArgsBinding(Map<TypeParamDef, WurstType> b) {
		List<TypeParamDef> keys = Lists.newArrayList(b.keySet());
		for (TypeParamDef p : keys) {
			WurstType t = b.get(p);
			b.put(p, normalizeType(t,b));
		}
	}

	private WurstType normalizeType(WurstType t, Map<TypeParamDef, WurstType> b) {
		t = t.normalize();
		if (t instanceof WurstTypeTypeParam) {
			WurstTypeTypeParam tp = (WurstTypeTypeParam) t;
			TypeParamDef tpDef = tp.getDef();
			if (b.containsKey(tpDef)) {
				WurstType t2 = b.get(tpDef);
				if (t != t2) {
					return normalizeType(t2, b);
				}
			}
		}
		return t;
	}

	@Override
	public WurstType setTypeArgs(Map<TypeParamDef, WurstType> typeParamBounds) {
		List<WurstType> newTypes = Lists.newArrayList();
		for (WurstType t : typeParameters) {
			newTypes.add(t.setTypeArgs(typeParamBounds));
		}
		return replaceTypeVars(newTypes);
	}

	abstract public WurstType replaceTypeVars(List<WurstType> newTypes);

	
	
	protected boolean checkTypeParametersEqual(List<WurstType> tps1, List<WurstType> tps2, @Nullable AstElement location) {
		if (tps1.size() != tps2.size()) {
			return false;
		}
		for (int i=0; i<tps1.size(); i++) {
			WurstType thisTp = tps1.get(i);
			WurstType otherTp = tps2.get(i);
			if (otherTp instanceof WurstTypeFreeTypeParam
					|| otherTp instanceof WurstTypeTypeParam) {
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
	public void addMemberMethods(AstElement node, String name,
			List<NameLink> result) {
		NamedScope scope = getDef();
		if (scope instanceof ModuleDef) {
			// cannot access functions from outside of module 
		} else if (scope != null) {
			for (NameLink n : scope.attrNameLinks().get(name)) {
				WurstType receiverType = n.getReceiverType();
				if (n.getType() == NameLinkType.FUNCTION
						&& receiverType != null
						&& receiverType.isSupertypeOf(this, node)) {
					result.add(n.hidingPrivateAndProtected());
				}
			}
		}
	}
	
	@Override
	public Stream<NameLink> getMemberMethods(AstElement node) {
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
					.map(n -> n.hidingPrivateAndProtected());
		}
		return Collections.<NameLink>emptyList().stream();
	}
	
	@Override
	public boolean isNestedInside(WurstType other) {
		if (other instanceof WurstTypeNamedScope) {
			WurstTypeNamedScope wtns = (WurstTypeNamedScope) other;
			NamedScope scope = wtns.getDef();
			AstElement node = this.getDef();
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