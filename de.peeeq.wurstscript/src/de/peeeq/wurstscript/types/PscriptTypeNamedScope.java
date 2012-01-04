package de.peeeq.wurstscript.types;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.AstElementWithTypeParameters;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.TypeParamDefs;

public abstract class PscriptTypeNamedScope extends PscriptType {

	private final boolean isStaticRef;
	private final List<PscriptType> typeParameters;
	
	public PscriptTypeNamedScope(boolean isStaticRef) {
		this.isStaticRef = isStaticRef;
		typeParameters = Lists.newArrayList();
	}
	
	public PscriptTypeNamedScope(List<PscriptType> typeParameters) {
		isStaticRef = false; // when there are type parameters this cannot be a static reference
		this.typeParameters = typeParameters;
	}

	@Override
	public String getName() {
		return getDef().getName();
	}

	public abstract NamedScope getDef();

	@Override
	public String getFullName() {
		return getName();
	}

	@Override
	public String printJass() {
		// classes are just translated to integers:
		return "integer";
	}
	
	public boolean isStaticRef() {
		return isStaticRef;
	}

	@Override
	public boolean isSubtypeOf(PscriptType obj) {
		if (obj instanceof PscriptTypeTypeParam) {
			return true;
		}
		if (obj instanceof PscriptTypeNamedScope) {
			PscriptTypeNamedScope other = (PscriptTypeNamedScope) obj;
			if (other.getDef().equals(this.getDef())) {
				if (this.typeParameters.size() != other.typeParameters.size()) {
					return false;
				}
				for (int i = 0; i < typeParameters.size(); i++) {
					if (!typeParameters.get(i).isSubtypeOf(other.typeParameters.get(i))) {
						// this means covariant type parameters, but this should be no problem as
						// wurst has no (real) subtypes for classtypes
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	public List<PscriptType> getTypeParameters() {
		return typeParameters;
	}

	public PscriptType getTypeParameterBinding(TypeParamDef def) {
		PscriptType t = getTypeParamBounds().get(def);
		return t != null ? t : PScriptTypeUnknown.instance();
	}
	
	
	Map<TypeParamDef, PscriptType> cache_typeParamBounds;
	private Map<TypeParamDef, PscriptType> getTypeParamBounds() {
		if (cache_typeParamBounds == null) {
			cache_typeParamBounds = Maps.newHashMap();
			if (getDef() instanceof AstElementWithTypeParameters) {
				AstElementWithTypeParameters wtp = (AstElementWithTypeParameters) getDef();
				TypeParamDefs tps = wtp.getTypeParameters();
				for (int index = 0; index < typeParameters.size(); index++) {
					cache_typeParamBounds.put(tps.get(index), typeParameters.get(index));
				}
			}
		}
		return cache_typeParamBounds;
	}
	
	protected String printTypeParams() {
		if (typeParameters.size() == 0) {
			return "";
		}
		String s = "{";
		for (int i=0; i<typeParameters.size(); i++) {
			if (i > 0) {
				s += ", ";
			}
			s += typeParameters.get(i);
		}
		return s + "}";
	}
	
	@Override
	public  PscriptType replaceBoundTypeVars(PscriptType t) {
		if (t instanceof PscriptTypeTypeParam) {
			PscriptTypeTypeParam tpt = (PscriptTypeTypeParam) t;
			PscriptType s = getTypeParamBounds().get(tpt.getDef());
			if (s != null) {
				return s;
			}
		} else if (t instanceof PscriptTypeNamedScope) {
			PscriptTypeNamedScope ns = (PscriptTypeNamedScope) t;
			return ns.replaceTypeVars(getTypeParamBounds());
		}
		return t;
	}

	private PscriptType replaceTypeVars(Map<TypeParamDef, PscriptType> typeParamBounds) {
		List<PscriptType> newTypes = Lists.newArrayList();
		for (PscriptType t : typeParameters) {
			if (t instanceof PscriptTypeTypeParam) {
				PscriptType r = typeParamBounds.get(((PscriptTypeTypeParam) t).getDef());
				if (r != null) {
					newTypes.add(r);
				} else {
					newTypes.add(t);
				}
			} else if (t instanceof PscriptTypeNamedScope) {
				PscriptTypeNamedScope ns = (PscriptTypeNamedScope) t;
				newTypes.add(ns.replaceTypeVars(typeParamBounds));				
			} else {
				newTypes.add(t);
			}
		}
		return replaceTypeVars(newTypes);
	}

	abstract public PscriptType replaceTypeVars(List<PscriptType> newTypes);

}