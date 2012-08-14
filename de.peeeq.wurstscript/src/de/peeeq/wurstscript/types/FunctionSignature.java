package de.peeeq.wurstscript.types;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.TypeParamDef;

public class FunctionSignature {
	public static FunctionSignature empty = new FunctionSignature(Collections.<WurstType>emptyList(), WurstTypeUnknown.instance());
	private List<WurstType> paramTypes;
	private WurstType returnType;
	
	
	public FunctionSignature(List<WurstType> paramTypes, WurstType returnType) {
		this.paramTypes = paramTypes;
		this.returnType = returnType;
	}
	
	
	public List<WurstType> getParamTypes() {
		return paramTypes;
	}
	public WurstType getReturnType() {
		return returnType;
	}


	public FunctionSignature setTypeArgs(Map<TypeParamDef, WurstType> typeArgBinding) {
		WurstType r2 = returnType.setTypeArgs(typeArgBinding);
		List<WurstType> pt2 = Lists.newArrayList();
		for (WurstType p : paramTypes) {
			pt2.add(p.setTypeArgs(typeArgBinding));
		}
		return new FunctionSignature(pt2, r2);
	}
	
	
}
