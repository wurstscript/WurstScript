package de.peeeq.wurstscript.types;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.AstElement;
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

	public boolean isStaticRef() {
		return isStaticRef;
	}

	@Override
	public boolean isSubtypeOf(PscriptType obj, AstElement location) {
		if (obj instanceof PscriptTypeTypeParam) {
			return false;
		}
		if (obj instanceof PscriptTypeNamedScope) {
			PscriptTypeNamedScope other = (PscriptTypeNamedScope) obj;
			if (other.getDef().equals(this.getDef())) {
				if (this.typeParameters.size() != other.typeParameters.size()) {
					return false;
				}
				for (int i = 0; i < typeParameters.size(); i++) {
					if (!typeParameters.get(i).isSubtypeOf(other.typeParameters.get(i), location)) {
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
	


	public Map<TypeParamDef, PscriptType> getTypeArgBinding() {
		if (getDef() instanceof AstElementWithTypeParameters) {
			Map<TypeParamDef, PscriptType> result = Maps.newHashMap();
			AstElementWithTypeParameters def = (AstElementWithTypeParameters) getDef();
			for (int i=0; i<typeParameters.size(); i++) {
				PscriptType t = typeParameters.get(i);
				TypeParamDef tDef = def.getTypeParameters().get(i);
				result.put(tDef, t);
			}
			return result ;
		}
		return super.getTypeArgBinding();
	}

	public PscriptType setTypeArgs(Map<TypeParamDef, PscriptType> typeParamBounds) {
		List<PscriptType> newTypes = Lists.newArrayList();
		for (PscriptType t : typeParameters) {
			newTypes.add(t.setTypeArgs(typeParamBounds));
		}
		return replaceTypeVars(newTypes);
	}

	abstract public PscriptType replaceTypeVars(List<PscriptType> newTypes);

}