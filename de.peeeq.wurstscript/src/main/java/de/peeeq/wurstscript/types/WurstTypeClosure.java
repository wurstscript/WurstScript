package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.AttrClosureAbstractMethod;
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
	public boolean isSubtypeOfIntern(WurstType other, Element location) {
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
		} else if (other instanceof WurstTypeCode) {
			return paramTypes.size() == 0;
		} else {
			FunctionSignature abstractMethod = AttrClosureAbstractMethod.getAbstractMethodSignature(other);
			if (abstractMethod != null) {
				return closureImplementsAbstractMethod(abstractMethod, location);
			}
		}
		return false;
	}


	private boolean closureImplementsAbstractMethod(FunctionSignature abstractMethod,
			Element location) {
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
			// void return type accepts every other returntype
			if (!(abstractMethod.getReturnType() instanceof WurstTypeVoid)) {
				return false;
			}
		}
		return true;
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
		sb.append(") -> ");
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

	public List<WurstType> getParamTypes() {
		return paramTypes;
	}
	
	public WurstType getReturnType() {
		return returnType;
	}

}
