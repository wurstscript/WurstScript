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
	public boolean isSubtypeOf(WurstType obj, AstElement location) {
		if (obj instanceof WurstTypeBoundTypeParam) {
			WurstTypeBoundTypeParam b = (WurstTypeBoundTypeParam) obj;
			return this.isSubtypeOf(b.getBaseType(), location);
		}
		
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

	public WurstType getTypeParameterBinding(TypeParamDef def) {
		WurstType t = getTypeParamBounds().get(def);
		return t != null ? t : WurstTypeUnknown.instance();
	}
	
	
	Map<TypeParamDef, WurstType> cache_typeParamBounds;
	private Map<TypeParamDef, WurstType> getTypeParamBounds() {
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
		String s = "<";
		for (int i=0; i<typeParameters.size(); i++) {
			if (i > 0) {
				s += ", ";
			}
			s += typeParameters.get(i);
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
	


	public Map<TypeParamDef, WurstType> getTypeArgBinding() {
		if (getDef() instanceof AstElementWithTypeParameters) {
			Map<TypeParamDef, WurstType> result = Maps.newHashMap();
			AstElementWithTypeParameters def = (AstElementWithTypeParameters) getDef();
			for (int i=0; i<typeParameters.size(); i++) {
				WurstType t = typeParameters.get(i);
				TypeParamDef tDef = def.getTypeParameters().get(i);
				result.put(tDef, t);
			}
			return result ;
		}
		return super.getTypeArgBinding();
	}

	public WurstType setTypeArgs(Map<TypeParamDef, WurstType> typeParamBounds) {
		List<WurstType> newTypes = Lists.newArrayList();
		for (WurstType t : typeParameters) {
			newTypes.add(t.setTypeArgs(typeParamBounds));
		}
		return replaceTypeVars(newTypes);
	}

	abstract public WurstType replaceTypeVars(List<WurstType> newTypes);

	
	
	protected boolean checkTypeParametersEqual(List<WurstType> tps1, List<WurstType> tps2, AstElement location) {
		if (tps1.size() != tps2.size()) {
			return false;
		}
		for (int i=0; i<tps1.size(); i++) {
			WurstType thisTp = tps1.get(i);
			WurstType otherTp = tps2.get(i);
			if (!thisTp.equalsType(otherTp, location)) {
				return false;
			}
		}
		return true;
	}
	
}