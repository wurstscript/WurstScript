package de.peeeq.wurstscript.types;

import java.util.List;

import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.NameLinkType;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;

public class WurstTypeClosure extends WurstType {

	private final List<WurstType> paramTypes;
	private final WurstType returnType;

	public WurstTypeClosure(List<WurstType> paramTypes, WurstType returnType) {
		this.paramTypes = paramTypes;
		this.returnType = returnType;
	}

	@Override
	public boolean isSubtypeOfIntern(WurstType other, AstElement location) {
		if (other instanceof WurstTypeClosure) {
			WurstTypeClosure o = (WurstTypeClosure) other;
			if (paramTypes.size() != o.paramTypes.size()) {
				return false;
			}
			// contravariant parameter types
			for (int i=0; i<paramTypes.size(); i++) {
				if (!o.paramTypes.get(i).isSubtypeOf(paramTypes.get(i), location)) {
					return false;
				}
			}
			// covariant return types
			if (!returnType.isSubtypeOf(o.returnType, location)) {
				return false;
			}
			return true;
		} else {
			FunctionSignature abstractMethod = getAbstractMethodSignature(other);
			if (abstractMethod != null) {
				return closureImplementsAbstractMethod(abstractMethod, location);
			}
		}
		return false;
	}


	private boolean closureImplementsAbstractMethod(FunctionSignature abstractMethod,
			AstElement location) {
		if (paramTypes.size() != abstractMethod.getParamTypes().size()) {
			return false;
		}
		
		// contravariant parameter types
		for (int i=0; i<paramTypes.size(); i++) {
			if (!abstractMethod.getParamTypes().get(i).isSubtypeOf(paramTypes.get(i), location)) {
				return false;
			}
		}
		// covariant return types
		if (!returnType.isSubtypeOf(abstractMethod.getReturnType(), location)) {
			return false;
		}
		return true;
	}

	private FunctionSignature getAbstractMethodSignature(Multimap<String, NameLink> nameLinks) {
		NameLink abstractMethod = findAbstractMethod(nameLinks);
		if (abstractMethod != null) {
			return FunctionSignature.fromNameLink(abstractMethod);
		}
		return null;
	}

	public NameLink findAbstractMethod(Multimap<String, NameLink> nameLinks) {
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
	
	public NameLink findAbstractMethod(WurstType type) {
		if (type instanceof WurstTypeClassOrInterface) {
			WurstTypeClassOrInterface t = (WurstTypeClassOrInterface) type;
			return findAbstractMethod(t.getDef().attrNameLinks());
		}
		return null;
	}
	
	
	public FunctionSignature getAbstractMethodSignature(WurstType type) {
		if (type instanceof WurstTypeClassOrInterface) {
			WurstTypeClassOrInterface ct = (WurstTypeClassOrInterface) type;
			FunctionSignature sig = getAbstractMethodSignature(ct.getDef().attrNameLinks());
			sig = sig.setTypeArgs(ct.getTypeArgBinding());
			return sig;
		}
		return null;
	}

	@Override
	public String getName() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		boolean first = true;
		for (WurstType t : paramTypes) {
			if (!first) {
				sb.append(", ");
			}
			sb.append(t.getName());
			first = false;
		}
		sb.append(") => ");
		sb.append(returnType.getName());
		return sb.toString();
	}

	@Override
	public String getFullName() {
		return getName();
	}

	@Override
	public ImType imTranslateType() {
		return WurstTypeInt.instance().imTranslateType();
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImIntVal(0);
	}

	

}
